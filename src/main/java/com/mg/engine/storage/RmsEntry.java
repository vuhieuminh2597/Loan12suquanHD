package com.mg.engine.storage;

import com.mg.engine.util.ByteUtils;

/** Index entry describing a logical binary record stored across chunked RMS slots inside {@link RmsBank}. */
class RmsEntry {
    int id;
    int byteLength;
    int[] chunkRecordIds;
    byte[] pendingData;

    byte[] serialize() {
        int size = 12 + this.chunkRecordIds.length * 4;
        byte[] out = new byte[size];
        int offset = 0;
        System.arraycopy(ByteUtils.a(this.id), 0, out, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.byteLength), 0, out, offset, 4);
        offset += 4;
        System.arraycopy(ByteUtils.a(this.chunkRecordIds.length), 0, out, offset, 4);
        offset += 4;

        for (int i = 0; i < this.chunkRecordIds.length; ++i) {
            System.arraycopy(ByteUtils.a(this.chunkRecordIds[i]), 0, out, offset, 4);
            offset += 4;
        }
        return out;
    }

    int deserialize(byte[] data, int offset) {
        int startOffset = offset;
        this.id = ByteUtils.a(data, offset);
        offset += 4;
        this.byteLength = ByteUtils.a(data, offset);
        offset += 4;
        this.chunkRecordIds = new int[ByteUtils.a(data, offset)];
        offset += 4;

        for (int i = 0; i < this.chunkRecordIds.length; ++i) {
            this.chunkRecordIds[i] = ByteUtils.a(data, offset);
            offset += 4;
        }
        return offset - startOffset;
    }
}



