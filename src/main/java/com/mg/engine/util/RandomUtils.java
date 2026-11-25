package com.mg.engine.util;

import java.util.Random;

/** Shared PRNG helper. */
public class RandomUtils {
    public static final Random RANDOM = new Random(System.currentTimeMillis());

    public static final int nextInt(int bound) {
        return RANDOM.nextInt(bound);
    }

    public static final int nextInt() {
        return RANDOM.nextInt();
    }

    public static final int nextRange(int minInclusive, int maxInclusive) {
        return RANDOM.nextInt(maxInclusive - minInclusive + 1) + minInclusive;
    }
}



