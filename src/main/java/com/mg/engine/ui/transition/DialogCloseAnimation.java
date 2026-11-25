package com.mg.engine.ui.transition;

import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import javax.microedition.lcdui.Graphics;

/** Shrinking zoom animation played when a modal dialog closes. */
public class DialogCloseAnimation extends DialogAnimation {
    private boolean shrinking;

    public void start(int startX, int startY, int endX, int endY, int width, int height) {
        if (height < 18) {
            height = 18;
        }
        if (width < 18) {
            width = 18;
        }
        this.centerX = startX + width / 2;
        this.centerY = startY + height / 2;
        this.targetX = this.centerX;
        this.targetY = this.centerY;
        this.targetWidth = 18;
        this.targetHeight = 18;
        this.currentWidth = width;
        this.currentHeight = height;
        this.shrinking = true;
        this.finished = false;
        this.resetInterpolation();
    }

    public void update() {
        if (this.finished) {
            return;
        }
        this.snapshotBeforeStep();
        if (this.shrinking) {
            boolean widthDone = true;
            if (this.currentWidth != this.targetWidth) {
                int dx = this.targetWidth - this.currentWidth;
                if (Math.abs(dx) < 5) {
                    this.currentWidth += dx;
                } else {
                    this.currentWidth += dx / 2;
                    widthDone = false;
                }
            }
            if (widthDone) {
                int dy = this.targetHeight - this.currentHeight;
                if (Math.abs(dy) < 5) {
                    this.currentHeight += dy;
                    this.finished = true;
                } else {
                    this.currentHeight += dy / 2;
                }
            }
        } else {
            int dy = this.targetY - this.centerY;
            if (Math.abs(dy) < 2) {
                this.centerY += dy;
                this.finished = true;
            } else {
                this.centerY += dy / 2;
            }
        }
    }

    public void render(Graphics g) {
        GUIManager.a(g, GameConfig.themeColor, GameConfig.themeHighlightColor, this.getLeft(), this.getTop(), this.getWidth(), this.getHeight());
    }
}


