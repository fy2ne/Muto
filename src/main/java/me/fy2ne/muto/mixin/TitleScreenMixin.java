package me.fy2ne.muto.mixin;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.client.gui.MutoReloadScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void muto$addTitleReloadButton(CallbackInfo ci) {
        if (!me.fy2ne.muto.config.MutoConfig.get().showTitleScreenButton) {
            return;
        }

        int expectedRowY = this.height / 4 + 48 + 72;

        // Collect existing 20x20 icon buttons on this row (Friends, Language, Accessibility, ModMenu)
        List<AbstractWidget> iconRow = new ArrayList<>();
        for (var child : this.children()) {
            if (child instanceof AbstractWidget widget) {
                if (widget.getWidth() == 20 && widget.getHeight() == 20 && Math.abs(widget.getY() - expectedRowY) <= 6) {
                    iconRow.add(widget);
                }
            }
        }

        // Sort existing buttons from left to right
        iconRow.sort(Comparator.comparingInt(AbstractWidget::getX));

        // Create Muto reload button using official SpriteIconButton API
        SpriteIconButton reloadBtn = SpriteIconButton.builder(
                Component.literal("Reload Mods"),
                btn -> {
                    if (this.minecraft != null) {
                        if (!me.fy2ne.muto.config.MutoConfig.get().confirmBeforeReload) {
                            this.minecraft.setScreenAndShow(new MutoReloadScreen(this));
                            return;
                        }
                        this.minecraft.setScreenAndShow(new ConfirmScreen(
                                accepted -> {
                                    if (accepted && this.minecraft != null) {
                                        this.minecraft.setScreenAndShow(new MutoReloadScreen(this));
                                    } else if (this.minecraft != null) {
                                        this.minecraft.setScreenAndShow(this);
                                    }
                                },
                                Component.literal("§f§lReload Mods?"),
                                Component.literal("§7Muto will scan /mods, update active classes, and synchronize loader registries.\n\n§eUnsaved world state should be saved before proceeding."),
                                Component.literal("Yes"),
                                Component.literal("No")
                        ));
                    }
                },
                true
        )
        .width(20)
        .size(20, 20)
        .sprite(Identifier.fromNamespaceAndPath("muto", "icon/reload"), 15, 15)
        .build();

        reloadBtn.setTooltip(Tooltip.create(Component.literal("Reload Mods")));

        // Add to icon row as the 5th button
        iconRow.add(reloadBtn);
        this.addRenderableWidget(reloadBtn);

        // Center all 5 buttons symmetrically: totalWidth = N * 20 + (N - 1) * 4
        int totalCount = iconRow.size();
        int totalWidth = totalCount * 20 + (totalCount - 1) * 4;
        int startX = (this.width - totalWidth) / 2;

        for (int i = 0; i < totalCount; i++) {
            AbstractWidget widget = iconRow.get(i);
            widget.setX(startX + i * 24);
            widget.setY(expectedRowY);
        }

        MutoLog.info("aligned {} icon buttons on title screen (muto at index {})", totalCount, totalCount - 1);
    }
}
