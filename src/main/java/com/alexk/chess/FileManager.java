package com.alexk.chess;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileManager {

    /**
     * Reads the entire contents of a file into a single String.
     * Returns null if an error occurs.
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
     * Creates the file if it doesn't exist and writes the given string to it.
     * Prints any errors to the console.
     */
    public static void writeStringToFile(String filePath, String content) {
        try {
            Path path = Path.of(filePath);

            // Create parent directories if missing
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            // Create file if missing
            if (!Files.exists(path)) {
                Files.createFile(path);
            }

            // Write content
            Files.writeString(path, content, StandardCharsets.UTF_8);

        } catch (IOException e) {
            System.err.println("[FileManager] Error writing to file: " + filePath);
            e.printStackTrace();
        }
    }

    /**
     * Deletes a file if it exists.
     * Returns true if deleted, false otherwise.
     * Prints any errors to the console.
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
