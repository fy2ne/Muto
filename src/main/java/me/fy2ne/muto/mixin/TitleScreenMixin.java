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

    private SpriteIconButton muto$reloadBtn;

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

        if (me.fy2ne.muto.update.MutoUpdateChecker.isUpdateAvailable()) {
            reloadBtn.setTooltip(Tooltip.create(Component.literal("Reload Mods\n§a● Update available: v" + me.fy2ne.muto.update.MutoUpdateChecker.getLatestVersion())));
        } else {
            reloadBtn.setTooltip(Tooltip.create(Component.literal("Reload Mods")));
        }

        this.muto$reloadBtn = reloadBtn;

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

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void muto$renderUpdateBadge(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.muto$reloadBtn != null && this.muto$reloadBtn.visible && me.fy2ne.muto.update.MutoUpdateChecker.isUpdateAvailable()) {
            int x = this.muto$reloadBtn.getX() + 13;
            int y = this.muto$reloadBtn.getY() - 2;

            // Dark outline
            graphics.fill(x + 2, y, x + 6, y + 1, 0xFF052E16);
            graphics.fill(x + 1, y + 1, x + 7, y + 2, 0xFF052E16);
            graphics.fill(x, y + 2, x + 8, y + 6, 0xFF052E16);
            graphics.fill(x + 1, y + 6, x + 7, y + 7, 0xFF052E16);
            graphics.fill(x + 2, y + 7, x + 6, y + 8, 0xFF052E16);

            // Emerald green fill
            graphics.fill(x + 2, y + 1, x + 6, y + 2, 0xFF22C55E);
            graphics.fill(x + 1, y + 2, x + 7, y + 6, 0xFF22C55E);
            graphics.fill(x + 2, y + 6, x + 6, y + 7, 0xFF22C55E);

            // Highlight shine (mint crescent and white specular pip)
            graphics.fill(x + 2, y + 2, x + 4, y + 3, 0xFF86EFAC);
            graphics.fill(x + 2, y + 3, x + 3, y + 4, 0xFF86EFAC);
            graphics.fill(x + 4, y + 4, x + 6, y + 5, 0xFFFFFFFF);
        }
    }
}
