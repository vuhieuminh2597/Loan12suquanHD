package com.mg.engine.graphics;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdFontRenderer;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Custom compact Vietnamese pixel-art font engine designed specifically for 240x320 J2ME screens.
 * Provides two harmonized pixel faces:
 * <ul>
 *   <li>{@link #PLAIN}: 4x7 compact body (11px line height, ~4.4px average advance)</li>
 *   <li>{@link #BOLD}: 5x7 bold-stem body with crisp 1px Vietnamese diacritics (12px line height, ~5.2px average advance)</li>
 * </ul>
 * Pre-composites foreground + 1px drop-shadow into an LRU cache of MIDP-2.0 translucent {@link Image} strips
 * so each character renders in a single hardware-accelerated {@code g.drawRegion(...)} call.
 */
public final class VietnamesePixelFont {
    public static final String CHARSET =
            " 0123456789.,:!?()+-*/#$%\"'@<=>;_&[]|~"
            + "abcdefghijklmnopqrstuvwxyz"
            + "áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "ÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỴĐ";

    private static final int GLYPH_HEIGHT = 12;
    private static final int STRIP_HEIGHT = GLYPH_HEIGHT + 1; // +1px for bottom drop shadow
    private static final int CACHE_SIZE = 16;

    // Diacritic modifier constants
    private static final int MOD_NONE = 0;
    private static final int MOD_BREVE = 1;      // ă
    private static final int MOD_CIRCUMFLEX = 2; // â, ê, ô
    private static final int MOD_HORN = 3;       // ơ, ư

    // Vietnamese tone constants
    private static final int TONE_NONE = 0;
    private static final int TONE_SAC = 1;   // ´
    private static final int TONE_HUYEN = 2; // `
    private static final int TONE_HOI = 3;   // ?
    private static final int TONE_NGA = 4;   // ~
    private static final int TONE_NANG = 5;  // .

    private static final String VIET_LOWER =
            "áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ";
    private static final String VIET_UPPER =
            "ÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỴ";

    public static final VietnamesePixelFont PLAIN = new VietnamesePixelFont(false);
    public static final VietnamesePixelFont BOLD = new VietnamesePixelFont(true);

    private final boolean bold;
    private final int lineHeight;
    private final int glyphCount;
    private final int[] glyphWidths;
    private final int[] cellOffsetsX;
    private final int stripWidth;
    private final byte[] masterMask; // 1 = foreground pixel, 0 = transparent (size: stripWidth * GLYPH_HEIGHT)

    private final int[] cacheKeys = new int[CACHE_SIZE];
    private final Image[] cacheStrips = new Image[CACHE_SIZE];
    private int cacheClock = 0;
    private final int[] cacheLastUsed = new int[CACHE_SIZE];

    private VietnamesePixelFont(boolean bold) {
        this.bold = bold;
        this.lineHeight = bold ? 12 : 11;
        this.glyphCount = CHARSET.length();
        this.glyphWidths = new int[this.glyphCount];
        this.cellOffsetsX = new int[this.glyphCount];

        int[][] allRows = new int[this.glyphCount][GLYPH_HEIGHT];
        int totalW = 0;
        for (int i = 0; i < this.glyphCount; ++i) {
            char ch = CHARSET.charAt(i);
            int[] rows = allRows[i];
            int w = buildGlyph(ch, this.bold, rows);
            this.glyphWidths[i] = w;
            this.cellOffsetsX[i] = totalW;
            totalW += w + 2; // +1px shadow column + 1px safety guard
        }
        this.stripWidth = totalW;
        this.masterMask = new byte[this.stripWidth * GLYPH_HEIGHT];

        for (int i = 0; i < this.glyphCount; ++i) {
            int ox = this.cellOffsetsX[i];
            int w = this.glyphWidths[i];
            int[] rows = allRows[i];
            for (int y = 0; y < GLYPH_HEIGHT; ++y) {
                int bits = rows[y];
                if (bits == 0) {
                    continue;
                }
                int rowBase = y * this.stripWidth + ox;
                for (int dx = 0; dx < w; ++dx) {
                    if ((bits & (1 << (w - 1 - dx))) != 0) {
                        this.masterMask[rowBase + dx] = 1;
                    }
                }
            }
        }

        for (int i = 0; i < CACHE_SIZE; ++i) {
            this.cacheKeys[i] = Integer.MIN_VALUE;
        }
    }

    public int getHeight() {
        return this.lineHeight;
    }

    public int charWidth(char ch) {
        int idx = indexOfChar(ch);
        return this.glyphWidths[idx];
    }

    public int stringWidth(String text) {
        if (text == null || text.length() == 0) {
            return 0;
        }
        int len = text.length();
        int total = 0;
        for (int i = 0; i < len; ++i) {
            int idx = indexOfChar(text.charAt(i));
            total += this.glyphWidths[idx] + 1;
        }
        return total - 1;
    }

    public void drawShadowString(Graphics g, String text, int x, int y, int anchor, int fgColor, int shadowColor) {
        if (text == null || text.length() == 0) {
            return;
        }
        if (HdConfig.isHdRemaster() && g != null && g.getHdCanvas() != null) {
            HdFontRenderer.drawHdShadowText(
                    g.getHdCanvas(),
                    this.bold,
                    text,
                    x,
                    y,
                    anchor,
                    fgColor,
                    shadowColor,
                    this.stringWidth(text),
                    this.lineHeight);
            return;
        }
        int len = text.length();
        int drawX = x;
        int drawY = y;

        if ((anchor & Graphics.HCENTER) != 0) {
            drawX -= this.stringWidth(text) >> 1;
        } else if ((anchor & Graphics.RIGHT) != 0) {
            drawX -= this.stringWidth(text);
        }

        if ((anchor & Graphics.VCENTER) != 0) {
            drawY -= this.lineHeight >> 1;
        } else if ((anchor & Graphics.BOTTOM) != 0) {
            drawY -= this.lineHeight;
        }

        Image strip = getOrCreateStrip(fgColor & 0xFFFFFF, shadowColor & 0xFFFFFF);
        int stripW = strip.getWidth();
        for (int i = 0; i < len; ++i) {
            char ch = text.charAt(i);
            int idx = indexOfChar(ch);
            int w = this.glyphWidths[idx];
            if (idx > 0 && w > 0) {
                int srcX = this.cellOffsetsX[idx];
                int regionW = w + 1;
                if (srcX >= 0 && srcX + regionW <= stripW) {
                    g.drawRegion(
                            strip,
                            srcX, 0,
                            regionW, STRIP_HEIGHT,
                            0,
                            drawX, drawY,
                            Graphics.TOP | Graphics.LEFT);
                }
            }
            drawX += w + 1;
        }
    }

    private Image getOrCreateStrip(int fgRgb, int shadowRgb) {
        int key = (fgRgb * 16777619) ^ (shadowRgb * 31) ^ 0x5A5A5A5A;
        if (key == Integer.MIN_VALUE) {
            key = 1;
        }
        ++this.cacheClock;
        for (int i = 0; i < CACHE_SIZE; ++i) {
            if (this.cacheKeys[i] == key && this.cacheStrips[i] != null) {
                this.cacheLastUsed[i] = this.cacheClock;
                return this.cacheStrips[i];
            }
        }

        int evictIdx = 0;
        int oldest = Integer.MAX_VALUE;
        for (int i = 0; i < CACHE_SIZE; ++i) {
            if (this.cacheStrips[i] == null) {
                evictIdx = i;
                break;
            }
            if (this.cacheLastUsed[i] < oldest) {
                oldest = this.cacheLastUsed[i];
                evictIdx = i;
            }
        }

        int sw = this.stripWidth;
        int[] argb = new int[sw * STRIP_HEIGHT];
        int fgPixel = 0xFF000000 | fgRgb;
        int shPixel = 0xFF000000 | shadowRgb;
        byte[] mask = this.masterMask;

        // Pass 1: 1px bottom-right drop shadow
        for (int y = 0; y < GLYPH_HEIGHT; ++y) {
            int srcRow = y * sw;
            int dstRow = (y + 1) * sw + 1;
            for (int x = 0; x < sw - 1; ++x) {
                if (mask[srcRow + x] != 0) {
                    argb[dstRow + x] = shPixel;
                }
            }
        }

        // Pass 2: foreground pixels on top
        for (int y = 0; y < GLYPH_HEIGHT; ++y) {
            int row = y * sw;
            for (int x = 0; x < sw; ++x) {
                if (mask[row + x] != 0) {
                    argb[row + x] = fgPixel;
                }
            }
        }

        Image created = Image.createRGBImage(argb, sw, STRIP_HEIGHT, true);
        this.cacheKeys[evictIdx] = key;
        this.cacheStrips[evictIdx] = created;
        this.cacheLastUsed[evictIdx] = this.cacheClock;
        return created;
    }

    private static int indexOfChar(char ch) {
        int idx = CHARSET.indexOf(ch);
        return idx >= 0 ? idx : 0;
    }

    private static int buildGlyph(char ch, boolean bold, int[] outRows) {
        char baseChar = ch;
        int mod = MOD_NONE;
        int tone = TONE_NONE;
        boolean isUpperViet = false;

        if (ch == 'đ') {
            baseChar = 'đ';
        } else if (ch == 'Đ') {
            baseChar = 'Đ';
        } else {
            int vLow = VIET_LOWER.indexOf(ch);
            if (vLow >= 0) {
                int[] decoded = decodeVietIndex(vLow, false);
                baseChar = (char) decoded[0];
                mod = decoded[1];
                tone = decoded[2];
            } else {
                int vUp = VIET_UPPER.indexOf(ch);
                if (vUp >= 0) {
                    int[] decoded = decodeVietIndex(vUp, true);
                    baseChar = (char) decoded[0];
                    mod = decoded[1];
                    tone = decoded[2];
                    isUpperViet = true;
                }
            }
        }

        int w = fillBaseGlyph(baseChar, isUpperViet, outRows);

        // For accented lowercase 'i' (í, ì, ỉ, ĩ), remove the top dot so the tone mark replaces it cleanly
        if (baseChar == 'i' && tone >= TONE_SAC && tone <= TONE_NGA) {
            outRows[3] = 0;
        }

        // Apply horizontal bolding to main glyph body (rows 3..11) if bold font face
        if (bold && baseChar != ' ') {
            int newW = w + 1;
            for (int y = 3; y < GLYPH_HEIGHT; ++y) {
                int b = outRows[y];
                outRows[y] = (b << 1) | b;
            }
            w = newW;
        }

        // Apply horn modifier (ơ, ư, Ơ, Ư) on the top-right shoulder of the vowel
        if (mod == MOD_HORN) {
            int hornW = w + 1;
            for (int y = 0; y < GLYPH_HEIGHT; ++y) {
                outRows[y] <<= 1;
            }
            if (isUpperViet) {
                outRows[3] |= 1;
                outRows[4] |= 3;
            } else {
                outRows[4] |= 1;
                outRows[5] |= 3;
            }
            w = hornW;
        }

        // Apply circumflex (^) or breve (˘) above the vowel
        if (mod == MOD_CIRCUMFLEX || mod == MOD_BREVE) {
            if (w < 3) {
                int pad = 3 - w;
                for (int y = 0; y < GLYPH_HEIGHT; ++y) {
                    outRows[y] <<= ((pad + 1) >> 1);
                }
                w = 3;
            }
            int shift = (w - 3) >> 1;
            int hatTop = (mod == MOD_CIRCUMFLEX) ? (0x2 << shift) : (0x5 << shift);
            int hatBot = (mod == MOD_CIRCUMFLEX) ? (0x5 << shift) : (0x2 << shift);
            outRows[2] |= hatTop;
            outRows[3] |= hatBot;
        }

        // Apply Vietnamese tone mark (sắc, huyền, hỏi, ngã, nặng)
        if (tone != TONE_NONE) {
            if (tone == TONE_NANG) {
                int dotShift = (mod == MOD_HORN) ? ((w + 1) >> 1) : (w >> 1);
                outRows[11] |= (1 << dotShift);
            } else {
                if (w < 3) {
                    int pad = 3 - w;
                    for (int y = 0; y < GLYPH_HEIGHT; ++y) {
                        outRows[y] <<= ((pad + 1) >> 1);
                    }
                    w = 3;
                }
                boolean hasHat = (mod == MOD_CIRCUMFLEX || mod == MOD_BREVE);
                int r0 = hasHat ? 0 : (isUpperViet ? 1 : 2);
                int r1 = r0 + 1;
                int effectiveW = (mod == MOD_HORN) ? (w - 1) : w;
                int centerShift = ((effectiveW - 3) >> 1) + ((mod == MOD_HORN) ? 1 : 0);
                if (centerShift < 0) {
                    centerShift = 0;
                }
                switch (tone) {
                    case TONE_SAC:
                        outRows[r0] |= (0x1 << centerShift);
                        outRows[r1] |= (0x2 << centerShift);
                        break;
                    case TONE_HUYEN:
                        outRows[r0] |= (0x4 << centerShift);
                        outRows[r1] |= (0x2 << centerShift);
                        break;
                    case TONE_HOI:
                        outRows[r0] |= (0x6 << centerShift);
                        outRows[r1] |= (0x2 << centerShift);
                        break;
                    case TONE_NGA:
                        outRows[r0] |= (0x3 << centerShift);
                        outRows[r1] |= (0x6 << centerShift);
                        break;
                }
            }
        }

        return w;
    }

    /**
     * Decodes index {@code 0..66} in {@link #VIET_LOWER} / {@link #VIET_UPPER} into
     * {@code {baseAsciiChar, modifier, tone}}.
     */
    private static int[] decodeVietIndex(int idx, boolean upper) {
        char base;
        int mod = MOD_NONE;
        int tone;

        if (idx < 17) {
            base = upper ? 'A' : 'a';
            if (idx < 5) {
                tone = idx + 1;
            } else if (idx == 5) {
                mod = MOD_BREVE;
                tone = TONE_NONE;
            } else if (idx < 11) {
                mod = MOD_BREVE;
                tone = idx - 5;
            } else if (idx == 11) {
                mod = MOD_CIRCUMFLEX;
                tone = TONE_NONE;
            } else {
                mod = MOD_CIRCUMFLEX;
                tone = idx - 11;
            }
        } else if (idx < 28) {
            base = upper ? 'E' : 'e';
            int sub = idx - 17;
            if (sub < 5) {
                tone = sub + 1;
            } else if (sub == 5) {
                mod = MOD_CIRCUMFLEX;
                tone = TONE_NONE;
            } else {
                mod = MOD_CIRCUMFLEX;
                tone = sub - 5;
            }
        } else if (idx < 33) {
            base = upper ? 'I' : 'i';
            tone = (idx - 28) + 1;
        } else if (idx < 50) {
            base = upper ? 'O' : 'o';
            int sub = idx - 33;
            if (sub < 5) {
                tone = sub + 1;
            } else if (sub == 5) {
                mod = MOD_CIRCUMFLEX;
                tone = TONE_NONE;
            } else if (sub < 11) {
                mod = MOD_CIRCUMFLEX;
                tone = sub - 5;
            } else if (sub == 11) {
                mod = MOD_HORN;
                tone = TONE_NONE;
            } else {
                mod = MOD_HORN;
                tone = sub - 11;
            }
        } else if (idx < 61) {
            base = upper ? 'U' : 'u';
            int sub = idx - 50;
            if (sub < 5) {
                tone = sub + 1;
            } else if (sub == 5) {
                mod = MOD_HORN;
                tone = TONE_NONE;
            } else {
                mod = MOD_HORN;
                tone = sub - 5;
            }
        } else {
            base = upper ? 'Y' : 'y';
            tone = (idx - 61) + 1;
        }

        return new int[]{base, mod, tone};
    }

    /**
     * Populates {@code r[0..11]} with the 1-bit pixel pattern for {@code baseChar} and returns its width.
     * Baseline is at row 9; lowercase x-height occupies rows 5..9 (5px); uppercase/digits occupy rows 3..9 (7px),
     * or rows 4..9 (6px) when {@code isUpperViet} is true so uppercase diacritics fit cleanly on rows 0..3.
     */
    private static int fillBaseGlyph(char ch, boolean isUpperViet, int[] r) {
        switch (ch) {
            case ' ':
                return 2;
            case '.':
                r[8] = 1; r[9] = 1;
                return 1;
            case ',':
                r[8] = 1; r[9] = 1; r[10] = 2;
                return 2;
            case ':':
                r[5] = 1; r[6] = 1; r[8] = 1; r[9] = 1;
                return 1;
            case ';':
                r[5] = 1; r[6] = 1; r[8] = 1; r[9] = 1; r[10] = 2;
                return 2;
            case '!':
                r[3] = 1; r[4] = 1; r[5] = 1; r[6] = 1; r[7] = 1; r[9] = 1;
                return 1;
            case '?':
                r[3] = 6; r[4] = 9; r[5] = 1; r[6] = 2; r[7] = 4; r[9] = 4;
                return 4;
            case '(':
                r[3] = 1; r[4] = 2; r[5] = 2; r[6] = 2; r[7] = 2; r[8] = 2; r[9] = 1;
                return 2;
            case ')':
                r[3] = 2; r[4] = 1; r[5] = 1; r[6] = 1; r[7] = 1; r[8] = 1; r[9] = 2;
                return 2;
            case '[':
                r[3] = 3; r[4] = 2; r[5] = 2; r[6] = 2; r[7] = 2; r[8] = 2; r[9] = 3;
                return 2;
            case ']':
                r[3] = 3; r[4] = 1; r[5] = 1; r[6] = 1; r[7] = 1; r[8] = 1; r[9] = 3;
                return 2;
            case '|':
                r[3] = 1; r[4] = 1; r[5] = 1; r[6] = 1; r[7] = 1; r[8] = 1; r[9] = 1;
                return 1;
            case '+':
                r[5] = 2; r[6] = 7; r[7] = 2;
                return 3;
            case '-':
                r[6] = 7;
                return 3;
            case '*':
                r[4] = 5; r[5] = 2; r[6] = 5;
                return 3;
            case '/':
                r[3] = 1; r[4] = 1; r[5] = 2; r[6] = 2; r[7] = 2; r[8] = 4; r[9] = 4;
                return 3;
            case '%':
                r[3] = 17; r[4] = 18; r[5] = 4; r[6] = 4; r[7] = 9; r[8] = 17; r[9] = 1;
                return 5;
            case '#':
                r[4] = 10; r[5] = 31; r[6] = 10; r[7] = 31; r[8] = 10;
                return 5;
            case '$':
                r[3] = 2; r[4] = 7; r[5] = 10; r[6] = 6; r[7] = 3; r[8] = 14; r[9] = 4;
                return 4;
            case '"':
                r[3] = 5; r[4] = 5;
                return 3;
            case '\'':
                r[3] = 1; r[4] = 1;
                return 1;
            case '@':
                r[3] = 14; r[4] = 17; r[5] = 21; r[6] = 23; r[7] = 16; r[8] = 14;
                return 5;
            case '<':
                r[4] = 1; r[5] = 2; r[6] = 4; r[7] = 2; r[8] = 1;
                return 3;
            case '=':
                r[5] = 7; r[7] = 7;
                return 3;
            case '>':
                r[4] = 4; r[5] = 2; r[6] = 1; r[7] = 2; r[8] = 4;
                return 3;
            case '_':
                r[9] = 15;
                return 4;
            case '&':
                r[3] = 6; r[4] = 9; r[5] = 6; r[6] = 13; r[7] = 9; r[8] = 10; r[9] = 5;
                return 4;
            case '~':
                r[5] = 6; r[6] = 12;
                return 4;

            // Digits 0..9 (4px wide, 7px tall on rows 3..9; '1' is 3px wide)
            case '0':
                r[3] = 6; r[4] = 9; r[5] = 9; r[6] = 9; r[7] = 9; r[8] = 9; r[9] = 6;
                return 4;
            case '1':
                r[3] = 2; r[4] = 6; r[5] = 2; r[6] = 2; r[7] = 2; r[8] = 2; r[9] = 7;
                return 3;
            case '2':
                r[3] = 6; r[4] = 9; r[5] = 1; r[6] = 2; r[7] = 4; r[8] = 8; r[9] = 15;
                return 4;
            case '3':
                r[3] = 14; r[4] = 1; r[5] = 1; r[6] = 6; r[7] = 1; r[8] = 1; r[9] = 14;
                return 4;
            case '4':
                r[3] = 2; r[4] = 6; r[5] = 10; r[6] = 10; r[7] = 15; r[8] = 2; r[9] = 2;
                return 4;
            case '5':
                r[3] = 15; r[4] = 8; r[5] = 14; r[6] = 1; r[7] = 1; r[8] = 9; r[9] = 6;
                return 4;
            case '6':
                r[3] = 6; r[4] = 8; r[5] = 14; r[6] = 9; r[7] = 9; r[8] = 9; r[9] = 6;
                return 4;
            case '7':
                r[3] = 15; r[4] = 1; r[5] = 2; r[6] = 2; r[7] = 4; r[8] = 4; r[9] = 4;
                return 4;
            case '8':
                r[3] = 6; r[4] = 9; r[5] = 9; r[6] = 6; r[7] = 9; r[8] = 9; r[9] = 6;
                return 4;
            case '9':
                r[3] = 6; r[4] = 9; r[5] = 9; r[6] = 9; r[7] = 7; r[8] = 1; r[9] = 6;
                return 4;

            // Lowercase a..z + đ (rows 5..9 x-height, rows 3..9 ascenders, rows 5..11 descenders)
            case 'a':
                r[5] = 6; r[6] = 1; r[7] = 7; r[8] = 9; r[9] = 7;
                return 4;
            case 'b':
                r[3] = 8; r[4] = 8; r[5] = 14; r[6] = 9; r[7] = 9; r[8] = 9; r[9] = 14;
                return 4;
            case 'c':
                r[5] = 7; r[6] = 8; r[7] = 8; r[8] = 8; r[9] = 7;
                return 4;
            case 'd':
                r[3] = 1; r[4] = 1; r[5] = 7; r[6] = 9; r[7] = 9; r[8] = 9; r[9] = 7;
                return 4;
            case 'đ':
                r[3] = 2; r[4] = 7; r[5] = 14; r[6] = 18; r[7] = 18; r[8] = 18; r[9] = 14;
                return 5;
            case 'e':
                r[5] = 6; r[6] = 9; r[7] = 15; r[8] = 8; r[9] = 7;
                return 4;
            case 'f':
                r[3] = 3; r[4] = 4; r[5] = 7; r[6] = 4; r[7] = 4; r[8] = 4; r[9] = 4;
                return 3;
            case 'g':
                r[5] = 7; r[6] = 9; r[7] = 9; r[8] = 7; r[9] = 1; r[10] = 9; r[11] = 6;
                return 4;
            case 'h':
                r[3] = 8; r[4] = 8; r[5] = 14; r[6] = 9; r[7] = 9; r[8] = 9; r[9] = 9;
                return 4;
            case 'i':
                r[3] = 1; r[5] = 1; r[6] = 1; r[7] = 1; r[8] = 1; r[9] = 1;
                return 1;
            case 'j':
                r[3] = 1; r[5] = 1; r[6] = 1; r[7] = 1; r[8] = 1; r[9] = 1; r[10] = 5; r[11] = 2;
                return 3;
            case 'k':
                r[3] = 8; r[4] = 8; r[5] = 9; r[6] = 10; r[7] = 12; r[8] = 10; r[9] = 9;
                return 4;
            case 'l':
                r[3] = 2; r[4] = 2; r[5] = 2; r[6] = 2; r[7] = 2; r[8] = 2; r[9] = 1;
                return 2;
            case 'm':
                r[5] = 26; r[6] = 21; r[7] = 21; r[8] = 21; r[9] = 21;
                return 5;
            case 'n':
                r[5] = 14; r[6] = 9; r[7] = 9; r[8] = 9; r[9] = 9;
                return 4;
            case 'o':
                r[5] = 6; r[6] = 9; r[7] = 9; r[8] = 9; r[9] = 6;
                return 4;
            case 'p':
                r[5] = 14; r[6] = 9; r[7] = 9; r[8] = 14; r[9] = 8; r[10] = 8; r[11] = 8;
                return 4;
            case 'q':
                r[5] = 7; r[6] = 9; r[7] = 9; r[8] = 7; r[9] = 1; r[10] = 1; r[11] = 1;
                return 4;
            case 'r':
                r[5] = 5; r[6] = 6; r[7] = 4; r[8] = 4; r[9] = 4;
                return 3;
            case 's':
                r[5] = 7; r[6] = 8; r[7] = 6; r[8] = 1; r[9] = 14;
                return 4;
            case 't':
                r[3] = 2; r[4] = 2; r[5] = 7; r[6] = 2; r[7] = 2; r[8] = 2; r[9] = 3;
                return 3;
            case 'u':
                r[5] = 9; r[6] = 9; r[7] = 9; r[8] = 9; r[9] = 7;
                return 4;
            case 'v':
                r[5] = 9; r[6] = 9; r[7] = 9; r[8] = 6; r[9] = 6;
                return 4;
            case 'w':
                r[5] = 17; r[6] = 21; r[7] = 21; r[8] = 21; r[9] = 10;
                return 5;
            case 'x':
                r[5] = 9; r[6] = 6; r[7] = 6; r[8] = 9; r[9] = 9;
                return 4;
            case 'y':
                r[5] = 9; r[6] = 9; r[7] = 9; r[8] = 7; r[9] = 1; r[10] = 9; r[11] = 6;
                return 4;
            case 'z':
                r[5] = 15; r[6] = 2; r[7] = 4; r[8] = 8; r[9] = 15;
                return 4;

            // Uppercase A..Z + Đ
            case 'A':
                if (isUpperViet) {
                    r[4] = 6; r[5] = 9; r[6] = 9; r[7] = 15; r[8] = 9; r[9] = 9;
                } else {
                    r[3] = 6; r[4] = 9; r[5] = 9; r[6] = 15; r[7] = 9; r[8] = 9; r[9] = 9;
                }
                return 4;
            case 'B':
                r[3] = 14; r[4] = 9; r[5] = 9; r[6] = 14; r[7] = 9; r[8] = 9; r[9] = 14;
                return 4;
            case 'C':
                r[3] = 6; r[4] = 9; r[5] = 8; r[6] = 8; r[7] = 8; r[8] = 9; r[9] = 6;
                return 4;
            case 'D':
                r[3] = 14; r[4] = 9; r[5] = 9; r[6] = 9; r[7] = 9; r[8] = 9; r[9] = 14;
                return 4;
            case 'Đ':
                r[3] = 14; r[4] = 9; r[5] = 9; r[6] = 29; r[7] = 9; r[8] = 9; r[9] = 14;
                return 5;
            case 'E':
                if (isUpperViet) {
                    r[4] = 15; r[5] = 8; r[6] = 14; r[7] = 8; r[8] = 8; r[9] = 15;
                } else {
                    r[3] = 15; r[4] = 8; r[5] = 8; r[6] = 14; r[7] = 8; r[8] = 8; r[9] = 15;
                }
                return 4;
            case 'F':
                r[3] = 15; r[4] = 8; r[5] = 8; r[6] = 14; r[7] = 8; r[8] = 8; r[9] = 8;
                return 4;
            case 'G':
                r[3] = 6; r[4] = 9; r[5] = 8; r[6] = 11; r[7] = 9; r[8] = 9; r[9] = 6;
                return 4;
            case 'H':
                r[3] = 9; r[4] = 9; r[5] = 9; r[6] = 15; r[7] = 9; r[8] = 9; r[9] = 9;
                return 4;
            case 'I':
                if (isUpperViet) {
                    r[4] = 2; r[5] = 2; r[6] = 2; r[7] = 2; r[8] = 2; r[9] = 2;
                    return 3;
                }
                r[3] = 7; r[4] = 2; r[5] = 2; r[6] = 2; r[7] = 2; r[8] = 2; r[9] = 7;
                return 3;
            case 'J':
                r[3] = 7; r[4] = 1; r[5] = 1; r[6] = 1; r[7] = 9; r[8] = 9; r[9] = 6;
                return 4;
            case 'K':
                r[3] = 9; r[4] = 10; r[5] = 12; r[6] = 12; r[7] = 10; r[8] = 9; r[9] = 9;
                return 4;
            case 'L':
                r[3] = 8; r[4] = 8; r[5] = 8; r[6] = 8; r[7] = 8; r[8] = 8; r[9] = 15;
                return 4;
            case 'M':
                r[3] = 17; r[4] = 27; r[5] = 21; r[6] = 21; r[7] = 17; r[8] = 17; r[9] = 17;
                return 5;
            case 'N':
                r[3] = 9; r[4] = 13; r[5] = 15; r[6] = 11; r[7] = 9; r[8] = 9; r[9] = 9;
                return 4;
            case 'O':
                if (isUpperViet) {
                    r[4] = 6; r[5] = 9; r[6] = 9; r[7] = 9; r[8] = 9; r[9] = 6;
                } else {
                    r[3] = 6; r[4] = 9; r[5] = 9; r[6] = 9; r[7] = 9; r[8] = 9; r[9] = 6;
                }
                return 4;
            case 'P':
                r[3] = 14; r[4] = 9; r[5] = 9; r[6] = 14; r[7] = 8; r[8] = 8; r[9] = 8;
                return 4;
            case 'Q':
                r[3] = 6; r[4] = 9; r[5] = 9; r[6] = 9; r[7] = 11; r[8] = 10; r[9] = 5;
                return 4;
            case 'R':
                r[3] = 14; r[4] = 9; r[5] = 9; r[6] = 14; r[7] = 10; r[8] = 9; r[9] = 9;
                return 4;
            case 'S':
                r[3] = 7; r[4] = 8; r[5] = 8; r[6] = 6; r[7] = 1; r[8] = 1; r[9] = 14;
                return 4;
            case 'T':
                r[3] = 31; r[4] = 4; r[5] = 4; r[6] = 4; r[7] = 4; r[8] = 4; r[9] = 4;
                return 5;
            case 'U':
                if (isUpperViet) {
                    r[4] = 9; r[5] = 9; r[6] = 9; r[7] = 9; r[8] = 9; r[9] = 6;
                } else {
                    r[3] = 9; r[4] = 9; r[5] = 9; r[6] = 9; r[7] = 9; r[8] = 9; r[9] = 6;
                }
                return 4;
            case 'V':
                r[3] = 9; r[4] = 9; r[5] = 9; r[6] = 9; r[7] = 9; r[8] = 6; r[9] = 6;
                return 4;
            case 'W':
                r[3] = 17; r[4] = 17; r[5] = 17; r[6] = 21; r[7] = 21; r[8] = 27; r[9] = 17;
                return 5;
            case 'X':
                r[3] = 9; r[4] = 9; r[5] = 6; r[6] = 6; r[7] = 6; r[8] = 9; r[9] = 9;
                return 4;
            case 'Y':
                if (isUpperViet) {
                    r[4] = 9; r[5] = 9; r[6] = 6; r[7] = 2; r[8] = 2; r[9] = 2;
                    return 4;
                }
                r[3] = 17; r[4] = 17; r[5] = 10; r[6] = 4; r[7] = 4; r[8] = 4; r[9] = 4;
                return 5;
            case 'Z':
                r[3] = 15; r[4] = 1; r[5] = 2; r[6] = 4; r[7] = 8; r[8] = 8; r[9] = 15;
                return 4;
            default:
                return 3;
        }
    }
}
