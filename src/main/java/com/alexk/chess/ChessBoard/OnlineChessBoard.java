package com.alexk.chess.ChessBoard;

import com.alexk.chess.ChessEngine.ChessEngine;
import com.alexk.chess.Pionia.Pioni;
import com.alexk.chess.Utilities;

import java.util.ArrayList;

public class OnlineChessBoard extends ChessBoard {
    private final ArrayList<Pioni> pionia = new ArrayList<>();
    private boolean whiteTurn = true;
    private int movesRemaining = 100;
    private Boolean gameEnded = false;
    private ChessEngine.Winner winner = null;
    private String state;
    public OnlineChessBoard(ArrayList<Pioni> pionia, boolean whiteTurn, int movesRemaining, boolean gameEnded, String state, ChessEngine.Winner winner){
        this.pionia.addAll(pionia);
        this.whiteTurn = whiteTurn;
        this.movesRemaining = movesRemaining;
        this.gameEnded = gameEnded;
        this.state = state;
        this.winner = winner;
    }
    public OnlineChessBoard(){
        this.loadBoard();
    }
    public boolean isDangerousPosition(char xOrig, int yOrig, boolean white){
        for (Pioni p : getPionia().stream().filter(pioni -> pioni.getIsWhite() != white).toList()) {
            if (p.isLegalMove(xOrig,yOrig)) return true;
        }
        return false;
    }
    public Pioni getPioniAt(char xPos, int yPos){
        return pionia.stream().filter(pioni -> Utilities.int2Char(pioni.getPosition()[0]) == xPos && pioni.getPosition()[1] == yPos && !pioni.getCaptured()).findFirst().orElse(null);
    }

    public ArrayList<Pioni> getPionia(){
        return pionia;
    }
    public void setPionia(ArrayList<Pioni> pionia){
        this.pionia.clear();
        this.pionia.addAll(pionia);
    }
    public Boolean getWhiteTurn(){
        return whiteTurn;
    }
    public Pioni getPioniAt(int x, int y){
        return getPioniAt(Utilities.int2Char(x),y);
    }
    public String getState() {return state;}
    public void setState(String state) {this.state = state;}
    public void setGameEnded(Boolean gameEnded) {this.gameEnded = gameEnded;}
    public void setGameEndedWinner(Boolean gameEnded, ChessEngine.Winner winner) {}
    public Boolean getGameEnded() { return gameEnded; }
    public void setWhiteTurn(boolean whiteTurn){ this.whiteTurn = whiteTurn; }
    public void setMovesRemaining(int movesRemaining){ this.movesRemaining = movesRemaining; }


    public void loadBoard(){}
    public void move(char xOrig, int yOrig, char xDest,int yDest){}
    public int getMovesRemaining(){ return movesRemaining; }
    public ChessEngine.Winner getWinner() { return winner; }
    public void setWinner(ChessEngine.Winner winner) { this.winner = winner; }
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
    public void capture(Pioni p){}
    public void placePioniAt(Pioni p, char xPos, int yPos){}

}
