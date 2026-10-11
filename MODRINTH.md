<div align="center">

<img src="https://raw.githubusercontent.com/fy2ne/muto/main/src/main/resources/assets/muto/icon.png" width="96" height="96" alt="Muto Logo" />

# Muto

**The industrial-grade hot mod reloader for Minecraft.**

Enable, disable, load, and hot-swap Fabric mods live in-game without restarting Minecraft.

[![CurseForge](https://img.shields.io/badge/CurseForge-muto-F16436?style=flat-square)](https://www.curseforge.com/minecraft/mc-mods/muto)
[![Minecraft Java Edition](https://img.shields.io/badge/Minecraft-26.3-00AF5C?style=flat-square&logo=minecraft&logoColor=white)](https://fabricmc.net/)
[![Fabric](https://img.shields.io/badge/Platform-Fabric-DBB26A?style=flat-square)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-25+-orange?style=flat-square)](https://openjdk.org/)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue?style=flat-square)](https://github.com/fy2ne/muto/blob/main/LICENSE)

</div>

---

## Overview

In Minecraft's history, resource packs have `F3+T` and datapacks have `/reload`. **Mods never had an in-game reload.** Whenever you tweaked a mod, tested an update, or needed to turn off a conflicting mod, you had to close the game, wait minutes for Java and assets to restart, and repeat that dozens of times.

**Muto changes that.**

Muto provides a real-time hot-swap engine built directly into Minecraft and ModMenu:
- **Toggle mods on or off** directly in your mod list.
- **Load new mods** placed in your `mods/` directory without restarting.
- **Stage your changes** and review what's being enabled or disabled before committing.
- **Hot reload in seconds** using dynamic ClassLoader unmounting, registry thawing, and mixin retransformation.

<br />

<div align="center">

![Muto in ModMenu](https://raw.githubusercontent.com/fy2ne/muto/main/docs/images/screenshot-modmenu.png)

</div>

---

## Key Features

### Native ModMenu Integration
- **Direct Action Buttons**: Enable, disable, or load mods right from ModMenu's mod description pane with crisp, vanilla-styled monochrome pixel-art controls.
- **Mod State Badges**: Visual tags identifying mod state at a glance:
  - `Disabled` (red) — Mod jar renamed to `.jar.disabled`.
  - `Unloaded` (gold) — Mod jar detected in `mods/` but not yet loaded into runtime.
  - `Pending: Disable`, `Pending: Enable`, `Pending: Load` (green/gold) — Staged changes awaiting confirmation.
- **Smart Filter Cycle**: A dedicated filter button cycling through **All**, **Active**, **Disabled**, and **Unloaded** mods.
- **Safe Staging Workflow**: Click **Save (N)** to open a change review screen listing exact operations before applying.
- **Disabled Mod Protection**: Prevents broken config screens from opening when a mod is inactive.

### Industrial Reload Pipeline
- **Knot ClassLoader Rotation**: Dynamically unmounts and remounts mod classpaths at runtime.
- **Registry Thawing & Tombstoning**: Safely unfreezes Minecraft's `MappedRegistry` to register or prune mod entries.
- **Packet & KeyMapping Sync**: Re-registers custom network payload types and rebuilds vanilla keybind arrays.
- **Safe Boundary Manager**:
  - **Title Screen**: Instant hot reload in seconds.
  - **Singleplayer Worlds**: Safely saves world chunks, exits cleanly, applies the mod hot-swap, and reconnects the player automatically.
  - **Multiplayer Servers**: Gracefully disconnects, hot-swaps client mods, and reconnects to the server.

---

## Requirements

- **Platform:** Fabric Loader 0.16.0+
- **Minecraft:** 26.3
- **Java Runtime:** Java 25+
- **Dependencies:** Fabric API (Zero external config libraries required; ModMenu optional)

---

## Developer API

Other mods and developer tools can integrate with Muto as an optional soft dependency.

```groovy
dependencies {
    modCompileOnly "me.fy2ne:muto:0.2.0-beta"
}
```

```java
import me.fy2ne.muto.api.MutoApi;
import me.fy2ne.muto.api.MutoEvents;

// Check availability & trigger safe reload
if (MutoApi.isAvailable() && !MutoApi.isReloading()) {
    MutoApi.triggerBoundaryReload(currentScreen);
}

// Subscribe to reload lifecycle
MutoEvents.RELOAD_START.register((startEpochMs, diff) -> {
    // Teardown caches or listeners
});

MutoEvents.RELOAD_FINISH.register((endEpochMs, diff, success, durationMs, error) -> {
    // Re-initialize state
});
```

---

## Links

- **Documentation & Wiki:** [https://muto.fy2ne.me](https://muto.fy2ne.me)
- **Source Code & GitHub:** [https://github.com/fy2ne/muto](https://github.com/fy2ne/muto)
- **Issue Tracker:** [https://github.com/fy2ne/muto/issues](https://github.com/fy2ne/muto/issues)
- **CurseForge:** [https://www.curseforge.com/minecraft/mc-mods/muto](https://www.curseforge.com/minecraft/mc-mods/muto)
