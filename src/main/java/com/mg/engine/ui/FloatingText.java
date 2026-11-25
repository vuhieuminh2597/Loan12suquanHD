package com.mg.engine.ui;

/** Base temporary floating notification text with configurable tick lifetime. */
public abstract class FloatingText {
    protected int x;
    protected int y;
    protected boolean active;
    protected String text;
    protected int elapsedTicks;
    protected int maxTicks = 8;

    public void a(int durationTicks) {
        this.maxTicks = durationTicks;
    }

    public void a(String text, int x, int y) {
        this.elapsedTicks = 0;
        this.text = text;
        this.x = x;
        this.y = y;
        this.active = true;
    }

    public void a() {
        if (this.elapsedTicks < this.maxTicks) {
            ++this.elapsedTicks;
        } else {
            this.active = false;
        }
    }
}


