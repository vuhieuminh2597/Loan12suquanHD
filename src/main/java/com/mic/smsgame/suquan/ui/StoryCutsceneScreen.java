package com.mic.smsgame.suquan.ui;

import com.mg.engine.audio.BgmPlayer;
import com.mg.engine.core.ActionListener;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.engine.graphics.Rect;
import com.mg.engine.sprite.AnimatedSprite;
import com.mg.engine.sprite.ScrollingCreditsText;
import com.mg.engine.storage.CampaignStorage;
import com.mg.engine.storage.EngineStorage;
import com.mg.engine.ui.BaseDialog;
import com.mg.engine.ui.BaseScreen;
import com.mg.engine.ui.SoftKeyBar;
import com.mg.engine.ui.SoftKeyButton;
import com.mg.engine.ui.TextSoftKeyButton;
import com.mg.engine.util.RandomUtils;
import com.mg.engine.util.StringUtils;
import com.mic.smsgame.suquan.data.CampaignData;
import com.mic.smsgame.suquan.data.GameStrings;
import com.mic.smsgame.suquan.vfx.ComboBurstEffect;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Historical intro and victory epilogue cutscene screen with panning map camera,
 * scrolling historical text, and celebratory fireworks.
 */
public class StoryCutsceneScreen extends BaseScreen implements ActionListener {
    private static final int STATE_VICTORY_ZOOM_IN = 0;
    private static final int STATE_VICTORY_CREDITS = 1;
    private static final int STATE_VICTORY_BANNER = 2;
    private static final int STATE_INTRO_ZOOM_IN = 3;
    private static final int STATE_INTRO_SCROLL = 4;
    private static final int STATE_PROCEED_TO_CHAR_SELECT = 5;

    private static final int ACTION_FINISH_GAME = 101;
    private static final int ACTION_SKIP_INTRO = 102;
    private static final int ACTION_SKIP_VICTORY_SCROLL = 103;
    private static final int ACTION_RETURN_MAIN_MENU = 104;

    private static final int[][] WARLORD_MAP_TARGETS = new int[][]{
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null,
            {60, 338}, null, null, {186, 229}, null, null, {303, 287}, null, null, {103, 76},
            null, null, {148, 116}, null, null, {108, 183}, null, null, {265, 160}, null, null,
            {318, 107}, null, null, {243, 205}, null, null, {178, 170}, null, null, {29, 88},
            null, null, {313, 228}, null, null, null, null, null, {170, 295},
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null
    };

    private int state = -1;
    private Image endBannerPrimary;
    private Image endBannerSecondary;
    private Image mapImage;
    private int mapOffsetX;
    private int mapOffsetY;
    private int mapWidth;
    private int mapHeight;
    private ScrollingCreditsText scrollingText;
    private Rect savedClip;
    private Rect mapViewport;
    private Rect textViewport;
    private Rect expandingViewport;
    private int panDirection;
    private ComboBurstEffect[] fireworks;
    private int animTimer;
    private boolean blinkEndSubBanner;
    private boolean focusingTarget = false;
    private int targetMapX;
    private int targetMapY;
    private AnimatedSprite pointerArrow;

    public StoryCutsceneScreen(boolean isIntro) {
        this.a((ActionListener) this);
        this.a((SoftKeyBar) null);
        super.b_ = 240;
        super.c_ = 320;
        super.g = (GameConfig.screenWidth - super.b_) >> 1;
        super.a_ = (GameConfig.screenHeight - super.c_) >> 1;

        this.mapImage = MgImageLoader.load("/m/m");
        this.mapWidth = this.mapImage.getWidth();
        this.mapHeight = this.mapImage.getHeight();
        this.mapViewport = new Rect(super.g + 20, super.a_ + 30, 200, 130);
        this.expandingViewport = new Rect(this.mapViewport.a + 100, this.mapViewport.b + 65, 0, 0);
        this.textViewport = new Rect(super.g, super.a_ + 165, super.b_, GameConfig.screenHeight - 20 - (super.a_ + 165));
        this.savedClip = new Rect();

        this.pointerArrow = new AnimatedSprite(MgImageLoader.load("/m/ar"), 6);
        this.pointerArrow.c(1);
        this.pointerArrow.b(true);
        this.pointerArrow.a(new byte[][]{{0, 1, 2, 3, 4, 5, 5, 4, 3, 2, 1, 0}});
        this.pointerArrow.a_(0);

        this.setState(isIntro ? STATE_INTRO_ZOOM_IN : STATE_VICTORY_ZOOM_IN);
        BgmPlayer.getInstance().load(isIntro ? "intro" : "victory", -1);
        BgmPlayer.getInstance().setTempo(120000);
        BgmPlayer.getInstance().playAsync();
    }

    private boolean panCameraToward(int targetX, int targetY, Rect viewport) {
        int currentCenterX = (viewport.c >> 1) + viewport.a - this.mapOffsetX - super.g;
        int currentCenterY = (viewport.d >> 1) + viewport.b - this.mapOffsetY - super.a_ + 15;
        int halfW = viewport.c >> 1;
        int halfH = viewport.d >> 1;

        if (targetX > this.mapWidth - halfW) {
            targetX = this.mapWidth - halfW;
        }
        if (targetX < halfW) {
            targetX = halfW;
        }
        if (targetY > this.mapHeight - halfH) {
            targetY = this.mapHeight - halfH;
        }
        if (targetY < halfH) {
            targetY = halfH;
        }

        int stepX;
        if (targetX - currentCenterX == 4) {
            stepX = targetX == currentCenterX ? 0 : (targetX > currentCenterX ? 1 : -1);
        } else {
            stepX = (targetX - currentCenterX) / 4;
        }

        int stepY;
        if (targetY - currentCenterY == 4) {
            stepY = targetY == currentCenterY ? 0 : (targetY > currentCenterY ? 1 : -1);
        } else {
            stepY = (targetY - currentCenterY) / 4;
        }

        if (stepX == 0 && stepY == 0) {
            return false;
        }
        this.mapOffsetX -= stepX;
        this.mapOffsetY -= stepY;
        return true;
    }

    private void setState(int nextState) {
        this.state = nextState;
        switch (this.state) {
            case STATE_VICTORY_ZOOM_IN:
                this.mapOffsetX = -43;
                this.mapOffsetY = -154;
                break;
            case STATE_VICTORY_CREDITS: {
                String[] lines = new String[GameStrings.i.length];
                String playerName = CampaignData.c();
                for (int i = 0; i < lines.length; ++i) {
                    lines[i] = StringUtils.replace(GameStrings.i[i], "{s}", playerName);
                }
                this.scrollingText = new ScrollingCreditsText(lines, this.textViewport);
                this.fireworks = new ComboBurstEffect[]{
                        new ComboBurstEffect(MgImageLoader.load("/crystalviolet")),
                        new ComboBurstEffect(MgImageLoader.load("/crystalblue")),
                        new ComboBurstEffect(MgImageLoader.load("/crystalgreen")),
                        new ComboBurstEffect(MgImageLoader.load("/crystalred")),
                        new ComboBurstEffect(MgImageLoader.load("/crystalyellow"))
                };
                for (int i = 0; i < this.fireworks.length; ++i) {
                    this.fireworks[i].a_(false);
                }
                this.animTimer = 30;
                this.b(new TextSoftKeyButton("Bỏ qua", ACTION_SKIP_VICTORY_SCROLL));
                break;
            }
            case STATE_VICTORY_BANNER:
                this.endBannerPrimary = MgImageLoader.load("/m/e1");
                this.endBannerSecondary = MgImageLoader.load("/m/e2");
                this.animTimer = 0;
                this.b((SoftKeyButton) null);
                this.c(new TextSoftKeyButton("Kết thúc", ACTION_FINISH_GAME));
                break;
            case STATE_INTRO_ZOOM_IN:
                this.mapOffsetX = this.mapViewport.a;
                this.mapOffsetY = this.mapViewport.b;
                break;
            case STATE_INTRO_SCROLL:
                this.scrollingText = new ScrollingCreditsText(GameStrings.h, this.textViewport);
                this.panDirection = 1;
                this.b(new TextSoftKeyButton("Bỏ qua", ACTION_SKIP_INTRO));
                break;
        }
    }

    public void a() {
        switch (this.state) {
            case STATE_VICTORY_ZOOM_IN: {
                boolean expanding = false;
                if (this.expandingViewport.a > this.mapViewport.a) {
                    expanding = true;
                    this.expandingViewport.a -= 2;
                    this.expandingViewport.c += 4;
                }
                if (this.expandingViewport.b > this.mapViewport.b) {
                    expanding = true;
                    this.expandingViewport.b -= 2;
                    this.expandingViewport.d += 4;
                }
                if (!expanding) {
                    this.setState(STATE_VICTORY_CREDITS);
                }
                break;
            }
            case STATE_VICTORY_CREDITS:
                this.scrollingText.b();
                if (this.scrollingText.a()) {
                    this.setState(STATE_VICTORY_BANNER);
                }
                --this.animTimer;
                if (this.animTimer == 0) {
                    this.animTimer = 20;
                    this.fireworks[RandomUtils.nextInt(5)].a(
                            RandomUtils.nextInt(GameConfig.screenWidth),
                            RandomUtils.nextInt(GameConfig.screenHeight),
                            6,
                            "");
                }
                for (int i = 0; i < this.fireworks.length; ++i) {
                    this.fireworks[i].b();
                }
                break;
            case STATE_VICTORY_BANNER:
                ++this.animTimer;
                if (this.animTimer % 3 == 0) {
                    this.blinkEndSubBanner = !this.blinkEndSubBanner;
                }
                break;
            case STATE_INTRO_ZOOM_IN: {
                boolean expanding = false;
                if (this.expandingViewport.a > this.mapViewport.a) {
                    expanding = true;
                    this.expandingViewport.a -= 2;
                    this.expandingViewport.c += 4;
                }
                if (this.expandingViewport.b > this.mapViewport.b) {
                    expanding = true;
                    this.expandingViewport.b -= 2;
                    this.expandingViewport.d += 4;
                }
                if (!expanding) {
                    this.setState(STATE_INTRO_SCROLL);
                }
                break;
            }
            case STATE_INTRO_SCROLL: {
                this.scrollingText.b();
                if (this.scrollingText.a()) {
                    this.setState(STATE_PROCEED_TO_CHAR_SELECT);
                }
                int lineIdx = this.scrollingText.c();
                if (lineIdx < WARLORD_MAP_TARGETS.length && WARLORD_MAP_TARGETS[lineIdx] != null) {
                    this.focusingTarget = true;
                    this.targetMapX = WARLORD_MAP_TARGETS[lineIdx][0];
                    this.targetMapY = WARLORD_MAP_TARGETS[lineIdx][1];
                }
                if (!this.focusingTarget) {
                    switch (this.panDirection) {
                        case 1:
                            --this.mapOffsetX;
                            if (super.g + this.mapOffsetX + this.mapImage.getWidth() <= this.mapViewport.a + this.mapViewport.c) {
                                this.panDirection = 2;
                            }
                            return;
                        case 2:
                            --this.mapOffsetY;
                            if (super.a_ + this.mapOffsetY + this.mapImage.getHeight() <= this.mapViewport.b + this.mapViewport.d) {
                                this.panDirection = 3;
                            }
                            return;
                        case 3:
                            ++this.mapOffsetX;
                            if (super.g + this.mapOffsetX >= this.mapViewport.a) {
                                this.panDirection = 4;
                            }
                            return;
                        case 4:
                            ++this.mapOffsetY;
                            if (super.a_ + this.mapOffsetY >= this.mapViewport.b) {
                                this.panDirection = 1;
                            }
                            return;
                    }
                } else {
                    this.pointerArrow.e();
                    this.panCameraToward(this.targetMapX, this.targetMapY, this.mapViewport);
                }
                break;
            }
            case STATE_PROCEED_TO_CHAR_SELECT:
                GUIManager.getController().a(-1, 8, false);
                break;
        }
    }

    private void drawScrollingText(Graphics g) {
        DrawUtils.saveClip(g, this.savedClip);
        DrawUtils.intersectClip(g, this.textViewport.a, this.textViewport.b, this.textViewport.c, this.textViewport.d);
        this.scrollingText.a(g, super.g, super.a_);
        DrawUtils.restoreClip(g, this.savedClip);
    }

    private void drawMapWindow(Graphics g, Rect clipRect) {
        DrawUtils.saveClip(g, this.savedClip);
        DrawUtils.intersectClip(g, clipRect.a, clipRect.b, clipRect.c, clipRect.d);
        g.drawImage(this.mapImage, super.g + this.mapOffsetX, super.a_ + this.mapOffsetY, 0);
        if (this.focusingTarget) {
            this.pointerArrow.a(g, super.g + this.mapOffsetX + this.targetMapX - 10, super.a_ + this.mapOffsetY + this.targetMapY - 30);
        }
        DrawUtils.restoreClip(g, this.savedClip);
    }

    public void a(Graphics g) {
        g.setColor(0);
        g.fillRect(super.g, super.a_, super.b_, super.c_);
        switch (this.state) {
            case STATE_VICTORY_ZOOM_IN:
            case STATE_INTRO_ZOOM_IN:
                this.drawMapWindow(g, this.expandingViewport);
                break;
            case STATE_VICTORY_CREDITS:
                this.drawMapWindow(g, this.mapViewport);
                this.drawScrollingText(g);
                for (int i = 0; i < this.fireworks.length; ++i) {
                    this.fireworks[i].a(g);
                }
                break;
            case STATE_VICTORY_BANNER: {
                int cx = GameConfig.screenWidth >> 1;
                int cy = GameConfig.screenHeight >> 1;
                int center = Graphics.HCENTER | Graphics.VCENTER;
                if (this.animTimer < 10) {
                    if (this.animTimer % 2 == 0) {
                        g.drawImage(this.endBannerPrimary, cx + 2, cy - 2, center);
                    } else {
                        g.drawImage(this.endBannerPrimary, cx - 2, cy + 2, center);
                    }
                } else {
                    g.drawImage(this.endBannerPrimary, cx, cy, center);
                }
                if (this.blinkEndSubBanner) {
                    g.drawImage(this.endBannerSecondary, cx, cy + 40, center);
                }
                break;
            }
            case STATE_INTRO_SCROLL:
                this.drawMapWindow(g, this.mapViewport);
                this.drawScrollingText(g);
                break;
        }
    }

    public void onAction(int screenId, int actionId) {
        switch (actionId) {
            case ACTION_FINISH_GAME:
                CampaignStorage.clearCampaignSave();
                EngineStorage.k();
                BaseDialog congrats = GUIManager.a("Chúc mừng", "Chúc mừng bạn đã thống nhất giang sơn, hoàn thành trò chơi Loạn 12 Sứ Quân!", "Đóng", ACTION_RETURN_MAIN_MENU, 1);
                congrats.a((ActionListener) this);
                GUIManager.getInstance().a(congrats, true);
                break;
            case ACTION_SKIP_INTRO:
                this.setState(STATE_PROCEED_TO_CHAR_SELECT);
                break;
            case ACTION_SKIP_VICTORY_SCROLL:
                this.setState(STATE_VICTORY_BANNER);
                break;
            case ACTION_RETURN_MAIN_MENU:
                GUIManager.getInstance().a(false);
                GUIManager.getInstance().e.a(-1, 2, true);
                break;
        }
    }

    public void e() {
    }

    public void r() {
        BgmPlayer.getInstance().destroy();
    }
}
