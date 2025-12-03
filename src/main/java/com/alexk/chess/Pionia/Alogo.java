package com.alexk.chess.Pionia;

import com.alexk.chess.ChessBoard;
import com.alexk.chess.Utilities;

import java.util.Arrays;

/**
 * Represents a Knight piece in a chess game.
 * <p>
 * The Alogo (Knight) class extends the Pioni abstract class and implements
 * the specific movement rules for a knight in chess. Knights move in an "L" shape:
 * two squares in one direction and one square perpendicular to that direction.
 * Knights are the only pieces that can "jump over" other pieces.
 * </p>
 *
 * @author Alex K
 * @version 1.0
 * @see Pioni
 * @see ChessBoard
 */
public class Alogo extends Pioni {

    /**
     * Constructs a new Alogo (Knight) piece with the specified parameters.
     *
     * @param isWhite     indicates whether the knight is white (true) or black (false)
     * @param chessBoard  the chess board on which the knight is placed
     * @param initialX    the initial column position of the knight (A-H)
     * @param initialY    the initial row position of the knight (1-8)
     * @throws IllegalArgumentException if initialX or initialY are out of bounds
     */
    public Alogo(Boolean isWhite, ChessBoard chessBoard, char initialX, int initialY) {
        super(isWhite, chessBoard, initialX, initialY);
    }

    /**
     * Determines whether moving the knight to the specified position is a legal move.
     * <p>
     * A knight moves in an "L" shape: two squares in one direction and one square
     * perpendicular. The eight possible moves are:
     * (+2, +1), (+2, -1), (-2, +1), (-2, -1),
     * (+1, +2), (+1, -2), (-1, +2), (-1, -2).
     * The destination square must be either empty or occupied by an opponent's piece.
     * </p>
     *
     * @param x the destination column (A-H)
     * @param y the destination row (1-8)
     * @return true if the move is legal according to knight movement rules, false otherwise
     */
    @Override
    public boolean isLegalMove(char x, int y) {
        if (!isWithinBounds(x, y)) return false;

        int destX = Utilities.char2Int(x);
        int destY = y;
        int currentX = Utilities.char2Int(getXPos());
        int currentY = getYPos();

        // All possible knight moves
        final int[][] allowed = {
                {2, -1}, {-1, -2}, {1, -2}, {2, -1},
                {2, 1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}
        };

        // Check if any of the allowed moves matches the destination
        boolean validMove = Arrays.stream(allowed).anyMatch(arr ->
                currentX + arr[0] == destX && currentY + arr[1] == destY);

        // Check if destination is empty or has opponent's piece
        boolean validCapture = this.chessBoard.getPioniAt(x, y) == null ||
                this.chessBoard.getPioniAt(x, y).getIsWhite() != getIsWhite();

        return validMove && validCapture;
    }
}