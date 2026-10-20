package me.fy2ne.muto.automation;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.api.MutoApi;
import me.fy2ne.muto.engine.ReloadEngine;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ModsFolderWatcher {
    private static final AtomicBoolean ENABLED = new AtomicBoolean(false);
    private static final AtomicBoolean THREAD_STARTED = new AtomicBoolean(false);
    private static volatile long suppressUntilMs = 0L;

    private ModsFolderWatcher() {}

    public static void suppressForMs(long ms) {
        suppressUntilMs = Math.max(suppressUntilMs, System.currentTimeMillis() + ms);
    }

    public static boolean isEnabled() {
        return ENABLED.get();
    }

    public static void setEnabled(boolean enabled) {
        ENABLED.set(enabled);
        if (enabled && THREAD_STARTED.compareAndSet(false, true)) {
            Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
            if (!Files.isDirectory(modsDir)) return;
            Thread t = new Thread(() -> watchLoop(modsDir), "muto-folder-watcher");
            t.setDaemon(true);
            t.start();
        }
    }

    private static void watchLoop(Path dir) {
        try (WatchService ws = dir.getFileSystem().newWatchService()) {
            dir.register(ws,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
            MutoLog.info("mods folder watcher online");

            while (true) {
                WatchKey key = ws.take();
                boolean relevant = hasRelevantEvent(key);
                key.reset();
                if (!relevant) continue;

                // wait for file copy bursts to settle
                Thread.sleep(750);
                WatchKey next;
                while ((next = ws.poll(300, TimeUnit.MILLISECONDS)) != null) {
                    next.pollEvents();
                    next.reset();
                }

                if (!ENABLED.get()) continue;
                if (System.currentTimeMillis() < suppressUntilMs) continue;
                if (ReloadEngine.INSTANCE.isReloading()) continue;

                if (MutoApi.isSafeToReload()) {
                    MutoLog.info("folder change detected, starting hot reload");
                    MutoApi.reloadAsync();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            MutoLog.warn("mods watcher stopped: {}", e.getMessage());
        }
    }

    private static boolean hasRelevantEvent(WatchKey key) {
        boolean found = false;
        for (WatchEvent<?> ev : key.pollEvents()) {
            Object ctx = ev.context();
            if (ctx instanceof Path p) {
                String name = p.getFileName().toString();
                if (name.endsWith(".jar") || name.endsWith(".disabled") || name.endsWith(".deactivation")) {
                    found = true;
                }
            }
        }
        return found;
    }
}
