package com.alexk.chess;

import java.io.*;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.alexk.chess.ChessEngine.Result;

/**
 * Represents the complete state of a chess game for serialization.
 * <p>
 * This class stores all necessary information to save and load a chess game,
 * including board position, turn, timers, captured pieces, and game metadata.
 * Implements Serializable for object serialization.
 * </p>
 *
 * @author Alex K
 * @version 1.0
 * @see Serializable
 * @see ChessEngine
 */
public class GameDetails implements Serializable {

    /**
     * Serial version UID for serialization compatibility.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * FEN (Forsyth-Edwards Notation) string representing the board position.
     */
    private String fen;

    /**
     * Indicates whether it's white's turn (true) or black's turn (false).
     */
    private boolean whiteTurn;

    /**
     * White player's remaining time in seconds.
     */
    private long whiteTimeRemaining;

    /**
     * Black player's remaining time in seconds.
     */
    private long blackTimeRemaining;

    /**
     * Number of pawns captured by white.
     */
    private int whiteCapturedPawns;

    /**
     * Number of pawns captured by black.
     */
    private int blackCapturedPawns;

    /**
     * Total number of moves made by white.
     */
    private int whiteTotalMoves;

    /**
     * Total number of moves made by black.
     */
    private int blackTotalMoves;

    /**
     * Date and time when the game was saved.
     */
    private final LocalDateTime savedAt;

    /**
     * Date and time when the game was started.
     */
    private LocalDateTime startedAt;

    /**
     * Number of moves remaining before draw by 50-move rule.
     */
    private int movesRemaining;

    /**
     * The result of the game.
     */
    private Result winner;

    /**
     * Unique identifier for this game.
     */
    private UUID uuid;

    /**
     * Default constructor.
     * <p>
     * Sets savedAt to current time.
     * </p>
     */
    public GameDetails() {
        this.savedAt = LocalDateTime.now();
    }

    /**
     * Full constructor with all game details.
     *
     * @param uuid               unique identifier for the game
     * @param fen                FEN string of board position
     * @param whiteTurn          true if it's white's turn
     * @param movesRemaining     moves remaining before draw
     * @param whiteTimeRemaining white's remaining time in seconds
     * @param blackTimeRemaining black's remaining time in seconds
     * @param whiteCapturedPawns pawns captured by white
     * @param blackCapturedPawns pawns captured by black
     * @param whiteTotalMoves    total moves by white
     * @param blackTotalMoves    total moves by black
     * @param startedAt          when the game was started
     * @param winner             the game result
     */
    public GameDetails(UUID uuid,
                       String fen,
                       boolean whiteTurn,
                       int movesRemaining,
                       long whiteTimeRemaining,
                       long blackTimeRemaining,
                       int whiteCapturedPawns,
                       int blackCapturedPawns,
                       int whiteTotalMoves,
                       int blackTotalMoves,
                       LocalDateTime startedAt,
                       ChessEngine.Result winner) {
        this.fen = fen;
        this.whiteTurn = whiteTurn;
        this.movesRemaining = movesRemaining;
        this.whiteTimeRemaining = whiteTimeRemaining;
        this.blackTimeRemaining = blackTimeRemaining;
        this.whiteCapturedPawns = whiteCapturedPawns;
        this.blackCapturedPawns = blackCapturedPawns;
        this.whiteTotalMoves = whiteTotalMoves;
        this.blackTotalMoves = blackTotalMoves;
        this.savedAt = LocalDateTime.now();
        this.startedAt = startedAt;
        this.uuid = uuid == null ? UUID.randomUUID() : uuid;
        this.winner = winner;
    }

    /**
     * Saves this game details to a file.
     * <p>
     * Serializes the object and encodes it as Base64 for text storage.
     * </p>
     *
     * @param filePath the path to save the file
     */
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

    /**
     * Loads game details from a file.
     *
     * @param filePath the path to load the file from
     * @return the loaded GameDetails object, or null if an error occurs
     */
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

    // Getters and setters with documentation

    /**
     * Gets the unique identifier for this game.
     *
     * @return the UUID
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Sets the unique identifier for this game.
     *
     * @param uuid the UUID to set
     */
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Gets the FEN string representation of the board.
     *
     * @return the FEN string
     */
    public String getFen() {
        return fen;
    }

    /**
     * Sets the FEN string representation of the board.
     *
     * @param fen the FEN string to set
     */
    public void setFen(String fen) {
        this.fen = fen;
    }

    /**
     * Checks if it's white's turn.
     *
     * @return true if it's white's turn, false otherwise
     */
    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    /**
     * Sets whose turn it is.
     *
     * @param whiteTurn true for white's turn, false for black's
     */
    public void setWhiteTurn(boolean whiteTurn) {
        this.whiteTurn = whiteTurn;
    }

    /**
     * Gets white player's remaining time.
     *
     * @return remaining time in seconds
     */
    public long getWhiteTimeRemaining() {
        return whiteTimeRemaining;
    }

    /**
     * Gets black player's remaining time.
     *
     * @return remaining time in seconds
     */
    public long getBlackTimeRemaining() {
        return blackTimeRemaining;
    }

    /**
     * Sets white player's remaining time.
     *
     * @param whiteTimeRemaining remaining time in seconds
     */
    public void setWhiteTimeRemaining(int whiteTimeRemaining) {
        this.whiteTimeRemaining = whiteTimeRemaining;
    }

    /**
     * Sets black player's remaining time.
     *
     * @param blackTimeRemaining remaining time in seconds
     */
    public void setBlackTimeRemaining(long blackTimeRemaining) {
        this.blackTimeRemaining = blackTimeRemaining;
    }

    /**
     * Gets number of pawns captured by white.
     *
     * @return count of captured pawns
     */
    public int getWhiteCapturedPawns() {
        return whiteCapturedPawns;
    }

    /**
     * Sets number of pawns captured by white.
     *
     * @param whiteCapturedPawns count of captured pawns
     */
    public void setWhiteCapturedPawns(int whiteCapturedPawns) {
        this.whiteCapturedPawns = whiteCapturedPawns;
    }

    /**
     * Gets number of pawns captured by black.
     *
     * @return count of captured pawns
     */
    public int getBlackCapturedPawns() {
        return blackCapturedPawns;
    }

    /**
     * Sets number of pawns captured by black.
     *
     * @param blackCapturedPawns count of captured pawns
     */
    public void setBlackCapturedPawns(int blackCapturedPawns) {
        this.blackCapturedPawns = blackCapturedPawns;
    }

    /**
     * Gets total moves made by white.
     *
     * @return total moves count
     */
    public int getWhiteTotalMoves() {
        return whiteTotalMoves;
    }

    /**
     * Sets total moves made by white.
     *
     * @param whiteTotalMoves total moves count
     */
    public void setWhiteTotalMoves(int whiteTotalMoves) {
        this.whiteTotalMoves = whiteTotalMoves;
    }

    /**
     * Gets total moves made by black.
     *
     * @return total moves count
     */
    public int getBlackTotalMoves() {
        return blackTotalMoves;
    }

    /**
     * Sets total moves made by black.
     *
     * @param blackTotalMoves total moves count
     */
    public void setBlackTotalMoves(int blackTotalMoves) {
        this.blackTotalMoves = blackTotalMoves;
    }

    /**
     * Gets when the game was saved.
     *
     * @return save timestamp
     */
    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    /**
     * Gets when the game was started.
     *
     * @return start timestamp
     */
    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    /**
     * Sets when the game was started.
     *
     * @param startedAt start timestamp
     */
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    /**
     * Gets the default save file path for this game.
     *
     * @return file path string
     */
    public String getSaveFilePath() {
        return "./games/" + uuid + ".game";
    }

    /**
     * Sets the game result.
     *
     * @param winner the result to set
     */
    public void setWinner(Result winner) {
        this.winner = winner;
    }

    /**
     * Gets the game result.
     *
     * @return the game result
     */
    public Result getWinner() {
        return winner;
    }

    /**
     * Gets moves remaining before draw.
     *
     * @return moves remaining count
     */
    public int getMovesRemaining() {
        return movesRemaining;
    }

    /**
     * Sets moves remaining before draw.
     *
     * @param movesRemaining moves remaining count
     */
    public void setMovesRemaining(int movesRemaining) {
        this.movesRemaining = movesRemaining;
    }
}