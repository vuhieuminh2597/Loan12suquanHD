package com.mg.engine.storage;

import com.mg.engine.util.ByteUtils;
import com.mg.engine.util.ObjectList;
import javax.microedition.rms.RecordStore;

/**
 * Double-buffered chunked binary storage bank built on top of MIDP {@link RecordStore}.
 * Slots 1 and 2 hold alternating index headers; slot 3 stores the active index slot id (1 or 2).
 */
public class RmsBank {
    private static final int DEFAULT_HEADER_SIZE = 2048;

    private final String storeName;
    private final int chunkSize;
    private final int headerSize;
    private final ObjectList entries;
    private final ObjectList freeSlots;
    private final ObjectList reclaimedSlots;
    private int activeHeaderSlot;

    private RmsBank(String storeName, int chunkSize) {
        this(storeName, chunkSize, DEFAULT_HEADER_SIZE);
    }

    private RmsBank(String storeName, int chunkSize, int headerSize) {
        this.headerSize = headerSize;
        this.storeName = storeName;
        this.chunkSize = chunkSize;
        this.entries = new ObjectList();
        this.freeSlots = new ObjectList();
        this.reclaimedSlots = new ObjectList();
        this.loadIndex();
    }

    private byte[] initializeEmptyStore(RecordStore store) {
        byte[] emptyHeader = new byte[this.headerSize];
        RmsHelper.addRecord(store, emptyHeader);
        RmsHelper.addRecord(store, emptyHeader);
        RmsHelper.addRecord(store, new byte[]{1});
        return emptyHeader;
    }

    private void loadIndex() {
        RecordStore store = RmsHelper.open(this.storeName, false);
        try {
            byte[] headerBytes;
            if (store != null && RmsHelper.getNumRecords(store) >= 3) {
                this.activeHeaderSlot = 0;
                byte[] activeSlotBytes = RmsHelper.getRecord(store, 3);
                if (activeSlotBytes != null && activeSlotBytes.length > 0) {
                    this.activeHeaderSlot = activeSlotBytes[0];
                }
                if (this.activeHeaderSlot < 1 || this.activeHeaderSlot > 2) {
                    this.activeHeaderSlot = 1;
                }
                headerBytes = RmsHelper.getRecord(store, this.activeHeaderSlot);
            } else {
                RmsHelper.deleteStore(this.storeName);
                store = RmsHelper.open(this.storeName, true);
                headerBytes = this.initializeEmptyStore(store);
                this.activeHeaderSlot = 1;
            }

            int totalRecords = RmsHelper.getNumRecords(store);
            int accountedRecords = 3;
            RmsHelper.close(store);

            int offset = 0;
            int entryCount = ByteUtils.a(headerBytes, offset);
            offset += 4;

            for (int i = 0; i < entryCount; ++i) {
                RmsEntry entry = new RmsEntry();
                offset += entry.deserialize(headerBytes, offset);
                this.entries.a(entry);
                accountedRecords += entry.chunkRecordIds.length;
            }

            int freeCount = ByteUtils.a(headerBytes, offset);
            offset += 4;
            for (int i = 0; i < freeCount; ++i) {
                this.freeSlots.a(new Integer(ByteUtils.a(headerBytes, offset)));
                offset += 4;
            }

            accountedRecords += this.freeSlots.b();
            if (totalRecords - 3 > accountedRecords) {
                this.collectOrphanSlots(totalRecords);
            }
        } catch (Throwable error) {
            RmsHelper.close(store);
            error.printStackTrace();
        }
    }

    private void collectOrphanSlots(int totalRecords) {
        boolean[] used = new boolean[totalRecords];
        for (int i = this.entries.b() - 1; i >= 0; --i) {
            RmsEntry entry = (RmsEntry) this.entries.b(i);
            for (int j = entry.chunkRecordIds.length - 1; j >= 0; --j) {
                if (entry.chunkRecordIds[j] >= 0 && entry.chunkRecordIds[j] < used.length) {
                    used[entry.chunkRecordIds[j]] = true;
                }
            }
        }
        for (int i = this.freeSlots.b() - 1; i >= 0; --i) {
            int slotId = ((Integer) this.freeSlots.b(i)).intValue();
            if (slotId >= 0 && slotId < used.length) {
                used[slotId] = true;
            }
        }
        for (int slot = used.length - 1; slot > 2; --slot) {
            if (!used[slot]) {
                this.freeSlots.a(new Integer(slot));
            }
        }
    }

    private synchronized byte[] readEntry(int id) {
        RecordStore store = RmsHelper.open(this.storeName, false);
        try {
            RmsEntry entry = this.findEntry(id);
            if (entry == null || entry.chunkRecordIds == null || store == null) {
                RmsHelper.close(store);
                return null;
            }
            if (entry.pendingData != null) {
                RmsHelper.close(store);
                return entry.pendingData;
            }

            byte[] result = new byte[entry.byteLength];
            int copied = 0;
            int[] chunkIds = entry.chunkRecordIds;
            for (int i = 0; i < chunkIds.length; ++i) {
                byte[] chunk = RmsHelper.getRecord(store, chunkIds[i]);
                if (chunk == null) {
                    this.entries.b(entry);
                    this.markReclaimedSlots(entry.chunkRecordIds, 0, entry.chunkRecordIds.length);
                    RmsHelper.close(store);
                    return null;
                }
                int len = chunk.length;
                if (len > result.length - copied) {
                    len = result.length - copied;
                }
                System.arraycopy(chunk, 0, result, copied, len);
                copied += len;
            }
            RmsHelper.close(store);
            return result;
        } catch (Throwable error) {
            RmsHelper.close(store);
            error.printStackTrace();
            return null;
        }
    }

    private synchronized void writeEntry(int id, byte[] data, int offset, int length) {
        if (data == null) {
            return;
        }
        try {
            RmsEntry entry = this.findEntry(id);
            if (entry != null) {
                if (entry.chunkRecordIds != null) {
                    this.markReclaimedSlots(entry.chunkRecordIds, 0, entry.chunkRecordIds.length);
                }
            } else {
                entry = new RmsEntry();
                this.entries.a(entry);
            }

            entry.id = id;
            entry.byteLength = length;
            int chunkCount = length / this.chunkSize;
            if (length % this.chunkSize != 0) {
                ++chunkCount;
            }

            int[] allocatedSlots = new int[chunkCount];
            for (int i = 0; i < allocatedSlots.length; ++i) {
                int lastIdx = this.freeSlots.b() - 1;
                if (lastIdx >= 0) {
                    allocatedSlots[i] = ((Integer) this.freeSlots.b(lastIdx)).intValue();
                    this.freeSlots.a(lastIdx);
                } else {
                    allocatedSlots[i] = -1;
                }
            }

            entry.chunkRecordIds = allocatedSlots;
            entry.pendingData = new byte[length];
            System.arraycopy(data, offset, entry.pendingData, 0, length);
        } catch (Throwable error) {
            this.deleteEntry(id);
            error.printStackTrace();
        }
    }

    private synchronized void flushEntries() {
        RecordStore store = RmsHelper.open(this.storeName, false);
        try {
            if (store == null) {
                store = RmsHelper.open(this.storeName, true);
                this.initializeEmptyStore(store);
            }

            for (int i = this.entries.b() - 1; i >= 0; --i) {
                RmsEntry entry = (RmsEntry) this.entries.b(i);
                if (entry.pendingData != null) {
                    this.writeChunks(store, entry.pendingData, entry.chunkRecordIds);
                    entry.pendingData = null;
                }
            }

            byte[] header = new byte[this.headerSize];
            int offset = 0;
            System.arraycopy(ByteUtils.a(this.entries.b()), 0, header, offset, 4);
            offset += 4;

            for (int i = this.entries.b() - 1; i >= 0; --i) {
                RmsEntry entry = (RmsEntry) this.entries.b(i);
                byte[] serialized = entry.serialize();
                System.arraycopy(serialized, 0, header, offset, serialized.length);
                offset += serialized.length;
            }

            for (int i = this.reclaimedSlots.b() - 1; i >= 0; --i) {
                this.freeSlots.a(this.reclaimedSlots.b(i));
            }
            this.reclaimedSlots.a();

            System.arraycopy(ByteUtils.a(this.freeSlots.b()), 0, header, offset, 4);
            offset += 4;

            for (int i = this.freeSlots.b() - 1; i >= 0; --i) {
                int slotId = ((Integer) this.freeSlots.b(i)).intValue();
                System.arraycopy(ByteUtils.a(slotId), 0, header, offset, 4);
                offset += 4;
            }

            int nextHeaderSlot = this.activeHeaderSlot == 1 ? 2 : 1;
            RmsHelper.setRecord(store, nextHeaderSlot, header);
            RmsHelper.setRecord(store, 3, new byte[]{(byte) nextHeaderSlot});
            RmsHelper.close(store);
            this.activeHeaderSlot = nextHeaderSlot;
        } catch (Throwable error) {
            RmsHelper.close(store);
            error.printStackTrace();
        }
    }

    private void deleteEntry(int id) {
        try {
            for (int i = this.entries.b() - 1; i >= 0; --i) {
                RmsEntry entry = (RmsEntry) this.entries.b(i);
                if (entry.id == id) {
                    this.entries.a(i);
                    if (entry.chunkRecordIds != null) {
                        this.releaseFreeSlots(entry.chunkRecordIds, 0, entry.chunkRecordIds.length);
                    }
                }
            }
        } catch (Throwable error) {
            error.printStackTrace();
        }
    }

    private RmsEntry findEntry(int id) {
        try {
            for (int i = this.entries.b() - 1; i >= 0; --i) {
                RmsEntry entry = (RmsEntry) this.entries.b(i);
                if (entry.id == id) {
                    return entry;
                }
            }
        } catch (Throwable error) {
            error.printStackTrace();
        }
        return null;
    }

    private void releaseFreeSlots(int[] slots, int offset, int count) {
        try {
            if (slots == null) {
                return;
            }
            int end = offset + count;
            for (int i = offset; i < end; ++i) {
                this.freeSlots.a(new Integer(slots[i]));
            }
        } catch (Throwable error) {
            error.printStackTrace();
        }
    }

    private void markReclaimedSlots(int[] slots, int offset, int count) {
        try {
            if (slots == null) {
                return;
            }
            int end = offset + count;
            for (int i = offset; i < end; ++i) {
                this.reclaimedSlots.a(new Integer(slots[i]));
            }
        } catch (Throwable error) {
            error.printStackTrace();
        }
    }

    private void writeChunks(RecordStore store, byte[] data, int[] slotIds) {
        try {
            byte[] chunkBuf = new byte[this.chunkSize];
            int offset = 0;
            for (int i = 0; i < slotIds.length; ++i) {
                int len = data.length - offset;
                if (len > chunkBuf.length) {
                    len = chunkBuf.length;
                }
                System.arraycopy(data, offset, chunkBuf, 0, len);
                offset += len;
                if (slotIds[i] > 3) {
                    RmsHelper.setRecord(store, slotIds[i], chunkBuf);
                } else {
                    slotIds[i] = RmsHelper.addRecord(store, chunkBuf);
                }
            }
        } catch (Throwable error) {
            error.printStackTrace();
        }
    }

    private boolean hasEntry(int id) {
        return this.findEntry(id) != null;
    }

    public static RmsBank open(String storeName, int chunkSize) {
        return new RmsBank(storeName, chunkSize);
    }

    public static boolean exists(RmsBank bank, int id) {
        return bank.hasEntry(id);
    }

    public static void write(RmsBank bank, int id, byte[] data) {
        bank.writeEntry(id, data, 0, data.length);
    }

    public static void write(RmsBank bank, int id, byte[] data, int offset, int length) {
        bank.writeEntry(id, data, offset, length);
    }

    public static byte[] read(RmsBank bank, int id) {
        return bank.readEntry(id);
    }

    public static void delete(RmsBank bank, int id) {
        bank.deleteEntry(id);
    }

    public static void flush(RmsBank bank) {
        bank.flushEntries();
    }
}



