package me.fy2ne.muto.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class MutoEvents {
    private MutoEvents() {}

    @FunctionalInterface
    public interface ReloadStartListener {
        void onReloadStart(long startEpochMs, ModDiff expectedDiff);
    }

    @FunctionalInterface
    public interface ReloadFinishListener {
        void onReloadFinish(long endEpochMs, ModDiff diff, boolean success, long durationMs, Throwable error);
    }

    public static final Event<ReloadStartListener> RELOAD_START = EventFactory.createArrayBacked(
            ReloadStartListener.class,
            listeners -> (startEpochMs, expectedDiff) -> {
                for (var listener : listeners) {
                    listener.onReloadStart(startEpochMs, expectedDiff);
                }
            }
    );

    public static final Event<ReloadFinishListener> RELOAD_FINISH = EventFactory.createArrayBacked(
            ReloadFinishListener.class,
            listeners -> (endEpochMs, diff, success, durationMs, error) -> {
                for (var listener : listeners) {
                    listener.onReloadFinish(endEpochMs, diff, success, durationMs, error);
                }
            }
    );
}
