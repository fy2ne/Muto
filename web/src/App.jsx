import { useEffect, useMemo, useRef, useState } from "react";
import katex from "katex";
import {
  ArrowLeft,
  ArrowRight,
  BookOpen,
  Boxes,
  Check,
  Code2,
  Copy,
  Cpu,
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

const REPO = "https://github.com/fy2ne/muto";
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

async function sendFeedbackPayload(msg, email, page) {
  const cleanEmail = email.trim();
  const payload = { message: msg.trim(), email: cleanEmail, page };

  try {
    const res = await fetch("/api/feedback", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    const ct = res.headers.get("content-type") || "";
    if (res.ok && ct.includes("application/json")) return true;
  } catch {
    // local vite dev server has no worker route; fall back to direct inbound endpoint
  }

  const fromAddr = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(cleanEmail)
    ? cleanEmail.toLowerCase()
    : "anon@muto.fy2ne.me";
  const subject = `[Muto Docs] Feedback (${page})${cleanEmail ? ` from ${cleanEmail}` : ""}`;
  const text = `Page: ${page}\nReply-To: ${cleanEmail || "not provided"}\n\n${msg.trim()}`;

  const fallback = await fetch("https://emails.fy2ne.me/api/inbound", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      to: "hello@fy2ne.me",
      from: fromAddr,
      subject,
      text,
    }),
  });

  if (!fallback.ok) throw new Error("send failed");
  return true;
}

function FeedbackBox({ page, onClose, compact = false }) {
  const [msg, setMsg] = useState("");
  const [email, setEmail] = useState("");
  const [state, setState] = useState("idle"); // idle | sending | sent | err
  const inputRef = useRef(null);

  useEffect(() => {
    if (!compact) inputRef.current?.focus();
  }, [compact]);

  const submit = async (e) => {
    e?.preventDefault();
    if (msg.trim().length < 2 || state === "sending") return;
    setState("sending");
    try {
      await sendFeedbackPayload(msg, email, page);
      setState("sent");
      setMsg("");
      setTimeout(() => {
        setState("idle");
        onClose?.();
      }, 1600);
    } catch {
      setState("err");
    }
  };

  const onKey = (e) => {
    if ((e.metaKey || e.ctrlKey) && e.key === "Enter") {
      submit(e);
    } else if (e.key === "Escape" && onClose) {
      onClose();
    }
  };

  if (state === "sent") {
    return (
      <div className={`fb-box ${compact ? "fb-box-inline" : "fb-box-pop"}`}>
        <div className="fb-sent">
          <Check size={14} color="#f59e0b" />
          <span>Sent to hello@fy2ne.me</span>
        </div>
      </div>
    );
  }

  return (
    <form
      className={`fb-box ${compact ? "fb-box-inline" : "fb-box-pop"}`}
      onSubmit={submit}
    >
      <div className="fb-head">
        <span className="fb-title">Quick feedback / contact</span>
        <a className="fb-mailto" href="mailto:hello@fy2ne.me">
          hello@fy2ne.me
        </a>
      </div>
      <textarea
        ref={inputRef}
        className="fb-textarea"
        rows={3}
        value={msg}
        onChange={(e) => {
          setMsg(e.target.value);
          if (state === "err") setState("idle");
        }}
        onKeyDown={onKey}
        placeholder="Bug report, missing doc detail, or question..."
        maxLength={4000}
        required
      />
      <div className="fb-foot">
        <input
          type="email"
          className="fb-email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          onKeyDown={onKey}
          placeholder="your@email.com (optional, for reply)"
          maxLength={160}
        />
        <button
          type="submit"
          className="fb-send"
          disabled={msg.trim().length < 2 || state === "sending"}
        >
          <Send size={11} />
          <span>{state === "sending" ? "Sending..." : "Send"}</span>
        </button>
      </div>
      {state === "err" && (
        <div className="fb-err">
          Could not send right now. Email <a href="mailto:hello@fy2ne.me">hello@fy2ne.me</a> directly.
        </div>
      )}
    </form>
  );
}

const nav = [
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
      { id: "atomic-rollback", title: "Atomic Rollback Guarantee", icon: ShieldCheck, short: "Failure recovery and ReloadHistoryEntry records." },
    ],
  },
];

const articles = {
  overview: {
    title: "Architecture Overview",
    category: "Getting Started",
    sections: [
      { id: "scope", label: "Scope" },
      { id: "subsystems", label: "Core subsystems" },
      { id: "stages", label: "Execution order" },
      { id: "constraints", label: "Target environment" },
    ],
  },
  install: {
    title: "Dependency & Installation",
    category: "Getting Started",
    sections: [
      { id: "coordinates", label: "Coordinates" },
      { id: "adding", label: "Gradle setup" },
      { id: "matrix", label: "Version matrix" },
      { id: "guard", label: "Soft-dependency guard" },
    ],
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
  },
  "safe-guard": {
    title: "Safe Execution Boundary",
    category: "Getting Started",
    sections: [
      { id: "boundary", label: "Boundary check" },
      { id: "why", label: "Registry consistency" },
      { id: "threading", label: "Threading model" },
    ],
  },
  "mod-tiers": {
    title: "Mod Classification Tiers",
    category: "Core Concepts",
    sections: [
      { id: "tier1", label: "Tier 1: CLEAN" },
      { id: "tier2", label: "Tier 2: STANDARD" },
      { id: "tier3", label: "Tier 3: STUBBORN" },
      { id: "querying", label: "Querying tiers" },
    ],
  },
  "api-reference": {
    title: "Public API Reference",
    category: "Core Concepts",
    sections: [
      { id: "mutoapi", label: "MutoApi" },
      { id: "records", label: "Result & plan records" },
      { id: "moddiff", label: "ModDiff" },
      { id: "example", label: "Usage example" },
    ],
  },
  events: {
    title: "Lifecycle Events",
    category: "Core Concepts",
    sections: [
      { id: "start", label: "RELOAD_START" },
      { id: "finish", label: "RELOAD_FINISH" },
      { id: "example", label: "Registration" },
      { id: "threading", label: "Thread context" },
    ],
  },
  testing: {
    title: "Testing & CI",
    category: "Core Concepts",
    sections: [
      { id: "stack", label: "Test stack" },
      { id: "run", label: "Running tests" },
      { id: "classes", label: "Test classes" },
      { id: "ci", label: "CI workflows" },
    ],
  },
  source: {
    title: "Repository & Packages",
    category: "Core Concepts",
    sections: [
      { id: "repo", label: "Repository" },
      { id: "layout", label: "Package layout" },
      { id: "tasks", label: "Gradle tasks" },
      { id: "license", label: "License" },
    ],
  },
  "fast-refresh": {
    title: "Fast Refresh Dev Loop",
    category: "Developer Integration",
    sections: [
      { id: "how", label: "Mechanism" },
      { id: "setup", label: "Setup" },
      { id: "files", label: "Trigger & result files" },
    ],
  },
  "mod-state": {
    title: "Mod State Management",
    category: "Developer Integration",
    sections: [
      { id: "staging", label: "Staging changes" },
      { id: "protected", label: "Protected mods" },
      { id: "applying", label: "Applying renames" },
    ],
  },
  config: {
    title: "Configuration (muto.json)",
    category: "Developer Integration",
    sections: [
      { id: "file", label: "Config file" },
      { id: "flags", label: "Options" },
      { id: "notes", label: "Loading behavior" },
    ],
  },
  "file-locks": {
    title: "Windows File Locking",
    category: "Developer Integration",
    sections: [
      { id: "problem", label: "NTFS file locks" },
      { id: "handling", label: "Deferred rename strategy" },
    ],
  },
  "jvm-tuning": {
    title: "JVM & JetBrains Runtime",
    category: "Developer Integration",
    sections: [
      { id: "metaspace", label: "Metaspace growth" },
      { id: "heap", label: "Heap sizing" },
      { id: "flags", label: "JVM flags" },
    ],
  },
  "arch-rotation": {
    title: "Knot Integration & Reload",
    category: "Internals",
    sections: [
      { id: "knot", label: "Knot classpath injection" },
      { id: "transformer", label: "Dynamic mixin registration" },
      { id: "consequence", label: "Already-loaded classes" },
    ],
  },
  "registry-thawing": {
    title: "Registry Thawing Mechanics",
    category: "Internals",
    sections: [
      { id: "freeze", label: "Unfreeze & refreeze" },
      { id: "prune", label: "Namespace pruning" },
      { id: "caveat", label: "Internal field mapping" },
    ],
  },
  "atomic-rollback": {
    title: "Atomic Rollback Guarantee",
    category: "Internals",
    sections: [
      { id: "guarantee", label: "Rollback invariants" },
      { id: "results", label: "Handling results" },
    ],
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
            no screens, commands, or player-facing UI. Other mods link against{" "}
            <code>me.fy2ne.muto.api</code> and trigger hot-swaps programmatically.
          </p>

          <h2 id="scope">Scope</h2>
          <ul>
            <li>
              Provides runtime Knot classpath injection, <code>MappedRegistry</code> freeze/thaw
              primitives, and lifecycle hooks.
            </li>
            <li>
              Runs on the client only and restricts swaps to the title screen (no active world or
              integrated server).
            </li>
            <li>
              Licensed under <code>LGPL-3.0-only</code> so both open and proprietary mods can link
              against the API.
            </li>
          </ul>

          <h2 id="subsystems">Core subsystems</h2>
          <ul>
            <li>
              <strong>Loader bridge</strong>: <code>me.fy2ne.muto.engine.knot.KnotInjector</code>{" "}
              appends new or updated jars (plus nested Jar-in-Jar archives) to Fabric Loader's live{" "}
              <code>Knot</code> classloader.{" "}
              <code>me.fy2ne.muto.engine.mixin.DynamicMixinEngine</code> registers newly discovered
              mixin configs with the SpongePowered Mixin service.
            </li>
            <li>
              <strong>Reload engine</strong>: <code>me.fy2ne.muto.engine.ReloadEngine</code> runs
              directory scanning, SHA-256 diffing, registry thawing, initializer execution, and
              resource remounting behind <code>me.fy2ne.muto.api.MutoApi</code>.
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
            <li>
              Invoke <code>ModInitializer</code> and schedule <code>ClientModInitializer</code> on
              the render thread.
            </li>
            <li>Refreeze built-in registries.</li>
            <li>Synchronize keybindings and Fabric Loader mod metadata.</li>
            <li>Remount resource packs and reload shaders when <code>autoReloadResources</code> is set.</li>
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
              Muto mutates Knot's internal classpath and unfreezes <code>MappedRegistry</code>{" "}
              instances directly. Do not run it alongside another mod that patches Knot or registry
              freeze flags at runtime.
            </p>
          </DocAlert>
        </>
      );

    case "install":
      return (
        <>
          <h1>Dependency &amp; Installation</h1>
          <p>
            Add Muto Runtime as a <code>compileOnly</code> (or <code>modCompileOnly</code>)
            dependency so your mod compiles against <code>me.fy2ne.muto.api</code> without bundling
            it. Place the runtime jar in <code>mods/</code> when testing or deploying.
          </p>

          <h2 id="coordinates">Coordinates</h2>
          <ul>
            <li>Maven group: <code>me.fy2ne</code> (or <code>maven.modrinth</code> via Modrinth Maven)</li>
            <li>Artifact: <code>muto</code></li>
            <li>Package root: <code>me.fy2ne.muto</code></li>
            <li>Fabric mod id: <code>muto</code></li>
            <li>Current release: <code>{VERSION}</code></li>
          </ul>

          <h2 id="adding">Gradle setup</h2>
          <p>
            Pull the artifact from Modrinth Maven. On Minecraft 26.x with Loom 1.15 non-remapping
            builds, standard <code>compileOnly</code> works directly:
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
    compileOnly "maven.modrinth:muto:${VERSION}"
}`}
          />
          <p>
            Commits to <code>26.x</code> also publish <code>me.fy2ne:muto</code> to GitHub Packages
            via <code>.github/workflows/publish.yml</code>.
          </p>

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
            Keep Muto optional in your <code>fabric.mod.json</code> and gate calls behind{" "}
            <code>FabricLoader.getInstance().isModLoaded("muto")</code> so your mod still boots when
            Muto is absent:
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

          <DocAlert kind="warning" label="Runtime jar">
            <p>
              <code>compileOnly</code> only exposes headers at build time. Drop the{" "}
              <code>muto</code> jar into <code>mods/</code> at runtime or{" "}
              <code>isAvailable()</code> will return <code>false</code>.
            </p>
          </DocAlert>
        </>
      );

    case "pipeline":
      return (
        <>
          <h1>Reload Pipeline Guide</h1>
          <p>
            Place a newly built or downloaded jar into <code>mods/</code>, then call{" "}
            <code>MutoApi.reloadAsync()</code>. The engine diffs the directory against its last
            snapshot and applies changes in place.
          </p>

          <h2 id="trigger">Triggering a reload</h2>
          <p>
            <code>reloadAsync()</code> runs on a worker thread and returns a{" "}
            <code>CompletableFuture&lt;ReloadResult&gt;</code>. Pass a{" "}
            <code>Consumer&lt;String&gt;</code> to receive stage notifications as the swap
            progresses:
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
            <code>MutoApi.plan()</code> hashes <code>mods/</code> and builds a{" "}
            <code>ReloadPlan</code> without touching Knot or registries:
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
            <li>Scan <code>mods/</code> and compute SHA-256 hashes for all jars.</li>
            <li>Diff against the active baseline snapshot.</li>
            <li>Run preflight checks on declared entrypoint classes.</li>
            <li>Append new and updated jars (plus nested jars) to Knot's classpath.</li>
            <li>Register new mixin configs with the Mixin service.</li>
            <li>Tear down removed/updated mod state and prune their registry namespaces.</li>
            <li>Unfreeze built-in registries.</li>
            <li>
              Invoke <code>ModInitializer</code> and dispatch <code>ClientModInitializer</code> on
              the render thread.
            </li>
            <li>Refreeze built-in registries.</li>
            <li>Sync keymappings and update Fabric Loader mod list metadata.</li>
            <li>Remount resources and reload shaders when <code>autoReloadResources</code> is enabled.</li>
          </ol>

          <h2 id="progress">Progress callbacks</h2>
          <p>
            The stage consumer runs on the reload worker thread. If your callback mutates client UI
            or OpenGL state, dispatch back onto the main thread with{" "}
            <code>Minecraft.getInstance().submit(...)</code>.
          </p>

          <DocAlert kind="note" label="No-op runs">
            <p>
              When no jar hashes have changed in <code>mods/</code>, <code>reloadAsync()</code>{" "}
              resolves immediately with <code>success = true</code> and an empty{" "}
              <code>ModDiff</code>.
            </p>
          </DocAlert>
        </>
      );

    case "safe-guard":
      return (
        <>
          <h1>Safe Execution Boundary</h1>
          <p>
            Swapping mods mutates global registries and Knot's classpath.{" "}
            <code>MutoApi.isSafeToReload()</code> checks that the client is idle on the title screen
            before a swap begins.
          </p>

          <h2 id="boundary">Boundary check</h2>
          <p>
            <code>isSafeToReload()</code> returns <code>true</code> only when:
          </p>
          <ul>
            <li><code>Minecraft.getInstance()</code> is initialized.</li>
            <li><code>isReloading()</code> is <code>false</code>.</li>
            <li>No world is loaded (<code>mc.level == null</code>).</li>
            <li>No integrated server is active (<code>!mc.hasSingleplayerServer()</code>).</li>
          </ul>
          <p>
            Any unexpected exception inside the check returns <code>false</code>.
          </p>

          <h2 id="why">Registry consistency</h2>
          <p>
            Unfreezing <code>MappedRegistry</code> and pruning namespaces while a world ticks will
            invalidate live holder references and numeric IDs. Restricting swaps to the title screen
            ensures registries can be pruned and re-populated before any world state binds to them.
          </p>

          <h2 id="threading">Threading model</h2>
          <ul>
            <li><code>reloadAsync()</code> executes off the main thread.</li>
            <li>Stage callbacks and <code>MutoEvents</code> hooks run on the reload worker thread.</li>
            <li>
              Client render initializers and resource reloads are marshaled onto the main thread via{" "}
              <code>Minecraft.getInstance().submit(...)</code>.
            </li>
          </ul>

          <DocAlert kind="warning" label="Never bypass in-world">
            <p>
              Triggering <code>reloadAsync()</code> while a level is loaded can corrupt chunk and
              entity registry IDs. Always check <code>isSafeToReload()</code> first.
            </p>
          </DocAlert>
        </>
      );

    case "mod-tiers":
      return (
        <>
          <h1>Mod Classification Tiers</h1>
          <p>
            <code>ModScanner</code> assigns each discovered jar a <code>ModTier</code> based on its{" "}
            <code>fabric.mod.json</code> entrypoints, mixins, and mod ID.
          </p>

          <h2 id="tier1">Tier 1: CLEAN</h2>
          <p>
            Client-only mods that declare a <code>client</code> entrypoint, no <code>main</code> or{" "}
            <code>server</code> entrypoints, and zero mixin configs. These touch HUD/render hooks
            without registering content and reload without registry thawing.
          </p>

          <h2 id="tier2">Tier 2: STANDARD</h2>
          <p>
            Mods that register blocks, items, entities, networking payloads, or dynamic mixins.
            Reloading runs the full pipeline: Knot injection, namespace pruning,{" "}
            <code>RegistryFreezer.unfreezeAll()</code>, initializer execution, and{" "}
            <code>freezeAll()</code>.
          </p>

          <h2 id="tier3">Tier 3: STUBBORN</h2>
          <p>
            Core modules locked to the root classloader: <code>minecraft</code>,{" "}
            <code>fabricloader</code>, <code>fabric</code>, <code>java</code>, <code>muto</code>,
            and any mod ID starting with <code>fabric-</code> or <code>quilt</code>. Also includes
            mods whose mixins target classes already loaded by the JVM. Requires a client restart.
          </p>

          <h2 id="querying">Querying tiers</h2>
          <CodeSnippet
            filename="Tiers.java"
            code={`Set<String> reloadable = MutoApi.getReloadableModIds(); // CLEAN + STANDARD
Set<String> stubborn   = MutoApi.getStubbornModIds();   // STUBBORN`}
          />
          <p>
            On disk, <code>ModStateManager.isCore()</code> uses the same core ID list to block
            renaming system jars to <code>.disabled</code>.
          </p>
        </>
      );

    case "api-reference":
      return (
        <>
          <h1>Public API Reference</h1>
          <p>
            All public methods live in <code>me.fy2ne.muto.api</code> and are safe to call from any
            thread.
          </p>

          <h2 id="mutoapi">MutoApi</h2>
          <ul>
            <li>
              <code>boolean isAvailable()</code> - returns <code>true</code> when the{" "}
              <code>muto</code> runtime is initialized.
            </li>
            <li>
              <code>boolean isReloading()</code> - returns <code>true</code> while a swap
              transaction is active.
            </li>
            <li>
              <code>boolean isSafeToReload()</code> - returns <code>true</code> on the title screen
              with no active world or reload.
            </li>
            <li>
              <code>ReloadPlan plan()</code> - scans <code>mods/</code> and returns the diff without
              applying changes.
            </li>
            <li>
              <code>CompletableFuture&lt;ReloadResult&gt; reloadAsync()</code> - runs the hot-swap
              pipeline asynchronously.
            </li>
            <li>
              <code>CompletableFuture&lt;ReloadResult&gt; reloadAsync(Consumer&lt;String&gt; stageNotifier)</code>{" "}
              - runs the pipeline and streams stage names to <code>stageNotifier</code>.
            </li>
            <li>
              <code>boolean startFileWatcher()</code> / <code>boolean stopFileWatcher()</code> /{" "}
              <code>boolean isFileWatcherRunning()</code> - controls the <code>muto.trigger</code>{" "}
              file watcher.
            </li>
            <li>
              <code>Set&lt;String&gt; getReloadableModIds()</code> - Tier 1 and Tier 2 mod IDs in
              the active snapshot.
            </li>
            <li>
              <code>Set&lt;String&gt; getStubbornModIds()</code> - Tier 3 mod IDs in the active
              snapshot.
            </li>
          </ul>

          <h2 id="records">Result &amp; plan records</h2>
          <ul>
            <li>
              <code>ReloadResult(boolean success, ModDiff diff, long durationMs, String error)</code>{" "}
              - returned when <code>reloadAsync()</code> completes. Static factories:{" "}
              <code>ReloadResult.ok(...)</code> and <code>ReloadResult.fail(...)</code>.
            </li>
            <li>
              <code>ReloadPlan(ModSnapshot currentSnapshot, ModSnapshot targetSnapshot, ModDiff diff, List&lt;ScannedMod&gt; reloadable, List&lt;ScannedMod&gt; stubborn)</code>{" "}
              - returned by <code>MutoApi.plan()</code>. Check <code>hasWork()</code> before
              triggering a reload.
            </li>
          </ul>

          <h2 id="moddiff">ModDiff</h2>
          <p>
            Holds four unmodifiable sets of mod IDs: <code>added</code>, <code>removed</code>,{" "}
            <code>updated</code> (same ID, different SHA-256), and <code>unchanged</code>.{" "}
            <code>hasChanges()</code> returns <code>true</code> if any of the first three sets is
            non-empty.
          </p>

          <h2 id="example">Usage example</h2>
          <CodeSnippet
            filename="Integration.java"
            code={`import me.fy2ne.muto.api.ModDiff;
import me.fy2ne.muto.api.MutoApi;
import me.fy2ne.muto.engine.ReloadPlan;

if (!MutoApi.isAvailable() || !MutoApi.isSafeToReload()) return;

ReloadPlan plan = MutoApi.plan();
if (!plan.hasWork()) {
    LOG.info("no mod changes detected");
    return;
}

MutoApi.reloadAsync(stage -> LOG.info("[muto] {}", stage))
    .thenAccept(res -> {
        if (!res.success()) {
            LOG.warn("swap failed: {}", res.error());
            return;
        }
        ModDiff diff = res.diff();
        LOG.info("swapped in {}ms ({} updated)",
                res.durationMs(), diff.updated().size());
    });`}
          />
        </>
      );

    case "events":
      return (
        <>
          <h1>Lifecycle Events</h1>
          <p>
            <code>me.fy2ne.muto.api.MutoEvents</code> exposes Fabric <code>Event</code> instances
            for tearing down caches before a swap and rebinding state after completion.
          </p>

          <h2 id="start">RELOAD_START</h2>
          <p>
            Fires right before the pipeline mutates Knot or registries. Receives the start timestamp
            and the planned <code>ModDiff</code>:
          </p>
          <CodeSnippet
            filename="ReloadStartListener.java"
            code={`void onReloadStart(long startEpochMs, ModDiff expectedDiff);`}
          />

          <h2 id="finish">RELOAD_FINISH</h2>
          <p>
            Fires when the transaction finishes (whether it succeeded, failed, or was a no-op):
          </p>
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
    MyNetworkLayer.closeStaleChannels();
});

MutoEvents.RELOAD_FINISH.register((endMs, diff, ok, ms, err) -> {
    if (ok) {
        MyModList.refresh();
    } else {
        LOG.warn("swap failed", err);
    }
});`}
          />

          <h2 id="threading">Thread context</h2>
          <p>
            Listeners execute synchronously on the reload worker thread. Wrap render or screen calls
            in <code>Minecraft.getInstance().submit(...)</code>.
          </p>
        </>
      );

    case "testing":
      return (
        <>
          <h1>Testing &amp; CI</h1>
          <p>
            Unit tests run headless under JUnit 5 without booting a Minecraft client.
          </p>

          <h2 id="stack">Test stack</h2>
          <ul>
            <li><code>org.junit.jupiter:junit-jupiter-api:5.11.4</code></li>
            <li><code>org.junit.jupiter:junit-jupiter-engine:5.11.4</code></li>
            <li><code>org.junit.platform:junit-platform-launcher:1.11.4</code></li>
          </ul>

          <h2 id="run">Running tests</h2>
          <CodeSnippet filename="terminal" code={`./gradlew test`} />
          <p>
            HTML reports output to <code>build/reports/tests/test/index.html</code>; XML results
            output to <code>build/test-results/test/</code>.
          </p>

          <h2 id="classes">Test classes</h2>
          <ul>
            <li>
              <code>ModStateManagerTest</code> - core-mod lockouts and staged enable/disable sets.
            </li>
            <li>
              <code>ModTierClassificationTest</code> - Tier 1, 2, and 3 classification rules.
            </li>
            <li>
              <code>ReloadShellTest</code> - plan calculation and snapshot diffs without a live game.
            </li>
            <li>
              <code>api.MutoApiTest</code> - public API guards and fallback behavior.
            </li>
          </ul>

          <h2 id="ci">CI workflows</h2>
          <ul>
            <li>
              <code>.github/workflows/build.yml</code> - runs <code>./gradlew build</code> on Temurin
              JDK 25 and uploads the built jar plus failure reports.
            </li>
            <li>
              <code>.github/workflows/publish.yml</code> - publishes to GitHub Packages when{" "}
              <code>gradle.properties</code> changes on <code>26.x</code> or on release.
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
            Source is hosted at <a href={REPO}>{REPO}</a> on the <code>26.x</code> branch.
          </p>

          <h2 id="layout">Package layout</h2>
          <ul>
            <li>
              <code>me.fy2ne.muto.api</code> - <code>MutoApi</code>, <code>MutoEvents</code>,{" "}
              <code>ModDiff</code>.
            </li>
            <li>
              <code>me.fy2ne.muto.engine</code> - <code>ReloadEngine</code>, <code>ModScanner</code>,{" "}
              <code>ModSnapshot</code>, <code>RegistryFreezer</code>, <code>MutoClassLoader</code>,{" "}
              <code>ReloadPlan</code>, <code>ReloadResult</code>.
            </li>
            <li>
              <code>me.fy2ne.muto.engine.knot</code> - <code>KnotInjector</code> reflection bridge.
            </li>
            <li>
              <code>me.fy2ne.muto.engine.mixin</code> - <code>DynamicMixinEngine</code>.
            </li>
            <li>
              <code>me.fy2ne.muto.engine.resource</code> - <code>ResourceHotMount</code>.
            </li>
            <li>
              <code>me.fy2ne.muto.mixin</code> - accessors and hooks into <code>MappedRegistry</code>,
              networking payloads, keymappings, and pack repositories.
            </li>
            <li>
              <code>me.fy2ne.muto.mods</code> - <code>ModStateManager</code>, <code>DiskMod</code>,{" "}
              <code>DisabledModsRegistry</code>.
            </li>
            <li>
              <code>me.fy2ne.muto.config</code> - <code>MutoConfig</code>.
            </li>
            <li>
              <code>me.fy2ne.muto.automation</code> - <code>MutoTriggerWatcher</code>.
            </li>
          </ul>

          <h2 id="tasks">Gradle tasks</h2>
          <ul>
            <li><code>./gradlew build</code> - compile, run JUnit tests, and assemble jars.</li>
            <li><code>./gradlew jar</code> - build the runtime jar.</li>
            <li><code>./gradlew runClient</code> - launch a dev Minecraft client.</li>
            <li><code>./gradlew mutoWatch</code> - watch sources and hot-swap on save.</li>
            <li><code>./gradlew publish</code> - publish artifact to GitHub Packages.</li>
          </ul>

          <h2 id="license">License</h2>
          <p>
            Licensed under <strong>GNU Lesser General Public License v3.0</strong> (
            <code>LGPL-3.0-only</code>). Any mod (open or closed source) can depend on and call
            Muto's API; edits to Muto itself remain covered by LGPL-3.0. See{" "}
            <a href={`${REPO}/blob/26.x/LICENSE`}>LICENSE</a>.
          </p>
        </>
      );

    case "fast-refresh":
      return (
        <>
          <h1>Fast Refresh Dev Loop</h1>
          <p>
            During local development, Muto can watch for a trigger file, recompile your mod on save,
            copy the new jar into <code>run/mods</code>, and hot-swap it without closing the client.
          </p>

          <h2 id="how">Mechanism</h2>
          <p>
            <code>MutoTriggerWatcher</code> polls the run directory every 200ms for{" "}
            <code>muto.trigger</code>. When found, it deletes the trigger, runs{" "}
            <code>MutoApi.reloadAsync()</code>, and writes the outcome to <code>muto.result</code>.
            The <code>mutoWatch</code> Gradle task watches <code>src/main/java</code> and{" "}
            <code>src/main/resources</code>, builds the jar on change, drops it into{" "}
            <code>run/mods</code>, and touches <code>muto.trigger</code>.
          </p>

          <h2 id="setup">Setup</h2>
          <p>Apply the watcher script in your mod's <code>build.gradle</code>:</p>
          <CodeSnippet
            filename="build.gradle"
            code={`apply from: 'https://raw.githubusercontent.com/fy2ne/muto/26.x/muto-watcher.gradle'`}
          />
          <p>Enable the file watcher in your client entrypoint and start the Gradle loop:</p>
          <CodeSnippet filename="terminal" code={`./gradlew mutoWatch`} />
          <p>
            Control the watcher programmatically via <code>MutoApi.startFileWatcher()</code>,{" "}
            <code>MutoApi.stopFileWatcher()</code>, and <code>MutoApi.isFileWatcherRunning()</code>.
          </p>

          <h2 id="files">Trigger &amp; result files</h2>
          <ul>
            <li>
              <code>muto.trigger</code> - created by <code>mutoWatch</code> after copying a new jar;
              deleted immediately by the watcher.
            </li>
            <li>
              <code>muto.result</code> - written after each swap with <code>SUCCESS</code>,{" "}
              <code>DURATION</code>, and the added/removed/updated mod sets.
            </li>
          </ul>

          <DocAlert kind="note" label="Development only">
            <p>
              Gate <code>MutoApi.startFileWatcher()</code> behind a dev environment check (such as{" "}
              <code>FabricLoader.getInstance().isDevelopmentEnvironment()</code>) so production
              clients do not poll the disk.
            </p>
          </DocAlert>
        </>
      );

    case "mod-state":
      return (
        <>
          <h1>Mod State Management</h1>
          <p>
            <code>me.fy2ne.muto.mods.ModStateManager</code> stages enable/disable toggles in memory
            and commits them on the next reload transaction.
          </p>

          <h2 id="staging">Staging changes</h2>
          <ul>
            <li>
              <code>stageToggle(String modId)</code> / <code>toggleMod(DiskMod mod)</code> - flip a
              mod's staged state.
            </li>
            <li>
              <code>isStaged(String modId)</code>, <code>isStagedForDisable(...)</code>,{" "}
              <code>isStagedForEnable(...)</code> - query staged state.
            </li>
            <li>
              <code>getStagedDisables()</code> / <code>getStagedEnables()</code> - inspect pending ID
              sets.
            </li>
            <li>
              <code>hasStagedChanges()</code>, <code>getStagedCount()</code>,{" "}
              <code>clearStaged()</code>.
            </li>
          </ul>
          <p>
            <code>scanDiskMods()</code> lists all jars in <code>mods/</code> (including{" "}
            <code>.jar.disabled</code>, <code>.disabled</code>, and <code>.deactivation</code>) as{" "}
            <code>DiskMod</code> records.
          </p>

          <h2 id="protected">Protected mods</h2>
          <p>
            Mods matched by <code>isCore(...)</code> (<code>minecraft</code>,{" "}
            <code>fabricloader</code>, <code>fabric-api</code>, <code>java</code>, <code>muto</code>)
            cannot be staged or disabled.
          </p>

          <h2 id="applying">Applying renames</h2>
          <p>
            <code>applyFileRenames()</code> drains the staged sets and appends or strips{" "}
            <code>.disabled</code> on disk. If Windows holds an open file lock on a jar, the state
            is recorded in <code>DisabledModsRegistry</code> and the rename completes on the next
            client startup.
          </p>
        </>
      );

    case "config":
      return (
        <>
          <h1>Configuration (muto.json)</h1>
          <p>
            <code>me.fy2ne.muto.config.MutoConfig</code> persists settings to{" "}
            <code>config/muto.json</code> via Gson.
          </p>

          <h2 id="file">Config file</h2>
          <CodeSnippet
            filename="config/muto.json"
            code={`{
  "autoReloadResources": true
}`}
          />

          <h2 id="flags">Options</h2>
          <ul>
            <li>
              <code>autoReloadResources</code> (default: <code>true</code>): calls{" "}
              <code>ResourceHotMount.remountAndReload()</code> at the end of a swap so new assets,
              lang files, textures, and shaders mount immediately.
            </li>
          </ul>

          <h2 id="notes">Loading behavior</h2>
          <p>
            <code>MutoConfig.get()</code> reads <code>config/muto.json</code> on first access and
            writes the default file if none exists.
          </p>
        </>
      );

    case "file-locks":
      return (
        <>
          <h1>Windows File Locking</h1>

          <h2 id="problem">NTFS file locks</h2>
          <p>
            On Windows, the JVM keeps an active file lock on any jar opened by{" "}
            <code>Knot</code>'s classloader. Overwriting or renaming that jar in place throws an{" "}
            <code>AccessDeniedException</code> / sharing violation.
          </p>

          <h2 id="handling">Deferred rename strategy</h2>
          <ul>
            <li>
              Updated jars are written under a new filename; Knot loads the new archive while the
              old handle stays untouched.
            </li>
            <li>
              When <code>ModStateManager.applyFileRenames()</code> hits a locked jar, it persists
              the disabled state in <code>DisabledModsRegistry</code> instead of aborting the reload.
            </li>
            <li>
              On the next boot, before Knot opens mod jars, Muto reconciles pending renames.
            </li>
          </ul>
        </>
      );

    case "jvm-tuning":
      return (
        <>
          <h1>JVM &amp; JetBrains Runtime</h1>

          <h2 id="metaspace">Metaspace growth</h2>
          <p>
            Each reload defines new classes inside Knot and allocates metaspace. Reclaiming old
            metadata requires class unloading during concurrent GC cycles.
          </p>

          <h2 id="heap">Heap sizing</h2>
          <p>
            For heavy dev sessions with frequent reloads, reserve metaspace headroom above your
            baseline client heap <Math tex="H_{base}" />. With <Math tex="N" /> reloads between full
            class-unloading sweeps and average per-reload footprint <Math tex="S_{class}" />:
          </p>
          <Math block tex="H_{jvm} \;\ge\; H_{base} + k \cdot N \cdot S_{class}" />
          <p>
            where <Math tex="k \approx 2" /> covers transient allocation spikes during initializer
            execution.
          </p>

          <h2 id="flags">JVM flags</h2>
          <CodeSnippet
            filename="jvm-args"
            code={`-Xmx4G
-XX:+UseG1GC
-XX:+ClassUnloadingWithConcurrentMark
-XX:MetaspaceSize=256M
-XX:MaxMetaspaceSize=1G`}
          />
          <DocAlert kind="note" label="JDK selection">
            <p>
              Muto runs on any Java 25+ JDK (Temurin, Oracle, GraalVM). Running on JetBrains Runtime
              (JBR) with <code>-XX:+AllowEnhancedClassRedefinition</code> is optional.
            </p>
          </DocAlert>
        </>
      );

    case "arch-rotation":
      return (
        <>
          <h1>Knot Integration &amp; Reload</h1>

          <h2 id="knot">Knot classpath injection</h2>
          <p>
            Fabric Loader initializes its <code>Knot</code> classloader once at startup.{" "}
            <code>me.fy2ne.muto.engine.knot.KnotInjector</code> uses reflection to append a new or
            updated jar (and its nested Jar-in-Jar dependencies) directly to Knot's URL classpath so
            new classes resolve through the active loader.
          </p>

          <h2 id="transformer">Dynamic mixin registration</h2>
          <p>
            <code>me.fy2ne.muto.engine.mixin.DynamicMixinEngine</code> registers a new jar's mixin
            configs with the global Mixin service via <code>Mixins.addConfiguration</code>. Those
            mixins apply to any target class loaded by the JVM after the swap.
          </p>

          <h2 id="consequence">Already-loaded classes</h2>
          <DocAlert kind="warning" label="Mixin weaving limit">
            <p>
              Standard JVMs cannot re-run Mixin bytecode transformers on classes that are already
              defined. Mods whose mixins target already-loaded core classes are marked{" "}
              <code>STUBBORN</code> (Tier 3) and require a full restart.
            </p>
          </DocAlert>
        </>
      );

    case "registry-thawing":
      return (
        <>
          <h1>Registry Thawing Mechanics</h1>
          <p>
            Minecraft freezes every <code>MappedRegistry</code> at the end of bootstrap. Registering
            new blocks, items, or payloads at runtime requires clearing that flag for the duration
            of the reload.
          </p>

          <h2 id="freeze">Unfreeze &amp; refreeze</h2>
          <p>
            <code>me.fy2ne.muto.engine.RegistryFreezer</code> iterates{" "}
            <code>BuiltInRegistries.REGISTRY</code>. It clears the <code>frozen</code> boolean via{" "}
            <code>MappedRegistryAccessor</code> (with a reflective fallback) in{" "}
            <code>unfreezeAll()</code> before mod entrypoints run, and restores it in{" "}
            <code>freezeAll()</code> inside a <code>finally</code> block.
          </p>

          <h2 id="prune">Namespace pruning</h2>
          <p>
            Before re-running a mod's initializers, <code>pruneNamespace(String namespace)</code>{" "}
            removes prior entries under that mod's namespace from <code>byLocation</code>,{" "}
            <code>byKey</code>, <code>byValue</code>, <code>registrationInfos</code>,{" "}
            <code>byId</code>, and <code>toId</code>, then compacts trailing nulls in the ID table so
            re-registration does not collide.
          </p>

          <h2 id="caveat">Internal field mapping</h2>
          <DocAlert kind="warning" label="Mojang internal fields">
            <p>
              <code>pruneNamespace</code> targets internal <code>MappedRegistry</code> collections
              on Minecraft 26.x. If a field layout changes in a future snapshot, the freezer logs a
              warning and skips reflective pruning rather than crashing the client.
            </p>
          </DocAlert>
        </>
      );

    case "atomic-rollback":
      return (
        <>
          <h1>Atomic Rollback Guarantee</h1>

          <h2 id="guarantee">Rollback invariants</h2>
          <p>
            If an entrypoint or mixin registration throws during a reload, the engine unwinds cleanly
            so the client remains playable on the previous snapshot:
          </p>
          <ul>
            <li>
              <code>RegistryFreezer.freezeAll()</code> always runs in a <code>finally</code> block.
            </li>
            <li>
              The baseline <code>ModSnapshot</code> is only replaced after all initializers succeed.
            </li>
            <li>
              Every run emits a <code>ReloadResult</code> and appends a{" "}
              <code>ReloadHistoryEntry</code> for inspection.
            </li>
          </ul>

          <h2 id="results">Handling results</h2>
          <CodeSnippet
            filename="ResultHandling.java"
            code={`MutoApi.reloadAsync().thenAccept(res -> {
    if (res.success()) {
        LOG.info("reload ok in {}ms", res.durationMs());
    } else {
        LOG.warn("reload aborted, previous snapshot active: {}", res.error());
    }
});`}
          />
        </>
      );

    default:
      return null;
  }
}

export default function App() {
  const [cur, setCur] = useState("overview");
  const [menu, setMenu] = useState(false);
  const [q, setQ] = useState("");
  const [activeSec, setActiveSec] = useState(null);
  const [fbOpen, setFbOpen] = useState(false);
  const [footFb, setFootFb] = useState(false);
  const scrollRef = useRef(null);
  const fbWrapRef = useRef(null);

  const page = articles[cur];

  const flat = useMemo(
    () =>
      nav.flatMap((g) =>
        g.items.map((it) => ({ ...it, group: g.title }))
      ),
    []
  );

  const hits = useMemo(() => {
    const term = q.trim().toLowerCase();
    if (!term) return null;
    return flat.filter(
      (it) =>
        it.title.toLowerCase().includes(term) ||
        it.short.toLowerCase().includes(term) ||
        it.group.toLowerCase().includes(term)
    );
  }, [q, flat]);

  const idx = flat.findIndex((p) => p.id === cur);
  const prev = idx > 0 ? flat[idx - 1] : null;
  const next = idx < flat.length - 1 ? flat[idx + 1] : null;

  const go = (id) => {
    setCur(id);
    setQ("");
    setActiveSec(null);
    setMenu(false);
    scrollRef.current?.scrollTo({ top: 0 });
  };

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: 0 });
  }, [cur]);

  useEffect(() => {
    if (!fbOpen) return;
    const onDocClick = (e) => {
      if (fbWrapRef.current && !fbWrapRef.current.contains(e.target)) {
        setFbOpen(false);
      }
    };
    document.addEventListener("mousedown", onDocClick);
    return () => document.removeEventListener("mousedown", onDocClick);
  }, [fbOpen]);

  const onScroll = () => {
    const box = scrollRef.current;
    if (!box) return;
    const top = box.scrollTop + 120;
    let found = null;
    for (const sec of page.sections) {
      const el = document.getElementById(sec.id);
      if (el && el.offsetTop <= top) found = sec.id;
    }
    setActiveSec(found);
  };

  return (
    <div className="docs-shell">
      <header className="docs-header">
        <div className="docs-header-left">
          <button
            className="menu-btn"
            onClick={() => setMenu((m) => !m)}
            aria-label="Toggle navigation"
            type="button"
          >
            {menu ? <X size={17} /> : <MenuIcon size={17} />}
          </button>
          <span className="brand" onClick={() => go("overview")}>
            <img src="/icon.png" alt="Muto Runtime" className="brand-logo" />
            <span className="brand-text">Muto Runtime</span>
            <span className="brand-badge">Beta</span>
          </span>
          <span className="brand-version">{VERSION}</span>
        </div>
        <div className="docs-header-right">
          <div className="fb-anchor" ref={fbWrapRef}>
            <button
              type="button"
              className={`fb-trigger-btn ${fbOpen ? "active" : ""}`}
              onClick={() => setFbOpen((v) => !v)}
            >
              <MessageSquare size={13} />
              <span>Feedback</span>
            </button>
            {fbOpen && (
              <FeedbackBox
                page={cur}
                onClose={() => setFbOpen(false)}
              />
            )}
          </div>
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

      <aside className={`sidebar ${menu ? "open" : ""}`}>
        <div className="sb-scroll">
          <div className="sb-search-card">
            <div className="sb-search-input">
              <Search size={14} />
              <input
                value={q}
                onChange={(e) => setQ(e.target.value)}
                placeholder="Search docs"
                aria-label="Search docs"
              />
              {q && (
                <button
                  className="sb-search-clear"
                  onClick={() => setQ("")}
                  aria-label="Clear search"
                  type="button"
                >
                  <X size={13} />
                </button>
              )}
            </div>
          </div>

          {hits ? (
            <nav className="sb-nav">
              <div className="sb-section">
                <div className="sb-section-head">
                  {hits.length} match{hits.length === 1 ? "" : "es"}
                </div>
                <div className="sb-section-body">
                  {hits.map((it) => (
                    <div
                      key={it.id}
                      className={`sb-link ${cur === it.id ? "active" : ""}`}
                      onClick={() => go(it.id)}
                    >
                      <span className="sb-link-icon">
                        <it.icon size={15} />
                      </span>
                      <span className="sb-link-label">{it.title}</span>
                    </div>
                  ))}
                </div>
              </div>
            </nav>
          ) : (
            <nav className="sb-nav">
              {nav.map((g) => (
                <div className="sb-section" key={g.title}>
                  <div className="sb-section-head">{g.title}</div>
                  <div className="sb-section-body">
                    {g.items.map((it) => (
                      <div
                        key={it.id}
                        className={`sb-link ${cur === it.id ? "active" : ""}`}
                        onClick={() => go(it.id)}
                      >
                        <span className="sb-link-icon">
                          <it.icon size={15} />
                        </span>
                        <span className="sb-link-label">{it.title}</span>
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
            <span>{page.category}</span>
            <span className="sep">/</span>
            <span className="current">{page.title}</span>
          </div>

          <div className="md">
            <ArticleContent id={cur} />
          </div>

          <nav className="pager" aria-label="Page navigation">
            {prev ? (
              <button className="pager-card prev" onClick={() => go(prev.id)} type="button">
                <span className="pager-dir"><ArrowLeft size={12} /> Previous</span>
                <span className="pager-title">{prev.title}</span>
              </button>
            ) : <span />}
            {next ? (
              <button className="pager-card next" onClick={() => go(next.id)} type="button">
                <span className="pager-dir">Next <ArrowRight size={12} /></span>
                <span className="pager-title">{next.title}</span>
              </button>
            ) : <span />}
          </nav>

          {footFb && (
            <div className="fb-foot-wrap">
              <FeedbackBox
                page={cur}
                compact
                onClose={() => setFootFb(false)}
              />
            </div>
          )}

          <footer className="page-foot">
            <div className="page-foot-links">
              <a href={`${REPO}/edit/26.x/web/src/App.jsx`} target="_blank" rel="noreferrer">
                Edit this page
              </a>
              <span className="sep">·</span>
              <button
                type="button"
                className="page-foot-btn"
                onClick={() => setFootFb((v) => !v)}
              >
                {footFb ? "Close feedback" : "Send feedback"}
              </button>
              <span className="sep">·</span>
              <a href="mailto:hello@fy2ne.me">hello@fy2ne.me</a>
            </div>
            <span>{VERSION} · 26.x</span>
          </footer>
        </div>

        <aside className="toc">
          <div className="toc-current-page">{page.title}</div>
          <div className="toc-sections">
            {page.sections.map((sec) => (
              <a
                key={sec.id}
                className={`toc-anchor ${activeSec === sec.id ? "active" : ""}`}
                onClick={() =>
                  document
                    .getElementById(sec.id)
                    ?.scrollIntoView({ behavior: "smooth", block: "start" })
                }
              >
                {sec.label}
              </a>
            ))}
          </div>
        </aside>
      </main>
    </div>
  );
}
