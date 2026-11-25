package com.mg.engine.ui;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdInputState;
import com.loan12suquan.hd.HdUiRenderer;
import com.mg.engine.core.ActionListener;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.engine.storage.EngineStorage;
import com.mg.engine.util.ByteUtils;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Startup sound volume & On/Off selection screen upgraded to 100% HD "Cổ Phong Hoàng Kim"
 * with an Imperial Đông Sơn Bronze Drum & Bell acoustic medallion, live drag slider, and hover feedback.
 */
public class SoundConfigScreen extends BaseScreen implements ActionListener {
    private static final int ACTION_CONFIRM = 1;
    private static final int ACTION_MUTE = 2;
    private static final int ACTION_UNMUTE = 3;

    private static final int CARD_W = 216;
    private static final int CARD_H = 196;
    private static final int BAR_W = 124;
    private static final int BAR_H = 16;
    private static final int STEP_BTN_W = 26;
    private static final int STEP_BTN_H = 22;

    private int selectedVolume;
    private SoftKeyButton enableButton;
    private SoftKeyButton disableButton;
    private Image soundIcon;
    private Image menuArt;
    private int nextScreenId;
    private int animTick;

    public SoundConfigScreen() {
        this(2);
    }

    public SoundConfigScreen(int nextScreenId) {
        this.a(new SoftKeyBar());
        byte[] saved = EngineStorage.o();
        GameConfig.soundVolume = saved == null ? 80 : ByteUtils.a(saved);
        if (GameConfig.soundVolume < 0) {
            GameConfig.soundVolume = 0;
        } else if (GameConfig.soundVolume > 100) {
            GameConfig.soundVolume = 100;
        }

        this.selectedVolume = GameConfig.soundVolume;
        this.soundIcon = MgImageLoader.load("/_sound");
        this.menuArt = MgImageLoader.load("/bkmenu");

        this.c(new TextSoftKeyButton("Vào game", ACTION_CONFIRM));
        this.enableButton = new TextSoftKeyButton("Bật âm", ACTION_UNMUTE);
        this.disableButton = new TextSoftKeyButton("Tắt âm", ACTION_MUTE);
        this.nextScreenId = nextScreenId;
        this.syncMuteSoftKey();
        this.a((ActionListener) this);
    }

    private void syncMuteSoftKey() {
        if (this.selectedVolume <= 0) {
            this.a(this.enableButton);
        } else {
            this.a(this.disableButton);
        }
    }

    private void setVolumeClamped(int vol) {
        if (vol < 0) {
            vol = 0;
        } else if (vol > 100) {
            vol = 100;
        }
        this.selectedVolume = vol;
        this.syncMuteSoftKey();
    }

    public void a(int keyIndex) {
        super.a(keyIndex);
        if (keyIndex == 14 || keyIndex == 4 || keyIndex == 13 || keyIndex == 8) {
            this.setVolumeClamped(this.selectedVolume - 10);
        } else if (keyIndex == 15 || keyIndex == 6 || keyIndex == 12 || keyIndex == 2) {
            this.setVolumeClamped(this.selectedVolume + 10);
        }
    }

    public void a(int x, int y) {
        super.a(x, y);
        int cardX = (this.b_ - CARD_W) >> 1;
        int cardY = ((this.c_ - 23) - CARD_H) >> 1;
        int barX = (this.b_ - BAR_W) >> 1;
        int barY = cardY + 138;
        int minusX = barX - STEP_BTN_W - 6;
        int plusX = barX + BAR_W + 6;
        int btnY = barY - 3;

        int iconCx = this.b_ >> 1;
        int iconCy = cardY + 80;
        if (x >= iconCx - 44 && x <= iconCx + 44 && y >= iconCy - 42 && y <= iconCy + 42) {
            this.setVolumeClamped(this.selectedVolume > 0 ? 0 : 80);
            return;
        }

        if (y >= btnY - 6 && y <= btnY + STEP_BTN_H + 6) {
            if (x >= minusX - 4 && x <= minusX + STEP_BTN_W + 2) {
                this.setVolumeClamped(this.selectedVolume - 20);
            } else if (x >= plusX - 2 && x <= plusX + STEP_BTN_W + 4) {
                this.setVolumeClamped(this.selectedVolume + 20);
            } else if (x >= barX && x <= barX + BAR_W) {
                updateSliderFromX(x, barX);
            }
        }
        if (x >= cardX + 20 && x <= cardX + CARD_W - 20 && y >= cardY + 164 && y <= cardY + CARD_H - 4) {
            this.onAction(this.q, ACTION_CONFIRM);
        }
    }

    public void b(int dragX, int dragY) {
        int cardY = ((this.c_ - 23) - CARD_H) >> 1;
        int barX = (this.b_ - BAR_W) >> 1;
        int barY = cardY + 138;
        if (dragY >= barY - 14 && dragY <= barY + BAR_H + 14 && dragX >= barX - 8 && dragX <= barX + BAR_W + 8) {
            updateSliderFromX(dragX, barX);
        }
    }

    private void updateSliderFromX(int x, int barX) {
        int pct = (x - barX) * 100 / BAR_W;
        if (pct < 6) {
            pct = 0;
        } else if (pct > 94) {
            pct = 100;
        }
        this.setVolumeClamped(pct);
    }

    public void a() {
        ++this.animTick;
        int wheel = HdInputState.consumeWheelDelta();
        if (wheel != 0) {
            this.setVolumeClamped(this.selectedVolume - wheel * 10);
        }
    }

    public void a(Graphics g) {
        g.setColor(0x160D08);
        g.fillRect(0, 0, this.b_, this.c_);
        if (this.menuArt != null) {
            g.drawImage(this.menuArt, GameConfig.contentOffsetX, GameConfig.contentOffsetY, 0);
        }
        DrawUtils.drawHatchedOverlay(g, 0x120905, 0, 0, this.b_, this.c_);

        int cardX = (this.b_ - CARD_W) >> 1;
        int cardY = ((this.c_ - 23) - CARD_H) >> 1;
        int boldH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD);

        DrawUtils.drawAntiquePanel(
                g, cardX, cardY, CARD_W, CARD_H,
                0x362417, 0x22150D, 0x130B06,
                0xC29444, 0xF5D27E, 0xFFE680);

        int headerH = boldH + 8;
        DrawUtils.drawAntiquePanel(
                g, cardX + 6, cardY + 6, CARD_W - 12, headerH,
                0x1A5E54, 0x114039, 0x0A2622,
                0xB8893D, 0xF0C975, 0xFFFFFF);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, "THIẾT LẬP ÂM THANH",
                this.b_ >> 1, cardY + 6 + ((headerH - boldH) >> 1),
                Graphics.TOP | Graphics.HCENTER,
                0xFFF4D0, 0x120804);

        // Speaker / Đông Sơn Bronze Drum Medallion
        if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
            HdUiRenderer.drawHdSoundMedallion(g.getHdCanvas(), this.b_ >> 1, cardY + 80, this.selectedVolume, this.animTick);
        } else {
            int iconSrcX = this.selectedVolume > 0 ? 0 : 100;
            DrawUtils.drawRegion(
                    g, this.soundIcon, iconSrcX, 0, 100, 100,
                    this.b_ >> 1, cardY + 80,
                    Graphics.HCENTER | Graphics.VCENTER);
        }

        // [-] Slider [+] row
        int barX = (this.b_ - BAR_W) >> 1;
        int barY = cardY + 138;
        int minusX = barX - STEP_BTN_W - 5;
        int plusX = barX + BAR_W + 5;
        int btnY = barY - 3;

        boolean minusHover = HdInputState.isHovered(minusX, btnY, STEP_BTN_W, STEP_BTN_H);
        boolean plusHover = HdInputState.isHovered(plusX, btnY, STEP_BTN_W, STEP_BTN_H);

        DrawUtils.drawAntiquePanel(
                g, minusX, btnY, STEP_BTN_W, STEP_BTN_H,
                minusHover ? 0x246B5C : 0x422B1B,
                minusHover ? 0x16473D : 0x28190F,
                0x140C07,
                minusHover ? 0xFFD700 : 0xB8893D,
                0xF0C975, 0xFFE680);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, "-",
                minusX + (STEP_BTN_W >> 1), btnY + ((STEP_BTN_H - boldH) >> 1),
                Graphics.TOP | Graphics.HCENTER,
                0xFFF4D0, 0x120804);

        DrawUtils.drawAntiquePanel(
                g, plusX, btnY, STEP_BTN_W, STEP_BTN_H,
                plusHover ? 0x246B5C : 0x422B1B,
                plusHover ? 0x16473D : 0x28190F,
                0x140C07,
                plusHover ? 0xFFD700 : 0xB8893D,
                0xF0C975, 0xFFE680);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, "+",
                plusX + (STEP_BTN_W >> 1), btnY + ((STEP_BTN_H - boldH) >> 1),
                Graphics.TOP | Graphics.HCENTER,
                0xFFF4D0, 0x120804);

        String volLabel = this.selectedVolume > 0 ? ("Âm lượng: " + this.selectedVolume + "%") : "Đang tắt tiếng";
        DrawUtils.drawRpgStatBar(
                g, barX, barY, BAR_W, BAR_H,
                this.selectedVolume, 100,
                0x7CF5E6, 0x1D7A68, 0x0E3D34, 0xD6FFFA,
                4, volLabel);

        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_PLAIN, "Kéo thanh trượt hoặc dùng F3 mọi lúc trong game",
                this.b_ >> 1, cardY + 168,
                Graphics.TOP | Graphics.HCENTER,
                0xD8C8B0, 0x120804);
    }

    public void onAction(int screenId, int actionId) {
        if (actionId == ACTION_CONFIRM) {
            if (GameConfig.soundVolume != this.selectedVolume) {
                EngineStorage.d(this.selectedVolume);
            }
            GameConfig.soundVolume = this.selectedVolume;
            GameConfig.soundEnabled = GameConfig.soundVolume > 0;
            this.proceedToNextScreen();
        } else if (actionId == ACTION_MUTE) {
            this.setVolumeClamped(0);
        } else if (actionId == ACTION_UNMUTE) {
            this.setVolumeClamped(80);
        }
    }

    public void proceedToNextScreen() {
        GUIManager.getInstance().e.a(1, this.nextScreenId, false);
    }
}
