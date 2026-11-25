package javax.microedition.lcdui;

/**
 * LibGDX bridge for MIDP {@link Canvas}.
 */
public abstract class Canvas extends Displayable {
    public static final int UP = 1;
    public static final int DOWN = 6;
    public static final int LEFT = 2;
    public static final int RIGHT = 5;
    public static final int FIRE = 8;

    public static final int GAME_A = 9;
    public static final int GAME_B = 10;
    public static final int GAME_C = 11;
    public static final int GAME_D = 12;

    public static final int KEY_NUM0 = 48;
    public static final int KEY_NUM1 = 49;
    public static final int KEY_NUM2 = 50;
    public static final int KEY_NUM3 = 51;
    public static final int KEY_NUM4 = 52;
    public static final int KEY_NUM5 = 53;
    public static final int KEY_NUM6 = 54;
    public static final int KEY_NUM7 = 55;
    public static final int KEY_NUM8 = 56;
    public static final int KEY_NUM9 = 57;
    public static final int KEY_STAR = 42;
    public static final int KEY_POUND = 35;

    private static volatile Runnable repaintCallback;

    protected Canvas() {
    }

    public static void setRepaintCallback(Runnable callback) {
        repaintCallback = callback;
    }

    protected abstract void paint(Graphics g);

    public final void invokePaint(Graphics g) {
        this.paint(g);
    }

    public void setFullScreenMode(boolean mode) {
    }

    public boolean hasPointerEvents() {
        return true;
    }

    public boolean hasPointerMotionEvents() {
        return true;
    }

    public int getKeyCode(int gameAction) {
        switch (gameAction) {
            case UP:    return -1;
            case DOWN:  return -2;
            case LEFT:  return -3;
            case RIGHT: return -4;
            case FIRE:  return -5;
            default:    return 0;
        }
    }

    public int getGameAction(int keyCode) {
        switch (keyCode) {
            case -1:
            case KEY_NUM2:
                return UP;
            case -2:
            case KEY_NUM8:
                return DOWN;
            case -3:
            case KEY_NUM4:
                return LEFT;
            case -4:
            case KEY_NUM6:
                return RIGHT;
            case -5:
            case KEY_NUM5:
                return FIRE;
            default:
                return 0;
        }
    }

    public final void repaint() {
        Runnable cb = repaintCallback;
        if (cb != null) {
            cb.run();
        }
    }

    public final void repaint(int x, int y, int width, int height) {
        repaint();
    }

    public final void serviceRepaints() {
    }

    public void keyPressed(int keyCode) {
    }

    public void keyReleased(int keyCode) {
    }

    public void keyRepeated(int keyCode) {
    }

    public void pointerPressed(int x, int y) {
    }

    public void pointerDragged(int x, int y) {
    }

    public void pointerReleased(int x, int y) {
    }

    public void showNotify() {
    }

    public void hideNotify() {
    }
}
