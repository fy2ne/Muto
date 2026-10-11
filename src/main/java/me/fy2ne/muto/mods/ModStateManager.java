package me.fy2ne.muto.mods;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.fy2ne.muto.MutoLog;
import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ModStateManager {
    public static final ModStateManager INSTANCE = new ModStateManager();

    private static final Set<String> CORE_MOD_IDS = Set.of(
            "minecraft",
            "fabricloader",
            "fabric",
            "fabric-api",
            "java",
            "muto",
            "fabric-api-base",
            "fabric-lifecycle-events-v1",
            "fabric-resource-loader-v0"
    );

    private final Set<String> stagedDisables = new HashSet<>();
    private final Set<String> stagedEnables = new HashSet<>();

    private ModStateManager() {}

    public boolean isCore(String modId) {
        if (modId == null) return false;
        String lower = modId.toLowerCase(Locale.ROOT);
        return CORE_MOD_IDS.contains(lower) || lower.startsWith("fabric-api") || lower.equals("muto");
    }

    public boolean isProtectedMod(String modId) {
        return isCore(modId);
    }

    public synchronized boolean isStaged(String modId) {
        return stagedDisables.contains(modId) || stagedEnables.contains(modId);
    }

    public synchronized boolean getStagedState(String modId) {
        if (stagedEnables.contains(modId)) return true;
        if (stagedDisables.contains(modId)) return false;
        return !DisabledModsRegistry.isDisabled(modId);
    }

    public synchronized void stageToggle(String modId) {
        if (isCore(modId)) return;
        boolean isLoaded = FabricLoader.getInstance().getModContainer(modId).isPresent();
        boolean isDisabled = DisabledModsRegistry.isDisabled(modId);
        boolean isUnloaded = !isLoaded && !isDisabled;

        if (isDisabled || isUnloaded) {
            if (stagedEnables.contains(modId)) {
                stagedEnables.remove(modId);
            } else {
                stagedEnables.add(modId);
            }
        } else {
            if (stagedDisables.contains(modId)) {
                stagedDisables.remove(modId);
            } else {
                stagedDisables.add(modId);
            }
        }
    }

    public synchronized void toggleMod(DiskMod mod) {
        if (isCore(mod.id())) return;

        if (mod.disabled()) {
            if (stagedEnables.contains(mod.id())) {
                stagedEnables.remove(mod.id());
            } else {
                stagedEnables.add(mod.id());
            }
        } else {
            if (stagedDisables.contains(mod.id())) {
                stagedDisables.remove(mod.id());
            } else {
                stagedDisables.add(mod.id());
            }
        }
    }

    public synchronized boolean isStagedForDisable(String modId) {
        return stagedDisables.contains(modId);
    }

    public synchronized boolean isStagedForEnable(String modId) {
        return stagedEnables.contains(modId);
    }

    public synchronized boolean hasStagedChanges() {
        return !stagedDisables.isEmpty() || !stagedEnables.isEmpty();
    }

    public synchronized int getStagedCount() {
        return stagedDisables.size() + stagedEnables.size();
    }

    public synchronized void clearStaged() {
        stagedDisables.clear();
        stagedEnables.clear();
    }

    public synchronized Set<String> getStagedDisables() {
        return new HashSet<>(stagedDisables);
    }

    public synchronized Set<String> getStagedEnables() {
        return new HashSet<>(stagedEnables);
    }

    public List<DiskMod> scanDiskMods() {
        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
        if (!Files.isDirectory(modsDir)) {
            return Collections.emptyList();
        }

        Set<String> activeLoaderIds = new HashSet<>();
        for (var c : FabricLoader.getInstance().getAllMods()) {
            activeLoaderIds.add(c.getMetadata().getId());
        }

        Map<String, DiskMod> result = new LinkedHashMap<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(modsDir)) {
            for (Path path : stream) {
                if (!Files.isRegularFile(path)) continue;
                String filename = path.getFileName().toString();

                boolean isDisabled = filename.endsWith(".jar.disabled") || filename.endsWith(".disabled") || filename.endsWith(".deactivation");
                boolean isNormalJar = filename.endsWith(".jar");

                if (!isDisabled && !isNormalJar) continue;

                DiskMod parsed = parseModMetadata(path, isDisabled, activeLoaderIds);
                if (parsed != null) {
                    result.put(parsed.id(), parsed);
                }
            }
        } catch (Exception ex) {
            MutoLog.warn("failed scanning disk mods: {}", ex.getMessage());
        }

        List<DiskMod> list = new ArrayList<>(result.values());
        list.sort(Comparator.comparing(DiskMod::name, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    private DiskMod parseModMetadata(Path path, boolean disabled, Set<String> activeLoaderIds) {
        try (JarFile jf = new JarFile(path.toFile())) {
            JarEntry fmj = jf.getJarEntry("fabric.mod.json");
            if (fmj == null) return null;

            try (InputStream in = jf.getInputStream(fmj);
                 InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                if (!root.has("id")) return null;

                String id = root.get("id").getAsString();
                String name = root.has("name") ? root.get("name").getAsString() : id;
                String version = root.has("version") ? root.get("version").getAsString() : "?";

                boolean active = activeLoaderIds.contains(id);
                boolean core = isCore(id);

                boolean isGloballyDisabled = disabled || DisabledModsRegistry.isDisabled(id);
                return new DiskMod(id, name, version, path, isGloballyDisabled, active, core);
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    public synchronized List<String> applyFileRenames() {
        List<String> logMessages = new ArrayList<>();
        List<DiskMod> all = scanDiskMods();
        Map<String, DiskMod> byId = new HashMap<>();
        for (DiskMod m : all) byId.put(m.id(), m);

        for (String id : stagedDisables) {
            DisabledModsRegistry.setDisabled(id, true);
            DiskMod m = byId.get(id);
            if (m != null) {
                Path curr = m.filePath();
                String currName = curr.getFileName().toString();
                if (!currName.endsWith(".disabled") && !currName.endsWith(".deactivation")) {
                    String targetName = currName + ".disabled";
                    Path target = curr.resolveSibling(targetName);
                    try {
                        Files.move(curr, target);
                        logMessages.add("Disabled: " + id + " -> " + targetName);
                    } catch (Exception ex) {
                        MutoLog.info("file {} locked on disk by Windows, recorded in disabled registry", currName);
                        logMessages.add("Disabled: " + id + " (unmounted; file will rename on reboot)");
                    }
                } else {
                    logMessages.add("Disabled: " + id);
                }
            }
        }

        for (String id : stagedEnables) {
            DisabledModsRegistry.setDisabled(id, false);
            DiskMod m = byId.get(id);
            if (m != null) {
                Path curr = m.filePath();
                String currName = curr.getFileName().toString();
                String targetName = null;
                if (currName.endsWith(".disabled")) {
                    targetName = currName.substring(0, currName.length() - ".disabled".length());
                } else if (currName.endsWith(".deactivation")) {
                    targetName = currName.substring(0, currName.length() - ".deactivation".length());
                }
                if (targetName != null) {
                    Path target = curr.resolveSibling(targetName);
                    try {
                        Files.move(curr, target);
                        logMessages.add("Enabled: " + id + " -> " + targetName);
                    } catch (Exception ex) {
                        MutoLog.warn("failed renaming {} to enabled: {}", currName, ex.getMessage());
                        logMessages.add("Enabled: " + id + " (via registry)");
                    }
                } else {
                    logMessages.add("Enabled: " + id);
                }
            }
        }

        stagedDisables.clear();
        stagedEnables.clear();
        return logMessages;
    }
}
