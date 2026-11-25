package com.mg.engine.ui;

import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.engine.graphics.Rect;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** On-screen 5-way directional touch pad (Left, Right, Up, Down, Fire) for touchscreen devices. */
public class TouchDPad {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected Image spriteImage;
    private boolean pressed;
    protected int[] keyCodes;
    protected Rect[] hitZones;
    private int activeZone;

    public int a() {
        return this.width;
    }

    public int b() {
        return this.height;
    }

    public TouchDPad(String imagePath, int x, int y) {
        this.a(x, y);
        this.spriteImage = MgImageLoader.load(imagePath);
        this.width = this.spriteImage.getWidth() / 2;
        this.height = this.spriteImage.getHeight();
        this.keyCodes = new int[]{14, 15, 12, 13, 16};
        this.hitZones = new Rect[]{
                new Rect(0, 13, 18, 28),
                new Rect(36, 13, 18, 28),
                new Rect(13, 0, 28, 18),
                new Rect(13, 36, 28, 18),
                new Rect(20, 20, 14, 14)
        };
        this.activeZone = -1;
        this.pressed = false;
    }

    public void a(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int b(int px, int py) {
        if (this.pressed) {
            return -1;
        }
        if (px >= this.x && px <= this.x + this.width && py >= this.y && py <= this.y + this.height) {
            this.pressed = true;
            int localX = px - this.x;
            int localY = py - this.y;
            for (int i = 0; i < this.hitZones.length; ++i) {
                if (this.hitZones[i].a(localX, localY)) {
                    this.activeZone = i;
                    return this.keyCodes[i];
                }
            }
        }
        this.activeZone = -1;
        return -1;
    }

    public int c() {
        this.pressed = false;
        if (this.activeZone > -1) {
            int released = this.activeZone;
            this.activeZone = -1;
            return this.keyCodes[released];
        }
        return -1;
    }

    public void a(Graphics g) {
        DrawUtils.drawRegion(g, this.spriteImage, 0, 0, this.width, this.height, this.x, this.y, Graphics.TOP | Graphics.LEFT);
        if (this.activeZone > -1) {
            Rect zone = this.hitZones[this.activeZone];
            DrawUtils.drawRegion(
                    g,
                    this.spriteImage,
                    this.width + zone.a,
                    zone.b,
                    zone.c,
                    zone.d,
                    this.x + zone.a,
                    this.y + zone.b,
                    Graphics.TOP | Graphics.LEFT
            );
        }
    }
}


