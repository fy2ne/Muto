import React, { useState } from 'react';
import katex from 'katex';
import {
  RotateCw,
  Cpu,
  Layers,
  ShieldCheck,
  Sliders,
  Code2,
  Check,
  Copy,
  Lock,
  Zap,
  Terminal,
  Wrench,
  BookOpen,
  ArrowRight,
  Search,
  X,
  Sparkles
} from 'lucide-react';

/* ============================================================
   SEAMLESS MATH DISPLAY (Zero background, natural typography)
   ============================================================ */
function Math({ tex, block = false }) {
  try {
    const html = katex.renderToString(tex, {
      displayMode: block,
      throwOnError: false
    });
    return <span className={block ? "math-block" : "math-inline"} dangerouslySetInnerHTML={{ __html: html }} />;
  } catch {
    return <code>{tex}</code>;
  }
}

/* ============================================================
   OFFICIAL SVGS
   ============================================================ */
function GithubIcon({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="currentColor">
      <path fillRule="evenodd" clipRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.53 1.032 1.53 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"/>
    </svg>
  );
}

function ModrinthIcon({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="currentColor">
      <path d="M12.252 0C5.503 0 .024 5.466.024 12.203c0 4.545 2.502 8.514 6.223 10.605l2.258-3.902a7.712 7.712 0 0 1-3.97-6.703c0-4.269 3.473-7.734 7.747-7.734 4.275 0 7.748 3.465 7.748 7.734 0 2.766-1.464 5.2-3.666 6.556l2.235 3.914c3.606-2.128 6.012-6.046 6.012-10.47C24.611 5.466 19.08 0 12.252 0zm.019 7.426a4.777 4.777 0 0 0-4.777 4.777 4.777 4.777 0 0 0 4.777 4.777 4.777 4.777 0 0 0 4.777-4.777 4.777 4.777 0 0 0-4.777-4.777z"/>
    </svg>
  );
}

function CurseForgeIcon({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="currentColor">
      <path d="M18.8 6.5C18.1 4.4 16.3 2.7 14.1 2.2c-.6-.1-1.2.3-1.3.9-.1.6.3 1.2.9 1.3 1.5.3 2.8 1.5 3.3 3 .1.4.5.7.9.7h.2c.5-.1.8-.5.7-.9zM12 2C6.5 2 2 6.5 2 12c0 3.6 1.9 6.7 4.8 8.4.3.2.7.2 1 0 .3-.2.4-.5.4-.8v-3.1c0-.4.2-.8.5-1.1l2.1-2.1c.3-.3.8-.5 1.2-.5s.9.2 1.2.5l2.1 2.1c.3.3.5.7.5 1.1v3.1c0 .3.2.7.5.8.3.2.7.2 1 0 2.9-1.7 4.7-4.8 4.7-8.4 0-5.5-4.5-10-10-10zm2.2 12.8l-1.4-1.4c-.4-.4-1.1-.4-1.6 0l-1.4 1.4V11c0-.6.4-1 1-1h2.8c.6 0 1 .4 1 1v3.8z"/>
    </svg>
  );
}

function WebsiteIcon({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="10" />
      <line x1="2" y1="12" x2="22" y2="12" />
      <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
    </svg>
  );
}

/* ============================================================
   MODMENU UI PREVIEW
   ============================================================ */
function ModMenuPreviewFrame() {
  return (
    <div className="screenshot-frame">
      <div className="screenshot-frame-header">
        <span>ModMenu + Muto Live Integration</span>
        <span style={{ color: '#4ade80', fontSize: '11px' }}>In-Game 26.3 Capture</span>
      </div>
      <img
        src="/screenshot-modmenu.png"
        alt="Muto in ModMenu"
        className="screenshot-frame-img"
      />
    </div>
  );
}

/* ============================================================
   CLEAN CODE BLOCK
   ============================================================ */
function CodeSnippet({ code, filename = "Terminal" }) {
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="codeblock">
      <div className="codeblock-head">
        <span className="codeblock-filename">{filename}</span>
        <button className="codeblock-copy" onClick={handleCopy} aria-label="Copy code">
          {copied ? <Check size={12} color="#4ade80" /> : <Copy size={12} />}
          <span>{copied ? "Copied" : "Copy"}</span>
        </button>
      </div>
      <pre>
        <code>{code}</code>
      </pre>
    </div>
  );
}

/* ============================================================
   MAIN DOCS COMPONENT
   ============================================================ */
export default function App() {
  const [selectedId, setSelectedId] = useState('overview');
  const [query, setQuery] = useState('');

  const articles = {
    overview: {
      category: 'Getting Started',
      title: 'Architecture Overview',
      subtitle: 'Industrial-grade hot mod reloader and management pipeline for Fabric 26.3',
      isOverview: true,
      hasMcDemo: true,
      sections: [
        {
          heading: 'Live mod management without restarts',
          content: (
            <>
              For over 15 years, Java Minecraft treated installed mods as immutable boot-time artifacts. Disabling a mod, testing a build update, or resolving a conflict always meant closing Minecraft, waiting minutes for the JVM and assets to reload, and repeating that dozens of times.
              <br /><br />
              Muto eliminates that bottleneck. Built directly into Minecraft and ModMenu, Muto allows you to toggle mods off, load newly added jars, review staged changes, and execute safe in-game hot swaps in under 5 seconds.
            </>
          ),
          steps: [
            <>Toggle any mod on or off directly inside ModMenu with native pixel-art buttons.</>,
            <>Filter your mod list instantly between <strong>All</strong>, <strong>Active</strong>, <strong>Disabled</strong>, and <strong>Unloaded</strong>.</>,
            <>Click <strong>Save (N)</strong> to review your staged changes before applying.</>,
            <>Muto rotates classloaders, unfreezes registries, syncs network payloads, and reconnects seamlessly.</>
          ]
        },
        {
          heading: 'Third-party & developer integration',
          content: (
            <>
              External mods, in-game mod browsers, and downloaders (like Resourcify) can integrate with Muto as an optional soft dependency. If Muto is loaded, downloaders can trigger safe boundary reloads automatically when downloads complete.
            </>
          ),
          snippet: {
            filename: 'DownloaderIntegration.java',
            code: `if (me.fy2ne.muto.api.MutoApi.isAvailable()) {
    if (!me.fy2ne.muto.api.MutoApi.isReloading()) {
        // Safely saves world & reconnects automatically if inside a game
        me.fy2ne.muto.api.MutoApi.triggerBoundaryReload(currentScreen);
    }
}`
          }
        }
      ],
      related: [
        { id: 'start-reloading', label: 'Mod Management Guide', desc: 'Step-by-step workflow for enabling, disabling, and loading mods.' },
        { id: 'modmenu-sync', label: 'ModMenu Integration', desc: 'Action buttons, state badges, and filter cycle.' },
        { id: 'safe-guard', label: 'Safe Execution Boundary', desc: 'World saving and automatic singleplayer reconnection.' }
      ]
    },
    'start-reloading': {
      category: 'Getting Started',
      title: 'Mod Management & Reloading Guide',
      subtitle: 'Enabling, disabling, and loading mods directly through ModMenu',
      sections: [
        {
          heading: 'Managing mods in-game',
          steps: [
            <>Open <strong>Mods</strong> (ModMenu) from the Title Screen or Game Menu.</>,
            <>Select any mod. Action buttons appear dynamically in the top-right of the description pane.</>,
            <>Click the <strong>Power</strong> icon to disable a mod, or the <strong>Plus</strong> icon to stage an unloaded mod.</>,
            <>The mod badge immediately displays <code>Pending: Disable</code>, <code>Pending: Enable</code>, or <code>Pending: Load</code>.</>,
            <>Use the <strong>Filter</strong> button to view only active, disabled, or unloaded mods.</>,
            <>Click <strong>Save (N)</strong> at the bottom of the screen to open the change review dialog.</>,
            <>Click <strong>Apply & Reload</strong>. Muto renames disk files, rotates classloaders, and resynchronizes the game.</>
          ]
        },
        {
          heading: 'Adding new mods during runtime',
          steps: [
            <>Drop a new <code>.jar</code> into your <code>.minecraft/mods</code> folder while Minecraft is running.</>,
            <>Open ModMenu — Muto's background scanner detects the new jar as <strong>Unloaded</strong> (gold tag).</>,
            <>Click the <strong>Load Mod</strong> (+) button next to it.</>,
            <>Click <strong>Save (1)</strong> &rarr; <strong>Apply & Reload</strong> to inject it into the active client.</>
          ]
        }
      ],
      related: [
        { id: 'safe-guard', label: 'Safe Execution Boundary', desc: 'How Muto protects world state during hot reloads.' },
        { id: 'modmenu-sync', label: 'ModMenu Integration', desc: 'Details on badges, filters, and button positioning.' }
      ]
    },
    'safe-guard': {
      category: 'Getting Started',
      title: 'Safe Execution Boundary & World Reconnect',
      subtitle: 'Zero world corruption with automated save, disconnect, reload, and reconnect',
      sections: [
        {
          heading: 'The Boundary Manager Architecture',
          content: (
            <>
              Hot-swapping bytecode or unfreezing block registries while an active world is ticking chunks could cause client-server desync. Muto solves this through the <code>ReloadBoundaryManager</code>:
            </>
          ),
          alert: {
            type: 'warning',
            label: 'Boundary Protection',
            text: 'Muto automatically handles state transitions: on the Title Screen it reloads in under 2 seconds; in Singleplayer worlds it saves chunks, exits cleanly, applies the hot swap, and reconnects back into your world automatically.'
          },
          steps: [
            <><strong>Title Screen:</strong> Directly swaps classloaders and thaws registries without leaving the menu.</>,
            <><strong>Singleplayer Worlds:</strong> Flushes chunk saving, disconnects with <code>MutoReconnectingScreen</code>, completes the reload, and re-enters your world seamlessly.</>,
            <><strong>Multiplayer Servers:</strong> Gracefully disconnects, hot-swaps client-side mods, and reconnects to the target server address.</>
          ]
        }
      ],
      related: [
        { id: 'overview', label: 'Architecture Overview', desc: 'Core hot-swap fundamentals.' },
        { id: 'start-reloading', label: 'Mod Management Guide', desc: 'Step-by-step hot-swapping guide.' }
      ]
    },
    'mod-tiers': {
      category: 'Core Concepts',
      title: 'Mod Classification Tiers',
      subtitle: 'Understanding Clean, Standard, and Stubborn operational boundaries',
      sections: [
        {
          heading: 'Three operational tiers for rock-solid stability',
          content: (
            <>
              To guarantee stability across diverse mod ecosystems, Muto categorizes installed mods into three operational tiers based on their entrypoints and registry interactions.
            </>
          ),
          steps: [
            <><strong>Tier A (Clean Mods):</strong> Client-side tweaks, HUDs, shaders, and UI helpers (e.g. AppleSkin, FullBright). They reload instantly in milliseconds without mutating registry topologies.</>,
            <><strong>Tier B (Standard Mods):</strong> Content mods registering blocks, items, sounds, and network payloads. Reloaded via Knot classloader rotation with temporary registry unfreezing and packet re-registration.</>,
            <><strong>Tier C (Stubborn / Core Mods):</strong> Foundational runtime modules (Fabric Loader, Fabric API, root mixin mutators). Kept locked in the root loader to preserve JVM memory safety.</>
          ]
        },
        {
          heading: 'Topological dependency resolution',
          content: (
            <>
              Mod dependencies form a finite Directed Acyclic Graph <Math tex="\mathcal{G} = (V, E)" />, where vertices represent mod containers and directed edges represent required dependencies. Unloading executes in strict reverse topological order:
              <Math block tex="\tau^{-1}(\mathcal{G}) = (m_n, m_{n-1}, \dots, m_1) \quad \text{where} \quad (u, v) \in E \implies \text{index}(u) < \text{index}(v)" />
              This ensures downstream dependents unmount cleanly before their prerequisite library providers are torn down.
            </>
          )
        }
      ],
      related: [
        { id: 'modmenu-sync', label: 'ModMenu Integration', desc: 'How mod tiers and states display in ModMenu.' },
        { id: 'arch-rotation', label: 'ClassLoader Rotation', desc: 'Knot classloader teardown mechanics.' }
      ]
    },
    'modmenu-sync': {
      category: 'Core Concepts',
      title: 'ModMenu Live Sync & Action Controls',
      subtitle: 'Native Minecraft SpriteIconButton controls, adaptive alignment, and state tags',
      sections: [
        {
          heading: 'Native Minecraft GUI Controls',
          content: (
            <>
              Muto injects natively into ModMenu using non-invasive <code>@Pseudo</code> mixins. If ModMenu is installed, the controls integrate seamlessly; if ModMenu is not present, Muto functions independently via keybinds and commands with zero crash risk.
            </>
          ),
          steps: [
            <><strong>Native Sprite Buttons:</strong> Uses Minecraft's <code>SpriteIconButton</code> so all buttons inherit custom resource pack textures and GUI themes.</>,
            <><strong>Adaptive Alignment:</strong> If a mod has a config button, the action button sits adjacent at <code>right - 22px</code>; if no config button exists, it aligns flush against the right edge.</>,
            <><strong>Disabled Mod Safety:</strong> Config buttons for disabled mods remain visible but inactive (grayed out) with a tooltip: <em>"Cannot configure disabled mod. Enable the mod first."</em></>,
            <><strong>Status Badges:</strong> Color-coded badges indicate <code>Disabled</code> (red), <code>Unloaded</code> (gold), and staged changes (green).</>
          ]
        }
      ],
      related: [
        { id: 'config-screen', label: 'Muto Configuration Screen', desc: 'Native settings UI for Muto preferences.' },
        { id: 'start-reloading', label: 'Mod Management Guide', desc: 'How to use the staged review screen.' }
      ]
    },
    'config-screen': {
      category: 'Core Concepts',
      title: 'Muto Configuration Screen',
      subtitle: 'Native Minecraft preference screen accessible via ModMenu, /muto, or F7',
      sections: [
        {
          heading: 'Lightweight configuration with zero external dependencies',
          content: (
            <>
              Unlike mods that require third-party libraries (like Cloth Config), Muto features a standalone native Minecraft GUI (<code>MutoConfigScreen</code>) built directly on Vanilla <code>Screen</code> APIs.
            </>
          ),
          steps: [
            <>Toggle automatic boundary reload behavior (singleplayer reconnect vs title screen).</>,
            <>Configure toast notification verbosity and sound effects.</>,
            <>Manage developer inspection options and diagnostic logs.</>
          ]
        }
      ],
      related: [
        { id: 'safe-guard', label: 'Safe Execution Boundary', desc: 'World saving and auto-reconnect.' },
        { id: 'modmenu-sync', label: 'ModMenu Integration', desc: 'Accessing config from ModMenu.' }
      ]
    },
    'resourcify-guide': {
      category: 'Developer Integration',
      title: 'Downloader & In-Game Installer API',
      subtitle: 'Enabling in-game mod downloading and live hot-swapping for external mods',
      sections: [
        {
          heading: 'Soft-dependency architecture',
          content: (
            <>
              External mods (like in-game mod browsers or downloaders) can allow players to search and download mods in-game, automatically hot-reloading them with Muto without restarting the client.
            </>
          ),
          steps: [
            <>Add Muto API as a <code>modCompileOnly</code> dependency in your <code>build.gradle</code>.</>,
            <>Add <code>"muto": "&gt;=0.2.0-beta"</code> under <code>suggests</code> in your <code>fabric.mod.json</code>.</>,
            <>When your downloader finishes saving a jar to <code>mods/</code>, check <code>MutoApi.isAvailable()</code>.</>,
            <>If Muto is present, trigger <code>MutoApi.triggerBoundaryReload(currentScreen)</code>.</>,
            <>If Muto is absent, display a fallback message: <code>Restart Minecraft to apply changes</code>.</>
          ],
          snippet: {
            filename: 'DownloaderIntegration.java',
            code: `// Soft-dependency check
if (me.fy2ne.muto.api.MutoApi.isAvailable()) {
    if (!me.fy2ne.muto.api.MutoApi.isReloading()) {
        // Automatically handles singleplayer world save & reconnect
        me.fy2ne.muto.api.MutoApi.triggerBoundaryReload(currentScreen);
    }
} else {
    notifyPlayer("Mod downloaded. Restart Minecraft to apply changes.");
}`
          }
        },
        {
          heading: 'Gradle dependency configuration (GitHub Packages)',
          snippet: {
            filename: 'build.gradle',
            code: `repositories {
    maven {
        name = "GitHubPackages"
        url = "https://maven.pkg.github.com/fy2ne/muto"
    }
}

dependencies {
    modCompileOnly "me.fy2ne:muto:0.2.0-beta"
}`
          }
        }
      ],
      related: [
        { id: 'api-reference', label: 'Public MutoApi Reference', desc: 'Static methods and lifecycle events.' },
        { id: 'overview', label: 'Architecture Overview', desc: 'Core hot-swap fundamentals.' }
      ]
    },
    'api-reference': {
      category: 'Developer Integration',
      title: 'Public MutoApi Reference',
      subtitle: 'Static methods and lifecycle event hooks in me.fy2ne.muto.api',
      sections: [
        {
          heading: 'MutoApi class methods (me.fy2ne.muto.api.MutoApi)',
          steps: [
            <><code>MutoApi.isAvailable()</code> &mdash; Returns true if Muto is active in FabricLoader.</>,
            <><code>MutoApi.isReloading()</code> &mdash; Returns true if ReloadEngine is currently processing a swap.</>,
            <><code>MutoApi.isSafeToReload()</code> &mdash; Returns true if the client is not currently executing a reload.</>,
            <><code>MutoApi.triggerBoundaryReload(Screen)</code> &mdash; Triggers a safe boundary reload (handles world save & auto-reconnect).</>,
            <><code>MutoApi.reloadAsync()</code> &mdash; Scans <code>mods/</code> and executes the hot reload asynchronously.</>,
            <><code>MutoApi.getReloadableModIds()</code> &mdash; Returns mod IDs tracked in the active reload snapshot.</>,
            <><code>MutoApi.getStubbornModIds()</code> &mdash; Returns core mod IDs locked in the root classloader.</>
          ],
          snippet: {
            filename: 'me.fy2ne.muto.api.MutoApi.java',
            code: `package me.fy2ne.muto.api;

public final class MutoApi {
    public static boolean isAvailable();
    public static boolean isReloading();
    public static boolean isSafeToReload();
    public static void triggerBoundaryReload(net.minecraft.client.gui.screens.Screen returnScreen);
    public static CompletableFuture<ReloadResult> reloadAsync();
    public static Set<String> getReloadableModIds();
    public static Set<String> getStubbornModIds();
}`
          }
        },
        {
          heading: 'Lifecycle Events (me.fy2ne.muto.api.MutoEvents)',
          snippet: {
            filename: 'LifecycleHooks.java',
            code: `import me.fy2ne.muto.api.MutoEvents;

// Teardown state before reload starts
MutoEvents.RELOAD_START.register((startEpochMs, expectedDiff) -> {
    if (expectedDiff.removed().contains("my_mod")) {
        MySubsystem.shutdown();
    }
});

// Re-init state after reload settles
MutoEvents.RELOAD_FINISH.register((endEpochMs, diff, success, durationMs, error) -> {
    if (success && diff.added().contains("my_mod")) {
        MySubsystem.bootstrap();
    }
});`
          }
        }
      ],
      related: [
        { id: 'resourcify-guide', label: 'Downloader API Guide', desc: 'Code examples for in-game mod installers.' },
        { id: 'arch-rotation', label: 'ClassLoader Rotation', desc: 'Runtime execution lifecycle.' }
      ]
    },
    'file-locks': {
      category: 'Developer Integration',
      title: 'Windows File Locking Solution',
      subtitle: 'Eliminating the "File in use by another process" Windows operating system lock',
      sections: [
        {
          heading: 'Why Windows locks jar files and how Muto solves it',
          content: (
            <>
              On Windows, the operating system prevents open jar files from being renamed or overwritten (<code>ERROR_SHARING_VIOLATION</code>). If a user attempts to disable or delete an active mod jar while Minecraft is open, Windows rejects the file action.
              <br /><br />
              Muto solves this by using non-locking jar inspection and staging file renames (<code>.jar &harr; .jar.disabled</code>) with robust exponential retry backoff.
            </>
          ),
          steps: [
            <>Jars dropped into <code>mods/</code> are indexed with minimal handle retention.</>,
            <>Muto uses an exponential retry loop with 120ms backoff when performing file renames.</>,
            <>Operating system file descriptors are released immediately after metadata inspection.</>,
            <>If an external process locks the file, Muto reports the error gracefully rather than crashing the client.</>
          ]
        }
      ],
      related: [
        { id: 'start-reloading', label: 'Mod Management Guide', desc: 'How to manage jars in the mods folder.' },
        { id: 'jvm-tuning', label: 'JVM & JetBrains Runtime', desc: 'JVM flags for optimal runtime performance.' }
      ]
    },
    'jvm-tuning': {
      category: 'Developer Integration',
      title: 'JVM & JetBrains Runtime (JBR)',
      subtitle: 'Enabling advanced DCEVM bytecode redefinition and fast swapping',
      sections: [
        {
          heading: 'Enhanced class redefinition with DCEVM',
          content: (
            <>
              Standard JVMs allow hot-reloading method bodies only. To hot-reload added methods, modified fields, or newly introduced classes without full classloader disposal, Muto integrates with the <code>JetBrains Runtime (JBR)</code>.
            </>
          ),
          steps: [
            <>Install JetBrains Runtime (JBR) for Minecraft.</>,
            <>Add the JVM argument: <code>-XX:+AllowEnhancedClassRedefinition</code> in your launcher.</>,
            <>At game boot, Muto auto-detects DCEVM capabilities and logs: <code>DCEVM active: true</code>.</>,
            <>Reload transactions execute up to 4x faster with near-instant method swaps.</>
          ]
        }
      ],
      related: [
        { id: 'arch-rotation', label: 'ClassLoader Rotation', desc: 'Fallback mechanism on standard JVMs.' },
        { id: 'api-reference', label: 'Public MutoApi Reference', desc: 'API hooks for external tools.' }
      ]
    },
    'arch-rotation': {
      category: 'Architecture',
      title: 'Knot & ClassLoader Rotation',
      subtitle: 'Deep technical internals of the Muto hot-swap engine',
      sections: [
        {
          heading: 'Dynamic ClassLoader isolation',
          content: (
            <>
              Standard Fabric mods are loaded by Knot ClassLoader at game start. Because Java does not allow unloading classes from a running ClassLoader without garbage-collecting the loader itself, Muto separates mods into manageable classloader trees.
              <br /><br />
              During a reload transaction:
            </>
          ),
          steps: [
            <>The existing child ClassLoader is detached from active references.</>,
            <>Minecraft's <code>MappedRegistry</code> is unfrozen, allowing new entries to be registered.</>,
            <>A fresh child ClassLoader is initialized with the updated jar files.</>,
            <>The new mod entrypoints execute, ModMenu receives refreshed metadata, and registries are refrozen.</>
          ]
        },
        {
          heading: 'Formal GC Unreachability Condition',
          content: (
            <>
              Under JVM Specification &sect;5.3, a ClassLoader instance <code>L</code> and its loaded classes can only be reclaimed by the Garbage Collector if no active reference path exists from the root set:
              <Math block tex="\operatorname{RetainedBytes}(L) \to 0 \iff \forall r \in \mathcal{R}_{\text{JVM}}, \quad r \not\rightsquigarrow L" />
              Because Knot and FabricLoader permanently retain root references in static registries, standard mod classes remain pinned in memory indefinitely. Muto isolates dynamic mods so cutting the active snapshot reference guarantees full JVM garbage collection.
            </>
          )
        }
      ],
      related: [
        { id: 'registry-thawing', label: 'Registry Thawing Mechanics', desc: 'How Muto unfreezes Vanilla MappedRegistry.' },
        { id: 'atomic-rollback', label: 'Atomic Rollback Guarantee', desc: 'Crash isolation and snapshot recovery.' }
      ]
    },
    'registry-thawing': {
      category: 'Architecture',
      title: 'Registry Thawing Mechanics',
      subtitle: 'Safely mutating Minecraft frozen registries during hot-swap transactions',
      sections: [
        {
          heading: 'Bypassing Vanilla Registry Freeze Locks',
          content: (
            <>
              Vanilla Minecraft calls <code>registry.freeze()</code> after the main menu loads. Registering a block or item after freeze throws an <code>IllegalStateException: Registry is already frozen</code>.
              <br /><br />
              Muto safely unfreezes the registry topology during the reload window:
            </>
          ),
          steps: [
            <>Muto accesses the private <code>frozen</code> boolean on <code>MappedRegistry</code> via JVM accessor handle.</>,
            <>The registry is temporarily marked un-frozen for the duration of the entrypoint execution.</>,
            <>New identifiers and objects are registered into their respective namespaces.</>,
            <>The registry is refrozen immediately before client input resumes, preserving data integrity.</>
          ]
        }
      ],
      related: [
        { id: 'arch-rotation', label: 'ClassLoader Rotation', desc: 'How classes are isolated.' },
        { id: 'atomic-rollback', label: 'Atomic Rollback Guarantee', desc: 'Crash recovery guarantees.' }
      ]
    },
    'atomic-rollback': {
      category: 'Architecture',
      title: 'Atomic Rollback Guarantee',
      subtitle: 'Zero crash tolerance with snapshot-backed transactional rollbacks',
      sections: [
        {
          heading: 'Fault-tolerant mod initialization',
          content: (
            <>
              If a newly added or updated mod has a fatal bug or throws an unhandled exception during its reload initialization, Muto ensures your Minecraft client never crashes.
            </>
          ),
          steps: [
            <>Before executing entrypoints, Muto snapshots the active classloader state and registry index.</>,
            <>If an entrypoint throws an exception, the reload transaction catches the fault.</>,
            <>The broken mod is tagged as <code>Faulty</code>, and the prior stable snapshot is immediately restored.</>,
            <>A toast alert is displayed with the error details, allowing the player to remove the broken jar safely.</>
          ]
        }
      ],
      related: [
        { id: 'arch-rotation', label: 'ClassLoader Rotation', desc: 'ClassLoader rotation details.' },
        { id: 'safe-guard', label: 'Safe Execution Boundary', desc: 'Boundary manager and reconnect flow.' }
      ]
    }
  };

  const navCategories = [
    {
      title: 'Getting Started',
      items: [
        { id: 'overview', label: 'Architecture Overview', icon: BookOpen },
        { id: 'start-reloading', label: 'Mod Management Guide', icon: RotateCw },
        { id: 'safe-guard', label: 'Safe Execution Boundary', icon: ShieldCheck }
      ]
    },
    {
      title: 'Core Concepts',
      items: [
        { id: 'mod-tiers', label: 'Mod Classification Tiers', icon: Cpu },
        { id: 'modmenu-sync', label: 'ModMenu Integration', icon: Terminal },
        { id: 'config-screen', label: 'Muto Configuration', icon: Sliders }
      ]
    },
    {
      title: 'Developer Integration',
      items: [
        { id: 'resourcify-guide', label: 'Downloader API Guide', icon: Layers },
        { id: 'api-reference', label: 'Public MutoApi Reference', icon: Code2 },
        { id: 'file-locks', label: 'Windows File Locking', icon: Wrench },
        { id: 'jvm-tuning', label: 'JVM & JetBrains Runtime', icon: Zap }
      ]
    },
    {
      title: 'Architecture',
      items: [
        { id: 'arch-rotation', label: 'ClassLoader Rotation', icon: RotateCw },
        { id: 'registry-thawing', label: 'Registry Thawing', icon: Lock },
        { id: 'atomic-rollback', label: 'Atomic Rollback', icon: ShieldCheck }
      ]
    }
  ];

  const currentArticle = articles[selectedId] || articles.overview;

  return (
    <div className="docs-shell">
      
      {/* NAVBAR */}
      <header className="docs-header">
        <div className="docs-header-left">
          <div className="brand" onClick={() => setSelectedId('overview')}>
            <img src="/icon.png" alt="Muto" style={{ width: '20px', height: '20px', objectFit: 'contain' }} />
            <span className="brand-text">Muto</span>
            <span className="brand-badge">Beta</span>
          </div>
        </div>

        {/* OFFICIAL SVG ICONS WITH WEAVETAB BLACK TOOLTIPS */}
        <div className="docs-header-right">
          
          {/* GitHub */}
          <a
            href="https://github.com/fy2ne/muto"
            target="_blank"
            rel="noopener noreferrer"
            className="nav-icon-link"
            aria-label="GitHub"
          >
            <GithubIcon size={16} />
            <span className="nav-tooltip">View Source on GitHub</span>
          </a>

          {/* Modrinth */}
          <a
            href="https://modrinth.com/mod/muto"
            target="_blank"
            rel="noopener noreferrer"
            className="nav-icon-link"
            aria-label="Modrinth"
          >
            <ModrinthIcon size={16} />
            <span className="nav-tooltip">Download on Modrinth</span>
          </a>

          {/* CurseForge */}
          <a
            href="https://curseforge.com/minecraft/mc-mods/muto"
            target="_blank"
            rel="noopener noreferrer"
            className="nav-icon-link"
            aria-label="CurseForge"
          >
            <CurseForgeIcon size={16} />
            <span className="nav-tooltip">Browse on CurseForge</span>
          </a>

          {/* fy2ne.me website */}
          <a
            href="https://fy2ne.me"
            target="_blank"
            rel="noopener noreferrer"
            className="nav-icon-link"
            aria-label="Personal Website"
          >
            <WebsiteIcon size={16} />
            <span className="nav-tooltip">Visit fy2ne.me</span>
          </a>

        </div>
      </header>

      {/* LEFT SIDEBAR */}
      <aside className="sidebar">
        <div className="sb-scroll">
          
          {/* Search Card */}
          <div className="sb-search-card">
            <div className="sb-search-input">
              <Search size={13} />
              <input
                type="text"
                placeholder="Search documentation..."
                value={query}
                onChange={(e) => setQuery(e.target.value)}
              />
              {query && (
                <button className="sb-search-clear" onClick={() => setQuery('')}>
                  <X size={12} />
                </button>
              )}
            </div>
          </div>

          {/* Navigation Sections */}
          <div className="sb-nav">
            {navCategories.map((cat, cIdx) => (
              <div key={cIdx} className="sb-section">
                <div className="sb-section-head">{cat.title}</div>
                <div className="sb-section-body">
                  {cat.items
                    .filter(item => !query || item.label.toLowerCase().includes(query.toLowerCase()))
                    .map((item) => {
                      const Icon = item.icon;
                      const active = selectedId === item.id;
                      return (
                        <div
                          key={item.id}
                          className={`sb-link ${active ? 'active' : ''}`}
                          onClick={() => setSelectedId(item.id)}
                        >
                          <span className="sb-link-icon">
                            <Icon size={13} />
                          </span>
                          <span className="sb-link-label">{item.label}</span>
                        </div>
                      );
                    })}
                </div>
              </div>
            ))}
          </div>

        </div>
      </aside>

      {/* MAIN CONTENT + TABLE OF CONTENTS */}
      <main className="docs-main">
        
        {/* CENTER ARTICLE */}
        <div className="docs-content">
          
          {/* Breadcrumb */}
          <div className="page-breadcrumb">
            <span>Docs</span>
            <span className="sep">/</span>
            <span>{currentArticle.category}</span>
            <span className="sep">/</span>
            <span className="current">{currentArticle.title}</span>
          </div>

          <div className="md">
            <h1>{currentArticle.title}</h1>
            <p style={{ color: 'var(--text-muted)', fontSize: '15.5px', marginTop: '-6px', marginBottom: '28px' }}>
              {currentArticle.subtitle}
            </p>

            {/* ModMenu Preview Frame */}
            {currentArticle.hasMcDemo && <ModMenuPreviewFrame />}

            {/* Overview Tier Breakdown */}
            {currentArticle.isOverview && (
              <div className="sub-feature-grid">
                <div className="sub-feature-card">
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
                    <div className="sub-feature-label">Clean Mods</div>
                    <span className="sb-badge" style={{ color: '#4ade80' }}>Tier A</span>
                  </div>
                  <div className="sub-feature-desc">
                    HUDs, menus, and client tweaks. Hot-swap instantly with zero registry footprint.
                  </div>
                </div>

                <div className="sub-feature-card">
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
                    <div className="sub-feature-label">Standard Mods</div>
                    <span className="sb-badge sb-badge-beta">Tier B</span>
                  </div>
                  <div className="sub-feature-desc">
                    Content mods registering blocks & items. Swapped via child loader rotation with registry unfreezing.
                  </div>
                </div>

                <div className="sub-feature-card">
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
                    <div className="sub-feature-label">Stubborn Mods</div>
                    <span className="sb-badge" style={{ color: '#ef4444' }}>Tier C</span>
                  </div>
                  <div className="sub-feature-desc">
                    Fabric Loader & core mixins. Locked in root loader to preserve JVM memory safety.
                  </div>
                </div>
              </div>
            )}

            {/* Sections */}
            {currentArticle.sections.map((sec, sIdx) => {
              const secId = `sec-${sIdx}`;
              return (
                <div key={sIdx} id={secId} style={{ scrollMarginTop: '64px' }}>
                  <h2>{sec.heading}</h2>

                  {sec.content && <p>{sec.content}</p>}

                  {sec.alert && (
                    <div className={`doc-alert doc-alert-${sec.alert.type}`}>
                      <div className="doc-alert-label">{sec.alert.label}</div>
                      <div className="doc-alert-body">
                        <p>{sec.alert.text}</p>
                      </div>
                    </div>
                  )}

                  {sec.steps && (
                    <ol>
                      {sec.steps.map((step, stIdx) => (
                        <li key={stIdx}>{step}</li>
                      ))}
                    </ol>
                  )}

                  {sec.snippet && (
                    <CodeSnippet
                      filename={sec.snippet.filename}
                      code={sec.snippet.code}
                    />
                  )}
                </div>
              );
            })}

            {/* Related Topics */}
            {currentArticle.related && (
              <div style={{ marginTop: '48px', paddingTop: '28px', borderTop: '1px solid var(--border)' }}>
                <h3 style={{ color: 'var(--text)', fontSize: '13.5px', textTransform: 'uppercase', letterSpacing: '0.08em', marginBottom: '14px' }}>
                  Related Topics
                </h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  {currentArticle.related.map((rel, rIdx) => (
                    <div
                      key={rIdx}
                      onClick={() => setSelectedId(rel.id)}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        padding: '10px 14px',
                        border: '1px solid var(--border)',
                        borderRadius: '6px',
                        cursor: 'pointer',
                        transition: 'border-color 0.12s ease'
                      }}
                      onMouseEnter={(e) => e.currentTarget.style.borderColor = 'var(--border-strong)'}
                      onMouseLeave={(e) => e.currentTarget.style.borderColor = 'var(--border)'}
                    >
                      <div>
                        <div style={{ fontSize: '13.5px', fontWeight: 500, color: 'var(--text)' }}>{rel.label}</div>
                        <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{rel.desc}</div>
                      </div>
                      <ArrowRight size={13} color="var(--accent)" />
                    </div>
                  ))}
                </div>
              </div>
            )}

          </div>

        </div>

        {/* RIGHT TOC */}
        <aside className="toc">
          <h4>On this page</h4>
          <div className="toc-current-page">{currentArticle.title}</div>
          <div className="toc-sections">
            {currentArticle.sections?.map((sec, sIdx) => {
              const secId = `sec-${sIdx}`;
              return (
                <a
                  key={sIdx}
                  href={`#${secId}`}
                  className="toc-anchor"
                  onClick={(e) => {
                    e.preventDefault();
                    document.getElementById(secId)?.scrollIntoView({ behavior: 'smooth' });
                  }}
                >
                  {sec.heading}
                </a>
              );
            })}
          </div>
        </aside>

      </main>

    </div>
  );
}
