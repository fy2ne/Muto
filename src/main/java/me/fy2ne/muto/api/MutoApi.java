package me.fy2ne.muto.api;

import me.fy2ne.muto.automation.ModsFolderWatcher;
import me.fy2ne.muto.automation.MutoTriggerWatcher;
import me.fy2ne.muto.boundary.ReloadBoundaryManager;
import me.fy2ne.muto.engine.ReloadEngine;
import me.fy2ne.muto.engine.ReloadHistoryEntry;
import me.fy2ne.muto.engine.ReloadPlan;
import me.fy2ne.muto.engine.ReloadResult;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class MutoApi {
    private MutoApi() {}

    public static boolean isAvailable() {
        return FabricLoader.getInstance().isModLoaded("muto");
    }

    public static boolean isReloading() {
        return ReloadEngine.INSTANCE.isReloading();
    }

    public static boolean isSafeToReload() {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc == null || isReloading()) return false;
            return mc.level == null && !mc.hasSingleplayerServer();
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static ReloadPlan plan() {
        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
        return ReloadEngine.INSTANCE.plan(modsDir);
    }

    public static CompletableFuture<ReloadResult> reloadAsync() {
        return reloadAsync(stage -> {});
    }

    public static CompletableFuture<ReloadResult> reloadAsync(Consumer<String> stageNotifier) {
        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
        return ReloadEngine.INSTANCE.reloadAsync(modsDir, stageNotifier);
    }

    /**
     * Triggers a safe boundary reload. If the player is inside a singleplayer world
     * or connected to a multiplayer server, cleanly disconnects to a progress screen,
     * runs the reload pipeline, and automatically rejoins the world/server on completion.
     */
    public static void reloadWithBoundary(Screen returnScreen) {
        ReloadBoundaryManager.triggerReload(returnScreen);
    }

    public static List<ReloadHistoryEntry> getHistory() {
        return ReloadEngine.INSTANCE.history();
    }

    public static boolean isFolderWatcherEnabled() {
        return ModsFolderWatcher.isEnabled();
    }

    public static void setFolderWatcherEnabled(boolean enabled) {
        ModsFolderWatcher.setEnabled(enabled);
    }

    public static boolean isAutoReloadResources() {
        return ReloadEngine.INSTANCE.isAutoReloadResources();
    }

    public static void setAutoReloadResources(boolean enabled) {
        ReloadEngine.INSTANCE.setAutoReloadResources(enabled);
    }

    public static boolean startFileWatcher() {
        return MutoTriggerWatcher.start();
    }

    public static boolean stopFileWatcher() {
        return MutoTriggerWatcher.stop();
    }

    public static boolean isFileWatcherRunning() {
        return MutoTriggerWatcher.isRunning();
    }

    public static Set<String> getReloadableModIds() {
        try {
            Set<String> ids = new java.util.LinkedHashSet<>();
            for (var mod : ReloadEngine.INSTANCE.currentSnapshot().reloadableMods()) {
                ids.add(mod.id());
            }
            return Collections.unmodifiableSet(ids);
        } catch (Throwable t) {
            return Collections.emptySet();
        }
    }

    public static Set<String> getStubbornModIds() {
        try {
            Set<String> ids = new java.util.LinkedHashSet<>();
            for (var mod : ReloadEngine.INSTANCE.currentSnapshot().stubbornMods()) {
                ids.add(mod.id());
            }
            return Collections.unmodifiableSet(ids);
        } catch (Throwable t) {
            return Collections.emptySet();
        }
    }
}
