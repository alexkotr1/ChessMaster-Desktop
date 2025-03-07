package com.alexk.chess.ChessEngine;

import com.alexk.chess.ChessBoard.ChessBoard;
import com.alexk.chess.Pionia.Pioni;
import com.alexk.chess.Utilities;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class ChessEngine  {
    public enum Winner { White, Black, Draw };
    public abstract void playChess();
    public abstract ArrayList<Pioni> nextMove(char xOrig, int yOrig, char xDest, int yDest);
    public abstract Pioni upgradePioni(Pioni p,String type);
    public abstract HashMap<Pioni,ArrayList<int[]>> kingCheckMate(boolean white);
    public abstract boolean stalemateCheck(boolean white);
    public abstract boolean checkDumbMove(Pioni p, int[] dest);
    public abstract ChessBoard getBoard();
    public abstract void refreshBoard(Runnable callback);
    public abstract void setTotalMoves(int totalMoves);
    public abstract int getTotalMoves();
    public String toFen(){
        StringBuilder fen = new StringBuilder();
        for (int y = 8;y>=1;y--){
            int emptyCounter = 0;
            for (int x = 1;x<=8;x++){
                Pioni pioni = getBoard().getPioniAt(Utilities.int2Char(x),y);
                if (pioni != null) {
                    if (emptyCounter != 0) fen.append(emptyCounter);
                    fen.append(pioni.print());
                    emptyCounter = 0;
                }
                else emptyCounter++;
                if (x == 8) fen.append(emptyCounter == 0 ? "" : emptyCounter).append(y != 1 ? "/" : "");
            }
        }
        fen.append(" ").append(getBoard().getWhiteTurn() ? "w" : "b").append(" ");
        boolean whiteKingSideRights = getBoard().castlingRights(true,true);
        boolean whiteQueenSideRights = getBoard().castlingRights(true,false);
        boolean blackKingSideRights = getBoard().castlingRights(false,true);
        boolean blackQueenSideRights = getBoard().castlingRights(false,false);

        if (whiteKingSideRights) fen.append("K");
        if (whiteQueenSideRights) fen.append("Q");
        if (blackKingSideRights) fen.append("k");
        if (blackQueenSideRights) fen.append("q");

        if (!whiteKingSideRights && !whiteQueenSideRights && !blackKingSideRights && !blackQueenSideRights) fen.append("-");

        fen
                .append(" ")
                .append("-")
                .append(" ")
                .append(getBoard().getMovesRemaining())
                .append(" ")
                .append(getTotalMoves());

        return fen.toString();
    }

}
