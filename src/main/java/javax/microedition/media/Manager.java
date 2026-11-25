package javax.microedition.media;

import com.loan12suquan.hd.platform.HdPlatform;
import java.io.IOException;
import java.io.InputStream;

/**
 * Cross-platform MIDP {@link Manager} delegating to {@link HdPlatform#backend()}
 * (JavaSound MIDI/WAV on Desktop, WebAudio Polyphonic Synth + PCM Buffer on Web/iPhone 17).
 */
public final class Manager {
    private static String nextClipHint;

    private Manager() {
    }

    public static synchronized void setNextClipHint(String clipName) {
        nextClipHint = clipName;
    }

    public static synchronized Player createPlayer(InputStream stream, String type) throws IOException, MediaException {
        String hint = nextClipHint;
        nextClipHint = null;
        return HdPlatform.backend().createMediaPlayer(stream, type, hint);
    }
}
