package com.mg.engine.storage;

import javax.microedition.rms.RecordStore;

/** Low-level exception-safe wrapper around MIDP {@link RecordStore} operations. */
public final class RmsHelper {
    private RmsHelper() {
    }

    public static RecordStore open(String storeName, boolean createIfNecessary) {
        try {
            return RecordStore.openRecordStore(storeName, createIfNecessary);
        } catch (Throwable error) {
            error.printStackTrace();
            return null;
        }
    }

    public static void close(RecordStore store) {
        if (store == null) {
            return;
        }
        try {
            store.closeRecordStore();
        } catch (Throwable error) {
            error.printStackTrace();
        }
    }

    public static byte[] getRecord(RecordStore store, int recordId) {
        try {
            return store.getRecord(recordId);
        } catch (Throwable error) {
            error.printStackTrace();
            return null;
        }
    }

    public static int setRecord(RecordStore store, int recordId, byte[] data, int offset, int length) {
        try {
            store.setRecord(recordId, data, offset, length);
            return recordId;
        } catch (Throwable error) {
            error.printStackTrace();
            return -1;
        }
    }

    public static int setRecord(RecordStore store, int recordId, byte[] data) {
        return setRecord(store, recordId, data, 0, data.length);
    }

    public static int addRecord(RecordStore store, byte[] data, int offset, int length) {
        try {
            return store.addRecord(data, offset, length);
        } catch (Throwable error) {
            error.printStackTrace();
            return -1;
        }
    }

    public static int addRecord(RecordStore store, byte[] data) {
        return addRecord(store, data, 0, data.length);
    }

    public static int getNumRecords(RecordStore store) {
        try {
            return store.getNumRecords();
        } catch (Throwable error) {
            error.printStackTrace();
            return 0;
        }
    }

    public static void deleteStore(String storeName) {
        try {
            RecordStore.deleteRecordStore(storeName);
        } catch (Throwable error) {
            error.printStackTrace();
        }
    }
}



