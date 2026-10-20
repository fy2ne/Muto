package me.fy2ne.muto.engine;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.mixin.MappedRegistryAccessor;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.lang.reflect.Field;
import java.util.*;

public final class RegistryFreezer {
    private static Field frozenField;
    private static Field byLocationField;
    private static Field byKeyField;
    private static Field byValueField;
    private static Field registrationInfosField;
    private static Field byIdField;
    private static Field toIdField;
    private static boolean pruneUnavailableLogged = false;

    static {
        try {
            frozenField = MappedRegistry.class.getDeclaredField("frozen");
            frozenField.setAccessible(true);
        } catch (Exception e) {
            MutoLog.warn("direct frozen field access failed: {}", e.getMessage());
        }

        try {
            byLocationField = MappedRegistry.class.getDeclaredField("byLocation");
            byLocationField.setAccessible(true);
            byKeyField = MappedRegistry.class.getDeclaredField("byKey");
            byKeyField.setAccessible(true);
            byValueField = MappedRegistry.class.getDeclaredField("byValue");
            byValueField.setAccessible(true);
            registrationInfosField = MappedRegistry.class.getDeclaredField("registrationInfos");
            registrationInfosField.setAccessible(true);
            byIdField = MappedRegistry.class.getDeclaredField("byId");
            byIdField.setAccessible(true);
            toIdField = MappedRegistry.class.getDeclaredField("toId");
            toIdField.setAccessible(true);
        } catch (Exception e) {
            MutoLog.warn("registry map field access failed: {}", e.getMessage());
        }
    }

    public record EntryBackup(
            Identifier id,
            ResourceKey<?> key,
            Object holder,
            Object value,
            Object regInfo,
            Integer rawId
    ) {}

    public static boolean setFrozen(MappedRegistry<?> reg, boolean state) {
        if (reg instanceof MappedRegistryAccessor accessor) {
            accessor.muto$setFrozen(state);
            return true;
        }

        if (frozenField != null) {
            try {
                frozenField.setBoolean(reg, state);
                return true;
            } catch (Exception ex) {
                MutoLog.error("failed setting frozen flag on {}: {}", reg.key().identifier(), ex.getMessage());
            }
        }
        return false;
    }

    public static int unfreezeAll() {
        int count = 0;
        try {
            for (Registry<?> reg : BuiltInRegistries.REGISTRY) {
                if (reg instanceof MappedRegistry<?> mapped) {
                    if (setFrozen(mapped, false)) {
                        count++;
                    }
                }
            }
            MutoLog.info("unfroze {} registries", count);
        } catch (LinkageError e) {
            MutoLog.info("builtin registries not bootstrapped, skipping unfreeze pass");
        } catch (Throwable t) {
            MutoLog.error("unfreeze pass failed: {}", t.getMessage());
        }
        return count;
    }

    public static int freezeAll() {
        int count = 0;
        try {
            for (Registry<?> reg : BuiltInRegistries.REGISTRY) {
                if (reg instanceof MappedRegistry<?> mapped) {
                    if (setFrozen(mapped, true)) {
                        count++;
                    }
                }
            }
            MutoLog.info("refroze {} registries", count);
        } catch (LinkageError e) {
            MutoLog.info("builtin registries not bootstrapped, skipping freeze pass");
        } catch (Throwable t) {
            MutoLog.error("freeze pass failed: {}", t.getMessage());
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    public static Map<MappedRegistry<?>, List<EntryBackup>> snapshotNamespaces(Collection<String> namespaces) {
        Map<MappedRegistry<?>, List<EntryBackup>> snap = new IdentityHashMap<>();
        if (namespaces == null || namespaces.isEmpty() || byLocationField == null || byKeyField == null) {
            return snap;
        }

        Set<String> lowerNs = new HashSet<>();
        for (String ns : namespaces) {
            if (ns != null) lowerNs.add(ns.toLowerCase(Locale.ROOT));
        }

        try {
            for (Registry<?> reg : BuiltInRegistries.REGISTRY) {
                if (!(reg instanceof MappedRegistry<?> mapped)) continue;

                Map<Identifier, Object> locMap = (Map<Identifier, Object>) byLocationField.get(mapped);
                Map<ResourceKey<?>, Object> keyMap = (Map<ResourceKey<?>, Object>) byKeyField.get(mapped);
                Map<Object, Object> valMap = byValueField != null ? (Map<Object, Object>) byValueField.get(mapped) : null;
                Map<ResourceKey<?>, Object> infoMap = registrationInfosField != null ? (Map<ResourceKey<?>, Object>) registrationInfosField.get(mapped) : null;
                Map<Object, Integer> toIdMap = toIdField != null ? (Map<Object, Integer>) toIdField.get(mapped) : null;

                if (locMap == null || keyMap == null) continue;

                List<EntryBackup> entries = new ArrayList<>();
                for (var e : locMap.entrySet()) {
                    Identifier id = e.getKey();
                    if (!lowerNs.contains(id.getNamespace().toLowerCase(Locale.ROOT))) continue;

                    Object holder = e.getValue();
                    ResourceKey<?> matchedKey = null;
                    for (ResourceKey<?> rk : keyMap.keySet()) {
                        if (rk.identifier().equals(id)) {
                            matchedKey = rk;
                            break;
                        }
                    }

                    Object value = null;
                    if (valMap != null) {
                        for (var ve : valMap.entrySet()) {
                            if (Objects.equals(ve.getValue(), holder)) {
                                value = ve.getKey();
                                break;
                            }
                        }
                    }
                    if (value == null && holder instanceof net.minecraft.core.Holder.Reference<?> ref) {
                        try { value = ref.value(); } catch (Throwable ignored) {}
                    }

                    Object info = (matchedKey != null && infoMap != null) ? infoMap.get(matchedKey) : null;
                    Integer rawId = (value != null && toIdMap != null) ? toIdMap.get(value) : null;

                    entries.add(new EntryBackup(id, matchedKey, holder, value, info, rawId));
                }

                if (!entries.isEmpty()) {
                    snap.put(mapped, entries);
                }
            }
        } catch (LinkageError ignored) {
        } catch (Throwable t) {
            MutoLog.warn("registry snapshot warning: {}", t.getMessage());
        }
        return snap;
    }

    @SuppressWarnings("unchecked")
    public static void restoreSnapshot(Collection<String> namespaces, Map<MappedRegistry<?>, List<EntryBackup>> snap) {
        if (namespaces != null) {
            for (String ns : namespaces) {
                pruneNamespace(ns);
            }
        }
        if (snap == null || snap.isEmpty() || byLocationField == null || byKeyField == null) return;

        int restored = 0;
        try {
            for (var regEntry : snap.entrySet()) {
                MappedRegistry<?> mapped = regEntry.getKey();
                Map<Identifier, Object> locMap = (Map<Identifier, Object>) byLocationField.get(mapped);
                Map<ResourceKey<?>, Object> keyMap = (Map<ResourceKey<?>, Object>) byKeyField.get(mapped);
                Map<Object, Object> valMap = byValueField != null ? (Map<Object, Object>) byValueField.get(mapped) : null;
                Map<ResourceKey<?>, Object> infoMap = registrationInfosField != null ? (Map<ResourceKey<?>, Object>) registrationInfosField.get(mapped) : null;
                List<Object> byIdList = byIdField != null ? (List<Object>) byIdField.get(mapped) : null;
                Map<Object, Integer> toIdMap = toIdField != null ? (Map<Object, Integer>) toIdField.get(mapped) : null;

                for (EntryBackup b : regEntry.getValue()) {
                    if (locMap != null && b.holder() != null) {
                        locMap.put(b.id(), b.holder());
                    }
                    if (keyMap != null && b.key() != null && b.holder() != null) {
                        keyMap.put(b.key(), b.holder());
                    }
                    if (valMap != null && b.value() != null && b.holder() != null) {
                        valMap.put(b.value(), b.holder());
                    }
                    if (infoMap != null && b.key() != null && b.regInfo() != null) {
                        infoMap.put(b.key(), b.regInfo());
                    }
                    if (b.rawId() != null && b.value() != null && toIdMap != null && byIdList != null) {
                        while (byIdList.size() <= b.rawId()) {
                            byIdList.add(null);
                        }
                        byIdList.set(b.rawId(), b.holder());
                        toIdMap.put(b.value(), b.rawId());
                    }
                    restored++;
                }
            }
            if (restored > 0) {
                MutoLog.info("tx rollback restored {} registry entries", restored);
            }
        } catch (Throwable t) {
            MutoLog.error("tx rollback restore error: {}", t.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static int pruneNamespace(String namespace) {
        if (byLocationField == null || byKeyField == null) {
            if (!pruneUnavailableLogged) {
                pruneUnavailableLogged = true;
                MutoLog.warn("registry prune unavailable: MappedRegistry internal fields were not resolved "
                        + "on this Minecraft version, so stale namespace entries will not be removed "
                        + "(reloading a mod that registers blocks/items may duplicate entries)");
            }
            return 0;
        }
        int prunedTotal = 0;

        try {
            for (Registry<?> reg : BuiltInRegistries.REGISTRY) {
                if (!(reg instanceof MappedRegistry<?> mapped)) continue;

                Map<Identifier, ?> locMap = (Map<Identifier, ?>) byLocationField.get(mapped);
                Map<ResourceKey<?>, ?> keyMap = (Map<ResourceKey<?>, ?>) byKeyField.get(mapped);
                Map<Object, ?> valMap = byValueField != null ? (Map<Object, ?>) byValueField.get(mapped) : null;
                Map<ResourceKey<?>, ?> infoMap = registrationInfosField != null ? (Map<ResourceKey<?>, ?>) registrationInfosField.get(mapped) : null;
                List<Object> byIdList = byIdField != null ? (List<Object>) byIdField.get(mapped) : null;
                Map<Object, Integer> toIdMap = toIdField != null ? (Map<Object, Integer>) toIdField.get(mapped) : null;

                if (locMap == null || keyMap == null) continue;

                List<Identifier> matched = new ArrayList<>();
                for (Identifier id : locMap.keySet()) {
                    if (id.getNamespace().equalsIgnoreCase(namespace)) {
                        matched.add(id);
                    }
                }

                for (Identifier id : matched) {
                    Object holder = locMap.remove(id);
                    if (holder != null) {
                        keyMap.entrySet().removeIf(e -> e.getKey().identifier().equals(id));
                        if (infoMap != null) {
                            infoMap.entrySet().removeIf(e -> e.getKey().identifier().equals(id));
                        }

                        Object value = null;
                        if (valMap != null) {
                            var it = valMap.entrySet().iterator();
                            while (it.hasNext()) {
                                var entry = it.next();
                                if (Objects.equals(entry.getValue(), holder)) {
                                    value = entry.getKey();
                                    it.remove();
                                    break;
                                }
                            }
                        }
                        if (value == null && holder instanceof net.minecraft.core.Holder.Reference<?> ref) {
                            try {
                                value = ref.value();
                            } catch (Throwable ignored) {}
                        }

                        if (value != null && toIdMap != null) {
                            Integer rawId = toIdMap.remove(value);
                            if (rawId != null && byIdList != null && rawId >= 0 && rawId < byIdList.size()) {
                                byIdList.set(rawId, null);
                            }
                        }

                        prunedTotal++;
                    }
                }

                if (byIdList != null) {
                    while (!byIdList.isEmpty() && byIdList.get(byIdList.size() - 1) == null) {
                        byIdList.remove(byIdList.size() - 1);
                    }
                }
            }

            if (prunedTotal > 0) {
                MutoLog.info("pruned {} entries for namespace {}", prunedTotal, namespace);
            }
        } catch (LinkageError e) {
            MutoLog.info("builtin registries not bootstrapped, skipping prune for {}", namespace);
        } catch (Throwable t) {
            MutoLog.error("prune failed for {}: {}", namespace, t.getMessage());
        }

        return prunedTotal;
    }
}
