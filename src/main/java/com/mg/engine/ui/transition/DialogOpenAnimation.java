package com.mg.engine.ui.transition;

import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import javax.microedition.lcdui.Graphics;

/** Expanding zoom animation played when a modal dialog opens. */
public class DialogOpenAnimation extends DialogAnimation {
    private boolean expanding;
    private boolean movingUp;

    public void start(int startX, int startY, int endX, int endY, int width, int height) {
        if (height < 18) {
            height = 18;
        }
        if (width < 18) {
            width = 18;
        }
        this.targetX = endX + width / 2;
        this.targetY = endY + height / 2;
        this.targetWidth = width;
        this.targetHeight = height;
        this.currentWidth = 18;
        this.currentHeight = 18;
        this.centerX = this.targetX;
        this.centerY = this.targetY;
        this.movingUp = this.centerY > endY;
        this.expanding = false;
        this.finished = false;
        this.resetInterpolation();
    }

    public void update() {
        if (this.finished) {
            return;
        }
        this.snapshotBeforeStep();
        if (this.expanding) {
            this.centerY = this.targetY;
            boolean heightDone = true;
            if (this.currentHeight != this.targetHeight) {
                int dy = this.targetHeight - this.currentHeight;
                if (Math.abs(dy) < 5) {
                    this.currentHeight += dy;
                } else {
                    this.currentHeight += dy / 2;
                    heightDone = false;
                }
            }
            if (heightDone) {
                int dx = this.targetWidth - this.currentWidth;
                if (Math.abs(dx) < 5) {
                    this.currentWidth += dx;
                    this.finished = true;
                } else {
                    this.currentWidth += dx / 2;
                }
            }
        } else {
            int aimY = this.movingUp ? this.targetY - 2 : this.targetY + 2;
            int dy = aimY - this.centerY;
            if (Math.abs(dy) < 2) {
                this.centerY += dy;
                this.expanding = true;
            } else {
                this.centerY += dy / 2;
            }
        }
    }

    public void render(Graphics g) {
        GUIManager.a(g, GameConfig.themeColor, GameConfig.themeHighlightColor, this.getLeft(), this.getTop(), this.getWidth(), this.getHeight());
    }
}


