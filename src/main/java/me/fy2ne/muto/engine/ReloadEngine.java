package me.fy2ne.muto.engine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.api.ModDiff;
import me.fy2ne.muto.api.MutoEvents;
import me.fy2ne.muto.engine.knot.KnotInjector;
import me.fy2ne.muto.engine.mixin.DynamicMixinEngine;
import me.fy2ne.muto.engine.resource.ResourceHotMount;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Closeable;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class ReloadEngine {
    public static final ReloadEngine INSTANCE = new ReloadEngine();

    private static final int MAX_HISTORY = 50;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type HISTORY_LIST_TYPE = new TypeToken<ArrayList<ReloadHistoryEntry>>() {}.getType();

    private final AtomicBoolean reloading = new AtomicBoolean(false);
    private volatile ModSnapshot currentSnapshot = ModSnapshot.empty();
    private volatile boolean autoReloadResources = true;
    private final Map<String, List<Object>> instancesByMod = new LinkedHashMap<>();

    private static final List<ReloadHistoryEntry> HISTORY = new CopyOnWriteArrayList<>();
    private static boolean historyLoaded = false;

    private ReloadEngine() {}

    public boolean isReloading() {
        return reloading.get();
    }

    public boolean isAutoReloadResources() {
        return autoReloadResources;
    }

    public void setAutoReloadResources(boolean enabled) {
        this.autoReloadResources = enabled;
    }

    public ModSnapshot currentSnapshot() {
        return currentSnapshot;
    }

    public void recordInitialSnapshot(Path modsDir) {
        ensureHistoryLoaded();
        List<ScannedMod> scanned = ModScanner.scan(modsDir);
        currentSnapshot = new ModSnapshot(scanned);
        MutoLog.info("baseline snapshot: {} mods ({} reloadable, {} stubborn)",
                currentSnapshot.mods().size(),
                currentSnapshot.reloadableMods().size(),
                currentSnapshot.stubbornMods().size());
    }

    public ClassLoader currentLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public ReloadPlan plan(Path modsDir) {
        List<ScannedMod> scanned = ModScanner.scan(modsDir);
        ModSnapshot target = new ModSnapshot(scanned);
        ModDiff diff = currentSnapshot.diffAgainst(target);
        return new ReloadPlan(
                currentSnapshot,
                target,
                diff,
                target.reloadableMods(),
                target.stubbornMods()
        );
    }

    public CompletableFuture<ReloadResult> reloadAsync(Path modsDir, Consumer<String> stageNotifier) {
        return CompletableFuture.supplyAsync(() -> execute(plan(modsDir), stageNotifier));
    }

    public List<ReloadHistoryEntry> history() {
        ensureHistoryLoaded();
        return Collections.unmodifiableList(HISTORY);
    }

    private static synchronized void ensureHistoryLoaded() {
        if (historyLoaded) return;
        historyLoaded = true;
        try {
            Path file = resolveHistoryPath();
            if (file != null && Files.exists(file)) {
                try (Reader reader = Files.newBufferedReader(file)) {
                    List<ReloadHistoryEntry> saved = GSON.fromJson(reader, HISTORY_LIST_TYPE);
                    if (saved != null) {
                        HISTORY.addAll(saved);
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    private static synchronized void appendHistory(ReloadHistoryEntry entry) {
        ensureHistoryLoaded();
        HISTORY.add(entry);
        while (HISTORY.size() > MAX_HISTORY) {
            HISTORY.removeFirst();
        }
        try {
            Path file = resolveHistoryPath();
            if (file == null) return;
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            Path tmp = file.resolveSibling("history.json.tmp");
            try (Writer writer = Files.newBufferedWriter(tmp)) {
                GSON.toJson(new ArrayList<>(HISTORY), HISTORY_LIST_TYPE, writer);
            }
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (Throwable ignored) {}
    }

    private static Path resolveHistoryPath() {
        try {
            FabricLoader fl = FabricLoader.getInstance();
            if (fl != null && fl.getConfigDir() != null) {
                return fl.getConfigDir().resolve("muto").resolve("history.json");
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public synchronized ReloadResult execute(ReloadPlan plan, Consumer<String> stageNotifier) {
        if (!reloading.compareAndSet(false, true)) {
            MutoLog.warn("reload already in progress, rejecting request");
            return ReloadResult.fail(plan.diff(), 0L, "reload in progress");
        }

        long startMs = System.currentTimeMillis();
        MutoEvents.RELOAD_START.invoker().onReloadStart(startMs, plan.diff());
        List<String> stepLogs = new ArrayList<>();

        try {
            ModDiff diff = plan.diff();

            if (!plan.hasWork()) {
                notify(stageNotifier, stepLogs, "Scanning /mods: all mod files are up to date.");
                notify(stageNotifier, stepLogs, "No mod changes detected — reload is a no-op.");
                long noopMs = System.currentTimeMillis() - startMs;
                MutoLog.info("no mod changes detected, reload is a no-op ({}ms)", noopMs);
                MutoEvents.RELOAD_FINISH.invoker().onReloadFinish(
                        System.currentTimeMillis(), diff, true, noopMs, null);
                appendHistory(new ReloadHistoryEntry(startMs, noopMs, diff, true, stepLogs));
                return ReloadResult.ok(diff, noopMs);
            }

            notify(stageNotifier, stepLogs, String.format("Scanning /mods: %d active mods detected in directory.", plan.targetSnapshot().mods().size()));

            if (!diff.added().isEmpty()) {
                notify(stageNotifier, stepLogs, String.format("Detected +%d new mod(s): %s", diff.added().size(), String.join(", ", diff.added())));
            }
            if (!diff.removed().isEmpty()) {
                notify(stageNotifier, stepLogs, String.format("Detected -%d removed mod(s): %s", diff.removed().size(), String.join(", ", diff.removed())));
            }
            if (!diff.updated().isEmpty()) {
                notify(stageNotifier, stepLogs, String.format("Detected ~%d modified mod(s): %s", diff.updated().size(), String.join(", ", diff.updated())));
            }

            List<ScannedMod> initializable = new ArrayList<>();
            for (ScannedMod mod : plan.reloadable()) {
                if (diff.added().contains(mod.id()) || diff.updated().contains(mod.id())) {
                    initializable.add(mod);
                }
            }

            ClassLoader knotCl = KnotInjector.getKnotClassLoader();
            ClassLoader prevCl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(knotCl);

            Set<String> affectedNamespaces = new LinkedHashSet<>();
            affectedNamespaces.addAll(diff.added());
            affectedNamespaces.addAll(diff.removed());
            affectedNamespaces.addAll(diff.updated());

            Map<String, List<Object>> backupInstances = new LinkedHashMap<>();
            for (String ns : affectedNamespaces) {
                List<Object> existing = instancesByMod.get(ns);
                if (existing != null) {
                    backupInstances.put(ns, new ArrayList<>(existing));
                }
            }
            var registryBackup = RegistryFreezer.snapshotNamespaces(affectedNamespaces);

            try {
                List<Path> allInjectedPaths = new ArrayList<>();
                if (!initializable.isEmpty()) {
                    notify(stageNotifier, stepLogs, "Injecting mod jars into KnotClassLoader...");
                    for (ScannedMod m : initializable) {
                        List<Path> injected = KnotInjector.injectWithNested(m.jarPath());
                        allInjectedPaths.addAll(injected);
                        notify(stageNotifier, stepLogs, String.format("Knot classpath registered: %s (%d jars)", m.id(), injected.size()));
                    }
                }

                for (ScannedMod mod : initializable) {
                    preflightCheck(knotCl, mod);
                }

                if (!allInjectedPaths.isEmpty()) {
                    notify(stageNotifier, stepLogs, "Registering dynamic mixin configurations...");
                    for (Path p : allInjectedPaths) {
                        int registered = DynamicMixinEngine.registerDynamicMixins(p);
                        if (registered > 0) {
                            notify(stageNotifier, stepLogs, String.format("Registered %d mixin config(s) for %s", registered, p.getFileName()));
                        }
                    }
                }

                if (!diff.removed().isEmpty()) {
                    notify(stageNotifier, stepLogs, "Tearing down removed mod contexts...");
                    for (String rem : diff.removed()) {
                        teardownMod(rem);
                        RegistryFreezer.pruneNamespace(rem);
                        notify(stageNotifier, stepLogs, String.format("Unloaded mod context: %s", rem));
                    }
                }
                for (String upd : diff.updated()) {
                    teardownMod(upd);
                    RegistryFreezer.pruneNamespace(upd);
                    notify(stageNotifier, stepLogs, String.format("Refreshed mod context: %s", upd));
                }

                notify(stageNotifier, stepLogs, "Unfreezing registries & invoking entrypoints in Knot...");
                RegistryFreezer.unfreezeAll();

                for (ScannedMod mod : initializable) {
                    notify(stageNotifier, stepLogs, String.format("Invoking entrypoints for %s...", mod.id()));
                    initializeMod(mod);
                }

                notify(stageNotifier, stepLogs, "Finalizing engine state & freezing registries...");
                RegistryFreezer.freezeAll();

                syncKeyMappings();

                notify(stageNotifier, stepLogs, "Synchronizing Fabric Loader mod metadata...");
                syncFabricLoader(plan.targetSnapshot(), diff);

                if (autoReloadResources && (!initializable.isEmpty() || !diff.removed().isEmpty())) {
                    notify(stageNotifier, stepLogs, "Mounting resources & reloading OpenGL shaders...");
                    try {
                        ResourceHotMount.remountAndReload().get(15, java.util.concurrent.TimeUnit.SECONDS);
                    } catch (Throwable t) {
                        MutoLog.warn("resource hot mount wait skipped: {}", t.getMessage());
                    }
                }

                currentSnapshot = plan.targetSnapshot();
                long durationMs = System.currentTimeMillis() - startMs;
                String finishMsg = String.format("Reload finished cleanly in %dms (+%d -%d ~%d)",
                        durationMs, diff.added().size(), diff.removed().size(), diff.updated().size());
                notify(stageNotifier, stepLogs, finishMsg);
                MutoLog.info(finishMsg);

                MutoEvents.RELOAD_FINISH.invoker().onReloadFinish(
                        System.currentTimeMillis(),
                        diff,
                        true,
                        durationMs,
                        null
                );

                appendHistory(new ReloadHistoryEntry(startMs, durationMs, diff, true, stepLogs));
                return ReloadResult.ok(diff, durationMs);

            } catch (Throwable err) {
                long durationMs = System.currentTimeMillis() - startMs;
                String failMsg = "Reload failed (rolled back): " + err.getMessage();
                notify(stageNotifier, stepLogs, failMsg);
                MutoLog.error("reload failed, executing transaction rollback: {}", err.getMessage(), err);

                // roll back newly created instances and restore prior state
                for (ScannedMod m : initializable) {
                    teardownMod(m.id());
                }
                instancesByMod.putAll(backupInstances);
                RegistryFreezer.restoreSnapshot(affectedNamespaces, registryBackup);
                RegistryFreezer.freezeAll();

                MutoEvents.RELOAD_FINISH.invoker().onReloadFinish(
                        System.currentTimeMillis(),
                        diff,
                        false,
                        durationMs,
                        err
                );

                appendHistory(new ReloadHistoryEntry(startMs, durationMs, diff, false, stepLogs));
                return ReloadResult.fail(diff, durationMs, err.getMessage());
            } finally {
                Thread.currentThread().setContextClassLoader(prevCl);
            }
        } finally {
            reloading.set(false);
        }
    }

    private void preflightCheck(ClassLoader loader, ScannedMod mod) throws ClassNotFoundException {
        for (String epClass : mod.getClassesFor("main")) {
            Class.forName(epClass, false, loader);
        }
        for (String epClass : mod.getClassesFor("client")) {
            Class.forName(epClass, false, loader);
        }
    }

    private void teardownMod(String modId) {
        List<Object> instances = instancesByMod.remove(modId);
        if (instances != null) {
            for (Object inst : instances) {
                closeQuietly(inst);
            }
        }
        teardownNetworking(modId);
    }

    private void teardownNetworking(String modId) {
    }

    private void closeQuietly(Object inst) {
        try {
            if (inst instanceof Closeable c) {
                c.close();
            } else if (inst instanceof AutoCloseable ac) {
                ac.close();
            }
        } catch (Exception ex) {
            MutoLog.warn("error closing mod instance {}: {}", inst.getClass().getName(), ex.getMessage());
        }
    }

    private void initializeMod(ScannedMod mod) throws Exception {
        List<Object> created = new ArrayList<>();
        ClassLoader knotCl = Thread.currentThread().getContextClassLoader();

        try {
            for (String clsName : mod.getClassesFor("main")) {
                Class<?> cls = Class.forName(clsName, true, knotCl);
                Object obj = cls.getDeclaredConstructor().newInstance();
                created.add(obj);

                if (obj instanceof ModInitializer init) {
                    init.onInitialize();
                    MutoLog.info("initialized main in knot: {}", clsName);
                }
            }

            for (String clsName : mod.getClassesFor("client")) {
                Class<?> cls = Class.forName(clsName, true, knotCl);
                Object obj = cls.getDeclaredConstructor().newInstance();
                created.add(obj);

                if (obj instanceof ClientModInitializer clientInit) {
                    net.minecraft.client.Minecraft mc = null;
                    try {
                        mc = net.minecraft.client.Minecraft.getInstance();
                    } catch (Throwable ignored) {}
                    if (mc != null && !mc.isSameThread()) {
                        mc.submit(clientInit::onInitializeClient).get();
                    } else {
                        clientInit.onInitializeClient();
                    }
                    MutoLog.info("initialized client in knot: {}", clsName);
                }
            }
        } catch (Throwable ex) {
            for (Object inst : created) {
                closeQuietly(inst);
            }
            MutoLog.error("entrypoint failure in {}: {}", mod.id(), ex.getMessage(), ex);
            if (ex instanceof Exception e) throw e;
            throw new RuntimeException(ex);
        }

        if (!created.isEmpty()) {
            instancesByMod.computeIfAbsent(mod.id(), k -> new ArrayList<>()).addAll(created);
        }
    }

    @SuppressWarnings("unchecked")
    private void syncFabricLoader(ModSnapshot snapshot, ModDiff diff) {
        try {
            FabricLoader fl = FabricLoader.getInstance();
            Field modMapField = null;
            Field modsListField = null;
            Class<?> cur = fl.getClass();
            while (cur != null && (modMapField == null || modsListField == null)) {
                try {
                    if (modMapField == null) modMapField = cur.getDeclaredField("modMap");
                } catch (NoSuchFieldException ignored) {}
                try {
                    if (modsListField == null) modsListField = cur.getDeclaredField("mods");
                } catch (NoSuchFieldException ignored) {}
                cur = cur.getSuperclass();
            }

            if (modMapField != null) {
                modMapField.setAccessible(true);
                Map<String, Object> map = (Map<String, Object>) modMapField.get(fl);
                List<Object> list = null;
                if (modsListField != null) {
                    modsListField.setAccessible(true);
                    list = (List<Object>) modsListField.get(fl);
                }

                if (diff != null) {
                    for (String removedId : diff.removed()) {
                        if (map != null) map.remove(removedId);
                        if (list != null) {
                            list.removeIf(item -> {
                                if (item instanceof net.fabricmc.loader.api.ModContainer mc) {
                                    return mc.getMetadata().getId().equals(removedId);
                                }
                                return false;
                            });
                        }
                    }

                    for (String addedId : diff.added()) {
                        ScannedMod mod = snapshot.mods().get(addedId);
                        if (mod == null) continue;
                        try {
                            Object container = createContainer(mod);
                            if (container != null) {
                                if (map != null) map.put(mod.id(), container);
                                if (list != null && !list.contains(container)) list.add(container);
                                MutoLog.info("synced {} into FabricLoader", mod.id());
                            }
                        } catch (Throwable ex) {
                            MutoLog.warn("failed registering container for {}: {}", mod.id(), ex.getMessage());
                        }
                    }
                }

                MutoLog.info("fabric loader modMap synced (tracked mods: {})", snapshot.mods().size());
            }
        } catch (Throwable t) {
            MutoLog.warn("fabric loader modMap sync skipped: {}", t.getMessage());
        }
    }

    private Object createContainer(ScannedMod mod) {
        try {
            Optional<net.fabricmc.loader.api.ModContainer> existing = FabricLoader.getInstance().getModContainer(mod.id());
            if (existing.isPresent()) return existing.get();

            try (java.util.jar.JarFile jf = new java.util.jar.JarFile(mod.jarPath().toFile())) {
                java.util.jar.JarEntry entry = jf.getJarEntry("fabric.mod.json");
                if (entry != null) {
                    try (java.io.InputStream is = jf.getInputStream(entry)) {
                        Class<?> parserCls = Class.forName("net.fabricmc.loader.impl.metadata.ModMetadataParser");
                        java.lang.reflect.Method parseMethod = null;
                        for (java.lang.reflect.Method m : parserCls.getMethods()) {
                            if (m.getName().equals("parseMetadata")) {
                                parseMethod = m;
                                break;
                            }
                        }
                        if (parseMethod != null) {
                            Object vOverrides = null;
                            try {
                                Class<?> voCls = Class.forName("net.fabricmc.loader.impl.metadata.VersionOverrides");
                                vOverrides = voCls.getDeclaredConstructor().newInstance();
                            } catch (Throwable ignored) {}

                            Object dOverrides = null;
                            try {
                                Class<?> doCls = Class.forName("net.fabricmc.loader.impl.metadata.DependencyOverrides");
                                dOverrides = doCls.getDeclaredConstructor(java.nio.file.Path.class).newInstance(FabricLoader.getInstance().getConfigDir());
                            } catch (Throwable ignored) {}

                            Class<?>[] pTypes = parseMethod.getParameterTypes();
                            Object[] args = new Object[pTypes.length];
                            for (int i = 0; i < pTypes.length; i++) {
                                Class<?> pt = pTypes[i];
                                if (java.io.InputStream.class.isAssignableFrom(pt)) {
                                    args[i] = is;
                                } else if (String.class.isAssignableFrom(pt)) {
                                    args[i] = mod.jarPath().toString();
                                } else if (List.class.isAssignableFrom(pt)) {
                                    args[i] = List.of();
                                } else if (pt.getName().contains("VersionOverrides")) {
                                    args[i] = vOverrides;
                                } else if (pt.getName().contains("DependencyOverrides")) {
                                    args[i] = dOverrides;
                                } else if (pt == boolean.class) {
                                    args[i] = Boolean.FALSE;
                                } else if (pt == int.class) {
                                    args[i] = 0;
                                } else if (pt.isPrimitive()) {
                                    args[i] = 0;
                                } else {
                                    args[i] = null;
                                }
                            }
                            Object meta = parseMethod.invoke(null, args);

                            Class<?> candidateCls = Class.forName("net.fabricmc.loader.impl.discovery.ModCandidateImpl");
                            java.lang.reflect.Method createPlain = null;
                            for (java.lang.reflect.Method m : candidateCls.getDeclaredMethods()) {
                                if (m.getName().equals("createPlain")) {
                                    createPlain = m;
                                    break;
                                }
                            }
                            if (createPlain != null) {
                                createPlain.setAccessible(true);
                                Class<?>[] cpTypes = createPlain.getParameterTypes();
                                Object[] cArgs = new Object[cpTypes.length];
                                for (int i = 0; i < cpTypes.length; i++) {
                                    Class<?> pt = cpTypes[i];
                                    if (List.class.isAssignableFrom(pt)) {
                                        cArgs[i] = List.of(mod.jarPath());
                                    } else if (Collection.class.isAssignableFrom(pt)) {
                                        cArgs[i] = List.of();
                                    } else if (pt.isInstance(meta) || pt.getName().contains("Metadata")) {
                                        cArgs[i] = meta;
                                    } else if (pt == boolean.class) {
                                        cArgs[i] = Boolean.FALSE;
                                    } else if (pt == int.class) {
                                        cArgs[i] = 0;
                                    } else if (pt.isPrimitive()) {
                                        cArgs[i] = 0;
                                    } else {
                                        cArgs[i] = null;
                                    }
                                }
                                Object candidate = createPlain.invoke(null, cArgs);

                                Class<?> containerCls = Class.forName("net.fabricmc.loader.impl.ModContainerImpl");
                                java.lang.reflect.Constructor<?> ctor = containerCls.getDeclaredConstructor(candidateCls);
                                ctor.setAccessible(true);
                                return ctor.newInstance(candidate);
                            }
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            Throwable root = ex.getCause() != null ? ex.getCause() : ex;
            MutoLog.warn("reflection ModContainer creation error for {}: {}, using dynamic proxy", mod.id(), root.toString());
        }

        return createFallbackProxyContainer(mod);
    }

    private Object createFallbackProxyContainer(ScannedMod mod) {
        try {
            ClassLoader cl = getClass().getClassLoader();
            Class<?> modContainerInterface = net.fabricmc.loader.api.ModContainer.class;
            Class<?> modMetadataInterface = net.fabricmc.loader.api.metadata.ModMetadata.class;

            Object metaProxy = java.lang.reflect.Proxy.newProxyInstance(cl, new Class<?>[]{modMetadataInterface}, (proxy, method, args) -> {
                String name = method.getName();
                Class<?> ret = method.getReturnType();
                if ("getId".equals(name) || "getName".equals(name)) return mod.id();
                if ("getVersion".equals(name)) {
                    return net.fabricmc.loader.api.Version.parse(mod.version().isBlank() ? "1.0.0" : mod.version());
                }
                if ("getType".equals(name)) return "fabric";
                if ("getDescription".equals(name)) return "Dynamically loaded by Muto";
                if ("getAuthors".equals(name) || "getContributors".equals(name) || "getLicense".equals(name)) {
                    return List.of();
                }
                if ("getContact".equals(name)) return net.fabricmc.loader.api.metadata.ContactInformation.EMPTY;
                if ("getProvides".equals(name) || "getDependencies".equals(name)) return List.of();
                if ("getCustomValues".equals(name)) return Map.of();
                if ("getEnvironment".equals(name)) return net.fabricmc.api.EnvType.CLIENT;
                if ("loadsInEnvironment".equals(name)) return Boolean.TRUE;
                if ("isBuiltin".equals(name)) return Boolean.FALSE;
                if ("containsCustomValue".equals(name)) return Boolean.FALSE;
                if ("getIcons".equals(name) || "getIconPath".equals(name)) return Optional.empty();
                if ("toString".equals(name)) return mod.id() + " " + mod.version();

                if (ret == boolean.class) return Boolean.FALSE;
                if (ret == int.class) return 0;
                if (ret == long.class) return 0L;
                if (ret == float.class) return 0.0f;
                if (ret == double.class) return 0.0d;
                if (List.class.isAssignableFrom(ret) || Collection.class.isAssignableFrom(ret)) return List.of();
                if (Set.class.isAssignableFrom(ret)) return Set.of();
                if (Map.class.isAssignableFrom(ret)) return Map.of();
                if (Optional.class.isAssignableFrom(ret)) return Optional.empty();
                return null;
            });

            Class<?> modOriginInterface = net.fabricmc.loader.api.metadata.ModOrigin.class;
            Object originProxy = java.lang.reflect.Proxy.newProxyInstance(cl, new Class<?>[]{modOriginInterface}, (proxy, method, mArgs) -> {
                String mName = method.getName();
                Class<?> ret = method.getReturnType();
                if ("getPaths".equals(mName)) return List.of(mod.jarPath());
                if ("getKind".equals(mName)) {
                    for (Object c : ret.getEnumConstants()) {
                        if ("PATH".equals(c.toString())) return c;
                    }
                }
                if (ret == boolean.class) return Boolean.FALSE;
                if (ret == int.class) return 0;
                if (List.class.isAssignableFrom(ret)) return List.of();
                return null;
            });

            return java.lang.reflect.Proxy.newProxyInstance(cl, new Class<?>[]{modContainerInterface}, (proxy, method, args) -> {
                String name = method.getName();
                Class<?> ret = method.getReturnType();
                if ("getMetadata".equals(name)) return metaProxy;
                if ("getRootPaths".equals(name)) return List.of(mod.jarPath());
                if ("getRootPath".equals(name)) return mod.jarPath();
                if ("getOrigin".equals(name)) return originProxy;
                if ("getContainingMod".equals(name)) return Optional.empty();
                if ("getContainedMods".equals(name)) return List.of();
                if ("findPath".equals(name) || "getPath".equals(name)) return Optional.empty();
                if ("toString".equals(name)) return mod.id() + " " + mod.version();

                if (ret == boolean.class) return Boolean.FALSE;
                if (ret == int.class) return 0;
                if (ret == long.class) return 0L;
                if (ret == float.class) return 0.0f;
                if (ret == double.class) return 0.0d;
                if (List.class.isAssignableFrom(ret) || Collection.class.isAssignableFrom(ret)) return List.of();
                if (Set.class.isAssignableFrom(ret)) return Set.of();
                if (Map.class.isAssignableFrom(ret)) return Map.of();
                if (Optional.class.isAssignableFrom(ret)) return Optional.empty();
                return null;
            });
        } catch (Throwable t) {
            MutoLog.warn("failed creating fallback proxy container: {}", t.getMessage());
            return null;
        }
    }

    public void syncKeyMappings() {
        try {
            net.minecraft.client.Minecraft mc = null;
            try {
                mc = net.minecraft.client.Minecraft.getInstance();
            } catch (Throwable ignored) {}
            if (mc == null || mc.options == null) return;

            final net.minecraft.client.Minecraft client = mc;
            Runnable sync = () -> {
                try {
                    Class<?> regClass = Class.forName("net.fabricmc.fabric.impl.client.keymapping.KeyMappingRegistryImpl");
                    java.lang.reflect.Method processMethod = regClass.getMethod("process", net.minecraft.client.KeyMapping[].class);
                    net.minecraft.client.KeyMapping[] updated = (net.minecraft.client.KeyMapping[]) processMethod.invoke(null, (Object) client.options.keyMappings);
                    if (updated != null) {
                        try {
                            ((me.fy2ne.muto.mixin.OptionsAccessor) client.options).muto$setKeyMappings(updated);
                        } catch (Throwable fallback) {
                            java.lang.reflect.Field uField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                            uField.setAccessible(true);
                            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) uField.get(null);
                            java.lang.reflect.Field kmField = net.minecraft.client.Options.class.getDeclaredField("keyMappings");
                            long offset = unsafe.objectFieldOffset(kmField);
                            unsafe.putObject(client.options, offset, updated);
                        }
                        net.minecraft.client.KeyMapping.resetMapping();
                        MutoLog.info("synced {} total key mappings into active game options", updated.length);
                    }
                } catch (Throwable t) {
                    MutoLog.warn("keymapping sync note: {}", t.getMessage());
                }
            };
            if (!client.isSameThread()) {
                client.submit(sync).get();
            } else {
                sync.run();
            }
        } catch (Throwable t) {
            MutoLog.warn("failed dispatching keymapping sync: {}", t.getMessage());
        }
    }

    private void notify(Consumer<String> notifier, List<String> logs, String msg) {
        MutoLog.info("stage: {}", msg);
        if (logs != null) logs.add(msg);
        if (notifier != null) {
            notifier.accept(msg);
        }
    }
}
