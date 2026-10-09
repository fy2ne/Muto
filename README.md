<div align="center">

# Muto

**Production-grade hot mod reloader for Minecraft**

Hot-reload added, removed, or updated mods on the fly from the title screen — without restarting Minecraft.

[![Modrinth](https://img.shields.io/modrinth/dt/muto?label=Modrinth&color=00AF5C)](https://modrinth.com/project/muto)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.3-brightgreen)](https://fabricmc.net/)
[![Fabric Loader](https://img.shields.io/badge/Fabric%20Loader-%E2%89%A50.16.0-DBB26A)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-25-orange)](https://openjdk.org/)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)

[Download](https://modrinth.com/project/muto) · [Issues](https://github.com/fy2ne/muto/issues) · [Homepage](https://muto.fy2ne.me)

</div>

---

## About

Muto is a Fabric mod that hot-reloads your mod set without restarting Minecraft. Whenever you add new jars to your `mods/` directory, delete existing ones, or swap in an updated build, click reload from the title screen and Muto applies the changes live.

**Status: beta.** The reload engine is under active development. The UI, event API, and runtime capability detection are in place; the full classloader-swap pipeline is being hardened.

## Features

- **Reload button on the title screen** — one click starts a reload sequence with a staged progress UI
- **Safe by design in-game** — the pause screen shows a disabled reload button with guidance (leave the world first)
- **ModMenu integration** — browse loaded mods, tier classification (Clean, Standard, Stubborn), and live status via ModMenu
- **Native Mojang SystemToasts** — subtle in-game notification toasts for capability guidance, reload summary (+added, -removed, ~updated), and failures
- **Atomic rollback safety** — if an added/updated mod crashes during reload, Muto catches the error and cleanly rolls back to the previous snapshot
- **Mod diffing API** — `ModDiff` tracks added, removed, updated, and unchanged mods
- **Event hooks** — subscribe to `RELOAD_START` and `RELOAD_FINISH` to observe or extend reload behavior
- **Runtime capability detection** — detects JetBrains Runtime (JBR) and DCEVM enhanced class redefinition at startup
- **Dedicated logs** — every reload stage is written to `logs/muto.log` alongside normal game logs

## Requirements

| Component | Version |
|---|---|
| Minecraft | 26.3 |
| Fabric Loader | ≥ 0.16.0 |
| Fabric API | 0.161.0+26.3 |
| Java | 25+ |

> **Tip:** For best hot-reload results, run the game on the [JetBrains Runtime](https://www.jetbrains.com/runtime/) or a JVM with DCEVM/`AllowEnhancedClassRedefinition` enabled. Muto reports both automatically at startup.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 26.3
2. Drop `muto-<version>.jar` and [Fabric API](https://modrinth.com/mod/fabric-api) into your `mods/` folder
3. Launch the game — a **↻** button appears on the title screen

## Usage

1. Whenever you add, delete, or update jars in your `mods/` folder, return to the **title screen**
2. Click the **↻** button (in the bottom utility button row)
3. Muto walks through reload stages with a progress bar
4. When complete, you are returned to the title screen with the new mod set active

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
