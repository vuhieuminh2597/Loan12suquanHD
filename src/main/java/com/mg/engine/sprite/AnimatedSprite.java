package com.mg.engine.sprite;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Horizontal sprite-strip animated drawable with optional MIDP transform flag.
 */
public class AnimatedSprite extends FrameAnimator {
    protected int transform;
    protected Image spriteSheet;
    protected int frameCount;

    public AnimatedSprite() {
        this.transform = 0;
    }

    public AnimatedSprite(int x, int y, Image spriteSheet, int frameCount) {
        this(x, y);
        this.a(spriteSheet, frameCount);
    }

    public AnimatedSprite(Image spriteSheet, int frameCount) {
        this.transform = 0;
        this.a(spriteSheet, frameCount);
    }

    public AnimatedSprite(int x, int y) {
        this.transform = 0;
        this.d(x, y);
    }

    public void a(Image spriteSheet, int frameCount) {
        this.frameCount = frameCount;
        this.frameIndex = -1;
        if (spriteSheet != null) {
            this.width = spriteSheet.getWidth() / this.frameCount;
            this.height = spriteSheet.getHeight();
            this.spriteSheet = spriteSheet;
        }
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (!this.visible) {
            return;
        }
        if (this.spriteSheet != null
                && this.frameIndex > -1
                && this.animFrames[this.animState][this.frameIndex] > -1) {
            g.drawRegion(
                    this.spriteSheet,
                    this.animFrames[this.animState][this.frameIndex] * this.width,
                    0,
                    this.width,
                    this.height,
                    this.transform,
                    this.getRenderX() + offsetX,
                    this.getRenderY() + offsetY,
                    this.anchor);
        }
    }

    public void a() {
    }
}

