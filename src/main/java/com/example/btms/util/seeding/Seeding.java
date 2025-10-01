package com.example.btms.util.seeding;

import java.util.ArrayList;
import java.util.List;

/**
 * Seeding mode mappings for bracket positions.
 * Returns position indices (0-based) for seeds in order (seed#1 at index 0).
 * Only 8-slot mappings are explicitly defined for modes 1..7 as per request.
 * For other sizes or undefined modes, return null so caller can fallback.
 */
public final class Seeding {
    private Seeding() {
    }

    /**
     * Get position list for N participants within a block of size M using given
     * mode.
     * If mapping is not defined, return null to let caller fallback.
     */
    public static List<Integer> positionsFor(int N, int M, int mode) {
        int[] map = mappingFor(M, mode);
        if (map == null)
            return null;
        int k = Math.min(N, map.length);
        List<Integer> out = new ArrayList<>(k);
        for (int i = 0; i < k; i++)
            out.add(map[i]);
        return out;
    }

    /**
     * Return full mapping array (length M) of target positions (0-based) per seed
     * index.
     */
    public static int[] mappingFor(int M, int mode) {
        if (M == 8) {
            return switch (mode) {
                case 1 -> // [1,5,3,7,2,6,4,8] -> 0-based
                    new int[] { 0, 4, 2, 6, 1, 5, 3, 7 };
                case 2 -> // [8,4,6,2,7,3,5,1]
                    new int[] { 7, 3, 5, 1, 6, 2, 4, 0 };
                case 3 -> // [1,8,5,4,3,6,7,2]
                    new int[] { 0, 7, 4, 3, 2, 5, 6, 1 };
                case 4 -> // [1,2,4,3,5,6,7,8]
                    new int[] { 0, 1, 3, 2, 4, 5, 6, 7 };
                case 5 -> // [8,7,6,5,4,3,2,1]
                    new int[] { 7, 6, 5, 4, 3, 2, 1, 0 };
                case 6 -> // [8,3,6,2,7,4,5,1]
                    new int[] { 7, 2, 5, 1, 6, 3, 4, 0 };
                case 7 -> // [1,8,4,5,2,7,3,6]
                    new int[] { 0, 7, 3, 4, 1, 6, 2, 5 };
                default -> null;
            };
        }
        // Can extend with M=4,16 if needed. Null -> fallback to existing algorithm
        return null;
    }

    /** Preview text for UI for an 8-slot bracket. */
    public static String previewForMode(int mode) {
        int[] map = mappingFor(8, mode);
        if (map == null)
            return "[top-heavy]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < map.length; i++) {
            if (i > 0)
                sb.append(',');
            sb.append(map[i] + 1); // show 1-based in UI
        }
        sb.append(']');
        return sb.toString();
    }
}
