package com.mic.smsgame.suquan.gfx;

import com.mg.engine.sprite.ToggleImageSprite;
import javax.microedition.lcdui.Image;

/**
 * Main-menu button sprite displaying index 1 when focused and index 0 when unfocused.
 */
public class MenuItemSprite extends ToggleImageSprite {
    public MenuItemSprite(Image[] states) {
        super(states);
    }

    public void a(boolean focused) {
        this.visible = focused;
        this.currentIndex = focused ? 1 : 0;
    }
}

