package me.fy2ne.muto.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void muto$addDisabledReloadButton(CallbackInfo ci) {
        int x = this.width / 2 + 106;
        int y = this.height / 4 + 72 + -16 + 24;

        Button reloadBtn = Button.builder(Component.literal("↻"), btn -> {})
                .bounds(x, y, 20, 20)
                .tooltip(Tooltip.create(Component.literal("Leave world to reload mods.")))
                .build();

        reloadBtn.active = false;
        this.addRenderableWidget(reloadBtn);
        me.fy2ne.muto.MutoLog.info("pause screen disabled reload button mounted at ({}, {})", x, y);
    }
}
