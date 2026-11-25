package com.mg.engine.audio;

import com.loan12suquan.hd.platform.HdPlatform;
import com.mg.engine.core.AsyncTask;
import com.mg.engine.core.AsyncTaskPool;
import com.mg.engine.core.DebugLogForm;
import com.mg.engine.core.GameConfig;
import com.mg.engine.storage.EngineStorage;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VolumeControl;

/** Engine AMR sound effect player used for map/UI audio cues (level up, victory, etc.). */
public class SfxPlayer implements AsyncTask, PlayerListener {
    private Player player;
    private VolumeControl volumeControl;
    private String currentClip = "";
    private boolean ready = true;
    private boolean playing;

    public SfxPlayer() {
    }

    public SfxPlayer(String clipName) {
        this.load(clipName);
    }

    public void load(String clipName) {
        if (!this.ready) {
            return;
        }
        DebugLogForm.log("Play " + clipName + " - " + this.currentClip + "...");
        if (clipName.equals(this.currentClip)) {
            this.stop();
            return;
        }
        try {
            this.ready = false;
            if (!GameConfig.soundEnabled || !GameConfig.sfxEnabled) {
                return;
            }
            this.destroy();
            Manager.setNextClipHint(clipName);
            this.player = Manager.createPlayer(HdPlatform.backend().openResourceStream("/audio/" + clipName + ".amr"), "audio/amr");
            this.player.setLoopCount(GameConfig.isOldSonyEricsson ? -1 : 1);
            this.player.realize();
            this.player.prefetch();
            this.player.addPlayerListener(this);

            this.volumeControl = (VolumeControl) this.player.getControl("VolumeControl");
            if (this.volumeControl != null) {
                if (GameConfig.isOldSonyEricsson) {
                    this.volumeControl.setMute(true);
                    this.volumeControl.setLevel(GameConfig.soundVolume);
                    this.player.start();
                } else {
                    this.volumeControl.setMute(!GameConfig.soundEnabled);
                    this.volumeControl.setLevel(GameConfig.soundVolume);
                }
            }

            DebugLogForm.log("Write currFile " + this.currentClip);
            this.currentClip = clipName;
            EngineStorage.a(false);
            this.ready = true;
        } catch (Throwable error) {
            this.ready = true;
            DebugLogForm.log("[Create Effect Sound" + error);
            EngineStorage.a(true);
            this.destroy();
        }
    }

    public void destroy() {
        if (this.player != null) {
            try {
                this.player.stop();
                this.player.deallocate();
                this.player.close();
            } catch (Throwable error) {
                DebugLogForm.log("[Destroy Effect Sound" + error);
            } finally {
                this.player = null;
                this.volumeControl = null;
            }
        }
    }

    public void playAsync() {
        if (GameConfig.soundEnabled && GameConfig.sfxEnabled) {
            if (this.player == null && !this.currentClip.equals("")) {
                this.load(this.currentClip);
            }
            AsyncTaskPool.getInstance().submit(this);
        }
    }

    public void stop() {
        if (this.player != null) {
            try {
                this.player.stop();
                this.player.setMediaTime(0L);
            } catch (Throwable error) {
                this.destroy();
                DebugLogForm.log("[STOP EFFECT Music]: " + error);
            }
        }
    }

    public void runTask() {
        try {
            if (GameConfig.isOldSonyEricsson) {
                this.player.setMediaTime(0L);
                if (this.volumeControl != null) {
                    this.volumeControl.setMute(!GameConfig.soundEnabled);
                }
            } else {
                if (GameConfig.isNokiaSeries40) {
                    BgmPlayer.getInstance().stop();
                }
                this.player.setMediaTime(0L);
                if (this.volumeControl != null) {
                    this.volumeControl.setMute(!GameConfig.soundEnabled);
                    this.volumeControl.setLevel(GameConfig.soundVolume);
                }
                this.player.start();
            }
        } catch (Exception error) {
            DebugLogForm.log("[Play Effect Sound" + error);
            EngineStorage.a(true);
            if (!GameConfig.sfxEnabled) {
                this.destroy();
            }
        }
    }

    public boolean isPlaying() {
        return this.playing;
    }

    public void playerUpdate(Player source, String event, Object eventData) {
        if (event.equals("endOfMedia")) {
            try {
                if (GameConfig.isOldSonyEricsson) {
                    if (this.volumeControl != null) {
                        this.volumeControl.setLevel(GameConfig.soundVolume);
                        this.volumeControl.setMute(true);
                    }
                } else {
                    if (this.volumeControl != null) {
                        this.volumeControl.setMute(!GameConfig.soundEnabled);
                        this.volumeControl.setLevel(GameConfig.soundVolume);
                    }
                    if (GameConfig.isNokiaSeries40) {
                        this.stop();
                        BgmPlayer.getInstance().playAsync();
                    }
                }
            } catch (Exception error) {
                try {
                    this.player.setMediaTime(0L);
                } catch (Exception ignored) {
                }
            }
            this.playing = false;
        } else if (event.equals("started")) {
            this.playing = true;
        } else if (event.equals("stopped")) {
            this.playing = false;
        }
    }
}



