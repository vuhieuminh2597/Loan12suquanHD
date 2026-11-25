package javax.microedition.rms;

import com.loan12suquan.hd.platform.HdPlatform;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cross-platform implementation of MIDP {@link RecordStore} backed by {@code saves/*.rms}
 * on Desktop and {@code localStorage} on Web / iPhone 17 PWA, with portable {@code .sqhd}
 * backup export/import support across PC and iPhone.
 */
public class RecordStore {
    private static final int MAGIC = 0x53514844; // "SQHD"
    private static final int BACKUP_BUNDLE_MAGIC = 0x5351424B; // "SQBK"
    private static final Map<String, RecordStore> CACHE = new HashMap<String, RecordStore>();

    private static final String[] KNOWN_STORE_NAMES = new String[]{
            "sq_sys", "sq_data", "sq_camp", "sq_snap", "sq_relic", "sq_hd_cfg",
            "sys", "data", "save", "config", "suquan", "Option", "SaveGame"
    };

    private final String name;
    private final List<byte[]> records = new ArrayList<byte[]>(); // 0-indexed list for 1-based recordId

    private RecordStore(String name) {
        this.name = name;
    }

    public static synchronized RecordStore openRecordStore(String recordStoreName, boolean createIfNecessary) throws Exception {
        RecordStore existing = CACHE.get(recordStoreName);
        if (existing != null) {
            return existing;
        }
        byte[] raw = HdPlatform.backend().loadRecordStoreBytes(recordStoreName);
        if (raw == null) {
            if (!createIfNecessary) {
                return null;
            }
            RecordStore created = new RecordStore(recordStoreName);
            created.saveToBackend();
            CACHE.put(recordStoreName, created);
            return created;
        }
        RecordStore store = new RecordStore(recordStoreName);
        store.loadFromBytes(raw);
        CACHE.put(recordStoreName, store);
        return store;
    }

    public static synchronized void deleteRecordStore(String recordStoreName) {
        CACHE.remove(recordStoreName);
        HdPlatform.backend().deleteRecordStoreBytes(recordStoreName);
    }

    public synchronized void closeRecordStore() {
        saveToBackend();
    }

    public synchronized int getNumRecords() {
        int count = 0;
        for (int i = 0; i < this.records.size(); ++i) {
            if (this.records.get(i) != null) {
                ++count;
            }
        }
        return count;
    }

    public synchronized int addRecord(byte[] data, int offset, int numBytes) {
        byte[] copy = new byte[Math.max(0, numBytes)];
        if (data != null && numBytes > 0) {
            System.arraycopy(data, offset, copy, 0, numBytes);
        }
        this.records.add(copy);
        saveToBackend();
        return this.records.size();
    }

    public synchronized void setRecord(int recordId, byte[] newData, int offset, int numBytes) {
        int idx = recordId - 1;
        while (this.records.size() <= idx) {
            this.records.add(new byte[0]);
        }
        byte[] copy = new byte[Math.max(0, numBytes)];
        if (newData != null && numBytes > 0) {
            System.arraycopy(newData, offset, copy, 0, numBytes);
        }
        this.records.set(idx, copy);
        saveToBackend();
    }

    public synchronized byte[] getRecord(int recordId) {
        int idx = recordId - 1;
        if (idx < 0 || idx >= this.records.size()) {
            return null;
        }
        byte[] data = this.records.get(idx);
        if (data == null) {
            return null;
        }
        byte[] copy = new byte[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        return copy;
    }

    public synchronized int getRecord(int recordId, byte[] buffer, int offset) {
        byte[] data = getRecord(recordId);
        if (data == null) {
            return 0;
        }
        System.arraycopy(data, 0, buffer, offset, data.length);
        return data.length;
    }

    public synchronized int getRecordSize(int recordId) {
        int idx = recordId - 1;
        if (idx < 0 || idx >= this.records.size() || this.records.get(idx) == null) {
            return 0;
        }
        return this.records.get(idx).length;
    }

    public synchronized void deleteRecord(int recordId) {
        int idx = recordId - 1;
        if (idx >= 0 && idx < this.records.size()) {
            this.records.set(idx, null);
            saveToBackend();
        }
    }

    private byte[] serializeToBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(MAGIC);
        dos.writeInt(this.records.size());
        for (int i = 0; i < this.records.size(); ++i) {
            byte[] rec = this.records.get(i);
            if (rec == null) {
                dos.writeInt(-1);
            } else {
                dos.writeInt(rec.length);
                dos.write(rec);
            }
        }
        dos.flush();
        return baos.toByteArray();
    }

    private void saveToBackend() {
        try {
            HdPlatform.backend().saveRecordStoreBytes(this.name, serializeToBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromBytes(byte[] raw) {
        this.records.clear();
        if (raw == null || raw.length < 8) {
            return;
        }
        try {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(raw));
            int magic = dis.readInt();
            if (magic != MAGIC) {
                return;
            }
            int count = dis.readInt();
            for (int i = 0; i < count; ++i) {
                int len = dis.readInt();
                if (len < 0) {
                    this.records.add(null);
                } else {
                    byte[] buf = new byte[len];
                    dis.readFully(buf);
                    this.records.add(buf);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Exports all active & known RecordStores into a single portable {@code .sqhd} byte array. */
    public static synchronized byte[] exportAllStoresToBytes() throws IOException {
        Map<String, byte[]> dump = new HashMap<String, byte[]>();
        for (Map.Entry<String, RecordStore> entry : CACHE.entrySet()) {
            dump.put(entry.getKey(), entry.getValue().serializeToBytes());
        }
        for (int i = 0; i < KNOWN_STORE_NAMES.length; ++i) {
            String k = KNOWN_STORE_NAMES[i];
            if (!dump.containsKey(k)) {
                byte[] b = HdPlatform.backend().loadRecordStoreBytes(k);
                if (b != null && b.length > 0) {
                    dump.put(k, b);
                }
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(BACKUP_BUNDLE_MAGIC);
        dos.writeInt(dump.size());
        for (Map.Entry<String, byte[]> e : dump.entrySet()) {
            dos.writeUTF(e.getKey());
            dos.writeInt(e.getValue().length);
            dos.write(e.getValue());
        }
        dos.flush();
        return baos.toByteArray();
    }

    /** Restores all RecordStores from a {@code .sqhd} backup byte array and clears in-memory cache. */
    public static synchronized boolean importAllStoresFromBytes(byte[] bundle) {
        if (bundle == null || bundle.length < 8) {
            return false;
        }
        try {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bundle));
            if (dis.readInt() != BACKUP_BUNDLE_MAGIC) {
                return false;
            }
            int count = dis.readInt();
            if (count < 0 || count > 64) {
                return false;
            }
            CACHE.clear();
            for (int i = 0; i < count; ++i) {
                String storeName = dis.readUTF();
                int len = dis.readInt();
                byte[] data = new byte[len];
                dis.readFully(data);
                HdPlatform.backend().saveRecordStoreBytes(storeName, data);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
