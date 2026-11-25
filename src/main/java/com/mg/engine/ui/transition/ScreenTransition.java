package com.mg.engine.ui.transition;

import com.mg.engine.core.GameConfig;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Base full-screen transition rendered between {@link com.mg.engine.ui.BaseScreen} switches. */
public abstract class ScreenTransition {
    protected int viewportX;
    protected int viewportY;
    protected int screenWidth;
    protected int screenHeight;
    protected boolean finished;

    public ScreenTransition() {
        this.viewportX = GameConfig.renderScale > 1 ? 0 : GameConfig.viewportOffsetX;
        this.viewportY = GameConfig.renderScale > 1 ? 0 : GameConfig.viewportOffsetY;
        this.screenWidth = GameConfig.screenWidth;
        this.screenHeight = GameConfig.screenHeight;
    }

    public abstract void update();

    public abstract void render(Graphics g, Image frameBuffer);

    public abstract void reset();

    public abstract void init();

    public boolean isFinished() {
        return this.finished;
    }
}


