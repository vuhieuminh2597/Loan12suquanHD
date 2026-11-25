package com.mic.smsgame.suquan.gfx;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdUiRenderer;
import com.mg.engine.graphics.BattleResourceManager;
import com.mg.engine.sprite.Drawable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 * Pulsing 4-way directional hint arrows displayed around a suggested match-3 tile.
 */
public class DirectionHintArrows extends Drawable {
    public static final int DIR_LEFT = 1;
    public static final int DIR_RIGHT = 2;
    public static final int DIR_UP = 4;
    public static final int DIR_DOWN = 8;

    private Image arrowImage;
    private int activeTimer;
    private int directionMask;
    private int pulseTick;

    public DirectionHintArrows() {
        this.arrowImage = BattleResourceManager.a().j[0];
    }

    public void a(int centerX, int centerY, int directionMask) {
        this.d(centerX, centerY);
        this.activeTimer = -1;
        this.directionMask = directionMask;
    }

    public void a() {
        this.activeTimer = 0;
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (this.activeTimer == 0) {
            return;
        }
        if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
            com.loan12suquan.hd.platform.HdCanvas2D g2 = g.getHdCanvas();
            int offset = this.pulseTick >= 3 ? 15 : 13;
            if ((this.directionMask & DIR_LEFT) == DIR_LEFT) {
                HdUiRenderer.drawHdDirectionalChevron(g2, this.x - offset, this.y, 2, 0x7CFF9E, false, false);
            }
            if ((this.directionMask & DIR_RIGHT) == DIR_RIGHT) {
                HdUiRenderer.drawHdDirectionalChevron(g2, this.x + offset, this.y, 3, 0x7CFF9E, false, false);
            }
            if ((this.directionMask & DIR_UP) == DIR_UP) {
                HdUiRenderer.drawHdDirectionalChevron(g2, this.x, this.y - offset, 0, 0x7CFF9E, false, false);
            }
            if ((this.directionMask & DIR_DOWN) == DIR_DOWN) {
                HdUiRenderer.drawHdDirectionalChevron(g2, this.x, this.y + offset, 1, 0x7CFF9E, false, false);
            }
            return;
        }
        int offset = this.pulseTick >= 3 ? 9 : 8;
        int w = this.arrowImage.getWidth();
        int h = this.arrowImage.getHeight();
        if ((this.directionMask & DIR_LEFT) == DIR_LEFT) {
            g.drawRegion(this.arrowImage, 0, 0, w, h, Sprite.TRANS_ROT270, this.x - offset, this.y, Graphics.RIGHT | Graphics.VCENTER);
        }
        if ((this.directionMask & DIR_RIGHT) == DIR_RIGHT) {
            g.drawRegion(this.arrowImage, 0, 0, w, h, Sprite.TRANS_ROT90, this.x + offset, this.y, Graphics.LEFT | Graphics.VCENTER);
        }
        if ((this.directionMask & DIR_UP) == DIR_UP) {
            g.drawRegion(this.arrowImage, 0, 0, w, h, Sprite.TRANS_NONE, this.x, this.y - offset, Graphics.HCENTER | Graphics.BOTTOM);
        }
        if ((this.directionMask & DIR_DOWN) == DIR_DOWN) {
            g.drawRegion(this.arrowImage, 0, 0, w, h, Sprite.TRANS_ROT180, this.x, this.y + offset, Graphics.HCENTER | Graphics.TOP);
        }
    }

    public void b() {
        if (this.activeTimer == 0) {
            return;
        }
        if (this.activeTimer > 0) {
            --this.activeTimer;
        }
        ++this.pulseTick;
        if (this.pulseTick > 6) {
            this.pulseTick = 0;
        }
    }
}

