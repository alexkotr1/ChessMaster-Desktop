package com.alexk.chess;

import com.alexk.chess.Pionia.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core chess engine that manages game logic, rules, and state.
 * <p>
 * This class implements chess game rules including move validation, check/checkmate
 * detection, stalemate detection, pawn promotion, and FEN notation generation.
 * It serves as the bridge between the graphical interface and the chess board state.
 * </p>
 *
 * @author Alex K
 * @version 1.0
 * @see ChessBoard
 * @see GameDetails
 */
public class ChessEngine {

    /**
     * The chess board managed by this engine.
     */
    protected ChessBoard chessBoard;

    /**
     * Enumeration of possible game results.
     */
    public enum Result {
        /** White wins */
        White,
        /** Black wins */
        Black,
        /** Game is tied */
        Tie,
        /** Game is still in progress */
        InProgress
    };

    /**
     * The current winner of the game.
     */
    private Result winner;

    /**
     * Unique identifier for this game.
     */
    private final UUID uuid;

    /**
     * List of all possible board positions for move generation.
     */
    ArrayList<int[]> allPositions = new ArrayList<>();

    /**
     * Constructs a new chess engine.
     *
     * @param uuid unique identifier for the game, or null to generate a new one
     */
    public ChessEngine(UUID uuid) {
        // Initialize all possible board positions
        for (int x = 1; x <= 8; x++) {
            for (int y = 1; y <= 8; y++) {
                allPositions.add(new int[]{x, y});
            }
        }
        this.uuid = uuid == null ? UUID.randomUUID() : uuid;
    }

    /**
     * Initializes and starts a new chess game.
     * <p>
     * Sets up the board with standard starting position and prints it.
     * </p>
     */
    public void playChess() {
        chessBoard = new ChessBoard();
        chessBoard.loadBoard();
        chessBoard.printBoard();
    }

    /**
     * Sets the chess board for this engine.
     *
     * @param chessBoard the chess board to use
     */
    public void setChessBoard(ChessBoard chessBoard) {
        this.chessBoard = chessBoard;
    }

    /**
     * Creates a ChessEngine from saved game details.
     *
     * @param gameDetails the saved game details
     * @return a ChessEngine configured from the saved game
     */
    public static ChessEngine fromGameDetails(GameDetails gameDetails) {
        ChessEngine chessEngine = new ChessEngine(gameDetails.getUUID());
        chessEngine.setChessBoard(ChessBoard.fromFEN(gameDetails.getFen()));
        chessEngine.getBoard().setWhiteTimeRemaining(gameDetails.getWhiteTimeRemaining());
        chessEngine.getBoard().setBlackTimeRemaining(gameDetails.getBlackTimeRemaining());
        chessEngine.setWinner(gameDetails.getWinner());
        return chessEngine;
    }

    /**
     * Attempts to execute a move from one position to another.
     * <p>
     * Validates the move, executes it if legal, updates game state,
     * and saves the game to file.
     * </p>
     *
     * @param xOrig the starting column (A-H)
     * @param yOrig the starting row (1-8)
     * @param xDest the destination column (A-H)
     * @param yDest the destination row (1-8)
     * @return true if the move was executed successfully, false otherwise
     */
    public boolean nextMove(char xOrig, int yOrig, char xDest, int yDest) {
        Pioni p = chessBoard.getPioniAt(xOrig, yOrig);
        Pioni pioniAtDest = chessBoard.getPioniAt(xDest, yDest);

        // Validate piece exists
        if (p == null) {
            System.out.println("There is no pioni at " + xOrig + " at " + yOrig);
            return false;
        }

        // Validate turn
        if (p.getIsWhite() != chessBoard.getWhiteTurn()) {
            System.out.println("It's not " + (chessBoard.getWhiteTurn() ? "black" : "white" + "'s turn"));
            return false;
        }

        // Validate move legality
        if (!p.isLegalMove(xDest, yDest)) {
            System.out.println("Illegal move!");
            return false;
        }

        // Update move counts
        if (p.getIsWhite()) chessBoard.incrementWhiteMoves();
        else chessBoard.incrementBlackMoves();

        // Switch turn
        chessBoard.setWhiteTurn(!chessBoard.getWhiteTurn());

        // Handle castling
        if (p.type.equals("Vasilias") && pioniAtDest != null &&
                pioniAtDest.type.equals("Pyrgos") && p.getIsWhite() == pioniAtDest.getIsWhite()) {

            int[] dest = pioniAtDest.getPosition();
            int[] orig = p.getPosition();

            // Move king two squares toward rook
            chessBoard.move(xOrig, yOrig,
                    Utilities.int2Char(dest[0] > orig[0] ? orig[0] + 2 : orig[0] - 2),
                    yOrig);

            // Move rook to other side of king
            chessBoard.move(Utilities.int2Char(dest[0]), dest[1],
                    Utilities.int2Char(dest[0] > orig[0] ? orig[0] - 1 : orig[0] + 1),
                    yOrig);

            chessBoard.printBoard();
            saveGameState();
            return true;
        }

        // Execute normal move
        chessBoard.move(xOrig, yOrig, xDest, yDest);
        chessBoard.printBoard();

        // Save game state
        saveGameState();
        System.out.println(toFen());
        return true;
    }

    /**
     * Promotes a pawn to another piece type.
     *
     * @param p        the pawn to promote
     * @param type     the type to promote to ("Alogo", "Pyrgos", "Stratigos", or "Vasilissa")
     * @return the new promoted piece, or null if promotion failed
     */
    public Pioni upgradePioni(Pioni p, String type) {
        // Validate promotion conditions
        if (p.type.equals("Stratiotis") &&
                ((p.getIsWhite() && p.getYPos() == 8) || (!p.getIsWhite() && p.getYPos() == 1))) {

            Pioni upgradedPioni;
            switch (type) {
                case "Alogo":
                    upgradedPioni = new Alogo(p.isWhite, chessBoard, p.getXPos(), p.getYPos());
                    break;
                case "Pyrgos":
                    upgradedPioni = new Pyrgos(p.isWhite, chessBoard, p.getXPos(), p.getYPos());
                    break;
                case "Stratigos":
                    upgradedPioni = new Stratigos(p.isWhite, chessBoard, p.getXPos(), p.getYPos());
                    break;
                case "Vasilissa":
                    upgradedPioni = new Vasilissa(p.isWhite, chessBoard, p.getXPos(), p.getYPos());
                    break;
                default:
                    System.err.println("Something went wrong!");
                    return null;
            }

            // Replace pawn with promoted piece
            chessBoard.getPionia().remove(p);
            chessBoard.getPionia().add(upgradedPioni);
            return upgradedPioni;
        }
        return null;
    }

    /**
     * Checks if the specified king is in check.
     *
     * @param chessBoard the chess board to check
     * @param white      true to check white king, false for black king
     * @return true if the king is in check, false otherwise
     */
    public static boolean checkKingMat(ChessBoard chessBoard, boolean white) {
        Pioni allyKing = chessBoard.getPionia()
                .stream()
                .filter(p -> p.getIsWhite() == white && p.type.equals("Vasilias"))
                .findFirst()
                .orElse(null);

        if (allyKing == null) return false;

        // Check if any opponent piece can attack the king
        for (Pioni p : chessBoard.getPionia().stream()
                .filter(pioni -> !pioni.getCaptured())
                .collect(Collectors.toCollection(ArrayList::new))) {

            if (p.getIsWhite() != white && p.isLegalMove(allyKing.getXPos(), allyKing.getYPos())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds all legal moves when the king is in check.
     * <p>
     * When a king is in check, only moves that remove the check are legal.
     * This method finds all such moves for all pieces of the specified color.
     * </p>
     *
     * @param white true to find moves for white, false for black
     * @return a map from pieces to their legal destination squares when in check
     */
    public HashMap<Pioni, ArrayList<int[]>> kingCheckMate(boolean white) {
        HashMap<Pioni, ArrayList<int[]>> legalMovesWhenKingThreatened = new HashMap<>();

        // Get all pieces of the specified color
        ArrayList<Pioni> duplicatePieces = chessBoard.getPionia().stream()
                .filter(pioni -> pioni.getIsWhite() == white && !pioni.getCaptured())
                .collect(Collectors.toCollection(ArrayList::new));

        // Test every possible move for every piece
        for (Pioni p : duplicatePieces) {
            for (int[] pos : allPositions) {
                // Create a test board to simulate the move
                ChessBoard testChessBoard = chessBoard.clone();
                Pioni duplicatePioni = testChessBoard.getPioniAt(p.getXPos(), p.getYPos());

                // Check if move is legal on the test board
                if (!duplicatePioni.isLegalMove(Utilities.int2Char(pos[0]), pos[1])) continue;

                // Execute the move on the test board
                testChessBoard.move(p.getXPos(), p.getYPos(), Utilities.int2Char(pos[0]), pos[1]);

                // Check if the move removes the check
                if (!ChessEngine.checkKingMat(testChessBoard, white)) {
                    Pioni origPioni = chessBoard.getPioniAt(p.getXPos(), p.getYPos());
                    ArrayList<int[]> existingRoutes = legalMovesWhenKingThreatened.get(origPioni);
                    if (existingRoutes == null) existingRoutes = new ArrayList<>();
                    existingRoutes.add(new int[]{pos[0], pos[1]});
                    legalMovesWhenKingThreatened.put(origPioni, existingRoutes);
                }
            }
        }
        return legalMovesWhenKingThreatened;
    }

    /**
     * Checks for stalemate for the specified player.
     * <p>
     * Stalemate occurs when a player has no legal moves but is not in check.
     * </p>
     *
     * @param white true to check white for stalemate, false for black
     * @return true if the player is in stalemate, false otherwise
     */
    public boolean stalemateCheck(boolean white) {
        ArrayList<Pioni> duplicatePieces = chessBoard.getPionia().stream()
                .filter(pioni -> pioni.getIsWhite() == white && !pioni.getCaptured())
                .collect(Collectors.toCollection(ArrayList::new));

        // Check if any piece has any legal move
        for (Pioni p : duplicatePieces) {
            for (int[] pos : allPositions) {
                if (p.isLegalMove(Utilities.int2Char(pos[0]), pos[1]) &&
                        !checkDumbMove(p, pos)) {
                    return false; // Found a legal move
                }
            }
        }
        return true; // No legal moves found
    }

    /**
     * Checks if a move would leave the player's king in check.
     * <p>
     * A move that leaves the player's own king in check is illegal.
     * </p>
     *
     * @param p    the piece to move
     * @param dest the destination coordinates [x, y]
     * @return true if the move would leave the king in check, false otherwise
     */
    public boolean checkDumbMove(Pioni p, int[] dest) {
        ChessBoard testChessBoard = chessBoard.clone();
        testChessBoard.move(p.getXPos(), p.getYPos(), Utilities.int2Char(dest[0]), dest[1]);
        return ChessEngine.checkKingMat(testChessBoard, p.isWhite);
    }

    /**
     * Gets the chess board managed by this engine.
     *
     * @return the chess board
     */
    public ChessBoard getBoard() {
        return chessBoard;
    }

    /**
     * Converts the current board position to FEN notation.
     * <p>
     * FEN (Forsyth-Edwards Notation) is a standard notation for describing
     * chess positions. This method generates the piece placement part of FEN.
     * </p>
     *
     * @return the FEN string representing the current position
     */
    public String toFen() {
        StringBuilder fen = new StringBuilder();

        // Piece placement (ranks 8 to 1)
        for (int y = 8; y >= 1; y--) {
            int emptyCounter = 0;
            for (int x = 1; x <= 8; x++) {
                Pioni pioni = getBoard().getPioniAt(Utilities.int2Char(x), y);
                if (pioni != null) {
                    if (emptyCounter != 0) fen.append(emptyCounter);
                    fen.append(pioni.print());
                    emptyCounter = 0;
                } else {
                    emptyCounter++;
                }

                // End of rank
                if (x == 8) {
                    fen.append(emptyCounter == 0 ? "" : emptyCounter);
                    if (y != 1) fen.append("/");
                }
            }
        }

        // Active color
        fen.append(" ").append(getBoard().getWhiteTurn() ? "w" : "b").append(" ");

        // Castling availability
        boolean whiteKingSideRights = getBoard().castlingRights(true, true);
        boolean whiteQueenSideRights = getBoard().castlingRights(true, false);
        boolean blackKingSideRights = getBoard().castlingRights(false, true);
        boolean blackQueenSideRights = getBoard().castlingRights(false, false);

        if (whiteKingSideRights) fen.append("K");
        if (whiteQueenSideRights) fen.append("Q");
        if (blackKingSideRights) fen.append("k");
        if (blackQueenSideRights) fen.append("q");

        if (!whiteKingSideRights && !whiteQueenSideRights &&
                !blackKingSideRights && !blackQueenSideRights) {
            fen.append("-");
        }

        // En passant, halfmove clock, and fullmove number
        fen.append(" ")
                .append("-")  // En passant target square (not implemented)
                .append(" ")
                .append(getBoard().getMovesRemaining())  // Halfmove clock
                .append(" ")
                .append("50");  // Fullmove number (placeholder)

        return fen.toString();
    }

    /**
     * Sets the winner of the game and saves the final state.
     *
     * @param winner the game result
     */
    public void setWinner(Result winner) {
        this.winner = winner;
        saveGameState();
    }

    /**
     * Gets the current game result.
     *
     * @return the game result
     */
    public Result getWinner() {
        return winner != null ? winner : Result.InProgress;
    }

    /**
     * Saves the current game state to a file.
     */
    private void saveGameState() {
        GameDetails details = new GameDetails(
                uuid,
                toFen(),
                chessBoard.getWhiteTurn(),
                getBoard().getMovesRemaining(),
                getBoard().getWhiteTimeRemaining(),
                getBoard().getBlackTimeRemaining(),
                getBoard().getWhiteCapturedPawns(),
                getBoard().getBlackCapturedPawns(),
                getBoard().getWhiteMoves(),
                getBoard().getBlackMoves(),
                LocalDateTime.now(),
                getWinner()
        );
        details.saveToFile("./games/" + uuid + ".game");
    }
}