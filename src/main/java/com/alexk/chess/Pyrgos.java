package com.alexk.chess;

import java.util.ArrayList;

public class Pyrgos extends Pioni {
private boolean moved;

    public Pyrgos(Boolean isWhite, ChessBoard chessBoard, char initialX, int initialY) {
        super(isWhite, chessBoard, initialX, initialY);
    }

    @Override
    public boolean isLegalMove(char x, int y) {
        if (!isWithinBounds(x,y)) return false;
        int destX = Utilities.char2Int(x);
        int currentX = Utilities.char2Int(getXPos());
        int currentY = getYPos();
        ArrayList<int[]> route = getRoute(currentX,currentY,destX,y);
        return (route != null && !route.isEmpty() && route.getLast()[0] == destX && route.getLast()[1] == y) && (this.chessBoard.getPioniAt(x,y) == null || this.chessBoard.getPioniAt(x,y).getIsWhite() != getIsWhite());

    }

    public void setMoved(boolean moved) { this.moved = moved; }
    public boolean getMoved() { return moved; }

}
