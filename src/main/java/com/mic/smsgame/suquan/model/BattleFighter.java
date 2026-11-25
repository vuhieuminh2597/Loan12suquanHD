package com.mic.smsgame.suquan.model;

import com.mg.engine.util.ByteUtils;
import com.mic.smsgame.suquan.data.CampaignData;
import com.mic.smsgame.suquan.data.DialogueData;

/**
 * Runtime combatant state during a match-3 battle (current/displayed HP, food, mana,
 * lightning-shield immunity, active item buffs, and battle gold/EXP earned).
 */
public class BattleFighter {
    private static final int DEFAULT_MAX_FOOD = 7500;
    private static final int DEFAULT_MAX_MANA = 36;

    public static final int PASSIVE_NONE = 0;
    public static final int PASSIVE_FIRE_SOUL = 1;
    public static final int PASSIVE_THUNDER_AURA = 2;
    public static final int PASSIVE_FROST_HEART = 3;
    public static final int PASSIVE_TIGER_MIGHT = 4;
    public static final int PASSIVE_IRON_BASTION = 5;
    public static final int PASSIVE_DRAGON_MAJESTY = 6;
    public static final int PASSIVE_BEAST_FURY = 7;
    public static final int PASSIVE_VENOM_BODY = 8;
    public static final int PASSIVE_STONE_BARK = 9;
    public static final int PASSIVE_SPIRIT_WISP = 10;

    public static final int ARCHETYPE_BALANCED = 0;
    public static final int ARCHETYPE_AGGRESSIVE = 1;
    public static final int ARCHETYPE_CASTER = 2;
    public static final int ARCHETYPE_DEFENDER = 3;

    // Specialized Boss Archetypes (distinct combat identities, active cycles, and tactical counterplay)
    public static final int BOSS_ARCHETYPE_NONE = 0;
    public static final int BOSS_ARCHETYPE_BERSERKER = 1;      // Siêu Tấn Công (Cuồng Sát / Xuyên Giáp)
    public static final int BOSS_ARCHETYPE_FORTRESS = 2;       // Siêu Phòng Thủ (Thành Đồng / Phản Đòn)
    public static final int BOSS_ARCHETYPE_CONTROLLER = 3;     // Siêu Khống Chế (Áp Chế / Nhiễu Hồn)
    public static final int BOSS_ARCHETYPE_ARCHMAGE = 4;       // Siêu Pháp Sư (Tụ Lôi / Xả Chiêu Liên Hoàn)
    public static final int BOSS_ARCHETYPE_WARLORD_FIRE = 5;   // Hỏa Áp Bức (Binh Hỏa / Đốt Kho Lương)
    public static final int BOSS_ARCHETYPE_OVERLORD_PHASE = 6; // Bá Vương 2 Pha (Long Uy / Long Nộ Thức Tỉnh)

    private int maxHp;
    private int currentHp;
    private int displayedHp;
    private boolean shielded = false;
    private int maxFood = DEFAULT_MAX_FOOD;
    private int currentFood = 0;
    private int maxMana = DEFAULT_MAX_MANA;
    private int currentMana = 0;
    /** Active battle item buff slots (indices 0..5). */
    public ItemSlot[] a;
    private int earnedExp;
    private int earnedGold;
    private int lowHpThreshold;
    private HeroStats baseStats;
    private int aiMistakeChance;
    private int passiveId = PASSIVE_NONE;
    private int aiArchetype = ARCHETYPE_BALANCED;
    private int bossArchetype = BOSS_ARCHETYPE_NONE;
    private int burnTurnsRemaining = 0;
    private int burnDamagePerTurn = 0;
    private int poisonTurnsRemaining = 0;
    private int poisonDamagePerTurn = 0;
    private int woundTurnsRemaining = 0;
    private int curseTurnsRemaining = 0;
    private int shieldTurnsDisplay = 0;
    private int relicMask = 0;
    private int relicStartManaBonus = 0;
    private int relicHeartBonusPct = 0;
    private boolean enrageTriggered = false;
    private boolean tutorialDisabled = false;
    private int barrierHp = 0;
    private int furyStacks = 0;
    private int armorBreakTurns = 0;
    private int bossCycleTurn = 0;
    private boolean phase2Triggered = false;
    private boolean sealExplosionImminent = false;

    /** Byte length written/read by {@link #writeExtendedSnapshot} and {@link #readExtendedSnapshot}. */
    public static final int EXTENDED_SNAPSHOT_BYTES = 28;
    /** Byte length written/read by {@link #writeBossSnapshot} and {@link #readBossSnapshot} (SQV4). */
    public static final int BOSS_SNAPSHOT_BYTES = 20;

    public int writeBossSnapshot(byte[] buffer, int offset) {
        int start = offset;
        System.arraycopy(ByteUtils.a(this.barrierHp), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.furyStacks), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.armorBreakTurns), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.bossCycleTurn), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.phase2Triggered ? 1 : 0), 0, buffer, offset, 4);
        offset += 4;
        return offset - start;
    }

    public int readBossSnapshot(byte[] buffer, int offset) {
        int start = offset;
        this.barrierHp = ByteUtils.a(buffer, offset);
        offset += 4;
        this.furyStacks = ByteUtils.a(buffer, offset);
        offset += 4;
        this.armorBreakTurns = ByteUtils.a(buffer, offset);
        offset += 4;
        this.bossCycleTurn = ByteUtils.a(buffer, offset);
        offset += 4;
        this.phase2Triggered = ByteUtils.a(buffer, offset) != 0;
        offset += 4;

        int maxBarrier = this.getMaxBarrierHp();
        if (this.barrierHp < 0) {
            this.barrierHp = 0;
        } else if (this.barrierHp > maxBarrier) {
            this.barrierHp = maxBarrier;
        }
        if (this.furyStacks < 0) {
            this.furyStacks = 0;
        } else if (this.furyStacks > 4) {
            this.furyStacks = 4;
        }
        if (this.armorBreakTurns < 0) {
            this.armorBreakTurns = 0;
        }
        if (this.bossCycleTurn < 0) {
            this.bossCycleTurn = 0;
        }
        return offset - start;
    }

    public int writeExtendedSnapshot(byte[] buffer, int offset) {
        int start = offset;
        System.arraycopy(ByteUtils.a(this.burnTurnsRemaining), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.burnDamagePerTurn), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.poisonTurnsRemaining), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.poisonDamagePerTurn), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.woundTurnsRemaining), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.curseTurnsRemaining), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.enrageTriggered ? 1 : 0), 0, buffer, offset, 4);
        offset += 4;
        return offset - start;
    }

    public int readExtendedSnapshot(byte[] buffer, int offset) {
        int start = offset;
        this.burnTurnsRemaining = ByteUtils.a(buffer, offset);
        offset += 4;
        this.burnDamagePerTurn = ByteUtils.a(buffer, offset);
        offset += 4;
        this.poisonTurnsRemaining = ByteUtils.a(buffer, offset);
        offset += 4;
        this.poisonDamagePerTurn = ByteUtils.a(buffer, offset);
        offset += 4;
        this.woundTurnsRemaining = ByteUtils.a(buffer, offset);
        offset += 4;
        this.curseTurnsRemaining = ByteUtils.a(buffer, offset);
        offset += 4;
        this.enrageTriggered = ByteUtils.a(buffer, offset) != 0;
        offset += 4;

        if (this.burnTurnsRemaining <= 0) {
            this.burnTurnsRemaining = 0;
            this.burnDamagePerTurn = 0;
        } else if (this.burnDamagePerTurn < 0) {
            this.burnDamagePerTurn = 0;
        }
        if (this.poisonTurnsRemaining <= 0) {
            this.poisonTurnsRemaining = 0;
            this.poisonDamagePerTurn = 0;
        } else if (this.poisonDamagePerTurn < 0) {
            this.poisonDamagePerTurn = 0;
        }
        if (this.woundTurnsRemaining < 0) {
            this.woundTurnsRemaining = 0;
        }
        if (this.curseTurnsRemaining < 0) {
            this.curseTurnsRemaining = 0;
        }
        return offset - start;
    }

    public int a(byte[] buffer, int offset) {
        int start = offset;
        System.arraycopy(ByteUtils.a(this.currentHp), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.maxHp), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.currentMana), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.currentFood), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.earnedExp), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.earnedGold), 0, buffer, offset, 4);
        offset += 4;

        for (int i = 0; i < this.a.length; ++i) {
            offset += this.a[i].a(buffer, offset);
        }
        return offset - start;
    }

    public int b(byte[] buffer, int offset) {
        int start = offset;
        this.currentHp = this.displayedHp = ByteUtils.a(buffer, offset);
        offset += 4;
        this.maxHp = ByteUtils.a(buffer, offset);
        offset += 4;
        this.currentMana = ByteUtils.a(buffer, offset);
        offset += 4;
        this.currentFood = ByteUtils.a(buffer, offset);
        offset += 4;
        this.earnedExp = ByteUtils.a(buffer, offset);
        offset += 4;
        this.earnedGold = ByteUtils.a(buffer, offset);
        offset += 4;

        if (this.currentHp < 0) {
            this.currentHp = this.displayedHp = 0;
        }
        if (this.currentMana < 0) {
            this.currentMana = 0;
        } else if (this.currentMana > this.maxMana) {
            this.currentMana = this.maxMana;
        }
        if (this.currentFood < 0) {
            this.currentFood = 0;
        } else if (this.currentFood > this.maxFood) {
            this.currentFood = this.maxFood;
        }

        for (int i = 0; i < this.a.length; ++i) {
            offset += this.a[i].b(buffer, offset);
        }
        this.clearDebuffs();
        this.enrageTriggered = false;
        return offset - start;
    }

    public BattleFighter(HeroStats baseStats) {
        this.baseStats = baseStats;
        int level = baseStats.e;
        if (level < 30) {
            this.aiMistakeChance = 70;
        } else if (level < 60) {
            this.aiMistakeChance = 50;
        } else {
            this.aiMistakeChance = 30;
        }
        this.resolvePassiveAndArchetype();
        if (this.bossArchetype != BOSS_ARCHETYPE_NONE) {
            this.aiMistakeChance = level < 35 ? 35 : (level < 70 ? 25 : 15);
        }
        this.a();
    }

    public static int resolveBossArchetypeForTemplate(int templateId) {
        if (!CampaignData.isBossTemplate(templateId)) {
            return BOSS_ARCHETYPE_NONE;
        }
        switch (templateId) {
            case 6:  // Đinh Bộ Lĩnh
            case 74: // Ấu Rồng
                return BOSS_ARCHETYPE_OVERLORD_PHASE;
            case 9:  // Lê Hoàn
            case 13: // Bùi Quang Dũng
            case 18: // Phạm Bạch Hổ
            case 26: // Tàn Tướng Đỗ Động
            case 28: // Nguyễn Siêu
            case 29: // Nguyễn Trí Khả
            case 34: // Lữ Xử Bình
                return BOSS_ARCHETYPE_BERSERKER;
            case 14: // Nguyễn Ninh
            case 24: // Đỗ Cảnh Thạc
            case 27: // Ngô Nhật Khánh
                return BOSS_ARCHETYPE_FORTRESS;
            case 12: // Nguyễn Minh Quang
            case 22: // Ngô Xương Xí
            case 40: // Kiều Thuận
            case 47: // Ma Nhãn (Nhiệm vụ phụ #0)
            case 73: // Nhện Tinh
                return BOSS_ARCHETYPE_CONTROLLER;
            case 8:  // Lưu Cơ
            case 20: // Lã Đường
            case 30: // Lý Khuê
            case 32: // Nguyễn Thủ Tiệp
            case 38: // Kiều Công Hãn
                return BOSS_ARCHETYPE_ARCHMAGE;
            case 7:  // Nguyễn Bặc
            case 33: // Phản Tướng
            case 36: // Nguyễn Khoan
                return BOSS_ARCHETYPE_WARLORD_FIRE;
            default:
                return BOSS_ARCHETYPE_NONE;
        }
    }

    private static int resolveMonsterPassive(int templateId) {
        switch (templateId) {
            case 42: // Sơn Tặc
            case 46: // Quỷ Lùn
            case 51: // Hầu Tinh
            case 52: // Ấu Quỷ
            case 56: // Lão Hầu Tinh
            case 57: // Quái Sư
            case 60: // Độc Nhãn Trư
            case 65: // Quái Ưng
                return PASSIVE_BEAST_FURY;
            case 43: // Bọ Quái
            case 44: // Quỷ Ong
            case 50: // Xà Tinh
            case 61: // Quái Cóc
            case 62: // Sâu Quỷ
            case 63: // Trùng Quỷ
            case 64: // Lão Trùng Quỷ
            case 68: // Độc Ngư
            case 69: // Quỷ Cóc
                return PASSIVE_VENOM_BODY;
            case 45: // Thạch Sùng Tinh
            case 53: // Thổ Tinh
            case 54: // Thạch Ma
            case 58: // Mộc Ma
            case 59: // Đại Mộc Ma
            case 66: // Thằn Lằn Quỷ
            case 70: // Ma Cây
                return PASSIVE_STONE_BARK;
            case 47: // Ma Nhãn (thường)
            case 48: // Ngọc Thố Tinh
            case 49: // Ma Điểu
            case 55: // Linh Miêu
            case 67: // Hạc Yêu
            case 71: // Ma Trơi
            case 72: // Chồn Tinh
                return PASSIVE_SPIRIT_WISP;
            default:
                return PASSIVE_NONE;
        }
    }

    private void resolvePassiveAndArchetype() {
        int id = this.baseStats != null ? this.baseStats.a : -1;
        int level = this.baseStats != null ? this.baseStats.e : 1;

        this.passiveId = PASSIVE_NONE;
        this.aiArchetype = ARCHETYPE_BALANCED;
        this.bossArchetype = resolveBossArchetypeForTemplate(id);

        if (this.bossArchetype != BOSS_ARCHETYPE_NONE) {
            // Bosses use their dedicated Boss Archetype exclusively (never stacking a hidden legacy passive on top).
            switch (this.bossArchetype) {
                case BOSS_ARCHETYPE_BERSERKER:
                case BOSS_ARCHETYPE_WARLORD_FIRE:
                case BOSS_ARCHETYPE_OVERLORD_PHASE:
                    this.aiArchetype = ARCHETYPE_AGGRESSIVE;
                    break;
                case BOSS_ARCHETYPE_FORTRESS:
                    this.aiArchetype = ARCHETYPE_DEFENDER;
                    break;
                case BOSS_ARCHETYPE_CONTROLLER:
                case BOSS_ARCHETYPE_ARCHMAGE:
                default:
                    this.aiArchetype = ARCHETYPE_CASTER;
                    break;
            }
            return;
        }

        switch (id) {
            case 0:
            case 3:
                this.passiveId = PASSIVE_FIRE_SOUL;
                this.aiArchetype = ARCHETYPE_AGGRESSIVE;
                return;
            case 1:
            case 4:
                this.passiveId = PASSIVE_THUNDER_AURA;
                this.aiArchetype = ARCHETYPE_CASTER;
                return;
            case 2:
            case 5:
                this.passiveId = PASSIVE_FROST_HEART;
                this.aiArchetype = ARCHETYPE_DEFENDER;
                return;
            default:
                break;
        }

        if (level < 12) {
            return;
        }

        // Outpost guards & frontier elite soldiers (Cấp 12+)
        if (id < 42 || id == 75 || id == 76) {
            if (id == 21 || id == 23 || id == 25 || id == 35 || id == 41 || (id == 75 && (level % 2 == 0))) {
                this.passiveId = PASSIVE_IRON_BASTION;
                this.aiArchetype = ARCHETYPE_DEFENDER;
            } else if (id == 31 || id == 37 || id == 39 || id == 75 || id == 76) {
                this.passiveId = PASSIVE_TIGER_MIGHT;
                this.aiArchetype = ARCHETYPE_AGGRESSIVE;
            }
            return;
        }

        // Folklore beasts, venomous insects, stone/tree demons & wisps (Cấp 12+)
        this.passiveId = resolveMonsterPassive(id);
        switch (this.passiveId) {
            case PASSIVE_BEAST_FURY:
                this.aiArchetype = ARCHETYPE_AGGRESSIVE;
                break;
            case PASSIVE_STONE_BARK:
                this.aiArchetype = ARCHETYPE_DEFENDER;
                break;
            case PASSIVE_SPIRIT_WISP:
                this.aiArchetype = ARCHETYPE_CASTER;
                break;
            case PASSIVE_VENOM_BODY:
                this.aiArchetype = (id % 2 == 0) ? ARCHETYPE_CASTER : ARCHETYPE_AGGRESSIVE;
                break;
            default:
                break;
        }
    }

    public void disablePassivesForTutorial() {
        this.tutorialDisabled = true;
        this.passiveId = PASSIVE_NONE;
        this.bossArchetype = BOSS_ARCHETYPE_NONE;
        this.barrierHp = 0;
        this.furyStacks = 0;
        this.armorBreakTurns = 0;
        this.bossCycleTurn = 0;
        this.phase2Triggered = false;
        this.clearDebuffs();
    }

    /** Resets runtime battle state from {@link #baseStats}. */
    public void a() {
        this.maxHp = this.baseStats.b;
        this.currentHp = this.displayedHp = this.baseStats.c;
        this.currentMana = 0;
        this.currentFood = this.maxFood;
        this.shielded = false;
        this.shieldTurnsDisplay = 0;
        this.clearDebuffs();
        this.enrageTriggered = false;
        this.barrierHp = 0;
        this.furyStacks = 0;
        this.armorBreakTurns = 0;
        this.bossCycleTurn = 0;
        this.phase2Triggered = false;
        this.sealExplosionImminent = false;
        this.a = new ItemSlot[6];
        for (int i = 0; i < this.a.length; ++i) {
            this.a[i] = new ItemSlot();
            this.a[i].a = i;
        }
        this.earnedGold = 0;
        this.earnedExp = 0;

        if (!this.tutorialDisabled) {
            if (this.passiveId == PASSIVE_THUNDER_AURA || this.passiveId == PASSIVE_TIGER_MIGHT) {
                this.currentMana = 8;
            } else if (this.passiveId == PASSIVE_FROST_HEART) {
                this.currentMana = 4;
            } else if (this.passiveId == PASSIVE_SPIRIT_WISP) {
                this.currentMana = 6;
            } else if (this.passiveId == PASSIVE_STONE_BARK) {
                int stoneShield = this.maxHp * 8 / 100;
                this.addBarrier(stoneShield < 6 ? 6 : stoneShield);
            }
            switch (this.bossArchetype) {
                case BOSS_ARCHETYPE_FORTRESS:
                    this.addBarrier(this.maxHp * 12 / 100);
                    break;
                case BOSS_ARCHETYPE_OVERLORD_PHASE:
                    this.currentMana = 6;
                    break;
                case BOSS_ARCHETYPE_ARCHMAGE:
                    this.currentMana = 6;
                    break;
                default:
                    break;
            }
            if (this.baseStats != null) {
                int btMana = this.baseStats.getBreakthroughBonusStartMana();
                if (btMana > 0) {
                    this.f(btMana);
                }
            }
            if (this.relicStartManaBonus > 0) {
                this.f(this.relicStartManaBonus);
            } else if ((this.relicMask & 4) != 0) {
                this.f(4);
            }
        }
    }

    /** Configures the permanent Sacred Relic bitmask (bit0=Attack, bit1=HP, bit2=Mana, bit3=Food/Heart). */
    public void setRelicMask(int relicMask) {
        this.relicMask = this.tutorialDisabled ? 0 : relicMask;
    }

    /** Configures level-scaled battle-only Sacred Relic bonuses (Ngọc Bội starting Mana & Tượng Phật Heart heal %). */
    public void setRelicBattleBonuses(int startManaBonus, int heartHealBonusPct) {
        if (this.tutorialDisabled) {
            this.relicStartManaBonus = 0;
            this.relicHeartBonusPct = 0;
        } else {
            this.relicStartManaBonus = startManaBonus < 0 ? 0 : startManaBonus;
            this.relicHeartBonusPct = heartHealBonusPct < 0 ? 0 : heartHealBonusPct;
        }
    }

    public int getRelicHeartBonusPct() {
        if (this.tutorialDisabled) {
            return 0;
        }
        if (this.relicHeartBonusPct > 0) {
            return this.relicHeartBonusPct;
        }
        return (this.relicMask & 8) != 0 ? 15 : 0;
    }

    public boolean hasRelic(int relicBit) {
        return !this.tutorialDisabled && (this.relicMask & relicBit) != 0;
    }

    public int getShieldTurnsDisplay() {
        return this.shielded ? this.shieldTurnsDisplay : 0;
    }

    public void setShieldTurnsDisplay(int turns) {
        this.shieldTurnsDisplay = turns < 0 ? 0 : turns;
    }

    public int b() {
        return this.baseStats.a;
    }

    public int getLevel() {
        return this.baseStats.e;
    }

    public String getDisplayName() {
        int id = this.baseStats.a;
        if (id >= 0 && id < DialogueData.a.length) {
            String name = DialogueData.a[id];
            if ("PLAYER".equals(name)) {
                String playerName = CampaignData.c();
                return (playerName != null && playerName.length() > 0) ? playerName : "Chủ tướng";
            }
            return name;
        }
        return "Sứ quân";
    }

    public int c() {
        return this.aiMistakeChance;
    }

    public int[] d() {
        return this.baseStats.f;
    }

    public int e() {
        return this.baseStats.b;
    }

    public int f() {
        return this.earnedExp;
    }

    public void a(int deltaExp) {
        this.earnedExp += deltaExp;
    }

    public int g() {
        return this.earnedGold;
    }

    public void b(int deltaGold) {
        this.earnedGold += deltaGold;
    }

    public void h() {
        this.baseStats.c = this.baseStats.b;
    }

    /** Commits remaining battle HP (clamped to base max HP) back into persistent {@link HeroStats}. */
    public void i() {
        if (this.currentHp > this.baseStats.b) {
            this.currentHp = this.baseStats.b;
        }
        this.baseStats.c = this.currentHp;
    }

    public int j() {
        return this.lowHpThreshold;
    }

    public void c(int lowHpThreshold) {
        this.lowHpThreshold = lowHpThreshold;
    }

    public boolean k() {
        return this.shielded;
    }

    public void a(boolean shielded) {
        this.shielded = shielded;
        if (!shielded) {
            this.shieldTurnsDisplay = 0;
        }
    }

    public int l() {
        return this.displayedHp;
    }

    public void d(int displayedHp) {
        this.displayedHp = displayedHp;
    }

    /** Returns effective attack power (accounting for passives, Last Stand, food morale/starvation, wound, and x2 Sword buff). */
    public int m() {
        int atk = this.baseStats.d;
        if (!this.tutorialDisabled) {
            if (this.passiveId == PASSIVE_FIRE_SOUL
                    || this.bossArchetype == BOSS_ARCHETYPE_BERSERKER
                    || this.bossArchetype == BOSS_ARCHETYPE_WARLORD_FIRE) {
                int bonus = atk * 15 / 100;
                atk += (bonus < 1 ? 1 : bonus);
            } else if (this.passiveId == PASSIVE_FROST_HEART) {
                int pct = this.barrierHp > 0 ? 20 : 10;
                int bonus = atk * pct / 100;
                atk += (bonus < 1 ? 1 : bonus);
            } else if ((this.passiveId == PASSIVE_TIGER_MIGHT || this.passiveId == PASSIVE_BEAST_FURY)
                    && this.maxHp > 0 && (this.currentHp * 100 / this.maxHp) < 45) {
                int bonus = atk * 20 / 100;
                atk += (bonus < 1 ? 1 : bonus);
            }
            if (this.phase2Triggered && this.bossArchetype == BOSS_ARCHETYPE_OVERLORD_PHASE) {
                int p2Bonus = atk * 15 / 100;
                atk += (p2Bonus < 1 ? 1 : p2Bonus);
            }
            if (this.isHighBossLastStandActive()) {
                int lsBonus = atk * 10 / 100;
                atk += (lsBonus < 1 ? 1 : lsBonus);
            }
            if (this.woundTurnsRemaining > 0) {
                atk = atk * 80 / 100;
                if (atk < 1) {
                    atk = 1;
                }
            }
            if (this.isStarving()) {
                atk = atk * 70 / 100;
                if (atk < 1) {
                    atk = 1;
                }
            } else if (this.isLowFood()) {
                atk = atk * 85 / 100;
                if (atk < 1) {
                    atk = 1;
                }
            }
        }
        return this.a[0].b > 0 ? (atk + atk) : atk;
    }

    public boolean isLowFood() {
        return !this.tutorialDisabled && this.currentFood > 0
                && this.maxFood > 0 && (this.currentFood * 100 / this.maxFood) <= 25;
    }

    public boolean isStarving() {
        return !this.tutorialDisabled && this.currentFood <= 0;
    }

    public boolean isHighBossLastStandActive() {
        return !this.tutorialDisabled
                && this.bossArchetype == BOSS_ARCHETYPE_BERSERKER
                && this.getLevel() >= 50
                && this.maxHp > 0
                && this.currentHp > 0
                && (this.currentHp * 100 / this.maxHp) < 35;
    }

    public int n() {
        return this.maxMana;
    }

    public int o() {
        return this.currentMana;
    }

    public void e(int mana) {
        if (mana < 0) {
            mana = 0;
        } else if (mana > this.maxMana) {
            mana = this.maxMana;
        }
        this.currentMana = mana;
    }

    public void f(int deltaMana) {
        this.e(this.currentMana + deltaMana);
    }

    public void a(int manaCost, boolean ignoreShield) {
        if (!this.shielded || ignoreShield) {
            this.e(this.currentMana - manaCost);
        }
    }

    public int p() {
        return this.currentFood;
    }

    public void g(int food) {
        if (food < 0) {
            food = 0;
        } else if (food > this.maxFood) {
            food = this.maxFood;
        }
        this.currentFood = food;
    }

    public int q() {
        return this.maxFood;
    }

    public void h(int maxFood) {
        this.maxFood = maxFood;
        this.currentFood = maxFood;
    }

    public void i(int deltaFood) {
        this.g(this.currentFood + deltaFood);
    }

    public void j(int foodCost) {
        this.g(this.currentFood - foodCost);
    }

    public int r() {
        return this.currentHp;
    }

    public int getBossArchetype() {
        return this.tutorialDisabled ? BOSS_ARCHETYPE_NONE : this.bossArchetype;
    }

    public boolean isBossFighter() {
        return !this.tutorialDisabled && this.bossArchetype != BOSS_ARCHETYPE_NONE;
    }

    public int getBarrierHp() {
        return this.tutorialDisabled ? 0 : this.barrierHp;
    }

    public int getMaxBarrierHp() {
        int cap = this.maxHp * 20 / 100;
        return cap < 10 ? 10 : cap;
    }

    public void setBarrierHp(int value) {
        int maxB = this.getMaxBarrierHp();
        if (value < 0) {
            value = 0;
        } else if (value > maxB) {
            value = maxB;
        }
        this.barrierHp = value;
    }

    /** Adds virtual armor (Barrier HP, capped at 30% MaxHP) and returns actual amount added. */
    public int addBarrier(int amount) {
        if (this.tutorialDisabled || amount <= 0) {
            return 0;
        }
        int before = this.barrierHp;
        this.setBarrierHp(before + amount);
        return this.barrierHp - before;
    }

    /** Shatters up to {@code amount} of virtual armor (Barrier HP) and returns actual amount shattered. */
    public int shatterBarrier(int amount) {
        if (this.barrierHp <= 0 || amount <= 0) {
            return 0;
        }
        int removed = amount > this.barrierHp ? this.barrierHp : amount;
        this.barrierHp -= removed;
        return removed;
    }

    public int getFuryStacks() {
        return this.tutorialDisabled ? 0 : this.furyStacks;
    }

    public void setFuryStacks(int stacks) {
        if (stacks < 0) {
            stacks = 0;
        } else if (stacks > 4) {
            stacks = 4;
        }
        this.furyStacks = stacks;
    }

    public void addFuryStacks(int delta) {
        if (!this.tutorialDisabled) {
            this.setFuryStacks(this.furyStacks + delta);
        }
    }

    public int getArmorBreakTurns() {
        return this.tutorialDisabled ? 0 : this.armorBreakTurns;
    }

    public boolean isArmorBroken() {
        return !this.tutorialDisabled && this.armorBreakTurns > 0;
    }

    public void applyArmorBreak(int turns) {
        if (this.tutorialDisabled || this.shielded) {
            return;
        }
        if (turns > this.armorBreakTurns) {
            this.armorBreakTurns = turns;
        }
    }

    public int getBossCycleTurn() {
        return this.bossCycleTurn;
    }

    public void setBossCycleTurn(int turn) {
        this.bossCycleTurn = turn < 0 ? 0 : turn;
    }

    public int incrementBossCycleTurn() {
        ++this.bossCycleTurn;
        return this.bossCycleTurn;
    }

    public boolean isPhase2Triggered() {
        return !this.tutorialDisabled && this.phase2Triggered;
    }

    public void setPhase2Triggered(boolean triggered) {
        this.phase2Triggered = triggered;
    }

    public void setSealExplosionImminent(boolean imminent) {
        this.sealExplosionImminent = imminent;
    }

    /** Returns true when an active Boss Seal on the board is down to 1 turn and will detonate on the Boss's next turn. */
    public boolean isBossCycleImminent() {
        return this.isBossFighter() && this.currentHp > 0 && this.sealExplosionImminent;
    }

    /** Applies incoming spell/standard damage (blocked by Khiên Sét; spells shred Barrier at 150% efficiency). */
    public void k(int damage) {
        if (this.shielded) {
            return;
        }
        this.applyDamageInternal(damage, false, true);
    }

    /** Applies shield-piercing damage (used when Lửa Địa Ngục / Long Trời Lở Đất shatters Khiên Sét). */
    public void applyPierceDamage(int damage) {
        this.applyDamageInternal(damage, false, true);
    }

    /**
     * Applies Sword match damage against this fighter:
     * - Basic 3-Sword matches take -20% mitigation against Siêu Phòng Thủ (Fortress Boss).
     * - Heavy 4+ Sword / Fiery Sword matches deal full 100% damage AND shred Barrier at 150% efficiency.
     */
    public void applySwordAttackDamage(int rawDamage, boolean isHeavySwordHit) {
        if (this.shielded) {
            return;
        }
        this.applyDamageInternal(rawDamage, !isHeavySwordHit, isHeavySwordHit);
    }

    private void applyDamageInternal(int rawDamage, boolean isBasicThreeSword, boolean bonusBarrierShred) {
        if (rawDamage <= 0) {
            return;
        }
        int damage = rawDamage;
        if (!this.tutorialDisabled) {
            if (this.burnTurnsRemaining > 0) {
                // Bị Bỏng: nhận thêm +20% sát thương khi bị tấn công
                damage = damage * 120 / 100;
            }
            if (this.armorBreakTurns > 0) {
                // Hàn Băng: giòn băng nhận thêm +25% sát thương và xuyên thủ cứng
                damage = damage * 125 / 100;
            }
            if (this.armorBreakTurns <= 0 && isBasicThreeSword && this.bossArchetype == BOSS_ARCHETYPE_FORTRESS) {
                // Siêu Phòng Thủ: giảm 20% sát thương từ Kiếm ghép 3 cơ bản
                damage = damage * 80 / 100;
            } else if (this.bossArchetype == BOSS_ARCHETYPE_OVERLORD_PHASE) {
                damage = damage * 90 / 100;
            } else if (this.armorBreakTurns <= 0 && this.passiveId == PASSIVE_IRON_BASTION && this.maxHp > 0 && (this.currentHp * 100 / this.maxHp) > 40) {
                damage = damage * 80 / 100;
            } else if (this.passiveId == PASSIVE_DRAGON_MAJESTY) {
                damage = damage * 88 / 100;
            }
        }
        if (this.a[3].b > 0) {
            damage = damage / 4;
        }
        if (damage < 1) {
            damage = 1;
        }
        this.consumeBarrierAndHp(damage, bonusBarrierShred);
    }

    private void consumeBarrierAndHp(int damage, boolean bonusBarrierShred) {
        int hpDamage = damage;
        if (!this.tutorialDisabled && this.barrierHp > 0) {
            if (bonusBarrierShred) {
                // Tuyệt chiêu & Kiếm 4+/Kiếm Lửa phá Giáp Ảo với hiệu suất 150%
                int effectiveShred = damage * 3 / 2;
                if (effectiveShred < 2) {
                    effectiveShred = 2;
                }
                if (effectiveShred <= this.barrierHp) {
                    this.barrierHp -= effectiveShred;
                    hpDamage = 0;
                } else {
                    int dmgUsedOnBarrier = (this.barrierHp * 2 + 2) / 3;
                    this.barrierHp = 0;
                    hpDamage = damage - dmgUsedOnBarrier;
                    if (hpDamage < 0) {
                        hpDamage = 0;
                    }
                }
            } else {
                int absorbed = damage > this.barrierHp ? this.barrierHp : damage;
                this.barrierHp -= absorbed;
                hpDamage = damage - absorbed;
            }
        }
        if (hpDamage > 0) {
            this.m(this.currentHp - hpDamage);
        }

        if (!this.tutorialDisabled && !this.enrageTriggered && this.passiveId == PASSIVE_FROST_HEART) {
            if (this.currentHp > 0 && this.maxHp > 0 && (this.currentHp * 100 / this.maxHp) <= 35) {
                this.enrageTriggered = true;
                this.f(12);
                this.reduceDebuffsByOneTurn();
            }
        }
    }

    /**
     * Heals HP up to maxHp; for Water heroes ({@link #PASSIVE_FROST_HEART}), 75% of any excess
     * healing crystallizes into virtual Barrier HP (capped at 20% MaxHP, activating +20% ATK Hàn Giáp).
     */
    public int healWithFrostBarrierOverflow(int healAmount) {
        if (healAmount <= 0) {
            return 0;
        }
        int effectiveHeal = this.scaleIncomingHeal(healAmount);
        int missing = this.maxHp - this.currentHp;
        int overflow = effectiveHeal - (missing > 0 ? missing : 0);
        this.m(this.currentHp + effectiveHeal);
        if (!this.tutorialDisabled && this.passiveId == PASSIVE_FROST_HEART && overflow > 0 && this.maxHp > 0) {
            int crystalCap = this.maxHp * 20 / 100;
            if (crystalCap < 10) {
                crystalCap = 10;
            }
            if (this.barrierHp < crystalCap) {
                int gain = overflow * 3 / 4;
                if (gain < 1) {
                    gain = 1;
                }
                int room = crystalCap - this.barrierHp;
                if (gain > room) {
                    gain = room;
                }
                return this.addBarrier(gain);
            }
        }
        return 0;
    }

    public int getPassiveId() {
        return this.tutorialDisabled ? PASSIVE_NONE : this.passiveId;
    }

    public int getAiArchetype() {
        return this.aiArchetype;
    }

    public String getPassiveName() {
        if (this.isBossFighter()) {
            return getBossArchetypeTitle(this.bossArchetype);
        }
        switch (this.getPassiveId()) {
            case PASSIVE_FIRE_SOUL:
                return "Hỏa Linh";
            case PASSIVE_THUNDER_AURA:
                return "Lôi Điện";
            case PASSIVE_FROST_HEART:
                return "Băng Tâm";
            case PASSIVE_TIGER_MIGHT:
                return "Hổ Uy";
            case PASSIVE_IRON_BASTION:
                return "Thành Đồng";
            case PASSIVE_DRAGON_MAJESTY:
                return "Long Uy";
            case PASSIVE_BEAST_FURY:
                return "Hung Thú";
            case PASSIVE_VENOM_BODY:
                return "Độc Thể";
            case PASSIVE_STONE_BARK:
                return "Thạch Mộc Giáp";
            case PASSIVE_SPIRIT_WISP:
                return "Huyễn Yêu";
            default:
                return "Không có";
        }
    }

    public String getPassiveShortDesc() {
        if (this.isBossFighter()) {
            String base = this.getBossArchetypeBadge() + " " + this.getBossAbilityShortDesc();
            if (this.bossArchetype == BOSS_ARCHETYPE_BERSERKER && this.getLevel() >= 50) {
                return base + "; <35% Máu Tử Chiến +10% Công";
            }
            return base;
        }
        switch (this.getPassiveId()) {
            case PASSIVE_FIRE_SOUL:
                return "Hỏa Linh (+15% Công, +20% ST Kiếm Lửa, x3+ rèn Kiếm Lửa)";
            case PASSIVE_THUNDER_AURA:
                int btMana = this.baseStats != null ? this.baseStats.getBreakthroughBonusStartMana() : 0;
                return "Lôi Điện (+" + (8 + btMana) + " Nộ đầu, hoàn 4 Nộ, ghép 4+ thêm +2 Nộ)";
            case PASSIVE_FROST_HEART:
                return "Băng Tâm (+10~20% Công, +2% Máu & +2 Nộ/lượt, Tim dư -> Giáp, Kiếm 4+ gây Hàn Băng)";
            case PASSIVE_TIGER_MIGHT:
                return "Hổ Uy (+8 Nộ đầu, Máu <45% +20% Công)";
            case PASSIVE_IRON_BASTION:
                return "Thành Đồng (Máu >40% giảm 20% ST)";
            case PASSIVE_DRAGON_MAJESTY:
                return "Long Uy (+3% Máu, +3 Nộ/lượt, -15% ST)";
            case PASSIVE_BEAST_FURY:
                return "Hung Thú (Máu <45% hóa điên +20% Công)";
            case PASSIVE_VENOM_BODY:
                return "Độc Thể (Kiếm 4+/Lửa gây Độc, giảm 15% ST độc)";
            case PASSIVE_STONE_BARK:
                return "Thạch Mộc Giáp (+8% Giáp Ảo đầu trận)";
            case PASSIVE_SPIRIT_WISP:
                return "Huyễn Yêu (+6 Nộ đầu, hồi +1 Nộ/lượt)";
            default:
                return "Chiến Binh (Cân bằng)";
        }
    }

    public static String getBossArchetypeTitle(int archetype) {
        switch (archetype) {
            case BOSS_ARCHETYPE_BERSERKER:
                return "Siêu Tấn Công";
            case BOSS_ARCHETYPE_FORTRESS:
                return "Siêu Phòng Thủ";
            case BOSS_ARCHETYPE_CONTROLLER:
                return "Siêu Khống Chế";
            case BOSS_ARCHETYPE_ARCHMAGE:
                return "Siêu Pháp Sư";
            case BOSS_ARCHETYPE_WARLORD_FIRE:
                return "Hỏa Áp Bức";
            case BOSS_ARCHETYPE_OVERLORD_PHASE:
                return "Bá Vương 2 Pha";
            default:
                return "";
        }
    }

    public static String getBossArchetypeBadgeForArchetype(int archetype) {
        switch (archetype) {
            case BOSS_ARCHETYPE_BERSERKER:
                return "[SIÊU TẤN CÔNG]";
            case BOSS_ARCHETYPE_FORTRESS:
                return "[SIÊU PHÒNG THỦ]";
            case BOSS_ARCHETYPE_CONTROLLER:
                return "[SIÊU KHỐNG CHẾ]";
            case BOSS_ARCHETYPE_ARCHMAGE:
                return "[SIÊU PHÁP SƯ]";
            case BOSS_ARCHETYPE_WARLORD_FIRE:
                return "[HỎA ÁP BỨC]";
            case BOSS_ARCHETYPE_OVERLORD_PHASE:
                return "[BÁ VƯƠNG 2 PHA]";
            default:
                return "";
        }
    }

    public String getBossArchetypeBadge() {
        return getBossArchetypeBadgeForArchetype(this.getBossArchetype());
    }

    public String getBossShortHudTag() {
        switch (this.getBossArchetype()) {
            case BOSS_ARCHETYPE_BERSERKER:
                return "CÔNG";
            case BOSS_ARCHETYPE_FORTRESS:
                return "THỦ";
            case BOSS_ARCHETYPE_CONTROLLER:
                return "KHỐNG";
            case BOSS_ARCHETYPE_ARCHMAGE:
                return "PHÁP";
            case BOSS_ARCHETYPE_WARLORD_FIRE:
                return "HỎA";
            case BOSS_ARCHETYPE_OVERLORD_PHASE:
                return this.phase2Triggered ? "PHA 2" : "2 PHA";
            default:
                return null;
        }
    }

    public static String getBossAbilityDescForArchetype(int archetype) {
        switch (archetype) {
            case BOSS_ARCHETYPE_BERSERKER:
                return "+15% Công; đặt Ấn Trảm Kích (nổ gây 1.25x Công + rèn 1 Kiếm Lửa)";
            case BOSS_ARCHETYPE_FORTRESS:
                return "12% Giáp đầu, giảm 20% ST Kiếm 3 viên; đặt Ấn Thành Đồng (nổ dựng +12% Giáp)";
            case BOSS_ARCHETYPE_CONTROLLER:
                return "Hồi 2% Máu/lượt; đặt Ấn Áp Chế (nổ rút 6 Nộ + gây Nhiễu Hồn/Độc)";
            case BOSS_ARCHETYPE_ARCHMAGE:
                return "6 Nộ đầu, +2 Nộ/lượt; đặt Ấn Tụ Lôi (nổ nạp ngay +10 Nộ cho Boss)";
            case BOSS_ARCHETYPE_WARLORD_FIRE:
                return "+15% Công, T.Chiêu đốt 65 Lương; đặt Ấn Binh Hỏa (nổ đốt 120 Lương + Bỏng)";
            case BOSS_ARCHETYPE_OVERLORD_PHASE:
                return "-10% ST nhận, +2% Máu/lượt; <=50% Máu mở Pha 2 (+15% Giáp, +15% Công, 2 Ấn Hấp Huyết)";
            default:
                return "";
        }
    }

    public String getBossAbilityShortDesc() {
        return getBossAbilityDescForArchetype(this.getBossArchetype());
    }

    public static String getBossCounterStrategyForArchetype(int archetype) {
        switch (archetype) {
            case BOSS_ARCHETYPE_BERSERKER:
                return "Phá Ấn để cướp +6 Nộ & rèn 1 Kiếm Lửa; thủ Quỷ Diện Giáp hoặc bật Khiên Sét chặn đòn";
            case BOSS_ARCHETYPE_FORTRESS:
                return "Phá Ấn phá thêm 6% Giáp Boss; dùng T.Chiêu hoặc Kiếm 4+/Kiếm Lửa phá Giáp gấp 1.5 lần";
            case BOSS_ARCHETYPE_CONTROLLER:
                return "Phá Ấn để giảm 1 lượt debuff; dùng Quả Cầu Lửa / Bình Thuốc / Cam Lộ Thủy";
            case BOSS_ARCHETYPE_ARCHMAGE:
                return "Phá Ấn để cướp +6 Nộ & rút 4 Nộ Boss; dùng Mũi Tên Băng / Băng Phong áp chế Nộ";
            case BOSS_ARCHETYPE_WARLORD_FIRE:
                return "Phá Ấn Binh Hỏa đoạt lại +160 Lương; giữ Lương thực > 25% bằng Đùi Gà";
            case BOSS_ARCHETYPE_OVERLORD_PHASE:
                return "Pha 1 tích Nộ; vào Pha 2 dùng T.Chiêu quét 2 Ấn để phản sát thương & phá Giáp";
            default:
                return "";
        }
    }

    public String getBossCounterStrategy() {
        return getBossCounterStrategyForArchetype(this.getBossArchetype());
    }

    /** Allocation-friendly tactical summary for World Map Quest Tracker & Arena preview. */
    public static String getBossTacticalSummaryForTemplate(int templateId) {
        int arch = resolveBossArchetypeForTemplate(templateId);
        if (arch == BOSS_ARCHETYPE_NONE) {
            return null;
        }
        return getBossArchetypeBadgeForArchetype(arch)
                + " Khả năng: " + getBossAbilityDescForArchetype(arch)
                + " | Phương án: " + getBossCounterStrategyForArchetype(arch);
    }

    public int getBurnTurns() {
        return this.burnTurnsRemaining;
    }

    public int getBurnDamage() {
        return this.burnDamagePerTurn;
    }

    public int getPoisonTurns() {
        return this.poisonTurnsRemaining;
    }

    public int getPoisonDamage() {
        return this.poisonDamagePerTurn;
    }

    public boolean isEnrageTriggered() {
        return this.enrageTriggered;
    }

    public boolean isPoisoned() {
        return !this.tutorialDisabled && this.poisonTurnsRemaining > 0;
    }

    public int getWoundTurns() {
        return this.woundTurnsRemaining;
    }

    public boolean isWounded() {
        return !this.tutorialDisabled && this.woundTurnsRemaining > 0;
    }

    public int getCurseTurns() {
        return this.curseTurnsRemaining;
    }

    public boolean isCursed() {
        return !this.tutorialDisabled && this.curseTurnsRemaining > 0;
    }

    public boolean hasAnyDebuff() {
        return !this.tutorialDisabled
                && (this.burnTurnsRemaining > 0
                || this.poisonTurnsRemaining > 0
                || this.woundTurnsRemaining > 0
                || this.curseTurnsRemaining > 0
                || this.armorBreakTurns > 0);
    }

    /** Inflicts Burn (DoT at turn start + takes +20% damage from attacks). */
    public void applyBurn(int turns, int damagePerTurn) {
        if (this.tutorialDisabled || this.shielded) {
            return;
        }
        if (turns > this.burnTurnsRemaining) {
            this.burnTurnsRemaining = turns;
        }
        if (damagePerTurn > this.burnDamagePerTurn) {
            this.burnDamagePerTurn = damagePerTurn;
        }
    }

    /** Inflicts Poison (DoT at turn start + reduces Heart tile healing by 30%). */
    public void applyPoison(int turns, int damagePerTurn) {
        if (this.tutorialDisabled || this.shielded) {
            return;
        }
        if (turns > this.poisonTurnsRemaining) {
            this.poisonTurnsRemaining = turns;
        }
        if (damagePerTurn > this.poisonDamagePerTurn) {
            this.poisonDamagePerTurn = damagePerTurn;
        }
    }

    /** Inflicts Heavy Wound (-20% Attack + +75% Food cost per action). */
    public void applyWound(int turns) {
        if (this.tutorialDisabled || this.shielded) {
            return;
        }
        if (turns > this.woundTurnsRemaining) {
            this.woundTurnsRemaining = turns;
        }
    }

    /** Inflicts Soul Disruption (-40% Mana gained from matching Mana gems). */
    public void applyCurse(int turns) {
        if (this.tutorialDisabled || this.shielded) {
            return;
        }
        if (turns > this.curseTurnsRemaining) {
            this.curseTurnsRemaining = turns;
        }
    }

    /** Cleanses all negative status ailments (Burn, Poison, Wound, Soul Disruption, Armor Break). */
    public void clearDebuffs() {
        this.burnTurnsRemaining = 0;
        this.burnDamagePerTurn = 0;
        this.poisonTurnsRemaining = 0;
        this.poisonDamagePerTurn = 0;
        this.woundTurnsRemaining = 0;
        this.curseTurnsRemaining = 0;
        this.armorBreakTurns = 0;
    }

    /** Reduces all active debuff durations by 1 turn (used by Bình Thuốc item and Ngọc Hộ Giáp). */
    public void reduceDebuffsByOneTurn() {
        if (this.burnTurnsRemaining > 0) {
            --this.burnTurnsRemaining;
            if (this.burnTurnsRemaining <= 0) {
                this.burnDamagePerTurn = 0;
            }
        }
        if (this.poisonTurnsRemaining > 0) {
            --this.poisonTurnsRemaining;
            if (this.poisonTurnsRemaining <= 0) {
                this.poisonDamagePerTurn = 0;
            }
        }
        if (this.woundTurnsRemaining > 0) {
            --this.woundTurnsRemaining;
        }
        if (this.curseTurnsRemaining > 0) {
            --this.curseTurnsRemaining;
        }
        if (this.armorBreakTurns > 0) {
            --this.armorBreakTurns;
        }
    }

    public void clearBurn() {
        this.clearDebuffs();
    }

    /**
     * Processes start-of-turn passive regeneration and status ailment damage/durations.
     */
    public void processTurnStartEffects() {
        if (this.tutorialDisabled || this.currentHp <= 0) {
            return;
        }
        if (this.passiveId == PASSIVE_FROST_HEART || this.bossArchetype == BOSS_ARCHETYPE_CONTROLLER) {
            int regen = this.maxHp * 2 / 100;
            if (regen < 2) {
                regen = 2;
            }
            this.l(regen);
            if (this.passiveId == PASSIVE_FROST_HEART) {
                this.f(2);
            }
        } else if (this.passiveId == PASSIVE_DRAGON_MAJESTY || this.bossArchetype == BOSS_ARCHETYPE_OVERLORD_PHASE) {
            int regen = this.maxHp * 2 / 100;
            if (regen < 2) {
                regen = 2;
            }
            this.l(regen);
            if (this.passiveId == PASSIVE_DRAGON_MAJESTY) {
                this.f(2);
            }
        }
        if (this.bossArchetype == BOSS_ARCHETYPE_ARCHMAGE) {
            this.f(2);
        } else if (this.passiveId == PASSIVE_SPIRIT_WISP) {
            this.f(1);
        }

        if (this.burnTurnsRemaining > 0) {
            --this.burnTurnsRemaining;
            if (!this.shielded && this.burnDamagePerTurn > 0 && this.currentHp > 1) {
                int dotDmg = this.burnDamagePerTurn;
                if (this.passiveId == PASSIVE_VENOM_BODY) {
                    dotDmg = dotDmg * 85 / 100;
                    if (dotDmg < 1) {
                        dotDmg = 1;
                    }
                }
                int nextHp = this.currentHp - dotDmg;
                if (nextHp < 1) {
                    nextHp = 1;
                }
                this.m(nextHp);
            }
            if (this.burnTurnsRemaining <= 0) {
                this.burnDamagePerTurn = 0;
            }
        }

        if (this.poisonTurnsRemaining > 0) {
            --this.poisonTurnsRemaining;
            if (!this.shielded && this.poisonDamagePerTurn > 0 && this.currentHp > 1) {
                int dotDmg = this.poisonDamagePerTurn;
                if (this.passiveId == PASSIVE_VENOM_BODY) {
                    dotDmg = dotDmg * 85 / 100;
                    if (dotDmg < 1) {
                        dotDmg = 1;
                    }
                }
                int nextHp = this.currentHp - dotDmg;
                if (nextHp < 1) {
                    nextHp = 1;
                }
                this.m(nextHp);
            }
            if (this.poisonTurnsRemaining <= 0) {
                this.poisonDamagePerTurn = 0;
            }
        }

        if (this.woundTurnsRemaining > 0) {
            --this.woundTurnsRemaining;
        }

        if (this.curseTurnsRemaining > 0) {
            --this.curseTurnsRemaining;
        }

        if (this.armorBreakTurns > 0) {
            --this.armorBreakTurns;
        }

        if (this.isStarving() && this.currentHp > 0) {
            int starveDmg = this.maxHp * 6 / 100;
            if (starveDmg < 3) {
                starveDmg = 3;
            }
            this.m(this.currentHp - starveDmg);
        }
    }

    public boolean isEnragedActive() {
        return !this.tutorialDisabled
                && (((this.passiveId == PASSIVE_TIGER_MIGHT || this.passiveId == PASSIVE_BEAST_FURY)
                && this.maxHp > 0
                && (this.currentHp * 100 / this.maxHp) < 45)
                || this.isHighBossLastStandActive()
                || (this.phase2Triggered && this.bossArchetype == BOSS_ARCHETYPE_OVERLORD_PHASE));
    }

    private int scaleIncomingHeal(int healAmount) {
        if (healAmount <= 0) {
            return 0;
        }
        if (!this.tutorialDisabled && this.armorBreakTurns > 0 && healAmount > 1) {
            int chilled = healAmount * 60 / 100;
            return chilled < 1 ? 1 : chilled;
        }
        return healAmount;
    }

    public void l(int healAmount) {
        this.m(this.currentHp + this.scaleIncomingHeal(healAmount));
    }

    public void m(int hp) {
        if (hp < 0) {
            hp = 0;
        } else if (hp > this.maxHp) {
            hp = this.maxHp;
        }
        this.currentHp = hp;
    }

    public int s() {
        return this.maxHp;
    }

    public void n(int maxHp) {
        this.maxHp = maxHp;
    }
}
