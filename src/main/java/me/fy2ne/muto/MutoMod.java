package me.fy2ne.muto;

import net.fabricmc.api.ModInitializer;

import java.lang.management.ManagementFactory;
import java.util.List;

public final class MutoMod implements ModInitializer {
    private static boolean dcevmDetected;
    private static boolean jbrDetected;

    @Override
    public void onInitialize() {
        MutoLog.info("muto core initializing (v0.1.0)");
        detectRuntimeCapabilities();
        try {
            java.nio.file.Path modsDir = net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir().resolve("mods");
            me.fy2ne.muto.engine.ReloadEngine.INSTANCE.recordInitialSnapshot(modsDir);
            me.fy2ne.muto.automation.MutoTriggerWatcher.start();
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
