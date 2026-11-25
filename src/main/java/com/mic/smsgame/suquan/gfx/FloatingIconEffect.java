package com.mic.smsgame.suquan.gfx;

import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.sprite.Drawable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Rising and blinking status/reward icon popup drawn from a 6-icon horizontal strip.
 */
public class FloatingIconEffect extends Drawable {
    private static final int ICON_COUNT = 6;

    private Image iconStrip;
    private int elapsedTicks;
    private int iconIndex;
    private int startDelay;

    public FloatingIconEffect(Image iconStrip) {
        this.visible = false;
        this.iconStrip = iconStrip;
        this.width = iconStrip.getWidth() / ICON_COUNT;
        this.height = iconStrip.getHeight();
    }

    public void a(int iconIndex, int x, int y) {
        this.x = x;
        this.y = y;
        this.visible = true;
        this.elapsedTicks = 0;
        this.iconIndex = iconIndex;
        this.startDelay = 5;
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (!this.visible || this.startDelay > 0) {
            return;
        }
        if (this.elapsedTicks > 15) {
            if (this.elapsedTicks % 2 == 0) {
                DrawUtils.drawRegion(
                        g,
                        this.iconStrip,
                        this.iconIndex * this.width,
                        0,
                        this.width,
                        this.height,
                        this.x,
                        this.y - 7,
                        Graphics.HCENTER | Graphics.VCENTER);
            }
        } else {
            int subRise = ((this.elapsedTicks << 8) + (GameConfig.highFpsEnabled ? GameConfig.renderSubTick256 : 0)) >> 9;
            DrawUtils.drawRegion(
                    g,
                    this.iconStrip,
                    this.iconIndex * this.width,
                    0,
                    this.width,
                    this.height,
                    this.x,
                    this.y - subRise,
                    Graphics.HCENTER | Graphics.VCENTER);
        }
    }

    public void a() {
        if (!this.visible) {
            return;
        }
        if (this.startDelay > 0) {
            --this.startDelay;
            return;
        }
        ++this.elapsedTicks;
        if (this.elapsedTicks > 24) {
            this.visible = false;
        }
    }
}

