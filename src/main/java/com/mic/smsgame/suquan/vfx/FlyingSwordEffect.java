package com.mic.smsgame.suquan.vfx;

import com.mg.engine.core.GameConfig;
import com.mg.engine.sprite.Drawable;
import com.mg.engine.util.RandomUtils;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 * Sword projectile launched from matched sword gems that curves back slightly before lunging at the enemy hero.
 */
public class FlyingSwordEffect extends Drawable {
    private int destX;
    private int destY;
    private int apexX;
    private int apexY;
    private boolean divingAtTarget = false;
    private Image spriteSheet;
    private int stepX;
    private int stepY;
    private int frameIndex;
    private int transform;
    private int[] trailX = new int[3];
    private int[] trailY = new int[3];
    private int trailCount;

    public FlyingSwordEffect(Image spriteSheet) {
        this.spriteSheet = spriteSheet;
        this.width = spriteSheet.getWidth() / 3;
        this.height = spriteSheet.getHeight();
    }

    public void b(int startX, int startY, int targetX, int targetY) {
        this.d(startX, startY);
        this.trailCount = 0;
        this.destX = targetX;
        this.destY = targetY;
        this.apexX = startX;
        this.apexY = startY;

        int arcOffset = 20 + RandomUtils.nextInt(40);
        int dx = Math.abs(startX - targetX);
        int dy = Math.abs(startY - targetY);
        if (dx > dy) {
            this.apexY -= (dx - dy) + arcOffset;
            this.apexX += targetX < GameConfig.screenWidth / 2 ? arcOffset : -arcOffset;
        } else {
            this.apexY -= arcOffset;
            arcOffset += dy - dx;
            this.apexX += targetX < GameConfig.screenWidth / 2 ? arcOffset : -arcOffset;
        }

        this.transform = targetX < GameConfig.screenWidth / 2 ? Sprite.TRANS_MIRROR : Sprite.TRANS_NONE;
        this.frameIndex = 0;
        this.divingAtTarget = false;
        this.visible = true;
        this.stepX = this.b(this.x, this.apexX, 14) + RandomUtils.nextInt(4);
        this.stepY = this.b(this.y, this.apexY, 14) + RandomUtils.nextInt(4);
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (this.visible) {
            int rx = this.getRenderX();
            int ry = this.getRenderY();
            if (this.trailCount > 0) {
                int prevTx = rx;
                int prevTy = ry;
                for (int i = 0; i < this.trailCount; ++i) {
                    int tx = this.trailX[i];
                    int ty = this.trailY[i];
                    g.setColor(i == 0 ? 0xFFF2A6 : (i == 1 ? 0xD4A84A : 0x7A5820));
                    g.drawLine(prevTx, prevTy, tx, ty);
                    if (i == 0) {
                        g.setColor(0xFFFFFF);
                        g.fillRect(tx - 1, ty - 1, 2, 2);
                    }
                    prevTx = tx;
                    prevTy = ty;
                }
            }
            g.drawRegion(
                    this.spriteSheet,
                    this.frameIndex * this.width,
                    0,
                    this.width,
                    this.height,
                    this.transform,
                    rx,
                    ry,
                    Graphics.HCENTER | Graphics.VCENTER);
        }
    }

    public void a() {
        if (!this.visible) {
            return;
        }
        this.trailX[2] = this.trailX[1];
        this.trailY[2] = this.trailY[1];
        this.trailX[1] = this.trailX[0];
        this.trailY[1] = this.trailY[0];
        this.trailX[0] = this.x;
        this.trailY[0] = this.y;
        if (this.trailCount < 3) {
            ++this.trailCount;
        }
        if (this.divingAtTarget) {
            if (this.frameIndex < 2) {
                ++this.frameIndex;
            }
            ++this.stepX;
            ++this.stepY;
            if (this.c(this.destX, this.destY, this.stepX, this.stepY)) {
                this.visible = false;
                this.frameIndex = 1;
            }
        } else if (this.c(this.apexX, this.apexY, this.stepX, this.stepY)) {
            this.divingAtTarget = true;
            this.stepX = this.b(this.x, this.destX, 28);
            this.stepY = this.b(this.y, this.destY, 28);
        }
    }
}

