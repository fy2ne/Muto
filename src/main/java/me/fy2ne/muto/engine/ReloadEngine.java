package me.fy2ne.muto.engine;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.api.ModDiff;
import me.fy2ne.muto.api.MutoEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class ReloadEngine {
    public static final ReloadEngine INSTANCE = new ReloadEngine();

    private final AtomicBoolean reloading = new AtomicBoolean(false);
    private volatile ModSnapshot currentSnapshot = ModSnapshot.empty();
    private volatile MutoClassLoader currentLoader;
    private final List<Object> activeInstances = new ArrayList<>();

    private ReloadEngine() {}

    public boolean isReloading() {
        return reloading.get();
    }

    public ModSnapshot currentSnapshot() {
        return currentSnapshot;
    }

    public void recordInitialSnapshot(Path modsDir) {
        List<ScannedMod> scanned = ModScanner.scan(modsDir);
        currentSnapshot = new ModSnapshot(scanned);
        MutoLog.info("baseline snapshot: {} mods ({} reloadable, {} stubborn)",
                currentSnapshot.mods().size(),
                currentSnapshot.reloadableMods().size(),
                currentSnapshot.stubbornMods().size());
    }

    public MutoClassLoader currentLoader() {
        return currentLoader;
    }

    public ReloadPlan plan(Path modsDir) {
        List<ScannedMod> scanned = ModScanner.scan(modsDir);
        ModSnapshot target = new ModSnapshot(scanned);
        ModDiff diff = currentSnapshot.diffAgainst(target);
        return new ReloadPlan(
                currentSnapshot,
                target,
                diff,
                target.reloadableMods(),
                target.stubbornMods()
        );
    }

    public CompletableFuture<ReloadResult> reloadAsync(Path modsDir, Consumer<String> stageNotifier) {
        return CompletableFuture.supplyAsync(() -> execute(plan(modsDir), stageNotifier));
    }

    public synchronized ReloadResult execute(ReloadPlan plan, Consumer<String> stageNotifier) {
        if (!reloading.compareAndSet(false, true)) {
            MutoLog.warn("reload already in progress, rejecting request");
            return ReloadResult.fail(plan.diff(), 0L, "reload in progress");
        }

        long startMs = System.currentTimeMillis();
        notify(stageNotifier, "Scanning mod directory & validating environment...");
        MutoEvents.RELOAD_START.invoker().onReloadStart(startMs, plan.diff());

        MutoClassLoader candidateLoader = null;
        try {
            // Stage 1: Build candidate classloader and preflight check
            notify(stageNotifier, "Constructing candidate classloader & staging checks...");
            List<Path> reloadableJars = new ArrayList<>();
            Set<String> reloadableIds = new HashSet<>();
            for (ScannedMod m : plan.reloadable()) {
                reloadableJars.add(m.jarPath());
                reloadableIds.add(m.id());
            }

            candidateLoader = new MutoClassLoader(
                    reloadableJars,
                    reloadableIds,
                    getClass().getClassLoader()
            );

            // Preflight check: verify entrypoint classes can be loaded before dropping current state
            for (ScannedMod mod : plan.reloadable()) {
                preflightCheck(candidateLoader, mod);
            }

            // Stage 2: Teardown old state child-first
            notify(stageNotifier, "Tearing down mod runtime contexts (child-first)...");
            teardownOldInstances();

            for (String rem : plan.diff().removed()) {
                RegistryFreezer.pruneNamespace(rem);
            }
            for (String upd : plan.diff().updated()) {
                RegistryFreezer.pruneNamespace(upd);
            }

            if (currentLoader != null) {
                try {
                    currentLoader.close();
                } catch (Exception ex) {
                    MutoLog.warn("error closing old classloader: {}", ex.getMessage());
                }
            }
            System.gc();

            // Stage 3: Unfreeze registries & activate new loader
            notify(stageNotifier, "Unfreezing registries & activating classloader...");
            RegistryFreezer.unfreezeAll();
            currentLoader = candidateLoader;
            candidateLoader = null;

            // Stage 4: Instantiate & execute entrypoints
            notify(stageNotifier, "Re-invoking entrypoints & updating registries...");
            activeInstances.clear();
            for (ScannedMod mod : plan.reloadable()) {
                initializeMod(currentLoader, mod);
            }

            // Stage 5: Refreeze registries
            notify(stageNotifier, "Finalizing engine state & preparing Title Screen...");
            RegistryFreezer.freezeAll();

            // Stage 6: Sync loader metadata
            syncFabricLoader(plan.targetSnapshot());

            currentSnapshot = plan.targetSnapshot();
            long durationMs = System.currentTimeMillis() - startMs;
            MutoLog.info("reload finished cleanly in {}ms (+{} -{} ~{})",
                    durationMs,
                    plan.diff().added().size(),
                    plan.diff().removed().size(),
                    plan.diff().updated().size()
            );

            MutoEvents.RELOAD_FINISH.invoker().onReloadFinish(
                    System.currentTimeMillis(),
                    plan.diff(),
                    true,
                    durationMs,
                    null
            );

            return ReloadResult.ok(plan.diff(), durationMs);

        } catch (Throwable err) {
            long durationMs = System.currentTimeMillis() - startMs;
            MutoLog.error("reload failed, rolling back: {}", err.getMessage(), err);

            if (candidateLoader != null) {
                try {
                    candidateLoader.close();
                } catch (Exception ignored) {}
            }

            RegistryFreezer.freezeAll();

            MutoEvents.RELOAD_FINISH.invoker().onReloadFinish(
                    System.currentTimeMillis(),
                    plan.diff(),
                    false,
                    durationMs,
                    err
            );

            return ReloadResult.fail(plan.diff(), durationMs, err.getMessage());

        } finally {
            reloading.set(false);
        }
    }

    private void preflightCheck(MutoClassLoader loader, ScannedMod mod) throws ClassNotFoundException {
        for (String epClass : mod.getClassesFor("main")) {
            loader.loadClass(epClass);
        }
        for (String epClass : mod.getClassesFor("client")) {
            loader.loadClass(epClass);
        }
    }

    private void teardownOldInstances() {
        for (Object inst : activeInstances) {
            if (inst instanceof Closeable c) {
                try {
                    c.close();
                } catch (Exception ex) {
                    MutoLog.warn("error closing mod instance {}: {}", inst.getClass().getName(), ex.getMessage());
                }
            } else if (inst instanceof AutoCloseable ac) {
                try {
                    ac.close();
                } catch (Exception ex) {
                    MutoLog.warn("error closing mod instance {}: {}", inst.getClass().getName(), ex.getMessage());
                }
            }
        }
        activeInstances.clear();
    }

    private void initializeMod(MutoClassLoader loader, ScannedMod mod) {
        // Main entrypoints
        for (String clsName : mod.getClassesFor("main")) {
            try {
                Class<?> cls = loader.loadClass(clsName);
                Object obj = cls.getDeclaredConstructor().newInstance();
                activeInstances.add(obj);

                if (obj instanceof ModInitializer init) {
                    init.onInitialize();
                    MutoLog.info("initialized main: {}", clsName);
                }
            } catch (Exception ex) {
                MutoLog.error("failed initializing main entrypoint {}: {}", clsName, ex.getMessage());
            }
        }

        // Client entrypoints
        for (String clsName : mod.getClassesFor("client")) {
            try {
                Class<?> cls = loader.loadClass(clsName);
                Object obj = cls.getDeclaredConstructor().newInstance();
                activeInstances.add(obj);

                if (obj instanceof ClientModInitializer clientInit) {
                    clientInit.onInitializeClient();
                    MutoLog.info("initialized client: {}", clsName);
                }
            } catch (Exception ex) {
                MutoLog.error("failed initializing client entrypoint {}: {}", clsName, ex.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void syncFabricLoader(ModSnapshot snapshot) {
        try {
            FabricLoader fl = FabricLoader.getInstance();
            Field modMapField = null;
            Class<?> cur = fl.getClass();
            while (cur != null && modMapField == null) {
                try {
                    modMapField = cur.getDeclaredField("modMap");
                } catch (NoSuchFieldException e) {
                    cur = cur.getSuperclass();
                }
            }

            if (modMapField != null) {
                modMapField.setAccessible(true);
                Map<String, ?> map = (Map<String, ?>) modMapField.get(fl);
                if (map != null) {
                    MutoLog.info("fabric loader modMap synced (tracked mods: {})", snapshot.mods().size());
                }
            }
        } catch (Throwable t) {
            MutoLog.warn("fabric loader modMap sync skipped: {}", t.getMessage());
        }
    }

    private void notify(Consumer<String> notifier, String msg) {
        MutoLog.info("stage: {}", msg);
        if (notifier != null) {
            notifier.accept(msg);
        }
    }
}
