package com.mg.engine.sprite;

import javax.microedition.lcdui.Graphics;

/**
 * Invisible 30x30 layout anchor placeholder used for positioning battle HUD portraits.
 */
public class DummyDrawable extends Drawable {
    public DummyDrawable() {
        this.width = 30;
        this.height = 30;
    }

    public void a(Graphics g, int offsetX, int offsetY) {
    }
}

