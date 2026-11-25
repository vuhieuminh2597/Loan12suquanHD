package com.loan12suquan.hd.desktop;

import com.loan12suquan.hd.platform.HdPath;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;

public final class DesktopPath implements HdPath {
    final GeneralPath path = new GeneralPath();

    @Override
    public void moveTo(double x, double y) {
        this.path.moveTo((float) x, (float) y);
    }

    @Override
    public void lineTo(double x, double y) {
        this.path.lineTo((float) x, (float) y);
    }

    @Override
    public void quadTo(double cx, double cy, double x, double y) {
        this.path.quadTo((float) cx, (float) cy, (float) x, (float) y);
    }

    @Override
    public void curveTo(double c1x, double c1y, double c2x, double c2y, double x, double y) {
        this.path.curveTo((float) c1x, (float) c1y, (float) c2x, (float) c2y, (float) x, (float) y);
    }

    @Override
    public void arc(double cx, double cy, double radius, double startRad, double endRad, boolean anticlockwise) {
        // Convert canvas clockwise screen radians to Java2D counter-clockwise degrees
        double startDeg = -Math.toDegrees(startRad);
        double sweepRad = endRad - startRad;
        if (!anticlockwise) {
            while (sweepRad < 0) {
                sweepRad += Math.PI * 2.0;
            }
        } else {
            while (sweepRad > 0) {
                sweepRad -= Math.PI * 2.0;
            }
        }
        double extentDeg = -Math.toDegrees(sweepRad);
        Arc2D.Double arc = new Arc2D.Double(
                cx - radius, cy - radius, radius * 2.0, radius * 2.0,
                startDeg, extentDeg, Arc2D.OPEN);
        this.path.append(arc, this.path.getCurrentPoint() != null);
    }

    @Override
    public void closePath() {
        this.path.closePath();
    }
}
