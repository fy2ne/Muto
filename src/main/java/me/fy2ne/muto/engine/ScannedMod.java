package me.fy2ne.muto.engine;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record ScannedMod(
        String id,
        String version,
        Path jarPath,
        String hash,
        boolean hasMixins,
        Map<String, List<String>> entrypoints,
        long sizeBytes
) {
    public boolean hasEntrypoint(String key) {
        return entrypoints != null && entrypoints.containsKey(key);
    }

    public List<String> getClassesFor(String key) {
        if (entrypoints == null) return Collections.emptyList();
        return entrypoints.getOrDefault(key, Collections.emptyList());
    }
}
