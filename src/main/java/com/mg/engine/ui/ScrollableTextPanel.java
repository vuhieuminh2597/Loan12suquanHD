package com.mg.engine.ui;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdInputState;
import com.loan12suquan.hd.HdUiRenderer;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import javax.microedition.lcdui.Graphics;

/**
 * Scrollable multi-line antique text panel with golden title header and crisp Vietnamese pixel text.
 */
public class ScrollableTextPanel extends BaseScreen {
    private String[] wrappedLines;
    private int topY;
    private int bottomY;
    private String title;
    private int scrollOffsetY = 0;
    private int scrollStep = 4;
    private boolean autoScrollEnabled = false;
    private int autoScrollPauseTicks = 20;
    private int layoutMode = 0;
    protected int c = 12;
    protected int d;
    protected int e;
    protected int f;

    public ScrollableTextPanel(String title, String bodyText, int layoutMode) {
        this.d = GameConfig.themeColor;
        this.title = title;
        this.layoutMode = layoutMode;
        this.a(bodyText);
        this.computeBounds();
    }

    private void computeBounds() {
        int boldH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD);
        int plainH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_PLAIN);
        int lineStep = plainH + 2;
        if (this.layoutMode == 0) {
            this.topY = 0;
            this.bottomY = GameConfig.screenHeight - 23;
        } else {
            int lineCount = (this.wrappedLines != null && this.wrappedLines.length > 0) ? this.wrappedLines.length : 1;
            int headerH = (this.title != null && this.title.length() > 0) ? (boldH + 14) : 10;
            int contentHeight = headerH + lineCount * lineStep + 14;
            if (contentHeight < 68) {
                contentHeight = 68;
            }
            int availH = GameConfig.screenHeight - 28;
            if (contentHeight > availH) {
                contentHeight = availH;
            }
            this.topY = (availH - contentHeight) >> 1;
            this.bottomY = this.topY + contentHeight;
        }

        super.g = 8;
        super.a_ = this.topY;
        this.e = GameConfig.screenWidth - 16;
        this.f = this.bottomY - this.topY;
        if (this.layoutMode != 0) {
            super.b_ = this.e;
            super.c_ = this.f;
        }
    }

    protected void b(Graphics g) {
        DrawUtils.drawAntiquePanel(
                g, super.g, super.a_, this.e, this.f,
                0x362417, 0x22150D, 0x130B06,
                0xC29444, 0xF5D27E, 0xFFE680);
    }

    protected void e(Graphics g) {
        int boldH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD);
        int plainH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_PLAIN);
        int lineStep = plainH + 2;
        int centerX = super.g + (this.e >> 1);
        int bodyTopY;

        if (this.title != null && this.title.length() > 0) {
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_BOLD, this.title,
                    centerX, this.topY + 7, Graphics.TOP | Graphics.HCENTER,
                    0xFFE680, 0x120804);
            int divY = this.topY + boldH + 10;
            if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
                HdUiRenderer.drawHdDivider(g.getHdCanvas(), centerX, divY - 2, this.e - 24);
            } else {
                g.setColor(0x8F6932);
                g.drawLine(super.g + 12, divY, super.g + this.e - 12, divY);
            }
            bodyTopY = divY + 5;
        } else {
            bodyTopY = this.topY + 9;
        }

        int clipH = (this.topY + this.f - 8) - bodyTopY;
        if (clipH > 4 && this.wrappedLines != null) {
            g.setClip(super.g + 6, bodyTopY, this.e - 12, clipH);
            int drawY = bodyTopY + this.scrollOffsetY;
            for (int i = 0; i < this.wrappedLines.length; ++i) {
                if (drawY + plainH >= bodyTopY && drawY <= bodyTopY + clipH) {
                    DrawUtils.drawShadowText(
                            g, DrawUtils.UI_FONT_PLAIN, this.wrappedLines[i],
                            centerX, drawY, Graphics.TOP | Graphics.HCENTER,
                            0xFFF4DC, 0x120804);
                }
                drawY += lineStep;
            }
            g.setClip(0, 0, GameConfig.screenWidth, GameConfig.screenHeight);
        }
    }

    public void a() {
        int wheel = HdInputState.consumeWheelDelta();
        if (wheel != 0) {
            this.c(-wheel * 4);
        }
        if (this.autoScrollPauseTicks == 0 && this.autoScrollEnabled) {
            this.scrollOffsetY -= this.scrollStep / 2;
        }
        if (this.autoScrollPauseTicks > 0) {
            --this.autoScrollPauseTicks;
        }
    }

    public void a(String bodyText) {
        int wrapW = GameConfig.screenWidth - 36;
        this.wrappedLines = DrawUtils.wrapText(DrawUtils.UI_FONT_PLAIN, bodyText, wrapW);
    }

    public void a(int keyCode) {
        if (keyCode == 12) {
            this.autoScrollPauseTicks = 20;
            if (this.scrollOffsetY < 0) {
                this.scrollOffsetY += this.scrollStep;
            }
        } else if (keyCode == 13) {
            this.autoScrollPauseTicks = 20;
            if (this.autoScrollEnabled) {
                this.scrollOffsetY -= this.scrollStep;
            }
        }
        super.a(keyCode);
    }

    public void a(int pointerX, int pointerY) {
        this.autoScrollPauseTicks = 20;
        super.a(pointerX, pointerY);
    }

    public void c(int dragDeltaY) {
        this.autoScrollPauseTicks = 20;
        if (dragDeltaY > 0) {
            if (this.scrollOffsetY < 0) {
                this.scrollOffsetY += this.scrollStep * dragDeltaY;
                if (this.scrollOffsetY > 0) {
                    this.scrollOffsetY = 0;
                }
            }
        } else if (this.autoScrollEnabled) {
            this.scrollOffsetY -= -dragDeltaY * this.scrollStep;
        }
    }

    public void r() {
        this.title = null;
        this.wrappedLines = null;
        super.r();
    }

    public void a(Graphics g) {
        this.b(g);
        this.e(g);
    }
}
