package me.fy2ne.muto;

import me.fy2ne.muto.api.ModDiff;
import me.fy2ne.muto.api.MutoEvents;
import me.fy2ne.muto.engine.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class ReloadShellTest {

    @Test
    void testModDiffBehavior() {
        ModDiff empty = ModDiff.EMPTY;
        Assertions.assertFalse(empty.hasChanges());

        ModDiff modified = new ModDiff(
                Set.of("sodium"),
                Set.of("broken_mod"),
                Set.of("jei"),
                Set.of("muto")
        );
        Assertions.assertTrue(modified.hasChanges());
        Assertions.assertTrue(modified.added().contains("sodium"));
        Assertions.assertTrue(modified.removed().contains("broken_mod"));
        Assertions.assertTrue(modified.updated().contains("jei"));
        Assertions.assertTrue(modified.unchanged().contains("muto"));
    }

    @Test
    void testReloadEventPipeline() {
        AtomicBoolean startFired = new AtomicBoolean(false);
        AtomicBoolean finishFired = new AtomicBoolean(false);
        AtomicLong recordedDuration = new AtomicLong(0);

        MutoEvents.RELOAD_START.register((epochMs, expectedDiff) -> {
            startFired.set(true);
        });

        MutoEvents.RELOAD_FINISH.register((epochMs, diff, success, durationMs, error) -> {
            finishFired.set(true);
            recordedDuration.set(durationMs);
        });

        long start = System.currentTimeMillis();
        MutoEvents.RELOAD_START.invoker().onReloadStart(start, ModDiff.EMPTY);
        Assertions.assertTrue(startFired.get());

        MutoEvents.RELOAD_FINISH.invoker().onReloadFinish(
                System.currentTimeMillis(),
                ModDiff.EMPTY,
                true,
                150L,
                null
        );
        Assertions.assertTrue(finishFired.get());
        Assertions.assertEquals(150L, recordedDuration.get());
    }

    @Test
    void testModScannerAndSnapshotParsing(@TempDir Path tempDir) throws IOException {
        createMockJar(tempDir, "addon-1.0.jar", "addon", "1.0.0", "java.lang.Object");

        List<ScannedMod> scanned = ModScanner.scan(tempDir);
        Assertions.assertEquals(1, scanned.size());

        ScannedMod mod = scanned.getFirst();
        Assertions.assertEquals("addon", mod.id());
        Assertions.assertEquals("1.0.0", mod.version());
        Assertions.assertNotNull(mod.hash());
        Assertions.assertEquals(64, mod.hash().length());
        Assertions.assertTrue(mod.hasEntrypoint("main"));
        Assertions.assertEquals(List.of("java.lang.Object"), mod.getClassesFor("main"));

        ModSnapshot snapshot = new ModSnapshot(scanned);
        Assertions.assertEquals(1, snapshot.mods().size());
        Assertions.assertEquals(ModTier.STANDARD, snapshot.tier("addon"));
        Assertions.assertEquals(1, snapshot.reloadableMods().size());
        Assertions.assertEquals(0, snapshot.stubbornMods().size());
    }

    @Test
    void testModTierClassification(@TempDir Path tempDir) throws IOException {
        createMockJar(tempDir, "muto-0.1.0.jar", "muto", "0.1.0", null);
        createMockJar(tempDir, "client-hud.jar", "hud_tweaks", "1.0.0", null);

        List<ScannedMod> scanned = ModScanner.scan(tempDir);
        ModSnapshot snapshot = new ModSnapshot(scanned);

        Assertions.assertEquals(ModTier.STUBBORN, snapshot.tier("muto"));
    }

    @Test
    void testMutoClassLoaderLifecycle(@TempDir Path tempDir) throws IOException {
        Path jar = createMockJar(tempDir, "sample.jar", "sample", "1.0", "java.lang.Object");

        MutoClassLoader loader = new MutoClassLoader(
                List.of(jar),
                Set.of("sample"),
                getClass().getClassLoader()
        );

        Assertions.assertFalse(loader.isClosed());
        Assertions.assertTrue(loader.modIds().contains("sample"));
        Assertions.assertEquals(1, loader.jarPaths().size());

        loader.close();
        Assertions.assertTrue(loader.isClosed());
    }

    @Test
    void testReloadEnginePlanAndExecution(@TempDir Path tempDir) throws IOException {
        createMockJar(tempDir, "dynamic-mod-1.0.jar", "dynamic_mod", "1.0.0", "java.lang.Object");

        ReloadPlan plan = ReloadEngine.INSTANCE.plan(tempDir);
        Assertions.assertTrue(plan.hasWork());
        Assertions.assertEquals(1, plan.reloadable().size());
        Assertions.assertTrue(plan.diff().added().contains("dynamic_mod"));

        List<String> stages = new ArrayList<>();
        ReloadResult result = ReloadEngine.INSTANCE.execute(plan, stages::add);

        Assertions.assertTrue(result.success());
        Assertions.assertNull(result.error());
        Assertions.assertTrue(result.durationMs() >= 0);
        Assertions.assertFalse(stages.isEmpty());
        Assertions.assertEquals(1, ReloadEngine.INSTANCE.currentSnapshot().mods().size());
    }

    @Test
    void testCrashingModRollbackAndSafety(@TempDir Path tempDir) throws IOException {
        // deliberate broken mod with nonexistent entrypoint class
        createMockJar(tempDir, "broken-mod.jar", "broken_mod", "0.0.1", "me.fy2ne.missing.NonExistentClass");

        ReloadPlan plan = ReloadEngine.INSTANCE.plan(tempDir);
        Assertions.assertTrue(plan.hasWork());

        ReloadResult result = ReloadEngine.INSTANCE.execute(plan, s -> {});

        // verify safety rule #1: must not crash, must return failure, must report error
        Assertions.assertFalse(result.success());
        Assertions.assertNotNull(result.error());
        Assertions.assertTrue(result.error().contains("ClassNotFoundException") || result.error().contains("NonExistentClass"));
        Assertions.assertFalse(ReloadEngine.INSTANCE.isReloading());
    }

    private static Path createMockJar(Path dir, String fileName, String modId, String version, String mainClass) throws IOException {
        Path jarPath = dir.resolve(fileName);
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jarPath))) {
            jos.putNextEntry(new ZipEntry("fabric.mod.json"));
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"schemaVersion\": 1,\n");
            sb.append("  \"id\": \"").append(modId).append("\",\n");
            sb.append("  \"version\": \"").append(version).append("\",\n");
            sb.append("  \"name\": \"").append(modId).append("\"");
            if (mainClass != null) {
                sb.append(",\n  \"entrypoints\": {\n");
                sb.append("    \"main\": [\"").append(mainClass).append("\"]\n");
                sb.append("  }\n");
            } else {
                sb.append("\n");
            }
            sb.append("}");

            jos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            jos.closeEntry();
        }
        return jarPath;
    }
}
