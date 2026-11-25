package com.mic.smsgame.suquan.gfx;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdFontRenderer;
import com.loan12suquan.hd.platform.HdCanvas2D;
import com.mg.engine.graphics.BattleResourceManager;
import com.mg.engine.graphics.FontManager;
import com.mg.engine.graphics.SmallNumberFont;
import com.mg.engine.sprite.Drawable;
import javax.microedition.lcdui.Graphics;

/**
 * Countdown {@code mm:ss} battle turn timer widget with blinking warning below 30s and 10s.
 */
public class BattleTimerWidget extends Drawable {
    private static final int TICKS_PER_SECOND = 25;
    private static final int FAST_BLINK_THRESHOLD = 250; // 10 seconds
    private static final int SLOW_BLINK_THRESHOLD = 750; // 30 seconds

    private boolean showDigits;
    private int remainingTicks;
    private SmallNumberFont numberFont;

    public int a() {
        return this.remainingTicks;
    }

    public void a(int remainingTicks) {
        this.remainingTicks = remainingTicks;
    }

    public BattleTimerWidget(int x, int y) {
        this.x = x;
        this.y = y;
        this.numberFont = new SmallNumberFont(BattleResourceManager.a().B);
    }

    public void b(int durationTicks) {
        this.remainingTicks = durationTicks;
        this.showDigits = durationTicks >= 0;
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (this.remainingTicks < 0) {
            return;
        }
        int totalSeconds = this.remainingTicks / TICKS_PER_SECOND;
        if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
            HdCanvas2D g2 = g.getHdCanvas();
            int s = HdConfig.HD_SCALE;
            boolean urgent = (this.remainingTicks > 0 && totalSeconds <= 5);
            int pulse = (this.remainingTicks >> 1) & 1;
            int px = (this.x - 20) * s;
            int py = 223 * s;
            int pw = 40 * s;
            int ph = 11 * s;
            if (urgent) {
                g2.setColor(pulse == 0 ? 0x6E1212 : 0x3D140A);
            } else {
                g2.setColorRgba(20, 11, 6, 225);
            }
            g2.fillRoundRect(px, py, pw, ph, 12);
            g2.setColor(urgent ? (pulse == 0 ? 0xFF4D4D : 0xFFD700) : 0xC89842);
            g2.setStrokeWidth(2.0f);
            g2.drawRoundRect(px, py, pw, ph, 12);
            if (this.showDigits) {
                int minutes = totalSeconds / 60;
                int seconds = totalSeconds % 60;
                String timeStr = (minutes < 10 ? ("0" + minutes) : ("" + minutes))
                        + ":" + (seconds < 10 ? ("0" + seconds) : ("" + seconds));
                HdFontRenderer.drawSubtitleHd(
                        g2, timeStr,
                        this.x * s, py + 24,
                        true,
                        urgent ? 0xFFB8B8 : 0xFFF5B8,
                        0x0D0603);
            }
            return;
        }
        if (this.remainingTicks > 0 && totalSeconds <= 5) {
            int pulse = (this.remainingTicks >> 1) & 1;
            g.setColor(pulse == 0 ? 0x6E1212 : 0x3D140A);
            g.fillRect(this.x - 19, this.y - 2, 38, 12);
            g.setColor(pulse == 0 ? 0xFF4D4D : 0xFFD700);
            g.drawRect(this.x - 19, this.y - 2, 37, 11);
        }
        if (!this.showDigits) {
            return;
        }
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        FontManager.primaryFont.a(g, ":", this.x, this.y - 4, 1);
        this.numberFont.a(g, minutes, this.x - 2, this.y, 2);
        this.numberFont.a(g, seconds, this.x + 2, this.y, 0);
    }

    public void b() {
        if (this.remainingTicks <= 0) {
            return;
        }
        --this.remainingTicks;
        if (this.remainingTicks == 0) {
            this.showDigits = true;
        } else if (this.remainingTicks < FAST_BLINK_THRESHOLD) {
            if (this.remainingTicks % 3 == 0) {
                this.showDigits = !this.showDigits;
            }
        } else if (this.remainingTicks < SLOW_BLINK_THRESHOLD && this.remainingTicks % 8 == 0) {
            this.showDigits = !this.showDigits;
        }
    }
}

