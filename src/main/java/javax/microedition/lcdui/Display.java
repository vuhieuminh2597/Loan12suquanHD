package javax.microedition.lcdui;

import javax.microedition.midlet.MIDlet;

public class Display {
    private static final Display INSTANCE = new Display();
    private volatile Displayable current;

    private Display() {
    }

    public static Display getDisplay(MIDlet m) {
        return INSTANCE;
    }

    public static Display getActiveDisplay() {
        return INSTANCE;
    }

    public synchronized void setCurrent(Displayable nextDisplayable) {
        if (this.current instanceof Canvas && this.current != nextDisplayable) {
            ((Canvas) this.current).hideNotify();
        }
        this.current = nextDisplayable;
        if (nextDisplayable instanceof Canvas) {
            ((Canvas) nextDisplayable).showNotify();
        }
    }

    public Displayable getCurrent() {
        return this.current;
    }

    public boolean vibrate(int duration) {
        return true;
    }
}
