package me.fy2ne.muto.client;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.MutoMod;
import me.fy2ne.muto.api.MutoEvents;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;

public final class MutoClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MutoLog.info("muto client initialized");

        if (me.fy2ne.muto.config.MutoConfig.get().checkModrinthUpdates) {
            me.fy2ne.muto.update.MutoUpdateChecker.checkAsync();
        }

        MutoEvents.RELOAD_FINISH.register((endMs, diff, success, durationMs, error) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) return;
            if (success) {
                mc.execute(() -> MutoToasts.showReloadSuccess(mc, diff, durationMs));
            } else {
                String msg = error != null ? error.getMessage() : null;
                mc.execute(() -> MutoToasts.showReloadFailure(mc, msg));
            }
        });
    }
}
