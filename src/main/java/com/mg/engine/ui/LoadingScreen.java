package com.mg.engine.ui;

import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.FontManager;
import javax.microedition.lcdui.Graphics;

/**
 * Base loading screen displaying a progress bar while the next screen initializes on a background thread.
 */
public class LoadingScreen extends BaseScreen {
    private int bgPaintCount;
    protected int c;
    protected int d;
    protected BaseScreen e;
    protected boolean f;
    protected int t;
    protected boolean u;

    public void a(BaseScreen targetScreen, boolean withTransition) {
        if (targetScreen != null) {
            this.e = targetScreen;
            this.u = withTransition;
            this.f = true;
        }
    }

    public LoadingScreen() {
        this.t = GameConfig.frameBackgroundColor;
        this.b();
    }

    public final void b() {
        this.bgPaintCount = 0;
        this.c = 0;
        this.d = 5;
        this.e = null;
        this.f = false;
    }

    protected void b(Graphics g) {
        int barY = super.c_ - 20;
        g.setColor(0xFFFFFF);
        g.drawRect(0, barY, super.b_, 19);
        g.setColor(0xEC465A);
        g.fillRect(0, barY + 1, this.c * super.b_ / 100, 18);
        FontManager.primaryFont.a(g, "Vui lòng chờ...", super.b_ / 2, barY + 3, 1);
    }

    public void a() {
        if (this.c == 100) {
            if (this.e != null) {
                GUIManager.getController().a(this.e, false, this.u);
                this.e = null;
            }
            return;
        }
        if (this.f) {
            this.d += 3;
        }
        this.c += this.d;
        if (this.c == 95) {
            this.c = 90;
        }
        if (this.c > 100) {
            this.c = 100;
        }
    }

    protected void e(Graphics g) {
        g.setColor(this.t);
        g.fillRect(0, 0, super.b_, super.c_);
    }

    public final void a(Graphics g) {
        if (this.bgPaintCount <= 2) {
            this.e(g);
            ++this.bgPaintCount;
        }
        this.b(g);
    }
}
