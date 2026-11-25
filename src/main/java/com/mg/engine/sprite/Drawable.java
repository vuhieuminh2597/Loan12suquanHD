package com.mg.engine.sprite;

import com.mg.engine.core.GameConfig;
import javax.microedition.lcdui.Graphics;

/**
 * Base 2D drawable object with position, size, anchor, visibility, linear movement helpers,
 * and automatic 60 FPS sub-tick render interpolation.
 */
public abstract class Drawable {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected int anchor;
    protected boolean visible = true;
    private int prevX;
    private int prevY;
    private int moveTickStampX = -1;
    private int moveTickStampY = -1;

    public abstract void a(Graphics g, int offsetX, int offsetY);

    public void a(Graphics g) {
        this.a(g, 0, 0);
    }

    public void d(int x, int y) {
        this.f(x);
        this.g(y);
    }

    public void a(int x, int y, int width, int height) {
        this.d(x, y);
        this.h(width);
        this.i(height);
    }

    public int i() {
        return this.x;
    }

    public void f(int x) {
        this.x = x;
        this.prevX = x;
        this.moveTickStampX = -1;
    }

    public int j() {
        return this.y;
    }

    public void g(int y) {
        this.y = y;
        this.prevY = y;
        this.moveTickStampY = -1;
    }

    /** Moves {@code x} smoothly while recording {@code prevX} for 60 FPS sub-tick interpolation. */
    public final void stepToX(int nextX) {
        if (this.moveTickStampX != GameConfig.logicTickCounter) {
            this.prevX = this.x;
            this.moveTickStampX = GameConfig.logicTickCounter;
        }
        this.x = nextX;
    }

    /** Moves {@code y} smoothly while recording {@code prevY} for 60 FPS sub-tick interpolation. */
    public final void stepToY(int nextY) {
        if (this.moveTickStampY != GameConfig.logicTickCounter) {
            this.prevY = this.y;
            this.moveTickStampY = GameConfig.logicTickCounter;
        }
        this.y = nextY;
    }

    /** Returns 60 FPS sub-tick interpolated X coordinate (falls back to exact {@code x} when stationary or 25 FPS). */
    public final int getRenderX() {
        if (!GameConfig.highFpsEnabled || this.moveTickStampX != GameConfig.logicTickCounter) {
            return this.x;
        }
        return this.prevX + (((this.x - this.prevX) * GameConfig.renderSubTick256) >> 8);
    }

    /** Returns 60 FPS sub-tick interpolated Y coordinate (falls back to exact {@code y} when stationary or 25 FPS). */
    public final int getRenderY() {
        if (!GameConfig.highFpsEnabled || this.moveTickStampY != GameConfig.logicTickCounter) {
            return this.y;
        }
        return this.prevY + (((this.y - this.prevY) * GameConfig.renderSubTick256) >> 8);
    }

    public int k() {
        return this.width;
    }

    public void h(int width) {
        this.width = width;
    }

    public int l() {
        return this.height;
    }

    public void i(int height) {
        this.height = height;
    }

    public void j(int anchor) {
        this.anchor = anchor;
    }

    /** Adjusts {@code (x, y)} from an anchor point into top-left coordinates. */
    public void k(int anchorFlags) {
        if ((anchorFlags & Graphics.BOTTOM) == Graphics.BOTTOM) {
            this.y -= this.height;
        } else if ((anchorFlags & Graphics.VCENTER) == Graphics.VCENTER) {
            this.y -= this.height / 2;
        }

        if ((anchorFlags & Graphics.RIGHT) == Graphics.RIGHT) {
            this.x -= this.width;
        } else if ((anchorFlags & Graphics.HCENTER) == Graphics.HCENTER) {
            this.x -= this.width / 2;
        }
        this.prevX = this.x;
        this.prevY = this.y;
        this.moveTickStampX = -1;
        this.moveTickStampY = -1;
    }

    /** Steps both X and Y toward {@code (destX, destY)}; returns true when reached. */
    public boolean c(int destX, int destY, int stepX, int stepY) {
        boolean reached = this.e(destX, stepX);
        reached &= this.f(destY, stepY);
        return reached;
    }

    /** Computes a positive step size to cover {@code |to - from|} in roughly {@code divisor} steps. */
    public final int b(int from, int to, int divisor) {
        int dist = Math.abs(to - from);
        return dist < divisor ? 1 : dist / divisor;
    }

    /** Moves {@code x} toward {@code destX} by {@code step}; returns true if arrived. */
    public final boolean e(int destX, int step) {
        if (this.moveTickStampX != GameConfig.logicTickCounter) {
            this.prevX = this.x;
            this.moveTickStampX = GameConfig.logicTickCounter;
        }
        boolean wasGreater = this.x > destX;
        if (wasGreater) {
            this.x -= step;
        } else {
            this.x += step;
        }
        boolean isGreater = this.x > destX;
        if (wasGreater != isGreater) {
            this.x = destX;
        }
        return this.x == destX;
    }

    /** Moves {@code y} toward {@code destY} by {@code step}; returns true if arrived. */
    public final boolean f(int destY, int step) {
        if (this.moveTickStampY != GameConfig.logicTickCounter) {
            this.prevY = this.y;
            this.moveTickStampY = GameConfig.logicTickCounter;
        }
        boolean wasGreater = this.y > destY;
        if (wasGreater) {
            this.y -= step;
        } else {
            this.y += step;
        }
        boolean isGreater = this.y > destY;
        if (wasGreater != isGreater) {
            this.y = destY;
        }
        return this.y == destY;
    }

    public boolean m() {
        return this.visible;
    }

    public void a(boolean visible) {
        this.visible = visible;
    }
}

