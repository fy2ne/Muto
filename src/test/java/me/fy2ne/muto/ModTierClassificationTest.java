package me.fy2ne.muto;

import me.fy2ne.muto.engine.ModSnapshot;
import me.fy2ne.muto.engine.ModTier;
import me.fy2ne.muto.engine.ScannedMod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ModTierClassificationTest {

    @Test
    void testCoreModsAreStubborn() {
        for (String coreId : List.of("minecraft", "fabricloader", "fabric", "java", "muto", "fabric-api-base", "fabric-renderer-api-v1")) {
            ScannedMod mod = new ScannedMod(
                    coreId, "1.0", Path.of(coreId + ".jar"), "sha256",
                    false,
                    Map.of("main", List.of("com.example.Mod")),
                    1024L
            );
            Assertions.assertEquals(ModTier.STUBBORN, ModTier.classify(mod));
            Assertions.assertFalse(ModTier.classify(mod).isReloadable());
        }
    }

    @Test
    void testPureClientModIsClean() {
        ScannedMod mod = new ScannedMod(
                "hud_tweak", "2.1.0", Path.of("hud_tweak.jar"), "sha256",
                false,
                Map.of("client", List.of("com.example.client.HudClient")),
                2048L
        );
        Assertions.assertEquals(ModTier.CLEAN, ModTier.classify(mod));
        Assertions.assertTrue(ModTier.classify(mod).isReloadable());
    }

    @Test
    void testStandardModWithMainEntrypoint() {
        ScannedMod mod = new ScannedMod(
                "simple_items", "1.0.0", Path.of("simple_items.jar"), "sha256",
                false,
                Map.of("main", List.of("com.example.SimpleItems")),
                4096L
        );
        Assertions.assertEquals(ModTier.STANDARD, ModTier.classify(mod));
        Assertions.assertTrue(ModTier.classify(mod).isReloadable());
    }

    @Test
    void testModWithoutEntrypointsOrMixinsIsStubborn() {
        ScannedMod mod = new ScannedMod(
                "empty_lib", "1.0.0", Path.of("empty_lib.jar"), "sha256",
                false,
                Map.of(),
                512L
        );
        Assertions.assertEquals(ModTier.STUBBORN, ModTier.classify(mod));
        Assertions.assertFalse(ModTier.classify(mod).isReloadable());
    }

    @Test
    void testSnapshotPartitioning() {
        ScannedMod clientMod = new ScannedMod("client_tool", "1.0", Path.of("1.jar"), "h1", false, Map.of("client", List.of("Client")), 100L);
        ScannedMod stdMod = new ScannedMod("gameplay", "1.0", Path.of("2.jar"), "h2", false, Map.of("main", List.of("Main")), 200L);
        ScannedMod coreMod = new ScannedMod("muto", "1.0", Path.of("3.jar"), "h3", false, Map.of("main", List.of("Muto")), 300L);

        ModSnapshot snapshot = new ModSnapshot(List.of(clientMod, stdMod, coreMod));
        Assertions.assertEquals(3, snapshot.mods().size());
        Assertions.assertEquals(2, snapshot.reloadableMods().size());
        Assertions.assertEquals(1, snapshot.stubbornMods().size());
        List<String> reloadableIds = snapshot.reloadableMods().stream().map(ScannedMod::id).toList();
        List<String> stubbornIds = snapshot.stubbornMods().stream().map(ScannedMod::id).toList();
        Assertions.assertTrue(reloadableIds.contains("client_tool"));
        Assertions.assertTrue(reloadableIds.contains("gameplay"));
        Assertions.assertTrue(stubbornIds.contains("muto"));
    }
}
