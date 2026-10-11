package me.fy2ne.muto.engine.agent;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.engine.MutoAgent;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;

public final class MutoAgentBootstrap {
    private static volatile boolean attemptedAttach = false;

    private MutoAgentBootstrap() {}

    public static synchronized Instrumentation getInstrumentation() {
        if (MutoAgent.isAvailable()) {
            return MutoAgent.getInstrumentation();
        }

        if (attemptedAttach) return null;
        attemptedAttach = true;

        try {
            Path selfJar = FabricLoader.getInstance().getModContainer("muto")
                    .flatMap(c -> c.getOrigin().getPaths().stream().findFirst())
                    .orElse(null);

            if (selfJar == null || !java.nio.file.Files.isRegularFile(selfJar) || !selfJar.toString().endsWith(".jar")) {
                Path devLibs = FabricLoader.getInstance().getGameDir().resolve("../build/libs").normalize();
                if (java.nio.file.Files.isDirectory(devLibs)) {
                    try (var stream = java.nio.file.Files.list(devLibs)) {
                        selfJar = stream.filter(p -> p.toString().endsWith(".jar") && !p.toString().contains("-sources"))
                                .findFirst()
                                .orElse(null);
                    }
                }
                if (selfJar == null) {
                    Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
                    if (java.nio.file.Files.isDirectory(modsDir)) {
                        try (var stream = java.nio.file.Files.list(modsDir)) {
                            selfJar = stream.filter(p -> p.getFileName().toString().startsWith("muto-") && p.toString().endsWith(".jar"))
                                    .findFirst()
                                    .orElse(null);
                        }
                    }
                }
            }

            if (selfJar == null || !java.nio.file.Files.exists(selfJar)) {
                MutoLog.warn("cannot resolve muto mod jar path for dynamic agent attach");
                return null;
            }

            String pid = String.valueOf(ProcessHandle.current().pid());
            MutoLog.info("attempting dynamic agent attach to pid {}", pid);

            // VirtualMachine reflection to prevent linkage errors on unusual runtimes
            Class<?> vmClass = Class.forName("com.sun.tools.attach.VirtualMachine");
            var attachMethod = vmClass.getMethod("attach", String.class);
            Object vm = attachMethod.invoke(null, pid);

            var loadAgentMethod = vmClass.getMethod("loadAgent", String.class);
            loadAgentMethod.invoke(vm, selfJar.toAbsolutePath().toString());

            var detachMethod = vmClass.getMethod("detach");
            detachMethod.invoke(vm);

            MutoLog.info("dynamic agent attach call completed");
        } catch (Throwable t) {
            MutoLog.warn("dynamic self-attach failed: {}", t.getMessage());
        }

        return MutoAgent.getInstrumentation();
    }
}
