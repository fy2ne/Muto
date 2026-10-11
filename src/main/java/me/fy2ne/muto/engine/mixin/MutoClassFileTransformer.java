package me.fy2ne.muto.engine.mixin;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.engine.knot.KnotInjector;

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class MutoClassFileTransformer implements ClassFileTransformer {
    private final Set<String> targetClasses = ConcurrentHashMap.newKeySet();
    private Method transformMethod;

    public void addTargets(Set<String> targets) {
        if (targets != null) {
            targetClasses.addAll(targets);
        }
    }

    public void removeTargets(Set<String> targets) {
        if (targets != null) {
            targetClasses.removeAll(targets);
        }
    }

    public boolean hasTargets() {
        return !targetClasses.isEmpty();
    }

    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer
    ) {
        if (className == null || classfileBuffer == null) return null;

        String dotted = className.replace('/', '.');
        if (!targetClasses.contains(dotted)) {
            return null;
        }

        Object mixinTransformer = KnotInjector.getMixinTransformer();
        if (mixinTransformer == null) {
            return null;
        }

        byte[] inputBytes = classfileBuffer;
        try {
            Object delegate = KnotInjector.getDelegate();
            if (delegate != null) {
                try {
                    Method getPreBytes = delegate.getClass().getDeclaredMethod("getPreMixinClassByteArray", String.class, boolean.class);
                    getPreBytes.setAccessible(true);
                    byte[] pre = (byte[]) getPreBytes.invoke(delegate, dotted, false);
                    if (pre != null && pre.length > 0) {
                        inputBytes = pre;
                    }
                } catch (NoSuchMethodException ignored) {}
            }
        } catch (Throwable ignored) {}

        try {
            if (transformMethod == null) {
                try {
                    Class<?> imtClass = Class.forName("org.spongepowered.asm.mixin.transformer.IMixinTransformer");
                    Class<?> envClass = Class.forName("org.spongepowered.asm.mixin.MixinEnvironment");
                    transformMethod = imtClass.getMethod("transformClass", envClass, String.class, byte[].class);
                    transformMethod.setAccessible(true);
                } catch (Throwable ignored) {
                    for (Method m : mixinTransformer.getClass().getMethods()) {
                        if (m.getName().equals("transformClass") && m.getParameterCount() == 3) {
                            transformMethod = m;
                            transformMethod.setAccessible(true);
                            break;
                        }
                    }
                }
            }

            if (transformMethod != null) {
                Class<?> envClass = Class.forName("org.spongepowered.asm.mixin.MixinEnvironment");
                Method getDefEnv = envClass.getMethod("getDefaultEnvironment");
                Object defEnv = getDefEnv.invoke(null);

                byte[] out = (byte[]) transformMethod.invoke(mixinTransformer, defEnv, dotted, inputBytes);
                if (out != null && out.length > 0) {
                    MutoLog.info("live mixin woven into {}", dotted);
                    return out;
                }
            }
        } catch (Throwable t) {
            Throwable cause = t.getCause() != null ? t.getCause() : t;
            MutoLog.warn("mixin transform failed for {}: {}", dotted, cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName());
        }

        return null;
    }
}
