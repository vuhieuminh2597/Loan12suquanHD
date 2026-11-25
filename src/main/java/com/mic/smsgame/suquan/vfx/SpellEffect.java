package com.mic.smsgame.suquan.vfx;

import com.mg.engine.sprite.AnimatedSprite;

/**
 * Base class for battle spell visual effects.
 */
public abstract class SpellEffect extends AnimatedSprite {
    protected int spellType;

    public int b() {
        return this.spellType;
    }

    public void a(int spellType) {
        this.spellType = spellType;
    }

    /** Launches the effect from {@code (startX, startY)} toward {@code (targetX, targetY)} with {@code delayOrDuration}. */
    public abstract void a(int startX, int startY, int targetX, int targetY, int delayOrDuration);
}

