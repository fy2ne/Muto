package me.fy2ne.muto.engine;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.api.ModDiff;
import me.fy2ne.muto.api.MutoEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class ReloadEngine {
    public static final ReloadEngine INSTANCE = new ReloadEngine();

    private final AtomicBoolean reloading = new AtomicBoolean(false);
    private volatile ModSnapshot currentSnapshot = ModSnapshot.empty();
    private volatile MutoClassLoader currentLoader;
    private final Map<String, List<Object>> instancesByMod = new LinkedHashMap<>();

    private ReloadEngine() {}

    public boolean isReloading() {
        return reloading.get();
    }

    public ModSnapshot currentSnapshot() {
        return currentSnapshot;
    }

    public void recordInitialSnapshot(Path modsDir) {
        List<ScannedMod> scanned = ModScanner.scan(modsDir);
        currentSnapshot = new ModSnapshot(scanned);
        MutoLog.info("baseline snapshot: {} mods ({} reloadable, {} stubborn)",
                currentSnapshot.mods().size(),
                currentSnapshot.reloadableMods().size(),
                currentSnapshot.stubbornMods().size());
    }

    public MutoClassLoader currentLoader() {
        return currentLoader;
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

    public synchronized ReloadResult execute(ReloadPlan plan, Consumer<String> stageNotifier) {
        if (!reloading.compareAndSet(false, true)) {
            MutoLog.warn("reload already in progress, rejecting request");
            return ReloadResult.fail(plan.diff(), 0L, "reload in progress");
        }

        long startMs = System.currentTimeMillis();
        MutoEvents.RELOAD_START.invoker().onReloadStart(startMs, plan.diff());

        try {
            ModDiff diff = plan.diff();

            // Fast path: the mods folder is unchanged. Nothing can be safely
            // reloaded, and re-invoking the entrypoints of already-loaded mods
            // would re-run their initializers against global state — which is
            // exactly what produces errors like "duplicate mod id: sodium".
            if (!plan.hasWork()) {
                notify(stageNotifier, "No mod changes detected — nothing to reload.");
                long noopMs = System.currentTimeMillis() - startMs;
                MutoLog.info("no mod changes detected, reload is a no-op ({}ms)", noopMs);
                MutoEvents.RELOAD_FINISH.invoker().onReloadFinish(
                        System.currentTimeMillis(), diff, true, noopMs, null);
                return ReloadResult.ok(diff, noopMs);
            }

            notify(stageNotifier, "Scanning mod directory & validating environment...");

            // Fabric already loads every mod that existed at startup on its own
            // classpath. Re-initializing one of those duplicates its global state,
            // so the only mods we may initialize are those ADDED after launch:
            // they are absent from the parent classpath and load fresh in our
            // child loader. Updated/removed mods still hold live references in
            // the running game and cannot be hot-swapped safely.
            List<ScannedMod> initializable = new ArrayList<>();
            for (ScannedMod mod : plan.reloadable()) {
                if (diff.added().contains(mod.id())) {
                    initializable.add(mod);
                }
            }

            if (!diff.updated().isEmpty() || !diff.removed().isEmpty()) {
                MutoLog.warn("{} updated / {} removed mod(s) are bound to the running game and "
                                + "cannot be hot-swapped — restart to apply them",
                        diff.updated().size(), diff.removed().size());
            }

            MutoClassLoader candidateLoader = null;
            try {
                // Stage 1: Build candidate classloader and preflight check
                if (!initializable.isEmpty()) {
                    notify(stageNotifier, "Constructing candidate classloader & staging checks...");
                    List<Path> reloadableJars = new ArrayList<>();
                    Set<String> reloadableIds = new HashSet<>();
                    for (ScannedMod m : initializable) {
                        reloadableJars.add(m.jarPath());
                        reloadableIds.add(m.id());
                    }

                    candidateLoader = new MutoClassLoader(
                            reloadableJars,
                            reloadableIds,
                            getClass().getClassLoader()
                    );

                    // Preflight: verify entrypoint classes load before dropping state
                    for (ScannedMod mod : initializable) {
                        preflightCheck(candidateLoader, mod);
                    }
                }

                // Stage 2: Tear down only removed mod contexts. Mods that remain
                // loaded must be left running or we would drop working functionality.
                notify(stageNotifier, "Tearing down removed mod contexts...");
                for (String rem : diff.removed()) {
                    teardownMod(rem);
                    RegistryFreezer.pruneNamespace(rem);
                }
                for (String upd : diff.updated()) {
                    RegistryFreezer.pruneNamespace(upd);
                }

                if (currentLoader != null) {
                    try {
                        currentLoader.close();
                    } catch (Exception ex) {
                        MutoLog.warn("error closing old classloader: {}", ex.getMessage());
                    }
                }
                System.gc();

                // Stage 3: Unfreeze registries & activate new loader
                notify(stageNotifier, "Unfreezing registries & activating classloader...");
                RegistryFreezer.unfreezeAll();
                currentLoader = candidateLoader;
                candidateLoader = null;

                // Stage 4: Instantiate & execute entrypoints (added mods only)
                notify(stageNotifier, "Re-invoking entrypoints & updating registries...");
                for (ScannedMod mod : initializable) {
                    initializeMod(currentLoader, mod);
                }

                // Stage 5: Refreeze registries
                notify(stageNotifier, "Finalizing engine state & preparing Title Screen...");
                RegistryFreezer.freezeAll();

                // Stage 6: Sync loader metadata
                syncFabricLoader(plan.targetSnapshot(), diff);

                currentSnapshot = plan.targetSnapshot();
                long durationMs = System.currentTimeMillis() - startMs;
                MutoLog.info("reload finished cleanly in {}ms (+{} -{} ~{})",
                        durationMs,
                        diff.added().size(),
                        diff.removed().size(),
                        diff.updated().size()
                );

                MutoEvents.RELOAD_FINISH.invoker().onReloadFinish(
                        System.currentTimeMillis(),
                        diff,
                        true,
                        durationMs,
                        null
                );

                return ReloadResult.ok(diff, durationMs);

            } catch (Throwable err) {
                long durationMs = System.currentTimeMillis() - startMs;
                MutoLog.error("reload failed, rolling back: {}", err.getMessage(), err);

                if (candidateLoader != null) {
                    try {
                        candidateLoader.close();
                    } catch (Exception ignored) {}
                }

                RegistryFreezer.freezeAll();

                MutoEvents.RELOAD_FINISH.invoker().onReloadFinish(
                        System.currentTimeMillis(),
                        diff,
                        false,
                        durationMs,
                        err
                );

                return ReloadResult.fail(diff, durationMs, err.getMessage());
            }
        } finally {
            reloading.set(false);
        }
    }

    private void preflightCheck(MutoClassLoader loader, ScannedMod mod) throws ClassNotFoundException {
        for (String epClass : mod.getClassesFor("main")) {
            loader.loadClass(epClass);
        }
        for (String epClass : mod.getClassesFor("client")) {
            loader.loadClass(epClass);
        }
    }

    private void teardownMod(String modId) {
        List<Object> instances = instancesByMod.remove(modId);
        if (instances == null) return;
        for (Object inst : instances) {
            closeQuietly(inst);
        }
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

    private void initializeMod(MutoClassLoader loader, ScannedMod mod) {
        List<Object> created = new ArrayList<>();

        // Main entrypoints
        for (String clsName : mod.getClassesFor("main")) {
            try {
                Class<?> cls = loader.loadClass(clsName);
                Object obj = cls.getDeclaredConstructor().newInstance();
                created.add(obj);

                if (obj instanceof ModInitializer init) {
                    init.onInitialize();
                    MutoLog.info("initialized main: {}", clsName);
                }
            } catch (Throwable ex) {
                MutoLog.error("failed initializing main entrypoint {}: {}", clsName, ex.getMessage(), ex);
            }
        }

        // Client entrypoints
        for (String clsName : mod.getClassesFor("client")) {
            try {
                Class<?> cls = loader.loadClass(clsName);
                Object obj = cls.getDeclaredConstructor().newInstance();
                created.add(obj);

                if (obj instanceof ClientModInitializer clientInit) {
                    clientInit.onInitializeClient();
                    MutoLog.info("initialized client: {}", clsName);
                }
            } catch (Throwable ex) {
                MutoLog.error("failed initializing client entrypoint {}: {}", clsName, ex.getMessage(), ex);
            }
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
                        syncModMenuRemove(removedId);
                    }

                    for (String addedId : diff.added()) {
                        ScannedMod mod = snapshot.mods().get(addedId);
                        if (mod == null) continue;
                        try {
                            Object container = createContainer(mod);
                            if (container != null) {
                                if (map != null) map.put(mod.id(), container);
                                if (list != null && !list.contains(container)) list.add(container);
                                syncModMenuAdd(mod.id(), container);
                                MutoLog.info("synced {} into FabricLoader and ModMenu", mod.id());
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

    @SuppressWarnings("unchecked")
    private void syncModMenuAdd(String modId, Object container) {
        try {
            Class<?> modMenuCls = Class.forName("com.terraformersmc.modmenu.ModMenu");
            Field modsField = modMenuCls.getDeclaredField("MODS");
            modsField.setAccessible(true);
            Map<String, Object> menuMods = (Map<String, Object>) modsField.get(null);

            Field rootModsField = modMenuCls.getDeclaredField("ROOT_MODS");
            rootModsField.setAccessible(true);
            Map<String, Object> rootMods = (Map<String, Object>) rootModsField.get(null);

            Class<?> fabricModCls = null;
            for (String name : List.of(
                    "com.terraformersmc.modmenu.util.mod.fabric.FabricMod",
                    "com.terraformersmc.modmenu.util.mod.FabricMod"
            )) {
                try {
                    fabricModCls = Class.forName(name);
                    break;
                } catch (ClassNotFoundException ignored) {}
            }

            if (fabricModCls != null && container instanceof net.fabricmc.loader.api.ModContainer mc) {
                java.lang.reflect.Constructor<?> ctor = null;
                for (java.lang.reflect.Constructor<?> c : fabricModCls.getDeclaredConstructors()) {
                    c.setAccessible(true);
                    if (c.getParameterCount() == 2) {
                        ctor = c;
                        break;
                    } else if (c.getParameterCount() == 1 && ctor == null) {
                        ctor = c;
                    }
                }

                if (ctor != null) {
                    Object modItem = (ctor.getParameterCount() == 2)
                            ? ctor.newInstance(mc, Collections.emptySet())
                            : ctor.newInstance(mc);

                    menuMods.put(modId, modItem);
                    rootMods.put(modId, modItem);
                    MutoLog.info("synced {} into ModMenu active cache", modId);

                    try {
                        java.lang.reflect.Method clear = modMenuCls.getDeclaredMethod("clearModCountCache");
                        clear.setAccessible(true);
                        clear.invoke(null);
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable t) {
            Throwable root = t.getCause() != null ? t.getCause() : t;
            MutoLog.warn("ModMenu add sync skipped: {}", root.toString());
        }
    }

    @SuppressWarnings("unchecked")
    private void syncModMenuRemove(String modId) {
        try {
            Class<?> modMenuCls = Class.forName("com.terraformersmc.modmenu.ModMenu");
            Field modsField = modMenuCls.getDeclaredField("MODS");
            modsField.setAccessible(true);
            Map<String, Object> menuMods = (Map<String, Object>) modsField.get(null);

            Field rootModsField = modMenuCls.getDeclaredField("ROOT_MODS");
            rootModsField.setAccessible(true);
            Map<String, Object> rootMods = (Map<String, Object>) rootModsField.get(null);

            if (menuMods != null) menuMods.remove(modId);
            if (rootMods != null) rootMods.remove(modId);

            try {
                java.lang.reflect.Method clear = modMenuCls.getDeclaredMethod("clearModCountCache");
                clear.setAccessible(true);
                clear.invoke(null);
            } catch (Throwable ignored) {}

            MutoLog.info("removed {} from ModMenu active cache", modId);
        } catch (Throwable t) {
            MutoLog.warn("ModMenu remove sync skipped: {}", t.getMessage());
        }
    }

    private void notify(Consumer<String> notifier, String msg) {
        MutoLog.info("stage: {}", msg);
        if (notifier != null) {
            notifier.accept(msg);
        }
    }
}
