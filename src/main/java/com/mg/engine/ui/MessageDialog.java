package com.mg.engine.ui;

import com.mg.engine.core.GameConfig;
import javax.microedition.lcdui.Graphics;

/** Standard framed alert/confirm modal dialog supporting 1 to 3 bottom soft-key buttons. */
public class MessageDialog extends BaseDialog {
    private final ScrollableTextPanel textPanel;

    public MessageDialog(String title, String message, String[] buttonLabels, int[] actionIds, int mode) {
        super(mode);
        this.textPanel = new ScrollableTextPanel(title, message, 1);
        if (buttonLabels != null) {
            if (buttonLabels.length == 1) {
                this.c(new TextSoftKeyButton(buttonLabels[0], actionIds[0]));
            } else if (buttonLabels.length == 2) {
                this.c(new TextSoftKeyButton(buttonLabels[0], actionIds[0]));
                this.b(new TextSoftKeyButton(buttonLabels[1], actionIds[1]));
            } else {
                this.a(new TextSoftKeyButton(buttonLabels[0], actionIds[0]));
                this.c(new TextSoftKeyButton(buttonLabels[1], actionIds[1]));
                this.b(new TextSoftKeyButton(buttonLabels[1], actionIds[2]));
            }
        }

        this.g = this.textPanel.g();
        this.a_ = this.textPanel.h();
        this.b_ = this.textPanel.i();
        this.c_ = this.textPanel.j();
        this.a(this.l());
    }

    public void a(SoftKeyBar bar) {
        if (bar != null) {
            bar.a(GameConfig.themeColor);
        }
        super.a(bar);
    }

    public void a(int keyIndex) {
        this.textPanel.a(keyIndex);
    }

    public void a(int x, int y) {
        this.textPanel.a(x, y);
    }

    protected void e() {
        this.textPanel.a();
    }

    public void a(Graphics g) {
        this.textPanel.a(g);
    }
}


