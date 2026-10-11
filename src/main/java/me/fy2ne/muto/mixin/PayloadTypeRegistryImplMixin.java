package me.fy2ne.muto.mixin;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(targets = "net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl", remap = false)
public abstract class PayloadTypeRegistryImplMixin {

    @Shadow
    private Map<Identifier, ?> packetTypes;

    @Inject(
            method = "register(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$Type;Lnet/minecraft/network/codec/StreamCodec;)Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$TypeAndCodec;",
            at = @At("HEAD"),
            require = 0
    )
    private void muto$allowPayloadReRegistration(
            CustomPacketPayload.Type<?> type,
            StreamCodec<?, ?> codec,
            CallbackInfoReturnable<?> cir
    ) {
        if (this.packetTypes != null && type != null) {
            this.packetTypes.remove(type.id());
        }
    }
}
