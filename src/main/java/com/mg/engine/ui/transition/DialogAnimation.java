package com.mg.engine.ui.transition;

import com.mg.engine.core.GameConfig;
import javax.microedition.lcdui.Graphics;

/** Base open/close zoom animation for modal {@link com.mg.engine.ui.BaseDialog} popups. */
public abstract class DialogAnimation {
    protected int centerX;
    protected int centerY;
    protected int targetX;
    protected int targetY;
    protected int currentWidth;
    protected int currentHeight;
    protected int targetWidth;
    protected int targetHeight;
    protected boolean finished;
    protected int prevCenterY;
    protected int prevWidth;
    protected int prevHeight;
    protected int animTickStamp = -1;

    public abstract void start(int startX, int startY, int endX, int endY, int width, int height);

    protected final void resetInterpolation() {
        this.prevCenterY = this.centerY;
        this.prevWidth = this.currentWidth;
        this.prevHeight = this.currentHeight;
        this.animTickStamp = -1;
    }

    protected final void snapshotBeforeStep() {
        if (this.animTickStamp != GameConfig.logicTickCounter) {
            this.prevCenterY = this.centerY;
            this.prevWidth = this.currentWidth;
            this.prevHeight = this.currentHeight;
            this.animTickStamp = GameConfig.logicTickCounter;
        }
    }

    public boolean isFinished() {
        return this.finished;
    }

    public void update() {
    }

    public void render(Graphics g) {
    }

    public int getLeft() {
        return this.centerX - this.getWidth() / 2;
    }

    public int getTop() {
        int cy = this.centerY;
        if (GameConfig.highFpsEnabled && this.animTickStamp == GameConfig.logicTickCounter) {
            cy = this.prevCenterY + (((this.centerY - this.prevCenterY) * GameConfig.renderSubTick256) >> 8);
        }
        return cy - this.getHeight() / 2;
    }

    public int getWidth() {
        if (GameConfig.highFpsEnabled && this.animTickStamp == GameConfig.logicTickCounter) {
            return this.prevWidth + (((this.currentWidth - this.prevWidth) * GameConfig.renderSubTick256) >> 8);
        }
        return this.currentWidth;
    }

    public int getHeight() {
        if (GameConfig.highFpsEnabled && this.animTickStamp == GameConfig.logicTickCounter) {
            return this.prevHeight + (((this.currentHeight - this.prevHeight) * GameConfig.renderSubTick256) >> 8);
        }
        return this.currentHeight;
    }
}


