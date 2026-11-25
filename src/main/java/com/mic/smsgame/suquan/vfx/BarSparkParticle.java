package com.mic.smsgame.suquan.vfx;

import com.mg.engine.sprite.AnimatedSprite;
import com.mg.engine.util.RandomUtils;
import javax.microedition.lcdui.Graphics;

/**
 * Two-stage burst-and-fall spark particle emitted from HP/mana HUD bars on hit or drain.
 */
public class BarSparkParticle extends AnimatedSprite {
    private boolean fallingPhase = false;
    private int startDelay;

    public void a(int startX, int startY, int peakX, int peakY, int delayTicks) {
        this.d(startX, startY);
        this.targetX = peakX;
        this.targetY = peakY;
        this.visible = true;
        this.stepX = this.b(this.x, peakX, 12) + RandomUtils.nextInt(3);
        this.stepY = this.b(this.y, peakY, 12) + RandomUtils.nextInt(3);
        this.fallingPhase = false;
        this.startDelay = delayTicks;
    }

    public void a() {
        if (this.startDelay > 0) {
            --this.startDelay;
            return;
        }
        if (this.fallingPhase) {
            ++this.stepY;
            ++this.stepX;
        } else {
            this.stepX = this.b(this.x, this.targetX, 4) + RandomUtils.nextInt(2);
            this.stepY = this.b(this.y, this.targetY, 8) + RandomUtils.nextInt(2);
        }

        if (this.c(this.targetX, this.targetY, this.stepX, this.stepY)) {
            if (this.fallingPhase) {
                this.visible = false;
            } else {
                this.targetX = this.x + RandomUtils.nextInt(20);
                this.targetY = this.y + 40 + RandomUtils.nextInt(100);
                this.stepX = this.b(this.x, this.targetX, 14) + RandomUtils.nextInt(2);
                this.stepY = this.b(this.y, this.targetY, 10) + RandomUtils.nextInt(2);
                this.fallingPhase = true;
                this.c(this.targetX, this.targetY, this.stepX, this.stepY);
            }
        }
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (this.startDelay <= 0) {
            super.a(g, offsetX, offsetY);
        }
    }
}

