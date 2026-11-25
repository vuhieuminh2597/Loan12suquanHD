package com.loan12suquan.hd.platform;

/**
 * Cross-platform ARGB pixel & vector drawing surface backed by {@code BufferedImage} on Desktop
 * and offscreen/onscreen HTML5 {@code <canvas>} on Web / iPhone PWA.
 */
public interface HdSurface {
    int getWidth();

    int getHeight();

    HdCanvas2D getCanvas2D();

    int getArgb(int x, int y);

    void getArgb(int x, int y, int w, int h, int[] outArgb, int offset, int scanlength);

    void setArgb(int x, int y, int w, int h, int[] srcArgb, int offset, int scanlength);

    Object getNativeHandle();
}
