package com.alexk.chess.Pionia;

import com.alexk.chess.ChessBoard;
import com.alexk.chess.Utilities;

import java.util.ArrayList;

/**
 * Represents a Pawn piece in a chess game.
 * <p>
 * The Stratiotis (Pawn) class extends the Pioni abstract class and implements
 * the specific movement rules for a pawn in chess. Pawns have unique movement:
 * they move forward one square (two on their first move), capture diagonally,
 * and can promote to other pieces upon reaching the opposite side of the board.
 * </p>
 *
 * @author Alex K
 * @version 1.0
 * @see Pioni
 * @see ChessBoard
 */
public class Stratiotis extends Pioni {

    /**
     * Constructs a new Stratiotis (Pawn) piece with the specified parameters.
     *
     * @param isWhite     indicates whether the pawn is white (true) or black (false)
     * @param chessBoard  the chess board on which the pawn is placed
     * @param initialX    the initial column position of the pawn (A-H)
     * @param initialY    the initial row position of the pawn (1-8)
     * @throws IllegalArgumentException if initialX or initialY are out of bounds
     */
    public Stratiotis(Boolean isWhite, ChessBoard chessBoard, char initialX, int initialY) {
        super(isWhite, chessBoard, initialX, initialY);
    }

    /**
     * Determines whether moving the pawn to the specified position is a legal move.
     * <p>
     * Pawn movement rules:
     * 1. Pawns move forward one square (white: increasing y, black: decreasing y)
     * 2. On their first move, pawns can move forward two squares
     * 3. Pawns capture diagonally one square forward
     * 4. Pawns cannot move forward if the square is occupied
     * 5. Pawns cannot capture straight ahead
     * </p>
     *
     * @param x the destination column (A-H)
     * @param y the destination row (1-8)
     * @return true if the move is legal according to pawn movement rules, false otherwise
     */
    @Override
    public boolean isLegalMove(char x, int y) {
        int destX = Utilities.char2Int(x);
        int currentX = Utilities.char2Int(getXPos());
        int currentY = getYPos();

        // Check bounds and direction
        if (!isWithinBounds(x, y) ||
                (isWhite && currentY > y) ||
                (!isWhite && currentY < y)) return false;

        // Diagonal capture
        if (destX != currentX) {
            return Math.abs(destX - currentX) == 1 &&
                    Math.abs(currentY - y) == 1 &&
                    chessBoard.getPioniAt(x, y) != null &&
                    chessBoard.getPioniAt(x, y).getIsWhite() != isWhite;
        }

        // Cannot move forward onto an occupied square
        if (y != currentY && chessBoard.getPioniAt(x, y) != null) {
            return false;
        }

        // Check move distance
        if (Math.abs(y - currentY) > 2) {
            return false;
        }

        // Two-square move only allowed from starting position
        if (Math.abs(y - currentY) == 2) {
            if ((isWhite && currentY != 2) || (!isWhite && currentY != 7)) {
                return false;
            }
        }

        // Check path for blocking pieces
        ArrayList<int[]> route = getRoute(currentX, currentY, destX, y);
        return (route != null && !route.isEmpty() &&
                route.getLast()[0] == destX &&
                route.getLast()[1] == y) &&
                (this.chessBoard.getPioniAt(x, y) == null ||
                        this.chessBoard.getPioniAt(x, y).getIsWhite() != getIsWhite());
    }
}