package com.mg.engine.util;

import java.io.UnsupportedEncodingException;

/** CLDC-compatible string splitting, replacement, and UTF-8 encoding utilities. */
public class StringUtils {
    public static boolean isBlank(String text) {
        if (text == null) {
            return true;
        }
        String trimmed = text.trim();
        return trimmed.length() == 0;
    }

    public static boolean equalsIgnoreCase(String first, String second) {
        if (first == null || second == null) {
            return false;
        }
        return first.toLowerCase().equals(second.toLowerCase());
    }

    public static String[] split(String text, String delimiter) {
        return split(text, delimiter, -1, false);
    }

    public static String[] split(String text, String delimiter, int limit, boolean ignoreCase) {
        if (limit == 0 || text == null) {
            return null;
        }
        if (delimiter == null) {
            return new String[]{text};
        }

        ObjectList parts = new ObjectList();
        String remaining = text;
        int delimiterLen = delimiter.length();
        int idx = ignoreCase ? text.toLowerCase().indexOf(delimiter.toLowerCase()) : text.indexOf(delimiter);
        if (idx < 0) {
            return new String[]{text};
        }

        while (idx >= 0) {
            if (limit == 1) {
                if (!isBlank(remaining)) {
                    parts.a(remaining);
                }
                return toStringArray(parts);
            }
            if (idx > 0) {
                parts.a(remaining.substring(0, idx));
                if (limit > 0) {
                    --limit;
                }
            }
            remaining = remaining.substring(idx + delimiterLen);
            idx = ignoreCase ? remaining.toLowerCase().indexOf(delimiter.toLowerCase()) : remaining.indexOf(delimiter);
        }

        if (!isBlank(remaining)) {
            parts.a(remaining);
        }
        return toStringArray(parts);
    }

    public static String[] toStringArray(ObjectList list) {
        if (list == null || list.b() == 0) {
            return null;
        }
        int count = list.b();
        String[] result = new String[count];
        for (int i = 0; i < count; ++i) {
            result[i] = (String) list.b(i);
        }
        return result;
    }

    public static byte[] toUtf8Bytes(String text) throws UnsupportedEncodingException {
        return toBytes(text, null);
    }

    public static byte[] toBytes(String text, String charset) throws UnsupportedEncodingException {
        if (charset == null) {
            charset = "UTF-8";
        }
        return text.getBytes(charset);
    }

    public static String fromUtf8Bytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return new String(bytes, 0, bytes.length, "UTF-8");
        } catch (Throwable fallback) {
            return new String(bytes, 0, bytes.length);
        }
    }

    public static String replace(String text, String target, String replacement) {
        int start = 0;
        int matchIdx;
        StringBuffer buffer = new StringBuffer();
        while ((matchIdx = text.indexOf(target, start)) >= 0) {
            buffer.append(text.substring(start, matchIdx));
            buffer.append(replacement);
            start = matchIdx + target.length();
        }
        buffer.append(text.substring(start));
        return buffer.toString();
    }
}



