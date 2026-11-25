package com.mic.smsgame.suquan.ui;

import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.engine.ui.BaseScreen;
import com.mg.engine.util.RandomUtils;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Full-screen "Loạn 12 Sứ Quân" key-art splash shown briefly before {@link MainMenuScreen}. */
public class TitleSplashScreen extends BaseScreen {
    private static final int DISPLAY_TICKS = 30;

    private final Image backgroundImage = MgImageLoader.load("/bksplashscreen");
    private int elapsedTicks;

    public TitleSplashScreen() {
        GUIManager.getInstance().a(DISPLAY_TICKS);
    }

    public void a(Graphics g) {
        g.drawImage(this.backgroundImage, GameConfig.contentOffsetX, GameConfig.contentOffsetY, Graphics.TOP | Graphics.LEFT);
    }

    public void a() {
        ++this.elapsedTicks;
        if (this.elapsedTicks == DISPLAY_TICKS) {
            GUIManager.getController().f = GUIManager.getController().d(RandomUtils.nextInt(GUIManager.getController().j()));
            GUIManager.getInstance().a((BaseScreen) new MainMenuScreen(), true);
        }
    }
}


