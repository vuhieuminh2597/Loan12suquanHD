package com.loan12suquan.hd;

import com.loan12suquan.hd.platform.HdCanvas2D;
import com.loan12suquan.hd.platform.HdPath;
import com.loan12suquan.hd.platform.HdPlatform;

/**
 * 100% Native Vector "Cổ Phong Hoàng Kim" UI Component Library ({@code 720x960} {@code 3x} HD),
 * shared across Desktop (Java2D/OpenGL) and Web / iPhone 17 PWA (HTML5 Canvas2D).
 */
public final class HdUiRenderer {

    private HdUiRenderer() {
    }

    public static int rgb(int hex) {
        return HdPlatform.opaque(hex);
    }

    public static int rgba(int hex, int alpha) {
        return HdPlatform.argb(hex, alpha);
    }

    public static void setArgb(HdCanvas2D g2, int argb) {
        g2.setColorRgba(
                (argb >>> 16) & 0xFF,
                (argb >>> 8) & 0xFF,
                argb & 0xFF,
                (argb >>> 24) & 0xFF);
    }

    private static void setGradient2(HdCanvas2D g2, double x1, double y1, double x2, double y2, int argb1, int argb2) {
        g2.setLinearGradient(x1, y1, x2, y2, new float[]{0f, 1f}, new int[]{argb1, argb2});
    }

    /**
     * Smooth translucent dark glass & radial vignette replacing J2ME diagonal zebra hatching.
     */
    public static void drawHdDimOverlay(HdCanvas2D g2, int x, int y, int w, int h, int baseRgb, int alpha) {
        if (g2 == null || w <= 0 || h <= 0) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        int hx = x * s;
        int hy = y * s;
        int hw = w * s;
        int hh = h * s;

        g2.setColorHexAlpha(baseRgb, alpha);
        g2.fillRect(hx, hy, hw, hh);

        if (w >= 180 && h >= 180) {
            float cx = hx + hw * 0.5f;
            float cy = hy + hh * 0.45f;
            float radius = Math.max(hw, hh) * 0.72f;
            g2.setRadialGradient(
                    cx, cy, radius,
                    new float[]{0.0f, 0.65f, 1.0f},
                    new int[]{rgba(0x000000, 0), rgba(0x080402, 55), rgba(0x060301, 145)});
            g2.fillRect(hx, hy, hw, hh);
        }
    }

    /**
     * Renders an ornate HD antique wood-and-bronze plaque with corner filigree accents.
     */
    public static void drawHdAntiquePanel(
            HdCanvas2D g2,
            int x,
            int y,
            int w,
            int h,
            int bgTop,
            int bgBody,
            int bgBottom,
            int borderOuter,
            int borderHighlight,
            int rivetColor,
            int expectedTextLogicalW) {
        if (g2 == null || w < 4 || h < 4) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        int hx = x * s;
        int hy = y * s;
        int hw = w * s;
        int hh = h * s;
        float arc = Math.min(12f, Math.min(hw, hh) * 0.24f);

        // 1. Soft outer drop shadow
        g2.setColorRgba(0, 0, 0, 105);
        g2.fillRoundRect(hx + 2f, hy + 3.5f, hw - 1f, hh - 1f, arc + 2f);

        // 2. Smooth 3-stop lacquered wood gradient interior
        g2.setLinearGradient(
                hx, hy, hx, hy + hh,
                new float[]{0.0f, 0.38f, 1.0f},
                new int[]{rgb(bgTop), rgb(bgBody), rgb(bgBottom)});
        g2.fillRoundRect(hx + 1.5f, hy + 1.5f, hw - 3f, hh - 3f, arc);

        // 3. Top inner silk-lacquer sheen
        if (hh >= 24) {
            setGradient2(
                    g2,
                    hx, hy + 3f,
                    hx, hy + Math.min(20f, hh * 0.35f),
                    rgba(borderHighlight, 52),
                    rgba(borderHighlight, 0));
            g2.fillRoundRect(hx + 4f, hy + 3f, hw - 8f, Math.min(18f, hh * 0.35f), arc * 0.7f);
        }

        // 4. Inner recessed dark rim
        g2.setColorHexAlpha(0x0D0704, 210);
        g2.setStrokeWidth(1.8f);
        g2.drawRoundRect(hx + 3.5f, hy + 3.5f, hw - 7f, hh - 7f, Math.max(2f, arc - 2f));

        // 5. Outer burnished bronze/gold frame
        setGradient2(g2, hx, hy, hx, hy + hh, rgb(borderHighlight), rgb(borderOuter));
        g2.setStrokeWidth(2.4f);
        g2.drawRoundRect(hx + 1.5f, hy + 1.5f, hw - 3f, hh - 3f, arc);

        // 6. Top metallic gleam
        g2.setColorHexAlpha(borderHighlight, 195);
        g2.setStrokeWidth(1.2f);
        g2.drawLine(hx + (int) arc, hy + 3, hx + hw - (int) arc, hy + 3);

        // 7. Corner filigree brackets on large dialog cards, or rivets on compact plaques
        if (hw >= 180 && hh >= 140) {
            drawCornerFiligree(g2, hx, hy, hw, hh, rgba(borderHighlight, 175));
        } else if (hw >= 36 && hh >= 24) {
            int inset = 5;
            int[][] corners = new int[][]{
                    {hx + inset, hy + inset},
                    {hx + hw - inset - 3, hy + inset},
                    {hx + inset, hy + hh - inset - 3},
                    {hx + hw - inset - 3, hy + hh - inset - 3}
            };
            for (int i = 0; i < corners.length; ++i) {
                g2.setColorHexAlpha(0x120804, 200);
                g2.fillOval(corners[i][0] + 1, corners[i][1] + 1, 4, 4);
                g2.setColor(rivetColor);
                g2.fillOval(corners[i][0], corners[i][1], 3, 3);
            }
        }
    }

    private static void drawCornerFiligree(HdCanvas2D g2, int hx, int hy, int hw, int hh, int argbCol) {
        setArgb(g2, argbCol);
        g2.setStrokeWidth(1.6f);
        int m = 7;
        int len = 14;
        // Top-left
        g2.drawLine(hx + m, hy + m, hx + m + len, hy + m);
        g2.drawLine(hx + m, hy + m, hx + m, hy + m + len);
        g2.drawRect(hx + m, hy + m, 5, 5);
        // Top-right
        g2.drawLine(hx + hw - m - len, hy + m, hx + hw - m, hy + m);
        g2.drawLine(hx + hw - m, hy + m, hx + hw - m, hy + m + len);
        g2.drawRect(hx + hw - m - 5, hy + m, 5, 5);
        // Bottom-left
        g2.drawLine(hx + m, hy + hh - m, hx + m + len, hy + hh - m);
        g2.drawLine(hx + m, hy + hh - m - len, hx + m, hy + hh - m);
        g2.drawRect(hx + m, hy + hh - m - 5, 5, 5);
        // Bottom-right
        g2.drawLine(hx + hw - m - len, hy + hh - m, hx + hw - m, hy + hh - m);
        g2.drawLine(hx + hw - m, hy + hh - m - len, hx + hw - m, hy + hh - m);
        g2.drawRect(hx + hw - m - 5, hy + hh - m - 5, 5, 5);
    }

    /**
     * 100% Vector HD Imperial Rosewood & Gold Scroll Frame replacing {@code /frame1} & {@code /frame2}.
     */
    public static void drawHdScrollFrame(HdCanvas2D g2, int x, int y, int w, int h, int bodyColor) {
        if (g2 == null || w < 20 || h < 20) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        int hx = x * s;
        int hy = y * s;
        int hw = w * s;
        int hh = h * s;

        // 1. Hanging crimson silk cords & golden tassels on left and right
        drawSilkTassel(g2, hx + 6, hy + 26, hh - 52);
        drawSilkTassel(g2, hx + hw - 6, hy + 26, hh - 52);

        // 2. Scroll parchment/lacquered wood body
        int bodyX = hx + 14;
        int bodyY = hy + 16;
        int bodyW = hw - 28;
        int bodyH = hh - 32;

        g2.setColorRgba(0, 0, 0, 130);
        g2.fillRoundRect(bodyX + 3, bodyY + 5, bodyW, bodyH, 10);

        g2.setLinearGradient(
                bodyX, bodyY, bodyX, bodyY + bodyH,
                new float[]{0f, 0.15f, 0.85f, 1f},
                new int[]{rgb(0x3E2717), rgb(bodyColor), rgb(0x21140B), rgb(0x160C06)});
        g2.fillRoundRect(bodyX, bodyY, bodyW, bodyH, 8);

        // Layered crimson & burnished gold scroll borders
        g2.setColor(0x6E201B);
        g2.setStrokeWidth(4.0f);
        g2.drawRoundRect(bodyX + 2, bodyY + 2, bodyW - 4, bodyH - 4, 6);

        setGradient2(g2, bodyX, bodyY, bodyX, bodyY + bodyH, rgb(0xF5D27E), rgb(0xA67530));
        g2.setStrokeWidth(2.2f);
        g2.drawRoundRect(bodyX + 5, bodyY + 5, bodyW - 10, bodyH - 10, 4);

        drawCornerFiligree(g2, bodyX + 2, bodyY + 2, bodyW - 4, bodyH - 4, rgba(0xFFE680, 185));

        // 3. Top and Bottom Cylindrical Rosewood & Jade Scroll Rollers
        drawScrollRoller(g2, hx, hy, hw, 22);
        drawScrollRoller(g2, hx, hy + hh - 22, hw, 22);
    }

    private static void drawScrollRoller(HdCanvas2D g2, int rx, int ry, int rw, int rh) {
        g2.setColorRgba(0, 0, 0, 120);
        g2.fillRoundRect(rx + 2, ry + 3, rw - 2, rh, 12);

        g2.setLinearGradient(
                rx, ry + 3, rx, ry + rh - 3,
                new float[]{0f, 0.4f, 1f},
                new int[]{rgb(0xFFEBA6), rgb(0xC9933B), rgb(0x6E4918)});
        g2.fillRoundRect(rx, ry + 3, 18, rh - 6, 8);
        g2.fillRoundRect(rx + rw - 18, ry + 3, 18, rh - 6, 8);

        int barX = rx + 10;
        int barW = rw - 20;
        g2.setLinearGradient(
                barX, ry, barX, ry + rh,
                new float[]{0f, 0.30f, 0.65f, 1f},
                new int[]{rgb(0x9E342B), rgb(0x6E1E18), rgb(0x3D100C), rgb(0x210806)});
        g2.fillRoundRect(barX, ry, barW, rh, 10);

        g2.setLinearGradient(
                barX, ry, barX, ry + rh,
                new float[]{0f, 0.45f, 1f},
                new int[]{rgb(0xFFF2B2), rgb(0xD49E42), rgb(0x7A541C)});
        int cw = 54;
        g2.fillRect(rx + (rw - cw) / 2, ry + 1, cw, rh - 2);
        g2.fillRect(barX + 18, ry + 1, 7, rh - 2);
        g2.fillRect(barX + barW - 25, ry + 1, 7, rh - 2);

        g2.setColorRgba(255, 240, 200, 95);
        g2.fillRoundRect(barX + 4, ry + 3, barW - 8, 4, 4);

        g2.setColor(0xE8BC66);
        g2.setStrokeWidth(1.8f);
        g2.drawRoundRect(barX, ry, barW, rh, 10);
    }

    private static void drawSilkTassel(HdCanvas2D g2, int cx, int topY, int totalH) {
        int cordH = Math.min(totalH, 165);
        g2.setColor(0xB82828);
        g2.setStrokeRound(3.0f);
        g2.drawLine(cx, topY, cx, topY + cordH);
        g2.setColor(0xFFD700);
        g2.fillOval(cx - 5, topY + cordH / 2 - 5, 10, 10);
        g2.setColor(0x7A4E12);
        g2.setStrokeWidth(1.2f);
        g2.drawOval(cx - 5, topY + cordH / 2 - 5, 10, 10);

        int fy = topY + cordH;
        g2.setColor(0xFFD700);
        g2.fillRoundRect(cx - 4, fy - 2, 8, 5, 3);
        g2.setColor(0xD93030);
        HdPath fringe = g2.createPath();
        fringe.moveTo(cx - 3, fy + 3);
        fringe.lineTo(cx + 3, fy + 3);
        fringe.lineTo(cx + 6, fy + 24);
        fringe.lineTo(cx - 6, fy + 24);
        fringe.closePath();
        g2.fillPath(fringe);
    }

    /**
     * 100% Vector HD Ornamental Gold & Jade Divider replacing {@code /frame3}.
     */
    public static void drawHdDivider(HdCanvas2D g2, int logicalCenterX, int logicalY, int logicalSpanW) {
        if (g2 == null) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        int cx = logicalCenterX * s;
        int cy = logicalY * s + 5;
        int halfW = (logicalSpanW * s) / 2;

        g2.setLinearGradient(
                cx - halfW, cy, cx + halfW, cy,
                new float[]{0f, 0.25f, 0.5f, 0.75f, 1f},
                new int[]{rgba(0xB8893D, 0), rgb(0xC89842), rgb(0xFFF2A6), rgb(0xC89842), rgba(0xB8893D, 0)});
        g2.setStrokeWidth(2.6f);
        g2.drawLine(cx - halfW, cy, cx + halfW, cy);

        HdPath diamond = g2.createPath();
        diamond.moveTo(cx, cy - 6);
        diamond.lineTo(cx + 9, cy);
        diamond.lineTo(cx, cy + 6);
        diamond.lineTo(cx - 9, cy);
        diamond.closePath();
        g2.setColor(0x165E52);
        g2.fillPath(diamond);
        g2.setColor(0xFFE680);
        g2.setStrokeWidth(1.8f);
        g2.drawPath(diamond);
    }

    /**
     * 100% Vector HD Beveled Item / Skill / Portrait Socket replacing pixelated {@code /focusitem}.
     */
    public static void drawHdItemSocket(
            HdCanvas2D g2, int x, int y, int w, int h,
            boolean selected, boolean hovered, boolean activeBuffGlow) {
        if (g2 == null) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        int hx = x * s;
        int hy = y * s;
        int hw = w * s;
        int hh = h * s;
        float arc = 14f;

        if (selected || hovered) {
            if (selected) {
                g2.setColorRgba(92, 245, 215, 85);
            } else {
                g2.setColorRgba(255, 215, 110, 65);
            }
            g2.fillRoundRect(hx - 4f, hy - 4f, hw + 8f, hh + 8f, arc + 6f);
        }

        int topCol = selected ? rgb(0x1E5E52) : (hovered ? rgb(0x47301E) : rgb(0x2E1D12));
        int botCol = selected ? rgb(0x0E2B25) : (hovered ? rgb(0x26180E) : rgb(0x160D08));
        setGradient2(g2, hx, hy, hx, hy + hh, topCol, botCol);
        g2.fillRoundRect(hx + 2f, hy + 2f, hw - 4f, hh - 4f, arc);

        g2.setColorRgba(0, 0, 0, 145);
        g2.setStrokeWidth(2.0f);
        g2.drawRoundRect(hx + 5f, hy + 5f, hw - 10f, hh - 10f, arc - 4f);

        int rimTop = activeBuffGlow ? rgb(0x7CFF9E) : (selected ? rgb(0xFFF2A6) : (hovered ? rgb(0xF5D27E) : rgb(0xC29444)));
        int rimBot = activeBuffGlow ? rgb(0x28A856) : (selected ? rgb(0x33D6C4) : (hovered ? rgb(0xB8893D) : rgb(0x6E4E24)));
        setGradient2(g2, hx, hy, hx, hy + hh, rimTop, rimBot);
        g2.setStrokeWidth(selected ? 3.2f : 2.2f);
        g2.drawRoundRect(hx + 2f, hy + 2f, hw - 4f, hh - 4f, arc);

        if (selected) {
            g2.setColor(0xFFE680);
            g2.setStrokeRound(2.8f);
            int b = 12;
            g2.drawLine(hx, hy + b, hx, hy);
            g2.drawLine(hx, hy, hx + b, hy);
            g2.drawLine(hx + hw - b, hy, hx + hw, hy);
            g2.drawLine(hx + hw, hy, hx + hw, hy + b);
            g2.drawLine(hx, hy + hh - b, hx, hy + hh);
            g2.drawLine(hx, hy + hh, hx + b, hy + hh);
            g2.drawLine(hx + hw - b, hy + hh, hx + hw, hy + hh);
            g2.drawLine(hx + hw, hy + hh - b, hx + hw, hy + hh);
        }
    }

    /**
     * 100% Vector HD Đông Sơn Bronze Drum & Golden Bell Sound Medallion replacing cartoonish {@code /_sound}.
     */
    public static void drawHdSoundMedallion(HdCanvas2D g2, int logicalCx, int logicalCy, int volumePct, int animTick) {
        if (g2 == null) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        float cx = logicalCx * s;
        float cy = logicalCy * s;
        float outerR = 112f;
        boolean active = volumePct > 0;

        g2.setRadialGradient(
                cx, cy, outerR + 24f,
                new float[]{0f, 0.7f, 1f},
                new int[]{
                        active ? rgba(0x38DCC3, 75) : rgba(0xB45A3C, 45),
                        rgba(0x142823, 25),
                        rgba(0x000000, 0)
                });
        g2.fillOval(cx - outerR - 24f, cy - outerR - 24f, (outerR + 24f) * 2f, (outerR + 24f) * 2f);

        g2.setRadialGradient(
                cx - 18f, cy - 22f, outerR * 1.15f,
                new float[]{0f, 0.6f, 1f},
                new int[]{
                        active ? rgb(0x285E54) : rgb(0x3B2618),
                        active ? rgb(0x13332D) : rgb(0x22150D),
                        rgb(0x0D0704)
                });
        g2.fillOval(cx - outerR, cy - outerR, outerR * 2f, outerR * 2f);

        g2.setColorHexAlpha(0xF5D27E, 75);
        g2.setStrokeWidth(1.5f);
        g2.drawOval(cx - outerR + 12f, cy - outerR + 12f, (outerR - 12f) * 2f, (outerR - 12f) * 2f);
        g2.drawOval(cx - outerR + 26f, cy - outerR + 26f, (outerR - 26f) * 2f, (outerR - 26f) * 2f);

        setGradient2(g2, cx, cy - outerR, cx, cy + outerR, rgb(0xFFF2A6), rgb(0x8F6528));
        g2.setStrokeWidth(4.2f);
        g2.drawOval(cx - outerR, cy - outerR, outerR * 2f, outerR * 2f);

        float bellX = cx - (active ? 22f : 6f);
        HdPath horn = g2.createPath();
        horn.moveTo(bellX - 28f, cy - 14f);
        horn.lineTo(bellX - 10f, cy - 14f);
        horn.lineTo(bellX + 18f, cy - 38f);
        horn.lineTo(bellX + 18f, cy + 38f);
        horn.lineTo(bellX - 10f, cy + 14f);
        horn.lineTo(bellX - 28f, cy + 14f);
        horn.closePath();

        g2.setLinearGradient(
                bellX, cy - 38f, bellX, cy + 38f,
                new float[]{0f, 0.45f, 1f},
                new int[]{rgb(0xFFF5B8), rgb(0xE0A83B), rgb(0x8A5A14)});
        g2.fillPath(horn);
        g2.setColor(0x3B2008);
        g2.setStrokeRound(2.4f);
        g2.drawPath(horn);

        if (active) {
            int waves = volumePct >= 75 ? 3 : (volumePct >= 35 ? 2 : 1);
            for (int i = 1; i <= 3; ++i) {
                float r = 22f + i * 18f;
                boolean lit = i <= waves;
                if (lit) {
                    g2.setColor(0x7CF5E6);
                } else {
                    g2.setColorHexAlpha(0x8F6932, 65);
                }
                g2.setStrokeRound(lit ? 4.2f : 2.2f);
                g2.drawArc(bellX + 6f - r * 0.4f, cy - r, r * 1.4f, r * 2f, -42f, 84f);
            }
        } else {
            float mx = cx + 34f;
            g2.setColor(0xFF5C5C);
            g2.setStrokeRound(5.5f);
            g2.drawLine((int) (mx - 16), (int) (cy - 16), (int) (mx + 16), (int) (cy + 16));
            g2.drawLine((int) (mx + 16), (int) (cy - 16), (int) (mx - 16), (int) (cy + 16));
        }
    }

    /**
     * 100% Vector HD Imperial Calligraphic Title Emblem ("LOẠN 12 SỨ QUÂN — ĐẠI CỒ VIỆT HD").
     */
    public static void drawHdImperialTitleLogo(HdCanvas2D g2, int logicalCenterX, int logicalBottomY, int animTick) {
        if (g2 == null) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        int cx = logicalCenterX * s;
        int bottomY = logicalBottomY * s;
        int bannerW = 636;
        int bannerH = 168;
        int bx = cx - bannerW / 2;
        int by = bottomY - bannerH - 12;

        // 1. Radiant golden halo behind banner
        g2.setRadialGradient(
                cx, by + bannerH * 0.52f, bannerW * 0.55f,
                new float[]{0f, 0.65f, 1f},
                new int[]{rgba(0xFFD25F, 115), rgba(0xB45F19, 45), rgba(0x000000, 0)});
        g2.fillOval(cx - bannerW * 0.55f, by - 25f, bannerW * 1.1f, bannerH + 50f);

        // 2. Imperial Crimson-Mahogany Shield Plaque
        g2.setColorRgba(0, 0, 0, 150);
        g2.fillRoundRect(bx + 4, by + 24, bannerW, bannerH - 28, 26f);

        g2.setLinearGradient(
                bx, by + 18, bx, by + bannerH - 10,
                new float[]{0f, 0.45f, 1f},
                new int[]{rgb(0x6E1818), rgb(0x3D0C0E), rgb(0x1C0607)});
        g2.fillRoundRect(bx, by + 18, bannerW, bannerH - 28, 26f);

        // Double ornate gold border
        setGradient2(g2, bx, by + 18, bx, by + bannerH - 10, rgb(0xFFF5B8), rgb(0xB88228));
        g2.setStrokeWidth(4.0f);
        g2.drawRoundRect(bx, by + 18, bannerW, bannerH - 28, 26f);

        g2.setColorHexAlpha(0xFFE680, 140);
        g2.setStrokeWidth(1.5f);
        g2.drawRoundRect(bx + 8, by + 26, bannerW - 16, bannerH - 44, 18f);
        drawCornerFiligree(g2, bx + 4, by + 22, bannerW - 8, bannerH - 36, rgba(0xFFF2A6, 200));

        // 3. Top Crown Ribbon ("ĐẠI CỒ VIỆT • BẢN HD")
        int crownW = 306;
        int crownH = 32;
        int crownX = cx - crownW / 2;
        int crownY = by + 2;
        setGradient2(g2, crownX, crownY, crownX, crownY + crownH, rgb(0x1D6355), rgb(0x0E332B));
        g2.fillRoundRect(crownX, crownY, crownW, crownH, 16f);
        g2.setColor(0xFFD700);
        g2.setStrokeWidth(2.2f);
        g2.drawRoundRect(crownX, crownY, crownW, crownH, 16f);
        HdFontRenderer.drawSubtitleHd(g2, "ĐẠI CỒ VIỆT  •  BẢN HD", cx, crownY + 23, true, 0xFFF5B8, 0x0A1F1A);

        // 4. Main Display Title ("LOẠN 12 SỨ QUÂN")
        HdFontRenderer.drawDisplayCenteredHd(
                g2, "LOẠN 12 SỨ QUÂN",
                cx, by + 98,
                true,
                0xFFF9C4, 0xE6A12E,
                0x290806, 7.5f);

        // 5. Subtitle motto strip
        HdFontRenderer.drawSubtitleHd(
                g2, "KHỞI NGHĨA HOA LƯ  •  THỐNG NHẤT GIANG SƠN",
                cx, by + 136, true, 0xFFE0A3, 0x1A0605);
    }

    /**
     * 100% Vector HD Imperial Menu Plaque Button with jade/gold side finials and hover/focus aura.
     */
    public static void drawHdMenuButton(
            HdCanvas2D g2,
            int logicalX,
            int logicalY,
            int logicalW,
            int logicalH,
            String label,
            boolean selected,
            boolean hovered,
            boolean highlightContinue) {
        if (g2 == null) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        int hx = logicalX * s;
        int hy = logicalY * s;
        int hw = logicalW * s;
        int hh = logicalH * s;
        float arc = 18f;
        boolean active = selected || hovered;

        if (active) {
            if (selected) {
                g2.setColorRgba(255, 215, 90, 85);
            } else {
                g2.setColorRgba(110, 245, 220, 60);
            }
            g2.fillRoundRect(hx - 5f, hy - 4f, hw + 10f, hh + 8f, arc + 6f);
        }

        g2.setColorRgba(0, 0, 0, 130);
        g2.fillRoundRect(hx + 2f, hy + 4f, hw, hh, arc);

        int top, mid, bot;
        if (selected) {
            top = highlightContinue ? rgb(0x247A68) : rgb(0x7A2422);
            mid = highlightContinue ? rgb(0x165246) : rgb(0x521414);
            bot = highlightContinue ? rgb(0x0C2E27) : rgb(0x2B0909);
        } else if (hovered) {
            top = rgb(0x4D3320);
            mid = rgb(0x332013);
            bot = rgb(0x1C1009);
        } else {
            top = rgb(0x382417);
            mid = rgb(0x24160E);
            bot = rgb(0x140B06);
        }

        g2.setLinearGradient(hx, hy, hx, hy + hh, new float[]{0f, 0.5f, 1f}, new int[]{top, mid, bot});
        g2.fillRoundRect(hx, hy, hw, hh, arc);

        setGradient2(
                g2, hx, hy + 2f, hx, hy + hh * 0.45f,
                rgba(0xFFF5C8, active ? 75 : 35),
                rgba(0xFFF5C8, 0));
        g2.fillRoundRect(hx + 4f, hy + 3f, hw - 8f, hh * 0.42f, arc - 4f);

        int rimTop = active ? rgb(0xFFF5B8) : rgb(0xD8AD5E);
        int rimBot = active ? rgb(0xE09E34) : rgb(0x7A5726);
        setGradient2(g2, hx, hy, hx, hy + hh, rimTop, rimBot);
        g2.setStrokeWidth(active ? 3.0f : 2.2f);
        g2.drawRoundRect(hx, hy, hw, hh, arc);

        int cy = hy + hh / 2;
        drawMiniDiamond(g2, hx + 18, cy, active ? 0xFFF2A6 : 0xB8893D);
        drawMiniDiamond(g2, hx + hw - 18, cy, active ? 0xFFF2A6 : 0xB8893D);

        int textColor = selected ? 0xFFF9D2 : (hovered ? 0xFFF2B8 : 0xF0DFC2);
        HdFontRenderer.drawHdShadowText(
                g2, true, label,
                logicalX + (logicalW >> 1),
                logicalY + ((logicalH - 12) >> 1),
                16 | 1,
                textColor, 0x120604,
                logicalW - 18, 12);
    }

    private static void drawMiniDiamond(HdCanvas2D g2, int cx, int cy, int rgbHex) {
        HdPath d = g2.createPath();
        d.moveTo(cx, cy - 5);
        d.lineTo(cx + 5, cy);
        d.lineTo(cx, cy + 5);
        d.lineTo(cx - 5, cy);
        d.closePath();
        g2.setColor(rgbHex);
        g2.fillPath(d);
    }

    /**
     * 100% Vector HD Carved Rosewood & Gold Bottom SoftKey Dock.
     */
    public static void drawHdSoftKeyBar(HdCanvas2D g2, int logicalScreenW, int logicalScreenH) {
        if (g2 == null) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        int barY = (logicalScreenH - 23) * s;
        int barW = logicalScreenW * s;
        int barH = 23 * s;

        g2.setLinearGradient(
                0, barY, 0, barY + barH,
                new float[]{0f, 0.25f, 1f},
                new int[]{rgb(0x2E1C11), rgb(0x1C1009), rgb(0x0D0704)});
        g2.fillRect(0, barY, barW, barH);

        g2.setColor(0x7A5726);
        g2.fillRect(0, barY, barW, 3);
        g2.setLinearGradient(
                0, barY + 2, barW, barY + 2,
                new float[]{0f, 0.5f, 1f},
                new int[]{rgba(0xCCA052, 80), rgb(0xFFE680), rgba(0xCCA052, 80)});
        g2.fillRect(24, barY + 2, barW - 48, 2);
    }

    /**
     * 100% Vector HD Crossed-Swords "VS" Medallion for {@code BattleIntroPanel}.
     */
    public static void drawHdVsMedallion(HdCanvas2D g2, int logicalCenterX, int logicalY, int logicalH, int animTick) {
        if (g2 == null) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        float cx = logicalCenterX * s;
        float cy = (logicalY + logicalH * 0.5f) * s;

        g2.setLinearGradient(
                cx - 210f, cy, cx + 210f, cy,
                new float[]{0f, 0.25f, 0.5f, 0.75f, 1f},
                new int[]{rgba(0x8C1922, 0), rgba(0x8C1922, 200), rgb(0xFFE680), rgba(0x8C1922, 200), rgba(0x8C1922, 0)});
        g2.fillRoundRect(cx - 210f, cy - 7f, 420, 14, 10);

        for (int side = -1; side <= 1; side += 2) {
            g2.save();
            g2.translate(cx, cy);
            g2.rotate(side * Math.toRadians(58));
            HdPath blade = g2.createPath();
            blade.moveTo(0, -48f);
            blade.lineTo(6f, 12f);
            blade.lineTo(-6f, 12f);
            blade.closePath();
            setGradient2(g2, -6f, 0, 6f, 0, rgb(0xFFFFFF), rgb(0x9CB2C9));
            g2.fillPath(blade);
            g2.setColor(0x36495E);
            g2.setStrokeWidth(1.5f);
            g2.drawPath(blade);

            g2.setColor(0xFFD700);
            g2.fillRoundRect(-13, 10, 26, 5, 4);
            g2.setColor(0x6E1B1B);
            g2.fillRect(-3, 15, 6, 14);
            g2.setColor(0xFFE680);
            g2.fillOval(-5, 27, 10, 10);
            g2.restore();
        }

        float pulse = 23f + (float) Math.sin(animTick * 0.25) * 2.5f;
        g2.setColorRgba(92, 245, 220, 90);
        g2.fillOval(cx - pulse - 5f, cy - pulse - 5f, (pulse + 5f) * 2f, (pulse + 5f) * 2f);

        g2.setRadialGradient(
                cx - 4f, cy - 5f, 24f,
                new float[]{0f, 0.7f, 1f},
                new int[]{rgb(0x38D9C0), rgb(0x14594E), rgb(0x092924)});
        g2.fillOval(cx - 22f, cy - 22f, 44f, 44f);
        setGradient2(g2, cx, cy - 22f, cx, cy + 22f, rgb(0xFFF5B8), rgb(0xB88328));
        g2.setStrokeWidth(3.0f);
        g2.drawOval(cx - 22f, cy - 22f, 44f, 44f);

        HdFontRenderer.drawHdShadowText(
                g2, true, "VS",
                logicalCenterX, (int) (cy / s) - 6,
                16 | 1, 0xFFF9C4, 0x0A1F1A, 32, 13);
    }

    /**
     * 100% Vector HD Sacred Relic Nexus Lines replacing neon-purple {@code /r/br} in {@code SuQuanCollectionScreen}.
     */
    public static void drawHdRelicNexusLines(HdCanvas2D g2, int ox, int oy, int animTick) {
        if (g2 == null) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        int c0x = (ox + 8 + 54) * s;
        int c1x = (ox + 124 + 54) * s;
        int r0y = (oy + 28 + 37) * s;
        int r1y = (oy + 108 + 37) * s;
        int midX = (c0x + c1x) / 2;
        int midY = (r0y + r1y) / 2;

        g2.setColorHexAlpha(0xFFE680, 95);
        g2.setStrokeWidth(3.2f);
        g2.drawRect(c0x, r0y, c1x - c0x, r1y - r0y);
        g2.drawLine(c0x, r0y, c1x, r1y);
        g2.drawLine(c1x, r0y, c0x, r1y);

        float r = 16f + (float) Math.sin(animTick * 0.2) * 2f;
        g2.setColorRgba(80, 235, 205, 85);
        g2.fillOval(midX - r, midY - r, r * 2f, r * 2f);
        g2.setColor(0xFFD700);
        g2.setStrokeWidth(2.2f);
        g2.drawOval(midX - 10f, midY - 10f, 20f, 20f);
    }

    /**
     * 100% Vector HD Antique Gold Vertical Scrollbar for scrollable dialogs & panels.
     */
    public static void drawHdVerticalScrollbar(
            HdCanvas2D g2, int logicalTrackX, int logicalTopY, int logicalTrackH,
            int scrollY, int maxScrollY, int viewportH) {
        if (g2 == null || maxScrollY <= 0 || logicalTrackH < 20) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        int tx = logicalTrackX * s;
        int ty = logicalTopY * s;
        int th = logicalTrackH * s;
        int tw = 8;

        g2.setColorRgba(12, 6, 3, 175);
        g2.fillRoundRect(tx, ty, tw, th, 6);
        g2.setColorHexAlpha(0x8F6932, 130);
        g2.setStrokeWidth(1.2f);
        g2.drawRoundRect(tx, ty, tw, th, 6);

        int totalH = viewportH + maxScrollY;
        int thumbH = Math.max(28, (int) ((long) th * viewportH / Math.max(1, totalH)));
        if (thumbH > th - 4) thumbH = th - 4;
        int clampedScroll = Math.max(0, Math.min(maxScrollY, scrollY));
        int thumbY = ty + 2 + (int) ((long) (th - 4 - thumbH) * clampedScroll / maxScrollY);

        setGradient2(g2, tx, thumbY, tx + tw, thumbY + thumbH, rgb(0xFFF2A6), rgb(0xB88328));
        g2.fillRoundRect(tx + 1, thumbY, tw - 2, thumbH, 5);
    }

    /**
     * 100% Vector HD Pill Badge replacing 5x7 sprite digits ({@code SmallNumberFont}).
     */
    public static void drawHdCountBadge(HdCanvas2D g2, int value, int logicalX, int logicalY, int align) {
        if (g2 == null) {
            return;
        }
        String str = String.valueOf(value);
        final int s = HdConfig.HD_SCALE;
        int textW = HdFontRenderer.measureSubtitleHdWidth(g2, str);
        int pillW = Math.max(24, textW + 12);
        int pillH = 22;

        int hx = logicalX * s;
        int hy = logicalY * s - 2;
        if (align == 2) {
            hx -= pillW - 4;
        } else if (align == 1) {
            hx -= pillW / 2;
        }

        g2.setColorRgba(14, 8, 5, 225);
        g2.fillRoundRect(hx, hy, pillW, pillH, 9);
        g2.setColor(0xC89842);
        g2.setStrokeWidth(1.5f);
        g2.drawRoundRect(hx, hy, pillW, pillH, 9);

        HdFontRenderer.drawSubtitleHd(
                g2, str,
                hx + pillW / 2, hy + 17,
                true, 0xFFF5B8, 0x0A0503);
    }

    /**
     * 100% Vector HD Directional Chevron Arrow (0=UP, 1=DOWN, 2=LEFT, 3=RIGHT).
     */
    public static void drawHdDirectionalChevron(
            HdCanvas2D g2, int logicalCx, int logicalCy, int dir, int colorRgb, boolean withBadge, boolean hovered) {
        if (g2 == null) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        float cx = logicalCx * s;
        float cy = logicalCy * s;

        if (withBadge) {
            float r = hovered ? 22f : 19f;
            g2.setColorRgba(0, 0, 0, 130);
            g2.fillOval(cx - r + 1.5f, cy - r + 2.5f, r * 2f, r * 2f);
            setGradient2(
                    g2, cx, cy - r, cx, cy + r,
                    hovered ? rgb(0x288573) : rgb(0x1D5E52),
                    rgb(0x0E2E28));
            g2.fillOval(cx - r, cy - r, r * 2f, r * 2f);
            g2.setColor(hovered ? 0xFFF5B8 : 0xFFD700);
            g2.setStrokeWidth(hovered ? 3.0f : 2.2f);
            g2.drawOval(cx - r, cy - r, r * 2f, r * 2f);
        }

        g2.save();
        g2.translate(cx, cy);
        double angle = (dir == 0) ? 0 : (dir == 1 ? Math.PI : (dir == 2 ? -Math.PI / 2.0 : Math.PI / 2.0));
        g2.rotate(angle);

        HdPath arrow = g2.createPath();
        arrow.moveTo(0, -9f);
        arrow.lineTo(9f, 5f);
        arrow.lineTo(3f, 2f);
        arrow.lineTo(3f, 9f);
        arrow.lineTo(-3f, 9f);
        arrow.lineTo(-3f, 2f);
        arrow.lineTo(-9f, 5f);
        arrow.closePath();

        g2.setColorRgba(0, 0, 0, 160);
        g2.translate(1, 1.5);
        g2.fillPath(arrow);
        g2.translate(-1, -1.5);

        g2.setColor(colorRgb);
        g2.fillPath(arrow);

        g2.restore();
    }

    /**
     * Renders a glossy 3D gemstone RPG gauge (HP / EXP / Food / Mana) at {@code 3x} HD resolution.
     */
    public static void drawHdRpgStatBar(
            HdCanvas2D g2,
            int x,
            int y,
            int w,
            int h,
            int current,
            int max,
            int colorTop,
            int colorMid,
            int colorBot,
            int colorEdge,
            int tickDivisions,
            String overlayText,
            int overlayTextLogicalW) {
        if (g2 == null || w < 8 || h < 6) {
            return;
        }
        final int s = HdConfig.HD_SCALE;
        int hx = x * s;
        int hy = y * s;
        int hw = w * s;
        int hh = h * s;
        float arc = Math.min(14f, hh * 0.45f);

        setGradient2(g2, hx, hy, hx, hy + hh, rgb(0x0E0704), rgb(0x22130B));
        g2.fillRoundRect(hx + 1.5f, hy + 1.5f, hw - 3f, hh - 3f, arc);

        int pad = hh >= 30 ? 4 : 3;
        int innerX = hx + pad;
        int innerY = hy + pad;
        int innerW = hw - pad * 2;
        int innerH = hh - pad * 2;
        int fillW = 0;

        if (max > 0 && current > 0 && innerW > 0 && innerH > 0) {
            int clamped = Math.min(current, max);
            fillW = (int) ((long) innerW * (long) clamped / (long) max);
            if (fillW < 3) {
                fillW = 3;
            }
            if (fillW > innerW) {
                fillW = innerW;
            }
            float fillArc = Math.max(3f, arc - 3f);

            g2.setLinearGradient(
                    innerX, innerY, innerX, innerY + innerH,
                    new float[]{0.0f, 0.45f, 1.0f},
                    new int[]{rgb(colorTop), rgb(colorMid), rgb(colorBot)});
            g2.fillRoundRect(innerX, innerY, fillW, innerH, fillArc);

            if (innerH >= 10 && fillW >= 8) {
                g2.setColorHexAlpha(0xFFFFFF, 92);
                g2.fillRoundRect(innerX + 2, innerY + 1, Math.max(2, fillW - 4), Math.max(3, innerH / 3), 4);
            }

            if (fillW < innerW - 2) {
                g2.setColorHexAlpha(colorEdge, 235);
                g2.fillRect(innerX + fillW - 3, innerY + 1, 3, innerH - 2);
            }
        }

        if (tickDivisions == 3) {
            for (int t = 1; t < 3; ++t) {
                int tx = innerX + (innerW * t) / 3;
                g2.setColorHexAlpha(0x0D0604, 215);
                g2.fillRect(tx - 1, innerY, 3, innerH);
                g2.setColor(0xE8C06B);
                g2.fillRect(tx, hy + 1, 2, 3);
                g2.fillRect(tx, hy + hh - 4, 2, 3);
            }
        } else if (tickDivisions == 4 && innerH >= 10) {
            for (int t = 1; t < 4; ++t) {
                int tx = innerX + (innerW * t) / 4;
                if ((tx - innerX) < fillW) {
                    g2.setColorHexAlpha(0x1A0B05, 175);
                } else {
                    g2.setColorHexAlpha(0x6E4E26, 150);
                }
                g2.fillRect(tx, innerY + innerH - 5, 2, 5);
            }
        }

        setGradient2(g2, hx, hy, hx, hy + hh, rgb(0xECC471), rgb(0x755222));
        g2.setStrokeWidth(2.0f);
        g2.drawRoundRect(hx + 1.5f, hy + 1.5f, hw - 3f, hh - 3f, arc);

        if (overlayText != null && !overlayText.isEmpty()) {
            int textY = y + ((h - 11) >> 1);
            HdFontRenderer.drawHdShadowText(
                    g2, false, overlayText,
                    x + (w >> 1), textY,
                    16 | 1,
                    0xFFFFFF, 0x100603,
                    Math.max(overlayTextLogicalW, w - 6), 11);
        }
    }
}
