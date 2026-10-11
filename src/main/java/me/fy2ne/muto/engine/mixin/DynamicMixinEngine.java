package me.fy2ne.muto.engine.mixin;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.engine.agent.MutoAgentBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.Config;

import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class DynamicMixinEngine {
    private static final MutoClassFileTransformer TRANSFORMER = new MutoClassFileTransformer();
    private static boolean transformerInstalled = false;

    private DynamicMixinEngine() {}

    public static synchronized int injectAndRetransform(Path jarPath) {
        if (jarPath == null) return 0;

        List<String> configs = extractConfigs(jarPath);
        if (configs.isEmpty()) {
            return 0;
        }

        for (String cfg : configs) {
            try {
                Mixins.addConfiguration(cfg);
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
            MutoLog.info("no loaded target classes discovered for configs in {}", jarPath.getFileName());
            return 0;
        }

        return retransformClasses(targets);
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

    private static int retransformClasses(Set<String> targetNames) {
        Instrumentation inst = MutoAgentBootstrap.getInstrumentation();
        if (inst == null) {
            MutoLog.warn("instrumentation unavailable, cannot trigger bytecode retransformation");
            return 0;
        }

        if (!inst.isRetransformClassesSupported()) {
            MutoLog.warn("jvm does not support retransformClasses");
            return 0;
        }

        if (!transformerInstalled) {
            inst.addTransformer(TRANSFORMER, true);
            transformerInstalled = true;
            MutoLog.info("muto classfile transformer installed");
        }

        TRANSFORMER.addTargets(targetNames);

        Class<?>[] loaded = inst.getAllLoadedClasses();
        List<Class<?>> matched = new ArrayList<>();
        for (Class<?> clazz : loaded) {
            if (targetNames.contains(clazz.getName()) && inst.isModifiableClass(clazz)) {
                matched.add(clazz);
            }
        }

        if (matched.isEmpty()) {
            MutoLog.info("none of {} mixin target classes are currently loaded", targetNames.size());
            return 0;
        }

        try {
            MutoLog.info("retransforming {} target classes for dynamic mixin injection...", matched.size());
            inst.retransformClasses(matched.toArray(new Class<?>[0]));
            MutoLog.info("retransformation complete: {} classes updated in-memory", matched.size());
            return matched.size();
        } catch (Throwable err) {
            MutoLog.warn("batch retransformation failed ({}), attempting per-class isolated retransformation...", err.getMessage());
            int successCount = 0;
            for (Class<?> c : matched) {
                try {
                    inst.retransformClasses(c);
                    successCount++;
                    MutoLog.info("retransformed {} successfully", c.getName());
                } catch (Throwable singleErr) {
                    MutoLog.warn("skipping schema-restricted target {}: {}", c.getName(), singleErr.getMessage());
                }
            }
            MutoLog.info("isolated retransformation finished: {}/{} classes updated", successCount, matched.size());
            return successCount;
        }
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
