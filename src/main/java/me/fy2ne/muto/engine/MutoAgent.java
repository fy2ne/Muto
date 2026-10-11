package me.fy2ne.muto.engine;

import me.fy2ne.muto.MutoLog;

import java.lang.instrument.Instrumentation;

public final class MutoAgent {
    private static volatile Instrumentation instrumentation;

    public static void premain(String args, Instrumentation inst) {
        init(inst, "premain");
    }

    public static void agentmain(String args, Instrumentation inst) {
        init(inst, "agentmain");
    }

    private static void init(Instrumentation inst, String source) {
        instrumentation = inst;
        try {
            System.getProperties().put("muto.internal.instrumentation", inst);
        } catch (Throwable ignored) {}
        MutoLog.info("muto agent attached via {} (retransform={})", source, inst.isRetransformClassesSupported());
    }

    public static Instrumentation getInstrumentation() {
        if (instrumentation != null) {
            return instrumentation;
        }
        try {
            Object obj = System.getProperties().get("muto.internal.instrumentation");
            if (obj instanceof Instrumentation inst) {
                instrumentation = inst;
                return inst;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static boolean isAvailable() {
        return getInstrumentation() != null;
    }

    public static boolean canRetransform() {
        Instrumentation inst = getInstrumentation();
        return inst != null && inst.isRetransformClassesSupported();
    }
}
