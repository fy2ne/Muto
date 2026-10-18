package me.fy2ne.muto.mods;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.fy2ne.muto.MutoLog;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class DisabledModsRegistry {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type SET_TYPE = new TypeToken<HashSet<String>>() {}.getType();
    private static final Set<String> DISABLED_IDS = new HashSet<>();
    private static Path configPath;
    private static boolean initialized = false;

    private DisabledModsRegistry() {}

    private static synchronized void ensureInit() {
        if (initialized) return;
        try {
            Path dir = FabricLoader.getInstance().getConfigDir().resolve("muto");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            configPath = dir.resolve("disabled_mods.json");
            if (Files.exists(configPath)) {
                try (Reader reader = Files.newBufferedReader(configPath)) {
                    Set<String> loaded = GSON.fromJson(reader, SET_TYPE);
                    if (loaded != null) {
                        DISABLED_IDS.addAll(loaded);
                    }
                }
            }
        } catch (Throwable t) {
            MutoLog.warn("failed loading disabled mods registry: {}", t.getMessage());
        } finally {
            initialized = true;
        }
    }

    public static synchronized boolean isDisabled(String modId) {
        if (modId == null) return false;
        ensureInit();
        return DISABLED_IDS.contains(modId);
    }

    public static synchronized void setDisabled(String modId, boolean disabled) {
        if (modId == null) return;
        ensureInit();
        if (disabled) {
            DISABLED_IDS.add(modId);
        } else {
            DISABLED_IDS.remove(modId);
        }
        save();
    }

    public static synchronized Set<String> getDisabledModIds() {
        ensureInit();
        return new HashSet<>(DISABLED_IDS);
    }

    private static synchronized void save() {
        if (configPath == null) return;
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(DISABLED_IDS, SET_TYPE, writer);
        } catch (Throwable t) {
            MutoLog.error("failed saving disabled mods registry: {}", t.getMessage());
        }
    }

    public static synchronized void reconcileFileRenames(Path modsDir) {
        ensureInit();
        if (modsDir == null || !Files.isDirectory(modsDir)) return;
        try (var stream = Files.newDirectoryStream(modsDir)) {
            for (Path path : stream) {
                if (!Files.isRegularFile(path)) continue;
                String fn = path.getFileName().toString();

                if (fn.endsWith(".jar")) {
                    String id = readModId(path);
                    if (id != null && DISABLED_IDS.contains(id)) {
                        try {
                            Path target = path.resolveSibling(fn + ".disabled");
                            Files.move(path, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            MutoLog.info("reconciled disable rename: {} -> {}", fn, target.getFileName());
                        } catch (Throwable t) {
                            MutoLog.warn("file {} locked on disk, rename deferred: {}", fn, t.getMessage());
                        }
                    }
                } else if (fn.endsWith(".jar.disabled")) {
                    String id = readModId(path);
                    if (id != null && !DISABLED_IDS.contains(id)) {
                        try {
                            String targetName = fn.substring(0, fn.length() - ".disabled".length());
                            Path target = path.resolveSibling(targetName);
                            Files.move(path, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            MutoLog.info("reconciled enable rename: {} -> {}", fn, target.getFileName());
                        } catch (Throwable t) {
                            MutoLog.warn("file {} locked on disk, rename deferred: {}", fn, t.getMessage());
                        }
                    }
                }
            }
        } catch (Throwable t) {
            MutoLog.warn("failed reconciling disabled file renames: {}", t.getMessage());
        }
    }

    private static String readModId(Path path) {
        try (java.util.jar.JarFile jf = new java.util.jar.JarFile(path.toFile())) {
            java.util.jar.JarEntry entry = jf.getJarEntry("fabric.mod.json");
            if (entry == null) return null;
            try (java.io.InputStream in = jf.getInputStream(entry);
                 java.io.InputStreamReader reader = new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8)) {
                com.google.gson.JsonObject root = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
                if (root.has("id")) {
                    return root.get("id").getAsString();
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }
}
