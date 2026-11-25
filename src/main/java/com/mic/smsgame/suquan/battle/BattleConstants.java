package com.mic.smsgame.suquan.battle;

/**
 * Shared gem-type lookup tables and AI/score weight constants for match-3 battles.
 */
public interface BattleConstants {
    /** Standard spawnable gem types: sword(0), food(1), heart(2), mana(3), gold(4), exp(5). */
    byte[] h = new byte[]{0, 1, 2, 3, 4, 5};
    /** Match-group mapping where fire-sword (6) matches with regular sword (0), 7..11 are empty/sentinels. */
    byte[] i = new byte[]{0, 1, 2, 3, 4, 5, 0, 7, 8, 9, 10, 11};
    /** Match-length bonus multipliers indexed by run length. */
    int[] j = new int[]{1, 1, 1, 1, 20, 40, 60, 80, 100};
}
