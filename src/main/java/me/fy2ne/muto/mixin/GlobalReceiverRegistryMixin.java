package me.fy2ne.muto.mixin;

import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(targets = "net.fabricmc.fabric.impl.networking.GlobalReceiverRegistry", remap = false)
public abstract class GlobalReceiverRegistryMixin<H> {

    @Shadow
    private Map<Identifier, H> handlers;

    @Inject(
            method = "registerGlobalReceiver",
            at = @At("HEAD"),
            require = 0
    )
    private void muto$allowReceiverReRegistration(Identifier id, H handler, CallbackInfoReturnable<Boolean> cir) {
        if (this.handlers != null && id != null) {
            this.handlers.remove(id);
        }
    }
}
