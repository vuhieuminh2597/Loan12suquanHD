package com.mg.engine.graphics;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdUiRenderer;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Compact digit renderer upgraded to 100% Vector HD pill badges in HD Remaster mode. */
public class SmallNumberFont {
    private final int[] digitX = new int[]{0, 5, 10, 15, 20, 25, 30, 35, 40, 45};
    private final Image stripImage;

    public SmallNumberFont() {
        this.stripImage = MgImageLoader.load("/numbersmall");
    }

    public SmallNumberFont(Image customStrip) {
        this.stripImage = customStrip;
    }

    public void a(Graphics g, int value, int x, int y, int align) {
        if (HdConfig.isHdRemaster() && g != null && g.getHdCanvas() != null) {
            HdUiRenderer.drawHdCountBadge(g.getHdCanvas(), value, x, y, align);
            return;
        }
        int digits = String.valueOf(value).length();
        int totalWidth = digits * 6 - 1;
        if (align == 0) {
            x += totalWidth;
        } else if (align == 1) {
            x += totalWidth / 2;
        }
        x -= 5;

        int n = value;
        for (int i = 0; i < digits; ++i) {
            int digit = n % 10;
            DrawUtils.drawRegion(g, this.stripImage, this.digitX[digit], 0, 5, 7, x, y, 0);
            n /= 10;
            x -= 6;
        }
    }
}
