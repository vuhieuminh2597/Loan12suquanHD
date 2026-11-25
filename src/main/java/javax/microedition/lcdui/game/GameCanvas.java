package javax.microedition.lcdui.game;

import com.loan12suquan.hd.HdConfig;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * LibGDX HD bridge for MIDP {@link GameCanvas} (used by {@link com.mg.engine.core.SplashCanvas}).
 */
public abstract class GameCanvas extends Canvas {
    private final Image offscreenBuffer;
    private final Graphics offscreenGraphics;

    protected GameCanvas(boolean suppressKeyEvents) {
        this.offscreenBuffer = Image.createImage(HdConfig.LOGICAL_WIDTH, HdConfig.LOGICAL_HEIGHT);
        this.offscreenGraphics = this.offscreenBuffer.getGraphics();
    }

    protected Graphics getGraphics() {
        return this.offscreenGraphics;
    }

    public Object getBufferLock() {
        return this.offscreenBuffer;
    }

    @Override
    protected void paint(Graphics g) {
        synchronized (this.offscreenBuffer) {
            g.drawImage(this.offscreenBuffer, 0, 0, Graphics.TOP | Graphics.LEFT);
        }
    }

    public void flushGraphics() {
        repaint();
    }

    public void flushGraphics(int x, int y, int width, int height) {
        repaint();
    }
}
