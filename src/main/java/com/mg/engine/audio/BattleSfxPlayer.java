package com.mg.engine.audio;

import com.loan12suquan.hd.platform.HdPlatform;
import com.mg.engine.core.AsyncTask;
import com.mg.engine.core.AsyncTaskPool;
import com.mg.engine.core.DebugLogForm;
import com.mg.engine.core.GameConfig;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VolumeControl;

/** Battle AMR sound effect player with timeout protection against stalled playback. */
public class BattleSfxPlayer implements AsyncTask, PlayerListener {
    private Player player;
    private VolumeControl volumeControl;
    private String currentClip = "";
    private boolean active;
    private long startTimeMs;
    private int timeoutCount;
    private int timeoutThresholdMs = 5000;

    public BattleSfxPlayer() {
    }

    public BattleSfxPlayer(String clipName) {
        this.load(clipName);
    }

    public void load(String clipName) {
        this.active = false;
        DebugLogForm.log("Play " + clipName + " - " + this.currentClip + "...");
        if (this.player != null && clipName.equals(this.currentClip)) {
            this.stop();
            return;
        }
        try {
            if (clipName != null && clipName.length() > 0) {
                this.currentClip = clipName;
            }
            if (!GameConfig.soundEnabled) {
                return;
            }
            this.destroy();
            Manager.setNextClipHint(clipName);
            this.player = Manager.createPlayer(HdPlatform.backend().openResourceStream("/audio/" + clipName + ".amr"), "audio/amr");
            this.player.setLoopCount(1);
            this.player.realize();
            this.player.prefetch();
            this.player.addPlayerListener(this);

            this.volumeControl = (VolumeControl) this.player.getControl("VolumeControl");
            if (this.volumeControl != null) {
                this.volumeControl.setMute(!GameConfig.soundEnabled);
                this.volumeControl.setLevel(GameConfig.soundVolume);
            }

            this.currentClip = clipName;
            DebugLogForm.log("Write currFile " + this.currentClip);
        } catch (Throwable error) {
            DebugLogForm.log("[Create Effect Sound" + error);
            this.destroy();
        }
    }

    public void destroy() {
        if (this.player != null) {
            try {
                this.player.stop();
                this.player.deallocate();
                this.player.close();
                DebugLogForm.log("destroy " + this.currentClip);
            } catch (Throwable error) {
                DebugLogForm.log("[Destroy Effect Sound" + error);
            } finally {
                this.player = null;
                this.volumeControl = null;
            }
        }
    }

    public void playAsync() {
        if (GameConfig.soundEnabled) {
            this.active = true;
            if (this.player == null && !this.currentClip.equals("")) {
                this.load(this.currentClip);
            }
            this.startTimeMs = System.currentTimeMillis();
            AsyncTaskPool.getInstance().submit(this);
        }
    }

    public void stop() {
        if (this.player != null) {
            try {
                this.player.stop();
                this.active = false;
            } catch (Throwable error) {
                this.destroy();
                DebugLogForm.log("[STOP EFFECT Music]: " + error);
            }
        }
    }

    public void runTask() {
        if (!this.active) {
            return;
        }
        try {
            if (GameConfig.isNokiaSeries40) {
                this.player.stop();
            }
            this.player.setMediaTime(0L);
            if (this.volumeControl != null) {
                this.volumeControl.setMute(!GameConfig.soundEnabled);
                this.volumeControl.setLevel(GameConfig.soundVolume);
            }
            if (this.active) {
                this.player.start();
            }
        } catch (Exception error) {
            this.destroy();
            this.active = false;
            DebugLogForm.log("[Play Effect Sound " + error);
        }
    }

    public boolean isPlaying() {
        if (this.active) {
            if (this.timeoutCount > 3) {
                this.stop();
            } else if (System.currentTimeMillis() - this.startTimeMs > (long) this.timeoutThresholdMs) {
                ++this.timeoutCount;
                this.stop();
            }
        }
        return this.active;
    }

    public void playerUpdate(Player source, String event, Object eventData) {
        if (GameConfig.soundEnabled) {
            if (event.equals("endOfMedia")) {
                this.active = false;
            } else if (event.equals("started") && !this.active) {
                this.stop();
            }
        }
    }
}



