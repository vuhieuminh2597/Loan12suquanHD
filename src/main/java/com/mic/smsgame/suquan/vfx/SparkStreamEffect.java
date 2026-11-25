package com.mic.smsgame.suquan.vfx;

import com.mg.engine.sprite.Drawable;
import com.mg.engine.util.RandomUtils;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Cluster of 5 animated spark particles flying from matched gems to a HUD bar or portrait.
 */
public class SparkStreamEffect extends Drawable {
    private static final int PARTICLE_COUNT = 5;
    private static final byte[][] SPARK_FRAMES = new byte[][]{{0, 0, 0, 0, 1, 1, 1, 2, 2, 2}};

    private SparkStreamParticle[] particles = new SparkStreamParticle[PARTICLE_COUNT];

    public SparkStreamEffect() {
        for (int i = 0; i < this.particles.length; ++i) {
            this.particles[i] = new SparkStreamParticle();
            this.particles[i].a(SPARK_FRAMES);
        }
    }

    public void a(int startX, int startY, int targetX, int targetY, Image sparkSheet) {
        for (int i = 0; i < this.particles.length; ++i) {
            this.particles[i].a(sparkSheet, 3);
            this.particles[i].b(RandomUtils.nextInt(SPARK_FRAMES[0].length));
            this.particles[i].b(
                    startX + RandomUtils.nextInt() % 13,
                    startY + RandomUtils.nextInt() % 13,
                    targetX + RandomUtils.nextInt() % 20,
                    targetY + RandomUtils.nextInt() % 3);
        }
        this.visible = true;
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (!this.visible) {
            return;
        }
        for (int i = this.particles.length - 1; i >= 0; --i) {
            this.particles[i].a(g);
        }
    }

    public void a() {
        if (!this.visible) {
            return;
        }
        int finishedCount = 0;
        for (int i = this.particles.length - 1; i >= 0; --i) {
            if (this.particles[i].m()) {
                this.particles[i].e();
            } else {
                ++finishedCount;
            }
        }
        if (finishedCount >= this.particles.length) {
            this.visible = false;
        }
    }
}

