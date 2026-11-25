package com.mic.smsgame.suquan.model;

import com.mg.engine.storage.EngineStorage;
import com.mg.engine.util.ByteUtils;
import com.mic.smsgame.suquan.data.CampaignData;

/**
 * Persistent RPG stats for a hero or enemy warlord (avatar, HP, attack, level, skills, bonuses).
 */
public class HeroStats {
    public static final int MAX_LEVEL = 99;

    /** Character / avatar ID in {@link com.mic.smsgame.suquan.data.DialogueData#a}. */
    public int a;
    /** Maximum HP. */
    public int b;
    /** Current HP. */
    public int c;
    /** Attack power. */
    public int d;
    /** Character level. */
    public int e;
    /** Unlocked skill IDs (indices into {@link com.mic.smsgame.suquan.data.GameStrings#a}). */
    public int[] f;
    /** Bonus max HP from equipment/upgrades. */
    public int g;
    /** Bonus attack from equipment/upgrades. */
    public int h;

    /** Returns true if this hero is one of the 6 playable protagonists (avatar IDs {@code 0..5}). */
    public boolean isPlayableHero() {
        return this.a >= 0 && this.a <= 5;
    }

    /**
     * Returns the current 10-level Elemental Breakthrough tier ({@code 0..9}, triggered at
     * levels 10, 20, ..., 90) for playable heroes, or {@code 0} for NPCs/enemies.
     */
    public int getBreakthroughTier() {
        return this.isPlayableHero() ? (this.e / 10) : 0;
    }

    /** Cumulative bonus max HP granted by 10-level Elemental Breakthroughs. */
    public int getBreakthroughBonusHp() {
        int tier = this.getBreakthroughTier();
        if (tier <= 0) {
            return 0;
        }
        int elem = this.a % 3;
        int perTier = elem == 0 ? 15 : (elem == 1 ? 22 : 30);
        return tier * perTier;
    }

    /** Cumulative bonus attack power granted by 10-level Elemental Breakthroughs. */
    public int getBreakthroughBonusAttack() {
        int tier = this.getBreakthroughTier();
        if (tier <= 0) {
            return 0;
        }
        int elem = this.a % 3;
        int perTier = elem == 0 ? 3 : 2;
        return tier * perTier;
    }

    /** Cumulative bonus starting battle Mana granted by Thunder & Water 10-level Breakthroughs. */
    public int getBreakthroughBonusStartMana() {
        int tier = this.getBreakthroughTier();
        if (tier <= 0) {
            return 0;
        }
        int elem = this.a % 3;
        return elem == 1 ? tier : (elem == 2 ? (tier * 2) : 0);
    }

    /** Short Vietnamese description of per-10-level breakthrough rewards for hero avatar {@code avatarId}. */
    public static String getBreakthroughStepDesc(int avatarId) {
        int elem = avatarId % 3;
        if (elem == 0) {
            return "+15 Máu, +3 Công";
        }
        if (elem == 1) {
            return "+22 Máu, +2 Công, +1 Nộ đầu";
        }
        return "+30 Máu, +2 Công, +2 Nộ đầu";
    }

    /** Compact breakthrough bonus summary for hero profile & milestone banners. */
    public String getBreakthroughSummary() {
        if (!this.isPlayableHero()) {
            return null;
        }
        int tier = this.getBreakthroughTier();
        if (tier <= 0) {
            return "Đột phá (0/9 - C.10): " + getBreakthroughStepDesc(this.a);
        }
        int hp = this.getBreakthroughBonusHp();
        int atk = this.getBreakthroughBonusAttack();
        int mana = this.getBreakthroughBonusStartMana();
        String bonus = "+" + hp + " Máu, +" + atk + " Công" + (mana > 0 ? (", +" + mana + " Nộ") : "");
        if (tier >= 9) {
            return "Đột phá (9/9 Tối đa): " + bonus;
        }
        return "Đột phá (" + tier + "/9 - C." + ((tier + 1) * 10) + "): " + bonus;
    }

    /** Safe lookup of permanent Sacred Relic HP% (Khánh Kim Long, 0..20%). */
    private static int readRelicHpPercentSafe() {
        try {
            return EngineStorage.getRelicHpPercent();
        } catch (Throwable ignored) {
            return 0;
        }
    }

    /** Safe lookup of permanent Sacred Relic ATK% (Kính Bát Quái, 0..16%). */
    private static int readRelicAtkPercentSafe() {
        try {
            return EngineStorage.getRelicAtkPercent();
        } catch (Throwable ignored) {
            return 0;
        }
    }

    /** Permanent MaxHP bonus (+5%..+20% by Khánh Kim Long level 1..5, min +5) for playable heroes. */
    public int getRelicBonusHp(int baseMaxHp) {
        if (!this.isPlayableHero()) {
            return 0;
        }
        int pct = readRelicHpPercentSafe();
        if (pct <= 0) {
            return 0;
        }
        int bonus = baseMaxHp * pct / 100;
        return bonus < 5 ? 5 : bonus;
    }

    /** Permanent Attack bonus (+4%..+16% by Kính Bát Quái level 1..5, min +1) for playable heroes. */
    public int getRelicBonusAttack(int baseAttack) {
        if (!this.isPlayableHero()) {
            return 0;
        }
        int pct = readRelicAtkPercentSafe();
        if (pct <= 0) {
            return 0;
        }
        int bonus = baseAttack * pct / 100;
        return bonus < 1 ? 1 : bonus;
    }

    /** Levels up the hero by 1 (capped at {@link #MAX_LEVEL}), recalculates max HP/attack, and restores full HP. */
    public void a() {
        if (this.e < MAX_LEVEL) {
            ++this.e;
        }
        this.b();
        this.c = this.b;
    }

    /** Recomputes max HP and attack from current level, permanent bonuses, breakthroughs, and Sacred Relics. */
    public void b() {
        int baseAtk = this.h + CampaignData.v(this.e) + this.getBreakthroughBonusAttack();
        int baseHp = this.g + CampaignData.w(this.e) + this.getBreakthroughBonusHp();
        this.d = baseAtk + this.getRelicBonusAttack(baseAtk);
        this.b = baseHp + this.getRelicBonusHp(baseHp);
    }

    /**
     * Recomputes stats after a Sacred Relic unlock and grants any newly gained MaxHP directly
     * to current HP so the player immediately sees the permanent boost outside battle.
     * Returns true if MaxHP or Attack changed.
     */
    public boolean refreshAfterRelicUnlock() {
        int oldMaxHp = this.b;
        int oldAtk = this.d;
        this.b();
        if (this.b > oldMaxHp) {
            this.c += (this.b - oldMaxHp);
            if (this.c > this.b) {
                this.c = this.b;
            }
        }
        return this.b != oldMaxHp || this.d != oldAtk;
    }

    /** Returns a shallow copy of scalar stats (skills array not copied). */
    public HeroStats c() {
        HeroStats copy = new HeroStats();
        copy.a = this.a;
        copy.b = this.b;
        copy.c = this.c;
        copy.d = this.d;
        copy.e = this.e;
        copy.h = this.h;
        copy.g = this.g;
        return copy;
    }

    /** Serializes this hero into {@code buffer} starting at {@code offset}; returns bytes written. */
    public int a(byte[] buffer, int offset) {
        int start = offset;
        System.arraycopy(ByteUtils.a(this.a), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.b), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.c), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.d), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.e), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.g), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.h), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.f.length), 0, buffer, offset, 4);
        offset += 4;

        for (int i = 0; i < this.f.length; ++i) {
            System.arraycopy(ByteUtils.a(this.f[i]), 0, buffer, offset, 4);
            offset += 4;
        }
        return offset - start;
    }

    /** Deserializes this hero from {@code buffer} starting at {@code offset}; returns bytes read. */
    public int b(byte[] buffer, int offset) {
        int start = offset;
        this.a = ByteUtils.a(buffer, offset);
        offset += 4;
        this.b = ByteUtils.a(buffer, offset);
        offset += 4;
        this.c = ByteUtils.a(buffer, offset);
        offset += 4;
        this.d = ByteUtils.a(buffer, offset);
        offset += 4;
        this.e = ByteUtils.a(buffer, offset);
        offset += 4;
        this.g = ByteUtils.a(buffer, offset);
        offset += 4;
        this.h = ByteUtils.a(buffer, offset);
        offset += 4;
        this.f = new int[ByteUtils.a(buffer, offset)];
        offset += 4;

        for (int i = 0; i < this.f.length; ++i) {
            this.f[i] = ByteUtils.a(buffer, offset);
            offset += 4;
        }
        return offset - start;
    }
}
