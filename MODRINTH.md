<div align="center">

# ⚡ Muto — Hot Mod Reloader

### *Think "resource packs, but for mods."*

**Reload, add, remove, and update mods without ever restarting Minecraft.**

[![Minecraft 26.x](https://img.shields.io/badge/Minecraft-26.3-00AF5C?style=for-the-badge&logo=minecraft&logoColor=white)](https://modrinth.com/mods)
[![Fabric Loader](https://img.shields.io/badge/Fabric-Loader%20%E2%89%A50.16-DBB26A?style=for-the-badge)](https://fabricmc.net/)
[![Stage: Alpha](https://img.shields.io/badge/Status-Public%20Alpha-blue?style=for-the-badge)]()

---

</div>

## 💡 What is Muto?

Tired of quitting Minecraft, waiting through minutes of launch screens, and loading back in just because you dropped a new mod into your `mods` folder or tweaked an existing one?

**Muto transforms mod loading into an official-feeling, seamless experience.** 

Just drop jars into your `mods` folder, click the **↻ Reload** button on the Title Screen, and watch your changes take effect live in seconds.

---

## ✨ Features

- 🔄 **One-Click Title Screen Reload** — A dedicated Mojang-styled reload button right on the main menu.
- 🛡️ **Atomic Crash-Protection & Auto-Rollback** — If an added or updated mod is broken or crashes during load, Muto catches the failure instantly, rolls back to your safe state, and explains what went wrong in a clean notification. No crash reports on desktop, no broken game state.
- 🔒 **Safe In-Game Locking** — You can't accidentally break a running world. In the pause menu, the reload button is gracefully locked with a reminder: *"Leave world to reload mods."*
- 📋 **Built-in Mod Menu Screen** — If you have ModMenu installed, open Muto's config to view your entire mod list classified by reload tier and live status.
- 🔔 **Native Mojang System Notifications** — Smooth in-game toast notifications inform you how many mods were added, removed, or updated and how many milliseconds it took.

---

## 🧩 Compatibility & How It Handles Mods

Muto is built to work alongside your existing mod loadout using an automated **3-Tier Protection Engine**:

| Tier | Badge | What it covers | Reload behavior |
|---|:---:|---|---|
| **Clean Mods** | `✦ Clean` | HUD tweaks, visual enhancements, mini-maps, utilities, client tools | **Instant Live Reload** |
| **Standard Mods** | `● Standard` | Gameplay additions, items, blocks, custom recipes | **Reloadable** via dynamic class isolation & registry refresh |
| **Stubborn / Core Mods** | `■ Locked` | Fabric API, Minecraft core, Java bytecode injectors | **Protected & Locked** (Kept active safely to prevent crashes) |

> ℹ️ *Note: Muto is currently in **Active Alpha**. While it isolates reloads and protects your game, deeply invasive core mods that inject low-level mixins into the graphics pipeline or root loader may still require a full restart.*

---

## 🚀 How to Use

1. **Install Muto**: Place `muto-0.1.0-alpha.1.jar` into your `.minecraft/mods` directory.
2. **Launch Minecraft 26.3** with Fabric Loader.
3. Whenever you add a new mod, remove one, or update a file in `.minecraft/mods`:
   - Go to the **Title Screen**.
   - Click the **↻** button.
   - Done! Your new mod set is active immediately.

---

## ⚡ Maximum Performance Tip (Optional)

Muto works out-of-the-box on standard Java 25. For instant hotswapping on code changes, run Minecraft using the **JetBrains Runtime (JBR)** with `-XX:+AllowEnhancedClassRedefinition` enabled in your launcher settings. Muto will automatically detect it and grant maximum reload capabilities!

---

<div align="center">
Crafted with passion by <a href="https://github.com/fy2ne">fy2ne</a>
</div>
