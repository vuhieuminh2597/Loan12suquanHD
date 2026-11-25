package com.mg.engine.ui;

import com.mg.engine.sprite.AnimatedSprite;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Centered animated sprite block inside a {@link RichContentDialog}.
 */
public class AnimatedSpriteBlock extends ContentBlock {
    private AnimatedSprite sprite;

    public AnimatedSpriteBlock(Image sheet, int frameCount) {
        this.sprite = new AnimatedSprite(0, 0, sheet, frameCount);
    }

    public void a(byte[][] animFrames) {
        if (this.sprite != null) {
            this.sprite.a(animFrames);
        }
    }

    public void a(int frameDelay) {
        if (this.sprite != null) {
            this.sprite.c(frameDelay);
        }
    }

    public void a(boolean looping) {
        if (this.sprite != null) {
            this.sprite.b(looping);
        }
    }

    public void b() {
        if (this.sprite != null) {
            this.sprite.e();
        }
    }

    public void b(int state) {
        if (this.sprite != null) {
            this.sprite.a_(state);
        }
    }

    public int a() {
        return this.sprite.l();
    }

    public void a(Graphics g, int x, int y) {
        if (this.sprite != null) {
            this.sprite.a(g, x + (super.c.c - this.sprite.k()) / 2, y);
        }
    }
}
