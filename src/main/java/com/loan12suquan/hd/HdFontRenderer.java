package com.loan12suquan.hd;

import com.loan12suquan.hd.platform.HdCanvas2D;
import com.loan12suquan.hd.platform.HdPlatform;

/**
 * Cross-platform High-Definition TrueType Vietnamese Typography Engine for the {@code 720x960} ({@code 3x}) HD canvas.
 */
public final class HdFontRenderer {

    private HdFontRenderer() {
    }

    public static void drawHdShadowText(
            HdCanvas2D g2,
            boolean bold,
            String text,
            int logicalX,
            int logicalY,
            int anchor,
            int fgRgb,
            int shadowRgb,
            int maxLogicalWidth,
            int logicalLineH) {
        if (g2 == null || text == null || text.isEmpty()) {
            return;
        }

        final int s = HdConfig.HD_SCALE;
        int fontKind = bold ? HdCanvas2D.FONT_BOLD : HdCanvas2D.FONT_PLAIN;

        int rawHdW = g2.measureTextWidth(fontKind, text);
        int targetMaxHdW = Math.max(12, maxLogicalWidth * s + 8);
        double scaleX = (rawHdW > targetMaxHdW && rawHdW > 0)
                ? ((double) targetMaxHdW / (double) rawHdW)
                : 1.0;
        int effectiveHdW = (int) Math.round(rawHdW * scaleX);
        int hdLineH = logicalLineH * s;

        int drawHdX = logicalX * s;
        int drawHdY = logicalY * s;

        if ((anchor & 1) != 0) {
            drawHdX -= effectiveHdW / 2;
        } else if ((anchor & 8) != 0) {
            drawHdX -= effectiveHdW;
        }

        if ((anchor & 2) != 0) {
            drawHdY -= hdLineH / 2;
        } else if ((anchor & 32) != 0) {
            drawHdY -= hdLineH;
        }

        int ascent = g2.getFontAscent(fontKind);
        int descent = g2.getFontDescent(fontKind);
        int baselineY = drawHdY + ((hdLineH - (ascent + descent)) / 2) + ascent;

        drawStandardGlyphs(g2, fontKind, text, drawHdX, baselineY, scaleX, fgRgb, shadowRgb, 2.3f);
    }

    public static void drawDisplayCenteredHd(
            HdCanvas2D g2,
            String text,
            int hdCenterX,
            int hdBaselineY,
            boolean largeDisplay,
            int topRgb,
            int botRgb,
            int strokeRgb,
            float strokeWidth) {
        if (g2 == null || text == null || text.isEmpty()) {
            return;
        }
        int fontKind = largeDisplay ? HdCanvas2D.FONT_DISPLAY : HdCanvas2D.FONT_SUBTITLE;
        int w = g2.measureTextWidth(fontKind, text);
        int maxW = HdConfig.PLAYFIELD_HD_WIDTH - 48;
        double scaleX = (w > maxW && w > 0) ? ((double) maxW / (double) w) : 1.0;
        int effW = (int) Math.round(w * scaleX);
        int drawX = hdCenterX - effW / 2;

        g2.drawStyledText(
                fontKind, text, drawX, hdBaselineY, scaleX,
                topRgb, botRgb,
                HdPlatform.argb(0x000000, 185),
                HdPlatform.opaque(strokeRgb),
                strokeWidth,
                0.0, 4.0);
    }

    public static int measureSubtitleHdWidth(HdCanvas2D g2, String text) {
        if (g2 == null || text == null || text.isEmpty()) {
            return 0;
        }
        return g2.measureTextWidth(HdCanvas2D.FONT_SUBTITLE, text);
    }

    public static void drawSubtitleHd(HdCanvas2D g2, String text, int hdX, int hdBaselineY, boolean centered, int fgRgb, int shadowRgb) {
        if (g2 == null || text == null || text.isEmpty()) {
            return;
        }
        int w = g2.measureTextWidth(HdCanvas2D.FONT_SUBTITLE, text);
        int drawX = centered ? (hdX - w / 2) : hdX;
        drawStandardGlyphs(g2, HdCanvas2D.FONT_SUBTITLE, text, drawX, hdBaselineY, 1.0, fgRgb, shadowRgb, 2.2f);
    }

    private static void drawStandardGlyphs(
            HdCanvas2D g2,
            int fontKind,
            String text,
            int drawHdX,
            int baselineY,
            double scaleX,
            int fgRgb,
            int shadowRgb,
            float outlineStroke) {
        int r = (fgRgb >>> 16) & 0xFF;
        int g = (fgRgb >>> 8) & 0xFF;
        int b = fgRgb & 0xFF;
        int topRgb = (Math.min(255, r + 20) << 16) | (Math.min(255, g + 18) << 8) | Math.min(255, b + 14);
        int botRgb = (Math.max(0, r - 14) << 16) | (Math.max(0, g - 16) << 8) | Math.max(0, b - 20);

        int sr = ((shadowRgb >>> 16) & 0xFF) / 2;
        int sg = ((shadowRgb >>> 8) & 0xFF) / 2;
        int sb = (shadowRgb & 0xFF) / 2;
        int outlineRgb = (sr << 16) | (sg << 8) | sb;

        g2.drawStyledText(
                fontKind, text, drawHdX, baselineY, scaleX,
                topRgb, botRgb,
                HdPlatform.argb(shadowRgb, 225),
                HdPlatform.argb(outlineRgb, 215),
                outlineStroke,
                1.8, 2.0);
    }
}
