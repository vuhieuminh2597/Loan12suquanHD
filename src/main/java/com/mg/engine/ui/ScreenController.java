package com.mg.engine.ui;

import com.loan12suquan.hd.platform.HdPlatform;
import com.mg.engine.core.ActionListener;
import com.mg.engine.core.AsyncTask;
import com.mg.engine.core.AsyncTaskPool;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.PixelArtUpscaler;
import com.mg.engine.storage.EngineStorage;
import com.mg.engine.ui.transition.DialogAnimation;
import com.mg.engine.ui.transition.ScreenTransition;
import com.mg.engine.util.RandomUtils;
import java.util.Stack;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Abstract screen/dialog lifecycle router managing the active {@link BaseScreen},
 * modal {@link BaseDialog} stack, screen transitions, dialog open/close zoom animations,
 * and background screen loading via {@link AsyncTaskPool}.
 */
public abstract class ScreenController implements AsyncTask, ActionListener {
    public volatile boolean a = false;
    private Stack dialogStack;
    public volatile BaseScreen b;
    public volatile BaseScreen c;
    public volatile BaseDialog d;
    protected volatile LoadingScreen e;
    public volatile ScreenTransition f;
    protected ScreenTransition[] g;
    protected volatile boolean h;
    public volatile DialogAnimation i;
    public DialogAnimation j;
    public DialogAnimation k;
    protected volatile boolean l;
    private int pendingNextScreenId;
    private int pendingFromScreenId;
    private boolean pendingWithTransition;
    private int shakeTicks;
    private int shakeAmplitude = 2;
    private int shakeOffsetX;
    private int shakeOffsetY;
    private boolean lastUsedDirectScreen = false;

    public void a(LoadingScreen loadingScreen) {
        this.e = loadingScreen;
    }

    public ScreenController(boolean unused) {
        AsyncTaskPool.getInstance().init(5);
        this.l();
        this.m();
    }

    public boolean isShaking() {
        return this.shakeTicks > 0 || this.shakeOffsetX != 0 || this.shakeOffsetY != 0;
    }

    public final void a(Graphics bufferGraphics, Image backBuffer, Graphics screenGraphics) {
        this.a(bufferGraphics, backBuffer, null, null, screenGraphics);
    }

    public final void a(
            Graphics bufferGraphics,
            Image backBuffer,
            Graphics presentGraphics,
            Image presentBuffer,
            Graphics screenGraphics) {
        ScreenTransition trans = this.f;
        boolean useDirectScreenTarget = HdPlatform.isWeb()
                && this.d == null
                && !(this.h && trans != null)
                && !this.isShaking()
                && GameConfig.viewportOffsetX == 0
                && GameConfig.viewportOffsetY == 0;
        Graphics targetGfx = useDirectScreenTarget ? screenGraphics : bufferGraphics;

        DialogAnimation anim = this.i;
        BaseDialog dlg = this.d;
        BaseScreen scr = this.b;
        if (!useDirectScreenTarget && this.lastUsedDirectScreen && scr != null) {
            scr.a(bufferGraphics, false);
        }
        this.lastUsedDirectScreen = useDirectScreenTarget;
        if (this.l && anim != null) {
            if (scr != null) {
                scr.a(targetGfx, false);
            }
            anim.render(targetGfx);
        } else if (dlg != null) {
            if (dlg.c()) {
                if (scr != null) {
                    scr.a(targetGfx, false);
                }
                if (dlg.d()) {
                    dlg.b(false);
                }
            }
            dlg.d(targetGfx);
        } else if (scr != null) {
            scr.d(targetGfx);
        }

        if (!useDirectScreenTarget) {
            if (this.h && trans != null) {
                trans.render(screenGraphics, backBuffer);
            } else {
                screenGraphics.drawImage(
                        backBuffer,
                        GameConfig.viewportOffsetX + this.shakeOffsetX,
                        GameConfig.viewportOffsetY + this.shakeOffsetY,
                        Graphics.TOP | Graphics.LEFT
                );
            }
        }
    }

    /** Called on every 60 Hz visual frame for low-latency touch input, smooth screen wipes, and 60 Hz shake. */
    public final void onVisualFrame60Hz() {
        if (this.shakeTicks > 0) {
            int amp = this.shakeAmplitude < 1 ? 1 : this.shakeAmplitude;
            int signX = (GameConfig.visualFrameCounter & 1) == 0 ? 1 : -1;
            int signY = ((GameConfig.visualFrameCounter >> 1) & 1) == 0 ? 1 : -1;
            this.shakeOffsetX = signX * (1 + RandomUtils.nextInt(amp));
            this.shakeOffsetY = signY * RandomUtils.nextInt(amp + 1);
        } else if (this.shakeOffsetX != 0 || this.shakeOffsetY != 0) {
            this.shakeOffsetX = 0;
            this.shakeOffsetY = 0;
        }

        DialogAnimation anim = this.i;
        if (this.l && anim != null) {
            return;
        }
        BaseDialog dlg = this.d;
        BaseScreen scr = this.b;
        ScreenTransition trans = this.f;
        if (dlg == null && scr != null && this.h && trans != null) {
            trans.update();
            if (trans.isFinished()) {
                if (this.f == trans) {
                    this.h = false;
                    this.f = null;
                }
                this.a = false;
            }
            return;
        }
        if (!this.a) {
            if (dlg != null) {
                dlg.pollImmediateInput60Hz();
            } else if (scr != null) {
                scr.pollImmediateInput60Hz();
            }
        }
    }

    public final void a() {
        if (this.shakeTicks > 0) {
            --this.shakeTicks;
            if (this.shakeTicks == 0) {
                this.shakeOffsetX = 0;
                this.shakeOffsetY = 0;
            } else if (!GameConfig.highFpsEnabled) {
                int amp = this.shakeAmplitude < 1 ? 1 : this.shakeAmplitude;
                int signX = (this.shakeTicks & 1) == 0 ? 1 : -1;
                int signY = ((this.shakeTicks >> 1) & 1) == 0 ? 1 : -1;
                this.shakeOffsetX = signX * (1 + RandomUtils.nextInt(amp));
                this.shakeOffsetY = signY * RandomUtils.nextInt(amp + 1);
            }
        }

        DialogAnimation anim = this.i;
        if (this.l && anim != null) {
            if (this.h) {
                this.h = false;
                this.f = null;
            }
            anim.update();
            if (anim.isFinished()) {
                if (this.i == anim) {
                    this.l = false;
                    this.i = null;
                }
                if (this.h) {
                    this.h = false;
                    this.f = null;
                }
                this.a = false;
            }
            return;
        }

        BaseDialog dlg = this.d;
        if (dlg != null) {
            dlg.n();
            BaseDialog curDlg = this.d;
            BaseScreen scr = this.b;
            if (curDlg != null && !curDlg.d() && scr != null) {
                scr.n();
            }
        } else {
            BaseScreen scr = this.b;
            if (scr != null) {
                ScreenTransition trans = this.f;
                if (this.h && trans != null) {
                    if (!GameConfig.highFpsEnabled) {
                        trans.update();
                        if (trans.isFinished()) {
                            if (this.f == trans) {
                                this.h = false;
                                this.f = null;
                            }
                            this.a = false;
                        }
                    }
                    return;
                }
                scr.n();
            }
        }
    }

    public final void a(int keyIndex) {
        if (!this.a && this.d == null && this.b != null && !this.b.c()) {
            ++GameConfig.keyStates[keyIndex];
            this.b.a(keyIndex);
        }
    }

    public final boolean b() {
        if (this.d != null) {
            return this.d.f();
        }
        return this.b != null && this.b.f();
    }

    public final boolean d() {
        if (this.d != null) {
            return this.d.q();
        }
        return this.b != null && this.b.q();
    }

    public final void b(int keyIndex) {
        if (!this.a && this.d == null && this.b != null && !this.b.c()) {
            GameConfig.keyStates[keyIndex] = 0;
            this.b.b(keyIndex);
        }
    }

    public final void a(int x, int y) {
        if (this.a) {
            return;
        }
        int dpadKey = (this.d == null && this.b != null) ? this.b.c(x, y) : -1;
        if (dpadKey > -1) {
            GameConfig.keyStates[dpadKey] = 3;
            ++GameConfig.keyStates[dpadKey];
            this.b.a(dpadKey);
        } else {
            GameConfig.pointerPressedX = x;
            GameConfig.pointerPressedY = y;
        }
    }

    public final void b(int x, int y) {
        if (this.a) {
            return;
        }
        GameConfig.pointerDraggedX = x;
        GameConfig.pointerDraggedY = y;
    }

    public final void c(int x, int y) {
        if (this.a) {
            return;
        }
        int dpadKey = (this.d == null && this.b != null) ? this.b.d() : -1;
        if (dpadKey > -1) {
            GameConfig.keyStates[dpadKey] = 0;
            this.b.b(dpadKey);
        } else {
            GameConfig.pointerReleasedX = x;
            GameConfig.pointerReleasedY = y;
        }
    }

    public void a(BaseDialog dialog) {
        if (dialog == null) {
            return;
        }
        if (this.d != null) {
            BaseDialog previous = this.d;
            previous.b(true);
            if (this.dialogStack == null) {
                this.dialogStack = new Stack();
            }
            this.dialogStack.push(previous);
        }

        this.d = dialog;
        if (this.d.b() && this.j != null) {
            if (this.h) {
                this.h = false;
                this.f = null;
            }
            this.l = true;
            this.i = this.j;
            this.i.start(this.d.g, GameConfig.screenHeight, this.d.g, this.d.a_, this.d.b_, this.d.c_);
            this.a = true;
        }
    }

    public void a(boolean animate) {
        GameConfig.clearKeyStates();
        if (this.d != null) {
            if (animate && this.k != null) {
                if (this.h) {
                    this.h = false;
                    this.f = null;
                }
                this.l = true;
                this.i = this.k;
                this.i.start(this.d.g, this.d.a_, this.d.g, GameConfig.screenHeight, this.d.b_, this.d.c_);
                this.a = true;
            }
            this.d.r();
        }

        this.d = null;
        if (this.dialogStack != null) {
            if (this.dialogStack.size() > 0) {
                this.d = (BaseDialog) this.dialogStack.pop();
                if (this.d != null) {
                    this.d.b(true);
                }
            }
            System.gc();
        }
    }

    public void e() {
        GameConfig.clearKeyStates();
        if (this.d != null) {
            this.d.r();
        }
        this.d = null;
        if (this.dialogStack != null) {
            for (int idx = 0; idx < this.dialogStack.size(); ++idx) {
                ((BaseDialog) this.dialogStack.elementAt(idx)).r();
            }
            this.dialogStack.removeAllElements();
            this.i = null;
            this.l = false;
            System.gc();
        }
    }

    public void a(BaseScreen nextScreen, boolean disposePrevious, boolean withTransition) {
        if (nextScreen == null) {
            return;
        }
        if (disposePrevious && this.b != null) {
            this.b.r();
        }
        this.b = nextScreen;
        if (this.f != null && withTransition && !this.l) {
            this.h = true;
            this.f.reset();
            this.a = true;
        }
    }

    public void a(int fromScreenId, int nextScreenId, boolean withTransition) {
        this.a(fromScreenId, nextScreenId, withTransition, false);
    }

    public void a(int fromScreenId, int nextScreenId, boolean withTransition, boolean saveBeforeSwitch) {
        this.pendingFromScreenId = fromScreenId;
        this.pendingNextScreenId = nextScreenId;
        this.pendingWithTransition = withTransition;
        if (saveBeforeSwitch) {
            this.c = this.b;
        }
        this.f();
        AsyncTaskPool.getInstance().submit(this);
    }

    private void executePendingScreenLoad() {
        BaseScreen prevToSave;
        synchronized (GUIManager.ENGINE_LOCK) {
            prevToSave = this.c;
            this.c = null;
        }
        if (prevToSave != null) {
            prevToSave.e();
        }

        BaseScreen nextScreen = this.d(this.pendingFromScreenId, this.pendingNextScreenId);
        synchronized (GUIManager.ENGINE_LOCK) {
            if (this.e != null) {
                this.e.a(nextScreen, this.pendingWithTransition);
            }
        }
    }

    public void runTask() {
        if (HdPlatform.isWeb()) {
            HdPlatform.backend().scheduleDelayed(75, new Runnable() {
                @Override
                public void run() {
                    executePendingScreenLoad();
                }
            });
            return;
        }
        try {
            Thread.sleep(100L);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        executePendingScreenLoad();
    }

    public void f() {
        if (this.e == null) {
            this.e = new LoadingScreen();
        }
        this.e.b();
        GameConfig.isInGameActive = false;
        this.a(this.e, true, false);
    }

    public void g() {
        this.h = false;
        this.l = false;
        this.f = null;
        this.i = null;
        if (this.b != null) {
            this.b.t();
        }
    }

    public void h() {
        if (this.b != null) {
            this.b.s();
        }
    }

    public void c(int ticks) {
        this.triggerShake(ticks, 2);
    }

    public void triggerShake(int ticks, int amplitude) {
        if (ticks <= 0) {
            return;
        }
        if (ticks > this.shakeTicks) {
            this.shakeTicks = ticks;
        }
        if (amplitude > this.shakeAmplitude || this.shakeTicks == ticks) {
            this.shakeAmplitude = amplitude < 1 ? 1 : (amplitude > 5 ? 5 : amplitude);
        }
    }

    /** Unlocks a relic slot offline and opens {@link CollectionScreenBase} with the reveal animation. */
    public void openRelicSlot(int slotIndex) {
        EngineStorage.a(slotIndex, 0, "OFFLINE", "2000 Vàng");
        CollectionScreenBase collectionScreen;
        BaseScreen active = GUIManager.getInstance().e.b;
        if (active != null && active.k() == 3) {
            collectionScreen = (CollectionScreenBase) active;
            collectionScreen.v();
        } else {
            collectionScreen = this.k();
        }
        if (collectionScreen == null) {
            return;
        }
        collectionScreen.e(slotIndex);
        GUIManager.getInstance().a((BaseScreen) collectionScreen, false);
        GUIManager.getInstance().e();
    }

    public ScreenTransition d(int index) {
        return this.g[index];
    }

    public int j() {
        return this.g.length;
    }

    protected abstract BaseScreen d(int fromScreenId, int nextScreenId);

    public abstract CollectionScreenBase k();

    public abstract void l();

    public abstract void m();
}


