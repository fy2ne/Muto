package me.fy2ne.muto.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.fabricmc.fabric.impl.client.keymapping.KeyMappingRegistryImpl", remap = false)
public class KeyMappingRegistryImplMixin {

    @Redirect(
            method = "registerKeyMapping",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;options:Lnet/minecraft/client/Options;"),
            require = 0
    )
    private static Options muto$bypassOptionsInitGuard(Minecraft mc) {
        return null;
    }

    @Inject(method = "registerKeyMapping", at = @At("RETURN"), require = 0)
    private static void muto$onKeyMappingRegistered(KeyMapping keyMapping, CallbackInfoReturnable<KeyMapping> cir) {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null && mc.options != null && keyMapping != null) {
                me.fy2ne.muto.engine.ReloadEngine.INSTANCE.syncKeyMappings();
            }
        } catch (Throwable ignored) {}
    }
}
