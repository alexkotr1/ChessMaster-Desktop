package com.alexk.chess.Pionia;

import com.alexk.chess.ChessBoard;
import com.alexk.chess.Utilities;

import java.util.ArrayList;

/**
 * Represents a Queen piece in a chess game.
 * <p>
 * The Vasilissa (Queen) class extends the Pioni abstract class and implements
 * the specific movement rules for a queen in chess. The queen combines the
 * movements of both the rook and bishop - it can move any number of squares
 * horizontally, vertically, or diagonally.
 * </p>
 *
 * @author Alex K
 * @version 1.0
 * @see Pioni
 * @see ChessBoard
 */
public class Vasilissa extends Pioni {

    /**
     * Constructs a new Vasilissa (Queen) piece with the specified parameters.
     *
     * @param isWhite     indicates whether the queen is white (true) or black (false)
     * @param chessBoard  the chess board on which the queen is placed
     * @param initialX    the initial column position of the queen (A-H)
     * @param initialY    the initial row position of the queen (1-8)
     * @throws IllegalArgumentException if initialX or initialY are out of bounds
     */
    public Vasilissa(Boolean isWhite, ChessBoard chessBoard, char initialX, int initialY) {
        super(isWhite, chessBoard, initialX, initialY);
    }

    /**
     * Determines whether moving the queen to the specified position is a legal move.
     * <p>
     * A queen can move any number of squares horizontally, vertically, or diagonally,
     * but cannot jump over other pieces. The destination square must be either empty
     * or occupied by an opponent's piece.
     * </p>
     *
     * @param x the destination column (A-H)
     * @param y the destination row (1-8)
     * @return true if the move is legal according to queen movement rules, false otherwise
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