package com.mg.engine.audio;

import com.loan12suquan.hd.platform.HdPlatform;
import com.mg.engine.core.AsyncTask;
import com.mg.engine.core.AsyncTaskPool;
import com.mg.engine.core.DebugLogForm;
import com.mg.engine.core.GameConfig;
import java.io.IOException;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.TempoControl;
import javax.microedition.media.control.VolumeControl;

/** Singleton MIDI background music player with FreeJ2ME-Plus lazy-TempoControl safety. */
public class BgmPlayer implements AsyncTask, PlayerListener {
    public static BgmPlayer instance;

    private Player player;
    private String currentTrack = "";
    private String lastRequestedTrack = "";
    private int lastRequestedLoop = -1;
    private VolumeControl volumeControl;
    private TempoControl tempoControl;
    private int tempo = 100;
    private boolean playing;

    protected BgmPlayer() {
    }

    public static BgmPlayer getInstance() {
        if (instance == null) {
            instance = new BgmPlayer();
        }
        return instance;
    }

    public void load(String trackName, int loopCount) {
        if (trackName != null && trackName.length() > 0) {
            this.lastRequestedTrack = trackName;
            this.lastRequestedLoop = loopCount;
        }
        try {
            this.loadInternal(trackName, loopCount);
        } catch (Throwable firstError) {
            firstError.printStackTrace();
            DebugLogForm.log("Create Back ground 1: " + firstError);
            this.destroy();

            try {
                this.loadInternal(trackName, loopCount);
            } catch (Exception retryError) {
                DebugLogForm.log("Back ground 2: " + retryError);
            }
        }
    }

    /**
     * Updates global sound/music volume live at runtime (0..100), persists to RMS,
     * and immediately mutes/stops or resumes the active BGM track without restarting the app.
     */
    public void setLiveVolume(int newVolume) {
        if (newVolume < 0) {
            newVolume = 0;
        } else if (newVolume > 100) {
            newVolume = 100;
        }
        boolean wasEnabled = GameConfig.soundEnabled;
        GameConfig.soundVolume = newVolume;
        GameConfig.soundEnabled = newVolume > 0;
        com.mg.engine.storage.EngineStorage.d(newVolume);
        if (newVolume <= 0) {
            if (this.volumeControl != null) {
                try {
                    this.volumeControl.setMute(true);
                    this.volumeControl.setLevel(0);
                } catch (Throwable ignored) {
                }
            }
            this.stop();
        } else {
            if (this.player != null) {
                if (this.volumeControl != null) {
                    try {
                        this.volumeControl.setMute(false);
                        this.volumeControl.setLevel(newVolume);
                    } catch (Throwable ignored) {
                    }
                }
                if (!wasEnabled) {
                    this.playAsync();
                }
            } else if (this.lastRequestedTrack != null && this.lastRequestedTrack.length() > 0) {
                int savedTempo = this.tempo;
                this.load(this.lastRequestedTrack, this.lastRequestedLoop);
                if (savedTempo > 100) {
                    this.setTempo(savedTempo);
                }
                this.playAsync();
            }
        }
    }

    /** Cycles through 4 clean volume presets: 100% -> 70% -> 35% -> 0% (Muted) -> 100%. */
    public int cycleLiveVolumePreset() {
        int cur = GameConfig.soundEnabled ? GameConfig.soundVolume : 0;
        int next;
        if (cur <= 0) {
            next = 100;
        } else if (cur > 75) {
            next = 70;
        } else if (cur > 40) {
            next = 35;
        } else {
            next = 0;
        }
        this.setLiveVolume(next);
        return next;
    }

    private void loadInternal(String trackName, int loopCount) throws IOException, MediaException {
        if (!GameConfig.soundEnabled) {
            return;
        }
        if (this.player != null && this.currentTrack.equals(trackName)) {
            this.tempo = GameConfig.defaultMidiTempo;
            return;
        }

        this.destroy();
        Manager.setNextClipHint(trackName);
        this.player = Manager.createPlayer(HdPlatform.backend().openResourceStream("/audio/" + trackName + ".mid"), "audio/midi");
        this.player.setLoopCount(loopCount);
        this.player.realize();
        this.player.prefetch();
        this.player.addPlayerListener(this);

        this.volumeControl = (VolumeControl) this.player.getControl("VolumeControl");
        if (this.volumeControl != null) {
            this.volumeControl.setLevel(GameConfig.soundVolume);
            this.volumeControl.setMute(!GameConfig.soundEnabled);
        }

        this.tempoControl = (TempoControl) this.player.getControl("TempoControl");
        if (this.tempoControl != null) {
            GameConfig.defaultMidiTempo = this.tempoControl.getTempo();
            this.tempo = GameConfig.defaultMidiTempo;
            this.applyTempoSafe(this.tempo);
            DebugLogForm.log("Temp when create=" + this.tempo);
        }

        this.currentTrack = trackName;
        this.playing = false;
    }

    public void stop() {
        if (this.player != null) {
            try {
                this.player.stop();
            } catch (Throwable error) {
                this.destroy();
                DebugLogForm.log("[STOP BK Music]: " + error);
            }
        }
    }

    public void destroy() {
        if (this.player != null) {
            try {
                this.player.stop();
                this.player.deallocate();
                this.player.close();
            } catch (Throwable error) {
                DebugLogForm.log("[Destroy BK Music]: " + error);
            } finally {
                this.player = null;
                this.volumeControl = null;
                this.tempoControl = null;
            }
        }
        this.currentTrack = "";
    }

    public void playAsync() {
        if (GameConfig.soundEnabled) {
            AsyncTaskPool.getInstance().submit(this);
        }
    }

    public void runTask() {
        try {
            if (this.player != null) {
                if (this.volumeControl != null) {
                    this.volumeControl.setMute(!GameConfig.soundEnabled);
                    this.volumeControl.setLevel(GameConfig.soundVolume);
                }
                this.player.start();
                this.applyTempoSafe(this.tempo);
            }
        } catch (Exception error) {
            DebugLogForm.log("[Player BK Music]: " + error);
            this.destroy();
        }
    }

    public int getTempo() {
        return this.tempo;
    }

    public void setTempo(int newTempo) {
        this.tempo = newTempo;
        this.applyTempoSafe(newTempo);
    }

    private void applyTempoSafe(int targetTempo) {
        if (this.tempoControl == null) {
            return;
        }
        try {
            this.tempoControl.setTempo(targetTempo);
        } catch (NullPointerException ignored) {
            // FreeJ2ME-Plus initializes MIDIPlayer.midi lazily inside start(); applied after start().
        } catch (Throwable error) {
            this.tempo = GameConfig.defaultMidiTempo;
            try {
                this.tempoControl.setTempo(this.tempo);
            } catch (Throwable ignoredAgain) {
            }
        }
    }

    public void playerUpdate(Player source, String event, Object eventData) {
        if (event.equals("endOfMedia")) {
            if (this.volumeControl != null) {
                this.volumeControl.setLevel(GameConfig.soundVolume);
                this.volumeControl.setMute(!GameConfig.soundEnabled);
            }
            this.applyTempoSafe(this.tempo);
        } else if (event.equals("started")) {
            this.playing = true;
        } else if (event.equals("stopped")) {
            this.playing = false;
        }
    }
}



