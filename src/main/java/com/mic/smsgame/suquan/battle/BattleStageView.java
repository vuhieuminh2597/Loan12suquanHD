package com.mic.smsgame.suquan.battle;

import com.loan12suquan.hd.HdBattleStageRenderer;
import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdInputState;
import com.loan12suquan.hd.platform.HdCanvas2D;
import com.loan12suquan.hd.platform.HdSurface;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.BattleResourceManager;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.FontManager;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.engine.graphics.Rect;
import com.mg.engine.sprite.Drawable;
import com.mg.engine.sprite.DummyDrawable;
import com.mg.engine.sprite.FrameAnimator;
import com.mg.engine.ui.BlinkingText;
import com.mg.engine.ui.MenuSelectionListener;
import com.mg.engine.util.ObjectList;
import com.mg.engine.util.RandomUtils;
import com.mic.smsgame.suquan.data.CampaignData;
import com.mic.smsgame.suquan.gfx.BannerPopupEffect;
import com.mic.smsgame.suquan.gfx.BattleHudView;
import com.mic.smsgame.suquan.gfx.BattleTimerWidget;
import com.mic.smsgame.suquan.gfx.BoardCursorSprite;
import com.mic.smsgame.suquan.gfx.DirectionHintArrows;
import com.mic.smsgame.suquan.gfx.FloatingIconEffect;
import com.mic.smsgame.suquan.gfx.GemBoardRenderer;
import com.mic.smsgame.suquan.gfx.GemTileSprite;
import com.mic.smsgame.suquan.model.BattleFighter;
import com.mic.smsgame.suquan.model.GemMatch;
import com.mic.smsgame.suquan.model.SwapMove;
import com.mic.smsgame.suquan.ui.BattleTutorialOverlay;
import com.mic.smsgame.suquan.ui.ItemSelectDialog;
import com.mic.smsgame.suquan.ui.SkillSelectDialog;
import com.mic.smsgame.suquan.vfx.ChainLightningVfx;
import com.mic.smsgame.suquan.vfx.ComboBurstEffect;
import com.mic.smsgame.suquan.vfx.FireProjectileVfx;
import com.mic.smsgame.suquan.vfx.FlyingSwordEffect;
import com.mic.smsgame.suquan.vfx.FrozenVfx;
import com.mic.smsgame.suquan.vfx.HealingVfx;
import com.mic.smsgame.suquan.vfx.IceBoltVfx;
import com.mic.smsgame.suquan.vfx.LightningShieldVfx;
import com.mic.smsgame.suquan.vfx.LightningStrikeVfx;
import com.mic.smsgame.suquan.vfx.SparkStreamEffect;
import com.mic.smsgame.suquan.vfx.SpellEffect;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Battle scene view coordinating the match-3 board renderer, selection cursor, HUD bars,
 * particle/spell VFX pools, timer, banner announcements, softkey labels, and battle dialogs.
 */
public class BattleStageView implements BattleConstants {
    BattleModel a;
    GemBoardRenderer b;
    BoardCursorSprite c;
    private Image starIcon;
    private ObjectList comboBurstPool;
    private Image backgroundCanvas;
    private Image[] crystalSheets;
    private ObjectList flyingSwordPool;
    private ObjectList spellEffectPool;
    private DirectionHintArrows directionHintArrows;
    public BattleHudView d;
    private Rect savedClip;
    private ObjectList sparkStreamPool;
    private FloatingIconEffect floatingIconEffect;
    private BannerPopupEffect bannerPopup;
    private String[] softKeyLabels = new String[3];
    private int softKeyDisplayTicks;
    private int softKeyOffsetY;
    private BlinkingText blinkingAlertText;
    private SkillSelectDialog skillSelectDialog;
    public ItemSelectDialog e;
    private BattleTimerWidget turnTimerWidget;
    private boolean stageVisible = true;
    private boolean playerTurnInteractive = false;
    public Rect f;
    public BattleTutorialOverlay g;
    private Image[] fighterPortraits;

    public void setPlayerTurnInteractive(boolean interactive) {
        this.playerTurnInteractive = interactive;
    }

    public Image getFighterPortrait(int fighterIndex) {
        if (this.fighterPortraits != null && fighterIndex >= 0 && fighterIndex < this.fighterPortraits.length) {
            return this.fighterPortraits[fighterIndex];
        }
        return null;
    }

    public void a(String str1, int num2) {
        this.softKeyLabels[num2] = str1;
    }

    public void a() {
        this.softKeyDisplayTicks = 0;
    }

    public void b() {
        this.softKeyOffsetY = 15;
        this.softKeyDisplayTicks = 30 + this.softKeyOffsetY;
        if (GameConfig.hasPointerEvents) {
            this.softKeyDisplayTicks = -1;
        }

    }

    public void c() {
        this.softKeyLabels = new String[3];
    }

    public BattleStageView(BattleModel val1) {
        try {
            this.a = val1;
            this.p();
            this.savedClip = new Rect();
        } catch (Exception err3) {
            ((Throwable)err3).printStackTrace();
        }

    }

    private void p() {
        this.comboBurstPool = new ObjectList();
        this.flyingSwordPool = new ObjectList();
        this.spellEffectPool = new ObjectList();
        this.sparkStreamPool = new ObjectList();
        this.bannerPopup = new BannerPopupEffect(GameConfig.screenWidth / 2, GameConfig.screenHeight / 2 - 15);
        this.blinkingAlertText = new BlinkingText();
        this.blinkingAlertText.a(30);
        this.backgroundCanvas = BattleResourceManager.a().b;
        this.crystalSheets = BattleResourceManager.a().c;
        this.b = new GemBoardRenderer(GameConfig.contentOffsetX + 36 + 10, GameConfig.contentOffsetY + 54 + 10, this.a.isBossBoard());
        this.c = new BoardCursorSprite();
        GemTileSprite tile1 = this.b.a(0, 0);
        this.c.a(((Drawable)tile1).i(), ((Drawable)tile1).j());
        BattleFighter[] fighter2 = this.a.d();
        this.d = new BattleHudView(fighter2);
        this.d.d(this.b.i() - 18, this.b.j() + 167);
        DummyDrawable[] val3 = this.d.a();
        Image[] img4 = BattleResourceManager.a().a(fighter2[0].b(), fighter2[1].b());
        this.fighterPortraits = img4;
        this.d.setFighterPortraits(img4);
        Graphics gfx5 = this.backgroundCanvas.getGraphics();

        for(int num6 = 0; num6 < img4.length; ++num6) {
            gfx5.drawImage(img4[num6], val3[num6].i(), val3[num6].j(), 0);
        }
        this.starIcon = BattleResourceManager.a().a;
        this.directionHintArrows = new DirectionHintArrows();
        BattleResourceManager.a().a(fighter2[0].d());
        BattleResourceManager.a().a(fighter2[1].d());
        BattleResourceManager.a().c(fighter2[0].d());
        this.skillSelectDialog = new SkillSelectDialog(this.a.b(0).d());
        this.e = new ItemSelectDialog();
        this.turnTimerWidget = new BattleTimerWidget(GameConfig.screenWidth >> 1, this.b.j() + 163);
        this.turnTimerWidget.b(CampaignData.g);
        this.floatingIconEffect = new FloatingIconEffect(BattleResourceManager.a().E);
        this.f = new Rect(this.b.i() - 10, this.b.j() - 10, this.b.k(), this.b.l());
        if (this.a.d) {
            this.g = new BattleTutorialOverlay();
        }

    }

    public void d() {
        if (this.stageVisible) {
            this.b.a();
            this.c.a();

            for(int num1 = 0; num1 < this.comboBurstPool.b(); ++num1) {
                ((ComboBurstEffect)this.comboBurstPool.b(num1)).b();
            }

            for(int num2 = 0; num2 < this.flyingSwordPool.b(); ++num2) {
                ((FlyingSwordEffect)this.flyingSwordPool.b(num2)).a();
            }

            for(int num3 = 0; num3 < this.spellEffectPool.b(); ++num3) {
                ((SpellEffect)this.spellEffectPool.b(num3)).e();
            }

            for(int num4 = this.sparkStreamPool.b() - 1; num4 >= 0; --num4) {
                ((SparkStreamEffect)this.sparkStreamPool.b(num4)).a();
            }

            this.directionHintArrows.b();
            this.d.b();
            this.bannerPopup.e();
            if (this.softKeyDisplayTicks > 0) {
                --this.softKeyDisplayTicks;
                if (this.softKeyDisplayTicks < 15) {
                    --this.softKeyOffsetY;
                }
            }

            this.blinkingAlertText.a();
            this.turnTimerWidget.b();
            this.floatingIconEffect.a();
            if (this.g != null) {
                this.g.h();
            }

        }
    }

    public void a(Graphics gfx1) {
        boolean hd = HdConfig.isHdRemaster() && gfx1.getHdCanvas() != null;
        if (hd) {
            HdCanvas2D g2 = gfx1.getHdCanvas();
            HdSurface stageSurf = HdBattleStageRenderer.getOrCreateStageBackdrop(this.a != null && this.a.isBossBoard());
            g2.drawSurface(
                    stageSurf,
                    GameConfig.contentOffsetX * HdConfig.HD_SCALE,
                    GameConfig.contentOffsetY * HdConfig.HD_SCALE,
                    stageSurf.getWidth(),
                    stageSurf.getHeight());
            if (this.stageVisible && this.playerTurnInteractive) {
                int boardLeft = GameConfig.contentOffsetX + 36;
                int boardTop = GameConfig.contentOffsetY + 54;
                int hx = HdInputState.hoverX;
                int hy = HdInputState.hoverY;
                if (hx >= boardLeft && hx < boardLeft + 168 && hy >= boardTop && hy < boardTop + 168) {
                    int col = (hx - boardLeft) / 21;
                    int row = (hy - boardTop) / 21;
                    int s = HdConfig.HD_SCALE;
                    int cx = (boardLeft + col * 21) * s;
                    int cy = (boardTop + row * 21) * s;
                    int cw = 21 * s;
                    g2.setColorRgba(255, 232, 130, 42);
                    g2.fillRoundRect(cx + 3, cy + 3, cw - 6, cw - 6, 10);
                    g2.setColorRgba(255, 225, 110, 155);
                    g2.setStrokeWidth(2.0f);
                    g2.drawRoundRect(cx + 3, cy + 3, cw - 6, cw - 6, 10);
                }
            }
        } else {
            gfx1.drawImage(this.backgroundCanvas, 0, 0, 0);
        }
        if (this.stageVisible) {
            DrawUtils.saveClip(gfx1, this.savedClip);
            DrawUtils.intersectClip(gfx1, this.savedClip, GameConfig.contentOffsetX + 30, GameConfig.contentOffsetY + 51, 180, GameConfig.screenHeight);
            this.b.a(gfx1);
            DrawUtils.restoreClip(gfx1, this.savedClip);
            this.c.a(gfx1);
            this.directionHintArrows.a(gfx1);
        }

        this.d.a(gfx1);
        if (this.stageVisible) {
            this.blinkingAlertText.a(gfx1, 0, 0);

            for(int num2 = 0; num2 < this.comboBurstPool.b(); ++num2) {
                ((ComboBurstEffect)this.comboBurstPool.b(num2)).a(gfx1, 0, 0);
            }

            for(int num3 = this.sparkStreamPool.b() - 1; num3 >= 0; --num3) {
                ((SparkStreamEffect)this.sparkStreamPool.b(num3)).a(gfx1);
            }

            for(int num4 = 0; num4 < this.flyingSwordPool.b(); ++num4) {
                ((FlyingSwordEffect)this.flyingSwordPool.b(num4)).a(gfx1, 0, 0);
            }

            for(int num5 = 0; num5 < this.spellEffectPool.b(); ++num5) {
                ((SpellEffect)this.spellEffectPool.b(num5)).a(gfx1);
            }

            this.bannerPopup.a(gfx1);
            this.turnTimerWidget.a(gfx1);
            this.floatingIconEffect.a(gfx1);
            if (this.g != null) {
                this.g.a(gfx1);
            }

            if (!this.a.d || this.softKeyDisplayTicks != 0) {
                this.drawActionDock(gfx1);
            }

        }
    }

    private void drawActionDock(Graphics g) {
        int dockH = 24;
        int dockY = GameConfig.screenHeight - dockH - 1;
        int thirdW = GameConfig.screenWidth / 3;
        int textY = dockY + ((dockH - DrawUtils.getFontHeight(DrawUtils.UI_FONT_BOLD)) >> 1);
        boolean persistentDock = !this.a.d;
        boolean interactive = this.a.d || this.playerTurnInteractive;

        // Left button: T.Chiêu (shows live Mana progress X/12 when < 12, or [1..3] when ready)
        if (persistentDock || this.softKeyLabels[0] != null) {
            BattleFighter player = this.a.b(0);
            boolean hasSkills = (player != null && player.d().length > 0 && player.n() > 0);
            int readyTiers = hasSkills ? (player.o() * 3 / player.n()) : 0;
            if (!this.a.d && player != null && player.isStarving() && readyTiers > 2) {
                readyTiers = 2;
            }
            String label;
            if (readyTiers > 0) {
                label = "T.Chiêu [" + readyTiers + "]";
            } else if (persistentDock && hasSkills) {
                label = "Chiêu " + player.o() + "/12";
            } else {
                label = this.softKeyLabels[0] != null ? this.softKeyLabels[0] : "T.Chiêu";
            }
            int bx = 3;
            int bw = thirdW - 5;
            boolean hov0 = interactive && HdInputState.isHovered(bx, dockY, bw, dockH);
            if (readyTiers > 0 && interactive) {
                DrawUtils.drawAntiquePanel(
                        g, bx, dockY, bw, dockH,
                        hov0 ? 0x2F8A77 : 0x246B5C, hov0 ? 0x1D5C4F : 0x16473D, 0x0E2B25,
                        0xFFD700, 0xFFF2A6, 0xFFFFFF);
                DrawUtils.drawShadowText(
                        g, DrawUtils.UI_FONT_BOLD, label,
                        bx + (bw >> 1), textY, Graphics.TOP | Graphics.HCENTER,
                        0xFFF5B8, 0x0E241E);
            } else if (readyTiers > 0) {
                DrawUtils.drawAntiquePanel(
                        g, bx, dockY, bw, dockH,
                        0x183D35, 0x102923, 0x0A1A16,
                        0x8A7A3A, 0xB8A65C, 0xD6C885);
                DrawUtils.drawShadowText(
                        g, DrawUtils.UI_FONT_BOLD, label,
                        bx + (bw >> 1), textY, Graphics.TOP | Graphics.HCENTER,
                        0xB8C7BE, 0x0E1A16);
            } else if (interactive) {
                DrawUtils.drawAntiquePanel(
                        g, bx, dockY, bw, dockH,
                        hov0 ? 0x4A311F : 0x362417, hov0 ? 0x301F13 : 0x24170E, 0x140C07,
                        hov0 ? 0xFFD700 : 0x8F6932, 0xCCA052, 0xE6BD6A);
                DrawUtils.drawShadowText(
                        g, DrawUtils.UI_FONT_BOLD, label,
                        bx + (bw >> 1), textY, Graphics.TOP | Graphics.HCENTER,
                        hov0 ? 0xFFF5B8 : 0xF2E2C4, 0x120804);
            } else {
                DrawUtils.drawAntiquePanel(
                        g, bx, dockY, bw, dockH,
                        0x241810, 0x18100A, 0x100A06,
                        0x5E4624, 0x826538, 0x9E7E4B);
                DrawUtils.drawShadowText(
                        g, DrawUtils.UI_FONT_BOLD, label,
                        bx + (bw >> 1), textY, Graphics.TOP | Graphics.HCENTER,
                        0x9E8E7A, 0x100804);
            }
        }

        // Center button: Túi (beveled mahogany-bronze with live item count)
        if (persistentDock || this.softKeyLabels[1] != null) {
            int itemCount = 0;
            if (this.a.a != null) {
                for (int i = 0; i < this.a.a.length; ++i) {
                    if (this.a.a[i].a != 2 && this.a.a[i].b > 0) {
                        itemCount += this.a.a[i].b;
                    }
                }
            }
            String baseBag = this.softKeyLabels[1] != null ? this.softKeyLabels[1] : "Túi";
            String label = itemCount > 0 ? ("Túi (" + itemCount + ")") : baseBag;
            int bx = thirdW + 2;
            int bw = thirdW - 4;
            boolean hov1 = interactive && HdInputState.isHovered(bx, dockY, bw, dockH);
            if (interactive) {
                DrawUtils.drawAntiquePanel(
                        g, bx, dockY, bw, dockH,
                        hov1 ? 0x4F3521 : 0x3B2718, hov1 ? 0x362315 : 0x281A10, 0x160D08,
                        hov1 ? 0xFFD700 : (itemCount > 0 ? 0xB3833E : 0x7A592A),
                        itemCount > 0 ? 0xE6BA68 : 0xA88044,
                        0xF2CC79);
                DrawUtils.drawShadowText(
                        g, DrawUtils.UI_FONT_BOLD, label,
                        bx + (bw >> 1), textY, Graphics.TOP | Graphics.HCENTER,
                        hov1 ? 0xFFF5B8 : 0xF2E2C4, 0x120804);
            } else {
                DrawUtils.drawAntiquePanel(
                        g, bx, dockY, bw, dockH,
                        0x241810, 0x18100A, 0x100A06,
                        0x5E4624, 0x826538, 0x9E7E4B);
                DrawUtils.drawShadowText(
                        g, DrawUtils.UI_FONT_BOLD, label,
                        bx + (bw >> 1), textY, Graphics.TOP | Graphics.HCENTER,
                        0x9E8E7A, 0x100804);
            }
        }

        // Right button: T.Dừng (beveled crimson-lacquer antique wood)
        if (persistentDock || this.softKeyLabels[2] != null) {
            String pauseLabel = this.softKeyLabels[2] != null ? this.softKeyLabels[2] : "T.Dừng";
            int bx = thirdW * 2 + 2;
            int bw = GameConfig.screenWidth - bx - 3;
            boolean hov2 = interactive && HdInputState.isHovered(bx, dockY, bw, dockH);
            if (interactive) {
                DrawUtils.drawAntiquePanel(
                        g, bx, dockY, bw, dockH,
                        hov2 ? 0x6E2A32 : 0x542026, hov2 ? 0x4A1B20 : 0x381418, 0x1F0A0C,
                        hov2 ? 0xFFD700 : 0xB86B4B, 0xE69E7E, 0xF5C7A9);
                DrawUtils.drawShadowText(
                        g, DrawUtils.UI_FONT_BOLD, pauseLabel,
                        bx + (bw >> 1), textY, Graphics.TOP | Graphics.HCENTER,
                        hov2 ? 0xFFF5B8 : 0xFFE4D6, 0x1A0808);
            } else {
                DrawUtils.drawAntiquePanel(
                        g, bx, dockY, bw, dockH,
                        0x331418, 0x210C0E, 0x140708,
                        0x754534, 0x99614E, 0xB37864);
                DrawUtils.drawShadowText(
                        g, DrawUtils.UI_FONT_BOLD, pauseLabel,
                        bx + (bw >> 1), textY, Graphics.TOP | Graphics.HCENTER,
                        0xB89E96, 0x140606);
            }
        }
    }

    public void e() {
        this.c.b(0);
        GemTileSprite tile1 = this.b.a(this.a.f() - 2, this.a.g() - 2);
        this.c.a(((Drawable)tile1).i(), ((Drawable)tile1).j());
    }

    public void f() {
        this.c.b(0);
        GemTileSprite tile1 = this.b.a(this.a.f() - 2, this.a.g() - 2);
        this.c.b(((Drawable)tile1).i(), ((Drawable)tile1).j());
    }

    public void g() {
        this.c.b(0);
        int num1 = this.a.f();
        int num2 = this.a.g();
        GemTileSprite tile3 = this.b.a(num1 - 2, num2 - 2);
        int num4 = 0;
        if (num2 > 2) {
            num4 |= 1;
        }

        if (num2 < 9) {
            num4 |= 2;
        }

        if (num1 > 2) {
            num4 |= 4;
        }

        if (num1 < 9) {
            num4 |= 8;
        }

        this.c.a(num4, ((Drawable)tile3).i(), ((Drawable)tile3).j());
    }

    private int a(int num1, int num2, int num3, int num4) {
        byte bVal5 = 0;
        if (num2 < num4) {
            bVal5 = 2;
        } else if (num2 > num4) {
            bVal5 = 1;
        } else if (num1 < num3) {
            bVal5 = 8;
        } else if (num1 > num3) {
            bVal5 = 4;
        }

        return bVal5;
    }

    public void a(int num1, int num2) {
        this.c.b(0);
        int num3 = this.a.f();
        int num4 = this.a.g();
        GemTileSprite tile5 = this.b.a(num3 - 2, num4 - 2);
        GemTileSprite tile6 = this.b.a(num1 - 2, num2 - 2);
        this.c.b(((Drawable)tile6).i(), ((Drawable)tile6).j(), ((Drawable)tile5).i(), ((Drawable)tile5).j());
    }

    public void a(SwapMove move1) {
        int num2 = move1.a;
        int num3 = move1.b;
        int num4;
        if ((move1.e[0] & 255) < 3 && (move1.e[1] & 255) < 3) {
            num4 = this.a(move1.a, move1.b, move1.c, move1.d);
        } else {
            num4 = this.a(move1.c, move1.d, move1.a, move1.b);
            num2 = move1.c;
            num3 = move1.d;
        }

        this.c.b(1);
        GemTileSprite tile5 = this.b.a(num2 - 2, num3 - 2);
        this.c.a(num4, ((Drawable)tile5).i(), ((Drawable)tile5).j());
    }

    public void b(SwapMove move1) {
        int num2 = move1.a;
        int num3 = move1.b;
        int num4;
        if ((move1.e[0] & 255) < 3 && (move1.e[1] & 255) < 3) {
            num4 = this.a(move1.a, move1.b, move1.c, move1.d);
        } else {
            num4 = this.a(move1.c, move1.d, move1.a, move1.b);
            num2 = move1.c;
            num3 = move1.d;
        }

        int num5 = (num3 - 2) * 21 + this.b.i();
        int num6 = (num2 - 2) * 21 + this.b.j();
        this.directionHintArrows.a(num5, num6, num4);
    }

    public void h() {
        this.directionHintArrows.a();
    }

    public void i() {
        this.c.b();
    }

    public int j() {
        int num1 = 0;

        for(int num2 = 0; num2 < 8; ++num2) {
            int num3 = 0 + num1;

            for(int num4 = 0; num4 < 8; ++num4) {
                GemTileSprite tile5 = this.b.a(num2, num4);
                tile5.b(21, num3);
                ++num3;
            }

            ++num1;
        }

        return 22;
    }

    public int k() {
        int num1 = 0;
        int[][] num2 = this.a.e();
        int[] num3 = new int[12];

        for(int num4 = 2; num4 < 10; ++num4) {
            int num5 = 0;

            for(int num6 = 9; num6 >= 2; --num6) {
                this.b(num5, num4, num6, num4, num2[num6][num4], num3[num4] + num1);
                int numTmp2 = num3[num4]++;
                --num5;
            }

            ++num1;
        }

        return 21;
    }

    public void syncBossSeals() {
        if (this.b != null && this.a != null) {
            this.b.syncBossSeals(this.a);
        }
    }

    /** Spawns a crystal spark stream from board cell {@code (row, col)} (2..9) to {@code targetFighterIdx}'s HUD bar. */
    public void spawnSealSpark(int row, int col, int targetFighterIdx, int crystalIndex) {
        if (this.b == null || this.d == null || row < 2 || row >= 10 || col < 2 || col >= 10) {
            return;
        }
        DummyDrawable[] anchors = this.d.a();
        if (anchors == null || targetFighterIdx < 0 || targetFighterIdx >= anchors.length) {
            return;
        }
        DummyDrawable anchor = anchors[targetFighterIdx];
        int startX = this.b.i() + (col - 2) * 21 + 10;
        int startY = this.b.j() + (row - 2) * 21 + 10;
        int endX = ((Drawable) anchor).i() + ((Drawable) anchor).k() / 2;
        int endY = ((Drawable) anchor).j() + ((Drawable) anchor).l() / 2;
        this.a(startX, startY, endX, endY, crystalIndex);
    }

    public void l() {
        int[][] num1 = this.a.e();

        for(int num2 = 2; num2 < 10; ++num2) {
            for(int num3 = 9; num3 >= 2; --num3) {
                this.a(num3, num2, num1[num3][num2]);
            }
        }
        this.syncBossSeals();
    }

    public void a(boolean flag1) {
        this.stageVisible = flag1;
    }

    public void a(int num1, int num2, int num3) {
        this.b.a(num1, num2, num3);
    }

    public void a(int num1, int num2, int num3, int num4, int num5, int num6) {
        this.b.a(num1, num2, num3, num4, num5, num6);
        this.syncBossSeals();
    }

    public void b(int num1, int num2, int num3, int num4, int num5, int num6) {
        this.b.b(num1, num2, num3, num4, num5, num6);
        this.syncBossSeals();
    }

    public int a(GemMatch match1, int num2) {
        if (BattleConstants.i[match1.e] == 0) {
            num2 = (num2 + 1) % 2;
            this.b(match1, num2);
        }

        DummyDrawable val3 = this.d.a()[num2];
        int num4 = ((Drawable)val3).i() + ((Drawable)val3).k() / 2;
        byte bVal5 = 35;
        if (match1.e != 4 && match1.e != 5) {
            if (num2 == 0) {
                num4 += bVal5;
            } else {
                num4 -= bVal5 + 20;
            }
        }

        int num6 = ((Drawable)val3).j();
        switch (match1.e) {
            case 1:
                num6 += 8;
                break;
            case 2:
                this.d.a(12);
                num6 += 0;
                break;
            case 3:
                num6 += 16;
                break;
            case 4:
            case 5:
                num6 += ((Drawable)val3).l() / 2;
                break;
        }

        int sparkType = match1.e;
        int num7 = this.b.i();
        int num8 = this.b.j();
        int num9 = match1.a - 2;
        int num10 = match1.b - 2;
        int num11 = 0;
        int num12 = num8 + num9 * 21;
        int num13 = num9 + match1.c;
        int num14 = num10 + match1.d;

        for(int num15 = num9; num15 < num13; ++num15) {
            num11 = num7 + num10 * 21;

            for(int num16 = num10; num16 < num14; ++num16) {
                this.a(num11, num12, num4, num6, sparkType);
                num11 += 21;
            }

            num12 += 21;
        }

        return this.b.a(match1, num4, num6);
    }

    public int b(boolean flag1) {
        Image img2;
        if (flag1) {
            img2 = MgImageLoader.load("/strwin");
        } else {
            img2 = MgImageLoader.load("/strlose");
        }

        return this.bannerPopup.showResultBanner(img2, flag1);
    }

    public void a(int num1, int num2, int num3, int num4, int num5) {
        if (num5 != 0 && num5 <= 5) {
            SparkStreamEffect stream6 = null;
            Image img7 = this.crystalSheets[num5];
            if (img7 != null) {
                for(int num8 = this.sparkStreamPool.b() - 1; num8 > -1; --num8) {
                    SparkStreamEffect stream9 = (SparkStreamEffect)this.sparkStreamPool.b(num8);
                    if (!((Drawable)stream9).m()) {
                        stream6 = stream9;
                        break;
                    }
                }

                if (stream6 == null) {
                    stream6 = new SparkStreamEffect();
                    this.sparkStreamPool.a(stream6);
                }

                stream6.a(num1, num2, num3, num4, img7);
            }
        }
    }

    public void b(int num1, int num2, int num3, int num4, int num5) {
        ComboBurstEffect burst6 = null;

        for(int num7 = this.comboBurstPool.b() - 1; num7 > -1; --num7) {
            ComboBurstEffect burst8 = (ComboBurstEffect)this.comboBurstPool.b(num7);
            if (!burst8.a()) {
                burst6 = burst8;
                break;
            }
        }

        if (burst6 == null) {
            burst6 = new ComboBurstEffect(this.starIcon);
            this.comboBurstPool.a(burst6);
        }

        int num10 = (num3 + num5 / 2 - 2) * 21 - (num5 % 2 == 0 ? 10 : 4) + this.b.i();
        int num11 = (num2 + num4 / 2 - 2) * 21 - (num4 % 2 == 0 ? 10 : 5) + this.b.j();
        int num9 = num1 - 1;
        burst6.a(num10, num11, num9, "x" + num1);
    }

    public void b(GemMatch match1, int num2) {
        Image img3 = this.crystalSheets[0];
        DummyDrawable[] val4 = this.d.a();
        int num5 = val4[num2].i() + 3 + RandomUtils.nextInt(val4[num2].k() - 6);
        int num6 = val4[num2].j() + 3 + RandomUtils.nextInt(val4[num2].l() - 6);
        Object val7 = null;
        int num8 = match1.a + match1.c;
        int num9 = match1.b + match1.d;

        for(int num10 = match1.a; num10 < num8; ++num10) {
            for(int num11 = match1.b; num11 < num9; ++num11) {
                FlyingSwordEffect sword15 = null;
                GemTileSprite tile12 = this.b.a(num10 - 2, num11 - 2);
                tile12.a_(0);

                for(int num13 = this.flyingSwordPool.b() - 1; num13 > -1; --num13) {
                    FlyingSwordEffect sword14 = (FlyingSwordEffect)this.flyingSwordPool.b(num13);
                    if (!((Drawable)sword14).m()) {
                        sword15 = sword14;
                        break;
                    }
                }

                if (sword15 == null) {
                    sword15 = new FlyingSwordEffect(img3);
                    this.flyingSwordPool.a(sword15);
                }

                sword15.b(((Drawable)tile12).i(), ((Drawable)tile12).j(), num5, num6);
            }
        }

        this.d.b(num2, 36);
        this.d.a(26);
        DummyDrawable val16 = val4[num2];
        int num17 = ((Drawable)val16).i() + ((Drawable)val16).k() / 2;

        for(int num18 = 0; num18 < this.spellEffectPool.b(); ++num18) {
            SpellEffect vfx19 = (SpellEffect)this.spellEffectPool.b(num18);
            if (vfx19.b() == 4 && ((Drawable)vfx19).i() == num17) {
                ((LightningShieldVfx)vfx19).e(26);
            }
        }

    }

    public void a(int num1) {
        DummyDrawable val2 = this.d.a()[num1];
        int num3 = ((Drawable)val2).i() + ((Drawable)val2).k() / 2;

        for(int num4 = 0; num4 < this.spellEffectPool.b(); ++num4) {
            SpellEffect vfx5 = (SpellEffect)this.spellEffectPool.b(num4);
            if (((Drawable)vfx5).m() && vfx5.b() == 8 && ((Drawable)vfx5).i() == num3) {
                ((FrameAnimator)vfx5).a_(1);
            }
        }

    }

    public void b(int num1) {
        DummyDrawable val2 = this.d.a()[num1];
        int num3 = ((Drawable)val2).i() + ((Drawable)val2).k() / 2;

        for(int num4 = 0; num4 < this.spellEffectPool.b(); ++num4) {
            SpellEffect vfx5 = (SpellEffect)this.spellEffectPool.b(num4);
            if (vfx5.b() == 4 && ((Drawable)vfx5).i() == num3) {
                ((LightningShieldVfx)vfx5).d(15);
            }
        }

    }

    public void b(int num1, int num2, int num3) {
        this.d.b(num1, num2);
        this.d.a(num3);
        DummyDrawable val2 = this.d.a()[num1];
        int centerX = ((Drawable)val2).i() + ((Drawable)val2).k() / 2;
        for (int i = 0; i < this.spellEffectPool.b(); ++i) {
            SpellEffect vfx = (SpellEffect)this.spellEffectPool.b(i);
            if (((Drawable)vfx).m() && vfx.b() == 4 && ((Drawable)vfx).i() == centerX) {
                ((LightningShieldVfx)vfx).e(num3);
            }
        }
    }

    public int a(int num1, int num2, GemMatch[] match3, int num4) {
        int num5 = 0;
        int num6 = 0;
        int num7 = (num2 + 1) % 2;
        switch (num1) {
            case 0:
                num6 = 0;
                num5 = (byte)0;
                int num55 = -300;
                short sVal58 = 300;
                if (num2 == 0) {
                    num55 = -num55;
                }

                DummyDrawable val106 = this.d.a()[num7];
                int num77 = ((Drawable)val106).i() + ((Drawable)val106).k() / 2;
                int num97 = ((Drawable)val106).j() + ((Drawable)val106).l() / 2;
                num5 = this.a(num5, num1, num77 - num55, num97 - sVal58, num77, num97, num6);
                this.b(num7, num6 + 28, num6 + 18);
                num6 = RandomUtils.nextInt(5) + 7;
                GemTileSprite tile112 = this.b.a(match3[4].a - 2, match3[4].b - 2);
                num77 = ((Drawable)tile112).i();
                num97 = ((Drawable)tile112).j();
                this.a(num5, num1, num77 - num55, num97 - sVal58, num77, num97, num6);
                num6 += 18;
                break;
            case 1:
                num6 = 0;
                num5 = 0;
                int num54 = -300;
                short sVal57 = 300;
                int num74 = 0;
                int num94 = 0;
                if (num2 == 0) {
                    num54 = -num54;
                }

                DummyDrawable val105 = this.d.a()[num7];
                num74 = ((Drawable)val105).i() + ((Drawable)val105).k() / 2;
                num94 = ((Drawable)val105).j() + ((Drawable)val105).l() / 2;
                num5 = this.a(num5, num1, num74 - num54, num94 - sVal57, num74, num94, num6);
                this.b(num7, num6 + 28, num6 + 18);
                int num114 = 0;
                int num115 = num4 / 4;

                for(int num116 = 0; num116 < num115; ++num116) {
                    GemTileSprite tile111 = this.b.a(match3[num114].a - 2, match3[num114].b - 2);
                    num74 = ((Drawable)tile111).i() - 10;
                    num94 = ((Drawable)tile111).j() - 10;
                    int num119 = RandomUtils.nextInt(15) + 7;
                    num5 = this.a(num5, num1, num74 - num54, num94 - sVal57, num74, num94, num119);
                    if (num119 > num6) {
                        num6 = num119;
                    }

                    num114 += 4;
                }

                num6 += 16;
                break;
            case 2:
                num6 = 0;
                num5 = 0;
                DummyDrawable val104 = this.d.a()[num7];
                int num72 = ((Drawable)val104).i() + ((Drawable)val104).k() / 2;
                int num92 = ((Drawable)val104).j() + ((Drawable)val104).l() / 2;
                num5 = this.a(num5, num1, num72, num92, num72, num92, num6);
                this.b(num7, num6 + 10, num6 + 4);
                int num14 = num4 / 16;
                int num15 = 5;

                for(int num113 = 0; num113 < num14; ++num113) {
                    GemTileSprite tile110 = this.b.a(match3[num15].a - 2, match3[num15].b - 2);
                    num72 = ((Drawable)tile110).i();
                    num92 = ((Drawable)tile110).j();
                    int num17 = RandomUtils.nextInt(15) + 4;
                    num5 = this.a(num5, num1, num72, num92, num72, num92, num17);
                    if (num17 > num6) {
                        num6 = num17;
                    }

                    num15 += 16;
                }

                num6 += 6;
                break;
            case 3:
                num6 = 0;
                num5 = 0;
                int[] num122 = new int[num4 + 1];
                int[] num124 = new int[num4 + 1];
                int num22 = num124.length - 1;

                for(int num23 = 0; num23 < num22; ++num23) {
                    GemTileSprite tile109 = this.b.a(match3[num23].a - 2, match3[num23].b - 2);
                    num122[num23] = ((Drawable)tile109).i();
                    num124[num23] = ((Drawable)tile109).j();
                }

                num6 = num124.length * 5 + 10;
                this.b(num7, num6, num6 - 6);
                DummyDrawable val103 = this.d.a()[num7];
                int num71 = ((Drawable)val103).i() + ((Drawable)val103).k() / 2;
                int num91 = ((Drawable)val103).j() + ((Drawable)val103).l() / 2;
                num122[num122.length - 1] = num71;
                num124[num122.length - 1] = num91;
                num5 = this.a(num5, num1, num71, num91, num71, num91, num6);
                ChainLightningVfx vfx125 = (ChainLightningVfx)this.spellEffectPool.b(num5);
                vfx125.a(num122, num124);
                break;
            case 4:
                num6 = 0;
                num5 = (byte)0;
                DummyDrawable val102 = this.d.a()[num2];
                int num70 = ((Drawable)val102).i() + ((Drawable)val102).k() / 2;
                int num90 = ((Drawable)val102).j() + ((Drawable)val102).l() / 2;
                boolean shieldRefreshed = false;
                for (int sIdx = 0; sIdx < this.spellEffectPool.b(); ++sIdx) {
                    SpellEffect existingShield = (SpellEffect)this.spellEffectPool.b(sIdx);
                    if (existingShield.b() == 4 && ((Drawable)existingShield).i() == num70) {
                        existingShield.a(num70, num90, num70, num90, 0);
                        shieldRefreshed = true;
                        break;
                    }
                }
                if (!shieldRefreshed) {
                    this.a(num5, num1, num70, num90, num70, num90, 0);
                }
                break;
            case 5:
                num6 = 0;
                num5 = 0;
                int num67 = 0;
                int num87 = 0;
                DummyDrawable val101 = this.d.a()[num7];
                num67 = ((Drawable)val101).i() + ((Drawable)val101).k() / 2;
                num87 = ((Drawable)val101).j() + ((Drawable)val101).l() / 2;
                num5 = this.a(num5, num1, num67, num87, num67, num87, num6);
                this.b(num7, num6 + 12, num6 + 10);
                int num16 = 4;
                int num18 = num4 / 9;

                for(int num118 = 0; num118 < num18; ++num118) {
                    GemTileSprite tile108 = this.b.a(match3[num16].a - 2, match3[num16].b - 2);
                    num67 = ((Drawable)tile108).i();
                    num87 = ((Drawable)tile108).j();
                    int num121 = RandomUtils.nextInt(20) + 6;
                    num5 = this.a(num5, num1, num67, num87, num67, num87, num121);
                    if (num121 > num6) {
                        num6 = num121;
                    }

                    num16 += 9;
                }

                num6 += 5;
                break;
            case 6:
                num6 = 0;
                num5 = 0;
                int num53 = -55;
                short sVal56 = 300;
                int num63 = 0;
                int num83 = 0;
                if (num2 == 0) {
                    num53 = -num53;
                }

                DummyDrawable val100 = this.d.a()[num7];
                num63 = ((Drawable)val100).i() + ((Drawable)val100).k() / 2;
                num83 = ((Drawable)val100).j() + ((Drawable)val100).l() / 2;
                num5 = this.a(num5, 6, num63 - num53, num83 - sVal56, num63, num83, num6);
                this.b(num7, num6 + 28, num6 + 18);
                BattleFighter fighter117 = this.a.b(num7);
                if (!fighter117.k()) {
                    num6 = RandomUtils.nextInt(5) + 7;
                    num83 = ((Drawable)val100).j() + 28;
                    if (num7 == 0) {
                        num63 = ((Drawable)val100).i() - 1 + ((Drawable)val100).k() + 44 * fighter117.o() / fighter117.n();
                    } else {
                        num63 = ((Drawable)val100).i() - 7 - 44 * fighter117.o() / fighter117.n();
                    }

                    num5 = this.a(num5, 6, num63 - num53, num83 - sVal56, num63, num83, num6);
                    num6 += 18;
                    this.d.a(num7, num6 - 4);
                }

                for(int num120 = 0; num120 < num4; ++num120) {
                    GemTileSprite tile107 = this.b.a(match3[num120].a - 2, match3[num120].b - 2);
                    num63 = ((Drawable)tile107).i() - 10;
                    num83 = ((Drawable)tile107).j() - 10;
                    int num123 = RandomUtils.nextInt(15) + 7;
                    num5 = this.a(num5, 6, num63 - num53, num83 - sVal56, num63, num83, num123);
                    if (num123 > num6) {
                        num6 = num123;
                    }
                }

                num6 += 3;
                break;
            case 7:
                num5 = (byte)0;
                num6 = 14;
                this.d.a(num6 - 2);
                num6 = 0;
                DummyDrawable val99 = this.d.a()[num2];
                int num62 = ((Drawable)val99).i() + ((Drawable)val99).k() / 2;
                int num82 = ((Drawable)val99).j() + ((Drawable)val99).l() / 2;
                this.a(num5, num1, num62, num82 - 15, num62, num82, 0);
                break;
            case 8:
                num6 = 0;
                num5 = 0;
                int num8 = -55;
                short sVal9 = 300;
                int num10 = 0;
                int num11 = 0;
                if (num2 == 0) {
                    num8 = -num8;
                }

                DummyDrawable val12 = this.d.a()[num7];
                num10 = ((Drawable)val12).i() + ((Drawable)val12).k() / 2;
                num11 = ((Drawable)val12).j() + ((Drawable)val12).l() / 2;
                if (match3 != null) {
                    num5 = this.a(num5, 6, num10 - num8, num11 - sVal9, num10, num11, num6);
                    this.b(num7, num6 + 23, num6 + 13);
                }

                num5 = this.a(num5, 8, num10, num11, num10, num11, num6 + 28);
                BattleFighter fighter19 = this.a.b(num7);
                if (fighter19.k()) {
                    ((FrozenVfx)this.spellEffectPool.b(num5)).c();
                }

                if (match3 != null && !fighter19.k()) {
                    num6 = RandomUtils.nextInt(5) + 7;
                    num11 = ((Drawable)val12).j() + 27;
                    if (num7 == 0) {
                        num10 = ((Drawable)val12).i() - 3 + ((Drawable)val12).k() + 44 * fighter19.o() / fighter19.n();
                    } else {
                        num10 = ((Drawable)val12).i() - 5 - 44 * fighter19.o() / fighter19.n();
                    }

                    num5 = this.a(num5, 6, num10 - num8, num11 - sVal9, num10, num11, num6);
                    num6 += 18;
                    this.d.a(num7, num6 - 4);
                }

                for(int num20 = 0; num20 < num4; ++num20) {
                    GemTileSprite tile13 = this.b.a(match3[num20].a - 2, match3[num20].b - 2);
                    num10 = ((Drawable)tile13).i() - 10;
                    num11 = ((Drawable)tile13).j() - 10;
                    int num21 = RandomUtils.nextInt(15) + 7;
                    num5 = this.a(num5, 6, num10 - num8, num11 - sVal9, num10, num11, num21);
                    if (num21 > num6) {
                        num6 = num21;
                    }
                }

                num6 = 36;
        }

        return num6;
    }

    private int b(int num1, int num2) {
        if (num1 < 0) {
            return -1;
        } else {
            for(int num3 = num1; num3 < this.spellEffectPool.b(); ++num3) {
                SpellEffect vfx4 = (SpellEffect)this.spellEffectPool.b(num3);
                if (!((Drawable)vfx4).m() && vfx4.b() == num2) {
                    return num3;
                }
            }

            return -1;
        }
    }

    private int a(int num1, int num2, int num3, int num4, int num5, int num6, int num7) {
        num1 = this.b(num1, num2);
        Object val8 = null;
        if (num1 >= 0) {
            val8 = (SpellEffect)this.spellEffectPool.b(num1);
        } else {
            switch (num2) {
                case 0:
                case 1:
                    val8 = new FireProjectileVfx(BattleResourceManager.a().r, 9);
                    ((FrameAnimator)val8).a(new byte[][]{{0, 1, 2, 3}, {4, 4, 5, 5, 6, 6, 7, 7, 8, 8}});
                    break;
                case 2:
                    val8 = new FireProjectileVfx(BattleResourceManager.a().s, 4);
                    ((FrameAnimator)val8).a(new byte[][]{new byte[1], {0, 0, 1, 1, 2, 2, 3, 3, 3}});
                    break;
                case 3:
                    val8 = new ChainLightningVfx(BattleResourceManager.a().y, 3, BattleResourceManager.a().h);
                    ((FrameAnimator)val8).a(new byte[][]{{0, 1, 2, -1}});
                    break;
                case 4:
                    val8 = new LightningShieldVfx(BattleResourceManager.a().x, 7);
                    ((FrameAnimator)val8).a(new byte[][]{{0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 4}, {5, 5, 6, 6, 5, 5, 6, 6}});
                    break;
                case 5:
                    val8 = new LightningStrikeVfx(BattleResourceManager.a().t, 5);
                    ((FrameAnimator)val8).a(new byte[][]{{0, 0, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4}});
                    break;
                case 6:
                    val8 = new IceBoltVfx(BattleResourceManager.a().u, 5);
                    ((FrameAnimator)val8).a(new byte[][]{new byte[1], {1, 1, 2, 2, 3, 3, 4, 4}});
                    break;
                case 7:
                    val8 = new HealingVfx(BattleResourceManager.a().w, 3);
                    ((FrameAnimator)val8).a(new byte[][]{{0, 0, 0, 1, 1, 1, 2, 2, 0, 0, 1, 1, 2, 2}});
                    break;
                case 8:
                    val8 = new FrozenVfx(BattleResourceManager.a().v, 9);
                    ((FrameAnimator)val8).a(new byte[][]{{0, 0, 1, 1, 2, 2, 3, 3, 4, 4}, {5, 5, 6, 6, 7, 7, 8, 8}});
            }

            ((SpellEffect)val8).a(num2);
            this.spellEffectPool.a(val8);
            num1 = this.spellEffectPool.b() - 1;
        }

        ((SpellEffect)val8).a(num3, num4, num5, num6, num7);
        return num1;
    }

    public void c(boolean flag1) {
        this.d.d(flag1);
    }

    public void c(int num1) {
        this.d.b(num1);
    }

    public void showSkillBanner(String text, boolean isPlayer, boolean isShatter) {
        if (this.d != null) {
            this.d.showSkillBanner(text, isPlayer, isShatter);
        }
    }

    public void showFoodBurnPopup(int fighterIndex, int burnAmount) {
        if (this.d != null) {
            this.d.showFoodBurnPopup(fighterIndex, burnAmount);
        }
    }

    public String getSelectedTileName() {
        if (this.a == null || !this.a.h()) {
            return null;
        }
        int r = this.a.f();
        int c = this.a.g();
        int gem = this.a.a(r, c);
        String base;
        switch (gem) {
            case 0:
                base = "Kiếm";
                break;
            case 1:
                base = "Bánh Chưng";
                break;
            case 2:
                base = "Tim Máu";
                break;
            case 3:
                base = "Nộ Khí";
                break;
            case 4:
                base = "Ngọc Vàng";
                break;
            case 5:
                base = "Binh Thư";
                break;
            case 6:
                base = "Kiếm Lửa";
                break;
            default:
                return null;
        }
        return this.a.getBossSealTurns(r, c) > 0 ? (base + " (Ấn Boss)") : base;
    }

    public void a(int num1, MenuSelectionListener val2, BattleFighter fighter3, boolean tileSelected) {
        this.skillSelectDialog.a(num1, val2, fighter3, tileSelected, tileSelected ? this.getSelectedTileName() : null);
    }

    public void m() {
        this.d.c();
    }

    public void n() {
        for(int num2 = this.spellEffectPool.b() - 1; num2 > -1; --num2) {
            SpellEffect vfx1 = (SpellEffect)this.spellEffectPool.b(num2);
            ((Drawable)vfx1).a(false);
        }

        this.turnTimerWidget.b(CampaignData.g);
        this.d.b(false);
        this.d.c(false);
        this.d.resetTracking();
        this.syncBossSeals();
    }

    public void c(int num1, int num2, int num3) {
        int num4 = (num3 - 2) * 21 + this.b.i();
        int num5 = (num2 - 2) * 21 + this.b.j();
        this.blinkingAlertText.a("Còn " + num1 + " lượt", num4, num5);
    }

    public void d(boolean flag1) {
        this.d.b(flag1);
    }

    public void e(boolean flag1) {
        this.d.c(flag1);
    }

    public void d(int num1) {
        DummyDrawable val2 = this.d.a()[num1];
        int num3 = ((Drawable)val2).i() + ((Drawable)val2).k() / 2;
        byte bVal4 = 35;
        if (num1 == 0) {
            num3 += bVal4;
        } else {
            num3 -= bVal4 + 20;
        }

        int num5 = ((Drawable)val2).j();
        int[] num6 = new int[]{-20, -10, 10, 20, -10, 10};
        int[] num7 = new int[]{0, -20, -20, 0, 20, 20};

        for(int num8 = 0; num8 < num7.length; ++num8) {
            this.a(num3 + num6[num8], num5 + num7[num8], num3, num5, 2);
        }

    }

    public int o() {
        return this.turnTimerWidget.a();
    }

    public void e(int num1) {
        this.turnTimerWidget.a(num1);
    }

    public void d(int num1, int num2, int num3) {
        int num4 = (num3 - 2) * 21 + this.b.i();
        int num5 = (num2 - 2) * 21 + this.b.j();
        this.floatingIconEffect.a(num1, num4, num5);
    }
}





