package me.fy2ne.muto.client.gui;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.engine.ReloadEngine;
import me.fy2ne.muto.engine.ReloadResult;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MutoReconnectingScreen extends Screen {
    private static final int DARK_BG = 0xEE0B0F19;
    private static final int CARD_BG = 0xFF141923;
    private static final int CARD_BORDER = 0x30FFFFFF;
    private static final int PROGRESS_BG = 0xFF1E2536;
    private static final int PROGRESS_FILL = 0xFF38BDF8; // vibrant cyan/sky
    private static final int TEXT_PRIMARY = 0xFFF1F5F9;
    private static final int TEXT_SECONDARY = 0xFF94A3B8;

    private final Screen origin;
    private final Path modsDir;
    private final ServerData targetServer;
    private final boolean isSingleplayer;

    private volatile String statusText = "Preparing hot reload...";
    private volatile float progress = 0.05f;
    private float renderedProgress = 0.05f;
    private final AtomicBoolean reloadStarted = new AtomicBoolean(false);
    private final AtomicBoolean completed = new AtomicBoolean(false);

    public MutoReconnectingScreen(Screen origin, Path modsDir, ServerData targetServer, boolean isSingleplayer) {
        super(Component.literal("Muto Hot Reload"));
        this.origin = origin;
        this.modsDir = modsDir;
        this.targetServer = targetServer;
        this.isSingleplayer = isSingleplayer;
    }

    @Override
    protected void init() {
        int btnW = 120;
        int btnH = 20;
        int btnX = (this.width - btnW) / 2;
        int btnY = (this.height / 2) + 60;

        this.addRenderableWidget(
                Button.builder(Component.literal("Cancel to Menu"), btn -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreenAndShow(new TitleScreen());
                    }
                }).bounds(btnX, btnY, btnW, btnH).build()
        );

        if (reloadStarted.compareAndSet(false, true)) {
            startReload();
        }
    }

    private void startReload() {
        ReloadEngine.INSTANCE.reloadAsync(modsDir, msg -> {
            this.statusText = msg;
            if (msg.startsWith("Scanning")) progress = 0.20f;
            else if (msg.startsWith("Constructing") || msg.startsWith("Building")) progress = 0.40f;
            else if (msg.startsWith("Tearing") || msg.startsWith("Unfreez")) progress = 0.65f;
            else if (msg.startsWith("Invoking") || msg.startsWith("Re-invoking")) progress = 0.85f;
            else if (msg.startsWith("Finaliz")) progress = 0.95f;
        }).thenAccept(res -> {
            progress = 1.0f;
            completed.set(true);
            if (this.minecraft != null) {
                this.minecraft.execute(() -> onFinish(res));
            }
        });
    }

    private void onFinish(ReloadResult res) {
        if (this.minecraft == null) return;

        if (targetServer != null) {
            this.statusText = "Reconnecting to " + targetServer.name + "...";
            try {
                ServerAddress addr = ServerAddress.parseString(targetServer.ip);
                ConnectScreen.startConnecting(new TitleScreen(), this.minecraft, addr, targetServer, false, null);
                return;
            } catch (Throwable t) {
                MutoLog.error("auto-reconnect failed: {}", t.getMessage(), t);
            }
        }

        // Singleplayer or Title Screen fallback
        if (origin != null && !(origin instanceof MutoReconnectingScreen)) {
            this.minecraft.setScreenAndShow(origin);
        } else {
            this.minecraft.setScreenAndShow(new TitleScreen());
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        // Smooth progress interpolation
        renderedProgress += (progress - renderedProgress) * 0.15f;

        // Background overlay
        graphics.fill(0, 0, this.width, this.height, DARK_BG);

        int cardW = 340;
        int cardH = 140;
        int cardX = (this.width - cardW) / 2;
        int cardY = (this.height - cardH) / 2 - 10;

        // Card Container
        graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, CARD_BG);
        graphics.fill(cardX, cardY, cardX + cardW, cardY + 1, CARD_BORDER);
        graphics.fill(cardX, cardY + cardH - 1, cardX + cardW, cardY + cardH, CARD_BORDER);
        graphics.fill(cardX, cardY + 1, cardX + 1, cardY + cardH - 1, CARD_BORDER);
        graphics.fill(cardX + cardW - 1, cardY + 1, cardX + cardW, cardY + cardH - 1, CARD_BORDER);

        // Title
        String title = targetServer != null ? "Reloading & Reconnecting" : "Applying Mod Changes";
        graphics.centeredText(this.font, Component.literal(title), this.width / 2, cardY + 18, TEXT_PRIMARY);

        // Server Subtitle if applicable
        if (targetServer != null) {
            String serverSub = "Server: " + targetServer.ip;
            graphics.centeredText(this.font, Component.literal(serverSub), this.width / 2, cardY + 34, TEXT_SECONDARY);
        } else if (isSingleplayer) {
            graphics.centeredText(this.font, Component.literal("Singleplayer World Saved"), this.width / 2, cardY + 34, TEXT_SECONDARY);
        }

        // Status message
        graphics.centeredText(this.font, Component.literal(statusText), this.width / 2, cardY + 56, TEXT_PRIMARY);

        // Progress Bar
        int barW = 280;
        int barH = 8;
        int barX = (this.width - barW) / 2;
        int barY = cardY + 76;

        graphics.fill(barX, barY, barX + barW, barY + barH, PROGRESS_BG);
        int fillW = Math.max(2, (int) (barW * Math.min(1.0f, renderedProgress)));
        graphics.fill(barX, barY, barX + fillW, barY + barH, PROGRESS_FILL);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
