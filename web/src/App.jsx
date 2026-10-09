import React, { useState } from 'react';
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
   MINECRAFT INTERACTIVE TITLE SCREEN PHOTOCOPY DEMO
   ============================================================ */
function MinecraftTitleScreenMock() {
  const [clicked, setClicked] = useState(false);
  const [toast, setToast] = useState(null);

  const triggerReload = () => {
    setClicked(true);
    setToast('↻ Muto: Scanned /mods · 14 mods active (340ms)');
    setTimeout(() => setClicked(false), 200);
    setTimeout(() => setToast(null), 3500);
  };

  return (
    <div className="mc-widget-card">
      <div className="mc-widget-header">
        <div className="mc-widget-title">
          <Sparkles size={12} color="var(--accent)" />
          <span>Interactive Title Screen Photocopy</span>
        </div>
        <span className="mc-widget-badge">TitleScreenMixin.java</span>
      </div>

      <div className="mc-screen-mock">
        {/* Title Screen Main Buttons */}
        <div className="mc-btn mc-btn-wide">Singleplayer</div>
        <div className="mc-btn mc-btn-wide">Multiplayer</div>

        {/* Options & Icon Row (Injected by Muto TitleScreenMixin) */}
        <div className="mc-row-icons" style={{ marginTop: '4px' }}>
          <div className="mc-btn mc-btn-icon" title="Language">🌐</div>
          <div className="mc-btn mc-btn-icon" title="Accessibility">♿</div>
          <div className="mc-btn mc-btn-icon" title="Options">⚙</div>
          <div className="mc-btn mc-btn-icon" title="ModMenu">🧩</div>
          
          {/* Muto 20x20 Reload Button */}
          <div 
            className={`mc-btn mc-btn-icon mc-btn-active-muto ${clicked ? 'active' : ''}`}
            onClick={triggerReload}
            title="Muto: Reload Mods"
            style={{ position: 'relative' }}
          >
            <img src="/icon.png" alt="Reload" style={{ width: '15px', height: '15px' }} />
            {/* Green update pip indicator from TitleScreenMixin */}
            <span style={{
              position: 'absolute',
              top: '2px',
              right: '2px',
              width: '5px',
              height: '5px',
              borderRadius: '50%',
              background: '#22c55e',
              boxShadow: '0 0 4px #22c55e'
            }} />
          </div>
        </div>

        {/* Toast Simulation */}
        {toast && (
          <div className="mc-toast-preview">
            <span>{toast}</span>
          </div>
        )}
      </div>
    </div>
  );
}

/* ============================================================
   CLEAN CODE BLOCK
   ============================================================ */
function CodeSnippet({ code, filename = "Terminal" }) {
  const [copied, setCopied] = useState(false);

  const copy = () => {
    navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 1800);
  };

  return (
    <div className="codeblock">
      <div className="codeblock-head">
        <span>{filename}</span>
        <button className="codeblock-copy" onClick={copy}>
          {copied ? <Check size={12} color="#4ade80" /> : <Copy size={12} />}
          <span>{copied ? 'copied' : 'copy'}</span>
        </button>
      </div>
      <pre><code>{code}</code></pre>
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
      subtitle: 'Dynamic Runtime Mod Reloader for Minecraft Java Edition on Fabric Loader',
      isOverview: true,
      hasMcDemo: true,
      sections: [
        {
          heading: 'How live hot-swapping works in Minecraft',
          content: (
            <>
              For over 15 years, Java Minecraft modding treated mod discovery and class loading as immutable boot-time events. Changing or testing a single mod required closing the game and relaunching the entire JVM.
              <br /><br />
              Muto breaks this barrier by deploying disposable child classloaders. Reloadable mods are isolated into dedicated classloader branches, allowing them to be loaded, unloaded, garbage collected, and swapped dynamically directly from the title screen in under 500 milliseconds.
            </>
          ),
          steps: [
            <>Drop or replace your mod jar inside the <code>mods/</code> directory.</>,
            <>Return to the Minecraft <code>Title Screen</code>.</>,
            <>Click the golden <code>↻ Reload</code> button or trigger it via the public API.</>,
            <>Muto rotates the child classloader, unfreezes registries, and updates ModMenu in real time.</>
          ]
        },
        {
          heading: 'Third-party integration architecture',
          content: (
            <>
              External mods (such as in-game mod browsers or downloaders like Resourcify) can integrate with Muto as a soft dependency. If Muto is loaded, downloaders can invoke hot-swap reloads automatically upon file download completion.
            </>
          ),
          snippet: {
            filename: 'DownloaderIntegration.java',
            code: `if (FabricLoader.getInstance().isModLoaded("muto")) {
    if (me.fy2ne.muto.api.MutoApi.isSafeToReload()) {
        me.fy2ne.muto.api.MutoApi.reloadAsync().thenAccept(res -> {
            if (res.success()) {
                System.out.println("Mod active! Reloaded in " + res.durationMs() + "ms");
            }
        });
    }
}`
          }
        }
      ],
      related: [
        { id: 'resourcify-guide', label: 'Resourcify & Downloader API', desc: 'Enable in-game mod downloads with live hot-swap support.' },
        { id: 'mod-tiers', label: 'Mod Classification Tiers', desc: 'Detailed breakdown of Clean, Standard, and Stubborn mods.' },
        { id: 'api-reference', label: 'Public MutoApi Reference', desc: 'Complete Java API specification for developers.' }
      ]
    },
    'start-reloading': {
      category: 'Getting Started',
      title: 'Hot-Reloading Guide',
      subtitle: 'Adding, removing, and updating mods without restarting Minecraft',
      sections: [
        {
          heading: 'Adding a new mod jar',
          steps: [
            <>Exit to the <code>Title Screen</code> of Minecraft (never reload inside an active world).</>,
            <>Navigate to your Minecraft instance directory and open the <code>mods/</code> folder.</>,
            <>Place your new mod jar into <code>mods/</code>.</>,
            <>Click the golden <code>↻ Reload</code> button on the title screen.</>,
            <>A native toast notification will appear in the top-right corner confirming <code>+1 added</code> and reporting elapsed time.</>
          ]
        },
        {
          heading: 'Updating an existing mod',
          steps: [
            <>Return to the <code>Title Screen</code>.</>,
            <>Replace the existing jar in <code>mods/</code> with the updated version.</>,
            <>Click the <code>↻ Reload</code> button.</>,
            <>Muto rotates the child classloader, tears down old instances, and boots the new entrypoints cleanly.</>
          ]
        }
      ],
      related: [
        { id: 'safe-guard', label: 'Safe Execution Guard', desc: 'Why in-world reloading is locked to prevent state desync.' },
        { id: 'file-locks', label: 'Windows File Locking Solution', desc: 'How Muto bypasses OS sharing violation errors.' }
      ]
    },
    'safe-guard': {
      category: 'Getting Started',
      title: 'Safe Execution Guard',
      subtitle: 'Preventing state corruption and world desynchronization',
      sections: [
        {
          heading: 'In-World Protection Mechanism',
          content: (
            <>
              Reloading mods while loaded into a singleplayer or multiplayer world would corrupt entity registries, chunk data, and network packets.
              <br /><br />
              Muto enforces strict execution guards:
            </>
          ),
          alert: {
            type: 'warning',
            label: 'Safety Requirement',
            text: 'Hot-reloading is strictly constrained to the Title Screen. When an active world or server connection is detected, reload triggers are locked to prevent client desynchronization.'
          },
          steps: [
            <>When the game is paused inside an active world, the reload button is visually locked with an informative tooltip.</>,
            <>The public API method <code>MutoApi.isSafeToReload()</code> returns false whenever a client world or integrated server is running.</>,
            <>Direct invocations during unsafe states fail immediately with an explicit error without touching classloaders.</>
          ]
        }
      ],
      related: [
        { id: 'overview', label: 'Architecture Overview', desc: 'Explore the classloader isolation pipeline.' },
        { id: 'start-reloading', label: 'Hot-Reloading Guide', desc: 'Step-by-step hot-swapping instructions.' }
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
              To guarantee zero JVM crashes, Muto categorizes all discovered mods into three operational tiers based on their entrypoint signatures and registry interactions.
            </>
          ),
          steps: [
            <><strong>Clean Mods (Tier 1):</strong> Client-side tweaks, HUDs, and UI helpers (e.g. AppleSkin, FullBright). They reload instantly in milliseconds without mutating registry topologies.</>,
            <><strong>Standard Mods (Tier 2):</strong> Mods that register custom blocks, items, sound events, or recipes (e.g. Distant Horizons, Structory). Reloaded via child classloader rotation with temporary registry unfreezing.</>,
            <><strong>Stubborn / Core Mods (Tier 3):</strong> Foundational runtime modules (Fabric Loader, Fabric API, core mixin hooks). Locked in the root classloader to preserve native JVM memory safety.</>
          ]
        }
      ],
      related: [
        { id: 'modmenu-sync', label: 'ModMenu Live Sync', desc: 'How reloaded mod tiers update inside ModMenu.' },
        { id: 'arch-rotation', label: 'Child ClassLoader Rotation', desc: 'Classloader teardown mechanics.' }
      ]
    },
    'modmenu-sync': {
      category: 'Core Concepts',
      title: 'ModMenu Live Sync',
      subtitle: 'Dynamic metadata reflection and live UI updates inside ModMenu',
      sections: [
        {
          heading: 'Live mod list reflection',
          content: (
            <>
              By default, ModMenu indexes installed mods once at startup. When Muto hot-swaps or removes a mod, ModMenu is refreshed automatically.
            </>
          ),
          steps: [
            <>Muto synchronizes with FabricLoader's internal mod container registry.</>,
            <>ModMenu's cached mod instances and badge providers are notified of new or removed mods.</>,
            <>Mod descriptions, icons, and configuration screen entrypoints update immediately without requiring a game reboot.</>
          ]
        }
      ],
      related: [
        { id: 'cloth-config', label: 'Cloth Config UI', desc: 'In-game preference screen for Muto.' },
        { id: 'mod-tiers', label: 'Mod Classification Tiers', desc: 'Understand tier badges in ModMenu.' }
      ]
    },
    'cloth-config': {
      category: 'Core Concepts',
      title: 'Cloth Config UI',
      subtitle: 'In-game configuration settings and developer options',
      sections: [
        {
          heading: 'Configurable options',
          steps: [
            <><strong>Toast Notifications:</strong> Enable or disable native Mojang system toasts for reload summaries.</>,
            <><strong>Automatic Registry Unfreezing:</strong> Allow Standard tier mods to register new blocks and items during hot-swap transactions.</>,
            <><strong>Developer Verbose Logging:</strong> Output detailed classloader rotation and bytecode diffs to <code>logs/muto.log</code>.</>
          ]
        }
      ],
      related: [
        { id: 'safe-guard', label: 'Safe Execution Guard', desc: 'Preventing state corruption.' },
        { id: 'jvm-tuning', label: 'JVM & JetBrains Runtime', desc: 'Optimizing bytecode reload performance.' }
      ]
    },
    'resourcify-guide': {
      category: 'Developer Integration',
      title: 'Resourcify & Downloader API',
      subtitle: 'Enabling in-game mod downloading and live hot-swapping',
      sections: [
        {
          heading: 'Soft-dependency architecture',
          content: (
            <>
              External mods (like Resourcify) can allow players to search and download mods in-game, automatically hot-reloading them with Muto without restarting the client.
            </>
          ),
          steps: [
            <>Add Muto API as a <code>modCompileOnly</code> dependency in your <code>build.gradle</code>.</>,
            <>Add <code>"muto": "*"</code> under <code>suggests</code> in your <code>fabric.mod.json</code>.</>,
            <>When your downloader finishes saving a jar to disk, check <code>FabricLoader.getInstance().isModLoaded("muto")</code>.</>,
            <>If Muto is present, verify <code>MutoApi.isSafeToReload()</code> and call <code>MutoApi.reloadAsync()</code>.</>,
            <>If Muto is absent, display a fallback message: <code>Restart Minecraft to apply changes</code>.</>
          ],
          snippet: {
            filename: 'DownloaderIntegration.java',
            code: `// Soft-dependency check
if (FabricLoader.getInstance().isModLoaded("muto")) {
    if (me.fy2ne.muto.api.MutoApi.isSafeToReload()) {
        me.fy2ne.muto.api.MutoApi.reloadAsync().thenAccept(result -> {
            if (result.success()) {
                System.out.println("Mod active! Hot-swapped in " + result.durationMs() + "ms");
            }
        });
    } else {
        notifyPlayer("Mod downloaded. Return to Title Screen to apply without restart.");
    }
} else {
    notifyPlayer("Mod downloaded. Restart Minecraft to apply changes.");
}`
          }
        },
        {
          heading: 'Gradle dependency configuration',
          snippet: {
            filename: 'build.gradle',
            code: `repositories {
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
    }
}

dependencies {
    modCompileOnly "maven.modrinth:muto:0.1.0-beta.1"
}`
          }
        }
      ],
      related: [
        { id: 'api-reference', label: 'Public MutoApi Reference', desc: 'Static methods and async callbacks.' },
        { id: 'overview', label: 'Architecture Overview', desc: 'Core hot-swap fundamentals.' }
      ]
    },
    'api-reference': {
      category: 'Developer Integration',
      title: 'Public MutoApi Reference',
      subtitle: 'Verified static methods and event lifecycle hooks directly in me.fy2ne.muto.api',
      sections: [
        {
          heading: 'MutoApi class definition (me.fy2ne.muto.api.MutoApi)',
          steps: [
            <><code>MutoApi.isAvailable()</code> — Returns true if Muto is active in FabricLoader.</>,
            <><code>MutoApi.isReloading()</code> — Returns true if ReloadEngine is currently processing a swap.</>,
            <><code>MutoApi.isSafeToReload()</code> — Returns true if client.level == null and screen is TitleScreen.</>,
            <><code>MutoApi.reloadAsync()</code> — Scans /mods and rotates child loaders asynchronously.</>,
            <><code>MutoApi.getReloadableModIds()</code> — Returns mod IDs tracked in the current active snapshot.</>,
            <><code>MutoApi.getStubbornModIds()</code> — Returns locked root-classloader mod IDs.</>
          ],
          snippet: {
            filename: 'me.fy2ne.muto.api.MutoApi.java',
            code: `package me.fy2ne.muto.api;

public final class MutoApi {
    private MutoApi() {}

    public static boolean isAvailable() {
        return FabricLoader.getInstance().isModLoaded("muto");
    }

    public static boolean isReloading() {
        return ReloadEngine.INSTANCE.isReloading();
    }

    public static boolean isSafeToReload() {
        try {
            var client = net.minecraft.client.Minecraft.getInstance();
            return client != null && client.level == null 
                && client.screen instanceof net.minecraft.client.gui.screens.TitleScreen;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static CompletableFuture<ReloadResult> reloadAsync() {
        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
        return ReloadEngine.INSTANCE.reloadAsync(modsDir, stage -> {});
    }

    public static Set<String> getReloadableModIds() {
        return ReloadEngine.INSTANCE.currentSnapshot().reloadableMods();
    }

    public static Set<String> getStubbornModIds() {
        return ReloadEngine.INSTANCE.currentSnapshot().stubbornMods();
    }
}`
          }
        }
      ],
      related: [
        { id: 'resourcify-guide', label: 'Resourcify & Downloader API', desc: 'Code examples for in-game mod installers.' },
        { id: 'arch-rotation', label: 'Child ClassLoader Rotation', desc: 'Runtime execution lifecycle.' }
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
              On Windows, the operating system prevents open jar files from being overwritten or deleted (<code>ERROR_SHARING_VIOLATION</code>). If a player attempts to delete an active mod jar while Minecraft is open, Windows rejects the file action.
              <br /><br />
              Muto solves this by copying loaded jars into a sandboxed memory-mapped shadow cache. The physical file in <code>mods/</code> is released immediately, allowing players and downloaders to freely overwrite or delete jars while Minecraft remains running.
            </>
          ),
          steps: [
            <>Jars dropped into <code>mods/</code> are indexed through sandboxed memory buffers.</>,
            <>Muto uses an exponential retry loop with 120ms backoff when inspecting jar headers during file-copy operations.</>,
            <>Operating system file descriptors are released immediately after entrypoint mapping.</>,
            <>If an external antivirus locks the file, Muto displays an alert toast rather than crashing the client.</>
          ]
        }
      ],
      related: [
        { id: 'start-reloading', label: 'Hot-Reloading Guide', desc: 'How to manage jars in the mods folder.' },
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
        { id: 'arch-rotation', label: 'Child ClassLoader Rotation', desc: 'Fallback mechanism on standard JVMs.' },
        { id: 'api-reference', label: 'Public MutoApi Reference', desc: 'API hooks for external tools.' }
      ]
    },
    'arch-rotation': {
      category: 'Architecture',
      title: 'Child ClassLoader Rotation',
      subtitle: 'Deep technical internals of the Muto hot-swap engine',
      sections: [
        {
          heading: 'Dynamic ClassLoader isolation',
          content: (
            <>
              Standard Fabric mods are loaded by Knot / SpongeMixin ClassLoaders at game start. Because Java does not allow unloading classes from a running ClassLoader without garbage-collecting the loader itself, Muto separates mods into child classloader trees.
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
        { id: 'arch-rotation', label: 'Child ClassLoader Rotation', desc: 'How classes are isolated.' },
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
        { id: 'arch-rotation', label: 'Child ClassLoader Rotation', desc: 'ClassLoader rotation details.' },
        { id: 'safe-guard', label: 'Safe Execution Guard', desc: 'In-world protection.' }
      ]
    }
  };

  const navCategories = [
    {
      title: 'Getting Started',
      items: [
        { id: 'overview', label: 'Architecture Overview', icon: BookOpen },
        { id: 'start-reloading', label: 'Hot-Reloading Guide', icon: RotateCw },
        { id: 'safe-guard', label: 'Safe Execution Guard', icon: ShieldCheck }
      ]
    },
    {
      title: 'Core Concepts',
      items: [
        { id: 'mod-tiers', label: 'Mod Classification Tiers', icon: Cpu },
        { id: 'modmenu-sync', label: 'ModMenu Live Sync', icon: Terminal },
        { id: 'cloth-config', label: 'Cloth Config UI', icon: Sliders }
      ]
    },
    {
      title: 'Developer Integration',
      items: [
        { id: 'resourcify-guide', label: 'Resourcify & Downloader API', icon: Layers },
        { id: 'api-reference', label: 'Public MutoApi Reference', icon: Code2 },
        { id: 'file-locks', label: 'Windows File Locking Solution', icon: Wrench },
        { id: 'jvm-tuning', label: 'JVM & JetBrains Runtime', icon: Zap }
      ]
    },
    {
      title: 'Architecture',
      items: [
        { id: 'arch-rotation', label: 'Child ClassLoader Rotation', icon: RotateCw },
        { id: 'registry-thawing', label: 'Registry Thawing Mechanics', icon: Lock },
        { id: 'atomic-rollback', label: 'Atomic Rollback Guarantee', icon: ShieldCheck }
      ]
    }
  ];

  const currentArticle = articles[selectedId] || articles.overview;

  const tocItems = [
    { id: 'overview', label: 'Architecture Overview' },
    { id: 'start-reloading', label: 'Hot-Reloading Guide' },
    { id: 'safe-guard', label: 'Safe Execution Guard' },
    { id: 'mod-tiers', label: 'Mod Classification Tiers' },
    { id: 'resourcify-guide', label: 'Resourcify Integration' },
    { id: 'api-reference', label: 'Public MutoApi Reference' },
    { id: 'file-locks', label: 'Windows File Locking' },
    { id: 'arch-rotation', label: 'ClassLoader Rotation' }
  ];

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
            href="https://modrinth.com/project/muto"
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

      {/* LEFT SIDEBAR (No footer, pure text hover) */}
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

            {/* Photocopy of Minecraft Title Screen & Buttons */}
            {currentArticle.hasMcDemo && <MinecraftTitleScreenMock />}

            {/* Overview Tier Breakdown */}
            {currentArticle.isOverview && (
              <div className="sub-feature-grid">
                <div className="sub-feature-card">
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
                    <div className="sub-feature-label">Clean Mods</div>
                    <span className="sb-badge" style={{ color: '#4ade80' }}>Tier 1</span>
                  </div>
                  <div className="sub-feature-desc">
                    HUDs, menus, and client tweaks. Hot-swap instantly with zero registry footprint.
                  </div>
                </div>

                <div className="sub-feature-card">
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
                    <div className="sub-feature-label">Standard Mods</div>
                    <span className="sb-badge sb-badge-beta">Tier 2</span>
                  </div>
                  <div className="sub-feature-desc">
                    Content mods registering blocks & items. Swapped via child loader rotation with registry unfreezing.
                  </div>
                </div>

                <div className="sub-feature-card">
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
                    <div className="sub-feature-label">Stubborn Mods</div>
                    <span className="sb-badge" style={{ color: '#ef4444' }}>Tier 3</span>
                  </div>
                  <div className="sub-feature-desc">
                    Fabric Loader & core mixins. Locked in root loader to preserve JVM memory safety.
                  </div>
                </div>
              </div>
            )}

            {/* Sections */}
            {currentArticle.sections.map((sec, sIdx) => (
              <div key={sIdx}>
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
            ))}

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
          <div>
            {tocItems.map((toc) => {
              const active = selectedId === toc.id;
              return (
                <a
                  key={toc.id}
                  className={active ? 'active' : ''}
                  onClick={() => setSelectedId(toc.id)}
                >
                  {toc.label}
                </a>
              );
            })}
          </div>
        </aside>

      </main>

    </div>
  );
}
