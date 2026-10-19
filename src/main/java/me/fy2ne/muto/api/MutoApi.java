package me.fy2ne.muto.api;

import me.fy2ne.muto.automation.MutoTriggerWatcher;
import me.fy2ne.muto.engine.ReloadEngine;
import me.fy2ne.muto.engine.ReloadPlan;
import me.fy2ne.muto.engine.ReloadResult;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Primary entry point for Muto Engine integrations.
 *
 * <p>Muto Engine is a developer library — it exposes a runtime hot-swap pipeline
 * for Fabric mods. Declare it as a soft optional dependency and guard all calls
 * with {@link #isAvailable()} to remain compatible with installs that don't have
 * Muto Engine present.
 *
 * <p>Typical integration pattern:
 * <pre>{@code
 * if (FabricLoader.getInstance().isModLoaded("muto")) {
 *     if (MutoApi.isSafeToReload()) {
 *         MutoApi.reloadAsync().thenAccept(result -> {
 *             if (result.success()) log("hot-swapped in " + result.durationMs() + "ms");
 *         });
 *     }
 * }
 * }</pre>
 *
 * <p>All methods are thread-safe. {@link #reloadAsync()} and
 * {@link #reloadAsync(Consumer)} are non-blocking and return a
 * {@link CompletableFuture} that resolves on a background thread.
 *
 * @see MutoEvents
 * @see ModDiff
 * @see me.fy2ne.muto.engine.ReloadResult
 * @see me.fy2ne.muto.engine.ReloadPlan
 */
public final class MutoApi {
    private MutoApi() {}

    /**
     * Returns {@code true} if Muto Engine is loaded and available at runtime.
     *
     * <p>Use this as the outermost guard before calling any other method.
     */
    public static boolean isAvailable() {
        return FabricLoader.getInstance().isModLoaded("muto");
    }

    /**
     * Returns {@code true} if a reload transaction is currently in progress.
     *
     * <p>Concurrent reload requests while this returns {@code true} will be
     * rejected and return a failed {@link ReloadResult} immediately.
     */
    public static boolean isReloading() {
        return ReloadEngine.INSTANCE.isReloading();
    }

    /**
     * Returns {@code true} if the client is in a safe state for hot-reloading.
     *
     * <p>"Safe" means the client is on the title screen — not ticking inside a
     * singleplayer world or connected to a multiplayer server. Calling
     * {@link #reloadAsync()} while inside a world is technically possible but
     * callers should respect this boundary to avoid mid-tick registry corruption.
     */
    public static boolean isSafeToReload() {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc == null || isReloading()) return false;
            return mc.level == null && !mc.hasSingleplayerServer();
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Scans {@code /mods} and computes the reload plan without executing it.
     *
     * <p>Use this to inspect what would change — which mods are added, removed,
     * or updated — before committing to a reload. The returned {@link ReloadPlan}
     * exposes the diff and the classified mod lists.
     *
     * <p>This method is cheap and non-destructive. It does not inject jars or
     * mutate registries.
     *
     * @return the computed reload plan for the current /mods state
     */
    public static ReloadPlan plan() {
        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
        return ReloadEngine.INSTANCE.plan(modsDir);
    }

    /**
     * Asynchronously executes a full hot-swap pipeline against the current
     * {@code /mods} directory.
     *
     * <p>The pipeline: scan → diff → inject jars into Knot's classpath →
     * register dynamic mixin configs → tear down removed/updated contexts and
     * prune their registry namespaces → unfreeze registries → re-run
     * ModInitializer → refreeze registries → sync Fabric Loader metadata →
     * sync keybindings.
     *
     * <p>If no mod changes are detected, returns immediately with a successful
     * no-op result.
     *
     * @return a {@link CompletableFuture} resolving to the reload result
     */
    public static CompletableFuture<ReloadResult> reloadAsync() {
        return reloadAsync(stage -> {});
    }

    /**
     * Asynchronously executes a hot-swap pipeline with per-stage progress callbacks.
     *
     * <p>The {@code stageNotifier} is called on the reload thread with a human-readable
     * status string for each pipeline stage (e.g. "Scanning /mods: 12 active mods
     * detected"). Useful for displaying progress in a UI or logging verbosely.
     *
     * @param stageNotifier callback invoked once per pipeline stage
     * @return a {@link CompletableFuture} resolving to the reload result
     */
    public static CompletableFuture<ReloadResult> reloadAsync(Consumer<String> stageNotifier) {
        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
        return ReloadEngine.INSTANCE.reloadAsync(modsDir, stageNotifier);
    }

    /**
     * Starts the opt-in developer file trigger watcher.
     *
     * <p>When active, Muto Engine polls for a {@code muto.trigger} file in the game
     * directory every 200ms. When found, the file is consumed and a reload is executed
     * automatically. This integrates with the {@code ./gradlew mutoWatch} Gradle task.
     *
     * <p>This is a developer tool and should not be auto-started in production mods.
     *
     * @return {@code true} if the watcher was started, {@code false} if already running
     */
    public static boolean startFileWatcher() {
        return MutoTriggerWatcher.start();
    }

    /**
     * Stops the developer file trigger watcher.
     *
     * @return {@code true} if stopped, {@code false} if it wasn't running
     */
    public static boolean stopFileWatcher() {
        return MutoTriggerWatcher.stop();
    }

    /**
     * Returns {@code true} if the file watcher is currently active.
     */
    public static boolean isFileWatcherRunning() {
        return MutoTriggerWatcher.isRunning();
    }

    /**
     * Returns the IDs of all mods currently tracked as dynamically reloadable
     * (Tier 1 and Tier 2) in the last recorded snapshot.
     *
     * <p>Tier 1 — clean client mods (HUDs, UI, tweaks).
     * Tier 2 — content mods with blocks, items, or registry entries.
     *
     * @return unmodifiable set of reloadable mod IDs
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
     * Returns the IDs of all mods classified as Tier 3 (stubborn / core).
     *
     * <p>Tier 3 mods are already loaded and mixin-woven, so their classes cannot
     * be re-woven in place and they cannot be reloaded without a full JVM restart.
     * This includes Fabric Loader, Fabric API, and mods with deeply-woven mixins.
     *
     * @return unmodifiable set of stubborn mod IDs
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
