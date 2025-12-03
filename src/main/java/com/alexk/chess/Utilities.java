package com.alexk.chess;

/**
 * Utility class providing helper methods for coordinate conversion.
 * <p>
 * This class converts between character representation of chess board columns
 * (A-H) and their corresponding integer values (1-8).
 * </p>
 *
 * @author Alex K
 * @version 1.0
 */
public class Utilities {

    /**
     * Converts a character column designation to its integer equivalent.
     * <p>
     * Maps A->1, B->2, ..., H->8
     * </p>
     *
     * @param c the column character (A-H, case-insensitive)
     * @return the corresponding integer (1-8)
     * @throws IllegalArgumentException if character is outside A-H range
     */
    public static int char2Int(char c) {
        return c - 64;  // 'A' is ASCII 65, so 'A' - 64 = 1
    }

    /**
     * Converts an integer column value to its character equivalent.
     * <p>
     * Maps 1->A, 2->B, ..., 8->H
     * </p>
     *
     * @param d the column integer (1-8)
     * @return the corresponding character (A-H)
     * @throws IllegalArgumentException if integer is outside 1-8 range
     */
    public static char int2Char(int d) {
        return (char) (d + 64);  // 1 + 64 = 65 = 'A'
    }
}