package javax.microedition.lcdui;

import com.mg.engine.graphics.VietnamesePixelFont;

/**
 * MIDP {@link Font} bridge wired to our Vietnamese pixel/HD font metrics.
 */
public final class Font {
    public static final int STYLE_PLAIN = 0;
    public static final int STYLE_BOLD = 1;
    public static final int STYLE_ITALIC = 2;
    public static final int STYLE_UNDERLINED = 4;

    public static final int SIZE_SMALL = 8;
    public static final int SIZE_MEDIUM = 0;
    public static final int SIZE_LARGE = 16;

    public static final int FACE_SYSTEM = 0;
    public static final int FACE_MONOSPACE = 32;
    public static final int FACE_PROPORTIONAL = 64;

    private static final Font DEFAULT_PLAIN = new Font(FACE_PROPORTIONAL, STYLE_PLAIN, SIZE_SMALL);
    private static final Font DEFAULT_BOLD = new Font(FACE_PROPORTIONAL, STYLE_BOLD, SIZE_SMALL);

    private final int face;
    private final int style;
    private final int size;

    private Font(int face, int style, int size) {
        this.face = face;
        this.style = style;
        this.size = size;
    }

    public static Font getDefaultFont() {
        return DEFAULT_PLAIN;
    }

    public static Font getFont(int face, int style, int size) {
        if ((style & STYLE_BOLD) != 0) {
            return DEFAULT_BOLD;
        }
        return DEFAULT_PLAIN;
    }

    public boolean isBold() {
        return (this.style & STYLE_BOLD) != 0;
    }

    public int getFace() {
        return this.face;
    }

    public int getStyle() {
        return this.style;
    }

    public int getSize() {
        return this.size;
    }

    public int getHeight() {
        return isBold() ? VietnamesePixelFont.BOLD.getHeight() : VietnamesePixelFont.PLAIN.getHeight();
    }

    public int stringWidth(String str) {
        if (str == null) {
            return 0;
        }
        return isBold() ? VietnamesePixelFont.BOLD.stringWidth(str) : VietnamesePixelFont.PLAIN.stringWidth(str);
    }

    public int substringWidth(String str, int offset, int len) {
        if (str == null || len <= 0) {
            return 0;
        }
        return stringWidth(str.substring(offset, offset + len));
    }

    public int charWidth(char ch) {
        return isBold() ? VietnamesePixelFont.BOLD.charWidth(ch) : VietnamesePixelFont.PLAIN.charWidth(ch);
    }
}
