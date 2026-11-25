package com.mic.smsgame.suquan.model;

/**
 * Represents a valid match-3 swap between {@code (a, b)} and {@code (c, d)}
 * along with the four packed horizontal/vertical match masks in {@link #e}.
 */
public class SwapMove {
    public int a;
    public int b;
    public int c;
    public int d;
    public int[] e = new int[4];
}
