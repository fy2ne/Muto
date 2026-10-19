package me.fy2ne.muto.engine.mixin;

import me.fy2ne.muto.MutoLog;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.Config;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Registers a dynamically added mod's mixin configurations with the global Mixin service.
 *
 * <p>Mixin only supports weaving at class-definition time, so a configuration registered here
 * applies to classes the JVM defines <em>after</em> the reload. Classes that were already defined -
 * and already processed by Mixin - cannot be re-woven without a JVM restart. Attempting to do so by
 * feeding their "pre-mixin" bytes back through {@code MixinTransformer.transformClass} corrupts
 * them with {@code InvalidMixinException: ... @Overwrite is required by the parent configuration},
 * and the transformer is not reachable via reflection on a modular JVM anyway. This class
 * therefore never retransforms already-loaded classes.
 */
public final class DynamicMixinEngine {
    private DynamicMixinEngine() {}

    /**
     * Registers every mixin configuration declared by {@code jarPath} with the global Mixin
     * service. Registered configurations take effect for classes loaded after this call.
     *
     * @param jarPath the newly added or updated mod jar
     * @return the number of configurations that were registered
     */
    public static synchronized int registerDynamicMixins(Path jarPath) {
        if (jarPath == null) return 0;

        List<String> configs = extractConfigs(jarPath);
        if (configs.isEmpty()) {
            return 0;
        }

        int registered = 0;
        for (String cfg : configs) {
            try {
                Mixins.addConfiguration(cfg);
                registered++;
                MutoLog.info("registered dynamic mixin config: {}", cfg);
            } catch (Throwable t) {
                MutoLog.warn("error registering mixin config {}: {}", cfg, t.getMessage());
            }
        }

        Set<String> targets = resolveTargets(configs);
        if (targets.isEmpty()) {
            targets = scanJarMixinTargets(jarPath);
            if (!targets.isEmpty()) {
                MutoLog.info("resolved {} mixin targets from bytecode scan for {}", targets.size(), jarPath.getFileName());
            }
        }

        if (targets.isEmpty()) {
            MutoLog.info("no mixin targets discovered for configs in {}", jarPath.getFileName());
        } else {
            MutoLog.info("{} mixin target(s) registered for {} (applies to classes loaded after this reload)",
                    targets.size(), jarPath.getFileName());
        }

        return registered;
    }

    public static List<String> extractConfigs(Path jarPath) {
        List<String> configs = new ArrayList<>();
        try (JarFile jf = new JarFile(jarPath.toFile())) {
            JarEntry entry = jf.getJarEntry("fabric.mod.json");
            if (entry == null) return configs;

            try (var is = jf.getInputStream(entry);
                 var reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                com.google.gson.JsonObject root = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
                if (root.has("mixins")) {
                    var mixinsEl = root.get("mixins");
                    if (mixinsEl.isJsonArray()) {
                        for (var el : mixinsEl.getAsJsonArray()) {
                            if (el.isJsonPrimitive()) {
                                configs.add(el.getAsString());
                            } else if (el.isJsonObject() && el.getAsJsonObject().has("config")) {
                                configs.add(el.getAsJsonObject().get("config").getAsString());
                            }
                        }
                    }
                }
            }

            if (configs.isEmpty()) {
                var entries = jf.entries();
                while (entries.hasMoreElements()) {
                    var e = entries.nextElement();
                    String name = e.getName();
                    if (!name.contains("/") && name.endsWith(".mixins.json")) {
                        configs.add(name);
                    }
                }
            }
        } catch (Throwable t) {
            MutoLog.warn("failed parsing fabric.mod.json mixins from {}: {}", jarPath.getFileName(), t.getMessage());
        }
        return configs;
    }

    private static Set<String> resolveTargets(List<String> configs) {
        Set<String> targets = new HashSet<>();
        try {
            Set<Config> allConfigs = Mixins.getConfigs();
            if (allConfigs == null) return targets;

            for (Config cfg : allConfigs) {
                if (configs.contains(cfg.getName()) || configs.stream().anyMatch(c -> c.endsWith(cfg.getName()))) {
                    var iConfig = cfg.getConfig();
                    if (iConfig != null && iConfig.getTargets() != null) {
                        targets.addAll(iConfig.getTargets());
                    }
                }
            }
        } catch (Throwable t) {
            MutoLog.warn("error resolving target classes from mixin configs: {}", t.getMessage());
        }
        return targets;
    }

    public static Set<String> scanJarMixinTargets(Path jarPath) {
        Set<String> targets = new HashSet<>();
        try (JarFile jf = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jf.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class") && !entry.isDirectory()) {
                    try (var is = jf.getInputStream(entry)) {
                        org.objectweb.asm.ClassReader cr = new org.objectweb.asm.ClassReader(is);
                        cr.accept(new org.objectweb.asm.ClassVisitor(org.objectweb.asm.Opcodes.ASM9) {
                            @Override
                            public org.objectweb.asm.AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                                if ("Lorg/spongepowered/asm/mixin/Mixin;".equals(descriptor)) {
                                    return new org.objectweb.asm.AnnotationVisitor(org.objectweb.asm.Opcodes.ASM9) {
                                        @Override
                                        public void visit(String name, Object value) {
                                            if (value instanceof org.objectweb.asm.Type t) {
                                                targets.add(t.getClassName());
                                            } else if (value instanceof String s) {
                                                targets.add(s.replace('/', '.'));
                                            }
                                        }

                                        @Override
                                        public org.objectweb.asm.AnnotationVisitor visitArray(String name) {
                                            return new org.objectweb.asm.AnnotationVisitor(org.objectweb.asm.Opcodes.ASM9) {
                                                @Override
                                                public void visit(String n, Object v) {
                                                    if (v instanceof org.objectweb.asm.Type t) {
                                                        targets.add(t.getClassName());
                                                    } else if (v instanceof String s) {
                                                        targets.add(s.replace('/', '.'));
                                                    }
                                                }
                                            };
                                        }
                                    };
                                }
                                return null;
                            }
                        }, org.objectweb.asm.ClassReader.SKIP_CODE | org.objectweb.asm.ClassReader.SKIP_DEBUG | org.objectweb.asm.ClassReader.SKIP_FRAMES);
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable t) {
            MutoLog.warn("error scanning jar mixin targets for {}: {}", jarPath.getFileName(), t.getMessage());
        }
        return targets;
    }
}
