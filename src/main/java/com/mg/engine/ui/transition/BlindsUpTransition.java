package com.mg.engine.ui.transition;

import com.mg.engine.graphics.DrawUtils;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Bottom-to-top horizontal venetian-blinds transition. */
public class BlindsUpTransition extends BlindsTransition {
    public void init() {
        this.stripCount = this.screenHeight / STRIP_SIZE + 1;
        this.stripProgress = new int[this.stripCount];
    }

    public void render(Graphics g, Image frameBuffer) {
        int bottomY = this.screenHeight;
        for (int i = 0; i < this.stripCount && this.stripProgress[i] > 0; ++i) {
            int h = this.stripProgress[i];
            int y = bottomY - h;
            if (i == this.stripCount - 1) {
                if (y + h <= 0) {
                    continue;
                }
                if (y < 0) {
                    h += y;
                    y = 0;
                }
            }
            DrawUtils.drawRegion(g, frameBuffer, 0, y, this.screenWidth, h, this.viewportX, this.viewportY + y, Graphics.TOP | Graphics.LEFT);
            bottomY -= STRIP_SIZE;
        }
    }
}


