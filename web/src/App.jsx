import { useEffect, useMemo, useRef, useState } from "react";
import katex from "katex";
import {
  ArrowRight,
  BookOpen,
  Boxes,
  Check,
  Code2,
  Copy,
  Cpu,
  Layers,
  Lock,
  RotateCw,
  Search,
  Settings,
  ShieldCheck,
  Sliders,
  Terminal,
  Wrench,
  X,
  Zap,
} from "lucide-react";

const REPO = "https://github.com/fy2ne/muto";
const VERSION = "0.3.0-beta";

/* ------------------------------------------------------------------ */
/* Shared components                                                   */
/* ------------------------------------------------------------------ */

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
  return (
    <div className={`doc-alert doc-alert-${kind}`}>
      <div className="doc-alert-label">{label}</div>
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

/* ------------------------------------------------------------------ */
/* Navigation                                                          */
/* ------------------------------------------------------------------ */

const nav = [
  {
    title: "Getting Started",
    items: [
      { id: "overview", title: "Architecture Overview", icon: BookOpen, short: "What Muto Runtime is and how the two halves fit together." },
      { id: "install", title: "Dependency & Installation", icon: Layers, short: "Maven coordinates, version matrix, optional-guard pattern." },
      { id: "pipeline", title: "Reload Pipeline Guide", icon: RotateCw, short: "Trigger, inspect and trace a hot-swap transaction." },
      { id: "safe-guard", title: "Safe Execution Boundary", icon: ShieldCheck, short: "When it is safe to reload and on which thread." },
    ],
  },
  {
    title: "Core Concepts",
    items: [
      { id: "mod-tiers", title: "Mod Classification Tiers", icon: Cpu, short: "Tier 1/2/3 classification and reloadability." },
      { id: "api-reference", title: "Public API Reference", icon: Code2, short: "Every public entry point in me.fy2ne.muto.api." },
      { id: "events", title: "Lifecycle Events", icon: Zap, short: "RELOAD_START and RELOAD_FINISH hooks." },
      { id: "testing", title: "Testing & CI", icon: Sliders, short: "JUnit 5 test stack and the GitHub Actions pipelines." },
      { id: "source", title: "Repository & Packages", icon: Terminal, short: "Source layout, Gradle tasks, license." },
    ],
  },
  {
    title: "Developer Integration",
    items: [
      { id: "fast-refresh", title: "Fast Refresh Dev Loop", icon: Zap, short: "mutoWatch and the muto.trigger file." },
      { id: "mod-state", title: "Mod State Management", icon: Boxes, short: "Stage enable/disable changes before a reload." },
      { id: "config", title: "Configuration (muto.json)", icon: Settings, short: "Runtime flags written by MutoConfig." },
      { id: "file-locks", title: "Windows File Locking", icon: Wrench, short: "Why jars cannot be replaced in place." },
      { id: "jvm-tuning", title: "JVM & JetBrains Runtime", icon: Terminal, short: "Heap sizing and GC flags for repeated swaps." },
    ],
  },
  {
    title: "Internals",
    items: [
      { id: "arch-rotation", title: "Knot Integration & Reload", icon: RotateCw, short: "Appending jars to Knot and registering mixins." },
      { id: "registry-thawing", title: "Registry Thawing Mechanics", icon: Lock, short: "unfreeze, pruneNamespace, refreeze." },
      { id: "atomic-rollback", title: "Atomic Rollback Guarantee", icon: ShieldCheck, short: "What survives a failed reload." },
    ],
  },
];

const articles = {
  overview: {
    title: "Architecture Overview",
    category: "Getting Started",
    sections: [
      { id: "what", label: "What it is" },
      { id: "halves", label: "Two halves" },
      { id: "stages", label: "Pipeline at a glance" },
      { id: "constraints", label: "Constraints" },
    ],
    related: ["install", "pipeline"],
  },
  install: {
    title: "Dependency & Installation",
    category: "Getting Started",
    sections: [
      { id: "coordinates", label: "Coordinates" },
      { id: "adding", label: "Adding the dependency" },
      { id: "matrix", label: "Version matrix" },
      { id: "guard", label: "Optional-guard pattern" },
    ],
    related: ["api-reference", "pipeline"],
  },
  pipeline: {
    title: "Reload Pipeline Guide",
    category: "Getting Started",
    sections: [
      { id: "trigger", label: "Triggering a reload" },
      { id: "plan", label: "Inspecting the plan" },
      { id: "stages", label: "Pipeline stages" },
      { id: "progress", label: "Progress callbacks" },
    ],
    related: ["safe-guard", "mod-tiers"],
  },
  "safe-guard": {
    title: "Safe Execution Boundary",
    category: "Getting Started",
    sections: [
      { id: "boundary", label: "The boundary check" },
      { id: "why", label: "Why the boundary exists" },
      { id: "threading", label: "Threading model" },
    ],
    related: ["pipeline", "registry-thawing"],
  },
  "mod-tiers": {
    title: "Mod Classification Tiers",
    category: "Core Concepts",
    sections: [
      { id: "tier1", label: "Tier 1 - clean client mods" },
      { id: "tier2", label: "Tier 2 - content mods" },
      { id: "tier3", label: "Tier 3 - stubborn / core" },
      { id: "querying", label: "Querying tiers" },
    ],
    related: ["pipeline", "registry-thawing"],
  },
  "api-reference": {
    title: "Public API Reference",
    category: "Core Concepts",
    sections: [
      { id: "mutoapi", label: "MutoApi" },
      { id: "records", label: "Result & plan records" },
      { id: "moddiff", label: "ModDiff" },
      { id: "example", label: "Full example" },
    ],
    related: ["events", "install"],
  },
  events: {
    title: "Lifecycle Events",
    category: "Core Concepts",
    sections: [
      { id: "start", label: "RELOAD_START" },
      { id: "finish", label: "RELOAD_FINISH" },
      { id: "example", label: "Example" },
      { id: "threading", label: "Threading" },
    ],
    related: ["api-reference", "pipeline"],
  },
  testing: {
    title: "Testing & CI",
    category: "Core Concepts",
    sections: [
      { id: "stack", label: "Test stack" },
      { id: "run", label: "Running the tests" },
      { id: "classes", label: "Test classes" },
      { id: "ci", label: "Continuous integration" },
    ],
    related: ["source", "install"],
  },
  source: {
    title: "Repository & Packages",
    category: "Core Concepts",
    sections: [
      { id: "repo", label: "Repository" },
      { id: "layout", label: "Source layout" },
      { id: "tasks", label: "Gradle tasks" },
      { id: "license", label: "License" },
    ],
    related: ["install", "testing"],
  },
  "fast-refresh": {
    title: "Fast Refresh Dev Loop",
    category: "Developer Integration",
    sections: [
      { id: "how", label: "How it works" },
      { id: "setup", label: "Setup" },
      { id: "files", label: "Trigger & result files" },
    ],
    related: ["pipeline", "config"],
  },
  "mod-state": {
    title: "Mod State Management",
    category: "Developer Integration",
    sections: [
      { id: "staging", label: "Staging changes" },
      { id: "protected", label: "Protected mods" },
      { id: "applying", label: "Applying changes" },
    ],
    related: ["pipeline", "file-locks"],
  },
  config: {
    title: "Configuration (muto.json)",
    category: "Developer Integration",
    sections: [
      { id: "file", label: "The config file" },
      { id: "flags", label: "Flags" },
      { id: "notes", label: "Notes" },
    ],
    related: ["fast-refresh", "source"],
  },
  "file-locks": {
    title: "Windows File Locking",
    category: "Developer Integration",
    sections: [
      { id: "problem", label: "The problem" },
      { id: "handling", label: "How Muto handles it" },
    ],
    related: ["mod-state", "pipeline"],
  },
  "jvm-tuning": {
    title: "JVM & JetBrains Runtime",
    category: "Developer Integration",
    sections: [
      { id: "metaspace", label: "Metaspace churn" },
      { id: "heap", label: "Heap sizing" },
      { id: "flags", label: "Recommended flags" },
    ],
    related: ["arch-rotation", "config"],
  },
  "arch-rotation": {
    title: "Knot Integration & Reload",
    category: "Internals",
    sections: [
      { id: "knot", label: "Knot injection" },
      { id: "transformer", label: "Dynamic mixin registration" },
      { id: "consequence", label: "The hard limit" },
    ],
    related: ["registry-thawing", "jvm-tuning"],
  },
  "registry-thawing": {
    title: "Registry Thawing Mechanics",
    category: "Internals",
    sections: [
      { id: "freeze", label: "Unfreeze & refreeze" },
      { id: "prune", label: "Namespace pruning" },
      { id: "caveat", label: "Version sensitivity" },
    ],
    related: ["mod-tiers", "arch-rotation"],
  },
  "atomic-rollback": {
    title: "Atomic Rollback Guarantee",
    category: "Internals",
    sections: [
      { id: "guarantee", label: "The guarantee" },
      { id: "results", label: "Reading results" },
    ],
    related: ["pipeline", "events"],
  },
};

/* ------------------------------------------------------------------ */
/* Article bodies                                                      */
/* ------------------------------------------------------------------ */

function ArticleContent({ id }) {
  switch (id) {
    case "overview":
      return (
        <>
          <h1>Architecture Overview</h1>
          <p>
            Muto Runtime (<code>me.fy2ne.muto</code>) is a <strong>developer library</strong> for
            Fabric that hot-swaps mods into an already-running Minecraft client. It is not a
            player-facing mod: there is no in-game screen, no menu and no gameplay feature. You
            depend on it from your own mod and call its API to inject mod jars into Fabric Loader at
            runtime and re-run their initializers without restarting the JVM.
          </p>
          <p>
            The artifact and mod id are <code>muto</code>; the code you call lives in{" "}
            <code>me.fy2ne.muto.api</code>.
          </p>

          <h2 id="what">What it is - and isn't</h2>
          <ul>
            <li>
              <strong>Is</strong>: a runtime hot-swap pipeline, Knot classpath injection, registry
              freeze/thaw helpers, and a small public API.
            </li>
            <li>
              <strong>Is</strong>: client-only, scoped to the title screen, licensed under the GNU
              Lesser General Public License v3.0.
            </li>
            <li>
              <strong>Isn't</strong>: a mod manager, a downloader, a launcher, or an in-game
              configuration UI.
            </li>
          </ul>

          <h2 id="halves">Two halves</h2>
          <p>
            The runtime is split into Fabric Loader integration and the reload engine that
            orchestrates the swap.
          </p>
          <ul>
            <li>
              <strong>Loader integration</strong> -{" "}
              <code>me.fy2ne.muto.engine.knot.KnotInjector</code> reaches into Fabric Loader's{" "}
              <code>Knot</code> to append a newly added jar to its classpath, and{" "}
              <code>me.fy2ne.muto.engine.mixin.DynamicMixinEngine</code> registers the jar's mixin
              configurations with the global Mixin service.
            </li>
            <li>
              <strong>Engine</strong> - <code>me.fy2ne.muto.engine.ReloadEngine</code> owns the
              scan, diff, registry thaw and mod-init sequence. The public API in{" "}
              <code>me.fy2ne.muto.api</code> is a thin facade over it.
            </li>
          </ul>

          <h2 id="stages">Pipeline at a glance</h2>
          <ol>
            <li>Scan the <code>mods</code> directory and hash each jar.</li>
            <li>Diff against the last snapshot (added / removed / updated / unchanged).</li>
            <li>Inject new and updated jars into Knot's classpath.</li>
            <li>Register the new jars' dynamic mixin configurations.</li>
            <li>Tear down removed / updated contexts and prune their registry namespaces.</li>
            <li>Unfreeze the built-in registries.</li>
            <li>
              Re-run each reloadable mod's <code>ModInitializer</code> /{" "}
              <code>ClientModInitializer</code>.
            </li>
            <li>Refreeze the registries.</li>
            <li>Sync key bindings and Fabric Loader mod metadata.</li>
            <li>Optionally remount resources and reload shaders.</li>
          </ol>

          <h2 id="constraints">Constraints</h2>
          <ul>
            <li>Minecraft <code>&gt;=26.1 &lt;27.0</code> (built and tested against <code>26.3</code>).</li>
            <li>Fabric Loader <code>&gt;=0.16.0</code> (developed against <code>0.19.5</code>).</li>
            <li>Fabric API (minimum declared <code>0.144.1</code>, build target <code>0.161.0+26.3</code>).</li>
            <li>Java <code>25</code>.</li>
            <li>Client environment only.</li>
          </ul>

          <DocAlert kind="note" label="Design intent">
            <p>
              The runtime assumes it is the only party injecting jars into Knot's classpath and
              mutating the game registries. It is not designed to coexist with other hot-reload
              tooling in the same process.
            </p>
          </DocAlert>
        </>
      );

    case "install":
      return (
        <>
          <h1>Dependency &amp; Installation</h1>
          <p>
            Muto Runtime consumes as a normal Fabric mod artifact. Add it as a{" "}
            <code>modCompileOnly</code> dependency so your code compiles against the API, and make
            sure the runtime jar is present in the game's <code>mods</code> folder at run time.
          </p>

          <h2 id="coordinates">Coordinates</h2>
          <ul>
            <li>Maven group: <code>me.fy2ne</code></li>
            <li>Artifact: <code>muto</code></li>
            <li>Java package: <code>me.fy2ne.muto</code></li>
            <li>Fabric mod id: <code>muto</code></li>
            <li>Current version: <code>{VERSION}</code> (jar build tag <code>+26.x</code>)</li>
          </ul>

          <h2 id="adding">Adding the dependency</h2>
          <p>
            The cheapest path is the Modrinth Maven repository, which serves the same jar that is
            published to the mod listing.
          </p>
          <CodeSnippet
            filename="build.gradle"
            code={`repositories {
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
    }
}

dependencies {
    modCompileOnly "maven.modrinth:muto:${VERSION}"
}`}
          />
          <p>
            The library build is also published to GitHub Packages as{" "}
            <code>me.fy2ne:muto</code> by <code>.github/workflows/publish.yml</code>, which you can
            resolve with the standard GitHub Maven repository and a <code>GITHUB_TOKEN</code>.
          </p>

          <h2 id="matrix">Version matrix</h2>
          <ul>
            <li>
              <strong>Minecraft</strong> - <code>&gt;=26.1 &lt;27.0</code>, developed against{" "}
              <code>26.3</code>
            </li>
            <li>
              <strong>Fabric Loader</strong> - declared <code>&gt;=0.16.0</code>, developed against{" "}
              <code>0.19.5</code>
            </li>
            <li>
              <strong>Fabric API</strong> - declared <code>&gt;=0.144.1</code>, build target{" "}
              <code>0.161.0+26.3</code>
            </li>
            <li>
              <strong>Java</strong> - <code>&gt;=25</code>
            </li>
            <li>
              <strong>Environment</strong> - <code>client</code>
            </li>
          </ul>

          <h2 id="guard">Optional-guard pattern</h2>
          <p>
            Because Muto is an optional dependency, declare it as a soft dependency and guard every
            call behind <code>MutoApi.isAvailable()</code> so your mod keeps working on installs
            that do not ship the runtime.
          </p>
          <CodeSnippet
            filename="MyMod.java"
            code={`import me.fy2ne.muto.api.MutoApi;
import net.fabricmc.loader.api.FabricLoader;

if (FabricLoader.getInstance().isModLoaded("muto") && MutoApi.isAvailable()) {
    MutoApi.reloadAsync().thenAccept(result -> {
        if (result.success()) {
            System.out.println("hot-swapped in " + result.durationMs() + "ms");
        }
    });
}`}
          />

          <DocAlert kind="warning" label="Runtime jar required">
            <p>
              <code>modCompileOnly</code> gives you the API at compile time only. The{" "}
              <code>muto</code> jar must still be present in <code>mods/</code> at run time or{" "}
              <code>isAvailable()</code> returns <code>false</code> and nothing will happen.
            </p>
          </DocAlert>
        </>
      );

    case "pipeline":
      return (
        <>
          <h1>Reload Pipeline Guide</h1>
          <p>
            A reload is the core operation. From your mod you build a jar, place it into the run
            directory's <code>mods</code> folder, then ask the engine to hot-swap it in. The pipeline
            never restarts the JVM.
          </p>

          <h2 id="trigger">Triggering a reload</h2>
          <p>
            <code>reloadAsync()</code> returns a <code>CompletableFuture&lt;ReloadResult&gt;</code>{" "}
            that resolves on a background thread. Use the overload that takes a{" "}
            <code>Consumer&lt;String&gt;</code> to receive a human-readable line for every stage.
          </p>
          <CodeSnippet
            filename="ReloadTrigger.java"
            code={`MutoApi.reloadAsync(stage -> LOG.info("[muto] {}", stage))
    .thenAccept(result -> {
        if (result.success()) {
            LOG.info("reload ok in {}ms, updated={}",
                    result.durationMs(), result.diff().updated());
        } else {
            LOG.warn("reload failed: {}", result.error());
        }
    });`}
          />

          <h2 id="plan">Inspecting the plan</h2>
          <p>
            <code>MutoApi.plan()</code> computes the diff without executing anything. It is cheap and
            does not inject jars or mutate registries, so it is safe to call for previews and
            logging.
          </p>
          <CodeSnippet
            filename="PlanPreview.java"
            code={`ReloadPlan plan = MutoApi.plan();

if (plan.hasWork()) {
    ModDiff diff = plan.diff();
    LOG.info("added={} removed={} updated={}",
            diff.added(), diff.removed(), diff.updated());
    LOG.info("reloadable={} stubborn={}",
            plan.reloadable().size(), plan.stubborn().size());
}`}
          />

          <h2 id="stages">Pipeline stages</h2>
          <ol>
            <li>Scan <code>mods</code> and hash each jar.</li>
            <li>Compute the diff against the recorded snapshot.</li>
            <li>Inject new and updated jars into Knot's classpath.</li>
            <li>Register the new jars' dynamic mixin configurations.</li>
            <li>Tear down removed / updated contexts and prune their registry namespaces.</li>
            <li>Unfreeze built-in registries.</li>
            <li>
              Re-run <code>ModInitializer</code> / <code>ClientModInitializer</code> for each
              reloadable mod.
            </li>
            <li>Refreeze the registries.</li>
            <li>Sync key bindings and Fabric Loader mod metadata.</li>
          </ol>

          <h2 id="progress">Progress callbacks</h2>
          <p>
            The stage notifier is invoked synchronously on the reload background thread - not the
            Minecraft main thread. Logging is fine; if you need to touch game state, submit the work
            with <code>Minecraft.getInstance().submit(...)</code>.
          </p>

          <DocAlert kind="note" label="No-op reloads">
            <p>
              If no mod changes are detected the pipeline returns immediately with a successful
              no-op <code>ReloadResult</code> and an empty diff.
            </p>
          </DocAlert>
        </>
      );

    case "safe-guard":
      return (
        <>
          <h1>Safe Execution Boundary</h1>
          <p>
            A hot swap mutates global registry and Fabric Loader state, so it is only safe at a
            quiescent point in the client lifecycle.{" "}
            <code>MutoApi.isSafeToReload()</code> encodes that boundary.
          </p>

          <h2 id="boundary">The boundary check</h2>
          <p>The method returns <code>true</code> only when all of the following hold:</p>
          <ul>
            <li>A <code>Minecraft</code> instance exists.</li>
            <li>No reload is currently in progress (<code>isReloading()</code> is false).</li>
            <li>The client is not inside a level (<code>mc.level == null</code>).</li>
            <li>No singleplayer server is running (<code>!mc.hasSingleplayerServer()</code>).</li>
          </ul>
          <p>
            If the check throws for any reason it fails closed and returns <code>false</code>.
          </p>

          <h2 id="why">Why the boundary exists</h2>
          <p>
            Registering blocks, items or entities into a live registry while the world is ticking
            can leave holder references and numeric ids inconsistent. Restricting reloads to the
            title screen keeps the registry in a state that can be thawed and rebuilt without racing
            the game loop.
          </p>

          <h2 id="threading">Threading model</h2>
          <ul>
            <li>
              <code>reloadAsync()</code> is non-blocking and resolves on a background thread.
            </li>
            <li>
              Stage notifiers and <code>MutoEvents</code> listeners fire on that same background
              thread.
            </li>
            <li>
              To run code on the game thread from a listener, use{" "}
              <code>Minecraft.getInstance().submit(...)</code>.
            </li>
          </ul>

          <DocAlert kind="warning" label="Respect the check">
            <p>
              Calling <code>reloadAsync()</code> while inside a world is technically possible but
              unsupported; it can corrupt mid-tick registry state. Always gate on{" "}
              <code>isSafeToReload()</code>.
            </p>
          </DocAlert>
        </>
      );

    case "mod-tiers":
      return (
        <>
          <h1>Mod Classification Tiers</h1>
          <p>
            Every scanned mod is classified before the diff is executed. The tier decides how much
            work the pipeline has to do and whether the mod can be swapped at all.
          </p>

          <h2 id="tier1">Tier 1 - clean client mods</h2>
          <p>
            HUDs, tweaks and UI-only mods that register nothing into game registries. They are the
            cheapest to swap: the jar is injected into Knot's classpath and the initializer is
            re-run.
          </p>

          <h2 id="tier2">Tier 2 - content mods</h2>
          <p>
            Mods that register blocks, items, entities or other registry entries. A swap requires
            the full registry thaw: unfreeze, prune the old namespace, re-run the initializer, and
            refreeze.
          </p>

          <h2 id="tier3">Tier 3 - stubborn / core</h2>
          <p>
            Fabric Loader, Fabric API and <code>muto</code> itself, plus mods with deeply-woven
            mixins. Their classes are already loaded and mixin-woven, so they cannot be re-woven in
            place and cannot be reloaded without a full JVM restart.
          </p>

          <h2 id="querying">Querying tiers</h2>
          <p>
            The API exposes the classification of the last recorded snapshot:
          </p>
          <CodeSnippet
            filename="Tiers.java"
            code={`Set<String> reloadable = MutoApi.getReloadableModIds(); // Tier 1 + Tier 2
Set<String> stubborn  = MutoApi.getStubbornModIds();   // Tier 3`}
          />
          <p>
            The core set is defined by{" "}
            <code>me.fy2ne.muto.mods.ModStateManager</code>: <code>minecraft</code>,{" "}
            <code>fabricloader</code>, <code>fabric</code>, <code>fabric-api</code>,{" "}
            <code>java</code>, <code>muto</code>, <code>fabric-api-base</code>,{" "}
            <code>fabric-lifecycle-events-v1</code> and <code>fabric-resource-loader-v0</code>.
            Any id starting with <code>fabric-api</code> is treated as core.
          </p>
        </>
      );

    case "api-reference":
      return (
        <>
          <h1>Public API Reference</h1>
          <p>
            All public entry points live in <code>me.fy2ne.muto.api</code>. Everything is
            thread-safe.
          </p>

          <h2 id="mutoapi">MutoApi</h2>
          <ul>
            <li>
              <code>boolean isAvailable()</code> - whether the <code>muto</code> mod is loaded.
              Outermost guard for every other call.
            </li>
            <li>
              <code>boolean isReloading()</code> - whether a reload transaction is in progress.
            </li>
            <li>
              <code>boolean isSafeToReload()</code> - whether the client is at a reloadable boundary
              (title screen).
            </li>
            <li>
              <code>ReloadPlan plan()</code> - computes the reload plan without executing it.
            </li>
            <li>
              <code>CompletableFuture&lt;ReloadResult&gt; reloadAsync()</code> - executes the full
              pipeline against the current <code>mods</code> directory.
            </li>
            <li>
              <code>
                CompletableFuture&lt;ReloadResult&gt; reloadAsync(Consumer&lt;String&gt; stageNotifier)
              </code>{" "}
              - same, with per-stage progress callbacks.
            </li>
            <li>
              <code>boolean startFileWatcher()</code> / <code>boolean stopFileWatcher()</code> /{" "}
              <code>boolean isFileWatcherRunning()</code> - control the dev trigger watcher.
            </li>
            <li>
              <code>Set&lt;String&gt; getReloadableModIds()</code> - Tier 1 + Tier 2 ids.
            </li>
            <li>
              <code>Set&lt;String&gt; getStubbornModIds()</code> - Tier 3 ids.
            </li>
          </ul>

          <h2 id="records">Result &amp; plan records</h2>
          <ul>
            <li>
              <code>ReloadResult(boolean success, ModDiff diff, long durationMs, String error)</code>{" "}
              - outcome of one pipeline run. Helpers <code>ReloadResult.ok(...)</code> and{" "}
              <code>ReloadResult.fail(...)</code>.
            </li>
            <li>
              <code>ReloadPlan(ModSnapshot currentSnapshot, ModSnapshot targetSnapshot, ModDiff diff, List&lt;ScannedMod&gt; reloadable, List&lt;ScannedMod&gt; stubborn)</code>{" "}
              - preview. <code>hasWork()</code> is true when the diff has changes.
            </li>
          </ul>

          <h2 id="moddiff">ModDiff</h2>
          <p>
            An immutable snapshot of the change set: <code>added</code>, <code>removed</code>,{" "}
            <code>updated</code> (same id, different jar hash) and <code>unchanged</code>. All sets
            are unmodifiable and never null. <code>hasChanges()</code> reports whether anything was
            added, removed or updated.
          </p>

          <h2 id="example">Full example</h2>
          <CodeSnippet
            filename="Integration.java"
            code={`import me.fy2ne.muto.api.ModDiff;
import me.fy2ne.muto.api.MutoApi;
import me.fy2ne.muto.engine.ReloadPlan;
import me.fy2ne.muto.engine.ReloadResult;

if (!MutoApi.isAvailable()) return;

ReloadPlan plan = MutoApi.plan();
if (!plan.hasWork()) {
    LOG.info("nothing to reload");
    return;
}

MutoApi.reloadAsync(stage -> LOG.info("[muto] {}", stage))
    .thenAccept(result -> {
        if (!result.success()) {
            LOG.warn("reload failed: {}", result.error());
            return;
        }
        ModDiff diff = result.diff();
        LOG.info("swapped in {}ms ({} updated)",
                result.durationMs(), diff.updated().size());
    });`}
          />
        </>
      );

    case "events":
      return (
        <>
          <h1>Lifecycle Events</h1>
          <p>
            <code>MutoEvents</code> exposes Fabric <code>Event</code> hooks so other mods can react
            to a hot-swap without polling. Register listeners during your mod's initialization.
          </p>

          <h2 id="start">RELOAD_START</h2>
          <p>
            Fired immediately before the pipeline begins. Receives the epoch timestamp of the
            trigger and the expected <code>ModDiff</code>. Use it to tear down state that depends on
            the mods about to be swapped.
          </p>
          <CodeSnippet
            filename="ReloadStartListener.java"
            code={`void onReloadStart(long startEpochMs, ModDiff expectedDiff);`}
          />

          <h2 id="finish">RELOAD_FINISH</h2>
          <p>
            Fired after the pipeline finishes - success, failure or no-op. Use it to refresh caches
            and re-bind resources.
          </p>
          <CodeSnippet
            filename="ReloadFinishListener.java"
            code={`void onReloadFinish(long endEpochMs, ModDiff diff, boolean success,
                    long durationMs, Throwable error);`}
          />

          <h2 id="example">Example</h2>
          <CodeSnippet
            filename="ListenerRegistration.java"
            code={`MutoEvents.RELOAD_START.register((startMs, diff) -> {
    MyTextureCache.clear();
    MyNetworkLayer.closeStaleChannels();
});

MutoEvents.RELOAD_FINISH.register((endMs, diff, success, durationMs, error) -> {
    if (success) {
        MyModList.refresh();
    } else {
        LOG.warn("reload failed: {}", error);
    }
});`}
          />

          <h2 id="threading">Threading</h2>
          <p>
            Listeners run synchronously on the reload background thread, not the main Minecraft
            thread. To touch game state, submit work via{" "}
            <code>Minecraft.getInstance().submit(...)</code>.
          </p>
        </>
      );

    case "testing":
      return (
        <>
          <h1>Testing &amp; CI</h1>
          <p>
            The runtime ships with a JUnit 5 unit-test suite that runs headless, without launching
            Minecraft, plus two GitHub Actions workflows.
          </p>

          <h2 id="stack">Test stack</h2>
          <ul>
            <li>
              <code>org.junit.jupiter:junit-jupiter-api:5.11.4</code>
            </li>
            <li>
              <code>org.junit.jupiter:junit-jupiter-engine:5.11.4</code>
            </li>
            <li>
              <code>org.junit.platform:junit-platform-launcher:1.11.4</code>
            </li>
          </ul>
          <p>
            These are declared in <code>build.gradle</code> and wired into the standard Loom{" "}
            <code>test</code> source set.
          </p>

          <h2 id="run">Running the tests</h2>
          <CodeSnippet filename="terminal" code={`./gradlew test`} />
          <p>
            HTML results are written to <code>build/reports/tests/test/index.html</code>; raw XML
            results are under <code>build/test-results/test/</code>.
          </p>

          <h2 id="classes">Test classes</h2>
          <ul>
            <li>
              <code>ModStateManagerTest</code> - core-mod protection and staged enable/disable
              toggling.
            </li>
            <li>
              <code>ModTierClassificationTest</code> - Tier 1/2/3 classification rules.
            </li>
            <li>
              <code>ReloadShellTest</code> - the reload shell: plan construction and diff handling
              without a live game.
            </li>
            <li>
              <code>api.MutoApiTest</code> - the public API surface and availability guard.
            </li>
          </ul>

          <h2 id="ci">Continuous integration</h2>
          <ul>
            <li>
              <strong>Build</strong> (<code>.github/workflows/build.yml</code>) - runs on pushes and
              pull requests to <code>main</code>. Uses <code>ubuntu-latest</code> with Temurin JDK
              25, runs <code>./gradlew build</code>, uploads the built jar, and uploads the test
              report on failure.
            </li>
            <li>
              <strong>Publish</strong> (<code>.github/workflows/publish.yml</code>) - publishes to
              GitHub Packages on a push to <code>26.x</code> that changes{" "}
              <code>gradle.properties</code>, on a published release, or via manual dispatch.
            </li>
          </ul>
        </>
      );

    case "source":
      return (
        <>
          <h1>Repository &amp; Packages</h1>

          <h2 id="repo">Repository</h2>
          <p>
            Source lives at <a href={REPO}>{REPO}</a>. Active development happens on the{" "}
            <code>26.x</code> branch; the build workflows target <code>main</code>.
          </p>

          <h2 id="layout">Source layout</h2>
          <ul>
            <li>
              <code>src/main/java/me/fy2ne/muto/api</code> - public surface:{" "}
              <code>MutoApi</code>, <code>MutoEvents</code>, <code>ModDiff</code>.
            </li>
            <li>
              <code>src/main/java/me/fy2ne/muto/engine</code> - <code>ReloadEngine</code>,{" "}
              <code>ModScanner</code>, <code>ModSnapshot</code>, <code>RegistryFreezer</code>,{" "}
              <code>MutoClassLoader</code>, <code>ReloadPlan</code>, <code>ReloadResult</code>.
            </li>
            <li>
              <code>src/main/java/me/fy2ne/muto/engine/knot</code> - Knot reflection access.
            </li>
            <li>
              <code>src/main/java/me/fy2ne/muto/engine/mixin</code> - dynamic mixin registration.
            </li>
            <li>
              <code>src/main/java/me/fy2ne/muto/engine/resource</code> - resource hot-mounting.
            </li>
            <li>
              <code>src/main/java/me/fy2ne/muto/mixin</code> - accessors and injections against
              Minecraft internals.
            </li>
            <li>
              <code>src/main/java/me/fy2ne/muto/mods</code> - disk mod state and the disabled-mods
              registry.
            </li>
            <li>
              <code>src/main/java/me/fy2ne/muto/config</code> - <code>MutoConfig</code>.
            </li>
            <li>
              <code>src/main/java/me/fy2ne/muto/automation</code> - the trigger-file watcher.
            </li>
            <li>
              <code>src/main/resources</code> - <code>fabric.mod.json</code>,{" "}
              <code>muto.mixins.json</code> and assets.
            </li>
            <li>
              <code>src/test/java</code> - the JUnit suite.
            </li>
            <li>
              <code>web</code> - this documentation site (Vite + React).
            </li>
          </ul>

          <h2 id="tasks">Gradle tasks</h2>
          <ul>
            <li>
              <code>./gradlew build</code> - compile, test and assemble.
            </li>
            <li>
              <code>./gradlew jar</code> - package the runtime jar.
            </li>
            <li>
              <code>./gradlew runClient</code> - launch a dev client.
            </li>
            <li>
              <code>./gradlew mutoWatch</code> - the fast-refresh dev loop (see{" "}
              <a href="#fast-refresh">Fast Refresh Dev Loop</a>).
            </li>
            <li>
              <code>./gradlew publish</code> - publish to GitHub Packages.
            </li>
          </ul>

          <h2 id="license">License</h2>
          <p>
            Muto Runtime is licensed under the{" "}
            <strong>GNU Lesser General Public License v3.0</strong> (
            <code>LGPL-3.0-only</code>). Any mod may depend on and link against Muto, including
            closed-source mods; modifications to Muto's own source must remain open under the LGPL.
            See <a href={REPO + "/blob/26.x/LICENSE"}>LICENSE</a>.
          </p>
        </>
      );

    case "fast-refresh":
      return (
        <>
          <h1>Fast Refresh Dev Loop</h1>
          <p>
            For iterating on a mod you are actively developing, Muto provides a file-triggered
            rebuild-and-swap loop so you never restart the client.
          </p>

          <h2 id="how">How it works</h2>
          <p>
            A watcher polls for a <code>muto.trigger</code> file in the game directory every 200ms.
            When it appears, the file is consumed, a reload is executed, and the outcome is written
            to <code>muto.result</code>. The Gradle task <code>mutoWatch</code> wires this together:
            it watches <code>src/main/java</code> and <code>src/main/resources</code>, recompiles on
            change, copies the jar into <code>run/mods</code>, and writes the trigger file.
          </p>

          <h2 id="setup">Setup</h2>
          <p>In your mod's build script:</p>
          <CodeSnippet
            filename="build.gradle"
            code={`apply from: 'https://raw.githubusercontent.com/fy2ne/muto/main/muto-watcher.gradle'`}
          />
          <p>Then, with the client already running:</p>
          <CodeSnippet filename="terminal" code={`./gradlew mutoWatch`} />
          <p>
            Alternatively, start the watcher from code with{" "}
            <code>MutoApi.startFileWatcher()</code> and stop it with{" "}
            <code>MutoApi.stopFileWatcher()</code>; query state with{" "}
            <code>MutoApi.isFileWatcherRunning()</code>.
          </p>

          <h2 id="files">Trigger &amp; result files</h2>
          <ul>
            <li>
              <code>muto.trigger</code> - written by the Gradle task to request a reload; deleted by
              the watcher once consumed.
            </li>
            <li>
              <code>muto.result</code> - written by the runtime with{" "}
              <code>SUCCESS</code>, <code>DURATION</code> and the added/removed/updated sets.
            </li>
          </ul>

          <DocAlert kind="note" label="Dev-only">
            <p>
              The watcher is a development tool. Do not auto-start it in a production mod.
            </p>
          </DocAlert>
        </>
      );

    case "mod-state":
      return (
        <>
          <h1>Mod State Management</h1>
          <p>
            <code>me.fy2ne.muto.mods.ModStateManager</code> tracks enable/disable changes that are
            staged and only applied at the next reload, so a change is always all-or-nothing.
          </p>

          <h2 id="staging">Staging changes</h2>
          <ul>
            <li>
              <code>stageToggle(String modId)</code> / <code>toggleMod(DiskMod mod)</code> - flip
              the staged state of a mod.
            </li>
            <li>
              <code>isStaged(String modId)</code>, <code>isStagedForDisable(...)</code>,{" "}
              <code>isStagedForEnable(...)</code> - inspect the pending state.
            </li>
            <li>
              <code>getStagedDisables()</code> / <code>getStagedEnables()</code> - the pending sets.
            </li>
            <li>
              <code>hasStagedChanges()</code> / <code>getStagedCount()</code> /{" "}
              <code>clearStaged()</code>.
            </li>
          </ul>
          <p>
            <code>scanDiskMods()</code> returns the on-disk mods (including{" "}
            <code>.jar.disabled</code> / <code>.disabled</code> / <code>.deactivation</code> files)
            as <code>DiskMod</code> records.
          </p>

          <h2 id="protected">Protected mods</h2>
          <p>
            Core mods reported by <code>isCore(...)</code> cannot be staged or disabled. The set
            covers Minecraft, Fabric Loader, Fabric API, Java and <code>muto</code> itself.
          </p>

          <h2 id="applying">Applying changes</h2>
          <p>
            <code>applyFileRenames()</code> drains the staged sets and renames the corresponding jar
            files to/from a <code>.disabled</code> suffix. If a file is locked on disk (see{" "}
            <a href="#file-locks">Windows File Locking</a>), the state is still recorded in{" "}
            <code>DisabledModsRegistry</code> so the rename happens on the next boot.
          </p>
        </>
      );

    case "config":
      return (
        <>
          <h1>Configuration (muto.json)</h1>
          <p>
            <code>me.fy2ne.muto.config.MutoConfig</code> reads and writes{" "}
            <code>config/muto.json</code> using Gson. It falls back to the working directory's{" "}
            <code>config</code> folder when the Fabric config directory is unavailable. If the file
            does not exist, defaults are written on first access.
          </p>

          <h2 id="file">The config file</h2>
          <CodeSnippet
            filename="config/muto.json"
            code={`{
  "autoReloadResources": true
}`}
          />

          <h2 id="flags">Flags</h2>
          <p>
            The runtime currently reads a single flag from this file:
          </p>
          <ul>
            <li>
              <code>autoReloadResources</code> - re-mount resources as part of a reload
              (<code>ReloadEngine</code>).
            </li>
          </ul>

          <h2 id="notes">Notes</h2>
          <DocAlert kind="warning" label="Inert keys">
            <p>
              <code>MutoConfig</code> still declares nine additional boolean keys ({" "}
              <code>autoReloadOnDiskChange</code>, <code>showToasts</code>,{" "}
              <code>playReloadSound</code>, <code>fastShaderReload</code>,{" "}
              <code>autoReconnect</code>, <code>showTitleScreenButton</code>,{" "}
              <code>confirmBeforeReload</code>, <code>checkModrinthUpdates</code>,{" "}
              <code>simulateUpdateAvailable</code>). None of them are read anywhere in the codebase.
              They are leftovers from an earlier player-facing iteration and can be ignored.
            </p>
          </DocAlert>
        </>
      );

    case "file-locks":
      return (
        <>
          <h1>Windows File Locking</h1>

          <h2 id="problem">The problem</h2>
          <p>
            On Windows the JVM holds an open handle to a mod jar for as long as classes are loaded
            from it. Replacing or renaming that file in place fails with a sharing violation, which
            is why naive hot-reload tooling breaks on Windows.
          </p>

          <h2 id="handling">How Muto handles it</h2>
          <ul>
            <li>
              Muto never overwrites a jar in place. An update arrives as a new file, and the old jar
              is left on disk (or renamed aside) so any handle the loader still holds stays valid.
            </li>
            <li>
              If a rename still fails because the file is locked,{" "}
              <code>ModStateManager.applyFileRenames()</code> logs the lock and records the intended
              state in <code>DisabledModsRegistry</code> instead of failing the transaction.
            </li>
            <li>
              The rename is retried on the next boot, when the handle has been released.
            </li>
          </ul>
          <p>
            Because state is persisted rather than inferred from the filename alone, a mod can be{" "}
            <em>logically</em> disabled even while its jar still carries its original name.
          </p>
        </>
      );

    case "jvm-tuning":
      return (
        <>
          <h1>JVM &amp; JetBrains Runtime</h1>

          <h2 id="metaspace">Metaspace churn</h2>
          <p>
            A reload appends newly added jars to Knot's classpath and re-runs their initializers.
            Every distinct class the JVM defines adds metadata to metaspace, and that metadata is only
            reclaimed by a GC that unloads classes. A dev session that reloads dozens of times per
            hour therefore benefits from a little extra headroom.
          </p>

          <h2 id="heap">Heap sizing</h2>
          <p>
            A reasonable starting point for a mod-dev client is to give the client roughly double
            the heap of a normal run, so the metadata churn from repeated reloads fits comfortably.
            Let <Math tex="H_{base}" /> be the heap the client needs without reloading and{" "}
            <Math tex="N" /> the number of reloads between full collections; a safe reservation is:
          </p>
          <Math block tex="H_{jvm} \;\ge\; H_{base} + k \cdot N \cdot S_{class}" />
          <p>
            where <Math tex="S_{class}" /> is the average metaspace footprint added per reload and{" "}
            <Math tex="k" /> is a small safety factor (2 is a good default).
          </p>

          <h2 id="flags">Recommended flags</h2>
          <CodeSnippet
            filename="jvm-args"
            code={`-Xmx4G
-XX:+UseG1GC
-XX:+ClassUnloadingWithConcurrentMark
-XX:MetaspaceSize=256M
-XX:MaxMetaspaceSize=1G`}
          />
          <DocAlert kind="note" label="Metaspace unloading">
            <p>
              Class unloading is the mechanism that lets reloading scale. Ensure your collector has
              class unloading enabled, and prefer a build of the JVM (such as JetBrains Runtime)
              that is tuned for long-lived interactive sessions.
            </p>
          </DocAlert>
        </>
      );

    case "arch-rotation":
      return (
        <>
          <h1>Knot Integration &amp; Reload</h1>

          <h2 id="knot">Knot injection</h2>
          <p>
            Fabric Loader's <code>Knot</code> classloader is built once at boot and owns the
            installed mod set. <code>me.fy2ne.muto.engine.knot.KnotInjector</code> uses reflection to
            append a newly added jar to that classpath, so the jar's classes resolve without a
            restart. There is no disposable child loader in the production path: the new jar joins
            the loader that is already running.
          </p>

          <h2 id="transformer">Dynamic mixin registration</h2>
          <p>
            <code>me.fy2ne.muto.engine.mixin.DynamicMixinEngine</code> registers a newly added mod's
            mixin configurations with the global Mixin service{" "}
            (<code>Mixins.addConfiguration</code>). Mixin weaves only when a class is first defined,
            so those configurations apply to every class the JVM defines after the reload.
          </p>

          <h2 id="consequence">The hard limit</h2>
          <DocAlert kind="warning" label="Already-loaded classes">
            <p>
              A class the JVM has already defined and transformed cannot be re-woven by a later
              reload. New mixins therefore only affect classes loaded after the swap, and a mod whose
              mixins target classes that are already live is classified as Tier 3 and needs a
              restart.
            </p>
          </DocAlert>
        </>
      );

    case "registry-thawing":
      return (
        <>
          <h1>Registry Thawing Mechanics</h1>
          <p>
            Minecraft's <code>MappedRegistry</code> is <code>frozen</code> once the game has
            started. Registering new content requires clearing that flag for the duration of the
            swap.
          </p>

          <h2 id="freeze">Unfreeze &amp; refreeze</h2>
          <p>
            <code>me.fy2ne.muto.engine.RegistryFreezer</code> walks{" "}
            <code>BuiltInRegistries.REGISTRY</code>. For each entry it prefers the{" "}
            <code>MappedRegistryAccessor</code> mixin and falls back to setting the{" "}
            <code>frozen</code> field reflectively. <code>unfreezeAll()</code> runs before mod init;{" "}
            <code>freezeAll()</code> runs after.
          </p>

          <h2 id="prune">Namespace pruning</h2>
          <p>
            Before re-registration, entries left behind by the previous load must be removed.{" "}
            <code>pruneNamespace(String namespace)</code> strips matching keys from the registry's{" "}
            <code>byLocation</code>, <code>byKey</code>, <code>byValue</code>,{" "}
            <code>registrationInfos</code>, <code>byId</code> and <code>toId</code> structures, then
            trims trailing nulls from the id list. This avoids duplicate ids when a mod registers the
            same block or item twice across a reload.
          </p>

          <h2 id="caveat">Version sensitivity</h2>
          <DocAlert kind="warning" label="Field names are not API">
            <p>
              Pruning relies on the internal field names of <code>MappedRegistry</code>. They are
              mapped per Minecraft version and are not a stable contract. If a field lookup fails,
              the freezer logs a warning and skips the reflective path rather than crashing.
            </p>
          </DocAlert>
        </>
      );

    case "atomic-rollback":
      return (
        <>
          <h1>Atomic Rollback Guarantee</h1>

          <h2 id="guarantee">The guarantee</h2>
          <p>
            A failed reload must not leave the client in a half-registered state. The pipeline is
            structured so that either the new mod set is fully installed, or the client is left
            running with the previous set:
          </p>
          <ul>
            <li>Registries are always refrozen as the pipeline unwinds.</li>
            <li>A partially applied diff is never committed.</li>
            <li>Every run produces a <code>ReloadResult</code> - including no-ops and failures.</li>
            <li>
              Each run is recorded as a <code>ReloadHistoryEntry</code> for post-mortem inspection.
            </li>
          </ul>

          <h2 id="results">Reading results</h2>
          <p>
            Check <code>success()</code> first. On success, inspect <code>diff()</code> and{" "}
            <code>durationMs()</code>. On failure, <code>error()</code> carries a human-readable
            message; the diff reflects what was processed.
          </p>
          <CodeSnippet
            filename="ResultHandling.java"
            code={`MutoApi.reloadAsync().thenAccept(result -> {
    if (result.success()) {
        LOG.info("reload ok in {}ms", result.durationMs());
    } else {
        LOG.warn("reload failed, still on previous set: {}", result.error());
    }
});`}
          />
        </>
      );

    default:
      return null;
  }
}

/* ------------------------------------------------------------------ */
/* App shell                                                           */
/* ------------------------------------------------------------------ */

export default function App() {
  const [current, setCurrent] = useState("overview");
  const [query, setQuery] = useState("");
  const [activeSection, setActiveSection] = useState(null);
  const scrollRef = useRef(null);

  const article = articles[current];

  const flat = useMemo(
    () =>
      nav.flatMap((group) =>
        group.items.map((item) => ({ ...item, group: group.title }))
      ),
    []
  );

  const results = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return null;
    return flat.filter(
      (item) =>
        item.title.toLowerCase().includes(q) ||
        item.short.toLowerCase().includes(q) ||
        item.group.toLowerCase().includes(q)
    );
  }, [query, flat]);

  const go = (id) => {
    setCurrent(id);
    setQuery("");
    setActiveSection(null);
    scrollRef.current?.scrollTo({ top: 0 });
  };

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: 0 });
  }, [current]);

  const onScroll = () => {
    const container = scrollRef.current;
    if (!container) return;
    const top = container.scrollTop + 120;
    let active = null;
    for (const section of article.sections) {
      const el = document.getElementById(section.id);
      if (el && el.offsetTop <= top) active = section.id;
    }
    setActiveSection(active);
  };

  return (
    <div className="docs-shell">
      <header className="docs-header">
        <div className="docs-header-left">
          <span className="brand" onClick={() => go("overview")}>
            <Terminal size={18} color="#f59e0b" />
            <span className="brand-text">Muto Runtime</span>
            <span className="brand-badge">Beta</span>
          </span>
        </div>
        <div className="docs-header-right">
          <a
            className="nav-icon-link"
            href={REPO}
            target="_blank"
            rel="noreferrer"
            aria-label="Source on GitHub"
          >
            <Code2 size={16} />
            <span className="nav-tooltip">Source</span>
          </a>
        </div>
      </header>

      <aside className="sidebar">
        <div className="sb-scroll">
          <div className="sb-search-card">
            <div className="sb-search-input">
              <Search size={14} />
              <input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Search the docs"
                aria-label="Search the docs"
              />
              {query && (
                <button
                  className="sb-search-clear"
                  onClick={() => setQuery("")}
                  aria-label="Clear search"
                  type="button"
                >
                  <X size={13} />
                </button>
              )}
            </div>
          </div>

          {results ? (
            <nav className="sb-nav">
              <div className="sb-section">
                <div className="sb-section-head">
                  {results.length} result{results.length === 1 ? "" : "s"}
                </div>
                <div className="sb-section-body">
                  {results.map((item) => (
                    <div
                      key={item.id}
                      className={`sb-link ${current === item.id ? "active" : ""}`}
                      onClick={() => go(item.id)}
                    >
                      <span className="sb-link-icon">
                        <item.icon size={15} />
                      </span>
                      <span className="sb-link-label">{item.title}</span>
                    </div>
                  ))}
                </div>
              </div>
            </nav>
          ) : (
            <nav className="sb-nav">
              {nav.map((group) => (
                <div className="sb-section" key={group.title}>
                  <div className="sb-section-head">{group.title}</div>
                  <div className="sb-section-body">
                    {group.items.map((item) => (
                      <div
                        key={item.id}
                        className={`sb-link ${current === item.id ? "active" : ""}`}
                        onClick={() => go(item.id)}
                      >
                        <span className="sb-link-icon">
                          <item.icon size={15} />
                        </span>
                        <span className="sb-link-label">{item.title}</span>
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </nav>
          )}
        </div>
      </aside>

      <main className="docs-main" ref={scrollRef} onScroll={onScroll}>
        <div className="docs-content">
          <div className="page-breadcrumb">
            <span>Muto Runtime</span>
            <span className="sep">/</span>
            <span>{article.category}</span>
            <span className="sep">/</span>
            <span className="current">{article.title}</span>
          </div>

          <div className="md">
            <ArticleContent id={current} />
          </div>

          {article.related?.length > 0 && (
            <div className="md" style={{ marginTop: 48 }}>
              <h2>Continue reading</h2>
              <div style={{ display: "flex", flexWrap: "wrap", gap: 10 }}>
                {article.related.map((rid) => (
                  <button
                    key={rid}
                    className="codeblock-copy"
                    onClick={() => go(rid)}
                    type="button"
                    style={{ cursor: "pointer" }}
                  >
                    {articles[rid].title}
                    <ArrowRight size={12} />
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>

        <aside className="toc">
          <div className="toc-current-page">{article.title}</div>
          <div className="toc-sections">
            {article.sections.map((section) => (
              <a
                key={section.id}
                className={`toc-anchor ${activeSection === section.id ? "active" : ""}`}
                onClick={() =>
                  document
                    .getElementById(section.id)
                    ?.scrollIntoView({ behavior: "smooth", block: "start" })
                }
              >
                {section.label}
              </a>
            ))}
          </div>
        </aside>
      </main>
    </div>
  );
}
