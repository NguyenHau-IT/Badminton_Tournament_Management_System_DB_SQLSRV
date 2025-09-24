package com.example.btms.util.time;

import java.time.Duration;
import java.time.Instant;

public class TimeUtil {
    public static long secondsSince(Instant start) {
        return Duration.between(start, Instant.now()).getSeconds();
    }

    public static String formatElapsed(long sec) {
        long m = sec / 60;
        long s = sec % 60;
        return String.format("%02d:%02d", m, s);
    }
}
