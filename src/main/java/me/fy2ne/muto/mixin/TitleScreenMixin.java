package me.fy2ne.muto.mixin;

import me.fy2ne.muto.client.gui.MutoReloadScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void muto$addTitleReloadButton(CallbackInfo ci) {
        int x = this.width / 2 + 128;
        int y = this.height / 4 + 48 + 72 + 12;

        Button reloadBtn = Button.builder(Component.literal("↻"), btn -> {
            if (this.minecraft != null) {
                this.minecraft.setScreenAndShow(new MutoReloadScreen(this));
            }
        })
        .bounds(x, y, 20, 20)
        .tooltip(Tooltip.create(Component.literal("Reload Mods (Muto)")))
        .build();

        this.addRenderableWidget(reloadBtn);
        if (this.minecraft != null) {
            me.fy2ne.muto.client.MutoToasts.showCapabilityHint(this.minecraft);
        }
        me.fy2ne.muto.MutoLog.info("title screen reload button mounted at ({}, {})", x, y);
    }
}
