package javax.microedition.midlet;

import com.loan12suquan.hd.platform.HdPlatform;

/**
 * Cross-platform MIDP {@link MIDlet} lifecycle bridge for Desktop (LibGDX) and Web/iPhone 17 (TeaVM PWA).
 */
public abstract class MIDlet {
    private boolean destroyed = false;

    protected MIDlet() {
    }

    protected abstract void startApp();

    protected abstract void pauseApp();

    protected abstract void destroyApp(boolean unconditional);

    public final void invokeStartApp() {
        this.startApp();
    }

    public final void invokePauseApp() {
        this.pauseApp();
    }

    public final void invokeDestroyApp(boolean unconditional) {
        this.destroyApp(unconditional);
    }

    public final void notifyDestroyed() {
        this.destroyed = true;
        try {
            this.destroyApp(true);
        } catch (Throwable ignored) {
        }
        if (HdPlatform.isInitialized()) {
            HdPlatform.backend().requestExit();
        }
    }

    public final boolean isDestroyed() {
        return this.destroyed;
    }

    public final String getAppProperty(String key) {
        return null;
    }
}
