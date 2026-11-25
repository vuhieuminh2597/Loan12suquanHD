package javax.microedition.lcdui;

import com.loan12suquan.hd.HdConfig;

public abstract class Displayable {
    protected CommandListener commandListener;

    public void addCommand(Command cmd) {
    }

    public void removeCommand(Command cmd) {
    }

    public void setCommandListener(CommandListener l) {
        this.commandListener = l;
    }

    public int getWidth() {
        return HdConfig.LOGICAL_WIDTH;
    }

    public int getHeight() {
        return HdConfig.LOGICAL_HEIGHT;
    }
}
