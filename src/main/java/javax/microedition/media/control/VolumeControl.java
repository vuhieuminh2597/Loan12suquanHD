package javax.microedition.media.control;

public interface VolumeControl {
    void setMute(boolean mute);

    boolean isMuted();

    int setLevel(int level);

    int getLevel();
}
