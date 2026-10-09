package me.fy2ne.muto.mixin;

import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MappedRegistry.class)
public interface MappedRegistryAccessor {

    @Accessor("frozen")
    boolean muto$isFrozen();

    @Accessor("frozen")
    void muto$setFrozen(boolean frozen);
}
