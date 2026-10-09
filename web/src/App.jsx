import React, { useState, useMemo } from 'react';
import {
  RotateCw,
  Cpu,
  Layers,
  ShieldCheck,
  Sliders,
  Code2,
  HelpCircle,
  ExternalLink,
  Check,
  Copy,
  Search,
  Download,
  Info,
  ArrowLeft,
  Bell,
  Lock,
  History,
  FileCode,
  Terminal,
  Zap,
  Wrench,
  BookOpen
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
    <div className="code-box">
      <div className="code-box-header">
        <span>{filename || language}</span>
        <button
          onClick={handleCopy}
          style={{
            background: '#1c2028',
            border: '1px solid #303744',
            color: '#e5e7eb',
            borderRadius: '4px',
            padding: '0.15rem 0.5rem',
            fontSize: '0.74rem',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '0.3rem'
          }}
        >
          {copied ? <Check size={11} color="#22c55e" /> : <Copy size={11} />}
          <span>{copied ? 'Copied' : 'Copy'}</span>
        </button>
      </div>
      <pre className="code-box-pre">
        <code>{code}</code>
      </pre>
    </div>
  );
}

export default function App() {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedArticle, setSelectedArticle] = useState(null);

  // Articles data formatted with steps and key badges (Image 2 style)
  const articles = {
    'start-reloading': {
      title: 'Hot-Reloading Mods on the Fly',
      category: 'Live Reload Engine',
      sections: [
        {
          title: 'Adding a new mod jar',
          steps: [
            <>Exit to the <span className="key-badge">Title Screen</span> of Minecraft (never reload inside a live world).</>,
            <>Open your Minecraft instance directory and navigate to the <span className="key-badge">mods</span> folder.</>,
            <>Copy or move your new mod jar file (e.g. <span className="key-badge">appleskin-fabric-*.jar</span>) into the <span className="key-badge">mods</span> folder.</>,
            <>Return to Minecraft and click the yellow circular <span className="key-badge">↻ Reload</span> button on the title screen.</>,
            <>Wait for the reload sequence. A native toast notification will appear in the top-right corner confirming <span className="key-badge">+1 added</span> with the exact reload duration in milliseconds.</>
          ]
        },
        {
          title: 'Updating an existing mod jar',
          steps: [
            <>Return to the <span className="key-badge">Title Screen</span>.</>,
            <>Replace the older jar in <span className="key-badge">mods/</span> with the new updated version.</>,
            <>Press the <span className="key-badge">↻ Reload</span> button.</>,
            <>Muto rotates the child classloader, unbinds the old jar references, and re-initializes entrypoints cleanly.</>
          ]
        },
        {
          title: 'Removing or disabling a mod',
          steps: [
            <>On the <span className="key-badge">Title Screen</span>, delete the mod jar or append <span className="key-badge">.disabled</span> to its filename.</>,
            <>Click the <span className="key-badge">↻ Reload</span> button.</>,
            <>Muto detects the removal, notifies listeners via <span className="key-badge">MutoEvents.RELOAD_FINISH</span>, and unregisters its resources.</>
          ]
        }
      ]
    },
    'resourcify-guide': {
      title: 'Resourcify & In-Game Downloader Integration',
      category: 'For Developers',
      sections: [
        {
          title: 'How soft-dependency integration works',
          desc: 'External mods (like Resourcify) can allow players to search and download mods in-game, automatically hot-reloading them with Muto without restarting the client.',
          steps: [
            <>Add Muto API as a <span className="key-badge">modCompileOnly</span> dependency in your <span className="key-badge">build.gradle</span>.</>,
            <>Add <span className="key-badge">"muto": "&gt;=0.1.0-beta.1"</span> under <span className="key-badge">suggests</span> in your <span className="key-badge">fabric.mod.json</span>.</>,
            <>When your downloader finishes saving a jar to disk, query <span className="key-badge">FabricLoader.getInstance().isModLoaded("muto")</span>.</>,
            <>If Muto is active, verify <span className="key-badge">MutoApi.isSafeToReload()</span> and invoke <span className="key-badge">MutoApi.reloadAsync()</span>.</>,
            <>If Muto is absent, display a fallback message: <span className="key-badge">Restart Minecraft to apply</span>.</>
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
          title: 'Gradle dependency configuration',
          steps: [
            <>Open your project's <span className="key-badge">build.gradle</span>.</>,
            <>Add the Modrinth Maven repository to your <span className="key-badge">repositories</span> block.</>,
            <>Declare the Muto dependency using <span className="key-badge">modCompileOnly</span>.</>
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
      ]
    },
    'mod-tiers': {
      title: 'Mod Classification Tiers',
      category: 'Mod Classification',
      sections: [
        {
          title: 'Understanding Clean, Standard, and Stubborn mods',
          steps: [
            <><strong style={{ color: '#22c55e' }}>Clean Mods:</strong> Client-side tweaks, UI helpers, HUDs, and visual mods. They reload instantly without mutating registry topologies.</>,
            <><strong style={{ color: '#f59e0b' }}>Standard Mods:</strong> Mods registering custom blocks, items, or recipes. Handled by Muto through child classloader rotation and registry thawing.</>,
            <><strong style={{ color: '#ef4444' }}>Stubborn / Core Mods:</strong> Foundational modules (Fabric Loader, Fabric API, core mixins). Locked in the root classloader to preserve native JVM memory safety.</>
          ]
        }
      ]
    },
    'file-locks': {
      title: 'Windows File Locking & Shadow Cache',
      category: 'Troubleshooting',
      sections: [
        {
          title: 'Resolving "File in use by another process"',
          desc: 'Windows locks open jar files from being overwritten or deleted. Muto implements an automated shadow-cache architecture to eliminate this issue.',
          steps: [
            <>When dropping large jars into <span className="key-badge">mods/</span>, Windows Explorer briefly locks the file during copy.</>,
            <>Muto uses an exponential retry loop with 120ms backoff when inspecting jar headers.</>,
            <>Jar contents are mapped through sandboxed memory buffers so the operating system lock is safely released.</>,
            <>If a file remains locked by another application (e.g. antivirus), Muto flags it with a retry toast rather than crashing the client.</>
          ]
        }
      ]
    },
    'jbr-setup': {
      title: 'JetBrains Runtime (JBR) & DCEVM Setup',
      category: 'Installation',
      sections: [
        {
          title: 'Enabling advanced class redefinition',
          steps: [
            <>Download and install <span className="key-badge">JetBrains Runtime 25</span> with DCEVM support.</>,
            <>In your Minecraft launcher, edit your installation profile.</>,
            <>Under <span className="key-badge">Java Executable</span>, browse and select the JBR <span className="key-badge">bin/java.exe</span>.</>,
            <>In <span className="key-badge">JVM Arguments</span>, append the flag: <span className="key-badge">-XX:+AllowEnhancedClassRedefinition</span>.</>,
            <>Launch Minecraft. Open Muto's Cloth Config screen to verify that enhanced class redefinition is detected.</>
          ]
        }
      ]
    },
    'api-reference': {
      title: 'Muto Public API Reference',
      category: 'For Developers',
      sections: [
        {
          title: 'Class MutoApi methods',
          steps: [
            <><span className="key-badge">MutoApi.isAvailable()</span> — Returns true if Muto is active in Fabric.</>,
            <><span className="key-badge">MutoApi.isReloading()</span> — Returns true if a reload transaction is currently running.</>,
            <><span className="key-badge">MutoApi.isSafeToReload()</span> — Returns true if client is safely on the Title Screen.</>,
            <><span className="key-badge">MutoApi.reloadAsync()</span> — Dispatches non-blocking hot-reload returning CompletableFuture.</>,
            <><span className="key-badge">MutoApi.getReloadableModIds()</span> — Returns tracked mod IDs eligible for live reloading.</>
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
      ]
    },
    'config-screen': {
      title: 'In-Game Cloth Config Screen',
      category: 'Settings & Profiles',
      sections: [
        {
          title: 'Accessing and configuring Muto',
          steps: [
            <>Open the Minecraft <span className="key-badge">Title Screen</span> or ModMenu.</>,
            <>Click the <span className="key-badge">Muto</span> config button.</>,
            <>In the <span className="key-badge">Loaded Mods</span> tab, review all active jars, versions, and classification tiers.</>,
            <>In the <span className="key-badge">Settings</span> tab, toggle reload confirmation modals, toast alerts, or Modrinth updates. Click the <span className="key-badge">↺</span> button to reset any value to default.</>,
            <>In the <span className="key-badge">Developer Suite</span>, enable heap delta logging or preflight bypass for benchmarking.</>
          ]
        }
      ]
    }
  };

  const featureCategories = [
    {
      category: 'Live Reload Engine',
      items: [
        { id: 'start-reloading', label: 'Start Reloading', icon: RotateCw },
        { id: 'start-reloading', label: 'Title Screen Trigger (↻)', icon: Sliders },
        { id: 'start-reloading', label: 'Safe Execution Guard', icon: ShieldCheck },
      ]
    },
    {
      category: 'Mod Classification',
      items: [
        { id: 'mod-tiers', label: 'Clean Mods (UI / Tweaks)', icon: Zap },
        { id: 'mod-tiers', label: 'Standard Mods (Content)', icon: Cpu },
        { id: 'mod-tiers', label: 'Stubborn / Core Mods', icon: Lock },
      ]
    },
    {
      category: 'Ecosystem & Sync',
      items: [
        { id: 'start-reloading', label: 'ModMenu Sync Hook', icon: Terminal },
        { id: 'start-reloading', label: 'Mojang Native Toasts', icon: Bell },
        { id: 'resourcify-guide', label: 'Modrinth Update Jewels', icon: Download },
      ]
    },
    {
      category: 'Settings & Profiles',
      items: [
        { id: 'config-screen', label: 'Cloth Config UI', icon: Sliders },
        { id: 'config-screen', label: 'Developer Suite Flags', icon: Code2 },
        { id: 'config-screen', label: 'Reload History Log', icon: History },
      ]
    }
  ];

  const guideCategories = [
    {
      category: 'Troubleshooting',
      items: [
        { id: 'file-locks', label: 'Windows File Locks (Shadow Cache)', icon: Wrench },
        { id: 'mod-tiers', label: 'Mixin Reload Boundaries', icon: HelpCircle },
        { id: 'file-locks', label: 'Crash Logs & Rollback Protection', icon: ShieldCheck },
      ]
    },
    {
      category: 'Installation',
      items: [
        { id: 'jbr-setup', label: 'Fabric Loader 26.3 Setup', icon: Download },
        { id: 'jbr-setup', label: 'JetBrains Runtime (JBR)', icon: Cpu },
        { id: 'jbr-setup', label: 'JVM Launch Flags', icon: Terminal },
      ]
    },
    {
      category: 'For Developers / Integrators',
      items: [
        { id: 'resourcify-guide', label: 'Resourcify & Downloader Integration', icon: Layers },
        { id: 'resourcify-guide', label: 'Soft Dependency Pattern', icon: FileCode },
        { id: 'api-reference', label: 'Public MutoApi Reference', icon: Code2 },
        { id: 'api-reference', label: 'MutoEvents Lifecycle Hooks', icon: RotateCw },
      ]
    }
  ];

  const currentArticle = selectedArticle ? articles[selectedArticle] : null;

  return (
    <div className="bg-grid-pattern" style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Top Navigation Bar */}
      <header style={{ borderBottom: '1px solid var(--grid-line)', background: '#050608' }}>
        <div style={{ maxWidth: '1240px', margin: '0 auto', padding: '0.85rem 1.5rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '1rem', flexWrap: 'wrap' }}>
          <div 
            onClick={() => { setSelectedArticle(null); setSearchQuery(''); }}
            style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', cursor: 'pointer' }}
          >
            <img 
              src="/icon.png" 
              alt="Muto Logo" 
              style={{ width: '28px', height: '28px', borderRadius: '5px' }}
            />
            <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.5rem' }}>
              <span style={{ fontWeight: 700, fontSize: '1.15rem', color: '#fff', letterSpacing: '-0.02em' }}>Muto</span>
              <span style={{ color: 'var(--gold-main)', fontSize: '0.82rem', fontWeight: 600 }}>Wiki &amp; Docs</span>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <a
              href="https://github.com/fy2ne/muto"
              target="_blank"
              rel="noopener noreferrer"
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.45rem',
                background: '#121418',
                border: '1px solid #242933',
                color: '#e5e7eb',
                padding: '0.35rem 0.75rem',
                borderRadius: '5px',
                fontSize: '0.82rem',
                textDecoration: 'none'
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
                color: '#050608',
                fontWeight: 600,
                padding: '0.35rem 0.85rem',
                borderRadius: '5px',
                fontSize: '0.82rem',
                textDecoration: 'none'
              }}
            >
              <Download size={14} />
              <span>Modrinth</span>
            </a>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main style={{ flex: 1, maxWidth: '1240px', width: '100%', margin: '0 auto', padding: '1.75rem 1.5rem 5rem' }}>
        {/* Top Banner Notice and Search Bar (matching Image 1) */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr auto', gap: '1rem', alignItems: 'center', marginBottom: '2.5rem' }}>
          <div className="top-notice-banner">
            <Info size={16} color="var(--gold-main)" style={{ flexShrink: 0 }} />
            <span>Welcome to the all-in-one location for help with Muto Mod for Minecraft Java Edition.</span>
          </div>
          <div style={{ minWidth: '260px' }}>
            <input
              type="text"
              placeholder="Search wiki..."
              className="search-input"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{ width: '100%' }}
            />
          </div>
        </div>

        {/* View Mode: Article Reader (Image 2 style) */}
        {currentArticle ? (
          <div className="article-container">
            <button
              onClick={() => setSelectedArticle(null)}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.4rem',
                background: 'transparent',
                border: 'none',
                color: 'var(--gold-main)',
                fontSize: '0.86rem',
                fontWeight: 500,
                cursor: 'pointer',
                marginBottom: '1.5rem',
                padding: 0
              }}
            >
              <ArrowLeft size={14} />
              <span>Back to Wiki Hub</span>
            </button>

            <div style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--gold-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '0.25rem' }}>
              {currentArticle.category}
            </div>
            <h1 style={{ color: '#fff', fontSize: '1.85rem', fontWeight: 700, letterSpacing: '-0.02em', marginBottom: '1.75rem' }}>
              {currentArticle.title}
            </h1>

            {currentArticle.sections.map((section, idx) => (
              <div key={idx} className="article-section">
                <div className="article-title">{section.title}</div>
                {section.desc && <p style={{ color: '#9ca3af', marginBottom: '1rem', fontSize: '0.94rem' }}>{section.desc}</p>}
                
                <ol className="step-list">
                  {section.steps.map((step, sIdx) => (
                    <li key={sIdx} className="step-item">
                      <span>{step}</span>
                    </li>
                  ))}
                </ol>

                {section.codeSnippet && (
                  <div style={{ marginTop: '1.25rem' }}>
                    <CodeBox
                      filename={section.codeSnippet.filename}
                      code={section.codeSnippet.code}
                    />
                  </div>
                )}
              </div>
            ))}
          </div>
        ) : (
          /* View Mode: Wiki Hub Directory (Image 1 layout, Golden Yellow) */
          <div>
            {/* Features Section (Golden Yellow) */}
            <div style={{ marginBottom: '3.5rem' }}>
              <div className="section-title-gold">Features</div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '2.5rem 2rem' }}>
                {featureCategories.map((cat, idx) => (
                  <div key={idx}>
                    <div className="category-title">{cat.category}</div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.45rem' }}>
                      {cat.items
                        .filter(item => !searchQuery || item.label.toLowerCase().includes(searchQuery.toLowerCase()))
                        .map((item, itemIdx) => {
                          const Icon = item.icon;
                          return (
                            <div
                              key={itemIdx}
                              className="wiki-link"
                              onClick={() => setSelectedArticle(item.id)}
                            >
                              <Icon size={15} />
                              <span>{item.label}</span>
                            </div>
                          );
                        })}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Guides Section (Warm Amber/Gold) */}
            <div style={{ borderTop: '1px solid var(--grid-line)', paddingTop: '3rem' }}>
              <div className="section-title-gold" style={{ color: '#f59e0b' }}>Guides</div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '2.5rem 2rem' }}>
                {guideCategories.map((cat, idx) => (
                  <div key={idx}>
                    <div className="category-title">{cat.category}</div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.45rem' }}>
                      {cat.items
                        .filter(item => !searchQuery || item.label.toLowerCase().includes(searchQuery.toLowerCase()))
                        .map((item, itemIdx) => {
                          const Icon = item.icon;
                          return (
                            <div
                              key={itemIdx}
                              className="wiki-link"
                              onClick={() => setSelectedArticle(item.id)}
                            >
                              <Icon size={15} />
                              <span>{item.label}</span>
                            </div>
                          );
                        })}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Footer */}
      <footer style={{ borderTop: '1px solid var(--grid-line)', padding: '1.75rem 1.5rem', background: '#050608' }}>
        <div style={{ maxWidth: '1240px', margin: '0 auto', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '1rem', fontSize: '0.82rem', color: '#6b7280' }}>
          <div>
            Muto Mod for Minecraft Java Edition. Created by <a href="https://github.com/fy2ne" target="_blank" rel="noopener noreferrer" style={{ color: 'var(--gold-main)', textDecoration: 'none' }}>Anas Khezaz (fy2ne)</a>.
          </div>
          <div style={{ display: 'flex', gap: '1.25rem' }}>
            <a href="https://github.com/fy2ne/muto" target="_blank" rel="noopener noreferrer" style={{ color: '#9ca3af', textDecoration: 'none' }}>GitHub</a>
            <a href="https://modrinth.com/project/muto" target="_blank" rel="noopener noreferrer" style={{ color: '#9ca3af', textDecoration: 'none' }}>Modrinth</a>
            <a href="https://fy2ne.me" target="_blank" rel="noopener noreferrer" style={{ color: '#9ca3af', textDecoration: 'none' }}>fy2ne.me</a>
          </div>
        </div>
      </footer>
    </div>
  );
}
