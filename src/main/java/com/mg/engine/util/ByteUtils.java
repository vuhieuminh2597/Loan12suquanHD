package com.mg.engine.util;

/** Big-endian 32-bit integer serialization helpers. */
public class ByteUtils {
    public static int a(byte[] data) {
        return a(data[0], data[1], data[2], data[3]);
    }

    public static int a(byte b0, byte b1, byte b2, byte b3) {
        return ((b0 & 255) << 24) + ((b1 & 255) << 16) + ((b2 & 255) << 8) + (b3 & 255);
    }

    public static int a(byte[] data, int offset) {
        return a(data[offset], data[offset + 1], data[offset + 2], data[offset + 3]);
    }

    public static byte[] a(int value) {
        return new byte[]{
                (byte) (value >>> 24 & 255),
                (byte) (value >>> 16 & 255),
                (byte) (value >>> 8 & 255),
                (byte) (value & 255)
        };
    }
}



