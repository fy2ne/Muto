package me.fy2ne.muto.mixin;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.mods.DisabledModsRegistry;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(value = Screen.class, priority = 9999)
public abstract class ScreenMixin {

    @Shadow
    @Final
    private List<GuiEventListener> children;

    @Shadow
    @Final
    private List<Renderable> renderables;

    @Shadow
    protected abstract void removeWidget(GuiEventListener listener);

    @Inject(method = "init(II)V", at = @At("RETURN"))
    private void muto$purgeDisabledModWidgets(int width, int height, CallbackInfo ci) {
        Set<String> disabledMods = DisabledModsRegistry.getDisabledModIds();
        if (disabledMods.isEmpty() || this.children == null) return;

        List<GuiEventListener> snapshot = new ArrayList<>(this.children);
        for (GuiEventListener listener : snapshot) {
            if (listener == null) continue;

            String className = listener.getClass().getName().toLowerCase();
            String widgetText = "";
            if (listener instanceof AbstractWidget aw) {
                try {
                    widgetText = aw.getMessage().getString().toLowerCase();
                } catch (Throwable ignored) {}
            }

            boolean belongsToDisabled = false;
            for (String modId : disabledMods) {
                if (modId == null || modId.isEmpty()) continue;
                String lowerId = modId.toLowerCase();
                if (className.contains(lowerId) || widgetText.contains(lowerId)) {
                    belongsToDisabled = true;
                    break;
                }
            }

            if (belongsToDisabled) {
                if (listener instanceof AbstractWidget aw) {
                    aw.visible = false;
                    aw.active = false;
                }
                this.removeWidget(listener);
                if (listener instanceof Renderable r && this.renderables != null) {
                    this.renderables.remove(r);
                }
                MutoLog.info("suppressed ghost UI widget {} from disabled mod", listener.getClass().getSimpleName());
            }
        }
    }
}
