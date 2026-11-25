package com.mg.engine.ui;

import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.Rect;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Centered static image block inside a {@link RichContentDialog}.
 */
public class ImageBlock extends ContentBlock {
    protected Image image;

    public ImageBlock(Image image) {
        this.image = image;
        if (this.image != null) {
            super.a = this.image.getWidth();
            super.b = this.image.getHeight();
        }
    }

    public void a(Rect viewport) {
        super.a(viewport);
        if (super.b > viewport.d) {
            super.b = viewport.d;
        }
        if (super.a > viewport.c) {
            super.a = viewport.c;
        }
    }

    public void a(Graphics g, int x, int y) {
        DrawUtils.drawRegion(g, this.image, 0, 0, super.a, super.b, x + super.c.c / 2, y, Graphics.TOP | Graphics.HCENTER);
    }

    public void b() {
    }
}
