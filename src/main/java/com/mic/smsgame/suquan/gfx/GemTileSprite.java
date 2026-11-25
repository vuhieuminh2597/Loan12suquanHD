package com.mic.smsgame.suquan.gfx;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.platform.HdCanvas2D;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.sprite.AnimatedSprite;
import com.mg.engine.util.RandomUtils;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Single animated gem tile inside the 8x8 match-3 board with swap, fall,
 * explosion, clip-shrink, and periodic ripple-shiver animations.
 */
public class GemTileSprite extends AnimatedSprite {
    public static final int STATE_EMPTY = 0;
    public static final int STATE_IDLE = 1;
    public static final int STATE_EXPLODE = 2;
    public static final int STATE_FALL = 3;
    public static final int STATE_SWAP = 4;
    public static final int STATE_SHRINK = 5;
    public static final int STATE_CLEAR = 6;

    private int startDelay = 0;
    private int shrinkClipSize;
    private int shiverStartTick;
    private int shiverTimer;
    private int shiverCycleLength;
    private int shiverOffsetX;
    private int shiverOffsetY;
    private boolean shivering;
    private boolean specialShimmer;
    private int bossSealTurns;

    public void setSpecialShimmer(boolean specialShimmer) {
        this.specialShimmer = specialShimmer;
    }

    public void setBossSealTurns(int turns) {
        this.bossSealTurns = turns < 0 ? 0 : turns;
    }

    public void a(int shiverStartTick, int shiverCycleLength) {
        this.shiverStartTick = shiverStartTick;
        this.shiverCycleLength = shiverCycleLength;
    }

    public GemTileSprite(int x, int y) {
        super(x, y);
        this.b(false);
    }

    public void a(int x, int y, Image gemImage) {
        this.d(x, y);
        this.a(gemImage, 1);
        this.a_(STATE_IDLE);
    }

    public void a(int fromX, int fromY, int toX, int toY, Image gemImage) {
        this.setMoveRoute(fromX, fromY, toX, toY);
        this.a(gemImage, 1);
        this.a_(STATE_SWAP);
    }

    public void a(int fromX, int fromY, int toX, int toY, Image gemImage, int delayTicks) {
        this.setMoveRoute(fromX, fromY, toX, toY);
        this.a(gemImage, 1);
        this.a_(STATE_FALL);
        this.startDelay = delayTicks;
    }

    public void a(int x, int y, Image explodeSheet, int frameCount) {
        this.d(x, y);
        this.a(explodeSheet, frameCount);
        this.a_(STATE_EXPLODE);
    }

    public void b(int initialSize, int delayTicks) {
        this.shrinkClipSize = initialSize;
        this.a_(STATE_SHRINK);
        this.startDelay = delayTicks;
    }

    private void setMoveRoute(int fromX, int fromY, int toX, int toY) {
        this.d(fromX, fromY);
        this.c(toX, toY);
    }

    public void a_(int state) {
        super.a_(state);
        this.frameIndex = 0;
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (this.animState == STATE_SHRINK) {
            int rx = this.getRenderX();
            int ry = this.getRenderY();
            int clipX = rx - this.shrinkClipSize / 2;
            int clipY = ry - this.shrinkClipSize / 2;
            DrawUtils.saveClip(g, DrawUtils.tempClip);
            DrawUtils.intersectClip(g, DrawUtils.tempClip, clipX, clipY, this.shrinkClipSize, this.shrinkClipSize);
            super.a(g, offsetX, offsetY);
            DrawUtils.restoreClip(g, DrawUtils.tempClip);
        } else {
            if (this.shivering && this.animState == STATE_IDLE) {
                offsetX += this.shiverOffsetX;
                offsetY += this.shiverOffsetY;
            }
            super.a(g, offsetX, offsetY);
            if (this.specialShimmer && this.animState == STATE_IDLE && this.visible) {
                int phase = (GameConfig.visualFrameCounter + ((this.x + this.y) >> 2)) % 84;
                if (phase < 14) {
                    int gx = this.getRenderX() + offsetX - 7 + phase;
                    int gy = this.getRenderY() + offsetY - 6;
                    g.setColor(0xFFF6C8);
                    g.drawLine(gx, gy, gx - 4, gy + 10);
                    g.setColor(0xFFFFFF);
                    g.fillRect(gx - 2, gy + 3, 2, 2);
                }
            }
            if (this.bossSealTurns > 0 && this.animState != STATE_EMPTY && this.animState != STATE_EXPLODE && this.visible) {
                int rx = this.getRenderX() + offsetX;
                int ry = this.getRenderY() + offsetY;
                boolean urgent = (this.bossSealTurns == 1);
                boolean blink = ((GameConfig.visualFrameCounter >> (urgent ? 2 : 3)) & 1) == 0;
                int rimCol = urgent ? (blink ? 0xFF3344 : 0xFFD700) : (blink ? 0xFF9933 : 0xD9386A);
                if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
                    HdCanvas2D g2 = g.getHdCanvas();
                    int s = HdConfig.HD_SCALE;
                    int hx = (rx - 10) * s;
                    int hy = (ry - 10) * s;
                    int hw = 20 * s;
                    int hh = 20 * s;
                    g2.setColorHexAlpha(rimCol, 65);
                    g2.fillRoundRect(hx + 2, hy + 2, hw - 4, hh - 4, 12);
                    g2.setColor(rimCol);
                    g2.setStrokeWidth(3.2f);
                    g2.drawRoundRect(hx + 2, hy + 2, hw - 4, hh - 4, 12);
                    g2.setColor(0xFFF2A6);
                    g2.fillOval(hx, hy, 7, 7);
                    g2.fillOval(hx + hw - 7, hy, 7, 7);
                    g2.fillOval(hx, hy + hh - 7, 7, 7);
                    g2.fillOval(hx + hw - 7, hy + hh - 7, 7, 7);
                    g2.setColor(0x1F080C);
                    g2.fillRoundRect((rx + 2) * s, (ry + 1) * s, 8 * s, 9 * s, 6);
                    g2.setColor(rimCol);
                    g2.setStrokeWidth(2.0f);
                    g2.drawRoundRect((rx + 2) * s, (ry + 1) * s, 8 * s, 9 * s, 6);
                } else {
                    g.setColor(rimCol);
                    g.drawRect(rx - 10, ry - 10, 19, 19);
                    g.setColor(0xFFF2A6);
                    g.fillRect(rx - 10, ry - 10, 2, 2);
                    g.fillRect(rx + 8, ry - 10, 2, 2);
                    g.fillRect(rx - 10, ry + 8, 2, 2);
                    g.fillRect(rx + 8, ry + 8, 2, 2);
                    g.setColor(0x1F080C);
                    g.fillRect(rx + 2, ry + 1, 8, 9);
                    g.setColor(rimCol);
                    g.drawRect(rx + 2, ry + 1, 7, 8);
                }
                DrawUtils.drawShadowText(
                        g, DrawUtils.UI_FONT_PLAIN, "" + this.bossSealTurns,
                        rx + 6, ry, Graphics.TOP | Graphics.HCENTER,
                        urgent ? 0xFF8080 : 0xFFF280, 0x100406);
            }
        }
    }

    public void a() {
        if (this.shiverTimer < this.shiverCycleLength) {
            ++this.shiverTimer;
            if (this.shiverTimer == this.shiverStartTick) {
                this.shivering = true;
            }
            if (this.shivering) {
                this.shiverOffsetX = RandomUtils.nextInt() % 2;
                this.shiverOffsetY = RandomUtils.nextInt() % 2;
                if (this.shiverTimer == this.shiverStartTick + 3) {
                    this.shivering = false;
                }
            }
        } else {
            this.shiverTimer = 0;
        }

        if (this.startDelay > 0) {
            --this.startDelay;
            return;
        }

        switch (this.animState) {
            case STATE_EXPLODE:
                if (this.frameIndex >= this.animFrames[STATE_EXPLODE].length - 1) {
                    this.a_(STATE_EMPTY);
                }
                break;
            case STATE_FALL:
                if (this.f(this.targetY, this.b(this.y, this.targetY, 2))) {
                    this.a_(STATE_IDLE);
                }
                break;
            case STATE_SWAP:
                if (this.c(this.targetX, this.targetY, this.b(this.x, this.targetX, 2), this.b(this.y, this.targetY, 2))) {
                    this.a_(STATE_IDLE);
                }
                break;
            case STATE_SHRINK:
                if (this.shrinkClipSize <= 0) {
                    this.a_(STATE_EMPTY);
                    return;
                }
                this.shrinkClipSize -= 3;
                if (this.shrinkClipSize < 0) {
                    this.shrinkClipSize = 0;
                }
                break;
            case STATE_CLEAR:
                this.a_(STATE_EMPTY);
                break;
            default:
                break;
        }
    }
}

