package com.mg.engine.ui.transition;

/** Base 14-pixel venetian-blinds screen transition. */
public abstract class BlindsTransition extends ScreenTransition {
    protected static final int STRIP_SIZE = 14;

    protected int stripCount;
    protected int activeStrips;
    protected int[] stripProgress;

    public BlindsTransition() {
        this.init();
    }

    public void reset() {
        this.activeStrips = 0;
        for (int i = this.stripProgress.length - 1; i >= 0; --i) {
            this.stripProgress[i] = 0;
        }
        this.finished = false;
    }

    public void update() {
        if (this.finished) {
            return;
        }
        int lastActive = this.activeStrips <= this.stripCount ? this.activeStrips - 1 : this.stripCount - 1;
        for (int i = lastActive; i >= 0; --i) {
            if (this.stripProgress[i] < STRIP_SIZE) {
                ++this.stripProgress[i];
            }
        }
        if (this.activeStrips < this.stripCount) {
            this.stripProgress[this.activeStrips] = 1;
            ++this.activeStrips;
        } else if (this.stripProgress[this.stripCount - 1] >= STRIP_SIZE) {
            this.finished = true;
        }
    }
}


