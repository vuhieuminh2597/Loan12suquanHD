package com.mic.smsgame.suquan.data;

import com.mg.engine.core.GUIManager;
import com.mg.engine.storage.CampaignStorage;
import com.mg.engine.storage.EngineStorage;
import com.mg.engine.ui.BaseDialog;
import com.mg.engine.util.RandomUtils;
import com.mg.engine.util.StringUtils;
import com.mic.smsgame.suquan.model.HeroStats;
import com.mic.smsgame.suquan.ui.BattleResultDialog;

/**
 * Campaign progression tables (main/side quests, warlord & monster stat templates, arena tiers),
 * EXP/level formulas, and active battle session state persisted via {@link CampaignStorage}.
 */
public class CampaignData {
    /** Active arena opponent index ({@code 0..4}), or {@code -1} when not in arena mode. */
    public static int a = -1;
    /** Player's saved pre-arena HP restored after an arena match. */
    public static int b = 0;
    /** Active main-quest ID, or {@code -1}. */
    public static int c = -1;
    /** Active side-quest ID, or {@code -1}. */
    public static int d = -1;
    /** Player hero persistent stats. */
    public static HeroStats e;
    /** Current enemy combatant stats. */
    public static HeroStats f;
    /** Battle countdown timer limit in ticks ({@code -1} = untimed). */
    public static int g = -1;
    /** True if the player moves first in the upcoming battle. */
    public static boolean h = false;
    /** Gold collected during the current battle. */
    public static int i = 0;
    /** EXP collected during the current battle. */
    public static int j = 0;
    /** Enhancement Shards dropped from 5+ gem matches during the current battle (granted on win only). */
    public static int battleDroppedShards = 0;
    /** Bonus Enhancement Shards rolled upon winning an eligible (±12 level) battle. */
    public static int battleWinBonusShards = 0;
    /** True when entering a battle session; kept for world-map defeat cleanup compatibility. */
    public static boolean k = false;

    /** Main quest table: {@code {mode, nodeIndex, enemyTemplateId, rewardExp, rewardGold, milestoneType}}. */
    private static final int[][] MAIN_QUESTS = new int[][]{
            {1, 0, 8, 30, 10, 0}, {0, 0, -1, 0, 0, 0}, {0, 1, -1, 40, 20, 1}, {0, 0, -1, 0, 0, 0},
            {2, 24, 42, 50, 30, 1}, {0, 0, -1, 0, 0, 0}, {0, 3, 18, 70, 50, 1}, {0, 0, -1, 0, 0, 0},
            {2, 2, 22, 100, 80, 1}, {0, 0, -1, 0, 0, 0}, {2, 8, 27, 150, 120, 1}, {0, 8, -1, 0, 0, 0},
            {2, 3, 20, 150, 120, 1}, {0, 3, -1, 0, 0, 0}, {2, 27, 21, 50, 50, 0}, {2, 5, 20, 200, 150, 1},
            {0, 3, -1, 0, 0, 0}, {2, 25, 25, 50, 50, 0}, {2, 4, 24, 300, 170, 1}, {0, 4, -1, 0, 0, 0},
            {2, 4, 29, 400, 200, 1}, {0, 4, -1, 0, 0, 0}, {2, 7, 29, 500, 250, 0}, {2, 26, 28, 700, 300, 1},
            {0, 7, -1, 0, 0, 0}, {2, 18, 26, 900, 350, 1}, {0, 5, -1, 0, 0, 0}, {2, 6, 31, 1200, 450, 0},
            {2, 19, 30, 1500, 500, 1}, {0, 6, -1, 0, 0, 0}, {2, 10, 32, 1800, 550, 1}, {0, 10, -1, 0, 0, 0},
            {2, 28, 35, 1600, 500, 1}, {2, 9, 34, 2100, 600, 1}, {0, 9, -1, 0, 0, 0}, {2, 39, 37, 1800, 500, 0},
            {2, 11, 36, 2500, 650, 1}, {0, 11, -1, 0, 0, 0}, {2, 1, 33, 3000, 750, 1}, {0, 12, -1, 0, 0, 0},
            {2, 30, 41, 2500, 500, 0}, {2, 13, 38, 4000, 850, 1}, {2, 13, 40, 5000, 950, 1}, {0, 13, -1, 0, 0, 0},
            {2, 0, 74, 0, 0, 2}, {0, 0, -1, 0, 0, 0}
    };

    /** Map node indices unlocked upon completing each main quest. */
    private static final int[][] QUEST_UNLOCKED_NODES = new int[][]{
            null, {1, 32, 33}, null, {24}, null, {3, 34}, null, {22, 23, 31, 2}, null,
            {8, 18, 36, 8, 16, 15}, null, {5, 27}, null, {17}, null, null, {25, 4, 14}, null, null,
            {7, 35}, null, null, {26}, null, null, null, {37, 6, 17}, {19}, null, {38, 10}, null,
            {9, 28, 20, 21}, null, null, {11, 39}, null, null, {12, 29}, null, {30, 13}, null,
            null, null, {40}, null, null
    };

    /** Side quest table: {@code {mode, nodeIndex, enemyTemplateId, rewardItemId, rewardCount}}. */
    private static final int[][] SIDE_QUESTS = new int[][]{
            {2, 23, 47, 0, 10}, {2, 16, 9, 3, 0}, {2, 15, 12, 0, 20}, {2, 14, 14, 2, 20},
            {2, 22, 13, 1, 10}, {0, 17, -1, 3, 0}, {0, 20, -1, 0, 0}, {2, 21, 73, 3, 50}
    };

    /** Character and monster templates: {@code {baseLevel, bonusHp, bonusAttack, skillIds...}}. */
    private static final int[][] HERO_TEMPLATES = new int[][]{
            {1, 0, 0, 0, 1, 2}, {1, 0, 0, 3, 4, 5}, {1, 0, 0, 6, 7, 8},
            {1, 0, 0, 0, 1, 2}, {1, 0, 0, 3, 4, 5}, {1, 0, 0, 6, 7, 8},
            {1, 60, 5, 6, 7, 8}, {1, 30, 2, 0, 1, 2}, {1, 0, 0, 3, 4, 5},
            {20, 60, 4, 3, 4, 5}, {1, 0, 0, 0, 1, 2}, {1, 0, 0, 0, 1, 2},
            {25, 75, 5, 6, 7}, {15, 45, 3, 0, 3}, {30, 90, 6, 3, 4},
            {1, 0, 0, 0, 1, 2}, {1, 0, 0, 0, 1, 2}, {1, 0, 0, 3, 4, 5},
            {1, 0, 0, 0, 1, 2}, {1, 0, 0, 0, 1, 2}, {40, 60, 5, 3, 4, 5},
            {25, 0, 0, 3, 4}, {20, 30, 3, 3, 4}, {10, 0, 0, 6, 7},
            {50, 80, 6, 6, 7, 8}, {30, 0, 0, 6, 7}, {40, 50, 5, 0, 1, 2},
            {30, 40, 4, 0, 1}, {60, 100, 8, 0, 1, 2}, {45, 60, 6, 3, 4, 5},
            {70, 120, 9, 6, 7, 8}, {45, 0, 0, 3, 4, 5}, {80, 150, 10, 3, 4, 5},
            {60, 80, 7, 3, 4, 5}, {90, 220, 12, 0, 1, 2}, {55, 0, 0, 6, 7, 8},
            {70, 130, 9, 0, 1, 2}, {60, 60, 6, 0, 1, 2}, {95, 350, 16, 3, 4, 5},
            {65, 40, 4, 6, 7, 8}, {99, 550, 22, 6, 7, 8}, {75, 50, 5, 6, 7, 8},
            {7, 0, 0, 9}, {3, 0, 0, 12}, {6, 0, 0, 12}, {9, 0, 0, 15},
            {12, 0, 0, 9, 10}, {15, 0, 0, 12, 13}, {18, 0, 0, 15, 16}, {21, 0, 0, 9, 10},
            {24, 0, 0, 12, 13}, {27, 0, 0, 9, 10}, {30, 0, 0, 9, 10, 11}, {33, 0, 0, 12, 13, 14},
            {36, 0, 0, 12, 13, 14}, {39, 0, 0, 9, 10, 11}, {42, 0, 0, 9, 10, 11}, {45, 0, 0, 9, 10, 11},
            {48, 0, 0, 15, 16, 17}, {51, 0, 0, 15, 16, 17}, {54, 0, 0, 9, 10, 11}, {57, 0, 0, 12, 13, 14},
            {60, 0, 0, 12, 13, 14}, {63, 0, 0, 12, 13, 14}, {66, 0, 0, 12, 13, 14}, {69, 0, 0, 9, 10, 11},
            {72, 0, 0, 15, 16, 17}, {75, 0, 0, 9, 10, 11}, {78, 0, 0, 12, 13, 14}, {81, 0, 0, 12, 13, 14},
            {84, 0, 0, 15, 16, 17}, {87, 0, 0, 12, 13, 14}, {90, 0, 0, 15, 16, 17}, {68, 120, 8, 15, 16, 17},
            {96, 1000, 40, 9, 16, 11}, {1, 0, 0}, {40, 0, 0, 0, 1, 2}
    };

    /** Guard template ID stationed at each world-map node ({@code -1} if unguarded). */
    private static final int[] NODE_GUARD_HERO_IDS = new int[]{
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, 25, 75, 21, 35, -1, 41, 23, 75, 75, 75, 39, 75, 31, 76, 37, -1
    };

    /** Arena opponents: {@code {templateId, levelOffset, expPercentOfLevel, entryFeeGold}}. */
    private static final int[][] ARENA_OPPONENTS = new int[][]{
            {8, -5, 5, 50}, {7, 0, 10, 80}, {9, 5, 20, 140}, {14, 10, 40, 230}, {6, 20, 100, 500}
    };

    public static final void a(int battleStatus) {
        CampaignStorage.writeInt(101, battleStatus);
    }

    public static final int a() {
        return CampaignStorage.readInt(101);
    }

    public static final void b(int totalExp) {
        CampaignStorage.writeInt(103, totalExp);
    }

    public static final int b() {
        return CampaignStorage.readInt(103);
    }

    public static final String c() {
        String name = CampaignStorage.readString(104);
        if (StringUtils.isBlank(name)) {
            name = "PLAYER";
        }
        return name;
    }

    public static final void a(String playerName) {
        if (playerName == null) {
            playerName = "PLAYER";
        }
        CampaignStorage.writeString(104, playerName);
    }

    public static final int d() {
        return CampaignStorage.readInt(105);
    }

    public static final void c(int heroAvatarId) {
        CampaignStorage.writeInt(105, heroAvatarId);
    }

    public static final int e() {
        return MAIN_QUESTS.length;
    }

    public static final int d(int questId) {
        try {
            return MAIN_QUESTS[questId][0];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return 0;
        }
    }

    public static final int e(int questId) {
        try {
            return MAIN_QUESTS[questId][1];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return 0;
        }
    }

    public static final boolean f(int questId) {
        try {
            return MAIN_QUESTS[questId][5] == 1;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    public static final boolean g(int questId) {
        try {
            return MAIN_QUESTS[questId][5] == 2;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    public static final HeroStats h(int questId) {
        try {
            return MAIN_QUESTS[questId][2] < 0 ? null : t(MAIN_QUESTS[questId][2]);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return null;
        }
    }

    public static final int i(int questId) {
        try {
            return MAIN_QUESTS[questId][3] * 3 / 2;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return 0;
        }
    }

    public static final int j(int questId) {
        try {
            return MAIN_QUESTS[questId][4];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return 0;
        }
    }

    public static final boolean k(int questId) {
        try {
            return MAIN_QUESTS[questId][3] != 0 || MAIN_QUESTS[questId][4] != 0;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    public static final int[] l(int questId) {
        return QUEST_UNLOCKED_NODES[questId];
    }

    public static final int f() {
        return SIDE_QUESTS.length;
    }

    public static final boolean m(int sideQuestId) {
        try {
            if (sideQuestId >= 0 && sideQuestId < SIDE_QUESTS.length) {
                return SIDE_QUESTS[sideQuestId][3] != 0 || SIDE_QUESTS[sideQuestId][4] != 0;
            }
            return false;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    public static final int n(int sideQuestId) {
        try {
            return SIDE_QUESTS[sideQuestId][3];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return 0;
        }
    }

    public static final int o(int sideQuestId) {
        try {
            return SIDE_QUESTS[sideQuestId][4];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return 0;
        }
    }

    public static final int p(int sideQuestId) {
        try {
            return SIDE_QUESTS[sideQuestId][0];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return 0;
        }
    }

    public static final int q(int sideQuestId) {
        try {
            return SIDE_QUESTS[sideQuestId][1];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return 0;
        }
    }

    public static final int r(int sideQuestId) {
        return SIDE_QUESTS[sideQuestId][2];
    }

    public static final HeroStats s(int sideQuestId) {
        try {
            return SIDE_QUESTS[sideQuestId][2] < 0 ? null : t(SIDE_QUESTS[sideQuestId][2]);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return null;
        }
    }

    public static final HeroStats a(int nodeIndex, int referenceLevel) {
        if (nodeIndex < 0 || nodeIndex >= NODE_GUARD_HERO_IDS.length) {
            return null;
        }
        int guardTemplateId = NODE_GUARD_HERO_IDS[nodeIndex];
        if (guardTemplateId < 0) {
            return null;
        }
        return guardTemplateId == 75 ? y(referenceLevel) : t(guardTemplateId);
    }

    public static final HeroStats t(int templateId) {
        return b(templateId, -1);
    }

    public static final HeroStats b(int templateId, int customLevel) {
        if (templateId < 0 || templateId >= HERO_TEMPLATES.length) {
            return null;
        }
        HeroStats hero = new HeroStats();
        hero.a = templateId;
        hero.e = customLevel < 0 ? HERO_TEMPLATES[templateId][0] : customLevel;
        if (hero.isPlayableHero()) {
            hero.b();
            hero.c = hero.b;
        } else {
            hero.c = hero.b = w(hero.e) + HERO_TEMPLATES[templateId][1];
            hero.d = v(hero.e) + HERO_TEMPLATES[templateId][2];
        }
        int skillCount = HERO_TEMPLATES[templateId].length - 3;
        hero.f = new int[skillCount];
        for (int s = 0; s < skillCount; ++s) {
            hero.f[s] = HERO_TEMPLATES[templateId][s + 3];
        }
        return hero;
    }

    public static final boolean c(int targetLevel, int currentExp) {
        return currentExp >= u(targetLevel);
    }

    /** Returns cumulative EXP required to reach {@code level}. */
    public static final int u(int level) {
        return level <= 1 ? 0 : 16 * level * level - 25;
    }

    /** Returns base attack at {@code level}. */
    public static final int v(int level) {
        return level / 2 + 5;
    }

    /** Returns base max HP at {@code level}. */
    public static final int w(int level) {
        return 100 + level * 5;
    }

    /** Base victory bonus EXP by opponent level and type (Warlord/Hero vs Monster). */
    public static final int d(int enemyLevel, int enemyTemplateId) {
        if (enemyLevel < 1) {
            enemyLevel = 1;
        }
        return enemyTemplateId < 42
                ? (enemyLevel * enemyLevel * 2 / 10 + enemyLevel * 6 + 32)
                : (enemyLevel * enemyLevel / 10 + enemyLevel * 5 + 22);
    }

    /** Base victory bonus Gold by opponent level and type (Warlord/Hero vs Monster). */
    public static final int computeBaseWinGold(int enemyLevel, int enemyTemplateId) {
        if (enemyLevel < 1) {
            enemyLevel = 1;
        }
        return enemyTemplateId < 42
                ? (enemyLevel * 6 + 40)
                : (enemyLevel * 4 + 25);
    }

    /**
     * Scales base win-bonus rewards smoothly across level differences:
     * tapers gently below -5 levels instead of dropping abruptly to zero, and grants up to
     * +60% for beating stronger opponents plus up to +40% when finishing with >70% HP.
     */
    private static int scaleWinBonusReward(
            int baseReward, HeroStats player, HeroStats enemy,
            int underLevelFloorPct, int minUnderLevelReward) {
        if (baseReward <= 0 || player == null || enemy == null) {
            return 0;
        }
        int levelDiff = enemy.e - player.e;
        if (levelDiff < -5) {
            int stepsBelow = -5 - levelDiff;
            int scalePct = 100 - stepsBelow * 10;
            if (scalePct < underLevelFloorPct) {
                scalePct = underLevelFloorPct;
            }
            int scaled = baseReward * scalePct / 100;
            return scaled < minUnderLevelReward ? minUnderLevelReward : scaled;
        }
        if (levelDiff == 0) {
            // Equal-level clean finish (>75% HP) grants up to +20% mastery bonus
            int hpPct = player.b > 0 ? (player.c * 100 / player.b) : 0;
            if (hpPct > 75) {
                int cleanBonusPct = hpPct - 75;
                if (cleanBonusPct > 20) {
                    cleanBonusPct = 20;
                }
                return baseReward + (cleanBonusPct * baseReward / 100);
            }
            return baseReward;
        }
        if (levelDiff < 0) {
            return baseReward;
        }
        int cappedDiff = levelDiff > 12 ? 12 : levelDiff;
        int totalReward = baseReward + (cappedDiff * 5 * baseReward / 100);
        int hpPercent = player.b > 0 ? (player.c * 100 / player.b) : 0;
        if (hpPercent > 70) {
            int hpBonusPct = hpPercent - 60;
            if (hpBonusPct > 40) {
                hpBonusPct = 40;
            }
            totalReward += hpBonusPct * baseReward / 100;
        }
        return totalReward;
    }

    /** Computes victory bonus EXP based on opponent level/type, level difference, and remaining player HP%. */
    public static final int a(HeroStats player, HeroStats enemy) {
        if (enemy == null) {
            return 0;
        }
        return scaleWinBonusReward(d(enemy.e, enemy.a), player, enemy, 20, 5);
    }

    /** Computes victory bonus Gold based on opponent level/type, level difference, and remaining player HP%. */
    public static final int computeWinBonusGold(HeroStats player, HeroStats enemy) {
        if (enemy == null) {
            return 0;
        }
        return scaleWinBonusReward(computeBaseWinGold(enemy.e, enemy.a), player, enemy, 30, 10);
    }

    /**
     * Computes gold required to rest and restore 100% HP at a friendly citadel,
     * scaled proportionally by missing HP percentage and hero level (min 10 gold).
     */
    public static final int computeCitadelRestCost(HeroStats player) {
        if (player == null || player.b <= 0 || player.c >= player.b) {
            return 0;
        }
        int missingHp = player.b - player.c;
        int missingPct = (missingHp * 100 + player.b - 1) / player.b;
        int fullHealCost = 10 + player.e * 2;
        int cost = fullHealCost * missingPct / 100;
        return cost < 10 ? 10 : cost;
    }

    /** Computes a light EXP penalty upon defeat scaled to current level (never drops below level floor). */
    public static final int a(HeroStats player, HeroStats enemy, int currentExp) {
        if (player == null) {
            return 0;
        }
        int levelFloorExp = u(player.e);
        int availableExp = currentExp - levelFloorExp;
        if (availableExp <= 0) {
            return 0;
        }
        int levelSpan = u(player.e + 1) - levelFloorExp;
        int penalty = levelSpan * 5 / 100;
        if (penalty < 1) {
            penalty = 1;
        }
        if (penalty > availableExp) {
            penalty = availableExp;
        }
        return penalty;
    }

    /** Computes a small gold penalty upon defeat based on player level (clamped to current gold). */
    public static final int computeDefeatGoldPenalty(HeroStats player, int currentGold) {
        if (player == null || currentGold <= 0) {
            return 0;
        }
        int penalty = player.e * 5 + 5;
        if (penalty > currentGold) {
            penalty = currentGold;
        }
        return penalty;
    }

    /** Spawns a level-tiered random monster encounter appropriate for {@code roadIndex}. */
    public static final HeroStats x(int roadIndex) {
        int level = RandomUtils.nextRange(WorldMapData.d[roadIndex][2], WorldMapData.d[roadIndex][3]);
        if (level < 1) {
            level = 1;
        }
        int monsterTemplateId;
        if (level <= 20) {
            monsterTemplateId = RandomUtils.nextRange(43, 51);
        } else if (level <= 55) {
            monsterTemplateId = RandomUtils.nextRange(52, 62);
        } else {
            monsterTemplateId = RandomUtils.nextRange(63, 73);
        }
        return b(monsterTemplateId, level);
    }

    public static final HeroStats y(int referenceLevel) {
        int level = referenceLevel - RandomUtils.nextInt(6);
        if (level < 1) {
            level = 1;
        }
        HeroStats guard = b(75, level);
        if (guard != null) {
            if (level >= 45) {
                guard.f = new int[]{9, 10};
            } else if (level >= 15) {
                guard.f = new int[]{9};
            }
        }
        return guard;
    }

    public static final int z(int nodeIndex) {
        return NODE_GUARD_HERO_IDS[nodeIndex];
    }

    /** Returns the main quest combat enemy template ID, or {@code -1} if dialogue-only/invalid. */
    public static final int getMainQuestEnemyTemplateId(int questId) {
        if (questId < 0 || questId >= MAIN_QUESTS.length || MAIN_QUESTS[questId][0] == 0) {
            return -1;
        }
        return MAIN_QUESTS[questId][2];
    }

    /** Allocation-free lookup of a main quest combat enemy's level, or {@code -1} if non-combat. */
    public static final int getMainQuestEnemyLevel(int questId) {
        int tid = getMainQuestEnemyTemplateId(questId);
        return (tid >= 0 && tid < HERO_TEMPLATES.length) ? HERO_TEMPLATES[tid][0] : -1;
    }

    /** Returns the side quest combat enemy template ID, or {@code -1} if dialogue-only/invalid. */
    public static final int getSideQuestEnemyTemplateId(int sideQuestId) {
        if (sideQuestId < 0 || sideQuestId >= SIDE_QUESTS.length || SIDE_QUESTS[sideQuestId][0] == 0) {
            return -1;
        }
        return SIDE_QUESTS[sideQuestId][2];
    }

    /** Allocation-free lookup of a side quest combat enemy's level, or {@code -1} if non-combat. */
    public static final int getSideQuestEnemyLevel(int sideQuestId) {
        int tid = getSideQuestEnemyTemplateId(sideQuestId);
        return (tid >= 0 && tid < HERO_TEMPLATES.length) ? HERO_TEMPLATES[tid][0] : -1;
    }

    /** Allocation-free lookup of a stationed node guard's effective level, or {@code -1} if unguarded. */
    public static final int getNodeGuardLevel(int nodeIndex, int heroLevel) {
        if (nodeIndex < 0 || nodeIndex >= NODE_GUARD_HERO_IDS.length) {
            return -1;
        }
        int gid = NODE_GUARD_HERO_IDS[nodeIndex];
        if (gid < 0) {
            return -1;
        }
        if (gid == 75) {
            return heroLevel > 0 ? heroLevel : 1;
        }
        return gid < HERO_TEMPLATES.length ? HERO_TEMPLATES[gid][0] : -1;
    }

    /** Threat tier comparing {@code enemyLevel} to {@code heroLevel}: 0=Safe, 1=Even, 2=Hard, 3=Danger. */
    public static final int getThreatTier(int heroLevel, int enemyLevel) {
        int diff = enemyLevel - heroLevel;
        if (diff <= -3) {
            return 0;
        }
        if (diff <= 3) {
            return 1;
        }
        if (diff <= 8) {
            return 2;
        }
        return 3;
    }

    public static final int getThreatColor(int tier) {
        switch (tier) {
            case 0:
                return 0x7CFF9E;
            case 1:
                return 0xFFE680;
            case 2:
                return 0xFFAA44;
            default:
                return 0xFF6B6B;
        }
    }

    public static final String getThreatLabel(int tier) {
        switch (tier) {
            case 0:
                return "An toàn";
            case 1:
                return "Cân sức";
            case 2:
                return "Khó";
            default:
                return "Nguy hiểm!";
        }
    }

    public static final BaseDialog a(boolean questRewardMode) {
        return new BattleResultDialog(questRewardMode);
    }

    public static final BaseDialog g() {
        int currentExp = b();
        int currentGold = EngineStorage.f(2);
        int expPenalty = a(e, f, currentExp);
        int goldPenalty = computeDefeatGoldPenalty(e, currentGold);
        String msg = "Bạn đã thất bại.";
        if (expPenalty > 0 && goldPenalty > 0) {
            msg = msg + " Bị trừ {e} kinh nghiệm và {g} vàng.";
        } else if (expPenalty > 0) {
            msg = msg + " Bị trừ {e} kinh nghiệm.";
        } else if (goldPenalty > 0) {
            msg = msg + " Bị trừ {g} vàng.";
        }
        if (a < 0) {
            msg = msg + " Bạn có muốn đánh lại không?";
        }
        msg = StringUtils.replace(msg, "{e}", "" + expPenalty);
        msg = StringUtils.replace(msg, "{g}", "" + goldPenalty);
        return GUIManager.a("Chú ý", msg, null, null, 1);
    }

    /**
     * Returns true if {@code templateId} is a named Boss/Warlord/Side-Quest Boss/Arena Boss
     * that activates specialized Boss Archetype mechanics and the tactical Boss Board
     * (replacing Gold & EXP tiles with Ngọc Hộ Giáp & Ngọc Phá Giáp).
     * Regular road monsters and unnamed outpost guards return false so players still farm Gold/EXP tiles on roads.
     */
    public static final boolean isBossTemplate(int templateId) {
        switch (templateId) {
            case 6:  // Đinh Bộ Lĩnh (Đấu Trường hạng 5)
            case 7:  // Nguyễn Bặc (Đấu Trường hạng 2)
            case 8:  // Lưu Cơ (Đấu Trường hạng 1)
            case 9:  // Lê Hoàn (Nhiệm vụ phụ & Đấu Trường hạng 3)
            case 12: // Nguyễn Minh Quang (La Hán Trận)
            case 13: // Bùi Quang Dũng (Nhiệm vụ phụ)
            case 14: // Nguyễn Ninh (Nhiệm vụ phụ & Đấu Trường hạng 4)
            case 18: // Phạm Bạch Hổ (Đằng Châu)
            case 20: // Lã Đường (Tế Giang)
            case 22: // Ngô Xương Xí (Bình Kiều)
            case 24: // Đỗ Cảnh Thạc (Đỗ Động)
            case 26: // Tàn Tướng Đỗ Động (Ô Man)
            case 27: // Ngô Nhật Khánh (Đường Lâm)
            case 28: // Nguyễn Siêu (Tây/Đông Phù Liệt)
            case 29: // Nguyễn Trí Khả (Tiên phong Phù Liệt)
            case 30: // Lý Khuê (Siêu Loại / Dương Xá)
            case 32: // Nguyễn Thủ Tiệp (Tiên Du)
            case 33: // Phản Tướng (Kỳ Bố)
            case 34: // Lữ Xử Bình (Cổ Loa)
            case 36: // Nguyễn Khoan (Tam Đái)
            case 38: // Kiều Công Hãn (Phong Châu)
            case 40: // Kiều Thuận (Hồi Hồ)
            case 73: // Nhện Tinh (Đại yêu Tam Đảo)
            case 74: // Ấu Rồng (Boss cuối Hoa Lư)
                return true;
            case 47: // Ma Nhãn (Yêu quái Sơn Trang trong Nhiệm vụ phụ #0)
                return d == 0;
            default:
                return false;
        }
    }

    /** Returns true if an encounter is within ±12 levels of the player (eligible for Enhancement Shard drops). */
    public static final boolean isShardDropEligible(HeroStats player, HeroStats enemy) {
        if (player == null || enemy == null) {
            return false;
        }
        int diff = enemy.e - player.e;
        if (diff < 0) {
            diff = -diff;
        }
        return diff <= 12;
    }

    /** Returns Enhancement Shard reward for completing main quest {@code questId} (1 for standard, 2 for major boss, 0 otherwise). */
    public static final int getMainQuestShardReward(int questId) {
        if (questId < 0 || questId >= MAIN_QUESTS.length) {
            return 0;
        }
        int milestone = MAIN_QUESTS[questId][5];
        if (milestone == 0) {
            return 0;
        }
        if (milestone == 2 || questId == 18 || questId == 23 || questId == 28
                || questId == 30 || questId == 33 || questId == 41 || questId == 42) {
            return 2;
        }
        return 1;
    }

    public static final void h() {
        a(1);
        k = true;
    }

    public static final void i() {
        a(2);
        int totalExp = b();
        if (a >= 0) {
            e.c = b;
            totalExp += j;
        } else {
            if (e.c > e.b) {
                e.c = e.b;
            }
            totalExp += a(e, f) + j;
            EngineStorage.a(2, i + computeWinBonusGold(e, f), 6);
        }
        b(totalExp);
        while (e.e < HeroStats.MAX_LEVEL && c(e.e + 1, totalExp)) {
            e.a();
        }
        int totalEarnedShards = battleDroppedShards + battleWinBonusShards;
        if (totalEarnedShards > 0) {
            EngineStorage.addEnhancementShards(totalEarnedShards);
        }
        battleDroppedShards = 0;
        battleWinBonusShards = 0;
    }

    public static final void j() {
        a(3);
        int currentExp = b();
        int expPenalty = a(e, f, currentExp);
        if (expPenalty > 0) {
            b(currentExp - expPenalty);
        }
        int currentGold = EngineStorage.f(2);
        int goldPenalty = computeDefeatGoldPenalty(e, currentGold);
        if (goldPenalty > 0) {
            EngineStorage.b(2, goldPenalty);
        }
        if (a < 0) {
            e.c = e.b;
        } else {
            e.c = b;
        }
        j = 0;
        i = 0;
        battleDroppedShards = 0;
        battleWinBonusShards = 0;
    }

    public static final int[] k() {
        int[] ids = new int[ARENA_OPPONENTS.length];
        for (int idx = 0; idx < ids.length; ++idx) {
            ids[idx] = ARENA_OPPONENTS[idx][0];
        }
        return ids;
    }

    public static final HeroStats e(int arenaTier, int playerLevel) {
        int opponentLevel = playerLevel + ARENA_OPPONENTS[arenaTier][1];
        if (opponentLevel < 1) {
            opponentLevel = 1;
        }
        return b(ARENA_OPPONENTS[arenaTier][0], opponentLevel);
    }

    public static final int A(int arenaTier) {
        return ARENA_OPPONENTS[arenaTier][3];
    }

    public static final int f(int arenaTier, int playerLevel) {
        return (u(playerLevel + 1) - u(playerLevel)) * ARENA_OPPONENTS[arenaTier][2] / 100;
    }

    public static final int B(int arenaTier) {
        return ARENA_OPPONENTS[arenaTier][2];
    }
}
