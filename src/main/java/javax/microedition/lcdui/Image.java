package javax.microedition.lcdui;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdGemGenerator;
import com.loan12suquan.hd.HdSpriteUpscaler;
import com.loan12suquan.hd.platform.HdPlatform;
import com.loan12suquan.hd.platform.HdSurface;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.lcdui.game.Sprite;

/**
 * High-Definition cross-platform implementation of MIDP {@link Image} backed by {@link HdSurface}
 * (Java2D {@code BufferedImage} on Desktop, HTML5 {@code <canvas>} on Web / iPhone 17 PWA).
 */
public class Image {
    private final int logicalWidth;
    private final int logicalHeight;
    private final boolean mutableSurface;
    private final HdSurface raw1xBuffer;
    private HdSurface hd3xBuffer;
    private HdSurface nearest3xBuffer;
    private HdSurface hdRemasterOverride;
    private String sourcePath;

    private Image(int logicalWidth, int logicalHeight, boolean mutableSurface,
                  HdSurface raw1x, HdSurface hd3x) {
        this.logicalWidth = Math.max(1, logicalWidth);
        this.logicalHeight = Math.max(1, logicalHeight);
        this.mutableSurface = mutableSurface;
        this.raw1xBuffer = raw1x;
        this.hd3xBuffer = hd3x;
    }

    public static Image createImage(int width, int height) {
        int s = HdConfig.HD_SCALE;
        HdSurface hdSurface = HdPlatform.backend().createSurface(
                Math.max(1, width * s),
                Math.max(1, height * s),
                true);
        return new Image(width, height, true, null, hdSurface);
    }

    public static Image createImage(byte[] imageData, int imageOffset, int imageLength) {
        HdSurface decoded = HdPlatform.backend().decodeImageBytes(imageData, imageOffset, imageLength);
        return fromDecoded1x(decoded);
    }

    public static Image createImage(InputStream stream) throws IOException {
        if (stream == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
        byte[] chunk = new byte[2048];
        int n;
        while ((n = stream.read(chunk)) != -1) {
            baos.write(chunk, 0, n);
        }
        byte[] all = baos.toByteArray();
        return createImage(all, 0, all.length);
    }

    public static Image createImage(String name) throws IOException {
        InputStream stream = HdPlatform.backend().openResourceStream(name);
        if (stream == null) {
            return null;
        }
        try {
            return createImage(stream);
        } finally {
            stream.close();
        }
    }

    /**
     * Decodes only the raw {@code 1x} {@link HdSurface} from {@code resourcePath} without running
     * {@code upscale3x}, allowing {@code MgImageLoader} to remaster {@code 1x} pixels first and
     * run {@code upscale3x} only once.
     */
    public static HdSurface decodeResource1xOnly(String resourcePath) throws IOException {
        InputStream stream = HdPlatform.backend().openResourceStream(resourcePath);
        if (stream == null) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
            byte[] chunk = new byte[2048];
            int n;
            while ((n = stream.read(chunk)) != -1) {
                baos.write(chunk, 0, n);
            }
            byte[] all = baos.toByteArray();
            return HdPlatform.backend().decodeImageBytes(all, 0, all.length);
        } finally {
            stream.close();
        }
    }

    public static Image createRGBImage(int[] rgb, int width, int height, boolean processAlpha) {
        int total = width * height;
        int[] copy = new int[total];
        if (!processAlpha) {
            for (int i = 0; i < total; ++i) {
                copy[i] = rgb[i] | 0xFF000000;
            }
        } else {
            System.arraycopy(rgb, 0, copy, 0, total);
        }
        HdSurface raw1x = HdPlatform.backend().createSurfaceFromArgb(copy, width, height);
        HdSurface hd3x = HdSpriteUpscaler.upscale3x(copy, width, height);
        return new Image(width, height, false, raw1x, hd3x);
    }

    public static Image createImage(Image source, int x, int y, int width, int height, int transform) {
        if (source == null) {
            return null;
        }
        int s = HdConfig.HD_SCALE;
        boolean swapAxes = (transform == Sprite.TRANS_ROT90
                || transform == Sprite.TRANS_ROT270
                || transform == Sprite.TRANS_MIRROR_ROT90
                || transform == Sprite.TRANS_MIRROR_ROT270);
        int outLogicalW = swapAxes ? height : width;
        int outLogicalH = swapAxes ? width : height;

        HdSurface src1x = source.getRaw1xSurface();
        HdSurface sub1x = cropAndTransform(src1x, x, y, width, height, transform);

        HdSurface src3x = source.getHd3xSurface();
        HdSurface sub3x = cropAndTransform(src3x, x * s, y * s, width * s, height * s, transform);

        return new Image(outLogicalW, outLogicalH, false, sub1x, sub3x);
    }

    private static Image fromDecoded1x(HdSurface decoded) {
        if (decoded == null) {
            return null;
        }
        int w = decoded.getWidth();
        int h = decoded.getHeight();
        int[] pixels = new int[w * h];
        decoded.getArgb(0, 0, w, h, pixels, 0, w);
        HdSurface hd3x = HdSpriteUpscaler.upscale3x(pixels, w, h);
        return new Image(w, h, false, decoded, hd3x);
    }

    public void attachResourceMetadata(String basePath) {
        this.sourcePath = basePath;
        if (HdGemGenerator.isBoardGemPath(basePath) && this.hd3xBuffer != null) {
            this.hdRemasterOverride = HdGemGenerator.createHdGem(basePath, this.hd3xBuffer);
        }
    }

    public String getSourcePath() {
        return this.sourcePath;
    }

    public int getWidth() {
        return this.logicalWidth;
    }

    public int getHeight() {
        return this.logicalHeight;
    }

    public boolean isMutableSurface() {
        return this.mutableSurface;
    }

    public Graphics getGraphics() {
        return new Graphics(this.hd3xBuffer, this.logicalWidth, this.logicalHeight);
    }

    public void getRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height) {
        if (this.raw1xBuffer != null) {
            this.raw1xBuffer.getArgb(x, y, width, height, rgbData, offset, scanlength);
        } else if (this.hd3xBuffer != null) {
            int s = HdConfig.HD_SCALE;
            for (int ry = 0; ry < height; ++ry) {
                int hy = Math.min(this.hd3xBuffer.getHeight() - 1, (y + ry) * s + 1);
                for (int rx = 0; rx < width; ++rx) {
                    int hx = Math.min(this.hd3xBuffer.getWidth() - 1, (x + rx) * s + 1);
                    rgbData[offset + ry * scanlength + rx] = this.hd3xBuffer.getArgb(hx, hy);
                }
            }
        }
    }

    public HdSurface getRaw1xSurface() {
        return this.raw1xBuffer != null ? this.raw1xBuffer : this.hd3xBuffer;
    }

    public HdSurface getHd3xSurface() {
        return this.hd3xBuffer;
    }

    public HdSurface getActiveHdSurface() {
        if (this.mutableSurface) {
            return this.hd3xBuffer;
        }
        int mode = HdConfig.visualMode;
        if (mode == HdConfig.MODE_HD_REMASTER) {
            return this.hdRemasterOverride != null ? this.hdRemasterOverride : this.hd3xBuffer;
        }
        if (mode == HdConfig.MODE_HD_PIXEL) {
            return this.hd3xBuffer;
        }
        if (this.nearest3xBuffer == null && this.raw1xBuffer != null) {
            int w = this.raw1xBuffer.getWidth();
            int h = this.raw1xBuffer.getHeight();
            int[] px = new int[w * h];
            this.raw1xBuffer.getArgb(0, 0, w, h, px, 0, w);
            this.nearest3xBuffer = HdSpriteUpscaler.upscaleNearest3x(px, w, h);
        }
        return this.nearest3xBuffer != null ? this.nearest3xBuffer : this.hd3xBuffer;
    }

    public static HdSurface cropAndTransform(HdSurface src, int x, int y, int w, int h, int transform) {
        if (src == null || w <= 0 || h <= 0) {
            return HdPlatform.backend().createSurface(1, 1, false);
        }
        int srcW = src.getWidth();
        int srcH = src.getHeight();

        boolean swapAxes = (transform == Sprite.TRANS_ROT90
                || transform == Sprite.TRANS_ROT270
                || transform == Sprite.TRANS_MIRROR_ROT90
                || transform == Sprite.TRANS_MIRROR_ROT270);
        int outW = swapAxes ? h : w;
        int outH = swapAxes ? w : h;

        int clampX = Math.max(0, x);
        int clampY = Math.max(0, y);
        int clampW = Math.min(w - (clampX - x), srcW - clampX);
        int clampH = Math.min(h - (clampY - y), srcH - clampY);
        if (clampW <= 0 || clampH <= 0) {
            return HdPlatform.backend().createSurface(outW, outH, false);
        }

        int[] srcPixels = new int[w * h];
        src.getArgb(clampX, clampY, clampW, clampH, srcPixels, (clampY - y) * w + (clampX - x), w);

        int[] dstPixels = new int[outW * outH];
        for (int sy = 0; sy < h; ++sy) {
            int rowOff = sy * w;
            for (int sx = 0; sx < w; ++sx) {
                int argb = srcPixels[rowOff + sx];
                int dx;
                int dy;
                switch (transform) {
                    case Sprite.TRANS_MIRROR_ROT180:
                        dx = sx;
                        dy = h - 1 - sy;
                        break;
                    case Sprite.TRANS_MIRROR:
                        dx = w - 1 - sx;
                        dy = sy;
                        break;
                    case Sprite.TRANS_ROT180:
                        dx = w - 1 - sx;
                        dy = h - 1 - sy;
                        break;
                    case Sprite.TRANS_MIRROR_ROT270:
                        dx = sy;
                        dy = sx;
                        break;
                    case Sprite.TRANS_ROT90:
                        dx = h - 1 - sy;
                        dy = sx;
                        break;
                    case Sprite.TRANS_ROT270:
                        dx = sy;
                        dy = w - 1 - sx;
                        break;
                    case Sprite.TRANS_MIRROR_ROT90:
                        dx = h - 1 - sy;
                        dy = w - 1 - sx;
                        break;
                    default:
                        dx = sx;
                        dy = sy;
                        break;
                }
                dstPixels[dy * outW + dx] = argb;
            }
        }
        return HdPlatform.backend().createSurfaceFromArgb(dstPixels, outW, outH);
    }
}
