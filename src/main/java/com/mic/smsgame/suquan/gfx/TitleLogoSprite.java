package com.mic.smsgame.suquan.gfx;

import com.mg.engine.core.GUIManager;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.engine.sprite.Drawable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Drop-in bouncing "/title" logo displayed on the main menu screen.
 */
public class TitleLogoSprite extends Drawable {
    private static final int STATE_DROPPING = 0;
    private static final int STATE_BOUNCING = 1;
    private static final int[] BOUNCE_OFFSETS = new int[]{-2, -6, 0, -3, 0, -2, 0, -1, 0};

    private int bounceFrame;
    private Image logoImage = MgImageLoader.load("/title");
    private int animState;
    private int dropSpeed;
    private int restY;
    private int bounceOffsetY = 0;

    public TitleLogoSprite(int centerX, int restY) {
        this.restY = restY;
        this.d(centerX, restY - 150);
        this.dropSpeed = this.b(this.y, restY, 4);
        this.animState = STATE_DROPPING;
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        g.drawImage(this.logoImage, this.x, this.y + this.bounceOffsetY, Graphics.HCENTER | Graphics.BOTTOM);
    }

    public void a() {
        switch (this.animState) {
            case STATE_DROPPING:
                if (this.f(this.restY, this.dropSpeed)) {
                    this.animState = STATE_BOUNCING;
                    GUIManager.getInstance().a(10);
                } else {
                    ++this.dropSpeed;
                }
                break;
            case STATE_BOUNCING:
                this.bounceOffsetY = BOUNCE_OFFSETS[this.bounceFrame];
                ++this.bounceFrame;
                if (this.bounceFrame >= BOUNCE_OFFSETS.length) {
                    this.visible = false;
                }
                break;
        }
    }
}

