package com.mg.engine.ui;

import com.mg.engine.core.ActionListener;
import com.mg.engine.core.GameConfig;
import javax.microedition.lcdui.Graphics;

/**
 * Base UI container for screens and dialogs managing bounds, background,
 * softkey bar/buttons, pointer dispatch, and key repeat timing.
 */
public abstract class Window {
    protected int g = 0;
    protected int a_ = 0;
    protected int b_;
    protected int c_;
    protected boolean k;
    protected SoftKeyBar l;
    private SoftKeyButton[] softKeys;
    private int[] softKeyCooldowns;
    protected int m;
    protected int n;
    protected int o;
    protected boolean p;
    protected int q;
    protected ScreenBackground r;
    protected ActionListener s;

    public Window() {
        this.b_ = GameConfig.screenWidth;
        this.c_ = GameConfig.screenHeight;
        this.k = true;
        this.l = new SoftKeyBar();
        this.softKeys = new SoftKeyButton[3];
        this.softKeyCooldowns = new int[3];
        this.o = 13;
        this.p = true;
        this.r = null;
    }

    public boolean f() {
        return this.k;
    }

    public void a(ScreenBackground background) {
        this.r = background;
    }

    public int g() {
        return this.g;
    }

    public int h() {
        return this.a_;
    }

    public int i() {
        return this.b_;
    }

    public int j() {
        return this.c_;
    }

    public int k() {
        return this.q;
    }

    public void a(ActionListener listener) {
        this.s = listener;
    }

    public void c(boolean repeatEnabled) {
        this.p = repeatEnabled;
    }

    public SoftKeyBar l() {
        return this.l;
    }

    public void a(SoftKeyBar bar) {
        this.l = bar;
    }

    public void m() {
        for (int i = 0; i < this.softKeys.length; ++i) {
            this.softKeys[i] = null;
        }
    }

    private boolean hasVisibleSoftKeyBar() {
        if (this.l != null) {
            for (int i = 0; i < this.softKeys.length; ++i) {
                if (this.softKeys[i] != null) {
                    return true;
                }
            }
        }
        return false;
    }

    protected final void c(Graphics g) {
        if (this.hasVisibleSoftKeyBar()) {
            this.l.a(g);
        }
        for (int i = 0; i < this.softKeys.length; ++i) {
            if (this.softKeys[i] != null) {
                this.softKeys[i].a(g);
            }
        }
    }

    public final void d(Graphics g) {
        if (this.r != null) {
            this.r.a(g);
        }
        this.a(g);
        this.c(g);
    }

    public final void a(Graphics g, boolean drawSoftKeys) {
        if (this.r != null) {
            this.r.a(g);
        }
        this.a(g);
        if (drawSoftKeys) {
            this.c(g);
        }
    }

    public final void n() {
        if (this.r != null) {
            this.r.a();
        }
        for (int i = 0; i < this.softKeys.length; ++i) {
            if (this.softKeys[i] != null) {
                this.softKeys[i].c();
                if (this.softKeyCooldowns[i] > 0) {
                    this.softKeyCooldowns[i]--;
                    if (this.softKeyCooldowns[i] == 0 && this.s != null) {
                        this.s.onAction(this.q, this.softKeys[i].a());
                    }
                }
            }
        }
        this.pollPointerEvents();
        this.pollKeyStates();
        this.a();
    }

    /** Low-latency 60 Hz pointer & softkey dispatch between 25 Hz simulation ticks. */
    public final void pollImmediateInput60Hz() {
        this.pollPointerEvents();
        for (int key = 0; key < 21; ++key) {
            if (GameConfig.keyStates[key] == 2) {
                if (!this.handleSoftKeyTrigger(key)) {
                    this.a(key);
                }
                GameConfig.keyStates[key] = 0;
            } else if (GameConfig.keyStates[key] == 1) {
                this.b(key);
                GameConfig.keyStates[key] = 0;
            }
        }
    }

    public boolean o() {
        return this.softKeys[0] != null;
    }

    public boolean p() {
        return this.softKeys[2] != null;
    }

    public boolean q() {
        return this.softKeys[1] != null;
    }

    public void a(SoftKeyButton leftButton) {
        this.softKeys[0] = leftButton;
        this.softKeyCooldowns[0] = 0;
        if (leftButton != null) {
            this.softKeys[0].a(3, GameConfig.screenHeight - 21, 20);
        }
    }

    public void b(SoftKeyButton rightButton) {
        this.softKeys[2] = rightButton;
        this.softKeyCooldowns[2] = 0;
        if (rightButton != null) {
            this.softKeys[2].a(GameConfig.screenWidth - 3, GameConfig.screenHeight - 21, 24);
        }
    }

    public void c(SoftKeyButton centerButton) {
        this.softKeys[1] = centerButton;
        this.softKeyCooldowns[1] = 0;
        if (centerButton != null) {
            this.softKeys[1].a(GameConfig.screenWidth >> 1, GameConfig.screenHeight - 21, 17);
        }
    }

    private void pollPointerEvents() {
        if (!GameConfig.hasPointerEvents) {
            return;
        }
        int px = GameConfig.pointerPressedX;
        int py = GameConfig.pointerPressedY;
        if (px > -1 && py > -1) {
            this.d(px, py);
            GameConfig.pointerPressedY = -1;
            GameConfig.pointerPressedX = -1;
        }

        px = GameConfig.pointerReleasedX;
        py = GameConfig.pointerReleasedY;
        if (px > -1 && py > -1) {
            this.e(px, py);
            GameConfig.pointerReleasedY = -1;
            GameConfig.pointerReleasedX = -1;
        }

        px = GameConfig.pointerDraggedX;
        py = GameConfig.pointerDraggedY;
        if (px > -1 && py > -1) {
            this.a_(px, py);
            GameConfig.pointerDraggedY = -1;
            GameConfig.pointerDraggedX = -1;
        }
    }

    private void pollKeyStates() {
        for (int key = 0; key < 21; ++key) {
            if (GameConfig.keyStates[key] == 1) {
                this.b(key);
            } else if (GameConfig.keyStates[key] == 2) {
                if (!this.handleSoftKeyTrigger(key)) {
                    this.a(key);
                }
            } else if (GameConfig.keyStates[key] >= 3) {
                if (GameConfig.keyStates[key] > this.o || GameConfig.keyStates[key] == 3) {
                    if (!this.handleSoftKeyTrigger(key)) {
                        this.a(key);
                    }
                    if (!this.p) {
                        GameConfig.keyStates[key] = 0;
                    }
                }
                if (GameConfig.keyStates[key] < Integer.MAX_VALUE) {
                    GameConfig.keyStates[key]++;
                }
                return;
            }
            GameConfig.keyStates[key] = 0;
        }
    }

    private boolean handleSoftKeyTrigger(int keyCode) {
        if (this.s == null) {
            return false;
        }
        boolean handled = false;
        if (this.o() && keyCode == 17) {
            if (this.softKeyCooldowns[0] == 0) {
                this.softKeys[0].b();
                this.softKeyCooldowns[0] = 3;
                handled = true;
            }
        } else if (this.p() && keyCode == 18) {
            if (this.softKeyCooldowns[2] == 0) {
                this.softKeys[2].b();
                this.softKeyCooldowns[2] = 3;
                handled = true;
            }
        } else if (this.q() && keyCode == 16 && this.softKeyCooldowns[1] == 0) {
            this.softKeys[1].b();
            this.softKeyCooldowns[1] = 3;
            handled = true;
        }
        return handled;
    }

    public final void d(int pointerX, int pointerY) {
        boolean hitSoftKey = false;
        this.m = pointerY;
        this.n = pointerX;
        if (this.o() && this.softKeys[0].a(pointerX, pointerY)) {
            GameConfig.keyStates[17] = 2;
            hitSoftKey = true;
        }
        if (this.p() && this.softKeys[2].a(pointerX, pointerY)) {
            GameConfig.keyStates[18] = 2;
            hitSoftKey = true;
        }
        if (this.q() && this.softKeys[1].a(pointerX, pointerY)) {
            GameConfig.keyStates[16] = 2;
            hitSoftKey = true;
        }
        if (!hitSoftKey) {
            this.a(pointerX, pointerY);
        }
    }

    public final void e(int pointerX, int pointerY) {
        this.g(pointerX, pointerY);
    }

    public final void a_(int pointerX, int pointerY) {
        this.b(pointerX, pointerY);
        this.c(pointerY - this.m);
        this.d(pointerX - this.n);
        this.m = pointerY;
        this.n = pointerX;
    }

    public void a(int keyCode) {
    }

    public void b(int keyCode) {
    }

    public void a(int pointerX, int pointerY) {
    }

    public void b(int pointerX, int pointerY) {
    }

    public void g(int pointerX, int pointerY) {
    }

    public void c(int deltaY) {
    }

    public void d(int deltaX) {
    }

    public void r() {
    }

    public void s() {
    }

    public void t() {
    }

    public abstract void a();

    public abstract void a(Graphics g);
}
