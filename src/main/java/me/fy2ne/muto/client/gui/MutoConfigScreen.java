package me.fy2ne.muto.client.gui;

import me.fy2ne.muto.config.MutoConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class MutoConfigScreen extends Screen {
    private final Screen parent;

    public MutoConfigScreen(Screen parent) {
        super(Component.literal("Muto Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        MutoConfig cfg = MutoConfig.get();
        int btnW = Math.min(220, (this.width - 40) / 2 - 8);
        int btnH = 20;
        int leftX = this.width / 2 - btnW - 6;
        int rightX = this.width / 2 + 6;
        int startY = 50;
        int gapY = 26;

        // Option 1: Auto-Reload On Disk Change
        this.addRenderableWidget(Button.builder(
                getOptionText("Auto-Reload On Disk Change", cfg.autoReloadOnDiskChange),
                btn -> {
                    cfg.autoReloadOnDiskChange = !cfg.autoReloadOnDiskChange;
                    btn.setMessage(getOptionText("Auto-Reload On Disk Change", cfg.autoReloadOnDiskChange));
                })
                .bounds(leftX, startY, btnW, btnH)
                .tooltip(Tooltip.create(Component.literal("Automatically trigger hot-reload when jars in mods folder are added or replaced")))
                .build());

        // Option 2: Show Reload Toast
        this.addRenderableWidget(Button.builder(
                getOptionText("Reload Toast Notification", cfg.showToasts),
                btn -> {
                    cfg.showToasts = !cfg.showToasts;
                    btn.setMessage(getOptionText("Reload Toast Notification", cfg.showToasts));
                })
                .bounds(rightX, startY, btnW, btnH)
                .tooltip(Tooltip.create(Component.literal("Show in-game toast notification when reload finishes cleanly")))
                .build());

        // Option 3: Play Sound Cue
        this.addRenderableWidget(Button.builder(
                getOptionText("Reload Sound Effect", cfg.playReloadSound),
                btn -> {
                    cfg.playReloadSound = !cfg.playReloadSound;
                    btn.setMessage(getOptionText("Reload Sound Effect", cfg.playReloadSound));
                })
                .bounds(leftX, startY + gapY, btnW, btnH)
                .tooltip(Tooltip.create(Component.literal("Play sound cue when reload completes successfully")))
                .build());

        // Option 4: Fast Shader Reload
        this.addRenderableWidget(Button.builder(
                getOptionText("Fast Shader Refresh", cfg.fastShaderReload),
                btn -> {
                    cfg.fastShaderReload = !cfg.fastShaderReload;
                    btn.setMessage(getOptionText("Fast Shader Refresh", cfg.fastShaderReload));
                })
                .bounds(rightX, startY + gapY, btnW, btnH)
                .tooltip(Tooltip.create(Component.literal("Single-pass shader reload without duplicate freeze screens")))
                .build());

        // Option 5: Auto-Reconnect
        this.addRenderableWidget(Button.builder(
                getOptionText("Auto-Reconnect", cfg.autoReconnect),
                btn -> {
                    cfg.autoReconnect = !cfg.autoReconnect;
                    btn.setMessage(getOptionText("Auto-Reconnect", cfg.autoReconnect));
                })
                .bounds(leftX, startY + gapY * 2, btnW, btnH)
                .tooltip(Tooltip.create(Component.literal("Automatically reconnect if hot-reload is triggered while connected to a server")))
                .build());

        // Option 6: Title Screen Button
        this.addRenderableWidget(Button.builder(
                getOptionText("Title Screen Button", cfg.showTitleScreenButton),
                btn -> {
                    cfg.showTitleScreenButton = !cfg.showTitleScreenButton;
                    btn.setMessage(getOptionText("Title Screen Button", cfg.showTitleScreenButton));
                })
                .bounds(rightX, startY + gapY * 2, btnW, btnH)
                .tooltip(Tooltip.create(Component.literal("Display quick-reload icon on the Minecraft title screen")))
                .build());

        // Done Button
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> {
            MutoConfig.save();
            if (this.minecraft != null) {
                this.minecraft.setScreenAndShow(this.parent);
            }
        })
                .bounds(this.width / 2 - 100, this.height - 32, 200, 20)
                .build());
    }

    private static Component getOptionText(String label, boolean state) {
        return Component.literal(label + ": " + (state ? "§aON" : "§cOFF"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        if (this.minecraft != null) {
            graphics.text(this.minecraft.font, this.title, this.width / 2 - this.minecraft.font.width(this.title) / 2, 20, 0xFFFFFFFF);
        }
    }

    @Override
    public void onClose() {
        MutoConfig.save();
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parent);
        }
    }
}
