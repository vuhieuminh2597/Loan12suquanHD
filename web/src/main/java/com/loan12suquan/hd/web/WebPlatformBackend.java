package com.loan12suquan.hd.web;

import com.loan12suquan.hd.platform.HdPlatformBackend;
import com.loan12suquan.hd.platform.HdSurface;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import org.teavm.jso.JSObject;
import org.teavm.jso.typedarrays.Int8Array;

/**
 * HTML5 / iPhone 17 PWA implementation of {@link HdPlatformBackend} using zero-copy TypedArrays.
 */
public final class WebPlatformBackend implements HdPlatformBackend {
    private static final String IMG_TOKEN_PREFIX = "SQIMG:";

    public static byte[] toJavaBytes(Int8Array jsBytes) {
        if (jsBytes == null) {
            return null;
        }
        int len = jsBytes.getLength();
        byte[] out = new byte[len];
        for (int i = 0; i < len; ++i) {
            out[i] = jsBytes.get(i);
        }
        return out;
    }

    public static Int8Array toJsBytes(byte[] data) {
        if (data == null) {
            return null;
        }
        Int8Array out = Int8Array.create(data.length);
        for (int i = 0; i < data.length; ++i) {
            out.set(i, data[i]);
        }
        return out;
    }

    @Override
    public boolean isWeb() {
        return true;
    }

    @Override
    public HdSurface createSurface(int width, int height, boolean fillWhite) {
        return WebSurface.create(width, height, fillWhite);
    }

    @Override
    public HdSurface createSurfaceFromArgb(int[] argb, int width, int height) {
        return WebSurface.createFromArgb(argb, width, height);
    }

    @Override
    public HdSurface decodeImageBytes(byte[] bytes, int offset, int length) {
        if (bytes != null && length > IMG_TOKEN_PREFIX.length()) {
            boolean matchesToken = true;
            for (int i = 0; i < IMG_TOKEN_PREFIX.length(); ++i) {
                if ((bytes[offset + i] & 0xFF) != IMG_TOKEN_PREFIX.charAt(i)) {
                    matchesToken = false;
                    break;
                }
            }
            if (matchesToken) {
                String path = new String(
                        bytes,
                        offset + IMG_TOKEN_PREFIX.length(),
                        length - IMG_TOKEN_PREFIX.length(),
                        StandardCharsets.UTF_8);
                JSObject preloaded = WebDomBridge.getPreloadedImageCanvas(path);
                if (preloaded != null) {
                    return WebSurface.wrapPreloadedCanvas(preloaded);
                }
            }
        }
        return WebSurface.create(1, 1, false);
    }

    @Override
    public InputStream openResourceStream(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            return null;
        }
        String norm = resourcePath.startsWith("/") ? resourcePath : ("/" + resourcePath);
        if (WebDomBridge.hasPreloadedImage(norm)) {
            byte[] token = (IMG_TOKEN_PREFIX + norm).getBytes(StandardCharsets.UTF_8);
            return new ByteArrayInputStream(token);
        }
        Int8Array bin = WebDomBridge.getPreloadedBinaryTyped(norm);
        if (bin != null) {
            return new ByteArrayInputStream(toJavaBytes(bin));
        }
        return null;
    }

    @Override
    public byte[] loadRecordStoreBytes(String storeName) {
        String safe = storeName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return toJavaBytes(WebDomBridge.loadLocalStorageBytesTyped(safe));
    }

    @Override
    public void saveRecordStoreBytes(String storeName, byte[] data) {
        String safe = storeName.replaceAll("[^a-zA-Z0-9._-]", "_");
        WebDomBridge.saveLocalStorageBytesTyped(safe, toJsBytes(data));
    }

    @Override
    public void deleteRecordStoreBytes(String storeName) {
        String safe = storeName.replaceAll("[^a-zA-Z0-9._-]", "_");
        WebDomBridge.deleteLocalStorageBytes(safe);
    }

    @Override
    public Player createMediaPlayer(InputStream stream, String mimeType, String clipHint) throws IOException, MediaException {
        return WebMediaPlayer.create(stream, mimeType, clipHint);
    }

    @Override
    public void scheduleDelayed(int delayMs, final Runnable task) {
        WebDomBridge.setTimeout(Math.max(0, delayMs), new WebDomBridge.JsRunnable() {
            @Override
            public void run() {
                task.run();
            }
        });
    }

    @Override
    public void requestExit() {
    }
}
