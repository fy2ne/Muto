package me.fy2ne.muto.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.fy2ne.muto.MutoLog;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MutoConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("muto.json");

    private static MutoConfig INSTANCE;

    // General user settings
    public boolean confirmBeforeReload = true;
    public boolean autoReloadResources = true;
    public boolean showToasts = true;
    public boolean autoPruneShadowCache = true;
    public boolean showTitleScreenButton = true;
    public boolean checkModrinthUpdates = true;

    // Developer & diagnostic settings
    public boolean developerMode = false;
    public boolean verboseLogging = false;
    public boolean allowStubbornReload = false;
    public boolean skipPreflightCheck = false;
    public boolean trackAllocationMetrics = true;
    public boolean simulateUpdateAvailable = false;

    public static MutoConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, MutoConfig.class);
                if (INSTANCE != null) {
                    MutoLog.info("loaded configuration from muto.json");
                    return;
                }
            } catch (Exception e) {
                MutoLog.error("failed reading muto.json, using defaults", e);
            }
        }
        INSTANCE = new MutoConfig();
        save();
    }

    public static void save() {
        if (INSTANCE == null) return;
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (Exception e) {
            MutoLog.error("failed saving muto.json", e);
        }
    }
}
