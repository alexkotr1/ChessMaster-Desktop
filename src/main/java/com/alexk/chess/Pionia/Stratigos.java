package com.alexk.chess.Pionia;

import com.alexk.chess.ChessBoard;
import com.alexk.chess.Utilities;

import java.util.ArrayList;

/**
 * Represents a Bishop piece in a chess game.
 * <p>
 * The Stratigos (Bishop) class extends the Pioni abstract class and implements
 * the specific movement rules for a bishop in chess. Bishops move diagonally
 * any number of squares, but cannot jump over other pieces.
 * </p>
 *
 * @author Alex K
 * @version 1.0
 * @see Pioni
 * @see ChessBoard
 */
public class Stratigos extends Pioni {

    /**
     * Constructs a new Stratigos (Bishop) piece with the specified parameters.
     *
     * @param isWhite     indicates whether the bishop is white (true) or black (false)
     * @param chessBoard  the chess board on which the bishop is placed
     * @param initialX    the initial column position of the bishop (A-H)
     * @param initialY    the initial row position of the bishop (1-8)
     * @throws IllegalArgumentException if initialX or initialY are out of bounds
     */
    public Stratigos(Boolean isWhite, ChessBoard chessBoard, char initialX, int initialY) {
        super(isWhite, chessBoard, initialX, initialY);
    }

    /**
     * Determines whether moving the bishop to the specified position is a legal move.
     * <p>
     * A bishop can move any number of squares diagonally, but cannot jump over
     * other pieces. The destination square must be either empty or occupied by
     * an opponent's piece.
     * </p>
     *
     * @param x the destination column (A-H)
     * @param y the destination row (1-8)
     * @return true if the move is legal according to bishop movement rules, false otherwise
     */
    @Override
    public boolean isLegalMove(char x, int y) {
        if (!isWithinBounds(x, y)) return false;

        int destX = Utilities.char2Int(x);
        int currentX = Utilities.char2Int(getXPos());
        int currentY = getYPos();

        ArrayList<int[]> route = getRoute(currentX, currentY, destX, y);
        return (route != null && !route.isEmpty() &&
                route.getLast()[0] == destX &&
                route.getLast()[1] == y) &&
                (this.chessBoard.getPioniAt(x, y) == null ||
                        this.chessBoard.getPioniAt(x, y).getIsWhite() != getIsWhite());
    }
}