package de.gigabitzauber.jancontrol.util;

public final class JcCruiseUtil {

    private JcCruiseUtil() {

    }

    /**
     * Nearest multiple algorithm without using round and double precision div.
     */
    public static int getNearestMultiple(int input, int multiplier) {
        int remainder = input % multiplier;
        if (remainder < (multiplier + 1) / 2) {
            return input - remainder;
        } else {
            return input + (multiplier - remainder);
        }
    }
}
