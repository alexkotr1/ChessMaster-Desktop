package com.alexk.chess;

import com.alexk.chess.Pionia.*;

import java.util.ArrayList;

/**
 * Represents a chess board and manages piece placement and game state.
 * <p>
 * This class maintains the state of the chess board including piece positions,
 * turn management, move counting, and time tracking. It provides methods for
 * board setup, move validation, and game state queries.
 * </p>
 *
 * @author Alex K
 * @version 1.0
 * @see Pioni
 * @see ChessEngine
 */
public class ChessBoard {

    /**
     * List of all pieces on the board.
     */
    private final ArrayList<Pioni> Pionia = new ArrayList<>();

    /**
     * Indicates whether it's white's turn (true) or black's turn (false).
     */
    private boolean whiteTurn = true;

    /**
     * Number of moves remaining before draw by the 50-move rule.
     */
    private int movesRemaining = 100;

    /**
     * White player's remaining time in seconds.
     */
    private long whiteTimeRemaining = 0;

    /**
     * Black player's remaining time in seconds.
     */
    private long blackTimeRemaining = 0;

    /**
     * Total moves made by white player.
     */
    private int whiteMoves;

    /**
     * Total moves made by black player.
     */
    private int blackMoves;

    /**
     * Number of pawns captured by white.
     */
    private int whiteCapturedPawns;

    /**
     * Number of pawns captured by black.
     */
    private int blackCapturedPawns;

    /**
     * Places a piece at the specified position.
     *
     * @param p    the piece to place
     * @param xPos the column position (A-H)
     * @param yPos the row position (1-8)
     */
    public void placePioniAt(Pioni p, char xPos, int yPos) {
        p.setXPos(xPos);
        p.setYPos(yPos);
    }

    /**
     * Checks if a position is under attack by the opponent.
     *
     * @param xOrig the column to check (A-H)
     * @param yOrig the row to check (1-8)
     * @param white true to check if white is attacking, false for black
     * @return true if the position is under attack, false otherwise
     */
    public boolean isDangerousPosition(char xOrig, int yOrig, boolean white) {
        for (Pioni p : getPionia().stream().filter(pioni -> pioni.getIsWhite() != white).toList()) {
            if (p.isLegalMove(xOrig, yOrig)) return true;
        }
        return false;
    }

    /**
     * Initializes the board with standard chess starting position.
     * <p>
     * Places all pieces in their standard starting positions:
     * - Pawns on ranks 2 and 7
     * - Other pieces on ranks 1 and 8
     * </p>
     */
    public void loadBoard() {
        // Place pawns
        for (int x = 1; x <= 16; x++) {
            Pionia.add(new Stratiotis(x < 9, this,
                    Utilities.int2Char(x < 9 ? x : x - 8),
                    x < 9 ? 2 : 7));
        }

        // Place other pieces
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 4; y++) {
                switch (y) {
                    case 0: {
                        // Rooks
                        Pionia.add(new Pyrgos(x == 0, this, 'A', x == 0 ? 1 : 8));
                        Pionia.add(new Pyrgos(x == 0, this, 'H', x == 0 ? 1 : 8));
                        break;
                    }
                    case 1: {
                        // Knights
                        Pionia.add(new Alogo(x == 0, this, 'B', x == 0 ? 1 : 8));
                        Pionia.add(new Alogo(x == 0, this, 'G', x == 0 ? 1 : 8));
                        break;
                    }
                    case 2: {
                        // Bishops
                        Pionia.add(new Stratigos(x == 0, this, 'C', x == 0 ? 1 : 8));
                        Pionia.add(new Stratigos(x == 0, this, 'F', x == 0 ? 1 : 8));
                        break;
                    }
                    case 3: {
                        // Queen and King
                        Pionia.add(new Vasilissa(x == 0, this, 'D', x == 0 ? 1 : 8));
                        Pionia.add(new Vasilias(x == 0, this, 'E', x == 0 ? 1 : 8));
                        break;
                    }
                }
            }
        }
    }

    /**
     * Creates a ChessBoard from a FEN (Forsyth-Edwards Notation) string.
     * <p>
     * FEN notation describes a chess position with six fields separated by spaces:
     * 1. Piece placement
     * 2. Active color
     * 3. Castling availability
     * 4. En passant target square
     * 5. Halfmove clock
     * 6. Fullmove number
     * </p>
     *
     * @param fen the FEN string to parse
     * @return a ChessBoard configured according to the FEN
     * @throws IllegalArgumentException if the FEN string is invalid
     */
    public static ChessBoard fromFEN(String fen) {
        ChessBoard board = new ChessBoard();
        board.Pionia.clear();

        String[] parts = fen.trim().split("\\s+");
        if (parts.length < 1) {
            throw new IllegalArgumentException("Invalid FEN: empty string");
        }

        String boardPart = parts[0];
        String[] ranks = boardPart.split("/");
        if (ranks.length != 8) {
            throw new IllegalArgumentException("Invalid FEN: must have 8 ranks");
        }

        // Parse piece placement
        for (int rankIndex = 0; rankIndex < 8; rankIndex++) {
            String rankStr = ranks[rankIndex];
            int y = 8 - rankIndex; // FEN ranks are from 8 to 1
            int file = 1;

            for (int i = 0; i < rankStr.length(); i++) {
                char c = rankStr.charAt(i);

                if (Character.isDigit(c)) {
                    // Skip empty squares
                    file += c - '0';
                } else {
                    if (file < 1 || file > 8) {
                        throw new IllegalArgumentException("Invalid FEN rank: " + rankStr);
                    }

                    boolean isWhite = Character.isUpperCase(c);
                    char pieceChar = Character.toLowerCase(c);
                    char xPos = Utilities.int2Char(file);

                    // Create appropriate piece based on character
                    switch (pieceChar) {
                        case 'p' -> board.Pionia.add(new Stratiotis(isWhite, board, xPos, y));
                        case 'r' -> board.Pionia.add(new Pyrgos(isWhite, board, xPos, y));
                        case 'n' -> board.Pionia.add(new Alogo(isWhite, board, xPos, y));
                        case 'b' -> board.Pionia.add(new Stratigos(isWhite, board, xPos, y));
                        case 'q' -> board.Pionia.add(new Vasilissa(isWhite, board, xPos, y));
                        case 'k' -> board.Pionia.add(new Vasilias(isWhite, board, xPos, y));
                        default ->
                                throw new IllegalArgumentException("Invalid FEN piece char: " + c);
                    }

                    file++;
                }
            }

            if (file != 9) {
                throw new IllegalArgumentException("Invalid FEN rank (must cover 8 files): " + rankStr);
            }
        }

        // Parse active color
        if (parts.length > 1) {
            board.whiteTurn = parts[1].equalsIgnoreCase("w");
        } else {
            board.whiteTurn = true; // Default to white's turn
        }

        // Parse halfmove clock (for 50-move rule)
        board.movesRemaining = 100;
        if (parts.length > 4) {
            try {
                int halfmoveClock = Integer.parseInt(parts[4]);
                board.movesRemaining = Math.max(0, 100 - halfmoveClock);
            } catch (NumberFormatException ignored) {
                // Use default if parsing fails
            }
        }

        // Parse castling rights
        if (parts.length > 2) {
            String castling = parts[2];

            // Assume all pieces have moved by default
            for (Pioni p : board.Pionia) {
                if (p instanceof Vasilias vasilias) {
                    vasilias.setMoved(true);
                } else if (p instanceof Pyrgos pyrgos) {
                    pyrgos.setMoved(true);
                }
            }

            // Set unmoved status based on castling rights
            if (!castling.equals("-")) {
                for (char c : castling.toCharArray()) {
                    switch (c) {
                        case 'K' -> { // White king-side
                            Pioni k = board.getPioniAt('E', 1);
                            if (k instanceof Vasilias v) v.setMoved(false);
                            Pioni r = board.getPioniAt('H', 1);
                            if (r instanceof Pyrgos pr) pr.setMoved(false);
                        }
                        case 'Q' -> { // White queen-side
                            Pioni k = board.getPioniAt('E', 1);
                            if (k instanceof Vasilias v) v.setMoved(false);
                            Pioni r = board.getPioniAt('A', 1);
                            if (r instanceof Pyrgos pr) pr.setMoved(false);
                        }
                        case 'k' -> { // Black king-side
                            Pioni k = board.getPioniAt('E', 8);
                            if (k instanceof Vasilias v) v.setMoved(false);
                            Pioni r = board.getPioniAt('H', 8);
                            if (r instanceof Pyrgos pr) pr.setMoved(false);
                        }
                        case 'q' -> { // Black queen-side
                            Pioni k = board.getPioniAt('E', 8);
                            if (k instanceof Vasilias v) v.setMoved(false);
                            Pioni r = board.getPioniAt('A', 8);
                            if (r instanceof Pyrgos pr) pr.setMoved(false);
                        }
                    }
                }
            }
        }

        return board;
    }

    /**
     * Gets the piece at the specified position.
     *
     * @param xPos the column (A-H)
     * @param yPos the row (1-8)
     * @return the piece at the position, or null if no piece exists
     */
    public Pioni getPioniAt(char xPos, int yPos) {
        return Pionia.stream()
                .filter(pioni -> Utilities.int2Char(pioni.getPosition()[0]) == xPos &&
                        pioni.getPosition()[1] == yPos &&
                        !pioni.getCaptured())
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets all pieces on the board.
     *
     * @return an ArrayList of all pieces
     */
    public ArrayList<Pioni> getPionia() {
        return Pionia;
    }

    /**
     * Moves a piece from one position to another.
     * <p>
     * Handles piece movement, capture, and updates game state including
     * move counting and captured piece tracking.
     * </p>
     *
     * @param xOrig the starting column (A-H)
     * @param yOrig the starting row (1-8)
     * @param xDest the destination column (A-H)
     * @param yDest the destination row (1-8)
     */
    public void move(char xOrig, int yOrig, char xDest, int yDest) {
        movesRemaining--;
        Pioni p = getPioniAt(xOrig, yOrig);
        Pioni pioniAtDestination = getPioniAt(xDest, yDest);

        // Handle capture
        if (pioniAtDestination != null && p.getIsWhite() != pioniAtDestination.getIsWhite()) {
            capture(pioniAtDestination);
            movesRemaining = 100; // Reset 50-move counter on capture
        }

        // Reset 50-move counter on pawn move
        if (p.type.equals("Stratiotis")) movesRemaining = 100;

        // Move the piece
        placePioniAt(p, xDest, yDest);

        // Update moved status for castling
        if (p.type.equals("Pyrgos")) ((Pyrgos) p).setMoved(true);
        else if (p.type.equals("Vasilias")) ((Vasilias) p).setMoved(true);
    }

    /**
     * Gets whose turn it is.
     *
     * @return true if it's white's turn, false if black's
     */
    public Boolean getWhiteTurn() {
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
     * Sets the moves remaining before draw.
     *
     * @param movesRemaining number of moves remaining
     */
    public void setMovesRemaining(int movesRemaining) {
        this.movesRemaining = movesRemaining;
    }

    /**
     * Gets the moves remaining before draw.
     *
     * @return number of moves remaining
     */
    public int getMovesRemaining() {
        return movesRemaining;
    }

    /**
     * Prints the current board state to the console.
     * <p>
     * Uses standard chess notation with piece characters:
     * K/k = king, Q/q = queen, R/r = rook, N/n = knight, B/b = bishop, P/p = pawn
     * Uppercase for white, lowercase for black.
     * </p>
     */
    public void printBoard() {
        System.out.println("   a  b  c  d  e  f  g  h  \n  ------------------------");
        for (int y = 8; y >= 1; y--) {
            System.out.printf("%d  %s  %s  %s  %s  %s  %s  %s  %s %d%n", y,
                    this.getPioniAt('A', y) == null ? " " : this.getPioniAt('A', y).print(),
                    this.getPioniAt('B', y) == null ? " " : this.getPioniAt('B', y).print(),
                    this.getPioniAt('C', y) == null ? " " : this.getPioniAt('C', y).print(),
                    this.getPioniAt('D', y) == null ? " " : this.getPioniAt('D', y).print(),
                    this.getPioniAt('E', y) == null ? " " : this.getPioniAt('E', y).print(),
                    this.getPioniAt('F', y) == null ? " " : this.getPioniAt('F', y).print(),
                    this.getPioniAt('G', y) == null ? " " : this.getPioniAt('G', y).print(),
                    this.getPioniAt('H', y) == null ? " " : this.getPioniAt('H', y).print(),
                    y);
        }
        System.out.println("  ------------------------\n   a  b  c  d  e  f  g  h");
    }

    /**
     * Sets white player's remaining time.
     *
     * @param whiteTimeRemaining remaining time in seconds
     */
    public void setWhiteTimeRemaining(long whiteTimeRemaining) {
        this.whiteTimeRemaining = whiteTimeRemaining;
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
     * Sets black player's remaining time.
     *
     * @param blackTimeRemaining remaining time in seconds
     */
    public void setBlackTimeRemaining(long blackTimeRemaining) {
        this.blackTimeRemaining = blackTimeRemaining;
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
     * Checks if castling is allowed for the specified side.
     *
     * @param white    true to check white's castling, false for black's
     * @param kingSide true for king-side castling, false for queen-side
     * @return true if castling is allowed, false otherwise
     */
    public boolean castlingRights(boolean white, boolean kingSide) {
        Vasilias king = null;
        Pyrgos rook = null;

        for (Pioni p : getPionia()) {
            if (p.getIsWhite() == white) {
                if (p.getType().equals("Vasilias")) king = (Vasilias) p;
                else if (p.getType().equals("Pyrgos")) {
                    if (((Pyrgos) p).getKingSide() == kingSide) rook = (Pyrgos) p;
                }
            }
        }

        return king != null && rook != null &&
                !king.getMoved() && !rook.getMoved();
    }

    /**
     * Creates a deep copy of this chess board.
     *
     * @return a cloned ChessBoard
     */
    @Override
    protected ChessBoard clone() {
        ChessBoard chessBoard = new ChessBoard();
        chessBoard.whiteTurn = whiteTurn;

        // Clone all pieces
        for (Pioni p : Pionia) {
            Pioni clone = p.clone();
            clone.setChessBoard(chessBoard);
            chessBoard.Pionia.add(clone);
        }

        return chessBoard;
    }

    /**
     * Captures a piece (marks it as captured).
     *
     * @param p the piece to capture
     */
    public void capture(Pioni p) {
        if (p.getIsWhite()) incrementWhiteCapturedPawns();
        else incrementBlackCapturedPawns();
        p.setCaptured(true);
    }

    /**
     * Sets the number of pawns captured by white.
     *
     * @param count the count to set
     */
    public void setWhiteCapturedPawns(int count) {
        this.whiteCapturedPawns = count;
    }

    /**
     * Gets the number of pawns captured by white.
     *
     * @return the count of captured pawns
     */
    public int getWhiteCapturedPawns() {
        return whiteCapturedPawns;
    }

    /**
     * Sets the number of pawns captured by black.
     *
     * @param count the count to set
     */
    public void setBlackCapturedPawns(int count) {
        this.blackCapturedPawns = count;
    }

    /**
     * Gets the number of pawns captured by black.
     *
     * @return the count of captured pawns
     */
    public int getBlackCapturedPawns() {
        return blackCapturedPawns;
    }

    /**
     * Increments the count of pawns captured by white.
     */
    public void incrementWhiteCapturedPawns() {
        this.whiteCapturedPawns++;
    }

    /**
     * Increments the count of pawns captured by black.
     */
    public void incrementBlackCapturedPawns() {
        this.blackCapturedPawns++;
    }

    /**
     * Sets the total moves made by white.
     *
     * @param moves the move count to set
     */
    public void setWhiteMoves(int moves) {
        this.whiteMoves = moves;
    }

    /**
     * Gets the total moves made by white.
     *
     * @return the move count
     */
    public int getWhiteMoves() {
        return whiteMoves;
    }

    /**
     * Sets the total moves made by black.
     *
     * @param moves the move count to set
     */
    public void setBlackMoves(int moves) {
        this.blackMoves = moves;
    }

    /**
     * Gets the total moves made by black.
     *
     * @return the move count
     */
    public int getBlackMoves() {
        return blackMoves;
    }

    /**
     * Increments the total moves made by white.
     */
    public void incrementWhiteMoves() {
        this.whiteMoves++;
    }

    /**
     * Increments the total moves made by black.
     */
    public void incrementBlackMoves() {
        this.blackMoves++;
    }
}