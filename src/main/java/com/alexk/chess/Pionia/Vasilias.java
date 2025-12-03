package com.alexk.chess.Pionia;

import com.alexk.chess.ChessBoard;
import com.alexk.chess.Utilities;

import java.util.ArrayList;

/**
 * Represents a King piece in a chess game.
 * <p>
 * The Vasilias (King) class extends the Pioni abstract class and implements
 * the specific movement rules for a king in chess. The king moves one square
 * in any direction and can perform castling under specific conditions.
 * </p>
 *
 * @author Alex K
 * @version 1.0
 * @see Pioni
 * @see ChessBoard
 */
public class Vasilias extends Pioni {

    /**
     * Indicates whether this king has moved from its starting position.
     * Important for castling rights.
     */
    private boolean moved;

    /**
     * Constructs a new Vasilias (King) piece with the specified parameters.
     *
     * @param isWhite     indicates whether the king is white (true) or black (false)
     * @param chessBoard  the chess board on which the king is placed
     * @param initialX    the initial column position of the king (A-H)
     * @param initialY    the initial row position of the king (1-8)
     * @throws IllegalArgumentException if initialX or initialY are out of bounds
     */
    public Vasilias(Boolean isWhite, ChessBoard chessBoard, char initialX, int initialY) {
        super(isWhite, chessBoard, initialX, initialY);
    }

    /**
     * Determines whether moving the king to the specified position is a legal move.
     * <p>
     * A king can move one square in any direction (horizontally, vertically, or diagonally).
     * The king can also castle under the following conditions:
     * 1. Neither the king nor the castling rook has previously moved
     * 2. There are no pieces between the king and the rook
     * 3. The king is not in check
     * 4. The king does not pass through or end up in check
     * </p>
     *
     * @param x the destination column (A-H)
     * @param y the destination row (1-8)
     * @return true if the move is legal according to king movement rules, false otherwise
     */
    @Override
    public boolean isLegalMove(char x, int y) {
        int currentPositionX = position[0];
        int currentPositionY = position[1];
        int nextPositionX = Utilities.char2Int(x);
        int xDiff = Math.abs(currentPositionX - nextPositionX);
        int yDiff = Math.abs(currentPositionY - y);

        Pioni pioniAtDestination = chessBoard.getPioniAt(x, y);

        // Check for castling
        if (pioniAtDestination != null &&
                pioniAtDestination.getIsWhite() == getIsWhite() &&
                pioniAtDestination.type.equals("Pyrgos")) {

            boolean rookMoved = ((Pyrgos) pioniAtDestination).getMoved();
            boolean kingMoved = getMoved();

            if (!rookMoved && !kingMoved) {
                ArrayList<int[]> route = pioniAtDestination.getRoute(
                        Utilities.char2Int(pioniAtDestination.getXPos()),
                        pioniAtDestination.getYPos(),
                        position[0],
                        position[1]);

                if (route != null && !route.isEmpty() &&
                        route.stream().noneMatch(r -> {
                            Pioni pAtDest = chessBoard.getPioniAt(Utilities.int2Char(r[0]), r[1]);
                            return chessBoard.isDangerousPosition(Utilities.int2Char(r[0]), r[1], getIsWhite()) ||
                                    (pAtDest != null && !pAtDest.type.equals("Pyrgos") && !pAtDest.type.equals("Vasilias"));
                        })) {
                    return true;
                }
            }
        }

        // Normal king move (one square in any direction)
        return xDiff <= 1 && yDiff <= 1 &&
                (xDiff != 0 || yDiff != 0) &&
                (this.chessBoard.getPioniAt(x, y) == null ||
                        this.chessBoard.getPioniAt(x, y).getIsWhite() != getIsWhite());
    }

    /**
     * Sets whether this king has moved from its starting position.
     *
     * @param moved true if the king has moved, false otherwise
     */
    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    /**
     * Gets whether this king has moved from its starting position.
     *
     * @return true if the king has moved, false otherwise
     */
    public boolean getMoved() {
        return moved;
    }
}