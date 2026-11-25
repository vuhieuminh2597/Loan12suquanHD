package com.loan12suquan.hd.web;

import com.loan12suquan.hd.HdConfig;
import com.loan12suquan.hd.HdInputState;
import com.loan12suquan.hd.platform.HdPlatform;
import com.mg.engine.audio.BgmPlayer;
import com.mg.engine.core.DebugLogForm;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.core.SplashCanvas;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.engine.storage.EngineStorage;
import com.mic.smsgame.suquan.SQMidlet;
import java.util.Base64;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.rms.RecordStore;
import org.teavm.jso.JSObject;

/**
 * Main TeaVM HTML5 / iPhone 17 PWA entry point for <em>Loạn 12 Sứ Quân HD</em>.
 * <p>
 * Drives the {@code 720x960} Retina Canvas2D pipeline with a ProMotion-safe 60 FPS
 * fixed-timestep accumulator so gameplay & animations run identically on 60Hz and 120Hz iPhone 17 displays.
 */
public final class WebLauncher {
    private static final double TARGET_FRAME_MS = 1000.0 / 60.0;

    private static WebSurface screenSurface;
    private static Graphics screenGraphics;
    private static SQMidlet midlet;
    private static double lastTimestampMs = -1.0;
    private static double highRateAccumMs = 0.0;

    private static final String[] PREWARM_SPRITES = new String[]{
            "/bkmenu", "/swordfocus", "/avatar", "/itemshops",
            "/frame1", "/frame2", "/frame3", "/focusitem",
            "/sword", "/rice", "/heart", "/yinyang", "/gold", "/book", "/swordred",
            "/bkboard", "/star", "/m/m"
    };
    private static int prewarmCursor = 0;

    private WebLauncher() {
    }

    public static void main(String[] args) {
        HdPlatform.setBackend(new WebPlatformBackend());
        GUIManager.externalRenderLoop = true;

        JSObject domCanvas = WebDomBridge.getCanvasById("game-canvas");
        screenSurface = new WebSurface(
                domCanvas,
                HdConfig.PLAYFIELD_HD_WIDTH,
                HdConfig.PLAYFIELD_HD_HEIGHT,
                null,
                false);
        screenGraphics = new Graphics(
                screenSurface,
                HdConfig.LOGICAL_WIDTH,
                HdConfig.LOGICAL_HEIGHT);

        installDomCallbacks();

        midlet = new SQMidlet();
        midlet.invokeStartApp();

        schedulePrewarmSlice();
        scheduleNextFrame();
    }

    /**
     * Pre-warms heavy 3x xBRZ sprites across non-blocking idle slices during splash/menu
     * so opening Shop, Character Select, World Map, or Battle has zero frame-time hitching.
     */
    private static void schedulePrewarmSlice() {
        if (prewarmCursor >= PREWARM_SPRITES.length) {
            return;
        }
        WebDomBridge.setTimeout(18, new WebDomBridge.JsRunnable() {
            @Override
            public void run() {
                if (prewarmCursor < PREWARM_SPRITES.length) {
                    try {
                        MgImageLoader.load(PREWARM_SPRITES[prewarmCursor]);
                    } catch (Throwable ignored) {
                    }
                    ++prewarmCursor;
                    schedulePrewarmSlice();
                }
            }
        });
    }

    private static void scheduleNextFrame() {
        WebDomBridge.requestAnimationFrame(new WebDomBridge.FrameCallback() {
            @Override
            public void onFrame(double timestampMs) {
                onAnimationFrame(timestampMs);
                scheduleNextFrame();
            }
        });
    }

    private static void onAnimationFrame(double timestampMs) {
        if (lastTimestampMs < 0.0) {
            lastTimestampMs = timestampMs;
            stepAndRenderOne60HzFrame(16L);
            return;
        }
        double deltaMs = timestampMs - lastTimestampMs;
        lastTimestampMs = timestampMs;
        if (deltaMs < 0.0) {
            deltaMs = 0.0;
        } else if (deltaMs > 80.0) {
            deltaMs = 80.0;
        }

        long stepMs;
        if (deltaMs >= 11.5) {
            // Standard 60Hz / 75Hz display: lock 1:1 to every hardware VSync (zero beat-frequency stutter!)
            highRateAccumMs = 0.0;
            stepMs = Math.round(deltaMs);
        } else {
            // High-refresh 120Hz ProMotion (iPhone 17) / 144Hz display: lock 1:2 to 60 FPS cleanly
            highRateAccumMs += deltaMs;
            if (highRateAccumMs < 14.2) {
                return;
            }
            stepMs = Math.round(Math.min(33.0, highRateAccumMs));
            highRateAccumMs -= TARGET_FRAME_MS;
            if (highRateAccumMs < 0.0 || highRateAccumMs > 12.0) {
                highRateAccumMs = 0.0;
            }
        }

        stepSimulation60Hz(stepMs);
        paintActiveCanvas();
        if (HdConfig.particlesEnabled && HdConfig.visualMode != HdConfig.MODE_CLASSIC_PIXEL) {
            WebDomBridge.stepFxOverlay(stepMs * 0.001);
        }
    }

    private static void stepSimulation60Hz(long dtMs) {
        Displayable current = Display.getActiveDisplay().getCurrent();
        if (current instanceof SplashCanvas) {
            try {
                ((SplashCanvas) current).stepExternalSplash();
            } catch (Throwable err) {
                DebugLogForm.log(err);
            }
        } else if (current instanceof GUIManager) {
            try {
                ((GUIManager) current).stepExternalFrame(dtMs);
            } catch (Throwable err) {
                DebugLogForm.log(err);
            }
        }
    }

    private static void paintActiveCanvas() {
        Displayable current = Display.getActiveDisplay().getCurrent();
        if (current instanceof Canvas) {
            try {
                ((Canvas) current).invokePaint(screenGraphics);
            } catch (Throwable err) {
                DebugLogForm.log(err);
            }
        }
    }

    private static void stepAndRenderOne60HzFrame(long dtMs) {
        stepSimulation60Hz(dtMs);
        paintActiveCanvas();
    }

    private static void installDomCallbacks() {
        WebDomBridge.bindRuntimeCallbacks(
                new WebDomBridge.PointerCallback() {
                    @Override
                    public void onPointer(int kind, int lx, int ly, int wheelDelta) {
                        int cx = Math.max(0, Math.min(HdConfig.LOGICAL_WIDTH - 1, lx));
                        int cy = Math.max(0, Math.min(HdConfig.LOGICAL_HEIGHT - 1, ly));
                        Displayable cur = Display.getActiveDisplay().getCurrent();
                        switch (kind) {
                            case 0: // pointer/touch down
                                HdInputState.updateHover(cx, cy);
                                if (HdConfig.particlesEnabled && HdConfig.visualMode != HdConfig.MODE_CLASSIC_PIXEL) {
                                    WebDomBridge.spawnSparkBurst(cx * HdConfig.HD_SCALE, cy * HdConfig.HD_SCALE);
                                }
                                if (cur instanceof Canvas) {
                                    ((Canvas) cur).pointerPressed(cx, cy);
                                }
                                break;
                            case 1: // pointer/touch drag
                                HdInputState.updateHover(cx, cy);
                                if (cur instanceof Canvas) {
                                    ((Canvas) cur).pointerDragged(cx, cy);
                                }
                                break;
                            case 2: // mouse up
                                if (cur instanceof Canvas) {
                                    ((Canvas) cur).pointerReleased(cx, cy);
                                }
                                break;
                            case 20: // touch end (clear hover so tapped buttons don't stay stuck highlighted on iPhone)
                                if (cur instanceof Canvas) {
                                    ((Canvas) cur).pointerReleased(cx, cy);
                                }
                                HdInputState.clearHover();
                                break;
                            case 3: // mouse move
                                HdInputState.updateHover(cx, cy);
                                break;
                            case 4: // mouse leave
                                HdInputState.clearHover();
                                break;
                            case 5: // wheel
                                HdInputState.addWheelDelta(wheelDelta);
                                break;
                            case 6: // right-click -> right softkey (Back)
                                if (cur instanceof Canvas) {
                                    ((Canvas) cur).keyPressed(-7);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                },
                new WebDomBridge.KeyCallback() {
                    @Override
                    public void onKey(int kind, int midpKey, int typedChar) {
                        Displayable cur = Display.getActiveDisplay().getCurrent();
                        if (!(cur instanceof Canvas)) {
                            return;
                        }
                        Canvas canvas = (Canvas) cur;
                        if (kind == 0) { // keyDown
                            if (GameConfig.textInputActive) {
                                if (midpKey == 8) {
                                    canvas.keyPressed(8);
                                    return;
                                }
                                if (typedChar > 0) {
                                    return;
                                }
                            }
                            if (midpKey != 0) {
                                canvas.keyPressed(midpKey);
                            }
                        } else if (kind == 1) { // keyUp
                            if (midpKey != 0) {
                                canvas.keyReleased(midpKey);
                            }
                        } else if (kind == 2 && GameConfig.textInputActive && typedChar > 0) { // keyTyped
                            char ch = (char) typedChar;
                            if ((ch >= 'A' && ch <= 'Z')
                                    || (ch >= 'a' && ch <= 'z')
                                    || (ch >= '0' && ch <= '9')
                                    || ch == ' ') {
                                canvas.keyPressed(ch);
                            }
                        }
                    }
                },
                new WebDomBridge.UiCommandCallback() {
                    @Override
                    public String onCommand(String cmd, String payload) {
                        if ("toggleHd".equals(cmd)) {
                            HdConfig.cycleVisualMode();
                            GameConfig.highFpsEnabled = true;
                            GameConfig.hdUpscaleEnabled = HdConfig.isHdUpscaleActive();
                            EngineStorage.saveDisplaySettings();
                            return HdConfig.getModeBadgeLabel();
                        }
                        if ("getHdLabel".equals(cmd)) {
                            return HdConfig.getModeBadgeLabel();
                        }
                        if ("cycleVolume".equals(cmd)) {
                            int v = BgmPlayer.getInstance().cycleLiveVolumePreset();
                            return String.valueOf(v);
                        }
                        if ("getVolume".equals(cmd)) {
                            int v = GameConfig.soundEnabled ? GameConfig.soundVolume : 0;
                            return String.valueOf(v);
                        }
                        if ("back".equals(cmd)) {
                            Displayable cur = Display.getActiveDisplay().getCurrent();
                            if (cur instanceof Canvas) {
                                ((Canvas) cur).keyPressed(-7);
                            }
                            return "OK";
                        }
                        if ("leftSoft".equals(cmd)) {
                            Displayable cur = Display.getActiveDisplay().getCurrent();
                            if (cur instanceof Canvas) {
                                ((Canvas) cur).keyPressed(-6);
                            }
                            return "OK";
                        }
                        if ("exportSave".equals(cmd)) {
                            try {
                                byte[] bytes = RecordStore.exportAllStoresToBytes();
                                return Base64.getEncoder().encodeToString(bytes);
                            } catch (Exception e) {
                                return "";
                            }
                        }
                        if ("importSave".equals(cmd)) {
                            if (payload == null || payload.isEmpty()) {
                                return "ERR:Dữ liệu trống";
                            }
                            try {
                                byte[] decoded = Base64.getDecoder().decode(payload.trim());
                                boolean ok = RecordStore.importAllStoresFromBytes(decoded);
                                return ok ? "OK" : "ERR:Tệp sao lưu không hợp lệ";
                            } catch (Exception e) {
                                return "ERR:" + e.getMessage();
                            }
                        }
                        return "";
                    }
                });
    }
}
