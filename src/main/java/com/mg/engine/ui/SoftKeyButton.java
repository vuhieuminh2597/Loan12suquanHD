package com.mg.engine.ui;

import com.mg.engine.core.GameConfig;
import javax.microedition.lcdui.Graphics;

/** Base soft-key button widget with press feedback animation and pointer hit-testing. */
public class SoftKeyButton {
    protected int pressTicks;
    protected int x;
    protected int y;
    protected int width;
    protected int actionId;

    public int a() {
        return this.actionId;
    }

    public SoftKeyButton(int actionId) {
        this.actionId = actionId;
    }

    public void a(int anchorX, int anchorY, int anchor) {
        this.x = anchorX;
        if (anchor == (Graphics.TOP | Graphics.HCENTER)) {
            this.x = anchorX - (this.width >> 1);
        } else if (anchor == (Graphics.TOP | Graphics.RIGHT)) {
            this.x = anchorX - this.width;
        }
        this.y = anchorY;
    }

    public boolean a(int px, int py) {
        int hitW = this.width < 64 ? 64 : this.width + 10;
        int hitX = this.x + (this.width >> 1) - (hitW >> 1);
        return py >= GameConfig.screenHeight - 26 && px >= hitX && px < hitX + hitW;
    }

    public void b() {
        this.pressTicks = 3;
    }

    public void a(Graphics g) {
    }

    public void c() {
        if (this.pressTicks > 0) {
            --this.pressTicks;
        }
    }
}


