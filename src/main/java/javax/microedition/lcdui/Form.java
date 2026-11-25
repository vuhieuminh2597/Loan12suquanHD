package javax.microedition.lcdui;

import java.util.ArrayList;
import java.util.List;

public class Form extends Displayable {
    private final String title;
    private final List<String> items = new ArrayList<String>();

    public Form(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public int append(String str) {
        this.items.add(str);
        return this.items.size() - 1;
    }

    public void delete(int itemNum) {
        if (itemNum >= 0 && itemNum < this.items.size()) {
            this.items.remove(itemNum);
        }
    }

    public void deleteAll() {
        this.items.clear();
    }
}
