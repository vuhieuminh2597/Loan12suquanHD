package com.mic.smsgame.suquan.ui;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdInputState;
import com.loan12suquan.hd.HdUiRenderer;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.engine.graphics.SmallNumberFont;
import com.mg.engine.sprite.AnimatedSprite;
import com.mg.engine.storage.EngineStorage;
import com.mg.engine.util.RandomUtils;
import com.mic.smsgame.suquan.data.CampaignData;
import com.mic.smsgame.suquan.gfx.AvatarRenderer;
import com.mic.smsgame.suquan.model.BattleFighter;
import com.mic.smsgame.suquan.model.HeroStats;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Expanded antique Arena ("Đấu trường") opponent selection panel (`218x222`) displaying
 * 5 challenge tiers, entry fees, Boss archetype badges, multi-line counter-strategy text,
 * EXP reward breakdown, and tap-outside-to-dismiss support.
 */
public class ArenaSelectPanel {
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int[] opponentAvatarIds;
    private boolean[] selectableFlags;
    private int[] entryCosts;
    private int selectedIndex;
    private SmallNumberFont smallNumberFont;
    private Image opponentBadgeSheet;
    private int badgeWidth;
    private int badgeHeight;
    private AnimatedSprite focusCursor;
    private Image dimMaskImage;
    private int playerGold;
    private HeroStats selectedOpponent;
    private int rewardExp;
    private Image goldIcon;
    private String selectedOpponentName;
    private String selectedOpponentPassive;
    private String[] wrappedTacticLines;
    private int animTick;
    private boolean[] unlockedFlags;
    private Image lockIcon;

    public ArenaSelectPanel(boolean[] unlockedFlags) {
        this.unlockedFlags = unlockedFlags;
        this.lockIcon = MgImageLoader.load("/m/lock");
        this.opponentBadgeSheet = MgImageLoader.load("/m/g");
        this.badgeWidth = this.opponentBadgeSheet.getWidth() / 5;
        this.badgeHeight = this.opponentBadgeSheet.getHeight();
        this.dimMaskImage = MgImageLoader.load("/m/b");
        this.smallNumberFont = new SmallNumberFont();

        this.panelWidth = 222;
        this.panelHeight = 236;
        this.panelX = (GameConfig.screenWidth - this.panelWidth) >> 1;
        this.panelY = ((GameConfig.screenHeight - 23) - this.panelHeight) >> 1;
        this.playerGold = EngineStorage.f(2);
        this.selectedIndex = -1;
        this.opponentAvatarIds = CampaignData.k();
        this.selectableFlags = new boolean[this.opponentAvatarIds.length];
        this.entryCosts = new int[this.opponentAvatarIds.length];

        for (int i = 0; i < this.selectableFlags.length; ++i) {
            this.entryCosts[i] = CampaignData.A(i);
            if (this.playerGold >= this.entryCosts[i] && unlockedFlags[i]) {
                if (this.selectedIndex < 0) {
                    this.selectedIndex = i;
                }
                this.selectableFlags[i] = true;
            }
        }

        this.goldIcon = MgImageLoader.load("/gold");
        this.selectOpponent(this.selectedIndex);

        this.focusCursor = new AnimatedSprite(MgImageLoader.load("/focusitem"), 2);
        this.focusCursor.c(2);
        this.focusCursor.b(true);
        this.focusCursor.a(new byte[][]{{0, 1}});
        this.focusCursor.a_(0);
    }

    public boolean a() {
        return this.selectedIndex >= 0;
    }

    public boolean isOutsidePanel(int pointerX, int pointerY) {
        return pointerY < GameConfig.screenHeight - 24
                && (pointerX < this.panelX || pointerX > this.panelX + this.panelWidth
                || pointerY < this.panelY || pointerY > this.panelY + this.panelHeight);
    }

    private void selectOpponent(int index) {
        if (index < 0) {
            return;
        }
        CampaignData.f = CampaignData.e(index, CampaignData.e.e);
        this.selectedOpponent = CampaignData.f;
        this.rewardExp = CampaignData.f(index, CampaignData.e.e);
        this.selectedOpponentName = AvatarRenderer.a(this.opponentAvatarIds[index]);
        BattleFighter preview = new BattleFighter(this.selectedOpponent);
        this.selectedOpponentPassive = preview.getPassiveName();
        String tactic = preview.isBossFighter()
                ? ("Khắc chế: " + preview.getBossCounterStrategy())
                : ("Nội tại: " + preview.getPassiveShortDesc());
        this.wrappedTacticLines = DrawUtils.wrapText(DrawUtils.UI_FONT_PLAIN, tactic, this.panelWidth - 24);
    }

    /** Deducts arena gold fee and initializes first-turn priority for the chosen match. */
    public void b() {
        if (this.selectedIndex < 0) {
            return;
        }
        if (EngineStorage.f(5) > 0) {
            CampaignData.h = true;
            EngineStorage.g(5);
        } else if (CampaignData.e.e > CampaignData.f.e) {
            CampaignData.h = true;
        } else if (CampaignData.e.e < CampaignData.f.e) {
            CampaignData.h = false;
        } else {
            CampaignData.h = RandomUtils.nextInt(2) == 0;
        }
        CampaignData.a = this.selectedIndex;
        EngineStorage.b(2, CampaignData.A(this.selectedIndex));
    }

    public void c() {
        ++this.animTick;
        if (this.selectedIndex >= 0) {
            this.focusCursor.e();
            int wheel = HdInputState.consumeWheelDelta();
            if (wheel != 0) {
                this.a(wheel > 0 ? 15 : 14);
            }
        }
    }

    public void a(int keyCode) {
        if (this.selectedIndex < 0) {
            return;
        }
        switch (keyCode) {
            case 14:
                do {
                    this.selectedIndex = (this.selectedIndex - 1 + this.opponentAvatarIds.length) % this.opponentAvatarIds.length;
                } while (!this.selectableFlags[this.selectedIndex]);
                break;
            case 15:
                do {
                    this.selectedIndex = (this.selectedIndex + 1) % this.opponentAvatarIds.length;
                } while (!this.selectableFlags[this.selectedIndex]);
                break;
        }
        this.selectOpponent(this.selectedIndex);
    }

    /** Handles direct touch on the 5 arena tier badges; returns true if the already-selected tier was tapped again. */
    public boolean a(int pointerX, int pointerY) {
        int badgeY = this.panelY + 30;
        if (pointerY >= badgeY - 4 && pointerY <= badgeY + this.badgeHeight + 16) {
            int badgeX = this.panelX + 14;
            for (int i = 0; i < this.opponentAvatarIds.length; ++i) {
                if (pointerX >= badgeX - 2 && pointerX <= badgeX + 34) {
                    if (this.unlockedFlags[i] && this.selectableFlags[i]) {
                        if (i == this.selectedIndex) {
                            return true;
                        }
                        this.selectedIndex = i;
                        this.selectOpponent(this.selectedIndex);
                    }
                    return false;
                }
                badgeX += 39;
            }
        }
        return false;
    }

    public void a(Graphics g) {
        int x = this.panelX;
        int y = this.panelY;
        int w = this.panelWidth;
        int h = this.panelHeight;
        int boldH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD);
        int plainH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_PLAIN);
        int centerX = x + (w >> 1);

        DrawUtils.drawAntiquePanel(
                g, x, y, w, h,
                0x362417, 0x22150D, 0x130B06,
                0xC29444, 0xF5D27E, 0xFFE680);

        int headerH = boldH + 6;
        DrawUtils.drawAntiquePanel(
                g, x + 6, y + 5, w - 12, headerH,
                0x1A5E54, 0x114039, 0x0A2622,
                0xB8893D, 0xF0C975, 0xFFFFFF);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, "ĐẤU TRƯỜNG LUYỆN BINH",
                centerX, y + 5 + ((headerH - boldH) >> 1),
                Graphics.TOP | Graphics.HCENTER,
                0xFFF4D0, 0x120804);

        // 5 Challenge Tier Badges
        boolean hd = HdConfig.isHdRemaster() && g.getHdCanvas() != null;
        int badgeY = y + 30;
        int badgeX = x + 14;
        for (int i = 0; i < this.opponentAvatarIds.length; ++i) {
            boolean selected = (i == this.selectedIndex);
            boolean hovered = HdInputState.isHovered(badgeX - 3, badgeY - 4, 36, 36);
            if (hd) {
                HdUiRenderer.drawHdItemSocket(
                        g.getHdCanvas(), badgeX - 3, badgeY - 4, 36, 36,
                        selected, hovered && this.unlockedFlags[i] && this.selectableFlags[i], false);
            }
            if (this.unlockedFlags[i]) {
                DrawUtils.drawRegion(
                        g, this.opponentBadgeSheet, i * this.badgeWidth, 0,
                        this.badgeWidth, this.badgeHeight, badgeX, badgeY, 0);
                if (!this.selectableFlags[i]) {
                    if (hd) {
                        HdUiRenderer.drawHdDimOverlay(g.getHdCanvas(), badgeX - 1, badgeY - 2, 32, 32, 0x0E0805, 165);
                    } else {
                        g.drawImage(this.dimMaskImage, badgeX, badgeY, 0);
                    }
                }
                this.smallNumberFont.a(g, this.entryCosts[i], badgeX + (this.badgeWidth >> 1), badgeY + 24, 1);
            } else {
                g.drawImage(this.lockIcon, badgeX, badgeY, 0);
            }
            if (!hd && selected) {
                this.focusCursor.a(g, badgeX - 3, badgeY - 4);
            }
            badgeX += 39;
        }

        int curY = y + 66;
        if (hd) {
            HdUiRenderer.drawHdDivider(g.getHdCanvas(), centerX, curY - 2, w - 20);
        } else {
            g.setColor(0x7A592A);
            g.drawLine(x + 10, curY, x + w - 10, curY);
        }
        curY += 5;

        if (this.selectedIndex < 0) {
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_BOLD, "Không đủ vàng để vào Đấu Trường!",
                    centerX, curY + 24, Graphics.TOP | Graphics.HCENTER,
                    0xFFAE70, 0x120804);
        } else {
            // Opponent Card
            DrawUtils.drawAntiquePanel(
                    g, x + 10, curY, 36, 36,
                    0x3D291A, 0x1C110A, 0x100905,
                    0xC29444, 0xF5D27E, 0xFFFFFF);
            AvatarRenderer.a(g, this.selectedOpponent.a, x + 13, curY + 3, true);

            int infoX = x + 52;
            DrawUtils.drawFullNameInBox(
                    g, this.selectedOpponentName,
                    infoX, curY, (x + w - 10) - infoX, boldH,
                    false, 0xFFE680, 0x120804, this.animTick);
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_PLAIN,
                    "Cấp " + this.selectedOpponent.e + "  |  " + this.selectedOpponentPassive,
                    infoX, curY + boldH + 1, Graphics.TOP | Graphics.LEFT,
                    0x8CF5E8, 0x120804);
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_PLAIN,
                    "Máu: " + this.selectedOpponent.c + "  |  Công: " + this.selectedOpponent.d,
                    infoX, curY + boldH + plainH + 2, Graphics.TOP | Graphics.LEFT,
                    0xFFF4DC, 0x120804);
            curY += 40;

            if (this.wrappedTacticLines != null && this.wrappedTacticLines.length > 0) {
                int tacticTextH = DrawUtils.getWrappedHeight(DrawUtils.UI_FONT_PLAIN, this.wrappedTacticLines);
                if (tacticTextH < plainH * 2) {
                    tacticTextH = plainH * 2;
                }
                int tacticBoxH = tacticTextH + 6;
                int maxBoxH = (y + h - 46) - curY;
                if (tacticBoxH > maxBoxH && maxBoxH > plainH + 6) {
                    tacticBoxH = maxBoxH;
                }
                DrawUtils.drawAntiquePanel(
                        g, x + 8, curY, w - 16, tacticBoxH,
                        0x26180F, 0x190F09, 0x0E0804,
                        0x8F6932, 0xCCA052, 0xFFE680);
                DrawUtils.drawWrappedShadowText(
                        g, DrawUtils.UI_FONT_PLAIN, this.wrappedTacticLines,
                        x + 12, curY + 3, w - 24, tacticBoxH - 4,
                        0xFFD980, 0x100603);
                curY += tacticBoxH + 5;
            }
        }

        int footerY = y + h - 42;
        g.setColor(0x7A592A);
        g.drawLine(x + 10, footerY, x + w - 10, footerY);
        footerY += 4;

        if (this.selectedIndex >= 0) {
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_BOLD,
                    "Thưởng: +" + this.rewardExp + " EXP (" + CampaignData.B(this.selectedIndex) + "% cấp)",
                    x + 10, footerY, Graphics.TOP | Graphics.LEFT,
                    0x7CFF9E, 0x120804);
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_PLAIN,
                    "Phí vào: " + this.entryCosts[this.selectedIndex] + " vàng (Không mất Máu thật)",
                    x + 10, footerY + boldH + 2, Graphics.TOP | Graphics.LEFT,
                    0xD8C8B0, 0x120804);
        }

        // Current player Gold badge at top-right inside card
        String goldText = "" + this.playerGold;
        int gw = DrawUtils.getStringWidth(DrawUtils.UI_FONT_BOLD, goldText);
        g.drawImage(this.goldIcon, x + w - gw - 30, y + h - 22, 0);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, goldText,
                x + w - 10, y + h - 19, Graphics.TOP | Graphics.RIGHT,
                0xFFE680, 0x120804);
    }
}
