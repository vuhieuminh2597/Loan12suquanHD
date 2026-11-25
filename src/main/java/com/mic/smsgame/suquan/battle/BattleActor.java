package com.mic.smsgame.suquan.battle;

/**
 * Abstract battle participant (human player or AI opponent) acting on {@link BattleController}.
 */
public abstract class BattleActor {
    protected int actorIndex;
    protected BattleController controller;

    public BattleActor(BattleController controller, int actorIndex) {
        this.controller = controller;
        this.actorIndex = actorIndex;
    }

    public void a(int keyCode) {
    }

    public void a(int pointerX, int pointerY) {
    }

    public void onPointerDragged(int pointerX, int pointerY) {
    }

    public void onPointerReleased(int pointerX, int pointerY) {
    }

    /** Called when this actor's turn starts in interactive input mode. */
    public abstract void b();

    /** Called when this actor should compute and execute its turn asynchronously (AI). */
    public abstract void a();
}
