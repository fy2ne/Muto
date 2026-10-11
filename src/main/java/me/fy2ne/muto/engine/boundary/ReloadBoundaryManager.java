package me.fy2ne.muto.engine.boundary;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.client.gui.MutoReconnectingScreen;
import me.fy2ne.muto.client.gui.MutoReloadScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;

import java.nio.file.Path;

public final class ReloadBoundaryManager {

    private ReloadBoundaryManager() {}

    public static void triggerReload(Minecraft client, Screen currentScreen) {
        if (client == null) return;
        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");

        // 1. In multiplayer server: graceful disconnect, preserve serverData, reload, and auto-reconnect
        ServerData serverData = client.getCurrentServer();
        if (serverData != null) {
            MutoLog.info("boundary reload requested in server: {} ({})", serverData.name, serverData.ip);
            MutoReconnectingScreen screen = new MutoReconnectingScreen(currentScreen, modsDir, serverData, false);
            client.disconnect(screen, false);
            return;
        }

        // 2. In singleplayer world: save level and exit cleanly before applying mod reload
        if (client.level != null || client.hasSingleplayerServer()) {
            MutoLog.info("boundary reload requested in singleplayer: saving level and exiting to title screen");
            MutoReconnectingScreen screen = new MutoReconnectingScreen(currentScreen, modsDir, null, true);
            client.disconnect(screen, false);
            return;
        }

        // 3. Already on Menu / Title screen: reload directly
        MutoLog.info("boundary reload requested on menu screen");
        client.setScreenAndShow(new MutoReloadScreen(currentScreen));
    }
}
