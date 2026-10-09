package me.fy2ne.muto.engine;

import me.fy2ne.muto.api.ModDiff;

import java.util.List;

public record ReloadPlan(
        ModSnapshot currentSnapshot,
        ModSnapshot targetSnapshot,
        ModDiff diff,
        List<ScannedMod> reloadable,
        List<ScannedMod> stubborn
) {
    public boolean hasWork() {
        return diff != null && diff.hasChanges();
    }
}
