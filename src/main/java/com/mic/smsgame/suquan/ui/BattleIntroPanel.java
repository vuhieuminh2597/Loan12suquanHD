package com.mic.smsgame.suquan.ui;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdUiRenderer;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.engine.sprite.AnimatedSprite;
import com.mic.smsgame.suquan.data.CampaignData;
import com.mic.smsgame.suquan.gfx.AvatarRenderer;
import com.mic.smsgame.suquan.model.BattleFighter;
import com.mic.smsgame.suquan.model.HeroStats;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Pre-battle antique VS comparison panel displaying both combatants' portraits, full names,
 * first-turn priority badge, HP/ATK/Level stats, multi-line wrapped passives/abilities & tactics,
 * and crossed swords banner with zero horizontal marquee waiting.
 */
public class BattleIntroPanel {
    private final HeroStats topHero;
    private final HeroStats bottomHero;
    private final boolean topIsPlayer;
    private final String topName;
    private final String bottomName;
    private final String[] topPassiveLines;
    private final String[] bottomPassiveLines;
    private final String[] topTacticLines;
    private final String[] bottomTacticLines;
    private final Image swordsBannerImage;
    private final AnimatedSprite crystalSprite;
    private final int cardW;
    private final int topCardH;
    private final int bottomCardH;
    private final int cardX;
    private final int topCardY;
    private final int swordsY;
    private final int bottomCardY;
    private final int centerX;
    private int animTick;

    public BattleIntroPanel(HeroStats playerHero, HeroStats enemyHero) {
        if (CampaignData.h) {
            this.topHero = playerHero;
            this.bottomHero = enemyHero;
            this.topIsPlayer = true;
        } else {
            this.topHero = enemyHero;
            this.bottomHero = playerHero;
            this.topIsPlayer = false;
        }

        BattleFighter topFighter = new BattleFighter(this.topHero);
        BattleFighter bottomFighter = new BattleFighter(this.bottomHero);
        boolean isBossMatch = topFighter.isBossFighter() || bottomFighter.isBossFighter();

        this.topName = AvatarRenderer.a(this.topHero.a);
        this.bottomName = AvatarRenderer.a(this.bottomHero.a);

        this.cardW = 228;
        this.centerX = GameConfig.screenWidth >> 1;
        this.cardX = (GameConfig.screenWidth - this.cardW) >> 1;

        Font plainFont = DrawUtils.UI_FONT_PLAIN;
        int innerTextW = this.cardW - 14;
        String topAbilityPrefix = (!this.topIsPlayer && CampaignData.isBossTemplate(this.topHero.a))
                ? "Khả năng: "
                : "Nội tại: ";
        String bottomAbilityPrefix = (this.topIsPlayer && CampaignData.isBossTemplate(this.bottomHero.a))
                ? "Khả năng: "
                : "Nội tại: ";

        this.topPassiveLines = DrawUtils.wrapText(
                plainFont, topAbilityPrefix + topFighter.getPassiveShortDesc(), innerTextW);
        this.bottomPassiveLines = DrawUtils.wrapText(
                plainFont, bottomAbilityPrefix + bottomFighter.getPassiveShortDesc(), innerTextW);
        this.topTacticLines = DrawUtils.wrapText(
                plainFont, buildTacticLine(topFighter, this.topIsPlayer, isBossMatch), innerTextW);
        this.bottomTacticLines = DrawUtils.wrapText(
                plainFont, buildTacticLine(bottomFighter, !this.topIsPlayer, isBossMatch), innerTextW);

        this.swordsBannerImage = MgImageLoader.load("/m/sw");

        this.topCardH = computeCardHeight(plainFont, this.topPassiveLines, this.topTacticLines);
        this.bottomCardH = computeCardHeight(plainFont, this.bottomPassiveLines, this.bottomTacticLines);

        int swordsH = this.swordsBannerImage.getHeight();
        int totalHeight = this.topCardH + this.bottomCardH + swordsH + 6;
        int startY = (GameConfig.screenHeight - 23 - totalHeight) >> 1;
        if (startY < 4) {
            startY = 4;
        }
        this.topCardY = startY;
        this.swordsY = this.topCardY + this.topCardH + 3;
        this.bottomCardY = this.swordsY + swordsH + 3;

        this.crystalSprite = new AnimatedSprite(MgImageLoader.load("/crystalblue"), 3);
        this.crystalSprite.a(new byte[][]{{0, 1, 2, 1}});
        this.crystalSprite.c(4);
        this.crystalSprite.b(true);
        this.crystalSprite.d(
                this.centerX - (this.crystalSprite.k() >> 1),
                this.swordsY + ((swordsH - this.crystalSprite.l()) >> 1));
    }

    private static int computeCardHeight(Font plainFont, String[] passiveLines, String[] tacticLines) {
        int pasH = DrawUtils.getWrappedHeight(plainFont, passiveLines);
        int tacH = DrawUtils.getWrappedHeight(plainFont, tacticLines);
        int h = 47 + pasH + 3 + tacH + 6;
        return h > 82 ? h : 82;
    }

    private static String buildTacticLine(BattleFighter fighter, boolean isPlayer, boolean isBossMatch) {
        if (fighter.isBossFighter()) {
            return "Phương án: " + fighter.getBossCounterStrategy();
        }
        if (isPlayer && isBossMatch) {
            return "Màn Boss: Phá Ấn Boss trong 3 lượt để cướp +6 Nộ, +5% Máu & Khắc chế";
        }
        if (isPlayer && fighter.getLevel() >= 10 && fighter.b() >= 0 && fighter.b() <= 5) {
            return "Đột phá hệ: Đạt mốc Cấp " + ((fighter.getLevel() / 10) * 10) + " (+Chỉ số tự động)";
        }
        return "Bàn thường: Ghép 4/5 hoặc chữ L/T nhận thêm lượt & rèn Kiếm Lửa";
    }

    public void a() {
        ++this.animTick;
        this.crystalSprite.e();
    }

    private void drawCombatantCard(
            Graphics g, HeroStats hero, String name,
            String[] passiveLines, String[] tacticLines,
            boolean isPlayer, boolean goesFirst, int x, int y, int cardH) {
        Font boldFont = DrawUtils.UI_FONT_BOLD;
        Font plainFont = DrawUtils.UI_FONT_PLAIN;
        int boldH = DrawUtils.getFontHeight(boldFont);
        int plainH = DrawUtils.getFontHeight(plainFont);

        // Outer antique plaque with player jade or enemy crimson accent
        DrawUtils.drawAntiquePanel(
                g, x, y, this.cardW, cardH,
                isPlayer ? 0x1D4A42 : 0x521C29,
                0x26170E, 0x150C07,
                isPlayer ? 0x33D6C4 : 0xE0467C,
                isPlayer ? 0x8CF5E8 : 0xFF96BC,
                0xFFE680);

        // Portrait frame on left
        int portX = x + 7;
        int portY = y + 6;
        DrawUtils.drawAntiquePanel(
                g, portX, portY, 36, 36,
                0x3D291A, 0x1C110A, 0x100905,
                0xC29444, 0xF5D27E, 0xFFFFFF);
        AvatarRenderer.a(g, hero.a, portX + 3, portY + 3, isPlayer);

        int textX = portX + 41;
        int rightEdge = x + this.cardW - 7;

        // Row 1: Full Name + [ĐI TRƯỚC] or [ĐI SAU] badge
        String turnBadge = goesFirst ? "ĐI TRƯỚC" : "ĐI SAU";
        int badgeW = DrawUtils.getStringWidth(plainFont, turnBadge) + 8;
        int badgeH = plainH + 2;
        int badgeX = rightEdge - badgeW;
        int badgeY = y + 6;
        DrawUtils.drawAntiquePanel(
                g, badgeX, badgeY, badgeW, badgeH,
                goesFirst ? 0x8A5D12 : 0x3B281B,
                goesFirst ? 0x5E3D0A : 0x24170E,
                0x140C07,
                goesFirst ? 0xFFD700 : 0x8F6932,
                goesFirst ? 0xFFF2A6 : 0xCCA052,
                0xFFFFFF);
        DrawUtils.drawShadowText(
                g, plainFont, turnBadge,
                badgeX + (badgeW >> 1), badgeY + 1,
                Graphics.TOP | Graphics.HCENTER,
                goesFirst ? 0xFFF5B8 : 0xD4C0A6, 0x120804);

        int nameBoxW = badgeX - textX - 4;
        DrawUtils.drawFullNameInBox(
                g, name,
                textX, y + 6, nameBoxW, boldH,
                false, 0xFFE680, 0x120804, this.animTick);

        // Row 2: Level, HP, Attack Power (static)
        int row2Y = y + 6 + boldH + 3;
        String statsLine = "Cấp " + hero.e + "  |  Máu " + hero.c + "  |  Công " + hero.d;
        DrawUtils.drawShadowText(
                g, plainFont, statsLine,
                textX, row2Y,
                Graphics.TOP | Graphics.LEFT,
                0xFFF4DC, 0x120804);

        // Divider + Multi-line wrapped Khả năng/Nội tại + Phương án/Màn Boss
        int divY = y + 44;
        g.setColor(0x7A592A);
        g.drawLine(x + 7, divY, rightEdge, divY);

        int curY = divY + 3;
        int innerW = this.cardW - 14;
        int pasH = DrawUtils.getWrappedHeight(plainFont, passiveLines);
        DrawUtils.drawWrappedShadowText(
                g, plainFont, passiveLines,
                x + 7, curY, innerW, pasH + 2,
                0x99F2FF, 0x120804);
        curY += pasH + 3;

        int tacH = DrawUtils.getWrappedHeight(plainFont, tacticLines);
        DrawUtils.drawWrappedShadowText(
                g, plainFont, tacticLines,
                x + 7, curY, innerW, tacH + 2,
                isPlayer ? 0x7CFF9E : 0xFFD980, 0x120804);
    }

    public void a(Graphics g) {
        this.drawCombatantCard(
                g, this.topHero, this.topName, this.topPassiveLines, this.topTacticLines,
                this.topIsPlayer, true, this.cardX, this.topCardY, this.topCardH);

        if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
            HdUiRenderer.drawHdVsMedallion(
                    g.getHdCanvas(), this.centerX, this.swordsY,
                    this.swordsBannerImage.getHeight(), this.animTick);
        } else {
            g.drawImage(this.swordsBannerImage, this.centerX, this.swordsY, Graphics.TOP | Graphics.HCENTER);
            this.crystalSprite.a(g);
        }

        this.drawCombatantCard(
                g, this.bottomHero, this.bottomName, this.bottomPassiveLines, this.bottomTacticLines,
                !this.topIsPlayer, false, this.cardX, this.bottomCardY, this.bottomCardH);
    }
}

