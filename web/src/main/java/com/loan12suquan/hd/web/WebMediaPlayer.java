package com.loan12suquan.hd.web;

import com.loan12suquan.hd.HdAudioSynthesizer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.TempoControl;
import javax.microedition.media.control.VolumeControl;

/**
 * WebAudio implementation of MIDP {@link Player} supporting both 44.1 kHz synthesized SFX WAVs
 * and polyphonic Cổ Phong MIDI background music & jingles on Safari / iPhone 17 PWA.
 */
public final class WebMediaPlayer implements Player, VolumeControl, TempoControl {
    private static final int STATE_UNREALIZED = 100;
    private static final int STATE_REALIZED = 200;
    private static final int STATE_PREFETCHED = 300;
    private static final int STATE_STARTED = 400;
    private static final int STATE_CLOSED = 0;

    private final int jsHandle;
    private final List<PlayerListener> listeners = new ArrayList<PlayerListener>();
    private int state = STATE_UNREALIZED;
    private int loopCount = 1;
    private int volumeLevel = 80;
    private boolean muted = false;
    private int tempoMillibeats = 120000;

    private WebMediaPlayer(int jsHandle) {
        this.jsHandle = jsHandle;
    }

    public static Player create(InputStream stream, String mimeType, String clipHint) throws IOException, MediaException {
        String normMime = mimeType != null ? mimeType.toLowerCase() : "audio/amr";
        if (normMime.contains("midi") || normMime.contains("mid")) {
            byte[] midiBytes = readAll(stream);
            int handle = WebDomBridge.createWebMidiPlayerTyped(
                    clipHint != null ? clipHint : "bgm",
                    WebPlatformBackend.toJsBytes(midiBytes));
            return new WebMediaPlayer(handle);
        }
        byte[] wavBytes = HdAudioSynthesizer.getOrCreateWav(clipHint);
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignored) {
            }
        }
        int handle = WebDomBridge.createWebSfxPlayerTyped(
                clipHint != null ? clipHint : "sfx",
                WebPlatformBackend.toJsBytes(wavBytes));
        return new WebMediaPlayer(handle);
    }

    private static byte[] readAll(InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
        byte[] buf = new byte[1024];
        int n;
        while ((n = in.read(buf)) != -1) {
            baos.write(buf, 0, n);
        }
        in.close();
        return baos.toByteArray();
    }

    private void fireEvent(String event, Object data) {
        PlayerListener[] snapshot = this.listeners.toArray(new PlayerListener[0]);
        for (int i = 0; i < snapshot.length; ++i) {
            try {
                snapshot[i].playerUpdate(this, event, data);
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void realize() {
        if (this.state == STATE_UNREALIZED) {
            this.state = STATE_REALIZED;
        }
    }

    @Override
    public void prefetch() {
        if (this.state < STATE_PREFETCHED && this.state != STATE_CLOSED) {
            this.state = STATE_PREFETCHED;
        }
    }

    @Override
    public void start() {
        if (this.state == STATE_CLOSED) {
            return;
        }
        this.state = STATE_STARTED;
        WebDomBridge.startWebPlayer(
                this.jsHandle,
                this.loopCount,
                this.volumeLevel,
                this.muted,
                this.tempoMillibeats,
                new WebDomBridge.JsRunnable() {
                    @Override
                    public void run() {
                        if (state == STATE_STARTED) {
                            state = STATE_PREFETCHED;
                        }
                        fireEvent(PlayerListener.END_OF_MEDIA, 0L);
                    }
                });
        fireEvent(PlayerListener.STARTED, 0L);
    }

    @Override
    public void stop() {
        if (this.state == STATE_STARTED) {
            WebDomBridge.stopWebPlayer(this.jsHandle);
            this.state = STATE_PREFETCHED;
            fireEvent(PlayerListener.STOPPED, 0L);
        }
    }

    @Override
    public void deallocate() {
        stop();
    }

    @Override
    public void close() {
        stop();
        WebDomBridge.closeWebPlayer(this.jsHandle);
        this.state = STATE_CLOSED;
    }

    @Override
    public long setMediaTime(long now) {
        return 0L;
    }

    @Override
    public long getMediaTime() {
        return 0L;
    }

    @Override
    public void setLoopCount(int count) {
        this.loopCount = count;
    }

    @Override
    public void addPlayerListener(PlayerListener playerListener) {
        if (playerListener != null && !this.listeners.contains(playerListener)) {
            this.listeners.add(playerListener);
        }
    }

    @Override
    public void removePlayerListener(PlayerListener playerListener) {
        this.listeners.remove(playerListener);
    }

    @Override
    public Object getControl(String controlType) {
        if (controlType == null) {
            return null;
        }
        if (controlType.contains("VolumeControl")) {
            return this;
        }
        if (controlType.contains("TempoControl")) {
            return this;
        }
        return null;
    }

    @Override
    public void setMute(boolean mute) {
        this.muted = mute;
        WebDomBridge.updateWebPlayerVolume(this.jsHandle, this.volumeLevel, this.muted);
    }

    @Override
    public boolean isMuted() {
        return this.muted;
    }

    @Override
    public int setLevel(int level) {
        this.volumeLevel = Math.max(0, Math.min(100, level));
        WebDomBridge.updateWebPlayerVolume(this.jsHandle, this.volumeLevel, this.muted);
        return this.volumeLevel;
    }

    @Override
    public int getLevel() {
        return this.volumeLevel;
    }

    @Override
    public int setTempo(int millibeatsPerMinute) {
        if (millibeatsPerMinute > 0) {
            this.tempoMillibeats = millibeatsPerMinute;
            WebDomBridge.updateWebPlayerTempo(this.jsHandle, this.tempoMillibeats);
        }
        return this.tempoMillibeats;
    }

    @Override
    public int getTempo() {
        return this.tempoMillibeats;
    }
}
