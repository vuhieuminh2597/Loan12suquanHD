package com.loan12suquan.hd;

/**
 * Global HD configuration for <em>Loạn 12 Sứ Quân HD</em> (LibGDX Edition).
 * <p>
 * The original game logic operates in a {@code 240x320} coordinate system while all
 * rendering surfaces, vector UI panels, TrueType Vietnamese fonts, 3D faceted gems,
 * and xBRZ-upscaled sprites render natively at {@code 3x} HD ({@code 720x960} playfield,
 * presented inside a {@code 720x960} or {@code 720x1280} responsive LibGDX viewport).
 */
public final class HdConfig {
    public static final int LOGICAL_WIDTH = 240;
    public static final int LOGICAL_HEIGHT = 320;
    public static final int HD_SCALE = 3;
    public static final int PLAYFIELD_HD_WIDTH = LOGICAL_WIDTH * HD_SCALE;   // 720
    public static final int PLAYFIELD_HD_HEIGHT = LOGICAL_HEIGHT * HD_SCALE; // 960

    /** Default desktop window dimensions (Vertical HD 720x960, scalable to 720x1280 / Fullscreen). */
    public static final int WINDOW_DEFAULT_WIDTH = 720;
    public static final int WINDOW_DEFAULT_HEIGHT = 960;

    /**
     * Visual quality presets cycled via the Main Menu badge or {@code F2}:
     * <ul>
     *   <li>{@code 0}: <b>60 FPS | HD Remaster</b> (TrueType Vietnamese HD Font + 3D Faceted HD Gems + Vector Antique UI + xBRZ 3x Sprites + OpenGL Particles)</li>
     *   <li>{@code 1}: <b>60 FPS | HD Pixel</b> (Crisp xBRZ 3x Pixel Font + xBRZ 3x Original Gems & Sprites + OpenGL Particles)</li>
     *   <li>{@code 2}: <b>60 FPS | Cổ điển</b> (Classic 1:1 Nearest-Neighbor Pixel Art nostalgia mode)</li>
     * </ul>
     */
    public static final int MODE_HD_REMASTER = 0;
    public static final int MODE_HD_PIXEL = 1;
    public static final int MODE_CLASSIC_PIXEL = 2;

    public static volatile int visualMode = MODE_HD_REMASTER;
    public static volatile boolean particlesEnabled = true;

    private HdConfig() {
    }

    public static boolean isHdRemaster() {
        return visualMode == MODE_HD_REMASTER;
    }

    public static boolean isHdUpscaleActive() {
        return visualMode != MODE_CLASSIC_PIXEL;
    }

    public static String getModeBadgeLabel() {
        switch (visualMode) {
            case MODE_HD_REMASTER:
                return "60FPS | HD Mới";
            case MODE_HD_PIXEL:
                return "60FPS | HD Pixel";
            default:
                return "60FPS | Cổ điển";
        }
    }

    public static void cycleVisualMode() {
        visualMode = (visualMode + 1) % 3;
    }
}
