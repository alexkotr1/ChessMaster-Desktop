package com.example.chessgui;

import java.util.ArrayList;

public class Vasilissa extends Pioni {
    public Vasilissa(Boolean isWhite, ChessBoard chessBoard, char initialX, int initialY) {
        super(isWhite, chessBoard, initialX, initialY);
    }

    @Override
    public boolean isLegalMove(char x, int y) {
        if (!isWithinBounds(x,y)) return false;
        int destX = Utilities.char2Int(x);
        int currentX = Utilities.char2Int(getXPos());
        int currentY = getYPos();
        ArrayList<int[]> route = getRoute(currentX,currentY,destX,y);
        printRoute(route);
        return (route != null && !route.isEmpty() && route.getLast()[0] == destX && route.getLast()[1] == y);
    }
}
