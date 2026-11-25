package com.loan12suquan.hd;

import com.loan12suquan.hd.platform.HdPlatform;
import com.loan12suquan.hd.platform.HdSurface;

/**
 * High-Definition 3x Edge-Directed xBRZ + Bilateral Painterly Smoothing Upscaler
 * ({@code w x h -> (w*3) x (h*3)}).
 * <p>
 * Pass 1 reconstructs diagonal edges using a 3x3 Scale3x/xBRZ neighborhood kernel.
 * Pass 2 applies an edge-preserving bilateral anti-aliasing pass on the 3x grid so
 * portraits, relics, shop items, and terrain look smoothly painted without 3x3 staircasing,
 * while keeping dark ink outlines crisp.
 */
public final class HdSpriteUpscaler {

    private HdSpriteUpscaler() {
    }

    private static boolean similar(int c1, int c2) {
        if (c1 == c2) {
            return true;
        }
        int a1 = (c1 >>> 24) & 0xFF;
        int a2 = (c2 >>> 24) & 0xFF;
        if (a1 < 24 && a2 < 24) {
            return true;
        }
        if ((a1 < 128) != (a2 < 128)) {
            return false;
        }
        int dr = ((c1 >>> 16) & 0xFF) - ((c2 >>> 16) & 0xFF);
        if (dr < -42 || dr > 42) {
            return false;
        }
        int dg = ((c1 >>> 8) & 0xFF) - ((c2 >>> 8) & 0xFF);
        if (dg < -42 || dg > 42) {
            return false;
        }
        int db = (c1 & 0xFF) - (c2 & 0xFF);
        return db >= -42 && db <= 42;
    }

    private static int blend(int c1, int c2, int w1, int w2) {
        if (c1 == c2) {
            return c1;
        }
        int a1 = (c1 >>> 24) & 0xFF;
        int a2 = (c2 >>> 24) & 0xFF;
        int totalW = w1 + w2;
        if (a1 < 16 && a2 < 16) {
            return 0;
        }
        if (a1 < 16) {
            int outA = (a2 * w2) / totalW;
            return (outA << 24) | (c2 & 0x00FFFFFF);
        }
        if (a2 < 16) {
            int outA = (a1 * w1) / totalW;
            return (outA << 24) | (c1 & 0x00FFFFFF);
        }
        int a = (a1 * w1 + a2 * w2) / totalW;
        int r = (((c1 >>> 16) & 0xFF) * w1 + ((c2 >>> 16) & 0xFF) * w2) / totalW;
        int g = (((c1 >>> 8) & 0xFF) * w1 + ((c2 >>> 8) & 0xFF) * w2) / totalW;
        int b = ((c1 & 0xFF) * w1 + (c2 & 0xFF) * w2) / totalW;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int blendCorner(int edgeA, int edgeB, int center) {
        int edgeAvg = blend(edgeA, edgeB, 1, 1);
        return blend(edgeAvg, center, 4, 1);
    }

    /**
     * Upscales a 1x ARGB pixel array ({@code w x h}) by {@code 3x} into a {@code (w*3) x (h*3)} {@link BufferedImage}
     * with xBRZ diagonal reconstruction + bilateral contour smoothing.
     */
    public static HdSurface upscale3x(int[] src, int w, int h) {
        int dstW = w * 3;
        int dstH = h * 3;
        int[] pass1 = new int[dstW * dstH];

        int maxX = w - 1;
        int maxY = h - 1;

        for (int y = 0; y < h; ++y) {
            int ym1 = (y > 0 ? y - 1 : 0) * w;
            int y0 = y * w;
            int yp1 = (y < maxY ? y + 1 : maxY) * w;

            int dstRow0 = (y * 3) * dstW;
            int dstRow1 = dstRow0 + dstW;
            int dstRow2 = dstRow1 + dstW;

            for (int x = 0; x < w; ++x) {
                int xm1 = x > 0 ? x - 1 : 0;
                int xp1 = x < maxX ? x + 1 : maxX;

                int A = src[ym1 + xm1];
                int B = src[ym1 + x];
                int C = src[ym1 + xp1];
                int D = src[y0 + xm1];
                int E = src[y0 + x];
                int F = src[y0 + xp1];
                int G = src[yp1 + xm1];
                int H = src[yp1 + x];
                int I = src[yp1 + xp1];

                int E0 = E, E1 = E, E2 = E;
                int E3 = E, E4 = E, E5 = E;
                int E6 = E, E7 = E, E8 = E;

                if (!similar(B, H) && !similar(D, F)) {
                    boolean db = similar(D, B);
                    boolean bf = similar(B, F);
                    boolean dh = similar(D, H);
                    boolean hf = similar(H, F);

                    if (db) E0 = blendCorner(D, B, E);
                    if (bf) E2 = blendCorner(B, F, E);
                    if (dh) E6 = blendCorner(D, H, E);
                    if (hf) E8 = blendCorner(H, F, E);

                    if ((db && !similar(E, C)) || (bf && !similar(E, A))) {
                        E1 = blend(E0, E2, 1, 1);
                    }
                    if ((db && !similar(E, G)) || (dh && !similar(E, A))) {
                        E3 = blend(E0, E6, 1, 1);
                    }
                    if ((bf && !similar(E, I)) || (hf && !similar(E, C))) {
                        E5 = blend(E2, E8, 1, 1);
                    }
                    if ((dh && !similar(E, I)) || (hf && !similar(E, G))) {
                        E7 = blend(E6, E8, 1, 1);
                    }
                } else {
                    // Gentle gradient interpolation across smooth tonal ramps (portraits, sky, grass)
                    if (colorDist(D, E) < 68 && colorDist(D, E) > 0) {
                        E3 = blend(E, D, 3, 1);
                        E0 = blend(E3, B, 4, colorDist(B, E) < 68 ? 1 : 0);
                        E6 = blend(E3, H, 4, colorDist(H, E) < 68 ? 1 : 0);
                    }
                    if (colorDist(F, E) < 68 && colorDist(F, E) > 0) {
                        E5 = blend(E, F, 3, 1);
                        E2 = blend(E5, B, 4, colorDist(B, E) < 68 ? 1 : 0);
                        E8 = blend(E5, H, 4, colorDist(H, E) < 68 ? 1 : 0);
                    }
                    if (colorDist(B, E) < 68 && colorDist(B, E) > 0) {
                        E1 = blend(E, B, 3, 1);
                    }
                    if (colorDist(H, E) < 68 && colorDist(H, E) > 0) {
                        E7 = blend(E, H, 3, 1);
                    }
                }

                int dx = x * 3;
                pass1[dstRow0 + dx] = E0;
                pass1[dstRow0 + dx + 1] = E1;
                pass1[dstRow0 + dx + 2] = E2;

                pass1[dstRow1 + dx] = E3;
                pass1[dstRow1 + dx + 1] = E4;
                pass1[dstRow1 + dx + 2] = E5;

                pass1[dstRow2 + dx] = E6;
                pass1[dstRow2 + dx + 1] = E7;
                pass1[dstRow2 + dx + 2] = E8;
            }
        }

        // Pass 2: Lightweight 3x3 edge-preserving anti-alias pass on the HD buffer
        int[] dst = new int[dstW * dstH];
        System.arraycopy(pass1, 0, dst, 0, pass1.length);
        for (int y = 1; y < dstH - 1; ++y) {
            int row = y * dstW;
            for (int x = 1; x < dstW - 1; ++x) {
                int idx = row + x;
                int c = pass1[idx];
                int a = (c >>> 24) & 0xFF;
                if (a == 0) {
                    continue;
                }
                int up = pass1[idx - dstW];
                int dn = pass1[idx + dstW];
                int lf = pass1[idx - 1];
                int rt = pass1[idx + 1];

                int sumW = 4;
                int sumA = a * 4;
                int sumR = ((c >>> 16) & 0xFF) * 4;
                int sumG = ((c >>> 8) & 0xFF) * 4;
                int sumB = (c & 0xFF) * 4;

                int[] neighbors = new int[]{up, dn, lf, rt};
                for (int n = 0; n < 4; ++n) {
                    int nc = neighbors[n];
                    int na = (nc >>> 24) & 0xFF;
                    if (Math.abs(na - a) <= 80 && colorDist(c, nc) <= 85) {
                        sumW += 1;
                        sumA += na;
                        sumR += (nc >>> 16) & 0xFF;
                        sumG += (nc >>> 8) & 0xFF;
                        sumB += nc & 0xFF;
                    }
                }
                if (sumW > 4) {
                    dst[idx] = ((sumA / sumW) << 24)
                            | ((sumR / sumW) << 16)
                            | ((sumG / sumW) << 8)
                            | (sumB / sumW);
                }
            }
        }

        return HdPlatform.backend().createSurfaceFromArgb(dst, dstW, dstH);
    }

    private static int colorDist(int c1, int c2) {
        int a1 = (c1 >>> 24) & 0xFF;
        int a2 = (c2 >>> 24) & 0xFF;
        if (a1 < 32 || a2 < 32) {
            return 999;
        }
        int dr = Math.abs(((c1 >>> 16) & 0xFF) - ((c2 >>> 16) & 0xFF));
        int dg = Math.abs(((c1 >>> 8) & 0xFF) - ((c2 >>> 8) & 0xFF));
        int db = Math.abs((c1 & 0xFF) - (c2 & 0xFF));
        return Math.max(dr, Math.max(dg, db));
    }

    /**
     * Nearest-neighbor 3x integer upscale for classic pixel mode or bitmap font strips.
     */
    public static HdSurface upscaleNearest3x(int[] src, int w, int h) {
        int dstW = w * 3;
        int dstH = h * 3;
        int[] dst = new int[dstW * dstH];
        for (int y = 0; y < h; ++y) {
            int srcRow = y * w;
            int r0 = (y * 3) * dstW;
            int r1 = r0 + dstW;
            int r2 = r1 + dstW;
            for (int x = 0; x < w; ++x) {
                int p = src[srcRow + x];
                int dx = x * 3;
                dst[r0 + dx] = p;
                dst[r0 + dx + 1] = p;
                dst[r0 + dx + 2] = p;
                dst[r1 + dx] = p;
                dst[r1 + dx + 1] = p;
                dst[r1 + dx + 2] = p;
                dst[r2 + dx] = p;
                dst[r2 + dx + 1] = p;
                dst[r2 + dx + 2] = p;
            }
        }
        return HdPlatform.backend().createSurfaceFromArgb(dst, dstW, dstH);
    }
}
