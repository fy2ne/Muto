package me.fy2ne.muto.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.codec.StreamCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.network.protocol.common.custom.CustomPacketPayload$1")
public abstract class CustomPacketPayloadMixin {

    @WrapOperation(
            method = "writeCap",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V")
    )
    private void muto$safeEncodePayload(StreamCodec codec, Object buf, Object payload, Operation<Void> original) {
        try {
            original.call(codec, buf, payload);
        } catch (ClassCastException cce) {
            // Lingering mixin from disabled mod or unknown packet: discard safely instead of severing connection
        }
    }
}
