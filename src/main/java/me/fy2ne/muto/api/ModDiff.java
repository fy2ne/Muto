package me.fy2ne.muto.api;

import java.util.Collections;
import java.util.Set;

/**
 * Immutable snapshot of the difference between two mod states.
 *
 * <p>Produced by {@link MutoApi#plan()} and carried through the reload pipeline.
 * Received by {@link MutoEvents.ReloadStartListener} and
 * {@link MutoEvents.ReloadFinishListener}.
 *
 * <p>All sets are unmodifiable and never null.
 *
 * @param added     mod IDs present in the new state but not the old
 * @param removed   mod IDs present in the old state but not the new
 * @param updated   mod IDs present in both states but with changed jar content (hash mismatch)
 * @param unchanged mod IDs present in both states with identical jar content
 */
public record ModDiff(
        Set<String> added,
        Set<String> removed,
        Set<String> updated,
        Set<String> unchanged
) {
    /** An empty diff representing no mod changes. */
    public static final ModDiff EMPTY = new ModDiff(
            Collections.emptySet(),
            Collections.emptySet(),
            Collections.emptySet(),
            Collections.emptySet()
    );

    /**
     * Returns {@code true} if any mods were added, removed, or updated.
     * Returns {@code false} for a no-op diff (only unchanged mods).
     */
    public boolean hasChanges() {
        return !added.isEmpty() || !removed.isEmpty() || !updated.isEmpty();
    }
}
