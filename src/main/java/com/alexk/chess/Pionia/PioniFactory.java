package com.alexk.chess.Pionia;


import com.alexk.chess.ChessBoard.ChessBoard;

public class PioniFactory {
    public static Pioni createPioni(String type, boolean isWhite, ChessBoard board, char xPos, int yPos, String id, Boolean captured) {
        switch (type) {
            case "Stratiotis":
                return new Stratiotis(isWhite, board, xPos, yPos, id, captured);
            case "Pyrgos":
                return new Pyrgos(isWhite, board, xPos, yPos, id, captured);
            case "Alogo":
                return new Alogo(isWhite, board, xPos, yPos, id, captured);
            case "Stratigos":
                return new Stratigos(isWhite, board, xPos, yPos, id, captured);
            case "Vasilissa":
                return new Vasilissa(isWhite, board, xPos, yPos, id, captured);
            case "Vasilias":
                return new Vasilias(isWhite, board, xPos, yPos, id, captured);
            default:
                throw new IllegalArgumentException("Unknown Pioni type: " + type);
        }
    }
}