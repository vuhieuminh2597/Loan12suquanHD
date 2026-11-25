package javax.microedition.media;

public interface Player {
    void realize() throws MediaException;

    void prefetch() throws MediaException;

    void start() throws MediaException;

    void stop() throws MediaException;

    void deallocate();

    void close();

    long setMediaTime(long now) throws MediaException;

    long getMediaTime();

    void setLoopCount(int count);

    void addPlayerListener(PlayerListener playerListener);

    void removePlayerListener(PlayerListener playerListener);

    Object getControl(String controlType);
}
