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
import java.util.concurrent.TimeUnit;

public final class MutoTriggerWatcher {
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "muto-trigger-watcher");
        t.setDaemon(true);
        return t;
    });

    public static void start() {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        Path modsDir = gameDir.resolve("mods");
        Path triggerFile = gameDir.resolve("muto.trigger");
        Path resultFile = gameDir.resolve("muto.result");

        SCHEDULER.scheduleWithFixedDelay(() -> {
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
        }, 1, 1, TimeUnit.SECONDS);

        MutoLog.info("muto trigger watcher active (watch: {})", triggerFile);
    }

    public static void onClientStarted() {
        String autoTest = System.getProperty("muto.testAutoReload");
        if (!"true".equalsIgnoreCase(autoTest)) return;

        Path gameDir = FabricLoader.getInstance().getGameDir();
        Path modsDir = gameDir.resolve("mods");
        Path resultFile = gameDir.resolve("muto.result");

        long delaySec = Long.getLong("muto.testDelay", 5L);
        SCHEDULER.schedule(() -> {
            try {
                MutoLog.info("[AUTO-TEST] initiating automated reload test after startup (delay: {}s)...", delaySec);
                String copyMod = System.getProperty("muto.testCopyMod");
                if (copyMod != null && !copyMod.isBlank()) {
                    Path sourceMod = gameDir.resolve("../Modsfortesting/" + copyMod).normalize();
                    if (Files.exists(sourceMod)) {
                        Path targetMod = modsDir.resolve(copyMod);
                        Files.copy(sourceMod, targetMod, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        MutoLog.info("[AUTO-TEST] staged test mod: {}", copyMod);
                    } else {
                        MutoLog.warn("[AUTO-TEST] test mod not found at {}", sourceMod);
                    }
                }

                ReloadResult res = ReloadEngine.INSTANCE.reloadAsync(modsDir, msg -> {
                    MutoLog.info("[AUTO-TEST] {}", msg);
                }).get(45, TimeUnit.SECONDS);

                String outcome = String.format("AUTO_TEST_RESULT: SUCCESS=%b, DURATION=%dms, ADDED=%s, REMOVED=%s, UPDATED=%s",
                        res.success(),
                        res.durationMs(),
                        res.diff().added(),
                        res.diff().removed(),
                        res.diff().updated());
                MutoLog.info(outcome);

                Files.writeString(resultFile, outcome + System.lineSeparator(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);

                if ("true".equalsIgnoreCase(System.getProperty("muto.testAutoExit"))) {
                    MutoLog.info("[AUTO-TEST] test complete, exiting process...");
                    Thread.sleep(1500);
                    System.exit(res.success() ? 0 : 1);
                }
            } catch (Throwable t) {
                MutoLog.error("[AUTO-TEST] auto reload failed: {}", t.getMessage(), t);
                if ("true".equalsIgnoreCase(System.getProperty("muto.testAutoExit"))) {
                    System.exit(2);
                }
            }
        }, delaySec, TimeUnit.SECONDS);
    }
}
