package com.mic.smsgame.suquan.ui;

import com.mg.engine.audio.BgmPlayer;
import com.mg.engine.core.GameConfig;
import com.mg.engine.ui.BaseScreen;
import com.mg.engine.ui.CollectionScreenBase;
import com.mg.engine.ui.ScreenController;
import com.mg.engine.ui.SoundConfigScreen;
import com.mg.engine.ui.transition.BlindsDownTransition;
import com.mg.engine.ui.transition.BlindsLeftTransition;
import com.mg.engine.ui.transition.BlindsRightTransition;
import com.mg.engine.ui.transition.BlindsUpTransition;
import com.mg.engine.ui.transition.DialogCloseAnimation;
import com.mg.engine.ui.transition.DialogOpenAnimation;
import com.mg.engine.ui.transition.ScreenTransition;

/** Main screen router and transition factory for Loạn 12 Sứ Quân. */
public class SuQuanController extends ScreenController {
    public static final int SCREEN_SOUND_CONFIG = 1;
    public static final int SCREEN_MAIN_MENU = 2;
    public static final int SCREEN_COLLECTION = 3;
    public static final int SCREEN_SHOP = 4;
    public static final int SCREEN_BATTLE = 5;
    public static final int SCREEN_TITLE_SPLASH = 6;
    public static final int SCREEN_WORLD_MAP = 7;
    public static final int SCREEN_CHARACTER_SELECT = 8;

    public SuQuanController() {
        super(true);
    }

    public CollectionScreenBase k() {
        if (GameConfig.isInGameActive) {
            return null;
        }
        int returnScreenId = SCREEN_MAIN_MENU;
        if (super.b != null) {
            returnScreenId = super.b.k();
        }
        if (WorldMapScreen.c) {
            returnScreenId = SCREEN_WORLD_MAP;
        }
        if (returnScreenId < 0) {
            return null;
        }
        if (returnScreenId == 0) {
            returnScreenId = SCREEN_MAIN_MENU;
        }
        return new SuQuanCollectionScreen(returnScreenId);
    }

    public void l() {
        super.g = new ScreenTransition[]{
                new BlindsUpTransition(),
                new BlindsDownTransition(),
                new BlindsRightTransition(),
                new BlindsLeftTransition()
        };
    }

    public void m() {
        super.k = new DialogCloseAnimation();
        super.j = new DialogOpenAnimation();
    }

    protected BaseScreen d(int fromScreenId, int nextScreenId) {
        GameConfig.isInGameActive = false;
        switch (nextScreenId) {
            case SCREEN_SOUND_CONFIG:
                return new SoundConfigScreen(SCREEN_TITLE_SPLASH);
            case SCREEN_MAIN_MENU:
                return new MainMenuScreen();
            case SCREEN_COLLECTION:
                return new SuQuanCollectionScreen(fromScreenId);
            case SCREEN_SHOP:
                return new ShopScreen(fromScreenId);
            case SCREEN_BATTLE:
                BattleScreen battleScreen = new BattleScreen();
                GameConfig.isInGameActive = true;
                return battleScreen;
            case SCREEN_TITLE_SPLASH:
                return new TitleSplashScreen();
            case SCREEN_WORLD_MAP:
                if (fromScreenId == SCREEN_MAIN_MENU) {
                    WorldMapScreen.d = true;
                }
                return new WorldMapScreen();
            case SCREEN_CHARACTER_SELECT:
                return new CharacterSelectScreen();
            default:
                return null;
        }
    }

    public void onAction(int screenId, int actionId) {
    }

    public void h() {
        super.h();
        if (!GameConfig.isInGameActive) {
            BgmPlayer.getInstance().playAsync();
        }
    }

    public void g() {
        super.g();
        if (!GameConfig.isInGameActive) {
            BgmPlayer.getInstance().stop();
        }
    }
}


