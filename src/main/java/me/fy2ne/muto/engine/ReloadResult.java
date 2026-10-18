package me.fy2ne.muto.engine;

import me.fy2ne.muto.api.ModDiff;

/**
 * The outcome of a single Muto Engine reload pipeline execution.
 *
 * <p>Returned by {@link me.fy2ne.muto.api.MutoApi#reloadAsync()} and
 * {@link me.fy2ne.muto.api.MutoApi#reloadAsync(java.util.function.Consumer)}.
 *
 * <p>A result is always produced — even for no-op reloads (no mod changes
 * detected) and failed pipelines. Check {@link #success()} first, then use
 * {@link #diff()} to inspect what changed and {@link #durationMs()} for timing.
 *
 * @param success    whether the pipeline completed without error
 * @param diff       the mod diff that was processed (may be empty for no-ops)
 * @param durationMs total wall-clock time the pipeline took, in milliseconds
 * @param error      human-readable error message if {@code success} is {@code false},
 *                   otherwise {@code null}
 */
public record ReloadResult(
        boolean success,
        ModDiff diff,
        long durationMs,
        String error
) {
    /** Constructs a successful result. */
    public static ReloadResult ok(ModDiff diff, long durationMs) {
        return new ReloadResult(true, diff, durationMs, null);
    }

    /** Constructs a failed result with an error message. */
    public static ReloadResult fail(ModDiff diff, long durationMs, String error) {
        return new ReloadResult(false, diff, durationMs, error);
    }
}
