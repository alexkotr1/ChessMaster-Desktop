package com.alexk.chess.Pionia;

import com.alexk.chess.ChessBoard;
import com.alexk.chess.Utilities;

import java.util.ArrayList;

/**
 * Represents a Rook piece in a chess game.
 * <p>
 * The Pyrgos (Rook) class extends the Pioni abstract class and implements
 * the specific movement rules for a rook in chess. Rooks move horizontally
 * and vertically any number of squares. This class also tracks whether the
 * rook has moved (important for castling rights).
 * </p>
 *
 * @author Alex K
 * @version 1.0
 * @see Pioni
 * @see ChessBoard
 */
public class Pyrgos extends Pioni {

    /**
     * Indicates whether this rook has moved from its starting position.
     */
    private boolean moved;

    /**
     * Indicates whether this rook is on the king's side (right side) or queen's side (left side).
     */
    private boolean kingSide;

    /**
     * Constructs a new Pyrgos (Rook) piece with the specified parameters.
     *
     * @param isWhite     indicates whether the rook is white (true) or black (false)
     * @param chessBoard  the chess board on which the rook is placed
     * @param initialX    the initial column position of the rook (A-H)
     * @param initialY    the initial row position of the rook (1-8)
     * @throws IllegalArgumentException if initialX or initialY are out of bounds
     */
    public Pyrgos(Boolean isWhite, ChessBoard chessBoard, char initialX, int initialY) {
        super(isWhite, chessBoard, initialX, initialY);
        this.kingSide = initialX > 4;  // E-H is king's side, A-D is queen's side
    }

    /**
     * Determines whether moving the rook to the specified position is a legal move.
     * <p>
     * A rook can move any number of squares horizontally or vertically,
     * but cannot jump over other pieces. The destination square must be
     * either empty or occupied by an opponent's piece.
     * </p>
     *
     * @param x the destination column (A-H)
     * @param y the destination row (1-8)
     * @return true if the move is legal according to rook movement rules, false otherwise
     */
    @Override
    public boolean isLegalMove(char x, int y) {
        if (!isWithinBounds(x, y)) return false;

        int destX = Utilities.char2Int(x);
        int currentX = Utilities.char2Int(getXPos());
        int currentY = getYPos();

        ArrayList<int[]> route = getRoute(currentX, currentY, destX, y);
        return (route != null && !route.isEmpty() &&
                route.get(route.size() - 1)[0] == destX &&
                route.get(route.size() - 1)[1] == y) &&
                (this.chessBoard.getPioniAt(x, y) == null ||
                        this.chessBoard.getPioniAt(x, y).getIsWhite() != getIsWhite());
    }

    /**
     * Gets whether this rook is on the king's side.
     *
     * @return true if on king's side (right side), false if on queen's side (left side)
     */
    public boolean getKingSide() {
        return kingSide;
    }

    /**
     * Sets whether this rook has moved from its starting position.
     *
     * @param moved true if the rook has moved, false otherwise
     */
    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    /**
     * Gets whether this rook has moved from its starting position.
     *
     * @return true if the rook has moved, false otherwise
     */
    public boolean getMoved() {
        return moved;
    }
}