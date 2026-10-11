package me.fy2ne.muto.mixin;

import me.fy2ne.muto.MutoLog;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.HashSet;
import java.util.Set;

@Mixin(targets = "de.maxhenkel.voicechat.intercompatibility.FabricClientCompatibilityManager", remap = false)
public class FabricClientCompatibilityManagerMixin {

    /**
     * @author Muto
     * @reason Prevent ClassCastException to IPackRepository on hot-reload when PackRepository was loaded at boot
     */
    @Overwrite
    public void addResourcePackSource(RepositorySource source) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) return;
            PackRepository repo = mc.getResourcePackRepository();
            if (repo == null) return;

            try {
                PackRepositoryAccessor accessor = (PackRepositoryAccessor) repo;
                Set<RepositorySource> oldSources = accessor.muto$getSources();
                Set<RepositorySource> newSources = new HashSet<>(oldSources != null ? oldSources : Set.of());
                newSources.add(source);
                accessor.muto$setSources(newSources);
                MutoLog.info("added voicechat resource pack source directly into PackRepository");
            } catch (Throwable fallback) {
                java.lang.reflect.Field f = PackRepository.class.getDeclaredField("sources");
                f.setAccessible(true);
                @SuppressWarnings("unchecked")
                Set<RepositorySource> oldSources = (Set<RepositorySource>) f.get(repo);
                Set<RepositorySource> newSources = new HashSet<>(oldSources != null ? oldSources : Set.of());
                newSources.add(source);
                f.set(repo, newSources);
                MutoLog.info("added voicechat resource pack source directly via reflection");
            }
        } catch (Throwable t) {
            MutoLog.warn("failed adding voicechat resource pack source: {}", t.getMessage());
        }
    }
}
