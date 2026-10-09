package me.fy2ne.muto.engine;

import me.fy2ne.muto.api.ModDiff;
import java.util.List;

public record ReloadHistoryEntry(
        long timestamp,
        long durationMs,
        ModDiff diff,
        boolean success,
        List<String> logs
) {}
