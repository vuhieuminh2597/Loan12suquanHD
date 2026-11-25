package com.mg.engine.core;

/**
 * Global runtime configuration, viewport geometry, input state buffers,
 * audio preferences, and platform flags shared across the MicroGame engine.
 */
public class GameConfig {
    // Viewport & display metrics
    public static boolean hasLetterbox = false;
    public static int screenWidth;
    public static int screenHeight;
    public static int canvasWidth;
    public static int canvasHeight;
    public static int viewportOffsetX = 0;
    public static int viewportOffsetY = 0;
    public static int contentOffsetX;
    public static int contentOffsetY;
    public static boolean isExact240x320 = false;
    public static int renderScale = 1;
    public static boolean highFpsEnabled = true;
    public static boolean hdUpscaleEnabled = true;
    public static int logicTickCounter = 0;
    public static int visualFrameCounter = 0;
    public static int renderSubTick256 = 0;

    // Input & pointer state
    public static boolean hasPointerEvents = false;
    public static int pointerPressedX = -1;
    public static int pointerPressedY = -1;
    public static int pointerReleasedX = -1;
    public static int pointerReleasedY = -1;
    public static int pointerDraggedX = -1;
    public static int pointerDraggedY = -1;
    public static int[] keyStates = new int[21];
    public static boolean textInputActive = false;
    public static int lastTypedKey = 0;

    // Device vendor detection (0=Nokia, 1=NokiaN, 2=Sony, 3=Moto, 4=Other, 5=RIM, 6=Siemens)
    public static int vendorId = 0;
    public static boolean isOldSonyEricsson = false;
    public static boolean isNokiaSeries40 = false;

    // Audio & haptics
    public static boolean vibrateEnabled = true;
    public static boolean soundEnabled = true;
    public static int soundVolume = 80;
    public static int defaultMidiTempo = 100;
    public static boolean sfxEnabled = true;

    // Session & persistence flags
    public static boolean rmsPersistenceEnabled = true;
    public static boolean debugMode = false;
    public static int playCount = 0;
    public static int maxStoredRecords = 10;
    public static int defaultExtraLives = 4;
    public static boolean mainMenuReady = false;
    public static boolean isInGameActive = false;
    public static boolean battleFastMode = false;

    // UI theme colors & resource extension
    public static int themeColor;
    public static int themeHighlightColor;
    public static int frameBorderColor = 0;
    public static int frameBackgroundColor = 0x22150D;
    public static String imageExtension = ".mg";

    static {
        setThemeColor(frameBackgroundColor);
    }

    public static void clearKeyStates() {
        for (int i = 0; i < keyStates.length; ++i) {
            keyStates[i] = 0;
        }
    }

    public static void setThemeColor(int rgb) {
        themeColor = rgb;
        int r = (rgb & 0xFF0000) >> 16;
        int g = (rgb & 0x00FF00) >> 8;
        int b = rgb & 0x0000FF;
        int blendDivisor = 6;
        themeHighlightColor = ((255 - r) / blendDivisor + r) << 16
                | ((255 - g) / blendDivisor + g) << 8
                | ((255 - b) / blendDivisor + b);
    }
}
