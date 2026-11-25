package com.mg.smsgame.main;

import com.mg.engine.core.AsyncTaskPool;
import com.mg.engine.core.DebugLogForm;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.core.SplashCanvas;
import com.mg.engine.storage.EngineStorage;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

/**
 * Base MIDlet lifecycle controller for MicroGame titles (offline standalone edition).
 */
public abstract class MGMIDlet extends MIDlet {
    public Display a;
    public static boolean b = false;
    private boolean started = false;
    protected long pausedTimestampMs = 0L;
    public boolean d;
    protected static MGMIDlet instance;

    public MGMIDlet(boolean debugMode) {
        GameConfig.debugMode = debugMode;
        this.a();
        this.started = false;

        try {
            this.detectNokiaPlatform();
            this.detectSonyEricssonPlatform();
        } catch (Throwable ignored) {
        }

        this.a = Display.getDisplay(this);
        instance = this;
        this.a.setCurrent(new SplashCanvas());
    }

    private void detectNokiaPlatform() {
        String platform = System.getProperty("microedition.platform");
        GameConfig.isNokiaSeries40 = false;
        if (platform != null) {
            int idx = platform.toLowerCase().trim().indexOf("nokia");
            if (idx >= 0) {
                GameConfig.isNokiaSeries40 = true;
                if (System.getProperty("com.nokia.mid.imei") != null
                        || System.getProperty("com.nokia.mid.imsi") != null
                        || System.getProperty("com.nokia.mid.networkid") != null
                        || System.getProperty("com.nokia.mid.networksignal") != null
                        || System.getProperty("com.nokia.mid.networkavailability") != null
                        || System.getProperty("com.nokia.mid.batterylevel") != null
                        || System.getProperty("com.nokia.mid.countrycode") != null) {
                    GameConfig.isNokiaSeries40 = false;
                }
            }
        }
        DebugLogForm.log("NokiaLessThanSerials60=" + GameConfig.isNokiaSeries40);
    }

    private void detectSonyEricssonPlatform() {
        GameConfig.isOldSonyEricsson = false;
        String platform = System.getProperty("com.sonyericsson.java.platform");
        if (platform == null || platform.length() <= 3) {
            return;
        }
        platform = platform.substring(3);
        int dot = platform.indexOf(".");
        if (dot > 0) {
            platform = platform.substring(0, dot);
        }
        int jpVersion = Integer.parseInt(platform);
        if (jpVersion < 8) {
            GameConfig.isOldSonyEricsson = true;
        }
    }

    protected void destroyApp(boolean unconditional) {
        if (GUIManager.instance != null) {
            GUIManager.instance.stopLoop();
        }
        AsyncTaskPool.getInstance().shutdown();
    }

    protected void pauseApp() {
        this.pausedTimestampMs = System.currentTimeMillis();
        this.b();
    }

    protected void startApp() {
        if (!this.started) {
            this.started = true;
            this.d = true;
        } else {
            long pausedMs = System.currentTimeMillis() - this.pausedTimestampMs;
            this.a(pausedMs);
        }
    }

    public final void c() {
        GUIManager gui;
        synchronized (GUIManager.ENGINE_LOCK) {
            EngineStorage.i();
            gui = GUIManager.getInstance();
            this.a(b);
            gui.startLoop();
        }
        this.a.setCurrent(gui);
    }

    public void d() {
        this.notifyDestroyed();
    }

    public void e() {
        this.a.setCurrent(DebugLogForm.getInstance());
    }

    public static MGMIDlet f() {
        return instance;
    }

    protected abstract void a();

    protected abstract void a(boolean resumed);

    protected abstract void b();

    protected abstract void a(long pausedMs);
}
