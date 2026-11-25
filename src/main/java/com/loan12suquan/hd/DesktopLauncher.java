package com.loan12suquan.hd;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.loan12suquan.hd.desktop.DesktopPlatformBackend;

/**
 * Desktop (LWJGL3 / OpenGL) entry point for <em>Loạn 12 Sứ Quân HD</em>.
 * <p>
 * Launch directly from IntelliJ IDEA via the <b>Chạy Loạn 12 Sứ Quân HD (LibGDX)</b>
 * run configuration or via {@code gradlew.bat run}.
 */
public final class DesktopLauncher {
    private DesktopLauncher() {
    }

    public static void main(String[] args) {
        DesktopPlatformBackend.installIfNeeded();
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Loạn 12 Sứ Quân HD — Cổ Phong Hoàng Kim (LibGDX Edition)");
        config.setWindowedMode(HdConfig.WINDOW_DEFAULT_WIDTH, HdConfig.WINDOW_DEFAULT_HEIGHT);
        config.setWindowSizeLimits(360, 480, -1, -1);
        config.setForegroundFPS(60);
        config.setIdleFPS(30);
        config.useVsync(true);
        config.setResizable(true);
        try {
            config.setWindowIcon(Files.FileType.Classpath, "icon.png");
        } catch (Throwable ignored) {
        }
        new Lwjgl3Application(new Loan12SuQuanHdGame(), config);
    }
}
