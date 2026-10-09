package me.fy2ne.muto.api;

import me.fy2ne.muto.engine.ReloadEngine;
import me.fy2ne.muto.engine.ReloadResult;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Public integration API for Muto.
 * <p>
 * Designed for external mod managers, in-game downloaders (e.g. Resourcify),
 * or developer tools that need to trigger live mod hot-reloads dynamically.
 */
public final class MutoApi {
    private MutoApi() {}

    /**
     * Checks if Muto is active and ready to handle reload requests.
     */
    public static boolean isAvailable() {
        return FabricLoader.getInstance().isModLoaded("muto");
    }

    /**
     * Returns true if a reload pipeline execution is currently active.
     */
    public static boolean isReloading() {
        return ReloadEngine.INSTANCE.isReloading();
    }

    /**
     * Returns true if the client is currently in a safe state for hot-reloading
     * (i.e. on the main title screen, not connected to a live world or server).
     */
    public static boolean isSafeToReload() {
        try {
            var client = net.minecraft.client.Minecraft.getInstance();
            return client != null && client.level == null && client.gui != null && client.gui.screen() instanceof net.minecraft.client.gui.screens.TitleScreen;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Asynchronously triggers a rescan of the /mods folder and executes
     * a hot-reload for newly added, removed, or updated mod jars.
     *
     * @return CompletableFuture resolving to the reload result
     */
    public static CompletableFuture<ReloadResult> reloadAsync() {
        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
        return ReloadEngine.INSTANCE.reloadAsync(modsDir, stage -> {});
    }

    /**
     * Retrieves the IDs of all reloadable mods tracked in the current snapshot.
     */
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

    /**
     * Retrieves the IDs of all stubborn/core mods (locked to prevent crash).
     */
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
