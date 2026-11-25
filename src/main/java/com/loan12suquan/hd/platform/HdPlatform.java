package com.loan12suquan.hd.platform;

/**
 * Global registry for the active {@link HdPlatformBackend}.
 */
public final class HdPlatform {
    private static HdPlatformBackend activeBackend;

    private HdPlatform() {
    }

    public static void setBackend(HdPlatformBackend backend) {
        activeBackend = backend;
    }

    public static HdPlatformBackend backend() {
        if (activeBackend == null) {
            throw new IllegalStateException("HdPlatformBackend has not been initialized!");
        }
        return activeBackend;
    }

    public static boolean isInitialized() {
        return activeBackend != null;
    }

    public static boolean isWeb() {
        return activeBackend != null && activeBackend.isWeb();
    }

    public static int argb(int hexRgb, int alpha) {
        int a = alpha < 0 ? 0 : (alpha > 255 ? 255 : alpha);
        return (a << 24) | (hexRgb & 0x00FFFFFF);
    }

    public static int opaque(int hexRgb) {
        return 0xFF000000 | (hexRgb & 0x00FFFFFF);
    }
}
