package com.mic.smsgame.suquan.vfx;

import com.mg.engine.sprite.AnimatedSprite;
import com.mg.engine.util.RandomUtils;

/**
 * Single particle belonging to {@link SparkStreamEffect}.
 */
public class SparkStreamParticle extends AnimatedSprite {
    public void b(int startX, int startY, int targetX, int targetY) {
        this.d(startX, startY);
        this.targetX = targetX;
        this.targetY = targetY;
        this.visible = true;
        this.stepX = this.b(this.x, targetX, 25) + RandomUtils.nextInt(3);
        this.stepY = this.b(this.y, targetY, 25) + RandomUtils.nextInt(3);
    }

    public void a() {
        if (this.c(this.targetX, this.targetY, this.stepX, this.stepY)) {
            this.visible = false;
        }
    }
}

