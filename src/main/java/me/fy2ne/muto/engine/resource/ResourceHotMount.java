package me.fy2ne.muto.engine.resource;

import me.fy2ne.muto.MutoLog;
import net.minecraft.client.Minecraft;

import java.util.concurrent.CompletableFuture;

public final class ResourceHotMount {
    private ResourceHotMount() {}

    public static CompletableFuture<Void> remountAndReload() {
        Minecraft mc = null;
        try {
            mc = Minecraft.getInstance();
        } catch (Throwable ignored) {}
        if (mc == null || mc.getResourceManager() == null) {
            return CompletableFuture.completedFuture(null);
        }

        final Minecraft client = mc;
        CompletableFuture<Void> future = new CompletableFuture<>();

        client.execute(() -> {
            try {
                var repo = client.getResourcePackRepository();
                if (repo != null) {
                    repo.reload();
                    MutoLog.info("pack repository rescanned");
                }

                client.reloadResourcePacks().thenRun(() -> {
                    MutoLog.info("gl shader and asset pipelines successfully remounted");
                    future.complete(null);
                }).exceptionally(ex -> {
                    MutoLog.warn("resource reload exception: {}", ex.getMessage());
                    future.complete(null);
                    return null;
                });
            } catch (Throwable t) {
                MutoLog.error("failed remounting resources: {}", t.getMessage());
                future.complete(null);
            }
        });

        return future;
    }
}
