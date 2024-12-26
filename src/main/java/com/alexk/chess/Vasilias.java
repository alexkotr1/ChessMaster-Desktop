package com.alexk.chess;

import java.util.ArrayList;

public class Vasilias extends Pioni {
    private boolean moved;
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
        Pioni pioniAtDestination = chessBoard.getPioniAt(x,y);
        if (pioniAtDestination != null && pioniAtDestination.getIsWhite() == getIsWhite() && pioniAtDestination.type.equals("Pyrgos")) {
            boolean rookMoved = ((Pyrgos) pioniAtDestination).getMoved();
            boolean kingMoved = getMoved();
            if (!rookMoved && !kingMoved) {
                ArrayList<int[]> route = pioniAtDestination.getRoute(Utilities.char2Int(pioniAtDestination.getXPos()), pioniAtDestination.getYPos(), position[0], position[1]);
                if (route != null && !route.isEmpty() && route.stream().noneMatch(r->{
                    Pioni pAtDest = chessBoard.getPioniAt(Utilities.int2Char(r[0]),r[1]);
                    return chessBoard.isDangerousPosition(Utilities.int2Char(r[0]),r[1],getIsWhite()) || (pAtDest != null && !pAtDest.type.equals("Pyrgos") && !pAtDest.type.equals("Vasilias"));
                })) return true;
            }
        }
        return xDiff <= 1 && yDiff <= 1 && (xDiff != 0 || yDiff != 0) && (this.chessBoard.getPioniAt(x,y) == null || this.chessBoard.getPioniAt(x,y).getIsWhite() != getIsWhite());
    }

    public void setMoved(boolean moved) {this.moved = moved;}
    public boolean getMoved() {return moved;}
}
