package me.fy2ne.muto.mixin;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.client.MutoToasts;
import me.fy2ne.muto.client.gui.MutoReloadScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
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
        int expectedRowY = this.height / 4 + 48 + 72 + 12;

        // Collect existing 20x20 icon buttons on this row (Realms/Friends, Language, Accessibility, ModMenu)
        List<AbstractWidget> iconRow = new ArrayList<>();
        for (var child : this.children()) {
            if (child instanceof AbstractWidget widget) {
                if (widget.getWidth() == 20 && widget.getHeight() == 20 && Math.abs(widget.getY() - expectedRowY) <= 8) {
                    iconRow.add(widget);
                }
            }
        }

        // Sort existing buttons from left to right
        iconRow.sort(Comparator.comparingInt(AbstractWidget::getX));

        // Create Muto reload button with confirmation dialog
        MutoIconReloadButton reloadBtn = new MutoIconReloadButton(0, expectedRowY, btn -> {
            if (this.minecraft != null) {
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
        });
        reloadBtn.setTooltip(Tooltip.create(Component.literal("Reload Mods")));

        // Add to icon row as the 5th (or next) button
        iconRow.add(reloadBtn);
        this.addRenderableWidget(reloadBtn);

        // Center all buttons symmetrically: totalWidth = N * 20 + (N - 1) * 4
        int totalCount = iconRow.size();
        int totalWidth = totalCount * 20 + (totalCount - 1) * 4;
        int startX = (this.width - totalWidth) / 2;

        for (int i = 0; i < totalCount; i++) {
            AbstractWidget widget = iconRow.get(i);
            widget.setX(startX + i * 24);
            widget.setY(expectedRowY);
        }

        if (this.minecraft != null) {
            MutoToasts.showCapabilityHint(this.minecraft);
        }
        MutoLog.info("aligned {} icon buttons on title screen (muto at index {})", totalCount, totalCount - 1);
    }

    private static final class MutoIconReloadButton extends Button {
        public MutoIconReloadButton(int x, int y, OnPress onPress) {
            super(x, y, 20, 20, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor g, int mx, int my, float dt) {
            int cx = this.getX() + 10;
            int cy = this.getY() + 10;
            int col = this.isHoveredOrFocused() ? 0xFFFFFFFF : 0xFFE0E0E0;

            // Draw clean high-res 12x12 reload circle icon with arrowheads
            // Top arc
            g.fill(cx - 3, cy - 5, cx + 4, cy - 4, col);
            // Bottom arc
            g.fill(cx - 4, cy + 4, cx + 3, cy + 5, col);
            // Left arc
            g.fill(cx - 5, cy - 3, cx - 4, cy + 4, col);
            // Right arc
            g.fill(cx + 4, cy - 4, cx + 5, cy + 3, col);

            // Top-right arrow
            g.fill(cx + 2, cy - 7, cx + 5, cy - 6, col);
            g.fill(cx + 4, cy - 6, cx + 5, cy - 3, col);

            // Bottom-left arrow
            g.fill(cx - 5, cy + 3, cx - 4, cy + 6, col);
            g.fill(cx - 5, cy + 5, cx - 2, cy + 6, col);
        }
    }
}
