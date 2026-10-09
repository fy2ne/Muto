package me.fy2ne.muto.engine;

import me.fy2ne.muto.api.ModDiff;

import java.util.*;

public final class ModSnapshot {
    private final Map<String, ScannedMod> mods;
    private final Map<String, ModTier> tiers;
    private final long timestamp;

    public ModSnapshot(List<ScannedMod> scanned) {
        this.mods = new LinkedHashMap<>();
        this.tiers = new LinkedHashMap<>();
        this.timestamp = System.currentTimeMillis();

        for (ScannedMod mod : scanned) {
            this.mods.put(mod.id(), mod);
            this.tiers.put(mod.id(), ModTier.classify(mod));
        }
    }

    public static ModSnapshot empty() {
        return new ModSnapshot(Collections.emptyList());
    }

    public Map<String, ScannedMod> mods() {
        return Collections.unmodifiableMap(mods);
    }

    public Map<String, ModTier> tiers() {
        return Collections.unmodifiableMap(tiers);
    }

    public ScannedMod get(String id) {
        return mods.get(id);
    }

    public ModTier tier(String id) {
        return tiers.getOrDefault(id, ModTier.STUBBORN);
    }

    public long timestamp() {
        return timestamp;
    }

    public List<ScannedMod> reloadableMods() {
        List<ScannedMod> list = new ArrayList<>();
        for (ScannedMod mod : mods.values()) {
            if (tier(mod.id()).isReloadable()) {
                list.add(mod);
            }
        }
        return list;
    }

    public List<ScannedMod> stubbornMods() {
        List<ScannedMod> list = new ArrayList<>();
        for (ScannedMod mod : mods.values()) {
            if (!tier(mod.id()).isReloadable()) {
                list.add(mod);
            }
        }
        return list;
    }

    public ModDiff diffAgainst(ModSnapshot other) {
        Set<String> added = new HashSet<>();
        Set<String> removed = new HashSet<>();
        Set<String> updated = new HashSet<>();
        Set<String> unchanged = new HashSet<>();

        Set<String> allIds = new HashSet<>(this.mods.keySet());
        allIds.addAll(other.mods.keySet());

        for (String id : allIds) {
            ScannedMod cur = this.mods.get(id);
            ScannedMod nxt = other.mods.get(id);

            if (cur == null && nxt != null) {
                added.add(id);
            } else if (cur != null && nxt == null) {
                removed.add(id);
            } else if (cur != null && nxt != null) {
                if (!Objects.equals(cur.hash(), nxt.hash())) {
                    updated.add(id);
                } else {
                    unchanged.add(id);
                }
            }
        }

        return new ModDiff(added, removed, updated, unchanged);
    }
}
