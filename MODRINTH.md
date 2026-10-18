<div align="center">

<img src="https://raw.githubusercontent.com/fy2ne/muto/main/src/main/resources/assets/muto/icon.png" width="96" height="96" alt="Muto Runtime Logo" />

# Muto Runtime

**Runtime hot-swap engine for Fabric mods.**

[![Minecraft Java Edition](https://img.shields.io/badge/Minecraft-26.3-00AF5C?style=flat-square&logo=minecraft&logoColor=white)](https://fabricmc.net/)
[![Fabric](https://img.shields.io/badge/Platform-Fabric-DBB26A?style=flat-square)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-25+-orange?style=flat-square)](https://openjdk.org/)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue?style=flat-square)](https://github.com/fy2ne/muto/blob/main/LICENSE)

</div>

---

## What is Muto Runtime?

Muto Runtime is a **developer library** — not a player-facing mod. It provides the runtime primitives that make it possible to load, unload, and hot-swap Fabric mods while Minecraft is running, without a restart.

For over 17 years, mod hot-reloading in Minecraft was considered impossible. Muto breaks that by chaining three JVM-level techniques that nobody had combined before for Fabric:

1. **Child ClassLoader Rotation** — reloadable mods run in isolated `URLClassLoader` instances that can be garbage-collected and replaced mid-session.
2. **Registry Thawing** — Minecraft's frozen `MappedRegistry` is surgically unfrozen via reflection and a Mixin accessor, allowing new entries to register before refreezing.
3. **JVM Agent Bytecode Hooks** — ships as both a Fabric mod and a Java agent (`Premain-Class`), enabling class retransformation on supported runtimes (JBR, DCEVM).

---

## For Mod Developers

Add Muto Runtime as an optional soft-dependency and call the API when you want to trigger a hot-swap.

### Gradle Dependency

```groovy
repositories {
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
    }
}

dependencies {
    modCompileOnly "maven.modrinth:muto:0.3.0-beta"
}
```

### Runtime Detection + Reload

```java
import net.fabricmc.loader.api.FabricLoader;
import me.fy2ne.muto.api.MutoApi;

public class YourModIntegration {
    public static void onModDownloaded(java.nio.file.Path jarPath) {
        if (!FabricLoader.getInstance().isModLoaded("muto")) return;

        if (MutoApi.isSafeToReload()) {
            MutoApi.reloadAsync().thenAccept(result -> {
                if (result.success()) {
                    System.out.println("reloaded in " + result.durationMs() + "ms");
                }
            });
        }
    }
}
```

### Lifecycle Events

```java
import me.fy2ne.muto.api.MutoEvents;

MutoEvents.RELOAD_START.register((startMs, diff) -> {
    // tear down caches, close stale connections
});

MutoEvents.RELOAD_FINISH.register((endMs, diff, success, durationMs, error) -> {
    // refresh UI, update mod lists, re-bind resources
});
```

### Opt-in File Watcher (Dev Tool)

For developer tooling that uses the `muto.trigger` / `muto.result` file-based hot-reload pipeline (e.g. Gradle `mutoWatch` task):

```java
MutoApi.startFileWatcher(); // begins polling for muto.trigger
MutoApi.stopFileWatcher();  // stop when done
```

---

## Public API Reference

| Method | Returns | Description |
|---|---|---|
| `MutoApi.isAvailable()` | `boolean` | Muto Runtime is present in runtime |
| `MutoApi.isReloading()` | `boolean` | Reload transaction is currently running |
| `MutoApi.isSafeToReload()` | `boolean` | Client is on title screen, not in a world |
| `MutoApi.plan()` | `ReloadPlan` | Scan /mods and compute a diff without executing |
| `MutoApi.reloadAsync()` | `CompletableFuture<ReloadResult>` | Execute reload pipeline asynchronously |
| `MutoApi.reloadAsync(notifier)` | `CompletableFuture<ReloadResult>` | Reload with per-stage progress callback |
| `MutoApi.getReloadableModIds()` | `Set<String>` | IDs of all dynamically reloadable mods |
| `MutoApi.getStubbornModIds()` | `Set<String>` | IDs of core mods locked in root classloader |
| `MutoApi.startFileWatcher()` | `boolean` | Start opt-in dev trigger watcher |
| `MutoApi.stopFileWatcher()` | `boolean` | Stop the trigger watcher |

---

## Mod Tier Classification

Muto classifies all discovered jars into tiers at startup:

- **Tier 1 — Clean**: Pure client tweaks, HUD mods, UI mods. Reloads in milliseconds, no registry involvement.
- **Tier 2 — Standard**: Content mods that register blocks, items, or recipes. Re-executes `ModInitializer` alongside registry thaw.
- **Tier 3 — Stubborn**: Fabric Loader, Fabric API, core mixin injectors. Locked permanently in the root classloader. Cannot be reloaded.

---

## Compatibility

- **Minecraft**: Java Edition 26.3
- **Loader**: Fabric Loader ≥ 0.16.0
- **Java**: 25+
- **JBR / DCEVM**: Optional — enables enhanced class redefinition beyond ClassLoader rotation
- **Environment**: Client-side only

---

## For Players

Muto Runtime runs silently in the background. You won't see any UI from it directly — it powers other mods that build on top of it. If a mod you're using requires Muto Runtime, just install it alongside that mod.
