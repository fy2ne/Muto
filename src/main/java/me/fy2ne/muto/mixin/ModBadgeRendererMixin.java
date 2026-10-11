package me.fy2ne.muto.mixin;

import com.terraformersmc.modmenu.util.mod.Mod;
import me.fy2ne.muto.engine.ModDependencyAnalyzer;
import me.fy2ne.muto.mods.DisabledModsRegistry;
import me.fy2ne.muto.mods.ModStateManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(targets = "com.terraformersmc.modmenu.util.mod.ModBadgeRenderer")
public abstract class ModBadgeRendererMixin {

    @Shadow
    protected Mod mod;

    @Shadow
    public abstract void drawBadge(GuiGraphicsExtractor graphics, FormattedCharSequence text, int outlineColor, int fillColor, int mouseX, int mouseY);

    @Inject(method = "draw", at = @At("RETURN"), require = 0)
    private void muto$renderMutoBadges(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.mod == null) return;
        String modId = this.mod.getId();
        if (modId == null) return;

        List<String> missing = ModDependencyAnalyzer.getMissing(modId);
        boolean isStaged = ModStateManager.INSTANCE.isStaged(modId);
        boolean isDisabled = DisabledModsRegistry.isDisabled(modId);
        boolean isLoadedInFabric = FabricLoader.getInstance().getModContainer(modId).isPresent();

        if (!missing.isEmpty()) {
            drawBadge(graphics, Component.literal("Missing: " + missing.get(0)).getVisualOrderText(), 0xFFFF4444, 0x88551111, mouseX, mouseY);
        } else if (isStaged) {
            boolean isUnloaded = !isLoadedInFabric && !isDisabled;
            if (isUnloaded) {
                drawBadge(graphics, Component.literal("Pending: Load").getVisualOrderText(), 0xFF55FF55, 0x88114411, mouseX, mouseY);
            } else {
                boolean nextState = ModStateManager.INSTANCE.getStagedState(modId);
                if (nextState) {
                    drawBadge(graphics, Component.literal("Pending: On").getVisualOrderText(), 0xFF55FF55, 0x88114411, mouseX, mouseY);
                } else {
                    drawBadge(graphics, Component.literal("Pending: Off").getVisualOrderText(), 0xFFFFAA00, 0x88553300, mouseX, mouseY);
                }
            }
        } else if (isDisabled) {
            drawBadge(graphics, Component.literal("Disabled").getVisualOrderText(), 0xFFFF5555, 0x88551111, mouseX, mouseY);
        } else if (!isLoadedInFabric) {
            drawBadge(graphics, Component.literal("Unloaded").getVisualOrderText(), 0xFFFFCC00, 0x88554400, mouseX, mouseY);
        }
    }
}
