package me.fy2ne.muto.engine;

import me.fy2ne.muto.MutoLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public final class MutoClassLoader extends URLClassLoader {
    private final List<Path> jarPaths;
    private final Set<String> modIds;
    private volatile boolean closed;

    public MutoClassLoader(List<Path> jars, Set<String> modIds, ClassLoader parent) {
        super(toUrls(collectJarsAndNested(jars)), parent);
        this.jarPaths = new ArrayList<>(jars);
        this.modIds = new HashSet<>(modIds);
        this.closed = false;
        MutoLog.info("muto child loader spawned with {} root jars", jars.size());
    }

    private static List<Path> collectJarsAndNested(List<Path> paths) {
        List<Path> result = new ArrayList<>(paths);
        Path cacheDir = Path.of(System.getProperty("java.io.tmpdir"), "muto_jij");
        try {
            Files.createDirectories(cacheDir);
        } catch (Exception ignored) {}

        for (Path p : paths) {
            try (java.util.jar.JarFile jf = new java.util.jar.JarFile(p.toFile())) {
                Enumeration<java.util.jar.JarEntry> entries = jf.entries();
                while (entries.hasMoreElements()) {
                    java.util.jar.JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (!entry.isDirectory() && name.endsWith(".jar") && (name.startsWith("META-INF/jars/") || name.startsWith("jars/"))) {
                        String cleanName = Path.of(name).getFileName().toString();
                        Path extracted = cacheDir.resolve(p.getFileName().toString() + "_" + cleanName);
                        if (!Files.exists(extracted)) {
                            try (InputStream is = jf.getInputStream(entry)) {
                                Files.copy(is, extracted, StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                        result.add(extracted);
                    }
                }
            } catch (Exception ignored) {}
        }
        return result;
    }

    private static URL[] toUrls(List<Path> paths) {
        List<URL> list = new ArrayList<>();
        for (Path p : paths) {
            try {
                list.add(p.toUri().toURL());
            } catch (Exception e) {
                MutoLog.error("bad jar path: {}", p, e);
            }
        }
        return list.toArray(new URL[0]);
    }

    public Set<String> modIds() {
        return Collections.unmodifiableSet(modIds);
    }

    public List<Path> jarPaths() {
        return Collections.unmodifiableList(jarPaths);
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            super.close();
            MutoLog.info("muto child loader closed ({} jars freed)", jarPaths.size());
        }
    }
}
