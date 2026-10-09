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
  ExternalLink,
  Search,
  X,
  FileCode,
  FolderOpen
} from 'lucide-react';

function GithubIcon({ size = 14 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M15 22v-4a4.8 4.8 0 0 0-1-3.5c3 0 6-2 6-5.5.08-1.25-.27-2.48-1-3.5.28-1.15.28-2.35 0-3.5 0 0-1 0-3 1.5-2.64-.5-5.36-.5-8 0C6 2 5 2 5 2c-.3 1.15-.3 2.35 0 3.5A5.403 5.403 0 0 0 4 9c0 3.5 3 5.5 6 5.5-.39.49-.68 1.05-.85 1.65-.17.6-.22 1.23-.15 1.85v4" />
      <path d="M9 18c-4.51 2-5-2-7-2" />
    </svg>
  );
}

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

export default function App() {
  const [selectedId, setSelectedId] = useState('overview');
  const [query, setQuery] = useState('');

  const articles = {
    overview: {
      category: 'Getting Started',
      title: 'Architecture Overview',
      subtitle: 'Dynamic Runtime Mod Reloader for Minecraft Java Edition on Fabric Loader',
      isOverview: true,
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
      subtitle: 'Static methods and event lifecycle hooks for external mod developers',
      sections: [
        {
          heading: 'MutoApi static methods',
          steps: [
            <><code>MutoApi.isAvailable()</code> — Returns true if Muto is active in the Fabric runtime.</>,
            <><code>MutoApi.isReloading()</code> — Returns true if a reload transaction is currently underway.</>,
            <><code>MutoApi.isSafeToReload()</code> — Returns true if client is safely on the Title Screen.</>,
            <><code>MutoApi.reloadAsync()</code> — Dispatches non-blocking hot-reload returning CompletableFuture.</>,
            <><code>MutoApi.getReloadableModIds()</code> — Returns tracked mod IDs eligible for live reloading.</>,
            <><code>MutoApi.getStubbornModIds()</code> — Returns IDs of root-locked mods.</>
          ],
          snippet: {
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
      
      {/* HEADER (Weavetab style) */}
      <header className="docs-header">
        <div className="docs-header-left">
          <div className="brand" onClick={() => setSelectedId('overview')}>
            <img src="/icon.png" alt="Muto" style={{ width: '20px', height: '20px', objectFit: 'contain' }} />
            <span className="brand-text">Muto</span>
            <span className="brand-badge">v0.1.0-beta</span>
          </div>
        </div>

        <div className="docs-header-right">
          <a
            href="https://github.com/fy2ne/muto"
            target="_blank"
            rel="noopener noreferrer"
            className="docs-navlink"
          >
            <GithubIcon size={14} />
            <span>GitHub</span>
          </a>
          <a
            href="https://modrinth.com/project/muto"
            target="_blank"
            rel="noopener noreferrer"
            className="docs-navlink"
          >
            <span>Modrinth</span>
          </a>
          <a
            href="https://fy2ne.me"
            target="_blank"
            rel="noopener noreferrer"
            className="docs-navlink"
          >
            <span>fy2ne.me</span>
          </a>
        </div>
      </header>

      {/* LEFT SIDEBAR (Weavetab style: pure text-hover, no box background) */}
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

        {/* Sidebar Footer */}
        <div className="sb-foot">
          <span className="sb-foot-dot" />
          <span className="sb-foot-meta">MUTO ENGINE · ONLINE</span>
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

        {/* RIGHT TOC (Weavetab style: minimal text hover) */}
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
