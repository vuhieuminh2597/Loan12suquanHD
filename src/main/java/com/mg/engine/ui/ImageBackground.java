package com.mg.engine.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Static image {@link ScreenBackground} rendered at a fixed position and anchor. */
public class ImageBackground extends ScreenBackground {
    private final Image image;
    private final int x;
    private final int y;
    private final int anchor;

    public ImageBackground(Image image, int x, int y, int anchor) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.anchor = anchor;
    }

    public void a(Graphics g) {
        if (this.image != null) {
            g.drawImage(this.image, this.x, this.y, this.anchor);
        }
    }

    public void a() {
    }
}


