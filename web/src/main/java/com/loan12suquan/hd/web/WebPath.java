package com.loan12suquan.hd.web;

import com.loan12suquan.hd.platform.HdPath;
import org.teavm.jso.JSObject;

public final class WebPath implements HdPath {
    final JSObject nativePath;

    public WebPath() {
        this.nativePath = WebDomBridge.createPath2D();
    }

    @Override
    public void moveTo(double x, double y) {
        WebDomBridge.pathMoveTo(this.nativePath, x, y);
    }

    @Override
    public void lineTo(double x, double y) {
        WebDomBridge.pathLineTo(this.nativePath, x, y);
    }

    @Override
    public void quadTo(double cx, double cy, double x, double y) {
        WebDomBridge.pathQuadTo(this.nativePath, cx, cy, x, y);
    }

    @Override
    public void curveTo(double c1x, double c1y, double c2x, double c2y, double x, double y) {
        WebDomBridge.pathCurveTo(this.nativePath, c1x, c1y, c2x, c2y, x, y);
    }

    @Override
    public void arc(double cx, double cy, double radius, double startRad, double endRad, boolean anticlockwise) {
        WebDomBridge.pathArc(this.nativePath, cx, cy, radius, startRad, endRad, anticlockwise);
    }

    @Override
    public void closePath() {
        WebDomBridge.pathClose(this.nativePath);
    }
}
