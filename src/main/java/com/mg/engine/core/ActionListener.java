package com.mg.engine.core;

/** Listener notified when a UI screen or dialog soft-key / command action is triggered. */
public interface ActionListener {
    void onAction(int screenId, int actionId);
}



