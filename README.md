<div align="center">

<img src="src/main/resources/assets/muto/icon.png" width="96" height="96" alt="Muto Runtime Logo" />

# Muto Runtime

**Hot-swap engine for Fabric mods. No restart. Ever.**

[![Modrinth](https://img.shields.io/modrinth/dt/muto?label=Modrinth&color=00AF5C)](https://modrinth.com/mod/muto)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.x-brightgreen)](https://fabricmc.net/)
[![Platform](https://img.shields.io/badge/Platform-Fabric-DBB26A)](https://fabricmc.net/)
[![License](https://img.shields.io/badge/license-LGPL--3.0-blue.svg)](LICENSE)

[Modrinth](https://modrinth.com/mod/muto) · [Documentation](https://muto.fy2ne.me) · [Issues](https://github.com/fy2ne/muto/issues)

</div>

---

## Overview

Muto Runtime is a **developer library** that provides runtime mod hot-swap primitives for Fabric. It is not a player-facing mod — it is the engine layer that other mods build on top of.

Hot-reloading Fabric mods inside a running JVM has long been considered impractical. Muto does it by combining three runtime techniques:

1. **Knot Classpath Injection** — a newly added or updated mod jar is appended to Fabric Loader's running Knot classpath (with nested jars), so its classes resolve without a restart.
2. **Registry Thawing** — Minecraft freezes its `MappedRegistry` at boot. Muto clears that frozen flag at the JVM level, prunes old namespace entries, lets the new mod re-register, then refreezes. The game never sees it happen.
3. **Dynamic Mixin Registration** — a new jar's mixin configurations are registered with the global Mixin service and apply to every class the JVM defines after the reload; classes that are already loaded cannot be re-woven.

---

## Integration (For Mod Developers)

Declare Muto Runtime as an optional compile-only dependency. Your mod works fine without it — but if Muto is present, users get zero-restart hot-reloading.

### Gradle

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

### Trigger a Reload

```java
import net.fabricmc.loader.api.FabricLoader;
import me.fy2ne.muto.api.MutoApi;

public class YourDownloaderIntegration {
    public static void onModDownloaded(java.nio.file.Path jar) {
        if (!FabricLoader.getInstance().isModLoaded("muto")) return;

        if (MutoApi.isSafeToReload()) {
            MutoApi.reloadAsync(stage -> System.out.println(stage))
                .thenAccept(result -> {
                    if (result.success()) {
                        System.out.println("hot-swapped in " + result.durationMs() + "ms");
                    } else {
                        System.err.println("swap failed: " + result.error());
                    }
                });
        }
    }
}
```

### Hook Lifecycle Events

```java
import me.fy2ne.muto.api.MutoEvents;

MutoEvents.RELOAD_START.register((startMs, diff) -> {
    // close stale network connections, clear texture caches
});

MutoEvents.RELOAD_FINISH.register((endMs, diff, success, durationMs, error) -> {
    // refresh UI, re-bind resources, update lists
});
```

---

## API Surface

| Method | Returns | Notes |
|---|---|---|
| `MutoApi.isAvailable()` | `boolean` | Is Muto present at runtime |
| `MutoApi.isReloading()` | `boolean` | Reload in flight |
| `MutoApi.isSafeToReload()` | `boolean` | Title screen, not in a world |
| `MutoApi.plan()` | `ReloadPlan` | Diff /mods without executing |
| `MutoApi.reloadAsync()` | `CompletableFuture<ReloadResult>` | Fire and forget reload |
| `MutoApi.reloadAsync(notifier)` | `CompletableFuture<ReloadResult>` | Reload with stage callbacks |
| `MutoApi.getReloadableModIds()` | `Set<String>` | Dynamic mods in current snapshot |
| `MutoApi.getStubbornModIds()` | `Set<String>` | Core mods that require a restart |
| `MutoApi.startFileWatcher()` | `boolean` | Opt-in dev trigger watcher |
| `MutoApi.stopFileWatcher()` | `boolean` | Stop the watcher |

---

## Mod Classification Tiers

| Tier | Type | Reload Behavior |
|---|---|---|
| **Tier 1** | Clean (UI, HUD, tweaks) | Instant, no registry touching |
| **Tier 2** | Standard (blocks, items, recipes) | Full pipeline with registry thaw |
| **Tier 3** | Stubborn (Fabric Loader, Fabric API, core mixins) | Locked — cannot be reloaded |

---

## Gradle `mutoWatch` (Dev Hot-Reload Pipeline)

For developers building mods on top of Muto Runtime, the project ships a `mutoWatch` Gradle task that watches `src/main/java` and `src/main/resources`, recompiles on change, and writes a `muto.trigger` file to `run/`. Muto Runtime picks this up via its opt-in file watcher and hot-swaps itself in the running game:

```bash
./gradlew mutoWatch
```

Activate the watcher in your mod's `onInitializeClient`:

```java
MutoApi.startFileWatcher();
```

---

## Requirements

- Minecraft Java Edition 26.3
- Fabric Loader ≥ 0.16.0
- Fabric API ≥ 0.161.0+26.3
- Java 25+
- Client-side only

### Optional — Class Redefinition

A class-redefinition-capable JVM ([JetBrains Runtime (JBR)](https://www.jetbrains.com/help/idea/configuring-build-jdk.html) or DCEVM) is **not required**. Muto reloads by injecting jars into Knot and re-running initializers, which works on any JDK.
