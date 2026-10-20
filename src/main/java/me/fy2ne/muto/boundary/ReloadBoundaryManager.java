package me.fy2ne.muto.boundary;

import me.fy2ne.muto.MutoLog;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

public final class ReloadBoundaryManager {
    private ReloadBoundaryManager() {}

    public static void triggerReload(Screen returnScreen) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");

        ServerData server = mc.getCurrentServer();
        if (server != null) {
            MutoLog.info("boundary reload in multiplayer: {} ({})", server.name, server.ip);
            MutoReconnectingScreen screen = new MutoReconnectingScreen(returnScreen, modsDir, server, null);
            mc.disconnect(screen, false);
            return;
        }

        if (mc.level != null || mc.hasSingleplayerServer()) {
            String levelId = resolveSingleplayerLevelId(mc);
            MutoLog.info("boundary reload in singleplayer: world={}", levelId);
            MutoReconnectingScreen screen = new MutoReconnectingScreen(returnScreen, modsDir, null, levelId);
            mc.disconnect(screen, false);
            return;
        }

        mc.setScreenAndShow(new MutoReconnectingScreen(returnScreen, modsDir, null, null));
    }

    private static String resolveSingleplayerLevelId(Minecraft mc) {
        try {
            var srv = mc.getSingleplayerServer();
            if (srv != null) {
                Path root = srv.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
                if (root.getFileName() != null) {
                    return root.getFileName().toString();
                }
            }
        } catch (Throwable t) {
            MutoLog.warn("could not resolve singleplayer level id: {}", t.getMessage());
        }
        return null;
    }
}
