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
        } catch (Exception e) {
            MutoLog.warn("registry map field access failed: {}", e.getMessage());
        }
    }

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
    public static int pruneNamespace(String namespace) {
        if (byLocationField == null || byKeyField == null) return 0;
        int prunedTotal = 0;

        try {
            for (Registry<?> reg : BuiltInRegistries.REGISTRY) {
                if (!(reg instanceof MappedRegistry<?> mapped)) continue;

                Map<Identifier, ?> locMap = (Map<Identifier, ?>) byLocationField.get(mapped);
                Map<ResourceKey<?>, ?> keyMap = (Map<ResourceKey<?>, ?>) byKeyField.get(mapped);
                Map<Object, ?> valMap = byValueField != null ? (Map<Object, ?>) byValueField.get(mapped) : null;
                Map<ResourceKey<?>, ?> infoMap = registrationInfosField != null ? (Map<ResourceKey<?>, ?>) registrationInfosField.get(mapped) : null;

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
                        if (valMap != null) {
                            valMap.entrySet().removeIf(e -> Objects.equals(e.getValue(), holder));
                        }
                        prunedTotal++;
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
