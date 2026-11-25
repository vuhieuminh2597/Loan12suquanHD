package com.loan12suquan.hd.web;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.platform.HdCanvas2D;
import com.loan12suquan.hd.platform.HdPath;
import com.loan12suquan.hd.platform.HdSurface;
import org.teavm.jso.JSObject;

/**
 * High-speed HTML5 {@code CanvasRenderingContext2D} implementation of {@link HdCanvas2D}
 * with zero-allocation gradient fast-paths and GPU 8-way sprite region transforms.
 */
public final class WebCanvas2D implements HdCanvas2D {
    private static final String[] HEX_BYTE = new String[256];

    static {
        final char[] digits = "0123456789abcdef".toCharArray();
        for (int i = 0; i < 256; ++i) {
            HEX_BYTE[i] = new String(new char[]{digits[(i >>> 4) & 0xF], digits[i & 0xF]});
        }
    }

    private final WebSurface ownerSurface;
    private final JSObject ctx;
    private boolean hasMidpClipFrame = false;
    private String currentPaintCss = "#000000";
    private float currentStrokeWidth = HdConfig.HD_SCALE;
    private boolean currentStrokeRound = false;

    public WebCanvas2D(WebSurface ownerSurface, JSObject ctx) {
        this.ownerSurface = ownerSurface;
        this.ctx = ctx;
        WebDomBridge.ctxSetStroke(this.ctx, this.currentStrokeWidth, false);
    }

    private static String toHexCss(int rgb) {
        int r = (rgb >>> 16) & 0xFF;
        int g = (rgb >>> 8) & 0xFF;
        int b = rgb & 0xFF;
        return "#" + HEX_BYTE[r] + HEX_BYTE[g] + HEX_BYTE[b];
    }

    @Override
    public void save() {
        WebDomBridge.ctxSave(this.ctx);
    }

    @Override
    public void restore() {
        WebDomBridge.ctxRestore(this.ctx);
        this.currentStrokeWidth = -1f;
    }

    @Override
    public void applyMidpTransformAndClip(int txHd, int tyHd, int clipXHd, int clipYHd, int clipWHd, int clipHHd) {
        if (this.hasMidpClipFrame) {
            WebDomBridge.ctxRestore(this.ctx);
            this.hasMidpClipFrame = false;
        }
        WebDomBridge.ctxSave(this.ctx);
        this.hasMidpClipFrame = true;
        WebDomBridge.ctxSetTranslateTransform(this.ctx, txHd, tyHd);
        WebDomBridge.ctxClipRect(this.ctx, clipXHd, clipYHd, Math.max(0, clipWHd), Math.max(0, clipHHd));
        WebDomBridge.ctxSetCssPaint(this.ctx, this.currentPaintCss);
        WebDomBridge.ctxSetStroke(this.ctx, this.currentStrokeWidth, this.currentStrokeRound);
    }

    @Override
    public void translate(double tx, double ty) {
        WebDomBridge.ctxTranslate(this.ctx, tx, ty);
    }

    @Override
    public void rotate(double radians) {
        WebDomBridge.ctxRotate(this.ctx, radians);
    }

    @Override
    public void scale(double sx, double sy) {
        WebDomBridge.ctxScale(this.ctx, sx, sy);
    }

    @Override
    public void clipRect(double x, double y, double w, double h) {
        WebDomBridge.ctxClipRect(this.ctx, x, y, w, h);
    }

    @Override
    public void clipOval(double x, double y, double w, double h) {
        WebDomBridge.ctxClipOval(this.ctx, x, y, w, h);
    }

    @Override
    public void clipPath(HdPath path) {
        if (path instanceof WebPath) {
            WebDomBridge.ctxClipPath(this.ctx, ((WebPath) path).nativePath);
        }
    }

    @Override
    public void setColor(int rgb) {
        this.currentPaintCss = toHexCss(rgb);
        WebDomBridge.ctxSetCssPaint(this.ctx, this.currentPaintCss);
    }

    @Override
    public void setColorRgba(int r, int g, int b, int a) {
        int rc = Math.max(0, Math.min(255, r));
        int gc = Math.max(0, Math.min(255, g));
        int bc = Math.max(0, Math.min(255, b));
        int ac = Math.max(0, Math.min(255, a));
        if (ac == 255) {
            setColor((rc << 16) | (gc << 8) | bc);
        } else {
            this.currentPaintCss = "rgba(" + rc + "," + gc + "," + bc + "," + (ac / 255.0f) + ")";
            WebDomBridge.ctxSetCssPaint(this.ctx, this.currentPaintCss);
        }
    }

    @Override
    public void setColorHexAlpha(int rgb, int alpha) {
        setColorRgba((rgb >>> 16) & 0xFF, (rgb >>> 8) & 0xFF, rgb & 0xFF, alpha);
    }

    @Override
    public void setLinearGradient(double x1, double y1, double x2, double y2, float[] stops, int[] argbColors) {
        int n = stops.length;
        if (n == 2 && stops[0] == 0f && stops[1] == 1f) {
            WebDomBridge.ctxSetLinearGradient2(this.ctx, x1, y1, x2, y2, argbColors[0], argbColors[1]);
        } else if (n == 3) {
            WebDomBridge.ctxSetLinearGradient3(
                    this.ctx, x1, y1, x2, y2,
                    stops[0], argbColors[0],
                    stops[1], argbColors[1],
                    stops[2], argbColors[2]);
        } else {
            WebDomBridge.ctxSetLinearGradientN(this.ctx, x1, y1, x2, y2, stops, argbColors);
        }
    }

    @Override
    public void setRadialGradient(double cx, double cy, double radius, float[] stops, int[] argbColors) {
        int n = stops.length;
        if (n == 2 && stops[0] == 0f && stops[1] == 1f) {
            WebDomBridge.ctxSetRadialGradient2(this.ctx, cx, cy, radius, argbColors[0], argbColors[1]);
        } else if (n == 3) {
            WebDomBridge.ctxSetRadialGradient3(
                    this.ctx, cx, cy, radius,
                    stops[0], argbColors[0],
                    stops[1], argbColors[1],
                    stops[2], argbColors[2]);
        } else {
            WebDomBridge.ctxSetRadialGradientN(this.ctx, cx, cy, radius, stops, argbColors);
        }
    }

    @Override
    public void setStrokeWidth(float width) {
        if (this.currentStrokeWidth != width || this.currentStrokeRound) {
            this.currentStrokeWidth = width;
            this.currentStrokeRound = false;
            WebDomBridge.ctxSetStroke(this.ctx, width, false);
        }
    }

    @Override
    public void setStrokeRound(float width) {
        if (this.currentStrokeWidth != width || !this.currentStrokeRound) {
            this.currentStrokeWidth = width;
            this.currentStrokeRound = true;
            WebDomBridge.ctxSetStroke(this.ctx, width, true);
        }
    }

    @Override
    public void fillRect(double x, double y, double w, double h) {
        this.ownerSurface.markCanvasDirty();
        WebDomBridge.ctxFillRect(this.ctx, x, y, w, h);
    }

    @Override
    public void drawRect(double x, double y, double w, double h) {
        this.ownerSurface.markCanvasDirty();
        WebDomBridge.ctxDrawRect(this.ctx, x, y, w, h);
    }

    @Override
    public void fillRoundRect(double x, double y, double w, double h, double arcDiameter) {
        this.ownerSurface.markCanvasDirty();
        WebDomBridge.ctxRoundRect(this.ctx, x, y, w, h, arcDiameter, true);
    }

    @Override
    public void drawRoundRect(double x, double y, double w, double h, double arcDiameter) {
        this.ownerSurface.markCanvasDirty();
        WebDomBridge.ctxRoundRect(this.ctx, x, y, w, h, arcDiameter, false);
    }

    @Override
    public void fillOval(double x, double y, double w, double h) {
        this.ownerSurface.markCanvasDirty();
        WebDomBridge.ctxOval(this.ctx, x, y, w, h, true);
    }

    @Override
    public void drawOval(double x, double y, double w, double h) {
        this.ownerSurface.markCanvasDirty();
        WebDomBridge.ctxOval(this.ctx, x, y, w, h, false);
    }

    @Override
    public void drawLine(double x1, double y1, double x2, double y2) {
        this.ownerSurface.markCanvasDirty();
        WebDomBridge.ctxDrawLine(this.ctx, x1, y1, x2, y2);
    }

    @Override
    public void fillArc(double x, double y, double w, double h, double startDeg, double arcDeg) {
        this.ownerSurface.markCanvasDirty();
        WebDomBridge.ctxArc(this.ctx, x, y, w, h, startDeg, arcDeg, true);
    }

    @Override
    public void drawArc(double x, double y, double w, double h, double startDeg, double arcDeg) {
        this.ownerSurface.markCanvasDirty();
        WebDomBridge.ctxArc(this.ctx, x, y, w, h, startDeg, arcDeg, false);
    }

    @Override
    public void fillPolygon(int[] xs, int[] ys, int count) {
        this.ownerSurface.markCanvasDirty();
        if (count == 3) {
            WebDomBridge.ctxFillTriangle(this.ctx, xs[0], ys[0], xs[1], ys[1], xs[2], ys[2]);
        } else {
            WebDomBridge.ctxFillPolygon(this.ctx, xs, ys, count);
        }
    }

    @Override
    public HdPath createPath() {
        return new WebPath();
    }

    @Override
    public void fillPath(HdPath path) {
        if (path instanceof WebPath) {
            this.ownerSurface.markCanvasDirty();
            WebDomBridge.ctxFillPath(this.ctx, ((WebPath) path).nativePath);
        }
    }

    @Override
    public void drawPath(HdPath path) {
        if (path instanceof WebPath) {
            this.ownerSurface.markCanvasDirty();
            WebDomBridge.ctxDrawPath(this.ctx, ((WebPath) path).nativePath);
        }
    }

    @Override
    public void drawSurface(HdSurface src, double dx, double dy, double dw, double dh) {
        if (src instanceof WebSurface) {
            this.ownerSurface.markCanvasDirty();
            WebDomBridge.ctxDrawSurface(this.ctx, ((WebSurface) src).getCanvasElement(), dx, dy, dw, dh);
        }
    }

    @Override
    public void drawSurfaceRegion(
            HdSurface src,
            int sx, int sy, int sw, int sh,
            int dx, int dy, int dw, int dh,
            boolean flipX, boolean flipY) {
        int transform = flipX ? (flipY ? 3 : 2) : (flipY ? 1 : 0);
        drawSurfaceRegionTransformed(src, sx, sy, sw, sh, dx, dy, dw, dh, transform);
    }

    @Override
    public void drawSurfaceRegionTransformed(
            HdSurface src,
            int sx, int sy, int sw, int sh,
            int dx, int dy, int dw, int dh,
            int midpTransform) {
        if (src instanceof WebSurface && sw > 0 && sh > 0 && dw > 0 && dh > 0) {
            this.ownerSurface.markCanvasDirty();
            WebDomBridge.ctxDrawSurfaceRegionTransformed(
                    this.ctx, ((WebSurface) src).getCanvasElement(),
                    sx, sy, sw, sh, dx, dy, dw, dh, midpTransform);
        }
    }

    @Override
    public int measureTextWidth(int fontKind, String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return WebDomBridge.measureTextWidth(fontKind & 3, text);
    }

    @Override
    public int getFontAscent(int fontKind) {
        return WebDomBridge.getFontAscent(fontKind & 3);
    }

    @Override
    public int getFontDescent(int fontKind) {
        return WebDomBridge.getFontDescent(fontKind & 3);
    }

    @Override
    public void drawStyledText(
            int fontKind,
            String text,
            double drawX,
            double baselineY,
            double scaleX,
            int topRgb,
            int botRgb,
            int shadowArgb,
            int outlineArgb,
            float outlineWidth,
            double shadowOffsetX,
            double shadowOffsetY) {
        if (text == null || text.isEmpty()) {
            return;
        }
        this.ownerSurface.markCanvasDirty();
        WebDomBridge.drawStyledText(
                this.ctx, fontKind & 3, text, drawX, baselineY, scaleX,
                topRgb, botRgb, shadowArgb, outlineArgb, outlineWidth, shadowOffsetX, shadowOffsetY);
    }
}
