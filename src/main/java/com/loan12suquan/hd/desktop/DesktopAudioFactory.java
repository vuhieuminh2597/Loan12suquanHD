package com.loan12suquan.hd.desktop;

import com.loan12suquan.hd.HdAudioSynthesizer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.TempoControl;
import javax.microedition.media.control.VolumeControl;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

public final class DesktopAudioFactory {
    private DesktopAudioFactory() {
    }

    public static Player createPlayer(InputStream stream, String type, String hint) throws IOException {
        if (type != null && type.toLowerCase().contains("midi")) {
            return new MidiAudioPlayer(stream);
        }
        return new HdWavSfxPlayer(stream, hint);
    }

    private static final class MidiAudioPlayer implements Player, VolumeControl, TempoControl {
        private final byte[] midiBytes;
        private Sequencer sequencer;
        private Synthesizer synthesizer;
        private int loopCount = 1;
        private int volumeLevel = 80;
        private boolean muted = false;
        private int millitempo = 120000;
        private final List<PlayerListener> listeners = new CopyOnWriteArrayList<PlayerListener>();

        MidiAudioPlayer(InputStream in) throws IOException {
            if (in != null) {
                try {
                    this.midiBytes = in.readAllBytes();
                } finally {
                    in.close();
                }
            } else {
                this.midiBytes = new byte[0];
            }
        }

        @Override
        public synchronized void realize() throws MediaException {
            if (this.sequencer != null) {
                return;
            }
            try {
                this.sequencer = MidiSystem.getSequencer(false);
                this.sequencer.open();
                this.synthesizer = MidiSystem.getSynthesizer();
                this.synthesizer.open();
                Receiver synthReceiver = this.synthesizer.getReceiver();
                this.sequencer.getTransmitter().setReceiver(synthReceiver);

                if (this.midiBytes.length > 0) {
                    Sequence seq = MidiSystem.getSequence(new ByteArrayInputStream(this.midiBytes));
                    this.sequencer.setSequence(seq);
                    float bpm = this.sequencer.getTempoInBPM();
                    if (bpm > 0f) {
                        this.millitempo = Math.round(bpm * 1000f);
                    }
                }

                this.sequencer.addMetaEventListener(new MetaEventListener() {
                    @Override
                    public void meta(MetaMessage meta) {
                        if (meta.getType() == 47) {
                            notifyListeners(PlayerListener.END_OF_MEDIA);
                        }
                    }
                });
            } catch (Exception e) {
                throw new MediaException(e.getMessage());
            }
        }

        @Override
        public synchronized void prefetch() throws MediaException {
            if (this.sequencer == null) {
                this.realize();
            }
            applyVolume();
        }

        @Override
        public synchronized void start() throws MediaException {
            if (this.sequencer == null) {
                this.realize();
            }
            try {
                if (!this.sequencer.isOpen()) {
                    this.sequencer.open();
                }
                if (this.loopCount < 0) {
                    this.sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
                } else if (this.loopCount > 1) {
                    this.sequencer.setLoopCount(this.loopCount - 1);
                } else {
                    this.sequencer.setLoopCount(0);
                }
                applyVolume();
                applyTempo();
                this.sequencer.start();
                notifyListeners(PlayerListener.STARTED);
            } catch (Exception e) {
                throw new MediaException(e.getMessage());
            }
        }

        @Override
        public synchronized void stop() {
            if (this.sequencer != null && this.sequencer.isOpen() && this.sequencer.isRunning()) {
                this.sequencer.stop();
                notifyListeners(PlayerListener.STOPPED);
            }
        }

        @Override
        public synchronized void deallocate() {
            stop();
        }

        @Override
        public synchronized void close() {
            stop();
            if (this.sequencer != null) {
                try {
                    this.sequencer.close();
                } catch (Throwable ignored) {
                }
                this.sequencer = null;
            }
            if (this.synthesizer != null) {
                try {
                    this.synthesizer.close();
                } catch (Throwable ignored) {
                }
                this.synthesizer = null;
            }
        }

        @Override
        public synchronized long setMediaTime(long now) {
            if (this.sequencer != null && this.sequencer.isOpen()) {
                this.sequencer.setMicrosecondPosition(now);
                return this.sequencer.getMicrosecondPosition();
            }
            return 0L;
        }

        @Override
        public synchronized long getMediaTime() {
            return (this.sequencer != null && this.sequencer.isOpen()) ? this.sequencer.getMicrosecondPosition() : 0L;
        }

        @Override
        public synchronized void setLoopCount(int count) {
            this.loopCount = count;
        }

        @Override
        public void addPlayerListener(PlayerListener listener) {
            if (listener != null && !this.listeners.contains(listener)) {
                this.listeners.add(listener);
            }
        }

        @Override
        public void removePlayerListener(PlayerListener listener) {
            this.listeners.remove(listener);
        }

        @Override
        public Object getControl(String controlType) {
            if ("VolumeControl".equals(controlType)) {
                return this;
            }
            if ("TempoControl".equals(controlType)) {
                return this;
            }
            return null;
        }

        @Override
        public synchronized void setMute(boolean mute) {
            this.muted = mute;
            applyVolume();
        }

        @Override
        public synchronized boolean isMuted() {
            return this.muted;
        }

        @Override
        public synchronized int setLevel(int level) {
            this.volumeLevel = Math.max(0, Math.min(100, level));
            applyVolume();
            return this.volumeLevel;
        }

        @Override
        public synchronized int getLevel() {
            return this.volumeLevel;
        }

        @Override
        public synchronized int setTempo(int millitempo) {
            if (millitempo > 0) {
                this.millitempo = millitempo;
                applyTempo();
            }
            return this.millitempo;
        }

        @Override
        public synchronized int getTempo() {
            return this.millitempo;
        }

        private void applyTempo() {
            if (this.sequencer != null && this.sequencer.isOpen() && this.millitempo > 0) {
                float bpm = this.millitempo / 1000.0f;
                if (bpm >= 20f && bpm <= 400f) {
                    this.sequencer.setTempoInBPM(bpm);
                }
            }
        }

        private void applyVolume() {
            if (this.synthesizer == null || !this.synthesizer.isOpen()) {
                return;
            }
            int midiVol = this.muted ? 0 : (this.volumeLevel * 127) / 100;
            MidiChannel[] channels = this.synthesizer.getChannels();
            if (channels != null) {
                for (int i = 0; i < channels.length; ++i) {
                    if (channels[i] != null) {
                        channels[i].controlChange(7, midiVol);
                    }
                }
            }
        }

        private void notifyListeners(String event) {
            for (PlayerListener l : this.listeners) {
                try {
                    l.playerUpdate(this, event, null);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private static final class HdWavSfxPlayer implements Player, VolumeControl {
        private final String clipKey;
        private Clip clip;
        private int volumeLevel = 80;
        private boolean muted = false;
        private final List<PlayerListener> listeners = new CopyOnWriteArrayList<PlayerListener>();

        HdWavSfxPlayer(InputStream in, String hint) {
            int byteLen = 0;
            if (in != null) {
                try {
                    byteLen = in.readAllBytes().length;
                } catch (IOException ignored) {
                } finally {
                    try {
                        in.close();
                    } catch (IOException ignored) {
                    }
                }
            }
            this.clipKey = (hint != null && !hint.isEmpty()) ? hint : guessClipByAmrSize(byteLen);
        }

        private static String guessClipByAmrSize(int size) {
            switch (size) {
                case 614:  return "explodechess";
                case 1190: return "attack";
                case 2086: return "fire1";
                case 3206: return "fire2";
                case 2470: return "fire3";
                case 3238: return "ice1";
                case 4006: return "ice2";
                case 3174: return "lvu";
                case 3270: return "lightning1";
                case 2886: return "lightning2";
                case 3334: return "useitem";
                case 2566: return "vs";
                default:   return "explodechess";
            }
        }

        @Override
        public synchronized void realize() throws MediaException {
            if (this.clip != null) {
                return;
            }
            try {
                byte[] wavBytes = HdAudioSynthesizer.getOrCreateWav(this.clipKey);
                AudioInputStream ais = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wavBytes));
                this.clip = AudioSystem.getClip();
                this.clip.open(ais);
                this.clip.addLineListener(new LineListener() {
                    @Override
                    public void update(LineEvent event) {
                        if (event.getType() == LineEvent.Type.STOP) {
                            for (PlayerListener l : listeners) {
                                try {
                                    l.playerUpdate(HdWavSfxPlayer.this, PlayerListener.END_OF_MEDIA, null);
                                } catch (Throwable ignored) {
                                }
                            }
                        }
                    }
                });
            } catch (Exception e) {
                throw new MediaException(e.getMessage());
            }
        }

        @Override
        public synchronized void prefetch() throws MediaException {
            if (this.clip == null) {
                this.realize();
            }
            applyGain();
        }

        @Override
        public synchronized void start() throws MediaException {
            if (this.clip == null) {
                this.realize();
            }
            if (this.clip != null) {
                if (this.clip.isRunning()) {
                    this.clip.stop();
                }
                this.clip.setFramePosition(0);
                applyGain();
                if (!this.muted && this.volumeLevel > 0) {
                    this.clip.start();
                }
            }
        }

        @Override
        public synchronized void stop() {
            if (this.clip != null && this.clip.isRunning()) {
                this.clip.stop();
            }
        }

        @Override
        public synchronized void deallocate() {
            stop();
        }

        @Override
        public synchronized void close() {
            stop();
            if (this.clip != null) {
                try {
                    this.clip.close();
                } catch (Throwable ignored) {
                }
                this.clip = null;
            }
        }

        @Override
        public synchronized long setMediaTime(long now) {
            if (this.clip != null) {
                this.clip.setMicrosecondPosition(now);
            }
            return now;
        }

        @Override
        public synchronized long getMediaTime() {
            return this.clip != null ? this.clip.getMicrosecondPosition() : 0L;
        }

        @Override
        public synchronized void setLoopCount(int count) {
        }

        @Override
        public void addPlayerListener(PlayerListener listener) {
            if (listener != null && !this.listeners.contains(listener)) {
                this.listeners.add(listener);
            }
        }

        @Override
        public void removePlayerListener(PlayerListener listener) {
            this.listeners.remove(listener);
        }

        @Override
        public Object getControl(String controlType) {
            if ("VolumeControl".equals(controlType)) {
                return this;
            }
            return null;
        }

        @Override
        public synchronized void setMute(boolean mute) {
            this.muted = mute;
            applyGain();
        }

        @Override
        public synchronized boolean isMuted() {
            return this.muted;
        }

        @Override
        public synchronized int setLevel(int level) {
            this.volumeLevel = Math.max(0, Math.min(100, level));
            applyGain();
            return this.volumeLevel;
        }

        @Override
        public synchronized int getLevel() {
            return this.volumeLevel;
        }

        private void applyGain() {
            if (this.clip == null || !this.clip.isOpen()) {
                return;
            }
            try {
                if (this.clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gain = (FloatControl) this.clip.getControl(FloatControl.Type.MASTER_GAIN);
                    if (this.muted || this.volumeLevel <= 0) {
                        gain.setValue(gain.getMinimum());
                    } else {
                        float db = (float) (20.0 * Math.log10(this.volumeLevel / 100.0));
                        db = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db));
                        gain.setValue(db);
                    }
                }
            } catch (Throwable ignored) {
            }
        }
    }
}
