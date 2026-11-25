package com.mg.engine.storage;

import com.mg.engine.core.DebugLogForm;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.util.ByteUtils;
import com.mg.engine.util.RandomUtils;
import java.io.IOException;
import java.io.InputStream;

/**
 * Persistent storage manager for engine preferences, extra lives, gold,
 * battle item inventory, and the 4-relic collection board.
 */
public class EngineStorage {
    public static int a = 10;
    public static RmsBank b = RmsBank.open("16b", 16);
    public static RmsBank c = RmsBank.open("161b", 161);

    public static int a(byte[] buffer, InputStream stream, int startOffset) throws IOException {
        int offset = startOffset;
        while (offset < buffer.length) {
            int bytesRead = stream.read(buffer, offset, buffer.length - offset);
            if (bytesRead < 0) {
                return -1;
            }
            offset += bytesRead;
        }
        return offset;
    }

    public static final boolean g() {
        return RmsBank.exists(b, 14);
    }

    public static final byte[] h() {
        return RmsBank.read(b, 14);
    }

    public static void b(byte[] data) {
        RmsBank.write(b, 14, data, 0, data.length);
        RmsBank.flush(b);
    }

    public static void i() {
        if (!GameConfig.rmsPersistenceEnabled) {
            return;
        }
        GameConfig.playCount = 0;
        byte[] raw = RmsBank.read(b, 8);
        if (raw != null && raw.length >= 4) {
            GameConfig.playCount = ByteUtils.a(raw);
        }
        ++GameConfig.playCount;
        raw = ByteUtils.a(GameConfig.playCount);
        RmsBank.write(b, 8, raw, 0, raw.length);
        RmsBank.flush(b);
    }

    private static int readAndDecrementInitialLives() {
        int lives;
        byte[] raw = RmsBank.read(b, 7);
        if (raw != null) {
            lives = ByteUtils.a(raw);
        } else {
            lives = GameConfig.defaultExtraLives;
        }
        if (lives < 1) {
            lives = 1;
        }
        if (lives > GameConfig.defaultExtraLives) {
            lives = GameConfig.defaultExtraLives;
        }
        RmsBank.write(b, 7, ByteUtils.a(lives > 1 ? lives - 1 : 1), 0, 4);
        RmsBank.flush(b);
        return lives;
    }

    public static boolean j() {
        return !RmsBank.exists(b, 4);
    }

    public static void k() {
        if (j()) {
            RmsBank.write(b, 4, new byte[]{1}, 0, 1);
        }
        RmsBank.write(b, 5, ByteUtils.a(readAndDecrementInitialLives()), 0, 4);
        RmsBank.flush(b);
        if (RmsBank.read(c, 9) == null) {
            // Grant starter campaign supplies on new game: 200 Gold + 2 Bình Thuốc.
            a(2, 200, 6);
            a(4, 2, 6);
        }
    }

    public static boolean l() {
        byte[] raw = RmsBank.read(b, 5);
        return raw != null && ByteUtils.a(raw) >= 1;
    }

    public static int m() {
        byte[] raw = RmsBank.read(b, 5);
        if (raw == null) {
            return 0;
        }
        int lives = ByteUtils.a(raw);
        return lives < 1 ? 0 : lives;
    }

    public static void n() {
        byte[] raw = RmsBank.read(b, 5);
        int lives = raw != null ? ByteUtils.a(raw) : 0;
        --lives;
        if (lives < 0) {
            lives = 0;
        }
        RmsBank.write(b, 5, ByteUtils.a(lives), 0, 4);
        RmsBank.flush(b);
    }

    public static void c(int deltaLives) {
        byte[] raw = RmsBank.read(b, 5);
        int lives = raw != null ? ByteUtils.a(raw) : 0;
        lives += deltaLives;
        RmsBank.write(b, 5, ByteUtils.a(lives), 0, 4);
        RmsBank.flush(b);
    }

    public static byte[] o() {
        return RmsBank.read(b, 11);
    }

    public static void d(int value) {
        RmsBank.write(b, 11, ByteUtils.a(value), 0, 4);
        RmsBank.flush(b);
    }

    public static void p() {
        if (!GameConfig.rmsPersistenceEnabled) {
            return;
        }
        GameConfig.sfxEnabled = true;
        byte[] raw = RmsBank.read(b, 13);
        if (raw != null) {
            if (raw[0] <= 0) {
                GameConfig.sfxEnabled = false;
            }
        } else {
            a(false);
        }
    }

    public static void a(boolean decrement) {
        if (!GameConfig.rmsPersistenceEnabled) {
            return;
        }
        byte counter = 3;
        byte[] raw = RmsBank.read(b, 13);
        if (raw != null) {
            counter = raw[0];
        }
        if (decrement) {
            --counter;
        } else {
            counter = 3;
        }
        GameConfig.sfxEnabled = counter > 0;
        RmsBank.write(b, 13, new byte[]{counter}, 0, 1);
        RmsBank.flush(b);
    }

    public static byte[] q() {
        return RmsBank.read(b, 12);
    }

    public static void r() {
        RmsBank.write(b, 12, ByteUtils.a(1), 0, 4);
        RmsBank.flush(b);
    }

    private static byte[] ensureCollectionData() {
        byte[] raw = RmsBank.read(c, 1);
        if (raw == null) {
            raw = new byte[(a + 1) * 4];
            byte[] reservedRelic = ByteUtils.a(RandomUtils.nextInt(4) + 1);
            System.arraycopy(reservedRelic, 0, raw, raw.length - 4, 4);
            RmsBank.write(c, 1, raw, 0, raw.length);
            RmsBank.flush(c);
        }
        return raw;
    }

    public static int[] s() {
        int[] slots = new int[a];
        byte[] raw = ensureCollectionData();
        for (int i = 0; i < slots.length; ++i) {
            slots[i] = ByteUtils.a(raw, i * 4);
        }
        return slots;
    }

    public static boolean a(int[] slots) {
        if (slots == null) {
            return false;
        }
        boolean[] collected = new boolean[4];
        for (int i = slots.length - 1; i > -1; --i) {
            if (slots[i] > 0) {
                collected[slots[i] - 1] = true;
            }
        }
        boolean allCollected = true;
        for (int i = 0; i < collected.length; ++i) {
            allCollected &= collected[i];
        }
        return allCollected;
    }

    public static final int RELIC_COUNT = 4;
    public static final int RELIC_MAX_LEVEL = 5;
    public static final int RELIC_KINH_BAT_QUAI = 1;
    public static final int RELIC_KHANH_KIM_LONG = 2;
    public static final int RELIC_NGOC_BOI = 4;
    public static final int RELIC_TUONG_PHAT = 8;

    private static final int RECORD_ENHANCEMENT_SHARDS = 11;
    private static final int RECORD_RELIC_UPGRADES = 12;
    private static final int RECORD_DISPLAY_SETTINGS = 13;

    public static void loadDisplaySettings() {
        try {
            if (c == null) {
                return;
            }
            byte[] raw = RmsBank.read(c, RECORD_DISPLAY_SETTINGS);
            if (raw != null && raw.length >= 2) {
                com.mg.engine.core.GameConfig.highFpsEnabled = raw[0] != 0;
                com.mg.engine.core.GameConfig.hdUpscaleEnabled = raw[1] != 0;
                if (raw.length >= 3 && raw[2] >= 0 && raw[2] <= 2) {
                    com.loan12suquan.hd.HdConfig.visualMode = raw[2];
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public static void saveDisplaySettings() {
        try {
            if (c == null) {
                return;
            }
            byte[] raw = new byte[]{
                    (byte) (com.mg.engine.core.GameConfig.highFpsEnabled ? 1 : 0),
                    (byte) (com.mg.engine.core.GameConfig.hdUpscaleEnabled ? 1 : 0),
                    (byte) com.loan12suquan.hd.HdConfig.visualMode
            };
            RmsBank.write(c, RECORD_DISPLAY_SETTINGS, raw, 0, raw.length);
            RmsBank.flush(c);
        } catch (Throwable ignored) {
        }
    }

    /** Stat tables indexed by relic level {@code 0..5}. */
    private static final int[] RELIC_ATK_PCT = new int[]{0, 4, 7, 10, 13, 16};
    private static final int[] RELIC_HP_PCT = new int[]{0, 5, 8, 12, 16, 20};
    private static final int[] RELIC_START_MANA = new int[]{0, 2, 3, 4, 5, 6};
    private static final int[] RELIC_FOOD_BONUS = new int[]{0, 150, 250, 350, 450, 600};
    private static final int[] RELIC_HEART_PCT = new int[]{0, 6, 10, 15, 20, 25};

    /** Craft/upgrade costs & base success chances indexed by current level {@code 0..4}. */
    private static final int[] RELIC_SHARD_COST = new int[]{4, 3, 4, 5, 6};
    private static final int[] RELIC_GOLD_COST = new int[]{0, 200, 400, 700, 1000};
    private static final int[] RELIC_BASE_SUCCESS_PCT = new int[]{100, 80, 60, 40, 25};
    public static final int RELIC_FAIL_PITY_STEP = 10;

    private static int cachedRelicMask = -1;
    private static boolean relicUpgradeLoaded = false;
    private static int cachedEnhancementShards = 0;
    private static final int[] cachedRelicLevels = new int[RELIC_COUNT];
    private static final int[] cachedRelicFails = new int[RELIC_COUNT];

    private static void ensureRelicUpgradeDataLoaded() {
        if (relicUpgradeLoaded) {
            return;
        }
        try {
            if (c == null) {
                return;
            }
            byte[] rawUpgrades = RmsBank.read(c, RECORD_RELIC_UPGRADES);
            if (rawUpgrades != null && rawUpgrades.length >= 8) {
                int mask = 0;
                for (int i = 0; i < RELIC_COUNT; ++i) {
                    int lv = rawUpgrades[i] & 0xFF;
                    if (lv > RELIC_MAX_LEVEL) {
                        lv = RELIC_MAX_LEVEL;
                    }
                    int fails = rawUpgrades[RELIC_COUNT + i] & 0xFF;
                    if (fails > 10) {
                        fails = 10;
                    }
                    cachedRelicLevels[i] = lv;
                    cachedRelicFails[i] = fails;
                    if (lv > 0) {
                        mask |= (1 << i);
                    }
                }
                cachedRelicMask = mask;
                byte[] rawShards = RmsBank.read(c, RECORD_ENHANCEMENT_SHARDS);
                int shards = (rawShards != null && rawShards.length >= 4) ? ByteUtils.a(rawShards, 0) : 0;
                cachedEnhancementShards = shards < 0 ? 0 : shards;
                relicUpgradeLoaded = true;
                return;
            }

            // One-time migration from legacy v1.12.x save (record 10 mask + record 1 mystery boxes).
            int legacyMask = 0;
            byte[] rawMask = RmsBank.read(c, 10);
            if (rawMask != null && rawMask.length >= 4) {
                legacyMask = ByteUtils.a(rawMask, 0) & 15;
            }
            int unopenedLegacyBoxes = 0;
            byte[] rawBoard = RmsBank.read(c, 1);
            if (rawBoard != null && rawBoard.length >= a * 4) {
                for (int i = 0; i < a; ++i) {
                    int slotVal = ByteUtils.a(rawBoard, i * 4);
                    if (slotVal >= 1 && slotVal <= RELIC_COUNT) {
                        legacyMask |= (1 << (slotVal - 1));
                    } else if (slotVal == -1) {
                        ++unopenedLegacyBoxes;
                    }
                }
            }

            int migratedShards = unopenedLegacyBoxes * 2;
            for (int i = 0; i < RELIC_COUNT; ++i) {
                if ((legacyMask & (1 << i)) != 0) {
                    cachedRelicLevels[i] = 1;
                    migratedShards += 4; // Compensate legacy owners so they can try upgrading immediately
                } else {
                    cachedRelicLevels[i] = 0;
                }
                cachedRelicFails[i] = 0;
            }
            cachedEnhancementShards = migratedShards;
            cachedRelicMask = legacyMask;
            relicUpgradeLoaded = true;
            saveRelicUpgradeState();
        } catch (Throwable ignored) {
        }
    }

    private static void saveRelicUpgradeState() {
        try {
            if (c == null) {
                return;
            }
            byte[] rawUpgrades = new byte[8];
            int mask = 0;
            for (int i = 0; i < RELIC_COUNT; ++i) {
                rawUpgrades[i] = (byte) cachedRelicLevels[i];
                rawUpgrades[RELIC_COUNT + i] = (byte) cachedRelicFails[i];
                if (cachedRelicLevels[i] > 0) {
                    mask |= (1 << i);
                }
            }
            cachedRelicMask = mask;
            RmsBank.write(c, RECORD_RELIC_UPGRADES, rawUpgrades, 0, rawUpgrades.length);
            byte[] shardBytes = ByteUtils.a(cachedEnhancementShards);
            RmsBank.write(c, RECORD_ENHANCEMENT_SHARDS, shardBytes, 0, shardBytes.length);
            byte[] maskBytes = ByteUtils.a(mask);
            RmsBank.write(c, 10, maskBytes, 0, maskBytes.length);
            RmsBank.flush(c);
        } catch (Throwable ignored) {
        }
    }

    public static int getEnhancementShards() {
        ensureRelicUpgradeDataLoaded();
        return cachedEnhancementShards;
    }

    public static void addEnhancementShards(int delta) {
        if (delta == 0) {
            return;
        }
        ensureRelicUpgradeDataLoaded();
        cachedEnhancementShards += delta;
        if (cachedEnhancementShards < 0) {
            cachedEnhancementShards = 0;
        }
        saveRelicUpgradeState();
    }

    public static int getRelicLevel(int relicIndex0To3) {
        if (relicIndex0To3 < 0 || relicIndex0To3 >= RELIC_COUNT) {
            return 0;
        }
        ensureRelicUpgradeDataLoaded();
        return cachedRelicLevels[relicIndex0To3];
    }

    public static int getRelicFailCount(int relicIndex0To3) {
        if (relicIndex0To3 < 0 || relicIndex0To3 >= RELIC_COUNT) {
            return 0;
        }
        ensureRelicUpgradeDataLoaded();
        return cachedRelicFails[relicIndex0To3];
    }

    public static int getRelicUpgradeShardCost(int currentLevel) {
        if (currentLevel < 0 || currentLevel >= RELIC_SHARD_COST.length) {
            return 0;
        }
        return RELIC_SHARD_COST[currentLevel];
    }

    public static int getRelicUpgradeGoldCost(int currentLevel) {
        if (currentLevel < 0 || currentLevel >= RELIC_GOLD_COST.length) {
            return 0;
        }
        return RELIC_GOLD_COST[currentLevel];
    }

    public static int getRelicBaseSuccessChance(int currentLevel) {
        if (currentLevel < 0 || currentLevel >= RELIC_BASE_SUCCESS_PCT.length) {
            return 0;
        }
        return RELIC_BASE_SUCCESS_PCT[currentLevel];
    }

    /** Effective success rate (%) including +10% pity per previous failure on that relic. */
    public static int getRelicUpgradeSuccessChance(int relicIndex0To3) {
        int lv = getRelicLevel(relicIndex0To3);
        if (lv >= RELIC_MAX_LEVEL) {
            return 0;
        }
        int chance = RELIC_BASE_SUCCESS_PCT[lv] + getRelicFailCount(relicIndex0To3) * RELIC_FAIL_PITY_STEP;
        return chance > 100 ? 100 : chance;
    }

    public static int getRelicAtkPercent() {
        return RELIC_ATK_PCT[getRelicLevel(0)];
    }

    public static int getRelicHpPercent() {
        return RELIC_HP_PCT[getRelicLevel(1)];
    }

    public static int getRelicStartManaBonus() {
        return RELIC_START_MANA[getRelicLevel(2)];
    }

    public static int getRelicFoodBonus() {
        return RELIC_FOOD_BONUS[getRelicLevel(3)];
    }

    public static int getRelicHeartPercent() {
        return RELIC_HEART_PCT[getRelicLevel(3)];
    }

    public static String getRelicStatDesc(int relicIndex0To3, int level) {
        if (level <= 0) {
            return "Chưa kích hoạt";
        }
        if (level > RELIC_MAX_LEVEL) {
            level = RELIC_MAX_LEVEL;
        }
        switch (relicIndex0To3) {
            case 0:
                return "+" + RELIC_ATK_PCT[level] + "% Công";
            case 1:
                return "+" + RELIC_HP_PCT[level] + "% Máu";
            case 2:
                return "+" + RELIC_START_MANA[level] + " Nộ đầu";
            case 3:
                return "+" + RELIC_FOOD_BONUS[level] + " Lương, +" + RELIC_HEART_PCT[level] + "% Tim";
            default:
                return "";
        }
    }

    public static boolean canCraftOrUpgradeAnyRelic() {
        ensureRelicUpgradeDataLoaded();
        int shards = cachedEnhancementShards;
        int gold = f(2);
        for (int i = 0; i < RELIC_COUNT; ++i) {
            int lv = cachedRelicLevels[i];
            if (lv < RELIC_MAX_LEVEL && shards >= RELIC_SHARD_COST[lv] && gold >= RELIC_GOLD_COST[lv]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to craft ({@code 0 -> 1}) or enhance ({@code 1..4 -> +1}) relic {@code relicIndex0To3}.
     * Returns: {@code 1} = success, {@code 0} = failed (shards/gold spent, +10% pity added, level preserved),
     * {@code -1} = insufficient shards, {@code -3} = insufficient gold, {@code -2} = already max level.
     */
    public static int attemptUpgradeRelic(int relicIndex0To3) {
        if (relicIndex0To3 < 0 || relicIndex0To3 >= RELIC_COUNT) {
            return -2;
        }
        ensureRelicUpgradeDataLoaded();
        int lv = cachedRelicLevels[relicIndex0To3];
        if (lv >= RELIC_MAX_LEVEL) {
            return -2;
        }
        int shardCost = RELIC_SHARD_COST[lv];
        int goldCost = RELIC_GOLD_COST[lv];
        if (cachedEnhancementShards < shardCost) {
            return -1;
        }
        if (goldCost > 0 && f(2) < goldCost) {
            return -3;
        }

        int successPct = getRelicUpgradeSuccessChance(relicIndex0To3);
        cachedEnhancementShards -= shardCost;
        if (goldCost > 0) {
            b(2, goldCost);
        }

        boolean succeeded = (successPct >= 100) || (RandomUtils.nextInt(100) < successPct);
        if (succeeded) {
            cachedRelicLevels[relicIndex0To3] = lv + 1;
            cachedRelicFails[relicIndex0To3] = 0;
            saveRelicUpgradeState();
            return 1;
        } else {
            if (cachedRelicFails[relicIndex0To3] < 10) {
                ++cachedRelicFails[relicIndex0To3];
            }
            saveRelicUpgradeState();
            return 0;
        }
    }

    private static void saveCollectionSlots(int[] slots) {
        if (slots == null) {
            return;
        }
        byte[] raw = ensureCollectionData();
        for (int i = 0; i < slots.length; ++i) {
            System.arraycopy(ByteUtils.a(slots[i]), 0, raw, i * 4, 4);
        }
        RmsBank.write(c, 1, raw, 0, raw.length);
        RmsBank.flush(c);
    }

    public static void a(int slotIndex, int mode, String rewardCode, String rewardName) {
        if (Math.abs(slotIndex) >= a) {
            return;
        }
        byte[] raw = RmsBank.read(c, 1);
        if (raw == null) {
            return;
        }
        int[] slots = new int[a + 1];
        int[] countsPerRelic = new int[4];
        for (int i = 0; i < slots.length; ++i) {
            slots[i] = ByteUtils.a(raw, i * 4);
            if (i < slots.length - 1 && slots[i] > 0) {
                ++countsPerRelic[slots[i] - 1];
            }
        }

        int chosenRelic = -1;
        if (mode != 1) {
            int openedCount = countsPerRelic[0] + countsPerRelic[1] + countsPerRelic[2] + countsPerRelic[3];
            if (openedCount >= 10) {
                return;
            }
            // In offline mode, allow all 4 relics (including the 4th relic) to be collected naturally.
            for (int i = 0; i < countsPerRelic.length; ++i) {
                if (countsPerRelic[i] <= 0) {
                    chosenRelic = i;
                    break;
                }
            }
            if (chosenRelic == -1) {
                chosenRelic = RandomUtils.nextInt(4);
            }
            ++chosenRelic;
            if (rewardCode != null) {
                byte[] codeBytes = rewardCode.getBytes();
                RmsBank.write(c, 2, codeBytes, 0, codeBytes.length);
            }
        } else {
            if (rewardCode == null) {
                return;
            }
            chosenRelic = Math.abs(slots[slots.length - 1]);
            byte[] codeBytes = rewardCode.getBytes();
            RmsBank.write(c, 2, codeBytes, 0, codeBytes.length);
        }

        DebugLogForm.log("index=" + slotIndex + ",mode=" + mode + ",chosenRelic=" + chosenRelic);
        byte[] relicBytes = ByteUtils.a(chosenRelic);
        System.arraycopy(relicBytes, 0, raw, slotIndex * 4, 4);
        RmsBank.write(c, 1, raw, 0, raw.length);
        unlockRelicBit(chosenRelic);

        if (rewardName != null) {
            byte[] nameBytes = rewardName.trim().getBytes();
            RmsBank.write(c, 3, nameBytes, 0, nameBytes.length);
        }
        RmsBank.flush(c);
    }

    /** Permanently records that relic {@code 1..4} has been unlocked (level >= 1). */
    public static void unlockRelicBit(int relicId1To4) {
        if (relicId1To4 < 1 || relicId1To4 > RELIC_COUNT) {
            return;
        }
        ensureRelicUpgradeDataLoaded();
        if (cachedRelicLevels[relicId1To4 - 1] < 1) {
            cachedRelicLevels[relicId1To4 - 1] = 1;
        }
        saveRelicUpgradeState();
    }

    /** Returns the 4-bit permanent Sacred Relic mask (bit0..bit3 for relics 1..4 with level >= 1). */
    public static int getUnlockedRelicMask() {
        ensureRelicUpgradeDataLoaded();
        if (cachedRelicMask >= 0) {
            return cachedRelicMask;
        }
        int mask = 0;
        for (int i = 0; i < RELIC_COUNT; ++i) {
            if (cachedRelicLevels[i] > 0) {
                mask |= (1 << i);
            }
        }
        return mask;
    }

    /** Returns how many distinct Sacred Relics (0..4) are permanently unlocked. */
    public static int countUnlockedRelics() {
        int mask = getUnlockedRelicMask();
        int count = 0;
        for (int i = 0; i < 4; ++i) {
            if ((mask & (1 << i)) != 0) {
                ++count;
            }
        }
        return count;
    }

    private static boolean deduplicateRelics(int[] slots) {
        boolean[] seen = new boolean[5];
        boolean modified = false;
        for (int i = 0; i < slots.length; ++i) {
            if (slots[i] > 0) {
                if (seen[slots[i]]) {
                    modified = true;
                    slots[i] = 0;
                } else {
                    seen[slots[i]] = true;
                }
            }
        }
        return modified;
    }

    public static int t() {
        int[] slots = s();
        for (int i = 0; i < slots.length; ++i) {
            if (slots[i] == 0) {
                slots[i] = -1;
                saveCollectionSlots(slots);
                return i;
            }
        }
        for (int i = slots.length - 1; i >= 0; --i) {
            if (slots[i] == -1) {
                return i;
            }
        }
        if (deduplicateRelics(slots)) {
            saveCollectionSlots(slots);
            return t();
        }
        return -1;
    }

    /** Unlocks the mystery relic box at {@code slotIndex} offline and shows the reveal screen. */
    public static void e(int slotIndex) {
        GUIManager.getController().openRelicSlot(slotIndex);
    }

    public static byte[] u() {
        return RmsBank.read(c, 2);
    }

    public static void a(int itemIndex, int deltaAmount, int totalSlots) {
        byte[] raw = null;
        if (itemIndex < totalSlots) {
            raw = RmsBank.read(c, 9);
            if (raw != null) {
                deltaAmount += ByteUtils.a(raw, itemIndex * 4);
            } else {
                raw = new byte[totalSlots * 4];
            }
        }
        int clamped = deltaAmount < 0 ? 0 : deltaAmount;
        byte[] valueBytes = ByteUtils.a(clamped);
        System.arraycopy(valueBytes, 0, raw, itemIndex * 4, 4);
        RmsBank.write(c, 9, raw, 0, raw.length);
        RmsBank.flush(c);
    }

    public static int[] v() {
        byte[] raw = RmsBank.read(c, 9);
        if (raw == null) {
            return null;
        }
        int[] items = new int[raw.length / 4];
        for (int i = 0; i < items.length; ++i) {
            items[i] = ByteUtils.a(raw, i * 4);
        }
        return items;
    }

    public static int f(int itemIndex) {
        byte[] raw = RmsBank.read(c, 9);
        return raw == null ? 0 : ByteUtils.a(raw, itemIndex * 4);
    }

    public static void b(int itemIndex, int amountToDeduct) {
        byte[] raw = RmsBank.read(c, 9);
        if (raw == null) {
            return;
        }
        int remaining = ByteUtils.a(raw, itemIndex * 4) - amountToDeduct;
        if (remaining < 0) {
            remaining = 0;
        }
        byte[] valueBytes = ByteUtils.a(remaining);
        System.arraycopy(valueBytes, 0, raw, itemIndex * 4, 4);
        RmsBank.write(c, 9, raw, 0, raw.length);
        RmsBank.flush(c);
    }

    public static void g(int itemIndex) {
        b(itemIndex, 1);
    }

    public static String w() {
        byte[] raw = RmsBank.read(c, 3);
        return raw == null ? null : new String(raw);
    }

    public static void x() {
        try {
            // Preserve all permanently unlocked Sacred Relic bits before resetting the 10-slot board.
            int existingMask = getUnlockedRelicMask();
            if (existingMask > 0) {
                cachedRelicMask = existingMask;
                byte[] maskBytes = ByteUtils.a(existingMask);
                RmsBank.write(c, 10, maskBytes, 0, maskBytes.length);
            }
            RmsBank.delete(c, 1);
            RmsBank.delete(c, 2);
            RmsBank.delete(c, 3);
            RmsBank.flush(c);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void a() {
        try {
            cachedRelicMask = -1;
            relicUpgradeLoaded = false;
            cachedEnhancementShards = 0;
            for (int i = 0; i < RELIC_COUNT; ++i) {
                cachedRelicLevels[i] = 0;
                cachedRelicFails[i] = 0;
            }
            RmsBank.delete(b, 10);
            RmsBank.delete(b, 6);
            RmsBank.delete(b, 5);
            RmsBank.delete(b, 16);
            RmsBank.delete(c, 1);
            RmsBank.delete(c, 2);
            RmsBank.delete(c, 3);
            RmsBank.delete(c, 9);
            RmsBank.delete(c, 10);
            RmsBank.delete(c, RECORD_ENHANCEMENT_SHARDS);
            RmsBank.delete(c, RECORD_RELIC_UPGRADES);
            RmsBank.flush(b);
            RmsBank.flush(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
