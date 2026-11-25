package com.mg.engine.util;

/** Lightweight unsynchronized growable object list (CLDC alternative to {@link java.util.Vector}). */
public class ObjectList {
    Object[] elements;
    private int count;

    public ObjectList() {
        this.count = 0;
    }

    public ObjectList(int initialCapacity) {
        this();
        this.elements = new Object[initialCapacity];
    }

    public void a(Object item) {
        if (this.elements == null) {
            this.elements = new Object[10];
        }
        if (this.count >= this.elements.length) {
            Object[] grown = new Object[this.elements.length + 10];
            System.arraycopy(this.elements, 0, grown, 0, this.elements.length);
            this.elements = grown;
        }
        this.elements[this.count] = item;
        ++this.count;
    }

    public Object a(int index) {
        if (this.elements == null) {
            return null;
        }
        if (index >= 0 && index <= this.count) {
            Object removed = this.elements[index];
            this.elements[index] = null;
            --this.count;
            if (index < this.elements.length - 1) {
                System.arraycopy(this.elements, index + 1, this.elements, index, this.count - index);
            }
            return removed;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public void a() {
        this.elements = null;
        this.count = 0;
    }

    public void b(Object item) {
        if (this.elements == null) {
            return;
        }
        for (int i = 0; i < this.count; ++i) {
            if (this.elements[i].equals(item)) {
                this.a(i);
                break;
            }
        }
    }

    public Object b(int index) {
        if (this.elements == null) {
            return null;
        }
        return index < this.count && index >= 0 ? this.elements[index] : null;
    }

    public int b() {
        return this.count;
    }

    public String toString() {
        if (this.count == 0) {
            return "[Size]: 0";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < this.count; ++i) {
            if (this.elements[i] != null) {
                sb.append("\n[Index]: ").append(i).append(" [Object]: ").append(this.elements[i].toString());
            } else {
                sb.append("\n[Index]: ").append(i).append(" [Object]: NULL");
            }
        }
        return sb.toString();
    }
}



