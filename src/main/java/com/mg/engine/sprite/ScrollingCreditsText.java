package com.mg.engine.sprite;

import com.mg.engine.graphics.FontManager;
import com.mg.engine.graphics.Rect;
import javax.microedition.lcdui.Graphics;

/**
 * Vertically scrolling centered text lines used in intro and story cutscenes.
 */
public class ScrollingCreditsText extends Drawable {
    private static final int LINE_HEIGHT = 15;
    private static final int FIXED_SHIFT = 10;
    private static final int SCROLL_SPEED_FIXED = 512; // 0.5 px per tick

    private String[] lines;
    private int topLineIndex;
    private int topYFixed;
    private int centerX;
    private int topY;

    public ScrollingCreditsText(String[] lines, Rect viewport) {
        this.x = viewport.a;
        this.y = viewport.b;
        this.width = viewport.c;
        this.height = viewport.d;
        this.lines = lines;
        this.topLineIndex = 0;
        this.topYFixed = (this.y + this.height) << FIXED_SHIFT;
        this.topY = this.topYFixed >> FIXED_SHIFT;
        this.centerX = this.x + (this.width >> 1);
    }

    public boolean a() {
        return this.topLineIndex >= this.lines.length;
    }

    public void b() {
        if (this.topLineIndex >= this.lines.length) {
            return;
        }
        this.topYFixed -= SCROLL_SPEED_FIXED;
        this.topY = this.topYFixed >> FIXED_SHIFT;
        if (this.topY + LINE_HEIGHT <= this.y) {
            ++this.topLineIndex;
            this.topYFixed += LINE_HEIGHT << FIXED_SHIFT;
            this.topY = this.topYFixed >> FIXED_SHIFT;
        }
    }

    public int c() {
        return this.topLineIndex;
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (this.topLineIndex >= this.lines.length) {
            return;
        }
        int drawY = this.topY;
        for (int i = this.topLineIndex; i < this.lines.length && drawY < this.y + this.height; ++i) {
            FontManager.secondaryFont.a(g, this.lines[i], this.centerX, drawY, 1);
            drawY += LINE_HEIGHT;
        }
    }
}

