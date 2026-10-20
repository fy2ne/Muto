# Public API Reference (`me.fy2ne.muto.api`)

All primary integrations communicate through `MutoApi`.

## Checking Availability

```java
if (MutoApi.isAvailable()) {
    // Muto Runtime is installed
}
```

## Reload Execution

### Simple Async Reload
```java
MutoApi.reloadAsync().thenAccept(result -> {
    if (result.success()) {
        System.out.println("Hot-swapped in " + result.durationMs() + "ms");
    } else {
        System.err.println("Reload failed: " + result.error());
    }
});
```

### Reload with Progress Stage Callbacks
```java
MutoApi.reloadAsync(stage -> {
    System.out.println("Current stage: " + stage);
});
```

### Boundary Reload (Safe In-World / In-Server)
```java
// Safely disconnects, applies reload, and reconnects to server or singleplayer world
MutoApi.reloadWithBoundary(currentScreen);
```

## Lifecycle Events

```java
// Fired immediately before reload pipeline execution
MutoEvents.RELOAD_START.register((startEpochMs, diff) -> {
    System.out.println("Reload started: +" + diff.added().size() + " -" + diff.removed().size());
});

// Fired after reload completes (success, failure, or no-op)
MutoEvents.RELOAD_FINISH.register((endEpochMs, diff, success, durationMs, error) -> {
    System.out.println("Reload finished: " + (success ? "OK" : "ROLLED BACK") + " in " + durationMs + "ms");
});
```

## Folder Watcher Control

```java
MutoApi.setFolderWatcherEnabled(true);
boolean active = MutoApi.isFolderWatcherEnabled();
```

## History & Audit

```java
List<ReloadHistoryEntry> history = MutoApi.getHistory();
for (ReloadHistoryEntry entry : history) {
    System.out.printf("[%s] %dms - %s%n", 
        entry.success() ? "OK" : "FAILED", 
        entry.durationMs(), 
        entry.diff());
}
```
