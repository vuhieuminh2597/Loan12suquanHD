package com.loan12suquan.hd.platform;

/**
 * Cross-platform vector path builder backed by {@code GeneralPath} on Desktop
 * and {@code CanvasRenderingContext2D} path commands on Web / iPhone PWA.
 */
public interface HdPath {
    void moveTo(double x, double y);

    void lineTo(double x, double y);

    void quadTo(double cx, double cy, double x, double y);

    void curveTo(double c1x, double c1y, double c2x, double c2y, double x, double y);

    void arc(double cx, double cy, double radius, double startRad, double endRad, boolean anticlockwise);

    void closePath();
}
