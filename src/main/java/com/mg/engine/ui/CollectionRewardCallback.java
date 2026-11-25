package com.mg.engine.ui;

import com.mg.engine.core.Callback;
import com.mg.engine.storage.EngineStorage;

/**
 * Callback invoked after claiming the completed relic collection reward, resetting the board.
 */
class CollectionRewardCallback extends Callback {
    private final CollectionScreenBase collectionScreen;

    CollectionRewardCallback(CollectionScreenBase collectionScreen) {
        this.collectionScreen = collectionScreen;
    }

    public void execute() {
        EngineStorage.x();
        this.collectionScreen.u = false;
        this.collectionScreen.c((SoftKeyButton) null);
        CollectionScreenBase.a(this.collectionScreen, EngineStorage.w());
        CollectionScreenBase.a(this.collectionScreen, false);
        this.collectionScreen.c = EngineStorage.s();
        this.collectionScreen.e = 0;
        CollectionScreenBase.a(this.collectionScreen);
    }
}
