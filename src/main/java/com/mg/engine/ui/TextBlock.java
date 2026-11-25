package com.mg.engine.ui;

import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.FontManager;
import com.mg.engine.graphics.GameFont;
import com.mg.engine.graphics.Rect;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Text paragraph or titled section block inside a {@link RichContentDialog}, rendered with crisp Vietnamese pixel text.
 */
public class TextBlock extends ContentBlock {
    private String rawText = null;
    private String[] wrappedLines;
    private boolean hasHeaderLine;
    private int alignment;
    private GameFont bodyFont;
    public int d = 0x1A5E54;
    private GameFont headerFont;

    public void a(GameFont font) {
        this.bodyFont = font;
    }

    private void initDefaultFonts() {
        this.bodyFont = FontManager.secondaryFont;
        this.headerFont = FontManager.primaryFont;
    }

    public TextBlock(String rawText, int alignment) {
        this.rawText = rawText;
        this.alignment = alignment;
        this.initDefaultFonts();
    }

    public TextBlock(String[] lines, boolean hasHeaderLine, int alignment) {
        this.wrappedLines = lines;
        this.hasHeaderLine = hasHeaderLine;
        this.alignment = alignment;
        this.initDefaultFonts();
    }

    private boolean isHighlightedBody() {
        return this.bodyFont == FontManager.primaryFont;
    }

    public void a(Rect viewport) {
        super.a(viewport);
        Font pixelFont = this.isHighlightedBody() ? DrawUtils.UI_FONT_BOLD : DrawUtils.UI_FONT_PLAIN;
        int lineStep = DrawUtils.getFontHeight(pixelFont) + 2;
        if (this.rawText != null) {
            this.wrappedLines = DrawUtils.wrapText(pixelFont, this.rawText, viewport.c - 4);
        }
        if (this.wrappedLines != null) {
            super.b = this.wrappedLines.length * lineStep;
            this.rawText = null;
        }
    }

    public void a(Graphics g, int x, int y) {
        if (this.wrappedLines == null) {
            return;
        }
        int startLine = 0;
        int bottomLimit = super.c.b + super.c.d;
        int anchor = Graphics.TOP | Graphics.LEFT;
        int drawX = x;
        if (this.alignment == 1) {
            anchor = Graphics.TOP | Graphics.HCENTER;
            drawX = x + (super.c.c >> 1);
        } else if (this.alignment == 2) {
            anchor = Graphics.TOP | Graphics.RIGHT;
            drawX = x + super.c.c;
        }

        if (this.hasHeaderLine && this.wrappedLines.length > 0) {
            startLine = 1;
            int headerW = DrawUtils.getStringWidth(DrawUtils.UI_FONT_BOLD, this.wrappedLines[0]) + 8;
            int headerH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD) + 2;
            if (this.d >= 0) {
                int bgX = (this.alignment == 1) ? (drawX - (headerW >> 1))
                        : (this.alignment == 2 ? (drawX - headerW) : drawX);
                g.setColor(0x1A5E54);
                g.fillRect(bgX, y - 1, headerW, headerH);
            }
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_BOLD, this.wrappedLines[0],
                    drawX, y, anchor, 0xFFE680, 0x120804);
            y += headerH + 2;
        }

        boolean highlighted = this.isHighlightedBody();
        Font pixelFont = highlighted ? DrawUtils.UI_FONT_BOLD : DrawUtils.UI_FONT_PLAIN;
        int lineStep = DrawUtils.getFontHeight(pixelFont) + 2;
        int textColor = highlighted ? 0xFFE680 : 0xFFF4DC;
        for (int i = startLine; i < this.wrappedLines.length && y <= bottomLimit; ++i) {
            DrawUtils.drawShadowText(g, pixelFont, this.wrappedLines[i], drawX, y, anchor, textColor, 0x120804);
            y += lineStep;
        }
    }

    public void b() {
    }
}
