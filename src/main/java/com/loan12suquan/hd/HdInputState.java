package com.loan12suquan.hd;

/**
 * Live PC/Touch HD UX state tracking mouse hover coordinates and scroll-wheel deltas
 * in logical {@code 240x320} units.
 */
public final class HdInputState {
    public static volatile int hoverX = -100;
    public static volatile int hoverY = -100;
    private static int wheelDelta = 0;

    private HdInputState() {
    }

    public static void updateHover(int logicalX, int logicalY) {
        hoverX = logicalX;
        hoverY = logicalY;
    }

    public static void clearHover() {
        hoverX = -100;
        hoverY = -100;
    }

    public static boolean isHovered(int x, int y, int w, int h) {
        int hx = hoverX;
        int hy = hoverY;
        return hx >= x && hx <= x + w && hy >= y && hy <= y + h;
    }

    public static synchronized void addWheelDelta(int amountY) {
        if (amountY != 0) {
            wheelDelta += amountY;
        }
    }

    public static synchronized int consumeWheelDelta() {
        int d = wheelDelta;
        wheelDelta = 0;
        return d;
    }
}
