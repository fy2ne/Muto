package me.fy2ne.muto.client;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.MutoMod;
import me.fy2ne.muto.api.MutoEvents;
import com.mojang.blaze3d.platform.InputConstants;
import me.fy2ne.muto.engine.boundary.ReloadBoundaryManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class MutoClient implements ClientModInitializer {
    public static final KeyMapping RELOAD_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.muto.reload",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_F9,
            KeyMapping.Category.MISC
    ));

    @Override
    public void onInitializeClient() {
        MutoLog.info("muto client initialized");

        try {
            me.fy2ne.muto.mods.DisabledModsRegistry.reconcileFileRenames(
                    net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir().resolve("mods")
            );
        } catch (Throwable ignored) {}

        if (me.fy2ne.muto.config.MutoConfig.get().checkModrinthUpdates) {
            me.fy2ne.muto.update.MutoUpdateChecker.checkAsync();
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (RELOAD_KEY.consumeClick()) {
                net.minecraft.client.gui.screens.Screen current = client.gui != null ? client.gui.screen() : null;
                ReloadBoundaryManager.triggerReload(client, current);
            }
        });

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

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            me.fy2ne.muto.automation.MutoTriggerWatcher.onClientStarted();
        });
    }
}

