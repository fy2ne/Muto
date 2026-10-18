package me.fy2ne.muto.automation;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.engine.ReloadEngine;
import me.fy2ne.muto.engine.ReloadResult;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MutoTriggerWatcher {
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "muto-trigger-watcher");
        t.setDaemon(true);
        return t;
    });

    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);
    private static ScheduledFuture<?> taskHandle;

    public static synchronized boolean start() {
        if (!RUNNING.compareAndSet(false, true)) {
            return false;
        }

        Path gameDir = FabricLoader.getInstance().getGameDir();
        Path modsDir = gameDir.resolve("mods");
        Path triggerFile = gameDir.resolve("muto.trigger");
        Path resultFile = gameDir.resolve("muto.result");

        taskHandle = SCHEDULER.scheduleWithFixedDelay(() -> {
            try {
                if (Files.exists(triggerFile)) {
                    MutoLog.info("external reload trigger detected at {}", triggerFile);
                    try {
                        Files.deleteIfExists(triggerFile);
                    } catch (IOException ignored) {}

                    ReloadResult res = ReloadEngine.INSTANCE.reloadAsync(modsDir, msg -> {
                        MutoLog.info("[TRIGGER] {}", msg);
                    }).get(30, TimeUnit.SECONDS);

                    String outcome = String.format("SUCCESS=%b, DURATION=%dms, ADDED=%s, REMOVED=%s, UPDATED=%s%n",
                            res.success(),
                            res.durationMs(),
                            res.diff().added(),
                            res.diff().removed(),
                            res.diff().updated());

                    Files.writeString(resultFile, outcome,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);

                    MutoLog.info("trigger reload finished: {}", outcome.trim());
                }
            } catch (Throwable t) {
                MutoLog.warn("trigger watcher tick error: {}", t.getMessage());
            }
        }, 200, 200, TimeUnit.MILLISECONDS);

        MutoLog.info("muto trigger watcher active (watch: {})", triggerFile);
        return true;
    }

    public static synchronized boolean stop() {
        if (!RUNNING.compareAndSet(true, false)) {
            return false;
        }
        if (taskHandle != null) {
            taskHandle.cancel(false);
            taskHandle = null;
        }
        MutoLog.info("muto trigger watcher stopped");
        return true;
    }

    public static boolean isRunning() {
        return RUNNING.get();
    }
}
