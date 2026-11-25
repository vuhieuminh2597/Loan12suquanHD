package com.mg.engine.graphics;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Real-time integer CLDC-1.0 pixel-art 2x upscaler ({@code 240x320 -> 480x640}).
 * Combines edge-directed EPX / Scale2x diagonal reconstruction with fast 75/25 anti-aliased
 * sub-pixel bridging so portraits, weapons, map terrain, and Vietnamese pixel fonts render
 * razor-sharp without bilinear blur or staircase jaggies.
 */
public final class PixelArtUpscaler {
    private static int cachedSrcW;
    private static int cachedSrcH;
    private static int[] srcPixels;
    private static int[] dstPixels;
    private static boolean allocationFailed = false;

    private PixelArtUpscaler() {
    }

    private static boolean ensureBuffers(int srcW, int srcH) {
        if (allocationFailed || srcW <= 0 || srcH <= 0) {
            return false;
        }
        if (srcPixels != null && dstPixels != null && cachedSrcW == srcW && cachedSrcH == srcH) {
            return true;
        }
        try {
            srcPixels = new int[srcW * srcH];
            dstPixels = new int[(srcW << 1) * (srcH << 1)];
            cachedSrcW = srcW;
            cachedSrcH = srcH;
            return true;
        } catch (OutOfMemoryError oom) {
            srcPixels = null;
            dstPixels = null;
            allocationFailed = true;
            return false;
        }
    }

    private static boolean isSimilar(int c1, int c2) {
        if (c1 == c2) {
            return true;
        }
        int dr = ((c1 >> 16) & 0xFF) - ((c2 >> 16) & 0xFF);
        if (dr < -28 || dr > 28) {
            return false;
        }
        int dg = ((c1 >> 8) & 0xFF) - ((c2 >> 8) & 0xFF);
        if (dg < -28 || dg > 28) {
            return false;
        }
        int db = (c1 & 0xFF) - (c2 & 0xFF);
        return db >= -28 && db <= 28;
    }

    /** Fast 75% edgeColor + 25% centerColor blend using parallel 24-bit integer bit-shifts. */
    private static int blendEdge75(int edgeA, int edgeB, int center) {
        int edgeAvg = (edgeA == edgeB) ? edgeA : (((edgeA & 0xFEFEFE) + (edgeB & 0xFEFEFE)) >> 1);
        int mid = ((edgeAvg & 0xFEFEFE) + (center & 0xFEFEFE)) >> 1;
        return 0xFF000000 | (((edgeAvg & 0xFEFEFE) + (mid & 0xFEFEFE)) >> 1);
    }

    /**
     * Presents {@code srcImage} ({@code 240x320}) scaled {@code 2x} ({@code 480x640}) onto {@code screenGraphics}.
     * Returns {@code true} if 2x presentation succeeded, or {@code false} if fallback 1x drawImage should be used.
     */
    public static boolean present2x(Image srcImage, Graphics screenGraphics, int drawX, int drawY, boolean smartScale2x) {
        if (srcImage == null || screenGraphics == null) {
            return false;
        }
        int w = srcImage.getWidth();
        int h = srcImage.getHeight();
        if (!ensureBuffers(w, h)) {
            return false;
        }

        int[] src = srcPixels;
        int[] dst = dstPixels;
        int dstW = w << 1;
        int dstH = h << 1;
        srcImage.getRGB(src, 0, w, 0, 0, w, h);

        if (!smartScale2x) {
            // Crisp 2x nearest-neighbor integer upscale
            for (int y = 0; y < h; ++y) {
                int srcRow = y * w;
                int dstRow0 = (y << 1) * dstW;
                int dstRow1 = dstRow0 + dstW;
                for (int x = 0; x < w; ++x) {
                    int p = src[srcRow + x] | 0xFF000000;
                    int dx = x << 1;
                    dst[dstRow0 + dx] = p;
                    dst[dstRow0 + dx + 1] = p;
                    dst[dstRow1 + dx] = p;
                    dst[dstRow1 + dx + 1] = p;
                }
            }
            screenGraphics.drawRGB(dst, 0, dstW, drawX, drawY, dstW, dstH, false);
            return true;
        }

        // Smart EPX / Scale2x with 75/25 diagonal anti-aliasing
        int lastX = w - 1;
        int lastY = h - 1;
        for (int y = 0; y < h; ++y) {
            int srcRow = y * w;
            int upRow = (y > 0 ? (y - 1) : 0) * w;
            int downRow = (y < lastY ? (y + 1) : lastY) * w;
            int dstRow0 = (y << 1) * dstW;
            int dstRow1 = dstRow0 + dstW;

            for (int x = 0; x < w; ++x) {
                int p = src[srcRow + x] | 0xFF000000;
                int a = src[upRow + x] | 0xFF000000;
                int d = src[downRow + x] | 0xFF000000;
                int c = src[srcRow + (x > 0 ? x - 1 : 0)] | 0xFF000000;
                int b = src[srcRow + (x < lastX ? x + 1 : lastX)] | 0xFF000000;

                int dx = x << 1;
                if (isSimilar(c, b) || isSimilar(a, d)) {
                    // Fast path for flat fills & uniform vertical/horizontal spans
                    dst[dstRow0 + dx] = p;
                    dst[dstRow0 + dx + 1] = p;
                    dst[dstRow1 + dx] = p;
                    dst[dstRow1 + dx + 1] = p;
                } else {
                    dst[dstRow0 + dx] = isSimilar(a, c) ? blendEdge75(a, c, p) : p;
                    dst[dstRow0 + dx + 1] = isSimilar(a, b) ? blendEdge75(a, b, p) : p;
                    dst[dstRow1 + dx] = isSimilar(c, d) ? blendEdge75(c, d, p) : p;
                    dst[dstRow1 + dx + 1] = isSimilar(b, d) ? blendEdge75(b, d, p) : p;
                }
            }
        }

        screenGraphics.drawRGB(dst, 0, dstW, drawX, drawY, dstW, dstH, false);
        return true;
    }
}
