package com.mg.engine.ui;

import com.mg.engine.core.GUIManager;

/**
 * Base modal dialog supporting open/close zoom animations, background dim/redraw, and optional auto-close timer.
 */
public abstract class BaseDialog extends Window {
    public int dialogType = 0;
    protected boolean animated = true;
    protected boolean redrawParentScreen = true;
    protected boolean pauseParentUpdate = true;
    private int autoCloseTicks;

    public void a(boolean animated) {
        this.animated = animated;
    }

    public boolean b() {
        return this.animated;
    }

    public boolean c() {
        return this.redrawParentScreen;
    }

    public void b(boolean redrawParentScreen) {
        this.redrawParentScreen = redrawParentScreen;
    }

    public boolean d() {
        return this.pauseParentUpdate;
    }

    public BaseDialog(int dialogType) {
        this.dialogType = dialogType;
        if (this.dialogType == 1) {
            this.autoCloseTicks = -1;
        } else if (this.dialogType == 0) {
            this.autoCloseTicks = 2000;
        }
    }

    public final void a() {
        if (this.autoCloseTicks > 0) {
            --this.autoCloseTicks;
            if (this.autoCloseTicks == 1) {
                GUIManager.getInstance().a(true);
                return;
            }
        }
        this.e();
    }

    protected void e() {
    }
}
