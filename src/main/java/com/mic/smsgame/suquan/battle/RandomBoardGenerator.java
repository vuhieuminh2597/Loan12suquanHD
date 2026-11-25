package com.mic.smsgame.suquan.battle;

import com.mg.engine.util.RandomUtils;

/**
 * Fair random match-3 board generator guaranteeing at least one valid move
 * and zero pre-existing 3-in-a-row matches on spawn.
 */
public class RandomBoardGenerator extends BoardGenerator {
    public void a(int[][] grid) {
        this.b(grid);
        int gemTypeCount = BattleConstants.h.length;

        // Fill checkerboard half first.
        boolean shiftCol = false;
        for (int row = 2; row < 10; ++row) {
            int col = shiftCol ? 3 : 2;
            shiftCol = !shiftCol;
            for (; col < 10; col += 2) {
                grid[row][col] = RandomUtils.nextInt(gemTypeCount);
            }
        }

        // Guarantee at least one playable 3-match pattern near the center.
        int seedRow = RandomUtils.nextInt(5) + 4;
        int seedCol = RandomUtils.nextInt(6) + 3;
        if (grid[seedRow][seedCol] != 7) {
            --seedRow;
        }
        switch (RandomUtils.nextInt(4)) {
            case 0:
                grid[seedRow][seedCol - 1] = grid[seedRow][seedCol + 1] = grid[seedRow - 1][seedCol];
                break;
            case 1:
                grid[seedRow][seedCol - 1] = grid[seedRow][seedCol + 1] = grid[seedRow + 1][seedCol];
                break;
            case 2:
                grid[seedRow - 1][seedCol] = grid[seedRow + 1][seedCol] = grid[seedRow][seedCol - 1];
                break;
            default:
                grid[seedRow - 1][seedCol] = grid[seedRow + 1][seedCol] = grid[seedRow][seedCol + 1];
                break;
        }

        // Fill remaining checkerboard squares avoiding any immediate 3-in-a-row.
        shiftCol = true;
        for (int row = 2; row < 10; ++row) {
            int col = shiftCol ? 3 : 2;
            shiftCol = !shiftCol;
            for (; col < 10; col += 2) {
                boolean[] forbidden = new boolean[BattleConstants.h.length];
                int left1 = grid[row][col - 1];
                int left2 = grid[row][col - 2];
                int right1 = grid[row][col + 1];
                int right2 = grid[row][col + 2];
                if (left1 == left2) {
                    forbidden[left1] = true;
                }
                if (right1 == right2) {
                    forbidden[right1] = true;
                }
                if (right1 == left1) {
                    forbidden[left1] = true;
                }

                int up1 = grid[row - 1][col];
                int up2 = grid[row - 2][col];
                int down1 = grid[row + 1][col];
                int down2 = grid[row + 2][col];
                if (up1 == up2) {
                    forbidden[up1] = true;
                }
                if (down1 == down2) {
                    forbidden[down1] = true;
                }
                if (up1 == down1) {
                    forbidden[up1] = true;
                }

                int chosen = RandomUtils.nextInt(forbidden.length);
                while (forbidden[chosen]) {
                    chosen = (chosen + 1) % forbidden.length;
                }
                grid[row][col] = chosen;
            }
        }
    }

    public void a(int[][] grid, int row, int col) {
        grid[row][col] = RandomUtils.nextInt(BattleConstants.h.length);
        // 11% chance to upgrade a spawned sword (0) into a fiery sword (6).
        if (grid[row][col] == 0 && RandomUtils.nextInt(100) < 11) {
            grid[row][col] = 6;
        }
    }
}
