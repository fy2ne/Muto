import React, { useState } from 'react';
import {
  RotateCw,
  Cpu,
  Layers,
  ShieldCheck,
  Sliders,
  Code2,
  HelpCircle,
  Check,
  Copy,
  ChevronDown,
  ChevronRight,
  Download,
  Lock,
  Zap,
  Terminal,
  FileCode,
  Wrench,
  BookOpen,
  ArrowUpRight
} from 'lucide-react';

function GithubIcon({ size = 15, style = {} }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={style}>
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
    <div className="code-block">
      <div className="code-header">
        <span>{filename || language}</span>
        <button
          onClick={handleCopy}
          style={{
            background: '#141822',
            border: '1px solid #283142',
            color: '#e5e7eb',
            borderRadius: '4px',
            padding: '0.15rem 0.5rem',
            fontSize: '0.74rem',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '0.3rem',
            transition: 'color 0.15s ease'
          }}
        >
          {copied ? <Check size={11} color="#22c55e" /> : <Copy size={11} />}
          <span>{copied ? 'Copied' : 'Copy'}</span>
        </button>
      </div>
      <pre className="code-pre">
        <code>{code}</code>
      </pre>
    </div>
  );
}

export default function App() {
  const [selectedId, setSelectedId] = useState('overview');
  const [searchQuery, setSearchQuery] = useState('');
  const [featuresOpen, setFeaturesOpen] = useState(true);
  const [guidesOpen, setGuidesOpen] = useState(true);
  const [archOpen, setArchOpen] = useState(true);

  const articles = {
    overview: {
      title: 'Muto Architecture & Hot-Swap Overview',
      subtitle: 'Dynamic Runtime Mod Reloader for Minecraft Java Edition (Fabric 26.3)',
      showcaseCards: true,
      sections: [
        {
          heading: 'How does live hot-swapping work in Minecraft?',
          content: (
            <>
              For over 15 years, Java modding treated mod discovery and class loading as immutable boot-time events. Changing a single mod required closing Minecraft and waiting through a 60-second JVM relaunch.
              <br /><br />
              Muto breaks this limitation by deploying dynamic <span className="key-badge">MutoClassLoader</span> child instances. Reloadable mods are isolated into their own classloader trees, allowing them to be loaded, unloaded, garbage collected, and swapped dynamically directly from the title screen in under 500 milliseconds.
            </>
          ),
          steps: [
            <>Drop or replace your mod jar inside the <span className="key-badge">mods</span> directory.</>,
            <>Return to the Minecraft <span className="key-badge">Title Screen</span>.</>,
            <>Click the yellow <span className="key-badge">↻ Reload</span> button or trigger it via the public API.</>,
            <>Muto rotates the child classloader, unfreezes registries, and updates ModMenu in real time.</>
          ]
        },
        {
          heading: 'Can external mods like Resourcify integrate with Muto?',
          content: (
            <>
              Yes. External downloaders can query <span className="key-badge">FabricLoader.getInstance().isModLoaded("muto")</span> and invoke <span className="key-badge">MutoApi.reloadAsync()</span>. This allows players to browse, download, and activate mods inside Minecraft without ever closing the game.
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
        { id: 'resourcify-guide', label: 'Resourcify & Downloader API', icon: Layers },
        { id: 'mod-tiers', label: 'Mod Classification Tiers', icon: Cpu },
        { id: 'api-reference', label: 'Public MutoApi Reference', icon: Code2 },
        { id: 'file-locks', label: 'Windows File Locking Solution', icon: Wrench }
      ]
    },
    'start-reloading': {
      title: 'Hot-Reloading Mods on the Fly',
      subtitle: 'Adding, updating, and removing mods without restarting the client',
      sections: [
        {
          heading: 'Adding a new mod jar',
          steps: [
            <>Exit to the <span className="key-badge">Title Screen</span> of Minecraft (never reload inside a live world).</>,
            <>Open your Minecraft instance directory and navigate to the <span className="key-badge">mods</span> folder.</>,
            <>Move your new mod jar (e.g. <span className="key-badge">appleskin-fabric-*.jar</span>) into <span className="key-badge">mods</span>.</>,
            <>Click the yellow circular <span className="key-badge">↻ Reload</span> button on the title screen.</>,
            <>A native toast notification will appear in the top-right corner confirming <span className="key-badge">+1 added</span> along with the reload duration in milliseconds.</>
          ]
        },
        {
          heading: 'Updating an existing mod',
          steps: [
            <>Return to the <span className="key-badge">Title Screen</span>.</>,
            <>Replace the older jar in <span className="key-badge">mods</span> with the new updated version.</>,
            <>Press the <span className="key-badge">↻ Reload</span> button.</>,
            <>Muto rotates the child classloader, tears down old instances, and boots the new entrypoints cleanly.</>
          ]
        }
      ],
      related: [
        { id: 'mod-tiers', label: 'Mod Classification Tiers', icon: Cpu },
        { id: 'file-locks', label: 'Windows File Locking Guide', icon: Wrench }
      ]
    },
    'mod-tiers': {
      title: 'Mod Classification Tiers',
      subtitle: 'Understanding Clean, Standard, and Stubborn mods',
      sections: [
        {
          heading: 'Three operational tiers for maximum stability',
          content: (
            <>
              To guarantee zero JVM crashes, Muto categorizes all discovered mods into three isolated operational tiers based on their entrypoint signatures and registry interactions.
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
        { id: 'start-reloading', label: 'Start Reloading Guide', icon: RotateCw },
        { id: 'arch-rotation', label: 'Child ClassLoader Rotation', icon: Zap }
      ]
    },
    'resourcify-guide': {
      title: 'Resourcify & Downloader Integration',
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
            <>Add Muto API as a <span className="key-badge">modCompileOnly</span> dependency in your <span className="key-badge">build.gradle</span>.</>,
            <>Add <span className="key-badge">"muto": "&gt;=0.1.0-beta.1"</span> under <span className="key-badge">suggests</span> in your <span className="key-badge">fabric.mod.json</span>.</>,
            <>When your downloader finishes saving a jar to disk, check <span className="key-badge">FabricLoader.getInstance().isModLoaded("muto")</span>.</>,
            <>If Muto is present, verify <span className="key-badge">MutoApi.isSafeToReload()</span> and call <span className="key-badge">MutoApi.reloadAsync()</span>.</>,
            <>If Muto is absent, display a fallback message: <span className="key-badge">Restart Minecraft to apply changes</span>.</>
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
            <>Add the Modrinth Maven repository to your <span className="key-badge">repositories</span> block.</>,
            <>Declare Muto as a <span className="key-badge">modCompileOnly</span> dependency.</>
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
        { id: 'api-reference', label: 'Public MutoApi Reference', icon: Code2 },
        { id: 'overview', label: 'Architecture Overview', icon: BookOpen }
      ]
    },
    'file-locks': {
      title: 'Windows File Locking & Shadow Cache',
      subtitle: 'Eliminating the "File in use by another process" Windows operating system lock',
      sections: [
        {
          heading: 'Why Windows locks jar files and how Muto solves it',
          content: (
            <>
              On Windows, the operating system prevents open jar files from being overwritten or deleted (`ERROR_SHARING_VIOLATION`). If a player attempts to delete an active mod jar while Minecraft is open, Windows rejects the file action.
              <br /><br />
              Muto solves this by copying loaded jars into a sandboxed memory-mapped shadow cache. The physical file in <span className="key-badge">mods/</span> is released immediately, allowing players and downloaders to freely overwrite or delete jars while Minecraft remains running.
            </>
          ),
          steps: [
            <>Jars dropped into <span className="key-badge">mods</span> are indexed through sandboxed memory buffers.</>,
            <>Muto uses an exponential retry loop with 120ms backoff when inspecting jar headers during file-copy operations.</>,
            <>Operating system file descriptors are released immediately after entrypoint mapping.</>,
            <>If an external antivirus locks the file, Muto displays an alert toast rather than crashing the client.</>
          ]
        }
      ],
      related: [
        { id: 'start-reloading', label: 'Hot-Reloading Guide', icon: RotateCw },
        { id: 'arch-rotation', label: 'Child ClassLoader Rotation', icon: Zap }
      ]
    },
    'api-reference': {
      title: 'Public MutoApi Reference',
      subtitle: 'API methods and event lifecycle hooks for external mod developers',
      sections: [
        {
          heading: 'MutoApi static methods',
          steps: [
            <><span className="key-badge">MutoApi.isAvailable()</span> — Returns true if Muto is active in the Fabric runtime.</>,
            <><span className="key-badge">MutoApi.isReloading()</span> — Returns true if a reload transaction is currently underway.</>,
            <><span className="key-badge">MutoApi.isSafeToReload()</span> — Returns true if client is safely on the Title Screen.</>,
            <><span className="key-badge">MutoApi.reloadAsync()</span> — Dispatches non-blocking hot-reload returning CompletableFuture.</>,
            <><span className="key-badge">MutoApi.getReloadableModIds()</span> — Returns tracked mod IDs eligible for live reloading.</>,
            <><span className="key-badge">MutoApi.getStubbornModIds()</span> — Returns IDs of root-locked mods.</>
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
        { id: 'resourcify-guide', label: 'Resourcify Integration', icon: Layers },
        { id: 'overview', label: 'Muto Overview', icon: BookOpen }
      ]
    },
    'arch-rotation': {
      title: 'Child ClassLoader Rotation & Registry Thawing',
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
            <>Minecraft's <span className="key-badge">MappedRegistry</span> is unfrozen, allowing new entries to be registered.</>,
            <>A fresh child ClassLoader is initialized with the updated jar files.</>,
            <>The new mod entrypoints execute, ModMenu receives refreshed metadata, and registries are refrozen.</>
          ]
        }
      ],
      related: [
        { id: 'mod-tiers', label: 'Mod Classification Tiers', icon: Cpu },
        { id: 'overview', label: 'Architecture Overview', icon: BookOpen }
      ]
    }
  };

  const featureLinks = [
    { id: 'overview', label: 'Architecture Overview', icon: BookOpen },
    { id: 'start-reloading', label: 'Start Reloading', icon: RotateCw },
    { id: 'mod-tiers', label: 'Mod Classification Tiers', icon: Cpu },
    { id: 'start-reloading', label: 'Title Screen (↻) Trigger', icon: Sliders },
    { id: 'overview', label: 'Safe Execution Guard', icon: ShieldCheck },
    { id: 'overview', label: 'ModMenu Live Sync', icon: Terminal },
    { id: 'overview', label: 'Cloth Config UI', icon: Sliders },
  ];

  const guideLinks = [
    { id: 'resourcify-guide', label: 'Resourcify & Downloader API', icon: Layers },
    { id: 'file-locks', label: 'Windows File Locking Solution', icon: Wrench },
    { id: 'api-reference', label: 'Public MutoApi Reference', icon: Code2 },
    { id: 'overview', label: 'JVM & JBR Arguments', icon: Terminal },
  ];

  const archLinks = [
    { id: 'arch-rotation', label: 'Child ClassLoader Rotation', icon: Zap },
    { id: 'arch-rotation', label: 'MappedRegistry Thawing', icon: Lock },
    { id: 'overview', label: 'Atomic Rollback Guarantee', icon: ShieldCheck },
  ];

  const currentArticle = articles[selectedId] || articles.overview;

  const tocItems = [
    { id: 'overview', number: '1.', label: 'What is Muto?' },
    { id: 'start-reloading', number: '2.', label: 'Hot-Swap & Reload Guide' },
    { id: 'mod-tiers', number: '3.', label: 'Mod Classification Tiers' },
    { id: 'resourcify-guide', number: '4.', label: 'In-Game Downloader API (Resourcify)' },
    { id: 'file-locks', number: '5.', label: 'Windows Lock Resolution' },
    { id: 'api-reference', number: '6.', label: 'Public MutoApi Reference' },
    { id: 'arch-rotation', number: '7.', label: 'Child ClassLoader Rotation' },
  ];

  return (
    <div className="bg-grid" style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Top Header */}
      <header style={{ borderBottom: '1px solid var(--border-grid)', background: 'rgba(6, 7, 9, 0.95)', backdropFilter: 'blur(8px)', position: 'sticky', top: 0, zIndex: 50 }}>
        <div style={{ maxWidth: '1440px', margin: '0 auto', padding: '0.75rem 1.5rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '1rem' }}>
          
          {/* Logo with transparent background and yellow reload icon */}
          <div 
            onClick={() => setSelectedId('overview')}
            style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', cursor: 'pointer' }}
          >
            {/* Zero background wrapper, clean yellow SVG icon */}
            <RotateCw size={22} color="#f59e0b" strokeWidth={2.4} />
            <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.45rem' }}>
              <span style={{ fontWeight: 700, fontSize: '1.15rem', color: '#fff', letterSpacing: '-0.02em' }}>Muto</span>
              <span style={{ color: 'var(--gold-main)', fontSize: '0.8rem', fontWeight: 600 }}>Wiki</span>
            </div>
          </div>

          {/* Header Action Buttons */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
            <a
              href="https://github.com/fy2ne/muto"
              target="_blank"
              rel="noopener noreferrer"
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.45rem',
                background: '#0d1017',
                border: '1px solid var(--border-ui)',
                color: '#e5e7eb',
                padding: '0.35rem 0.75rem',
                borderRadius: '5px',
                fontSize: '0.82rem',
                textDecoration: 'none',
                transition: 'color 0.15s ease'
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
                background: 'var(--gold-main)',
                color: '#060709',
                fontWeight: 600,
                padding: '0.35rem 0.85rem',
                borderRadius: '5px',
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

      {/* 3-Column Layout (Matching Screenshot) */}
      <div style={{ maxWidth: '1440px', width: '100%', margin: '0 auto', flex: 1 }}>
        <div className="wiki-layout">
          
          {/* LEFT COLUMN: Tree Navigation */}
          <aside className="nav-sidebar" style={{ borderRight: '1px solid var(--border-grid)', padding: '1.5rem 1rem', overflowY: 'auto' }}>
            
            {/* Features Collapsible */}
            <div style={{ marginBottom: '1.25rem' }}>
              <div 
                className="category-toggle" 
                onClick={() => setFeaturesOpen(!featuresOpen)}
                style={{ color: '#f59e0b' }}
              >
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  {featuresOpen ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
                  <span>Features</span>
                </span>
              </div>
              {featuresOpen && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.15rem', marginTop: '0.25rem', paddingLeft: '0.5rem' }}>
                  {featureLinks
                    .filter(link => !searchQuery || link.label.toLowerCase().includes(searchQuery.toLowerCase()))
                    .map((link, idx) => {
                      const Icon = link.icon;
                      const active = selectedId === link.id;
                      return (
                        <div
                          key={idx}
                          className={`nav-tree-item ${active ? 'active' : ''}`}
                          onClick={() => setSelectedId(link.id)}
                        >
                          <Icon size={13} style={{ color: active ? '#f59e0b' : '#6b7280', flexShrink: 0 }} />
                          <span>{link.label}</span>
                        </div>
                      );
                    })}
                </div>
              )}
            </div>

            {/* Guides Collapsible */}
            <div style={{ marginBottom: '1.25rem' }}>
              <div 
                className="category-toggle" 
                onClick={() => setGuidesOpen(!guidesOpen)}
                style={{ color: '#d97706' }}
              >
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  {guidesOpen ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
                  <span>Guides</span>
                </span>
              </div>
              {guidesOpen && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.15rem', marginTop: '0.25rem', paddingLeft: '0.5rem' }}>
                  {guideLinks
                    .filter(link => !searchQuery || link.label.toLowerCase().includes(searchQuery.toLowerCase()))
                    .map((link, idx) => {
                      const Icon = link.icon;
                      const active = selectedId === link.id;
                      return (
                        <div
                          key={idx}
                          className={`nav-tree-item ${active ? 'active' : ''}`}
                          onClick={() => setSelectedId(link.id)}
                        >
                          <Icon size={13} style={{ color: active ? '#d97706' : '#6b7280', flexShrink: 0 }} />
                          <span>{link.label}</span>
                        </div>
                      );
                    })}
                </div>
              )}
            </div>

            {/* Architecture Collapsible */}
            <div style={{ marginBottom: '1.25rem' }}>
              <div 
                className="category-toggle" 
                onClick={() => setArchOpen(!archOpen)}
                style={{ color: '#eab308' }}
              >
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  {archOpen ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
                  <span>Architecture</span>
                </span>
              </div>
              {archOpen && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.15rem', marginTop: '0.25rem', paddingLeft: '0.5rem' }}>
                  {archLinks
                    .filter(link => !searchQuery || link.label.toLowerCase().includes(searchQuery.toLowerCase()))
                    .map((link, idx) => {
                      const Icon = link.icon;
                      const active = selectedId === link.id;
                      return (
                        <div
                          key={idx}
                          className={`nav-tree-item ${active ? 'active' : ''}`}
                          onClick={() => setSelectedId(link.id)}
                        >
                          <Icon size={13} style={{ color: active ? '#eab308' : '#6b7280', flexShrink: 0 }} />
                          <span>{link.label}</span>
                        </div>
                      );
                    })}
                </div>
              )}
            </div>

          </aside>

          {/* CENTER COLUMN: Main Content */}
          <main style={{ padding: '2rem 2.5rem', overflowY: 'auto' }}>
            
            {/* Top Showcase Cards (like top items in screenshot) */}
            {currentArticle.showcaseCards && (
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '2.5rem' }}>
                <div className="tier-card" style={{ borderLeft: '3px solid #22c55e' }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.4rem' }}>
                    <span style={{ fontSize: '0.74rem', color: '#22c55e', fontWeight: 600, textTransform: 'uppercase' }}>Tier 1</span>
                    <Zap size={14} color="#22c55e" />
                  </div>
                  <div style={{ color: '#fff', fontWeight: 600, fontSize: '0.98rem' }}>Clean Mods</div>
                  <div style={{ fontSize: '0.8rem', color: '#9ca3af', marginTop: '0.25rem' }}>HUDs, UI tweaks, graphics (AppleSkin, FullBright). Zero registry overhead.</div>
                </div>

                <div className="tier-card" style={{ borderLeft: '3px solid #f59e0b', background: '#0c0f16' }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.4rem' }}>
                    <span style={{ fontSize: '0.74rem', color: '#f59e0b', fontWeight: 600, textTransform: 'uppercase' }}>Tier 2</span>
                    <Cpu size={14} color="#f59e0b" />
                  </div>
                  <div style={{ color: '#fff', fontWeight: 600, fontSize: '0.98rem' }}>Standard Mods</div>
                  <div style={{ fontSize: '0.8rem', color: '#9ca3af', marginTop: '0.25rem' }}>Blocks, items, worldgen (Distant Horizons, Structory). Registry unfreezing.</div>
                </div>

                <div className="tier-card" style={{ borderLeft: '3px solid #ef4444' }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.4rem' }}>
                    <span style={{ fontSize: '0.74rem', color: '#ef4444', fontWeight: 600, textTransform: 'uppercase' }}>Tier 3</span>
                    <Lock size={14} color="#ef4444" />
                  </div>
                  <div style={{ color: '#fff', fontWeight: 600, fontSize: '0.98rem' }}>Stubborn Mods</div>
                  <div style={{ fontSize: '0.8rem', color: '#9ca3af', marginTop: '0.25rem' }}>Fabric Loader, Fabric API, core mixin injectors. Locked in root classloader.</div>
                </div>
              </div>
            )}

            {/* Article Header */}
            <div style={{ marginBottom: '2rem' }}>
              <h1 style={{ color: '#fff', fontSize: '1.75rem', fontWeight: 700, letterSpacing: '-0.02em', marginBottom: '0.4rem' }}>
                {currentArticle.title}
              </h1>
              <p style={{ color: '#9ca3af', fontSize: '0.94rem' }}>
                {currentArticle.subtitle}
              </p>
            </div>

            {/* Article Sections (Matching screenshot's clean paragraphs and questions) */}
            {currentArticle.sections.map((sec, idx) => (
              <div key={idx} style={{ marginBottom: '2.5rem' }}>
                <h2 style={{ color: '#fff', fontSize: '1.18rem', fontWeight: 600, marginBottom: '0.75rem' }}>
                  {sec.heading}
                </h2>
                
                {sec.content && (
                  <p style={{ color: '#9ca3af', fontSize: '0.92rem', lineHeight: 1.7, marginBottom: '1.25rem' }}>
                    {sec.content}
                  </p>
                )}

                {sec.steps && (
                  <ol style={{ paddingLeft: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.75rem', marginBottom: '1.25rem' }}>
                    {sec.steps.map((st, sIdx) => (
                      <li key={sIdx} style={{ color: '#d1d5db', fontSize: '0.9rem', lineHeight: 1.6 }}>
                        {st}
                      </li>
                    ))}
                  </ol>
                )}

                {sec.codeSnippet && (
                  <CodeBox
                    filename={sec.codeSnippet.filename}
                    code={sec.codeSnippet.code}
                  />
                )}
              </div>
            ))}

            {/* Related Pages Section (like screenshot's bottom left) */}
            {currentArticle.related && currentArticle.related.length > 0 && (
              <div style={{ borderTop: '1px solid var(--border-grid)', paddingTop: '1.75rem', marginTop: '2rem' }}>
                <div style={{ fontSize: '0.86rem', fontWeight: 600, color: '#fff', marginBottom: '0.75rem' }}>
                  Related Pages
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.45rem' }}>
                  {currentArticle.related.map((rel, rIdx) => {
                    const RelIcon = rel.icon;
                    return (
                      <div
                        key={rIdx}
                        className="related-link"
                        onClick={() => setSelectedId(rel.id)}
                      >
                        <RelIcon size={14} />
                        <span>{rel.label}</span>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

          </main>

          {/* RIGHT COLUMN: Search + Table of Contents (Matching Screenshot) */}
          <aside className="toc-sidebar" style={{ borderLeft: '1px solid var(--border-grid)', padding: '1.5rem 1.25rem', overflowY: 'auto' }}>
            
            {/* Minimal Search Input (top right) */}
            <div style={{ marginBottom: '1.75rem' }}>
              <input
                type="text"
                placeholder="Search wiki..."
                className="search-box"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>

            {/* Numbered Table of Contents (matching 1., 2., 3., 4., 5. in screenshot) */}
            <div>
              <div style={{ fontSize: '0.78rem', fontWeight: 600, color: '#6b7280', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '0.65rem' }}>
                On This Page
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.2rem' }}>
                {tocItems.map((toc, idx) => {
                  const active = selectedId === toc.id;
                  return (
                    <div
                      key={idx}
                      className={`toc-link ${active ? 'active' : ''}`}
                      onClick={() => setSelectedId(toc.id)}
                    >
                      <span style={{ color: active ? 'var(--gold-hover)' : 'var(--text-muted)', marginRight: '0.4rem' }}>{toc.number}</span>
                      <span>{toc.label}</span>
                    </div>
                  );
                })}
              </div>
            </div>

          </aside>

        </div>
      </div>

      {/* Footer */}
      <footer style={{ borderTop: '1px solid var(--border-grid)', padding: '1.25rem 1.5rem', background: '#060709' }}>
        <div style={{ maxWidth: '1440px', margin: '0 auto', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '1rem', fontSize: '0.82rem', color: '#6b7280' }}>
          <div>
            Muto Mod for Minecraft Java Edition. Created by <a href="https://github.com/fy2ne" target="_blank" rel="noopener noreferrer" style={{ color: 'var(--gold-main)', textDecoration: 'none' }}>fy2ne</a>.
          </div>
          <div style={{ display: 'flex', gap: '1.25rem' }}>
            <a href="https://github.com/fy2ne/muto" target="_blank" rel="noopener noreferrer" style={{ color: '#9ca3af', textDecoration: 'none', transition: 'color 0.15s ease' }}>GitHub</a>
            <a href="https://modrinth.com/project/muto" target="_blank" rel="noopener noreferrer" style={{ color: '#9ca3af', textDecoration: 'none', transition: 'color 0.15s ease' }}>Modrinth</a>
            <a href="https://fy2ne.me" target="_blank" rel="noopener noreferrer" style={{ color: '#9ca3af', textDecoration: 'none', transition: 'color 0.15s ease' }}>fy2ne.me</a>
          </div>
        </div>
      </footer>
    </div>
  );
}
