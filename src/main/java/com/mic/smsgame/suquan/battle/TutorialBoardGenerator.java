package com.mic.smsgame.suquan.battle;

import com.mg.engine.util.RandomUtils;

/**
 * Deterministic initial board layout for the interactive tutorial battle with Lưu Cơ.
 */
public class TutorialBoardGenerator extends BoardGenerator {
    private static final int[][] TUTORIAL_GRID = new int[][]{
            {9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9},
            {9, 11, 10, 10, 10, 10, 10, 10, 10, 10, 11, 9},
            {9, 11, 1, 4, 0, 2, 3, 5, 5, 0, 11, 9},
            {9, 11, 1, 0, 4, 0, 2, 5, 4, 1, 11, 9},
            {9, 11, 0, 1, 0, 0, 3, 1, 0, 0, 11, 9},
            {9, 11, 1, 0, 5, 1, 1, 0, 0, 5, 11, 9},
            {9, 11, 1, 3, 2, 4, 2, 5, 2, 6, 11, 9},
            {9, 11, 3, 2, 3, 2, 4, 2, 4, 2, 11, 9},
            {9, 11, 4, 4, 1, 3, 0, 4, 2, 5, 11, 9},
            {9, 11, 1, 1, 0, 0, 1, 1, 2, 3, 11, 9},
            {9, 11, 10, 10, 10, 10, 10, 10, 10, 10, 11, 9},
            {9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9}
    };

    public void a(int[][] grid) {
        for (int row = 2; row < 10; ++row) {
            for (int col = 2; col < 10; ++col) {
                grid[row][col] = TUTORIAL_GRID[row][col];
            }
        }
    }

    public void a(int[][] grid, int row, int col) {
        grid[row][col] = 1 + RandomUtils.nextInt(BattleConstants.h.length - 1);
    }
}
