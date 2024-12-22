package com.example.chessgui;

import java.util.ArrayList;

public class Stratigos extends Pioni {


    public Stratigos(Boolean isWhite, ChessBoard chessBoard, char initialX, int initialY) {
        super(isWhite, chessBoard, initialX, initialY);
    }

    @Override
    public boolean isLegalMove(char x, int y) {
        if (!isWithinBounds(x,y)) return false;
        int destX = Utilities.char2Int(x);
        int currentX = Utilities.char2Int(getXPos());
        int currentY = getYPos();
        ArrayList<int[]> route = getRoute(currentX,currentY,destX,y);
        System.out.println("Dest X: " + destX + " DestY: " + y);
        Pioni.printRoute(route);
        return (route != null && !route.isEmpty() && route.getLast()[0] == destX && route.getLast()[1] == y);

    }

}
