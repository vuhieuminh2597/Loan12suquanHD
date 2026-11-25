package com.mg.engine.graphics;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Compact digit-only sprite font loaded from {@code /f/nb} for battle counters and gold/level numbers. */
public class NumberSpriteFont {
    private final int[] digitX = new int[]{0, 7, 11, 18, 25, 33, 40, 47, 54, 61};
    private final int[] digitWidth = new int[]{7, 4, 7, 7, 8, 7, 7, 7, 7, 7};
    private final Image stripImage = MgImageLoader.load("/f/nb");
    private final int digitHeight;

    public NumberSpriteFont() {
        this.digitHeight = this.stripImage.getHeight();
    }

    public int a(int value) {
        int totalWidth = 0;
        int n = value;
        do {
            int digit = n % 10;
            n /= 10;
            totalWidth += this.digitWidth[digit] + 1;
        } while (n > 0);
        return totalWidth;
    }

    public void a(Graphics g, int value, int x, int y) {
        int drawX = x + this.a(value);
        int n = value;
        do {
            int digit = n % 10;
            n /= 10;
            drawX -= this.digitWidth[digit] + 1;
            DrawUtils.drawRegion(g, this.stripImage, this.digitX[digit], 0, this.digitWidth[digit], this.digitHeight, drawX, y, 0);
        } while (n > 0);
    }
}



