package com.mic.smsgame.suquan.gfx;

import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.engine.sprite.Drawable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 * World-map battle encounter animation showing two crossed swords clashing
 * followed by an 8-way crystal spark burst.
 */
public class SwordClashEffect extends Drawable {
    private Image swordImage = MgImageLoader.load("/m/fsw");
    private Image crystalSheet = MgImageLoader.load("/crystalblue");
    private int tickCounter;
    private int crystalFrameWidth;
    private int crystalFrameHeight;
    private int crystalFrameIndex;
    private int swordSpread;
    private int burstRadius;
    private boolean finished;
    private boolean burstPhase;

    public SwordClashEffect() {
        this.crystalFrameWidth = this.crystalSheet.getWidth() / 3;
        this.crystalFrameHeight = this.crystalSheet.getHeight();
    }

    public void a() {
        this.swordSpread = 15;
        this.burstRadius = 0;
        this.burstPhase = false;
        this.finished = false;
        this.tickCounter = 0;
    }

    public void b() {
        if (this.finished) {
            return;
        }
        ++this.tickCounter;
        if (this.tickCounter % 2 == 0) {
            return;
        }
        if (this.swordSpread > 0) {
            this.swordSpread -= 3;
        }
        if (!this.burstPhase) {
            ++this.crystalFrameIndex;
            if (this.crystalFrameIndex >= 3) {
                this.crystalFrameIndex = 3;
                this.burstPhase = true;
            }
        } else {
            this.burstRadius += 5;
            --this.crystalFrameIndex;
            if (this.crystalFrameIndex < 0) {
                this.crystalFrameIndex = 0;
                this.finished = true;
            }
        }
    }

    public boolean c() {
        return this.finished;
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (this.finished) {
            return;
        }
        int cx = offsetX + this.x;
        int cy = offsetY + this.y;
        int center = Graphics.HCENTER | Graphics.VCENTER;
        g.drawImage(this.swordImage, cx + this.swordSpread, cy + this.swordSpread, center);
        g.drawRegion(this.swordImage, 0, 0, 24, 24, Sprite.TRANS_MIRROR, cx - this.swordSpread, cy + this.swordSpread, center);

        int srcX = this.crystalFrameWidth * this.crystalFrameIndex;
        if (!this.burstPhase) {
            DrawUtils.drawRegion(g, this.crystalSheet, srcX, 0, this.crystalFrameWidth, this.crystalFrameHeight, cx, cy, center);
        } else {
            int r = this.burstRadius;
            DrawUtils.drawRegion(g, this.crystalSheet, srcX, 0, this.crystalFrameWidth, this.crystalFrameHeight, cx + r, cy, center);
            DrawUtils.drawRegion(g, this.crystalSheet, srcX, 0, this.crystalFrameWidth, this.crystalFrameHeight, cx, cy + r, center);
            DrawUtils.drawRegion(g, this.crystalSheet, srcX, 0, this.crystalFrameWidth, this.crystalFrameHeight, cx - r, cy, center);
            DrawUtils.drawRegion(g, this.crystalSheet, srcX, 0, this.crystalFrameWidth, this.crystalFrameHeight, cx, cy - r, center);
            DrawUtils.drawRegion(g, this.crystalSheet, srcX, 0, this.crystalFrameWidth, this.crystalFrameHeight, cx + r, cy + r, center);
            DrawUtils.drawRegion(g, this.crystalSheet, srcX, 0, this.crystalFrameWidth, this.crystalFrameHeight, cx + r, cy - r, center);
            DrawUtils.drawRegion(g, this.crystalSheet, srcX, 0, this.crystalFrameWidth, this.crystalFrameHeight, cx - r, cy + r, center);
            DrawUtils.drawRegion(g, this.crystalSheet, srcX, 0, this.crystalFrameWidth, this.crystalFrameHeight, cx - r, cy - r, center);
        }
    }
}

