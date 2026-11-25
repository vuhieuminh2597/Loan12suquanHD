package com.loan12suquan.hd;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * High-Definition 44.1 kHz 16-bit PCM Combat & UI Sound Effect Synthesizer.
 * <p>
 * Replaces legacy 8 kHz 4.75 kbps mono telephone AMR clips with crisp 44.1 kHz
 * stereo-grade PCM WAV sound effects for all 14 battle, spell, and world map cues.
 */
public final class HdAudioSynthesizer {
    private static final int SAMPLE_RATE = 44100;
    private static final Map<String, byte[]> WAV_CACHE = Collections.synchronizedMap(new HashMap<String, byte[]>());

    private HdAudioSynthesizer() {
    }

    public static byte[] getOrCreateWav(String clipName) {
        String key = (clipName == null || clipName.isEmpty()) ? "explodechess" : clipName.toLowerCase();
        byte[] cached = WAV_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        float[] samples = synthesizeSamples(key);
        byte[] wav = encodePcm16Wav(samples);
        WAV_CACHE.put(key, wav);
        return wav;
    }

    private static float[] synthesizeSamples(String key) {
        Random rng = new Random(key.hashCode());
        if (key.equals("attack")) {
            // Sharp metallic sword whoosh + blade impact clang
            int len = (int) (SAMPLE_RATE * 0.34f);
            float[] out = new float[len];
            for (int i = 0; i < len; ++i) {
                float t = (float) i / SAMPLE_RATE;
                float whooshEnv = t < 0.14f ? (t / 0.14f) : (float) Math.exp(-(t - 0.14f) * 18f);
                float noise = (rng.nextFloat() * 2f - 1f) * whooshEnv * 0.45f;
                float clangEnv = t >= 0.08f ? (float) Math.exp(-(t - 0.08f) * 14f) : 0f;
                float clang = (float) (Math.sin(2 * Math.PI * 1180 * t) * 0.45
                        + Math.sin(2 * Math.PI * 2420 * t) * 0.35
                        + Math.sin(2 * Math.PI * 3890 * t) * 0.20) * clangEnv;
                out[i] = clamp(noise + clang);
            }
            return out;
        }
        if (key.equals("explodechess")) {
            // Crisp crystalline gem match chime + resonant pop
            int len = (int) (SAMPLE_RATE * 0.28f);
            float[] out = new float[len];
            for (int i = 0; i < len; ++i) {
                float t = (float) i / SAMPLE_RATE;
                float env = (float) Math.exp(-t * 13f);
                float pitch = 587.33f + (t < 0.06f ? 0f : 293.66f); // D5 -> A5 chime
                float tone = (float) (Math.sin(2 * Math.PI * pitch * t) * 0.55
                        + Math.sin(2 * Math.PI * (pitch * 2f) * t) * 0.30
                        + Math.sin(2 * Math.PI * (pitch * 3f) * t) * 0.15);
                out[i] = clamp(tone * env * 0.85f);
            }
            return out;
        }
        if (key.startsWith("fire")) {
            // Roaring flame surge + deep low-end ember burst (tier 1..3)
            int tier = key.endsWith("3") ? 3 : (key.endsWith("2") ? 2 : 1);
            int len = (int) (SAMPLE_RATE * (0.42f + tier * 0.12f));
            float[] out = new float[len];
            float lp = 0f;
            for (int i = 0; i < len; ++i) {
                float t = (float) i / SAMPLE_RATE;
                float env = (float) (Math.min(1.0, t * 18.0) * Math.exp(-t * (4.2f - tier * 0.6f)));
                float rawNoise = rng.nextFloat() * 2f - 1f;
                lp = lp * 0.86f + rawNoise * 0.14f;
                float sub = (float) Math.sin(2 * Math.PI * (110f - t * 40f) * t) * 0.45f;
                float crackle = (rng.nextFloat() > 0.96f) ? 0.5f : 0f;
                out[i] = clamp((lp * 1.4f + sub + crackle * 0.3f) * env);
            }
            return out;
        }
        if (key.startsWith("lightning")) {
            // Electric thunder snap + high-voltage arc crackle
            int tier = key.endsWith("3") ? 3 : (key.endsWith("2") ? 2 : 1);
            int len = (int) (SAMPLE_RATE * (0.38f + tier * 0.10f));
            float[] out = new float[len];
            for (int i = 0; i < len; ++i) {
                float t = (float) i / SAMPLE_RATE;
                float env = (float) Math.exp(-t * (6.5f - tier * 0.8f));
                float zapFreq = 920f + (float) Math.sin(t * 95f) * 480f;
                float zap = (float) Math.sin(2 * Math.PI * zapFreq * t) * 0.45f;
                float spark = (rng.nextFloat() * 2f - 1f) * 0.55f;
                out[i] = clamp((zap + spark) * env);
            }
            return out;
        }
        if (key.startsWith("ice")) {
            // Shimmering crystalline frost bell + glacial shatter
            int tier = key.endsWith("3") ? 3 : (key.endsWith("2") ? 2 : 1);
            int len = (int) (SAMPLE_RATE * (0.40f + tier * 0.10f));
            float[] out = new float[len];
            float[] freqs = new float[]{1046.5f, 1318.5f, 1567.98f, 2093.0f};
            for (int i = 0; i < len; ++i) {
                float t = (float) i / SAMPLE_RATE;
                float env = (float) Math.exp(-t * 6.8f);
                float sum = 0f;
                for (int f = 0; f < freqs.length; ++f) {
                    sum += (float) Math.sin(2 * Math.PI * freqs[f] * t) * 0.22f;
                }
                float frostNoise = (rng.nextFloat() * 2f - 1f) * 0.18f * (float) Math.exp(-t * 14f);
                out[i] = clamp((sum + frostNoise) * env);
            }
            return out;
        }
        if (key.equals("lvu")) {
            // Triumphant 4-note imperial brass/guzheng level-up fanfare (C5 - E5 - G5 - C6)
            int len = (int) (SAMPLE_RATE * 0.72f);
            float[] out = new float[len];
            float[] notes = new float[]{523.25f, 659.25f, 783.99f, 1046.50f};
            for (int i = 0; i < len; ++i) {
                float t = (float) i / SAMPLE_RATE;
                int noteIdx = Math.min(3, (int) (t / 0.14f));
                float localT = t - noteIdx * 0.14f;
                float env = (float) Math.exp(-localT * (noteIdx == 3 ? 4.5f : 9.0f));
                float freq = notes[noteIdx];
                float sig = (float) (Math.sin(2 * Math.PI * freq * t) * 0.55
                        + Math.sin(2 * Math.PI * freq * 2f * t) * 0.28
                        + Math.sin(2 * Math.PI * freq * 3f * t) * 0.14);
                out[i] = clamp(sig * env * 0.85f);
            }
            return out;
        }
        if (key.equals("vs")) {
            // Dramatic war-drum thud + crossed-swords metallic ring
            int len = (int) (SAMPLE_RATE * 0.55f);
            float[] out = new float[len];
            for (int i = 0; i < len; ++i) {
                float t = (float) i / SAMPLE_RATE;
                float drumEnv = (float) Math.exp(-t * 9.5f);
                float drumFreq = 135f * (float) Math.exp(-t * 6f) + 48f;
                float drum = (float) Math.sin(2 * Math.PI * drumFreq * t) * drumEnv * 0.65f;
                float ringEnv = t > 0.05f ? (float) Math.exp(-(t - 0.05f) * 6.0f) : 0f;
                float ring = (float) (Math.sin(2 * Math.PI * 1480f * t) * 0.30
                        + Math.sin(2 * Math.PI * 2210f * t) * 0.22) * ringEnv;
                out[i] = clamp(drum + ring);
            }
            return out;
        }
        // Default / "useitem": soothing jade potion/scroll activation chime (G5 -> B5 -> D6)
        int len = (int) (SAMPLE_RATE * 0.38f);
        float[] out = new float[len];
        float[] notes = new float[]{783.99f, 987.77f, 1174.66f};
        for (int i = 0; i < len; ++i) {
            float t = (float) i / SAMPLE_RATE;
            int idx = Math.min(2, (int) (t / 0.10f));
            float localT = t - idx * 0.10f;
            float env = (float) Math.exp(-localT * 8.5f);
            float f = notes[idx];
            float sig = (float) (Math.sin(2 * Math.PI * f * t) * 0.60 + Math.sin(2 * Math.PI * f * 2f * t) * 0.25);
            out[i] = clamp(sig * env * 0.8f);
        }
        return out;
    }

    private static float clamp(float v) {
        return v < -1.0f ? -1.0f : (v > 1.0f ? 1.0f : v);
    }

    private static byte[] encodePcm16Wav(float[] samples) {
        int numSamples = samples.length;
        int dataSize = numSamples * 2;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(44 + dataSize);
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            // RIFF header
            dos.writeBytes("RIFF");
            writeLittleEndianInt(dos, 36 + dataSize);
            dos.writeBytes("WAVE");
            // fmt subchunk
            dos.writeBytes("fmt ");
            writeLittleEndianInt(dos, 16); // PCM
            writeLittleEndianShort(dos, 1); // AudioFormat = 1 (PCM)
            writeLittleEndianShort(dos, 1); // Channels = 1 (Mono)
            writeLittleEndianInt(dos, SAMPLE_RATE);
            writeLittleEndianInt(dos, SAMPLE_RATE * 2);
            writeLittleEndianShort(dos, 2);
            writeLittleEndianShort(dos, 16);
            // data subchunk
            dos.writeBytes("data");
            writeLittleEndianInt(dos, dataSize);
            for (int i = 0; i < numSamples; ++i) {
                int pcm = (int) (clamp(samples[i]) * 32767.0f);
                writeLittleEndianShort(dos, pcm);
            }
            dos.flush();
        } catch (IOException ignored) {
        }
        return baos.toByteArray();
    }

    private static void writeLittleEndianInt(DataOutputStream dos, int v) throws IOException {
        dos.writeByte(v & 0xFF);
        dos.writeByte((v >>> 8) & 0xFF);
        dos.writeByte((v >>> 16) & 0xFF);
        dos.writeByte((v >>> 24) & 0xFF);
    }

    private static void writeLittleEndianShort(DataOutputStream dos, int v) throws IOException {
        dos.writeByte(v & 0xFF);
        dos.writeByte((v >>> 8) & 0xFF);
    }
}
