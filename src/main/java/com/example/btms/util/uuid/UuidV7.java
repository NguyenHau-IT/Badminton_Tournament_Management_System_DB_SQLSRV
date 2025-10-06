package com.example.btms.util.uuid;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Minimal UUID v7 generator (time-ordered) per draft RFC 4122bis.
 * Layout:
 * - 48 bits: Unix epoch milliseconds
 * - 4 bits: version (0111b)
 * - 12 bits: random_a
 * - 2 bits: variant (10b)
 * - 62 bits: random_b
 */
public final class UuidV7 {
    private UuidV7() {
    }

    public static String generate() {
        long unixMillis = System.currentTimeMillis() & 0x0000FFFFFFFFFFFFL; // 48 bits

        // random parts
        long randA = ThreadLocalRandom.current().nextLong() & 0x0FFFL; // 12 bits
        long randB = ThreadLocalRandom.current().nextLong(); // 64 bits, we'll mask below

        // Build MSB: 48-bit time | version(7) | 12-bit randA
        long msb = (unixMillis << 16);
        msb |= 0x7000L; // version 7 in bits 12-15 of the low 16 bits of MSB
        msb |= randA;

        // Build LSB: variant(10) in top 2 bits | 62-bit randB
        long lsb = randB & 0x3FFFFFFFFFFFFFFFL; // clear top 2 bits
        lsb |= 0x8000000000000000L; // set variant to 10xx

        return new UUID(msb, lsb).toString();
    }
}
