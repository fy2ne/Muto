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
        MutoLog.info("muto agent attached via {} (retransform={})", source, inst.isRetransformClassesSupported());
    }

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public static boolean isAvailable() {
        return instrumentation != null;
    }

    public static boolean canRetransform() {
        return instrumentation != null && instrumentation.isRetransformClassesSupported();
    }
}
