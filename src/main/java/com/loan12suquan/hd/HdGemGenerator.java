package com.loan12suquan.hd;

import com.loan12suquan.hd.platform.HdCanvas2D;
import com.loan12suquan.hd.platform.HdPath;
import com.loan12suquan.hd.platform.HdPlatform;
import com.loan12suquan.hd.platform.HdSurface;

/**
 * 100% Faithful Cross-Platform HD Vector Remaster of the 7 original <em>Loạn 12 Sứ Quân</em> Match-3 board gems
 * ({@code 22x22} logical -> {@code 66x66} native {@code 3x} HD), shared identically on Desktop & Web/iPhone 17:
 * <ul>
 *   <li>{@code /sword}: Silver square plaque + Top-Left (-45°) silver broadsword + orange-gold guard & pink jewel.</li>
 *   <li>{@code /rice}: Free-standing square green Vietnamese Bánh Chưng tied with 3x3 golden bamboo lạt strings.</li>
 *   <li>{@code /heart}: Free-standing glossy magenta-pink 3D heart with twin lobe highlights & sculpted mid-groove.</li>
 *   <li>{@code /yinyang}: Free-standing Vermilion-Red (left/top, yellow sun orb) & Sky-Blue (right/bottom, white moon orb) Yin-Yang.</li>
 *   <li>{@code /gold}: Free-standing bright golden Kim Nguyên Bảo (sycee boat ingot) with top dome & white-gold shine.</li>
 *   <li>{@code /book}: Ice-blue square plaque + diagonal rolled Lavender-Purple Silk Scroll (Binh Thư) tied with green ribbon.</li>
 *   <li>{@code /swordred}: Silver square plaque + Top-Left (-45°) crimson-red broadsword engulfed in golden flames.</li>
 * </ul>
 */
public final class HdGemGenerator {

    private HdGemGenerator() {
    }

    private static int c(int r, int g, int b) {
        return 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    private static int ca(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    private static int resolveGemType(String resourcePath) {
        if (resourcePath == null) {
            return -1;
        }
        if (resourcePath.equals("/sword")) return 0;
        if (resourcePath.equals("/rice")) return 1;
        if (resourcePath.equals("/heart")) return 2;
        if (resourcePath.equals("/yinyang")) return 3;
        if (resourcePath.equals("/gold")) return 4;
        if (resourcePath.equals("/book")) return 5;
        if (resourcePath.equals("/swordred")) return 6;
        return -1;
    }

    public static boolean isBoardGemPath(String resourcePath) {
        return resolveGemType(resourcePath) >= 0;
    }

    public static HdSurface createHdGem(String resourcePath, HdSurface fallbackIcon) {
        int gemType = resolveGemType(resourcePath);
        if (gemType < 0) {
            return fallbackIcon;
        }

        final int sz = 22 * HdConfig.HD_SCALE; // 66x66 (exact 3x of 22x22 original gem sprite)
        HdSurface out = HdPlatform.backend().createSurface(sz, sz, false);
        HdCanvas2D g2 = out.getCanvas2D();

        switch (gemType) {
            case 0:
                drawOriginalSwordHd(g2, sz, false);
                break;
            case 1:
                drawOriginalBanhChungHd(g2, sz);
                break;
            case 2:
                drawOriginalMagentaHeartHd(g2, sz);
                break;
            case 3:
                drawOriginalRedBlueYinYangHd(g2, sz);
                break;
            case 4:
                drawOriginalGoldIngotHd(g2, sz);
                break;
            case 5:
                drawOriginalPurpleScrollBookHd(g2, sz);
                break;
            default:
                drawOriginalSwordHd(g2, sz, true);
                break;
        }

        return out;
    }

    /**
     * 1) {@code /sword} & 7) {@code /swordred}:
     * Silver square plaque + diagonal broadsword pointing TOP-LEFT (-45°) + golden-orange guard & pink gem.
     */
    private static void drawOriginalSwordHd(HdCanvas2D g2, int sz, boolean fireSword) {
        float bx = 5.5f;
        float by = 5.5f;
        float bw = sz - 11f;
        float bh = sz - 11f;

        // Drop shadow
        g2.setColorRgba(0, 0, 0, 135);
        g2.fillRoundRect(bx + 1.5f, by + 2f, bw, bh, 9f);

        // Outer silver metallic bevel
        g2.setLinearGradient(
                bx, by, bx + bw, by + bh,
                new float[]{0f, 0.5f, 1f},
                new int[]{c(235, 240, 250), c(176, 184, 200), c(115, 122, 138)});
        g2.fillRoundRect(bx, by, bw, bh, 8f);
        g2.setColorRgba(26, 30, 38, 255);
        g2.setStrokeWidth(1.6f);
        g2.drawRoundRect(bx, by, bw, bh, 8f);

        // Recessed slate-gray interior
        g2.setLinearGradient(
                bx + 4f, by + 4f, bx + bw - 4f, by + bh - 4f,
                new float[]{0f, 1f},
                fireSword
                        ? new int[]{c(92, 74, 82), c(64, 52, 62)}
                        : new int[]{c(84, 90, 104), c(108, 116, 132)});
        g2.fillRoundRect(bx + 4.5f, by + 4.5f, bw - 9f, bh - 9f, 5f);
        g2.setColorRgba(45, 50, 60, 255);
        g2.setStrokeWidth(1.5f);
        g2.drawRoundRect(bx + 4.5f, by + 4.5f, bw - 9f, bh - 9f, 5f);

        // 2. If Fire Sword (/swordred), draw roaring golden-orange flames inside the silver frame
        if (fireSword) {
            HdPath topFlame = g2.createPath();
            topFlame.moveTo(14f, 12f);
            topFlame.quadTo(26f, 7f, 35f, 13f);
            topFlame.quadTo(44f, 9f, 50f, 17f);
            topFlame.quadTo(57f, 20f, 54f, 30f);
            topFlame.quadTo(50f, 37f, 42f, 38f);
            topFlame.lineTo(14f, 12f);
            topFlame.closePath();

            HdPath botFlame = g2.createPath();
            botFlame.moveTo(12f, 24f);
            botFlame.quadTo(8f, 34f, 13f, 43f);
            botFlame.quadTo(11f, 52f, 21f, 53f);
            botFlame.quadTo(29f, 57f, 38f, 50f);
            botFlame.lineTo(12f, 24f);
            botFlame.closePath();

            g2.setRadialGradient(
                    33f, 31f, 26f,
                    new float[]{0f, 0.6f, 1f},
                    new int[]{c(255, 250, 120), c(255, 180, 20), c(230, 65, 10)});
            g2.fillPath(topFlame);
            g2.fillPath(botFlame);

            g2.setColorRgba(255, 248, 130, 255);
            g2.fillOval(32f, 12f, 18f, 13f);
            g2.fillOval(13f, 32f, 13f, 17f);
        }

        // 3. Diagonal Broadsword rotated -45° (pointing TOP-LEFT, guard at BOTTOM-RIGHT)
        g2.save();
        g2.translate(sz * 0.5, sz * 0.5);
        g2.rotate(Math.toRadians(-45));

        // Sword shadow on plaque
        g2.setColorRgba(0, 0, 0, 110);
        HdPath shadowBlade = g2.createPath();
        shadowBlade.moveTo(1.5f, -33f);
        shadowBlade.lineTo(10.5f, -23f);
        shadowBlade.lineTo(9.5f, 11f);
        shadowBlade.lineTo(-7.5f, 11f);
        shadowBlade.lineTo(-8.5f, -23f);
        shadowBlade.closePath();
        g2.fillPath(shadowBlade);

        HdPath bladeLeft = g2.createPath();
        bladeLeft.moveTo(0f, -38f);
        bladeLeft.lineTo(-9.5f, -26f);
        bladeLeft.lineTo(-8.5f, 11f);
        bladeLeft.lineTo(0f, 11f);
        bladeLeft.closePath();

        HdPath bladeRight = g2.createPath();
        bladeRight.moveTo(0f, -38f);
        bladeRight.lineTo(9.5f, -26f);
        bladeRight.lineTo(8.5f, 11f);
        bladeRight.lineTo(0f, 11f);
        bladeRight.closePath();

        if (fireSword) {
            g2.setLinearGradient(
                    -8f, -30f, 0f, 10f,
                    new float[]{0f, 0.5f, 1f},
                    new int[]{c(235, 35, 15), c(185, 12, 8), c(128, 6, 6)});
            g2.fillPath(bladeLeft);
            g2.setLinearGradient(
                    0f, -32f, 9f, 10f,
                    new float[]{0f, 0.5f, 1f},
                    new int[]{c(255, 95, 45), c(240, 28, 12), c(175, 10, 8)});
            g2.fillPath(bladeRight);

            g2.setColorRgba(88, 4, 4, 255);
            g2.setStrokeRound(2.6f);
            g2.drawLine(0, -24, 0, 9);
        } else {
            g2.setLinearGradient(
                    -8f, -30f, 0f, 10f,
                    new float[]{0f, 0.5f, 1f},
                    new int[]{c(220, 232, 248), c(168, 188, 218), c(122, 144, 178)});
            g2.fillPath(bladeLeft);
            g2.setLinearGradient(
                    0f, -32f, 9f, 10f,
                    new float[]{0f, 0.5f, 1f},
                    new int[]{c(255, 255, 255), c(242, 248, 255), c(195, 212, 236)});
            g2.fillPath(bladeRight);

            g2.setColorRgba(140, 162, 195, 255);
            g2.setStrokeWidth(1.6f);
            g2.drawLine(0, -27, 0, 9);
        }

        HdPath bladeOutline = g2.createPath();
        bladeOutline.moveTo(0f, -38f);
        bladeOutline.lineTo(9.5f, -26f);
        bladeOutline.lineTo(8.5f, 11f);
        bladeOutline.lineTo(-8.5f, 11f);
        bladeOutline.lineTo(-9.5f, -26f);
        bladeOutline.closePath();
        if (fireSword) {
            g2.setColorRgba(75, 8, 6, 255);
        } else {
            g2.setColorRgba(34, 44, 62, 255);
        }
        g2.setStrokeWidth(1.8f);
        g2.drawPath(bladeOutline);

        // Dark Grip & Pommel at bottom-right corner
        g2.setColorRgba(38, 44, 56, 255);
        g2.fillRoundRect(-3, 15, 6, 15, 3);
        g2.setColorRgba(95, 110, 132, 255);
        g2.fillRect(-1, 16, 2, 12);
        g2.setColorRgba(20, 24, 32, 255);
        g2.setStrokeWidth(1.5f);
        g2.drawRoundRect(-3, 15, 6, 15, 3);

        // Ornate Golden-Orange Winged Crossguard
        HdPath guard = g2.createPath();
        guard.moveTo(-15f, 9f);
        guard.quadTo(-16f, 15f, -10f, 17f);
        guard.quadTo(0f, 20f, 10f, 17f);
        guard.quadTo(16f, 15f, 15f, 9f);
        guard.quadTo(11f, 6f, 7f, 10f);
        guard.quadTo(0f, 12f, -7f, 10f);
        guard.quadTo(-11f, 6f, -15f, 9f);
        guard.closePath();

        g2.setLinearGradient(
                -15f, 8f, 15f, 18f,
                new float[]{0f, 0.45f, 1f},
                new int[]{c(255, 232, 85), c(255, 142, 18), c(178, 62, 8)});
        g2.fillPath(guard);
        g2.setColorRgba(78, 24, 6, 255);
        g2.setStrokeWidth(1.6f);
        g2.drawPath(guard);

        // Signature Bright Magenta-Pink Jewel in center of crossguard
        g2.setRadialGradient(
                -1.5f, 12f, 5.5f,
                new float[]{0f, 0.65f, 1f},
                new int[]{c(255, 225, 248), c(255, 48, 185), c(148, 10, 95)});
        g2.fillOval(-4.5f, 9.2f, 9f, 9f);
        g2.setColorRgba(82, 12, 52, 255);
        g2.setStrokeWidth(1.1f);
        g2.drawOval(-4.5f, 9.2f, 9f, 9f);

        g2.restore();
    }

    /**
     * 2) {@code /rice}:
     * Free-standing square green Vietnamese Bánh Chưng tied with 3x3 golden-ivory bamboo lạt strings.
     */
    private static void drawOriginalBanhChungHd(HdCanvas2D g2, int sz) {
        int bx = 8;
        int by = 8;
        int bw = sz - 16; // 50x50
        int bh = sz - 16;

        // Soft drop shadow
        g2.setColorRgba(0, 0, 0, 140);
        g2.fillRoundRect(bx + 2, by + 2, bw, bh, 8);

        // Base dark leaf border
        g2.setColorRgba(14, 34, 8, 255);
        g2.fillRoundRect(bx - 1, by - 1, bw + 2, bh + 2, 7);

        // 4x4 grid of lush green Dong-leaf squares
        int[] cellStarts = new int[]{0, 13, 26, 39};
        int[] cellSizes = new int[]{10, 10, 10, 11};

        for (int r = 0; r < 4; ++r) {
            for (int col = 0; col < 4; ++col) {
                int cx = bx + cellStarts[col];
                int cy = by + cellStarts[r];
                int cw = cellSizes[col];
                int ch = cellSizes[r];

                g2.setColorRgba(6, 56, 6, 255);
                g2.fillRect(cx - 1, cy - 1, cw + 2, ch + 2);

                g2.setRadialGradient(
                        cx + cw * 0.35f, cy + ch * 0.35f, cw * 0.78f,
                        new float[]{0f, 0.55f, 1f},
                        new int[]{c(138, 245, 78), c(36, 168, 26), c(10, 82, 10)});
                g2.fillRoundRect(cx, cy, cw, ch, 3);

                g2.setColorRgba(210, 255, 145, 165);
                g2.fillOval(cx + 1, cy + 1, 4, 3);
            }
        }

        // 3 Vertical + 3 Horizontal Bright Golden-Ivory Bamboo Lạt Strings
        int[] tieOffsets = new int[]{10, 23, 36};
        for (int i = 0; i < tieOffsets.length; ++i) {
            int tx = bx + tieOffsets[i];
            g2.setColorRgba(42, 34, 6, 215);
            g2.fillRect(tx - 1, by, 5, bh);
            g2.setLinearGradient(
                    tx, by, tx + 4, by,
                    new float[]{0f, 0.5f, 1f},
                    new int[]{c(235, 205, 65), c(255, 252, 165), c(205, 165, 38)});
            g2.fillRect(tx, by, 4, bh);
        }
        for (int i = 0; i < tieOffsets.length; ++i) {
            int ty = by + tieOffsets[i];
            g2.setColorRgba(42, 34, 6, 215);
            g2.fillRect(bx, ty - 1, bw, 5);
            g2.setLinearGradient(
                    bx, ty, bx, ty + 4,
                    new float[]{0f, 0.5f, 1f},
                    new int[]{c(235, 205, 65), c(255, 252, 165), c(205, 165, 38)});
            g2.fillRect(bx, ty, bw, 4);
        }

        // Bright intersection knots
        g2.setColorRgba(255, 255, 225, 255);
        for (int r = 0; r < tieOffsets.length; ++r) {
            for (int col = 0; col < tieOffsets.length; ++col) {
                g2.fillRect(bx + tieOffsets[col] + 1, by + tieOffsets[r] + 1, 2, 2);
            }
        }

        g2.setColorRgba(14, 32, 8, 255);
        g2.setStrokeWidth(1.8f);
        g2.drawRoundRect(bx - 1, by - 1, bw + 1, bh + 1, 6);
    }

    /**
     * 3) {@code /heart}:
     * Free-standing glossy Magenta-Pink 3D Heart with twin white-pink upper highlights & sculpted mid-groove.
     */
    private static void drawOriginalMagentaHeartHd(HdCanvas2D g2, int sz) {
        float cx = sz * 0.5f;
        float cy = sz * 0.52f;
        float r = 25.5f;

        HdPath shadow = buildPlumpHeartPath(g2, cx + 1.5f, cy + 2.2f, r);
        g2.setColorRgba(0, 0, 0, 135);
        g2.fillPath(shadow);

        HdPath heart = buildPlumpHeartPath(g2, cx, cy, r);
        g2.setRadialGradient(
                cx - 4f, cy - 8f, r * 1.15f,
                new float[]{0f, 0.55f, 0.85f, 1f},
                new int[]{
                        c(255, 118, 198),
                        c(248, 28, 142),
                        c(198, 6, 98),
                        c(128, 0, 58)
                });
        g2.fillPath(heart);

        // Sculpted 3D middle contour groove clipped inside heart
        g2.save();
        g2.clipPath(heart);

        HdPath midShade = g2.createPath();
        midShade.moveTo(cx - 22f, cy - 3f);
        midShade.quadTo(cx, cy + 8f, cx + 22f, cy - 3f);
        midShade.quadTo(cx, cy + 14f, cx - 22f, cy - 3f);
        midShade.closePath();
        g2.setColorRgba(155, 0, 75, 145);
        g2.fillPath(midShade);

        HdPath lowerRidge = g2.createPath();
        lowerRidge.moveTo(cx - 17f, cy + 3f);
        lowerRidge.quadTo(cx, cy + 12f, cx + 17f, cy + 3f);
        g2.setColorRgba(255, 125, 202, 160);
        g2.setStrokeRound(3.2f);
        g2.drawPath(lowerRidge);

        // Twin glossy white-pink highlights on BOTH upper lobes
        g2.setColorRgba(255, 190, 230, 220);
        g2.fillOval(cx - 19f, cy - 18.5f, 12f, 7f);
        g2.fillOval(cx + 6f, cy - 18.5f, 12f, 7f);

        g2.setColorRgba(255, 255, 255, 255);
        g2.fillOval(cx - 16.5f, cy - 17.5f, 7.5f, 4f);
        g2.fillOval(cx + 8.5f, cy - 17.5f, 7.5f, 4f);

        g2.restore();

        g2.setColorRgba(62, 4, 30, 255);
        g2.setStrokeWidth(2.0f);
        g2.drawPath(heart);
    }

    private static HdPath buildPlumpHeartPath(HdCanvas2D g2, float cx, float cy, float r) {
        HdPath p = g2.createPath();
        p.moveTo(cx, cy + r * 0.88f);
        p.curveTo(cx - r * 0.55f, cy + r * 0.52f, cx - r * 1.12f, cy + r * 0.08f, cx - r * 1.04f, cy - r * 0.44f);
        p.curveTo(cx - r * 0.98f, cy - r * 0.86f, cx - r * 0.32f, cy - r * 0.92f, cx, cy - r * 0.48f);
        p.curveTo(cx + r * 0.32f, cy - r * 0.92f, cx + r * 0.98f, cy - r * 0.86f, cx + r * 1.04f, cy - r * 0.44f);
        p.curveTo(cx + r * 1.12f, cy + r * 0.08f, cx + r * 0.55f, cy + r * 0.52f, cx, cy + r * 0.88f);
        p.closePath();
        return p;
    }

    /**
     * 4) {@code /yinyang}:
     * Free-standing Red (left/top with glowing golden-yellow sun orb) & Sky-Blue (right/bottom with glowing white moon orb) Yin-Yang.
     */
    private static void drawOriginalRedBlueYinYangHd(HdCanvas2D g2, int sz) {
        float cx = sz * 0.5f;
        float cy = sz * 0.5f;
        float r = 24.5f;

        // Drop shadow
        g2.setColorRgba(0, 0, 0, 140);
        g2.fillOval(cx - r + 1.5f, cy - r + 2f, r * 2f, r * 2f);

        // Base full circle: Vibrant Sky-Cyan Blue (Right half + bottom lobe)
        g2.setRadialGradient(
                cx + 7f, cy + 5f, r * 1.25f,
                new float[]{0f, 0.7f, 1f},
                new int[]{c(65, 215, 255), c(0, 148, 240), c(0, 72, 165)});
        g2.fillOval(cx - r, cy - r, r * 2f, r * 2f);

        // Left half + top lobe minus bottom lobe: Pure geometric S-curve path (identical on Java2D & HTML5 Canvas2D)
        HdPath redArea = g2.createPath();
        redArea.arc(cx, cy, r, Math.PI * 0.5, Math.PI * 1.5, false);
        redArea.arc(cx, cy - r * 0.5, r * 0.5, -Math.PI * 0.5, Math.PI * 0.5, false);
        redArea.arc(cx, cy + r * 0.5, r * 0.5, -Math.PI * 0.5, Math.PI * 0.5, true);
        redArea.closePath();

        g2.setRadialGradient(
                cx - 7f, cy - 6f, r * 1.25f,
                new float[]{0f, 0.7f, 1f},
                new int[]{c(255, 95, 35), c(242, 32, 8), c(165, 8, 4)});
        g2.fillPath(redArea);

        // Top glowing Golden-Yellow Sun Orb inside Red head
        float topY = cy - r * 0.46f;
        float orbR = 6.8f;
        g2.setRadialGradient(
                cx - 1f, topY, orbR,
                new float[]{0f, 0.55f, 1f},
                new int[]{c(255, 255, 190), c(255, 220, 35), ca(255, 115, 0, 180)});
        g2.fillOval(cx - 1f - orbR, topY - orbR, orbR * 2f, orbR * 2f);

        // Bottom glowing Ice-White Moon Orb inside Blue head
        float botY = cy + r * 0.46f;
        g2.setRadialGradient(
                cx + 1f, botY, orbR,
                new float[]{0f, 0.55f, 1f},
                new int[]{c(255, 255, 255), c(185, 245, 255), ca(30, 165, 255, 180)});
        g2.fillOval(cx + 1f - orbR, botY - orbR, orbR * 2f, orbR * 2f);

        // Subtle 3D rim highlights on left (orange-red) and right (light-cyan)
        g2.setColorRgba(255, 150, 95, 130);
        g2.setStrokeRound(2.0f);
        g2.drawArc(cx - r + 2.5f, cy - r + 2.5f, (r - 2.5f) * 2f, (r - 2.5f) * 2f, 110f, 125f);

        g2.setColorRgba(165, 242, 255, 140);
        g2.drawArc(cx - r + 2.5f, cy - r + 2.5f, (r - 2.5f) * 2f, (r - 2.5f) * 2f, 290f, 125f);

        // Crisp dark outer ring
        g2.setColorRgba(22, 16, 28, 255);
        g2.setStrokeWidth(2.1f);
        g2.drawOval(cx - r, cy - r, r * 2f, r * 2f);
    }

    /**
     * 5) {@code /gold}:
     * Free-standing bright Golden Kim Nguyên Bảo (boat ingot) with top dome & white-gold shine.
     */
    private static void drawOriginalGoldIngotHd(HdCanvas2D g2, int sz) {
        float cx = sz * 0.5f;
        float cy = sz * 0.52f;

        g2.setColorRgba(0, 0, 0, 135);
        g2.fillOval(cx - 24f, cy - 4f, 49f, 24f);

        // 1. Top central gold dome
        g2.setRadialGradient(
                cx - 2f, cy - 16f, 12f,
                new float[]{0f, 0.6f, 1f},
                new int[]{c(255, 255, 210), c(255, 222, 35), c(215, 135, 10)});
        g2.fillOval(cx - 11.5f, cy - 19.5f, 23f, 14f);
        g2.setColorRgba(115, 58, 6, 255);
        g2.setStrokeWidth(1.8f);
        g2.drawOval(cx - 11.5f, cy - 19.5f, 23f, 14f);

        // 2. Boat-shaped Kim Nguyên Bảo body with raised left/right wings
        HdPath boat = g2.createPath();
        boat.moveTo(cx - 25f, cy - 11f);
        boat.quadTo(cx - 23f, cy - 17f, cx - 15f, cy - 14f);
        boat.quadTo(cx, cy - 7f, cx + 15f, cy - 14f);
        boat.quadTo(cx + 23f, cy - 17f, cx + 25f, cy - 11f);
        boat.curveTo(cx + 26f, cy - 2f, cx + 21f, cy + 8f, cx + 15f, cy + 13.5f);
        boat.lineTo(cx - 15f, cy + 13.5f);
        boat.curveTo(cx - 21f, cy + 8f, cx - 26f, cy - 2f, cx - 25f, cy - 11f);
        boat.closePath();

        g2.setLinearGradient(
                cx, cy - 15f, cx, cy + 14f,
                new float[]{0f, 0.55f, 0.82f, 1f},
                new int[]{
                        c(255, 242, 75),
                        c(255, 208, 18),
                        c(228, 138, 10),
                        c(168, 82, 6)
                });
        g2.fillPath(boat);

        HdPath baseBevel = g2.createPath();
        baseBevel.moveTo(cx - 18f, cy + 5f);
        baseBevel.lineTo(cx - 13f, cy + 9.5f);
        baseBevel.lineTo(cx + 13f, cy + 9.5f);
        baseBevel.lineTo(cx + 18f, cy + 5f);
        g2.setColorRgba(205, 110, 8, 255);
        g2.setStrokeRound(2.8f);
        g2.drawPath(baseBevel);

        HdPath crease = g2.createPath();
        crease.moveTo(cx - 16f, cy - 11f);
        crease.quadTo(cx, cy - 3.5f, cx + 16f, cy - 11f);
        g2.setColorRgba(185, 96, 8, 255);
        g2.setStrokeRound(2.0f);
        g2.drawPath(crease);

        // Signature bright white-gold specular gleam
        g2.setColorRgba(255, 255, 255, 255);
        HdPath gleam = g2.createPath();
        gleam.moveTo(cx + 3f, cy - 3f);
        gleam.lineTo(cx + 10f, cy - 6.5f);
        gleam.lineTo(cx + 15f, cy - 3f);
        gleam.lineTo(cx + 3f, cy - 3f);
        gleam.closePath();
        g2.fillPath(gleam);
        g2.fillOval(cx - 21f, cy - 13.5f, 5f, 3.5f);
        g2.fillOval(cx + 16f, cy - 13.5f, 5f, 3.5f);

        g2.setColorRgba(88, 42, 4, 255);
        g2.setStrokeWidth(2.0f);
        g2.drawPath(boat);
    }

    /**
     * 6) {@code /book}:
     * Ice-blue square plaque + diagonal rolled Lavender-Purple Silk Scroll (Binh Thư) tied with green ribbon.
     */
    private static void drawOriginalPurpleScrollBookHd(HdCanvas2D g2, int sz) {
        float bx = 7.5f;
        float by = 7.5f;
        float bw = sz - 15f;
        float bh = sz - 15f;

        g2.setColorRgba(0, 0, 0, 135);
        g2.fillRoundRect(bx + 1.5f, by + 2f, bw, bh, 9f);

        g2.setLinearGradient(
                bx, by, bx + bw, by + bh,
                new float[]{0f, 0.5f, 1f},
                new int[]{c(220, 240, 255), c(152, 196, 235), c(92, 136, 182)});
        g2.fillRoundRect(bx, by, bw, bh, 8f);
        g2.setColorRgba(22, 38, 58, 255);
        g2.setStrokeWidth(1.6f);
        g2.drawRoundRect(bx, by, bw, bh, 8f);

        g2.setLinearGradient(
                bx + 4f, by + 4f, bx + bw - 4f, by + bh - 4f,
                new float[]{0f, 1f},
                new int[]{c(72, 104, 138), c(105, 145, 185)});
        g2.fillRoundRect(bx + 4.5f, by + 4.5f, bw - 9f, bh - 9f, 5f);
        g2.setColorRgba(42, 68, 96, 255);
        g2.setStrokeWidth(1.4f);
        g2.drawRoundRect(bx + 4.5f, by + 4.5f, bw - 9f, bh - 9f, 5f);

        // Diagonal Rolled Lavender-Purple Silk Scroll (+45°)
        g2.save();
        g2.translate(sz * 0.5, sz * 0.5);
        g2.rotate(Math.toRadians(45));

        g2.setColorRgba(0, 0, 0, 110);
        g2.fillRoundRect(-11, -24, 24, 47, 10);

        g2.setLinearGradient(
                -12f, 0f, 12f, 0f,
                new float[]{0f, 0.35f, 0.75f, 1f},
                new int[]{
                        c(252, 225, 255),
                        c(225, 148, 252),
                        c(172, 72, 225),
                        c(112, 28, 165)
                });
        g2.fillRoundRect(-12f, -24f, 24f, 45f, 10f);
        g2.setColorRgba(68, 16, 104, 255);
        g2.setStrokeWidth(1.6f);
        g2.drawRoundRect(-12f, -24f, 24f, 45f, 10f);

        g2.setColorRgba(255, 238, 255, 255);
        g2.fillOval(-10f, -23f, 20f, 10f);

        g2.setColorRgba(245, 205, 255, 255);
        g2.fillOval(-12f, 10f, 24f, 14f);
        g2.setColorRgba(88, 18, 135, 255);
        g2.setStrokeWidth(1.8f);
        g2.drawOval(-12f, 10f, 24f, 14f);

        g2.setColorRgba(145, 42, 198, 255);
        g2.drawOval(-8f, 12.5f, 16f, 9f);
        g2.setColorRgba(74, 10, 115, 255);
        g2.fillOval(-4f, 14.8f, 8f, 4.5f);

        g2.setLinearGradient(
                -12.5f, -5f, 12.5f, 3f,
                new float[]{0f, 0.5f, 1f},
                new int[]{c(165, 255, 95), c(72, 208, 36), c(24, 118, 12)});
        g2.fillRoundRect(-12, -6, 24, 8, 4);
        g2.setColorRgba(16, 78, 8, 255);
        g2.setStrokeWidth(1.3f);
        g2.drawRoundRect(-12, -6, 24, 8, 4);

        g2.restore();

        // Two fluttering green ribbon tails extending toward bottom-right
        HdPath tail1 = g2.createPath();
        tail1.moveTo(35f, 29f);
        tail1.quadTo(48f, 31f, 56f, 41f);
        tail1.lineTo(50f, 45f);
        tail1.quadTo(42f, 37f, 33f, 34f);
        tail1.closePath();

        HdPath tail2 = g2.createPath();
        tail2.moveTo(33f, 33f);
        tail2.quadTo(41f, 45f, 48f, 56f);
        tail2.lineTo(41f, 57f);
        tail2.quadTo(35f, 46f, 30f, 36f);
        tail2.closePath();

        g2.setLinearGradient(
                32f, 31f, 54f, 54f,
                new float[]{0f, 0.5f, 1f},
                new int[]{c(155, 252, 85), c(68, 202, 32), c(22, 112, 10)});
        g2.fillPath(tail1);
        g2.fillPath(tail2);
        g2.setColorRgba(16, 75, 8, 255);
        g2.setStrokeWidth(1.3f);
        g2.drawPath(tail1);
        g2.drawPath(tail2);

        g2.setColorRgba(178, 255, 110, 255);
        g2.fillOval(31f, 29f, 8f, 8f);
        g2.setColorRgba(18, 82, 10, 255);
        g2.drawOval(31f, 29f, 8f, 8f);
    }
}
