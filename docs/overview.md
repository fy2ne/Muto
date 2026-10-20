# Muto Runtime Overview

Muto Runtime is an industrial hot-swap engine for Fabric Minecraft mods. It allows developers and players to load, unload, and update mod jars at runtime without restarting the JVM or the client.

## Core Capabilities

- **Zero JVM Restarts**: Swap code, resources, and mixin configurations while the client remains alive.
- **Knot ClassLoader Injection**: Dynamically attaches mod jar paths and JiJ (jar-in-jar) dependencies directly into the Knot runtime class hierarchy.
- **Dynamic Mixin Registration**: Registers new mixin configurations at runtime for newly loaded classes.
- **Registry Unfreezing & Pruning**: Safely unfreezes Minecraft `MappedRegistry` instances, prunes old namespaces, re-runs mod initializers, and refreezes registries.
- **Transaction Rollback**: If an incoming mod crashes during its entrypoint initialization, Muto automatically reverts the registry state and cleans up partial instances.
- **Boundary Auto-Rejoin**: If triggered while inside a singleplayer world or multiplayer server, Muto cleanly disconnects to an intermediate progress screen, performs the reload safely, and auto-rejoins.
- **Mods Folder Watcher**: Background daemon watching the `/mods` folder for changes, with burst debouncing and self-suppression during staged renames.

## Dependency Installation

### Gradle (Local Dev Tier)
```groovy
repositories {
    mavenLocal()
}

dependencies {
    modImplementation "me.fy2ne:muto:0.3.0-beta+26.x"
}
```

### fabric.mod.json
Declare a soft or hard dependency:
```json
"depends": {
    "muto": "~0.3.0-beta"
}
```
