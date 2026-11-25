package com.mg.engine.ui;

import com.loan12suquan.hd.HdConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.FontManager;
import javax.microedition.lcdui.Graphics;

/** Blinking {@link FloatingText} notification (toggles visibility every 3 ticks). */
public class BlinkingText extends FloatingText {
    private boolean visible;

    public void a() {
        super.a();
        if (this.elapsedTicks % 3 == 0) {
            this.visible = !this.visible;
        }
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (this.active && this.visible && this.text != null) {
            int cx = offsetX + this.x;
            int cy = offsetY + this.y;
            if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
                int tw = DrawUtils.getStringWidth(DrawUtils.UI_FONT_BOLD, this.text) + 12;
                int th = DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD) + 6;
                int bx = Math.max(4, Math.min(236 - tw, cx - (tw >> 1)));
                DrawUtils.drawAntiquePanel(
                        g, bx, cy - 2, tw, th,
                        0x6E1B22, 0x450E13, 0x1F0608,
                        0xFFD700, 0xFFF2A6, 0xFFFFFF);
                DrawUtils.drawShadowText(
                        g, DrawUtils.UI_FONT_BOLD, this.text,
                        bx + (tw >> 1), cy + 1, Graphics.TOP | Graphics.HCENTER,
                        0xFFF5B8, 0x120604);
                return;
            }
            FontManager.primaryFont.a(g, this.text, cx, cy, 1);
        }
    }
}


