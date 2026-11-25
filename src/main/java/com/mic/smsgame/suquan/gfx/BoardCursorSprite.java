package com.mic.smsgame.suquan.gfx;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdUiRenderer;
import com.loan12suquan.hd.platform.HdCanvas2D;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.BattleResourceManager;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.sprite.Drawable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 * Match-3 board selection cursor supporting idle highlight, selected swap arrows,
 * smooth movement lerp, and invalid-move flash.
 */
public class BoardCursorSprite extends Drawable {
    public static final int MODE_HIDDEN = 0;
    public static final int MODE_SELECTED = 1;
    public static final int MODE_IDLE = 2;
    public static final int MODE_MOVING = 3;
    public static final int MODE_LOCKED_FLASH = 4;

    private int mode;
    private Image[] lockFlashImages;
    private Image[] cursorBoxImages;
    private Image[] swapArrowImages;
    private int pulseTick;
    private int arrowDirectionMask;
    private int ownerIndex;
    private int destX;
    private int destY;
    private int flashTick;

    public void a(int mode) {
        this.mode = mode;
    }

    public BoardCursorSprite() {
        this.loadSprites();
    }

    private void loadSprites() {
        BattleResourceManager res = BattleResourceManager.a();
        this.cursorBoxImages = res.g;
        this.swapArrowImages = res.j;
        this.lockFlashImages = new Image[]{res.q};
    }

    public void a(Graphics g, int offsetX, int offsetY) {
        int center = Graphics.HCENTER | Graphics.VCENTER;
        int rx = this.getRenderX();
        int ry = this.getRenderY();
        if (HdConfig.isHdRemaster() && g.getHdCanvas() != null) {
            if (this.mode == MODE_HIDDEN) {
                return;
            }
            HdCanvas2D g2 = g.getHdCanvas();
            int s = HdConfig.HD_SCALE;
            int hx = (rx - 10) * s;
            int hy = (ry - 10) * s;
            int hw = 20 * s;
            int hh = 20 * s;

            boolean playerOwned = (this.ownerIndex == 0);
            if (this.mode == MODE_LOCKED_FLASH) {
                boolean f = (this.flashTick & 1) == 0;
                g2.setColorRgba(255, 64, 64, f ? 115 : 55);
                g2.fillRoundRect(hx, hy, hw, hh, 12);
                g2.setColor(f ? 0xFF4D4D : 0xFFE680);
                g2.setStrokeWidth(3.4f);
                g2.drawRoundRect(hx, hy, hw, hh, 12);
            } else {
                boolean selected = (this.mode == MODE_SELECTED);
                int mainCol = playerOwned
                        ? (selected ? 0xFFF2A6 : 0x66FFF0)
                        : (selected ? 0xFFD6E8 : 0xFF669E);
                if (playerOwned) {
                    g2.setColorRgba(80, 245, 220, selected ? 95 : 48);
                } else {
                    g2.setColorRgba(245, 80, 140, selected ? 95 : 48);
                }
                g2.fillRoundRect(hx - 2, hy - 2, hw + 4, hh + 4, 14);
                g2.setColor(mainCol);
                g2.setStrokeWidth(selected ? 3.0f : 2.2f);
                g2.drawRoundRect(hx + 1, hy + 1, hw - 2, hh - 2, 11);

                // Crisp corner brackets
                g2.setColor(0xFFE680);
                g2.setStrokeRound(3.2f);
                int b = 13;
                g2.drawLine(hx - 1, hy + b, hx - 1, hy - 1);
                g2.drawLine(hx - 1, hy - 1, hx + b, hy - 1);
                g2.drawLine(hx + hw + 1 - b, hy - 1, hx + hw + 1, hy - 1);
                g2.drawLine(hx + hw + 1, hy - 1, hx + hw + 1, hy + b);
                g2.drawLine(hx - 1, hy + hh + 1 - b, hx - 1, hy + hh + 1);
                g2.drawLine(hx - 1, hy + hh + 1, hx + b, hy + hh + 1);
                g2.drawLine(hx + hw + 1 - b, hy + hh + 1, hx + hw + 1, hy + hh + 1);
                g2.drawLine(hx + hw + 1, hy + hh + 1 - b, hx + hw + 1, hy + hh + 1);

                if (selected) {
                    int wave = (GameConfig.visualFrameCounter >> 2) & 7;
                    int arrowOffset = 15 + ((wave <= 4 ? wave : (8 - wave)) >> 1);
                    int arrowCol = playerOwned ? 0xFFF5B8 : 0xFFB8D9;
                    if ((this.arrowDirectionMask & 1) == 1) {
                        HdUiRenderer.drawHdDirectionalChevron(g2, rx - arrowOffset, ry, 2, arrowCol, false, false);
                    }
                    if ((this.arrowDirectionMask & 2) == 2) {
                        HdUiRenderer.drawHdDirectionalChevron(g2, rx + arrowOffset, ry, 3, arrowCol, false, false);
                    }
                    if ((this.arrowDirectionMask & 4) == 4) {
                        HdUiRenderer.drawHdDirectionalChevron(g2, rx, ry - arrowOffset, 0, arrowCol, false, false);
                    }
                    if ((this.arrowDirectionMask & 8) == 8) {
                        HdUiRenderer.drawHdDirectionalChevron(g2, rx, ry + arrowOffset, 1, arrowCol, false, false);
                    }
                }
            }
            return;
        }
        switch (this.mode) {
            case MODE_SELECTED: {
                boolean brightGlow = ((GameConfig.visualFrameCounter >> 3) & 1) == 0;
                g.setColor(this.ownerIndex == 0 ? (brightGlow ? 0x8CF5E8 : 0x33D6C4) : (brightGlow ? 0xFFB0CC : 0xE0467C));
                g.drawRect(rx - 10, ry - 10, 19, 19);
                g.drawImage(this.cursorBoxImages[this.ownerIndex], rx, ry, center);
                int wave = (GameConfig.visualFrameCounter >> 2) & 7;
                int arrowOffset = 13 + ((wave <= 4 ? wave : (8 - wave)) >> 1);
                Image arrow = this.swapArrowImages[this.ownerIndex];
                int aw = arrow.getWidth();
                int ah = arrow.getHeight();
                if ((this.arrowDirectionMask & 1) == 1) {
                    g.drawRegion(arrow, 0, 0, aw, ah, Sprite.TRANS_ROT270, rx - arrowOffset, ry, Graphics.RIGHT | Graphics.VCENTER);
                }
                if ((this.arrowDirectionMask & 2) == 2) {
                    g.drawRegion(arrow, 0, 0, aw, ah, Sprite.TRANS_ROT90, rx + arrowOffset, ry, Graphics.LEFT | Graphics.VCENTER);
                }
                if ((this.arrowDirectionMask & 4) == 4) {
                    g.drawRegion(arrow, 0, 0, aw, ah, Sprite.TRANS_NONE, rx, ry - arrowOffset, Graphics.HCENTER | Graphics.BOTTOM);
                }
                if ((this.arrowDirectionMask & 8) == 8) {
                    g.drawRegion(arrow, 0, 0, aw, ah, Sprite.TRANS_ROT180, rx, ry + arrowOffset, Graphics.HCENTER | Graphics.TOP);
                }
                break;
            }
            case MODE_IDLE:
            case MODE_MOVING:
                g.drawImage(this.cursorBoxImages[this.ownerIndex], rx, ry, center);
                break;
            case MODE_LOCKED_FLASH: {
                Image img = this.lockFlashImages[this.ownerIndex];
                int frameW = img.getWidth() >> 1;
                int frame = this.flashTick >= 2 ? 1 : 0;
                DrawUtils.drawRegion(g, img, frame * frameW, 0, frameW, img.getHeight(), rx, ry, center);
                break;
            }
        }
    }

    public void a() {
        switch (this.mode) {
            case MODE_MOVING: {
                int stepY = Math.abs(this.destY - this.y);
                stepY = stepY < 2 ? 2 : stepY / 2;
                boolean arrived = this.f(this.destY, stepY);

                int stepX = Math.abs(this.destX - this.x);
                stepX = stepX < 2 ? 2 : stepX / 2;
                arrived &= this.e(this.destX, stepX);
                if (arrived) {
                    this.a(MODE_IDLE);
                }
                // Fall through to pulse counter update (matches original behavior).
            }
            case MODE_SELECTED:
                ++this.pulseTick;
                if (this.pulseTick > 6) {
                    this.pulseTick = 0;
                }
                break;
            case MODE_LOCKED_FLASH:
                ++this.flashTick;
                if (this.flashTick >= 4) {
                    this.a(MODE_IDLE);
                }
                break;
            default:
                break;
        }
    }

    public void b() {
        this.mode = MODE_HIDDEN;
    }

    public void b(int ownerIndex) {
        this.ownerIndex = ownerIndex;
    }

    public void b(int fromX, int fromY, int toX, int toY) {
        this.mode = MODE_MOVING;
        this.destX = toX;
        this.destY = toY;
        this.d(fromX, fromY);
    }

    public void a(int x, int y) {
        this.mode = MODE_IDLE;
        this.d(x, y);
    }

    public void a(int directionMask, int x, int y) {
        this.mode = MODE_SELECTED;
        this.arrowDirectionMask = directionMask;
        this.d(x, y);
    }

    public void b(int x, int y) {
        this.mode = MODE_LOCKED_FLASH;
        this.d(x, y);
        this.flashTick = 0;
        this.pulseTick = 0;
    }
}

