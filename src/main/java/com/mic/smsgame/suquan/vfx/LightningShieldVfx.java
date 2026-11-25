package com.mic.smsgame.suquan.vfx;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Looping lightning shield aura with a triggered flash state.
 */
public class LightningShieldVfx extends SpellEffect {
    private static final int STATE_IDLE = 0;
    private static final int STATE_FLASH = 1;

    private int durationTicks;
    private int flashDelayTicks;

    public LightningShieldVfx(Image spriteSheet, int frameCount) {
        this.a(spriteSheet, frameCount);
        this.anchor = Graphics.HCENTER | Graphics.VCENTER;
    }

    public void a(int x, int y, int targetX, int targetY, int unused) {
        this.d(x, y);
        this.visible = true;
        this.durationTicks = 0;
        this.flashDelayTicks = 0;
        this.a_(STATE_IDLE);
        this.frameIndex = 0;
    }

    public void a_(int state) {
        super.a_(state);
        if (state == STATE_IDLE) {
            this.looping = true;
        } else {
            this.looping = false;
            this.frameIndex = 0;
        }
    }

    public void d(int durationTicks) {
        this.durationTicks = durationTicks;
    }

    public void a() {
        if (this.durationTicks > 0) {
            --this.durationTicks;
            if (this.durationTicks == 0) {
                this.visible = false;
            }
        }

        if (this.flashDelayTicks > 0) {
            --this.flashDelayTicks;
            if (this.flashDelayTicks == 0) {
                this.a_(STATE_FLASH);
            }
        }

        if (this.animState == STATE_FLASH && this.frameIndex >= this.animFrames[STATE_FLASH].length - 1) {
            this.a_(STATE_IDLE);
        }
    }

    public void e(int flashDelayTicks) {
        this.flashDelayTicks = flashDelayTicks;
    }
}

