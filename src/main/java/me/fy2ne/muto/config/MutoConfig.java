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

    private static Path getConfigFile() {
        try {
            Path dir = FabricLoader.getInstance().getConfigDir();
            if (dir != null) {
                return dir.resolve("muto.json");
            }
        } catch (Throwable ignored) {}
        return Path.of("config").resolve("muto.json");
    }

    private static MutoConfig INSTANCE;

    public boolean autoReloadOnDiskChange = false;
    public boolean showToasts = true;
    public boolean playReloadSound = true;
    public boolean fastShaderReload = true;
    public boolean autoReconnect = true;
    public boolean showTitleScreenButton = true;
    public boolean autoReloadResources = true;
    public boolean confirmBeforeReload = true;
    public boolean checkModrinthUpdates = false;
    public boolean simulateUpdateAvailable = false;

    public static MutoConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        Path configFile = getConfigFile();
        if (Files.exists(configFile)) {
            try (Reader reader = Files.newBufferedReader(configFile)) {
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
            Path configFile = getConfigFile();
            if (configFile.getParent() != null) {
                Files.createDirectories(configFile.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(configFile)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (Exception e) {
            MutoLog.error("failed saving muto.json", e);
        }
    }
}
