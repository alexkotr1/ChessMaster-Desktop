package com.alexk.chess;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class for file operations.
 * <p>
 * Provides methods for reading from and writing to files with UTF-8 encoding
 * and proper error handling.
 * </p>
 *
 * @author Alex K
 * @version 1.0
 */
public class FileManager {

    /**
     * Reads the entire contents of a file into a string.
     *
     * @param filePath the path to the file to read
     * @return the contents of the file as a string, or null if an error occurs
     */
    public static String readFileToString(String filePath) {
        try {
            return Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[FileManager] Error reading file: " + filePath);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Writes a string to a file.
     * <p>
     * Creates parent directories if they don't exist and creates the file
     * if it doesn't exist.
     * </p>
     *
     * @param filePath the path to the file to write
     * @param content  the content to write to the file
     */
    public static void writeStringToFile(String filePath, String content) {
        try {
            Path path = Path.of(filePath);

            // Create parent directories if they don't exist
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            // Create file if it doesn't exist
            if (!Files.exists(path)) {
                Files.createFile(path);
            }

            Files.writeString(path, content, StandardCharsets.UTF_8);

        } catch (IOException e) {
            System.err.println("[FileManager] Error writing to file: " + filePath);
            e.printStackTrace();
        }
    }

    /**
     * Deletes a file if it exists.
     *
     * @param filePath the path to the file to delete
     * @return true if the file was deleted, false otherwise
     */
    public static boolean deleteFile(String filePath) {
        try {
            Path path = Path.of(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("[FileManager] Error deleting file: " + filePath);
            e.printStackTrace();
            return false;
        }
    }
}