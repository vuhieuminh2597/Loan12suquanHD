package com.mg.engine.core;

import com.loan12suquan.hd.platform.HdPlatform;
import com.mg.engine.graphics.FontManager;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.smsgame.main.MGMIDlet;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;

/** Initial splash canvas displaying the MicroGame and partner logos before starting {@link GUIManager}. */
public class SplashCanvas extends GameCanvas implements Runnable {
    private static final int STAGE_MG_LOGO = 0;
    private static final int STAGE_PARTNER_LOGO = 1;

    private int tickCount = 0;
    private Graphics graphics;
    private Image logoImage;
    private int canvasHeight;
    private int canvasWidth;
    private int logoOffsetY;
    private int curtainStep = 0;
    private int bounceLimit = 0;
    private int bounceCount = 0;
    private int stripHeight = 10;
    private int stripReveal = 1;
    private int centerY;
    private int stripDirection = 1;
    private int maxCurtainSteps;
    private boolean running;
    private boolean splashFinished;
    private boolean curtainPaused;
    private int stage;
    private Font footerFont;

    public SplashCanvas() {
        super(false);
        this.maxCurtainSteps = 160 / this.stripHeight;
        this.running = true;
        this.curtainPaused = false;
        this.stage = STAGE_MG_LOGO;
        this.footerFont = null;

        this.setFullScreenMode(true);
        this.footerFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        this.graphics = this.getGraphics();
        this.canvasWidth = this.getWidth();
        this.canvasHeight = this.getHeight();

        FontManager.init();
        this.logoImage = MgImageLoader.load("/_mglogo");
        this.logoOffsetY = this.logoImage.getHeight() / 5;
        this.maxCurtainSteps = this.logoImage.getHeight() / 2 / this.stripHeight + 1;
        if (!HdPlatform.isWeb()) {
            new Thread(this).start();
        }
    }

    /** Non-blocking single-frame splash step used by Web / iPhone 17 {@code requestAnimationFrame} host. */
    public void stepExternalSplash() {
        if (this.splashFinished) {
            return;
        }
        if (GameConfig.debugMode || (!this.running && MGMIDlet.f().d)) {
            this.splashFinished = true;
            this.renderFrame();
            this.finishSplash();
            return;
        }
        if (this.stage == STAGE_MG_LOGO) {
            synchronized (this.getBufferLock()) {
                this.renderFrame();
                if (!this.curtainPaused && this.curtainStep < this.maxCurtainSteps) {
                    this.renderCurtainReveal();
                }
            }
            this.flushGraphics();
            ++this.tickCount;
            if (this.tickCount > 30) {
                this.curtainPaused = false;
            }
            if (this.tickCount > 60) {
                this.loadPartnerLogo();
                this.stage = STAGE_PARTNER_LOGO;
                this.tickCount = 0;
            }
        } else {
            synchronized (this.getBufferLock()) {
                this.renderFrame();
            }
            this.flushGraphics();
            ++this.tickCount;
            if (this.tickCount > 40) {
                this.running = false;
            }
        }
    }

    public void run() {
        if (!GameConfig.debugMode) {
            while (this.running || !MGMIDlet.f().d) {
                if (this.stage == STAGE_MG_LOGO) {
                    synchronized (this.getBufferLock()) {
                        this.renderFrame();
                        if (!this.curtainPaused && this.curtainStep < this.maxCurtainSteps) {
                            this.renderCurtainReveal();
                        }
                    }

                    this.flushGraphics();
                    ++this.tickCount;
                    if (this.tickCount > 30) {
                        this.curtainPaused = false;
                    }
                    if (this.tickCount > 60) {
                        this.loadPartnerLogo();
                        this.stage = STAGE_PARTNER_LOGO;
                        this.tickCount = 0;
                    }
                } else {
                    synchronized (this.getBufferLock()) {
                        this.renderFrame();
                    }
                    this.flushGraphics();
                    ++this.tickCount;
                    if (this.tickCount > 40) {
                        this.running = false;
                    }
                }

                try {
                    Thread.sleep(16L);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            this.renderFrame();
        }

        this.finishSplash();
    }

    private void loadPartnerLogo() {
        this.logoImage = MgImageLoader.load("/_partnerLogo");
    }

    private void finishSplash() {
        int rawW = this.getWidth();
        int rawH = this.getHeight();
        GameConfig.canvasWidth = rawW;
        GameConfig.canvasHeight = rawH;
        GameConfig.screenWidth = rawW > 240 ? 240 : rawW;
        GameConfig.screenHeight = rawH > 320 ? 320 : rawH;

        if (rawW >= 480 && rawH >= 640) {
            GameConfig.renderScale = 2;
            GameConfig.viewportOffsetX = (rawW - 480) / 2;
            GameConfig.viewportOffsetY = (rawH - 640) / 2;
            GameConfig.hasLetterbox = (rawW > 480 || rawH > 640);
        } else {
            GameConfig.renderScale = 1;
            GameConfig.viewportOffsetX = (rawW - GameConfig.screenWidth) / 2;
            GameConfig.viewportOffsetY = (rawH - GameConfig.screenHeight) / 2;
            GameConfig.hasLetterbox = (rawW > 240 || rawH > 320);
        }
        if (GameConfig.screenHeight == 320 && GameConfig.screenWidth == 240) {
            GameConfig.isExact240x320 = true;
        }

        GameConfig.contentOffsetX = (GameConfig.screenWidth - 240) / 2;
        GameConfig.contentOffsetY = (GameConfig.screenHeight - 320) / 2;
        MGMIDlet.f().c();
        this.releaseResources();
    }

    private void releaseResources() {
        this.logoImage = null;
        this.footerFont = null;
        System.gc();
    }

    private void renderFrame() {
        this.canvasWidth = this.getWidth();
        this.canvasHeight = this.getHeight();
        this.graphics.setClip(0, 0, this.canvasWidth, this.canvasHeight);
        this.graphics.setColor(0xFFFFFF);
        this.graphics.fillRect(0, 0, this.canvasWidth, this.canvasHeight);

        if (this.stage == STAGE_MG_LOGO) {
            this.graphics.drawImage(this.logoImage, this.canvasWidth / 2, this.canvasHeight / 2 - this.logoOffsetY, Graphics.HCENTER | Graphics.VCENTER);
        } else {
            this.graphics.drawImage(
                    this.logoImage,
                    (this.canvasWidth - this.logoImage.getWidth()) / 2,
                    (this.canvasHeight - this.logoImage.getHeight()) / 2,
                    0
            );
        }
    }

    public void keyPressed(int keyCode) {
        this.running = false;
    }

    private void renderCurtainReveal() {
        this.centerY = this.canvasHeight / 2 - this.logoOffsetY;
        this.graphics.setColor(0xFFFFFF);
        this.graphics.fillRect(0, 0, this.canvasWidth, this.centerY - this.stripHeight / 2 - this.curtainStep * this.stripHeight);
        this.graphics.fillRect(
                0,
                this.centerY + this.stripHeight / 2 + this.curtainStep * this.stripHeight,
                this.canvasWidth,
                this.canvasHeight - this.centerY + this.stripHeight / 2 + this.curtainStep * this.stripHeight
        );

        int y = this.centerY - this.stripHeight / 2 - this.curtainStep * this.stripHeight;
        this.graphics.fillRect(0, y, this.canvasWidth, this.stripHeight / 2 - this.stripReveal);
        y = y + 10 - (this.stripHeight / 2 - this.stripReveal);
        this.graphics.fillRect(0, y, this.canvasWidth, this.stripHeight / 2 - this.stripReveal);

        y = this.centerY - this.stripHeight / 2 + this.curtainStep * this.stripHeight;
        this.graphics.fillRect(0, y, this.canvasWidth, this.stripHeight / 2 - this.stripReveal);
        y = y + 10 - (this.stripHeight / 2 - this.stripReveal);
        this.graphics.fillRect(0, y, this.canvasWidth, this.stripHeight / 2 - this.stripReveal);

        this.stripReveal += this.stripDirection;
        if (this.stripReveal >= this.stripHeight / 2 || this.stripReveal <= 0) {
            if (this.bounceCount >= this.bounceLimit) {
                this.stripReveal = 0;
                ++this.curtainStep;
                this.stripDirection = 1;
                this.bounceCount = 0;
            } else {
                ++this.bounceCount;
                this.stripDirection = -this.stripDirection;
            }
        }
    }

    public void pointerPressed(int x, int y) {
        this.running = false;
    }
}



