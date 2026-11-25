package com.mg.engine.ui;

import com.loan12suquan.hd.HdInputState;
import com.mg.engine.graphics.DrawUtils;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/** Beveled antique wood-and-bronze soft-key button rendered on the bottom {@link SoftKeyBar} with HD hover glow. */
public class TextSoftKeyButton extends SoftKeyButton {
    private static final int BUTTON_HEIGHT = 20;
    private final String label;
    private final boolean visible;
    private final boolean isCancelStyle;

    public TextSoftKeyButton(String label, int actionId) {
        super(actionId);
        this.label = label != null ? label.trim() : "";
        this.visible = this.label.length() > 0;
        this.isCancelStyle = this.visible && (
                "Thoát".equals(this.label)
                || "Đóng".equals(this.label)
                || "Rút lui".equals(this.label)
                || "Không".equals(this.label)
                || "Trở về".equals(this.label)
                || "Bỏ qua".equals(this.label));
        if (this.visible) {
            int textW = DrawUtils.getStringWidth(DrawUtils.UI_FONT_BOLD, this.label);
            int w = textW + 14;
            if (w < 58) {
                w = 58;
            }
            if (w > 76) {
                w = 76;
            }
            this.width = w;
        } else {
            this.width = 0;
        }
    }

    public boolean a(int px, int py) {
        if (!this.visible) {
            return false;
        }
        return super.a(px, py);
    }

    public void a(Graphics g) {
        if (!this.visible) {
            return;
        }
        int bx = this.x;
        int by = this.y;
        int bw = this.width;
        int bh = BUTTON_HEIGHT;
        boolean hovered = HdInputState.isHovered(bx, by, bw, bh);

        if (this.pressTicks > 0) {
            DrawUtils.drawAntiquePanel(
                    g, bx, by, bw, bh,
                    0x246B5C, 0x16473D, 0x0E2B25,
                    0xFFD700, 0xFFF2A6, 0xFFFFFF);
        } else if (this.isCancelStyle) {
            DrawUtils.drawAntiquePanel(
                    g, bx, by, bw, bh,
                    hovered ? 0x6E2A32 : 0x542026,
                    hovered ? 0x4A1A20 : 0x381418,
                    0x1F0A0C,
                    hovered ? 0xFFD700 : 0xB86B4B,
                    hovered ? 0xFFF2A6 : 0xE69E7E,
                    0xF5C7A9);
        } else {
            DrawUtils.drawAntiquePanel(
                    g, bx, by, bw, bh,
                    hovered ? 0x246B5C : 0x422B1B,
                    hovered ? 0x16473D : 0x2B1B10,
                    hovered ? 0x0E2B25 : 0x170E08,
                    hovered ? 0xFFD700 : 0xB8893D,
                    hovered ? 0xFFF2A6 : 0xF0C975,
                    0xFFE680);
        }

        Font font = DrawUtils.UI_FONT_BOLD;
        if (DrawUtils.getStringWidth(font, this.label) > bw - 6) {
            font = DrawUtils.UI_FONT_PLAIN;
        }
        int textY = by + ((bh - DrawUtils.getFontHeight(font)) >> 1) + (this.pressTicks > 0 ? 1 : 0);
        int textColor = (this.pressTicks > 0 || hovered) ? 0xFFF9C4 : (this.isCancelStyle ? 0xFFE4D6 : 0xFFF4D0);
        DrawUtils.drawShadowText(
                g, font, this.label,
                bx + (bw >> 1), textY,
                Graphics.TOP | Graphics.HCENTER,
                textColor, 0x120804);
    }
}
