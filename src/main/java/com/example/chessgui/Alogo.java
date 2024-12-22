package com.example.chessgui;

import java.util.Arrays;

public class Alogo extends Pioni {


    public Alogo(Boolean isWhite, ChessBoard chessBoard, char initialX, int initialY) {
        super(isWhite, chessBoard, initialX, initialY);
    }

    @Override
    public boolean isLegalMove(char x, int y) {
        if (!isWithinBounds(x,y)) return false;
        int destX = Utilities.char2Int(x);
        int destY = y;
        int currentX = Utilities.char2Int(getXPos());
        int currentY = getYPos();
        final int[][] allowed = {{2,-1},{-1,-2},{1,-2},{2,-1},{2,1},{1,2},{-1,2},{-2,1}};
        return Arrays.stream(allowed).anyMatch(arr -> currentX + arr[0] == destX && currentY + arr[1] == destY);
    }
}
