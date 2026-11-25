package com.mg.engine.sprite;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Drawable backed by an array of state images that cycles when toggled.
 */
public class ToggleImageSprite extends Drawable {
    protected Image[] images;
    protected int currentIndex;

    public ToggleImageSprite(Image[] images) {
        this.images = images;
        if (images != null) {
            this.width = images[0].getWidth();
            this.height = images[0].getHeight();
        }
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        g.drawImage(this.images[this.currentIndex], offsetX + this.getRenderX(), offsetY + this.getRenderY(), this.anchor);
    }

    public void a(boolean selected) {
        super.a(selected);
        this.currentIndex = (this.currentIndex + 1) % this.images.length;
    }
}

