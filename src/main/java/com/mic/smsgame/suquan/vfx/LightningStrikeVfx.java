package com.mic.smsgame.suquan.vfx;

import com.mg.engine.graphics.DrawUtils;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Vertical thunderbolt blast rendered from variable-height sprite atlas rects.
 */
public class LightningStrikeVfx extends SpellEffect {
    private static final int[] FRAME_SRC_Y = new int[]{0, 17, 51, 68, 131};
    private static final int[] FRAME_HEIGHT = new int[]{17, 34, 17, 63, 62};
    private static final int[] FRAME_SRC_X = new int[]{19, 20, 17, 12, 0};
    private static final int[] FRAME_WIDTH = new int[]{12, 11, 4, 27, 56};
    private static final int[] FRAME_OFFSET_Y = new int[]{91, 74, 56, 1, 0};

    private int startDelay;

    public LightningStrikeVfx(Image spriteSheet, int unusedFrameCount) {
        this.spriteSheet = spriteSheet;
    }

    public void a(int startX, int startY, int targetX, int targetY, int delayTicks) {
        this.d(startX, startY);
        this.c(targetX, targetY);
        this.stepX = this.b(startX, targetX, 35);
        this.stepY = this.b(startY, targetY, 35);
        this.startDelay = delayTicks;
        this.visible = true;
        this.anchor = Graphics.HCENTER | Graphics.BOTTOM;
        this.frameIndex = 0;
    }

    public void a() {
        if (this.startDelay > 0) {
            --this.startDelay;
        } else if (this.frameIndex >= this.animFrames[0].length - 1) {
            this.visible = false;
        }
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (this.startDelay >= 1 || !this.visible) {
            return;
        }
        byte frame = this.animFrames[this.animState][this.frameIndex];
        if (this.spriteSheet != null && this.frameIndex > -1 && frame > -1) {
            DrawUtils.drawRegion(
                    g,
                    this.spriteSheet,
                    FRAME_SRC_X[frame],
                    FRAME_SRC_Y[frame],
                    FRAME_WIDTH[frame],
                    FRAME_HEIGHT[frame],
                    this.x + offsetX,
                    this.y - FRAME_OFFSET_Y[frame] + offsetY,
                    this.anchor);
        }
    }
}

