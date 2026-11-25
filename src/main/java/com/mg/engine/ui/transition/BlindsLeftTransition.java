package com.mg.engine.ui.transition;

import com.mg.engine.graphics.DrawUtils;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Right-to-left vertical venetian-blinds transition. */
public class BlindsLeftTransition extends BlindsTransition {
    public void init() {
        this.stripCount = this.screenWidth / STRIP_SIZE + 1;
        this.stripProgress = new int[this.stripCount];
    }

    public void render(Graphics g, Image frameBuffer) {
        int rightX = this.screenWidth;
        for (int i = 0; i < this.stripCount && this.stripProgress[i] > 0; ++i) {
            int w = this.stripProgress[i];
            int x = rightX - w;
            if (i == this.stripCount - 1) {
                if (x + w <= 0) {
                    continue;
                }
                if (x < 0) {
                    w += x;
                    x = 0;
                }
            }
            DrawUtils.drawRegion(g, frameBuffer, x, 0, w, this.screenHeight, this.viewportX + x, this.viewportY, Graphics.TOP | Graphics.LEFT);
            rightX -= STRIP_SIZE;
        }
    }
}


