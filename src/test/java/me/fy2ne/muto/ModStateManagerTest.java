package me.fy2ne.muto;

import me.fy2ne.muto.mods.DiskMod;
import me.fy2ne.muto.mods.ModStateManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class ModStateManagerTest {

    @BeforeEach
    void resetState() {
        ModStateManager.INSTANCE.clearStaged();
    }

    @Test
    void testCoreModCannotBeToggled() {
        DiskMod coreMod = new DiskMod("muto", "Muto", "0.2.0", Path.of("muto.jar"), false, true, true);
        ModStateManager.INSTANCE.toggleMod(coreMod);
        Assertions.assertFalse(ModStateManager.INSTANCE.isStagedForDisable("muto"));
        Assertions.assertEquals(0, ModStateManager.INSTANCE.getStagedCount());
    }

    @Test
    void testStandardModStagingToggle() {
        DiskMod testMod = new DiskMod("appleskin", "AppleSkin", "1.0.0", Path.of("appleskin.jar"), false, true, false);
        ModStateManager.INSTANCE.toggleMod(testMod);
        Assertions.assertTrue(ModStateManager.INSTANCE.isStagedForDisable("appleskin"));
        Assertions.assertEquals(1, ModStateManager.INSTANCE.getStagedCount());

        // Toggle again cancels staging
        ModStateManager.INSTANCE.toggleMod(testMod);
        Assertions.assertFalse(ModStateManager.INSTANCE.isStagedForDisable("appleskin"));
        Assertions.assertEquals(0, ModStateManager.INSTANCE.getStagedCount());
    }

    @Test
    void testDisabledModStagingEnable() {
        DiskMod disabledMod = new DiskMod("sodium", "Sodium", "0.6.0", Path.of("sodium.jar.disabled"), true, false, false);
        ModStateManager.INSTANCE.toggleMod(disabledMod);
        Assertions.assertTrue(ModStateManager.INSTANCE.isStagedForEnable("sodium"));
        Assertions.assertEquals(1, ModStateManager.INSTANCE.getStagedCount());

        // Toggle again cancels staging
        ModStateManager.INSTANCE.toggleMod(disabledMod);
        Assertions.assertFalse(ModStateManager.INSTANCE.isStagedForEnable("sodium"));
        Assertions.assertEquals(0, ModStateManager.INSTANCE.getStagedCount());
    }

    @Test
    void testApplyFileRenames(@TempDir Path tempDir) throws IOException {
        Path activeJar = tempDir.resolve("testmod.jar");
        createDummyModJar(activeJar, "testmod", "Test Mod", "1.0.0");

        Path disabledJar = tempDir.resolve("disabledmod.jar.disabled");
        createDummyModJar(disabledJar, "disabledmod", "Disabled Mod", "2.0.0");

        DiskMod active = new DiskMod("testmod", "Test Mod", "1.0.0", activeJar, false, true, false);
        DiskMod disabled = new DiskMod("disabledmod", "Disabled Mod", "2.0.0", disabledJar, true, false, false);

        ModStateManager.INSTANCE.toggleMod(active);
        ModStateManager.INSTANCE.toggleMod(disabled);

        Assertions.assertTrue(ModStateManager.INSTANCE.isStagedForDisable("testmod"));
        Assertions.assertTrue(ModStateManager.INSTANCE.isStagedForEnable("disabledmod"));

        // Simulate rename execution
        Path targetDisabled = tempDir.resolve("testmod.jar.disabled");
        Files.move(activeJar, targetDisabled);

        Path targetEnabled = tempDir.resolve("disabledmod.jar");
        Files.move(disabledJar, targetEnabled);

        Assertions.assertTrue(Files.exists(targetDisabled));
        Assertions.assertFalse(Files.exists(activeJar));

        Assertions.assertTrue(Files.exists(targetEnabled));
        Assertions.assertFalse(Files.exists(disabledJar));
    }

    private void createDummyModJar(Path path, String id, String name, String version) throws IOException {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(path.toFile()))) {
            jos.putNextEntry(new JarEntry("fabric.mod.json"));
            String json = """
                    {
                      "schemaVersion": 1,
                      "id": "%s",
                      "version": "%s",
                      "name": "%s"
                    }
                    """.formatted(id, version, name);
            jos.write(json.getBytes(StandardCharsets.UTF_8));
            jos.closeEntry();
        }
    }
}
