package com.mic.smsgame.suquan.vfx;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 * Chain-lightning spell VFX that sequentially arcs across a list of board/target nodes.
 */
public class ChainLightningVfx extends SpellEffect {
    private static final int SEGMENT_STEP = 21;
    private static final int HOP_INTERVAL_TICKS = 5;

    private int[] nodeX;
    private int[] nodeY;
    private int activeNodeCount;
    private Image[] arcSegments;
    private int remainingTicks;
    private int hopTimer;

    public ChainLightningVfx(Image sparkSheet, int frameCount, Image[] arcSegments) {
        this.a(sparkSheet, frameCount);
        this.arcSegments = arcSegments;
        this.anchor = Graphics.HCENTER | Graphics.VCENTER;
    }

    public void a(int startX, int startY, int targetX, int targetY, int durationTicks) {
        this.activeNodeCount = 1;
        this.visible = true;
        this.remainingTicks = durationTicks;
        this.hopTimer = HOP_INTERVAL_TICKS;
    }

    public void a(int[] nodeX, int[] nodeY) {
        this.nodeX = nodeX;
        this.nodeY = nodeY;
    }

    public void a() {
        if (this.hopTimer <= 0) {
            if (this.activeNodeCount < this.nodeX.length) {
                ++this.activeNodeCount;
            }
            this.hopTimer = HOP_INTERVAL_TICKS;
        } else {
            --this.hopTimer;
        }

        if (this.remainingTicks > 0) {
            --this.remainingTicks;
            if (this.remainingTicks == 0) {
                this.visible = false;
            }
        }
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (!this.visible) {
            return;
        }
        for (int i = 0; i < this.activeNodeCount; ++i) {
            super.a(g, this.nodeX[i], this.nodeY[i]);
        }
        int segmentCount = this.activeNodeCount - 1;
        for (int i = 0; i < segmentCount; ++i) {
            this.drawArc(g, this.nodeX[i], this.nodeY[i], this.nodeX[i + 1], this.nodeY[i + 1]);
        }
    }

    private void drawArc(Graphics g, int x1, int y1, int x2, int y2) {
        int curX = x1;
        int curY = y1;
        int nextX = 0;
        int nextY = 0;
        boolean stepXActive = true;
        boolean stepYActive = true;

        do {
            if (stepXActive) {
                boolean movingLeft = curX > x2;
                nextX = movingLeft ? curX - SEGMENT_STEP : curX + SEGMENT_STEP;
                stepXActive = movingLeft == (nextX > x2);
                if (!stepXActive) {
                    nextX = x2;
                }
            }

            if (stepYActive) {
                boolean movingUp = curY > y2;
                nextY = movingUp ? curY - SEGMENT_STEP : curY + SEGMENT_STEP;
                stepYActive = movingUp == (nextY > y2);
                if (!stepYActive) {
                    nextY = y2;
                }
            }

            Image diag = this.arcSegments[0];
            Image straight = this.arcSegments[1];
            if (curX < nextX) {
                if (curY < nextY) {
                    g.drawImage(diag, curX, curY, Graphics.TOP | Graphics.LEFT);
                } else if (curY > nextY) {
                    g.drawRegion(diag, 0, 0, diag.getWidth(), diag.getHeight(), Sprite.TRANS_MIRROR, curX, nextY, Graphics.TOP | Graphics.LEFT);
                } else {
                    g.drawImage(straight, curX, curY, Graphics.VCENTER | Graphics.LEFT);
                }
            } else if (curX > nextX) {
                if (curY < nextY) {
                    g.drawRegion(diag, 0, 0, diag.getWidth(), diag.getHeight(), Sprite.TRANS_MIRROR, nextX, curY, Graphics.TOP | Graphics.LEFT);
                } else if (curY > nextY) {
                    g.drawImage(diag, nextX, nextY, Graphics.TOP | Graphics.LEFT);
                } else {
                    g.drawImage(straight, nextX, nextY, Graphics.VCENTER | Graphics.LEFT);
                }
            } else if (curY < nextY) {
                g.drawRegion(straight, 0, 0, straight.getWidth(), straight.getHeight(), Sprite.TRANS_ROT90, curX, curY, Graphics.TOP | Graphics.HCENTER);
            } else if (curY > nextY) {
                g.drawRegion(straight, 0, 0, straight.getWidth(), straight.getHeight(), Sprite.TRANS_ROT90, curX, nextY, Graphics.TOP | Graphics.HCENTER);
            }

            curX = nextX;
            curY = nextY;
        } while (stepXActive || stepYActive);
    }
}

