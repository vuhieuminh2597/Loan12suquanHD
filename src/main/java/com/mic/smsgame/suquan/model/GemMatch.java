package com.mic.smsgame.suquan.model;

/**
 * Matched contiguous run or spell-cleared rectangular region on the match-3 board.
 */
public class GemMatch {
    /** Start board row (including 2-cell sentinel border). */
    public int a;
    /** Start board column (including 2-cell sentinel border). */
    public int b;
    /** Height in rows. */
    public int c;
    /** Width in columns. */
    public int d;
    /** Gem type ID ({@code 0..6}). */
    public int e;
    /** Combined match length for bonus/super-explosion checks. */
    public int f;
}
