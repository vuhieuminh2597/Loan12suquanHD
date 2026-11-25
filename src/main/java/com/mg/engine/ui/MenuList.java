package com.mg.engine.ui;

import com.mg.engine.sprite.Drawable;
import javax.microedition.lcdui.Graphics;

/**
 * Navigable list of {@link Drawable} items supporting keypad and pointer selection.
 */
public class MenuList extends Drawable {
    protected Drawable[] items;
    protected int selectedIndex;
    protected boolean wrapAround = true;
    protected MenuSelectionListener listener;

    public void a(Drawable[] items, int initialIndex) {
        this.items = items;
        this.selectedIndex = initialIndex;
        this.b(0, initialIndex);
    }

    public void a(int keyCode) {
        if (this.items == null) {
            return;
        }
        int prevIndex = this.selectedIndex;
        switch (keyCode) {
            case 12:
            case 14:
                --this.selectedIndex;
                if (this.selectedIndex < 0) {
                    this.selectedIndex = this.wrapAround ? this.items.length - 1 : 0;
                }
                break;
            case 13:
            case 15:
                ++this.selectedIndex;
                if (this.selectedIndex >= this.items.length) {
                    this.selectedIndex = this.wrapAround ? 0 : this.items.length - 1;
                }
                break;
            case 16:
                this.b(this.selectedIndex);
                return;
        }

        if (prevIndex != this.selectedIndex) {
            this.b(prevIndex, this.selectedIndex);
        }
    }

    public void a(int pointerX, int pointerY) {
        if (this.items == null) {
            return;
        }
        int prevIndex = this.selectedIndex;
        for (int i = 0; i < this.items.length; ++i) {
            Drawable item = this.items[i];
            if (pointerX >= item.i() && pointerX <= item.i() + item.k()
                    && pointerY >= item.j() && pointerY <= item.j() + item.l()) {
                this.selectedIndex = i;
                if (prevIndex == this.selectedIndex) {
                    this.b(this.selectedIndex);
                } else {
                    this.b(prevIndex, this.selectedIndex);
                }
                break;
            }
        }
    }

    public Drawable a() {
        return this.items[this.selectedIndex];
    }

    public void b() {
    }

    public final void a(Graphics g, int offsetX, int offsetY) {
        this.b(g);
    }

    protected void b(Graphics g) {
        for (int i = 0; i < this.items.length; ++i) {
            this.items[i].a(g);
        }
    }

    protected void b(int index) {
        if (this.listener != null) {
            this.listener.a(this.items[index].toString(), this.selectedIndex);
        }
    }

    protected void b(int prevIndex, int newIndex) {
        this.items[prevIndex].a(false);
        this.items[this.selectedIndex].a(true);
        if (this.listener != null) {
            this.listener.f(prevIndex, this.selectedIndex);
        }
    }

    public void a(MenuSelectionListener listener) {
        this.listener = listener;
    }
}

