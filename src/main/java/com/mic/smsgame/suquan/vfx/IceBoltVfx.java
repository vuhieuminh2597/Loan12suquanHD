package com.mic.smsgame.suquan.vfx;

import com.mg.engine.core.GUIManager;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 * Ice-bolt spell projectile that arcs toward target, triggers screen shake, and shatters.
 */
public class IceBoltVfx extends SpellEffect {
    private static final int STATE_FLYING = 0;
    private static final int STATE_IMPACT = 1;

    private int startDelay;
    private int[] trailX = new int[3];
    private int[] trailY = new int[3];
    private int trailCount;

    public IceBoltVfx(Image spriteSheet, int frameCount) {
        this.a(spriteSheet, frameCount);
    }

    public void a(int startX, int startY, int targetX, int targetY, int delayTicks) {
        this.d(startX, startY);
        this.c(targetX, targetY);
        this.stepX = this.b(startX, targetX, 15) + 1;
        this.stepY = this.b(startY, targetY, 20);
        this.startDelay = delayTicks;
        this.trailCount = 0;
        this.visible = true;
        this.a_(STATE_FLYING);
        this.anchor = Graphics.HCENTER | Graphics.BOTTOM;
        this.transform = startX > targetX ? Sprite.TRANS_MIRROR : Sprite.TRANS_NONE;
    }

    public void a() {
        if (this.startDelay > 0) {
            --this.startDelay;
            return;
        }
        switch (this.animState) {
            case STATE_FLYING:
                this.trailX[2] = this.trailX[1];
                this.trailY[2] = this.trailY[1];
                this.trailX[1] = this.trailX[0];
                this.trailY[1] = this.trailY[0];
                this.trailX[0] = this.x;
                this.trailY[0] = this.y - (this.height >> 1);
                if (this.trailCount < 3) {
                    ++this.trailCount;
                }
                if (this.c(this.targetX, this.targetY, this.stepX, this.stepY)) {
                    GUIManager.getController().c(10);
                    this.trailCount = 0;
                    this.a_(STATE_IMPACT);
                    this.anchor = Graphics.HCENTER | Graphics.BOTTOM;
                } else {
                    ++this.stepY;
                }
                break;
            case STATE_IMPACT:
                if (this.frameIndex >= this.animFrames[STATE_IMPACT].length - 1) {
                    this.visible = false;
                }
                break;
        }
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (this.startDelay < 1) {
            if (this.animState == STATE_FLYING && this.trailCount > 0) {
                for (int i = this.trailCount - 1; i >= 0; --i) {
                    int size = (3 - i) << 1;
                    int tx = this.trailX[i] + offsetX - (size >> 1);
                    int ty = this.trailY[i] + offsetY - (size >> 1);
                    g.setColor(i == 0 ? 0xE6FFFF : (i == 1 ? 0x5CE0FF : 0x1A7CA8));
                    g.fillRect(tx, ty, size, size);
                }
            }
            super.a(g, offsetX, offsetY);
        }
    }
}

