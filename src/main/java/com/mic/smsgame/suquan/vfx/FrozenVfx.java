package com.mic.smsgame.suquan.vfx;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Ice-prison freeze effect that forms over a target, holds until released via {@link #c()}, then shatters.
 */
public class FrozenVfx extends SpellEffect {
    private static final int STATE_FREEZE = 0;
    private static final int STATE_SHATTER = 1;

    private int startDelay;
    private boolean releaseRequested;

    public FrozenVfx(Image spriteSheet, int frameCount) {
        this.a(spriteSheet, frameCount);
        this.b(false);
        this.visible = false;
        this.anchor = Graphics.HCENTER | Graphics.VCENTER;
    }

    public void a(int startX, int startY, int targetX, int targetY, int delayTicks) {
        this.d(startX, startY);
        this.c(targetX, targetY);
        this.startDelay = delayTicks;
        this.a_(STATE_FREEZE);
        this.visible = true;
        this.looping = false;
        this.releaseRequested = false;
    }

    /** Signals that the frozen target should shatter on completion of the freeze frames. */
    public void c() {
        this.releaseRequested = true;
    }

    public void a() {
        if (this.startDelay > 0) {
            --this.startDelay;
            return;
        }
        if (this.animState == STATE_FREEZE) {
            if (this.releaseRequested && this.frameIndex >= this.animFrames[STATE_FREEZE].length - 1) {
                this.a_(STATE_SHATTER);
            }
        } else if (this.animState == STATE_SHATTER && this.frameIndex >= this.animFrames[STATE_SHATTER].length - 1) {
            this.visible = false;
        }
    }

    public void a(Graphics g) {
        if (this.startDelay <= 0) {
            super.a(g);
        }
    }
}

