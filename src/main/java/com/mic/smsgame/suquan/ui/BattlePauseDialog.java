package com.mic.smsgame.suquan.ui;

import com.loan12suquan.hd.HdInputState;
import com.mg.engine.audio.BgmPlayer;
import com.mg.engine.core.ActionListener;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.ui.BaseDialog;
import com.mg.engine.ui.SoftKeyBar;
import com.mg.engine.ui.TextSoftKeyButton;
import com.mic.smsgame.suquan.model.BattleFighter;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Antique RPG in-battle Pause Menu ("Tạm dừng trận đấu") providing 1-tap access to:
 * 1. Resume battle, 2. Inspect both combatants' stats/counter-strategies,
 * 3. Adjust live Sound/SFX volume (0%..100%), and 4. Save snapshot (`SQV6`) & return to Main Menu.
 */
public class BattlePauseDialog extends BaseDialog implements ActionListener {
    private static final int ACTION_EXEC_SELECTED = 10;
    private static final int ACTION_RESUME = 11;

    private static final int ROW_RESUME = 0;
    private static final int ROW_VIEW_STATS = 1;
    private static final int ROW_SOUND = 2;
    private static final int ROW_SAVE_AND_EXIT = 3;
    private static final int ROW_COUNT = 4;

    private final ActionListener controllerListener;
    private final BattleFighter playerFighter;
    private final Image playerPortrait;
    private final BattleFighter enemyFighter;
    private final Image enemyPortrait;
    private int selectedRow = 0;

    private final int headerH;
    private final int rowH = 27;
    private final int rowGap = 5;

    public BattlePauseDialog(
            ActionListener controllerListener,
            BattleFighter playerFighter,
            Image playerPortrait,
            BattleFighter enemyFighter,
            Image enemyPortrait) {
        super(1);
        this.controllerListener = controllerListener;
        this.playerFighter = playerFighter;
        this.playerPortrait = playerPortrait;
        this.enemyFighter = enemyFighter;
        this.enemyPortrait = enemyPortrait;

        int boldH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD);
        this.headerH = boldH + 7;

        super.b_ = 208;
        super.c_ = 12 + this.headerH + 8 + ROW_COUNT * this.rowH + (ROW_COUNT - 1) * this.rowGap + 8;
        super.g = (GameConfig.screenWidth - super.b_) >> 1;
        super.a_ = ((GameConfig.screenHeight - 23) - super.c_) >> 1;
        super.redrawParentScreen = true;

        this.a(new SoftKeyBar());
        this.c(new TextSoftKeyButton("Chọn", ACTION_EXEC_SELECTED));
        this.b(new TextSoftKeyButton("Tiếp tục", ACTION_RESUME));
        this.a((ActionListener) this);
    }

    protected void e() {
        int wheel = HdInputState.consumeWheelDelta();
        if (wheel != 0) {
            this.selectedRow = (this.selectedRow + (wheel > 0 ? 1 : -1) + ROW_COUNT) % ROW_COUNT;
        }
        int firstRowY = super.a_ + 6 + this.headerH + 8;
        int rowX = super.g + 10;
        int rowW = super.b_ - 20;
        for (int i = 0; i < ROW_COUNT; ++i) {
            int ry = firstRowY + i * (this.rowH + this.rowGap);
            if (HdInputState.isHovered(rowX, ry, rowW, this.rowH)) {
                this.selectedRow = i;
                break;
            }
        }
    }

    public void a(int keyCode) {
        switch (keyCode) {
            case 12: // UP
                this.selectedRow = (this.selectedRow - 1 + ROW_COUNT) % ROW_COUNT;
                break;
            case 13: // DOWN
                this.selectedRow = (this.selectedRow + 1) % ROW_COUNT;
                break;
            case 14: // LEFT on Sound row
                if (this.selectedRow == ROW_SOUND) {
                    int v = (GameConfig.soundEnabled ? GameConfig.soundVolume : 0) - 25;
                    BgmPlayer.getInstance().setLiveVolume(v < 0 ? 0 : v);
                }
                break;
            case 15: // RIGHT on Sound row
                if (this.selectedRow == ROW_SOUND) {
                    int v = (GameConfig.soundEnabled ? GameConfig.soundVolume : 0) + 25;
                    BgmPlayer.getInstance().setLiveVolume(v > 100 ? 100 : v);
                }
                break;
        }
    }

    public void a(int pointerX, int pointerY) {
        // Tap outside dialog frame -> resume battle immediately
        if (pointerY < GameConfig.screenHeight - 24
                && (pointerX < super.g || pointerX > super.g + super.b_
                || pointerY < super.a_ || pointerY > super.a_ + super.c_)) {
            this.resumeBattle();
            return;
        }
        int firstRowY = super.a_ + 6 + this.headerH + 8;
        int rowX = super.g + 10;
        int rowW = super.b_ - 20;
        if (pointerX >= rowX && pointerX <= rowX + rowW) {
            for (int i = 0; i < ROW_COUNT; ++i) {
                int ry = firstRowY + i * (this.rowH + this.rowGap);
                if (pointerY >= ry && pointerY <= ry + this.rowH) {
                    this.selectedRow = i;
                    this.executeRow(i);
                    return;
                }
            }
        }
    }

    private void resumeBattle() {
        if (this.controllerListener != null) {
            this.controllerListener.onAction(0, -2);
        } else {
            GUIManager.getInstance().a(true);
        }
    }

    private void executeRow(int row) {
        switch (row) {
            case ROW_RESUME:
                this.resumeBattle();
                break;
            case ROW_VIEW_STATS:
                GUIManager.getInstance().a(false);
                FighterStatsDialog stats = new FighterStatsDialog(
                        this.playerFighter,
                        this.playerPortrait,
                        this.enemyFighter,
                        this.enemyPortrait,
                        1);
                GUIManager.getInstance().a(stats, false);
                break;
            case ROW_SOUND:
                BgmPlayer.getInstance().cycleLiveVolumePreset();
                break;
            case ROW_SAVE_AND_EXIT:
                if (this.controllerListener != null) {
                    this.controllerListener.onAction(0, -1);
                }
                break;
        }
    }

    public void onAction(int screenId, int actionId) {
        if (actionId == ACTION_RESUME) {
            this.resumeBattle();
        } else if (actionId == ACTION_EXEC_SELECTED) {
            this.executeRow(this.selectedRow);
        }
    }

    public void a(Graphics g) {
        int x = super.g;
        int y = super.a_;
        int w = super.b_;
        int h = super.c_;
        Font boldFont = DrawUtils.UI_FONT_BOLD;
        int boldH = DrawUtils.getFontHeight(boldFont);

        DrawUtils.drawAntiquePanel(
                g, x, y, w, h,
                0x362417, 0x22150D, 0x130B06,
                0xC29444, 0xF5D27E, 0xFFE680);

        DrawUtils.drawAntiquePanel(
                g, x + 6, y + 6, w - 12, this.headerH,
                0x1A5E54, 0x114039, 0x0A2622,
                0xB8893D, 0xF0C975, 0xFFFFFF);
        DrawUtils.drawShadowText(
                g, boldFont, "TẠM DỪNG TRẬN ĐẤU",
                x + (w >> 1), y + 6 + ((this.headerH - boldH) >> 1),
                Graphics.TOP | Graphics.HCENTER,
                0xFFF4D0, 0x120804);

        int vol = GameConfig.soundEnabled ? GameConfig.soundVolume : 0;
        String[] labels = new String[]{
                "Tiếp tục chiến đấu",
                "Xem thông tin 2 phe",
                vol > 0 ? ("Âm thanh: " + vol + "% [Đổi]") : "Âm thanh: Đang tắt [Bật]",
                "Lưu trận & Về Menu"
        };

        int rowX = x + 10;
        int rowW = w - 20;
        int curY = y + 6 + this.headerH + 8;
        for (int i = 0; i < ROW_COUNT; ++i) {
            boolean sel = (i == this.selectedRow);
            boolean exitRow = (i == ROW_SAVE_AND_EXIT);
            int topBg = sel ? (exitRow ? 0x6E2028 : 0x1F6355) : (exitRow ? 0x45181D : 0x2B1B10);
            int botBg = sel ? (exitRow ? 0x471217 : 0x123D34) : (exitRow ? 0x2B0E11 : 0x1B1009);
            int border = sel ? 0xFFD700 : (exitRow ? 0x9E5942 : 0x7A592A);
            DrawUtils.drawAntiquePanel(
                    g, rowX, curY, rowW, this.rowH,
                    topBg, botBg, 0x0E0804,
                    border, sel ? 0xFFF2A6 : 0xCCA052, 0xFFFFFF);
            DrawUtils.drawShadowText(
                    g, boldFont, labels[i],
                    rowX + (rowW >> 1), curY + ((this.rowH - boldH) >> 1),
                    Graphics.TOP | Graphics.HCENTER,
                    sel ? 0xFFF5B8 : (exitRow ? 0xFFE4D6 : 0xF2E2C4), 0x120804);
            curY += this.rowH + this.rowGap;
        }
    }
}
