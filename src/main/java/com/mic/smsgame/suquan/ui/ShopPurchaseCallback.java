package com.mic.smsgame.suquan.ui;

import com.mg.engine.core.Callback;
import com.mg.engine.core.GUIManager;

/**
 * Callback completing an in-game shop item acquisition.
 */
class ShopPurchaseCallback extends Callback {
    private final ShopScreen shopScreen;

    ShopPurchaseCallback(ShopScreen shopScreen) {
        this.shopScreen = shopScreen;
    }

    public void execute() {
        GUIManager.getInstance().a(false);
        ShopScreen.a(this.shopScreen);
    }
}
