package com.mic.smsgame.suquan.ui;

import com.loan12suquan.hd.HdInputState;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.engine.storage.EngineStorage;
import com.mg.engine.ui.BaseDialog;
import com.mg.engine.ui.SoftKeyBar;
import com.mic.smsgame.suquan.data.CampaignData;
import com.mic.smsgame.suquan.gfx.AvatarRenderer;
import com.mic.smsgame.suquan.model.BattleFighter;
import com.mic.smsgame.suquan.model.HeroStats;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Two-tab World-Map Hero Profile & Quest Journal dialog ("T.Tin"):
 * - Tab 0 ("1. TƯỚNG & TÚI"): Portrait, stats, HP/EXP% bars, passive, breakthrough, relics, and 5-icon visual bag.
 * - Tab 1 ("2. NHIỆM VỤ"): Multi-line wrapped Main Quest & Side Quest briefings (zero marquee waiting).
 */
public class HeroInfoDialog extends BaseDialog {
    private static final int[] BAG_ITEM_IDS = new int[]{0, 1, 3, 4, 5};

    private final HeroStats heroStats;
    private final String heroName;
    private final String passiveDesc;
    private final int currentExp;
    private final int currentGold;
    private final int nextLevelExp;
    private final int currentLevelExp;
    private final int[] itemCounts;
    private final Image itemIconsSheet;
    private final String relicSummaryLine;
    private final String breakthroughLine;
    private final String[] wrappedPassive;
    private final String[] wrappedBreakthrough;
    private final String[] wrappedRelics;
    private final String locationLabel;
    private final String[] wrappedMainQuest;
    private final int mainQuestColor;
    private final String[] wrappedSideQuest;
    private final int sideQuestColor;
    private int activeTab = 0;
    private int animTick;

    private final int tabBarH;
    private final int identityH;
    private final int barH;
    private final int labelColW;

    public HeroInfoDialog(HeroStats heroStats, int currentExp, int currentGold) {
        this(heroStats, currentExp, currentGold, null, null, 0xFFE680, null, 0x99F2FF);
    }

    public HeroInfoDialog(
            HeroStats heroStats, int currentExp, int currentGold,
            String locationLabel, String mainQuestLine, int mainQuestColor,
            String sideQuestLine, int sideQuestColor) {
        super(1);
        this.heroStats = heroStats;
        this.heroName = AvatarRenderer.a(heroStats.a);
        this.passiveDesc = new BattleFighter(heroStats).getPassiveShortDesc();
        this.currentExp = currentExp;
        this.currentGold = currentGold;
        this.nextLevelExp = CampaignData.u(heroStats.e + 1);
        this.currentLevelExp = CampaignData.u(heroStats.e);
        this.locationLabel = locationLabel;
        this.mainQuestColor = mainQuestColor;
        this.sideQuestColor = sideQuestColor;

        int[] inv = EngineStorage.v();
        this.itemCounts = new int[5];
        for (int i = 0; i < BAG_ITEM_IDS.length; ++i) {
            int slotId = BAG_ITEM_IDS[i];
            this.itemCounts[i] = (inv != null && slotId < inv.length) ? inv[slotId] : 0;
        }
        this.itemIconsSheet = MgImageLoader.load("/itemshops");
        this.relicSummaryLine = buildRelicSummary();
        this.breakthroughLine = heroStats != null ? heroStats.getBreakthroughSummary() : null;

        Font boldFont = DrawUtils.UI_FONT_BOLD;
        Font plainFont = DrawUtils.UI_FONT_PLAIN;
        int boldH = DrawUtils.getFontHeight(boldFont);
        int plainH = DrawUtils.getFontHeight(plainFont);

        this.tabBarH = boldH + 7;
        int twoLineTextH = boldH + plainH + 6;
        this.identityH = twoLineTextH > 36 ? twoLineTextH : 36;
        this.barH = (plainH + 4) > 15 ? (plainH + 4) : 15;

        int w1 = DrawUtils.getStringWidth(boldFont, "Máu:");
        int w2 = DrawUtils.getStringWidth(boldFont, "EXP:");
        this.labelColW = (w1 > w2 ? w1 : w2) + 8;

        this.b_ = 228;
        int tab0WrapW = this.b_ - 20;
        this.wrappedPassive = DrawUtils.wrapText(plainFont, "Nội tại: " + this.passiveDesc, tab0WrapW);
        this.wrappedBreakthrough = this.breakthroughLine != null
                ? DrawUtils.wrapText(plainFont, this.breakthroughLine, tab0WrapW)
                : null;
        this.wrappedRelics = DrawUtils.wrapText(plainFont, this.relicSummaryLine, tab0WrapW);

        int wrapW = this.b_ - 28;
        this.wrappedMainQuest = DrawUtils.wrapText(
                plainFont,
                mainQuestLine != null ? mainQuestLine : "Chưa có nhiệm vụ mới.",
                wrapW);
        this.wrappedSideQuest = DrawUtils.wrapText(
                plainFont,
                sideQuestLine != null ? sideQuestLine : "Chưa mở thêm sự kiện phụ.",
                wrapW);

        int tab0MinH = 10 + this.tabBarH
                + this.identityH + 7
                + (2 * this.barH + 13)
                + DrawUtils.getWrappedHeight(plainFont, this.wrappedPassive) + 2
                + (this.wrappedBreakthrough != null ? (DrawUtils.getWrappedHeight(plainFont, this.wrappedBreakthrough) + 2) : 0)
                + DrawUtils.getWrappedHeight(plainFont, this.wrappedRelics) + 6
                + (boldH + 3) + 34 + 6;

        int mainQuestTextH = DrawUtils.getWrappedHeight(plainFont, this.wrappedMainQuest);
        if (mainQuestTextH < plainH * 3) {
            mainQuestTextH = plainH * 3;
        }
        int sideQuestTextH = DrawUtils.getWrappedHeight(plainFont, this.wrappedSideQuest);
        if (sideQuestTextH < plainH * 2) {
            sideQuestTextH = plainH * 2;
        }
        int tab1MinH = 10 + this.tabBarH
                + (boldH + 4)
                + (boldH + mainQuestTextH + 10) + 5
                + (boldH + sideQuestTextH + 10) + 6
                + plainH + 5;

        int targetH = 258;
        if (tab0MinH > targetH) {
            targetH = tab0MinH;
        }
        if (tab1MinH > targetH) {
            targetH = tab1MinH;
        }
        int maxH = GameConfig.screenHeight - 27;
        this.c_ = targetH > maxH ? maxH : targetH;
        this.g = (GameConfig.screenWidth - this.b_) >> 1;
        this.a_ = ((GameConfig.screenHeight - 23) - this.c_) >> 1;
        if (this.a_ < 2) {
            this.a_ = 2;
        }

        this.a(new SoftKeyBar());
        super.redrawParentScreen = true;
    }

    private static String buildRelicSummary() {
        int count = EngineStorage.countUnlockedRelics();
        int shards = EngineStorage.getEnhancementShards();
        String prefix = "Báu vật (" + count + "/4 | " + shards + "M): ";
        if (count == 0) {
            return prefix + (EngineStorage.canCraftOrUpgradeAnyRelic() ? "Đủ ghép mới!" : "Chưa mở");
        }
        String buffs = "";
        int atkPct = EngineStorage.getRelicAtkPercent();
        int hpPct = EngineStorage.getRelicHpPercent();
        int manaBonus = EngineStorage.getRelicStartManaBonus();
        int foodBonus = EngineStorage.getRelicFoodBonus();
        if (atkPct > 0) {
            buffs += "+" + atkPct + "%C";
        }
        if (hpPct > 0) {
            buffs += (buffs.length() > 0 ? ", " : "") + "+" + hpPct + "%M";
        }
        if (manaBonus > 0) {
            buffs += (buffs.length() > 0 ? ", " : "") + "+" + manaBonus + " Nộ";
        }
        if (foodBonus > 0) {
            buffs += (buffs.length() > 0 ? ", " : "") + "+" + foodBonus + "L";
        }
        if (EngineStorage.canCraftOrUpgradeAnyRelic()) {
            buffs += " [Nâng!]";
        }
        return prefix + buffs;
    }

    protected void e() {
        ++this.animTick;
        int wheel = HdInputState.consumeWheelDelta();
        if (wheel != 0) {
            this.activeTab = 1 - this.activeTab;
        }
    }

    public void a(int keyCode) {
        if (keyCode == 14 || keyCode == 15) {
            this.activeTab = 1 - this.activeTab;
        }
    }

    public void a(int pointerX, int pointerY) {
        // Tap outside dialog frame to dismiss cleanly
        if (pointerY < GameConfig.screenHeight - 24
                && (pointerX < this.g || pointerX > this.g + this.b_
                || pointerY < this.a_ || pointerY > this.a_ + this.c_)) {
            GUIManager.getInstance().a(true);
            return;
        }
        // Tap top tabs to switch between Tab 0 and Tab 1
        int tabTop = this.a_ + 5;
        if (pointerY >= tabTop && pointerY <= tabTop + this.tabBarH + 4) {
            int midX = this.g + (this.b_ >> 1);
            this.activeTab = (pointerX < midX) ? 0 : 1;
        }
    }

    public void a(Graphics g) {
        int x = this.g;
        int y = this.a_;
        int w = this.b_;
        int h = this.c_;
        Font boldFont = DrawUtils.UI_FONT_BOLD;
        Font plainFont = DrawUtils.UI_FONT_PLAIN;
        int boldH = DrawUtils.getFontHeight(boldFont);
        int plainH = DrawUtils.getFontHeight(plainFont);

        // Outer antique lacquer-and-jade frame
        DrawUtils.drawAntiquePanel(
                g, x, y, w, h,
                0x362417, 0x22150D, 0x130B06,
                0x33D6C4, 0x8CF5E8, 0xFFE680);

        // 2-Tab Header Bar
        int tabY = y + 5;
        int halfTabW = (w - 14) >> 1;
        int tab0X = x + 6;
        int tab1X = tab0X + halfTabW + 2;

        boolean h0 = HdInputState.isHovered(tab0X, tabY, halfTabW, this.tabBarH);
        boolean h1 = HdInputState.isHovered(tab1X, tabY, halfTabW, this.tabBarH);

        DrawUtils.drawAntiquePanel(
                g, tab0X, tabY, halfTabW, this.tabBarH,
                this.activeTab == 0 ? 0x1A5E54 : (h0 ? 0x3B2618 : 0x2B1B10),
                this.activeTab == 0 ? 0x114039 : (h0 ? 0x26180F : 0x1A1009),
                0x0E0804,
                this.activeTab == 0 ? 0xFFD700 : (h0 ? 0xCCA052 : 0x7A592A),
                this.activeTab == 0 ? 0xFFF2A6 : 0xA88044,
                0xFFFFFF);
        DrawUtils.drawShadowText(
                g, boldFont, "1. TƯỚNG & TÚI",
                tab0X + (halfTabW >> 1), tabY + ((this.tabBarH - boldH) >> 1),
                Graphics.TOP | Graphics.HCENTER,
                this.activeTab == 0 ? 0xFFF5B8 : (h0 ? 0xFFE680 : 0xB8A48C), 0x120804);

        DrawUtils.drawAntiquePanel(
                g, tab1X, tabY, halfTabW, this.tabBarH,
                this.activeTab == 1 ? 0x1A5E54 : (h1 ? 0x3B2618 : 0x2B1B10),
                this.activeTab == 1 ? 0x114039 : (h1 ? 0x26180F : 0x1A1009),
                0x0E0804,
                this.activeTab == 1 ? 0xFFD700 : (h1 ? 0xCCA052 : 0x7A592A),
                this.activeTab == 1 ? 0xFFF2A6 : 0xA88044,
                0xFFFFFF);
        DrawUtils.drawShadowText(
                g, boldFont, "2. NHIỆM VỤ",
                tab1X + (halfTabW >> 1), tabY + ((this.tabBarH - boldH) >> 1),
                Graphics.TOP | Graphics.HCENTER,
                this.activeTab == 1 ? 0xFFF5B8 : (h1 ? 0xFFE680 : 0xB8A48C), 0x120804);

        int contentY = tabY + this.tabBarH + 5;
        if (this.activeTab == 0) {
            this.drawHeroAndBagTab(g, x, contentY, w, h, boldFont, plainFont, boldH, plainH);
        } else {
            this.drawQuestJournalTab(g, x, contentY, w, h, boldFont, plainFont, boldH, plainH);
        }
    }

    private void drawHeroAndBagTab(
            Graphics g, int x, int curY, int w, int h,
            Font boldFont, Font plainFont, int boldH, int plainH) {
        int rightEdge = x + w - 10;
        int labelX = x + 10;

        // Identity Row: Portrait + Name + Level + Attack & Gold
        int portX = x + 10;
        int portY = curY + ((this.identityH - 36) >> 1);
        DrawUtils.drawAntiquePanel(
                g, portX, portY, 36, 36,
                0x3D291A, 0x1C110A, 0x100905,
                0xC29444, 0xF5D27E, 0xFFFFFF);
        AvatarRenderer.a(g, this.heroStats.a, portX + 3, portY + 3, true);

        int textX = portX + 42;
        String lvText = "Cấp " + this.heroStats.e;
        int lvW = DrawUtils.getStringWidth(boldFont, lvText);
        DrawUtils.drawShadowText(
                g, boldFont, lvText,
                rightEdge, curY, Graphics.TOP | Graphics.RIGHT,
                0x8CF5E8, 0x120804);
        DrawUtils.drawFullNameInBox(
                g, this.heroName,
                textX, curY, (rightEdge - textX) - lvW - 6, boldH,
                false, 0xFFE680, 0x120804, this.animTick);

        String subLine = "Công: " + this.heroStats.d + "  |  Vàng: " + this.currentGold;
        DrawUtils.drawShadowText(
                g, plainFont, subLine,
                textX, curY + boldH + 3,
                Graphics.TOP | Graphics.LEFT,
                0xFFF4DC, 0x120804);
        curY += this.identityH + 3;

        g.setColor(0x7A592A);
        g.drawLine(labelX, curY, rightEdge, curY);
        curY += 4;

        // Stat Bars: True-to-scale RPG HP & within-level EXP Gauges
        int barX = labelX + this.labelColW;
        int barW = rightEdge - barX;
        int labelOffsetY = (this.barH - boldH) >> 1;

        DrawUtils.drawShadowText(g, boldFont, "Máu:", labelX, curY + labelOffsetY, Graphics.TOP | Graphics.LEFT, 0xFF9E9E, 0x120804);
        DrawUtils.drawRpgStatBar(
                g, barX, curY, barW, this.barH,
                this.heroStats.c, this.heroStats.b,
                0xFF7D7D, 0xCC2424, 0x801111, 0xFFC4C4,
                4,
                this.heroStats.c + " / " + this.heroStats.b);
        curY += this.barH + 4;

        DrawUtils.drawShadowText(g, boldFont, "EXP:", labelX, curY + labelOffsetY, Graphics.TOP | Graphics.LEFT, 0xFFE680, 0x120804);
        int expCur;
        int expMax;
        String expLabel;
        if (this.heroStats.e >= HeroStats.MAX_LEVEL) {
            expCur = 1;
            expMax = 1;
            expLabel = "TỐI ĐA (100%)";
        } else {
            int expSpan = this.nextLevelExp - this.currentLevelExp;
            if (expSpan < 1) expSpan = 1;
            int expProg = this.currentExp - this.currentLevelExp;
            if (expProg < 0) expProg = 0;
            else if (expProg > expSpan) expProg = expSpan;
            int expPct = expProg * 100 / expSpan;
            expCur = expProg;
            expMax = expSpan;
            expLabel = expProg + " / " + expSpan + " (" + expPct + "%)";
        }
        DrawUtils.drawRpgStatBar(
                g, barX, curY, barW, this.barH,
                expCur, expMax,
                0xFFF08A, 0xDE9618, 0x8A5608, 0xFFFFCC,
                4,
                expLabel);
        curY += this.barH + 4;

        g.setColor(0x7A592A);
        g.drawLine(labelX, curY, rightEdge, curY);
        curY += 3;

        // Multi-Line Wrapped Passive, Breakthrough & Sacred Relics (zero marquee wait!)
        int infoW = rightEdge - labelX;
        int pasH = DrawUtils.getWrappedHeight(plainFont, this.wrappedPassive);
        DrawUtils.drawWrappedShadowText(
                g, plainFont, this.wrappedPassive,
                labelX, curY, infoW, pasH + 2,
                0x8CF5E8, 0x120804);
        curY += pasH + 2;

        if (this.wrappedBreakthrough != null && this.wrappedBreakthrough.length > 0) {
            int brkH = DrawUtils.getWrappedHeight(plainFont, this.wrappedBreakthrough);
            DrawUtils.drawWrappedShadowText(
                    g, plainFont, this.wrappedBreakthrough,
                    labelX, curY, infoW, brkH + 2,
                    0x7CFF9E, 0x120804);
            curY += brkH + 2;
        }

        int relH = DrawUtils.getWrappedHeight(plainFont, this.wrappedRelics);
        DrawUtils.drawWrappedShadowText(
                g, plainFont, this.wrappedRelics,
                labelX, curY, infoW, relH + 2,
                0xFFE680, 0x120804);
        curY += relH + 3;

        g.setColor(0x7A592A);
        g.drawLine(labelX, curY, rightEdge, curY);
        curY += 3;

        // Visual 5-Slot Battle Support Item Strip
        DrawUtils.drawShadowText(
                g, boldFont, "Hành trang chiến đấu:",
                labelX, curY, Graphics.TOP | Graphics.LEFT,
                0xFFE680, 0x120804);
        DrawUtils.drawShadowText(
                g, plainFont, "[</> Nhiệm vụ]",
                rightEdge, curY, Graphics.TOP | Graphics.RIGHT,
                0x8CF5E8, 0x120804);
        curY += boldH + 3;

        int slotW = 37;
        int slotH = 34;
        int gap = 5;
        int rowX = x + ((w - (5 * slotW + 4 * gap)) >> 1);
        for (int i = 0; i < 5; ++i) {
            int sx = rowX + i * (slotW + gap);
            int cnt = this.itemCounts[i];
            DrawUtils.drawAntiquePanel(
                    g, sx, curY, slotW, slotH,
                    cnt > 0 ? 0x2E1F14 : 0x1E140D,
                    cnt > 0 ? 0x1D130C : 0x130C08,
                    0x0C0704,
                    cnt > 0 ? 0xB8893D : 0x5C4326,
                    0xF0C975, 0xFFE680);
            if (this.itemIconsSheet != null) {
                DrawUtils.drawRegion(
                        g, this.itemIconsSheet, BAG_ITEM_IDS[i] * 28, 0, 28, 28,
                        sx + 4, curY + 3, 0);
            }
            String cntStr = "x" + cnt;
            DrawUtils.drawShadowText(
                    g, boldFont, cntStr,
                    sx + slotW - 2, curY + slotH - boldH - 1,
                    Graphics.TOP | Graphics.RIGHT,
                    cnt > 0 ? 0x7CFF9E : 0x9E8E7A, 0x0A0503);
        }
    }

    private void drawQuestJournalTab(
            Graphics g, int x, int curY, int w, int h,
            Font boldFont, Font plainFont, int boldH, int plainH) {
        int boxX = x + 8;
        int boxW = w - 16;

        String locStr = "Vị trí hiện tại: " + (this.locationLabel != null ? this.locationLabel : "Hoa Lư");
        DrawUtils.drawShadowText(
                g, boldFont, locStr,
                x + (w >> 1), curY, Graphics.TOP | Graphics.HCENTER,
                0xFFF5B8, 0x120804);
        curY += boldH + 4;

        // Main Quest Card (dynamically sized multi-line wrapped, zero marquee wait!)
        int mainTextH = DrawUtils.getWrappedHeight(plainFont, this.wrappedMainQuest);
        if (mainTextH < plainH * 3) {
            mainTextH = plainH * 3;
        }
        int mainCardH = boldH + mainTextH + 10;
        int maxMainH = (this.a_ + h - 22 - curY) - (boldH + plainH * 2 + 15);
        if (mainCardH > maxMainH && maxMainH > boldH + plainH * 2 + 10) {
            mainCardH = maxMainH;
        }
        DrawUtils.drawAntiquePanel(
                g, boxX, curY, boxW, mainCardH,
                0x291B11, 0x1A100A, 0x0E0804,
                0xC89B46, 0xF5D27E, 0xFFE680);
        DrawUtils.drawShadowText(
                g, boldFont, "NHIỆM VỤ CHÍNH:",
                boxX + 6, curY + 4, Graphics.TOP | Graphics.LEFT,
                0xFFE680, 0x100603);
        DrawUtils.drawWrappedShadowText(
                g, plainFont, this.wrappedMainQuest,
                boxX + 6, curY + boldH + 6, boxW - 12, mainCardH - boldH - 8,
                this.mainQuestColor, 0x100603);
        curY += mainCardH + 5;

        // Side Quest Card (multi-line wrapped)
        int sideCardH = (this.a_ + h - 21) - curY;
        if (sideCardH < boldH + plainH * 2 + 10) {
            sideCardH = boldH + plainH * 2 + 10;
        }
        DrawUtils.drawAntiquePanel(
                g, boxX, curY, boxW, sideCardH,
                0x192E2B, 0x101F1D, 0x0A1412,
                0x33D6C4, 0x8CF5E8, 0xFFFFFF);
        DrawUtils.drawShadowText(
                g, boldFont, "SỰ KIỆN / NHIỆM VỤ PHỤ:",
                boxX + 6, curY + 4, Graphics.TOP | Graphics.LEFT,
                0x8CF5E8, 0x0A1412);
        DrawUtils.drawWrappedShadowText(
                g, plainFont, this.wrappedSideQuest,
                boxX + 6, curY + boldH + 6, boxW - 12, sideCardH - boldH - 8,
                this.sideQuestColor, 0x0A1412);

        DrawUtils.drawShadowText(
                g, plainFont, "Chạm tab trên để xem Tướng  |  Chạm ngoài để đóng",
                x + (w >> 1), this.a_ + h - plainH - 5,
                Graphics.TOP | Graphics.HCENTER,
                0xD8C8B0, 0x120804);
    }
}
