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
  Download,
  Lock,
  Zap,
  Terminal,
  Wrench,
  BookOpen,
  ArrowRight,
  ExternalLink,
  ChevronRight,
  RefreshCw,
  CheckCircle2,
  AlertTriangle
} from 'lucide-react';

function GithubIcon({ size = 15 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M15 22v-4a4.8 4.8 0 0 0-1-3.5c3 0 6-2 6-5.5.08-1.25-.27-2.48-1-3.5.28-1.15.28-2.35 0-3.5 0 0-1 0-3 1.5-2.64-.5-5.36-.5-8 0C6 2 5 2 5 2c-.3 1.15-.3 2.35 0 3.5A5.403 5.403 0 0 0 4 9c0 3.5 3 5.5 6 5.5-.39.49-.68 1.05-.85 1.65-.17.6-.22 1.23-.15 1.85v4" />
      <path d="M9 18c-4.51 2-5-2-7-2" />
    </svg>
  );
}

function CodeBox({ code, language = "java", filename }) {
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="code-container">
      <div className="code-topbar">
        <span>{filename || language}</span>
        <button
          onClick={handleCopy}
          style={{
            background: '#161c28',
            border: '1px solid #283347',
            color: '#e2e8f0',
            borderRadius: '4px',
            padding: '0.2rem 0.55rem',
            fontSize: '0.75rem',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '0.35rem',
            transition: 'color 0.15s ease'
          }}
        >
          {copied ? <Check size={12} color="#22c55e" /> : <Copy size={12} />}
          <span>{copied ? 'Copied' : 'Copy'}</span>
        </button>
      </div>
      <pre className="code-content">
        <code>{code}</code>
      </pre>
    </div>
  );
}

export default function App() {
  const [selectedId, setSelectedId] = useState('overview');
  const [searchQuery, setSearchQuery] = useState('');

  const articles = {
    overview: {
      category: 'Getting Started',
      title: 'Muto Architecture & Hot-Swap Overview',
      subtitle: 'Dynamic Runtime Mod Reloader for Minecraft Java Edition (Fabric Loader)',
      showcaseCards: true,
      sections: [
        {
          heading: 'How does live hot-swapping work in Minecraft?',
          content: (
            <>
              For over 15 years, Java modding treated mod discovery and class loading as immutable boot-time events. Changing a single mod required closing Minecraft and waiting through a lengthy JVM relaunch.
              <br /><br />
              Muto breaks this limitation by deploying dynamic <span className="tech-pill">MutoClassLoader</span> child instances. Reloadable mods are isolated into disposable classloader trees, allowing them to be loaded, unloaded, garbage collected, and swapped dynamically directly from the title screen in under 500 milliseconds.
            </>
          ),
          steps: [
            <>Drop or replace your mod jar inside the <span className="tech-pill">mods</span> directory.</>,
            <>Return to the Minecraft <span className="tech-pill">Title Screen</span>.</>,
            <>Click the yellow <span className="tech-pill">↻ Reload</span> button or trigger it via the public API.</>,
            <>Muto rotates the child classloader, unfreezes registries, and updates ModMenu in real time.</>
          ]
        },
        {
          heading: 'Can external mods like Resourcify integrate with Muto?',
          content: (
            <>
              Yes. External downloaders can query <span className="tech-pill">FabricLoader.getInstance().isModLoaded("muto")</span> and invoke <span className="tech-pill">MutoApi.reloadAsync()</span>. This allows players to browse, download, and activate mods inside Minecraft without ever closing the game.
            </>
          ),
          codeSnippet: {
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
        { id: 'resourcify-guide', label: 'Resourcify & Downloader API', desc: 'Enable in-game mod downloads with hot-swap support.' },
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
            <>Exit to the <span className="tech-pill">Title Screen</span> of Minecraft (never reload inside an active world).</>,
            <>Open your Minecraft instance directory and navigate to the <span className="tech-pill">mods</span> folder.</>,
            <>Place your new mod jar into <span className="tech-pill">mods</span>.</>,
            <>Click the yellow <span className="tech-pill">↻ Reload</span> button on the title screen.</>,
            <>A native toast notification will appear in the top-right corner confirming <span className="tech-pill">+1 added</span> along with the reload duration in milliseconds.</>
          ]
        },
        {
          heading: 'Updating an existing mod',
          steps: [
            <>Return to the <span className="tech-pill">Title Screen</span>.</>,
            <>Replace the older jar in <span className="tech-pill">mods</span> with the updated version.</>,
            <>Click the <span className="tech-pill">↻ Reload</span> button.</>,
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
              Muto implements a strict <span className="tech-pill">Safe Execution Guard</span>:
            </>
          ),
          steps: [
            <>When the game is paused inside an active world, the reload button is visually locked with a tooltip explaining that players must return to the title screen first.</>,
            <>The public API method <span className="tech-pill">MutoApi.isSafeToReload()</span> returns false whenever a client world or integrated server is active.</>,
            <>Direct invocations during unsafe states return an immediate failure result without mutating classloaders or registry state.</>
          ]
        }
      ],
      related: [
        { id: 'overview', label: 'Architecture Overview', desc: 'Explore the full classloader pipeline.' },
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
            <><strong style={{ color: '#22c55e' }}>Clean Mods (Tier 1):</strong> Client-side tweaks, HUDs, and UI helpers (e.g. AppleSkin, FullBright). They reload instantly in milliseconds without mutating registry topologies.</>,
            <><strong style={{ color: '#f59e0b' }}>Standard Mods (Tier 2):</strong> Mods that register custom blocks, items, sound events, or recipes (e.g. Distant Horizons, Structory). Reloaded via child classloader rotation with temporary registry unfreezing.</>,
            <><strong style={{ color: '#ef4444' }}>Stubborn / Core Mods (Tier 3):</strong> Foundational runtime modules (Fabric Loader, Fabric API, core mixin hooks). Locked in the root classloader to preserve native JVM memory safety.</>
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
              By default, ModMenu indexes installed mods only once at startup. When Muto hot-swaps or removes a mod, ModMenu is refreshed automatically.
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
            <><strong style={{ color: '#fff' }}>Toast Notifications:</strong> Enable or disable native Mojang system toasts for reload summaries.</>,
            <><strong style={{ color: '#fff' }}>Automatic Registry Unfreezing:</strong> Allow Standard tier mods to register new blocks and items during hot-swap transactions.</>,
            <><strong style={{ color: '#fff' }}>Developer Verbose Logging:</strong> Output detailed classloader rotation and bytecode diffs to <span className="tech-pill">logs/muto.log</span>.</>
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
            <>Add Muto API as a <span className="tech-pill">modCompileOnly</span> dependency in your <span className="tech-pill">build.gradle</span>.</>,
            <>Add <span className="tech-pill">"muto": "*"</span> under <span className="tech-pill">suggests</span> in your <span className="tech-pill">fabric.mod.json</span>.</>,
            <>When your downloader finishes saving a jar to disk, check <span className="tech-pill">FabricLoader.getInstance().isModLoaded("muto")</span>.</>,
            <>If Muto is present, verify <span className="tech-pill">MutoApi.isSafeToReload()</span> and call <span className="tech-pill">MutoApi.reloadAsync()</span>.</>,
            <>If Muto is absent, display a fallback message: <span className="tech-pill">Restart Minecraft to apply changes</span>.</>
          ],
          codeSnippet: {
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
          steps: [
            <>Add the Modrinth Maven repository to your <span className="tech-pill">repositories</span> block.</>,
            <>Declare Muto as a <span className="tech-pill">modCompileOnly</span> dependency.</>
          ],
          codeSnippet: {
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
      subtitle: 'Static methods and event lifecycle hooks for external mod developers',
      sections: [
        {
          heading: 'MutoApi static methods',
          steps: [
            <><span className="tech-pill">MutoApi.isAvailable()</span> — Returns true if Muto is active in the Fabric runtime.</>,
            <><span className="tech-pill">MutoApi.isReloading()</span> — Returns true if a reload transaction is currently underway.</>,
            <><span className="tech-pill">MutoApi.isSafeToReload()</span> — Returns true if client is safely on the Title Screen.</>,
            <><span className="tech-pill">MutoApi.reloadAsync()</span> — Dispatches non-blocking hot-reload returning CompletableFuture.</>,
            <><span className="tech-pill">MutoApi.getReloadableModIds()</span> — Returns tracked mod IDs eligible for live reloading.</>,
            <><span className="tech-pill">MutoApi.getStubbornModIds()</span> — Returns IDs of root-locked mods.</>
          ],
          codeSnippet: {
            filename: 'me.fy2ne.muto.api.MutoApi',
            code: `public final class MutoApi {
    public static boolean isAvailable();
    public static boolean isReloading();
    public static boolean isSafeToReload();
    public static CompletableFuture<ReloadResult> reloadAsync();
    public static Set<String> getReloadableModIds();
    public static Set<String> getStubbornModIds();
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
              On Windows, the operating system prevents open jar files from being overwritten or deleted (<span className="tech-pill">ERROR_SHARING_VIOLATION</span>). If a player attempts to delete an active mod jar while Minecraft is open, Windows rejects the file action.
              <br /><br />
              Muto solves this by copying loaded jars into a sandboxed memory-mapped shadow cache. The physical file in <span className="tech-pill">mods/</span> is released immediately, allowing players and downloaders to freely overwrite or delete jars while Minecraft remains running.
            </>
          ),
          steps: [
            <>Jars dropped into <span className="tech-pill">mods</span> are indexed through sandboxed memory buffers.</>,
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
              Standard JVMs allow hot-reloading method bodies only. To hot-reload added methods, modified fields, or newly introduced classes without full classloader disposal, Muto integrates with the <span className="tech-pill">JetBrains Runtime (JBR)</span>.
            </>
          ),
          steps: [
            <>Install JetBrains Runtime (JBR) for Minecraft.</>,
            <>Add the JVM argument: <span className="tech-pill">-XX:+AllowEnhancedClassRedefinition</span> in your launcher.</>,
            <>At game boot, Muto auto-detects DCEVM capabilities and logs: <span className="tech-pill">DCEVM active: true</span>.</>,
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
            <>Minecraft's <span className="tech-pill">MappedRegistry</span> is unfrozen, allowing new entries to be registered.</>,
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
              Vanilla Minecraft calls <span className="tech-pill">registry.freeze()</span> after the main menu loads. Registering a block or item after freeze throws an <span className="tech-pill">IllegalStateException: Registry is already frozen</span>.
              <br /><br />
              Muto safely unfreezes the registry topology during the reload window:
            </>
          ),
          steps: [
            <>Muto accesses the private <span className="tech-pill">frozen</span> boolean on <span className="tech-pill">MappedRegistry</span> via JVM accessor handle.</>,
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
            <>The broken mod is tagged as <span className="tech-pill">Faulty</span>, and the prior stable snapshot is immediately restored.</>,
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

  const currentArticle = articles[selectedId] || articles.overview;

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
        { id: 'arch-rotation', label: 'Child ClassLoader Rotation', icon: RefreshCw },
        { id: 'registry-thawing', label: 'Registry Thawing Mechanics', icon: Lock },
        { id: 'atomic-rollback', label: 'Atomic Rollback Guarantee', icon: ShieldCheck }
      ]
    }
  ];

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
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', position: 'relative' }}>
      
      {/* Subtle Ambient Lighting (No harsh grating grid) */}
      <div className="ambient-glow" />

      {/* Top Header */}
      <header style={{ borderBottom: '1px solid var(--border-subtle)', background: 'rgba(8, 10, 15, 0.85)', backdropFilter: 'blur(12px)', position: 'sticky', top: 0, zIndex: 50 }}>
        <div style={{ maxWidth: '1440px', margin: '0 auto', padding: '0.85rem 1.5rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '1rem' }}>
          
          {/* Logo with transparent background and user's clean yellow reload icon */}
          <div 
            onClick={() => setSelectedId('overview')}
            style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', cursor: 'pointer' }}
          >
            <img src="/icon.png" alt="Muto" style={{ width: '22px', height: '22px', objectFit: 'contain' }} />
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <span style={{ fontWeight: 700, fontSize: '1.15rem', color: '#fff', letterSpacing: '-0.02em' }}>Muto</span>
              <span style={{ background: 'rgba(245, 158, 11, 0.12)', border: '1px solid rgba(245, 158, 11, 0.25)', color: 'var(--amber-hover)', fontSize: '0.72rem', fontWeight: 600, padding: '0.1rem 0.45rem', borderRadius: '4px' }}>
                v0.1.0-beta
              </span>
            </div>
          </div>

          {/* Action Links */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
            <a
              href="https://github.com/fy2ne/muto"
              target="_blank"
              rel="noopener noreferrer"
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.45rem',
                background: 'rgba(255, 255, 255, 0.04)',
                border: '1px solid var(--border-subtle)',
                color: '#e2e8f0',
                padding: '0.4rem 0.8rem',
                borderRadius: '6px',
                fontSize: '0.82rem',
                textDecoration: 'none',
                transition: 'background-color 0.15s ease, color 0.15s ease'
              }}
            >
              <GithubIcon size={14} />
              <span>GitHub</span>
            </a>
            <a
              href="https://modrinth.com/project/muto"
              target="_blank"
              rel="noopener noreferrer"
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.45rem',
                background: 'var(--amber-primary)',
                color: '#080a0f',
                fontWeight: 600,
                padding: '0.4rem 0.9rem',
                borderRadius: '6px',
                fontSize: '0.82rem',
                textDecoration: 'none',
                transition: 'background-color 0.15s ease'
              }}
            >
              <Download size={14} />
              <span>Modrinth</span>
            </a>
          </div>
        </div>
      </header>

      {/* Docs 3-Column Layout */}
      <div style={{ flex: 1, position: 'relative', zIndex: 1 }}>
        <div className="docs-container">
          
          {/* LEFT COLUMN: Clean Nav Tree */}
          <aside className="docs-sidebar" style={{ borderRight: '1px solid var(--border-subtle)', padding: '1.75rem 1.15rem', overflowY: 'auto' }}>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.75rem' }}>
              {navCategories.map((cat, cIdx) => (
                <div key={cIdx}>
                  <div className="sidebar-category-title">{cat.title}</div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.2rem' }}>
                    {cat.items
                      .filter(item => !searchQuery || item.label.toLowerCase().includes(searchQuery.toLowerCase()))
                      .map((item) => {
                        const Icon = item.icon;
                        const active = selectedId === item.id;
                        return (
                          <div
                            key={item.id}
                            className={`nav-link ${active ? 'active' : ''}`}
                            onClick={() => setSelectedId(item.id)}
                          >
                            <Icon size={14} style={{ color: active ? 'var(--amber-primary)' : 'var(--text-muted)', flexShrink: 0 }} />
                            <span>{item.label}</span>
                          </div>
                        );
                      })}
                  </div>
                </div>
              ))}
            </div>

          </aside>

          {/* CENTER COLUMN: Polished Article Content */}
          <main style={{ padding: '2.5rem 3rem', overflowY: 'auto' }}>
            
            {/* Breadcrumb */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '1.25rem' }}>
              <span>Docs</span>
              <ChevronRight size={12} />
              <span>{currentArticle.category}</span>
              <ChevronRight size={12} />
              <span style={{ color: 'var(--text-secondary)' }}>{currentArticle.title}</span>
            </div>

            {/* Article Heading */}
            <div style={{ marginBottom: '2.5rem' }}>
              <h1 style={{ color: '#fff', fontSize: '2rem', fontWeight: 700, letterSpacing: '-0.025em', marginBottom: '0.5rem', lineHeight: 1.25 }}>
                {currentArticle.title}
              </h1>
              <p style={{ color: 'var(--text-secondary)', fontSize: '1.05rem', lineHeight: 1.6 }}>
                {currentArticle.subtitle}
              </p>
            </div>

            {/* Clean Showcase Cards for Overview */}
            {currentArticle.showcaseCards && (
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '1rem', marginBottom: '2.75rem' }}>
                
                <div className="feature-card">
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                    <span style={{ fontSize: '0.72rem', color: '#22c55e', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>Tier 1 · Instant</span>
                    <Zap size={14} color="#22c55e" />
                  </div>
                  <div style={{ color: '#fff', fontWeight: 600, fontSize: '0.96rem' }}>Clean Mods</div>
                  <div style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', marginTop: '0.35rem', lineHeight: 1.5 }}>
                    Client HUDs and UI tweaks (AppleSkin, FullBright). Zero registry overhead.
                  </div>
                </div>

                <div className="feature-card" style={{ borderColor: 'rgba(245, 158, 11, 0.25)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                    <span style={{ fontSize: '0.72rem', color: 'var(--amber-hover)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>Tier 2 · Rotated</span>
                    <Cpu size={14} color="var(--amber-hover)" />
                  </div>
                  <div style={{ color: '#fff', fontWeight: 600, fontSize: '0.96rem' }}>Standard Mods</div>
                  <div style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', marginTop: '0.35rem', lineHeight: 1.5 }}>
                    Content mods registering blocks & items. Rotated with registry unfreezing.
                  </div>
                </div>

                <div className="feature-card">
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                    <span style={{ fontSize: '0.72rem', color: '#ef4444', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>Tier 3 · Locked</span>
                    <Lock size={14} color="#ef4444" />
                  </div>
                  <div style={{ color: '#fff', fontWeight: 600, fontSize: '0.96rem' }}>Stubborn Mods</div>
                  <div style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', marginTop: '0.35rem', lineHeight: 1.5 }}>
                    Fabric Loader & core mixin hooks. Locked in root loader for JVM memory safety.
                  </div>
                </div>

              </div>
            )}

            {/* Article Sections */}
            {currentArticle.sections.map((sec, idx) => (
              <div key={idx} style={{ marginBottom: '2.75rem' }}>
                <h2 style={{ color: '#fff', fontSize: '1.25rem', fontWeight: 600, marginBottom: '0.85rem', letterSpacing: '-0.015em' }}>
                  {sec.heading}
                </h2>
                
                {sec.content && (
                  <p style={{ color: 'var(--text-secondary)', fontSize: '0.94rem', lineHeight: 1.75, marginBottom: '1.35rem' }}>
                    {sec.content}
                  </p>
                )}

                {sec.steps && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.45rem', marginBottom: '1.5rem' }}>
                    {sec.steps.map((st, sIdx) => (
                      <div key={sIdx} className="step-item">
                        <div className="step-marker">{sIdx + 1}</div>
                        <div style={{ color: '#e2e8f0', fontSize: '0.92rem', lineHeight: 1.6, paddingTop: '0.15rem' }}>
                          {st}
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {sec.codeSnippet && (
                  <CodeBox
                    filename={sec.codeSnippet.filename}
                    code={sec.codeSnippet.code}
                  />
                )}
              </div>
            ))}

            {/* Related Topics Section */}
            {currentArticle.related && currentArticle.related.length > 0 && (
              <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: '2.25rem', marginTop: '3rem' }}>
                <div style={{ fontSize: '0.88rem', fontWeight: 600, color: '#fff', marginBottom: '1rem' }}>
                  Next Steps & Related Topics
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '0.85rem' }}>
                  {currentArticle.related.map((rel, rIdx) => (
                    <div
                      key={rIdx}
                      className="related-card"
                      onClick={() => setSelectedId(rel.id)}
                    >
                      <div>
                        <div style={{ fontWeight: 600, fontSize: '0.88rem', color: '#fff' }}>{rel.label}</div>
                        <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>{rel.desc}</div>
                      </div>
                      <ArrowRight size={14} color="var(--amber-hover)" />
                    </div>
                  ))}
                </div>
              </div>
            )}

          </main>

          {/* RIGHT COLUMN: Minimal Search & On This Page */}
          <aside className="docs-toc" style={{ borderLeft: '1px solid var(--border-subtle)', padding: '1.75rem 1.25rem', overflowY: 'auto' }}>
            
            {/* Search Input */}
            <div style={{ marginBottom: '1.75rem', position: 'relative' }}>
              <input
                type="text"
                placeholder="Search documentation..."
                className="docs-search"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>

            {/* Table of Contents */}
            <div>
              <div style={{ fontSize: '0.72rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.08em', marginBottom: '0.75rem', paddingLeft: '0.65rem' }}>
                On This Page
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.2rem' }}>
                {tocItems.map((toc) => {
                  const active = selectedId === toc.id;
                  return (
                    <div
                      key={toc.id}
                      className={`toc-item ${active ? 'active' : ''}`}
                      onClick={() => setSelectedId(toc.id)}
                    >
                      {toc.label}
                    </div>
                  );
                })}
              </div>
            </div>

          </aside>

        </div>
      </div>

      {/* Footer */}
      <footer style={{ borderTop: '1px solid var(--border-subtle)', padding: '1.5rem', background: '#080a0f', position: 'relative', zIndex: 1 }}>
        <div style={{ maxWidth: '1440px', margin: '0 auto', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '1rem', fontSize: '0.84rem', color: 'var(--text-muted)' }}>
          <div>
            Muto — Dynamic Runtime Mod Reloader for Minecraft Java Edition. Developed by <a href="https://github.com/fy2ne" target="_blank" rel="noopener noreferrer" style={{ color: 'var(--amber-hover)', textDecoration: 'none' }}>fy2ne</a>.
          </div>
          <div style={{ display: 'flex', gap: '1.5rem' }}>
            <a href="https://github.com/fy2ne/muto" target="_blank" rel="noopener noreferrer" style={{ color: 'var(--text-secondary)', textDecoration: 'none', transition: 'color 0.15s ease' }}>GitHub</a>
            <a href="https://modrinth.com/project/muto" target="_blank" rel="noopener noreferrer" style={{ color: 'var(--text-secondary)', textDecoration: 'none', transition: 'color 0.15s ease' }}>Modrinth</a>
            <a href="https://fy2ne.me" target="_blank" rel="noopener noreferrer" style={{ color: 'var(--text-secondary)', textDecoration: 'none', transition: 'color 0.15s ease' }}>fy2ne.me</a>
          </div>
        </div>
      </footer>
    </div>
  );
}
