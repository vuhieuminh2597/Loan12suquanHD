package com.mg.engine.core;

import com.mg.smsgame.main.MGMIDlet;
import java.util.Date;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

/** In-app debug log viewer form (active only when {@link GameConfig#debugMode} is enabled). */
public class DebugLogForm extends Form implements CommandListener {
    private static final int MAX_LOG_ENTRIES = 50;
    private static DebugLogForm instance;

    private int logMask = 255;
    private int entryCount = 0;

    protected DebugLogForm() {
        super("All log");
        this.setCommandListener(this);
        this.addCommand(new Command("Back", Command.BACK, 1));
        this.addCommand(new Command("Clear", Command.OK, 1));
        this.append("Start at " + new Date(System.currentTimeMillis()).toString());
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command.getLabel().equals("Back")) {
            MGMIDlet.f().a.setCurrent(GUIManager.getInstance());
        } else if (command.getLabel().equals("Clear")) {
            this.deleteAll();
            this.entryCount = 0;
            this.append("Start at " + new Date(System.currentTimeMillis()).toString());
        }
    }

    public static DebugLogForm getInstance() {
        if (instance == null) {
            instance = new DebugLogForm();
        }
        return instance;
    }

    private void appendMessage(String message) {
        if (!GameConfig.debugMode || (this.logMask & 1) == 0) {
            return;
        }
        if (this.entryCount >= MAX_LOG_ENTRIES) {
            getInstance().delete(0);
        }
        this.append(message);
        System.out.println(message);
        ++this.entryCount;
    }

    public static void log(String message) {
        getInstance().appendMessage(message);
    }

    private void appendError(Throwable error) {
        if (!GameConfig.debugMode || (this.logMask & 2) == 0) {
            return;
        }
        if (this.entryCount >= MAX_LOG_ENTRIES) {
            getInstance().delete(0);
        }
        error.printStackTrace();
        this.append(error.toString());
        ++this.entryCount;
    }

    public static void log(Throwable error) {
        getInstance().appendError(error);
    }
}



