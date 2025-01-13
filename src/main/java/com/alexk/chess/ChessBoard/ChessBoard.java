package com.alexk.chess.ChessBoard;

import com.alexk.chess.ChessEngine.ChessEngine;
import com.alexk.chess.Pionia.Pioni;
import com.alexk.chess.Pionia.Pyrgos;
import com.alexk.chess.Pionia.Vasilias;
import com.alexk.chess.Utilities;

import java.util.ArrayList;

public abstract class ChessBoard {
    public abstract void placePioniAt(Pioni p, char xPos, int yPos);
    public abstract boolean isDangerousPosition(char xOrig, int yOrig, boolean white);
    public abstract void loadBoard();
    public abstract Pioni getPioniAt(char xPos, int yPos);
    public abstract ArrayList<Pioni> getPionia();
    public abstract void move(char xOrig, int yOrig, char xDest,int yDest);
    public abstract Boolean getWhiteTurn();
    public abstract void setWhiteTurn(boolean whiteTurn);
    public abstract void setMovesRemaining(int movesRemaining);
    public abstract int getMovesRemaining();
    public abstract void capture(Pioni p);
    public abstract void setGameEndedWinner(Boolean gameEnded, ChessEngine.Winner winner);
    public abstract Boolean getGameEnded();
    public abstract ChessEngine.Winner getWinner();

    public void printBoard(){
            System.out.println("   a  b  c  d  e  f  g  h  \n  ------------------------");
            for (int y = 8;y>=1;y--){
                System.out.printf("%d  %s  %s  %s  %s  %s  %s  %s  %s %d%n",y,
                        this.getPioniAt('A',y) == null ? " " : this.getPioniAt('A',y).print(),
                        this.getPioniAt('B',y) == null ? " " : this.getPioniAt('B',y).print(),
                        this.getPioniAt('C',y) == null ? " " : this.getPioniAt('C',y).print(),
                        this.getPioniAt('D',y) == null ? " " : this.getPioniAt('D',y).print(),
                        this.getPioniAt('E',y) == null ? " " : this.getPioniAt('E',y).print(),
                        this.getPioniAt('F',y) == null ? " " : this.getPioniAt('F',y).print(),
                        this.getPioniAt('G',y) == null ? " " : this.getPioniAt('G',y).print(),
                        this.getPioniAt('H',y) == null ? " " : this.getPioniAt('H',y).print(),
                        y);
            }
            System.out.println("  ------------------------\n   a  b  c  d  e  f  g  h");
    }

    public boolean castlingRights(boolean white, boolean kingSide){
        Vasilias king = null;
        Pyrgos rook = null;
        for (Pioni p : getPionia()){
            if (p.getIsWhite() == white){
                if (p.getType().equals("Vasilias")) king = (Vasilias) p;
                else if (p.getType().equals("Pyrgos")){
                    if (((Pyrgos) p).getKingSide() == kingSide) rook = (Pyrgos) p;
                }
            }
        }
        return king != null && rook != null && !king.getMoved() && !rook.getMoved();
    }
    public int[] translateToBlackView(int x, int y){
        return new int[]{Math.abs(x - 9),Math.abs(y - 9)};
    }
    public int[] translateToWhiteView(int x, int y){
        return new int[]{x + Math.abs(x - 9),y + Math.abs(y - 9)};
    }
}
