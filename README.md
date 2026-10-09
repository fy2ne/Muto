<div align="center">

# Muto

**Production-grade hot mod reloader for Minecraft**

Hot-reload added, removed, or updated mods on the fly from the title screen — without restarting Minecraft.

[![Modrinth](https://img.shields.io/modrinth/dt/muto?label=Modrinth&color=00AF5C)](https://modrinth.com/project/muto)
[![Minecraft](https://img.shields.io/badge/Minecraft-Java%20Edition-brightgreen)](https://fabricmc.net/)
[![Platform](https://img.shields.io/badge/Platform-Fabric-DBB26A)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-JVM-orange)](https://openjdk.org/)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)

[Download](https://modrinth.com/project/muto) · [Issues](https://github.com/fy2ne/muto/issues) · [Homepage](https://muto.fy2ne.me)

</div>

---

## About

Muto is a Fabric mod that hot-reloads your mod set without restarting Minecraft. Whenever you add new jars to your `mods/` directory, delete existing ones, or swap in an updated build, click reload from the title screen and Muto applies the changes live.

**Status: beta.** The reload engine is under active development. The UI, event API, and runtime capability detection are in place; the full classloader-swap pipeline is being hardened.

## Features

- **Reload icon button on title screen** — 20x20 sprite button (**↻**) in the bottom utility row with confirmation dialog and staged reload screen
- **Safe by design in-game** — pause screen mounts a disabled **↻** button with a tooltip (*"Leave world to reload mods."*) to prevent world desync
- **Built-in Configuration UI** — native 5-tab GUI (Mods, Settings, Developer, History, Diagnostics) accessible via ModMenu with zero external library requirements (Cloth Config not needed)
- **Native Mojang SystemToasts** — clean in-game notification toasts for reload summary (+added, -removed, ~updated), capability guidance, and failure rollback alerts
- **Atomic rollback safety** — if an added/updated mod crashes during reload, Muto catches the error and cleanly rolls back to the previous snapshot
- **Mod diffing API** — `ModDiff` tracks added, removed, updated, and unchanged mods
- **Event hooks** — subscribe to `RELOAD_START` and `RELOAD_FINISH` to observe or extend reload behavior
- **Runtime capability detection** — detects JetBrains Runtime (JBR) and DCEVM enhanced class redefinition at startup
- **Dedicated logs** — every reload stage is written to `logs/muto.log` alongside normal game logs

## Platform Compatibility

| Component | Target |
|---|---|
| Platform | Fabric Loader |
| Minecraft | Java Edition |
| Java Runtime | JVM (Java 21 / 25+, JBR optional for DCEVM bytecode swap) |
| Dependencies | Fabric API (Zero external config libraries required) |

> **Tip:** For best hot-reload results, run the game on the [JetBrains Runtime](https://www.jetbrains.com/runtime/) or a JVM with DCEVM/`AllowEnhancedClassRedefinition` enabled. Muto reports both automatically at startup.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/)
2. Drop `muto-<version>.jar` and [Fabric API](https://modrinth.com/mod/fabric-api) into your `mods/` folder
3. Launch the game — a **↻** icon button appears in the bottom utility button row on the title screen

## Usage

1. Whenever you add, delete, or update jars in your `mods/` folder, return to the **title screen**
2. Click the **↻** icon button (in the bottom utility row next to ModMenu/Options)
3. Confirm the reload prompt to open the staged reload screen
4. Muto scans your `mods/` directory, rotates classloaders, unfreezes registries, and updates ModMenu
5. A native Mojang toast confirms the reload summary (+added, -removed, ~updated) and duration

Mods that change registries, mixins, or world data may still require a full restart. Muto is safest for client-side and resource-focused mods during development.

## Building from source

```bash
git clone https://github.com/fy2ne/muto.git
cd muto
./gradlew build          # Windows: gradlew.bat build
```

The built jar lands in `build/libs/`.

### Run a dev client

```bash
./gradlew runClient
```

### Run tests

```bash
./gradlew test
```

Requires **JDK 25** and an internet connection (Loom downloads Minecraft mappings on first build).

## API

Other mods can observe reload lifecycle events:

```java
MutoEvents.RELOAD_START.register((startEpochMs, expectedDiff) -> {
    MutoLog.info("reload started, {} changes expected", expectedDiff);
});

MutoEvents.RELOAD_FINISH.register((endEpochMs, diff, success, durationMs, error) -> {
    if (success) {
        MutoLog.info("reload finished in {}ms", durationMs);
    } else {
        MutoLog.error("reload failed", error);
    }
});
```

`ModDiff` is a record with `added`, `removed`, `updated`, and `unchanged` mod id sets, plus a `hasChanges()` helper.

## Contributing

Contributions are welcome. Please:

1. Open an issue first for large changes
2. Keep pull requests focused
3. Make sure `./gradlew build` passes

Use the [issue templates](https://github.com/fy2ne/muto/issues/new/choose) for bug reports and feature requests — logs from `logs/muto.log` and `logs/latest.log` help a lot.

## License

This project is licensed under the [Apache License 2.0](LICENSE).

---

<div align="center">
Made with ❤️ by <a href="https://github.com/fy2ne">fy2ne</a>
</div>
