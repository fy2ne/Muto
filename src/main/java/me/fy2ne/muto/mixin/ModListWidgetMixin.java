package me.fy2ne.muto.mixin;

import me.fy2ne.muto.client.gui.MutoModFilter;
import me.fy2ne.muto.mods.DisabledModsRegistry;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.terraformersmc.modmenu.gui.widget.ModListWidget")
public abstract class ModListWidgetMixin {

    @Inject(method = "addEntry(Lcom/terraformersmc/modmenu/gui/widget/entries/ModListEntry;)I", at = @At("HEAD"), cancellable = true, require = 0)
    private void muto$filterEntries(com.terraformersmc.modmenu.gui.widget.entries.ModListEntry entry, CallbackInfoReturnable<Integer> cir) {
        if (entry == null || entry.getMod() == null) return;
        String modId = entry.getMod().getId();
        if (modId == null) return;

        boolean isLoaded = FabricLoader.getInstance().getModContainer(modId).isPresent();
        boolean isDisabled = DisabledModsRegistry.isDisabled(modId);

        MutoModFilter filter = MutoModFilter.CURRENT;
        if (filter == MutoModFilter.ACTIVE) {
            if (isDisabled || !isLoaded) {
                cir.setReturnValue(0);
            }
        } else if (filter == MutoModFilter.DISABLED) {
            if (!isDisabled) {
                cir.setReturnValue(0);
            }
        } else if (filter == MutoModFilter.UNLOADED) {
            if (isLoaded || isDisabled) {
                cir.setReturnValue(0);
            }
        }
    }
}
