package com.mic.smsgame.suquan.battle;

import com.mg.engine.storage.EngineStorage;
import com.mg.engine.util.ByteUtils;
import com.mg.engine.util.ObjectList;
import com.mic.smsgame.suquan.data.CampaignData;
import com.mic.smsgame.suquan.model.BattleFighter;
import com.mic.smsgame.suquan.model.HeroStats;
import com.mic.smsgame.suquan.model.ItemSlot;
import com.mic.smsgame.suquan.model.SwapMove;

/**
 * Core match-3 battle model holding the 12x12 padded 8x8 gem grid, both {@link BattleFighter}s,
 * battle inventory, cursor coordinates, swap validation, move enumeration, and snapshot serialization.
 */
public class BattleModel implements BattleConstants {
    private static final int GRID_SIZE = 12;
    private static final int BOARD_MIN = 2;
    private static final int BOARD_MAX = 10;

    private int[][] grid;
    private BattleFighter[] fighters;
    private int matchMode;
    private int activeTurnIndex;
    private int cursorRow = BOARD_MIN;
    private int cursorCol = BOARD_MIN;
    /** Player's available consumable items during this battle. */
    public ItemSlot[] a;
    /** Base damage/food scale factor derived from fighter attack. */
    public int b;
    /** Food consumed per turn. */
    public int c;
    private BoardGenerator boardGenerator;
    /** True when this battle is the scripted tutorial match. */
    public boolean d = false;
    /** True when fighting a Named Boss (activates periodic Boss Seals on the classic 6-gem board). */
    private boolean bossBoard = false;
    /** Active Boss Seal countdown turns per board cell (0 = none, 1..3 = turns until detonation). */
    private byte[][] bossSealTurns = new byte[GRID_SIZE][GRID_SIZE];
    private static final int[] MAX_ITEMS_PER_BATTLE = new int[]{2, 1, 0, 2, 3, 0};

    private boolean tileSelected;

    public BattleModel(int matchMode, HeroStats playerStats, HeroStats enemyStats) {
        this.matchMode = matchMode;
        this.d = matchMode == 1;
        this.grid = new int[GRID_SIZE][GRID_SIZE];
        this.i();
        this.fighters = new BattleFighter[]{
                this.createFighter(playerStats),
                this.createFighter(enemyStats)
        };
        if (this.d) {
            this.fighters[0].disablePassivesForTutorial();
            this.fighters[1].disablePassivesForTutorial();
        } else {
            this.fighters[0].setRelicMask(EngineStorage.getUnlockedRelicMask());
            this.fighters[0].setRelicBattleBonuses(
                    EngineStorage.getRelicStartManaBonus(),
                    EngineStorage.getRelicHeartPercent());
        }

        int lowerAttack = Math.min(this.fighters[0].m(), this.fighters[1].m());
        this.b = lowerAttack / 2;
        this.c = 220;
        boolean isBossMatch = !this.d && (enemyStats != null) && CampaignData.isBossTemplate(enemyStats.a);
        this.bossBoard = isBossMatch;
        int maxFood = isBossMatch ? 1400 : 1200;
        for (int i = 0; i < this.fighters.length; ++i) {
            int fighterFood = maxFood;
            if (i == 0 && !this.d) {
                fighterFood += EngineStorage.getRelicFoodBonus();
            }
            this.fighters[i].h(fighterFood);
        }

        this.b();
        if (this.d) {
            this.fighters[0].e(9);
        }
    }

    /** Serializes the 8x8 board, cursor position, and both fighters into {@code buffer}. */
    public int a(byte[] buffer, int offset) {
        int start = offset;
        for (int row = BOARD_MIN; row < BOARD_MAX; ++row) {
            for (int col = BOARD_MIN; col < BOARD_MAX; ++col) {
                buffer[offset++] = (byte) this.grid[row][col];
            }
        }
        System.arraycopy(ByteUtils.a(this.cursorRow), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.cursorCol), 0, buffer, offset, 4);
        offset += 4;
        for (int i = 0; i < this.fighters.length; ++i) {
            offset += this.fighters[i].a(buffer, offset);
        }
        return offset - start;
    }

    /** Deserializes the 8x8 board, cursor position, and both fighters from {@code buffer}. */
    public int b(byte[] buffer, int offset) {
        int start = offset;
        for (int row = BOARD_MIN; row < BOARD_MAX; ++row) {
            for (int col = BOARD_MIN; col < BOARD_MAX; ++col) {
                this.grid[row][col] = buffer[offset++];
            }
        }
        this.cursorRow = ByteUtils.a(buffer, offset);
        offset += 4;
        this.cursorCol = ByteUtils.a(buffer, offset);
        offset += 4;
        for (int i = 0; i < this.fighters.length; ++i) {
            offset += this.fighters[i].b(buffer, offset);
        }
        return offset - start;
    }

    public int a() {
        return this.activeTurnIndex;
    }

    public void a(int turnIndex) {
        this.activeTurnIndex = turnIndex;
    }

    /** Reloads inventory quantities from RMS (capped to tactical per-battle limits) and resets cursor and fighter states. */
    public void b() {
        int[] savedCounts = EngineStorage.v();
        if (savedCounts == null) {
            savedCounts = new int[6];
        }
        savedCounts[5] = 0;
        this.a = new ItemSlot[savedCounts.length];
        for (int i = 0; i < this.a.length; ++i) {
            this.a[i] = new ItemSlot();
            this.a[i].a = i;
            int count = savedCounts[i];
            if (!this.d && i < MAX_ITEMS_PER_BATTLE.length && count > MAX_ITEMS_PER_BATTLE[i]) {
                count = MAX_ITEMS_PER_BATTLE[i];
            }
            this.a[i].b = count;
        }
        this.d(5);
        this.c(5);
        for (int i = 0; i < this.fighters.length; ++i) {
            this.fighters[i].a();
        }
        this.clearAllBossSeals();
    }

    public int getBossSealTurns(int row, int col) {
        if (row < BOARD_MIN || row >= BOARD_MAX || col < BOARD_MIN || col >= BOARD_MAX) {
            return 0;
        }
        return this.bossSealTurns[row][col];
    }

    public void setBossSealTurns(int row, int col, int turns) {
        if (row < BOARD_MIN || row >= BOARD_MAX || col < BOARD_MIN || col >= BOARD_MAX) {
            return;
        }
        this.bossSealTurns[row][col] = (byte) (turns <= 0 ? 0 : turns);
        this.refreshBossImminentFlag();
    }

    public void moveBossSeal(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow == toRow && fromCol == toCol) {
            return;
        }
        byte val = this.bossSealTurns[fromRow][fromCol];
        this.bossSealTurns[fromRow][fromCol] = 0;
        this.bossSealTurns[toRow][toCol] = val;
        this.refreshBossImminentFlag();
    }

    public void clearAllBossSeals() {
        for (int r = 0; r < GRID_SIZE; ++r) {
            for (int c = 0; c < GRID_SIZE; ++c) {
                this.bossSealTurns[r][c] = 0;
            }
        }
        this.refreshBossImminentFlag();
    }

    public int countActiveBossSeals() {
        int count = 0;
        for (int r = BOARD_MIN; r < BOARD_MAX; ++r) {
            for (int c = BOARD_MIN; c < BOARD_MAX; ++c) {
                if (this.bossSealTurns[r][c] > 0) {
                    ++count;
                }
            }
        }
        return count;
    }

    public void refreshBossImminentFlag() {
        if (this.fighters == null || this.fighters.length < 2 || this.fighters[1] == null) {
            return;
        }
        boolean hasOneTurnSeal = false;
        for (int r = BOARD_MIN; r < BOARD_MAX && !hasOneTurnSeal; ++r) {
            for (int c = BOARD_MIN; c < BOARD_MAX; ++c) {
                if (this.bossSealTurns[r][c] == 1) {
                    hasOneTurnSeal = true;
                    break;
                }
            }
        }
        this.fighters[1].setSealExplosionImminent(hasOneTurnSeal);
    }

    private BattleFighter createFighter(HeroStats stats) {
        return new BattleFighter(stats);
    }

    public int c() {
        return this.matchMode;
    }

    public boolean isBossBoard() {
        return this.bossBoard;
    }

    public BattleFighter[] d() {
        return this.fighters;
    }

    public BattleFighter b(int index) {
        return this.fighters[index];
    }

    public int a(int row, int col) {
        return this.grid[row][col];
    }

    public int[][] e() {
        return this.grid;
    }

    public int f() {
        return this.cursorRow;
    }

    public void c(int row) {
        this.cursorRow = row;
    }

    public int g() {
        return this.cursorCol;
    }

    public void d(int col) {
        this.cursorCol = col;
    }

    public boolean h() {
        return this.tileSelected;
    }

    public void a(boolean selected) {
        this.tileSelected = selected;
    }

    /** Moves cursor with wrap-around across rows/cols 2..9; returns true if cell is playable. */
    public boolean b(int row, int col) {
        if (row < BOARD_MIN) {
            row = BOARD_MAX - 1;
        } else if (row >= BOARD_MAX) {
            row = BOARD_MIN;
        }
        if (col < BOARD_MIN) {
            col = BOARD_MAX - 1;
        } else if (col >= BOARD_MAX) {
            col = BOARD_MIN;
        }
        if (this.isPlayableCell(row, col)) {
            this.cursorRow = row;
            this.cursorCol = col;
            return true;
        }
        return false;
    }

    public boolean c(int row, int col) {
        if (!this.tileSelected) {
            return true;
        }
        return row >= BOARD_MIN && row < BOARD_MAX && col >= BOARD_MIN && col < BOARD_MAX;
    }

    private boolean isPlayableCell(int row, int col) {
        return this.grid[row][col] >= 0;
    }

    /** Initializes sentinel border rings (values 8..11) and selects the board generator. */
    public void i() {
        this.boardGenerator = this.d ? (BoardGenerator) new TutorialBoardGenerator() : new RandomBoardGenerator();

        for (int col = 11; col >= 0; --col) {
            this.grid[0][col] = 8;
            this.grid[11][col] = 8;
        }
        for (int row = 11; row >= 0; --row) {
            this.grid[row][0] = 9;
            this.grid[row][11] = 9;
        }
        for (int col = 10; col >= 1; --col) {
            this.grid[1][col] = 10;
            this.grid[10][col] = 10;
        }
        for (int row = 10; row >= 1; --row) {
            this.grid[row][1] = 11;
            this.grid[row][10] = 11;
        }
    }

    public void j() {
        this.clearAllBossSeals();
        this.boardGenerator.a(this.grid);
    }

    public void d(int row, int col) {
        if (this.bossSealTurns[row][col] != 0) {
            this.bossSealTurns[row][col] = 0;
            this.refreshBossImminentFlag();
        }
        this.boardGenerator.a(this.grid, row, col);
    }

    /** Scans horizontally through {@code (row, col)} and packs {@code (left << 16) | (right << 8) | total | 0xFF000000}. */
    public int e(int row, int col) {
        if (this.grid[row][col] >= 7) {
            return 0;
        }
        int packed = 0xFF000000;
        int total = 1;
        int step = 1;
        byte group = BattleConstants.i[this.grid[row][col]];
        while (group == BattleConstants.i[this.grid[row][col - step]]) {
            ++total;
            ++step;
        }
        packed |= ((step - 1) & 255) << 16;

        step = 1;
        while (group == BattleConstants.i[this.grid[row][col + step]]) {
            ++total;
            ++step;
        }
        packed |= ((step - 1) & 255) << 8;
        packed |= total & 255;
        return packed;
    }

    /** Scans vertically through {@code (row, col)} and packs {@code (up << 16) | (down << 8) | total}. */
    public int f(int row, int col) {
        if (this.grid[row][col] >= 7) {
            return 0;
        }
        int packed = 0;
        int total = 1;
        int step = 1;
        byte group = BattleConstants.i[this.grid[row][col]];
        while (group == BattleConstants.i[this.grid[row - step][col]]) {
            ++total;
            ++step;
        }
        packed |= ((step - 1) & 255) << 16;

        step = 1;
        while (group == BattleConstants.i[this.grid[row + step][col]]) {
            ++total;
            ++step;
        }
        packed |= ((step - 1) & 255) << 8;
        packed |= total & 255;
        return packed;
    }

    public SwapMove a(int r1, int c1, int r2, int c2) {
        return this.testSwap(r1, c1, r2, c2);
    }

    public void b(int r1, int c1, int r2, int c2) {
        this.swapCells(this.grid, r1, c1, r2, c2);
        byte tmpSeal = this.bossSealTurns[r1][c1];
        this.bossSealTurns[r1][c1] = this.bossSealTurns[r2][c2];
        this.bossSealTurns[r2][c2] = tmpSeal;
    }

    private void swapCells(int[][] matrix, int r1, int c1, int r2, int c2) {
        int tmp = matrix[r1][c1];
        matrix[r1][c1] = matrix[r2][c2];
        matrix[r2][c2] = tmp;
    }

    private SwapMove testSwap(int r1, int c1, int r2, int c2) {
        try {
            if (this.grid[r1][c1] == this.grid[r2][c2]) {
                return null;
            }
            if (this.grid[r1][c1] >= 7 || this.grid[r2][c2] >= 7) {
                return null;
            }

            this.swapCells(this.grid, r1, c1, r2, c2);
            int h1 = this.e(r1, c1);
            int v1 = this.f(r1, c1);
            int h2 = this.e(r2, c2);
            int v2 = this.f(r2, c2);
            boolean matched = (h1 & 255) >= 3 || (v1 & 255) >= 3 || (h2 & 255) >= 3 || (v2 & 255) >= 3;
            this.swapCells(this.grid, r1, c1, r2, c2);

            if (matched) {
                SwapMove move = new SwapMove();
                move.a = r1;
                move.b = c1;
                move.c = r2;
                move.d = c2;
                move.e = new int[]{h1, v1, h2, v2};
                return move;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Returns an {@link ObjectList} of all valid {@link SwapMove}s currently available on the board. */
    public ObjectList k() {
        ObjectList moves = new ObjectList();
        for (int row = BOARD_MIN; row < BOARD_MAX; ++row) {
            for (int col = BOARD_MIN; col < BOARD_MAX; ++col) {
                SwapMove down = this.testSwap(row, col, row + 1, col);
                if (down != null) {
                    moves.a(down);
                }
                SwapMove right = this.testSwap(row, col, row, col + 1);
                if (right != null) {
                    moves.a(right);
                }
            }
        }
        return moves;
    }
}
