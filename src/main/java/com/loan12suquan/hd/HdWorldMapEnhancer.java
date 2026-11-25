package com.loan12suquan.hd;

import com.loan12suquan.hd.platform.HdCanvas2D;
import com.loan12suquan.hd.platform.HdPath;
import com.loan12suquan.hd.platform.HdSurface;
import com.mic.smsgame.suquan.data.WorldMapData;
import com.mic.smsgame.suquan.model.HeroStats;
import javax.microedition.lcdui.Image;

/**
 * 100% HD World Map Terrain Remaster & Vector Node/Road/Label/Hero HUD Renderer,
 * shared across Desktop (Java2D/OpenGL) and Web / iPhone 17 PWA (HTML5 Canvas2D).
 */
public final class HdWorldMapEnhancer {

    private HdWorldMapEnhancer() {
    }

    private static void setGradient2(HdCanvas2D g2, double x1, double y1, double x2, double y2, int argb1, int argb2) {
        g2.setLinearGradient(x1, y1, x2, y2, new float[]{0f, 1f}, new int[]{argb1, argb2});
    }

    /**
     * Cleans baked white+black bitmap text labels off the raw 1x {@code /m/m} map pixel array
     * by inpainting them from surrounding natural terrain pixels.
     */
    public static void inpaintBakedMapText(int[] pixels, int w, int h) {
        if (pixels == null || pixels.length < w * h) {
            return;
        }
        boolean[] isWhiteGlyph = new boolean[w * h];
        boolean[] mask = new boolean[w * h];

        for (int y = 2; y < h - 2; ++y) {
            int row = y * w;
            for (int x = 2; x < w - 2; ++x) {
                int c = pixels[row + x];
                int r = (c >>> 16) & 0xFF;
                int g = (c >>> 8) & 0xFF;
                int b = c & 0xFF;
                int maxC = Math.max(r, Math.max(g, b));
                int minC = Math.min(r, Math.min(g, b));
                if (minC >= 210 && (maxC - minC) <= 22) {
                    boolean nearBlack = false;
                    for (int dy = -2; dy <= 2 && !nearBlack; ++dy) {
                        for (int dx = -2; dx <= 2; ++dx) {
                            if (isDarkOutline(pixels[(y + dy) * w + (x + dx)])) {
                                nearBlack = true;
                                break;
                            }
                        }
                    }
                    if (nearBlack) {
                        isWhiteGlyph[row + x] = true;
                        mask[row + x] = true;
                    }
                }
            }
        }

        for (int y = 3; y < h - 3; ++y) {
            int row = y * w;
            for (int x = 3; x < w - 3; ++x) {
                if (!isWhiteGlyph[row + x]) {
                    continue;
                }
                for (int dy = -3; dy <= 3; ++dy) {
                    for (int dx = -3; dx <= 3; ++dx) {
                        int nIdx = (y + dy) * w + (x + dx);
                        int nc = pixels[nIdx];
                        int nr = (nc >>> 16) & 0xFF;
                        int ng = (nc >>> 8) & 0xFF;
                        int nb = nc & 0xFF;
                        int maxC = Math.max(nr, Math.max(ng, nb));
                        int minC = Math.min(nr, Math.min(ng, nb));
                        if (maxC < 105 || (maxC - minC <= 32 && maxC > 95)) {
                            mask[nIdx] = true;
                        }
                    }
                }
            }
        }

        int[] work = new int[w * h];
        System.arraycopy(pixels, 0, work, 0, pixels.length);
        boolean[] filled = new boolean[w * h];
        for (int i = 0; i < mask.length; ++i) {
            filled[i] = !mask[i];
        }

        for (int pass = 0; pass < 10; ++pass) {
            boolean anyRemaining = false;
            for (int y = 1; y < h - 1; ++y) {
                int row = y * w;
                for (int x = 1; x < w - 1; ++x) {
                    int idx = row + x;
                    if (filled[idx]) {
                        continue;
                    }
                    int sumR = 0, sumG = 0, sumB = 0, count = 0;
                    for (int dy = -1; dy <= 1; ++dy) {
                        for (int dx = -1; dx <= 1; ++dx) {
                            if (dx == 0 && dy == 0) continue;
                            int nIdx = (y + dy) * w + (x + dx);
                            if (filled[nIdx]) {
                                int nc = work[nIdx];
                                sumR += (nc >>> 16) & 0xFF;
                                sumG += (nc >>> 8) & 0xFF;
                                sumB += nc & 0xFF;
                                ++count;
                            }
                        }
                    }
                    if (count >= 2 || (pass >= 5 && count >= 1)) {
                        work[idx] = 0xFF000000
                                | ((sumR / count) << 16)
                                | ((sumG / count) << 8)
                                | (sumB / count);
                        filled[idx] = true;
                    } else {
                        anyRemaining = true;
                    }
                }
            }
            if (!anyRemaining) {
                break;
            }
        }

        for (int sPass = 0; sPass < 2; ++sPass) {
            for (int y = 1; y < h - 1; ++y) {
                int row = y * w;
                for (int x = 1; x < w - 1; ++x) {
                    int idx = row + x;
                    if (!mask[idx]) continue;
                    int sumR = 0, sumG = 0, sumB = 0;
                    for (int dy = -1; dy <= 1; ++dy) {
                        for (int dx = -1; dx <= 1; ++dx) {
                            int nc = work[(y + dy) * w + (x + dx)];
                            sumR += (nc >>> 16) & 0xFF;
                            sumG += (nc >>> 8) & 0xFF;
                            sumB += nc & 0xFF;
                        }
                    }
                    work[idx] = 0xFF000000 | ((sumR / 9) << 16) | ((sumG / 9) << 8) | (sumB / 9);
                }
            }
        }
        System.arraycopy(work, 0, pixels, 0, pixels.length);
    }

    private static boolean isDarkOutline(int argb) {
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;
        return r < 42 && g < 42 && b < 42;
    }

    public static void drawHdRoadDot(HdCanvas2D g2, int logicalX, int logicalY, boolean shimmerActive) {
        if (g2 == null) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        float cx = logicalX * s;
        float cy = logicalY * s;
        float r = shimmerActive ? 6.5f : 5.2f;

        g2.setColorRgba(0, 0, 0, 130);
        g2.fillOval(cx - r + 1f, cy - r + 1.5f, r * 2f, r * 2f);

        g2.setRadialGradient(
                cx - 1.5f, cy - 1.5f, r,
                new float[]{0f, 1f},
                shimmerActive
                        ? new int[]{HdUiRenderer.rgb(0xFFF9C4), HdUiRenderer.rgb(0xE6A12E)}
                        : new int[]{HdUiRenderer.rgb(0x99F6FF), HdUiRenderer.rgb(0x1E6BA8)});
        g2.fillOval(cx - r, cy - r, r * 2f, r * 2f);

        g2.setColor(0xFFF2A6);
        g2.setStrokeWidth(1.2f);
        g2.drawOval(cx - r, cy - r, r * 2f, r * 2f);
    }

    public static void drawHdMapNode(HdCanvas2D g2, int logicalX, int logicalY, boolean isCitadel, boolean enemyOwned, boolean hovered) {
        if (g2 == null) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        float cx = logicalX * s;
        float cy = logicalY * s;
        float r = isCitadel ? (hovered ? 15f : 13f) : (hovered ? 12f : 10f);

        if (hovered) {
            g2.setColorRgba(255, 225, 110, 95);
            g2.fillOval(cx - r - 5f, cy - r - 5f, (r + 5f) * 2f, (r + 5f) * 2f);
        }

        g2.setColorRgba(0, 0, 0, 145);
        g2.fillOval(cx - r + 1f, cy - r + 2f, r * 2f, r * 2f);

        int cInner = enemyOwned ? HdUiRenderer.rgb(0xFF7070) : HdUiRenderer.rgb(0x7CFFDC);
        int cOuter = enemyOwned ? HdUiRenderer.rgb(0x9E1922) : HdUiRenderer.rgb(0x146E5B);
        g2.setRadialGradient(
                cx - 2f, cy - 2f, r,
                new float[]{0f, 1f},
                new int[]{cInner, cOuter});
        g2.fillOval(cx - r, cy - r, r * 2f, r * 2f);

        g2.setColor(0xFFE680);
        g2.setStrokeWidth(2.2f);
        g2.drawOval(cx - r, cy - r, r * 2f, r * 2f);
    }

    public static void drawHdCitadelNameplates(
            HdCanvas2D g2,
            int camOffsetX,
            int camOffsetY,
            int currentNodeIdx,
            boolean[] nodeUnlocked) {
        if (g2 == null) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        for (int i = 0; i < WorldMapData.a.length; ++i) {
            String name = WorldMapData.a[i];
            if (name == null || name.isEmpty()) {
                continue;
            }
            int lx = camOffsetX + WorldMapData.b[i][0];
            int ly = camOffsetY + WorldMapData.b[i][1];
            if (lx < 16 || lx > 224 || ly < 28 || ly > 296) {
                continue;
            }
            if (lx < 126 && (ly - 21) < 32) {
                continue;
            }
            boolean unlocked = (nodeUnlocked != null && i < nodeUnlocked.length && nodeUnlocked[i]);
            boolean isCurrent = (i == currentNodeIdx);

            int hx = lx * s;
            int hy = (ly - 21) * s;
            int textW = HdFontRenderer.measureSubtitleHdWidth(g2, name);
            int pw = textW + 14;
            int ph = 22;

            g2.setColorRgba(14, 8, 4, unlocked ? 185 : 120);
            g2.fillRoundRect(hx - pw / 2, hy - ph + 4, pw, ph, 10);
            if (isCurrent || unlocked) {
                if (isCurrent) {
                    g2.setColor(0xFFD700);
                } else {
                    g2.setColorHexAlpha(0xCCA052, 145);
                }
                g2.setStrokeWidth(isCurrent ? 2.0f : 1.2f);
                g2.drawRoundRect(hx - pw / 2, hy - ph + 4, pw, ph, 10);
            }

            int fg = isCurrent ? 0xFFF5B8 : (unlocked ? 0xFFF4DC : 0xC4B49E);
            HdFontRenderer.drawSubtitleHd(g2, name, hx, hy, true, fg, 0x0D0603);
        }
    }

    public static void drawHdHeroToken(HdCanvas2D g2, int logicalX, int logicalY, Image avatarSheet, int avatarId, int animTick) {
        if (g2 == null) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        float cx = logicalX * s;
        float cy = logicalY * s;
        float r = 21f;

        float ringR = r + 5f + (animTick % 8) * 0.6f;
        g2.setColorRgba(100, 255, 225, 115);
        g2.setStrokeWidth(2.5f);
        g2.drawOval(cx - ringR, cy - ringR, ringR * 2f, ringR * 2f);

        g2.setColorRgba(0, 0, 0, 165);
        g2.fillOval(cx - r + 1.5f, cy - r + 3f, r * 2f, r * 2f);

        g2.save();
        g2.clipOval(cx - r, cy - r, r * 2f, r * 2f);
        g2.setColor(0x1D4A42);
        g2.fillOval(cx - r, cy - r, r * 2f, r * 2f);
        if (avatarSheet != null && avatarSheet.getActiveHdSurface() != null && avatarId >= 0) {
            HdSurface sheetBuf = avatarSheet.getActiveHdSurface();
            int cell = 30 * (sheetBuf.getWidth() >= 900 ? s : 1);
            int sx = (avatarId % 10) * cell;
            int sy = (avatarId / 10) * cell;
            if (sx + cell <= sheetBuf.getWidth() && sy + cell <= sheetBuf.getHeight()) {
                int dx1 = (int) (cx - r);
                int dy1 = (int) (cy - r);
                int dx2 = (int) (cx + r);
                int dy2 = (int) (cy + r);
                g2.drawSurfaceRegion(sheetBuf, sx, sy, cell, cell, dx1, dy1, dx2 - dx1, dy2 - dy1, false, false);
            }
        }
        g2.restore();

        setGradient2(g2, cx, cy - r, cx, cy + r, HdUiRenderer.rgb(0xFFF5B8), HdUiRenderer.rgb(0xC8932E));
        g2.setStrokeWidth(3.2f);
        g2.drawOval(cx - r, cy - r, r * 2f, r * 2f);

        float bob = (float) Math.sin(animTick * 0.25) * 4.5f;
        float py = cy - r - 14f + bob;
        HdPath pointer = g2.createPath();
        pointer.moveTo(cx, py + 10f);
        pointer.lineTo(cx - 9f, py - 5f);
        pointer.lineTo(cx + 9f, py - 5f);
        pointer.closePath();
        g2.setColorRgba(0, 0, 0, 150);
        g2.fillPath(pointer);
        g2.setColor(0xFFD700);
        g2.fillPath(pointer);
        g2.setColor(0x472808);
        g2.setStrokeWidth(1.5f);
        g2.drawPath(pointer);
    }

    public static void drawHdSideQuestBeacon(HdCanvas2D g2, int logicalX, int logicalY, int pulseFrame) {
        if (g2 == null) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        float cx = logicalX * s;
        float cy = logicalY * s - 32f + (float) Math.sin(pulseFrame * 0.3) * 3.5f;

        HdPath pennant = g2.createPath();
        pennant.moveTo(cx, cy + 25f);
        pennant.lineTo(cx - 13f, cy - 6f);
        pennant.quadTo(cx, cy - 15f, cx + 13f, cy - 6f);
        pennant.closePath();

        g2.setColorRgba(0, 0, 0, 145);
        g2.translate(1.5, 2.0);
        g2.fillPath(pennant);
        g2.translate(-1.5, -2.0);

        setGradient2(g2, cx, cy - 14f, cx, cy + 24f, HdUiRenderer.rgb(0x38E8D8), HdUiRenderer.rgb(0x0D525E));
        g2.fillPath(pennant);
        g2.setColor(0xC8FFF8);
        g2.setStrokeWidth(2.0f);
        g2.drawPath(pennant);

        HdFontRenderer.drawSubtitleHd(g2, "?", (int) cx, (int) cy + 11, true, 0xFFFFFF, 0x061D24);
    }

    public static void drawHdMiniThreatBadge(HdCanvas2D g2, int logicalCx, int logicalTopY, int enemyLv, int accentRgb) {
        if (g2 == null) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        String tag = "C." + enemyLv;
        int textW = HdFontRenderer.measureSubtitleHdWidth(g2, tag);
        int pw = textW + 14;
        int ph = 21;
        int hx = logicalCx * s - pw / 2;
        int hy = logicalTopY * s;
        if (hx < 6) hx = 6;
        if (hx + pw > 240 * s - 6) hx = 240 * s - 6 - pw;
        if (hy < 6 || hy + ph > (320 - 24) * s) return;

        g2.setColorRgba(14, 8, 5, 215);
        g2.fillRoundRect(hx, hy, pw, ph, 9);
        g2.setColor(accentRgb);
        g2.setStrokeWidth(1.6f);
        g2.drawRoundRect(hx, hy, pw, ph, 9);
        HdFontRenderer.drawSubtitleHd(g2, tag, hx + pw / 2, hy + 16, true, accentRgb, 0x0A0503);
    }

    public static void drawHdMainQuestBeacon(HdCanvas2D g2, int logicalX, int logicalY, int pulseFrame) {
        if (g2 == null) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        float cx = logicalX * s;
        float cy = logicalY * s - 34f + (float) Math.sin(pulseFrame * 0.35) * 4f;

        HdPath pennant = g2.createPath();
        pennant.moveTo(cx, cy + 28f);
        pennant.lineTo(cx - 14f, cy - 8f);
        pennant.quadTo(cx, cy - 18f, cx + 14f, cy - 8f);
        pennant.closePath();

        g2.setColorRgba(0, 0, 0, 150);
        g2.translate(1.5, 2.5);
        g2.fillPath(pennant);
        g2.translate(-1.5, -2.5);

        setGradient2(g2, cx, cy - 16f, cx, cy + 26f, HdUiRenderer.rgb(0xFF4D4D), HdUiRenderer.rgb(0x8C1118));
        g2.fillPath(pennant);
        g2.setColor(0xFFE680);
        g2.setStrokeWidth(2.4f);
        g2.drawPath(pennant);

        HdFontRenderer.drawSubtitleHd(g2, "!", (int) cx, (int) cy + 11, true, 0xFFF9C4, 0x2B0606);
    }

    public static void drawTopLeftHeroMiniPill(HdCanvas2D g2, HeroStats hero, int gold, boolean hovered) {
        if (g2 == null || hero == null) {
            return;
        }
        int pillX = 3;
        int pillY = 3;
        int pillW = 112;
        int pillH = 22;

        HdUiRenderer.drawHdAntiquePanel(
                g2, pillX, pillY, pillW, pillH,
                hovered ? 0x246B5C : 0x2B1B10,
                hovered ? 0x154239 : 0x1A1009,
                0x0D0704,
                hovered ? 0xFFD700 : 0xB8893D,
                0xFFF2A6, 0xFFE680, pillW);

        String topLine = "C." + hero.e + "  |  " + gold + "v";
        HdFontRenderer.drawHdShadowText(
                g2, true, topLine,
                pillX + (pillW >> 1), pillY + 2,
                16 | 1, 0xFFE680, 0x100603, pillW - 10, 10);

        HdUiRenderer.drawHdRpgStatBar(
                g2, pillX + 5, pillY + 13, pillW - 10, 6,
                hero.c, hero.b,
                0xFF8585, 0xCC2424, 0x7A0E14, 0xFFD6D6,
                0, null, 0);
    }
}
