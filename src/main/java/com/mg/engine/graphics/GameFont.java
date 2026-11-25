package com.mg.engine.graphics;

import javax.microedition.lcdui.Graphics;

/** Base text font abstraction supporting system fonts and custom Vietnamese bitmap fonts. */
public abstract class GameFont {
    public abstract int a();

    public abstract int a(String text);

    public abstract void a(Graphics g, String text, int x, int y, int align);

    public abstract int a(char ch);
}



