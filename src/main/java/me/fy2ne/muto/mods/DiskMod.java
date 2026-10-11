package me.fy2ne.muto.mods;

import java.nio.file.Path;

public record DiskMod(
        String id,
        String name,
        String version,
        Path filePath,
        boolean disabled,
        boolean activeInLoader,
        boolean isCore
) {
    public boolean isNew() {
        return !disabled && !activeInLoader;
    }
}
