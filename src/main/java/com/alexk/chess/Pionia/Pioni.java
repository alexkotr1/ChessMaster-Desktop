package com.alexk.chess.Pionia;

import com.alexk.chess.ChessBoard;
import com.alexk.chess.Utilities;

import java.util.ArrayList;

/**
 * Abstract base class representing a chess piece in the game.
 * <p>
 * This class provides common functionality and properties for all chess pieces,
 * including position management, move validation, and board interaction.
 * All specific piece types (Pawn, Rook, Knight, etc.) extend this class.
 * </p>
 *
 * @author Alex K
 * @version 1.0
 * @see ChessBoard
 * @see Utilities
 */
public abstract class Pioni {

    /**
     * Indicates whether the piece is white (true) or black (false).
     */
    public boolean isWhite;

    /**
     * The type of the chess piece (e.g., "Alogo", "Pyrgos", "Vasilias").
     */
    public String type;

    /**
     * The current position of the piece as [x, y] coordinates.
     * x: column (1-8 converted from A-H)
     * y: row (1-8)
     */
    public int[] position = new int[2];

    /**
     * Reference to the chess board on which this piece is placed.
     */
    public ChessBoard chessBoard;

    /**
     * File path to the image representing this piece.
     */
    private String imagePath;

    /**
     * Indicates whether this piece has been captured.
     */
    private boolean captured;

    /**
     * Constructs a new chess piece with the specified parameters.
     * <p>
     * Initializes the piece's position, type, color, and associated chess board.
     * Also sets the appropriate image path based on piece type and color.
     * </p>
     *
     * @param isWhite     indicates whether the piece is white (true) or black (false)
     * @param chessBoard  the chess board on which the piece is placed
     * @param initialX    the initial column position (A-H)
     * @param initialY    the initial row position (1-8)
     * @throws IllegalArgumentException if initialX or initialY are out of bounds
     */
    public Pioni(Boolean isWhite, ChessBoard chessBoard, char initialX, int initialY) {
        position[0] = Utilities.char2Int(initialX);
        position[1] = initialY;
        this.type = this.getClass().getSimpleName();
        this.isWhite = isWhite;
        this.chessBoard = chessBoard;

        // Set image path based on piece type and color
        switch (this.type) {
            case "Alogo":
                this.imagePath = this.isWhite ? "white knight.png" : "black knight.png";
                break;
            case "Pyrgos":
                this.imagePath = this.isWhite ? "white rook.png" : "black rook.png";
                break;
            case "Stratigos":
                this.imagePath = this.isWhite ? "white bishop.png" : "black bishop.png";
                break;
            case "Stratiotis":
                this.imagePath = this.isWhite ? "white pawn.png" : "black pawn.png";
                break;
            case "Vasilias":
                this.imagePath = this.isWhite ? "white king.png" : "black king.png";
                break;
            case "Vasilissa":
                this.imagePath = this.isWhite ? "white queen.png" : "black queen.png";
                break;
        }
    }

    /**
     * Determines whether moving this piece to the specified position is a legal move.
     * <p>
     * This abstract method must be implemented by each specific piece type
     * to define its unique movement rules.
     * </p>
     *
     * @param x the destination column (A-H)
     * @param y the destination row (1-8)
     * @return true if the move is legal according to chess rules, false otherwise
     */
    public abstract boolean isLegalMove(char x, int y);

    /**
     * Checks if the specified position is within the chess board boundaries.
     * <p>
     * The board is defined as columns A-H (1-8) and rows 1-8.
     * </p>
     *
     * @param x the column to check (A-H)
     * @param y the row to check (1-8)
     * @return true if the position is within board boundaries, false otherwise
     */
    protected boolean isWithinBounds(char x, int y) {
        int intX = Utilities.char2Int(x);
        return intX > 0 && intX <= 8 && y > 0 && y <= 8;
    }

    /**
     * Calculates the route from one position to another.
     * <p>
     * This method determines all intermediate squares between the start and end positions.
     * It handles straight lines (for rooks, queens, and pawns) and diagonals (for bishops and queens).
     * </p>
     *
     * @param x1 the starting column coordinate (1-8)
     * @param y1 the starting row coordinate (1-8)
     * @param x2 the ending column coordinate (1-8)
     * @param y2 the ending row coordinate (1-8)
     * @return an ArrayList of [x,y] coordinates representing the route,
     *         or null if the piece cannot move in a straight/diagonal line
     */
    public ArrayList<int[]> getRoute(int x1, int y1, int x2, int y2) {
        if (x2 < 0 || x2 > 8 || y2 < 0 || y2 > 8) return null;

        ArrayList<int[]> route = new ArrayList<>();

        // Handle straight moves (rook, queen, pawn)
        if ((y1 == y2 || x1 == x2) && (type.equals("Pyrgos") || type.equals("Vasilissa") || type.equals("Stratiotis"))) {
            boolean x1Equalsx2 = x1 == x2;
            int diff = Math.abs((x1 == x2 ? y1 - y2 : x1 - x2));

            for (int i = 0; i <= diff; i++) {
                int[] dest = x1Equalsx2 ?
                        new int[]{x1, y1 + (y1 > y2 ? -i : i)} :
                        new int[]{x1 + (x1 > x2 ? -i : i), y1};
                route.add(dest);

                // Stop if there's a piece blocking the path (not at start or end)
                if (i != 0 && i != diff &&
                        chessBoard.getPioniAt(Utilities.int2Char(dest[0]), dest[1]) != null) {
                    break;
                }
            }
        }
        // Handle diagonal moves (bishop, queen)
        else if (x1 != x2 && y1 != y2 && (type.equals("Vasilissa") || type.equals("Stratigos"))) {
            int diff = Math.abs(x1 - x2);
            if (Math.abs(x1 - x2) != Math.abs(y1 - y2)) return null;

            for (int i = 0; i <= diff; i++) {
                int destX = x1 + (x1 > x2 ? -i : i);
                int destY = y1 + (y1 > y2 ? -i : i);
                route.add(new int[]{destX, destY});

                // Stop if there's a piece blocking the path (not at start or end)
                if (i != 0 && i != diff &&
                        chessBoard.getPioniAt(Utilities.int2Char(destX), destY) != null) {
                    break;
                }
            }
        }
        return route;
    }

    /**
     * Returns the character representation of this piece for text display.
     * <p>
     * Uses standard chess notation:
     * k/K = king, q/Q = queen, r/R = rook, n/N = knight, b/B = bishop, p/P = pawn
     * Uppercase for white pieces, lowercase for black pieces.
     * </p>
     *
     * @return a single character representing this piece
     */
    public String print() {
        String printChar = "";
        switch (type) {
            case "Vasilias":
                printChar = "k";
                break;
            case "Vasilissa":
                printChar = "q";
                break;
            case "Pyrgos":
                printChar = "r";
                break;
            case "Alogo":
                printChar = "n";
                break;
            case "Stratigos":
                printChar = "b";
                break;
            case "Stratiotis":
                printChar = "p";
                break;
            default:
                System.err.println("Something went wrong!");
                break;
        }
        return isWhite ? printChar.toUpperCase() : printChar;
    }

    /**
     * Sets the position of this piece to the specified coordinates.
     *
     * @param x the new column position (A-H)
     * @param y the new row position (1-8)
     */
    public void setPosition(char x, int y) {
        position[0] = Utilities.char2Int(x);
        position[1] = y;
    }

    /**
     * Gets the current position of this piece.
     *
     * @return an array containing [x, y] coordinates
     */
    public int[] getPosition() {
        return position;
    }

    /**
     * Sets the X-coordinate (column) of this piece using an integer value.
     *
     * @param x the column coordinate (1-8)
     */
    public void setXPos(int x) {
        position[0] = x;
    }

    /**
     * Sets the X-coordinate (column) of this piece using a character value.
     *
     * @param x the column (A-H)
     */
    public void setXPos(char x) {
        position[0] = Utilities.char2Int(x);
    }

    /**
     * Sets the Y-coordinate (row) of this piece.
     *
     * @param y the row coordinate (1-8)
     */
    public void setYPos(int y) {
        position[1] = y;
    }

    /**
     * Gets the current X-coordinate (column) as a character.
     *
     * @return the column (A-H)
     */
    public char getXPos() {
        return Utilities.int2Char(position[0]);
    }

    /**
     * Gets the current Y-coordinate (row).
     *
     * @return the row (1-8)
     */
    public int getYPos() {
        return position[1];
    }

    /**
     * Sets the color of this piece.
     *
     * @param isWhite true for white, false for black
     */
    public void setIsWhite(boolean isWhite) {
        this.isWhite = isWhite;
    }

    /**
     * Gets the color of this piece.
     *
     * @return true if white, false if black
     */
    public boolean getIsWhite() {
        return isWhite;
    }

    /**
     * Sets the chess board associated with this piece.
     *
     * @param chessBoard the chess board to associate with this piece
     */
    public void setChessBoard(ChessBoard chessBoard) {
        this.chessBoard = chessBoard;
    }

    /**
     * Gets the chess board associated with this piece.
     *
     * @return the associated chess board
     */
    public ChessBoard getChessBoard() {
        return chessBoard;
    }

    /**
     * Sets the type of this piece.
     *
     * @param type the piece type (e.g., "Alogo", "Pyrgos", "Vasilias")
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the type of this piece.
     *
     * @return the piece type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the image path for this piece.
     *
     * @param imagePath the file path to the piece's image
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /**
     * Gets the image path for this piece.
     *
     * @return the file path to the piece's image
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Sets the captured status of this piece.
     *
     * @param captured true if the piece has been captured, false otherwise
     */
    public void setCaptured(Boolean captured) {
        this.captured = captured;
    }

    /**
     * Gets the captured status of this piece.
     *
     * @return true if the piece has been captured, false otherwise
     */
    public Boolean getCaptured() {
        return captured;
    }

    /**
     * Prints a route to the console for debugging purposes.
     * <p>
     * Displays each position in the route as [column, row] format.
     * </p>
     *
     * @param route the route to print, as an ArrayList of [x,y] coordinates
     */
    public static void printRoute(ArrayList<int[]> route) {
        if (route == null || route.isEmpty()) {
            System.out.println("Null or empty route");
            return;
        }
        for (int i = 0; i < route.size(); i++) {
            System.out.printf("%d:[%c,%s]%n", i,
                    Utilities.int2Char(route.get(i)[0]),
                    route.get(i)[1]);
        }
    }

    /**
     * Returns a string representation of this piece.
     * <p>
     * Format: "Type: [type] Position: [column,row]"
     * </p>
     *
     * @return a string describing this piece
     */
    @Override
    public String toString() {
        return String.format("Type: %s Position: [%c,%d]",
                type,
                Utilities.int2Char(position[0]),
                position[1]);
    }

    /**
     * Creates and returns a copy of this piece.
     * <p>
     * Uses reflection to create a new instance of the same class.
     * The cloned piece will not be associated with any chess board.
     * </p>
     *
     * @return a cloned copy of this piece
     * @throws AssertionError if the clone operation fails
     */
    @Override
    public Pioni clone() {
        try {
            Pioni cloned = this.getClass()
                    .getConstructor(Boolean.class, ChessBoard.class, char.class, int.class)
                    .newInstance(this.isWhite, null,
                            Utilities.int2Char(this.position[0]),
                            this.position[1]);
            cloned.setCaptured(this.getCaptured());
            cloned.setImagePath(this.getImagePath());
            return cloned;
        } catch (Exception e) {
            throw new AssertionError("Clone operation failed", e);
        }
    }
}