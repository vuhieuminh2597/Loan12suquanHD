package com.mic.smsgame.suquan.ui;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdInputState;
import com.loan12suquan.hd.HdUiRenderer;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.Rect;
import com.mg.engine.ui.BaseDialog;
import com.mg.engine.ui.SoftKeyBar;
import com.mic.smsgame.suquan.data.CampaignData;
import com.mic.smsgame.suquan.data.GameStrings;
import com.mic.smsgame.suquan.model.BattleFighter;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * In-battle modal dialog displaying complete combatant stats (avatar, full name, level,
 * HP/Barrier, Food, Mana tiers, Attack, multi-line wrapped active Buffs/Debuffs, Passives,
 * Boss counter-strategy, and Skills) with instant 1-tap / Left-Right switching between
 * Player ("1. CHỦ TƯỚNG") and Enemy ("2. ĐỐI THỦ") and zero marquee waiting.
 */
public class FighterStatsDialog extends BaseDialog {
    private final BattleFighter[] fighters;
    private final Image[] portraits;
    private final boolean canSwitchSides;
    private final Rect savedClip = new Rect();
    private int activeIndex;
    private int animTick;

    private final int titleH;
    private final int identityH;
    private final int barH;
    private final int barGap;
    private final int skillRowH;
    private final int closeBtnH;
    private final int labelColW;

    private String cachedBuffRaw;
    private boolean hasActiveStatusEffects;
    private String[] wrappedEffects;
    private String[] wrappedPassive;
    private String[] wrappedTactic;
    private boolean tacticIsBoss;
    private int infoScrollY;
    private int infoMaxScroll;
    private int infoViewportTopOffset;
    private int infoViewportH;

    public FighterStatsDialog(BattleFighter fighter, Image portrait, boolean isPlayer) {
        this(
                isPlayer ? fighter : null,
                isPlayer ? portrait : null,
                isPlayer ? null : fighter,
                isPlayer ? null : portrait,
                isPlayer ? 0 : 1);
    }

    public FighterStatsDialog(
            BattleFighter playerFighter,
            Image playerPortrait,
            BattleFighter enemyFighter,
            Image enemyPortrait,
            int initialIndex) {
        super(1);
        this.fighters = new BattleFighter[]{playerFighter, enemyFighter};
        this.portraits = new Image[]{playerPortrait, enemyPortrait};
        this.canSwitchSides = (playerFighter != null && enemyFighter != null);
        this.activeIndex = (initialIndex == 1 && enemyFighter != null) ? 1 : (playerFighter != null ? 0 : 1);

        Font boldFont = DrawUtils.UI_FONT_BOLD;
        Font plainFont = DrawUtils.UI_FONT_PLAIN;
        int boldH = DrawUtils.getFontHeight(boldFont);
        int plainH = DrawUtils.getFontHeight(plainFont);

        this.titleH = boldH + 5;
        int twoLineTextH = boldH + plainH + 6;
        this.identityH = twoLineTextH > 36 ? twoLineTextH : 36;
        this.barH = (plainH + 4) > 15 ? (plainH + 4) : 15;
        this.barGap = 3;
        this.skillRowH = plainH + 2;
        this.closeBtnH = boldH + 5;

        int w1 = DrawUtils.getStringWidth(boldFont, "Máu:");
        int w2 = DrawUtils.getStringWidth(boldFont, "Lương:");
        int w3 = DrawUtils.getStringWidth(boldFont, "Nộ khí:");
        int maxLabelW = w1 > w2 ? w1 : w2;
        if (w3 > maxLabelW) {
            maxLabelW = w3;
        }
        this.labelColW = maxLabelW + 6;

        super.b_ = 228;
        this.recomputeHeightForCurrentFighter();
        super.redrawParentScreen = true;
        this.a((SoftKeyBar) null);
    }

    private void recomputeHeightForCurrentFighter() {
        BattleFighter fighter = this.fighters[this.activeIndex];
        Font boldFont = DrawUtils.UI_FONT_BOLD;
        Font plainFont = DrawUtils.UI_FONT_PLAIN;
        int boldH = DrawUtils.getFontHeight(boldFont);
        int contentW = super.b_ - 20;

        this.infoScrollY = 0;
        if (fighter == null) {
            this.wrappedEffects = new String[0];
            this.wrappedPassive = new String[0];
            this.wrappedTactic = null;
            this.infoMaxScroll = 0;
            super.c_ = 220;
            super.g = (GameConfig.screenWidth - super.b_) >> 1;
            super.a_ = (GameConfig.screenHeight - super.c_) >> 1;
            return;
        }

        String buffSummary = this.buildBuffSummary(fighter);
        this.cachedBuffRaw = buffSummary;
        this.hasActiveStatusEffects = !"Bình thường".equals(buffSummary);
        this.wrappedEffects = DrawUtils.wrapText(plainFont, "Hiệu ứng: " + buffSummary, contentW);

        String passivePrefix = fighter.isBossFighter() ? "Khả năng: " : "Nội tại: ";
        this.wrappedPassive = DrawUtils.wrapText(plainFont, passivePrefix + fighter.getPassiveShortDesc(), contentW);

        if (fighter.isBossFighter()) {
            this.tacticIsBoss = true;
            this.wrappedTactic = DrawUtils.wrapText(
                    plainFont, "Phương án: " + fighter.getBossCounterStrategy(), contentW);
        } else if (CampaignData.f != null && CampaignData.isBossTemplate(CampaignData.f.a)) {
            this.tacticIsBoss = false;
            this.wrappedTactic = DrawUtils.wrapText(
                    plainFont, "Màn Boss: Phá Ấn trong 3 lượt cướp +6 Nộ, +5% Máu & Khắc chế", contentW);
        } else {
            this.tacticIsBoss = false;
            this.wrappedTactic = null;
        }

        int[] skills = fighter.d();
        int skillLines = (skills == null || skills.length == 0) ? 1 : (skills.length > 3 ? 3 : skills.length);

        int topFixedH = 5
                + this.titleH
                + 4
                + this.identityH
                + 6
                + (3 * this.barH + 2 * this.barGap)
                + 6;

        int infoContentH = DrawUtils.getWrappedHeight(plainFont, this.wrappedEffects) + 3
                + DrawUtils.getWrappedHeight(plainFont, this.wrappedPassive) + 3
                + (this.wrappedTactic != null ? (DrawUtils.getWrappedHeight(plainFont, this.wrappedTactic) + 3) : 0)
                + (boldH + 2)
                + (skillLines * this.skillRowH);

        int bottomFixedH = 5 + this.closeBtnH + 5;
        int idealH = topFixedH + infoContentH + bottomFixedH;
        int maxH = GameConfig.screenHeight - 6;
        int totalH = idealH > maxH ? maxH : idealH;

        this.infoViewportTopOffset = topFixedH;
        this.infoViewportH = totalH - topFixedH - bottomFixedH;
        this.infoMaxScroll = infoContentH > this.infoViewportH ? (infoContentH - this.infoViewportH) : 0;

        super.c_ = totalH;
        super.g = (GameConfig.screenWidth - super.b_) >> 1;
        super.a_ = (GameConfig.screenHeight - super.c_) >> 1;
    }

    protected void e() {
        ++this.animTick;
        int wheel = HdInputState.consumeWheelDelta();
        if (wheel != 0) {
            if (this.infoMaxScroll > 0) {
                this.infoScrollY = Math.max(0, Math.min(this.infoMaxScroll, this.infoScrollY + wheel * 20));
            } else if (this.canSwitchSides) {
                this.activeIndex = 1 - this.activeIndex;
                this.animTick = 0;
                this.recomputeHeightForCurrentFighter();
            }
        }
    }

    public void a(int keyCode) {
        if (this.canSwitchSides && (keyCode == 14 || keyCode == 15)) {
            this.activeIndex = 1 - this.activeIndex;
            this.animTick = 0;
            this.recomputeHeightForCurrentFighter();
            return;
        }
        if (this.infoMaxScroll > 0 && keyCode == 12) {
            this.infoScrollY -= 20;
            if (this.infoScrollY < 0) {
                this.infoScrollY = 0;
            }
            return;
        }
        if (this.infoMaxScroll > 0 && keyCode == 13) {
            this.infoScrollY += 20;
            if (this.infoScrollY > this.infoMaxScroll) {
                this.infoScrollY = this.infoMaxScroll;
            }
            return;
        }
        if (keyCode == 16 || keyCode == 18 || keyCode == 17 || keyCode == 11) {
            this.closeDialog();
        }
    }

    public void a(int pointerX, int pointerY) {
        // Tap outside dialog frame -> close immediately
        if (pointerX < super.g || pointerX > super.g + super.b_
                || pointerY < super.a_ || pointerY > super.a_ + super.c_) {
            this.closeDialog();
            return;
        }
        // Tap top dual-tab header -> switch between Player and Enemy
        if (this.canSwitchSides && pointerY >= super.a_ + 4 && pointerY <= super.a_ + 8 + this.titleH) {
            int midX = super.g + (super.b_ >> 1);
            int targetIdx = (pointerX < midX) ? 0 : 1;
            if (targetIdx != this.activeIndex) {
                this.activeIndex = targetIdx;
                this.animTick = 0;
                this.recomputeHeightForCurrentFighter();
                return;
            }
        }
        // Tap bottom close button -> close
        if (pointerY >= super.a_ + super.c_ - this.closeBtnH - 10) {
            this.closeDialog();
            return;
        }
        // If extreme multi-buff state requires vertical scroll, tap upper/lower info area to scroll
        if (this.infoMaxScroll > 0) {
            int vpTop = super.a_ + this.infoViewportTopOffset;
            int vpBot = vpTop + this.infoViewportH;
            if (pointerY >= vpTop && pointerY <= vpBot) {
                int midVp = (vpTop + vpBot) >> 1;
                if (pointerY < midVp) {
                    this.infoScrollY = this.infoScrollY > 24 ? (this.infoScrollY - 24) : 0;
                } else {
                    int next = this.infoScrollY + 24;
                    this.infoScrollY = next > this.infoMaxScroll ? this.infoMaxScroll : next;
                }
                return;
            }
        }
        if (!this.canSwitchSides && this.infoMaxScroll <= 0) {
            this.closeDialog();
        }
    }

    public void c(int deltaY) {
        if (deltaY != 0 && this.infoMaxScroll > 0) {
            int next = this.infoScrollY - deltaY;
            if (next < 0) {
                next = 0;
            } else if (next > this.infoMaxScroll) {
                next = this.infoMaxScroll;
            }
            this.infoScrollY = next;
        }
    }

    private void closeDialog() {
        GUIManager.getInstance().a(false);
    }

    private String buildBuffSummary(BattleFighter fighter) {
        String result = "";
        if (fighter.getBarrierHp() > 0) {
            result = "Giáp Ảo (" + fighter.getBarrierHp() + ")";
        }
        if (fighter.k()) {
            int shieldTurns = fighter.getShieldTurnsDisplay();
            String sText = shieldTurns > 0 ? ("Khiên Sét (" + shieldTurns + ")") : "Khiên Sét";
            result = result.length() > 0 ? (result + ", " + sText) : sText;
        }
        if (fighter.isPhase2Triggered() && fighter.getBossArchetype() == BattleFighter.BOSS_ARCHETYPE_OVERLORD_PHASE) {
            result = result.length() > 0 ? (result + ", Pha 2 Long Nộ (+15% Công)") : "Pha 2 Long Nộ (+15% Công)";
        } else if (fighter.isHighBossLastStandActive()) {
            result = result.length() > 0 ? (result + ", Tử Chiến (+10% Công)") : "Tử Chiến (+10% Công)";
        } else if (fighter.isEnragedActive()) {
            String furyName = fighter.getPassiveId() == BattleFighter.PASSIVE_BEAST_FURY ? "Hung Thú (+20% Công)" : "Hổ Uy (+20% Công)";
            result = result.length() > 0 ? (result + ", " + furyName) : furyName;
        }
        if (fighter.getBurnTurns() > 0) {
            String burnText = "Bị Bỏng (" + fighter.getBurnTurns() + ": +20% ST nhận, -40% Lương)";
            result = result.length() > 0 ? (result + ", " + burnText) : burnText;
        }
        if (fighter.getArmorBreakTurns() > 0) {
            String frostText = "Hàn Băng (" + fighter.getArmorBreakTurns() + ": +25% ST nhận, xuyên Thủ, -40% hồi máu)";
            result = result.length() > 0 ? (result + ", " + frostText) : frostText;
        }
        if (fighter.getPoisonTurns() > 0) {
            String poisonText = "Trúng Độc (" + fighter.getPoisonTurns() + ": -30% hồi Tim)";
            result = result.length() > 0 ? (result + ", " + poisonText) : poisonText;
        }
        if (fighter.getWoundTurns() > 0) {
            String woundText = "Trọng Thương (" + fighter.getWoundTurns() + ": -20% Công, +75% Lương, -40% hồi)";
            result = result.length() > 0 ? (result + ", " + woundText) : woundText;
        }
        if (fighter.getCurseTurns() > 0) {
            String curseText = "Nhiễu Hồn (" + fighter.getCurseTurns() + ": -40% Nộ)";
            result = result.length() > 0 ? (result + ", " + curseText) : curseText;
        }
        if (fighter.isStarving()) {
            result = result.length() > 0 ? (result + ", Tuyệt lương (-30% Công, khóa Chiêu 3)") : "Tuyệt lương (-30% Công, khóa Chiêu 3)";
        } else if (fighter.isLowFood()) {
            result = result.length() > 0 ? (result + ", Thiếu lương (-15% Công, -25% Nộ)") : "Thiếu lương (-15% Công, -25% Nộ)";
        }
        if (fighter.a != null) {
            if (fighter.a.length > 0 && fighter.a[0].b > 0) {
                result = result.length() > 0 ? (result + ", x2 Công") : "x2 Công";
            }
            if (fighter.a.length > 3 && fighter.a[3].b > 0) {
                result = result.length() > 0 ? (result + ", Giáp -75% ST") : "Giáp -75% ST";
            }
        }
        return result.length() > 0 ? result : "Bình thường";
    }

    public void a(Graphics g) {
        BattleFighter fighter = this.fighters[this.activeIndex];
        if (fighter == null) {
            return;
        }
        String liveBuff = this.buildBuffSummary(fighter);
        if (!liveBuff.equals(this.cachedBuffRaw)) {
            this.recomputeHeightForCurrentFighter();
        }

        Image portrait = this.portraits[this.activeIndex];
        boolean isPlayer = (this.activeIndex == 0);

        int x = super.g;
        int y = super.a_;
        int w = super.b_;
        int h = super.c_;
        Font boldFont = DrawUtils.UI_FONT_BOLD;
        Font plainFont = DrawUtils.UI_FONT_PLAIN;
        int boldH = DrawUtils.getFontHeight(boldFont);
        int plainH = DrawUtils.getFontHeight(plainFont);

        // 1. Outer antique wood-and-bronze card frame
        DrawUtils.drawAntiquePanel(
                g, x, y, w, h,
                0x362417, 0x22150D, 0x130B06,
                isPlayer ? 0x33D6C4 : 0xE0467C,
                isPlayer ? 0x8CF5E8 : 0xFF96BC,
                0xFFE680);

        int curY = y + 5;

        // 2. Dual-Side Tab Header (or single banner when only 1 fighter provided)
        if (this.canSwitchSides) {
            int halfW = (w - 14) >> 1;
            int t0X = x + 6;
            int t1X = t0X + halfW + 2;
            DrawUtils.drawAntiquePanel(
                    g, t0X, curY, halfW, this.titleH,
                    isPlayer ? 0x1A5E54 : 0x291B11,
                    isPlayer ? 0x114039 : 0x191009,
                    0x0E0804,
                    isPlayer ? 0xFFD700 : 0x7A592A,
                    isPlayer ? 0xFFF2A6 : 0xA88044,
                    0xFFFFFF);
            DrawUtils.drawShadowText(
                    g, boldFont, "1. CHỦ TƯỚNG",
                    t0X + (halfW >> 1), curY + ((this.titleH - boldH) >> 1),
                    Graphics.TOP | Graphics.HCENTER,
                    isPlayer ? 0xFFF5B8 : 0xB8A48C, 0x120804);

            DrawUtils.drawAntiquePanel(
                    g, t1X, curY, halfW, this.titleH,
                    !isPlayer ? 0x6E1636 : 0x291B11,
                    !isPlayer ? 0x4D0D24 : 0x191009,
                    0x0E0804,
                    !isPlayer ? 0xFFD700 : 0x7A592A,
                    !isPlayer ? 0xFFF2A6 : 0xA88044,
                    0xFFFFFF);
            DrawUtils.drawShadowText(
                    g, boldFont, "2. ĐỐI THỦ",
                    t1X + (halfW >> 1), curY + ((this.titleH - boldH) >> 1),
                    Graphics.TOP | Graphics.HCENTER,
                    !isPlayer ? 0xFFF5B8 : 0xB8A48C, 0x120804);
        } else {
            DrawUtils.drawAntiquePanel(
                    g, x + 6, curY, w - 12, this.titleH,
                    isPlayer ? 0x1A5E54 : 0x6E1636,
                    isPlayer ? 0x114039 : 0x4D0D24,
                    0x120A06,
                    0xB8893D, 0xF0C975, 0xFFFFFF);
            String title = isPlayer ? "THÔNG TIN CHỦ TƯỚNG" : "THÔNG TIN ĐỐI THỦ";
            DrawUtils.drawShadowText(
                    g, boldFont, title,
                    x + (w >> 1), curY + ((this.titleH - boldH) >> 1),
                    Graphics.TOP | Graphics.HCENTER,
                    0xFFF4D0, 0x120804);
        }
        curY += this.titleH + 4;

        // 3. Identity Row: 36x36 Portrait on Left + crisp static Attack & Role on Right
        int portX = x + 10;
        int portY = curY + ((this.identityH - 36) >> 1);
        DrawUtils.drawAntiquePanel(
                g, portX, portY, 36, 36,
                0x3D291A, 0x1C110A, 0x100905,
                0xC29444, 0xF5D27E, 0xFFFFFF);
        if (portrait != null) {
            g.drawImage(portrait, portX + 3, portY + 3, Graphics.TOP | Graphics.LEFT);
        }

        int textX = portX + 42;
        int rightEdge = x + w - 10;
        String lvText = "Cấp " + fighter.getLevel();
        int lvW = DrawUtils.getStringWidth(boldFont, lvText);

        DrawUtils.drawShadowText(
                g, boldFont, lvText,
                rightEdge, curY,
                Graphics.TOP | Graphics.RIGHT,
                isPlayer ? 0x8CF5E8 : 0xFFB0CC, 0x120804);
        int nameBoxW = (rightEdge - textX) - lvW - 6;
        DrawUtils.drawFullNameInBox(
                g, fighter.getDisplayName(),
                textX, curY, nameBoxW, boldH,
                false, 0xFFE680, 0x120804, this.animTick);

        String roleTitle = fighter.isBossFighter()
                ? fighter.getBossArchetypeTitle(fighter.getBossArchetype())
                : fighter.getPassiveName();
        String atkRoleLine = "Công: " + fighter.m() + "  |  " + roleTitle;
        int line2Y = curY + boldH + 3;
        DrawUtils.drawShadowText(
                g, plainFont, atkRoleLine,
                textX, line2Y,
                Graphics.TOP | Graphics.LEFT,
                0xFFF4DC, 0x120804);

        curY += this.identityH + 2;

        g.setColor(0x7A592A);
        g.drawLine(x + 10, curY, rightEdge, curY);
        curY += 4;

        // 4. Stat Bars: Máu, Lương, Nộ khí (Ornate RPG Gemstone Gauges)
        int labelX = x + 10;
        int barX = labelX + this.labelColW;
        int barW = rightEdge - barX;
        int labelOffsetY = (this.barH - boldH) >> 1;

        int curHp = fighter.r() < 0 ? 0 : fighter.r();
        int maxHp = fighter.s();
        int barrierHp = fighter.getBarrierHp();
        String hpLabel = barrierHp > 0 ? (curHp + "/" + maxHp + " (+Giáp " + barrierHp + ")") : (curHp + " / " + maxHp);
        DrawUtils.drawShadowText(g, boldFont, "Máu:", labelX, curY + labelOffsetY, Graphics.TOP | Graphics.LEFT, 0xFF9E9E, 0x120804);
        DrawUtils.drawRpgStatBar(
                g, barX, curY, barW, this.barH,
                curHp, maxHp,
                0xFF7D7D, 0xCC2424, 0x801111, 0xFFC4C4,
                4,
                null);
        if (barrierHp > 0 && fighter.getMaxBarrierHp() > 0) {
            int bw = (barW - 4) * barrierHp / fighter.getMaxBarrierHp();
            if (bw > barW - 4) bw = barW - 4;
            if (bw < 2) bw = 2;
            g.setColor(0x3DF2FF);
            g.fillRect(barX + 2, curY + this.barH - 4, bw, 2);
        }
        DrawUtils.drawShadowText(
                g, plainFont, hpLabel,
                barX + (barW >> 1), curY + ((this.barH - plainH) >> 1),
                Graphics.TOP | Graphics.HCENTER, 0xFFFFFF, 0x100603);
        curY += this.barH + this.barGap;

        int foodPct = fighter.q() > 0 ? (fighter.p() * 100 / fighter.q()) : 0;
        DrawUtils.drawShadowText(g, boldFont, "Lương:", labelX, curY + labelOffsetY, Graphics.TOP | Graphics.LEFT, 0xA8F088, 0x120804);
        DrawUtils.drawRpgStatBar(
                g, barX, curY, barW, this.barH,
                fighter.p(), fighter.q(),
                0x9EF57A, 0x3A8F22, 0x1E5210, 0xD4FFC2,
                4,
                fighter.p() + "/" + fighter.q() + " (" + foodPct + "%)");
        curY += this.barH + this.barGap;

        int curMana = fighter.o();
        int maxMana = fighter.n();
        int rawTiers = maxMana > 0 ? (curMana * 3 / maxMana) : 0;
        int readyTiers = (fighter.isStarving() && rawTiers > 2) ? 2 : rawTiers;
        DrawUtils.drawShadowText(g, boldFont, "Nộ khí:", labelX, curY + labelOffsetY, Graphics.TOP | Graphics.LEFT, 0x7AE8FF, 0x120804);
        DrawUtils.drawRpgStatBar(
                g, barX, curY, barW, this.barH,
                curMana, maxMana,
                0x8CF5FF, 0x168FA3, 0x0B5261, 0xD6FFFF,
                3,
                curMana + " / " + maxMana + " (Bậc " + readyTiers + ")");
        curY += this.barH + 2;

        g.setColor(0x7A592A);
        g.drawLine(x + 10, curY, rightEdge, curY);
        curY += 4;

        // 5. Multi-Line Wrapped Active Effects, Passive/Ability, Boss Counter-Strategy & Skills (Zero Marquee!)
        int contentW = rightEdge - labelX;
        int vpTop = curY;
        if (this.infoMaxScroll > 0) {
            DrawUtils.saveClip(g, this.savedClip);
            DrawUtils.intersectClip(g, x + 6, vpTop, w - 12, this.infoViewportH);
            curY -= this.infoScrollY;
        }

        int effH = DrawUtils.getWrappedHeight(plainFont, this.wrappedEffects);
        DrawUtils.drawWrappedShadowText(
                g, plainFont, this.wrappedEffects,
                labelX, curY, contentW, effH + 2,
                this.hasActiveStatusEffects ? 0xFFE680 : 0xD8C8B0, 0x120804);
        curY += effH + 3;

        int pasH = DrawUtils.getWrappedHeight(plainFont, this.wrappedPassive);
        DrawUtils.drawWrappedShadowText(
                g, plainFont, this.wrappedPassive,
                labelX, curY, contentW, pasH + 2,
                0x99F2FF, 0x120804);
        curY += pasH + 3;

        if (this.wrappedTactic != null && this.wrappedTactic.length > 0) {
            int tacH = DrawUtils.getWrappedHeight(plainFont, this.wrappedTactic);
            DrawUtils.drawWrappedShadowText(
                    g, plainFont, this.wrappedTactic,
                    labelX, curY, contentW, tacH + 2,
                    this.tacticIsBoss ? 0xFFD980 : 0x7CFF9E, 0x120804);
            curY += tacH + 3;
        }

        DrawUtils.drawShadowText(g, boldFont, "Tuyệt chiêu:", labelX, curY, Graphics.TOP | Graphics.LEFT, 0xFFE07A, 0x120804);
        if (this.infoMaxScroll > 0) {
            String scrollHint = this.infoScrollY < this.infoMaxScroll ? "[Cuộn xuống]" : "[Cuộn lên]";
            DrawUtils.drawShadowText(g, plainFont, scrollHint, rightEdge, curY + 1, Graphics.TOP | Graphics.RIGHT, 0x8CF5E8, 0x120804);
        }
        curY += boldH + 2;

        int[] skills = fighter.d();
        if (skills == null || skills.length == 0) {
            DrawUtils.drawShadowText(g, plainFont, "- Không có tuyệt chiêu", labelX + 4, curY, Graphics.TOP | Graphics.LEFT, 0xC8B8A2, 0x120804);
        } else {
            int maxSkills = skills.length > 3 ? 3 : skills.length;
            for (int i = 0; i < maxSkills; ++i) {
                int sid = skills[i];
                String skillName = (sid >= 0 && sid < GameStrings.a.length) ? GameStrings.a[sid] : ("Kỹ năng #" + sid);
                boolean starvedLock = (i == 2 && fighter.isStarving() && rawTiers >= 3);
                boolean ready = (i < readyTiers);
                String suffix = starvedLock ? " [Khóa: 0 Lương]" : (ready ? " [Sẵn sàng]" : "");
                String row = (i + 1) + ". " + skillName + suffix;
                int rowCol = starvedLock ? 0xFFAE70 : (ready ? 0x7CFF9E : 0xE6D8C0);
                DrawUtils.drawShadowText(
                        g, plainFont, row,
                        labelX + 4, curY,
                        Graphics.TOP | Graphics.LEFT,
                        rowCol, 0x120804);
                curY += this.skillRowH;
            }
        }

        if (this.infoMaxScroll > 0) {
            DrawUtils.restoreClip(g, this.savedClip);
            if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
                HdUiRenderer.drawHdVerticalScrollbar(
                        g.getHdCanvas(), x + w - 6, vpTop, this.infoViewportH,
                        this.infoScrollY, this.infoMaxScroll, this.infoViewportH);
            }
        }

        // 6. Antique "ĐÓNG" button inside bottom of dialog card
        int btnW = 84;
        int btnX = x + ((w - btnW) >> 1);
        int btnY = y + h - this.closeBtnH - 5;
        boolean closeHover = HdInputState.isHovered(btnX, btnY, btnW, this.closeBtnH);
        DrawUtils.drawAntiquePanel(
                g, btnX, btnY, btnW, this.closeBtnH,
                closeHover ? 0x702B33 : 0x542026,
                closeHover ? 0x4D1B21 : 0x381418,
                0x1F0A0C,
                closeHover ? 0xFFD700 : 0xC29444,
                0xF5D27E, 0xFFFFFF);
        int btnTextY = btnY + ((this.closeBtnH - boldH) >> 1);
        DrawUtils.drawShadowText(
                g, boldFont, "ĐÓNG",
                x + (w >> 1), btnTextY,
                Graphics.TOP | Graphics.HCENTER,
                0xFFF4D0, 0x120804);
    }
}
