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

/** Secondary MIDI player used for battle victory/defeat jingles. */
public class JinglePlayer implements AsyncTask, PlayerListener {
    private Player player;
    private String currentTrack = "";
    private VolumeControl volumeControl;
    private TempoControl tempoControl;
    private int tempo = 100;

    public void load(String trackName, int loopCount) {
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
    }

    public void stop() {
        if (this.player != null) {
            try {
                this.player.stop();
                this.player.deallocate();
            } catch (Throwable error) {
                this.destroy();
                DebugLogForm.log("[Destroy BK Music]: " + error);
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
                this.player.prefetch();
                this.player.setMediaTime(0L);
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
        if (GameConfig.soundEnabled && event.equals("endOfMedia")) {
            this.stop();
        }
    }
}



