package com.mg.engine.core;

import com.loan12suquan.hd.HdConfig;
import com.mg.engine.graphics.DrawUtils;
import com.mg.engine.graphics.MgImageLoader;
import com.mg.engine.storage.EngineStorage;
import com.mg.engine.ui.BaseDialog;
import com.mg.engine.ui.BaseScreen;
import com.mg.engine.ui.MessageDialog;
import com.mg.engine.ui.ScreenController;
import com.mg.engine.ui.Window;
import com.mg.smsgame.main.MGMIDlet;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Main MIDP {@link Canvas} driving the fixed-interval (40 ms / 25 FPS) game loop,
 * 240x320 double-buffer rendering, vendor key translation, and modal dialog dispatch.
 */
public class GUIManager extends Canvas implements ActionListener, Runnable {
    private static final long FRAME_INTERVAL_MS = 40L;

    /** Global monitor lock serializing state mutations, input, background loading commits, and rendering. */
    public static final Object ENGINE_LOCK = new Object();
    /** When true (LibGDX host), ticks are driven deterministically on the OpenGL render thread before paint(). */
    public static volatile boolean externalRenderLoop = false;

    public static GUIManager instance;
    public static int softKeyCooldown = 0;
    public static Image cornerImage;
    public static Image arrowImage;
    public static int exitCountdownTicks = -1000;

    private static int vibrateTicks = -1;

    private long frameStartMs;
    private long frameEndMs;
    private boolean stopped;
    private boolean backgroundCleared = false;
    private boolean pauseDialogOpen;
    private boolean hidden;
    private final Object pauseLock = new Object();
    private Graphics backBufferGraphics;
    private Image backBufferImage;
    private Graphics presentBufferGraphics;
    private Image presentBufferImage;
    private boolean lastWasShaking = false;
    private long externalLogicAccumMs = 0L;
    public ScreenController e;

    public static ScreenController getController() {
        return getInstance().e;
    }

    public static GUIManager getInstance() {
        if (instance == null) {
            instance = new GUIManager();
        }
        return instance;
    }

    protected GUIManager() {
        EngineStorage.p();
        EngineStorage.loadDisplaySettings();
        cornerImage = MgImageLoader.load("/_corner");
        arrowImage = MgImageLoader.load("/_arrow");
        this.backBufferImage = Image.createImage(GameConfig.screenWidth, GameConfig.screenHeight);
        this.backBufferGraphics = this.backBufferImage.getGraphics();
        if (GameConfig.renderScale == 2) {
            try {
                this.presentBufferImage = Image.createImage(GameConfig.screenWidth, GameConfig.screenHeight);
                this.presentBufferGraphics = this.presentBufferImage.getGraphics();
            } catch (OutOfMemoryError oom) {
                this.presentBufferImage = null;
                this.presentBufferGraphics = null;
                GameConfig.renderScale = 1;
            }
        }
        this.setFullScreenMode(true);
        GameConfig.hasPointerEvents = this.hasPointerEvents();

        String platform = System.getProperty("microedition.platform");
        if (this.getKeyCode(FIRE) == -20) {
            GameConfig.vendorId = 3;
        } else if (platform != null && platform.length() != 0) {
            if (platform.indexOf("NokiaN") >= 0) {
                GameConfig.vendorId = 1;
            } else if (platform.indexOf("Nokia") >= 0) {
                GameConfig.vendorId = 0;
            } else if (platform.indexOf("Sony") >= 0) {
                GameConfig.vendorId = 2;
            } else if (platform.indexOf("RIM") >= 0) {
                GameConfig.vendorId = 5;
            } else if (platform.indexOf("SIE-EL71") >= 0) {
                GameConfig.vendorId = 6;
            } else {
                GameConfig.vendorId = 4;
            }
        } else {
            GameConfig.vendorId = 4;
        }

        DebugLogForm.log("Vendor: " + platform + " detected as " + GameConfig.vendorId);
    }

    public void startLoop() {
        this.stopped = false;
        this.hidden = false;
        this.externalLogicAccumMs = 0L;
        if (!externalRenderLoop) {
            Thread loopThread = new Thread(this, "Loan12SuQuan-LegacyLoop");
            loopThread.setDaemon(true);
            loopThread.start();
        }
    }

    public void stopLoop() {
        this.stopped = true;
        synchronized (this.pauseLock) {
            this.pauseLock.notifyAll();
        }
    }

    /**
     * Steps game logic and 60 Hz visual interpolation directly on the LibGDX render thread
     * right before {@link #paint(Graphics)}, eliminating multi-thread race conditions and frame jitter.
     */
    public void stepExternalFrame(long deltaMs) {
        if (this.stopped || this.hidden) {
            return;
        }
        if (deltaMs < 0L) {
            deltaMs = 0L;
        } else if (deltaMs > 120L) {
            deltaMs = 120L;
        }
        synchronized (ENGINE_LOCK) {
            ScreenController ctrl = this.e;
            if (ctrl == null) {
                return;
            }
            if (GameConfig.highFpsEnabled) {
                this.externalLogicAccumMs += deltaMs;
                int steps = 0;
                while (this.externalLogicAccumMs >= FRAME_INTERVAL_MS && steps < 3) {
                    this.externalLogicAccumMs -= FRAME_INTERVAL_MS;
                    this.updateTick();
                    ++steps;
                }
                int sub = (int) ((this.externalLogicAccumMs << 8) / FRAME_INTERVAL_MS);
                GameConfig.renderSubTick256 = sub < 0 ? 0 : (sub > 255 ? 255 : sub);
                ++GameConfig.visualFrameCounter;
                ctrl.onVisualFrame60Hz();
            } else {
                this.externalLogicAccumMs = 0L;
                GameConfig.renderSubTick256 = 0;
                this.updateTick();
                ++GameConfig.visualFrameCounter;
            }
        }
    }

    public static final void a(Graphics g, int fillColor, int borderColor, int x, int y, int width, int height) {
        DrawUtils.drawAntiquePanel(
                g, x, y, width, height,
                0x3D2819, 0x26170E, 0x160D08,
                0xB8893D, 0xF0C975, 0xFFE680);
        if (!HdConfig.isHdRemaster() && cornerImage != null) {
            g.drawRegion(cornerImage, 0, 0, cornerImage.getWidth(), cornerImage.getHeight(), 0, x, y, Graphics.TOP | Graphics.LEFT);
            g.drawRegion(cornerImage, 0, 0, cornerImage.getWidth(), cornerImage.getHeight(), 2, x + width, y, Graphics.TOP | Graphics.RIGHT);
            g.drawRegion(cornerImage, 0, 0, cornerImage.getWidth(), cornerImage.getHeight(), 1, x, y + height, Graphics.BOTTOM | Graphics.LEFT);
            g.drawRegion(cornerImage, 0, 0, cornerImage.getWidth(), cornerImage.getHeight(), 3, x + width, y + height, Graphics.BOTTOM | Graphics.RIGHT);
        }
    }

    public static final void b(Graphics g, int fillColor, int borderColor, int x, int y, int width, int height) {
        g.setColor(0x120A06);
        g.fillRect(x, y, width, height);
        g.setColor(0x2A1A10);
        g.fillRect(x, y + 2, width, height - 2);
        g.setColor(0x1C110A);
        g.fillRect(x, y + (height >> 1), width, height - (height >> 1));
        g.setColor(0xB8893D);
        g.fillRect(x, y, width, 1);
        g.setColor(0xF0C975);
        g.fillRect(x, y + 1, width, 1);
    }

    protected void paint(Graphics g) {
        synchronized (ENGINE_LOCK) {
            ScreenController ctrl = this.e;
            boolean shaking = ctrl != null && ctrl.isShaking();
            if (!this.backgroundCleared || shaking || this.lastWasShaking) {
                g.setClip(0, 0, GameConfig.canvasWidth, GameConfig.canvasHeight);
                g.setColor(0);
                g.fillRect(0, 0, GameConfig.canvasWidth, GameConfig.canvasHeight);
                this.backgroundCleared = true;
                this.lastWasShaking = shaking;
            }

            if (ctrl != null) {
                int scale = GameConfig.renderScale > 1 ? GameConfig.renderScale : 1;
                if (shaking) {
                    g.setClip(0, 0, GameConfig.canvasWidth, GameConfig.canvasHeight);
                } else {
                    g.setClip(
                            GameConfig.viewportOffsetX,
                            GameConfig.viewportOffsetY,
                            GameConfig.screenWidth * scale,
                            GameConfig.screenHeight * scale);
                }
                ctrl.a(this.backBufferGraphics, this.backBufferImage, this.presentBufferGraphics, this.presentBufferImage, g);
            }
        }
    }

    protected void updateTick() {
        ++GameConfig.logicTickCounter;
        if (softKeyCooldown > 0) {
            --softKeyCooldown;
        }

        if (vibrateTicks > 0) {
            --vibrateTicks;
            if (vibrateTicks == 0) {
                MGMIDlet.f().a.vibrate(0);
            }
        }

        if (exitCountdownTicks > -1) {
            --exitCountdownTicks;
            if (exitCountdownTicks <= 0) {
                MGMIDlet.f().notifyDestroyed();
            }
        }

        this.e.a();
    }

    public void run() {
        this.stopped = false;
        this.hidden = false;
        long lastTimeMs = System.currentTimeMillis();
        long logicAccumMs = 0L;
        int frameMod3 = 0;

        while (!this.stopped) {
            if (!this.hidden) {
                try {
                    this.frameStartMs = System.currentTimeMillis();
                    long deltaMs = this.frameStartMs - lastTimeMs;
                    lastTimeMs = this.frameStartMs;
                    if (deltaMs < 0L) {
                        deltaMs = 0L;
                    } else if (deltaMs > 120L) {
                        deltaMs = 120L;
                    }

                    if (this.e != null) {
                        synchronized (ENGINE_LOCK) {
                            if (GameConfig.highFpsEnabled) {
                                logicAccumMs += deltaMs;
                                int steps = 0;
                                while (logicAccumMs >= FRAME_INTERVAL_MS && steps < 3) {
                                    logicAccumMs -= FRAME_INTERVAL_MS;
                                    this.updateTick();
                                    ++steps;
                                }
                                int sub = (int) ((logicAccumMs << 8) / FRAME_INTERVAL_MS);
                                GameConfig.renderSubTick256 = sub < 0 ? 0 : (sub > 255 ? 255 : sub);
                                ++GameConfig.visualFrameCounter;
                                this.e.onVisualFrame60Hz();
                            } else {
                                logicAccumMs = 0L;
                                GameConfig.renderSubTick256 = 0;
                                this.updateTick();
                                ++GameConfig.visualFrameCounter;
                            }
                        }
                        this.repaint();
                        this.serviceRepaints();
                    }

                    long targetIntervalMs;
                    if (GameConfig.highFpsEnabled) {
                        targetIntervalMs = (frameMod3 == 2) ? 16L : 17L;
                        frameMod3 = (frameMod3 + 1) % 3;
                    } else {
                        targetIntervalMs = FRAME_INTERVAL_MS;
                    }

                    this.frameEndMs = System.currentTimeMillis();
                    long elapsed = this.frameEndMs - this.frameStartMs;
                    if (elapsed < targetIntervalMs) {
                        Thread.sleep(targetIntervalMs - elapsed);
                    } else {
                        Thread.yield();
                    }
                } catch (Exception error) {
                    DebugLogForm.log((Throwable) error);
                    error.printStackTrace();
                }
            } else {
                synchronized (this.pauseLock) {
                    try {
                        this.pauseLock.wait();
                    } catch (InterruptedException error) {
                        error.printStackTrace();
                    }
                }
                lastTimeMs = System.currentTimeMillis();
            }
        }

        System.out.println("End GUIManager");
    }

    public void a(int durationTicks) {
        if (this.e != null) {
            this.e.c(durationTicks);
        }
    }

    public static void shakeScreen(int durationTicks, int amplitude) {
        if (GameConfig.vibrateEnabled && instance != null && instance.e != null) {
            instance.e.triggerShake(durationTicks, amplitude);
        }
    }

    public static BaseDialog a(String title, String message, String[] buttonLabels, int[] actionIds, int align) {
        return getInstance().createDialog(title, message, buttonLabels, actionIds, align);
    }

    public static BaseDialog a(String title, String message, String buttonLabel, int actionId, int align) {
        return getInstance().createDialog(title, message, new String[]{buttonLabel}, new int[]{actionId}, align);
    }

    public static BaseDialog a(String title, String message, String leftLabel, int leftActionId, String rightLabel, int rightActionId, int align) {
        return getInstance().createDialog(title, message, new String[]{leftLabel, rightLabel}, new int[]{leftActionId, rightActionId}, align);
    }

    public static BaseDialog a(String title, String message, boolean alignLeft) {
        return getInstance().createDialog(title, message, null, null, alignLeft ? 0 : 1);
    }

    public static BaseDialog b(String title, String message, boolean alignLeft) {
        return new MessageDialog(title, message, null, null, alignLeft ? 0 : 1);
    }

    private BaseDialog createDialog(String title, String message, String[] buttonLabels, int[] actionIds, int align) {
        return new MessageDialog(title, message, buttonLabels, actionIds, align);
    }

    public void a(BaseScreen screen, boolean withTransition) {
        this.a(screen, true, withTransition);
    }

    public void a(BaseScreen screen, boolean clearDialogs, boolean withTransition) {
        synchronized (ENGINE_LOCK) {
            clearInput();
            if (this.e != null) {
                this.e.a(screen, clearDialogs, withTransition);
            }
        }
    }

    public void a(BaseDialog dialog) {
        this.a(dialog, false);
    }

    public void a(BaseDialog dialog, boolean modal) {
        synchronized (ENGINE_LOCK) {
            clearInput();
            if (dialog != null) {
                dialog.a(modal);
                if (this.e != null) {
                    this.e.a(dialog);
                }
            }
        }
    }

    public void a(boolean closeAll) {
        synchronized (ENGINE_LOCK) {
            if (this.e != null) {
                this.e.a(closeAll);
            }
        }
    }

    public void e() {
        synchronized (ENGINE_LOCK) {
            if (this.e != null) {
                this.e.e();
            }
        }
    }

    private int mapMotorolaKey(int keyCode) {
        switch (keyCode) {
            case -23: return -8;
            case -22: return -7;
            case -21: return -6;
            case -20: return -5;
            case -6:  return -2;
            case -5:  return -4;
            case -2:  return -3;
            default:  return keyCode;
        }
    }

    private int mapSiemensKey(int keyCode) {
        switch (keyCode) {
            case -62: return -4;
            case -61: return -3;
            case -60: return -2;
            case -59: return -1;
            case -26: return -5;
            case -4:  return -7;
            case -1:  return -6;
            default:  return keyCode;
        }
    }

    private int mapNumPadToArrows(int keyCode) {
        switch (keyCode) {
            case '2': return -1;
            case '4': return -3;
            case '5': return -5;
            case '6': return -4;
            case '8': return -2;
            default:  return keyCode;
        }
    }

    public static void clearInput() {
        for (int i = 0; i < GameConfig.keyStates.length; ++i) {
            GameConfig.keyStates[i] = 0;
        }
        GameConfig.pointerDraggedY = -1;
        GameConfig.pointerDraggedX = -1;
        GameConfig.pointerReleasedY = -1;
        GameConfig.pointerReleasedX = -1;
        GameConfig.pointerPressedY = -1;
        GameConfig.pointerPressedX = -1;
    }

    public int mapBlackBerryKey(int keyCode) {
        if (keyCode == 'q' || keyCode == '#' || keyCode == 'Q') {
            return -6;
        }
        if (keyCode == 'p' || keyCode == '@' || keyCode == 'P') {
            return -7;
        }
        return keyCode;
    }

    public int translateKeyCode(int keyCode) {
        int mapped = -1;
        switch (keyCode) {
            case -11:
                mapped = 20;
                break;
            case -8:
            case 8:
                mapped = 19;
                break;
            case -7:
                if (GameConfig.vendorId == 1) {
                    if (softKeyCooldown == 0) {
                        softKeyCooldown = 5;
                        mapped = 18;
                    }
                } else {
                    mapped = 18;
                }
                break;
            case -6:
                if (GameConfig.vendorId == 1) {
                    if (softKeyCooldown == 0) {
                        softKeyCooldown = 5;
                        mapped = 17;
                    }
                } else {
                    mapped = 17;
                }
                break;
            case -5:
            case 10:
            case 13:
                mapped = 16;
                break;
            case -4:
            case 5:
                mapped = 15;
                break;
            case -3:
            case 2:
                mapped = 14;
                break;
            case -2:
            case 6:
                mapped = 13;
                break;
            case -1:
            case 1:
                mapped = 12;
                break;
            case '#':
                mapped = 11;
                break;
            case '*':
                mapped = 10;
                break;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                mapped = keyCode - '0';
                if (GameConfig.debugMode && keyCode == '9') {
                    MGMIDlet.f().e();
                }
                break;
            default:
                break;
        }
        return mapped;
    }

    public void keyPressed(int keyCode) {
        synchronized (ENGINE_LOCK) {
            if (this.e == null) {
                return;
            }
            if (GameConfig.textInputActive) {
                if ((keyCode >= 'A' && keyCode <= 'Z')
                        || (keyCode >= 'a' && keyCode <= 'z')
                        || (keyCode >= '0' && keyCode <= '9')
                        || keyCode == ' '
                        || keyCode == 8) {
                    GameConfig.lastTypedKey = keyCode;
                    return;
                }
            }
            if (GameConfig.vendorId == 5) {
                keyCode = this.mapBlackBerryKey(keyCode);
            } else if (GameConfig.vendorId == 3) {
                keyCode = this.mapMotorolaKey(keyCode);
            } else if (GameConfig.vendorId == 6) {
                keyCode = this.mapSiemensKey(keyCode);
            }

            if (this.e.b()) {
                keyCode = this.mapNumPadToArrows(keyCode);
            }

            int keyIndex = this.translateKeyCode(keyCode);
            if (keyIndex > -1) {
                if (keyIndex > 16) {
                    GameConfig.keyStates[keyIndex] = 2;
                } else {
                    if (keyIndex == 16 && this.e.d()) {
                        GameConfig.keyStates[keyIndex] = 2;
                        return;
                    }
                    GameConfig.keyStates[keyIndex] = 3;
                    this.e.a(keyIndex);
                }
            }
        }
    }

    public void keyReleased(int keyCode) {
        synchronized (ENGINE_LOCK) {
            if (this.e == null) {
                return;
            }
            if (GameConfig.vendorId == 5) {
                keyCode = this.mapBlackBerryKey(keyCode);
            } else if (GameConfig.vendorId == 3) {
                keyCode = this.mapMotorolaKey(keyCode);
            } else if (GameConfig.vendorId == 6) {
                keyCode = this.mapSiemensKey(keyCode);
            }

            if (this.e.b()) {
                keyCode = this.mapNumPadToArrows(keyCode);
            }

            int keyIndex = this.translateKeyCode(keyCode);
            if (keyIndex > -1) {
                if (GameConfig.keyStates[keyIndex] == 2) {
                    // Preserve unconsumed one-shot softkey/fire trigger until next 60Hz poll
                    return;
                }
                GameConfig.keyStates[keyIndex] = 1;
                if (keyIndex < 17 && this.e != null) {
                    this.e.b(keyIndex);
                }
            }
        }
    }

    public void pointerPressed(int x, int y) {
        synchronized (ENGINE_LOCK) {
            GameConfig.hasPointerEvents = true;
            if (this.e != null) {
                int scale = GameConfig.renderScale > 1 ? GameConfig.renderScale : 1;
                this.e.a((x - GameConfig.viewportOffsetX) / scale, (y - GameConfig.viewportOffsetY) / scale);
            }
        }
    }

    public void pointerDragged(int x, int y) {
        synchronized (ENGINE_LOCK) {
            GameConfig.hasPointerEvents = true;
            if (this.e != null) {
                int scale = GameConfig.renderScale > 1 ? GameConfig.renderScale : 1;
                this.e.b((x - GameConfig.viewportOffsetX) / scale, (y - GameConfig.viewportOffsetY) / scale);
            }
        }
    }

    public void pointerReleased(int x, int y) {
        synchronized (ENGINE_LOCK) {
            if (this.e != null) {
                int scale = GameConfig.renderScale > 1 ? GameConfig.renderScale : 1;
                this.e.c((x - GameConfig.viewportOffsetX) / scale, (y - GameConfig.viewportOffsetY) / scale);
            }
        }
    }

    public void showNotify() {
        this.backgroundCleared = false;
        this.setFullScreenMode(true);
        clearInput();
        this.hidden = false;
        synchronized (this.pauseLock) {
            this.pauseLock.notify();
        }
        if (!this.pauseDialogOpen && this.e != null) {
            this.e.h();
        }
    }

    public void hideNotify() {
        clearInput();
        if (this.e != null) {
            this.e.g();
        }
        this.hidden = true;
        if (!this.pauseDialogOpen && GameConfig.isInGameActive) {
            this.pauseDialogOpen = true;
            MessageDialog pauseDialog = new MessageDialog("Thông tin", "Bấm tiếp tục để chơi tiếp", new String[]{"Tiếp tục"}, new int[]{1}, 1);
            ((Window) pauseDialog).a((ActionListener) this);
            this.a((BaseDialog) pauseDialog, false);
        }
    }

    public static void vibrate(int durationMs) {
        vibrateTicks = (int) ((long) durationMs / FRAME_INTERVAL_MS);
        if ((long) durationMs % FRAME_INTERVAL_MS > 0L) {
            ++vibrateTicks;
        }
        MGMIDlet.f().a.vibrate(durationMs);
    }

    public void onAction(int screenId, int actionId) {
        if (actionId == 1) {
            this.pauseDialogOpen = false;
            if (this.e != null) {
                this.e.h();
            }
            getInstance().a(false);
        } else if (actionId == 3 || actionId == 6) {
            getInstance().a(true);
        } else if (actionId == 2 || actionId == 5) {
            getInstance().a(false);
        } else if (actionId == 4) {
            MGMIDlet.f().d();
        }
    }
}



