package com.mic.smsgame.suquan.ui;

import com.mg.engine.audio.SfxPlayer;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.engine.storage.EngineStorage;
import com.mg.engine.ui.BaseDialog;
import com.mg.engine.ui.SoftKeyBar;
import com.mg.engine.ui.SoftKeyButton;
import com.mic.smsgame.suquan.data.CampaignData;
import com.mic.smsgame.suquan.gfx.AvatarRenderer;
import com.mic.smsgame.suquan.model.HeroStats;
import com.mic.smsgame.suquan.vfx.ComboBurstEffect;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Post-battle summary dialog animating gold/EXP tallying, true-to-scale HP & within-level EXP gauges,
 * persistent reward breakdown figures, and level-up / 10-level breakthrough celebrations.
 */
public class BattleResultDialog extends BaseDialog {
    private static final int BAR_HEIGHT = 15;

    private Image goldIcon;
    private Image expBookIcon;
    private HeroStats heroPreview;
    private int currentExp;
    private int currentGold;
    private int nextLevelExp;
    private int currentLevelBaseExp;

    // Remaining amounts to animate into currentGold / currentExp
    private int remainingGoldToTally;
    private int remainingWinBonusGoldToTally;
    private int remainingQuestGoldToTally;
    private int remainingCollectedExpToTally;
    private int remainingWinBonusExpToTally;
    private int remainingQuestExpToTally;

    // Persistent earned totals displayed in the breakdown list (never decay to +0)
    private int displayCollectedGold;
    private int displayWinBonusGold;
    private int displayQuestBonusGold;
    private int displayCollectedExp;
    private int displayWinBonusExp;
    private int displayQuestBonusExp;

    private int initialDelayTicks;
    private SoftKeyButton pendingCenterButton;
    private boolean questRewardMode;
    private ComboBurstEffect levelUpBurst;
    private SfxPlayer levelUpSfx;
    private int breakthroughReachedLevel = 0;
    private int earnedShards = 0;
    private int labelColW;

    public BattleResultDialog(boolean questRewardMode) {
        super(1);
        this.questRewardMode = questRewardMode;
        this.earnedShards = questRewardMode ? 0 : (CampaignData.battleDroppedShards + CampaignData.battleWinBonusShards);
        super.b_ = 220;
        super.c_ = this.earnedShards > 0 ? 256 : 240;
        if (super.c_ > GameConfig.screenHeight - 28) {
            super.c_ = GameConfig.screenHeight - 28;
        }
        super.g = (GameConfig.screenWidth - super.b_) >> 1;
        super.a_ = ((GameConfig.screenHeight - 23) - super.c_) >> 1;

        if (CampaignData.a >= 0) {
            CampaignData.e.c = CampaignData.b;
        }
        this.heroPreview = CampaignData.e.c();
        if (this.heroPreview.c > this.heroPreview.b) {
            this.heroPreview.c = this.heroPreview.b;
        }

        this.currentExp = CampaignData.b();
        this.currentGold = EngineStorage.f(2);
        this.goldIcon = MgImageLoader.load("/gold");
        this.expBookIcon = MgImageLoader.load("/book");

        if (CampaignData.a >= 0) {
            CampaignData.j = CampaignData.f(CampaignData.a, CampaignData.e.e);
        }
        this.nextLevelExp = CampaignData.u(this.heroPreview.e + 1);
        this.currentLevelBaseExp = CampaignData.u(this.heroPreview.e);

        if (CampaignData.a < 0) {
            this.displayCollectedGold = CampaignData.i;
            this.displayWinBonusGold = questRewardMode ? 0 : CampaignData.computeWinBonusGold(CampaignData.e, CampaignData.f);
            this.displayQuestBonusGold = CampaignData.j(CampaignData.c);
            this.displayQuestBonusExp = CampaignData.i(CampaignData.c);
            this.displayWinBonusExp = questRewardMode ? 0 : CampaignData.a(CampaignData.e, CampaignData.f);
        }
        this.displayCollectedExp = CampaignData.j;

        this.remainingGoldToTally = this.displayCollectedGold;
        this.remainingWinBonusGoldToTally = this.displayWinBonusGold;
        this.remainingQuestGoldToTally = this.displayQuestBonusGold;
        this.remainingCollectedExpToTally = this.displayCollectedExp;
        this.remainingWinBonusExpToTally = this.displayWinBonusExp;
        this.remainingQuestExpToTally = this.displayQuestBonusExp;

        int w1 = DrawUtils.getStringWidth(DrawUtils.UI_FONT_BOLD, "Máu");
        int w2 = DrawUtils.getStringWidth(DrawUtils.UI_FONT_BOLD, "EXP");
        this.labelColW = (w1 > w2 ? w1 : w2) + 7;

        this.levelUpBurst = new ComboBurstEffect(MgImageLoader.load("/crystalviolet"));
        this.levelUpBurst.a_(false);
        this.initialDelayTicks = 50;
        this.levelUpSfx = new SfxPlayer("lvu");
        this.a(new SoftKeyBar());
    }

    public BattleResultDialog() {
        this(false);
    }

    public void c(SoftKeyButton centerButton) {
        this.pendingCenterButton = centerButton;
    }

    private void skipTallyAnimation() {
        if (this.initialDelayTicks > 0) {
            this.initialDelayTicks = 0;
            super.c(this.pendingCenterButton);
        }
        if (!this.questRewardMode) {
            this.currentGold += this.remainingGoldToTally + this.remainingWinBonusGoldToTally;
            this.remainingGoldToTally = 0;
            this.remainingWinBonusGoldToTally = 0;
            this.currentExp += this.remainingCollectedExpToTally + this.remainingWinBonusExpToTally;
            this.remainingCollectedExpToTally = 0;
            this.remainingWinBonusExpToTally = 0;
        } else {
            this.currentGold += this.remainingQuestGoldToTally;
            this.remainingQuestGoldToTally = 0;
            this.currentExp += this.remainingQuestExpToTally;
            this.remainingQuestExpToTally = 0;
        }
    }

    public void a(int keyCode) {
        this.skipTallyAnimation();
    }

    public void a(int pointerX, int pointerY) {
        this.skipTallyAnimation();
    }

    protected void e() {
        super.redrawParentScreen = true;
        if (this.initialDelayTicks >= 0) {
            --this.initialDelayTicks;
            if (this.initialDelayTicks == 0) {
                super.c(this.pendingCenterButton);
            }
            return;
        }

        if (!this.questRewardMode) {
            if (this.remainingGoldToTally > 0) {
                int step = Math.max(1, this.remainingGoldToTally / 10);
                this.remainingGoldToTally -= step;
                this.currentGold += step;
            }
            if (this.remainingWinBonusGoldToTally > 0) {
                int step = Math.max(1, this.remainingWinBonusGoldToTally / 10);
                this.remainingWinBonusGoldToTally -= step;
                this.currentGold += step;
            }
            if (this.remainingCollectedExpToTally > 0) {
                int step = Math.max(1, this.remainingCollectedExpToTally / 10);
                this.remainingCollectedExpToTally -= step;
                this.currentExp += step;
            }
            if (this.remainingWinBonusExpToTally > 0) {
                int step = Math.max(1, this.remainingWinBonusExpToTally / 10);
                this.remainingWinBonusExpToTally -= step;
                this.currentExp += step;
            }
        } else {
            if (this.remainingQuestExpToTally > 0) {
                int step = Math.max(1, this.remainingQuestExpToTally / 10);
                this.remainingQuestExpToTally -= step;
                this.currentExp += step;
            }
            if (this.remainingQuestGoldToTally > 0) {
                int step = Math.max(1, this.remainingQuestGoldToTally / 10);
                this.remainingQuestGoldToTally -= step;
                this.currentGold += step;
            }
        }

        while (this.heroPreview.e < HeroStats.MAX_LEVEL && CampaignData.c(this.heroPreview.e + 1, this.currentExp)) {
            int prevTier = this.heroPreview.getBreakthroughTier();
            this.heroPreview.a();
            int newTier = this.heroPreview.getBreakthroughTier();
            if (newTier > prevTier) {
                this.breakthroughReachedLevel = newTier * 10;
            }
            this.nextLevelExp = CampaignData.u(this.heroPreview.e + 1);
            this.currentLevelBaseExp = CampaignData.u(this.heroPreview.e);
            this.levelUpBurst.a(super.g + 160, super.a_ + 22, 1, "");
            if (!this.levelUpSfx.isPlaying()) {
                this.levelUpSfx.playAsync();
            }
        }
        this.levelUpBurst.b();
    }

    private void drawHeroHeader(Graphics g, int x, int y, int innerW) {
        int rightX = x + innerW;
        int boldH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD);

        // 1. Top Identity Row: 36x36 Portrait on Left + Name/Level & Attack/Gold beside it
        DrawUtils.drawAntiquePanel(
                g, x, y, 36, 36,
                0x3D291A, 0x1C110A, 0x100905,
                0xC29444, 0xF5D27E, 0xFFFFFF);
        AvatarRenderer.a(g, this.heroPreview.a, x + 3, y + 3, true);

        int infoX = x + 42;
        String lvText = "Cấp " + this.heroPreview.e;
        int lvW = DrawUtils.getStringWidth(DrawUtils.UI_FONT_BOLD, lvText);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, lvText,
                rightX, y + 2, Graphics.TOP | Graphics.RIGHT,
                0x8CF5E8, 0x120804);
        DrawUtils.drawFullNameInBox(
                g, AvatarRenderer.a(this.heroPreview.a),
                infoX, y + 2, (rightX - infoX) - lvW - 6, boldH,
                false, 0xFFE680, 0x120804, 0);

        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_PLAIN, "Công: " + this.heroPreview.d,
                infoX, y + 19, Graphics.TOP | Graphics.LEFT,
                0xFFF4DC, 0x120804);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, "Vàng: " + this.currentGold,
                rightX, y + 19, Graphics.TOP | Graphics.RIGHT,
                0xFFE680, 0x120804);

        // 2. Full-Width Labeled Proportional HP & EXP Gauges
        int barX = x + this.labelColW;
        int barW = rightX - barX;
        int labelOffY = (BAR_HEIGHT - boldH) >> 1;

        int hpY = y + 40;
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, "Máu",
                x, hpY + labelOffY, Graphics.TOP | Graphics.LEFT,
                0xFF9E9E, 0x120804);
        DrawUtils.drawRpgStatBar(
                g, barX, hpY, barW, BAR_HEIGHT,
                this.heroPreview.c, this.heroPreview.b,
                0xFF7D7D, 0xCC2424, 0x801111, 0xFFC4C4,
                4,
                this.heroPreview.c + " / " + this.heroPreview.b);

        int expY = hpY + BAR_HEIGHT + 4;
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, "EXP",
                x, expY + labelOffY, Graphics.TOP | Graphics.LEFT,
                0xFFE680, 0x120804);

        int expCur;
        int expMax;
        String expLabel;
        if (this.heroPreview.e >= HeroStats.MAX_LEVEL) {
            expCur = 1;
            expMax = 1;
            expLabel = "TỐI ĐA (100%)";
        } else {
            int span = this.nextLevelExp - this.currentLevelBaseExp;
            if (span < 1) {
                span = 1;
            }
            int prog = this.currentExp - this.currentLevelBaseExp;
            if (prog < 0) {
                prog = 0;
            } else if (prog > span) {
                prog = span;
            }
            int pct = prog * 100 / span;
            expCur = prog;
            expMax = span;
            expLabel = prog + " / " + span + " (" + pct + "%)";
        }
        DrawUtils.drawRpgStatBar(
                g, barX, expY, barW, BAR_HEIGHT,
                expCur, expMax,
                0xFFF08A, 0xDE9618, 0x8A5608, 0xFFFFCC,
                4,
                expLabel);

        this.levelUpBurst.a(g);
    }

    private void drawRewardsBreakdown(Graphics g, int x, int y, int innerW) {
        int rightX = x + innerW;
        g.setColor(0x7A592A);
        g.drawLine(x, y, rightX, y);
        y += 5;

        if (CampaignData.a < 0) {
            g.drawImage(this.goldIcon, x, y - 2, 0);
            DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_BOLD, "Vàng nhận được", x + 25, y + 2, Graphics.TOP | Graphics.LEFT, 0xFFE680, 0x120804);
            y += 20;
            if (this.questRewardMode) {
                DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_PLAIN, "- Thưởng nhiệm vụ", x + 6, y, Graphics.TOP | Graphics.LEFT, 0xFFF4DC, 0x120804);
                DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_BOLD, "+" + this.displayQuestBonusGold, rightX, y, Graphics.TOP | Graphics.RIGHT, 0x7CFF9E, 0x120804);
                y += 16;
            } else {
                DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_PLAIN, "- Thu thập bàn cờ", x + 6, y, Graphics.TOP | Graphics.LEFT, 0xFFF4DC, 0x120804);
                DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_BOLD, "+" + this.displayCollectedGold, rightX, y, Graphics.TOP | Graphics.RIGHT, 0x7CFF9E, 0x120804);
                y += 13;
                DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_PLAIN, "- Thưởng thắng trận", x + 6, y, Graphics.TOP | Graphics.LEFT, 0xFFF4DC, 0x120804);
                DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_BOLD, "+" + this.displayWinBonusGold, rightX, y, Graphics.TOP | Graphics.RIGHT, 0x7CFF9E, 0x120804);
                y += 16;
            }

            g.drawImage(this.expBookIcon, x, y - 2, 0);
            DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_BOLD, "Kinh nghiệm (EXP)", x + 25, y + 2, Graphics.TOP | Graphics.LEFT, 0x8CF5E8, 0x120804);
            y += 20;
            if (this.questRewardMode) {
                DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_PLAIN, "- Thưởng nhiệm vụ", x + 6, y, Graphics.TOP | Graphics.LEFT, 0xFFF4DC, 0x120804);
                DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_BOLD, "+" + this.displayQuestBonusExp, rightX, y, Graphics.TOP | Graphics.RIGHT, 0x7CFF9E, 0x120804);
            } else {
                DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_PLAIN, "- Thu thập bàn cờ", x + 6, y, Graphics.TOP | Graphics.LEFT, 0xFFF4DC, 0x120804);
                DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_BOLD, "+" + this.displayCollectedExp, rightX, y, Graphics.TOP | Graphics.RIGHT, 0x7CFF9E, 0x120804);
                y += 13;
                DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_PLAIN, "- Thưởng thắng trận", x + 6, y, Graphics.TOP | Graphics.LEFT, 0xFFF4DC, 0x120804);
                DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_BOLD, "+" + this.displayWinBonusExp, rightX, y, Graphics.TOP | Graphics.RIGHT, 0x7CFF9E, 0x120804);
            }
            if (this.earnedShards > 0) {
                y += 14;
                DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_BOLD, "- Mảnh Cường Hóa", x + 6, y, Graphics.TOP | Graphics.LEFT, 0x8CF5E8, 0x120804);
                DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_BOLD, "+" + this.earnedShards + " Mảnh", rightX, y, Graphics.TOP | Graphics.RIGHT, 0xFFE680, 0x120804);
            }
        } else {
            g.drawImage(this.expBookIcon, x, y - 2, 0);
            DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_BOLD, "Kinh nghiệm (EXP)", x + 25, y + 2, Graphics.TOP | Graphics.LEFT, 0x8CF5E8, 0x120804);
            y += 20;
            DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_PLAIN, "- Thưởng Đấu Trường", x + 6, y, Graphics.TOP | Graphics.LEFT, 0xFFF4DC, 0x120804);
            DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_BOLD, "+" + this.displayCollectedExp, rightX, y, Graphics.TOP | Graphics.RIGHT, 0x7CFF9E, 0x120804);
            if (this.earnedShards > 0) {
                y += 17;
                DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_BOLD, "- Mảnh Cường Hóa", x + 6, y, Graphics.TOP | Graphics.LEFT, 0x8CF5E8, 0x120804);
                DrawUtils.drawShadowText(g, DrawUtils.UI_FONT_BOLD, "+" + this.earnedShards + " Mảnh", rightX, y, Graphics.TOP | Graphics.RIGHT, 0xFFE680, 0x120804);
            }
        }
    }

    public void a(Graphics g) {
        DrawUtils.drawAntiquePanel(
                g, super.g, super.a_, super.b_, super.c_,
                0x362417, 0x22150D, 0x130B06,
                0xC29444, 0xF5D27E, 0xFFE680);
        int headerH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD) + 6;
        DrawUtils.drawAntiquePanel(
                g, super.g + 6, super.a_ + 6, super.b_ - 12, headerH,
                0x1A5E54, 0x114039, 0x0C2B26,
                0xB8893D, 0xF0C975, 0xFFFFFF);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD, this.questRewardMode ? "THƯỞNG NHIỆM VỤ" : "KẾT QUẢ TRẬN ĐẤU",
                GameConfig.screenWidth >> 1, super.a_ + 6 + ((headerH - DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD)) >> 1),
                Graphics.TOP | Graphics.HCENTER,
                0xFFF4D0, 0x120804);

        int contentX = super.g + 10;
        int innerW = super.b_ - 20;
        int heroHeaderY = super.a_ + headerH + 10;
        this.drawHeroHeader(g, contentX, heroHeaderY, innerW);
        this.drawRewardsBreakdown(g, contentX, heroHeaderY + 79, innerW);

        if (this.breakthroughReachedLevel > 0) {
            int bannerH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_PLAIN) + 6;
            int bannerY = super.a_ + super.c_ - bannerH - 6;
            int bannerX = super.g + 7;
            int bannerW = super.b_ - 14;
            DrawUtils.drawAntiquePanel(
                    g, bannerX, bannerY, bannerW, bannerH,
                    0x5C3A12, 0x3D240A, 0x1E1004,
                    0xFFD700, 0xFFF2A6, 0xFFFFFF);
            String btText = "ĐỘT PHÁ C." + this.breakthroughReachedLevel + ": "
                    + HeroStats.getBreakthroughStepDesc(this.heroPreview.a);
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_PLAIN, btText,
                    bannerX + (bannerW >> 1), bannerY + ((bannerH - DrawUtils.getFontHeight(DrawUtils.UI_FONT_PLAIN)) >> 1),
                    Graphics.TOP | Graphics.HCENTER,
                    0xFFF5B8, 0x1A0B03);
        }
    }
}
