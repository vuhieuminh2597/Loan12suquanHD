package com.mic.smsgame.suquan;

import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.smsgame.main.MGMIDlet;
import com.mic.smsgame.suquan.ui.SuQuanController;
import com.mic.smsgame.suquan.ui.SuQuanLoadingScreen;

/**
 * MIDlet entry point for <em>Loạn 12 Sứ Quân</em> (declared in {@code META-INF/MANIFEST.MF}).
 */
public class SQMidlet extends MGMIDlet {
    private static final int THEME_BG_COLOR = 0x2C1B10;
    private static final int THEME_BORDER_COLOR = 0xD4A24E;

    public SQMidlet() {
        super(false);
    }

    protected void a() {
    }

    protected void b() {
    }

    protected void a(long elapsedMs) {
    }

    protected void a(boolean resumed) {
        GameConfig.defaultExtraLives = 3;
        GameConfig.setThemeColor(THEME_BG_COLOR);
        GameConfig.frameBackgroundColor = THEME_BG_COLOR;
        GameConfig.frameBorderColor = THEME_BORDER_COLOR;
        GameConfig.themeHighlightColor = 0x000000;
        GUIManager.getInstance().e = new SuQuanController();
        GUIManager.getController().a(new SuQuanLoadingScreen());
        GUIManager.getInstance().e.a(-1, 1, false);
    }
}
