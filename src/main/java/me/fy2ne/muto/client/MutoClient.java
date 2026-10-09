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

        MutoEvents.RELOAD_FINISH.register((endMs, diff, success, durationMs, error) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) return;
            if (success) {
                int added   = diff != null ? diff.added().size() : 0;
                int removed = diff != null ? diff.removed().size() : 0;
                int updated = diff != null ? diff.updated().size() : 0;
                mc.execute(() -> MutoToasts.showReloadSuccess(mc, added, removed, updated, durationMs));
            } else {
                String msg = error != null ? error.getMessage() : null;
                mc.execute(() -> MutoToasts.showReloadFailure(mc, msg));
            }
        });
    }
}
