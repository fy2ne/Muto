package me.fy2ne.muto.engine;

import me.fy2ne.muto.api.ModDiff;

public record ReloadResult(
        boolean success,
        ModDiff diff,
        long durationMs,
        String error
) {
    public static ReloadResult ok(ModDiff diff, long durationMs) {
        return new ReloadResult(true, diff, durationMs, null);
    }

    public static ReloadResult fail(ModDiff diff, long durationMs, String error) {
        return new ReloadResult(false, diff, durationMs, error);
    }
}
