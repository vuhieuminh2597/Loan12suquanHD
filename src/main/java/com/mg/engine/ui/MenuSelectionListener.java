package com.mg.engine.ui;

/**
 * Callback interface for menu item activation and focus changes.
 */
public interface MenuSelectionListener {
    void a(String itemLabel, int selectedIndex);

    void f(int previousIndex, int currentIndex);
}
