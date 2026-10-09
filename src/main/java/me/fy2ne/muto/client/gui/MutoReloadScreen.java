package me.fy2ne.muto.client.gui;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.engine.ReloadEngine;
import me.fy2ne.muto.engine.ReloadResult;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public final class MutoReloadScreen extends Screen {
    private static final int DARK_PANEL = 0xD810141D;
    private static final int BORDER_COLOR = 0x40FFFFFF;
    private static final int BAR_BG = 0xFF141923;
    private static final int BAR_FILL = 0xFF22C55E;
    private static final int RED = 0xFFFF5555;

    private static final String[] STAGE_LABELS = {
        "Scanning mod directory…",
        "Building candidate classloader…",
        "Tearing down old runtime contexts…",
        "Unfreezing registries…",
        "Invoking entrypoints…",
        "Finalizing…"
    };

    private final Screen origin;
    private final long startMs;
    private float displayProg;
    private volatile float targetProg = 0.05f;
    private volatile String stageMsg = STAGE_LABELS[0];
    private final AtomicReference<ReloadResult> result = new AtomicReference<>();
    private boolean done;
    private Button returnBtn;
    private Button detailsToggleBtn;
    private boolean showLogs = false;

    private final List<String> log = new CopyOnWriteArrayList<>();
    private long dotTimer;
    private int dotCount;

    public MutoReloadScreen(Screen origin) {
        super(Component.literal("Reloading Mods"));
        this.origin = origin;
        this.startMs = System.currentTimeMillis();
        this.done = false;

        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");

        ReloadEngine.INSTANCE.reloadAsync(modsDir, msg -> {
            stageMsg = msg;
            log.add(msg);
            if (msg.startsWith("Scanning"))          targetProg = 0.18f;
            else if (msg.startsWith("Constructing") || msg.startsWith("Building")) targetProg = 0.38f;
            else if (msg.startsWith("Tearing"))      targetProg = 0.58f;
            else if (msg.startsWith("Unfreez"))      targetProg = 0.72f;
            else if (msg.startsWith("Re-invoking") || msg.startsWith("Invoking")) targetProg = 0.88f;
            else if (msg.startsWith("Finaliz"))      targetProg = 0.97f;
        }).thenAccept(res -> {
            result.set(res);
            targetProg = 1.0f;
        });
    }

    private void closeOrReturn() {
        if (this.minecraft == null) return;
        if (this.minecraft.level != null) {
            this.minecraft.setScreenAndShow(null);
        } else if (this.origin != null) {
            this.minecraft.setScreenAndShow(this.origin);
        } else {
            this.minecraft.setScreenAndShow(new TitleScreen());
        }
    }

    @Override
    protected void init() {
        super.init();
        int midX = this.width / 2;
        int midY = this.height / 2;

        detailsToggleBtn = Button.builder(
                Component.literal(showLogs ? "▲ Hide Logs" : "▼ Details"),
                btn -> {
                    showLogs = !showLogs;
                    detailsToggleBtn.setMessage(Component.literal(showLogs ? "▲ Hide Logs" : "▼ Details"));
                    repositionWidgets();
                }
        ).bounds(midX - 45, midY + 18, 90, 16).build();
        this.addRenderableWidget(detailsToggleBtn);

        returnBtn = Button.builder(
                Component.literal("Back"),
                btn -> closeOrReturn()
        ).bounds(midX - 50, midY + 45, 100, 20).build();
        returnBtn.visible = false;
        this.addRenderableWidget(returnBtn);

        repositionWidgets();
    }

    private void repositionWidgets() {
        int midX = this.width / 2;
        int midY = this.height / 2;
        if (showLogs) {
            if (detailsToggleBtn != null) detailsToggleBtn.setY(midY + 74);
            if (returnBtn != null) returnBtn.setY(midY + 96);
        } else {
            if (detailsToggleBtn != null) detailsToggleBtn.setY(midY + 18);
            if (returnBtn != null) returnBtn.setY(midY + 42);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return returnBtn != null && returnBtn.visible;
    }

    @Override
    public void tick() {
        super.tick();
        float gap = targetProg - displayProg;
        displayProg += gap * 0.18f;
        if (Math.abs(gap) < 0.005f) displayProg = targetProg;

        dotTimer++;
        if (dotTimer % 10 == 0) dotCount = (dotCount + 1) % 4;

        ReloadResult res = result.get();
        if (res != null && !done) {
            if (res.success()) {
                if (displayProg >= 0.99f) {
                    done = true;
                    long elapsed = System.currentTimeMillis() - startMs;

                    boolean noChanges = res.diff() == null || !res.diff().hasChanges();
                    if (noChanges) {
                        MutoLog.info("reload no-op in {}ms, returning", elapsed);
                        closeOrReturn();
                        return;
                    }

                    MutoLog.info("UI reload complete in {}ms, triggering resource reload", elapsed);
                    try {
                        this.minecraft.reloadResourcePacks().thenRun(() ->
                                this.minecraft.execute(this::closeOrReturn)
                        );
                    } catch (Exception ex) {
                        MutoLog.warn("resource reload error: {}", ex.getMessage());
                        closeOrReturn();
                    }
                }
            } else {
                done = true;
                stageMsg = res.error() != null ? "Failed: " + res.error() : "Reload failed — check muto.log";
                if (returnBtn != null) returnBtn.visible = true;
                showLogs = true;
                repositionWidgets();
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float dt) {
        int w = this.width;
        int h = this.height;
        int midX = w / 2;
        int midY = h / 2;

        // Translucent background
        g.fill(0, 0, w, h, 0x66000000);

        int cardW = 340;
        int cardH = showLogs ? 210 : 100;
        int cLeft = midX - cardW / 2;
        int cTop = midY - (showLogs ? 95 : 45);
        int cRight = midX + cardW / 2;
        int cBottom = cTop + cardH;

        // Card container
        g.fill(cLeft, cTop, cRight, cBottom, DARK_PANEL);
        g.fill(cLeft, cTop, cRight, cTop + 1, BORDER_COLOR);
        g.fill(cLeft, cBottom - 1, cRight, cBottom, BORDER_COLOR);
        g.fill(cLeft, cTop, cLeft + 1, cBottom, BORDER_COLOR);
        g.fill(cRight - 1, cTop, cRight, cBottom, BORDER_COLOR);

        // Title
        g.centeredText(this.font, Component.literal("§f§lReloading Mods"), midX, cTop + 12, 0xFFFFFFFF);

        // Stage label
        ReloadResult res = result.get();
        boolean failed = res != null && !res.success();
        String dots = ".".repeat(dotCount);
        String label = stageMsg + (failed ? "" : dots);
        int stageColor = failed ? RED : 0xFFCCCCCC;
        g.centeredText(this.font, Component.literal(label), midX, cTop + 30, stageColor);

        // Progress bar track
        int barW = 280;
        int barH = 5;
        int barX = midX - barW / 2;
        int barY = cTop + 48;

        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF2D3748);
        g.fill(barX, barY, barX + barW, barY + barH, BAR_BG);

        // Progress bar fill
        float clampedProg = Math.min(1.0f, displayProg);
        int fillW = (int) (barW * clampedProg);
        if (fillW > 0) {
            int fillColor = failed ? RED : BAR_FILL;
            g.fill(barX, barY, barX + fillW, barY + barH, fillColor);
        }

        // Percentage & Elapsed
        int pct = (int) (clampedProg * 100);
        long elapsed = System.currentTimeMillis() - startMs;
        g.text(this.font, Component.literal("§7" + pct + "%"), barX, barY + 8, 0xFFA0AEC0);
        g.text(this.font, Component.literal("§7" + (elapsed / 1000) + "s"), barX + barW - 18, barY + 8, 0xFFA0AEC0);

        // Logs console drawer when expanded
        if (showLogs) {
            int logBoxX = barX;
            int logBoxY = barY + 22;
            int logBoxW = barW;
            int logBoxH = 92;

            g.fill(logBoxX, logBoxY, logBoxX + logBoxW, logBoxY + logBoxH, 0xE6080B11);
            g.fill(logBoxX, logBoxY, logBoxX + logBoxW, logBoxY + 1, 0x30FFFFFF);
            g.fill(logBoxX, logBoxY + logBoxH - 1, logBoxX + logBoxW, logBoxY + logBoxH, 0x30FFFFFF);
            g.fill(logBoxX, logBoxY, logBoxX + 1, logBoxY + logBoxH, 0x30FFFFFF);
            g.fill(logBoxX + logBoxW - 1, logBoxY, logBoxX + logBoxW, logBoxY + logBoxH, 0x30FFFFFF);

            int startIdx = Math.max(0, log.size() - 6);
            int textY = logBoxY + 6;
            if (log.isEmpty()) {
                g.text(this.font, Component.literal("§8Listening to reload engine..."), logBoxX + 8, textY, 0xFF718096);
            } else {
                for (int i = startIdx; i < log.size(); i++) {
                    String line = log.get(i);
                    if (line.length() > 46) line = line.substring(0, 44) + "…";
                    g.text(this.font, Component.literal("§7> §f" + line), logBoxX + 6, textY, 0xFFE2E8F0);
                    textY += 13;
                }
            }
        }

        super.extractRenderState(g, mx, my, dt);
    }
}
