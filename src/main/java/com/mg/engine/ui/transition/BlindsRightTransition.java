package com.mg.engine.ui.transition;

import com.mg.engine.graphics.DrawUtils;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Left-to-right vertical venetian-blinds transition. */
public class BlindsRightTransition extends BlindsTransition {
    public void init() {
        this.stripCount = this.screenWidth / STRIP_SIZE + 1;
        this.stripProgress = new int[this.stripCount];
    }

    public void render(Graphics g, Image frameBuffer) {
        int x = 0;
        for (int i = 0; i < this.stripCount && this.stripProgress[i] > 0; ++i) {
            int w = this.stripProgress[i];
            if (i == this.stripCount - 1) {
                if (x >= this.screenWidth) {
                    continue;
                }
                if (x + w > this.screenWidth) {
                    w = this.screenWidth - x;
                }
            }
            DrawUtils.drawRegion(g, frameBuffer, x, 0, w, this.screenHeight, this.viewportX + x, this.viewportY, Graphics.TOP | Graphics.LEFT);
            x += STRIP_SIZE;
        }
    }
}


