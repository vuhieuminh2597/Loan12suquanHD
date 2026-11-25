package com.mic.smsgame.suquan.battle;

import com.mg.engine.core.ActionListener;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.BattleResourceManager;
import com.mg.engine.graphics.Rect;
import com.mg.engine.storage.CampaignStorage;
import com.mg.engine.storage.EngineStorage;
import com.mg.engine.ui.BaseDialog;
import com.mg.engine.ui.MenuSelectionListener;
import com.mg.engine.ui.SoftKeyButton;
import com.mg.engine.ui.TextSoftKeyButton;
import com.mg.engine.ui.Window;
import com.mg.engine.util.ByteUtils;
import com.mg.engine.util.ObjectList;
import com.mg.engine.util.RandomUtils;
import com.mic.smsgame.suquan.data.CampaignData;
import com.mic.smsgame.suquan.data.GameStrings;
import com.mic.smsgame.suquan.model.BattleFighter;
import com.mic.smsgame.suquan.model.GemMatch;
import com.mic.smsgame.suquan.model.ItemSlot;
import com.mic.smsgame.suquan.model.SwapMove;
import com.mic.smsgame.suquan.ui.BattlePauseDialog;
import com.mic.smsgame.suquan.ui.BattleTutorialOverlay;

/**
 * Main match-3 battle state machine orchestrating turns, gem swaps, cascades, scoring,
 * spell casting, item effects, tutorial scripts, win/lose resolution, and battle snapshots.
 */
public class BattleController implements ActionListener, BattleConstants, MenuSelectionListener {
    private static final int FOOD_COST_PER_ACTION = 100;
    private static final int MAX_BOARD_SHARDS_PER_BATTLE = 3;
    private static final int SNAPSHOT_EXT_MAGIC = 0x53515632; // "SQV2" extended snapshot trailer
    private static final int SNAPSHOT_EXT_VERSION_V2 = 2;
    private static final int SNAPSHOT_EXT_VERSION_V3 = 3;
    private static final int SNAPSHOT_EXT_VERSION_V4 = 4;
    private static final int SNAPSHOT_EXT_VERSION_V5 = 5;
    private static final int SNAPSHOT_EXT_VERSION = 6;
    /** Exact byte length between end of HeroStats pair and start of SNAPSHOT_EXT_MAGIC (216 model + 36 controller). */
    private static final int BASE_PAYLOAD_TO_TRAILER_BYTES = 252;

    final int a = 0;
    final int b = 6;
    final int c = 10;
    final int d = 7;
    final int e = 8;
    final int f = 1;
    final int g = 2;
    final int k = 3;
    final int l = 4;
    final int m = 5;
    final int n = 11;
    final int o = 9;
    final int p = 12;
    final int q = 13;
    final int r = 14;
    private BattleModel battleModel;
    private BattleStageView stageView;
    private BattleActor[] turnActors;
    private int battleState;
    private int[] columnDropCounts;
    private int stateDelayTicks;
    private GemMatch[] activeMatches;
    private int activeMatchCount;
    private SwapMove pendingSwapMove;
    private ObjectList availableMoves;
    private int animWaitTicks;
    private int cascadeComboCount;
    private boolean earnedExtraTurn = false;
    private int activeSkillId;
    private int turnCounter;
    private int bonusTurnBannerCount;
    private int[] shieldTurnsRemaining;
    private int[] frozenTurnsRemaining;
    private boolean itemUsedThisTurn;
    private boolean surrendered = false;
    private int pendingForgedFierySwords = 0;
    private int pendingBattleShards = 0;
    private boolean resolvingChainLightning = false;
    private int chainLightningManaAccum = 0;
    private boolean fierySwordDetonatedInWave = false;
    private boolean fireComboSwordForgedThisTurn = false;
    private int[] neighborRowOffsets = new int[]{-1, -1, 0, 1, 1, 1, 0, -1};
    private int[] neighborColOffsets = new int[]{0, 1, 1, 1, 0, -1, -1, -1};

    public void a(byte[] bVal1, int num2) {
        if (bVal1 != null) {
            this.resolvingChainLightning = false;
            this.chainLightningManaAccum = 0;
            this.fireComboSwordForgedThisTurn = false;
            this.pendingBattleShards = 0;
            this.battleModel.a(0);
            this.f(7);
            num2 += this.battleModel.b(bVal1, num2);
            this.stageView.l();
            this.availableMoves = this.battleModel.k();
            this.stageView.e(ByteUtils.a(bVal1, num2));
            num2 += 4;
            this.turnCounter = ByteUtils.a(bVal1, num2);
            num2 += 4;
            this.bonusTurnBannerCount = ByteUtils.a(bVal1, num2);
            num2 += 4;
            if (this.bonusTurnBannerCount > 0) {
                this.stageView.c(this.bonusTurnBannerCount, 6, 6);
            }

            for(int num3 = 0; num3 < this.shieldTurnsRemaining.length; ++num3) {
                this.shieldTurnsRemaining[num3] = ByteUtils.a(bVal1, num2);
                num2 += 4;
                if (this.shieldTurnsRemaining[num3] > 0) {
                    this.stageView.a(4, num3, this.activeMatches, 0);
                    BattleFighter sf = this.battleModel.b(num3);
                    sf.a(true);
                    sf.setShieldTurnsDisplay((this.shieldTurnsRemaining[num3] + 1) >> 1);
                }
            }

            for(int num9 = 0; num9 < this.frozenTurnsRemaining.length; ++num9) {
                this.frozenTurnsRemaining[num9] = ByteUtils.a(bVal1, num2);
                num2 += 4;
                if (this.frozenTurnsRemaining[num9] > 0) {
                    this.stageView.a(8, (num9 + 1) % 2, (GemMatch[])null, 0);
                }
            }

            this.itemUsedThisTurn = ByteUtils.a(bVal1, num2) == 1;
            num2 += 4;
            this.surrendered = ByteUtils.a(bVal1, num2) == 1;
            num2 += 4;

            // Optional extended snapshot trailer (v2..v6+); legacy v1.12.1 snapshots end right after `surrendered`.
            int[] pendingShardsOut = new int[]{0};
            readExtendedSnapshotTrailer(this.battleModel, bVal1, num2, pendingShardsOut);
            this.pendingBattleShards = pendingShardsOut[0];
            if (this.stageView != null) {
                this.stageView.syncBossSeals();
                if (this.stageView.d != null) {
                    this.stageView.d.resetTracking();
                }
            }

            ItemSlot[] slot10 = this.battleModel.b(0).a;
            if (slot10[3].b >= 1) {
                this.stageView.e(true);
            }

            if (slot10[0].b >= 1) {
                this.stageView.d(true);
            }

        }
    }

    /**
     * Peeks the SQV5/SQV6 extended trailer before {@link BattleModel} construction so Arena ({@code CampaignData.a/b})
     * and Side-Quest ({@code CampaignData.d}) context are restored on cold-boot resume.
     */
    public static void restoreSnapshotContextIfPresent(byte[] snapshot, int afterHeroStatsOffset) {
        if (snapshot == null || afterHeroStatsOffset < 0) {
            return;
        }
        int trailerOffset = afterHeroStatsOffset + BASE_PAYLOAD_TO_TRAILER_BYTES;
        if (trailerOffset + 8 > snapshot.length || ByteUtils.a(snapshot, trailerOffset) != SNAPSHOT_EXT_MAGIC) {
            return;
        }
        int extVersion = ByteUtils.a(snapshot, trailerOffset + 4);
        if (extVersion < SNAPSHOT_EXT_VERSION_V5) {
            return;
        }
        int pos = trailerOffset + 8 + (2 * BattleFighter.EXTENDED_SNAPSHOT_BYTES);
        if (pos + 4 > snapshot.length) {
            return;
        }
        int savedItemCount = ByteUtils.a(snapshot, pos);
        pos += 4;
        if (savedItemCount < 0) {
            savedItemCount = 0;
        }
        pos += savedItemCount * 4;
        pos += 4; // pendingShards (V3)
        pos += 2 * BattleFighter.BOSS_SNAPSHOT_BYTES; // boss state (V4)
        if (pos + 12 <= snapshot.length) {
            CampaignData.a = ByteUtils.a(snapshot, pos);
            CampaignData.b = ByteUtils.a(snapshot, pos + 4);
            CampaignData.d = ByteUtils.a(snapshot, pos + 8);
        }
    }

    /** Writes the SQV2..SQV6 extended trailer (debuffs, item uses, pending shards, Boss state, Arena/SideQuest context, Boss Seals). */
    public static int writeExtendedSnapshotTrailer(BattleModel model, byte[] buffer, int offset) {
        return writeExtendedSnapshotTrailer(model, buffer, offset, 0);
    }

    public static int writeExtendedSnapshotTrailer(BattleModel model, byte[] buffer, int offset, int pendingShards) {
        int start = offset;
        System.arraycopy(ByteUtils.a(SNAPSHOT_EXT_MAGIC), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(SNAPSHOT_EXT_VERSION), 0, buffer, offset, 4);
        offset += 4;
        for (int fi = 0; fi < 2; ++fi) {
            offset += model.b(fi).writeExtendedSnapshot(buffer, offset);
        }
        int itemCount = model.a != null ? model.a.length : 0;
        System.arraycopy(ByteUtils.a(itemCount), 0, buffer, offset, 4);
        offset += 4;
        for (int k = 0; k < itemCount; ++k) {
            System.arraycopy(ByteUtils.a(model.a[k].b), 0, buffer, offset, 4);
            offset += 4;
        }
        System.arraycopy(ByteUtils.a(pendingShards < 0 ? 0 : pendingShards), 0, buffer, offset, 4);
        offset += 4;
        for (int fi = 0; fi < 2; ++fi) {
            offset += model.b(fi).writeBossSnapshot(buffer, offset);
        }
        System.arraycopy(ByteUtils.a(CampaignData.a), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(CampaignData.b), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(CampaignData.d), 0, buffer, offset, 4);
        offset += 4;
        int sealCount = model.countActiveBossSeals();
        System.arraycopy(ByteUtils.a(sealCount), 0, buffer, offset, 4);
        offset += 4;
        if (sealCount > 0) {
            for (int r = 2; r < 10; ++r) {
                for (int c = 2; c < 10; ++c) {
                    int st = model.getBossSealTurns(r, c);
                    if (st > 0) {
                        int packed = ((r & 0xFF) << 16) | ((c & 0xFF) << 8) | (st & 0xFF);
                        System.arraycopy(ByteUtils.a(packed), 0, buffer, offset, 4);
                        offset += 4;
                    }
                }
            }
        }
        return offset - start;
    }

    /**
     * Reads the optional SQV2..SQV6 extended trailer into {@code model}.
     * Legacy v1.12.1 snapshots without the SQV2 magic header are safely ignored without error.
     */
    public static int readExtendedSnapshotTrailer(BattleModel model, byte[] buffer, int offset) {
        return readExtendedSnapshotTrailer(model, buffer, offset, null);
    }

    public static int readExtendedSnapshotTrailer(BattleModel model, byte[] buffer, int offset, int[] pendingShardsOut) {
        int start = offset;
        model.clearAllBossSeals();
        if (buffer == null || offset + 8 > buffer.length || ByteUtils.a(buffer, offset) != SNAPSHOT_EXT_MAGIC) {
            return 0;
        }
        offset += 4;
        int extVersion = ByteUtils.a(buffer, offset);
        offset += 4;
        if (extVersion < SNAPSHOT_EXT_VERSION_V2) {
            return offset - start;
        }
        for (int fi = 0; fi < 2 && offset + BattleFighter.EXTENDED_SNAPSHOT_BYTES <= buffer.length; ++fi) {
            offset += model.b(fi).readExtendedSnapshot(buffer, offset);
        }
        if (offset + 4 <= buffer.length) {
            int savedItemCount = ByteUtils.a(buffer, offset);
            offset += 4;
            if (savedItemCount < 0) {
                savedItemCount = 0;
            }
            for (int k = 0; k < savedItemCount && offset + 4 <= buffer.length; ++k) {
                int remainingUses = ByteUtils.a(buffer, offset);
                offset += 4;
                if (model.a != null && k < model.a.length) {
                    model.a[k].b = remainingUses < 0 ? 0 : remainingUses;
                }
            }
        }
        if (extVersion >= SNAPSHOT_EXT_VERSION_V3 && offset + 4 <= buffer.length) {
            int savedShards = ByteUtils.a(buffer, offset);
            offset += 4;
            if (pendingShardsOut != null && pendingShardsOut.length > 0) {
                pendingShardsOut[0] = savedShards < 0 ? 0 : savedShards;
            }
        }
        if (extVersion >= SNAPSHOT_EXT_VERSION_V4) {
            for (int fi = 0; fi < 2 && offset + BattleFighter.BOSS_SNAPSHOT_BYTES <= buffer.length; ++fi) {
                offset += model.b(fi).readBossSnapshot(buffer, offset);
            }
        }
        if (extVersion >= SNAPSHOT_EXT_VERSION_V5 && offset + 12 <= buffer.length) {
            CampaignData.a = ByteUtils.a(buffer, offset);
            offset += 4;
            CampaignData.b = ByteUtils.a(buffer, offset);
            offset += 4;
            CampaignData.d = ByteUtils.a(buffer, offset);
            offset += 4;
        }
        if (extVersion >= SNAPSHOT_EXT_VERSION && offset + 4 <= buffer.length) {
            int sealCount = ByteUtils.a(buffer, offset);
            offset += 4;
            if (sealCount < 0) {
                sealCount = 0;
            } else if (sealCount > 8) {
                sealCount = 8;
            }
            for (int si = 0; si < sealCount && offset + 4 <= buffer.length; ++si) {
                int packed = ByteUtils.a(buffer, offset);
                offset += 4;
                int sr = (packed >> 16) & 0xFF;
                int sc = (packed >> 8) & 0xFF;
                int st = packed & 0xFF;
                if (sr >= 2 && sr < 10 && sc >= 2 && sc < 10 && st > 0) {
                    model.setBossSealTurns(sr, sc, st > 3 ? 3 : st);
                }
            }
        }
        model.refreshBossImminentFlag();
        return offset - start;
    }

    /** Serializes the full mid-battle state (v1.12.1 base payload + SQV5 extended trailer). */
    public byte[] buildBattleSnapshotBytes() {
        int num1 = 0;
        byte[] bVal2 = new byte[768];
        System.arraycopy(ByteUtils.a(CampaignData.c), 0, bVal2, num1, 4);
        num1 += 4;
        num1 += CampaignData.e.a(bVal2, num1);
        num1 += CampaignData.f.a(bVal2, num1);
        num1 += this.battleModel.a(bVal2, num1);
        System.arraycopy(ByteUtils.a(this.stageView.o()), 0, bVal2, num1, 4);
        num1 += 4;
        System.arraycopy(ByteUtils.a(this.turnCounter), 0, bVal2, num1, 4);
        num1 += 4;
        System.arraycopy(ByteUtils.a(this.bonusTurnBannerCount), 0, bVal2, num1, 4);
        num1 += 4;

        for(int num3 = 0; num3 < this.shieldTurnsRemaining.length; ++num3) {
            System.arraycopy(ByteUtils.a(this.shieldTurnsRemaining[num3]), 0, bVal2, num1, 4);
            num1 += 4;
        }

        for(int num13 = 0; num13 < this.frozenTurnsRemaining.length; ++num13) {
            System.arraycopy(ByteUtils.a(this.frozenTurnsRemaining[num13]), 0, bVal2, num1, 4);
            num1 += 4;
        }

        System.arraycopy(ByteUtils.a(this.itemUsedThisTurn ? 1 : 0), 0, bVal2, num1, 4);
        num1 += 4;
        System.arraycopy(ByteUtils.a(this.surrendered ? 1 : 0), 0, bVal2, num1, 4);
        num1 += 4;
        num1 += writeExtendedSnapshotTrailer(this.battleModel, bVal2, num1, this.pendingBattleShards);

        byte[] trimmed = new byte[num1];
        System.arraycopy(bVal2, 0, trimmed, 0, num1);
        return trimmed;
    }

    private void u() {
        if (this.battleState == 8) {
            byte[] snapshot = this.buildBattleSnapshotBytes();
            CampaignStorage.saveBattleSnapshot(snapshot, 0, snapshot.length);
        }
    }

    public BattleController(BattleModel val1, BattleStageView val2) {
        this.battleModel = val1;
        this.stageView = val2;
        this.columnDropCounts = new int[64];
        this.activeMatches = new GemMatch[128];

        for(int num3 = 0; num3 < this.activeMatches.length; ++num3) {
            this.activeMatches[num3] = new GemMatch();
        }

        this.shieldTurnsRemaining = new int[2];
        this.frozenTurnsRemaining = new int[2];
        this.pendingBattleShards = 0;
        CampaignData.battleDroppedShards = 0;
        CampaignData.battleWinBonusShards = 0;
        this.b();
        if (!this.battleModel.d) {
            this.stageView.a((String)"T.Dừng", 2);
            this.stageView.a((String)"T.Chiêu", 0);
            if (GameConfig.hasPointerEvents) {
                this.stageView.a((String)"Túi", 1);
            }
        }

        this.v();
        this.f(0);
    }

    public void a() {
        this.shieldTurnsRemaining = new int[2];
        this.frozenTurnsRemaining = new int[2];
        this.surrendered = false;
        this.pendingBattleShards = 0;
        CampaignData.battleDroppedShards = 0;
        CampaignData.battleWinBonusShards = 0;
        this.resolvingChainLightning = false;
        this.chainLightningManaAccum = 0;
        this.fireComboSwordForgedThisTurn = false;
        BattleFighter[] fighter1 = this.battleModel.d();

        for(int num2 = 0; num2 < fighter1.length; ++num2) {
            fighter1[num2].h();
        }

        this.battleModel.b();
        this.stageView.n();
        this.stageView.e(CampaignData.g);
        this.turnCounter = this.bonusTurnBannerCount = 0;
        this.cascadeComboCount = 0;
        CampaignData.j = 0;
        CampaignData.i = 0;
        this.v();
        this.f(10);
        CampaignData.h();
    }

    private void v() {
        this.battleModel.a(CampaignData.h ? 0 : 1);
        this.turnCounter = this.bonusTurnBannerCount = 1;
    }

    public void b() {
        BattleFighter[] fighter1 = this.battleModel.d();
        this.turnActors = new BattleActor[2];
        this.turnActors[0] = this.g(fighter1[0].b(), 0);
        this.turnActors[1] = this.g(fighter1[1].b(), 1);
        byte bVal2 = 3;
        fighter1[0].c(fighter1[1].m() + bVal2);
        fighter1[1].c(fighter1[0].m() + bVal2);
    }

    private BattleActor g(int num1, int num2) {
        return (BattleActor)(num1 < 6 ? new HumanPlayerActor(this, num2) : new AiPlayerActor(this, num2));
    }

    private void w() {
        this.activeMatchCount = 0;
        int num1 = 0;
        int num2 = 0;
        boolean flag3 = false;
        boolean flag4 = false;
        int num5 = 0;
        int num6 = this.pendingSwapMove.a;
        int num7 = this.pendingSwapMove.b;
        num1 = this.pendingSwapMove.e[0];
        flag3 = (num1 & 255) >= 3;
        if (flag3) {
            num5 += num1 & 255;
        }

        num2 = this.pendingSwapMove.e[1];
        flag4 = (num2 & 255) >= 3;
        if (flag4) {
            num5 += num2 & 255;
        }

        if (flag3) {
            this.a(this.battleModel.a(num6, num7), num6, num7 - ((num1 & 16711680) >> 16), 1, num1 & 255, num5);
        }

        if (flag4) {
            this.a(this.battleModel.a(num6, num7), num6 - ((num2 & 16711680) >> 16), num7, num2 & 255, 1, num5);
        }

        num1 = 0;
        num2 = 0;
        flag3 = false;
        flag4 = false;
        num5 = 0;
        num6 = this.pendingSwapMove.c;
        num7 = this.pendingSwapMove.d;
        num1 = this.pendingSwapMove.e[2];
        flag3 = (num1 & 255) >= 3;
        if (flag3) {
            num5 += num1 & 255;
        }

        num2 = this.pendingSwapMove.e[3];
        flag4 = (num2 & 255) >= 3;
        if (flag4) {
            num5 += num2 & 255;
        }

        if (flag3) {
            this.a(this.battleModel.a(num6, num7), num6, num7 - ((num1 & 16711680) >> 16), 1, num1 & 255, num5);
        }

        if (flag4) {
            this.a(this.battleModel.a(num6, num7), num6 - ((num2 & 16711680) >> 16), num7, num2 & 255, 1, num5);
        }

    }

    public boolean isAnimatingOrAiTurn() {
        return this.battleState != 8 && this.battleState != 14;
    }

    public void c() {
        if (this.animWaitTicks > 0) {
            --this.animWaitTicks;
        } else {
            switch (this.battleState) {
                case 0:
                    if (this.battleModel.d) {
                        this.f(13);
                    } else {
                        this.f(6);
                    }
                    break;
                case 1:
                    this.w();
                    if (this.activeMatchCount > 0) {
                        this.f(3);
                    } else {
                        this.f(2);
                    }
                    break;
                case 2:
                    this.f(6);
                    break;
                case 3:
                    this.x();
                    this.f(4);
                    break;
                case 4:
                    this.f(5);
                    break;
                case 5:
                    this.f(3);
                case 6:
                case 13:
                default:
                    break;
                case 7:
                    if (this.y()) {
                        return;
                    }
                    this.stageView.c(this.battleModel.a());
                    this.stageView.c(false);
                    this.f(8);
                    if (this.battleModel.a() == 0) {
                        this.stageView.f();
                    }
                    break;
                case 8:
                    if (this.y()) {
                        return;
                    }

                    int num5 = this.battleModel.a();
                    this.turnActors[num5].b();
                    if (num5 == 0 && this.availableMoves != null) {
                        ++this.activeSkillId;
                        if (this.activeSkillId == 250) {
                            SwapMove move3 = (SwapMove)this.availableMoves.b(RandomUtils.nextInt(this.availableMoves.b()));
                            this.stageView.b(move3);
                        }
                    }
                    break;
                case 9:
                    this.f(12);
                    this.animWaitTicks = this.stageView.b(this.earnedExtraTurn);
                    if (this.earnedExtraTurn) {
                        BattleResourceManager.a().d();
                    } else {
                        BattleResourceManager.a().e();
                    }

                    return;
                case 10:
                    this.f(0);
                    break;
                case 11:
                    this.f(3);
                    break;
                case 12:
                    this.battleModel.b(0).i();
                    CampaignData.i = this.battleModel.b(0).f();
                    CampaignData.j = this.battleModel.b(0).g();
                    if (this.earnedExtraTurn) {
                        if (this.battleModel.isBossBoard() && CampaignData.a < 0) {
                            // Thưởng Chiến Công cộng thêm khi hạ gục Boss cốt truyện / nhiệm vụ phụ
                            int bossLv = this.battleModel.b(1).getLevel();
                            CampaignData.i += bossLv * 4 + 20;
                            CampaignData.j += bossLv * 8 + 40;
                        }
                        CampaignData.battleDroppedShards = this.pendingBattleShards;
                        CampaignData.battleWinBonusShards = 0;
                        if (!this.battleModel.d && this.isBattleShardEligible()) {
                            int enemyId = this.battleModel.b(1).b();
                            boolean isCampaignBoss = (CampaignData.a < 0) && CampaignData.isBossTemplate(enemyId);
                            int winDropChance = isCampaignBoss ? 50 : 15;
                            if (RandomUtils.nextInt(100) < winDropChance) {
                                CampaignData.battleWinBonusShards = 1;
                            }
                        }
                        BaseDialog dialog1 = CampaignData.a(false);
                        ((Window)dialog1).c((SoftKeyButton)(new TextSoftKeyButton("Đóng", -5)));
                        ((Window)dialog1).a((ActionListener)this);
                        GUIManager.getInstance().a(dialog1, true);
                    } else {
                        CampaignData.battleDroppedShards = 0;
                        CampaignData.battleWinBonusShards = 0;
                        BaseDialog dialog4 = CampaignData.g();
                        if (CampaignData.a < 0) {
                            ((Window)dialog4).a((SoftKeyButton)(new TextSoftKeyButton("Có", -3)));
                            ((Window)dialog4).b(new TextSoftKeyButton("Không", -4));
                        } else {
                            ((Window)dialog4).c((SoftKeyButton)(new TextSoftKeyButton("Trở về", -4)));
                        }

                        ((Window)dialog4).a((ActionListener)this);
                        GUIManager.getInstance().a(dialog4, true);
                    }
                    break;
                case 14:
                    if (!this.stageView.g.m()) {
                        this.f(13);
                    }
            }

        }
    }

    private void a(BattleFighter fighter1, int num2) {
        fighter1.k(num2);
    }

    private void b(BattleFighter fighter1, int num2) {
        fighter1.l(num2);
    }

    private void applySwordHit(int attackerIdx, int targetIdx, int swordDmg, boolean isHeavySwordHit) {
        BattleFighter attacker = this.battleModel.b(attackerIdx);
        BattleFighter target = this.battleModel.b(targetIdx);
        int finalSwordDmg = swordDmg;
        if (!this.battleModel.d
                && attacker.getPassiveId() == BattleFighter.PASSIVE_FROST_HEART
                && (target.isArmorBroken() || this.frozenTurnsRemaining[targetIdx] > 0)) {
            // Băng Tâm Toái Sát: chém mục tiêu đang dính Hàn Băng / Đóng Băng gây thêm +15% sát thương
            finalSwordDmg = finalSwordDmg * 115 / 100;
        }
        target.applySwordAttackDamage(finalSwordDmg, isHeavySwordHit);
        if (!this.battleModel.d && isHeavySwordHit && !target.k() && target.r() > 0) {
            if (attacker.getPassiveId() == BattleFighter.PASSIVE_VENOM_BODY) {
                int poisonDmg = attacker.getLevel() / 3 + 2;
                target.applyPoison(2, poisonDmg < 3 ? 3 : poisonDmg);
            } else if (attacker.getPassiveId() == BattleFighter.PASSIVE_FROST_HEART) {
                target.applyArmorBreak(2);
            }
        }
        if (isHeavySwordHit) {
            GUIManager.shakeScreen(4, 2);
        }
        this.checkBossThresholdAwakening(targetIdx);
    }

    private void burnOpponentFood(BattleFighter target, int targetIdx, int amount, boolean pierceHalfThroughShield) {
        if (this.battleModel.d || target == null || amount <= 0 || target.r() <= 0) {
            return;
        }
        int effective = amount;
        if (target.k()) {
            if (!pierceHalfThroughShield) {
                return;
            }
            effective = amount / 2;
        }
        if (effective <= 0 || target.p() <= 0) {
            return;
        }
        int before = target.p();
        target.j(effective);
        int actualBurned = before - target.p();
        if (actualBurned > 0 && this.stageView != null) {
            this.stageView.showFoodBurnPopup(targetIdx, actualBurned);
        }
    }

    private int countGemTypeInRect(int[][] board, int startRow, int startCol, int size, int gemType) {
        int count = 0;
        for (int r = startRow; r < startRow + size; ++r) {
            for (int c = startCol; c < startCol + size; ++c) {
                if (board[r][c] == gemType) {
                    ++count;
                }
            }
        }
        return count;
    }

    /** Forges {@code count} Fiery Swords (6) onto random safe tiles on the 8x8 board and updates their sprites. */
    private void forgeRandomFierySwordsOnBoard(int count) {
        if (count <= 0 || this.battleModel.d) {
            return;
        }
        int[][] board = this.battleModel.e();
        int forged = 0;
        for (int pass = 0; pass < 2 && forged < count; ++pass) {
            for (int attempt = 0; attempt < 24 && forged < count; ++attempt) {
                int r = 2 + RandomUtils.nextInt(8);
                int c = 2 + RandomUtils.nextInt(8);
                int cur = board[r][c];
                if (cur == 6 || cur >= 7) {
                    continue;
                }
                if (pass == 0 && cur != 0) {
                    continue;
                }
                board[r][c] = 6;
                this.stageView.a(r, c, 6);
                ++forged;
            }
        }
    }

    /**
     * Places up to {@code count} periodic Boss Hazard Seals (capped at max 2 active seals on the 8x8 board)
     * overlaid onto normal gems, preferring neutral Vàng (4) and Sách EXP (5) tiles.
     */
    private int placeBossSeals(int count, int turns) {
        if (count <= 0 || turns <= 0 || this.battleModel.d) {
            return 0;
        }
        int room = 2 - this.battleModel.countActiveBossSeals();
        if (room <= 0) {
            return 0;
        }
        int targetCount = count > room ? room : count;
        int[][] board = this.battleModel.e();
        int placed = 0;
        for (int pass = 0; pass < 2 && placed < targetCount; ++pass) {
            for (int attempt = 0; attempt < 36 && placed < targetCount; ++attempt) {
                int r = 2 + RandomUtils.nextInt(8);
                int c = 2 + RandomUtils.nextInt(8);
                int cur = board[r][c];
                if (cur < 0 || cur > 5 || this.battleModel.getBossSealTurns(r, c) > 0) {
                    continue;
                }
                if (pass == 0 && cur != 4 && cur != 5) {
                    continue;
                }
                this.battleModel.setBossSealTurns(r, c, turns);
                ++placed;
            }
        }
        if (placed > 0 && this.stageView != null) {
            this.stageView.syncBossSeals();
        }
        return placed;
    }

    /** Checks and triggers Overlord Phase-2 awakening (<= 50% MaxHP for Đinh Bộ Lĩnh & Ấu Rồng). */
    private void checkBossThresholdAwakening(int fighterIndex) {
        if (this.battleModel.d || fighterIndex < 0 || fighterIndex > 1) {
            return;
        }
        BattleFighter boss = this.battleModel.b(fighterIndex);
        if (!boss.isBossFighter() || boss.r() <= 0 || boss.s() <= 0 || boss.isPhase2Triggered()) {
            return;
        }
        int hpPct = boss.r() * 100 / boss.s();
        if (boss.getBossArchetype() == BattleFighter.BOSS_ARCHETYPE_OVERLORD_PHASE && hpPct <= 50) {
            boss.setPhase2Triggered(true);
            boss.addBarrier(boss.s() * 15 / 100);
            boss.f(10);
            this.placeBossSeals(2, 3);
            GUIManager.shakeScreen(8, 3);
            this.stageView.showSkillBanner("PHA 2 LONG NỘ: +15% GIÁP & ĐẶT 2 ẤN!", fighterIndex == 0, true);
        }
    }

    /**
     * Ticks/detonates existing Boss Seals and periodically places 1-2 new Boss Seals every 4 Boss turns
     * (plus 1 introductory seal on the Boss's first turn).
     */
    private void triggerBossTurnStartMechanics(int activeIndex) {
        if (this.battleModel.d || activeIndex < 0 || activeIndex > 1) {
            return;
        }
        BattleFighter boss = this.battleModel.b(activeIndex);
        if (!boss.isBossFighter() || boss.r() <= 0) {
            return;
        }
        int oppIdx = (activeIndex + 1) % 2;
        BattleFighter opp = this.battleModel.b(oppIdx);
        int arch = boss.getBossArchetype();
        boolean selfTargetSeal = (arch == BattleFighter.BOSS_ARCHETYPE_FORTRESS
                || arch == BattleFighter.BOSS_ARCHETYPE_ARCHMAGE);

        // 1. Tick down active Boss Seals; any seal at 1 turn detonates now!
        int detonatedCount = 0;
        for (int r = 2; r < 10; ++r) {
            for (int c = 2; c < 10; ++c) {
                int st = this.battleModel.getBossSealTurns(r, c);
                if (st > 1) {
                    this.battleModel.setBossSealTurns(r, c, st - 1);
                } else if (st == 1) {
                    this.battleModel.setBossSealTurns(r, c, 0);
                    ++detonatedCount;
                    if (this.stageView != null) {
                        this.stageView.spawnSealSpark(
                                r, c, selfTargetSeal ? activeIndex : oppIdx, selfTargetSeal ? 3 : 2);
                    }
                }
            }
        }

        if (detonatedCount > 0) {
            GUIManager.shakeScreen(6, 3);
            switch (arch) {
                case BattleFighter.BOSS_ARCHETYPE_BERSERKER: {
                    int strikeDmg = boss.m() * 5 / 4 * detonatedCount;
                    if (strikeDmg < 4) {
                        strikeDmg = 4;
                    }
                    opp.k(strikeDmg);
                    this.forgeRandomFierySwordsOnBoard(detonatedCount);
                    this.stageView.showSkillBanner("ẤN TRẢM KÍCH NỔ: SÁT THƯƠNG + KIẾM LỬA!", activeIndex == 0, false);
                    break;
                }
                case BattleFighter.BOSS_ARCHETYPE_FORTRESS: {
                    boss.addBarrier(boss.s() * 12 * detonatedCount / 100);
                    this.stageView.showSkillBanner("ẤN THÀNH ĐỒNG NỔ: DỰNG GIÁP HỘ THỂ!", activeIndex == 0, false);
                    break;
                }
                case BattleFighter.BOSS_ARCHETYPE_CONTROLLER: {
                    if (!opp.k()) {
                        opp.a(6 * detonatedCount, false);
                        boss.f(4 * detonatedCount);
                        opp.applyCurse(2);
                        if (boss.b() == 73 || boss.b() == 47) {
                            int pDmg = boss.getLevel() / 2 + 3;
                            opp.applyPoison(2, pDmg < 4 ? 4 : pDmg);
                        }
                    }
                    this.stageView.showSkillBanner("ẤN ÁP CHẾ NỔ: RÚT NỘ & NHIỄU HỒN!", activeIndex == 0, false);
                    break;
                }
                case BattleFighter.BOSS_ARCHETYPE_ARCHMAGE: {
                    boss.f(10 * detonatedCount);
                    this.stageView.showSkillBanner("ẤN TỤ LÔI NỔ: BOSS NẠP +" + (10 * detonatedCount) + " NỘ!", activeIndex == 0, false);
                    break;
                }
                case BattleFighter.BOSS_ARCHETYPE_WARLORD_FIRE: {
                    if (!opp.k()) {
                        this.burnOpponentFood(opp, oppIdx, 120 * detonatedCount, false);
                        int bDmg = boss.getLevel() / 2 + 2;
                        opp.applyBurn(2, bDmg < 3 ? 3 : bDmg);
                    }
                    boss.i(90 * detonatedCount);
                    this.stageView.showSkillBanner("ẤN BINH HỎA NỔ: ĐỐT LƯƠNG & GÂY BỎNG!", activeIndex == 0, false);
                    break;
                }
                case BattleFighter.BOSS_ARCHETYPE_OVERLORD_PHASE: {
                    int drainDmg = boss.m() * 11 / 10 * detonatedCount;
                    if (!opp.k()) {
                        opp.k(drainDmg < 4 ? 4 : drainDmg);
                    }
                    int healAmt = boss.s() * 5 * detonatedCount / 100;
                    if (healAmt < 3) {
                        healAmt = 3;
                    }
                    boss.l(healAmt);
                    this.stageView.showSkillBanner("ẤN HẤP HUYẾT NỔ: HÚT SINH LỰC!", activeIndex == 0, false);
                    break;
                }
                default:
                    break;
            }
        }

        // 2. Place 1-2 new Boss Seals on turn 1 and every 4 Boss turns thereafter (if player still alive).
        int cycle = boss.incrementBossCycleTurn();
        if (opp.r() > 0 && (cycle == 1 || (cycle > 1 && cycle % 4 == 0))) {
            int want = (cycle > 1 && (boss.getLevel() >= 55 || boss.isPhase2Triggered())) ? 2 : 1;
            int placed = this.placeBossSeals(want, 3);
            if (placed > 0 && detonatedCount == 0) {
                this.stageView.showSkillBanner(
                        "BOSS ĐẶT " + placed + " ẤN: PHÁ TRONG 3 LƯỢT (+NỘ)!", activeIndex == 0, false);
            }
        }

        this.battleModel.refreshBossImminentFlag();
        if (this.stageView != null) {
            this.stageView.syncBossSeals();
        }
    }

    private void x() {
        int num1 = this.battleModel.a();
        BattleFighter[] fighter2 = this.battleModel.d();
        boolean chainScaled = this.resolvingChainLightning && this.cascadeComboCount == 1;

        for(int num3 = 0; num3 < this.activeMatchCount; ++num3) {
            GemMatch match4 = this.activeMatches[num3];
            int num5 = match4.d > match4.c ? match4.d : match4.c;
            int num6 = num5 * num5 - 2 * num5;
            if (num6 < 0) {
                num6 = 1;
            }

            int actorLevel = fighter2[num1].getLevel();
            switch (BattleConstants.i[match4.e]) {
                case 0:
                    int num8 = num1 == 1 ? 0 : 1;
                    int swordDmg;
                    if (chainScaled) {
                        // Chuỗi Sét: mỗi ô Kiếm bị giật nổ gây 35% Công (tránh 1-hit lượt 1)
                        swordDmg = fighter2[num1].m() * 35 / 100;
                        if (swordDmg < 1) {
                            swordDmg = 1;
                        }
                    } else {
                        int scaledAtk = fighter2[num1].m();
                        if (num5 == 4) {
                            scaledAtk = scaledAtk * 135 / 100;
                        } else if (num5 >= 5) {
                            scaledAtk = scaledAtk * 175 / 100;
                        }
                        swordDmg = num6 * this.cascadeComboCount + scaledAtk;
                        if (this.fierySwordDetonatedInWave && fighter2[num1].getPassiveId() == BattleFighter.PASSIVE_FIRE_SOUL) {
                            swordDmg = swordDmg * 120 / 100;
                        }
                    }
                    boolean heavySwordHit = !chainScaled && (num5 >= 4 || this.fierySwordDetonatedInWave);
                    this.applySwordHit(num1, num8, swordDmg, heavySwordHit);
                    if (this.battleModel.d && num1 == 0 && fighter2[num1].r() <= 0) {
                        fighter2[num1].m(1);
                    }
                    break;
                case 1:
                    int baseFoodGain;
                    if (chainScaled) {
                        baseFoodGain = this.battleModel.c / 2; // 110 Food per chained Food tile
                    } else {
                        baseFoodGain = this.battleModel.c; // 220 for 3-match
                        if (num5 == 4) {
                            baseFoodGain = 350;
                        } else if (num5 >= 5) {
                            baseFoodGain = 500;
                        }
                    }
                    int cascadeBonus = this.cascadeComboCount > 1 ? (baseFoodGain * (this.cascadeComboCount - 1) / 4) : 0;
                    int totalFoodGain = baseFoodGain + cascadeBonus;
                    if (fighter2[num1].getBurnTurns() > 0 || fighter2[num1].isWounded()) {
                        // Bị Bỏng / Trọng Thương: giảm 40% lượng Lương hồi phục từ Đùi Gà
                        totalFoodGain = totalFoodGain * 60 / 100;
                    }
                    fighter2[num1].i(totalFoodGain);
                    if (!chainScaled && num5 >= 4) {
                        int oppIdx = num1 == 1 ? 0 : 1;
                        int stealFood = num5 >= 5 ? 200 : 120;
                        this.burnOpponentFood(fighter2[oppIdx], oppIdx, stealFood, false);
                    }
                    break;
                case 2:
                    int heartHeal;
                    if (chainScaled) {
                        // Chuỗi Sét: mỗi ô Tim bị giật nổ hồi 25% Công
                        heartHeal = fighter2[num1].m() * 25 / 100;
                        if (heartHeal < 2) {
                            heartHeal = 2;
                        }
                    } else {
                        int ownHalfAtk = fighter2[num1].m() / 2;
                        int heartBase = ownHalfAtk > this.battleModel.b ? ownHalfAtk : this.battleModel.b;
                        heartHeal = num6 * this.cascadeComboCount + heartBase;
                    }
                    int relicHeartPct = fighter2[num1].getRelicHeartBonusPct();
                    if (relicHeartPct > 0) {
                        heartHeal = heartHeal * (100 + relicHeartPct) / 100;
                    }
                    if (fighter2[num1].isPoisoned()) {
                        // Trúng Độc: giảm 30% lượng hồi máu từ ô Tim
                        heartHeal = heartHeal * 70 / 100;
                        if (heartHeal < 1) {
                            heartHeal = 1;
                        }
                    }
                    fighter2[num1].healWithFrostBarrierOverflow(heartHeal);
                    break;
                case 3:
                    int num9 = fighter2[num1].o() / 12;
                    int manaGain;
                    if (chainScaled) {
                        // Chuỗi Sét: giật ô Nộ nhận tối đa +3 Nộ (giảm còn 2 nếu Nhiễu Hồn hoặc Thiếu Lương)
                        int manaCap = (fighter2[num1].isCursed() || fighter2[num1].isLowFood()) ? 2 : 3;
                        if (this.chainLightningManaAccum < manaCap) {
                            manaGain = 1;
                            ++this.chainLightningManaAccum;
                        } else {
                            manaGain = 0;
                        }
                    } else {
                        manaGain = num6 * this.cascadeComboCount;
                        if (fighter2[num1].isCursed()) {
                            // Nhiễu Hồn: giảm 40% lượng Nộ nhận từ ô Nộ
                            manaGain = manaGain * 60 / 100;
                            if (manaGain < 1) {
                                manaGain = 1;
                            }
                        }
                        if (!this.battleModel.d && fighter2[num1].isLowFood() && manaGain > 0) {
                            // Thiếu Lương (<= 25%): giảm 25% lượng Nộ nhận từ ngọc
                            manaGain = manaGain * 75 / 100;
                            if (manaGain < 1) {
                                manaGain = 1;
                            }
                        }
                    }
                    if (manaGain > 0) {
                        fighter2[num1].f(manaGain);
                    }
                    if (num1 == 0 && fighter2[num1].o() / 12 != num9) {
                        this.stageView.m();
                    }
                    break;
                case 4:
                    fighter2[num1].a((num6 + actorLevel / 4) * this.cascadeComboCount);
                    break;
                case 5:
                    fighter2[num1].b((num6 + actorLevel / 2) * this.cascadeComboCount);
                    break;
            }
        }

        if (this.resolvingChainLightning) {
            this.resolvingChainLightning = false;
            this.chainLightningManaAccum = 0;
        }
    }

    private void f(int num1) {
        this.stageView.setPlayerTurnInteractive(!this.battleModel.d && num1 == 8 && this.battleModel.a() == 0);
        this.stageView.a();
        this.stageView.h();
        this.stageView.i();
        switch (num1) {
            case 0:
                this.stageView.c(true);
                this.battleModel.j();
                this.animWaitTicks = this.stageView.k();
                break;
            case 1:
                this.stageView.a(this.pendingSwapMove.a, this.pendingSwapMove.b, this.pendingSwapMove.c, this.pendingSwapMove.d, this.battleModel.a(this.pendingSwapMove.a, this.pendingSwapMove.b), this.battleModel.a(this.pendingSwapMove.c, this.pendingSwapMove.d));
                this.animWaitTicks = 10;
                this.battleModel.b(this.pendingSwapMove.a, this.pendingSwapMove.b, this.pendingSwapMove.c, this.pendingSwapMove.d);
                this.battleModel.a(false);
                break;
            case 2:
                this.stageView.a(this.pendingSwapMove.a, this.pendingSwapMove.b, this.pendingSwapMove.c, this.pendingSwapMove.d, this.battleModel.a(this.pendingSwapMove.a, this.pendingSwapMove.b), this.battleModel.a(this.pendingSwapMove.c, this.pendingSwapMove.d));
                this.battleModel.b(this.pendingSwapMove.a, this.pendingSwapMove.b, this.pendingSwapMove.c, this.pendingSwapMove.d);
                this.animWaitTicks = 10;
                BattleFighter fighter22 = this.battleModel.b(this.battleModel.a());
                int misSwapPenalty = fighter22.s() * 5 / 100;
                if (misSwapPenalty < 3) {
                    misSwapPenalty = 3;
                }
                this.a(fighter22, misSwapPenalty);
                this.stageView.b(this.battleModel.a(), 0, 3);
                break;
            case 3:
                if (this.activeMatchCount <= 0) {
                    if (this.battleModel.d) {
                        this.stageView.g.c();
                        this.stageView.g.a();
                        this.f(10);
                        return;
                    }

                    this.f(6);
                    return;
                }

                this.animWaitTicks = 0;
                if (this.activeMatchCount > 0) {
                    ++this.cascadeComboCount;
                }

                int[][] num7 = this.battleModel.e();
                int num8 = this.activeMatchCount;
                int num24 = this.battleModel.a();
                int defusedSeals = 0;

                for(int num9 = 0; num9 < num8; ++num9) {
                    GemMatch match10 = this.activeMatches[num9];

                    for(int num11 = match10.a + match10.c - 1; num11 >= match10.a; --num11) {
                        for(int num12 = match10.b + match10.d - 1; num12 >= match10.b; --num12) {
                            if (num7[num11][num12] == 6) {
                                this.b(6, num11, num12, 1, 1, 1);
                            }
                            if (this.battleModel.getBossSealTurns(num11, num12) > 0) {
                                this.battleModel.setBossSealTurns(num11, num12, 0);
                                ++defusedSeals;
                                if (num24 == 0 && this.stageView != null) {
                                    this.stageView.spawnSealSpark(num11, num12, 0, 3);
                                }
                            }

                            num7[num11][num12] = 7;
                        }
                    }
                }

                boolean fierySwordDetonated = false;
                int fieryFoodBurnedTiles = 0;
                for(int num23 = num8; num23 < this.activeMatchCount; ++num23) {
                    GemMatch match25 = this.activeMatches[num23];
                    if (match25.e == 6) {
                        fierySwordDetonated = true;
                        for(int num27 = 0; num27 < this.neighborRowOffsets.length; ++num27) {
                            int num29 = match25.a + this.neighborRowOffsets[num27];
                            int num13 = match25.b + this.neighborColOffsets[num27];
                            if (num7[num29][num13] < 7) {
                                if (num7[num29][num13] == 1) {
                                    ++fieryFoodBurnedTiles;
                                }
                                this.b(num7[num29][num13], num29, num13, 1, 1, 1);
                                if (this.battleModel.getBossSealTurns(num29, num13) > 0) {
                                    this.battleModel.setBossSealTurns(num29, num13, 0);
                                    ++defusedSeals;
                                    if (num24 == 0 && this.stageView != null) {
                                        this.stageView.spawnSealSpark(num29, num13, 0, 3);
                                    }
                                }
                                num7[num29][num13] = 7;
                            }
                        }

                        match25.e = 0;
                    }

                    if (this.battleModel.getBossSealTurns(match25.a, match25.b) > 0) {
                        this.battleModel.setBossSealTurns(match25.a, match25.b, 0);
                        ++defusedSeals;
                        if (num24 == 0 && this.stageView != null) {
                            this.stageView.spawnSealSpark(match25.a, match25.b, 0, 3);
                        }
                    }
                    num7[match25.a][match25.b] = 7;
                }
                this.fierySwordDetonatedInWave = fierySwordDetonated;
                if (fierySwordDetonated) {
                    GUIManager.shakeScreen(5, 2);
                }
                if (!this.battleModel.d && fieryFoodBurnedTiles > 0) {
                    int oppIdx = num24 == 1 ? 0 : 1;
                    int foodBurn = fieryFoodBurnedTiles * 60;
                    if (foodBurn > 240) {
                        foodBurn = 240;
                    }
                    this.burnOpponentFood(this.battleModel.b(oppIdx), oppIdx, foodBurn, false);
                }
                if (defusedSeals > 0) {
                    if (this.stageView != null) {
                        this.stageView.syncBossSeals();
                    }
                    if (num24 == 0 && !this.battleModel.d) {
                        BattleFighter player = this.battleModel.b(0);
                        BattleFighter boss = this.battleModel.b(1);
                        int prevSkillTier = player.o() / 12;
                        int manaReward = 6 * defusedSeals;
                        int healReward = player.s() * 5 * defusedSeals / 100;
                        if (healReward < 3) {
                            healReward = 3;
                        }
                        player.f(manaReward);
                        this.b(player, healReward);

                        String bonusTag = "";
                        switch (boss.getBossArchetype()) {
                            case BattleFighter.BOSS_ARCHETYPE_BERSERKER:
                                this.pendingForgedFierySwords += defusedSeals;
                                bonusTag = " + KIẾM LỬA";
                                break;
                            case BattleFighter.BOSS_ARCHETYPE_FORTRESS: {
                                int shatterAmt = boss.s() * 6 * defusedSeals / 100;
                                int removed = boss.shatterBarrier(shatterAmt < 4 ? 4 : shatterAmt);
                                if (removed > 0) {
                                    bonusTag = " (-" + removed + " GIÁP)";
                                }
                                break;
                            }
                            case BattleFighter.BOSS_ARCHETYPE_CONTROLLER:
                                for (int d = 0; d < defusedSeals; ++d) {
                                    player.reduceDebuffsByOneTurn();
                                }
                                bonusTag = " & GIẢI BẤT LỢI";
                                break;
                            case BattleFighter.BOSS_ARCHETYPE_ARCHMAGE:
                                boss.a(4 * defusedSeals, true);
                                bonusTag = " (-" + (4 * defusedSeals) + " NỘ BOSS)";
                                break;
                            case BattleFighter.BOSS_ARCHETYPE_WARLORD_FIRE:
                                player.i(160 * defusedSeals);
                                bonusTag = " (+" + (160 * defusedSeals) + " LƯƠNG)";
                                break;
                            case BattleFighter.BOSS_ARCHETYPE_OVERLORD_PHASE: {
                                int counterDmg = (player.m() / 2) * defusedSeals;
                                if (counterDmg < 4) {
                                    counterDmg = 4;
                                }
                                boss.k(counterDmg);
                                this.checkBossThresholdAwakening(1);
                                bonusTag = " (PHẢN " + counterDmg + " ST)";
                                break;
                            }
                            default:
                                break;
                        }

                        if (player.o() / 12 != prevSkillTier && this.stageView != null) {
                            this.stageView.m();
                        }
                        GUIManager.shakeScreen(4, 2);
                        this.stageView.showSkillBanner(
                                "PHÁ ẤN: +" + manaReward + " NỘ & +" + healReward + " MÁU" + bonusTag + "!", true, true);
                    }
                }

                boolean crossRewardRolled = false;
                boolean hasBigMatchThisWave = false;

                for(int num26 = 0; num26 < this.activeMatchCount; ++num26) {
                    GemMatch match28 = this.activeMatches[num26];
                    int num30 = 0;
                    num30 = this.stageView.a(match28, this.battleModel.a());
                    if (num30 > this.animWaitTicks) {
                        this.animWaitTicks = num30;
                    }

                    if (this.cascadeComboCount > 1) {
                        this.stageView.b(this.cascadeComboCount, match28.a, match28.b, match28.c, match28.d);
                    }

                    int matchLen32 = match28.d > match28.c ? match28.d : match28.c;
                    if (matchLen32 >= 4 || match28.f >= 5) {
                        hasBigMatchThisWave = true;
                    }
                    if (!this.battleModel.d && BattleConstants.i[match28.e] == 0 && matchLen32 >= 4) {
                        ++this.pendingForgedFierySwords;
                    }

                    if (num24 == 0) {
                        int centerR = match28.a + match28.c / 2;
                        int centerC = match28.b + match28.d / 2;
                        if (matchLen32 >= 5) {
                            int num14 = this.g(matchLen32);
                            if (num14 >= 0) {
                                this.stageView.d(num14, centerR, centerC);
                            }
                            this.tryDropBoardShard(matchLen32 >= 6 ? 25 : 15, "RƠI +1 MẢNH CƯỜNG HÓA!");
                        } else if (!crossRewardRolled && !this.battleModel.d && match28.f >= 5) {
                            // L/T intersection match (>= 5 cells) — rolled once per wave
                            crossRewardRolled = true;
                            int crossItem = this.rollRandomSupportItem(20);
                            if (crossItem >= 0) {
                                this.stageView.d(crossItem, centerR, centerC);
                            }
                            this.tryDropBoardShard(12, "CHỮ L/T: +1 MẢNH CƯỜNG HÓA!");
                        }
                    }
                }

                // Player elemental passive board interactions (Gói A)
                if (num24 == 0 && !this.battleModel.d) {
                    BattleFighter p0 = this.battleModel.b(0);
                    if (hasBigMatchThisWave && p0.getPassiveId() == BattleFighter.PASSIVE_THUNDER_AURA) {
                        int prevTier = p0.o() / 12;
                        p0.f(2);
                        if (p0.o() / 12 != prevTier && this.stageView != null) {
                            this.stageView.m();
                        }
                    }
                    if (this.cascadeComboCount >= 3
                            && !this.fireComboSwordForgedThisTurn
                            && p0.getPassiveId() == BattleFighter.PASSIVE_FIRE_SOUL
                            && this.activeMatchCount > 0) {
                        this.fireComboSwordForgedThisTurn = true;
                        ++this.pendingForgedFierySwords;
                        this.stageView.showSkillBanner(
                                "HỎA LINH COMBO x" + this.cascadeComboCount + ": RÈN 1 KIẾM LỬA!", true, true);
                    }
                }

                // Cascade combo milestone rewards (x4 and x5+) — works for both manual swaps and player skills!
                if (num24 == 0 && !this.battleModel.d && this.cascadeComboCount >= 4 && this.activeMatchCount > 0) {
                    GemMatch anchor = this.activeMatches[0];
                    int anchorR = anchor.a + anchor.c / 2;
                    int anchorC = anchor.b + anchor.d / 2;
                    int itemChance = (this.cascadeComboCount >= 5) ? 35 : 20;
                    int shardChance = (this.cascadeComboCount >= 5) ? 30 : 15;
                    int comboItem = this.rollRandomSupportItem(itemChance);
                    if (comboItem >= 0) {
                        this.stageView.d(comboItem, anchorR, anchorC);
                    }
                    this.tryDropBoardShard(shardChance, "COMBO x" + this.cascadeComboCount + ": +1 MẢNH CƯỜNG HÓA!");
                }

                BattleResourceManager.a().g();
                break;
            case 4:
                this.e();
                break;
            case 5:
                this.f();
                break;
            case 6:
                if (this.y()) {
                    return;
                }

                this.availableMoves = this.battleModel.k();
                if (this.availableMoves.b() <= 0) {
                    this.f(10);
                } else {
                    this.f(7);
                }

                return;
            case 7:
                this.activeSkillId = 0;

                for(int num15 = 0; num15 < 2; ++num15) {
                    if (this.frozenTurnsRemaining[num15] > 0) {
                        int numTmp2 = this.frozenTurnsRemaining[num15]--;
                        if (this.frozenTurnsRemaining[num15] == 0) {
                            this.animWaitTicks = 8;
                            this.stageView.a(num15);
                        }
                    }

                    if (this.turnCounter <= 0 && this.shieldTurnsRemaining[num15] > 0) {
                        int num33 = this.shieldTurnsRemaining[num15]--;
                        BattleFighter fighter19 = this.battleModel.b(num15);
                        if (this.shieldTurnsRemaining[num15] == 0) {
                            fighter19.a(false);
                            this.stageView.b(num15);
                        } else {
                            fighter19.setShieldTurnsDisplay((this.shieldTurnsRemaining[num15] + 1) >> 1);
                        }
                    }
                }

                this.itemUsedThisTurn = false;
                ItemSlot[] slot16 = this.battleModel.b(0).a;
                ItemSlot[] slot20 = this.battleModel.b(1).a;

                for(int num6 = 0; num6 < slot16.length; ++num6) {
                    slot16[num6].a();
                    slot20[num6].a();
                }

                if (slot16[3].b < 1) {
                    this.stageView.e(false);
                }

                if (slot16[0].b < 1) {
                    this.stageView.d(false);
                }

                this.battleModel.a(false);
                boolean switchedTurn = false;
                if (this.turnCounter <= 0) {
                    this.turnCounter = 0;
                    int num21 = (this.battleModel.a() + 1) % this.turnActors.length;
                    this.battleModel.a(num21);
                    this.animWaitTicks = 8;
                    switchedTurn = true;
                } else {
                    if (this.turnCounter > 1 || this.bonusTurnBannerCount > 1) {
                        this.stageView.c(this.turnCounter, 6, 6);
                    }

                    this.bonusTurnBannerCount = this.turnCounter--;
                }

                int currentActor = this.battleModel.a();
                this.battleModel.b(currentActor).processTurnStartEffects();
                this.checkBossThresholdAwakening(currentActor);
                if (switchedTurn) {
                    this.triggerBossTurnStartMechanics(currentActor);
                }
                this.cascadeComboCount = 0;
                this.resolvingChainLightning = false;
                this.chainLightningManaAccum = 0;
                this.fireComboSwordForgedThisTurn = false;
                break;
            case 8:
                if (this.battleModel.a() == 0) {
                    this.stageView.b();
                }

                this.turnActors[this.battleModel.a()].a();
                break;
            case 9:
                this.animWaitTicks = 20;
                this.stageView.c(true);
                break;
            case 10:
                this.stageView.c(true);
                this.animWaitTicks = this.stageView.j();
            case 11:
            case 12:
            default:
                break;
            case 13:
                this.cascadeComboCount = 0;
                this.stageView.c();
                this.stageView.a((String)"Bỏ qua", 2);
                int[] num2 = this.stageView.g.b();
                this.battleModel.c(num2[0]);
                this.battleModel.d(num2[1]);
                BattleTutorialOverlay val3 = this.stageView.g;
                if (val3 != null) {
                    int num4 = val3.g();
                    switch (num4) {
                        case 0:
                            val3.d();
                            this.f(14);
                            val3.c();
                            break;
                        case 1:
                            SwapMove move18 = new SwapMove();
                            move18.a = num2[0];
                            move18.b = num2[1];
                            move18.c = num2[2];
                            move18.d = num2[3];
                            this.availableMoves = new ObjectList(1);
                            this.availableMoves.a(move18);
                            this.turnCounter = 1;
                            this.battleModel.a(0);
                            this.f(7);
                            this.activeSkillId = 249;
                            break;
                        case 2:
                            SwapMove move17 = new SwapMove();
                            move17.a = num2[0];
                            move17.b = num2[1];
                            move17.c = num2[2];
                            move17.d = num2[3];
                            this.availableMoves = new ObjectList(1);
                            this.availableMoves.a(move17);
                            this.turnCounter = 1;
                            this.battleModel.a(1);
                            this.f(7);
                            break;
                        case 3:
                        case 5:
                            this.availableMoves = null;
                            this.turnCounter = 1;
                            this.battleModel.a(0);
                            this.f(8);
                            this.stageView.i();
                            if (val3.g() == 3) {
                                this.stageView.a((String)"T.Chiêu", 0);
                            } else if (GameConfig.hasPointerEvents) {
                                this.stageView.a((String)"Túi", 1);
                            }
                            break;
                        case 4:
                            this.earnedExtraTurn = true;
                            this.f(12);
                            break;
                        case 6:
                            this.availableMoves = null;
                            this.turnCounter = 1;
                            this.battleModel.a(0);
                            this.f(7);
                            break;
                        case 7:
                            this.battleModel.a(true);
                            this.turnCounter = 1;
                            this.battleModel.a(0);
                            SwapMove move5 = new SwapMove();
                            move5.a = num2[0];
                            move5.b = num2[1];
                            move5.c = num2[2];
                            move5.d = num2[3];
                            this.availableMoves = new ObjectList(1);
                            this.availableMoves.a(move5);
                            this.f(8);
                            this.stageView.g();
                    }

                    return;
                }
        }

        this.battleState = num1;
    }

    /** Returns true if current battle opponent is within ±12 levels of the player (Enhancement Shard eligible). */
    private boolean isBattleShardEligible() {
        int diff = this.battleModel.b(1).getLevel() - this.battleModel.b(0).getLevel();
        if (diff < 0) {
            diff = -diff;
        }
        return diff <= 12;
    }

    /** Rolls an in-battle Enhancement Shard drop capped at {@link #MAX_BOARD_SHARDS_PER_BATTLE} per battle. */
    private boolean tryDropBoardShard(int chancePct, String bannerText) {
        if (this.battleModel.d
                || chancePct <= 0
                || this.pendingBattleShards >= MAX_BOARD_SHARDS_PER_BATTLE
                || !this.isBattleShardEligible()) {
            return false;
        }
        if (RandomUtils.nextInt(100) < chancePct) {
            ++this.pendingBattleShards;
            this.stageView.showSkillBanner(bannerText, true, true);
            return true;
        }
        return false;
    }

    /** Rolls a random battle support item for L/T matches and x4+ cascade combos. */
    private int rollRandomSupportItem(int dropChancePct) {
        if (this.battleModel.d || dropChancePct <= 0 || RandomUtils.nextInt(100) >= dropChancePct) {
            return -1;
        }
        int r = RandomUtils.nextInt(100);
        int itemId;
        if (r < 8) {
            itemId = 1; // Nhân Sâm (8%)
        } else if (r < 22) {
            itemId = 0; // Long Thần Kiếm (14%)
        } else if (r < 42) {
            itemId = 3; // Quỷ Diện Giáp (20%)
        } else if (r < 68) {
            itemId = 5; // Ngọc Ấn (26%)
        } else {
            itemId = 4; // Bình Thuốc (32%)
        }
        if (this.battleModel.a != null && itemId < this.battleModel.a.length) {
            ++this.battleModel.a[itemId].b;
        }
        EngineStorage.a(itemId, 1, 6);
        return itemId;
    }

    private int g(int num1) {
        byte bVal2 = -1;
        if (this.battleModel.d) {
            bVal2 = 1;
            ++this.battleModel.a[bVal2].b;
        } else {
            int num3 = (num1 - 5) * (num1 - 5) * 15;
            int num4 = RandomUtils.nextInt(100);
            if (num4 < 40 + num3) {
                num4 = RandomUtils.nextInt(100);
                if (num4 < 10 + num3) {
                    if (RandomUtils.nextInt(100) < 50) {
                        bVal2 = 1;
                    } else {
                        bVal2 = 0;
                    }
                } else if (num4 < 25 + num3) {
                    bVal2 = 3;
                } else if (num4 < 45 + num3) {
                    bVal2 = 5;
                } else if (num4 < 65 + num3) {
                    bVal2 = 4;
                }
            }

            if (bVal2 >= 0) {
                ++this.battleModel.a[bVal2].b;
                EngineStorage.a(bVal2, 1, 6);
            }
        }

        return bVal2;
    }

    public int d() {
        int num1 = this.battleModel.a();
        BattleFighter fighter2 = this.battleModel.b(num1);
        if (fighter2.d().length < 1) {
            return 0;
        } else {
            int num3 = fighter2.o() * 3 / fighter2.n();
            if (!this.battleModel.d && fighter2.isStarving() && num3 > 2) {
                // Tuyệt lương (0 Lương): khóa Tuyệt chiêu Bậc 3 (36 Nộ)
                num3 = 2;
            }
            return num3;
        }
    }

    public void a(int num1) {
        int num2 = this.battleModel.a();
        BattleFighter fighter3 = this.battleModel.b(num2);
        if (!this.battleModel.d && fighter3.isStarving() && num1 >= 2) {
            return;
        }
        int num4 = (this.battleModel.a() + 1) % 2;
        BattleFighter fighter5 = this.battleModel.b(num4);
        this.activeMatchCount = 0;
        int[] num6 = fighter3.d();
        if (num1 > num6.length - 1) {
            num1 = num6.length - 1;
        }

        if (num1 >= 0) {
            int num7;
            num7 = num6[num1];
            if (!this.battleModel.d) {
                this.c(FOOD_COST_PER_ACTION);
            }
            fighter3.a(fighter3.n() * (num1 + 1) / 3, true);
            if (!this.battleModel.d
                    && !fighter3.isBossFighter()
                    && fighter3.getPassiveId() == BattleFighter.PASSIVE_THUNDER_AURA) {
                fighter3.f(4);
            }
            int[][] board = this.battleModel.e();
            boolean shatteredShield = false;
            String effectTag = null;
            switch (num7) {
                // =========================================================================
                // BRANCH 1 (ID 0..8): HERO & 12 WARLORDS ELEMENTAL SKILLS WITH BOARD SYNERGY
                // =========================================================================
                case 0: { // Quả Cầu Lửa: 1.0x ATK + Burn 2 (+20% dmg taken) + burn Food + 3x3 at cursor or Sword cluster
                    this.a(fighter5, fighter3.m());
                    int burnDmg0 = fighter3.getLevel() / 2 + 2;
                    fighter5.applyBurn(2, burnDmg0 < 3 ? 3 : burnDmg0);
                    if (!fighter5.k()) {
                        effectTag = "Bỏng";
                    }
                    int r0;
                    int c0;
                    if (num2 == 0 && this.battleModel.h()) {
                        r0 = this.clampWindowStart(this.battleModel.f() - 1, 3);
                        c0 = this.clampWindowStart(this.battleModel.g() - 1, 3);
                    } else {
                        int[] best = this.findBestWindow(3, 3, 2, 1, 2, 0, 0, 7);
                        r0 = best[0];
                        c0 = best[1];
                    }
                    int foodHit = this.countGemTypeInRect(board, r0, c0, 3, 1);
                    this.burnOpponentFood(fighter5, num4, 150 + foodHit * 40, false);
                    this.queueRectBlock(r0, c0, 3);
                    break;
                }
                case 1: { // Mưa Thiên Thạch: 1.5x ATK + Burn 3 + burn 240 Food + 3..5 2x2 blasts + forge 2 Fiery Swords
                    this.a(fighter5, fighter3.m() * 3 / 2);
                    int burnDmg1 = fighter3.getLevel() * 2 / 3 + 3;
                    fighter5.applyBurn(3, burnDmg1 < 4 ? 4 : burnDmg1);
                    if (!fighter5.k()) {
                        effectTag = "Bỏng";
                    }
                    this.burnOpponentFood(fighter5, num4, 240, false);
                    this.queueMeteor2x2Blocks(3 + RandomUtils.nextInt(3), false);
                    this.pendingForgedFierySwords += 2;
                    break;
                }
                case 2: { // Lửa Địa Ngục: Shatter Khiên Sét + 2.5x ATK + Burn 3 + burn 360 Food + 2x 4x4 + detonate Fiery Swords
                    if (fighter5.k()) {
                        shatteredShield = true;
                        this.shieldTurnsRemaining[num4] = 0;
                        fighter5.a(false);
                        this.stageView.b(num4);
                        fighter5.applyPierceDamage(fighter3.m() * 5 / 4);
                    } else {
                        this.a(fighter5, fighter3.m() * 5 / 2);
                    }
                    this.burnOpponentFood(fighter5, num4, shatteredShield ? 180 : 360, true);
                    int burnDmg2 = fighter3.getLevel() + 4;
                    fighter5.applyBurn(3, burnDmg2 < 5 ? 5 : burnDmg2);
                    effectTag = "Bỏng";
                    int leftRow = this.findBestHalfRow4x4(2, 3, 2, 1, 2, 0, 0, 7);
                    int rightRow = this.findBestHalfRow4x4(6, 3, 2, 1, 2, 0, 0, 7);
                    this.queueRectBlock(leftRow, 2, 4);
                    this.queueRectBlock(rightRow, 6, 4);
                    // Detonate all remaining Fiery Swords across the board (cap extra <= 15 so VFX block count 32/16 stays 2).
                    this.queueSpecificGemTargets(6, -1, 15, false);
                    break;
                }
                case 3: { // Chuỗi Sét: 0.8x ATK + chain up to 5 gems matching selected cursor gem (scaled tile effects)
                    this.a(fighter5, fighter3.m() * 8 / 10);
                    int targetGroup;
                    if (num2 == 0 && this.battleModel.h()) {
                        int selGem = board[this.battleModel.f()][this.battleModel.g()];
                        targetGroup = (selGem >= 0 && selGem <= 6) ? BattleConstants.i[selGem] : this.findMostAbundantCombatGem();
                    } else {
                        targetGroup = this.findMostAbundantCombatGem();
                    }
                    int targetB = (targetGroup == 0) ? 6 : -1;
                    this.resolvingChainLightning = true;
                    this.chainLightningManaAccum = 0;
                    this.queueSpecificGemTargets(targetGroup, targetB, 5, true);
                    break;
                }
                case 4: { // Khiên Sét: 3-turn Shield (6 handoffs) + cleanse ALL debuffs + absorb up to 4 Gold/Book tiles (+2 Mana each)
                    this.shieldTurnsRemaining[num2] = 6;
                    fighter3.a(true);
                    fighter3.setShieldTurnsDisplay(3);
                    fighter3.clearDebuffs();
                    int absorbed = this.queueSpecificGemTargets(4, 5, 4, false);
                    if (absorbed > 0) {
                        fighter3.f(absorbed * 2);
                    }
                    break;
                }
                case 5: { // Sấm Sét: 2.0x ATK + burn 320 Food + Trọng Thương 2 lượt + 3..4 quadrant 3x3 blasts
                    int blocks = 3 + RandomUtils.nextInt(2);
                    int manaBlasted = this.queueQuadrant3x3Blocks(blocks, true);
                    int baseDmg = fighter3.m() * 2;
                    int bonusDmg = manaBlasted * (fighter3.m() * 15 / 100);
                    this.a(fighter5, baseDmg + bonusDmg);
                    if (!fighter5.k()) {
                        this.burnOpponentFood(fighter5, num4, 320, false);
                        fighter5.applyWound(2);
                        effectTag = "Trọng Thương";
                    }
                    break;
                }
                case 6: { // Mũi Tên Băng: 1.2x ATK + Hàn Băng 2 lượt + -8 Nộ + phá 1 lượt Khiên + nổ 4 ô Nộ/Kiếm Lửa + rèn 1 Kiếm Lửa
                    if (fighter5.k() && this.shieldTurnsRemaining[num4] > 0) {
                        this.shieldTurnsRemaining[num4] -= 2;
                        if (this.shieldTurnsRemaining[num4] <= 0) {
                            this.shieldTurnsRemaining[num4] = 0;
                            fighter5.a(false);
                            this.stageView.b(num4);
                            shatteredShield = true;
                        } else {
                            fighter5.setShieldTurnsDisplay((this.shieldTurnsRemaining[num4] + 1) >> 1);
                            effectTag = "-1 Lượt Khiên";
                        }
                    }
                    this.a(fighter5, fighter3.m() * 6 / 5);
                    if (!fighter5.k()) {
                        fighter5.applyArmorBreak(2);
                        fighter5.a(8, false);
                        effectTag = "Hàn Băng";
                    }
                    this.queueSpecificGemTargets(3, 6, 4, true);
                    ++this.pendingForgedFierySwords;
                    break;
                }
                case 7: { // Cam Lộ Thủy: Giải bất lợi + Hồi 24% Máu (dư -> Giáp) + Hút Tim hóa Thủy Kích + Hàn Băng 2 + Băng Tâm thêm 1 lượt
                    fighter3.clearDebuffs();
                    int baseHeal7 = fighter3.s() * 24 / 100;
                    fighter3.healWithFrostBarrierOverflow(baseHeal7 < 8 ? 8 : baseHeal7);
                    int absorbedHearts = 0;
                    for (int r = 2; r < 10; ++r) {
                        for (int c = 2; c < 10; ++c) {
                            if (board[r][c] == 2) {
                                this.a(2, r, c, 1, 1, 1);
                                ++absorbedHearts;
                            }
                        }
                    }
                    int waveDmg = fighter3.m() + absorbedHearts * (fighter3.m() * 15 / 100);
                    this.a(fighter5, waveDmg);
                    if (!fighter5.k()) {
                        fighter5.applyArmorBreak(2);
                    }
                    if (!this.battleModel.d
                            && !fighter3.isBossFighter()
                            && fighter3.getPassiveId() == BattleFighter.PASSIVE_FROST_HEART) {
                        this.turnCounter += 1;
                        effectTag = "Thủy Kích + Thêm Lượt";
                    } else if (!fighter5.k()) {
                        effectTag = "Thủy Kích";
                    }
                    break;
                }
                case 8: { // Băng Phong: Phá Khiên! 2.2x ATK (+25% kết liễu Hàn Băng / <35% Máu) + Đóng Băng 2 lượt + Hàn Băng 3 + 3x3 + rèn 2 Kiếm Lửa
                    boolean executeBonus = fighter5.isArmorBroken()
                            || this.frozenTurnsRemaining[num4] > 0
                            || (fighter5.s() > 0 && (fighter5.r() * 100 / fighter5.s()) <= 35);
                    if (fighter5.k()) {
                        shatteredShield = true;
                        this.shieldTurnsRemaining[num4] = 0;
                        fighter5.a(false);
                        this.stageView.b(num4);
                        fighter5.applyPierceDamage(fighter3.m() * 27 / 20);
                        fighter5.applyArmorBreak(3);
                        this.burnOpponentFood(fighter5, num4, 180, true);
                        this.applyFreezeTurnsCustom(num4, fighter5, 1);
                    } else {
                        int baseFreezeDmg = fighter3.m() * 11 / 5;
                        if (executeBonus) {
                            baseFreezeDmg = baseFreezeDmg * 125 / 100;
                        }
                        this.a(fighter5, baseFreezeDmg);
                        fighter5.applyArmorBreak(3);
                        fighter5.a(18, false);
                        this.burnOpponentFood(fighter5, num4, 280, false);
                        this.applyFreezeTurns(num4, fighter5);
                        effectTag = executeBonus ? "Băng Trảm+Đóng Băng" : "Đóng Băng";
                    }
                    int r8;
                    int c8;
                    if (num2 == 0 && this.battleModel.h()) {
                        r8 = this.clampWindowStart(this.battleModel.f() - 1, 3);
                        c8 = this.clampWindowStart(this.battleModel.g() - 1, 3);
                    } else {
                        int[] best8 = this.findBestWindow(3, 2, 3, 0, 2, 0, 0, 7);
                        r8 = best8[0];
                        c8 = best8[1];
                    }
                    this.queueRectBlock(r8, c8, 3);
                    this.queueSpecificGemTargets(1, 6, 4, false);
                    this.pendingForgedFierySwords += 2;
                    break;
                }

                // =========================================================================
                // BRANCH 2 (ID 9..17): MONSTER & DEMON BOARD-SABOTAGE & DEBUFF ARTS (YÊU THUẬT)
                // =========================================================================
                case 9: { // Cuồng Trảo (vfx 0): 1.0x ATK + Trọng Thương 2 lượt (-20% ATK, +75% hao Lương) + 3x3 slash
                    this.a(fighter5, fighter3.m());
                    fighter5.applyWound(2);
                    if (!fighter5.k()) {
                        effectTag = "Trọng Thương";
                    }
                    int[] best = this.findBestWindow(3, 4, 1, 0, 1, 4, 0, 6);
                    this.queueRectBlock(best[0], best[1], 3);
                    break;
                }
                case 10: { // Cướp Lương Thực (vfx 1): 1.3x ATK + steal 200 Food + Trọng Thương 3 lượt + 3..4 2x2 blasts
                    this.a(fighter5, fighter3.m() * 13 / 10);
                    if (!fighter5.k()) {
                        this.burnOpponentFood(fighter5, num4, 200, false);
                        int stealBack = 200;
                        if (fighter3.getBurnTurns() > 0 || fighter3.isWounded()) {
                            stealBack = stealBack * 60 / 100;
                        }
                        fighter3.i(stealBack);
                        fighter5.applyWound(3);
                        effectTag = "Trọng Thương";
                    }
                    this.queueMeteor2x2Blocks(3 + RandomUtils.nextInt(2), true);
                    break;
                }
                case 11: { // Long Trời Lở Đất (vfx 2): Shatter Khiên Sét + 2.3x ATK + Bỏng 2 (+20% ST nhận) + Trọng Thương 2 + 2x 4x4 + 2 Kiếm Lửa
                    if (fighter5.k()) {
                        shatteredShield = true;
                        this.shieldTurnsRemaining[num4] = 0;
                        fighter5.a(false);
                        this.stageView.b(num4);
                        fighter5.applyPierceDamage(fighter3.m() * 115 / 100);
                    } else {
                        this.a(fighter5, fighter3.m() * 23 / 10);
                    }
                    int burnDmg11 = fighter3.getLevel() * 2 / 3 + 3;
                    fighter5.applyBurn(2, burnDmg11 < 4 ? 4 : burnDmg11);
                    fighter5.applyWound(2);
                    effectTag = "Bỏng+Thương";
                    int leftRow = this.findBestHalfRow4x4(2, 3, 2, 1, 2, 1, 0, 6);
                    int rightRow = this.findBestHalfRow4x4(6, 3, 2, 1, 2, 1, 0, 6);
                    this.queueRectBlock(leftRow, 2, 4);
                    this.queueRectBlock(rightRow, 6, 4);
                    this.pendingForgedFierySwords += 2;
                    break;
                }
                case 12: { // Độc Chướng (vfx 3): 0.8x ATK + Trúng Độc 2 lượt (DoT + -30% hồi Tim) + phá 8 Tim/Lương
                    this.a(fighter5, fighter3.m() * 8 / 10);
                    int poisonDmg = fighter3.getLevel() / 2 + 3;
                    fighter5.applyPoison(2, poisonDmg < 4 ? 4 : poisonDmg);
                    if (!fighter5.k()) {
                        effectTag = "Trúng Độc";
                    }
                    this.queueSpecificGemTargets(2, 1, 8, true);
                    break;
                }
                case 13: { // Yêu Giáp Hộ Thể (vfx 4): 2-turn Armor Shield (4 handoffs) + cleanse ALL debuffs + absorb 4 Heart/Food
                    this.shieldTurnsRemaining[num2] = 4;
                    fighter3.a(true);
                    fighter3.setShieldTurnsDisplay(2);
                    fighter3.clearDebuffs();
                    this.queueSpecificGemTargets(2, 1, 4, false);
                    break;
                }
                case 14: { // Bão Độc Ma Trơi (vfx 5): 1.8x ATK + Trúng Độc 3 lượt + Nhiễu Hồn 2 lượt + 3..4 3x3 + hút 3 Nộ/ô Nộ
                    int blocks = 3 + RandomUtils.nextInt(2);
                    int manaBlasted = this.queueQuadrant3x3Blocks(blocks, true);
                    this.a(fighter5, fighter3.m() * 18 / 10);
                    int poisonDmg = fighter3.getLevel() * 2 / 3 + 5;
                    fighter5.applyPoison(3, poisonDmg < 5 ? 5 : poisonDmg);
                    fighter5.applyCurse(2);
                    if (!fighter5.k()) {
                        effectTag = "Độc+Loạn";
                    }
                    if (manaBlasted > 0) {
                        fighter5.a(manaBlasted * 3, false);
                    }
                    break;
                }
                case 15: { // Nhiễu Hồn Tiễn (vfx 6): 0.8x ATK + -8 Nộ + Nhiễu Hồn 3 lượt (-40% Nộ ngọc) + bắn vỡ 4 ô Nộ
                    this.a(fighter5, fighter3.m() * 8 / 10);
                    fighter5.a(8, false);
                    fighter5.applyCurse(3);
                    if (!fighter5.k()) {
                        effectTag = "Nhiễu Hồn";
                    }
                    this.queueSpecificGemTargets(3, 6, 4, true);
                    break;
                }
                case 16: { // Huyết Mộc Hấp Thụ (vfx 7): Cleanse ALL debuffs + Heal 20% MaxHP + +150 Food + absorb all Hearts
                    fighter3.clearDebuffs();
                    this.b(fighter3, fighter3.s() / 10 * 2);
                    fighter3.i(150);
                    for (int r = 2; r < 10; ++r) {
                        for (int c = 2; c < 10; ++c) {
                            if (board[r][c] == 2) {
                                this.a(2, r, c, 1, 1, 1);
                            }
                        }
                    }
                    break;
                }
                case 17: { // Tơ Độc Giam Cầm (vfx 8): 1.5x ATK + -16 Nộ + Trói 2 lượt + Trúng Độc 3 lượt + phá 6 ô Kiếm
                    this.a(fighter5, fighter3.m() * 3 / 2);
                    fighter5.a(16, false);
                    int poisonDmg17 = fighter3.getLevel() * 2 / 3 + 4;
                    fighter5.applyPoison(3, poisonDmg17 < 4 ? 4 : poisonDmg17);
                    this.queueSpecificGemTargets(0, 6, 6, true);
                    this.applyFreezeTurns(num4, fighter5);
                    if (!fighter5.k()) {
                        effectTag = "Trói+Độc";
                    }
                    break;
                }
            }

            String skillName = (num7 >= 0 && num7 < GameStrings.a.length) ? GameStrings.a[num7] : "Tuyệt chiêu";
            String bannerLabel;
            if (shatteredShield) {
                bannerLabel = "PHÁ KHIÊN: " + skillName + "!";
            } else {
                bannerLabel = (num2 == 0 ? "BẠN: " : "ĐỊCH: ") + skillName
                        + (effectTag != null ? (" (" + effectTag + ")") : "!");
            }
            int vfxId = num7 % 9;
            if (!this.battleModel.d
                    && fighter3.getBossArchetype() == BattleFighter.BOSS_ARCHETYPE_WARLORD_FIRE
                    && vfxId != 4
                    && vfxId != 7
                    && !fighter5.k()) {
                this.burnOpponentFood(fighter5, num4, 65, false);
            }
            this.stageView.showSkillBanner(bannerLabel, num2 == 0, shatteredShield);
            if (shatteredShield || vfxId == 2 || vfxId == 5 || vfxId == 8) {
                GUIManager.shakeScreen(8, 3);
            } else if (vfxId == 1) {
                GUIManager.shakeScreen(5, 2);
            }
            this.checkBossThresholdAwakening(num4);

            this.animWaitTicks = this.stageView.a(vfxId, num2, this.activeMatches, this.activeMatchCount);
            this.f(11);
            BattleResourceManager.a().a(vfxId);
        }
    }

    private void applyFreezeTurns(int targetIndex, BattleFighter target) {
        this.applyFreezeTurnsCustom(targetIndex, target, 2);
    }

    private void applyFreezeTurnsCustom(int targetIndex, BattleFighter target, int turns) {
        if (!target.k() && turns > 0) {
            if (this.frozenTurnsRemaining[targetIndex] > 0) {
                this.turnCounter += 1;
                this.frozenTurnsRemaining[targetIndex] += 1;
            } else {
                this.turnCounter += turns;
                this.frozenTurnsRemaining[targetIndex] += (turns + 1);
            }
        }
    }

    private int clampWindowStart(int pos, int size) {
        if (pos < 2) {
            return 2;
        }
        int maxStart = 10 - size;
        return pos > maxStart ? maxStart : pos;
    }

    private boolean isCellAlreadyQueued(int row, int col) {
        for (int i = this.activeMatchCount - 1; i >= 0; --i) {
            if (this.activeMatches[i].a == row && this.activeMatches[i].b == col) {
                return true;
            }
        }
        return false;
    }

    private void queueRectBlock(int startRow, int startCol, int size) {
        for (int r = startRow + size - 1; r >= startRow; --r) {
            for (int c = startCol + size - 1; c >= startCol; --c) {
                this.a(this.battleModel.a(r, c), r, c, 1, 1, 1);
            }
        }
    }

    private int sealScoreDeltaForCaster(int row, int col) {
        if (this.battleModel.getBossSealTurns(row, col) <= 0) {
            return 0;
        }
        return this.battleModel.a() == 0 ? 10 : -12;
    }

    private int[] findBestWindow(int size, int w0, int w1, int w2, int w3, int w4, int w5, int w6) {
        int[][] board = this.battleModel.e();
        int[] weights = new int[]{w0, w1, w2, w3, w4, w5, w6};
        int maxStart = 10 - size;
        int bestScore = -9999;
        int bestR = 2;
        int bestC = 2;
        for (int r = 2; r <= maxStart; ++r) {
            for (int c = 2; c <= maxStart; ++c) {
                int score = 0;
                for (int dr = 0; dr < size; ++dr) {
                    for (int dc = 0; dc < size; ++dc) {
                        int gem = board[r + dr][c + dc];
                        if (gem >= 0 && gem < weights.length) {
                            score += weights[gem];
                        }
                        score += this.sealScoreDeltaForCaster(r + dr, c + dc);
                    }
                }
                if (score > bestScore || (score == bestScore && RandomUtils.nextInt(3) == 0)) {
                    bestScore = score;
                    bestR = r;
                    bestC = c;
                }
            }
        }
        return new int[]{bestR, bestC};
    }

    private int findBestHalfRow4x4(int startCol, int w0, int w1, int w2, int w3, int w4, int w5, int w6) {
        int[][] board = this.battleModel.e();
        int[] weights = new int[]{w0, w1, w2, w3, w4, w5, w6};
        int bestScore = -9999;
        int bestRow = 2;
        for (int r = 2; r <= 6; ++r) {
            int score = 0;
            for (int dr = 0; dr < 4; ++dr) {
                for (int dc = 0; dc < 4; ++dc) {
                    int gem = board[r + dr][startCol + dc];
                    if (gem >= 0 && gem < weights.length) {
                        score += weights[gem];
                    }
                    score += this.sealScoreDeltaForCaster(r + dr, startCol + dc);
                }
            }
            if (score > bestScore || (score == bestScore && RandomUtils.nextInt(2) == 0)) {
                bestScore = score;
                bestRow = r;
            }
        }
        return bestRow;
    }

    private int findMostAbundantCombatGem() {
        int[][] board = this.battleModel.e();
        int[] counts = new int[4]; // 0=Sword(incl Fiery), 1=Food, 2=Heart, 3=Mana
        for (int r = 2; r < 10; ++r) {
            for (int c = 2; c < 10; ++c) {
                int g = board[r][c];
                if (g == 0 || g == 6) {
                    ++counts[0];
                } else if (g >= 1 && g <= 3) {
                    ++counts[g];
                }
            }
        }
        int bestType = 0;
        int bestCount = counts[0];
        if (counts[3] > bestCount) {
            bestCount = counts[3];
            bestType = 3;
        }
        if (counts[2] > bestCount) {
            bestCount = counts[2];
            bestType = 2;
        }
        if (counts[1] > bestCount) {
            bestType = 1;
        }
        return bestType;
    }

    private int queueSpecificGemTargets(int targetA, int targetB, int maxCount, boolean fallbackRandom) {
        int[][] board = this.battleModel.e();
        boolean isPlayerCaster = (this.battleModel.a() == 0);
        int[] candR = new int[64];
        int[] candC = new int[64];
        int candCount = 0;
        // Player prioritizes sealed matching gems first; Boss skips its own sealed tiles.
        for (int pass = 0; pass < 2; ++pass) {
            for (int r = 2; r < 10; ++r) {
                for (int c = 2; c < 10; ++c) {
                    int g = board[r][c];
                    if ((g == targetA || (targetB >= 0 && g == targetB)) && !this.isCellAlreadyQueued(r, c)) {
                        boolean sealed = this.battleModel.getBossSealTurns(r, c) > 0;
                        if (!isPlayerCaster && sealed) {
                            continue;
                        }
                        if (isPlayerCaster && ((pass == 0 && !sealed) || (pass == 1 && sealed))) {
                            continue;
                        }
                        if (!isPlayerCaster && pass == 1) {
                            continue;
                        }
                        candR[candCount] = r;
                        candC[candCount] = c;
                        ++candCount;
                    }
                }
            }
        }
        // Shuffle non-sealed candidates for variety while keeping player's sealed priority at front
        int sealedFront = 0;
        if (isPlayerCaster) {
            while (sealedFront < candCount && this.battleModel.getBossSealTurns(candR[sealedFront], candC[sealedFront]) > 0) {
                ++sealedFront;
            }
        }
        for (int i = candCount - 1; i > sealedFront; --i) {
            int j = sealedFront + RandomUtils.nextInt(i - sealedFront + 1);
            int tr = candR[i]; candR[i] = candR[j]; candR[j] = tr;
            int tc = candC[i]; candC[i] = candC[j]; candC[j] = tc;
        }
        int added = 0;
        for (int i = 0; i < candCount && added < maxCount; ++i) {
            int r = candR[i];
            int c = candC[i];
            this.a(board[r][c], r, c, 1, 1, 1);
            ++added;
        }
        if (fallbackRandom) {
            int minNeeded = maxCount < 3 ? maxCount : 3;
            int attempts = 0;
            while (added < minNeeded && attempts < 40) {
                ++attempts;
                int r = RandomUtils.nextInt(8) + 2;
                int c = RandomUtils.nextInt(8) + 2;
                if (!this.isCellAlreadyQueued(r, c)
                        && (isPlayerCaster || this.battleModel.getBossSealTurns(r, c) == 0)) {
                    this.a(board[r][c], r, c, 1, 1, 1);
                    ++added;
                }
            }
        }
        return added;
    }

    private void queueMeteor2x2Blocks(int blockCount, boolean preferFoodGold) {
        int[][] board = this.battleModel.e();
        int[] blockR = new int[16];
        int[] blockC = new int[16];
        int[] blockScore = new int[16];
        int idx = 0;
        for (int br = 0; br < 4; ++br) {
            for (int bc = 0; bc < 4; ++bc) {
                int r0 = br * 2 + 2;
                int c0 = bc * 2 + 2;
                int score = RandomUtils.nextInt(3);
                for (int dr = 0; dr < 2; ++dr) {
                    for (int dc = 0; dc < 2; ++dc) {
                        int g = board[r0 + dr][c0 + dc];
                        if (preferFoodGold) {
                            if (g == 1 || g == 4) score += 5;
                            else if (g == 0 || g == 6) score += 2;
                        } else {
                            if (g == 6) score += 6;
                            else if (g == 0 || g == 3) score += 3;
                        }
                        score += this.sealScoreDeltaForCaster(r0 + dr, c0 + dc);
                    }
                }
                blockR[idx] = r0;
                blockC[idx] = c0;
                blockScore[idx] = score;
                ++idx;
            }
        }
        // Selection sort top blockCount blocks
        for (int i = 0; i < blockCount && i < idx; ++i) {
            int best = i;
            for (int j = i + 1; j < idx; ++j) {
                if (blockScore[j] > blockScore[best]) {
                    best = j;
                }
            }
            int tr = blockR[i]; blockR[i] = blockR[best]; blockR[best] = tr;
            int tc = blockC[i]; blockC[i] = blockC[best]; blockC[best] = tc;
            int ts = blockScore[i]; blockScore[i] = blockScore[best]; blockScore[best] = ts;
            this.queueRectBlock(blockR[i], blockC[i], 2);
        }
    }

    private int queueQuadrant3x3Blocks(int blockCount, boolean preferMana) {
        int[][] board = this.battleModel.e();
        int[] qr = new int[4];
        int[] qc = new int[4];
        int[] qScore = new int[4];
        int qIdx = 0;
        for (int rowBase = 0; rowBase < 8; rowBase += 4) {
            for (int colBase = 0; colBase < 8; colBase += 4) {
                int bestR = 2 + rowBase;
                int bestC = 2 + colBase;
                int bestS = -9999;
                for (int dr = 0; dr < 2; ++dr) {
                    for (int dc = 0; dc < 2; ++dc) {
                        int r0 = 2 + rowBase + dr;
                        int c0 = 2 + colBase + dc;
                        int s = 0;
                        for (int rr = 0; rr < 3; ++rr) {
                            for (int cc = 0; cc < 3; ++cc) {
                                int g = board[r0 + rr][c0 + cc];
                                if (preferMana && g == 3) s += 5;
                                else if (g == 6) s += 4;
                                else if (g == 0) s += 2;
                                s += this.sealScoreDeltaForCaster(r0 + rr, c0 + cc);
                            }
                        }
                        if (s > bestS) {
                            bestS = s;
                            bestR = r0;
                            bestC = c0;
                        }
                    }
                }
                qr[qIdx] = bestR;
                qc[qIdx] = bestC;
                qScore[qIdx] = bestS;
                ++qIdx;
            }
        }
        int manaHit = 0;
        for (int i = 0; i < blockCount && i < 4; ++i) {
            int best = i;
            for (int j = i + 1; j < 4; ++j) {
                if (qScore[j] > qScore[best]) {
                    best = j;
                }
            }
            int tr = qr[i]; qr[i] = qr[best]; qr[best] = tr;
            int tc = qc[i]; qc[i] = qc[best]; qc[best] = tc;
            int ts = qScore[i]; qScore[i] = qScore[best]; qScore[best] = ts;
            for (int rr = 0; rr < 3; ++rr) {
                for (int cc = 0; cc < 3; ++cc) {
                    if (board[qr[i] + rr][qc[i] + cc] == 3) {
                        ++manaHit;
                    }
                }
            }
            this.queueRectBlock(qr[i], qc[i], 3);
        }
        return manaHit;
    }

    private void h(int num1, int num2) {
        int num3 = this.activeMatchCount / num2;

        for(int num4 = 0; num4 < this.activeMatchCount; num4 += num2) {
            int num5 = RandomUtils.nextInt(num3) * num2;

            for(int num6 = 0; num6 < num2; ++num6) {
                int num7 = num4 + num6;
                int num8 = num5 + num6;
                GemMatch match9 = this.activeMatches[num7];
                this.activeMatches[num7] = this.activeMatches[num8];
                this.activeMatches[num8] = match9;
            }
        }

        if (num1 < num3) {
            this.activeMatchCount = num1 * num2;
        }

    }

    private boolean y() {
        BattleFighter[] fighter1 = this.battleModel.d();
        boolean flag2 = fighter1[0].r() <= 0 || fighter1[1].r() <= 0;
        if (!flag2 && this.stageView.o() != 1) {
            return false;
        } else {
            if (this.battleModel.d) {
                this.earnedExtraTurn = true;
            } else if (flag2) {
                if (fighter1[0].r() <= 0) {
                    this.earnedExtraTurn = false;
                } else {
                    this.earnedExtraTurn = true;
                }
            } else {
                switch (this.battleModel.c()) {
                    case 2:
                        this.earnedExtraTurn = false;
                        break;
                    case 3:
                        this.earnedExtraTurn = true;
                }
            }

            this.f(9);
            return true;
        }
    }

    public void e() {
        this.stateDelayTicks = 0;
        int[][] num1 = this.battleModel.e();
        int[] num2 = new int[12];
        int[] num3 = new int[12];

        for(int num4 = 2; num4 < 10; ++num4) {
            num2[num4] = 9;
        }

        int num8 = 0;

        for(int num5 = 2; num5 < 10; ++num5) {
            boolean flag6 = false;

            for(int num7 = 9; num7 >= 2; --num7) {
                if (num1[num7][num5] < 7) {
                    if (num7 != num2[num5]) {
                        this.columnDropCounts[this.stateDelayTicks++] = num2[num5] << 8 | num5;
                        num1[num2[num5]][num5] = num1[num7][num5];
                        num1[num7][num5] = 7;
                        this.battleModel.moveBossSeal(num7, num5, num2[num5], num5);
                        this.stageView.b(num7, num5, num2[num5], num5, num1[num2[num5]][num5], num3[num5] + num8);
                        int numTmp2 = num3[num5]++;
                        flag6 = true;
                    }

                    int num13 = num2[num5]--;
                }
            }

            if (flag6) {
                ++num8;
            }
        }

        num8 = 0;

        for(int num10 = 2; num10 < 10; ++num10) {
            int num11 = 0;
            if (num2[num10] >= 2) {
                for(int num12 = num2[num10]; num12 >= 2; --num12) {
                    this.columnDropCounts[this.stateDelayTicks++] = num12 << 8 | num10;
                    this.battleModel.d(num12, num10);
                    if (this.pendingForgedFierySwords > 0) {
                        num1[num12][num10] = 6;
                        --this.pendingForgedFierySwords;
                    }
                    this.stageView.b(num11, num10, num12, num10, num1[num12][num10], num3[num10] + num8);
                    int num14 = num3[num10]++;
                    --num11;
                    this.animWaitTicks = 10;
                }

                ++num8;
            }
        }
        this.pendingForgedFierySwords = 0;
        if (this.stageView != null) {
            this.stageView.syncBossSeals();
        }

    }

    public void f() {
        this.activeMatchCount = 0;
        int[][] num1 = new int[12][12];

        for(int num2 = 0; num2 < this.stateDelayTicks; ++num2) {
            int num3 = (this.columnDropCounts[num2] & '\uff00') >> 8;
            int num4 = this.columnDropCounts[num2] & 255;
            int num5 = 0;
            int num6 = 0;
            boolean flag7 = false;
            boolean flag8 = false;
            int num9 = 0;
            if ((num1[num3][num4] & 255) != 255) {
                num5 = this.battleModel.e(num3, num4);
                flag7 = (num5 & 255) >= 3;
                if (flag7) {
                    num9 += num5 & 255;
                }

                this.a(num1, num3, num4 - ((num5 & 16711680) >> 16), num5 & 255);
            }

            if ((num1[num3][num4] & '\uff00') != 65280) {
                num6 = this.battleModel.f(num3, num4);
                flag8 = (num6 & 255) >= 3;
                if (flag8) {
                    num9 += num6 & 255;
                }

                this.b(num1, num3 - ((num6 & 16711680) >> 16), num4, num6 & 255);
            }

            if (flag7) {
                this.a(this.battleModel.a(num3, num4), num3, num4 - ((num5 & 16711680) >> 16), 1, num5 & 255, num9);
            }

            if (flag8) {
                this.a(this.battleModel.a(num3, num4), num3 - ((num6 & 16711680) >> 16), num4, num6 & 255, 1, num9);
            }

            num1[num3][num4] = 65535;
        }

    }

    private void a(int[][] num1, int num2, int num3, int num4) {
        int num5 = num3 + num4;

        for(int num6 = num3; num6 < num5; ++num6) {
            num1[num2][num6] |= 255;
        }

    }

    private void b(int[][] num1, int num2, int num3, int num4) {
        int num5 = num2 + num4;

        for(int num6 = num2; num6 < num5; ++num6) {
            num1[num6][num3] |= 65280;
        }

    }

    private void a(int num1, int num2, int num3, int num4, int num5, int num6) {
        this.b(BattleConstants.i[num1], num2, num3, num4, num5, num6);
        if (num4 >= 4 || num5 >= 4) {
            ++this.turnCounter;
        }

    }

    private void b(int num1, int num2, int num3, int num4, int num5, int num6) {
        if (this.activeMatchCount >= this.activeMatches.length) {
            return;
        }
        GemMatch match7 = this.activeMatches[this.activeMatchCount];
        match7.e = num1;
        match7.a = num2;
        match7.c = num4;
        match7.b = num3;
        match7.d = num5;
        match7.f = num6;
        ++this.activeMatchCount;
    }

    public void b(int num1) {
        if (this.battleState == 14) {
            this.stageView.g.f();
        } else if (this.battleState == 8 && this.battleModel.a() == 0) {
            this.turnActors[this.battleModel.a()].a(num1);
        }

    }

    public void a(int num1, int num2) {
        if (this.battleState == 14) {
            this.stageView.g.f();
        } else if (this.battleState == 8 && this.battleModel.a() == 0) {
            this.turnActors[this.battleModel.a()].a(num1, num2);
        }

    }

    public void onPointerDragged(int x, int y) {
        if (this.battleState == 8 && this.battleModel.a() == 0) {
            this.turnActors[this.battleModel.a()].onPointerDragged(x, y);
        }
    }

    public void onPointerReleased(int x, int y) {
        if (this.battleState == 8 && this.battleModel.a() == 0) {
            this.turnActors[this.battleModel.a()].onPointerReleased(x, y);
        }
    }

    public void g() {
        if (this.battleModel.d) {
            BaseDialog dialog2 = GUIManager.a("Thông tin", "Bạn có muốn bỏ qua phần luyện tập không?", "Có", -6, "Không", -2, 1);
            ((Window)dialog2).a((ActionListener)this);
            this.stageView.a(false);
            GUIManager.getInstance().a(dialog2, true);
        } else {
            BattlePauseDialog pauseDialog = new BattlePauseDialog(
                    this,
                    this.battleModel.b(0),
                    this.stageView.getFighterPortrait(0),
                    this.battleModel.b(1),
                    this.stageView.getFighterPortrait(1));
            GUIManager.getInstance().a(pauseDialog, true);
        }
    }

    public Rect h() {
        return this.stageView.f;
    }

    public boolean a(int num1, int num2, int num3, int num4) {
        if (this.battleModel.d) {
            SwapMove move5 = (SwapMove)this.availableMoves.b(0);
            if ((move5.a != num1 || move5.b != num2 || move5.c != num3 || move5.d != num4) && (move5.a != num3 || move5.b != num4 || move5.c != num1 || move5.d != num2)) {
                this.stageView.g.e();
                this.f(14);
                return false;
            }
        }

        SwapMove move6 = this.battleModel.a(num1, num2, num3, num4);
        this.pendingSwapMove = move6;
        if (this.pendingSwapMove == null) {
            this.pendingSwapMove = new SwapMove();
            this.pendingSwapMove.b = num2;
            this.pendingSwapMove.a = num1;
            this.pendingSwapMove.d = num4;
            this.pendingSwapMove.c = num3;
        }

        if (!this.battleModel.d) {
            this.c(FOOD_COST_PER_ACTION);
        }

        this.f(1);
        if (move6 != null) {
            return true;
        } else {
            return false;
        }
    }

    public void i() {
        if (this.battleModel.d) {
            if (this.stageView.g.g() == 6) {
                this.stageView.g.c();
                this.f(13);
                return;
            }

            if (this.stageView.g.g() != 1) {
                this.stageView.g.e();
                this.f(14);
                return;
            }
        }

        if (this.battleModel.h()) {
            this.battleModel.a(false);
            this.stageView.e();
        } else {
            this.battleModel.a(true);
            this.stageView.g();
            this.stageView.h();
        }

    }

    public void c(int num1, int num2) {
        if (this.battleModel.d && !this.battleModel.h()) {
            this.stageView.g.e();
            this.f(14);
        } else {
            this.battleModel.c(num1);
            this.battleModel.d(num2);
            this.battleModel.a(false);
            this.stageView.e();
        }
    }

    /** Moves the cursor to (row, col) and immediately selects it for 1-tap / swipe-to-swap touch UX. */
    public boolean selectCellAt(int row, int col) {
        if (!this.battleModel.c(row, col)) {
            return false;
        }
        if (this.battleModel.d) {
            if (row == this.battleModel.f() && col == this.battleModel.g()) {
                if (!this.battleModel.h()) {
                    this.i();
                }
                return this.battleModel.h();
            }
            this.stageView.g.e();
            this.f(14);
            return false;
        }
        this.battleModel.c(row);
        this.battleModel.d(col);
        this.battleModel.a(true);
        this.stageView.g();
        this.stageView.h();
        return true;
    }

    public boolean j() {
        return this.battleModel.h();
    }

    public int k() {
        return this.battleModel.f();
    }

    public int l() {
        return this.battleModel.g();
    }

    public boolean m() {
        int num1 = this.battleModel.f();
        int num2 = this.battleModel.g();
        return this.b(num1, num2, num1 - 1, num2);
    }

    public boolean n() {
        int num1 = this.battleModel.f();
        int num2 = this.battleModel.g();
        return this.b(num1, num2, num1 + 1, num2);
    }

    public boolean o() {
        int num1 = this.battleModel.f();
        int num2 = this.battleModel.g();
        return this.b(num1, num2, num1, num2 - 1);
    }

    public boolean p() {
        int num1 = this.battleModel.f();
        int num2 = this.battleModel.g();
        return this.b(num1, num2, num1, num2 + 1);
    }

    public boolean b(int num1, int num2, int num3, int num4) {
        if (this.battleModel.d && !this.battleModel.h()) {
            this.stageView.g.e();
            this.f(14);
            return false;
        } else if (!this.battleModel.c(num3, num4)) {
            return false;
        } else if (this.battleModel.h()) {
            return this.a(num1, num2, num3, num4);
        } else {
            int num5 = this.battleModel.f();
            int num6 = this.battleModel.g();
            boolean flag7 = this.battleModel.b(num3, num4);
            if (flag7) {
                this.stageView.a(num5, num6);
            }

            return flag7;
        }
    }

    public void a(SwapMove move1) {
        this.stageView.a(move1);
    }

    public ObjectList q() {
        return this.availableMoves;
    }

    public int d(int num1, int num2) {
        return this.battleModel.a(num1, num2);
    }

    public boolean hasBossSealAt(int row, int col) {
        return this.battleModel.getBossSealTurns(row, col) > 0;
    }

    public int e(int num1, int num2) {
        int num3 = this.battleModel.a();
        int num4 = num3 == 0 ? 1 : 0;
        BattleFighter[] fighter5 = this.battleModel.d();
        BattleFighter self = fighter5[num3];
        BattleFighter opponent = fighter5[num4];
        int archetype = self.getAiArchetype();
        int num6 = 0;

        switch (num1) {
            case 0:
            case 6:
                if (!opponent.k() && opponent.r() <= num2 * num2 - 2 * num2 + self.m()) {
                    return 120;
                }
                if (opponent.k()) {
                    // Opponent is protected by Khiên Sét: normal swords deal 0 damage, fiery swords still clear 3x3 board.
                    num6 = (num1 == 6) ? 6 : 3;
                } else {
                    num6 = (num1 == 6) ? 12 : 9;
                    if (archetype == BattleFighter.ARCHETYPE_AGGRESSIVE) {
                        num6 += 3;
                    }
                    if (num2 >= 4) {
                        // Matching 4+ swords forges a new Fiery Sword!
                        num6 += 4;
                    }
                }
                break;
            case 1:
                int foodPct = self.q() > 0 ? (self.p() * 100 / self.q()) : 100;
                if (foodPct <= 15) {
                    num6 = 11;
                } else if (foodPct <= 25) {
                    num6 = 9;
                } else if (foodPct <= 50) {
                    num6 = 5;
                } else if (foodPct >= 80) {
                    num6 = 1;
                } else {
                    num6 = 3;
                }
                if (self.isWounded() && foodPct < 75) {
                    num6 += 2;
                }
                if (num2 >= 4 && !opponent.k() && opponent.p() > 0) {
                    int oppFoodPct = opponent.q() > 0 ? (opponent.p() * 100 / opponent.q()) : 100;
                    num6 += (oppFoodPct <= 40 ? 4 : 2);
                }
                break;
            case 2:
                int hpPct = self.s() > 0 ? (self.r() * 100 / self.s()) : 100;
                int oppHpPct = opponent.s() > 0 ? (opponent.r() * 100 / opponent.s()) : 100;
                if (hpPct >= 92) {
                    // At full HP, only value hearts slightly to deny a low-HP opponent.
                    num6 = (oppHpPct <= 35) ? 4 : 1;
                } else if (hpPct <= 25) {
                    num6 = 10;
                } else if (hpPct <= 55) {
                    num6 = 7;
                } else {
                    num6 = 4;
                }
                if (archetype == BattleFighter.ARCHETYPE_DEFENDER && hpPct < 85) {
                    num6 += 2;
                }
                break;
            case 3:
                if (self.o() >= self.n()) {
                    // Already at 36/36 full Mana.
                    num6 = 1;
                } else {
                    num6 = 4;
                    if (archetype == BattleFighter.ARCHETYPE_CASTER) {
                        num6 += 3;
                    }
                    if (opponent.k()) {
                        // Build Mana while opponent's Khiên Sét blocks sword attacks.
                        num6 += 3;
                    }
                    if ((self.o() % 12) >= 8) {
                        // Close to unlocking the next skill tier (12 / 24 / 36).
                        num6 += 3;
                    }
                }
                break;
            case 4:
            case 5:
            default:
                num6 = 1;
                break;
        }

        num6 += BattleConstants.j[num2];
        return num6;
    }

    /**
     * Context-aware AI spell selector. Evaluates available skill tiers (0..availableSkillCount-1)
     * against current HP, burn status, opponent's Khiên Sét, board Heart count, and lethal thresholds.
     * Returns the chosen skill tier index, or {@code -1} if the AI should swap gems / save Mana instead.
     */
    public int chooseBestAiSkillTier(int availableSkillCount, int bestMoveScore) {
        if (availableSkillCount < 1 || this.battleModel.d) {
            return -1;
        }
        int aiIndex = this.battleModel.a();
        int oppIndex = (aiIndex + 1) % 2;
        BattleFighter ai = this.battleModel.b(aiIndex);
        BattleFighter opp = this.battleModel.b(oppIndex);
        int[] skills = ai.d();
        if (skills == null || skills.length == 0) {
            return -1;
        }
        int maxTier = availableSkillCount > skills.length ? skills.length : availableSkillCount;
        int aiHpPct = ai.s() > 0 ? (ai.r() * 100 / ai.s()) : 100;
        int aiAtk = ai.m();

        // 1. Immediate lethal check: spells shred Barrier at 150% efficiency (2/3 damage needed for Barrier).
        int effectiveOppBarrier = (opp.getBarrierHp() * 2 + 2) / 3;
        for (int t = maxTier - 1; t >= 0; --t) {
            int sid = skills[t];
            int estDmg = this.estimateSkillDirectDamage(sid, aiAtk, opp);
            if (estDmg >= opp.r() + effectiveOppBarrier) {
                return t;
            }
        }

        // 2. If a lethal sword match (score >= 120) is on the board, take the free board kill.
        if (bestMoveScore >= 120) {
            return -1;
        }

        // 3. If a 4/5-in-a-row extra-turn match is on the board (score >= 28) and AI is not in urgent danger
        //    and not sitting at 36/36 max Mana, take the extra-turn board combo first!
        if (bestMoveScore >= 28 && aiHpPct > 35 && ai.o() < ai.n() && !opp.k()) {
            return -1;
        }

        // Count Heart gems on the 8x8 board for Cam Lộ Thủy evaluation.
        int heartGemsOnBoard = 0;
        int[][] grid = this.battleModel.e();
        for (int r = 2; r < 10; ++r) {
            for (int c = 2; c < 10; ++c) {
                if (grid[r][c] == 2) {
                    ++heartGemsOnBoard;
                }
            }
        }

        // 4. Evaluate available skills from highest tier down to lowest tier (supports both 0..8 and 9..17).
        int aiFoodPct = ai.q() > 0 ? (ai.p() * 100 / ai.q()) : 100;
        for (int t = maxTier - 1; t >= 0; --t) {
            int sid = skills[t];
            int baseType = sid % 9;
            switch (baseType) {
                case 7: // Cam Lộ Thủy (7) / Huyết Mộc Hấp Thụ (16): Cleanse + Heal 20% MaxHP + Absorb Hearts
                    if (aiHpPct <= 68 || ai.hasAnyDebuff() || (aiHpPct <= 85 && heartGemsOnBoard >= 8)) {
                        return t;
                    }
                    break;

                case 4: // Khiên Sét (4) / Yêu Giáp Hộ Thể (13): Shield + Cleanse + Board Synergy
                    if (this.shieldTurnsRemaining[aiIndex] <= 1) {
                        if (opp.o() >= 12 || aiHpPct <= 65 || ai.hasAnyDebuff() || RandomUtils.nextInt(100) < 70) {
                            return t;
                        }
                    }
                    break;

                case 2: // Lửa Địa Ngục (2) / Long Trời Lở Đất (11): Shatters opponent Khiên Sét + heavy blast
                    return t;

                case 8: // Băng Phong (8) / Tơ Độc Giam Cầm (17): Freeze + Mana drain + Board sabotage (8 also shatters shields!)
                    if (sid == 8 || !opp.k()) {
                        return t;
                    }
                    break;

                case 5: // Sấm Sét (5) / Bão Độc Ma Trơi (14)
                case 1: // Mưa Thiên Thạch (1) / Cướp Lương Thực (10)
                    if (!opp.k()) {
                        // Cướp Lương Thực (10) is cast immediately if AI is getting hungry!
                        if (sid == 10 && aiFoodPct <= 45) {
                            return t;
                        }
                        if (t == 1 && skills.length >= 3 && ai.o() >= 28 && aiHpPct > 40 && RandomUtils.nextInt(100) < 75) {
                            break;
                        }
                        return t;
                    }
                    break;

                case 0: // Quả Cầu Lửa (0) / Cuồng Trảo (9)
                case 3: // Chuỗi Sét (3) / Độc Chướng (12)
                case 6: // Mũi Tên Băng (6) / Nhiễu Hồn Tiễn (15)
                    if (!opp.k()) {
                        if (baseType == 6 && opp.o() >= 20) {
                            return t;
                        }
                        if (skills.length >= 2 && aiHpPct > 40) {
                            if (ai.o() >= 18) {
                                break;
                            }
                            if (RandomUtils.nextInt(100) < 55) {
                                break;
                            }
                        }
                        return t;
                    }
                    break;
            }
        }
        return -1;
    }

    private int estimateSkillDirectDamage(int skillId, int casterAtk, BattleFighter target) {
        int baseType = skillId % 9;
        if (target.k() && baseType != 2 && skillId != 8 && skillId != 6) {
            return 0;
        }
        int raw;
        switch (baseType) {
            case 0:
                raw = casterAtk;
                break;
            case 1:
                raw = (skillId == 10) ? (casterAtk * 13 / 10) : (casterAtk * 3 / 2);
                break;
            case 8:
                if (skillId == 8) {
                    if (target.k()) {
                        raw = casterAtk * 27 / 20;
                    } else {
                        raw = casterAtk * 11 / 5;
                        if (target.isArmorBroken() || (target.s() > 0 && (target.r() * 100 / target.s()) <= 35)) {
                            raw = raw * 125 / 100;
                        }
                    }
                } else {
                    raw = casterAtk * 3 / 2;
                }
                break;
            case 2:
                if (skillId == 11) {
                    raw = target.k() ? (casterAtk * 115 / 100) : (casterAtk * 23 / 10);
                } else {
                    raw = target.k() ? (casterAtk * 5 / 4) : (casterAtk * 5 / 2);
                }
                break;
            case 3:
                raw = casterAtk * 8 / 10;
                break;
            case 6:
                raw = (skillId == 6) ? (casterAtk * 6 / 5) : (casterAtk * 8 / 10);
                break;
            case 7:
                raw = (skillId == 7 && !target.k()) ? casterAtk : 0;
                break;
            case 5:
                raw = (skillId == 14) ? (casterAtk * 18 / 10) : (casterAtk * 2);
                break;
            default:
                return 0;
        }
        if (target.getBurnTurns() > 0) {
            raw = raw * 120 / 100;
        }
        if (target.isArmorBroken()) {
            raw = raw * 125 / 100;
        }
        if (target.a != null && target.a.length > 3 && target.a[3].b > 0) {
            raw /= 4;
        }
        return raw;
    }

    public void c(int num1) {
        BattleFighter fighter2 = this.battleModel.b(this.battleModel.a());
        // Trọng Thương: tăng +75% mức lương thực tiêu hao mỗi hành động
        int foodCost = fighter2.isWounded() ? (num1 * 175 / 100) : num1;
        fighter2.j(foodCost);
    }

    public void f(int num1, int num2) {
    }

    public void a(String str1, int num2) {
        if (str1.equals("skill")) {
            this.a(num2);
        } else {
            this.d(num2);
            this.itemUsedThisTurn = true;
            if (this.battleModel.d) {
                this.stageView.g.c();
                this.f(13);
            }
        }

    }

    public void d(int num1) {
        BattleFighter fighter2 = this.battleModel.b(this.battleModel.a());
        ItemSlot[] slot3 = fighter2.a;

        for(int num4 = 0; num4 < slot3.length; ++num4) {
            if (num1 == slot3[num4].a) {
                slot3[num4].a();
                if (slot3[num4].a == 1) {
                    fighter2.n(fighter2.e() * 2);
                    fighter2.l(fighter2.e());
                    this.surrendered = true;
                } else if (slot3[num4].a == 4) {
                    fighter2.reduceDebuffsByOneTurn();
                    this.b(fighter2, fighter2.s() * 1 / 10);
                    this.stageView.d(this.battleModel.a());
                } else if (num1 == 3) {
                    slot3[num4].b = 2;
                    this.stageView.e(true);
                } else if (num1 == 0) {
                    slot3[num4].b = 1;
                    this.stageView.d(true);
                }
                break;
            }
        }

        BattleResourceManager.a().h();
    }

    public void r() {
        if (!this.battleModel.d || this.stageView.g.g() == 3) {
            int num1 = this.d();
            this.stageView.a(num1, this, this.battleModel.b(this.battleModel.a()), this.battleModel.h());
        }
    }

    public void s() {
        int num1 = this.battleModel.c();
        if (num1 != 1 || this.stageView.g.g() == 5) {
            if (!this.itemUsedThisTurn) {
                if (this.surrendered) {
                    this.battleModel.a[1].b = 0;
                }

                this.stageView.e.a(this.battleModel.a, this, this.battleModel.b(0));
            }
        }
    }

    public void onAction(int num1, int num2) {
        switch (num2) {
            case -8:
            case -7:
            default:
                break;
            case -6:
                GUIManager.getInstance().a(false);
                this.earnedExtraTurn = true;
                this.f(12);
                break;
            case -5:
            case -4:
                GUIManager.getInstance().a(false);
                GUIManager.getController().a(5, 7, true, true);
                break;
            case -3:
                CampaignData.j();
                GUIManager.getInstance().a(false);
                this.a();
                break;
            case -2:
                this.stageView.a(true);
                GUIManager.getInstance().a(true);
                break;
            case -1:
                GUIManager.getInstance().a(false);
                this.u();
                GUIManager.getController().a(5, 2, true, false);
        }

    }

    public void t() {
        BattleFighter[] fighter1 = this.battleModel.d();

        for(int num2 = 0; num2 < fighter1.length; ++num2) {
            fighter1[num2].i();
        }

        CampaignStorage.clearBattleSnapshot();
        if (this.earnedExtraTurn) {
            CampaignData.i();
        } else {
            CampaignData.j();
        }

    }

    public int a(int num1, int num2, int num3, int num4, int num5) {
        if (num1 != 0) {
            return num1;
        } else {
            int[][] num6 = this.battleModel.e();

            for(int num7 = num3 - num4; num7 < num3; ++num7) {
                if (num6[num2][num7] == 6) {
                    return 6;
                }
            }

            for(int num8 = num3 + num5; num8 > num3; --num8) {
                if (num6[num2][num8] == 6) {
                    return 6;
                }
            }

            return 0;
        }
    }

    public int b(int num1, int num2, int num3, int num4, int num5) {
        if (num1 != 0) {
            return num1;
        } else {
            int[][] num6 = this.battleModel.e();

            for(int num7 = num2 - num4; num7 < num2; ++num7) {
                if (num6[num7][num3] == 6) {
                    return 6;
                }
            }

            for(int num8 = num2 + num5; num8 > num2; --num8) {
                if (num6[num8][num3] == 6) {
                    return 6;
                }
            }

            return 0;
        }
    }

    public boolean e(int num1) {
        return RandomUtils.nextInt(100) <= this.battleModel.b(num1).c();
    }
}





