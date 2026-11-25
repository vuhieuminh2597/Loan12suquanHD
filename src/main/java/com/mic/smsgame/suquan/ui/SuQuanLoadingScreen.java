package com.mic.smsgame.suquan.ui;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.platform.HdCanvas2D;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.FontManager;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.engine.graphics.Rect;
import com.mg.engine.ui.LoadingScreen;
import com.mg.engine.util.MathTrig;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Circular blue-crystal spinner loading screen displaying loading progress percentage. */
public class SuQuanLoadingScreen extends LoadingScreen {
    private static final int CRYSTAL_COUNT = 8;

    private final Image crystalSprite;
    private int activeIndex;
    private int trailingIndex;
    private final int[] dotX;
    private final int[] dotY;
    private final Rect spinnerBox;

    public SuQuanLoadingScreen() {
        super.t = 0x120A06;
        this.crystalSprite = MgImageLoader.load("/crystalblue");
        int diameter = 50;
        int boxSize = diameter + 20;
        this.spinnerBox = new Rect((super.b_ - boxSize) / 2, (super.c_ - boxSize) / 2, boxSize, boxSize);
        int centerX = this.spinnerBox.a + this.spinnerBox.c / 2;
        int centerY = this.spinnerBox.b + this.spinnerBox.d / 2;
        int radius = diameter / 2;
        int stepDeg = 360 / CRYSTAL_COUNT;
        int angleDeg = -90;

        this.dotX = new int[CRYSTAL_COUNT];
        this.dotY = new int[CRYSTAL_COUNT];
        for (int i = 0; i < CRYSTAL_COUNT; ++i) {
            this.dotX[i] = centerX + (radius * MathTrig.cos(angleDeg) >> MathTrig.FIXED_SHIFT);
            this.dotY[i] = centerY + (radius * MathTrig.sin(angleDeg) >> MathTrig.FIXED_SHIFT);
            angleDeg += stepDeg;
        }
        super.q = -1000;
    }

    public void a() {
        this.trailingIndex = this.activeIndex;
        this.activeIndex = (this.activeIndex + 1) % this.dotX.length;
        super.a();
    }

    protected void b(Graphics g) {
        if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
            g.setColor(0x120A06);
            g.fillRect(0, 0, super.b_, super.c_);
            HdCanvas2D g2 = g.getHdCanvas();
            int s = HdConfig.HD_SCALE;

            int cx = (super.b_ >> 1) * s;
            int cy = ((super.c_ >> 1) - 10) * s;
            g2.setColorHexAlpha(0xFFE680, 55);
            g2.setStrokeWidth(2.2f);
            g2.drawOval(cx - 86, cy - 86, 172, 172);
            g2.drawOval(cx - 64, cy - 64, 128, 128);

            for (int i = 0; i < this.dotX.length; ++i) {
                int px = this.dotX[i] * s;
                int py = (this.dotY[i] - 10) * s;
                boolean head = (i == this.activeIndex);
                boolean tail = (i == this.trailingIndex);
                int r = head ? 12 : (tail ? 9 : 6);
                if (head) {
                    g2.setColor(0xFFF5B8);
                } else if (tail) {
                    g2.setColor(0x4CE6D3);
                } else {
                    g2.setColorHexAlpha(0x8F6932, 140);
                }
                g2.fillOval(px - r, py - r, r * 2, r * 2);
            }

            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_BOLD, super.c + "%",
                    super.b_ >> 1, (super.c_ >> 1) - 16,
                    Graphics.TOP | Graphics.HCENTER,
                    0xFFE680, 0x0E0704);
            DrawUtils.drawRpgStatBar(
                    g, 36, (super.c_ >> 1) + 34, super.b_ - 72, 14,
                    super.c, 100,
                    0x7CFFDC, 0x1A8C78, 0x0D473D, 0xD6FFF5,
                    4, "Đang tải...");
            return;
        }

        g.setColor(0);
        g.fillRect(this.spinnerBox.a, this.spinnerBox.b, this.spinnerBox.c, this.spinnerBox.d);
        FontManager.primaryFont.a(
                g,
                String.valueOf(super.c),
                this.spinnerBox.a + (this.spinnerBox.c >> 1),
                this.spinnerBox.b - 4 + (this.spinnerBox.d >> 1),
                1
        );

        int frameW = this.crystalSprite.getWidth() / 3;
        int frameH = this.crystalSprite.getHeight();
        for (int i = this.dotX.length - 1; i >= 0; --i) {
            DrawUtils.drawRegion(g, this.crystalSprite, 0, 0, frameW, frameH, this.dotX[i], this.dotY[i], Graphics.HCENTER | Graphics.VCENTER);
        }
        DrawUtils.drawRegion(g, this.crystalSprite, frameW, 0, frameW, frameH, this.dotX[this.trailingIndex], this.dotY[this.trailingIndex], Graphics.HCENTER | Graphics.VCENTER);
        DrawUtils.drawRegion(g, this.crystalSprite, frameW * 2, 0, frameW, frameH, this.dotX[this.activeIndex], this.dotY[this.activeIndex], Graphics.HCENTER | Graphics.VCENTER);
    }
}


