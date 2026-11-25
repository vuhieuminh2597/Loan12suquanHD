package com.loan12suquan.hd.platform;

/**
 * Cross-platform 2D vector, image, gradient, and Vietnamese TrueType typography context
 * shared 100% identically between Desktop (Java2D/OpenGL) and Web/iPhone 17 (HTML5 Canvas2D).
 */
public interface HdCanvas2D {
    int FONT_PLAIN = 0;
    int FONT_BOLD = 1;
    int FONT_DISPLAY = 2;
    int FONT_SUBTITLE = 3;

    void save();

    void restore();

    void applyMidpTransformAndClip(int txHd, int tyHd, int clipXHd, int clipYHd, int clipWHd, int clipHHd);

    void translate(double tx, double ty);

    void rotate(double radians);

    void scale(double sx, double sy);

    void clipRect(double x, double y, double w, double h);

    void clipOval(double x, double y, double w, double h);

    void clipPath(HdPath path);

    void setColor(int rgb);

    void setColorRgba(int r, int g, int b, int a);

    void setColorHexAlpha(int rgb, int alpha);

    void setLinearGradient(double x1, double y1, double x2, double y2, float[] stops, int[] argbColors);

    void setRadialGradient(double cx, double cy, double radius, float[] stops, int[] argbColors);

    void setStrokeWidth(float width);

    void setStrokeRound(float width);

    void fillRect(double x, double y, double w, double h);

    void drawRect(double x, double y, double w, double h);

    void fillRoundRect(double x, double y, double w, double h, double arcDiameter);

    void drawRoundRect(double x, double y, double w, double h, double arcDiameter);

    void fillOval(double x, double y, double w, double h);

    void drawOval(double x, double y, double w, double h);

    void drawLine(double x1, double y1, double x2, double y2);

    void fillArc(double x, double y, double w, double h, double startDeg, double arcDeg);

    void drawArc(double x, double y, double w, double h, double startDeg, double arcDeg);

    void fillPolygon(int[] xs, int[] ys, int count);

    HdPath createPath();

    void fillPath(HdPath path);

    void drawPath(HdPath path);

    void drawSurface(HdSurface src, double dx, double dy, double dw, double dh);

    void drawSurfaceRegion(
            HdSurface src,
            int sx, int sy, int sw, int sh,
            int dx, int dy, int dw, int dh,
            boolean flipX, boolean flipY);

    void drawSurfaceRegionTransformed(
            HdSurface src,
            int sx, int sy, int sw, int sh,
            int dx, int dy, int dw, int dh,
            int midpTransform);

    int measureTextWidth(int fontKind, String text);

    int getFontAscent(int fontKind);

    int getFontDescent(int fontKind);

    void drawStyledText(
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
            double shadowOffsetY);
}
