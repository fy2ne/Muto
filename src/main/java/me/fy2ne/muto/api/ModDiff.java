package me.fy2ne.muto.api;

import java.util.Collections;
import java.util.Set;

public record ModDiff(
        Set<String> added,
        Set<String> removed,
        Set<String> updated,
        Set<String> unchanged
) {
    public static final ModDiff EMPTY = new ModDiff(
            Collections.emptySet(),
            Collections.emptySet(),
            Collections.emptySet(),
            Collections.emptySet()
    );

    public boolean hasChanges() {
        return !added.isEmpty() || !removed.isEmpty() || !updated.isEmpty();
    }
}
