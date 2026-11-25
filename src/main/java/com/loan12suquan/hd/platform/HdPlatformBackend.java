package com.loan12suquan.hd.platform;

import java.io.IOException;
import java.io.InputStream;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

/**
 * Host platform services abstraction (Desktop JVM/LibGDX vs Web/iPhone 17 TeaVM PWA).
 */
public interface HdPlatformBackend {
    boolean isWeb();

    HdSurface createSurface(int width, int height, boolean fillWhite);

    HdSurface createSurfaceFromArgb(int[] argb, int width, int height);

    HdSurface decodeImageBytes(byte[] bytes, int offset, int length);

    InputStream openResourceStream(String resourcePath);

    byte[] loadRecordStoreBytes(String storeName);

    void saveRecordStoreBytes(String storeName, byte[] data);

    void deleteRecordStoreBytes(String storeName);

    Player createMediaPlayer(InputStream stream, String mimeType, String clipHint) throws IOException, MediaException;

    void scheduleDelayed(int delayMs, Runnable task);

    void requestExit();
}
