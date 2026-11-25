package com.mic.smsgame.suquan.gfx;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdUiRenderer;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 * Renders the ornate wooden/gold scroll dialog frame used across SuQuan dialogs and panels.
 */
public class ScrollFrameRenderer {
    public static final int ANTIQUE_SCROLL_BG = 0x2B1B10;

    public static void a(Graphics g, int x, int y, int height, int fillColor, Image[] frameImages) {
        int width = frameImages[0].getWidth();
        int bodyColor = (fillColor == 0x773CF5) ? ANTIQUE_SCROLL_BG : fillColor;

        if (HdConfig.isHdRemaster() && g != null && g.getHdCanvas() != null) {
            HdUiRenderer.drawHdScrollFrame(g.getHdCanvas(), x, y, width, height, bodyColor);
            return;
        }

        g.setColor(bodyColor);
        g.fillRect(x + 9, y + 9, width - 18, height - 18);
        if (bodyColor == ANTIQUE_SCROLL_BG) {
            g.setColor(0x3A2516);
            g.fillRect(x + 9, y + 9, width - 18, 8);
            g.setColor(0x1E120A);
            g.fillRect(x + 9, y + height - 17, width - 18, 8);
        }

        g.setColor(0x160D08);
        g.drawRect(x + 8, y + 8, width - 17, height - 17);
        g.setColor(0xE6B85C);
        g.drawRect(x + 7, y + 7, width - 15, height - 15);
        g.setColor(0x5E1F1A);
        g.drawRect(x + 6, y + 6, width - 13, height - 13);
        g.setColor(0xA67530);
        g.drawRect(x + 5, y + 5, width - 11, height - 11);
        g.setColor(0x120A06);
        g.drawRect(x + 4, y + 4, width - 9, height - 9);

        g.drawImage(frameImages[0], x, y, Graphics.TOP | Graphics.LEFT);
        g.drawRegion(
                frameImages[0],
                0,
                0,
                frameImages[0].getWidth(),
                frameImages[0].getHeight(),
                Sprite.TRANS_MIRROR_ROT180,
                x,
                y + height,
                Graphics.BOTTOM | Graphics.LEFT);
        if (frameImages.length > 1) {
            g.drawImage(frameImages[1], x + 1, y + 23, Graphics.TOP | Graphics.HCENTER);
            g.drawImage(frameImages[1], x + width - 2, y + 23, Graphics.TOP | Graphics.HCENTER);
        }
    }
}
