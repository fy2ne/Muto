import { useEffect, useMemo, useRef, useState } from "react";
import katex from "katex";
import {
  ArrowLeft,
  ArrowRight,
  BookOpen,
  Boxes,
  Check,
  ChevronDown,
  ChevronRight,
  Code2,
  Copy,
  Cpu,
  Download,
  ExternalLink,
  Layers,
  Lock,
  Menu as MenuIcon,
  MessageSquare,
  RotateCw,
  Search,
  Send,
  Settings,
  ShieldCheck,
  Sliders,
  Terminal,
  Wrench,
  X,
  Zap,
} from "lucide-react";

const RUNTIME_REPO = "https://github.com/fy2ne/Muto-Runtime";
const MENU_REPO = "https://github.com/fy2ne/Muto-Menu";
const VERSION = "0.3.0-beta";

function CodeSnippet({ filename, code }) {
  const [copied, setCopied] = useState(false);

  const copy = () => {
    navigator.clipboard?.writeText(code).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 1400);
    });
  };

  return (
    <div className="codeblock">
      <div className="codeblock-head">
        <span className="codeblock-filename">{filename}</span>
        <button className="codeblock-copy" onClick={copy} type="button">
          {copied ? <Check size={12} /> : <Copy size={12} />}
          {copied ? "Copied" : "Copy"}
        </button>
      </div>
      <pre>
        <code>{code}</code>
      </pre>
    </div>
  );
}

function DocAlert({ kind = "note", label, children }) {
  const display = label || kind.toUpperCase();
  return (
    <div className={`doc-alert alert-${kind}`}>
      <div className="doc-alert-label">{display}</div>
      <div className="doc-alert-body">{children}</div>
    </div>
  );
}

function Math({ tex, block = false }) {
  const html = useMemo(
    () => katex.renderToString(tex, { throwOnError: false, displayMode: block }),
    [tex, block]
  );
  return (
    <span
      className={block ? "math-block" : "math-inline"}
      dangerouslySetInnerHTML={{ __html: html }}
    />
  );
}

const RUNTIME_NAV = [
  {
    title: "Getting Started",
    items: [
      { id: "overview", title: "Architecture Overview", icon: BookOpen, short: "Runtime hot-swap pipeline, Knot injection, and engine boundaries." },
      { id: "install", title: "Dependency & Installation", icon: Layers, short: "Maven coordinates, version targets, and soft-dependency guard." },
      { id: "pipeline", title: "Reload Pipeline Guide", icon: RotateCw, short: "Trigger, inspect, and trace a reload transaction." },
      { id: "safe-guard", title: "Safe Execution Boundary", icon: ShieldCheck, short: "When reloads are permitted and how threads are dispatched." },
    ],
  },
  {
    title: "Core Concepts",
    items: [
      { id: "mod-tiers", title: "Mod Classification Tiers", icon: Cpu, short: "Tier 1, 2, and 3 classification rules." },
      { id: "api-reference", title: "Public API Reference", icon: Code2, short: "Methods and records in me.fy2ne.muto.api." },
      { id: "events", title: "Lifecycle Events", icon: Zap, short: "RELOAD_START and RELOAD_FINISH hooks." },
      { id: "testing", title: "Testing & CI", icon: Sliders, short: "JUnit 5 suite and GitHub Actions workflows." },
      { id: "source", title: "Repository & Packages", icon: Terminal, short: "Package layout, Gradle tasks, and LGPL-3.0 terms." },
    ],
  },
  {
    title: "Developer Integration",
    items: [
      { id: "fast-refresh", title: "Fast Refresh Dev Loop", icon: Zap, short: "mutoWatch task and muto.trigger polling." },
      { id: "mod-state", title: "Mod State Management", icon: Boxes, short: "Staging enable/disable changes across reloads." },
      { id: "config", title: "Configuration (muto.json)", icon: Settings, short: "Runtime config flags in config/muto.json." },
      { id: "file-locks", title: "Windows File Locking", icon: Wrench, short: "Working around NTFS open-handle locks on mod jars." },
      { id: "jvm-tuning", title: "JVM & JetBrains Runtime", icon: Terminal, short: "Metaspace unloading and heap sizing for repeated swaps." },
    ],
  },
  {
    title: "Internals",
    items: [
      { id: "arch-rotation", title: "Knot Integration & Reload", icon: RotateCw, short: "Appending jars to Knot and registering late mixin configs." },
      { id: "registry-thawing", title: "Registry Thawing Mechanics", icon: Lock, short: "unfreezeAll, pruneNamespace, and freezeAll." },
      { id: "atomic-rollback", title: "Atomic Rollback Guarantee", icon: ShieldCheck, short: "Failure recovery, transaction rollback, and history." },
    ],
  },
];

const MENU_NAV = [
  {
    title: "Menu Features",
    items: [
      { id: "menu-overview", title: "Muto Menu Overview", icon: BookOpen, short: "In-game mods screen, hotkeys, and interactive controls." },
      { id: "menu-toggles", title: "Live Mod Toggling", icon: Sliders, short: "Enabling and disabling mods live with dependency safety warnings." },
      { id: "menu-watcher", title: "Folder Auto-Watcher", icon: RotateCw, short: "Watching the mods folder for jar drops and instant reload." },
      { id: "menu-rejoin", title: "In-World Reload & Rejoin", icon: Zap, short: "Hot-swapping inside multiplayer servers or singleplayer worlds." },
      { id: "menu-history", title: "Reload History Viewer", icon: Layers, short: "Auditing past reload durations, diffs, and execution logs." },
      { id: "menu-config", title: "Menu Settings (muto-menu.json)", icon: Settings, short: "Customizing hotkeys, auto-watch preferences, and warnings." },
    ],
  },
];

const articles = {
  overview: {
    title: "Architecture Overview",
    category: "Getting Started",
    product: "runtime",
    sections: [
      { id: "scope", label: "Scope" },
      { id: "subsystems", label: "Core subsystems" },
      { id: "stages", label: "Execution order" },
      { id: "constraints", label: "Target environment" },
    ],
    related: ["install", "pipeline", "api-reference"],
  },
  install: {
    title: "Dependency & Installation",
    category: "Getting Started",
    product: "runtime",
    sections: [
      { id: "coordinates", label: "Coordinates" },
      { id: "adding", label: "Gradle setup" },
      { id: "matrix", label: "Version matrix" },
      { id: "guard", label: "Soft-dependency guard" },
    ],
    related: ["overview", "api-reference", "mod-tiers"],
  },
  pipeline: {
    title: "Reload Pipeline Guide",
    category: "Getting Started",
    product: "runtime",
    sections: [
      { id: "trigger", label: "Triggering a reload" },
      { id: "plan", label: "Inspecting the plan" },
      { id: "stages", label: "Pipeline stages" },
      { id: "progress", label: "Progress callbacks" },
    ],
    related: ["safe-guard", "events", "atomic-rollback"],
  },
  "safe-guard": {
    title: "Safe Execution Boundary",
    category: "Getting Started",
    product: "runtime",
    sections: [
      { id: "boundary", label: "Boundary check" },
      { id: "why", label: "Registry consistency" },
      { id: "threading", label: "Threading model" },
    ],
    related: ["pipeline", "menu-rejoin", "atomic-rollback"],
  },
  "mod-tiers": {
    title: "Mod Classification Tiers",
    category: "Core Concepts",
    product: "runtime",
    sections: [
      { id: "tier1", label: "Tier 1: CLEAN" },
      { id: "tier2", label: "Tier 2: STANDARD" },
      { id: "tier3", label: "Tier 3: STUBBORN" },
      { id: "querying", label: "Querying tiers" },
    ],
    related: ["overview", "arch-rotation", "registry-thawing"],
  },
  "api-reference": {
    title: "Public API Reference",
    category: "Core Concepts",
    product: "runtime",
    sections: [
      { id: "mutoapi", label: "MutoApi" },
      { id: "records", label: "Result & plan records" },
      { id: "moddiff", label: "ModDiff" },
      { id: "example", label: "Usage example" },
    ],
    related: ["events", "pipeline", "atomic-rollback"],
  },
  events: {
    title: "Lifecycle Events",
    category: "Core Concepts",
    product: "runtime",
    sections: [
      { id: "start", label: "RELOAD_START" },
      { id: "finish", label: "RELOAD_FINISH" },
      { id: "example", label: "Registration" },
      { id: "threading", label: "Thread context" },
    ],
    related: ["api-reference", "pipeline"],
  },
  testing: {
    title: "Testing & CI",
    category: "Core Concepts",
    product: "runtime",
    sections: [
      { id: "stack", label: "Test stack" },
      { id: "run", label: "Running tests" },
      { id: "classes", label: "Test classes" },
      { id: "ci", label: "CI workflows" },
    ],
    related: ["source", "atomic-rollback"],
  },
  source: {
    title: "Repository & Packages",
    category: "Core Concepts",
    product: "runtime",
    sections: [
      { id: "repo", label: "Repository" },
      { id: "layout", label: "Package layout" },
      { id: "tasks", label: "Gradle tasks" },
      { id: "license", label: "License" },
    ],
    related: ["install", "testing"],
  },
  "fast-refresh": {
    title: "Fast Refresh Dev Loop",
    category: "Developer Integration",
    product: "runtime",
    sections: [
      { id: "how", label: "Mechanism" },
      { id: "setup", label: "Setup" },
      { id: "files", label: "Trigger & result files" },
    ],
    related: ["pipeline", "file-locks"],
  },
  "mod-state": {
    title: "Mod State Management",
    category: "Developer Integration",
    product: "runtime",
    sections: [
      { id: "staging", label: "Staging changes" },
      { id: "protected", label: "Protected mods" },
      { id: "applying", label: "Applying renames" },
    ],
    related: ["menu-toggles", "file-locks"],
  },
  config: {
    title: "Configuration (muto.json)",
    category: "Developer Integration",
    product: "runtime",
    sections: [
      { id: "file", label: "Config file" },
      { id: "flags", label: "Options" },
      { id: "notes", label: "Loading behavior" },
    ],
    related: ["menu-config", "fast-refresh"],
  },
  "file-locks": {
    title: "Windows File Locking",
    category: "Developer Integration",
    product: "runtime",
    sections: [
      { id: "problem", label: "NTFS file locks" },
      { id: "handling", label: "Deferred rename strategy" },
    ],
    related: ["mod-state", "fast-refresh"],
  },
  "jvm-tuning": {
    title: "JVM & JetBrains Runtime",
    category: "Developer Integration",
    product: "runtime",
    sections: [
      { id: "metaspace", label: "Metaspace growth" },
      { id: "heap", label: "Heap sizing" },
      { id: "flags", label: "JVM flags" },
    ],
    related: ["arch-rotation", "pipeline"],
  },
  "arch-rotation": {
    title: "Knot Integration & Reload",
    category: "Internals",
    product: "runtime",
    sections: [
      { id: "knot", label: "Knot classpath injection" },
      { id: "transformer", label: "Dynamic mixin registration" },
      { id: "consequence", label: "Already-loaded classes" },
    ],
    related: ["registry-thawing", "mod-tiers"],
  },
  "registry-thawing": {
    title: "Registry Thawing Mechanics",
    category: "Internals",
    product: "runtime",
    sections: [
      { id: "freeze", label: "Unfreeze & refreeze" },
      { id: "prune", label: "Namespace pruning" },
      { id: "caveat", label: "Internal field mapping" },
    ],
    related: ["arch-rotation", "atomic-rollback"],
  },
  "atomic-rollback": {
    title: "Atomic Rollback Guarantee",
    category: "Internals",
    product: "runtime",
    sections: [
      { id: "guarantee", label: "Rollback invariants" },
      { id: "results", label: "Handling results" },
    ],
    related: ["registry-thawing", "pipeline", "menu-history"],
  },

  // Muto Menu Pages
  "menu-overview": {
    title: "Muto Menu Overview",
    category: "Menu Features",
    product: "menu",
    sections: [
      { id: "features", label: "Key features" },
      { id: "installation", label: "Installation" },
      { id: "controls", label: "In-game access" },
    ],
    related: ["menu-toggles", "menu-watcher", "menu-history"],
  },
  "menu-toggles": {
    title: "Live Mod Toggling",
    category: "Menu Features",
    product: "menu",
    sections: [
      { id: "toggle-flow", label: "Toggling & staging" },
      { id: "dependency-warnings", label: "Dependency safety warnings" },
      { id: "applying", label: "Writing disk changes" },
    ],
    related: ["menu-overview", "mod-state", "menu-watcher"],
  },
  "menu-watcher": {
    title: "Folder Auto-Watcher",
    category: "Menu Features",
    product: "menu",
    sections: [
      { id: "switch", label: "Header toggle switch" },
      { id: "debounce", label: "Burst debounce & safety" },
      { id: "suppression", label: "Self-suppression during apply" },
    ],
    related: ["menu-overview", "menu-toggles", "pipeline"],
  },
  "menu-rejoin": {
    title: "In-World Reload & Rejoin",
    category: "Menu Features",
    product: "menu",
    sections: [
      { id: "boundary-flow", label: "The boundary reload flow" },
      { id: "multiplayer", label: "Multiplayer server auto-reconnect" },
      { id: "singleplayer", label: "Singleplayer world auto-reopen" },
    ],
    related: ["safe-guard", "menu-overview", "atomic-rollback"],
  },
  "menu-history": {
    title: "Reload History Viewer",
    category: "Menu Features",
    product: "menu",
    sections: [
      { id: "history-screen", label: "History screen layout" },
      { id: "diff-summary", label: "Diff and duration metrics" },
      { id: "stage-logs", label: "Stage logs expansion" },
    ],
    related: ["atomic-rollback", "menu-overview", "api-reference"],
  },
  "menu-config": {
    title: "Menu Settings (muto-menu.json)",
    category: "Menu Features",
    product: "menu",
    sections: [
      { id: "config-file", label: "Configuration path" },
      { id: "settings", label: "Settings breakdown" },
      { id: "keybinds", label: "Keybinding configuration" },
    ],
    related: ["menu-overview", "config"],
  },
};

function ArticleContent({ id }) {
  switch (id) {
    case "overview":
      return (
        <>
          <h1>Architecture Overview</h1>
          <p>
            Muto Runtime (<code>me.fy2ne.muto</code>) is a client-side Fabric library that loads,
            unloads, and updates mod jars inside a running Minecraft JVM without a restart. It ships
            as a pure developer engine with no required player UI. Frontend interfaces like{" "}
            <strong>Muto Menu</strong> link against <code>me.fy2ne.muto.api</code> and trigger hot-swaps programmatically.
          </p>

          <h2 id="scope">Scope</h2>
          <ul>
            <li>Provides runtime Knot classpath injection, <code>MappedRegistry</code> freeze/thaw primitives, and lifecycle hooks.</li>
            <li>Runs on the client only and provides safe boundary handoff for live singleplayer and multiplayer sessions.</li>
            <li>Licensed under <code>LGPL-3.0-only</code> so both open and proprietary mods can link against the API.</li>
          </ul>

          <h2 id="subsystems">Core subsystems</h2>
          <ul>
            <li>
              <strong>Loader bridge</strong>: <code>me.fy2ne.muto.engine.knot.KnotInjector</code> appends new or updated jars (plus nested Jar-in-Jar archives) to Fabric Loader's live <code>Knot</code> classloader. <code>me.fy2ne.muto.engine.mixin.DynamicMixinEngine</code> registers newly discovered mixin configs with the SpongePowered Mixin service.
            </li>
            <li>
              <strong>Reload engine</strong>: <code>me.fy2ne.muto.engine.ReloadEngine</code> runs directory scanning, SHA-256 diffing, registry thawing, transaction rollbacks, initializer execution, and resource remounting behind <code>me.fy2ne.muto.api.MutoApi</code>.
            </li>
          </ul>

          <h2 id="stages">Execution order</h2>
          <ol>
            <li>Scan <code>mods/</code> and hash each jar with SHA-256.</li>
            <li>Diff against the current snapshot (added, removed, updated, unchanged).</li>
            <li>Append new and updated jars to Knot's classpath.</li>
            <li>Register new mixin configurations with the Mixin service.</li>
            <li>Tear down removed or replaced mod state and prune their registry namespaces.</li>
            <li>Unfreeze built-in registries.</li>
            <li>Invoke <code>ModInitializer</code> and schedule <code>ClientModInitializer</code> on the render thread.</li>
            <li>Refreeze built-in registries.</li>
            <li>Synchronize keybindings and Fabric Loader mod metadata.</li>
            <li>Remount resource packs and reload OpenGL shaders.</li>
          </ol>

          <h2 id="constraints">Target environment</h2>
          <ul>
            <li>Minecraft <code>&gt;=26.1 &lt;27.0</code> (tested on <code>26.3</code>).</li>
            <li>Fabric Loader <code>&gt;=0.16.0</code> (built against <code>0.19.5</code>).</li>
            <li>Fabric API <code>&gt;=0.144.1</code> (built against <code>0.161.0+26.3</code>).</li>
            <li>Java <code>25+</code>, client environment only.</li>
          </ul>

          <DocAlert kind="note" label="Single reload owner">
            <p>
              Muto mutates Knot's internal classpath and unfreezes <code>MappedRegistry</code> instances directly. Do not run it alongside another mod that patches Knot or registry freeze flags at runtime.
            </p>
          </DocAlert>
        </>
      );

    case "install":
      return (
        <>
          <h1>Dependency &amp; Installation</h1>
          <p>
            Add Muto Runtime as a <code>compileOnly</code> (or <code>modCompileOnly</code>) dependency so your mod compiles against <code>me.fy2ne.muto.api</code> without bundling it. Place the runtime jar in <code>mods/</code> when testing or deploying.
          </p>

          <h2 id="coordinates">Coordinates</h2>
          <ul>
            <li>Maven group: <code>me.fy2ne</code></li>
            <li>Artifact: <code>muto</code></li>
            <li>Package root: <code>me.fy2ne.muto</code></li>
            <li>Fabric mod id: <code>muto</code></li>
                      </ul>

          <h2 id="adding">Gradle setup</h2>
          <p>Pull the artifact from local Maven or GitHub Packages / Modrinth Maven:</p>
          <CodeSnippet
            filename="build.gradle"
            code={`repositories {
    mavenLocal()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/fy2ne/Muto-Runtime")
    }
}

dependencies {
    compileOnly "me.fy2ne:muto:${VERSION}+26.x"
}`}
          />

          <h2 id="matrix">Version matrix</h2>
          <ul>
            <li><strong>Minecraft</strong>: <code>&gt;=26.1 &lt;27.0</code> (target <code>26.3</code>)</li>
            <li><strong>Fabric Loader</strong>: <code>&gt;=0.16.0</code></li>
            <li><strong>Fabric API</strong>: <code>&gt;=0.144.1</code></li>
            <li><strong>Java</strong>: <code>&gt;=25</code></li>
            <li><strong>Environment</strong>: <code>client</code></li>
          </ul>

          <h2 id="guard">Soft-dependency guard</h2>
          <p>
            Keep Muto optional in your <code>fabric.mod.json</code> and gate calls behind <code>FabricLoader.getInstance().isModLoaded("muto")</code> so your mod boots even if Muto is absent:
          </p>
          <CodeSnippet
            filename="MyMod.java"
            code={`import me.fy2ne.muto.api.MutoApi;
import net.fabricmc.loader.api.FabricLoader;

if (FabricLoader.getInstance().isModLoaded("muto") && MutoApi.isSafeToReload()) {
    MutoApi.reloadAsync(stage -> System.out.println("[muto] " + stage))
        .thenAccept(res -> {
            if (res.success()) {
                System.out.println("hot-swapped in " + res.durationMs() + "ms");
            } else {
                System.err.println("reload failed: " + res.error());
            }
        });
}`}
          />
        </>
      );

    case "pipeline":
      return (
        <>
          <h1>Reload Pipeline Guide</h1>
          <p>
            Place a newly built or downloaded jar into <code>mods/</code>, then call <code>MutoApi.reloadAsync()</code>. The engine diffs the directory against its last snapshot and applies changes in place.
          </p>

          <h2 id="trigger">Triggering a reload</h2>
          <p>
            <code>reloadAsync()</code> runs on a worker thread and returns a <code>CompletableFuture&lt;ReloadResult&gt;</code>. Pass a <code>Consumer&lt;String&gt;</code> to receive stage notifications as the swap progresses:
          </p>
          <CodeSnippet
            filename="ReloadTrigger.java"
            code={`MutoApi.reloadAsync(stage -> LOG.info("[muto] {}", stage))
    .thenAccept(res -> {
        if (res.success()) {
            LOG.info("reload ok in {}ms, updated={}",
                    res.durationMs(), res.diff().updated());
        } else {
            LOG.warn("reload failed: {}", res.error());
        }
    });`}
          />

          <h2 id="plan">Inspecting the plan</h2>
          <p>
            <code>MutoApi.plan()</code> hashes <code>mods/</code> and builds a <code>ReloadPlan</code> without touching Knot or registries:
          </p>
          <CodeSnippet
            filename="PlanPreview.java"
            code={`ReloadPlan plan = MutoApi.plan();

if (plan.hasWork()) {
    ModDiff diff = plan.diff();
    LOG.info("added={} removed={} updated={}",
            diff.added(), diff.removed(), diff.updated());
}`}
          />

          <h2 id="stages">Pipeline stages</h2>
          <ol>
            <li>Scan <code>mods/</code> and compute SHA-256 hashes for all jars.</li>
            <li>Diff against the active baseline snapshot.</li>
            <li>Run preflight checks on declared entrypoint classes.</li>
            <li>Snapshot affected registry namespaces for transactional rollback.</li>
            <li>Append new and updated jars (plus nested jars) to Knot's classpath.</li>
            <li>Register new dynamic mixin configs with the Mixin service.</li>
            <li>Tear down removed/updated mod state and prune their registry namespaces.</li>
            <li>Unfreeze built-in registries.</li>
            <li>Invoke <code>ModInitializer</code> and dispatch <code>ClientModInitializer</code> on the render thread.</li>
            <li>Refreeze built-in registries.</li>
            <li>Sync keymappings and update Fabric Loader mod list metadata.</li>
            <li>Remount resources and reload shaders.</li>
          </ol>

          <h2 id="progress">Progress callbacks</h2>
          <p>
            The stage consumer runs on the reload worker thread. If your callback mutates client UI or OpenGL state, dispatch back onto the main thread with <code>Minecraft.getInstance().submit(...)</code>.
          </p>
        </>
      );

    case "safe-guard":
      return (
        <>
          <h1>Safe Execution Boundary</h1>
          <p>
            Swapping mods mutates global registries and Knot's classpath. <code>MutoApi.isSafeToReload()</code> checks that the client is idle on the title screen before a swap begins.
          </p>

          <h2 id="boundary">Boundary check</h2>
          <p><code>isSafeToReload()</code> returns <code>true</code> only when:</p>
          <ul>
            <li><code>Minecraft.getInstance()</code> is initialized.</li>
            <li><code>isReloading()</code> is <code>false</code>.</li>
            <li>No world is loaded (<code>mc.level == null</code>).</li>
            <li>No integrated server is active (<code>!mc.hasSingleplayerServer()</code>).</li>
          </ul>

          <h2 id="why">Registry consistency</h2>
          <p>
            Unfreezing <code>MappedRegistry</code> and pruning namespaces while a world ticks will invalidate live holder references and numeric IDs. Restricting swaps to the title screen (or through the automatic boundary rejoin flow) ensures registries stay consistent.
          </p>

          <h2 id="threading">Threading model</h2>
          <ul>
            <li><code>reloadAsync()</code> executes off the main thread.</li>
            <li>Stage callbacks and <code>MutoEvents</code> hooks run on the reload worker thread.</li>
            <li>Client render initializers and resource reloads are marshaled onto the main thread via <code>Minecraft.getInstance().submit(...)</code>.</li>
          </ul>
        </>
      );

    case "mod-tiers":
      return (
        <>
          <h1>Mod Classification Tiers</h1>
          <p>
            <code>ModScanner</code> assigns each discovered jar a <code>ModTier</code> based on its <code>fabric.mod.json</code> entrypoints, mixins, and mod ID.
          </p>

          <h2 id="tier1">Tier 1: CLEAN</h2>
          <p>
            Client-only mods that declare a <code>client</code> entrypoint, no <code>main</code> or <code>server</code> entrypoints, and zero mixin configs. These touch HUD/render hooks without registering content and reload without registry thawing.
          </p>

          <h2 id="tier2">Tier 2: STANDARD</h2>
          <p>
            Mods that register blocks, items, entities, networking payloads, or dynamic mixins. Reloading runs the full pipeline: Knot injection, namespace pruning, <code>RegistryFreezer.unfreezeAll()</code>, initializer execution, and <code>freezeAll()</code>.
          </p>

          <h2 id="tier3">Tier 3: STUBBORN</h2>
          <p>
            Core modules locked to the root classloader: <code>minecraft</code>, <code>fabricloader</code>, <code>fabric</code>, <code>java</code>, <code>muto</code>, and any mod ID starting with <code>fabric-</code> or <code>quilt</code>.
          </p>

          <h2 id="querying">Querying tiers</h2>
          <CodeSnippet
            filename="Tiers.java"
            code={`Set<String> reloadable = MutoApi.getReloadableModIds(); // CLEAN + STANDARD
Set<String> stubborn   = MutoApi.getStubbornModIds();   // STUBBORN`}
          />
        </>
      );

    case "api-reference":
      return (
        <>
          <h1>Public API Reference</h1>
          <p>All public methods live in <code>me.fy2ne.muto.api</code> and are safe to call from any thread.</p>

          <h2 id="mutoapi">MutoApi</h2>
          <ul>
            <li><code>boolean isAvailable()</code> - returns <code>true</code> when the <code>muto</code> runtime is initialized.</li>
            <li><code>boolean isReloading()</code> - returns <code>true</code> while a swap transaction is active.</li>
            <li><code>boolean isSafeToReload()</code> - returns <code>true</code> on the title screen with no active world or reload.</li>
            <li><code>ReloadPlan plan()</code> - scans <code>mods/</code> and returns the diff without applying changes.</li>
            <li><code>CompletableFuture&lt;ReloadResult&gt; reloadAsync()</code> - runs the hot-swap pipeline asynchronously.</li>
            <li><code>void reloadWithBoundary(Screen returnScreen)</code> - disconnects safely from world/server, reloads, and auto-rejoins.</li>
            <li><code>List&lt;ReloadHistoryEntry&gt; getHistory()</code> - returns the last 50 recorded swap transactions.</li>
            <li><code>void setFolderWatcherEnabled(boolean enabled)</code> - controls the background folder watcher daemon.</li>
          </ul>

          <h2 id="records">Result &amp; plan records</h2>
          <ul>
            <li>
              <code>ReloadResult(boolean success, ModDiff diff, long durationMs, String error)</code> - returned when <code>reloadAsync()</code> completes.
            </li>
            <li>
              <code>ReloadPlan(ModSnapshot currentSnapshot, ModSnapshot targetSnapshot, ModDiff diff, List&lt;ScannedMod&gt; reloadable, List&lt;ScannedMod&gt; stubborn)</code>.
            </li>
          </ul>

          <h2 id="moddiff">ModDiff</h2>
          <p>
            Holds four unmodifiable sets of mod IDs: <code>added</code>, <code>removed</code>, <code>updated</code> (same ID, different SHA-256), and <code>unchanged</code>.
          </p>

          <h2 id="example">Usage example</h2>
          <CodeSnippet
            filename="Integration.java"
            code={`import me.fy2ne.muto.api.ModDiff;
import me.fy2ne.muto.api.MutoApi;

if (!MutoApi.isAvailable()) return;

if (MutoApi.isSafeToReload()) {
    MutoApi.reloadAsync(stage -> LOG.info("[muto] {}", stage))
        .thenAccept(res -> {
            if (res.success()) {
                LOG.info("swapped in {}ms", res.durationMs());
            } else {
                LOG.warn("swap failed, rolled back: {}", res.error());
            }
        });
} else {
    MutoApi.reloadWithBoundary(currentScreen);
}`}
          />
        </>
      );

    case "events":
      return (
        <>
          <h1>Lifecycle Events</h1>
          <p>
            <code>me.fy2ne.muto.api.MutoEvents</code> exposes Fabric <code>Event</code> instances for tearing down caches before a swap and rebinding state after completion.
          </p>

          <h2 id="start">RELOAD_START</h2>
          <CodeSnippet
            filename="ReloadStartListener.java"
            code={`void onReloadStart(long startEpochMs, ModDiff expectedDiff);`}
          />

          <h2 id="finish">RELOAD_FINISH</h2>
          <CodeSnippet
            filename="ReloadFinishListener.java"
            code={`void onReloadFinish(long endEpochMs, ModDiff diff, boolean success,
        long durationMs, Throwable error);`}
          />

          <h2 id="example">Registration</h2>
          <CodeSnippet
            filename="ListenerRegistration.java"
            code={`MutoEvents.RELOAD_START.register((startMs, diff) -> {
    MyTextureCache.clear();
});

MutoEvents.RELOAD_FINISH.register((endMs, diff, ok, ms, err) -> {
    if (ok) {
        MyModList.refresh();
    } else {
        LOG.warn("swap failed and rolled back", err);
    }
});`}
          />
        </>
      );

    case "testing":
      return (
        <>
          <h1>Testing &amp; CI</h1>
          <p>Unit tests run headless under JUnit 5 without booting a Minecraft client.</p>

          <h2 id="stack">Test stack</h2>
          <ul>
            <li><code>org.junit.jupiter:junit-jupiter-api:5.11.4</code></li>
            <li><code>org.junit.jupiter:junit-jupiter-engine:5.11.4</code></li>
            <li><code>org.junit.platform:junit-platform-launcher:1.11.4</code></li>
          </ul>

          <h2 id="run">Running tests</h2>
          <CodeSnippet filename="terminal" code={`./gradlew test`} />

          <h2 id="classes">Test classes</h2>
          <ul>
            <li><code>ModStateManagerTest</code> - core-mod lockouts and staged enable/disable sets.</li>
            <li><code>ModTierClassificationTest</code> - Tier 1, 2, and 3 classification rules.</li>
            <li><code>ReloadShellTest</code> - plan calculation, snapshot diffs, and rollback resilience.</li>
            <li><code>api.MutoApiTest</code> - public API guards and fallback behavior.</li>
          </ul>

          <h2 id="ci">CI workflows</h2>
          <ul>
            <li><code>.github/workflows/build.yml</code> - runs <code>./gradlew build</code> on JDK 25 on all <code>*.x</code> branches.</li>
            <li><code>.github/workflows/publish.yml</code> - publishes to GitHub Packages on release.</li>
          </ul>
        </>
      );

    case "source":
      return (
        <>
          <h1>Repository &amp; Packages</h1>

          <h2 id="repo">Repository</h2>
          <p>
            Source is hosted at <a href={RUNTIME_REPO} target="_blank" rel="noreferrer">{RUNTIME_REPO}</a> on the <code>26.x</code> branch.
          </p>

          <h2 id="layout">Package layout</h2>
          <ul>
            <li><code>me.fy2ne.muto.api</code> - <code>MutoApi</code>, <code>MutoEvents</code>, <code>ModDiff</code>.</li>
            <li><code>me.fy2ne.muto.engine</code> - <code>ReloadEngine</code>, <code>ModScanner</code>, <code>RegistryFreezer</code>.</li>
            <li><code>me.fy2ne.muto.engine.knot</code> - <code>KnotInjector</code> reflection bridge.</li>
            <li><code>me.fy2ne.muto.engine.mixin</code> - <code>DynamicMixinEngine</code>.</li>
            <li><code>me.fy2ne.muto.boundary</code> - <code>ReloadBoundaryManager</code>, <code>MutoReconnectingScreen</code>.</li>
            <li><code>me.fy2ne.muto.automation</code> - <code>ModsFolderWatcher</code>, <code>MutoTriggerWatcher</code>.</li>
            <li><code>me.fy2ne.muto.mods</code> - <code>ModStateManager</code>, <code>DiskMod</code>, <code>DisabledModsRegistry</code>.</li>
          </ul>

          <h2 id="license">License</h2>
          <p>
            Licensed under <strong>GNU Lesser General Public License v3.0</strong> (<code>LGPL-3.0-only</code>).
          </p>
        </>
      );

    case "fast-refresh":
      return (
        <>
          <h1>Fast Refresh Dev Loop</h1>
          <p>
            During local development, Muto watches for a trigger file, recompiles your mod on save, copies the new jar into <code>run/mods</code>, and hot-swaps it without closing the client.
          </p>

          <h2 id="how">Mechanism</h2>
          <p>
            <code>MutoTriggerWatcher</code> polls the run directory every 200ms for <code>muto.trigger</code>. When found, it deletes the trigger, runs <code>MutoApi.reloadAsync()</code>, and writes the outcome to <code>muto.result</code>.
          </p>

          <h2 id="setup">Setup</h2>
          <p>Apply the watcher script in your mod's <code>build.gradle</code>:</p>
          <CodeSnippet
            filename="build.gradle"
            code={`apply from: 'https://raw.githubusercontent.com/fy2ne/Muto-Runtime/26.x/muto-watcher.gradle'`}
          />
          <CodeSnippet filename="terminal" code={`./gradlew mutoWatch`} />
        </>
      );

    case "mod-state":
      return (
        <>
          <h1>Mod State Management</h1>
          <p>
            <code>me.fy2ne.muto.mods.ModStateManager</code> stages enable/disable toggles in memory and commits them on the next reload transaction.
          </p>

          <h2 id="staging">Staging changes</h2>
          <ul>
            <li><code>stageToggle(String modId)</code> / <code>toggleMod(DiskMod mod)</code> - flip a mod's staged state.</li>
            <li><code>isStaged(String modId)</code> - query staged state.</li>
            <li><code>getDependents(String modId, List&lt;DiskMod&gt; mods)</code> - reverse lookup mods that rely on <code>modId</code>.</li>
            <li><code>getMissingDependencies(DiskMod mod, List&lt;DiskMod&gt; mods)</code> - detect missing required dependencies.</li>
          </ul>

          <h2 id="protected">Protected mods</h2>
          <p>
            Mods matched by <code>isCore(...)</code> (<code>minecraft</code>, <code>fabricloader</code>, <code>fabric-api</code>, <code>java</code>, <code>muto</code>) cannot be staged or disabled.
          </p>

          <h2 id="applying">Applying renames</h2>
          <p>
            <code>applyFileRenames()</code> drains the staged sets and appends or strips <code>.disabled</code> on disk. If Windows holds an open file lock on a jar, the state is recorded in <code>DisabledModsRegistry</code> and reconciled on next boot.
          </p>
        </>
      );

    case "config":
      return (
        <>
          <h1>Configuration</h1>
          <p>
            Muto Runtime operates headlessly and avoids unnecessary configuration files. Runtime settings (like <code>autoReloadResources</code>) can be configured directly through <code>MutoApi</code> or via <strong>Muto Menu</strong>'s persistent settings.
          </p>
        </>
      );

    case "file-locks":
      return (
        <>
          <h1>Windows File Locking</h1>
          <h2 id="problem">NTFS file locks</h2>
          <p>
            On Windows, the JVM keeps an active file lock on any jar opened by <code>Knot</code>'s classloader. Overwriting or renaming that jar in place throws an <code>AccessDeniedException</code>.
          </p>

          <h2 id="handling">Deferred rename strategy</h2>
          <ul>
            <li>Updated jars are written under a new filename; Knot loads the new archive while the old handle stays untouched.</li>
            <li>When <code>applyFileRenames()</code> hits a locked jar, it persists the disabled state in <code>DisabledModsRegistry</code> without failing.</li>
            <li>On next client boot, before Knot opens jars, Muto reconciles pending renames.</li>
          </ul>
        </>
      );

    case "jvm-tuning":
      return (
        <>
          <h1>JVM &amp; JetBrains Runtime</h1>
          <h2 id="metaspace">Metaspace growth</h2>
          <p>
            Each reload defines new classes inside Knot and allocates metaspace. Reclaiming old metadata requires class unloading during concurrent GC cycles.
          </p>
          <CodeSnippet
            filename="jvm-args"
            code={`-Xmx4G
-XX:+UseG1GC
-XX:+ClassUnloadingWithConcurrentMark
-XX:MetaspaceSize=256M
-XX:MaxMetaspaceSize=1G`}
          />
        </>
      );

    case "arch-rotation":
      return (
        <>
          <h1>Knot Integration &amp; Reload</h1>
          <h2 id="knot">Knot classpath injection</h2>
          <p>
            <code>me.fy2ne.muto.engine.knot.KnotInjector</code> uses reflection to append a new or updated jar (and its nested Jar-in-Jar dependencies) directly to Knot's URL classpath so new classes resolve through the active loader.
          </p>

          <h2 id="transformer">Dynamic mixin registration</h2>
          <p>
            <code>me.fy2ne.muto.engine.mixin.DynamicMixinEngine</code> registers a new jar's mixin configs with the global Mixin service via <code>Mixins.addConfiguration</code>.
          </p>
        </>
      );

    case "registry-thawing":
      return (
        <>
          <h1>Registry Thawing Mechanics</h1>
          <p>
            Minecraft freezes every <code>MappedRegistry</code> at the end of bootstrap. Registering new blocks, items, or payloads at runtime requires clearing that flag for the duration of the reload.
          </p>

          <h2 id="freeze">Unfreeze &amp; refreeze</h2>
          <p>
            <code>me.fy2ne.muto.engine.RegistryFreezer</code> iterates <code>BuiltInRegistries.REGISTRY</code>. It clears the <code>frozen</code> boolean via <code>MappedRegistryAccessor</code> in <code>unfreezeAll()</code> before mod entrypoints run, and restores it in <code>freezeAll()</code> inside a <code>finally</code> block.
          </p>

          <h2 id="prune">Namespace pruning</h2>
          <p>
            Before re-running initializers, <code>pruneNamespace(String namespace)</code> removes prior entries under that mod's namespace from internal registry maps.
          </p>
        </>
      );

    case "atomic-rollback":
      return (
        <>
          <h1>Atomic Rollback Guarantee</h1>
          <h2 id="guarantee">Rollback invariants</h2>
          <p>
            If an entrypoint or mixin registration throws during a reload, Muto executes a full transactional rollback:
          </p>
          <ul>
            <li><code>RegistryFreezer.restoreSnapshot(...)</code> restores backed-up registry maps from before the reload.</li>
            <li>Newly created mod instances are closed and discarded; previous instances are restored.</li>
            <li><code>RegistryFreezer.freezeAll()</code> refreezes all registries.</li>
            <li>The failed transaction is recorded in <code>config/muto/history.json</code> for inspection.</li>
          </ul>

          <h2 id="results">Handling results</h2>
          <CodeSnippet
            filename="ResultHandling.java"
            code={`MutoApi.reloadAsync().thenAccept(res -> {
    if (res.success()) {
        LOG.info("reload ok in {}ms", res.durationMs());
    } else {
        LOG.warn("reload failed and rolled back cleanly: {}", res.error());
    }
});`}
          />
        </>
      );

    // Muto Menu pages
    case "menu-overview":
      return (
        <>
          <h1>Muto Menu Overview</h1>
          <p>
            <strong>Muto Menu</strong> (<code>me.fy2ne.muto.menu</code>) is the official in-game interface for Muto Runtime. It brings a sleek, interactive screen to manage, toggle, reload, and audit mods live inside Minecraft.
          </p>

          <h2 id="features">Key features</h2>
          <ul>
            <li><strong>Interactive Mods Screen</strong>: Open with <code>O</code> (rebindable in Controls) or click the <strong>Muto</strong> button on the Title and Pause screens.</li>
            <li><strong>Live Toggles</strong>: Toggle any non-core mod between <code>[on]</code> and <code>[off]</code>. Pending changes display an asterisk <code>*</code> until applied.</li>
            <li><strong>Folder Auto-Watcher</strong>: Drop jars in <code>.minecraft/mods</code> and watch them reload automatically on the title screen.</li>
            <li><strong>Boundary Reload &amp; Rejoin</strong>: Inside a singleplayer world or multiplayer server? Click <code>Reload &amp; Rejoin</code>—Muto drops to a sleek progress screen and rejoins automatically once hot-swap finishes.</li>
            <li><strong>Reload History Viewer</strong>: View past reload timestamps, durations, mod diffs, and stage logs.</li>
          </ul>

          <h2 id="installation">Installation</h2>
          <p>
            Place <code>muto-menu-0.3.0-beta+26.x.jar</code> into your <code>mods/</code> directory alongside <code>muto-0.3.0-beta+26.x.jar</code> and Fabric API.
          </p>

          <h2 id="controls">In-game access</h2>
          <p>Press <strong>`O`</strong> or navigate to the Pause Screen / Title Screen and click the top-right <strong>Muto</strong> button.</p>
        </>
      );

    case "menu-toggles":
      return (
        <>
          <h1>Live Mod Toggling</h1>
          <p>Muto Menu makes enabling and disabling mods as simple as clicking a button, with zero JVM restarts required.</p>

          <h2 id="toggle-flow">Toggling &amp; staging</h2>
          <p>
            Clicking a mod in the list stages it for toggle. The button updates immediately with an asterisk <code>*</code> indicating uncommitted changes:
          </p>
          <ul>
            <li><code>[off] MyMod 1.0.0 *</code> - Staged to be enabled.</li>
            <li><code>[on]  MyMod 1.0.0 *</code> - Staged to be disabled.</li>
          </ul>

          <h2 id="dependency-warnings">Dependency safety warnings</h2>
          <p>
            If you attempt to disable a library (e.g. <code>cloth-config</code>) that other active mods depend on, Muto Menu alerts you:
          </p>
          <CodeSnippet filename="In-Game Banner" code={`used by: ModA, ModB (click again to disable)`} />
          <p>Clicking a second time confirms that you want to proceed despite the warning.</p>

          <h2 id="applying">Writing disk changes</h2>
          <p>Clicking <strong>Apply (N)</strong> applies file renames (<code>.jar</code> &harr; <code>.jar.disabled</code>) and immediately invokes the hot-swap pipeline.</p>
        </>
      );

    case "menu-watcher":
      return (
        <>
          <h1>Folder Auto-Watcher</h1>
          <p>
            Muto Menu includes an integrated toggle for Muto Runtime's background directory watcher daemon.
          </p>

          <h2 id="switch">Header toggle switch</h2>
          <p>
            At the top of the Muto Menu screen, the <code>Watch: [ON / OFF]</code> button gives you instant control over auto-reloading:
          </p>
          <ul>
            <li><strong>ON</strong>: Dropping or deleting mod jars in <code>.minecraft/mods</code> triggers an automatic reload when on the title screen.</li>
            <li><strong>OFF</strong>: Suspends auto-reload so you can paste batches of mods in File Explorer without premature reloads.</li>
          </ul>

          <h2 id="debounce">Burst debounce &amp; safety</h2>
          <p>
            The watcher debounces file system events by 750ms to ensure large jar copies finish writing before a reload begins.
          </p>

          <h2 id="suppression">Self-suppression during apply</h2>
          <p>
            When you click <strong>Apply</strong> inside Muto Menu, the watcher automatically suppresses itself for 2.5 seconds to prevent double-reload feedback loops.
          </p>
        </>
      );

    case "menu-rejoin":
      return (
        <>
          <h1>In-World Reload &amp; Rejoin</h1>
          <p>
            Never leave your server or world manually to reload mods. Muto provides a seamless boundary reload flow.
          </p>

          <h2 id="boundary-flow">The boundary reload flow</h2>
          <p>
            When you open Muto Menu while inside a singleplayer world or multiplayer server, the bottom action button changes to <strong>`Reload &amp; Rejoin`</strong>.
          </p>

          <h2 id="multiplayer">Multiplayer server auto-reconnect</h2>
          <ol>
            <li>Captures your current server IP and connection info.</li>
            <li>Gracefully disconnects to <code>MutoReconnectingScreen</code> with a live progress bar.</li>
            <li>Executes the full reload pipeline safely on the title boundary.</li>
            <li>On success, automatically re-establishes the connection back to the server!</li>
          </ol>

          <h2 id="singleplayer">Singleplayer world auto-reopen</h2>
          <ol>
            <li>Flushes world chunks and saves the singleplayer level.</li>
            <li>Disconnects to the reload progress screen.</li>
            <li>Hot-swaps mod code and registries.</li>
            <li>Automatically calls <code>createWorldOpenFlows().openWorld(...)</code> to load your world right back up.</li>
          </ol>
        </>
      );

    case "menu-history":
      return (
        <>
          <h1>Reload History Viewer</h1>
          <p>
            Keep a complete audit trail of every mod hot-swap executed in your client session.
          </p>

          <h2 id="history-screen">History screen layout</h2>
          <p>
            Clicking <strong>`History (N)`</strong> in the top header opens a chronological list of past reloads:
          </p>
          <ul>
            <li><strong>Timestamp</strong>: When the reload occurred (<code>MM-dd HH:mm:ss</code>).</li>
            <li><strong>Status Tag</strong>: <code>[OK]</code> for clean swaps, <code>[ROLLBACK]</code> for recovered errors.</li>
            <li><strong>Duration</strong>: Total wall-clock time in milliseconds.</li>
            <li><strong>Diff Summary</strong>: <code>(+1 -0 ~2)</code> indicating added, removed, and updated mod counts.</li>
          </ul>

          <h2 id="stage-logs">Stage logs expansion</h2>
          <p>Clicking any history entry expands the exact stage output messages recorded during that swap.</p>
        </>
      );

    case "menu-config":
      return (
        <>
          <h1>Menu Settings (muto-menu.json)</h1>
          <p>Muto Menu persists user options in <code>.minecraft/config/muto-menu.json</code>.</p>

          <h2 id="config-file">Configuration path</h2>
          <CodeSnippet
            filename="config/muto-menu.json"
            code={`{
  "autoWatchFolder": true,
  "autoReloadResources": true,
  "warnDependencies": true
}`}
          />

          <h2 id="settings">Settings breakdown</h2>
          <ul>
            <li><code>autoWatchFolder</code>: Enables or disables the title-screen mods folder watcher.</li>
            <li><code>autoReloadResources</code>: Automatically re-indexes resource packs and shaders on reload.</li>
            <li><code>warnDependencies</code>: Warns before disabling a mod that other active mods depend on.</li>
          </ul>

          <h2 id="keybinds">Keybinding configuration</h2>
          <p>
            Configure the open key in <code>Options &gt; Controls &gt; Key Binds &gt; Misc &gt; Open Muto Menu</code> (default: <strong>`O`</strong>).
          </p>
        </>
      );

    default:
      return null;
  }
}


function getInitialRoute() {
  if (typeof window === "undefined") return { product: "runtime", id: "overview" };
  const path = window.location.pathname.replace(/^\/+|\/+$/g, "");
  const parts = path.split("/");

  if (parts[0] === "menu") {
    const artId = parts[1];
    if (artId && articles[artId] && articles[artId].product === "menu") {
      return { product: "menu", id: artId };
    }
    return { product: "menu", id: "menu-overview" };
  }

  const artId = parts[0] === "runtime" ? parts[1] : parts[0];
  if (artId && articles[artId] && articles[artId].product === "runtime") {
    return { product: "runtime", id: artId };
  }
  return { product: "runtime", id: "overview" };
}

function syncUrl(product, id) {
  if (typeof window === "undefined") return;
  const isDefault = (product === "runtime" && id === "overview") || (product === "menu" && id === "menu-overview");
  const newPath = isDefault ? `/${product}` : `/${product}/${id}`;
  if (window.location.pathname !== newPath) {
    window.history.pushState(null, "", newPath);
  }
}
export default function App() {
  const initial = useMemo(() => getInitialRoute(), []);
  const [product, setProduct] = useState(initial.product);
  const [cur, setCur] = useState(initial.id);
  const [menuOpen, setMenuOpen] = useState(false);
  const [q, setQ] = useState("");
  const [activeSec, setActiveSec] = useState(null);
  const scrollRef = useRef(null);

  useEffect(() => {
    if (typeof window !== "undefined" && (window.location.pathname === "/" || window.location.pathname === "")) {
      window.history.replaceState(null, "", "/runtime");
    }
  }, []);

  const nav = product === "runtime" ? RUNTIME_NAV : MENU_NAV;
  const currentRepo = product === "runtime" ? RUNTIME_REPO : MENU_REPO;
  const page = articles[cur] || (product === "runtime" ? articles.overview : articles["menu-overview"]);

  const currentNavItems = useMemo(
    () => nav.flatMap((g) => g.items.map((it) => ({ ...it, group: g.title }))),
    [nav]
  );

  const hits = useMemo(() => {
    const term = q.trim().toLowerCase();
    if (!term) return null;
    return currentNavItems.filter(
      (it) =>
        it.title.toLowerCase().includes(term) ||
        it.short.toLowerCase().includes(term) ||
        it.group.toLowerCase().includes(term)
    );
  }, [q, currentNavItems]);

  const idx = currentNavItems.findIndex((p) => p.id === cur);
  const prev = idx > 0 ? currentNavItems[idx - 1] : null;
  const next = idx < currentNavItems.length - 1 ? currentNavItems[idx + 1] : null;

  const go = (id) => {
    const target = articles[id];
    if (!target) return;
    setCur(id);
    syncUrl(product, id);
    setQ("");
    setActiveSec(null);
    setMenuOpen(false);
    scrollRef.current?.scrollTo({ top: 0 });
  };

  useEffect(() => {
    const onPop = () => {
      const route = getInitialRoute();
      setProduct(route.product);
      setCur(route.id);
    };
    window.addEventListener("popstate", onPop);
    return () => window.removeEventListener("popstate", onPop);
  }, []);

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: 0 });
  }, [cur]);

  const onScroll = () => {
    const box = scrollRef.current;
    if (!box) return;
    const top = box.scrollTop + 140;
    let found = null;
    if (page?.sections) {
      for (const sec of page.sections) {
        const el = document.getElementById(sec.id);
        if (el && el.offsetTop <= top) found = sec.id;
      }
    }
    setActiveSec(found);
  };

  return (
    <div className={`docs-shell ${product === "menu" ? "theme-menu" : "theme-runtime"}`}>
      <button
        className="mobile-menu-toggle"
        onClick={() => setMenuOpen((m) => !m)}
        aria-label="Toggle navigation"
        type="button"
      >
        {menuOpen ? <X size={18} /> : <MenuIcon size={18} />}
      </button>

      {/* Column 1: Left Sidebar */}
      <aside className={`sidebar ${menuOpen ? "open" : ""}`}>
        <div className="sb-header">
          <div className="sb-brand">
            <img
              src={product === "runtime" ? "/logo-runtime.png" : "/logo-menu.png"}
              alt={product === "runtime" ? "Muto Runtime" : "Muto Menu"}
              className="sb-brand-logo"
            />
            <span className="brand-title">
              {product === "runtime" ? "MUTO RUNTIME" : "MUTO MENU"}
            </span>
          </div>
        </div>

        <div className="sb-scroll">
          <nav className="sb-nav">
            {nav.map((g) => (
              <div className="sb-section" key={g.title}>
                <div className="sb-section-head">
                  <ChevronDown size={12} className="sb-chevron" />
                  <span>{g.title}</span>
                </div>
                <div className="sb-section-body">
                  {g.items.map((it) => (
                    <div
                      key={it.id}
                      className={`sb-link ${cur === it.id ? "active" : ""}`}
                      onClick={() => go(it.id)}
                    >
                      <span className="sb-link-icon">
                        <it.icon size={14} />
                      </span>
                      <span className="sb-link-label">{it.title}</span>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </nav>
        </div>
      </aside>

      {/* Column 2: Center Content */}
      <main className="docs-main" ref={scrollRef} onScroll={onScroll}>
        <div className="docs-content">
          <div className="page-breadcrumb">
            <span>Wiki</span>
            <span className="sep">/</span>
            <span className="current">{page?.title}</span>
          </div>

          <div className="md">
            <ArticleContent id={cur} />
          </div>

          <nav className="doc-pager" aria-label="Topic navigation">
            {prev ? (
              <button className="doc-pager-link prev" onClick={() => go(prev.id)} type="button">
                <ArrowLeft size={13} />
                <span>{prev.title}</span>
              </button>
            ) : <span />}
            {next ? (
              <button className="doc-pager-link next" onClick={() => go(next.id)} type="button">
                <span>{next.title}</span>
                <ArrowRight size={13} />
              </button>
            ) : <span />}
          </nav>

          <footer className="page-foot">
            <div className="page-foot-links">
              <a href={`${currentRepo}/tree/26.x`} target="_blank" rel="noreferrer">
                GitHub
              </a>
              <span className="sep">·</span>
              <a
                href={product === "runtime" ? "https://modrinth.com/mod/muto" : "https://modrinth.com/mod/muto-menu"}
                target="_blank"
                rel="noreferrer"
              >
                Modrinth
              </a>
              <span className="sep">·</span>
              <a href={`${currentRepo}/releases`} target="_blank" rel="noreferrer">
                Releases
              </a>
            </div>
            <span>{product === "runtime" ? "Muto Runtime" : "Muto Menu"}</span>
          </footer>
        </div>
      </main>

      {/* Column 3: Right Sidebar with Search & Numbered TOC */}
      <aside className="toc">
        <div className="toc-accent-bar" />
        <div className="toc-search-box">
          <Search size={14} className="toc-search-icon" />
          <input
            type="text"
            className="toc-search-input"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder="Search wiki..."
            aria-label="Search wiki"
          />
          {q && (
            <button
              className="toc-search-clear"
              onClick={() => setQ("")}
              aria-label="Clear search"
              type="button"
            >
              <X size={12} />
            </button>
          )}
        </div>

        {hits ? (
          <div className="toc-search-results">
            <div className="toc-results-head">
              {hits.length} result{hits.length === 1 ? "" : "s"}
            </div>
            <div className="toc-results-list">
              {hits.map((h) => (
                <button
                  key={h.id}
                  type="button"
                  className="toc-result-item"
                  onClick={() => go(h.id)}
                >
                  <span className="toc-result-title">{h.title}</span>
                  <span className="toc-result-group">{h.group}</span>
                </button>
              ))}
            </div>
          </div>
        ) : (
          <>
            {page?.sections && page.sections.length > 0 && (
              <div className="toc-sections-list">
                {page.sections.map((sec, idx) => (
                  <a
                    key={sec.id}
                    className={`toc-anchor ${activeSec === sec.id ? "active" : ""}`}
                    onClick={() =>
                      document
                        .getElementById(sec.id)
                        ?.scrollIntoView({ behavior: "smooth", block: "start" })
                    }
                  >
                    <span className="toc-num">{idx + 1}.</span>
                    <span className="toc-label">{sec.label}</span>
                  </a>
                ))}
              </div>
            )}
          </>
        )}
      </aside>
    </div>
  );
}
