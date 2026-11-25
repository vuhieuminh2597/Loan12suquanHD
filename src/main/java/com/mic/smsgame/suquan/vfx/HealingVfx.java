package com.mic.smsgame.suquan.vfx;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Healing aura effect that drifts gently toward a target point while playing its animation.
 */
public class HealingVfx extends SpellEffect {
    public HealingVfx(Image spriteSheet, int frameCount) {
        this.a(spriteSheet, frameCount);
        this.b(false);
        this.visible = true;
        this.anchor = Graphics.HCENTER | Graphics.VCENTER;
    }

    public void a(int startX, int startY, int targetX, int targetY, int unused) {
        this.d(startX, startY);
        this.c(targetX, targetY);
        this.visible = true;
        this.stepX = this.b(startX, targetX, 20);
        this.stepY = this.b(startY, targetY, 20);
        this.frameIndex = 0;
    }

    public void a() {
        this.c(this.targetX, this.targetY, this.stepX, this.stepY);
        if (this.frameIndex >= this.animFrames[0].length - 1) {
            this.visible = false;
        }
    }
}

