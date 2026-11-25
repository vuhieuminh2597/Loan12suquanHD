package com.mic.smsgame.suquan.gfx;

import com.loan12suquan.hd.HdBattleStageRenderer;
import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdInputState;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.BattleResourceManager;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.FontManager;
import com.mg.engine.sprite.AnimatedSprite;
import com.mg.engine.sprite.Drawable;
import com.mg.engine.sprite.DummyDrawable;
import com.mg.engine.util.RandomUtils;
import com.mic.smsgame.suquan.model.BattleFighter;
import com.mic.smsgame.suquan.vfx.BarSparkParticle;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 * Battle HUD renderer drawing both combatants' HP/over-HP/food/mana bars, active turn glow frames,
 * hit-slash effects, buff aura sprites, and bar spark particles.
 */
public class BattleHudView extends Drawable {
    private static final int[] PLAYER_TURN_COLORS = new int[]{0x013D38, 0x014E48, 0x005C55};
    private static final int[] ENEMY_TURN_COLORS = new int[]{0x4D0139, 0x5D0145, 0x6E0151};

    private BattleFighter[] fighters;
    private Image hpBarImage;
    private Image overHpBarImage;
    private Image manaBarImage;
    private Image foodBarImage;
    private Image slashSheet;
    private DummyDrawable[] avatarAnchors;
    private Image[] fighterPortraits;

    public void setFighterPortraits(Image[] portraits) {
        this.fighterPortraits = portraits;
    }

    private int turnGlowTick;
    private int hitTargetIndex;
    private int hitFlashTimer;
    private int queuedHitFlashTimer;
    private int activeTurnIndex = 0;
    private boolean hideTurnBorder;
    private int blinkCycleTick;
    private BarSparkParticle[] hpSparks;
    private BarSparkParticle[] manaSparks;
    private int hpDrainDelay;
    private int manaFlashTimer;
    private int[] lowHpBlinkTimers;
    private AnimatedSprite[] playerBuffAuras;

    private static final int POPUP_DAMAGE = 0;
    private static final int POPUP_HEAL = 1;
    private static final int POPUP_BARRIER_GAIN = 2;
    private static final int POPUP_BARRIER_HIT = 3;
    private static final int POPUP_PIERCE = 4;
    private static final int POPUP_FOOD_BURN = 5;

    // Enhanced HUD state: fighter full display names, HP damage trail, hit-synced popups, and banner queue.
    private String[] fighterLabels;
    private int headerAnimTick;
    private int[] trailHp;
    private int[] lastActualHp;
    private int[] lastBarrierHp;
    private int[] pendingHpDelta;
    private int[] pendingBarrierDelta;
    private int[] pendingFoodBurn;
    private boolean[] pendingPierce;
    private String[] popupTexts;
    private int[] popupX;
    private int[] popupY;
    private int[] popupTicks;
    private int[] popupTypes;
    private int[] popupOwner;
    private String skillBannerText;
    private int skillBannerTicks;
    private boolean skillBannerPlayer;
    private boolean skillBannerShatter;
    private String queuedBannerText;
    private boolean queuedBannerPlayer;
    private boolean queuedBannerShatter;

    // Reusable status-pill buffers (zero per-frame allocations)
    private String[] pillLabels = new String[14];
    private int[] pillFillTop = new int[14];
    private int[] pillFillBot = new int[14];
    private int[] pillBorder = new int[14];
    private int[] pillText = new int[14];

    public BattleHudView(BattleFighter[] fighters) {
        this.fighters = fighters;
        this.loadSprites();

        this.fighterLabels = new String[2];
        this.trailHp = new int[2];
        this.lastActualHp = new int[2];
        this.lastBarrierHp = new int[2];
        this.pendingHpDelta = new int[2];
        this.pendingBarrierDelta = new int[2];
        this.pendingFoodBurn = new int[2];
        this.pendingPierce = new boolean[2];
        this.popupTexts = new String[8];
        this.popupX = new int[8];
        this.popupY = new int[8];
        this.popupTicks = new int[8];
        this.popupTypes = new int[8];
        this.popupOwner = new int[8];
        this.resetTracking();

        this.avatarAnchors = new DummyDrawable[2];
        this.avatarAnchors[0] = new DummyDrawable();
        this.avatarAnchors[0].d(GameConfig.contentOffsetX + 35, GameConfig.contentOffsetY + 237);
        this.avatarAnchors[1] = new DummyDrawable();
        this.avatarAnchors[1].d(GameConfig.contentOffsetX + 175, GameConfig.contentOffsetY + 237);

        byte[][] sparkFrames = new byte[][]{{0, 0, 0, 0, 1, 1, 1, 2, 2, 1}};
        Image hpSparkSheet = BattleResourceManager.a().c[2];
        this.hpSparks = new BarSparkParticle[25];
        for (int i = 0; i < this.hpSparks.length; ++i) {
            this.hpSparks[i] = new BarSparkParticle();
            this.hpSparks[i].a(sparkFrames);
            this.hpSparks[i].a(hpSparkSheet, 3);
            this.hpSparks[i].a(false);
        }

        Image manaSparkSheet = BattleResourceManager.a().c[3];
        this.manaSparks = new BarSparkParticle[10];
        for (int i = 0; i < this.manaSparks.length; ++i) {
            this.manaSparks[i] = new BarSparkParticle();
            this.manaSparks[i].a(sparkFrames);
            this.manaSparks[i].a(manaSparkSheet, 3);
            this.manaSparks[i].a(false);
        }

        this.lowHpBlinkTimers = new int[2];
        this.playerBuffAuras = new AnimatedSprite[2];
        this.playerBuffAuras[0] = new AnimatedSprite(BattleResourceManager.a().C, 3);
        this.playerBuffAuras[0].d(
                this.avatarAnchors[0].i() + this.avatarAnchors[0].k() / 2,
                this.avatarAnchors[0].j() + this.avatarAnchors[0].l() / 2);
        this.playerBuffAuras[0].k(Graphics.HCENTER | Graphics.VCENTER);

        this.playerBuffAuras[1] = new AnimatedSprite(BattleResourceManager.a().D, 3);
        this.playerBuffAuras[1].d(
                this.avatarAnchors[0].i() + this.avatarAnchors[0].k() / 2,
                this.avatarAnchors[0].j() + this.avatarAnchors[0].l());
        this.playerBuffAuras[1].k(Graphics.HCENTER | Graphics.BOTTOM);

        byte[][] auraFrames = new byte[][]{{0, 0, 1, 1, 2, 2}};
        for (int i = 0; i < this.playerBuffAuras.length; ++i) {
            this.playerBuffAuras[i].a(false);
            this.playerBuffAuras[i].a(auraFrames);
        }
    }

    private void loadSprites() {
        BattleResourceManager res = BattleResourceManager.a();
        this.hpBarImage = res.l;
        this.manaBarImage = res.n;
        this.foodBarImage = res.o;
        this.overHpBarImage = res.m;
        this.slashSheet = res.p;
    }

    public DummyDrawable[] a() {
        return this.avatarAnchors;
    }

    public void b(boolean visible) {
        this.playerBuffAuras[1].a(visible);
    }

    public void c(boolean visible) {
        this.playerBuffAuras[0].a(visible);
    }

    /** Resets tracked HP/Barrier deltas, popups, and banner queue (e.g., on battle retry or snapshot load). */
    public void resetTracking() {
        for (int i = 0; i < 2; ++i) {
            this.fighterLabels[i] = this.fighters[i].getDisplayName() + " (C." + this.fighters[i].getLevel() + ")";
            this.trailHp[i] = this.fighters[i].r();
            this.lastActualHp[i] = this.fighters[i].r();
            this.lastBarrierHp[i] = this.fighters[i].getBarrierHp();
            this.pendingHpDelta[i] = 0;
            this.pendingBarrierDelta[i] = 0;
            this.pendingFoodBurn[i] = 0;
            this.pendingPierce[i] = false;
        }
        for (int i = 0; i < this.popupTicks.length; ++i) {
            this.popupTicks[i] = 0;
        }
        this.skillBannerTicks = 0;
        this.skillBannerText = null;
        this.queuedBannerText = null;
        this.hitFlashTimer = 0;
        this.queuedHitFlashTimer = 0;
        this.hpDrainDelay = 0;
    }

    public void showFoodBurnPopup(int fighterIndex, int burnAmount) {
        if (fighterIndex >= 0 && fighterIndex < this.pendingFoodBurn.length && burnAmount > 0) {
            this.pendingFoodBurn[fighterIndex] += burnAmount;
        }
    }

    public void markNextHitAsPierce(int fighterIndex) {
        if (fighterIndex >= 0 && fighterIndex < this.pendingPierce.length) {
            this.pendingPierce[fighterIndex] = true;
        }
    }

    public void showSkillBanner(String text, boolean isPlayer, boolean isShatter) {
        if (text == null) {
            return;
        }
        if (this.skillBannerTicks > 8 && this.skillBannerText != null && !text.equals(this.skillBannerText)) {
            if (this.skillBannerTicks > 18) {
                this.skillBannerTicks = 18;
            }
            this.queuedBannerText = text;
            this.queuedBannerPlayer = isPlayer;
            this.queuedBannerShatter = isShatter;
            return;
        }
        this.skillBannerText = text;
        this.skillBannerPlayer = isPlayer;
        this.skillBannerShatter = isShatter;
        this.skillBannerTicks = 38;
    }

    private void addPillSlot(int idx, String label, int fillTop, int fillBot, int border, int textCol) {
        if (idx >= 0 && idx < this.pillLabels.length) {
            this.pillLabels[idx] = label;
            this.pillFillTop[idx] = fillTop;
            this.pillFillBot[idx] = fillBot;
            this.pillBorder[idx] = border;
            this.pillText[idx] = textCol;
        }
    }

    private int drawStatusPill(
            Graphics g, String label, int x, int y, boolean alignRight,
            int fillTop, int fillBot, int borderCol, int textCol) {
        int plainH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_PLAIN);
        int pillW = DrawUtils.getStringWidth(DrawUtils.UI_FONT_PLAIN, label) + 8;
        int pillH = plainH + 3;
        int drawX = alignRight ? (x - pillW) : x;
        DrawUtils.drawAntiquePanel(
                g, drawX, y, pillW, pillH,
                fillTop, fillBot, 0x100805,
                borderCol, 0xF5D487, 0xFFFFFF);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_PLAIN, label,
                drawX + (pillW >> 1), y + 2, Graphics.TOP | Graphics.HCENTER,
                textCol, 0x120804);
        return pillW + 2;
    }

    private void drawFighterStatusRow(Graphics g, int fighterIndex) {
        BattleFighter f = this.fighters[fighterIndex];
        boolean rightSide = (fighterIndex == 1);
        int ox = GameConfig.contentOffsetX;
        int oy = GameConfig.contentOffsetY;
        int curX = rightSide ? (ox + 236) : (ox + 4);
        int rowY = oy + 272;
        int count = 0;

        if (f.k()) {
            int turns = f.getShieldTurnsDisplay();
            String tag = turns > 0 ? ("Khiên:" + turns) : "Khiên";
            this.addPillSlot(count++, tag, 0x185E70, 0x0E3B47, 0x5EE8FF, 0xD9FBFF);
        }
        if (f.getBarrierHp() > 0) {
            this.addPillSlot(count++, "Giáp:" + f.getBarrierHp(), 0x0E4E78, 0x08304D, 0x4CE2FF, 0xD6FAFF);
        }
        if (f.isPhase2Triggered() && f.getBossArchetype() == BattleFighter.BOSS_ARCHETYPE_OVERLORD_PHASE) {
            this.addPillSlot(count++, "Pha 2", 0x8A1822, 0x540C12, 0xFFD700, 0xFFF5B8);
        }
        if (f.getBurnTurns() > 0) {
            this.addPillSlot(count++, "Bỏng:" + f.getBurnTurns(), 0x7A2A0E, 0x4F1906, 0xFF8C3A, 0xFFE0B2);
        }
        if (f.getArmorBreakTurns() > 0) {
            this.addPillSlot(count++, "Hàn:" + f.getArmorBreakTurns(), 0x0D5C75, 0x073747, 0x5CEBFF, 0xD9FAFF);
        }
        if (f.getPoisonTurns() > 0) {
            this.addPillSlot(count++, "Độc:" + f.getPoisonTurns(), 0x3E1E59, 0x261038, 0xB86BFF, 0xE8C8FF);
        }
        if (f.getWoundTurns() > 0) {
            this.addPillSlot(count++, "Thương:" + f.getWoundTurns(), 0x6B1228, 0x420A18, 0xFF5577, 0xFFD0DB);
        }
        if (f.getCurseTurns() > 0) {
            this.addPillSlot(count++, "Loạn:" + f.getCurseTurns(), 0x1E2963, 0x111840, 0x7AA6FF, 0xD6E4FF);
        }
        if (f.isStarving()) {
            this.addPillSlot(count++, "Cạn Lương!", 0x7A1212, 0x4D0808, 0xFF5C5C, 0xFFD6D6);
        } else if (f.isLowFood()) {
            this.addPillSlot(count++, "Đói -15%", 0x7A4B08, 0x4D2E04, 0xFFBB33, 0xFFF0B8);
        }
        if (f.isHighBossLastStandActive()) {
            this.addPillSlot(count++, "Tử Chiến", 0x8A1822, 0x540C12, 0xFFD700, 0xFFF5B8);
        } else if (f.isEnragedActive() && f.getBossArchetype() != BattleFighter.BOSS_ARCHETYPE_OVERLORD_PHASE) {
            String enrageLabel = f.getPassiveId() == BattleFighter.PASSIVE_BEAST_FURY ? "Hung +20%" : "Cuồng +20%";
            this.addPillSlot(count++, enrageLabel, 0x7A3A0A, 0x4F2305, 0xFFB833, 0xFFF0B8);
        }
        if (f.a != null && f.a.length > 0 && f.a[0].b > 0) {
            this.addPillSlot(count++, "x2 Công", 0x7A540E, 0x4D3408, 0xFFD700, 0xFFF5B8);
        }
        if (f.a != null && f.a.length > 3 && f.a[3].b > 0) {
            this.addPillSlot(count++, "-75% ST", 0x27406B, 0x162642, 0x7AB6FF, 0xD6EAFF);
        }

        if (count <= 0) {
            return;
        }
        int startIdx = (count > 3) ? ((this.headerAnimTick / 32) % count) : 0;
        int maxDraw = count < 3 ? count : 3;
        int usedW = 0;
        for (int n = 0; n < maxDraw; ++n) {
            int pIdx = (startIdx + n) % count;
            int estW = DrawUtils.getStringWidth(DrawUtils.UI_FONT_PLAIN, this.pillLabels[pIdx]) + 10;
            if (n > 0 && usedW + estW > 114) {
                break;
            }
            int step = this.drawStatusPill(
                    g, this.pillLabels[pIdx], curX, rowY, rightSide,
                    this.pillFillTop[pIdx], this.pillFillBot[pIdx],
                    this.pillBorder[pIdx], this.pillText[pIdx]);
            curX += rightSide ? -step : step;
            usedW += step;
        }
    }

    private void drawSkillBanner(Graphics g) {
        if (this.skillBannerTicks <= 0 || this.skillBannerText == null) {
            return;
        }
        int ox = GameConfig.contentOffsetX;
        int oy = GameConfig.contentOffsetY;
        int boldH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD);
        int plainH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_PLAIN);

        int fillTop = this.skillBannerShatter ? 0x8C3B08 : (this.skillBannerPlayer ? 0x175E54 : 0x731434);
        int fillBot = this.skillBannerShatter ? 0x572204 : (this.skillBannerPlayer ? 0x0E3D36 : 0x4A0A20);
        int borderCol = this.skillBannerShatter ? 0xFFB833 : (this.skillBannerPlayer ? 0x4CE6D3 : 0xFF6699);
        int textCol = this.skillBannerShatter ? 0xFFF5B8 : 0xFFF8E0;

        int boldW = DrawUtils.getStringWidth(DrawUtils.UI_FONT_BOLD, this.skillBannerText);
        if (boldW <= 214) {
            int bw = boldW + 16;
            int bh = boldH + 6;
            int bx = ox + ((240 - bw) >> 1);
            int by = oy + 35;
            DrawUtils.drawAntiquePanel(g, bx, by, bw, bh, fillTop, fillBot, 0x100805, borderCol, 0xFFF2A6, 0xFFFFFF);
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_BOLD, this.skillBannerText,
                    bx + (bw >> 1), by + 3, Graphics.TOP | Graphics.HCENTER,
                    textCol, 0x120804);
            return;
        }

        int plainW = DrawUtils.getStringWidth(DrawUtils.UI_FONT_PLAIN, this.skillBannerText);
        if (plainW <= 218) {
            int bw = plainW + 14;
            int bh = plainH + 6;
            int bx = ox + ((240 - bw) >> 1);
            int by = oy + 35;
            DrawUtils.drawAntiquePanel(g, bx, by, bw, bh, fillTop, fillBot, 0x100805, borderCol, 0xFFF2A6, 0xFFFFFF);
            DrawUtils.drawShadowText(
                    g, DrawUtils.UI_FONT_PLAIN, this.skillBannerText,
                    bx + (bw >> 1), by + 3, Graphics.TOP | Graphics.HCENTER,
                    textCol, 0x120804);
            return;
        }

        String[] lines = DrawUtils.wrapText(DrawUtils.UI_FONT_PLAIN, this.skillBannerText, 216);
        int textH = DrawUtils.getWrappedHeight(DrawUtils.UI_FONT_PLAIN, lines);
        int bw = 228;
        int bh = textH + 6;
        int bx = ox + ((240 - bw) >> 1);
        int by = oy + 34;
        DrawUtils.drawAntiquePanel(g, bx, by, bw, bh, fillTop, fillBot, 0x100805, borderCol, 0xFFF2A6, 0xFFFFFF);
        DrawUtils.drawWrappedCenteredShadowText(
                g, DrawUtils.UI_FONT_PLAIN, lines,
                bx + (bw >> 1), by + 3, textH + 2,
                textCol, 0x120804);
    }

    private void spawnPopup(int fighterIndex, String text, int popupType) {
        int recentOnSameFighter = 0;
        for (int k = 0; k < this.popupTicks.length; ++k) {
            if (this.popupTicks[k] >= 18 && this.popupOwner[k] == fighterIndex) {
                ++recentOnSameFighter;
            }
        }
        for (int i = 0; i < this.popupTicks.length; ++i) {
            if (this.popupTicks[i] <= 0) {
                this.popupTexts[i] = text;
                this.popupX[i] = this.avatarAnchors[fighterIndex].i() + 15;
                this.popupY[i] = this.avatarAnchors[fighterIndex].j() - 6 - (recentOnSameFighter * 15);
                this.popupTicks[i] = 26;
                this.popupTypes[i] = popupType;
                this.popupOwner[i] = fighterIndex;
                return;
            }
        }
    }

    private void drawTopHeader(Graphics g) {
        int ox = GameConfig.contentOffsetX;
        int oy = GameConfig.contentOffsetY + 2;
        int boldH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD);
        int plainH = DrawUtils.getFontHeight(DrawUtils.UI_FONT_PLAIN);

        // Left: 2-line antique scroll plaque (Player name + Level on line 1, live HP + Attack on line 2)
        boolean pTurn = (this.activeTurnIndex == 0 && !this.hideTurnBorder);
        boolean pHover = HdInputState.isHovered(ox + 2, oy, 96, 30);
        DrawUtils.drawAntiquePanel(
                g, ox + 2, oy, 96, 30,
                pTurn ? 0x1D4A42 : (pHover ? 0x422D1D : 0x322216),
                pTurn ? 0x12332D : (pHover ? 0x2B1C12 : 0x22160E),
                0x130B06,
                pHover ? 0xFFD700 : (pTurn ? 0x33D6C4 : 0x8F6932),
                pTurn ? 0x8CF5E8 : 0xCCA052,
                pTurn ? 0xFFF2A6 : 0xE6BD6A);
        int marqueeTick = GameConfig.highFpsEnabled ? GameConfig.visualFrameCounter : (this.headerAnimTick << 1);
        DrawUtils.drawFullNameInBox(
                g, this.fighterLabels[0],
                ox + 5, oy + 3, 90, boldH,
                false, pTurn ? 0xFFF8DF : 0xF0DFC2, 0x120804, marqueeTick);
        String pSub = "Máu " + this.fighters[0].r() + " | Công " + this.fighters[0].m();
        if (DrawUtils.getStringWidth(DrawUtils.UI_FONT_PLAIN, pSub) > 90) {
            pSub = "Máu " + this.fighters[0].r() + "|Công " + this.fighters[0].m();
        }
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_PLAIN, pSub,
                ox + 5, oy + 16, Graphics.TOP | Graphics.LEFT,
                pTurn ? 0x8CF5E8 : 0xD4C0A6, 0x120804);

        // Right: 2-line antique scroll plaque (Enemy name + Level on line 1, live HP + Boss Tag / Telegraph on line 2)
        boolean eTurn = (this.activeTurnIndex == 1 && !this.hideTurnBorder);
        boolean eHover = HdInputState.isHovered(ox + 142, oy, 96, 30);
        boolean imminent = this.fighters[1].isBossCycleImminent();
        boolean pulseWarn = imminent && ((this.headerAnimTick & 4) == 0);
        int eBorderCol = eHover ? 0xFFD700 : (imminent
                ? (pulseWarn ? 0xFFD700 : 0xFF8C33)
                : (eTurn ? 0xE0467C : 0x8F6932));
        int eInnerCol = imminent
                ? (pulseWarn ? 0xFFF59E : 0xFFB85C)
                : (eTurn ? 0xFF8CB6 : 0xCCA052);
        DrawUtils.drawAntiquePanel(
                g, ox + 142, oy, 96, 30,
                eTurn ? 0x5C1930 : (imminent ? 0x4A2210 : (eHover ? 0x422D1D : 0x322216)),
                eTurn ? 0x3D1020 : (imminent ? 0x2E1308 : (eHover ? 0x2B1C12 : 0x22160E)),
                0x130B06,
                eBorderCol,
                eInnerCol,
                (eTurn || imminent) ? 0xFFF2A6 : 0xE6BD6A);
        DrawUtils.drawFullNameInBox(
                g, this.fighterLabels[1],
                ox + 145, oy + 3, 90, boldH,
                true, eTurn ? 0xFFF8DF : 0xF0DFC2, 0x120804, marqueeTick);
        String bossTag = this.fighters[1].getBossShortHudTag();
        String eSub;
        int eSubCol;
        if (imminent) {
            eSub = "Máu " + this.fighters[1].r() + " [SẮP NỔ!]";
            eSubCol = pulseWarn ? 0xFFF280 : 0xFFB84D;
        } else if (bossTag != null) {
            eSub = "Máu " + this.fighters[1].r() + " [" + bossTag + "]";
            eSubCol = eTurn ? 0xFFB0CC : 0xF2D49B;
        } else {
            eSub = "Máu " + this.fighters[1].r() + " | Công " + this.fighters[1].m();
            if (DrawUtils.getStringWidth(DrawUtils.UI_FONT_PLAIN, eSub) > 90) {
                eSub = "Máu " + this.fighters[1].r() + "|Công " + this.fighters[1].m();
            }
            eSubCol = eTurn ? 0xFFB0CC : 0xD4C0A6;
        }
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_PLAIN, eSub,
                ox + 235, oy + 16, Graphics.TOP | Graphics.RIGHT,
                eSubCol, 0x120804);

        // Center-Top: Compact Turn Medallion ("BẠN" / "ĐỊCH")
        int cx = ox + 100;
        DrawUtils.drawAntiquePanel(
                g, cx, oy, 40, 15,
                this.activeTurnIndex == 0 ? 0x176359 : 0x7A163B,
                this.activeTurnIndex == 0 ? 0x0E423B : 0x540D28,
                0x140A08,
                this.activeTurnIndex == 0 ? 0x3EE6D2 : 0xF05490,
                this.activeTurnIndex == 0 ? 0x99FFF2 : 0xFFA3C6,
                0xFFE680);
        int badgeTextY = oy + ((15 - boldH) >> 1);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_BOLD,
                this.activeTurnIndex == 0 ? "BẠN" : "ĐỊCH",
                cx + 20, badgeTextY, Graphics.TOP | Graphics.HCENTER,
                0xFFF8E0, 0x120804);

        // Center-Bottom: Compact 1x / 2x Speed Pill tucked neatly above board arch
        int speedX = ox + 102;
        int speedY = oy + 16;
        boolean fast = GameConfig.battleFastMode;
        boolean speedHover = HdInputState.isHovered(speedX, speedY, 36, 14);
        DrawUtils.drawAntiquePanel(
                g, speedX, speedY, 36, 14,
                fast ? 0x9E6408 : (speedHover ? 0x4A3120 : 0x342317),
                fast ? 0x6E4204 : (speedHover ? 0x301F14 : 0x22160E),
                0x140C07,
                (fast || speedHover) ? 0xFFD700 : 0x946D35,
                fast ? 0xFFF099 : 0xCCA052,
                fast ? 0xFFFFFF : 0xE6BD6A);
        int speedTextY = speedY + ((14 - plainH) >> 1);
        DrawUtils.drawShadowText(
                g, DrawUtils.UI_FONT_PLAIN,
                fast ? "2x" : "1x",
                speedX + 18, speedTextY, Graphics.TOP | Graphics.HCENTER,
                fast ? 0xFFF5B8 : 0xF0DFC2, 0x120804);
    }

    private static int clampBarWidth(int width, int maxWidth) {
        if (width < 0) {
            return 0;
        }
        return width > maxWidth ? maxWidth : width;
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        this.drawTopHeader(g);

        boolean hd = HdConfig.isHdRemaster() && g.getHdCanvas() != null;
        if (hd) {
            Image pPort = (this.fighterPortraits != null && this.fighterPortraits.length > 0) ? this.fighterPortraits[0] : null;
            Image ePort = (this.fighterPortraits != null && this.fighterPortraits.length > 1) ? this.fighterPortraits[1] : null;
            HdBattleStageRenderer.drawCombatantsLowerHud(
                    g.getHdCanvas(),
                    this.fighters[0], pPort, this.trailHp[0],
                    this.fighters[1], ePort, this.trailHp[1],
                    this.activeTurnIndex, this.hideTurnBorder, GameConfig.visualFrameCounter);
            HdBattleStageRenderer.drawBoardTurnAura(
                    g.getHdCanvas(), this.activeTurnIndex, this.hideTurnBorder, GameConfig.visualFrameCounter);
        } else {
        // Player (left) HP, food, and mana bars.
        BattleFighter player = this.fighters[0];
        int barX = this.avatarAnchors[0].i() + this.avatarAnchors[0].k() + 6;
        int barY = this.avatarAnchors[0].j();
        int curHp = player.l();
        int hpBarW = this.hpBarImage.getWidth();
        int hpBarH = this.hpBarImage.getHeight();
        int overBarMaxW = this.overHpBarImage.getWidth();
        int foodBarMaxW = this.foodBarImage.getWidth();
        int foodBarH = this.foodBarImage.getHeight();
        int manaBarW = this.manaBarImage.getWidth();
        int manaBarH = this.manaBarImage.getHeight();
        if (this.trailHp[0] > curHp && player.e() > 0) {
            int trailClamped = this.trailHp[0] > player.e() ? player.e() : this.trailHp[0];
            int trailW = clampBarWidth(hpBarW * trailClamped / player.e(), hpBarW);
            if (trailW > 0) {
                g.setColor(0xFFE066);
                g.fillRect(barX, barY, trailW, hpBarH);
            }
        }
        if (player.e() > 0 && (curHp > player.j() || this.lowHpBlinkTimers[0] % 2 == 0)) {
            if (curHp > player.e()) {
                DrawUtils.drawRegion(g, this.hpBarImage, 0, 0, hpBarW, hpBarH, barX, barY, 0);
                int overHp = curHp - player.e();
                int overW = clampBarWidth(hpBarW * overHp / player.e(), overBarMaxW);
                if (overW > 0) {
                    DrawUtils.drawRegion(g, this.overHpBarImage, 0, 0, overW, this.overHpBarImage.getHeight(), barX, barY, 0);
                }
            } else {
                int fillW = clampBarWidth(hpBarW * curHp / player.e(), hpBarW);
                if (player.l() > 0 && fillW < 1) {
                    fillW = 1;
                }
                if (fillW > 0) {
                    DrawUtils.drawRegion(g, this.hpBarImage, 0, 0, fillW, hpBarH, barX, barY, 0);
                }
            }
        }
        if (player.getBarrierHp() > 0 && player.getMaxBarrierHp() > 0) {
            int barrierW = clampBarWidth(hpBarW * player.getBarrierHp() / player.getMaxBarrierHp(), hpBarW);
            if (barrierW < 2) {
                barrierW = 2;
            }
            g.setColor(0x1EC8F5);
            g.fillRect(barX, barY + hpBarH - 2, barrierW, 2);
            g.setColor(0xB8FFFF);
            g.drawLine(barX, barY + hpBarH - 2, barX + barrierW - 1, barY + hpBarH - 2);
        }

        barY += 11;
        int playerFoodW = player.q() > 0 ? clampBarWidth(foodBarMaxW * player.p() / player.q(), foodBarMaxW) : 0;
        if (playerFoodW > 0) {
            DrawUtils.drawRegion(g, this.foodBarImage, 0, 0, playerFoodW, foodBarH, barX, barY, 0);
        }

        barY += 11;
        int playerManaW = player.n() > 0 ? clampBarWidth(manaBarW * player.o() / player.n(), manaBarW) : 0;
        if (playerManaW > 0) {
            DrawUtils.drawRegion(g, this.manaBarImage, 0, 0, playerManaW, manaBarH, barX, barY, 0);
            if (player.o() >= 12 && playerManaW >= 6) {
                int sheenPos = (GameConfig.visualFrameCounter % (playerManaW + 14)) - 7;
                if (sheenPos >= 0 && sheenPos < playerManaW - 2) {
                    g.setColor(0xB8FFFF);
                    g.fillRect(barX + sheenPos, barY + 1, 3, manaBarH - 2);
                }
            }
        }
        // 3-tier Mana notches (no blinking effect)
        g.setColor(0x140E20);
        g.drawLine(barX + manaBarW / 3, barY, barX + manaBarW / 3, barY + manaBarH - 1);
        g.drawLine(barX + (manaBarW * 2) / 3, barY, barX + (manaBarW * 2) / 3, barY + manaBarH - 1);

        // Enemy (right, mirrored) HP, food, and mana bars.
        BattleFighter enemy = this.fighters[1];
        barX = this.avatarAnchors[1].i() - 5;
        barY = this.avatarAnchors[0].j();
        int enemyHp = enemy.l();
        if (this.trailHp[1] > enemyHp && enemy.e() > 0) {
            int trailClamped = this.trailHp[1] > enemy.e() ? enemy.e() : this.trailHp[1];
            int trailW = clampBarWidth(hpBarW * trailClamped / enemy.e(), hpBarW);
            if (trailW > 0) {
                g.setColor(0xFFE066);
                g.fillRect(barX - trailW, barY, trailW, hpBarH);
            }
        }
        if (enemy.e() > 0 && (enemyHp > enemy.j() || this.lowHpBlinkTimers[1] % 2 == 0)) {
            if (enemyHp > enemy.e()) {
                g.drawRegion(this.hpBarImage, 0, 0, hpBarW, hpBarH, Sprite.TRANS_MIRROR, barX - hpBarW, barY, 0);
                int overHp = enemyHp - enemy.e();
                int overW = clampBarWidth(hpBarW * overHp / enemy.e(), overBarMaxW);
                if (overW > 0) {
                    g.drawRegion(this.overHpBarImage, 0, 0, overW, this.overHpBarImage.getHeight(), Sprite.TRANS_MIRROR, barX - overW, barY, 0);
                }
            } else {
                int fillW = clampBarWidth(hpBarW * enemyHp / enemy.e(), hpBarW);
                if (enemy.l() > 0 && fillW < 1) {
                    fillW = 1;
                }
                if (fillW > 0) {
                    g.drawRegion(this.hpBarImage, 0, 0, fillW, hpBarH, Sprite.TRANS_MIRROR, barX - fillW, barY, 0);
                }
            }
        }
        if (enemy.getBarrierHp() > 0 && enemy.getMaxBarrierHp() > 0) {
            int barrierW = clampBarWidth(hpBarW * enemy.getBarrierHp() / enemy.getMaxBarrierHp(), hpBarW);
            if (barrierW < 2) {
                barrierW = 2;
            }
            g.setColor(0x1EC8F5);
            g.fillRect(barX - barrierW, barY + hpBarH - 2, barrierW, 2);
            g.setColor(0xB8FFFF);
            g.drawLine(barX - barrierW, barY + hpBarH - 2, barX - 1, barY + hpBarH - 2);
        }

        barY += 11;
        int enemyFoodW = enemy.q() > 0 ? clampBarWidth(foodBarMaxW * enemy.p() / enemy.q(), foodBarMaxW) : 0;
        if (enemyFoodW > 0) {
            g.drawRegion(this.foodBarImage, 0, 0, enemyFoodW, foodBarH, Sprite.TRANS_MIRROR, barX - enemyFoodW, barY, 0);
        }

        barY += 11;
        int enemyManaW = enemy.n() > 0 ? clampBarWidth(manaBarW * enemy.o() / enemy.n(), manaBarW) : 0;
        if (enemyManaW > 0) {
            g.drawRegion(this.manaBarImage, 0, 0, enemyManaW, manaBarH, Sprite.TRANS_MIRROR, barX - enemyManaW, barY, 0);
            if (enemy.o() >= 12 && enemyManaW >= 6) {
                int sheenPos = (GameConfig.visualFrameCounter % (enemyManaW + 14)) - 7;
                if (sheenPos >= 0 && sheenPos < enemyManaW - 2) {
                    g.setColor(0xFFD6F5);
                    g.fillRect(barX - sheenPos - 3, barY + 1, 3, manaBarH - 2);
                }
            }
        }
        g.setColor(0x140E20);
        g.drawLine(barX - manaBarW / 3, barY, barX - manaBarW / 3, barY + manaBarH - 1);
        g.drawLine(barX - (manaBarW * 2) / 3, barY, barX - (manaBarW * 2) / 3, barY + manaBarH - 1);
        }

        // Portrait hit flash + sword slash overlay + 60 FPS expanding shockwave ring.
        if (this.hitFlashTimer > 0 && this.hitFlashTimer <= 10) {
            DummyDrawable targetAnchor = this.avatarAnchors[this.hitTargetIndex];
            int centerHitX = targetAnchor.i() + (targetAnchor.k() >> 1);
            int centerHitY = targetAnchor.j() + (targetAnchor.l() >> 1);
            int progress256 = ((10 - this.hitFlashTimer) << 8) + (GameConfig.highFpsEnabled ? GameConfig.renderSubTick256 : 0);
            int ringRadius = 6 + ((progress256 * 20) / 2560);
            g.setColor(this.hitFlashTimer > 5 ? 0xFFFFFF : 0xFFE066);
            g.drawArc(centerHitX - ringRadius, centerHitY - ringRadius, ringRadius << 1, ringRadius << 1, 0, 360);
            if (ringRadius > 9) {
                int innerR = ringRadius - 3;
                g.setColor(0xFF9933);
                g.drawArc(centerHitX - innerR, centerHitY - innerR, innerR << 1, innerR << 1, 0, 360);
            }
            if (this.hitFlashTimer > 6) {
                if (this.hitFlashTimer % 2 == 0) {
                    g.setColor(0xFFFFFF);
                    g.fillRect(targetAnchor.i(), targetAnchor.j(), targetAnchor.k(), targetAnchor.l());
                }
            } else {
                int slashW = this.slashSheet.getWidth() / 5;
                int slashH = this.slashSheet.getHeight();
                int frame = (6 - this.hitFlashTimer) % 5;
                int transform = this.hitTargetIndex == 0 ? Sprite.TRANS_NONE : Sprite.TRANS_MIRROR;
                g.drawRegion(
                        this.slashSheet,
                        frame * slashW, 0, slashW, slashH,
                        transform,
                        targetAnchor.i() + targetAnchor.k() / 2,
                        targetAnchor.j() + targetAnchor.l() / 2 - 7,
                        Graphics.HCENTER | Graphics.VCENTER);
            }
        }

        // Active turn glowing HUD and board borders (legacy mode only; HD mode uses HdBattleStageRenderer).
        if (!hd && !this.hideTurnBorder) {
            int borderX = this.avatarAnchors[this.activeTurnIndex].i() - 2;
            int borderY = this.avatarAnchors[this.activeTurnIndex].j() - 2;
            int[] palette = PLAYER_TURN_COLORS;
            if (this.activeTurnIndex == 1) {
                borderX = borderX + this.avatarAnchors[this.activeTurnIndex].k() - 83 + 5;
                palette = ENEMY_TURN_COLORS;
            }

            int bw = 82;
            int bh = 34;
            for (int layer = 0; layer < palette.length; ++layer) {
                DrawUtils.drawRoundedBorder(g, palette[layer], borderX, borderY, bw, bh);
                --borderX;
                --borderY;
                bw += 2;
                bh += 2;
            }

            int boardLayers = 0;
            if (this.turnGlowTick < 10) {
                if (this.turnGlowTick % 2 == 0) {
                    boardLayers = 3;
                }
            } else if (this.turnGlowTick < 18) {
                boardLayers = (this.turnGlowTick - 3) % 4;
            } else {
                boardLayers = 3;
            }

            int bx = this.avatarAnchors[0].i() - 2;
            int by = this.avatarAnchors[0].j() - 186;
            int bSize = 172;
            for (int layer = 0; layer < boardLayers; ++layer) {
                g.setColor(palette[layer]);
                g.drawRect(bx, by, bSize, bSize);
                --bx;
                --by;
                bSize += 2;
            }
        }

        for (int i = 0; i < this.playerBuffAuras.length; ++i) {
            this.playerBuffAuras[i].a(g);
        }
        for (int i = this.manaSparks.length - 1; i > -1; --i) {
            this.manaSparks[i].a(g);
        }
        for (int i = this.hpSparks.length - 1; i > -1; --i) {
            this.hpSparks[i].a(g);
        }

        // Floating damage / heal / barrier / pierce numbers above avatars (clamped to screen bounds)
        for (int i = 0; i < this.popupTicks.length; ++i) {
            if (this.popupTicks[i] > 0 && this.popupTexts[i] != null) {
                int py = this.popupY[i] - ((GameConfig.highFpsEnabled && GameConfig.renderSubTick256 >= 128) ? 1 : 0);
                int tw = DrawUtils.getStringWidth(DrawUtils.UI_FONT_BOLD, this.popupTexts[i]) + 10;
                int th = DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD) + 4;
                int boxX = this.popupX[i] - (tw >> 1);
                int minX = GameConfig.contentOffsetX + 2;
                int maxX = GameConfig.contentOffsetX + 238 - tw;
                if (boxX < minX) {
                    boxX = minX;
                } else if (boxX > maxX) {
                    boxX = maxX;
                }
                int pType = this.popupTypes[i];
                int fillTop;
                int fillBot;
                int borderCol;
                int innerCol;
                int textCol;
                switch (pType) {
                    case POPUP_HEAL:
                        fillTop = 0x166B36;
                        fillBot = 0x0E4A24;
                        borderCol = 0x52FF94;
                        innerCol = 0xA6FFC7;
                        textCol = 0xB8FFCA;
                        break;
                    case POPUP_BARRIER_GAIN:
                        fillTop = 0x0E5678;
                        fillBot = 0x08354D;
                        borderCol = 0x5CE8FF;
                        innerCol = 0xB8F5FF;
                        textCol = 0xD9FBFF;
                        break;
                    case POPUP_BARRIER_HIT:
                        fillTop = 0x1D3B6B;
                        fillBot = 0x112445;
                        borderCol = 0x80D4FF;
                        innerCol = 0xC2EBFF;
                        textCol = 0xE0F4FF;
                        break;
                    case POPUP_PIERCE:
                        fillTop = 0x6E1258;
                        fillBot = 0x450936;
                        borderCol = 0xFF7AE6;
                        innerCol = 0xFFE699;
                        textCol = 0xFFE885;
                        break;
                    case POPUP_FOOD_BURN:
                        fillTop = 0x7D4206;
                        fillBot = 0x4F2702;
                        borderCol = 0xFFB84D;
                        innerCol = 0xFFE39E;
                        textCol = 0xFFF2C2;
                        break;
                    default:
                        fillTop = 0x82161E;
                        fillBot = 0x540B12;
                        borderCol = 0xFFD700;
                        innerCol = 0xFFF2A6;
                        textCol = 0xFFF4B8;
                        break;
                }
                DrawUtils.drawAntiquePanel(
                        g, boxX, py - 2, tw, th,
                        fillTop, fillBot, 0x100806,
                        borderCol, innerCol, 0xFFFFFF);
                DrawUtils.drawShadowText(
                        g, DrawUtils.UI_FONT_BOLD, this.popupTexts[i],
                        boxX + (tw >> 1), py, Graphics.TOP | Graphics.HCENTER,
                        textCol, 0x120804);
            }
        }

        // Mini status badges below portraits (Shield turns, Burn/Poison, Starvation, Enrage, Item buffs)
        this.drawFighterStatusRow(g, 0);
        this.drawFighterStatusRow(g, 1);

        // Active skill cast announcement banner at top of gem board
        this.drawSkillBanner(g);
    }

    public void b() {
        ++this.headerAnimTick;
        if (this.skillBannerTicks > 0) {
            --this.skillBannerTicks;
            if (this.skillBannerTicks == 0 && this.queuedBannerText != null) {
                this.skillBannerText = this.queuedBannerText;
                this.skillBannerPlayer = this.queuedBannerPlayer;
                this.skillBannerShatter = this.queuedBannerShatter;
                this.queuedBannerText = null;
                this.skillBannerTicks = 38;
            }
        }
        if (!this.hideTurnBorder) {
            ++this.blinkCycleTick;
            if (this.blinkCycleTick == 10) {
                this.blinkCycleTick = 0;
            }
            if (this.turnGlowTick < 20) {
                ++this.turnGlowTick;
            }
        }

        if (this.hitFlashTimer > 0) {
            --this.hitFlashTimer;
            if (this.hitFlashTimer == 7) {
                BattleResourceManager.a().f();
                if (this.hitTargetIndex == 0) {
                    GUIManager.vibrate(250);
                }
            } else if (this.hitFlashTimer == 0) {
                this.hitFlashTimer = this.queuedHitFlashTimer;
                this.queuedHitFlashTimer = 0;
            }
        }

        // Smoothly interpolate displayed HP bar values toward actual HP and sync impact popups.
        for (int i = 0; i < this.fighters.length; ++i) {
            boolean waitingForImpact = (this.hitTargetIndex == i) && (this.hpDrainDelay > 2 || this.hitFlashTimer > 10);

            int curBarrier = this.fighters[i].getBarrierHp();
            if (curBarrier != this.lastBarrierHp[i]) {
                int bDelta = curBarrier - this.lastBarrierHp[i];
                this.lastBarrierHp[i] = curBarrier;
                if (bDelta > 0) {
                    this.spawnPopup(i, "+" + bDelta + " Giáp", POPUP_BARRIER_GAIN);
                } else if (waitingForImpact) {
                    this.pendingBarrierDelta[i] += bDelta;
                } else {
                    this.spawnPopup(i, bDelta + " Giáp", POPUP_BARRIER_HIT);
                }
            }

            int actualHp = this.fighters[i].r();
            if (actualHp != this.lastActualHp[i]) {
                int hpDelta = actualHp - this.lastActualHp[i];
                this.lastActualHp[i] = actualHp;
                if (actualHp > this.trailHp[i]) {
                    this.trailHp[i] = actualHp;
                }
                if (hpDelta > 0) {
                    this.spawnPopup(i, "+" + hpDelta, POPUP_HEAL);
                } else if (waitingForImpact) {
                    this.pendingHpDelta[i] += hpDelta;
                } else if (this.pendingPierce[i]) {
                    this.pendingPierce[i] = false;
                    this.spawnPopup(i, hpDelta + " XUYÊN!", POPUP_PIERCE);
                } else {
                    this.spawnPopup(i, "" + hpDelta, POPUP_DAMAGE);
                }
            }

            if (!waitingForImpact) {
                if (this.pendingBarrierDelta[i] < 0) {
                    this.spawnPopup(i, this.pendingBarrierDelta[i] + " Giáp", POPUP_BARRIER_HIT);
                    this.pendingBarrierDelta[i] = 0;
                }
                if (this.pendingHpDelta[i] < 0) {
                    int dmgVal = -this.pendingHpDelta[i];
                    if (this.pendingPierce[i]) {
                        this.pendingPierce[i] = false;
                        this.spawnPopup(i, this.pendingHpDelta[i] + " XUYÊN!", POPUP_PIERCE);
                    } else {
                        this.spawnPopup(i, "" + this.pendingHpDelta[i], POPUP_DAMAGE);
                    }
                    if (this.fighters[i].s() > 0 && dmgVal >= (this.fighters[i].s() / 5)) {
                        GUIManager.shakeScreen(4, 2);
                    }
                    this.pendingHpDelta[i] = 0;
                } else if (this.pendingPierce[i]) {
                    this.pendingPierce[i] = false;
                }
                if (this.pendingFoodBurn[i] > 0) {
                    this.spawnPopup(i, "-" + this.pendingFoodBurn[i] + " Lương", POPUP_FOOD_BURN);
                    this.pendingFoodBurn[i] = 0;
                }
            }

            if (this.hitTargetIndex == i && this.hpDrainDelay > 0) {
                --this.hpDrainDelay;
            } else {
                int displayedHp = this.fighters[i].l();
                int delta = actualHp - displayedHp;
                if (delta != 0) {
                    if (Math.abs(delta) >= 6) {
                        delta /= 6;
                    }
                    this.fighters[i].d(displayedHp + delta);
                }
                if (this.trailHp[i] > actualHp) {
                    int trailStep = (this.trailHp[i] - actualHp) / 10;
                    if (trailStep < 1) {
                        trailStep = 1;
                    }
                    this.trailHp[i] -= trailStep;
                    if (this.trailHp[i] < actualHp) {
                        this.trailHp[i] = actualHp;
                    }
                }
            }
        }

        for (int i = 0; i < this.popupTicks.length; ++i) {
            if (this.popupTicks[i] > 0) {
                --this.popupTicks[i];
                --this.popupY[i];
            }
        }

        for (int i = 0; i < this.lowHpBlinkTimers.length; ++i) {
            if (this.fighters[i].l() <= this.fighters[i].j()) {
                this.lowHpBlinkTimers[i]++;
                if (this.lowHpBlinkTimers[i] > 1) {
                    this.lowHpBlinkTimers[i] = 0;
                }
            }
        }

        for (int i = this.hpSparks.length - 1; i > -1; --i) {
            this.hpSparks[i].e();
        }
        for (int i = this.manaSparks.length - 1; i > -1; --i) {
            this.manaSparks[i].e();
        }
        if (this.manaFlashTimer > 0) {
            --this.manaFlashTimer;
        }
        for (int i = 0; i < this.playerBuffAuras.length; ++i) {
            this.playerBuffAuras[i].e();
        }
    }

    public void a(int delayTicks) {
        if (this.hpDrainDelay == 0) {
            this.hpDrainDelay = delayTicks;
        } else if (this.hpDrainDelay < delayTicks) {
            this.hpDrainDelay = (this.hpDrainDelay + delayTicks) >> 1;
        }
    }

    public void a(int fighterIndex, int delayTicks) {
        BattleFighter fighter = this.fighters[this.hitTargetIndex];
        int sparkY = this.avatarAnchors[fighterIndex].j() + 10;
        if (fighterIndex == 0) {
            int sparkX = this.avatarAnchors[fighterIndex].i() - 3 + this.avatarAnchors[fighterIndex].k()
                    + this.manaBarImage.getWidth() * fighter.o() / fighter.n();
            for (int i = 0; i < this.manaSparks.length; ++i) {
                if (!this.manaSparks[i].m()) {
                    this.manaSparks[i].a(sparkX, sparkY, sparkX - 5 + RandomUtils.nextInt(80), sparkY - 80 + RandomUtils.nextInt(100), delayTicks);
                    this.manaSparks[i].b(RandomUtils.nextInt(10));
                }
            }
        } else {
            int sparkX = this.avatarAnchors[fighterIndex].i() - 5
                    - this.manaBarImage.getWidth() * fighter.o() / fighter.n();
            for (int i = 0; i < this.manaSparks.length; ++i) {
                if (!this.manaSparks[i].m()) {
                    this.manaSparks[i].a(sparkX, sparkY, sparkX + 5 - RandomUtils.nextInt(80), sparkY - 80 + RandomUtils.nextInt(100), delayTicks);
                    this.manaSparks[i].b(RandomUtils.nextInt(10));
                }
            }
        }
    }

    public void b(int fighterIndex, int delayTicks) {
        this.a(this.hpDrainDelay);
        BattleFighter fighter = this.fighters[fighterIndex];
        if (fighter.k()) {
            return;
        }
        if (this.hitFlashTimer < 1) {
            this.hitFlashTimer = delayTicks;
        }
        if (this.hitFlashTimer < delayTicks) {
            this.queuedHitFlashTimer = delayTicks - this.hitFlashTimer;
            if (this.queuedHitFlashTimer < 6) {
                this.queuedHitFlashTimer = 0;
            }
        } else {
            this.queuedHitFlashTimer = this.hitFlashTimer - delayTicks;
            this.hitFlashTimer = delayTicks;
        }

        int sparkDelay = delayTicks - 5;
        if (sparkDelay < 0) {
            sparkDelay = 0;
        }

        this.hitTargetIndex = fighterIndex;
        int sparkY = this.avatarAnchors[fighterIndex].j();
        int remaining = 15;
        if (fighterIndex == 0) {
            int sparkX = this.avatarAnchors[fighterIndex].i() - 3 + this.avatarAnchors[fighterIndex].k()
                    + this.hpBarImage.getWidth() * fighter.r() / fighter.s();
            for (int i = 0; i < this.hpSparks.length; ++i) {
                if (!this.hpSparks[i].m()) {
                    this.hpSparks[i].a(sparkX, sparkY, sparkX - 5 + RandomUtils.nextInt(80), sparkY - 80 + RandomUtils.nextInt(100), sparkDelay);
                    this.hpSparks[i].b(RandomUtils.nextInt(10));
                    if (--remaining <= 0) {
                        break;
                    }
                }
            }
        } else {
            int sparkX = this.avatarAnchors[fighterIndex].i() - 5
                    - this.hpBarImage.getWidth() * fighter.r() / fighter.s();
            for (int i = 0; i < this.hpSparks.length; ++i) {
                if (!this.hpSparks[i].m()) {
                    this.hpSparks[i].a(sparkX, sparkY, sparkX + 5 - RandomUtils.nextInt(80), sparkY - 80 + RandomUtils.nextInt(100), sparkDelay);
                    this.hpSparks[i].b(RandomUtils.nextInt(10));
                    if (--remaining <= 0) {
                        break;
                    }
                }
            }
        }
    }

    public void b(int activeTurnIndex) {
        if (this.activeTurnIndex != activeTurnIndex) {
            this.turnGlowTick = 0;
        }
        this.activeTurnIndex = activeTurnIndex;
    }

    public void d(boolean hidden) {
        this.hideTurnBorder = hidden;
    }

    public void c() {
        this.manaFlashTimer = 40;
    }
}

