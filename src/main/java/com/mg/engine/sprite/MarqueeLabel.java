package com.mg.engine.sprite;

import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.GameFont;
import com.mg.engine.graphics.Rect;
import javax.microedition.lcdui.Graphics;

/**
 * Clipped text label that bounces horizontally when text exceeds its width.
 */
public class MarqueeLabel extends Drawable {
    private String text;
    private int textWidth;
    private int scrollOffsetX;
    private GameFont font;
    private boolean scrollable;
    private boolean scrollingLeft;

    public MarqueeLabel(String text, int alignment, GameFont font) {
        this.font = font;
        this.a(text, alignment);
        this.a(0, 0, font.a(text), font.a());
    }

    private void resetScroll() {
        if (this.anchor == 0) {
            this.scrollOffsetX = 0;
            this.scrollingLeft = true;
        } else {
            this.scrollOffsetX = this.width - this.textWidth;
            this.scrollingLeft = false;
        }
        this.scrollable = this.width < this.textWidth + 2;
    }

    public void a(String text, int alignment) {
        this.text = text;
        this.j(alignment);
        this.textWidth = this.font.a(text);
        this.resetScroll();
    }

    public void a(int x, int y, int width, int height) {
        super.a(x, y, width, this.font.a());
        this.resetScroll();
    }

    public void a() {
        if (!this.scrollable) {
            return;
        }
        if (this.scrollingLeft) {
            --this.scrollOffsetX;
            if (this.scrollOffsetX + this.textWidth <= this.width) {
                this.scrollingLeft = false;
            }
        } else {
            ++this.scrollOffsetX;
            if (this.scrollOffsetX >= 0) {
                this.scrollingLeft = true;
            }
        }
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        Rect savedClip = new Rect();
        DrawUtils.saveClip(g, savedClip);
        DrawUtils.intersectClip(g, offsetX + this.x, offsetY + this.y, this.width, this.height);
        this.font.a(g, this.text, offsetX + this.scrollOffsetX + this.x, offsetY + this.y, 0);
        DrawUtils.restoreClip(g, savedClip);
    }
}

