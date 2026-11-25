package com.mg.engine.graphics;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdUiRenderer;
import com.mg.engine.core.GameConfig;
import com.mg.engine.util.ObjectList;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Low-level MIDP {@link Graphics} clipping, sprite region, and primitive border helpers. */
public class DrawUtils {
    public static final Font UI_FONT_BOLD = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
    public static final Font UI_FONT_PLAIN = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    static int defaultAlpha = 255;
    public static byte[][] blurKernel3x3 = new byte[][]{{1, 2, 1}, {2, 4, 2}, {1, 2, 1}};
    public static byte[][] blurKernel5x5 = new byte[][]{{0, 1, 2, 1, 0}, {1, 3, 5, 3, 1}, {2, 5, 9, 5, 2}, {1, 3, 5, 3, 1}, {0, 1, 2, 1, 0}};
    public static Rect tempClip = new Rect(0, 0, GameConfig.screenWidth, GameConfig.screenHeight);

    public static void saveClip(Graphics g, Rect outClip) {
        outClip.a = g.getClipX();
        outClip.b = g.getClipY();
        outClip.c = g.getClipWidth();
        outClip.d = g.getClipHeight();
    }

    public static void intersectClip(Graphics g, int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        int left = x1 < x2 ? x2 : x1;
        int top = y1 < y2 ? y2 : y1;
        long right = Math.min((long) x1 + (long) w1, (long) x2 + (long) w2) - (long) left;
        long bottom = Math.min((long) y1 + (long) h1, (long) y2 + (long) h2) - (long) top;
        if (right < Integer.MIN_VALUE) {
            right = Integer.MIN_VALUE;
        }
        if (bottom < Integer.MIN_VALUE) {
            bottom = Integer.MIN_VALUE;
        }
        g.setClip(left, top, (int) right, (int) bottom);
    }

    public static void restoreClip(Graphics g, Rect clip) {
        g.setClip(clip.a, clip.b, clip.c, clip.d);
    }

    public static void intersectClip(Graphics g, Rect baseClip, int x, int y, int width, int height) {
        intersectClip(g, baseClip.a, baseClip.b, baseClip.c, baseClip.d, x, y, width, height);
    }

    public static void intersectClip(Graphics g, int x, int y, int width, int height) {
        intersectClip(g, g.getClipX(), g.getClipY(), g.getClipWidth(), g.getClipHeight(), x, y, width, height);
    }

    public static final void drawRegion(Graphics g, Image image, int srcX, int srcY, int width, int height,
                                        int destX, int destY, int anchor) {
        if ((anchor & Graphics.HCENTER) > 0) {
            destX -= width >> 1;
        } else if ((anchor & Graphics.RIGHT) > 0) {
            destX -= width;
        }

        if ((anchor & Graphics.VCENTER) > 0) {
            destY -= height >> 1;
        } else if ((anchor & Graphics.BOTTOM) > 0) {
            destY -= height;
        }

        saveClip(g, tempClip);
        intersectClip(g, tempClip, destX, destY, width, height);
        g.drawImage(image, destX - srcX, destY - srcY, 0);
        restoreClip(g, tempClip);
    }

    public static void drawHatchedOverlay(Graphics g, int color, int x, int y, int width, int height) {
        if (HdConfig.isHdRemaster() && g != null && g.getHdCanvas() != null) {
            HdUiRenderer.drawHdDimOverlay(g.getHdCanvas(), x, y, width, height, color, 178);
            return;
        }
        g.setColor(color);
        int x1 = x;
        int y1 = y;
        int x2 = x;
        int y2 = y;

        while (true) {
            g.drawLine(x1, y1, x2, y2);
            if (y1 < y + height) {
                y1 += 2;
            } else {
                x1 += 2;
                if (x1 > x + width) {
                    break;
                }
            }

            if (x2 < x + width) {
                x2 += 2;
            } else {
                y2 += 2;
                if (y2 > y + height) {
                    break;
                }
            }
        }
    }

    public static void drawRoundedBorder(Graphics g, int color, int x, int y, int width, int height) {
        g.setColor(color);
        g.fillRect(x + 2, y, width - 4, 1);
        g.fillRect(x + 2, y + height - 1, width - 4, 1);
        g.fillRect(x, y + 2, 1, height - 4);
        g.fillRect(x + width - 1, y + 2, 1, height - 4);
        g.fillRect(x + 1, y + 1, 1, 1);
        g.fillRect(x + 1, y + height - 2, 1, 1);
        g.fillRect(x + width - 2, y + 1, 1, 1);
        g.fillRect(x + width - 2, y + height - 2, 1, 1);
    }

    /**
     * Draws a pixel-art beveled antique wood-and-bronze plaque with 1px corner cuts,
     * 3-band interior shading, top/left inner highlight, bottom/right inner shadow, and 4 corner rivets.
     */
    public static void drawAntiquePanel(
            Graphics g,
            int x,
            int y,
            int w,
            int h,
            int bgTop,
            int bgBody,
            int bgBottom,
            int borderOuter,
            int borderHighlight,
            int rivetColor) {
        if (w < 6 || h < 6) {
            return;
        }
        if (HdConfig.isHdRemaster() && g != null && g.getHdCanvas() != null) {
            HdUiRenderer.drawHdAntiquePanel(
                    g.getHdCanvas(),
                    x, y, w, h,
                    bgTop, bgBody, bgBottom,
                    borderOuter, borderHighlight, rivetColor,
                    w);
            return;
        }
        // Main beveled fill (1px corner cut)
        g.setColor(bgBody);
        g.fillRect(x + 1, y + 1, w - 2, h - 2);
        // Top gleam band & bottom shadow band
        int topBandH = h > 16 ? 4 : 2;
        g.setColor(bgTop);
        g.fillRect(x + 2, y + 2, w - 4, topBandH);
        g.setColor(bgBottom);
        g.fillRect(x + 2, y + h - 4, w - 4, 2);

        // Outer beveled bronze border (1px corner cutout)
        g.setColor(borderOuter);
        g.fillRect(x + 1, y, w - 2, 1);
        g.fillRect(x + 1, y + h - 1, w - 2, 1);
        g.fillRect(x, y + 1, 1, h - 2);
        g.fillRect(x + w - 1, y + 1, 1, h - 2);

        // Inner top-left gold bevel highlight
        g.setColor(borderHighlight);
        g.fillRect(x + 2, y + 1, w - 4, 1);
        g.fillRect(x + 1, y + 2, 1, h - 4);

        // Inner bottom-right dark bevel shadow
        g.setColor(0x120A06);
        g.fillRect(x + 2, y + h - 2, w - 4, 1);
        g.fillRect(x + w - 2, y + 2, 1, h - 4);

        // 4 pixel corner rivets
        g.setColor(rivetColor);
        g.fillRect(x + 2, y + 2, 1, 1);
        g.fillRect(x + w - 3, y + 2, 1, 1);
        g.fillRect(x + 2, y + h - 3, 1, 1);
        g.fillRect(x + w - 3, y + h - 3, 1, 1);
    }

    /**
     * Draws a chamfered antique RPG gemstone gauge with 3-band liquid shading, leading-edge meniscus,
     * optional proportional scale ticks ({@code tickDivisions == 4} for 25% pips, {@code 3} for Mana tiers),
     * and vertically centered crisp pixel-font {@code overlayText}.
     */
    public static void drawRpgStatBar(
            Graphics g,
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
            String overlayText) {
        if (w < 10 || h < 10) {
            return;
        }
        if (HdConfig.isHdRemaster() && g != null && g.getHdCanvas() != null) {
            int logicalTextW = overlayText != null ? VietnamesePixelFont.PLAIN.stringWidth(overlayText) : 0;
            HdUiRenderer.drawHdRpgStatBar(
                    g.getHdCanvas(),
                    x, y, w, h,
                    current, max,
                    colorTop, colorMid, colorBot, colorEdge,
                    tickDivisions, overlayText, logicalTextW);
            return;
        }
        // 1. Recessed dark mahogany trough with 1px chamfered corners
        g.setColor(0x100805);
        g.fillRect(x + 1, y + 1, w - 2, h - 2);
        g.setColor(0x1C100A);
        g.fillRect(x + 2, y + (h >> 1), w - 4, (h >> 1) - 2);

        // 2. Beveled bronze-gold outer capsule rim
        g.setColor(0xA67B38);
        g.fillRect(x + 1, y, w - 2, 1);
        g.fillRect(x, y + 1, 1, h - 2);
        g.setColor(0x6E4E22);
        g.fillRect(x + 1, y + h - 1, w - 2, 1);
        g.fillRect(x + w - 1, y + 1, 1, h - 2);
        // Inner dark bevel lip
        g.setColor(0x0A0503);
        g.drawRect(x + 1, y + 1, w - 3, h - 3);
        // Top bronze gleam along outer rim
        g.setColor(0xE0B665);
        g.fillRect(x + 2, y, w - 4, 1);

        // 3. True-to-scale 3-band glossy liquid fill (long math avoids int overflow)
        int innerX = x + 2;
        int innerY = y + 2;
        int innerW = w - 4;
        int innerH = h - 4;
        int fillW = 0;
        if (max > 0 && current > 0) {
            int clamped = current > max ? max : current;
            fillW = (int) ((long) innerW * (long) clamped / (long) max);
            if (fillW < 1) {
                fillW = 1;
            }
            if (fillW > innerW) {
                fillW = innerW;
            }
            g.setColor(colorMid);
            g.fillRect(innerX, innerY, fillW, innerH);
            if (innerH >= 6) {
                g.setColor(colorTop);
                g.fillRect(innerX, innerY, fillW, 2);
                g.setColor(colorBot);
                g.fillRect(innerX, innerY + innerH - 2, fillW, 2);
            } else {
                g.setColor(colorTop);
                g.fillRect(innerX, innerY, fillW, 1);
            }
            if (fillW > 2) {
                g.setColor(colorEdge);
                g.fillRect(innerX + 1, innerY, fillW - 2, 1);
            }
            if (fillW < innerW) {
                g.setColor(colorEdge);
                g.fillRect(innerX + fillW - 1, innerY, 1, innerH);
            }
        }

        // 4. Scale ticks (3 = full Mana tier dividers; 4 = subtle 25%/50%/75% bottom pips)
        if (tickDivisions == 3) {
            for (int t = 1; t < 3; ++t) {
                int tx = innerX + (innerW * t) / 3;
                g.setColor(0x100805);
                g.drawLine(tx, innerY, tx, innerY + innerH - 1);
                g.setColor(0xD8AD5E);
                g.fillRect(tx, y, 1, 1);
                g.fillRect(tx, y + h - 1, 1, 1);
            }
        } else if (tickDivisions == 4 && innerH >= 6) {
            for (int t = 1; t < 4; ++t) {
                int tx = innerX + (innerW * t) / 4;
                g.setColor((tx - innerX) < fillW ? 0x24120A : 0x4A341B);
                g.fillRect(tx, innerY + innerH - 2, 1, 2);
            }
        }

        // 5. Crisp centered value readout
        if (overlayText != null && overlayText.length() > 0) {
            int plainH = VietnamesePixelFont.PLAIN.getHeight();
            int textY = y + ((h - plainH) >> 1);
            VietnamesePixelFont.PLAIN.drawShadowString(
                    g, overlayText,
                    x + (w >> 1), textY,
                    Graphics.TOP | Graphics.HCENTER,
                    0xFFFFFF, 0x100603);
        }
    }

    private static VietnamesePixelFont resolvePixelFont(Font font) {
        return (font == UI_FONT_BOLD) ? VietnamesePixelFont.BOLD : VietnamesePixelFont.PLAIN;
    }

    /** Returns the pixel line height of our compact Vietnamese pixel font (12px bold, 11px plain). */
    public static int getFontHeight(Font font) {
        return resolvePixelFont(font).getHeight();
    }

    /** Returns the exact pixel width of {@code text} in our compact Vietnamese pixel font. */
    public static int getStringWidth(Font font, String text) {
        return resolvePixelFont(font).stringWidth(text);
    }

    /** Draws crisp compact Vietnamese pixel-art text with a 1px drop shadow. */
    public static void drawShadowText(
            Graphics g, Font font, String text, int x, int y, int anchor, int color, int shadowColor) {
        resolvePixelFont(font).drawShadowString(g, text, x, y, anchor, color, shadowColor);
    }

    /**
     * Draws a full name inside [boxX, boxX + boxW] using our compact Vietnamese pixel font:
     * picks bold pixel font if it fits, plain pixel font otherwise, and smoothly ping-pong scrolls
     * inside a clipped viewport only if the text still exceeds boxW.
     */
    public static void drawFullNameInBox(
            Graphics g, String text, int boxX, int boxY, int boxW, int boxH,
            boolean alignRight, int color, int shadowColor, int animTick) {
        VietnamesePixelFont pFont = VietnamesePixelFont.BOLD;
        int textW = pFont.stringWidth(text);
        if (textW > boxW) {
            pFont = VietnamesePixelFont.PLAIN;
            textW = pFont.stringWidth(text);
        }
        int drawY = boxY + ((boxH - pFont.getHeight()) >> 1);
        if (textW <= boxW) {
            int drawX = alignRight ? (boxX + boxW) : boxX;
            int anchor = Graphics.TOP | (alignRight ? Graphics.RIGHT : Graphics.LEFT);
            pFont.drawShadowString(g, text, drawX, drawY, anchor, color, shadowColor);
            return;
        }
        int overflow = textW - boxW + 2;
        int pause = 20;
        int cycle = (overflow + pause) * 2;
        int phase = (animTick / 2) % cycle;
        int scrollOffset;
        if (phase < pause) {
            scrollOffset = 0;
        } else if (phase < pause + overflow) {
            scrollOffset = phase - pause;
        } else if (phase < pause * 2 + overflow) {
            scrollOffset = overflow;
        } else {
            scrollOffset = overflow - (phase - (pause * 2 + overflow));
        }
        saveClip(g, tempClip);
        intersectClip(g, tempClip, boxX, boxY, boxW, boxH);
        pFont.drawShadowString(g, text, boxX - scrollOffset, drawY, Graphics.TOP | Graphics.LEFT, color, shadowColor);
        restoreClip(g, tempClip);
    }

    /**
     * Wraps {@code text} into lines that each fit within {@code maxWidth} pixels when measured with our compact pixel font.
     */
    public static String[] wrapText(Font font, String text, int maxWidth) {
        if (text == null || text.length() == 0) {
            return new String[0];
        }
        VietnamesePixelFont pFont = resolvePixelFont(font);
        ObjectList lines = new ObjectList();
        int len = text.length();
        int idx = 0;
        String currentLine = "";

        while (idx < len) {
            while (idx < len && text.charAt(idx) == ' ') {
                ++idx;
            }
            if (idx >= len) {
                break;
            }
            int wordStart = idx;
            while (idx < len && text.charAt(idx) != ' ') {
                ++idx;
            }
            String word = text.substring(wordStart, idx);
            String candidate = currentLine.length() == 0 ? word : (currentLine + " " + word);
            if (pFont.stringWidth(candidate) <= maxWidth) {
                currentLine = candidate;
            } else {
                if (currentLine.length() > 0) {
                    lines.a(currentLine);
                    currentLine = "";
                }
                if (pFont.stringWidth(word) <= maxWidth) {
                    currentLine = word;
                } else {
                    String chunk = "";
                    for (int c = 0; c < word.length(); ++c) {
                        String nextChunk = chunk + word.charAt(c);
                        if (pFont.stringWidth(nextChunk) <= maxWidth || chunk.length() == 0) {
                            chunk = nextChunk;
                        } else {
                            lines.a(chunk);
                            chunk = "" + word.charAt(c);
                        }
                    }
                    currentLine = chunk;
                }
            }
        }
        if (currentLine.length() > 0) {
            lines.a(currentLine);
        }

        String[] result = new String[lines.b()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = (String) lines.b(i);
        }
        return result;
    }

    /**
     * Returns the exact pixel height occupied by {@code lines} when drawn with {@link #drawWrappedShadowText}.
     */
    public static int getWrappedHeight(Font font, String[] lines) {
        if (lines == null || lines.length == 0) {
            return 0;
        }
        int fontH = resolvePixelFont(font).getHeight();
        return lines.length * (fontH + 1) - 1;
    }

    /**
     * Draws word-wrapped lines using {@link #drawShadowText} within {@code maxHeight}.
     */
    public static void drawWrappedShadowText(
            Graphics g, Font font, String[] lines,
            int x, int y, int width, int maxHeight,
            int color, int shadowColor) {
        if (lines == null || lines.length == 0) {
            return;
        }
        VietnamesePixelFont pFont = resolvePixelFont(font);
        int fontH = pFont.getHeight();
        int lineStep = fontH + 1;
        int curY = y;
        for (int i = 0; i < lines.length; ++i) {
            if (curY + fontH > y + maxHeight + 2) {
                break;
            }
            pFont.drawShadowString(g, lines[i], x, curY, Graphics.TOP | Graphics.LEFT, color, shadowColor);
            curY += lineStep;
        }
    }

    /**
     * Draws horizontally centered word-wrapped lines within {@code maxHeight}.
     */
    public static void drawWrappedCenteredShadowText(
            Graphics g, Font font, String[] lines,
            int centerX, int y, int maxHeight,
            int color, int shadowColor) {
        if (lines == null || lines.length == 0) {
            return;
        }
        VietnamesePixelFont pFont = resolvePixelFont(font);
        int fontH = pFont.getHeight();
        int lineStep = fontH + 1;
        int curY = y;
        for (int i = 0; i < lines.length; ++i) {
            if (curY + fontH > y + maxHeight + 2) {
                break;
            }
            pFont.drawShadowString(g, lines[i], centerX, curY, Graphics.TOP | Graphics.HCENTER, color, shadowColor);
            curY += lineStep;
        }
    }
}



