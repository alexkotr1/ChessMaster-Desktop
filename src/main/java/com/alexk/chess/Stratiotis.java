package com.alexk.chess;

import java.util.ArrayList;

public class Stratiotis extends Pioni {


    public Stratiotis(Boolean isWhite, ChessBoard chessBoard, char initialX, int initialY) {
        super(isWhite, chessBoard, initialX, initialY);
    }

    @Override
    public boolean isLegalMove(char x, int y) {
        int destX = Utilities.char2Int(x);
        int currentX = Utilities.char2Int(getXPos());
        int currentY = getYPos();
        if (!isWithinBounds(x,y) || (isWhite && currentY > y) || (!isWhite && currentY < y)) return false;
        if (destX != currentX){
            return Math.abs(destX - currentX) == 1 && Math.abs(currentY - y) == 1 && chessBoard.getPioniAt(x, y) != null && chessBoard.getPioniAt(x, y).getIsWhite() != isWhite;
        }
        if (y != currentY && chessBoard.getPioniAt(x, y) != null) {return false;}

        if (Math.abs(y - currentY) > 2) {
            return false;
        }

        if (Math.abs(y - currentY) == 2) {
            if ((isWhite && currentY != 2) || (!isWhite && currentY != 7)) {
                return false;
            }
        }
        ArrayList<int[]> route = getRoute(currentX,currentY,destX,y);
        return (route != null && !route.isEmpty() && route.getLast()[0] == destX && route.getLast()[1] == y) && (this.chessBoard.getPioniAt(x,y) == null || this.chessBoard.getPioniAt(x,y).getIsWhite() != getIsWhite());
    }
}
