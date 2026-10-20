package me.fy2ne.muto.mods;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

public record DiskMod(
        String id,
        String name,
        String version,
        Path filePath,
        boolean disabled,
        boolean activeInLoader,
        boolean isCore,
        Map<String, String> depends
) {
    public DiskMod(
            String id,
            String name,
            String version,
            Path filePath,
            boolean disabled,
            boolean activeInLoader,
            boolean isCore
    ) {
        this(id, name, version, filePath, disabled, activeInLoader, isCore, Collections.emptyMap());
    }

    public boolean isNew() {
        return !disabled && !activeInLoader;
    }
}
