package com.example.chessgui;

public class Vasilias extends Pioni {
    public Vasilias(Boolean isWhite, ChessBoard chessBoard, char initialX, int initialY) {
        super(isWhite, chessBoard, initialX, initialY);
    }

    @Override
    public boolean isLegalMove(char x, int y) {
        int currentPositionX = position[0];
        int currentPositionY = position[1];
        int nextPositionX = Utilities.char2Int(x);
        int xDiff = Math.abs(currentPositionX - nextPositionX);
        int yDiff = Math.abs(currentPositionY - y);
        return xDiff <= 1 && yDiff <= 1 && (xDiff != 0 || yDiff != 0);
    }
}
