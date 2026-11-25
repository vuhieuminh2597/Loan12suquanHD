package com.mg.engine.ui;

/**
 * Base full-screen view with optional on-screen {@link TouchDPad} overlay.
 */
public abstract class BaseScreen extends Window {
    protected boolean inputLocked = true;
    protected TouchDPad touchDPad;

    public boolean c() {
        return this.inputLocked;
    }

    public void a(TouchDPad touchDPad) {
        this.touchDPad = touchDPad;
    }

    public int c(int pointerX, int pointerY) {
        return this.touchDPad != null ? this.touchDPad.b(pointerX, pointerY) : -1;
    }

    public int d() {
        return this.touchDPad != null ? this.touchDPad.c() : -1;
    }

    public void e() {
    }
}
