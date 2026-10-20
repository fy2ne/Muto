package me.fy2ne.muto;

import me.fy2ne.muto.engine.ReloadEngine;
import me.fy2ne.muto.mods.DisabledModsRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.List;

public final class MutoMod implements ClientModInitializer {
    private static boolean dcevmDetected;
    private static boolean jbrDetected;

    @Override
    public void onInitializeClient() {
        MutoLog.info("Muto Engine initializing (client runtime)");
        detectRuntimeCapabilities();
        try {
            Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
            DisabledModsRegistry.reconcileFileRenames(modsDir);
            ReloadEngine.INSTANCE.recordInitialSnapshot(modsDir);
        } catch (Throwable t) {
            MutoLog.warn("baseline snapshot deferred: {}", t.getMessage());
        }
    }

    private void detectRuntimeCapabilities() {
        String vmName = System.getProperty("java.vm.name", "");
        String vmVendor = System.getProperty("java.vm.vendor", "");
        jbrDetected = vmName.toLowerCase().contains("jetbrains") || vmVendor.toLowerCase().contains("jetbrains");

        List<String> vmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        dcevmDetected = vmArgs.stream().anyMatch(arg -> arg.contains("AllowEnhancedClassRedefinition"))
                || vmName.toLowerCase().contains("dcevm");

        MutoLog.info("runtime check: jbr={}, dcevm={}", jbrDetected, dcevmDetected);
    }

    public static boolean hasDcevm() {
        return dcevmDetected;
    }

    public static boolean isJbr() {
        return jbrDetected;
    }
}
