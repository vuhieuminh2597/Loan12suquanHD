package com.loan12suquan.hd;

import com.loan12suquan.hd.platform.HdCanvas2D;
import com.loan12suquan.hd.platform.HdPlatform;
import com.loan12suquan.hd.platform.HdSurface;
import com.mic.smsgame.suquan.model.BattleFighter;
import javax.microedition.lcdui.Image;

/**
 * 100% Native Vector HD Battle Stage, 8x8 Chessboard, Combatant HUD & Victory/Defeat Banner Renderer,
 * shared across Desktop (Java2D/OpenGL) and Web / iPhone 17 PWA (HTML5 Canvas2D).
 */
public final class HdBattleStageRenderer {
    private static HdSurface cachedStageBackdrop;
    private static boolean cachedBossTheme;

    private HdBattleStageRenderer() {
    }

    private static void setGradient2(HdCanvas2D g2, double x1, double y1, double x2, double y2, int argb1, int argb2) {
        g2.setLinearGradient(x1, y1, x2, y2, new float[]{0f, 1f}, new int[]{argb1, argb2});
    }

    /**
     * Renders (and caches) the static {@code 720x960} HD Ancient Citadel & 8x8 Gem Altar stage.
     */
    public static HdSurface getOrCreateStageBackdrop(boolean bossTheme) {
        if (cachedStageBackdrop != null && cachedBossTheme == bossTheme) {
            return cachedStageBackdrop;
        }
        final int W = HdConfig.PLAYFIELD_HD_WIDTH;   // 720
        final int H = HdConfig.PLAYFIELD_HD_HEIGHT;  // 960
        final int s = HdConfig.HD_SCALE;             // 3

        HdSurface img = HdPlatform.backend().createSurface(W, H, false);
        HdCanvas2D g2 = img.getCanvas2D();

        // 1. Atmospheric ancient battlefield sky & temple hall gradient
        int skyTop = bossTheme ? HdUiRenderer.rgb(0x2B0E14) : HdUiRenderer.rgb(0x14242B);
        int skyMid = bossTheme ? HdUiRenderer.rgb(0x1C0D0A) : HdUiRenderer.rgb(0x1B140E);
        int floorBot = HdUiRenderer.rgb(0x0E0805);
        g2.setLinearGradient(
                0, 0, 0, H,
                new float[]{0f, 0.55f, 1f},
                new int[]{skyTop, skyMid, floorBot});
        g2.fillRect(0, 0, W, H);

        // Subtle warm brazier glow behind the 8x8 board
        g2.setRadialGradient(
                W * 0.5f, 414f, 390f,
                new float[]{0f, 0.7f, 1f},
                new int[]{
                        bossTheme ? HdUiRenderer.rgba(0xDC4B2D, 70) : HdUiRenderer.rgba(0xEBAF4B, 55),
                        HdUiRenderer.rgba(0x3C1E0F, 25),
                        HdUiRenderer.rgba(0x000000, 0)
                });
        g2.fillRect(0, 80, W, 680);

        // 2. Left & Right Carved Bronze Temple Pillars
        drawTemplePillar(g2, 8, 108, 88, 576, bossTheme);
        drawTemplePillar(g2, W - 96, 108, 88, 576, bossTheme);

        // 3. Ornate Imperial Arch Header above the 8x8 board
        int archX = 86;
        int archY = 114;
        int archW = W - 172;
        int archH = 46;
        g2.setLinearGradient(
                archX, archY, archX, archY + archH,
                new float[]{0f, 0.5f, 1f},
                new int[]{
                        bossTheme ? HdUiRenderer.rgb(0x6E1B22) : HdUiRenderer.rgb(0x472C1A),
                        HdUiRenderer.rgb(0x2B190E),
                        HdUiRenderer.rgb(0x140B06)
                });
        g2.fillRoundRect(archX, archY, archW, archH, 18);
        setGradient2(g2, archX, archY, archX, archY + archH, HdUiRenderer.rgb(0xFFF2A6), HdUiRenderer.rgb(0x9E702B));
        g2.setStrokeWidth(2.8f);
        g2.drawRoundRect(archX, archY, archW, archH, 18);
        HdUiRenderer.drawHdDivider(g2, 120, 44, 150);

        // 4. Outer Altar Frame around the 8x8 Gem Board
        int bx = 36 * s;
        int by = 54 * s;
        int bSize = 168 * s;

        g2.setColorRgba(0, 0, 0, 165);
        g2.fillRoundRect(bx - 12, by - 10, bSize + 24, bSize + 24, 18);

        g2.setLinearGradient(
                bx - 10, by - 10, bx - 10, by + bSize + 10,
                new float[]{0f, 0.5f, 1f},
                new int[]{
                        bossTheme ? HdUiRenderer.rgb(0x8C2832) : HdUiRenderer.rgb(0x1D5E52),
                        HdUiRenderer.rgb(0x24160E),
                        HdUiRenderer.rgb(0x120A06)
                });
        g2.fillRoundRect(bx - 10, by - 10, bSize + 20, bSize + 20, 16);

        setGradient2(g2, bx, by - 10, bx, by + bSize + 10, HdUiRenderer.rgb(0xFFEBA6), HdUiRenderer.rgb(0x8F6528));
        g2.setStrokeWidth(3.2f);
        g2.drawRoundRect(bx - 10, by - 10, bSize + 20, bSize + 20, 16);

        // 5. Recessed 8x8 Alternating Beveled Stone/Mahogany Chessboard Cells (63x63 each)
        int cellHd = 21 * s;
        for (int r = 0; r < 8; ++r) {
            for (int c = 0; c < 8; ++c) {
                int cx = bx + c * cellHd;
                int cy = by + r * cellHd;
                boolean even = ((r + c) & 1) == 0;
                int tileTop = even ? HdUiRenderer.rgb(0x2B1B11) : HdUiRenderer.rgb(0x21140C);
                int tileBot = even ? HdUiRenderer.rgb(0x1F130B) : HdUiRenderer.rgb(0x170D07);
                setGradient2(g2, cx, cy, cx, cy + cellHd, tileTop, tileBot);
                g2.fillRect(cx, cy, cellHd, cellHd);

                g2.setColorRgba(255, 220, 150, even ? 22 : 12);
                g2.setStrokeWidth(1.0f);
                g2.drawRoundRect(cx + 4, cy + 4, cellHd - 8, cellHd - 8, 8);

                g2.setColorRgba(0, 0, 0, 95);
                g2.drawRect(cx, cy, cellHd - 1, cellHd - 1);
            }
        }

        g2.setColorHexAlpha(0xD8AD5E, 165);
        g2.setStrokeWidth(2.0f);
        g2.drawRect(bx - 1, by - 1, bSize + 2, bSize + 2);

        // 6. Lower Combatant Platform & Đông Sơn Meander Trim
        int platY = 228 * s;
        int platH = 62 * s;
        g2.setLinearGradient(
                0, platY, 0, platY + platH,
                new float[]{0f, 0.3f, 1f},
                new int[]{HdUiRenderer.rgb(0x332014), HdUiRenderer.rgb(0x1E120A), HdUiRenderer.rgb(0x100905)});
        g2.fillRect(0, platY, W, platH);

        g2.setColor(0x9E7332);
        g2.fillRect(0, platY, W, 3);
        g2.setColor(0xFFE680);
        g2.fillRect(48, platY + 1, W - 96, 1);

        drawMeanderWing(g2, 10, 238 * s, 22 * s, 28 * s);
        drawMeanderWing(g2, W - 10 - 22 * s, 238 * s, 22 * s, 28 * s);

        cachedStageBackdrop = img;
        cachedBossTheme = bossTheme;
        return img;
    }

    private static void drawTemplePillar(HdCanvas2D g2, int x, int y, int w, int h, boolean bossTheme) {
        g2.setLinearGradient(
                x, y, x + w, y,
                new float[]{0f, 0.35f, 0.7f, 1f},
                new int[]{
                        HdUiRenderer.rgb(0x1C1009),
                        bossTheme ? HdUiRenderer.rgb(0x4D1A1E) : HdUiRenderer.rgb(0x3D2617),
                        HdUiRenderer.rgb(0x26160D),
                        HdUiRenderer.rgb(0x120905)
                });
        g2.fillRoundRect(x, y, w, h, 16);

        g2.setColorHexAlpha(0xD8AD5E, 95);
        g2.setStrokeWidth(2.0f);
        g2.drawRoundRect(x + 4, y + 4, w - 8, h - 8, 12);

        g2.setColorHexAlpha(0xF5D27E, 48);
        g2.setStrokeWidth(2.2f);
        for (int py = y + 36; py < y + h - 36; py += 32) {
            g2.drawLine(x + 12, py + 12, x + w / 2, py);
            g2.drawLine(x + w / 2, py, x + w - 12, py + 12);
        }
    }

    private static void drawMeanderWing(HdCanvas2D g2, int hx, int hy, int hw, int hh) {
        g2.setColor(0x26170E);
        g2.fillRoundRect(hx, hy, hw, hh, 10);
        g2.setColorHexAlpha(0xE0B25B, 155);
        g2.setStrokeWidth(2.0f);
        g2.drawRoundRect(hx + 2, hy + 2, hw - 4, hh - 4, 8);
        g2.drawRoundRect(hx + 9, hy + 9, hw - 18, hh - 18, 4);
        g2.drawOval(hx + hw / 2 - 8, hy + hh / 2 - 8, 16, 16);
    }

    /**
     * Renders a sleek glowing active-turn aura around the 8x8 Gem Board in HD mode.
     */
    public static void drawBoardTurnAura(
            HdCanvas2D g2, int activeTurnIndex, boolean hideTurnBorder, int visualFrameCounter) {
        if (g2 == null || hideTurnBorder) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        int bx = 36 * s;
        int by = 54 * s;
        int bSize = 168 * s;

        float pulse = 0.55f + 0.45f * (float) Math.sin(visualFrameCounter * 0.14);
        if (activeTurnIndex == 0) {
            g2.setColorRgba(65, 240, 215, (int) (105 * pulse));
        } else {
            g2.setColorRgba(245, 78, 138, (int) (105 * pulse));
        }
        g2.setStrokeWidth(7.5f);
        g2.drawRoundRect(bx - 5, by - 5, bSize + 10, bSize + 10, 14);

        g2.setColor(activeTurnIndex == 0 ? 0x52F2DC : 0xFF70A6);
        g2.setStrokeWidth(2.6f);
        g2.drawRoundRect(bx - 4, by - 4, bSize + 8, bSize + 8, 12);
    }

    /**
     * Renders both combatants' HD Portraits, Active-Turn Aura, and 3 Glossy Stat Gauges (Máu, Lương, Nộ).
     */
    public static void drawCombatantsLowerHud(
            HdCanvas2D g2,
            BattleFighter player,
            Image playerPortrait,
            int playerTrailHp,
            BattleFighter enemy,
            Image enemyPortrait,
            int enemyTrailHp,
            int activeTurnIndex,
            boolean hideTurnBorder,
            int visualFrameCounter) {
        if (g2 == null || player == null || enemy == null) {
            return;
        }
        drawSingleFighterHud(
                g2, player, playerPortrait, playerTrailHp,
                false, activeTurnIndex == 0 && !hideTurnBorder, visualFrameCounter);
        drawSingleFighterHud(
                g2, enemy, enemyPortrait, enemyTrailHp,
                true, activeTurnIndex == 1 && !hideTurnBorder, visualFrameCounter);
    }

    private static void drawSingleFighterHud(
            HdCanvas2D g2,
            BattleFighter f,
            Image portrait,
            int trailHp,
            boolean rightSide,
            boolean myTurn,
            int visualFrameCounter) {
        final int s = HdConfig.HD_SCALE;
        int cardLogicalX = rightSide ? 123 : 32;
        int cardLogicalY = 235;
        int cardLogicalW = 85;
        int cardLogicalH = 35;
        boolean hovered = HdInputState.isHovered(cardLogicalX, cardLogicalY, cardLogicalW, cardLogicalH);

        int hx = cardLogicalX * s;
        int hy = cardLogicalY * s;
        int hw = cardLogicalW * s;
        int hh = cardLogicalH * s;

        if (myTurn || hovered) {
            float pulse = myTurn ? (0.6f + 0.4f * (float) Math.sin(visualFrameCounter * 0.14)) : 0.7f;
            if (hovered) {
                g2.setColorRgba(255, 225, 110, 95);
            } else if (rightSide) {
                g2.setColorRgba(245, 78, 138, (int) (115 * pulse));
            } else {
                g2.setColorRgba(65, 240, 215, (int) (115 * pulse));
            }
            g2.fillRoundRect(hx - 5, hy - 4, hw + 10, hh + 8, 18);
        }

        int topBg = myTurn
                ? (rightSide ? HdUiRenderer.rgb(0x4D1426) : HdUiRenderer.rgb(0x17473F))
                : (hovered ? HdUiRenderer.rgb(0x3D2617) : HdUiRenderer.rgb(0x2B1B10));
        int botBg = HdUiRenderer.rgb(0x130B06);
        setGradient2(g2, hx, hy, hx, hy + hh, topBg, botBg);
        g2.fillRoundRect(hx, hy, hw, hh, 14);

        int rimCol = hovered
                ? 0xFFD700
                : (myTurn ? (rightSide ? 0xFF7AA8 : 0x66FFF0) : 0x9E7538);
        g2.setColor(rimCol);
        g2.setStrokeWidth((myTurn || hovered) ? 2.8f : 1.8f);
        g2.drawRoundRect(hx, hy, hw, hh, 14);

        int portLogicalX = rightSide ? 175 : 35;
        int portLogicalY = 237;
        int px = portLogicalX * s;
        int py = portLogicalY * s;
        int pw = 30 * s;
        int ph = 30 * s;

        g2.setColor(0x140B06);
        g2.fillRoundRect(px - 2, py - 2, pw + 4, ph + 4, 10);
        if (portrait != null && portrait.getActiveHdSurface() != null) {
            g2.drawSurface(portrait.getActiveHdSurface(), px, py, pw, ph);
        }
        g2.setColor(myTurn ? 0xFFE680 : 0xC29444);
        g2.setStrokeWidth(2.2f);
        g2.drawRoundRect(px - 2, py - 2, pw + 4, ph + 4, 10);

        int barLogicalX = rightSide ? 126 : 68;
        int barLogicalW = 46;
        int barH = 9;

        drawMiniFighterBar(
                g2, barLogicalX, 237, barLogicalW, barH,
                f.l(), f.e(), trailHp, f.getBarrierHp(), f.getMaxBarrierHp(),
                rightSide,
                0xFF8585, 0xD92638, 0x7A0E18, 0);

        drawMiniFighterBar(
                g2, barLogicalX, 248, barLogicalW, barH,
                f.p(), f.q(), 0, 0, 0,
                rightSide,
                0xB2FF7A, 0x45A824, 0x1E540E, 0);

        drawMiniFighterBar(
                g2, barLogicalX, 259, barLogicalW, barH,
                f.o(), f.n(), 0, 0, 0,
                rightSide,
                0x99F6FF, 0x1898B8, 0x0B4861, 3);
    }

    private static void drawMiniFighterBar(
            HdCanvas2D g2,
            int lx, int ly, int lw, int lh,
            int cur, int max, int trail, int barrier, int maxBarrier,
            boolean rightAlignFill,
            int cTop, int cMid, int cBot, int divisions) {
        final int s = HdConfig.HD_SCALE;
        int hx = lx * s;
        int hy = ly * s;
        int hw = lw * s;
        int hh = lh * s;

        g2.setColor(0x0D0604);
        g2.fillRoundRect(hx, hy, hw, hh, 8);

        int pad = 2;
        int ix = hx + pad;
        int iy = hy + pad;
        int iw = hw - pad * 2;
        int ih = hh - pad * 2;

        if (max > 0) {
            if (trail > cur) {
                int tw = Math.min(iw, (int) ((long) iw * Math.min(trail, max) / max));
                if (tw > 0) {
                    int tx = rightAlignFill ? (ix + iw - tw) : ix;
                    g2.setColor(0xFFE066);
                    g2.fillRoundRect(tx, iy, tw, ih, 6);
                }
            }
            if (cur > 0) {
                int clamped = Math.min(cur, max);
                int fw = Math.max(3, (int) ((long) iw * clamped / max));
                int fx = rightAlignFill ? (ix + iw - fw) : ix;
                g2.setLinearGradient(
                        fx, iy, fx, iy + ih,
                        new float[]{0f, 0.5f, 1f},
                        new int[]{HdUiRenderer.rgb(cTop), HdUiRenderer.rgb(cMid), HdUiRenderer.rgb(cBot)});
                g2.fillRoundRect(fx, iy, fw, ih, 6);

                if (cur > max) {
                    int ow = Math.min(iw, (int) ((long) iw * (cur - max) / max));
                    int ox = rightAlignFill ? (ix + iw - ow) : ix;
                    g2.setColorRgba(120, 255, 175, 195);
                    g2.fillRoundRect(ox, iy, ow, ih, 6);
                }

                g2.setColorRgba(255, 255, 255, 85);
                g2.fillRoundRect(fx + 2, iy + 1, Math.max(2, fw - 4), Math.max(2, ih / 3), 4);
            }
            if (barrier > 0 && maxBarrier > 0) {
                int bw = Math.max(4, Math.min(iw, (int) ((long) iw * barrier / maxBarrier)));
                int bx = rightAlignFill ? (ix + iw - bw) : ix;
                g2.setColor(0x42F5FF);
                g2.fillRect(bx, iy + ih - 5, bw, 5);
            }
        }

        if (divisions == 3) {
            for (int t = 1; t < 3; ++t) {
                int tx = ix + (iw * t) / 3;
                g2.setColor(0x0D0604);
                g2.fillRect(tx - 1, iy, 2, ih);
            }
        }

        g2.setColor(0x8F6932);
        g2.setStrokeWidth(1.5f);
        g2.drawRoundRect(hx, hy, hw, hh, 8);
    }

    /**
     * Renders a dramatic 100% HD Imperial Victory ("ĐẠI THẮNG!") or Defeat ("BẠI TRẬN") plaque.
     */
    public static void drawHdResultBanner(HdCanvas2D g2, int logicalCenterX, int logicalCenterY, boolean victory, float scale) {
        if (g2 == null) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        int cx = logicalCenterX * s;
        int cy = logicalCenterY * s;
        int w = (int) (540 * scale);
        int h = (int) (132 * scale);
        int bx = cx - w / 2;
        int by = cy - h / 2;

        g2.setRadialGradient(
                cx, cy, w * 0.55f,
                new float[]{0f, 1f},
                new int[]{
                        victory ? HdUiRenderer.rgba(0xFFD750, 140) : HdUiRenderer.rgba(0xB42837, 130),
                        HdUiRenderer.rgba(0x000000, 0)
                });
        g2.fillOval(cx - w * 0.55f, cy - h * 0.8f, w * 1.1f, h * 1.6f);

        g2.setLinearGradient(
                bx, by, bx, by + h,
                new float[]{0f, 0.5f, 1f},
                victory
                        ? new int[]{HdUiRenderer.rgb(0x7A1E18), HdUiRenderer.rgb(0x470E0C), HdUiRenderer.rgb(0x210605)}
                        : new int[]{HdUiRenderer.rgb(0x2B2433), HdUiRenderer.rgb(0x18131F), HdUiRenderer.rgb(0x0B0810)});
        g2.fillRoundRect(bx, by, w, h, 28f);

        setGradient2(
                g2, bx, by, bx, by + h,
                victory ? HdUiRenderer.rgb(0xFFF5B8) : HdUiRenderer.rgb(0xC8B8D8),
                victory ? HdUiRenderer.rgb(0xC88A28) : HdUiRenderer.rgb(0x5E4E70));
        g2.setStrokeWidth(4.2f);
        g2.drawRoundRect(bx, by, w, h, 28f);

        HdFontRenderer.drawDisplayCenteredHd(
                g2,
                victory ? "ĐẠI THẮNG!" : "BẠI TRẬN",
                cx, cy + 16,
                true,
                victory ? 0xFFF9C4 : 0xE6DCEB,
                victory ? 0xF0A024 : 0x8A7899,
                victory ? 0x2B0805 : 0x120D1A,
                7.0f);
    }
}
