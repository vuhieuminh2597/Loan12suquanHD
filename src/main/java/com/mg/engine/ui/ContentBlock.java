package com.mg.engine.ui;

import com.mg.engine.graphics.Rect;
import javax.microedition.lcdui.Graphics;

/**
 * Abstract layout block rendered inside a {@link RichContentDialog}.
 */
public abstract class ContentBlock {
    protected int a;
    protected int b;
    protected Rect c;

    public int a() {
        return this.b;
    }

    public void a(Rect viewport) {
        this.c = viewport;
    }

    public abstract void a(Graphics g, int x, int y);

    public abstract void b();
}
