package com.mg.engine.graphics;

import com.loan12suquan.hd.HdWorldMapEnhancer;
import com.loan12suquan.hd.platform.HdPlatform;
import com.loan12suquan.hd.platform.HdSurface;
import com.mg.engine.core.GameConfig;
import com.mg.engine.storage.EngineStorage;
import com.mg.engine.util.ByteUtils;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.microedition.lcdui.Image;

/**
 * Loads MicroGame {@code .mg} image resources (standard PNGs whose first 4 bytes
 * store the big-endian file size instead of {@code 0x89 'P' 'N' 'G'}), or standard {@code .png} files,
 * applies the HD Pixel Color Boost & Micro-Contrast Remaster filter in a single pass,
 * and caches immutable sprites in memory for zero-lag screen transitions.
 */
public class MgImageLoader {
    public static boolean usePngFormat = true;
    public static boolean hdColorBoostEnabled = true;
    private static final byte[] PNG_MAGIC_PREFIX = new byte[]{-119, 80, 78, 71};
    private static final Map<String, Image> IMAGE_CACHE = Collections.synchronizedMap(new HashMap<String, Image>());

    private static Image loadMgInternal(String basePath) {
        String resourcePath = basePath + GameConfig.imageExtension;
        try {
            InputStream stream = HdPlatform.backend().openResourceStream(resourcePath);
            if (stream == null) {
                return null;
            }
            byte[] header = new byte[PNG_MAGIC_PREFIX.length];
            stream.read(header, 0, header.length);
            int totalLength = ByteUtils.a(header);
            byte[] buffer = new byte[totalLength];
            EngineStorage.a(buffer, stream, PNG_MAGIC_PREFIX.length);
            stream.close();

            for (int i = 0; i < PNG_MAGIC_PREFIX.length; ++i) {
                buffer[i] = PNG_MAGIC_PREFIX[i];
            }
            Image raw = Image.createImage(buffer, 0, totalLength);
            Image result = shouldEnhance(basePath) ? applyHdPixelRemaster(raw, "/m/m".equals(basePath)) : raw;
            if (result != null) {
                result.attachResourceMetadata(basePath);
            }
            return result;
        } catch (Exception error) {
            error.printStackTrace();
            return null;
        }
    }

    private static Image loadPngInternal(String basePath) {
        String resourcePath = basePath.endsWith(".png") ? basePath : (basePath + ".png");
        try {
            if (shouldEnhance(basePath)) {
                HdSurface rawSurf = Image.decodeResource1xOnly(resourcePath);
                if (rawSurf != null) {
                    int w = rawSurf.getWidth();
                    int h = rawSurf.getHeight();
                    int total = w * h;
                    if (total > 0 && total <= 524288) {
                        int[] in = new int[total];
                        rawSurf.getArgb(0, 0, w, h, in, 0, w);
                        Image enhanced = applyHdPixelRemasterFromPixels(in, w, h, "/m/m".equals(basePath));
                        if (enhanced != null) {
                            enhanced.attachResourceMetadata(basePath);
                            return enhanced;
                        }
                    }
                }
            }
            Image raw = Image.createImage(resourcePath);
            if (raw != null) {
                raw.attachResourceMetadata(basePath);
            }
            return raw;
        } catch (Throwable error) {
            error.printStackTrace();
            return null;
        }
    }

    private static boolean shouldEnhance(String basePath) {
        if (!hdColorBoostEnabled || basePath == null) {
            return false;
        }
        // Leave bitmap font strips untouched so glyph metrics and contrast stay exact.
        return !basePath.startsWith("/_font") && !basePath.startsWith("/f/");
    }

    private static int clampDetailDelta(int delta) {
        int abs = delta < 0 ? -delta : delta;
        if (abs <= 5) {
            return 0; // Ignore flat-area / skin-tone micro-noise
        }
        if (abs > 88) {
            // Dampen high-contrast outline jumps to prevent bright halo ringing
            return delta > 0 ? 18 : -18;
        }
        return delta;
    }

    private static int applyStudioToneCurve(int v) {
        if (v <= 0) {
            return 0;
        }
        if (v < 96) {
            // Deepen rich shadows without crushing dark detail (0..96 -> 0..92)
            return (v * 92) / 96;
        }
        if (v <= 192) {
            // Lively midtone contrast lift (96..192 -> 92..200)
            return 92 + ((v - 96) * 108) / 96;
        }
        if (v >= 255) {
            return 255;
        }
        // Smooth highlight shoulder roll-off prevents harsh clipping on gold & clouds (192..255 -> 200..255)
        return 200 + ((v - 192) * 55) / 63;
    }

    /**
     * Studio Pixel-Art Remaster v2 (pure integer CLDC-1.0 compatible):
     * 1. Crisp hand-drawn ink outline preservation.
     * 2. Edge-preserving bilateral micro-sharpening (sharpens armor/gems/foliage while keeping portraits & parchment smooth).
     * 3. Piecewise S-curve tone mapping with highlight shoulder roll-off.
     * 4. Adaptive perceptual vibrance (boosts muted tones strongly without oversaturating pure reds/golds).
     */
    private static Image applyHdPixelRemaster(Image src, boolean cleanBakedMapText) {
        if (src == null) {
            return null;
        }
        try {
            int w = src.getWidth();
            int h = src.getHeight();
            int total = w * h;
            if (total <= 0 || total > 524288) {
                return src;
            }
            int[] in = new int[total];
            src.getRGB(in, 0, w, 0, 0, w, h);
            return applyHdPixelRemasterFromPixels(in, w, h, cleanBakedMapText);
        } catch (Throwable fallback) {
            return src;
        }
    }

    private static Image applyHdPixelRemasterFromPixels(int[] in, int w, int h, boolean cleanBakedMapText) {
        int total = w * h;
        if (cleanBakedMapText) {
            HdWorldMapEnhancer.inpaintBakedMapText(in, w, h);
        }
        int[] out = new int[total];

            for (int y = 0; y < h; ++y) {
                int rowOffset = y * w;
                for (int x = 0; x < w; ++x) {
                    int idx = rowOffset + x;
                    int argb = in[idx];
                    int a = (argb >>> 24) & 0xFF;
                    if (a < 20) {
                        out[idx] = (a == 0) ? 0 : argb;
                        continue;
                    }

                    int r = (argb >>> 16) & 0xFF;
                    int g = (argb >>> 8) & 0xFF;
                    int b = argb & 0xFF;

                    // 1. Preserve crisp dark pixel-art ink outlines
                    int baseLum = (r * 77 + g * 150 + b * 29) >> 8;
                    if (baseLum < 22 && a > 200) {
                        r = (r * 82) / 100;
                        g = (g * 82) / 100;
                        b = (b * 82) / 100;
                        out[idx] = (a << 24) | (r << 16) | (g << 8) | b;
                        continue;
                    }

                    // 2. Edge-preserving bilateral micro-sharpening on opaque interior pixels
                    if (a > 210 && x > 0 && x < w - 1 && y > 0 && y < h - 1) {
                        int nUp = in[idx - w];
                        int nDown = in[idx + w];
                        int nLeft = in[idx - 1];
                        int nRight = in[idx + 1];
                        if (((nUp >>> 24) & 0xFF) > 210
                                && ((nDown >>> 24) & 0xFF) > 210
                                && ((nLeft >>> 24) & 0xFF) > 210
                                && ((nRight >>> 24) & 0xFF) > 210) {
                            int dr = clampDetailDelta(r - ((nUp >>> 16) & 0xFF))
                                    + clampDetailDelta(r - ((nDown >>> 16) & 0xFF))
                                    + clampDetailDelta(r - ((nLeft >>> 16) & 0xFF))
                                    + clampDetailDelta(r - ((nRight >>> 16) & 0xFF));
                            int dg = clampDetailDelta(g - ((nUp >>> 8) & 0xFF))
                                    + clampDetailDelta(g - ((nDown >>> 8) & 0xFF))
                                    + clampDetailDelta(g - ((nLeft >>> 8) & 0xFF))
                                    + clampDetailDelta(g - ((nRight >>> 8) & 0xFF));
                            int db = clampDetailDelta(b - (nUp & 0xFF))
                                    + clampDetailDelta(b - (nDown & 0xFF))
                                    + clampDetailDelta(b - (nLeft & 0xFF))
                                    + clampDetailDelta(b - (nRight & 0xFF));
                            r += (dr * 8) / 100;
                            g += (dg * 8) / 100;
                            b += (db * 8) / 100;
                            if (r < 0) r = 0; else if (r > 255) r = 255;
                            if (g < 0) g = 0; else if (g > 255) g = 255;
                            if (b < 0) b = 0; else if (b > 255) b = 255;
                        }
                    }

                    // 3. Piecewise S-curve tone mapping with highlight roll-off
                    r = applyStudioToneCurve(r);
                    g = applyStudioToneCurve(g);
                    b = applyStudioToneCurve(b);

                    // 4. Adaptive perceptual vibrance (boosts muted colors more, saturated colors gently)
                    int maxC = r > g ? (r > b ? r : b) : (g > b ? g : b);
                    int minC = r < g ? (r < b ? r : b) : (g < b ? g : b);
                    int chroma = maxC - minC;
                    int vibPct = chroma < 64 ? 132 : (chroma < 140 ? 122 : 110);
                    int lum = (r * 77 + g * 150 + b * 29) >> 8;
                    r = lum + ((r - lum) * vibPct) / 100;
                    g = lum + ((g - lum) * vibPct) / 100;
                    b = lum + ((b - lum) * vibPct) / 100;

                    if (r < 0) r = 0; else if (r > 255) r = 255;
                    if (g < 0) g = 0; else if (g > 255) g = 255;
                    if (b < 0) b = 0; else if (b > 255) b = 255;

                    out[idx] = (a << 24) | (r << 16) | (g << 8) | b;
                }
            }
        return Image.createRGBImage(out, w, h, true);
    }

    /** Loads via {@link #load(String)} (uses {@code .png} when {@link #usePngFormat} is true, else {@code .mg}). */
    public static Image loadMg(String basePath) {
        return load(basePath);
    }

    /** Loads from {@code .png} when {@link #usePngFormat} is true, otherwise from {@code .mg}, with session caching. */
    public static Image load(String basePath) {
        if (basePath == null || basePath.length() == 0) {
            return null;
        }
        Image cached = IMAGE_CACHE.get(basePath);
        if (cached != null) {
            return cached;
        }
        Image loaded = usePngFormat ? loadPngInternal(basePath) : loadMgInternal(basePath);
        if (loaded != null && !loaded.isMutableSurface()) {
            IMAGE_CACHE.put(basePath, loaded);
        }
        return loaded;
    }
}
