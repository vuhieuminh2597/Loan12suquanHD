package com.mg.engine.util;

/**
 * Integer square root helper (CLDC 1.0 safe, no {@code double}).
 */
public class MathUtils {
    /** Returns {@code floor(sqrt(value))} for non-negative {@code long} inputs. */
    public static long isqrt(long value) {
        long prev = 1L;
        long next = (prev + value / prev) >> 1;
        while (Math.abs(next - prev) > 1L) {
            prev = next;
            next = (next + value / next) >> 1;
        }
        while (next * next > value) {
            --next;
        }
        return next;
    }
}

