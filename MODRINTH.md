<div align="center">

# Muto

**Dynamic runtime mod reloader and hot-swap pipeline for Fabric.**

[![Minecraft Java Edition](https://img.shields.io/badge/Minecraft-Java%20Edition-00AF5C?style=flat-square&logo=minecraft&logoColor=white)](https://fabricmc.net/)
[![Fabric](https://img.shields.io/badge/Platform-Fabric-DBB26A?style=flat-square)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-JVM-orange?style=flat-square)](https://openjdk.org/)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue?style=flat-square)](LICENSE)

</div>

---

## Overview

**Muto** hot-reloads your active mod set on the fly. Whenever you add, remove, or update Fabric mod jars in your `mods/` directory, Muto dynamically applies those changes directly from the title screen without restarting the JVM or relaunching Minecraft.

Instead of restarting the entire game process just to test code or mod updates, Muto rescans your `mods/` directory, isolates candidate classes in disposable child classloaders, thaws registry entries for new registrations, and re-invokes entrypoints cleanly.

---

## Key Capabilities

- **Runtime Mod Hot-Reloading**: Rescans your `mods/` directory and applies added, removed, or updated mod jars directly from the main menu.
- **Child ClassLoader Isolation**: Dynamic mods are loaded into isolated `MutoClassLoader` instances that can be discarded and garbage-collected when reloaded, preventing stale class leaks.
- **Registry Lifecycle Control**: Hooks into Vanilla's `MappedRegistry` to unfreeze frozen registries during the reload pass, allowing new blocks, items, or identifiers to register without throwing `IllegalStateException`.
- **Atomic Failure Rollback**: If a new or updated mod throws an exception during initialization, Muto catches the fault, aborts the swap, rolls back to the prior stable mod snapshot, and surfaces the stack trace.
- **Mod Lifecycle Events & Developer API**: Other mods can hook into `MutoEvents.RELOAD_START` and `MutoEvents.RELOAD_FINISH` to tear down caches, flush listeners, or re-initialize custom subsystems.
- **Enhanced Bytecode Redefinition (JBR/DCEVM)**: When running under JetBrains Runtime with `-XX:+AllowEnhancedClassRedefinition`, Muto leverages live class retransformation for deeper class swaps.
- **ModMenu Inspection**: Adds a config screen inside ModMenu listing all detected mods, their versions, and their classification tier (`Clean`, `Standard`, or `Stubborn`).

---

## Mod Classification Tiers

Muto classifies installed jars into three operational tiers to protect game stability:

1. **Clean**: Lightweight client tweaks and UI mods without native hooks or intrusive mixins. Safest for rapid live swapping.
2. **Standard**: Mods introducing registry entries and common logic. Managed through child classloader rotation and registry thawing.
3. **Stubborn / Core**: Core infrastructure mods (`minecraft`, `fabricloader`, `fabric-api`, root bytecode mutators). These remain locked in the root loader to prevent JVM linkage corruption.

---

## Developer API

Mods can track reload passes and tear down static references or reload internal state by depending on Muto's event bus:

```java
// Register state cleanup before reload begins
MutoEvents.RELOAD_START.register((startEpochMs, diff) -> {
    if (diff.updated().contains("your_mod_id") || diff.removed().contains("your_mod_id")) {
        YourSubsystem.shutdown();
    }
});

// React after reload settles
MutoEvents.RELOAD_FINISH.register((endEpochMs, diff, success, durationMs, error) -> {
    if (success && diff.added().contains("your_mod_id")) {
        YourSubsystem.bootstrap();
    }
});
```

The `ModDiff` record gives you direct access to `added()`, `removed()`, `updated()`, and `unchanged()` mod identifier sets.

---

## Usage

1. Place `muto-<version>.jar` into your `.minecraft/mods/` directory along with Fabric API.
2. Launch Minecraft.
3. To reload:
   - Save and exit to the **Title Screen** (in-world reloads are disabled to prevent world state desync).
   - Click the reload icon (**↻**) on the title screen.
   - The reload pipeline executes, swaps loaders, re-executes entrypoints, and updates your active mod set.

---

## Requirements & Compatibility

- **Platform:** Fabric Loader
- **Minecraft:** Java Edition
- **Java Runtime:** JVM (Java 21 / 25+, JetBrains Runtime optional for DCEVM)
