package com.mg.engine.util;

/**
 * 16.16 fixed-point arithmetic helpers (no floating point on CLDC 1.0).
 */
public class FixedPointMath {
    public static final int FIXED_SHIFT = 16;
    public static final int FIXED_ONE = 1 << FIXED_SHIFT;

    public static final int DEG_90 = fromInt(90);
    public static final int DEG_180 = fromInt(180);
    public static final int DEG_270 = fromInt(270);
    public static final int DEG_360 = fromInt(360);

    public static int fromInt(int value) {
        return value << FIXED_SHIFT;
    }

    public static int toInt(int fixed) {
        return fixed >> FIXED_SHIFT;
    }

    public static int mul(int a, int b) {
        return (int) (((long) a * (long) b) >> FIXED_SHIFT);
    }

    public static int div(int numerator, int denominator) {
        return denominator > 4 ? (int) (((long) numerator << FIXED_SHIFT) / (long) denominator) : 0;
    }

    /** Computes {@code sqrt(fixed)} using 8 Newton-Raphson iterations. */
    public static int sqrt(int fixed) {
        int guess = (fixed + FIXED_ONE) >> 1;
        for (int i = 0; i < 8; ++i) {
            guess = (guess + div(fixed, guess)) >> 1;
        }
        return guess;
    }
}

