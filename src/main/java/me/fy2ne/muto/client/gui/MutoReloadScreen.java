package me.fy2ne.muto.client.gui;

import me.fy2ne.muto.MutoLog;
import me.fy2ne.muto.MutoMod;
import me.fy2ne.muto.engine.ReloadEngine;
import me.fy2ne.muto.engine.ReloadResult;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class MutoReloadScreen extends Screen {
    private static final int CYAN  = 0xFF00E5FF;
    private static final int DIM   = 0xFF718096;
    private static final int DARK  = 0xFF1A202C;
    private static final int RED   = 0xFFFF5555;
    private static final int BG    = 0xF20D1117;

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
    // collected log lines for the detail panel
    private final List<String> log = new ArrayList<>();
    private long dotTimer;
    private int dotCount;

    public MutoReloadScreen(Screen origin) {
        super(Component.literal("Muto — Reloading Mods"));
        this.origin = origin;
        this.startMs = System.currentTimeMillis();
        this.done = false;

        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");

        ReloadEngine.INSTANCE.reloadAsync(modsDir, msg -> {
            stageMsg = msg;
            log.add(msg);
            // map stage message to progress bucket
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
        returnBtn = Button.builder(
                Component.literal("Back"),
                btn -> closeOrReturn()
        ).bounds(this.width / 2 - 60, this.height / 2 + 62, 120, 20).build();
        returnBtn.visible = false;
        this.addRenderableWidget(returnBtn);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return returnBtn != null && returnBtn.visible;
    }

    @Override
    public void tick() {
        super.tick();
        // smooth progress interpolation — 15% per tick toward target
        float gap = targetProg - displayProg;
        displayProg += gap * 0.18f;
        if (Math.abs(gap) < 0.005f) displayProg = targetProg;

        // animated dots
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
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float dt) {
        int w = this.width;
        int h = this.height;
        int midX = w / 2;
        int midY = h / 2;

        // Soft translucent vignette so background remains visible
        g.fill(0, 0, w, h, 0x55000000);

        // Centered floating card
        int cardW = 300;
        int cardH = 110;
        int cLeft = midX - cardW / 2;
        int cTop = midY - cardH / 2;
        int cRight = midX + cardW / 2;
        int cBottom = midY + cardH / 2;

        g.fill(cLeft, cTop, cRight, cBottom, 0xD0111827);
        g.fill(cLeft, cTop, cRight, cTop + 1, 0x50FFFFFF);
        g.fill(cLeft, cBottom - 1, cRight, cBottom, 0x50FFFFFF);
        g.fill(cLeft, cTop, cLeft + 1, cBottom, 0x50FFFFFF);
        g.fill(cRight - 1, cTop, cRight, cBottom, 0x50FFFFFF);

        // Title
        g.centeredText(this.font, Component.literal("§f§lReloading Mods"), midX, cTop + 12, 0xFFFFFFFF);

        // Stage label with animated dots
        ReloadResult res = result.get();
        boolean failed = res != null && !res.success();
        String dots = ".".repeat(dotCount);
        String label = stageMsg + (failed ? "" : dots);
        int stageColor = failed ? RED : 0xFFCCCCCC;
        g.centeredText(this.font, Component.literal(label), midX, cTop + 32, stageColor);

        // Progress bar track
        int barW = 240;
        int barH = 4;
        int barX = midX - barW / 2;
        int barY = cTop + 54;

        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF2D3748);
        g.fill(barX, barY, barX + barW, barY + barH, DARK);

        // Fill
        float clampedProg = Math.min(1.0f, displayProg);
        int fillW = (int) (barW * clampedProg);
        if (fillW > 0) {
            int fillColor = failed ? RED : 0xFF22C55E;
            g.fill(barX, barY, barX + fillW, barY + barH, fillColor);
        }

        // Percentage & Elapsed
        int pct = (int) (clampedProg * 100);
        long elapsed = System.currentTimeMillis() - startMs;
        g.text(this.font, Component.literal("§7" + pct + "%"), barX, barY + 10, 0xFF888888);
        g.text(this.font, Component.literal("§7" + (elapsed / 1000) + "s"), barX + barW - 16, barY + 10, 0xFF888888);

        // Mod diff summary if finished
        if (res != null && res.diff() != null && res.diff().hasChanges()) {
            String summary = "§a+" + res.diff().added().size()
                    + " §c-" + res.diff().removed().size()
                    + " §e~" + res.diff().updated().size();
            g.centeredText(this.font, Component.literal(summary), midX, barY + 24, 0xFFFFFFFF);
        }

        super.extractRenderState(g, mx, my, dt);
    }
}
