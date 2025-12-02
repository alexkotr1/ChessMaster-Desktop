package com.alexk.chess;

import java.io.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import static com.alexk.chess.ChessEngine.Result;

public class GameDetails implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String fen;
    private boolean whiteTurn;
    private long whiteTimeRemaining;
    private long blackTimeRemaining;
    private final LocalDateTime savedAt;
    private LocalDateTime startedAt;
    private int movesRemaining;
    private Result winner;
    private UUID uuid;
    public GameDetails() {
        this.savedAt = LocalDateTime.now();
    }

    public GameDetails(UUID uuid, String fen, boolean whiteTurn, int movesRemaining,
                       long whiteTimeRemaining, long blackTimeRemaining, LocalDateTime startedAt, ChessEngine.Result winner)
    {
        this.fen = fen;
        this.whiteTurn = whiteTurn;
        this.movesRemaining = movesRemaining;
        this.whiteTimeRemaining = whiteTimeRemaining;
        this.blackTimeRemaining = blackTimeRemaining;
        this.savedAt = LocalDateTime.now();
        this.startedAt = startedAt;
        this.uuid = uuid == null ? UUID.randomUUID() : uuid;
        this.winner = winner;
    }

    // ------------------ Saving & Loading ------------------
    public void saveToFile(String filePath) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.close();

            String base64 = java.util.Base64.getEncoder().encodeToString(bos.toByteArray());
            FileManager.writeStringToFile(filePath, base64);

            System.out.println("[GameDetails] Game successfully saved to " + filePath);

        } catch (Exception e) {
            System.err.println("[GameDetails] Error saving game to file: " + filePath);
            e.printStackTrace();
        }
    }

    public static GameDetails loadFromFile(String filePath) {
        try {
            String base64 = FileManager.readFileToString(filePath);
            if (base64 == null) {
                System.err.println("[GameDetails] File is empty or cannot be read: " + filePath);
                return null;
            }

            byte[] data = java.util.Base64.getDecoder().decode(base64);

            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
            Object obj = in.readObject();
            in.close();

            if (obj instanceof GameDetails gd) {
                System.out.println("[GameDetails] Game successfully loaded from " + filePath);
                return gd;
            }

            System.err.println("[GameDetails] Invalid object type in file: " + filePath);
            return null;

        } catch (Exception e) {
            System.err.println("[GameDetails] Error loading game from file: " + filePath);
            e.printStackTrace();
            return null;
        }
    }

    // ------------------ Getters / Setters ------------------

    public UUID getUUID() {
        return uuid;
    }
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }
    public String getFen() {
        return fen;
    }

    public void setFen(String fen) {
        this.fen = fen;
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public void setWhiteTurn(boolean whiteTurn) {
        this.whiteTurn = whiteTurn;
    }
    public void setBlackTimeRemaining(long blackTimeRemaining) {
        this.blackTimeRemaining = blackTimeRemaining;
    }
    public long getWhiteTimeRemaining() {
        return whiteTimeRemaining;
    }
    public long getBlackTimeRemaining() {
        return blackTimeRemaining;
    }

    public void setWhiteTimeRemaining(int whiteTimeRemaining) {
        this.whiteTimeRemaining = whiteTimeRemaining;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public String getSaveFilePath() {
        return "./games/" + uuid + ".game";
    }
    public void setWinner(Result winner) {
        this.winner = winner;
    }
    public Result getWinner() {
        return winner;
    }
    public int getMovesRemaining() {
        return movesRemaining;
    }
    public void setMovesRemaining(int movesRemaining) {
        this.movesRemaining = movesRemaining;
    }
}
