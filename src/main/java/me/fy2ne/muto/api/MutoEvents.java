package me.fy2ne.muto.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Lifecycle event hooks for the Muto Engine reload pipeline.
 *
 * <p>Register listeners to react to mod hot-swap transactions — clean up stale
 * state before a reload begins, or refresh caches and UI once it completes.
 *
 * <p>Example:
 * <pre>{@code
 * MutoEvents.RELOAD_START.register((startMs, diff) -> {
 *     MyTextureCache.clear();
 *     MyNetworkLayer.closeStaleChannels();
 * });
 *
 * MutoEvents.RELOAD_FINISH.register((endMs, diff, success, durationMs, error) -> {
 *     if (success) {
 *         MyModList.refresh();
 *     } else {
 *         MyLogger.warn("reload failed: " + error);
 *     }
 * });
 * }</pre>
 *
 * <p>All listeners are invoked synchronously on the reload background thread —
 * not the main Minecraft thread. If you need to execute on the game thread,
 * submit work via {@code Minecraft.getInstance().submit(...)}.
 */
public final class MutoEvents {
    private MutoEvents() {}

    /**
     * Listener invoked immediately before the reload pipeline executes.
     *
     * <p>Receives the epoch timestamp of when the reload was triggered and
     * the computed {@link ModDiff} showing what is about to change.
     */
    @FunctionalInterface
    public interface ReloadStartListener {
        /**
         * @param startEpochMs epoch milliseconds when the reload was triggered
         * @param expectedDiff the computed diff of added, removed, and updated mods
         */
        void onReloadStart(long startEpochMs, ModDiff expectedDiff);
    }

    /**
     * Listener invoked after the reload pipeline completes — whether it
     * succeeded, failed, or was a no-op.
     */
    @FunctionalInterface
    public interface ReloadFinishListener {
        /**
         * @param endEpochMs  epoch milliseconds when the reload finished
         * @param diff        the actual diff that was processed
         * @param success     whether the pipeline completed without error
         * @param durationMs  total wall-clock time the reload took
         * @param error       the exception if {@code success} is {@code false}, otherwise {@code null}
         */
        void onReloadFinish(long endEpochMs, ModDiff diff, boolean success, long durationMs, Throwable error);
    }

    /**
     * Fired immediately before the hot-swap pipeline begins execution.
     *
     * <p>Use this to tear down any state that depends on the mods about to be
     * swapped — clear texture caches, close network connections, unregister
     * listeners that the old mod classes registered.
     */
    public static final Event<ReloadStartListener> RELOAD_START = EventFactory.createArrayBacked(
            ReloadStartListener.class,
            listeners -> (startEpochMs, expectedDiff) -> {
                for (var listener : listeners) {
                    listener.onReloadStart(startEpochMs, expectedDiff);
                }
            }
    );

    /**
     * Fired after the hot-swap pipeline finishes — on success, failure, or no-op.
     *
     * <p>Use this to refresh UI, re-bind resources, update mod lists, or log the
     * outcome. Always fires, even if the reload was a no-op (no mod changes detected).
     */
    public static final Event<ReloadFinishListener> RELOAD_FINISH = EventFactory.createArrayBacked(
            ReloadFinishListener.class,
            listeners -> (endEpochMs, diff, success, durationMs, error) -> {
                for (var listener : listeners) {
                    listener.onReloadFinish(endEpochMs, diff, success, durationMs, error);
                }
            }
    );
}
