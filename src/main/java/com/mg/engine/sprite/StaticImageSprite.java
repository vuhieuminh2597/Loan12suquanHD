package com.mg.engine.sprite;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Simple static {@link Image} drawable.
 */
public class StaticImageSprite extends Drawable {
    private Image image;
    private int yOffset;
    private int drawAnchor;

    public StaticImageSprite(Image image, int x, int y) {
        this.x = x;
        this.y = y;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.image = image;
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        g.drawImage(this.image, this.x, this.y + this.yOffset, this.drawAnchor);
    }

    public void a() {
    }
}

