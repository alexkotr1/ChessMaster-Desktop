package com.alexk.chess.Pionia;

import com.alexk.chess.ChessBoard.ChessBoard;
import com.alexk.chess.Utilities;

import java.io.Serializable;
import java.util.ArrayList;

public class Pyrgos extends Pioni implements Serializable {
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
        return (route != null && !route.isEmpty() && route.getLast()[0] == destX && route.getLast()[1] == y) && (this.getChessBoard().getPioniAt(x,y) == null || this.getChessBoard().getPioniAt(x,y).getIsWhite() != getIsWhite());

    }

    public void setMoved(boolean moved) { this.moved = moved; }
    public boolean getMoved() { return moved; }

}
