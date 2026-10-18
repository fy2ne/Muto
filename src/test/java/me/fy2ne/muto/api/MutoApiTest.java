package me.fy2ne.muto.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

class MutoApiTest {

    @Test
    void testApiAvailabilityContract() {
        boolean available = MutoApi.isAvailable();
        Assertions.assertFalse(available);
    }

    @Test
    void testApiReloadStateContract() {
        Assertions.assertFalse(MutoApi.isReloading());
        Assertions.assertFalse(MutoApi.isSafeToReload());
    }

    @Test
    void testModIdsCollectionsNotNull() {
        Set<String> reloadable = MutoApi.getReloadableModIds();
        Set<String> stubborn = MutoApi.getStubbornModIds();
        Assertions.assertNotNull(reloadable);
        Assertions.assertNotNull(stubborn);
    }

    @Test
    void testModDiffContract() {
        ModDiff empty = ModDiff.EMPTY;
        Assertions.assertFalse(empty.hasChanges());
        Assertions.assertEquals(0, empty.added().size());
        Assertions.assertEquals(0, empty.removed().size());
        Assertions.assertEquals(0, empty.updated().size());
        Assertions.assertEquals(0, empty.unchanged().size());

        ModDiff diff = new ModDiff(
                Set.of("test_added"),
                Set.of("test_removed"),
                Set.of("test_updated"),
                Collections.emptySet()
        );
        Assertions.assertTrue(diff.hasChanges());
        Assertions.assertTrue(diff.added().contains("test_added"));
        Assertions.assertTrue(diff.removed().contains("test_removed"));
        Assertions.assertTrue(diff.updated().contains("test_updated"));
    }

    @Test
    void testEventDispatchLifecycle() {
        AtomicBoolean startFired = new AtomicBoolean(false);
        AtomicBoolean finishFired = new AtomicBoolean(false);

        MutoEvents.RELOAD_START.register((epochMs, diff) -> {
            startFired.set(true);
        });

        MutoEvents.RELOAD_FINISH.register((epochMs, diff, success, durationMs, error) -> {
            finishFired.set(true);
        });

        ModDiff mockDiff = ModDiff.EMPTY;
        MutoEvents.RELOAD_START.invoker().onReloadStart(System.currentTimeMillis(), mockDiff);
        MutoEvents.RELOAD_FINISH.invoker().onReloadFinish(System.currentTimeMillis(), mockDiff, true, 10L, null);

        Assertions.assertTrue(startFired.get());
        Assertions.assertTrue(finishFired.get());
    }
}
