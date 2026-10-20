package me.fy2ne.muto.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.network.protocol.common.custom.CustomPacketPayload$1")
public abstract class CustomPacketPayloadMixin {

    private int muto$packetStartIndex = -1;

    @Inject(method = "writeCap", at = @At("HEAD"))
    private void muto$markPacketStart(FriendlyByteBuf buf, CustomPacketPayload.Type<?> type, CustomPacketPayload payload, CallbackInfo ci) {
        if (buf instanceof io.netty.buffer.ByteBuf bb) {
            muto$packetStartIndex = bb.writerIndex();
        }
    }

    @WrapOperation(
            method = "writeCap",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V")
    )
    private void muto$safeEncodePayload(StreamCodec codec, Object buf, Object payload, Operation<Void> original) {
        try {
            original.call(codec, buf, payload);
        } catch (ClassCastException cce) {
            if (buf instanceof io.netty.buffer.ByteBuf bb && muto$packetStartIndex >= 0) {
                bb.writerIndex(muto$packetStartIndex);
            }
            me.fy2ne.muto.MutoLog.warn("discarded packet payload and rewound netty buffer header: {}", cce.getMessage());
        }
    }
}
