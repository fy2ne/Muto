package me.fy2ne.muto.client;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.MutoMod;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId;
import net.minecraft.network.chat.Component;

public final class MutoToasts {

    // We register our own custom toast ID using the displayTime constructor
    private static final SystemToastId MUTO_CAPABILITY_ID = new SystemToastId(8000L);
    private static final SystemToastId MUTO_RELOAD_DONE   = new SystemToastId(5000L);
    private static final SystemToastId MUTO_RELOAD_FAIL   = new SystemToastId(8000L);

    private static boolean capabilityToastShown = false;

    private MutoToasts() {}

    public static void showCapabilityHint(net.minecraft.client.Minecraft mc) {
        if (capabilityToastShown) return;
        capabilityToastShown = true;

        if (MutoMod.isJbr() && MutoMod.hasDcevm()) return;

        String msg;
        if (!MutoMod.isJbr()) {
            msg = "Use JetBrains Runtime for Tier 1 hotswap (class-level live reload). Standard reload still works.";
        } else {
            msg = "JBR detected but AllowEnhancedClassRedefinition not set. Add -XX:+AllowEnhancedClassRedefinition to JVM args for best results.";
        }

        MutoLog.info("showing capability hint toast (jbr={}, dcevm={})", MutoMod.isJbr(), MutoMod.hasDcevm());
        SystemToast.add(
                mc.gui.toastManager(),
                MUTO_CAPABILITY_ID,
                Component.literal("Muto — Runtime Capability"),
                Component.literal(msg)
        );
    }

    public static void showReloadSuccess(net.minecraft.client.Minecraft mc, int added, int removed, int updated, long durationMs) {
        String summary = buildSummary(added, removed, updated) + " in " + durationMs + "ms";
        SystemToast.add(
                mc.gui.toastManager(),
                MUTO_RELOAD_DONE,
                Component.literal("Muto — Reload Complete"),
                Component.literal(summary)
        );
    }

    public static void showReloadFailure(net.minecraft.client.Minecraft mc, String error) {
        String msg = error != null ? error : "Unknown error — check logs/muto.log";
        if (msg.length() > 80) msg = msg.substring(0, 77) + "…";
        SystemToast.add(
                mc.gui.toastManager(),
                MUTO_RELOAD_FAIL,
                Component.literal("Muto — Reload Failed"),
                Component.literal(msg)
        );
    }

    private static String buildSummary(int added, int removed, int updated) {
        StringBuilder sb = new StringBuilder();
        if (added > 0) sb.append("+").append(added).append(" added");
        if (removed > 0) { if (!sb.isEmpty()) sb.append(", "); sb.append("-").append(removed).append(" removed"); }
        if (updated > 0) { if (!sb.isEmpty()) sb.append(", "); sb.append(updated).append(" updated"); }
        if (sb.isEmpty()) sb.append("No changes detected");
        return sb.toString();
    }
}
