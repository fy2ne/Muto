import React, { useState, useMemo } from 'react';
import { 
  RotateCw, 
  BookOpen, 
  Cpu, 
  Layers, 
  ShieldCheck, 
  Terminal, 
  Sliders, 
  Code2, 
  HelpCircle, 
  ExternalLink, 
  Check, 
  Copy, 
  Search,
  Sparkles,
  ArrowRight,
  Server,
  Download
} from 'lucide-react';

function GithubIcon({ size = 15, style = {} }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={style}>
      <path d="M15 22v-4a4.8 4.8 0 0 0-1-3.5c3 0 6-2 6-5.5.08-1.25-.27-2.48-1-3.5.28-1.15.28-2.35 0-3.5 0 0-1 0-3 1.5-2.64-.5-5.36-.5-8 0C6 2 5 2 5 2c-.3 1.15-.3 2.35 0 3.5A5.403 5.403 0 0 0 4 9c0 3.5 3 5.5 6 5.5-.39.49-.68 1.05-.85 1.65-.17.6-.22 1.23-.15 1.85v4" />
      <path d="M9 18c-4.51 2-5-2-7-2" />
    </svg>
  );
}

function CodeBlock({ code, language = "java", filename }) {
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
          className="btn-secondary"
          style={{ padding: '0.2rem 0.6rem', fontSize: '0.75rem', height: '24px' }}
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
  const [activeSection, setActiveSection] = useState('overview');
  const [searchQuery, setSearchQuery] = useState('');
  const [gradleTab, setGradleTab] = useState('groovy');

  const navItems = [
    { id: 'overview', title: 'Overview & Philosophy', icon: BookOpen },
    { id: 'getting-started', title: 'Installation & Setup', icon: Download },
    { id: 'pipeline', title: 'The Hot-Swap Engine', icon: Cpu },
    { id: 'resourcify-guide', title: 'External Mod Integration', icon: Layers, highlight: true },
    { id: 'api-reference', title: 'Public API Reference', icon: Code2 },
    { id: 'config-screen', title: 'In-Game UI & Controls', icon: Sliders },
    { id: 'faq', title: 'Technical FAQ & JBR', icon: HelpCircle },
  ];

  const filteredNav = useMemo(() => {
    if (!searchQuery.trim()) return navItems;
    const q = searchQuery.toLowerCase();
    return navItems.filter(item => item.title.toLowerCase().includes(q));
  }, [searchQuery]);

  return (
    <div className="wiki-layout">
      {/* Sidebar */}
      <aside className="wiki-sidebar">
        <div style={{ padding: '1.5rem 1.25rem 1rem', borderBottom: '1px solid var(--border-subtle)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.75rem' }}>
            <img 
              src="/icon.png" 
              alt="Muto Logo" 
              style={{ width: '32px', height: '32px', borderRadius: '6px' }}
            />
            <div>
              <div style={{ fontWeight: 700, fontSize: '1.1rem', color: '#fff', letterSpacing: '-0.02em', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                Muto
                <span className="badge-gold" style={{ fontSize: '0.65rem', padding: '0.1rem 0.45rem' }}>0.1.0-beta.1</span>
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Fabric Mod Reloader</div>
            </div>
          </div>

          {/* Search box */}
          <div style={{ position: 'relative', marginTop: '0.75rem' }}>
            <Search size={14} style={{ position: 'absolute', left: '10px', top: '9px', color: 'var(--text-muted)' }} />
            <input
              type="text"
              placeholder="Search docs..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{
                width: '100%',
                background: '#0e0f11',
                border: '1px solid var(--border-subtle)',
                borderRadius: '6px',
                padding: '0.4rem 0.6rem 0.4rem 2rem',
                fontSize: '0.82rem',
                color: '#fff',
                outline: 'none'
              }}
            />
          </div>
        </div>

        {/* Navigation links */}
        <nav style={{ padding: '0.85rem 0.75rem', flex: 1 }}>
          <div style={{ fontSize: '0.7rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', padding: '0.4rem 0.6rem 0.6rem' }}>
            Documentation
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
            {filteredNav.map((item) => {
              const Icon = item.icon;
              const isActive = activeSection === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => setActiveSection(item.id)}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.65rem',
                    width: '100%',
                    padding: '0.5rem 0.65rem',
                    borderRadius: '6px',
                    fontSize: '0.86rem',
                    fontWeight: isActive ? 600 : 500,
                    color: isActive ? '#fff' : (item.highlight ? '#f59e0b' : 'var(--text-secondary)'),
                    background: isActive ? 'rgba(245, 158, 11, 0.12)' : 'transparent',
                    border: isActive ? '1px solid rgba(245, 158, 11, 0.25)' : '1px solid transparent',
                    textAlign: 'left',
                    cursor: 'pointer',
                    transition: 'all 0.12s ease'
                  }}
                >
                  <Icon size={15} color={isActive ? '#f59e0b' : (item.highlight ? '#f59e0b' : 'currentColor')} />
                  <span>{item.title}</span>
                </button>
              );
            })}
          </div>
        </nav>

        {/* External Links */}
        <div style={{ padding: '1rem 1.25rem', borderTop: '1px solid var(--border-subtle)', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          <a
            href="https://github.com/fy2ne/muto"
            target="_blank"
            rel="noopener noreferrer"
            className="btn-secondary"
            style={{ fontSize: '0.8rem', justifyContent: 'center' }}
          >
            <GithubIcon size={14} />
            <span>GitHub Repository</span>
            <ExternalLink size={12} style={{ marginLeft: 'auto', opacity: 0.5 }} />
          </a>
          <a
            href="https://modrinth.com/project/muto"
            target="_blank"
            rel="noopener noreferrer"
            className="btn-secondary"
            style={{ fontSize: '0.8rem', justifyContent: 'center' }}
          >
            <Download size={14} />
            <span>Download on Modrinth</span>
            <ExternalLink size={12} style={{ marginLeft: 'auto', opacity: 0.5 }} />
          </a>
        </div>
      </aside>

      {/* Main Content */}
      <main className="wiki-content">
        {/* Top Header Actions */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
          <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
            <span className="badge-green">Minecraft 26.3 Compatible</span>
            <span className="badge-gold">Java 25+ Required</span>
          </div>
          <div style={{ display: 'flex', gap: '0.6rem' }}>
            <a
              href="https://github.com/fy2ne/muto"
              target="_blank"
              rel="noopener noreferrer"
              className="btn-secondary"
            >
              <GithubIcon size={15} />
              <span>Star on GitHub</span>
            </a>
            <a
              href="https://modrinth.com/project/muto"
              target="_blank"
              rel="noopener noreferrer"
              className="btn-primary"
            >
              <Download size={15} />
              <span>Get Mod Jar</span>
            </a>
          </div>
        </div>

        {/* Section: Overview */}
        {activeSection === 'overview' && (
          <div>
            <h1>Muto Architecture & Overview</h1>
            <p style={{ fontSize: '1.05rem', color: '#d1d5db' }}>
              <strong>Muto</strong> is a dynamic runtime mod reloader and hot-swap pipeline for Fabric. It allows players and developers to add, update, or remove mod jars in <code className="inline-code">.minecraft/mods/</code> and apply those changes live from the title screen without terminating the JVM or relaunching the game.
            </p>

            <div className="callout">
              <strong>Zero-Restart Iteration:</strong> Instead of waiting 45 to 90 seconds for Minecraft to reboot every time you tweak a mod or drop in a new utility jar, Muto executes the reload pass in under 500 milliseconds.
            </div>

            <h2>How It Works</h2>
            <p>
              Traditional Minecraft modding treats mod discovery and initialization as a strictly immutable startup event. Once Fabric’s root classloader initializes, jar files are held open and entrypoints cannot be re-executed.
            </p>
            <p>
              Muto breaks this limitation through four interlocking architectural components:
            </p>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '1rem', margin: '1.5rem 0' }}>
              <div className="card">
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#f59e0b', fontWeight: 600, marginBottom: '0.5rem' }}>
                  <Cpu size={16} />
                  <span>1. Child ClassLoaders</span>
                </div>
                <div style={{ fontSize: '0.88rem', color: 'var(--text-secondary)' }}>
                  Reloadable mods are mounted into isolated <code className="inline-code">MutoClassLoader</code> instances that can be dereferenced and garbage-collected upon swap.
                </div>
              </div>

              <div className="card">
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#f59e0b', fontWeight: 600, marginBottom: '0.5rem' }}>
                  <Layers size={16} />
                  <span>2. Registry Thawing</span>
                </div>
                <div style={{ fontSize: '0.88rem', color: 'var(--text-secondary)' }}>
                  Temporarily unfreezes Minecraft’s <code className="inline-code">MappedRegistry</code> during reload, permitting new blocks, items, and identifiers to register safely.
                </div>
              </div>

              <div className="card">
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#f59e0b', fontWeight: 600, marginBottom: '0.5rem' }}>
                  <ShieldCheck size={16} />
                  <span>3. Atomic Rollback</span>
                </div>
                <div style={{ fontSize: '0.88rem', color: 'var(--text-secondary)' }}>
                  If a newly added or updated mod throws an exception during initialization, Muto halts the swap and safely rolls back to the prior stable mod snapshot.
                </div>
              </div>

              <div className="card">
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#f59e0b', fontWeight: 600, marginBottom: '0.5rem' }}>
                  <RotateCw size={16} />
                  <span>4. JBR & DCEVM Hooks</span>
                </div>
                <div style={{ fontSize: '0.88rem', color: 'var(--text-secondary)' }}>
                  Leverages enhanced bytecode redefinition on the JetBrains Runtime when present to hot-swap existing loaded classes in place.
                </div>
              </div>
            </div>

            <h2>Mod Classification Tiers</h2>
            <p>
              To protect JVM stability, Muto classifies all detected jars into three operational tiers:
            </p>
            <table className="wiki-table">
              <thead>
                <tr>
                  <th>Tier</th>
                  <th>Examples</th>
                  <th>Behavior</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td><strong style={{ color: '#22c55e' }}>Clean</strong></td>
                  <td>AppleSkin, FPS Reducer, FullBright, Transition</td>
                  <td>Client-side tweaks & UI mods. Instant hot-swapping without touching world registries.</td>
                </tr>
                <tr>
                  <td><strong style={{ color: '#f59e0b' }}>Standard</strong></td>
                  <td>Distant Horizons, Structory, Waystones</td>
                  <td>Content mods adding items/blocks. Managed through child classloader rotation and registry thawing.</td>
                </tr>
                <tr>
                  <td><strong style={{ color: '#ef4444' }}>Stubborn / Core</strong></td>
                  <td>Fabric Loader, Fabric API, Minecraft Engine</td>
                  <td>Locked in the root loader. Protected from redefinition to prevent native linkage corruption.</td>
                </tr>
              </tbody>
            </table>
          </div>
        )}

        {/* Section: Getting Started */}
        {activeSection === 'getting-started' && (
          <div>
            <h1>Installation & Setup</h1>
            <p>Getting started with Muto takes under two minutes. No configuration files required by default.</p>

            <h2>System Requirements</h2>
            <table className="wiki-table">
              <thead>
                <tr>
                  <th>Component</th>
                  <th>Required Version</th>
                  <th>Notes</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Minecraft</td>
                  <td><code className="inline-code">26.3</code></td>
                  <td>Java Edition</td>
                </tr>
                <tr>
                  <td>Fabric Loader</td>
                  <td><code className="inline-code">≥ 0.16.0</code></td>
                  <td>Official Fabric runtime</td>
                </tr>
                <tr>
                  <td>Fabric API</td>
                  <td><code className="inline-code">0.161.0+26.3</code></td>
                  <td>Standard API module</td>
                </tr>
                <tr>
                  <td>Java Runtime</td>
                  <td><code className="inline-code">Java 25+</code></td>
                  <td>Recommended: JetBrains Runtime (JBR)</td>
                </tr>
              </tbody>
            </table>

            <h2>Installation Steps</h2>
            <ol style={{ paddingLeft: '1.25rem', color: 'var(--text-secondary)', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <li>
                Download the latest <strong style={{ color: '#fff' }}>muto-0.1.0-beta.1.jar</strong> from <a href="https://modrinth.com/project/muto" target="_blank" rel="noopener noreferrer" style={{ color: '#f59e0b' }}>Modrinth</a>.
              </li>
              <li>
                Place the jar into your <code className="inline-code">.minecraft/mods/</code> directory along with Fabric API.
              </li>
              <li>
                Launch Minecraft. You will see an official circular reload button (<strong style={{ color: '#fff' }}>↻</strong>) on your main title screen.
              </li>
            </ol>

            <div className="callout" style={{ marginTop: '1.5rem' }}>
              <strong>Optional JVM Flag for Enhanced Swapping:</strong> Add <code className="inline-code">-XX:+AllowEnhancedClassRedefinition</code> to your launcher's JVM arguments if running on JetBrains Runtime. Muto detects this flag automatically and reports it in its diagnostics screen.
            </div>
          </div>
        )}

        {/* Section: Pipeline */}
        {activeSection === 'pipeline' && (
          <div>
            <h1>The Hot-Swap Engine</h1>
            <p>
              When the user clicks the title screen reload button or an external mod invokes <code className="inline-code">MutoApi.reloadAsync()</code>, the engine executes a strict 5-stage transactional pipeline:
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', margin: '1.5rem 0' }}>
              <div className="card" style={{ borderLeft: '3px solid #f59e0b' }}>
                <strong style={{ color: '#fff' }}>Stage 1: Preflight & Diff Calculation</strong>
                <p style={{ margin: '0.4rem 0 0', fontSize: '0.88rem' }}>
                  Scans <code className="inline-code">/mods</code> using robust file-lock backoff and Gson parsing. Compares the disk state against the active <code className="inline-code">ModSnapshot</code> to compute <code className="inline-code">ModDiff</code> (added, removed, updated). If no changes exist, returns early as a zero-latency no-op.
                </p>
              </div>

              <div className="card" style={{ borderLeft: '3px solid #f59e0b' }}>
                <strong style={{ color: '#fff' }}>Stage 2: Event Notification & Cleanup</strong>
                <p style={{ margin: '0.4rem 0 0', fontSize: '0.88rem' }}>
                  Fires <code className="inline-code">MutoEvents.RELOAD_START</code>. Other mods subscribing to this hook tear down static caches, cancel scheduled network tasks, and release references to target classes.
                </p>
              </div>

              <div className="card" style={{ borderLeft: '3px solid #f59e0b' }}>
                <strong style={{ color: '#fff' }}>Stage 3: Child ClassLoader Rotation</strong>
                <p style={{ margin: '0.4rem 0 0', fontSize: '0.88rem' }}>
                  The existing child <code className="inline-code">MutoClassLoader</code> is detached and closed. A fresh instance is created, pointing to the updated set of jar URLs. Removed jars are fully unmounted.
                </p>
              </div>

              <div className="card" style={{ borderLeft: '3px solid #f59e0b' }}>
                <strong style={{ color: '#fff' }}>Stage 4: Registry Thawing & Entrypoint Execution</strong>
                <p style={{ margin: '0.4rem 0 0', fontSize: '0.88rem' }}>
                  Thaws <code className="inline-code">BuiltInRegistries</code>. Instantiates entrypoints for added and updated mods (<code className="inline-code">ModInitializer</code>, <code className="inline-code">ClientModInitializer</code>), records new instances, and immediately refreezes registries.
                </p>
              </div>

              <div className="card" style={{ borderLeft: '3px solid #22c55e' }}>
                <strong style={{ color: '#fff' }}>Stage 5: Asset Refresh & Toast Dispatch</strong>
                <p style={{ margin: '0.4rem 0 0', fontSize: '0.88rem' }}>
                  Fires <code className="inline-code">MutoEvents.RELOAD_FINISH</code>. Flushes the client's texture cache and resource managers, updates ModMenu's cache, and renders a native Mojang <code className="inline-code">SystemToast</code> indicating the mods added and duration.
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Section: Resourcify Guide */}
        {activeSection === 'resourcify-guide' && (
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
              <span className="badge-gold">Partner Integration Specification</span>
            </div>
            <h1>External Mod Integration Guide</h1>
            <p style={{ fontSize: '1.05rem', color: '#d1d5db' }}>
              This guide is written for mod managers, in-game downloaders (such as <strong>Resourcify</strong>), and dev tools that want to provide instant, restartless mod installation.
            </p>

            <h2>The Soft-Dependency Philosophy</h2>
            <p>
              You do <strong>not</strong> need to make Muto a required dependency for your mod. Using Fabric’s soft-dependency pattern, your mod functions normally for users without Muto (prompting them to restart Minecraft), while automatically unlocking <strong>instant hot-reloading</strong> when Muto is installed.
            </p>

            <h2>1. Add Gradle Dependency</h2>
            <p>Add the Muto API as a <code className="inline-code">compileOnly</code> or <code className="inline-code">modCompileOnly</code> dependency in your build script:</p>

            <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.5rem' }}>
              <button
                className={gradleTab === 'groovy' ? 'btn-primary' : 'btn-secondary'}
                onClick={() => setGradleTab('groovy')}
                style={{ padding: '0.25rem 0.75rem', fontSize: '0.8rem' }}
              >
                build.gradle (Groovy)
              </button>
              <button
                className={gradleTab === 'kotlin' ? 'btn-primary' : 'btn-secondary'}
                onClick={() => setGradleTab('kotlin')}
                style={{ padding: '0.25rem 0.75rem', fontSize: '0.8rem' }}
              >
                build.gradle.kts (Kotlin)
              </button>
            </div>

            {gradleTab === 'groovy' ? (
              <CodeBlock
                language="groovy"
                filename="build.gradle"
                code={`repositories {
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
    }
}

dependencies {
    // Soft compile-only dependency on Muto API
    modCompileOnly "maven.modrinth:muto:0.1.0-beta.1"
}`}
              />
            ) : (
              <CodeBlock
                language="kotlin"
                filename="build.gradle.kts"
                code={`repositories {
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
    }
}

dependencies {
    // Soft compile-only dependency on Muto API
    modCompileOnly("maven.modrinth:muto:0.1.0-beta.1")
}`}
              />
            )}

            <h2>2. Declare Soft Suggestion in <code className="inline-code">fabric.mod.json</code></h2>
            <p>Add Muto under <code className="inline-code">suggests</code> so users know live reloading is available:</p>

            <CodeBlock
              language="json"
              filename="src/main/resources/fabric.mod.json"
              code={`"suggests": {
    "muto": ">=0.1.0-beta.1"
}`}
            />

            <h2>3. Runtime Detection & Reload Trigger</h2>
            <p>
              When your downloader finishes saving a <code className="inline-code">.jar</code> file into <code className="inline-code">.minecraft/mods/</code>, check if Muto is present and invoke the reload pipeline:
            </p>

            <CodeBlock
              language="java"
              filename="DownloaderIntegration.java"
              code={`package com.example.mod;

import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Path;

public class ModInstaller {

    public static void onDownloadComplete(Path downloadedJar) {
        // 1. Check if Muto is installed
        if (FabricLoader.getInstance().isModLoaded("muto")) {
            applyWithMuto();
        } else {
            // Fallback for players without Muto
            notifyPlayer("Mod downloaded. Restart Minecraft to apply changes.");
        }
    }

    private static void applyWithMuto() {
        // Safe check: verify player is on the Title Screen
        if (!me.fy2ne.muto.api.MutoApi.isSafeToReload()) {
            notifyPlayer("Mod installed! Return to Title Screen to apply without restart.");
            return;
        }

        // Trigger asynchronous hot-reload
        notifyPlayer("Hot-reloading mod...");
        me.fy2ne.muto.api.MutoApi.reloadAsync().thenAccept(result -> {
            if (result.success()) {
                notifyPlayer("Mod active! Reloaded in " + result.durationMs() + "ms");
            } else {
                notifyPlayer("Reload encountered an error: " + result.error());
            }
        });
    }

    private static void notifyPlayer(String msg) {
        // Your custom toast or in-game message dispatch
        System.out.println("[Installer] " + msg);
    }
}`}
            />

            <div className="callout callout-info" style={{ marginTop: '1.5rem' }}>
              <strong>Safety Guaranteed:</strong> <code className="inline-code">MutoApi.isSafeToReload()</code> returns <code className="inline-code">true</code> only when the client is at the main title screen and not connected to an active singleplayer world or multiplayer server.
            </div>
          </div>
        )}

        {/* Section: API Reference */}
        {activeSection === 'api-reference' && (
          <div>
            <h1>Public API Reference</h1>
            <p>Muto exposes a clean, minimal public API surface located in package <code className="inline-code">me.fy2ne.muto.api</code>.</p>

            <h2>Class <code className="inline-code">MutoApi</code></h2>
            <CodeBlock
              language="java"
              filename="me.fy2ne.muto.api.MutoApi"
              code={`public final class MutoApi {
    /** True if Muto is installed and active in the Fabric environment. */
    public static boolean isAvailable();

    /** True if a reload pipeline execution is currently in flight. */
    public static boolean isReloading();

    /** True if client is on Title Screen (not inside a live world). */
    public static boolean isSafeToReload();

    /** Rescans /mods and executes the hot-reload asynchronously. */
    public static CompletableFuture<ReloadResult> reloadAsync();

    /** Set of mod IDs currently tracked as dynamically reloadable. */
    public static Set<String> getReloadableModIds();

    /** Set of core/stubborn mod IDs locked in the root classloader. */
    public static Set<String> getStubbornModIds();
}`}
            />

            <h2>Class <code className="inline-code">MutoEvents</code></h2>
            <p>Fabric event hooks to observe reload lifecycle transitions:</p>
            <CodeBlock
              language="java"
              filename="me.fy2ne.muto.api.MutoEvents"
              code={`// Fired immediately before reload pipeline execution begins
MutoEvents.RELOAD_START.register((startEpochMs, expectedDiff) -> {
    System.out.println("Reload started. Mod additions: " + expectedDiff.added());
});

// Fired when reload completes (or rolls back upon error)
MutoEvents.RELOAD_FINISH.register((endEpochMs, diff, success, durationMs, error) -> {
    if (success) {
        System.out.println("Reload succeeded in " + durationMs + "ms");
    } else {
        System.err.println("Reload rolled back due to error: " + error.getMessage());
    }
});`}
            />

            <h2>Record <code className="inline-code">ModDiff</code></h2>
            <CodeBlock
              language="java"
              filename="me.fy2ne.muto.api.ModDiff"
              code={`public record ModDiff(
    Set<String> added,
    Set<String> removed,
    Set<String> updated,
    Set<String> unchanged
) {
    public boolean hasChanges() {
        return !added.isEmpty() || !removed.isEmpty() || !updated.isEmpty();
    }
}`}
            />
          </div>
        )}

        {/* Section: Config Screen */}
        {activeSection === 'config-screen' && (
          <div>
            <h1>In-Game UI & Controls</h1>
            <p>
              Muto provides official Mojang-style title screen integration and a full Cloth Config-styled management UI.
            </p>

            <h2>Title Screen Quick Button</h2>
            <p>
              Renders a 20x20 button featuring the official <strong>↻</strong> reload icon alongside Minecraft’s utility buttons.
            </p>
            <ul style={{ paddingLeft: '1.25rem', color: 'var(--text-secondary)', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <li>
                <strong>One-Click Reload:</strong> Clicking the button runs preflight checks and launches the reload sequence.
              </li>
              <li>
                <strong>Modrinth Notification Jewel:</strong> When an update to Muto is published on Modrinth, an emerald green jewel badge automatically renders on the top-right of the button.
              </li>
            </ul>

            <h2>Cloth Config Management Screen</h2>
            <p>Access the config screen through ModMenu or by binding a shortcut:</p>
            <ul style={{ paddingLeft: '1.25rem', color: 'var(--text-secondary)', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <li>
                <strong>Loaded Mods:</strong> Live table of all detected jars with mod IDs, versions, and classification tiers (<span style={{ color: '#22c55e' }}>Clean</span>, <span style={{ color: '#f59e0b' }}>Standard</span>, <span style={{ color: '#ef4444' }}>Stubborn</span>).
              </li>
              <li>
                <strong>General Settings:</strong> Toggles for reload confirmation modal, toast notifications, resource auto-reloading, and Modrinth update checks. Each entry includes a Cloth Config <code className="inline-code">↺</code> reset button.
              </li>
              <li>
                <strong>Developer Suite:</strong> Advanced flags for classloader tracing, heap delta metrics, preflight bypass, and simulate update previews.
              </li>
              <li>
                <strong>Reload History:</strong> Step-by-step diagnostic log recording every past reload pass and millisecond breakdown.
              </li>
            </ul>
          </div>
        )}

        {/* Section: FAQ */}
        {activeSection === 'faq' && (
          <div>
            <h1>Technical FAQ & Troubleshooting</h1>

            <h3>Why are in-world reloads disabled?</h3>
            <p>
              Minecraft worlds maintain deep, stateful in-memory references to entity types, block state containers, and dimension managers. Reloading content-heavy mods while connected to a world causes desynchronization and potential world corruption. Exiting to the Title Screen guarantees that world states are cleanly unmounted before the reload pass begins.
            </p>

            <h3>How does Muto handle Windows file locking?</h3>
            <p>
              On Windows, opening a <code className="inline-code">.jar</code> file directly locks it from deletion. Muto incorporates an automated shadow-cache pipeline: during preflight checks, candidate jars are read into sandboxed memory buffers with retry loops, preventing Windows Explorer from displaying "File in use by another process".
            </p>

            <h3>Can mixins be hot-reloaded?</h3>
            <p>
              Fabric Mixins mutate bytecode at class-loading time. While client tweaks and registry additions reload seamlessly, structural mixins injected into Minecraft's root classes require the game process to relaunch or require the JetBrains Runtime with <code className="inline-code">-XX:+AllowEnhancedClassRedefinition</code>.
            </p>

            <h3>How do I enable JetBrains Runtime (JBR)?</h3>
            <p>
              Install the <a href="https://github.com/JetBrains/JetBrainsRuntime" target="_blank" rel="noopener noreferrer" style={{ color: '#f59e0b' }}>JetBrains Runtime 25</a> and set your launcher's Java Executable path to point to JBR's <code className="inline-code">bin/java.exe</code>. In your launcher's JVM arguments, append:
            </p>
            <CodeBlock
              language="bash"
              code="-XX:+AllowEnhancedClassRedefinition -XX:+UnlockDiagnosticVMOptions"
            />
          </div>
        )}

        {/* Footer */}
        <div style={{ marginTop: '4rem', paddingTop: '1.5rem', borderTop: '1px solid var(--border-subtle)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', fontSize: '0.82rem', color: 'var(--text-muted)' }}>
          <div>
            Created by <a href="https://github.com/fy2ne" target="_blank" rel="noopener noreferrer" style={{ color: '#f59e0b', textDecoration: 'none' }}>Anas Khezaz (fy2ne)</a>. Licensed under Apache 2.0.
          </div>
          <div>
            <a href="https://github.com/fy2ne/muto" target="_blank" rel="noopener noreferrer" style={{ color: 'var(--text-secondary)', textDecoration: 'none' }}>GitHub</a> · <a href="https://modrinth.com/project/muto" target="_blank" rel="noopener noreferrer" style={{ color: 'var(--text-secondary)', textDecoration: 'none' }}>Modrinth</a> · <a href="https://fy2ne.me" target="_blank" rel="noopener noreferrer" style={{ color: 'var(--text-secondary)', textDecoration: 'none' }}>fy2ne.me</a>
          </div>
        </div>
      </main>
    </div>
  );
}
