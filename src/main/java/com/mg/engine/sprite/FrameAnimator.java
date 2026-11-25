package com.mg.engine.sprite;

/**
 * Abstract animated drawable managing multi-state frame sequences, frame timing,
 * looping behavior, and movement targets.
 */
public abstract class FrameAnimator extends Drawable {
    protected int unusedTag = 0;
    protected int animState;
    protected byte[][] animFrames;
    protected int frameDelay;
    protected int frameTick;
    protected int frameIndex;
    protected boolean looping = true;
    protected int targetX;
    protected int targetY;
    protected int stepX;
    protected int stepY;

    public void c(int targetX, int targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
    }

    public byte[][] d() {
        return this.animFrames;
    }

    public void a(byte[][] animFrames) {
        this.animFrames = animFrames;
    }

    public void b(int frameIndex) {
        this.frameIndex = frameIndex;
    }

    public void c(int frameDelay) {
        this.frameDelay = frameDelay;
        this.frameTick = 0;
    }

    public void b(boolean looping) {
        this.looping = looping;
    }

    public void a_(int state) {
        this.animState = state;
        this.frameIndex %= this.animFrames[state].length;
    }

    /** Advances animation logic and frame index by one tick when visible. */
    public final void e() {
        if (!this.visible) {
            return;
        }
        this.a();
        if (this.frameTick >= this.frameDelay) {
            if (this.animFrames != null && this.animFrames[this.animState] != null) {
                if (this.looping) {
                    this.frameIndex = (this.frameIndex + 1) % this.animFrames[this.animState].length;
                } else {
                    ++this.frameIndex;
                    if (this.frameIndex >= this.animFrames[this.animState].length) {
                        this.frameIndex = this.animFrames[this.animState].length - 1;
                    }
                }
            } else {
                this.frameIndex = -1;
            }
            this.frameTick = 0;
        } else {
            ++this.frameTick;
        }
    }

    public abstract void a();
}

