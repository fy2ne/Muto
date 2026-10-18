package me.fy2ne.muto.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.spongepowered.asm.mixin.Pseudo;

import java.lang.reflect.Field;
import java.util.List;

@Pseudo
@Mixin(targets = "net.fabricmc.fabric.impl.client.keymapping.KeyMappingRegistryImpl", remap = false)
public class KeyMappingRegistryImplMixin {

    @Inject(method = "registerKeyMapping", at = @At("HEAD"), require = 0)
    private static void muto$evictDuplicateKeyMapping(KeyMapping keyMapping, CallbackInfoReturnable<KeyMapping> cir) {
        if (keyMapping == null) return;
        try {
            Class<?> cls = Class.forName("net.fabricmc.fabric.impl.client.keymapping.KeyMappingRegistryImpl");
            Field f = cls.getDeclaredField("MODDED_KEY_BINDINGS");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<KeyMapping> list = (List<KeyMapping>) f.get(null);
            if (list != null) {
                list.removeIf(k -> k != null && k.getName().equals(keyMapping.getName()));
            }
        } catch (Throwable ignored) {}
    }

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
