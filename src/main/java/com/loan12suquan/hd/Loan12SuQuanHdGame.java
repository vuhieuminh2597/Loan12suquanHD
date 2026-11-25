package com.loan12suquan.hd;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.loan12suquan.hd.desktop.DesktopPlatformBackend;
import com.loan12suquan.hd.desktop.DesktopSurface;
import com.loan12suquan.hd.platform.HdCanvas2D;
import com.mg.engine.audio.BgmPlayer;
import com.mg.engine.core.DebugLogForm;
import com.mg.engine.core.GUIManager;
import com.mg.engine.core.GameConfig;
import com.mg.engine.storage.EngineStorage;
import com.mic.smsgame.suquan.SQMidlet;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Random;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

/**
 * Main LibGDX OpenGL application host for <em>Loạn 12 Sứ Quân HD</em>.
 * <p>
 * Drives the {@code 720x960} ({@code 3x} HD) playfield texture pipeline, hardware-accelerated
 * 60 FPS OpenGL golden ember & touch-sparkle particle overlay, responsive {@link FitViewport},
 * and mouse/touch/keyboard input routing.
 */
public class Loan12SuQuanHdGame extends ApplicationAdapter {
    private static final int EMBER_COUNT = 38;
    private static final int TOUCH_SPARK_COUNT = 36;

    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;

    private DesktopSurface screenHdSurface;
    private Graphics screenGraphics;
    private int[] argbBuffer;
    private int[] rgbaBuffer;
    private Pixmap screenPixmap;
    private IntBuffer pixmapIntBuffer;
    private Texture screenTexture;
    private Texture glowParticleTexture;
    private Texture whitePixelTexture;
    private Texture hotkeyLegendTexture;
    private OrthographicCamera windowCamera;

    private SQMidlet midlet;
    private int lastVisualMode = -1;

    // 60 FPS OpenGL ambient golden embers + interactive touch burst particles
    private final float[] emberX = new float[EMBER_COUNT];
    private final float[] emberY = new float[EMBER_COUNT];
    private final float[] emberVx = new float[EMBER_COUNT];
    private final float[] emberVy = new float[EMBER_COUNT];
    private final float[] emberSize = new float[EMBER_COUNT];
    private final float[] emberAlpha = new float[EMBER_COUNT];
    private final float[] emberPhase = new float[EMBER_COUNT];

    private final float[] sparkX = new float[TOUCH_SPARK_COUNT];
    private final float[] sparkY = new float[TOUCH_SPARK_COUNT];
    private final float[] sparkVx = new float[TOUCH_SPARK_COUNT];
    private final float[] sparkVy = new float[TOUCH_SPARK_COUNT];
    private final float[] sparkLife = new float[TOUCH_SPARK_COUNT];
    private final float[] sparkMaxLife = new float[TOUCH_SPARK_COUNT];
    private int nextSparkIdx = 0;
    private final Random rng = new Random();
    private final Vector2 touchVec = new Vector2();

    @Override
    public void create() {
        DesktopPlatformBackend.installIfNeeded();
        final int hdW = HdConfig.PLAYFIELD_HD_WIDTH;
        final int hdH = HdConfig.PLAYFIELD_HD_HEIGHT;

        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(hdW, hdH, this.camera);
        this.viewport.apply(true);
        this.windowCamera = new OrthographicCamera();
        this.windowCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.batch = new SpriteBatch();

        this.screenHdSurface = DesktopSurface.create(hdW, hdH, false);
        this.screenGraphics = new Graphics(this.screenHdSurface, HdConfig.LOGICAL_WIDTH, HdConfig.LOGICAL_HEIGHT);
        this.argbBuffer = new int[hdW * hdH];
        this.rgbaBuffer = new int[hdW * hdH];

        this.screenPixmap = new Pixmap(hdW, hdH, Pixmap.Format.RGBA8888);
        ByteBuffer bb = this.screenPixmap.getPixels();
        bb.order(ByteOrder.BIG_ENDIAN);
        this.pixmapIntBuffer = bb.asIntBuffer();

        this.screenTexture = new Texture(this.screenPixmap);
        this.screenTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        this.glowParticleTexture = createRadialGlowTexture(32);
        this.whitePixelTexture = createWhitePixelTexture();
        this.hotkeyLegendTexture = createHotkeyLegendTexture();

        initAmbientEmbers();
        installInputProcessor();

        // Drive game logic & rendering sequentially on the LibGDX main thread (zero multi-thread race conditions)
        GUIManager.externalRenderLoop = true;

        // Boot the game MIDlet
        this.midlet = new SQMidlet();
        this.midlet.invokeStartApp();
    }

    private Texture createRadialGlowTexture(int size) {
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float center = (size - 1) * 0.5f;
        float radius = size * 0.5f;
        for (int y = 0; y < size; ++y) {
            for (int x = 0; x < size; ++x) {
                float dx = (x - center) / radius;
                float dy = (y - center) / radius;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < 1.0f) {
                    float falloff = (1.0f - dist);
                    falloff = falloff * falloff;
                    int a = Math.min(255, Math.max(0, (int) (falloff * 255f)));
                    p.drawPixel(x, y, 0xFFFFFF00 | a);
                } else {
                    p.drawPixel(x, y, 0);
                }
            }
        }
        Texture tex = new Texture(p);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        p.dispose();
        return tex;
    }

    private Texture createWhitePixelTexture() {
        Pixmap p = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        p.setColor(1f, 1f, 1f, 1f);
        p.fill();
        Texture tex = new Texture(p);
        p.dispose();
        return tex;
    }

    private Texture createHotkeyLegendTexture() {
        final int w = 720;
        final int h = 34;
        DesktopSurface img = DesktopSurface.create(w, h, false);
        HdCanvas2D g2 = img.getCanvas2D();
        g2.setColorRgba(18, 10, 6, 220);
        g2.fillRoundRect(4, 3, w - 8, h - 6, 12);
        g2.setColorHexAlpha(0xC89842, 165);
        g2.setStrokeWidth(1.5f);
        g2.drawRoundRect(4, 3, w - 8, h - 6, 12);
        HdFontRenderer.drawSubtitleHd(
                g2,
                "F2: Chế độ HD  |  F3: Âm lượng  |  F4: Hạt sáng  |  F11: Toàn màn hình  |  Chuột phải: Trở về",
                w / 2, 23, true, 0xE8D5B5, 0x0A0503);

        int[] argb = new int[w * h];
        img.getArgb(0, 0, w, h, argb, 0, w);
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        ByteBuffer bb = pm.getPixels();
        bb.order(ByteOrder.BIG_ENDIAN);
        IntBuffer ib = bb.asIntBuffer();
        for (int i = 0; i < argb.length; ++i) {
            int c = argb[i];
            ib.put((c << 8) | ((c >>> 24) & 0xFF));
        }
        ib.flip();
        Texture tex = new Texture(pm);
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        pm.dispose();
        return tex;
    }

    private void initAmbientEmbers() {
        for (int i = 0; i < EMBER_COUNT; ++i) {
            resetEmber(i, true);
        }
    }

    private void resetEmber(int i, boolean randomY) {
        this.emberX[i] = this.rng.nextFloat() * HdConfig.PLAYFIELD_HD_WIDTH;
        this.emberY[i] = randomY ? (this.rng.nextFloat() * HdConfig.PLAYFIELD_HD_HEIGHT) : -10f;
        this.emberVx[i] = (this.rng.nextFloat() - 0.5f) * 18f;
        this.emberVy[i] = 16f + this.rng.nextFloat() * 28f;
        this.emberSize[i] = 8f + this.rng.nextFloat() * 14f;
        this.emberAlpha[i] = 0.14f + this.rng.nextFloat() * 0.24f;
        this.emberPhase[i] = this.rng.nextFloat() * 6.28f;
    }

    private void spawnTouchBurst(float hdX, float hdY) {
        if (!HdConfig.particlesEnabled) {
            return;
        }
        // Convert top-down HD Y to bottom-up OpenGL viewport Y
        float glY = HdConfig.PLAYFIELD_HD_HEIGHT - hdY;
        for (int k = 0; k < 10; ++k) {
            int idx = this.nextSparkIdx;
            this.nextSparkIdx = (this.nextSparkIdx + 1) % TOUCH_SPARK_COUNT;
            float angle = this.rng.nextFloat() * 6.283185f;
            float speed = 35f + this.rng.nextFloat() * 110f;
            this.sparkX[idx] = hdX;
            this.sparkY[idx] = glY;
            this.sparkVx[idx] = (float) Math.cos(angle) * speed;
            this.sparkVy[idx] = (float) Math.sin(angle) * speed;
            this.sparkMaxLife[idx] = 0.25f + this.rng.nextFloat() * 0.20f;
            this.sparkLife[idx] = this.sparkMaxLife[idx];
        }
    }

    @Override
    public void render() {
        float dt = Math.min(0.05f, Gdx.graphics.getDeltaTime());

        // Step logic and repaint the active game Canvas sequentially on the OpenGL main thread
        Displayable current = Display.getActiveDisplay().getCurrent();
        if (current instanceof Canvas) {
            try {
                if (current instanceof GUIManager) {
                    ((GUIManager) current).stepExternalFrame((long) (dt * 1000f));
                }
                ((Canvas) current).invokePaint(this.screenGraphics);
            } catch (Throwable err) {
                DebugLogForm.log(err);
                err.printStackTrace();
            }
        }

        // Update texture filter if visual preset changed
        if (this.lastVisualMode != HdConfig.visualMode) {
            this.lastVisualMode = HdConfig.visualMode;
            TextureFilter filter = (HdConfig.visualMode == HdConfig.MODE_CLASSIC_PIXEL)
                    ? TextureFilter.Nearest
                    : TextureFilter.Linear;
            this.screenTexture.setFilter(filter, filter);
        }

        // Fast ARGB -> RGBA8888 conversion & upload to GPU texture
        final int hdW = HdConfig.PLAYFIELD_HD_WIDTH;
        final int hdH = HdConfig.PLAYFIELD_HD_HEIGHT;
        this.screenHdSurface.getArgb(0, 0, hdW, hdH, this.argbBuffer, 0, hdW);
        final int len = this.argbBuffer.length;
        for (int i = 0; i < len; ++i) {
            int argb = this.argbBuffer[i];
            this.rgbaBuffer[i] = (argb << 8) | ((argb >>> 24) & 0xFF);
        }
        this.pixmapIntBuffer.clear();
        this.pixmapIntBuffer.put(this.rgbaBuffer);
        this.pixmapIntBuffer.flip();
        this.screenTexture.draw(this.screenPixmap, 0, 0);

        // Clear letterbox background with deep antique mahogany
        Gdx.gl.glClearColor(0.07f, 0.04f, 0.025f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        this.viewport.apply();
        this.batch.setProjectionMatrix(this.camera.combined);
        this.batch.begin();
        this.batch.setColor(1f, 1f, 1f, 1f);
        this.batch.draw(this.screenTexture, 0, 0, hdW, hdH);

        // Render 60 FPS OpenGL additive golden particles & touch sparks
        if (HdConfig.particlesEnabled && HdConfig.visualMode != HdConfig.MODE_CLASSIC_PIXEL) {
            renderOpenGlParticles(dt);
        }
        this.batch.end();

        // Render ornate golden frame & optional margin hotkey legend when window has pillarbox/letterbox space
        renderOuterWindowFrame();
    }

    private void renderOuterWindowFrame() {
        int winW = Gdx.graphics.getBackBufferWidth();
        int winH = Gdx.graphics.getBackBufferHeight();
        int vpX = this.viewport.getScreenX();
        int vpY = this.viewport.getScreenY();
        int vpW = this.viewport.getScreenWidth();
        int vpH = this.viewport.getScreenHeight();
        if (vpX <= 2 && vpY <= 2) {
            return;
        }
        Gdx.gl.glViewport(0, 0, winW, winH);
        this.windowCamera.setToOrtho(false, winW, winH);
        this.batch.setProjectionMatrix(this.windowCamera.combined);
        this.batch.begin();

        // Carved burnished gold trim framing the 720x960 playfield
        this.batch.setColor(0.82f, 0.63f, 0.28f, 0.85f);
        this.batch.draw(this.whitePixelTexture, vpX - 3, vpY - 3, vpW + 6, 3);
        this.batch.draw(this.whitePixelTexture, vpX - 3, vpY + vpH, vpW + 6, 3);
        this.batch.draw(this.whitePixelTexture, vpX - 3, vpY - 3, 3, vpH + 6);
        this.batch.draw(this.whitePixelTexture, vpX + vpW, vpY - 3, 3, vpH + 6);

        // If bottom letterbox margin is >= 36px (e.g. 720x1280 tall mode), show crisp hotkey legend bar
        if (vpY >= 36 && this.hotkeyLegendTexture != null) {
            float lw = Math.min(vpW, 720f);
            float lh = lw * (34f / 720f);
            float lx = vpX + (vpW - lw) * 0.5f;
            float ly = (vpY - lh) * 0.5f;
            this.batch.setColor(1f, 1f, 1f, 0.92f);
            this.batch.draw(this.hotkeyLegendTexture, lx, ly, lw, lh);
        }
        this.batch.setColor(1f, 1f, 1f, 1f);
        this.batch.end();
    }

    private void renderOpenGlParticles(float dt) {
        this.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        // 1. Subtle floating golden embers
        for (int i = 0; i < EMBER_COUNT; ++i) {
            this.emberPhase[i] += dt * 2.4f;
            this.emberX[i] += (this.emberVx[i] + (float) Math.sin(this.emberPhase[i]) * 10f) * dt;
            this.emberY[i] += this.emberVy[i] * dt;
            if (this.emberY[i] > HdConfig.PLAYFIELD_HD_HEIGHT + 16f) {
                resetEmber(i, false);
            }
            float pulse = 0.65f + 0.35f * (float) Math.sin(this.emberPhase[i]);
            float alpha = this.emberAlpha[i] * pulse;
            float sz = this.emberSize[i];
            this.batch.setColor(1.0f, 0.82f, 0.38f, alpha);
            this.batch.draw(this.glowParticleTexture, this.emberX[i] - sz * 0.5f, this.emberY[i] - sz * 0.5f, sz, sz);
        }

        // 2. Interactive touch/click golden spark burst
        for (int i = 0; i < TOUCH_SPARK_COUNT; ++i) {
            if (this.sparkLife[i] > 0f) {
                this.sparkLife[i] -= dt;
                if (this.sparkLife[i] > 0f) {
                    this.sparkX[i] += this.sparkVx[i] * dt;
                    this.sparkY[i] += this.sparkVy[i] * dt;
                    this.sparkVy[i] -= 180f * dt;
                    float ratio = this.sparkLife[i] / this.sparkMaxLife[i];
                    float sz = 10f + 12f * ratio;
                    this.batch.setColor(1.0f, 0.90f, 0.50f, ratio * 0.85f);
                    this.batch.draw(this.glowParticleTexture, this.sparkX[i] - sz * 0.5f, this.sparkY[i] - sz * 0.5f, sz, sz);
                }
            }
        }

        this.batch.setColor(1f, 1f, 1f, 1f);
        this.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void resize(int width, int height) {
        if (this.viewport != null) {
            this.viewport.update(width, height, true);
        }
    }

    @Override
    public void dispose() {
        if (this.midlet != null) {
            try {
                this.midlet.invokeDestroyApp(true);
            } catch (Throwable ignored) {
            }
        }
        if (this.batch != null) {
            this.batch.dispose();
        }
        if (this.screenTexture != null) {
            this.screenTexture.dispose();
        }
        if (this.glowParticleTexture != null) {
            this.glowParticleTexture.dispose();
        }
        if (this.whitePixelTexture != null) {
            this.whitePixelTexture.dispose();
        }
        if (this.hotkeyLegendTexture != null) {
            this.hotkeyLegendTexture.dispose();
        }
        if (this.screenPixmap != null) {
            this.screenPixmap.dispose();
        }
    }

    private void installInputProcessor() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            private int toLogicalX(int screenX, int screenY) {
                touchVec.set(screenX, screenY);
                viewport.unproject(touchVec);
                int lx = (int) (touchVec.x / HdConfig.HD_SCALE);
                return Math.max(0, Math.min(HdConfig.LOGICAL_WIDTH - 1, lx));
            }

            private int toLogicalY(int screenX, int screenY) {
                touchVec.set(screenX, screenY);
                viewport.unproject(touchVec);
                // FitViewport unprojects Y in bottom-up OpenGL coords; convert to top-down MIDP coords
                float topDownHdY = HdConfig.PLAYFIELD_HD_HEIGHT - touchVec.y;
                int ly = (int) (topDownHdY / HdConfig.HD_SCALE);
                return Math.max(0, Math.min(HdConfig.LOGICAL_HEIGHT - 1, ly));
            }

            private void updateHoverFromScreen(int screenX, int screenY) {
                touchVec.set(screenX, screenY);
                viewport.unproject(touchVec);
                float topDownHdY = HdConfig.PLAYFIELD_HD_HEIGHT - touchVec.y;
                if (touchVec.x < 0f || touchVec.x >= HdConfig.PLAYFIELD_HD_WIDTH
                        || topDownHdY < 0f || topDownHdY >= HdConfig.PLAYFIELD_HD_HEIGHT) {
                    HdInputState.clearHover();
                    return;
                }
                int lx = (int) (touchVec.x / HdConfig.HD_SCALE);
                int ly = (int) (topDownHdY / HdConfig.HD_SCALE);
                HdInputState.updateHover(lx, ly);
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                updateHoverFromScreen(screenX, screenY);
                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                if (amountY != 0f) {
                    HdInputState.addWheelDelta(amountY > 0f ? 1 : -1);
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                updateHoverFromScreen(screenX, screenY);
                if (button == Input.Buttons.RIGHT) {
                    Displayable cur = Display.getActiveDisplay().getCurrent();
                    if (cur instanceof Canvas) {
                        ((Canvas) cur).keyPressed(-7);
                    }
                    return true;
                }
                int lx = toLogicalX(screenX, screenY);
                int ly = toLogicalY(screenX, screenY);
                spawnTouchBurst(lx * HdConfig.HD_SCALE, ly * HdConfig.HD_SCALE);
                Displayable cur = Display.getActiveDisplay().getCurrent();
                if (cur instanceof Canvas) {
                    ((Canvas) cur).pointerPressed(lx, ly);
                }
                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                updateHoverFromScreen(screenX, screenY);
                int lx = toLogicalX(screenX, screenY);
                int ly = toLogicalY(screenX, screenY);
                Displayable cur = Display.getActiveDisplay().getCurrent();
                if (cur instanceof Canvas) {
                    ((Canvas) cur).pointerDragged(lx, ly);
                }
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                int lx = toLogicalX(screenX, screenY);
                int ly = toLogicalY(screenX, screenY);
                Displayable cur = Display.getActiveDisplay().getCurrent();
                if (cur instanceof Canvas) {
                    ((Canvas) cur).pointerReleased(lx, ly);
                }
                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                // Global HD hotkeys: F2 = cycle HD mode, F3 = cycle volume, F4 = toggle particles, F11 = toggle Fullscreen
                if (keycode == Input.Keys.F2) {
                    HdConfig.cycleVisualMode();
                    GameConfig.highFpsEnabled = true;
                    GameConfig.hdUpscaleEnabled = HdConfig.isHdUpscaleActive();
                    EngineStorage.saveDisplaySettings();
                    return true;
                }
                if (keycode == Input.Keys.F3) {
                    BgmPlayer.getInstance().cycleLiveVolumePreset();
                    return true;
                }
                if (keycode == Input.Keys.F4) {
                    HdConfig.particlesEnabled = !HdConfig.particlesEnabled;
                    return true;
                }
                if (keycode == Input.Keys.F11) {
                    if (Gdx.graphics.isFullscreen()) {
                        Gdx.graphics.setWindowedMode(HdConfig.WINDOW_DEFAULT_WIDTH, HdConfig.WINDOW_DEFAULT_HEIGHT);
                    } else {
                        DisplayMode mode = Gdx.graphics.getDisplayMode();
                        Gdx.graphics.setFullscreenMode(mode);
                    }
                    return true;
                }

                // When naming hero on CharacterSelectScreen, let keyTyped handle alphanumeric keys
                if (GameConfig.textInputActive) {
                    if (keycode == Input.Keys.BACKSPACE || keycode == Input.Keys.DEL) {
                        Displayable cur = Display.getActiveDisplay().getCurrent();
                        if (cur instanceof Canvas) {
                            ((Canvas) cur).keyPressed(8);
                        }
                        return true;
                    }
                    if ((keycode >= Input.Keys.A && keycode <= Input.Keys.Z)
                            || (keycode >= Input.Keys.NUM_0 && keycode <= Input.Keys.NUM_9)
                            || keycode == Input.Keys.SPACE) {
                        return false;
                    }
                }

                int midpKey = mapGdxKeyToMidp(keycode);
                if (midpKey != 0) {
                    Displayable cur = Display.getActiveDisplay().getCurrent();
                    if (cur instanceof Canvas) {
                        ((Canvas) cur).keyPressed(midpKey);
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyUp(int keycode) {
                int midpKey = mapGdxKeyToMidp(keycode);
                if (midpKey != 0) {
                    Displayable cur = Display.getActiveDisplay().getCurrent();
                    if (cur instanceof Canvas) {
                        ((Canvas) cur).keyReleased(midpKey);
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                if (GameConfig.textInputActive) {
                    if ((character >= 'A' && character <= 'Z')
                            || (character >= 'a' && character <= 'z')
                            || (character >= '0' && character <= '9')
                            || character == ' ') {
                        Displayable cur = Display.getActiveDisplay().getCurrent();
                        if (cur instanceof Canvas) {
                            ((Canvas) cur).keyPressed(character);
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private static int mapGdxKeyToMidp(int keycode) {
        switch (keycode) {
            case Input.Keys.UP:
            case Input.Keys.W:
                return -1;
            case Input.Keys.DOWN:
            case Input.Keys.S:
                return -2;
            case Input.Keys.LEFT:
            case Input.Keys.A:
                return -3;
            case Input.Keys.RIGHT:
            case Input.Keys.D:
                return -4;
            case Input.Keys.ENTER:
            case Input.Keys.SPACE:
            case Input.Keys.NUMPAD_ENTER:
                return -5;
            case Input.Keys.Q:
            case Input.Keys.F1:
            case Input.Keys.TAB:
                return -6; // Left SoftKey (Menu / T.Chiêu)
            case Input.Keys.E:
            case Input.Keys.ESCAPE:
            case Input.Keys.BACKSPACE:
                return -7; // Right SoftKey (Back / Đóng / T.Dừng)
            case Input.Keys.NUM_0:
            case Input.Keys.NUMPAD_0:
                return '0';
            case Input.Keys.NUM_1:
            case Input.Keys.NUMPAD_1:
                return '1';
            case Input.Keys.NUM_2:
            case Input.Keys.NUMPAD_2:
                return '2';
            case Input.Keys.NUM_3:
            case Input.Keys.NUMPAD_3:
                return '3';
            case Input.Keys.NUM_4:
            case Input.Keys.NUMPAD_4:
                return '4';
            case Input.Keys.NUM_5:
            case Input.Keys.NUMPAD_5:
                return '5';
            case Input.Keys.NUM_6:
            case Input.Keys.NUMPAD_6:
                return '6';
            case Input.Keys.NUM_7:
            case Input.Keys.NUMPAD_7:
                return '7';
            case Input.Keys.NUM_8:
            case Input.Keys.NUMPAD_8:
                return '8';
            case Input.Keys.NUM_9:
            case Input.Keys.NUMPAD_9:
                return '9';
            case Input.Keys.STAR:
            case Input.Keys.MINUS:
                return '*';
            case Input.Keys.POUND:
            case Input.Keys.EQUALS:
            case Input.Keys.SLASH:
                return '#';
            default:
                return 0;
        }
    }
}
