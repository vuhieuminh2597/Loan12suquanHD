package com.mg.engine.ui;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdFontRenderer;
import com.loan12suquan.hd.HdUiRenderer;
import com.mg.engine.graphics.FontManager;
import javax.microedition.lcdui.Graphics;

/** Upward-drifting {@link FloatingText} used for battle score/combo/damage popups. */
public class RisingText extends FloatingText {
    public RisingText() {
        this.maxTicks = 10;
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (!this.active || this.text == null || this.text.isEmpty()) {
            return;
        }
        int cx = offsetX + this.x;
        int cy = offsetY + this.y - this.elapsedTicks;
        if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
            com.loan12suquan.hd.platform.HdCanvas2D g2 = g.getHdCanvas();
            int s = HdConfig.HD_SCALE;
            HdFontRenderer.drawDisplayCenteredHd(
                    g2, this.text,
                    cx * s, cy * s + 12,
                    true, 0xFFF9C4, 0xFFB833, 0x2B0C05, 5.5f);
            return;
        }
        FontManager.primaryFont.a(g, this.text, cx, cy, 1);
    }
}


