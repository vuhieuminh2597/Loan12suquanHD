package com.mic.smsgame.suquan.battle;

import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.Rect;

/**
 * Human player actor translating keypad presses, direct 1-tap selection,
 * swipe-to-swap board gestures, and HUD touch shortcuts into {@link BattleController} actions.
 */
public class HumanPlayerActor extends BattleActor {
    private static final int CELL_SIZE = 21;
    private static final int SENTINEL_OFFSET = 2;
    private static final int SWIPE_THRESHOLD_PX = 10;
    private static final int SOFTKEY_BAR_HEIGHT = 24;

    private int dragStartX = -1;
    private int dragStartY = -1;
    private int dragOriginRow = -1;
    private int dragOriginCol = -1;
    private boolean dragSwapped = false;

    public HumanPlayerActor(BattleController controller, int actorIndex) {
        super(controller, actorIndex);
    }

    public void a(int keyCode) {
        switch (keyCode) {
            case 11:
                this.controller.s();
                break;
            case 12:
                this.controller.m();
                break;
            case 13:
                this.controller.n();
                break;
            case 14:
                this.controller.o();
                break;
            case 15:
                this.controller.p();
                break;
            case 16:
                this.controller.i();
                break;
            case 17:
                this.controller.r();
                break;
            case 18:
                this.controller.g();
                break;
        }
    }

    public void a(int pointerX, int pointerY) {
        this.resetDragState();

        // Bottom softkey touch strip: Left = Skills ("T.Chiêu"), Center = Bag ("Túi"), Right = Pause ("Dừng").
        if (pointerY >= GameConfig.screenHeight - SOFTKEY_BAR_HEIGHT) {
            int thirdWidth = GameConfig.screenWidth / 3;
            int twoThirdsWidth = thirdWidth + thirdWidth;
            if (pointerX > twoThirdsWidth) {
                this.controller.g();
            } else if (pointerX > thirdWidth) {
                this.controller.s();
            } else {
                this.controller.r();
            }
            return;
        }

        // Direct HUD bar shortcut (Player HP/Food/Mana bars -> Skills; avatars & Enemy HUD open Stats in BattleScreen).
        int hudTop = GameConfig.contentOffsetY + 232;
        int hudBottom = GameConfig.contentOffsetY + 271;
        if (pointerY >= hudTop && pointerY <= hudBottom) {
            int leftBarsStart = GameConfig.contentOffsetX + 68;
            int leftBarsEnd = GameConfig.contentOffsetX + 118;
            if (pointerX >= leftBarsStart && pointerX <= leftBarsEnd) {
                this.controller.r();
                return;
            }
        }

        // Direct 8x8 gem board touch: 1-tap select, tap-adjacent swap, or hold-and-swipe swap.
        Rect boardBounds = this.controller.h();
        if (boardBounds.b(pointerX, pointerY)) {
            int col = (pointerX - boardBounds.a) / CELL_SIZE + SENTINEL_OFFSET;
            int row = (pointerY - boardBounds.b) / CELL_SIZE + SENTINEL_OFFSET;
            if (row < 2 || row > 9 || col < 2 || col > 9) {
                return;
            }

            this.dragStartX = pointerX;
            this.dragStartY = pointerY;
            this.dragOriginRow = row;
            this.dragOriginCol = col;
            this.dragSwapped = false;

            int cursorRow = this.controller.k();
            int cursorCol = this.controller.l();

            if (this.controller.j()) {
                int dRow = row - cursorRow;
                int dCol = col - cursorCol;
                if (dRow == 0 && dCol == 0) {
                    // Tapping the already-selected gem deselects it.
                    this.controller.i();
                    this.dragSwapped = true;
                } else if (dRow == 1 && dCol == 0) {
                    this.controller.n();
                    this.dragSwapped = true;
                } else if (dRow == -1 && dCol == 0) {
                    this.controller.m();
                    this.dragSwapped = true;
                } else if (dCol == 1 && dRow == 0) {
                    this.controller.p();
                    this.dragSwapped = true;
                } else if (dCol == -1 && dRow == 0) {
                    this.controller.o();
                    this.dragSwapped = true;
                } else {
                    // Non-adjacent gem: switch selection directly to the tapped gem.
                    this.controller.selectCellAt(row, col);
                }
            } else {
                // No gem selected yet: move cursor and select immediately in 1 tap.
                this.controller.selectCellAt(row, col);
            }
        }
    }

    public void onPointerDragged(int pointerX, int pointerY) {
        if (this.dragSwapped || this.dragOriginRow < SENTINEL_OFFSET || this.dragOriginCol < SENTINEL_OFFSET) {
            return;
        }
        int dx = pointerX - this.dragStartX;
        int dy = pointerY - this.dragStartY;
        int absDx = dx < 0 ? -dx : dx;
        int absDy = dy < 0 ? -dy : dy;
        if (absDx < SWIPE_THRESHOLD_PX && absDy < SWIPE_THRESHOLD_PX) {
            return;
        }

        // Ensure drag origin cell is selected before performing the directional swap.
        if (!this.controller.j()
                || this.controller.k() != this.dragOriginRow
                || this.controller.l() != this.dragOriginCol) {
            if (!this.controller.selectCellAt(this.dragOriginRow, this.dragOriginCol)) {
                this.dragSwapped = true;
                return;
            }
        }

        if (absDx >= absDy) {
            if (dx > 0) {
                this.controller.p();
            } else {
                this.controller.o();
            }
        } else {
            if (dy > 0) {
                this.controller.n();
            } else {
                this.controller.m();
            }
        }
        this.dragSwapped = true;
    }

    public void onPointerReleased(int pointerX, int pointerY) {
        this.resetDragState();
    }

    private void resetDragState() {
        this.dragStartX = -1;
        this.dragStartY = -1;
        this.dragOriginRow = -1;
        this.dragOriginCol = -1;
        this.dragSwapped = false;
    }

    public void a() {
        this.resetDragState();
    }

    public void b() {
        // Food is now deducted symmetrically per action in BattleController instead of per-frame wall-clock drain.
    }
}
