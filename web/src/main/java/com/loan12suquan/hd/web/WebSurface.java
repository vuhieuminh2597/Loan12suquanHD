package com.loan12suquan.hd.web;

import com.loan12suquan.hd.platform.HdCanvas2D;
import com.loan12suquan.hd.platform.HdSurface;
import org.teavm.jso.JSObject;
import org.teavm.jso.typedarrays.Int32Array;

public final class WebSurface implements HdSurface {
    private final JSObject canvas;
    private final int width;
    private final int height;
    private int[] argbCache;
    private boolean canvasDirty;
    private WebCanvas2D cachedContext;

    public WebSurface(JSObject canvas, int width, int height, int[] initialArgbCache, boolean canvasDirty) {
        this.canvas = canvas;
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.argbCache = initialArgbCache;
        this.canvasDirty = canvasDirty;
    }

    public static WebSurface create(int width, int height, boolean fillWhite) {
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        JSObject c = WebDomBridge.createCanvasElement(w, h, fillWhite);
        return new WebSurface(c, w, h, null, true);
    }

    public static WebSurface createFromArgb(int[] argb, int width, int height) {
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        int total = w * h;
        JSObject c = WebDomBridge.createCanvasElement(w, h, false);
        int[] copy = new int[total];
        if (argb != null && argb.length >= total) {
            System.arraycopy(argb, 0, copy, 0, total);
            Int32Array jsPixels = Int32Array.create(total);
            jsPixels.set(copy);
            WebDomBridge.writeCanvasArgbTyped(c, 0, 0, w, h, jsPixels);
        }
        return new WebSurface(c, w, h, copy, false);
    }

    public static WebSurface wrapPreloadedCanvas(JSObject preloadedCanvas) {
        int w = WebDomBridge.getCanvasWidth(preloadedCanvas);
        int h = WebDomBridge.getCanvasHeight(preloadedCanvas);
        int total = w * h;
        Int32Array jsPixels = WebDomBridge.readCanvasArgbTyped(preloadedCanvas, 0, 0, w, h);
        int[] pixels = new int[total];
        for (int i = 0; i < total; ++i) {
            pixels[i] = jsPixels.get(i);
        }
        return new WebSurface(preloadedCanvas, w, h, pixels, false);
    }

    public JSObject getCanvasElement() {
        return this.canvas;
    }

    void markCanvasDirty() {
        this.canvasDirty = true;
    }

    private void ensureArgbCacheSynced() {
        int total = this.width * this.height;
        if (this.argbCache == null) {
            this.argbCache = new int[total];
            this.canvasDirty = true;
        }
        if (this.canvasDirty) {
            Int32Array jsPixels = WebDomBridge.readCanvasArgbTyped(this.canvas, 0, 0, this.width, this.height);
            for (int i = 0; i < total; ++i) {
                this.argbCache[i] = jsPixels.get(i);
            }
            this.canvasDirty = false;
        }
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public HdCanvas2D getCanvas2D() {
        this.canvasDirty = true;
        if (this.cachedContext == null) {
            this.cachedContext = new WebCanvas2D(this, WebDomBridge.getContext2D(this.canvas));
        }
        return this.cachedContext;
    }

    @Override
    public int getArgb(int x, int y) {
        ensureArgbCacheSynced();
        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            return 0;
        }
        return this.argbCache[y * this.width + x];
    }

    @Override
    public void getArgb(int x, int y, int w, int h, int[] outArgb, int offset, int scanlength) {
        ensureArgbCacheSynced();
        for (int ry = 0; ry < h; ++ry) {
            int sy = y + ry;
            if (sy < 0 || sy >= this.height) {
                continue;
            }
            int srcRow = sy * this.width;
            int dstRow = offset + ry * scanlength;
            for (int rx = 0; rx < w; ++rx) {
                int sx = x + rx;
                if (sx >= 0 && sx < this.width) {
                    outArgb[dstRow + rx] = this.argbCache[srcRow + sx];
                }
            }
        }
    }

    @Override
    public void setArgb(int x, int y, int w, int h, int[] srcArgb, int offset, int scanlength) {
        ensureArgbCacheSynced();
        Int32Array patch = Int32Array.create(w * h);
        int p = 0;
        for (int ry = 0; ry < h; ++ry) {
            int dy = y + ry;
            int srcRow = offset + ry * scanlength;
            for (int rx = 0; rx < w; ++rx) {
                int dx = x + rx;
                int val = srcArgb[srcRow + rx];
                patch.set(p++, val);
                if (dy >= 0 && dy < this.height && dx >= 0 && dx < this.width) {
                    this.argbCache[dy * this.width + dx] = val;
                }
            }
        }
        WebDomBridge.writeCanvasArgbTyped(this.canvas, x, y, w, h, patch);
        this.canvasDirty = false;
    }

    @Override
    public Object getNativeHandle() {
        return this.canvas;
    }
}
