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
        // Disabled by default to avoid alarming startup warning popups
    }

    public static void showReloadSuccess(net.minecraft.client.Minecraft mc, me.fy2ne.muto.api.ModDiff diff, long durationMs) {
        if (!me.fy2ne.muto.config.MutoConfig.get().showToasts) return;
        String summary = buildSummary(diff) + " in " + durationMs + "ms";
        SystemToast.add(
                mc.gui.toastManager(),
                MUTO_RELOAD_DONE,
                Component.literal("Muto — Reload Complete"),
                Component.literal(summary)
        );
    }

    public static void showReloadFailure(net.minecraft.client.Minecraft mc, String error) {
        if (!me.fy2ne.muto.config.MutoConfig.get().showToasts) return;
        String msg = error != null ? error : "Unknown error — check logs/muto.log";
        if (msg.length() > 80) msg = msg.substring(0, 77) + "…";
        SystemToast.add(
                mc.gui.toastManager(),
                MUTO_RELOAD_FAIL,
                Component.literal("Muto — Reload Failed"),
                Component.literal(msg)
        );
    }

    private static String buildSummary(me.fy2ne.muto.api.ModDiff diff) {
        if (diff == null) return "No changes detected";
        int added = diff.added().size();
        int removed = diff.removed().size();
        int updated = diff.updated().size();

        StringBuilder sb = new StringBuilder();
        if (added > 0) {
            sb.append("+").append(added).append(" added");
            if (added <= 2) {
                sb.append(" (").append(String.join(", ", diff.added())).append(")");
            }
        }
        if (removed > 0) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append("-").append(removed).append(" removed");
        }
        if (updated > 0) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append("~").append(updated).append(" updated");
        }
        if (sb.isEmpty()) sb.append("No changes detected");
        return sb.toString();
    }
}
