package com.mg.engine.ui;

import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.Rect;
import com.mg.engine.sprite.Drawable;
import javax.microedition.lcdui.Graphics;

/**
 * {@link MenuList} subclass whose items alternate sliding in from left/right edges
 * and wobble gently when focus changes.
 */
public class SlidingMenuList extends MenuList {
    protected int[] restX;
    protected boolean slideEnabled;
    protected boolean wobbleEnabled = true;
    protected Rect clipBounds;
    protected Rect savedClip = new Rect();
    protected int offscreenLeftX;
    protected int offscreenRightX;
    private boolean animatingSlide;
    private boolean slidingIn;
    protected int[] wobbleOffsets = new int[]{0, -1, 1, -2, 2, -3, 3, 0};
    protected int wobbleFrame;

    public boolean c() {
        return this.animatingSlide;
    }

    public SlidingMenuList() {
        this(false);
    }

    public SlidingMenuList(boolean slideEnabled) {
        this(slideEnabled, null);
    }

    public SlidingMenuList(boolean slideEnabled, Rect clipBounds) {
        this.slideEnabled = slideEnabled;
        if (slideEnabled) {
            if (clipBounds == null) {
                clipBounds = new Rect(0, 0, GameConfig.screenWidth, GameConfig.screenHeight);
            }
            this.clipBounds = clipBounds;
        }
    }

    public void a(Drawable[] items, int initialIndex) {
        super.a(items, initialIndex);
        if (this.slideEnabled) {
            this.d();
        }
    }

    /** Starts the alternating left/right slide-in entrance animation. */
    public void d() {
        this.animatingSlide = true;
        this.slidingIn = true;
        this.restX = new int[this.items.length];
        for (int i = 0; i < this.restX.length; ++i) {
            this.restX[i] = this.items[i].i();
        }

        int maxWidth = 0;
        int minX = Integer.MAX_VALUE;
        for (int i = 0; i < this.items.length; ++i) {
            if (this.items[i].k() > maxWidth) {
                maxWidth = this.items[i].k();
            }
            if (this.items[i].i() < minX) {
                minX = this.items[i].i();
            }
        }

        int slideDistance = this.clipBounds.a + this.clipBounds.c - minX;
        if (minX + maxWidth - slideDistance > this.clipBounds.a) {
            slideDistance += minX + maxWidth - slideDistance - this.clipBounds.a;
        }

        this.offscreenLeftX = minX - slideDistance;
        this.offscreenRightX = minX + slideDistance;
        boolean fromLeft = true;
        for (int i = 0; i < this.items.length; ++i) {
            this.items[i].f(fromLeft ? this.offscreenLeftX : this.offscreenRightX);
            fromLeft = !fromLeft;
        }
    }

    protected void b(int prevIndex, int newIndex) {
        if (this.wobbleEnabled) {
            this.wobbleFrame = 0;
        }
        super.b(prevIndex, newIndex);
    }

    public void a(int keyCode) {
        if (!this.animatingSlide) {
            super.a(keyCode);
        }
    }

    public void a(int pointerX, int pointerY) {
        if (!this.animatingSlide) {
            super.a(pointerX, pointerY);
        }
    }

    public void b() {
        if (this.animatingSlide) {
            int settledCount = 0;
            for (int i = 0; i < this.items.length; ++i) {
                int currentX = this.items[i].i();
                int delta = this.restX[i] - currentX;
                if (Math.abs(delta) < 2) {
                    currentX += delta;
                    ++settledCount;
                } else {
                    currentX += delta / 2;
                }
                this.items[i].stepToX(currentX);
            }

            if (settledCount >= this.items.length) {
                this.animatingSlide = false;
                if (!this.slidingIn) {
                    super.b(this.selectedIndex);
                }
            }
        }

        if (this.wobbleFrame < this.wobbleOffsets.length - 1) {
            ++this.wobbleFrame;
        }
    }

    protected void b(Graphics g) {
        DrawUtils.saveClip(g, this.savedClip);
        DrawUtils.intersectClip(g, this.savedClip, this.clipBounds.a, this.clipBounds.b, this.clipBounds.c, this.clipBounds.d);
        if (this.animatingSlide) {
            super.b(g);
        } else {
            for (int i = 0; i < this.items.length; ++i) {
                if (this.selectedIndex != i) {
                    this.items[i].a(g);
                } else {
                    this.items[i].a(g, this.wobbleOffsets[this.wobbleFrame], 0);
                }
            }
        }
        DrawUtils.restoreClip(g, this.savedClip);
    }
}

