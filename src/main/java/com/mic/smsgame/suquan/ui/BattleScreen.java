package com.mic.smsgame.suquan.ui;

import com.mg.engine.core.ActionListener;
import com.mg.engine.core.DebugLogForm;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.BattleResourceManager;
import com.mg.engine.storage.CampaignStorage;
import com.mg.engine.ui.BaseScreen;
import com.mg.engine.ui.SoftKeyBar;
import com.mg.engine.util.ByteUtils;
import com.mic.smsgame.suquan.battle.BattleConstants;
import com.mic.smsgame.suquan.battle.BattleController;
import com.mic.smsgame.suquan.battle.BattleModel;
import com.mic.smsgame.suquan.battle.BattleStageView;
import com.mic.smsgame.suquan.data.CampaignData;
import com.mic.smsgame.suquan.model.HeroStats;
import javax.microedition.lcdui.Graphics;

/** Active match-3 battle screen wrapping the battle model, board renderer, and battle controller. */
public class BattleScreen extends BaseScreen implements BattleConstants {
    BattleModel c;
    BattleStageView d;
    BattleController e;

    public BattleScreen() {
        long startMs = System.currentTimeMillis();

        try {
            BattleResourceManager.b();
            try {
                byte[] snapshot = null;
                int offset = 0;
                if (CampaignStorage.hasBattleSnapshot()) {
                    snapshot = CampaignStorage.loadBattleSnapshot();
                    // Keep snapshot intact until battle ends or next turn overwrites it so tab refresh never loses it
                    CampaignData.c = ByteUtils.a(snapshot, offset);
                    offset += 4;
                    CampaignData.e = new HeroStats();
                    offset += CampaignData.e.b(snapshot, offset);
                    CampaignData.f = new HeroStats();
                    offset += CampaignData.f.b(snapshot, offset);
                    BattleController.restoreSnapshotContextIfPresent(snapshot, offset);
                }

                this.c = new BattleModel(CampaignData.d(CampaignData.c), CampaignData.e, CampaignData.f);
                this.d = new BattleStageView(this.c);
                this.e = new BattleController(this.c, this.d);
                this.e.a(snapshot, offset);
            } catch (Exception initError) {
                initError.printStackTrace();
                DebugLogForm.log((Throwable) initError);
                CampaignStorage.clearBattleSnapshot();
                GUIManager.getController().a(SuQuanController.SCREEN_BATTLE, SuQuanController.SCREEN_WORLD_MAP, false);
                return;
            }

            this.a((ActionListener) GUIManager.getController());
            this.a((SoftKeyBar) null);
            CampaignData.h();

            try {
                BattleResourceManager.a().c();
                BattleResourceManager.a().b(CampaignData.e.f);
                BattleResourceManager.a().b(CampaignData.f.f);
            } catch (Exception audioError) {
                BattleResourceManager.a().j();
                audioError.printStackTrace();
                DebugLogForm.log((Throwable) audioError);
            }
        } catch (Exception fatalError) {
            fatalError.printStackTrace();
            DebugLogForm.log((Throwable) fatalError);
            GUIManager.getController().a(SuQuanController.SCREEN_BATTLE, SuQuanController.SCREEN_WORLD_MAP, false);
        }

        DebugLogForm.log("DONE LOADPLAY" + (System.currentTimeMillis() - startMs));
        super.q = SuQuanController.SCREEN_BATTLE;
    }

    public void r() {
        BattleResourceManager.k();
    }

    public void a(int keyIndex) {
        this.e.b(keyIndex);
    }

    public void a(int x, int y) {
        int ox = GameConfig.contentOffsetX;
        int oy = GameConfig.contentOffsetY;

        // 1. Direct tap on top-center 1x/2x battle speed pill (works anytime)
        if (x >= ox + 96 && x <= ox + 144 && y >= oy + 15 && y <= oy + 35) {
            GameConfig.battleFastMode = !GameConfig.battleFastMode;
            return;
        }

        // 2. Tap Player Name Plaque (top-left), Player Avatar, or Player Status Pills -> open Player stats dialog
        boolean hitPlayerHeader = (x >= ox && x <= ox + 98 && y >= oy && y <= oy + 33);
        boolean hitPlayerAvatar = (x >= ox + 32 && x <= ox + 67 && y >= oy + 234 && y <= oy + 271)
                || (x >= ox + 2 && x <= ox + 118 && y >= oy + 272 && y <= oy + 290);
        if (hitPlayerHeader || hitPlayerAvatar) {
            this.showFighterStats(0);
            return;
        }

        // 3. Tap Enemy Name Plaque (top-right) OR anywhere on Enemy HUD (Avatar, Bars, Status Pills) -> open Enemy/Boss stats dialog
        boolean hitEnemyHeader = (x >= ox + 142 && x <= ox + 240 && y >= oy && y <= oy + 33);
        boolean hitEnemyHud = (x >= ox + 122 && x <= ox + 238 && y >= oy + 234 && y <= oy + 290);
        if (hitEnemyHeader || hitEnemyHud) {
            this.showFighterStats(1);
            return;
        }

        this.e.a(x, y);
    }

    private void showFighterStats(int fighterIndex) {
        if (this.c == null || this.d == null) {
            return;
        }
        FighterStatsDialog dialog = new FighterStatsDialog(
                this.c.b(0),
                this.d.getFighterPortrait(0),
                this.c.b(1),
                this.d.getFighterPortrait(1),
                fighterIndex);
        GUIManager.getInstance().a(dialog, false);
    }

    public void b(int x, int y) {
        this.e.onPointerDragged(x, y);
    }

    public void g(int x, int y) {
        this.e.onPointerReleased(x, y);
    }

    public void a() {
        this.e.c();
        this.d.d();
        if (GameConfig.battleFastMode && this.e.isAnimatingOrAiTurn()) {
            this.e.c();
            this.d.d();
        }
    }

    public void a(Graphics g) {
        this.d.a(g);
    }

    public void e() {
        this.e.t();
    }

    public void t() {
        this.d.a(false);
    }

    public void s() {
        this.d.a(true);
    }
}


