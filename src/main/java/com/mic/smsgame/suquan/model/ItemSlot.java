package com.mic.smsgame.suquan.model;

import com.mg.engine.util.ByteUtils;

/**
 * Inventory item or active battle buff slot pairing an item ID with quantity / remaining turns.
 */
public class ItemSlot {
    /** Item ID (0..5). */
    public int a;
    /** Quantity owned or active turns remaining. */
    public int b;

    /** Decrements quantity/turns by 1 if positive; returns true if decremented. */
    public boolean a() {
        if (this.b > 0) {
            --this.b;
            return true;
        }
        return false;
    }

    public int a(byte[] buffer, int offset) {
        int start = offset;
        System.arraycopy(ByteUtils.a(this.a), 0, buffer, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.b), 0, buffer, offset, 4);
        offset += 4;
        return offset - start;
    }

    public int b(byte[] buffer, int offset) {
        int start = offset;
        this.a = ByteUtils.a(buffer, offset);
        offset += 4;
        this.b = ByteUtils.a(buffer, offset);
        offset += 4;
        return offset - start;
    }
}
