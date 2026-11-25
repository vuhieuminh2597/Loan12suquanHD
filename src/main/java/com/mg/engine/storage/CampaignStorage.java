package com.mg.engine.storage;

import com.mg.engine.util.ByteUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Campaign hero progress and mid-battle resume snapshot persistence. */
public class CampaignStorage extends EngineStorage {
    public static final int RECORD_HERO_DATA = 100;
    public static final int RECORD_HERO_NAME = 104;
    public static final int RECORD_BATTLE_SNAPSHOT = 106;

    public static final void clearCampaignSave() {
        RmsBank.delete(EngineStorage.b, RECORD_HERO_DATA);
        RmsBank.delete(EngineStorage.b, 101);
        RmsBank.delete(EngineStorage.b, 103);
        RmsBank.delete(EngineStorage.b, RECORD_HERO_NAME);
        RmsBank.delete(EngineStorage.b, 105);
        clearBattleSnapshot();
        EngineStorage.a();
    }

    public static final void writeString(int recordId, String value) {
        try {
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            DataOutputStream dataOut = new DataOutputStream(bytesOut);
            dataOut.writeUTF(value);
            dataOut.flush();
            RmsBank.write(EngineStorage.b, recordId, bytesOut.toByteArray());
            RmsBank.flush(EngineStorage.b);
            dataOut.close();
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    public static final String readString(int recordId) {
        try {
            byte[] raw = RmsBank.read(EngineStorage.b, recordId);
            if (raw == null) {
                return "";
            }
            DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(raw));
            String result = dataIn.readUTF();
            dataIn.close();
            return result;
        } catch (IOException error) {
            error.printStackTrace();
            return null;
        }
    }

    public static final void writeInt(int recordId, int value) {
        RmsBank.write(EngineStorage.b, recordId, ByteUtils.a(value));
        RmsBank.flush(EngineStorage.b);
    }

    public static final int readInt(int recordId) {
        byte[] raw = RmsBank.read(EngineStorage.b, recordId);
        return raw == null ? 0 : ByteUtils.a(raw);
    }

    public static final void saveHeroData(byte[] heroBytes) {
        RmsBank.write(EngineStorage.b, RECORD_HERO_DATA, heroBytes);
        RmsBank.flush(EngineStorage.b);
    }

    public static final byte[] loadHeroData() {
        return RmsBank.read(EngineStorage.b, RECORD_HERO_DATA);
    }

    public static final boolean hasHeroSave() {
        return RmsBank.exists(EngineStorage.b, RECORD_HERO_DATA);
    }

    public static final void saveBattleSnapshot(byte[] snapshot, int offset, int length) {
        RmsBank.write(EngineStorage.c, RECORD_BATTLE_SNAPSHOT, snapshot, offset, length);
        RmsBank.flush(EngineStorage.c);
    }

    public static final byte[] loadBattleSnapshot() {
        return RmsBank.read(EngineStorage.c, RECORD_BATTLE_SNAPSHOT);
    }

    public static final boolean hasBattleSnapshot() {
        return RmsBank.exists(EngineStorage.c, RECORD_BATTLE_SNAPSHOT);
    }

    public static final void clearBattleSnapshot() {
        RmsBank.delete(EngineStorage.c, RECORD_BATTLE_SNAPSHOT);
        RmsBank.flush(EngineStorage.c);
    }
}



