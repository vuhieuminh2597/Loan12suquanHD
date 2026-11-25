package com.mic.smsgame.suquan.battle;

/**
 * Abstract strategy for populating and refilling the 12x12 padded 8x8 match-3 board.
 */
public abstract class BoardGenerator implements BattleConstants {
    protected void b(int[][] grid) {
        for (int row = 9; row >= 2; --row) {
            for (int col = 9; col >= 2; --col) {
                grid[row][col] = 7;
            }
        }
    }

    /** Populates the initial 8x8 playable interior (rows 2..9, cols 2..9). */
    public abstract void a(int[][] grid);

    /** Generates a single replacement gem at {@code (row, col)}. */
    public abstract void a(int[][] grid, int row, int col);
}
