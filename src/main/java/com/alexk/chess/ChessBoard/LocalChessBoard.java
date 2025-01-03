package com.alexk.chess.ChessBoard;

import com.alexk.chess.ChessEngine.ChessEngine;
import com.alexk.chess.Pionia.*;
import com.alexk.chess.Utilities;

import java.util.ArrayList;

public class LocalChessBoard extends ChessBoard {
    private final ArrayList<Pioni> pionia = new ArrayList<>();
    private boolean whiteTurn = true;
    private int movesRemaining = 100;
    private Boolean gameEnded = false;
    private ChessEngine.Winner winner = null;

    public void placePioniAt(Pioni p, char xPos, int yPos){
        p.setXPos(xPos);
        p.setYPos(yPos);
    }
    public boolean isDangerousPosition(char xOrig, int yOrig, boolean white){
        for (Pioni p : getPionia().stream().filter(pioni -> pioni.getIsWhite() != white).toList()) {
            if (p.isLegalMove(xOrig,yOrig)) return true;
        }
        return false;
    }
    public void loadBoard(){
        for (int x = 1;x<=16;x++){
            pionia.add(new Stratiotis(x < 9,this, Utilities.int2Char(x < 9 ? x : x - 8 ),x < 9 ? 2 : 7,null,false));
        }
        for (int x = 0;x<2;x++){
            for (int y = 0;y<4;y++){
                switch (y){
                    case 0:{
                        pionia.add(new Pyrgos(x == 0,this,'A',x == 0 ? 1 : 8,null,false));
                        pionia.add(new Pyrgos(x == 0,this,'H',x == 0 ? 1 : 8,null,false));
                        break;
                    }
                    case 1:{
                        pionia.add(new Alogo(x == 0,this,'B',x == 0 ? 1 : 8,null,false));
                        pionia.add(new Alogo(x == 0,this,'G',x == 0 ? 1 : 8,null,false));
                        break;
                    }
                    case 2:{
                        pionia.add(new Stratigos(x == 0,this,'C',x == 0 ? 1 : 8,null,false));
                        pionia.add(new Stratigos(x == 0,this,'F',x == 0 ? 1 : 8,null,false));
                        break;
                    }
                    case 3:{
                        pionia.add(new Vasilissa(x == 0,this,'D',x == 0 ? 1 : 8,null,false));
                        pionia.add(new Vasilias(x == 0,this,'E',x == 0 ? 1 : 8,null,false));
                        break;
                    }
                }
            }
        }
    }


    public Pioni getPioniAt(char xPos, int yPos){
        return pionia.stream().filter(pioni -> Utilities.int2Char(pioni.getPosition()[0]) == xPos && pioni.getPosition()[1] == yPos && !pioni.getCaptured()).findFirst().orElse(null);
    }

    public ArrayList<Pioni> getPionia(){
        return pionia;
    }
    public void move(char xOrig, int yOrig, char xDest,int yDest){
        movesRemaining--;
        Pioni p = getPioniAt(xOrig,yOrig);
        Pioni pDest = getPioniAt(xDest,yDest);
        if (p.getType().equals("Stratiotis")) movesRemaining = 100;
        if (pDest != null) capture(pDest);
        placePioniAt(p,xDest,yDest);
        if (p.getType().equals("Pyrgos")) ((Pyrgos) p).setMoved(true);
        else if (p.getType().equals("Vasilias")) ((Vasilias) p).setMoved(true);
    }

    public Boolean getWhiteTurn(){
        return whiteTurn;
    }
    public void setWhiteTurn(boolean whiteTurn){ this.whiteTurn = whiteTurn; }
    public void setMovesRemaining(int movesRemaining){ this.movesRemaining = movesRemaining; }
    public int getMovesRemaining(){ return movesRemaining; }
    public void setGameEndedWinner(Boolean gameEnded, ChessEngine.Winner winner) {
        this.gameEnded = gameEnded;
        this.winner = winner;
    }
    public Boolean getGameEnded() { return gameEnded; }
    public ChessEngine.Winner getWinner() { return winner; }

    @Override
    public ChessBoard clone() {
        ChessBoard chessBoard = new LocalChessBoard();
        chessBoard.setWhiteTurn(whiteTurn);
        for (Pioni p : pionia){
            Pioni clone = p.clone();
            clone.setChessBoard(chessBoard);
            chessBoard.getPionia().add(clone);
        }
        return chessBoard;
    }
    public void capture(Pioni p){
        p.setCaptured(true);
    }
}
