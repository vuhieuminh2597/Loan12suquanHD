package com.mg.engine.ui;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdUiRenderer;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.sprite.Drawable;
import javax.microedition.lcdui.Graphics;

/** Themed 17-pixel bottom bar behind left/center/right {@link SoftKeyButton}s. */
public class SoftKeyBar extends Drawable {
    private int fillColor;
    private int borderColor;

    public SoftKeyBar() {
        this(GameConfig.frameBackgroundColor);
    }

    public SoftKeyBar(int baseColor) {
        this.a(baseColor);
    }

    public void a(int rgb) {
        this.fillColor = rgb & 0xFFFFFF;
        int r = (rgb & 0xFF0000) >> 16;
        int g = (rgb & 0x00FF00) >> 8;
        int b = rgb & 0x0000FF;
        int blend = 6;
        this.borderColor = ((255 - r) / blend + r) << 16
                | ((255 - g) / blend + g) << 8
                | ((255 - b) / blend + b);
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (HdConfig.isHdRemaster() && g != null && g.getHdCanvas() != null) {
            HdUiRenderer.drawHdSoftKeyBar(g.getHdCanvas(), GameConfig.screenWidth, GameConfig.screenHeight);
            return;
        }
        int barY = GameConfig.screenHeight - 23;
        int barW = GameConfig.screenWidth;
        // Carved antique mahogany-and-bronze bottom dock bar
        g.setColor(0x100804);
        g.fillRect(0, barY, barW, 23);
        g.setColor(0x28180E);
        g.fillRect(0, barY + 2, barW, 8);
        g.setColor(0x1B1009);
        g.fillRect(0, barY + 10, barW, 13);
        g.setColor(0x7A592A);
        g.drawLine(0, barY, barW - 1, barY);
        g.setColor(0xCCA052);
        g.drawLine(16, barY + 1, barW - 17, barY + 1);
    }
}


