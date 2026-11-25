package com.mg.engine.graphics;

/** Mutable 2D integer rectangle used for clipping, layout bounds, and hit testing. */
public class Rect {
    public int a;
    public int b;
    public int c;
    public int d;

    public Rect() {
    }

    public Rect(int x, int y, int width, int height) {
        this.a = x;
        this.b = y;
        this.c = width;
        this.d = height;
    }

    public void a(int x, int y, int width, int height) {
        this.a = x;
        this.b = y;
        this.c = width;
        this.d = height;
    }

    public boolean a(int px, int py) {
        return px > this.a && px < this.a + this.c && py > this.b && py < this.b + this.d;
    }

    public boolean a(Rect other) {
        return this.b(other.a, other.b, other.c, other.d);
    }

    public boolean b(int rx, int ry, int rw, int rh) {
        int tw = this.c;
        int th = this.d;
        if (rw > 0 && rh > 0 && tw > 0 && th > 0) {
            int tx = this.a;
            int ty = this.b;
            int rRight = rw + rx;
            int rBottom = rh + ry;
            tw += tx;
            th += ty;
            return (rRight < rx || rRight > tx)
                    && (rBottom < ry || rBottom > ty)
                    && (tw < tx || tw > rx)
                    && (th < ty || th > ry);
        }
        return false;
    }

    public boolean b(int px, int py) {
        int w = this.c;
        int h = this.d;
        if ((w | h) < 0) {
            return false;
        }
        int x = this.a;
        int y = this.b;
        if (px >= x && py >= y) {
            w += x;
            h += y;
            return (w < x || w > px) && (h < y || h > py);
        }
        return false;
    }

    public String toString() {
        return "[X]: " + this.a + " [Y]: " + this.b + " [W]: " + this.c + " [H]: " + this.d;
    }
}



