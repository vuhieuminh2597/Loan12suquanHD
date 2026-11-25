package com.loan12suquan.hd.desktop;

import com.loan12suquan.hd.platform.HdCanvas2D;
import com.loan12suquan.hd.platform.HdSurface;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public final class DesktopSurface implements HdSurface {
    private final BufferedImage image;
    private DesktopCanvas2D cachedCanvas;

    public DesktopSurface(BufferedImage image) {
        this.image = image;
    }

    public static DesktopSurface create(int width, int height, boolean fillWhite) {
        BufferedImage img = new BufferedImage(
                Math.max(1, width),
                Math.max(1, height),
                BufferedImage.TYPE_INT_ARGB);
        if (fillWhite) {
            Graphics2D g2 = img.createGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, img.getWidth(), img.getHeight());
            g2.dispose();
        }
        return new DesktopSurface(img);
    }

    public BufferedImage getBufferedImage() {
        return this.image;
    }

    @Override
    public int getWidth() {
        return this.image.getWidth();
    }

    @Override
    public int getHeight() {
        return this.image.getHeight();
    }

    @Override
    public HdCanvas2D getCanvas2D() {
        if (this.cachedCanvas == null) {
            this.cachedCanvas = new DesktopCanvas2D(this.image.createGraphics());
        }
        return this.cachedCanvas;
    }

    @Override
    public int getArgb(int x, int y) {
        return this.image.getRGB(x, y);
    }

    @Override
    public void getArgb(int x, int y, int w, int h, int[] outArgb, int offset, int scanlength) {
        this.image.getRGB(x, y, w, h, outArgb, offset, scanlength);
    }

    @Override
    public void setArgb(int x, int y, int w, int h, int[] srcArgb, int offset, int scanlength) {
        this.image.setRGB(x, y, w, h, srcArgb, offset, scanlength);
    }

    @Override
    public Object getNativeHandle() {
        return this.image;
    }
}
