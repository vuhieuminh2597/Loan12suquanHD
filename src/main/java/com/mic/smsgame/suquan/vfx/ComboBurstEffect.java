package com.mic.smsgame.suquan.vfx;

import com.mg.engine.sprite.AnimatedSprite;
import com.mg.engine.sprite.Drawable;
import com.mg.engine.ui.RisingText;
import com.mg.engine.util.MathTrig;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Expanding ring of 20 sparkling sprites accompanied by rising combo text.
 */
public class ComboBurstEffect extends Drawable {
    private static final int SPARK_COUNT = 20;
    private static final int BASE_MAX_RADIUS = 35;

    private AnimatedSprite[] ringSparks;
    private int currentRadius = 3;
    private int maxRadius = 100;
    private boolean active = true;
    private RisingText comboLabel;

    public boolean a() {
        return this.active;
    }

    public void a_(boolean active) {
        this.active = active;
    }

    public ComboBurstEffect(Image sparkSheet) {
        byte[][] frames = new byte[][]{{0, 1, 2}};
        this.ringSparks = new AnimatedSprite[SPARK_COUNT];
        for (int i = 0; i < this.ringSparks.length; ++i) {
            this.ringSparks[i] = new AnimatedSprite(sparkSheet, 3);
            this.ringSparks[i].a(frames);
        }
        this.comboLabel = new RisingText();
        this.comboLabel.a(12);
    }

    public void a(int centerX, int centerY, int comboRank, String label) {
        this.currentRadius = 3;
        this.d(centerX, centerY);
        this.active = true;
        this.comboLabel.a(label, centerX, centerY + 3);
        this.maxRadius = BASE_MAX_RADIUS + (comboRank - 1) * 5;
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (!this.active) {
            return;
        }
        int angleStep = 360 / this.ringSparks.length;
        int angleDeg = 0;
        for (int i = 0; i < this.ringSparks.length; ++i) {
            int sparkX = this.x + (this.currentRadius * MathTrig.cos(angleDeg) >> MathTrig.FIXED_SHIFT);
            int sparkY = this.y + (this.currentRadius * MathTrig.sin(angleDeg) >> MathTrig.FIXED_SHIFT);
            this.ringSparks[i].a(g, sparkX, sparkY);
            angleDeg += angleStep;
        }
        if (this.currentRadius >= 7) {
            this.comboLabel.a(g, 0, 0);
        }
    }

    public void b() {
        if (!this.active) {
            return;
        }
        for (int i = 0; i < this.ringSparks.length; ++i) {
            this.ringSparks[i].e();
        }
        this.currentRadius += 3;
        if (this.currentRadius >= this.maxRadius) {
            this.active = false;
        }
        if (this.currentRadius >= 7) {
            this.comboLabel.a();
        }
    }
}

