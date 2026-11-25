package javax.microedition.lcdui;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.platform.HdCanvas2D;
import com.loan12suquan.hd.platform.HdSurface;
import com.mg.engine.graphics.VietnamesePixelFont;
import javax.microedition.lcdui.game.Sprite;

/**
 * High-Definition {@code 3x} ({@code 720x960}) cross-platform implementation of MIDP {@link Graphics}
 * backed by {@link HdCanvas2D} (Desktop Java2D and Web/iPhone 17 HTML5 Canvas2D).
 */
public class Graphics {
    public static final int HCENTER = 1;
    public static final int VCENTER = 2;
    public static final int LEFT = 4;
    public static final int RIGHT = 8;
    public static final int TOP = 16;
    public static final int BOTTOM = 32;
    public static final int BASELINE = 64;

    public static final int SOLID = 0;
    public static final int DOTTED = 1;

    private final HdSurface targetHdSurface;
    private final HdCanvas2D g2;
    private final int surfaceLogicalW;
    private final int surfaceLogicalH;

    private int translateX = 0;
    private int translateY = 0;
    private int clipX = 0;
    private int clipY = 0;
    private int clipW;
    private int clipH;
    private int currentColor = 0x000000;
    private int strokeStyle = SOLID;
    private Font currentFont = Font.getDefaultFont();

    public Graphics(HdSurface targetHdSurface, int surfaceLogicalW, int surfaceLogicalH) {
        this.targetHdSurface = targetHdSurface;
        this.surfaceLogicalW = surfaceLogicalW;
        this.surfaceLogicalH = surfaceLogicalH;
        this.clipW = surfaceLogicalW;
        this.clipH = surfaceLogicalH;
        this.g2 = targetHdSurface.getCanvas2D();
        this.g2.setStrokeWidth(HdConfig.HD_SCALE);
    }

    public HdCanvas2D getHdCanvas() {
        return this.g2;
    }

    public HdSurface getTargetHdSurface() {
        return this.targetHdSurface;
    }

    private void syncMidpState() {
        int s = HdConfig.HD_SCALE;
        this.g2.applyMidpTransformAndClip(
                this.translateX * s,
                this.translateY * s,
                this.clipX * s,
                this.clipY * s,
                this.clipW * s,
                this.clipH * s);
    }

    public void translate(int x, int y) {
        this.translateX += x;
        this.translateY += y;
        syncMidpState();
    }

    public int getTranslateX() {
        return this.translateX;
    }

    public int getTranslateY() {
        return this.translateY;
    }

    public int getColor() {
        return this.currentColor;
    }

    public void setColor(int rgb) {
        this.currentColor = rgb & 0x00FFFFFF;
        this.g2.setColor(this.currentColor);
    }

    public void setColor(int red, int green, int blue) {
        setColor(((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF));
    }

    public Font getFont() {
        return this.currentFont;
    }

    public void setFont(Font font) {
        this.currentFont = (font != null) ? font : Font.getDefaultFont();
    }

    public int getStrokeStyle() {
        return this.strokeStyle;
    }

    public void setStrokeStyle(int style) {
        this.strokeStyle = style;
    }

    public int getClipX() {
        return this.clipX;
    }

    public int getClipY() {
        return this.clipY;
    }

    public int getClipWidth() {
        return this.clipW;
    }

    public int getClipHeight() {
        return this.clipH;
    }

    public void setClip(int x, int y, int width, int height) {
        this.clipX = x;
        this.clipY = y;
        this.clipW = width;
        this.clipH = height;
        syncMidpState();
    }

    public void clipRect(int x, int y, int width, int height) {
        int left = Math.max(this.clipX, x);
        int top = Math.max(this.clipY, y);
        int right = Math.min(this.clipX + this.clipW, x + width);
        int bottom = Math.min(this.clipY + this.clipH, y + height);
        setClip(left, top, right - left, bottom - top);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        int s = HdConfig.HD_SCALE;
        this.g2.setColor(this.currentColor);
        if (x1 == x2 && y1 == y2) {
            this.g2.fillRect(x1 * s, y1 * s, s, s);
        } else if (y1 == y2) {
            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            this.g2.fillRect(minX * s, y1 * s, (maxX - minX + 1) * s, s);
        } else if (x1 == x2) {
            int minY = Math.min(y1, y2);
            int maxY = Math.max(y1, y2);
            this.g2.fillRect(x1 * s, minY * s, s, (maxY - minY + 1) * s);
        } else {
            this.g2.setStrokeRound(s);
            this.g2.drawLine(x1 * s + 1, y1 * s + 1, x2 * s + 1, y2 * s + 1);
        }
    }

    public void fillRect(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        int s = HdConfig.HD_SCALE;
        this.g2.setColor(this.currentColor);
        this.g2.fillRect(x * s, y * s, width * s, height * s);
    }

    public void drawRect(int x, int y, int width, int height) {
        if (width < 0 || height < 0) {
            return;
        }
        int s = HdConfig.HD_SCALE;
        int hx = x * s;
        int hy = y * s;
        int hw = (width + 1) * s;
        int hh = (height + 1) * s;
        this.g2.setColor(this.currentColor);
        this.g2.fillRect(hx, hy, hw, s);
        this.g2.fillRect(hx, hy + hh - s, hw, s);
        this.g2.fillRect(hx, hy, s, hh);
        this.g2.fillRect(hx + hw - s, hy, s, hh);
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        if (width <= 0 || height <= 0) {
            return;
        }
        int s = HdConfig.HD_SCALE;
        this.g2.setColor(this.currentColor);
        this.g2.fillRoundRect(x * s, y * s, width * s, height * s, arcWidth * s);
    }

    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        if (width < 0 || height < 0) {
            return;
        }
        int s = HdConfig.HD_SCALE;
        this.g2.setColor(this.currentColor);
        this.g2.setStrokeWidth(s);
        this.g2.drawRoundRect(x * s + 1, y * s + 1, width * s, height * s, arcWidth * s);
    }

    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        if (width <= 0 || height <= 0) {
            return;
        }
        int s = HdConfig.HD_SCALE;
        this.g2.setColor(this.currentColor);
        this.g2.fillArc(x * s, y * s, width * s, height * s, startAngle, arcAngle);
    }

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        if (width <= 0 || height <= 0) {
            return;
        }
        int s = HdConfig.HD_SCALE;
        this.g2.setColor(this.currentColor);
        this.g2.setStrokeRound(s * 0.9f);
        this.g2.drawArc(x * s, y * s, width * s, height * s, startAngle, arcAngle);
    }

    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        int s = HdConfig.HD_SCALE;
        int[] xs = new int[]{x1 * s + 1, x2 * s + 1, x3 * s + 1};
        int[] ys = new int[]{y1 * s + 1, y2 * s + 1, y3 * s + 1};
        this.g2.setColor(this.currentColor);
        this.g2.fillPolygon(xs, ys, 3);
    }

    public void drawString(String str, int x, int y, int anchor) {
        if (str == null || str.isEmpty()) {
            return;
        }
        int resolvedAnchor = (anchor == 0) ? (TOP | LEFT) : anchor;
        VietnamesePixelFont vf = (this.currentFont != null && this.currentFont.isBold())
                ? VietnamesePixelFont.BOLD
                : VietnamesePixelFont.PLAIN;
        vf.drawShadowString(this, str, x, y, resolvedAnchor, this.currentColor, 0x100603);
    }

    public void drawSubstring(String str, int offset, int len, int x, int y, int anchor) {
        if (str != null && len > 0) {
            drawString(str.substring(offset, offset + len), x, y, anchor);
        }
    }

    public void drawChar(char character, int x, int y, int anchor) {
        drawString(String.valueOf(character), x, y, anchor);
    }

    public void drawImage(Image img, int x, int y, int anchor) {
        if (img == null) {
            return;
        }
        int w = img.getWidth();
        int h = img.getHeight();
        int dx = x;
        int dy = y;
        if ((anchor & HCENTER) != 0) {
            dx -= w >> 1;
        } else if ((anchor & RIGHT) != 0) {
            dx -= w;
        }
        if ((anchor & VCENTER) != 0) {
            dy -= h >> 1;
        } else if ((anchor & BOTTOM) != 0) {
            dy -= h;
        }

        int s = HdConfig.HD_SCALE;
        HdSurface hdImg = img.getActiveHdSurface();
        if (hdImg != null) {
            this.g2.drawSurface(hdImg, dx * s, dy * s, w * s, h * s);
        }
    }

    public void drawRegion(Image src, int x_src, int y_src, int width, int height,
                           int transform, int x_dest, int y_dest, int anchor) {
        if (src == null || width <= 0 || height <= 0) {
            return;
        }
        boolean swapAxes = (transform == Sprite.TRANS_ROT90
                || transform == Sprite.TRANS_ROT270
                || transform == Sprite.TRANS_MIRROR_ROT90
                || transform == Sprite.TRANS_MIRROR_ROT270);
        int destW = swapAxes ? height : width;
        int destH = swapAxes ? width : height;

        int dx = x_dest;
        int dy = y_dest;
        if ((anchor & HCENTER) != 0) {
            dx -= destW >> 1;
        } else if ((anchor & RIGHT) != 0) {
            dx -= destW;
        }
        if ((anchor & VCENTER) != 0) {
            dy -= destH >> 1;
        } else if ((anchor & BOTTOM) != 0) {
            dy -= destH;
        }

        int s = HdConfig.HD_SCALE;
        HdSurface hdSrc = src.getActiveHdSurface();
        if (hdSrc == null) {
            return;
        }

        int sx1 = x_src * s;
        int sy1 = y_src * s;
        int sw = width * s;
        int sh = height * s;
        int hdx = dx * s;
        int hdy = dy * s;
        int hdw = destW * s;
        int hdh = destH * s;

        this.g2.drawSurfaceRegionTransformed(hdSrc, sx1, sy1, sw, sh, hdx, hdy, hdw, hdh, transform);
    }

    public void drawRGB(int[] rgbData, int offset, int scanlength, int x, int y,
                        int width, int height, boolean processAlpha) {
        if (rgbData == null || width <= 0 || height <= 0) {
            return;
        }
        int[] packed = new int[width * height];
        for (int row = 0; row < height; ++row) {
            System.arraycopy(rgbData, offset + row * scanlength, packed, row * width, width);
        }
        Image temp = Image.createRGBImage(packed, width, height, processAlpha);
        drawImage(temp, x, y, TOP | LEFT);
    }
}
