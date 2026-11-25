package com.loan12suquan.hd.desktop;

import com.badlogic.gdx.Gdx;
import com.loan12suquan.hd.platform.HdPlatform;
import com.loan12suquan.hd.platform.HdPlatformBackend;
import com.loan12suquan.hd.platform.HdSurface;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

public final class DesktopPlatformBackend implements HdPlatformBackend {

    public static void installIfNeeded() {
        if (!HdPlatform.isInitialized()) {
            HdPlatform.setBackend(new DesktopPlatformBackend());
        }
    }

    @Override
    public boolean isWeb() {
        return false;
    }

    @Override
    public HdSurface createSurface(int width, int height, boolean fillWhite) {
        return DesktopSurface.create(width, height, fillWhite);
    }

    @Override
    public HdSurface createSurfaceFromArgb(int[] argb, int width, int height) {
        BufferedImage img = new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_ARGB);
        if (argb != null && argb.length >= width * height) {
            img.setRGB(0, 0, width, height, argb, 0, width);
        }
        return new DesktopSurface(img);
    }

    @Override
    public HdSurface decodeImageBytes(byte[] bytes, int offset, int length) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes, offset, length);
            BufferedImage decoded = ImageIO.read(bais);
            bais.close();
            if (decoded == null) {
                return null;
            }
            BufferedImage argb = new BufferedImage(decoded.getWidth(), decoded.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = argb.createGraphics();
            g2.drawImage(decoded, 0, 0, null);
            g2.dispose();
            return new DesktopSurface(argb);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to decode image bytes", e);
        }
    }

    @Override
    public InputStream openResourceStream(String resourcePath) {
        if (resourcePath == null) {
            return null;
        }
        String norm = resourcePath.startsWith("/") ? resourcePath : ("/" + resourcePath);
        return DesktopPlatformBackend.class.getResourceAsStream(norm);
    }

    private static File resolveStoreFile(String storeName) {
        File dir = new File(System.getProperty("user.dir"), "saves");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String safe = storeName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return new File(dir, safe + ".rms");
    }

    @Override
    public byte[] loadRecordStoreBytes(String storeName) {
        File file = resolveStoreFile(storeName);
        if (!file.exists()) {
            return null;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void saveRecordStoreBytes(String storeName, byte[] data) {
        File file = resolveStoreFile(storeName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteRecordStoreBytes(String storeName) {
        File file = resolveStoreFile(storeName);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public Player createMediaPlayer(InputStream stream, String mimeType, String clipHint) throws IOException, MediaException {
        return DesktopAudioFactory.createPlayer(stream, mimeType, clipHint);
    }

    @Override
    public void scheduleDelayed(final int delayMs, final Runnable task) {
        Thread timer = new Thread(new Runnable() {
            @Override
            public void run() {
                if (delayMs > 0) {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ignored) {
                    }
                }
                task.run();
            }
        }, "HdDelayedTask");
        timer.setDaemon(true);
        timer.start();
    }

    @Override
    public void requestExit() {
        if (Gdx.app != null) {
            Gdx.app.exit();
        } else {
            System.exit(0);
        }
    }
}
