package com.loan12suquan.hd.desktop;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.platform.HdCanvas2D;
import com.loan12suquan.hd.platform.HdPath;
import com.loan12suquan.hd.platform.HdSurface;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;

public final class DesktopCanvas2D implements HdCanvas2D {
    private static final Font[] FONTS = new Font[4];
    private static final FontMetrics[] METRICS = new FontMetrics[4];

    static {
        String uiFamily = pickBestFontFamily(new String[]{"Segoe UI", "Tahoma", "Arial", Font.SANS_SERIF});
        String displayFamily = pickBestFontFamily(new String[]{"Palatino Linotype", "Georgia", "Times New Roman", uiFamily});
        AffineTransform condense = AffineTransform.getScaleInstance(0.90, 1.0);

        FONTS[FONT_PLAIN] = new Font(uiFamily, Font.BOLD, 23).deriveFont(condense);
        FONTS[FONT_BOLD] = new Font(uiFamily, Font.BOLD, 25).deriveFont(condense);
        FONTS[FONT_DISPLAY] = new Font(displayFamily, Font.BOLD, 48);
        FONTS[FONT_SUBTITLE] = new Font(uiFamily, Font.BOLD, 19);

        BufferedImage probe = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = probe.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        for (int i = 0; i < 4; ++i) {
            METRICS[i] = g2.getFontMetrics(FONTS[i]);
        }
        g2.dispose();
    }

    private static String pickBestFontFamily(String[] preferred) {
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (int p = 0; p < preferred.length; ++p) {
            for (int a = 0; a < available.length; ++a) {
                if (available[a].equalsIgnoreCase(preferred[p])) {
                    return available[a];
                }
            }
        }
        return Font.SANS_SERIF;
    }

    private static final class SavedState {
        final AffineTransform tx;
        final Shape clip;
        final Paint paint;
        final Stroke stroke;

        SavedState(AffineTransform tx, Shape clip, Paint paint, Stroke stroke) {
            this.tx = tx;
            this.clip = clip;
            this.paint = paint;
            this.stroke = stroke;
        }
    }

    private final Graphics2D g2;
    private final Deque<SavedState> stateStack = new ArrayDeque<SavedState>();

    public DesktopCanvas2D(Graphics2D g2) {
        this.g2 = g2;
        this.g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        this.g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        this.g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        this.g2.setStroke(new BasicStroke(HdConfig.HD_SCALE));
    }

    private static Color toAwtColor(int argb) {
        return new Color(argb, true);
    }

    private static Color[] toAwtColors(int[] argbColors) {
        Color[] cols = new Color[argbColors.length];
        for (int i = 0; i < argbColors.length; ++i) {
            cols[i] = new Color(argbColors[i], true);
        }
        return cols;
    }

    @Override
    public void save() {
        this.stateStack.push(new SavedState(
                this.g2.getTransform(),
                this.g2.getClip(),
                this.g2.getPaint(),
                this.g2.getStroke()));
    }

    @Override
    public void restore() {
        if (!this.stateStack.isEmpty()) {
            SavedState s = this.stateStack.pop();
            this.g2.setTransform(s.tx);
            this.g2.setClip(s.clip);
            this.g2.setPaint(s.paint);
            this.g2.setStroke(s.stroke);
        }
    }

    @Override
    public void applyMidpTransformAndClip(int txHd, int tyHd, int clipXHd, int clipYHd, int clipWHd, int clipHHd) {
        this.g2.setTransform(AffineTransform.getTranslateInstance(txHd, tyHd));
        if (clipWHd <= 0 || clipHHd <= 0) {
            this.g2.setClip(0, 0, 0, 0);
        } else {
            this.g2.setClip(clipXHd, clipYHd, clipWHd, clipHHd);
        }
    }

    @Override
    public void translate(double tx, double ty) {
        this.g2.translate(tx, ty);
    }

    @Override
    public void rotate(double radians) {
        this.g2.rotate(radians);
    }

    @Override
    public void scale(double sx, double sy) {
        this.g2.scale(sx, sy);
    }

    @Override
    public void clipRect(double x, double y, double w, double h) {
        this.g2.clip(new Rectangle2D.Double(x, y, Math.max(0, w), Math.max(0, h)));
    }

    @Override
    public void clipOval(double x, double y, double w, double h) {
        this.g2.clip(new Ellipse2D.Double(x, y, w, h));
    }

    @Override
    public void clipPath(HdPath path) {
        if (path instanceof DesktopPath) {
            this.g2.clip(((DesktopPath) path).path);
        }
    }

    @Override
    public void setColor(int rgb) {
        this.g2.setColor(new Color(rgb & 0x00FFFFFF));
    }

    @Override
    public void setColorRgba(int r, int g, int b, int a) {
        this.g2.setColor(new Color(
                Math.max(0, Math.min(255, r)),
                Math.max(0, Math.min(255, g)),
                Math.max(0, Math.min(255, b)),
                Math.max(0, Math.min(255, a))));
    }

    @Override
    public void setColorHexAlpha(int rgb, int alpha) {
        setColorRgba((rgb >>> 16) & 0xFF, (rgb >>> 8) & 0xFF, rgb & 0xFF, alpha);
    }

    @Override
    public void setLinearGradient(double x1, double y1, double x2, double y2, float[] stops, int[] argbColors) {
        if (x1 == x2 && y1 == y2) {
            x2 = x1 + 1.0;
        }
        if (stops.length == 2 && stops[0] == 0f && stops[1] == 1f) {
            this.g2.setPaint(new GradientPaint(
                    (float) x1, (float) y1, toAwtColor(argbColors[0]),
                    (float) x2, (float) y2, toAwtColor(argbColors[1])));
        } else {
            this.g2.setPaint(new LinearGradientPaint(
                    (float) x1, (float) y1, (float) x2, (float) y2,
                    stops, toAwtColors(argbColors)));
        }
    }

    @Override
    public void setRadialGradient(double cx, double cy, double radius, float[] stops, int[] argbColors) {
        float r = (float) Math.max(1.0, radius);
        this.g2.setPaint(new RadialGradientPaint(
                new Point2D.Float((float) cx, (float) cy),
                r,
                stops,
                toAwtColors(argbColors)));
    }

    @Override
    public void setStrokeWidth(float width) {
        this.g2.setStroke(new BasicStroke(width));
    }

    @Override
    public void setStrokeRound(float width) {
        this.g2.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }

    @Override
    public void fillRect(double x, double y, double w, double h) {
        this.g2.fill(new Rectangle2D.Double(x, y, w, h));
    }

    @Override
    public void drawRect(double x, double y, double w, double h) {
        this.g2.draw(new Rectangle2D.Double(x, y, w, h));
    }

    @Override
    public void fillRoundRect(double x, double y, double w, double h, double arcDiameter) {
        this.g2.fill(new RoundRectangle2D.Double(x, y, w, h, arcDiameter, arcDiameter));
    }

    @Override
    public void drawRoundRect(double x, double y, double w, double h, double arcDiameter) {
        this.g2.draw(new RoundRectangle2D.Double(x, y, w, h, arcDiameter, arcDiameter));
    }

    @Override
    public void fillOval(double x, double y, double w, double h) {
        this.g2.fill(new Ellipse2D.Double(x, y, w, h));
    }

    @Override
    public void drawOval(double x, double y, double w, double h) {
        this.g2.draw(new Ellipse2D.Double(x, y, w, h));
    }

    @Override
    public void drawLine(double x1, double y1, double x2, double y2) {
        this.g2.draw(new java.awt.geom.Line2D.Double(x1, y1, x2, y2));
    }

    @Override
    public void fillArc(double x, double y, double w, double h, double startDeg, double arcDeg) {
        this.g2.fill(new Arc2D.Double(x, y, w, h, startDeg, arcDeg, Arc2D.PIE));
    }

    @Override
    public void drawArc(double x, double y, double w, double h, double startDeg, double arcDeg) {
        this.g2.draw(new Arc2D.Double(x, y, w, h, startDeg, arcDeg, Arc2D.OPEN));
    }

    @Override
    public void fillPolygon(int[] xs, int[] ys, int count) {
        this.g2.fillPolygon(xs, ys, count);
    }

    @Override
    public HdPath createPath() {
        return new DesktopPath();
    }

    @Override
    public void fillPath(HdPath path) {
        if (path instanceof DesktopPath) {
            this.g2.fill(((DesktopPath) path).path);
        }
    }

    @Override
    public void drawPath(HdPath path) {
        if (path instanceof DesktopPath) {
            this.g2.draw(((DesktopPath) path).path);
        }
    }

    @Override
    public void drawSurface(HdSurface src, double dx, double dy, double dw, double dh) {
        if (src instanceof DesktopSurface) {
            this.g2.drawImage(
                    ((DesktopSurface) src).getBufferedImage(),
                    (int) Math.round(dx), (int) Math.round(dy),
                    (int) Math.round(dw), (int) Math.round(dh),
                    null);
        }
    }

    @Override
    public void drawSurfaceRegion(
            HdSurface src,
            int sx, int sy, int sw, int sh,
            int dx, int dy, int dw, int dh,
            boolean flipX, boolean flipY) {
        if (!(src instanceof DesktopSurface)) {
            return;
        }
        BufferedImage img = ((DesktopSurface) src).getBufferedImage();
        int sx1 = flipX ? (sx + sw) : sx;
        int sx2 = flipX ? sx : (sx + sw);
        int sy1 = flipY ? (sy + sh) : sy;
        int sy2 = flipY ? sy : (sy + sh);
        this.g2.drawImage(img, dx, dy, dx + dw, dy + dh, sx1, sy1, sx2, sy2, null);
    }

    @Override
    public void drawSurfaceRegionTransformed(
            HdSurface src,
            int sx, int sy, int sw, int sh,
            int dx, int dy, int dw, int dh,
            int midpTransform) {
        if (!(src instanceof DesktopSurface)) {
            return;
        }
        if (midpTransform == 0) {
            drawSurfaceRegion(src, sx, sy, sw, sh, dx, dy, dw, dh, false, false);
            return;
        }
        if (midpTransform == 1) {
            drawSurfaceRegion(src, sx, sy, sw, sh, dx, dy, dw, dh, false, true);
            return;
        }
        if (midpTransform == 2) {
            drawSurfaceRegion(src, sx, sy, sw, sh, dx, dy, dw, dh, true, false);
            return;
        }
        if (midpTransform == 3) {
            drawSurfaceRegion(src, sx, sy, sw, sh, dx, dy, dw, dh, true, true);
            return;
        }
        BufferedImage img = ((DesktopSurface) src).getBufferedImage();
        AffineTransform oldTx = this.g2.getTransform();
        switch (midpTransform) {
            case 4: // TRANS_MIRROR_ROT270
                this.g2.transform(new AffineTransform(0, 1, 1, 0, dx, dy));
                break;
            case 5: // TRANS_ROT90
                this.g2.transform(new AffineTransform(0, 1, -1, 0, dx + dw, dy));
                break;
            case 6: // TRANS_ROT270
                this.g2.transform(new AffineTransform(0, -1, 1, 0, dx, dy + dh));
                break;
            case 7: // TRANS_MIRROR_ROT90
                this.g2.transform(new AffineTransform(0, -1, -1, 0, dx + dw, dy + dh));
                break;
            default:
                break;
        }
        this.g2.drawImage(img, 0, 0, sw, sh, sx, sy, sx + sw, sy + sh, null);
        this.g2.setTransform(oldTx);
    }

    @Override
    public int measureTextWidth(int fontKind, String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return METRICS[fontKind & 3].stringWidth(text);
    }

    @Override
    public int getFontAscent(int fontKind) {
        return METRICS[fontKind & 3].getAscent();
    }

    @Override
    public int getFontDescent(int fontKind) {
        return METRICS[fontKind & 3].getDescent();
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
        int fk = fontKind & 3;
        Font font = FONTS[fk];
        FontMetrics fm = METRICS[fk];

        AffineTransform oldTx = this.g2.getTransform();
        Stroke oldStroke = this.g2.getStroke();
        Paint oldPaint = this.g2.getPaint();

        this.g2.translate(drawX, baselineY);
        if (scaleX < 0.999) {
            this.g2.scale(scaleX, 1.0);
        }

        FontRenderContext frc = this.g2.getFontRenderContext();
        GlyphVector gv = font.createGlyphVector(frc, text);
        Shape glyphShape = gv.getOutline(0f, 0f);

        // Drop shadow
        this.g2.translate(shadowOffsetX, shadowOffsetY);
        this.g2.setColor(toAwtColor(shadowArgb));
        this.g2.fill(glyphShape);
        this.g2.translate(-shadowOffsetX, -shadowOffsetY);

        // Outline
        this.g2.setColor(toAwtColor(outlineArgb));
        this.g2.setStroke(new BasicStroke(outlineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        this.g2.draw(glyphShape);

        // Gradient fill
        int ascent = fm.getAscent();
        int descent = fm.getDescent();
        this.g2.setPaint(new GradientPaint(
                0f, -ascent * 0.82f, new Color(topRgb & 0x00FFFFFF),
                0f, descent * 0.45f, new Color(botRgb & 0x00FFFFFF)));
        this.g2.fill(glyphShape);

        this.g2.setPaint(oldPaint);
        this.g2.setStroke(oldStroke);
        this.g2.setTransform(oldTx);
    }
}
