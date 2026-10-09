package me.fy2ne.muto.engine;

import java.util.Set;

public enum ModTier {
    CLEAN(1, "Clean", "hotswappable client mod"),
    STANDARD(2, "Standard", "reloadable via child classloader"),
    STUBBORN(3, "Stubborn", "loader-bound, excluded from live reload");

    private static final Set<String> CORE_IDS = Set.of(
            "minecraft",
            "fabricloader",
            "fabric",
            "java",
            "muto"
    );

    private final int level;
    private final String label;
    private final String desc;

    ModTier(int level, String label, String desc) {
        this.level = level;
        this.label = label;
        this.desc = desc;
    }

    public int level() {
        return level;
    }

    public String label() {
        return label;
    }

    public String desc() {
        return desc;
    }

    public boolean isReloadable() {
        return this != STUBBORN;
    }

    public static ModTier classify(ScannedMod mod) {
        String id = mod.id().toLowerCase();
        if (CORE_IDS.contains(id) || id.startsWith("fabric-") || id.startsWith("quilt")) {
            return STUBBORN;
        }

        // clean: pure client-side tweaks without server or common hooks
        if (mod.hasEntrypoint("client") && !mod.hasEntrypoint("main") && !mod.hasEntrypoint("server") && !mod.hasMixins()) {
            return CLEAN;
        }

        return STANDARD;
    }
}
