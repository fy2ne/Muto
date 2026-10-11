package me.fy2ne.muto.engine.knot;

import me.fy2ne.muto.MutoLog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;

public final class KnotInjector {
    private static Object cachedDelegate;
    private static Method addCodeSourceMethod;
    private static Method getMixinTransformerMethod;

    private KnotInjector() {}

    public static synchronized java.util.List<Path> injectWithNested(Path jarPath) {
        java.util.List<Path> all = new java.util.ArrayList<>();
        if (jarPath == null || !java.nio.file.Files.exists(jarPath)) return all;

        if (inject(jarPath)) {
            all.add(jarPath);
        }

        java.util.List<Path> nested = extractJijJars(jarPath);
        for (Path child : nested) {
            if (inject(child)) {
                all.add(child);
                MutoLog.info("injected nested JiJ jar: {}", child.getFileName());
            }
        }
        return all;
    }

    private static java.util.List<Path> extractJijJars(Path outerJar) {
        java.util.List<Path> list = new java.util.ArrayList<>();
        try {
            if (outerJar.getParent() != null) {
                Path legacyDir = outerJar.getParent().resolve(".muto_jij");
                if (java.nio.file.Files.exists(legacyDir)) {
                    try (var s = java.nio.file.Files.walk(legacyDir)) {
                        s.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                            try { java.nio.file.Files.deleteIfExists(p); } catch (Exception ignored) {}
                        });
                    } catch (Exception ignored) {}
                }
            }

            Path gameDir = net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir();
            Path targetDir = gameDir.resolve(".cache").resolve("muto_jij").normalize();
            java.nio.file.Files.createDirectories(targetDir);

            try (java.util.jar.JarFile jf = new java.util.jar.JarFile(outerJar.toFile())) {
                var entries = jf.entries();
                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.startsWith("META-INF/jars/") && name.endsWith(".jar") && !entry.isDirectory()) {
                        String fileName = java.nio.file.Paths.get(name).getFileName().toString();
                        Path dest = targetDir.resolve(fileName);
                        if (!java.nio.file.Files.exists(dest) || java.nio.file.Files.size(dest) != entry.getSize()) {
                            try (var in = jf.getInputStream(entry)) {
                                java.nio.file.Files.copy(in, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                        list.add(dest);
                    }
                }
            }
        } catch (Throwable t) {
            MutoLog.warn("error extracting JiJ from {}: {}", outerJar.getFileName(), t.getMessage());
        }
        return list;
    }

    public static synchronized boolean inject(Path jarPath) {
        if (jarPath == null) return false;
        boolean codeSourceAdded = false;
        try {
            Object delegate = getDelegate();
            if (delegate != null) {
                if (addCodeSourceMethod == null) {
                    addCodeSourceMethod = delegate.getClass().getDeclaredMethod("addCodeSource", Path.class);
                    addCodeSourceMethod.setAccessible(true);
                }
                addCodeSourceMethod.invoke(delegate, jarPath);
                codeSourceAdded = true;
                MutoLog.info("knot classdelegate appended: {}", jarPath.getFileName());
            } else {
                MutoLog.warn("knot delegate not resolved, relying on URL forwarding for {}", jarPath.getFileName());
            }
        } catch (Throwable t) {
            MutoLog.warn("direct addCodeSource error for {}: {}", jarPath.getFileName(), t.getMessage());
        }

        boolean urlAdded = injectUrl(jarPath);
        return codeSourceAdded || urlAdded;
    }

    public static synchronized Object getMixinTransformer() {
        try {
            Object delegate = getDelegate();
            if (delegate == null) return null;

            if (getMixinTransformerMethod == null) {
                for (Method m : delegate.getClass().getDeclaredMethods()) {
                    if (m.getName().equals("getMixinTransformer") && m.getParameterCount() == 0) {
                        getMixinTransformerMethod = m;
                        getMixinTransformerMethod.setAccessible(true);
                        break;
                    }
                }
            }

            if (getMixinTransformerMethod != null) {
                return getMixinTransformerMethod.invoke(delegate);
            }

            // fallback to field reflection
            Field f = delegate.getClass().getDeclaredField("mixinTransformer");
            f.setAccessible(true);
            return f.get(delegate);
        } catch (Throwable t) {
            MutoLog.warn("failed resolving mixin transformer from knot: {}", t.getMessage());
            return null;
        }
    }

    public static ClassLoader getKnotClassLoader() {
        ClassLoader cl = KnotInjector.class.getClassLoader();
        if (isKnot(cl)) return cl;
        cl = Thread.currentThread().getContextClassLoader();
        if (isKnot(cl)) return cl;
        return KnotInjector.class.getClassLoader();
    }

    private static boolean isKnot(ClassLoader cl) {
        return cl != null && cl.getClass().getName().contains("KnotClassLoader");
    }

    public static synchronized Object getDelegate() {
        if (cachedDelegate != null) return cachedDelegate;

        ClassLoader cl = getKnotClassLoader();
        while (cl != null) {
            try {
                Method m = cl.getClass().getDeclaredMethod("getDelegate");
                m.setAccessible(true);
                cachedDelegate = m.invoke(cl);
                if (cachedDelegate != null) return cachedDelegate;
            } catch (Throwable ignored) {}

            try {
                Field f = cl.getClass().getDeclaredField("delegate");
                f.setAccessible(true);
                cachedDelegate = f.get(cl);
                if (cachedDelegate != null) return cachedDelegate;
            } catch (Throwable ignored) {}

            cl = cl.getParent();
        }
        return null;
    }

    private static boolean injectUrl(Path jarPath) {
        try {
            ClassLoader cl = getKnotClassLoader();
            URL url = jarPath.toUri().toURL();

            try {
                Method m = cl.getClass().getDeclaredMethod("addUrlFwd", URL.class);
                m.setAccessible(true);
                m.invoke(cl, url);
                MutoLog.info("knot url forwarded: {}", jarPath.getFileName());
                return true;
            } catch (NoSuchMethodException ignored) {}

            try {
                Field f = cl.getClass().getDeclaredField("urlLoader");
                f.setAccessible(true);
                Object urlLoader = f.get(cl);
                if (urlLoader != null) {
                    Method addUrl = urlLoader.getClass().getMethod("addURL", URL.class);
                    addUrl.setAccessible(true);
                    addUrl.invoke(urlLoader, url);
                    MutoLog.info("knot urlLoader appended: {}", jarPath.getFileName());
                    return true;
                }
            } catch (Throwable ignored) {}

            return fallbackUrlInject(jarPath);
        } catch (Throwable t) {
            MutoLog.warn("url injection failed for {}: {}", jarPath.getFileName(), t.getMessage());
            return false;
        }
    }

    private static boolean fallbackUrlInject(Path jarPath) {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Method addUrl = null;
            Class<?> cur = cl.getClass();
            while (cur != null && addUrl == null) {
                for (Method m : cur.getDeclaredMethods()) {
                    if ((m.getName().equals("addUrlFwd") || m.getName().equals("addURL")) && m.getParameterCount() == 1) {
                        addUrl = m;
                        break;
                    }
                }
                cur = cur.getSuperclass();
            }

            if (addUrl != null) {
                addUrl.setAccessible(true);
                addUrl.invoke(cl, jarPath.toUri().toURL());
                MutoLog.info("knot url fallback injected: {}", jarPath.getFileName());
                return true;
            }
        } catch (Throwable ex) {
            MutoLog.error("knot injection completely failed for {}: {}", jarPath, ex.getMessage());
        }
        return false;
    }
}
