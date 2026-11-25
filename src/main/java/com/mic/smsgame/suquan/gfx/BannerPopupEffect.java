package com.mic.smsgame.suquan.gfx;

import com.loan12suquan.hd.HdBattleStageRenderer;
import com.loan12suquan.hd.HdConfig;
import com.mg.engine.sprite.AnimatedSprite;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Centered 3-frame banner popup used for battle announcements (Win, Lose, Extra Turn, etc.).
 */
public class BannerPopupEffect extends AnimatedSprite {
    private static final int STATE_POP_IN = 0;
    private static final int STATE_HOLD = 1;
    private static final int HOLD_TICKS = 45;

    private int holdTicksRemaining;
    private boolean hdResultMode;
    private boolean hdVictoryFlag;

    public BannerPopupEffect(int centerX, int centerY) {
        this.d(centerX, centerY);
        this.animFrames = new byte[][]{{0, 0, 0, 2, 1, 2}, {2}};
        this.visible = false;
        this.anchor = Graphics.HCENTER | Graphics.VCENTER;
    }

    /** Shows the given 3-frame banner sprite and returns total animation ticks. */
    public int a(Image bannerSheet) {
        this.hdResultMode = false;
        this.a(bannerSheet, 3);
        this.visible = true;
        this.a_(STATE_POP_IN);
        this.frameIndex = 0;
        this.holdTicksRemaining = HOLD_TICKS;
        return this.animFrames[STATE_POP_IN].length + this.holdTicksRemaining;
    }

    public int showResultBanner(Image bannerSheet, boolean victory) {
        int ticks = this.a(bannerSheet);
        this.hdResultMode = true;
        this.hdVictoryFlag = victory;
        return ticks;
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        if (this.visible && this.hdResultMode && HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
            float scale = 1.0f;
            if (this.animState == STATE_POP_IN) {
                int len = this.animFrames[STATE_POP_IN].length;
                scale = 0.76f + 0.24f * ((float) (this.frameIndex + 1) / Math.max(1, len));
            }
            HdBattleStageRenderer.drawHdResultBanner(
                    g.getHdCanvas(),
                    this.x + offsetX,
                    this.y + offsetY,
                    this.hdVictoryFlag,
                    scale);
            return;
        }
        super.a(g, offsetX, offsetY);
    }

    public void a() {
        switch (this.animState) {
            case STATE_POP_IN:
                if (this.frameIndex >= this.animFrames[STATE_POP_IN].length - 1) {
                    this.a_(STATE_HOLD);
                }
                break;
            case STATE_HOLD:
                if (this.holdTicksRemaining > 0) {
                    --this.holdTicksRemaining;
                    if (this.holdTicksRemaining == 0) {
                        this.spriteSheet = null;
                        this.visible = false;
                    }
                }
                break;
        }
    }
}

