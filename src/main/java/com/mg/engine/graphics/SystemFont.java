package com.mg.engine.graphics;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/** {@link GameFont} implementation backed by the platform's default LCDUI {@link Font}. */
public class SystemFont extends GameFont {
    Font font = Font.getDefaultFont();

    public int a() {
        return this.font.getHeight();
    }

    public int a(String text) {
        return this.font.stringWidth(text);
    }

    public void a(Graphics g, String text, int x, int y, int align) {
        if (align == 0) {
            g.drawString(text, x, y, 0);
        } else if (align == 2) {
            g.drawString(text, x, y, Graphics.TOP | Graphics.RIGHT);
        } else if (align == 1) {
            g.drawString(text, x, y, Graphics.TOP | Graphics.HCENTER);
        }
    }

    public int a(char ch) {
        return this.font.charWidth(ch);
    }
}



