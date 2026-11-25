package com.mic.smsgame.suquan.gfx;

import com.mg.engine.graphics.BattleResourceManager;
import com.mg.engine.sprite.Drawable;
import com.mic.smsgame.suquan.battle.BattleConstants;
import com.mic.smsgame.suquan.battle.BattleModel;
import com.mic.smsgame.suquan.model.GemMatch;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Visual view of the 8x8 match-3 gem grid owning {@link GemTileSprite} instances
 * for static placement, swaps, falling drops, and match explosions.
 */
public class GemBoardRenderer extends Drawable implements BattleConstants {
    private static final int BOARD_DIM = 8;
    private static final int CELL_SIZE = 21;
    private static final int SENTINEL_PADDING = 2;

    private Image[] gemImages;
    private Image[] normalExplodeSheets;
    private Image[] superExplodeSheets;
    private GemTileSprite[][] tiles;
    private byte[][] superExplodeFrames;
    private byte[] normalExplodeFrames;
    private int[] superExplodeFrameCounts;

    private boolean bossBoardMode;

    public GemBoardRenderer(int boardX, int boardY) {
        this(boardX, boardY, false);
    }

    public GemBoardRenderer(int boardX, int boardY, boolean bossBoardMode) {
        this.bossBoardMode = bossBoardMode;
        this.loadSprites();
        this.tiles = new GemTileSprite[BOARD_DIM][BOARD_DIM];
        this.superExplodeFrames = new byte[][]{{0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3}, new byte[1]};
        this.superExplodeFrameCounts = new int[]{4, 4, 4, 4, 4, 4, 4};
        this.normalExplodeFrames = new byte[]{0, 0, 1, 1, 2, 2, 3, 3};
        byte[][] baseFrames = new byte[][]{
                {-1},
                new byte[1],
                {0, 0, 1, 1, 2, 2, 3, 3},
                new byte[1],
                new byte[1],
                new byte[1],
                new byte[1]
        };

        this.x = boardX;
        this.y = boardY;
        this.width = BOARD_DIM * CELL_SIZE;
        this.height = BOARD_DIM * CELL_SIZE;

        int tileY = boardY + 10;
        for (int row = 0; row < BOARD_DIM; ++row) {
            int tileX = boardX + 10;
            for (int col = 0; col < BOARD_DIM; ++col) {
                this.tiles[row][col] = new GemTileSprite(tileX, tileY);
                this.tiles[row][col].j(Graphics.HCENTER | Graphics.VCENTER);
                this.tiles[row][col].a(baseFrames);
                tileX += CELL_SIZE;
            }
            tileY += CELL_SIZE;
        }

        // Configure concentric ripple shiver timing radiating from board center.
        int half = BOARD_DIM / 2;
        int rowOffset = 0;
        int startTick = 1000;
        int cycleLength = startTick + 32 + 3;
        for (int r = half - 1; r > -1; --r) {
            int colOffset = 0;
            int delay = startTick;
            for (int c = half - 1; c > -1; --c) {
                this.tiles[r][c].a(delay, cycleLength);
                this.tiles[half + rowOffset][c].a(delay, cycleLength);
                this.tiles[r][half + colOffset].a(delay, cycleLength);
                this.tiles[half + rowOffset][half + colOffset].a(delay, cycleLength);
                delay += 2;
                ++colOffset;
            }
            ++rowOffset;
            startTick += 2;
        }
    }

    private boolean isSpecialGem(int gemType) {
        return gemType == 6;
    }

    public void syncBossSeals(BattleModel model) {
        if (model == null) {
            return;
        }
        for (int r = 0; r < BOARD_DIM; ++r) {
            for (int c = 0; c < BOARD_DIM; ++c) {
                this.tiles[r][c].setBossSealTurns(model.getBossSealTurns(r + SENTINEL_PADDING, c + SENTINEL_PADDING));
            }
        }
    }

    public void a(int boardRow, int boardCol, int gemType) {
        int r = boardRow - SENTINEL_PADDING;
        int c = boardCol - SENTINEL_PADDING;
        this.tiles[r][c].setSpecialShimmer(this.isSpecialGem(gemType));
        this.tiles[r][c].a(this.x + c * CELL_SIZE, this.y + r * CELL_SIZE, this.gemImages[gemType]);
    }

    public void a(int row1, int col1, int row2, int col2, int gemType1, int gemType2) {
        int r1 = row1 - SENTINEL_PADDING;
        int c1 = col1 - SENTINEL_PADDING;
        int r2 = row2 - SENTINEL_PADDING;
        int c2 = col2 - SENTINEL_PADDING;
        int x1 = this.x + c1 * CELL_SIZE;
        int y1 = this.y + r1 * CELL_SIZE;
        int x2 = this.x + c2 * CELL_SIZE;
        int y2 = this.y + r2 * CELL_SIZE;
        this.tiles[r1][c1].setSpecialShimmer(this.isSpecialGem(gemType2));
        this.tiles[r2][c2].setSpecialShimmer(this.isSpecialGem(gemType1));
        this.tiles[r1][c1].a(x2, y2, x1, y1, this.gemImages[gemType2]);
        this.tiles[r2][c2].a(x1, y1, x2, y2, this.gemImages[gemType1]);
    }

    public void b(int fromRow, int fromCol, int toRow, int toCol, int gemType, int delayTicks) {
        int r1 = fromRow - SENTINEL_PADDING;
        int c1 = fromCol - SENTINEL_PADDING;
        int r2 = toRow - SENTINEL_PADDING;
        int c2 = toCol - SENTINEL_PADDING;
        int x1 = this.x + c1 * CELL_SIZE;
        int y1 = this.y + r1 * CELL_SIZE;
        int x2 = this.x + c2 * CELL_SIZE;
        int y2 = this.y + r2 * CELL_SIZE;
        this.tiles[r2][c2].setSpecialShimmer(this.isSpecialGem(gemType));
        this.tiles[r2][c2].a(x1, y1, x2, y2, this.gemImages[gemType], delayTicks);
    }

    public int a(GemMatch match, int unused1, int unused2) {
        int startRow = match.a - SENTINEL_PADDING;
        int startCol = match.b - SENTINEL_PADDING;
        int tileY = this.y + startRow * CELL_SIZE;
        int span = match.d > match.c ? match.d : match.c;

        byte[] frames;
        int frameCount;
        Image explodeSheet;
        if (span <= 3 && match.f < 7) {
            frames = this.normalExplodeFrames;
            explodeSheet = this.normalExplodeSheets[match.e];
            frameCount = 4;
        } else {
            frames = this.superExplodeFrames[0];
            frameCount = this.superExplodeFrameCounts[match.e];
            explodeSheet = this.superExplodeSheets[match.e];
        }

        int endRow = startRow + match.c;
        int endCol = startCol + match.d;
        for (int r = startRow; r < endRow; ++r) {
            int tileX = this.x + startCol * CELL_SIZE;
            for (int c = startCol; c < endCol; ++c) {
                this.tiles[r][c].d()[GemTileSprite.STATE_EXPLODE] = frames;
                this.tiles[r][c].a(tileX, tileY, explodeSheet, frameCount);
                tileX += CELL_SIZE;
            }
            tileY += CELL_SIZE;
        }
        return frames.length;
    }

    public GemTileSprite a(int row, int col) {
        return this.tiles[row][col];
    }

    private void loadSprites() {
        BattleResourceManager res = BattleResourceManager.a();
        this.gemImages = res.d;
        this.normalExplodeSheets = res.e;
        this.superExplodeSheets = res.f;
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        for (int r = 0; r < BOARD_DIM; ++r) {
            for (int c = 0; c < BOARD_DIM; ++c) {
                this.tiles[r][c].a(g);
            }
        }
    }

    public void a() {
        for (int r = 0; r < BOARD_DIM; ++r) {
            for (int c = 0; c < BOARD_DIM; ++c) {
                this.tiles[r][c].e();
            }
        }
    }
}

