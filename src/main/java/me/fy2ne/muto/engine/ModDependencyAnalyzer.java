package me.fy2ne.muto.engine;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.mods.DisabledModsRegistry;
import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ModDependencyAnalyzer {

    private static final Set<String> BUILTIN_SATISFIED = Set.of(
            "minecraft",
            "fabricloader",
            "fabric",
            "java",
            "muto",
            "fabric-api-base"
    );

    private static final Map<String, List<String>> MISSING_CACHE = new ConcurrentHashMap<>();

    private ModDependencyAnalyzer() {}

    public static List<String> getMissing(String modId) {
        if (modId == null) return Collections.emptyList();
        return MISSING_CACHE.getOrDefault(modId, Collections.emptyList());
    }

    public static void recordMissing(String modId, List<String> missing) {
        if (modId == null) return;
        if (missing == null || missing.isEmpty()) {
            MISSING_CACHE.remove(modId);
        } else {
            MISSING_CACHE.put(modId, new ArrayList<>(missing));
        }
    }

    public static void clearCache() {
        MISSING_CACHE.clear();
    }

    public static List<String> evaluateDependencies(String modId, Map<String, String> depends, Set<String> internalJijIds) {
        if (depends == null || depends.isEmpty()) {
            recordMissing(modId, Collections.emptyList());
            return Collections.emptyList();
        }

        Set<String> available = new HashSet<>(BUILTIN_SATISFIED);

        try {
            if (FabricLoader.getInstance() != null) {
                for (var container : FabricLoader.getInstance().getAllMods()) {
                    String activeId = container.getMetadata().getId();
                    if (!DisabledModsRegistry.isDisabled(activeId)) {
                        available.add(activeId);
                    }
                }
            }
        } catch (Throwable ignored) {}

        if (internalJijIds != null) {
            available.addAll(internalJijIds);
        }

        List<String> missing = new ArrayList<>();
        for (String depId : depends.keySet()) {
            if (depId.startsWith("fabric-api")) {
                // Fabric API submodules are satisfied if fabric or fabric-api is present
                if (available.contains("fabric") || available.contains("fabric-api") || available.contains(depId)) {
                    continue;
                }
            }

            if (!available.contains(depId)) {
                missing.add(depId);
            }
        }

        recordMissing(modId, missing);
        if (!missing.isEmpty()) {
            MutoLog.warn("mod '{}' is missing required dependencies: {}", modId, missing);
        }

        return missing;
    }

    public static Set<String> scanInternalJijIds(JarFile jf) {
        Set<String> ids = new HashSet<>();
        try {
            var entries = jf.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith("META-INF/jars/") && name.endsWith(".jar") && !entry.isDirectory()) {
                    try (InputStream in = jf.getInputStream(entry);
                         java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(in)) {
                        java.util.zip.ZipEntry ze;
                        while ((ze = zis.getNextEntry()) != null) {
                            if ("fabric.mod.json".equals(ze.getName())) {
                                JsonObject obj = JsonParser.parseReader(new InputStreamReader(zis, StandardCharsets.UTF_8)).getAsJsonObject();
                                if (obj.has("id")) {
                                    ids.add(obj.get("id").getAsString());
                                }
                                break;
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}
        return ids;
    }
}
