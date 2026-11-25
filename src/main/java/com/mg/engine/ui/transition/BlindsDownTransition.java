package com.mg.engine.ui.transition;

import com.mg.engine.graphics.DrawUtils;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Top-to-bottom horizontal venetian-blinds transition. */
public class BlindsDownTransition extends BlindsTransition {
    public void init() {
        this.stripCount = this.screenHeight / STRIP_SIZE + 1;
        this.stripProgress = new int[this.stripCount];
    }

    public void render(Graphics g, Image frameBuffer) {
        int y = 0;
        for (int i = 0; i < this.stripCount && this.stripProgress[i] > 0; ++i) {
            int h = this.stripProgress[i];
            if (i == this.stripCount - 1) {
                if (y >= this.screenHeight) {
                    continue;
                }
                if (y + h > this.screenHeight) {
                    h = this.screenHeight - y;
                }
            }
            DrawUtils.drawRegion(g, frameBuffer, 0, y, this.screenWidth, h, this.viewportX, this.viewportY + y, Graphics.TOP | Graphics.LEFT);
            y += STRIP_SIZE;
        }
    }
}


