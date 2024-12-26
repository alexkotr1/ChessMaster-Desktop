package com.alexk.chess;

import java.util.ArrayList;

public class ChessBoard {
    private final ArrayList<Pioni> Pionia = new ArrayList<>();
    private boolean whiteTurn = true;

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
            Pionia.add(new Stratiotis(x < 9,this,Utilities.int2Char(x < 9 ? x : x - 8 ),x < 9 ? 2 : 7));
        }
        for (int x = 0;x<2;x++){
            for (int y = 0;y<4;y++){
             switch (y){
                 case 0:{
                     Pionia.add(new Pyrgos(x == 0,this,'A',x == 0 ? 1 : 8));
                     Pionia.add(new Pyrgos(x == 0,this,'H',x == 0 ? 1 : 8));
                     break;
                 }
                 case 1:{
                     Pionia.add(new Alogo(x == 0,this,'B',x == 0 ? 1 : 8));
                     Pionia.add(new Alogo(x == 0,this,'G',x == 0 ? 1 : 8));
                     break;
                 }
                 case 2:{
                     Pionia.add(new Stratigos(x == 0,this,'C',x == 0 ? 1 : 8));
                     Pionia.add(new Stratigos(x == 0,this,'F',x == 0 ? 1 : 8));
                     break;
                 }
                 case 3:{
                     Pionia.add(new Vasilissa(x == 0,this,'D',x == 0 ? 1 : 8));
                     Pionia.add(new Vasilias(x == 0,this,'E',x == 0 ? 1 : 8));
                     break;
                 }
             }
            }
        }
    }
    public Pioni getPioniAt(char xPos, int yPos){
        return Pionia.stream().filter(pioni -> Utilities.int2Char(pioni.getPosition()[0]) == xPos && pioni.getPosition()[1] == yPos && !pioni.getCaptured()).findFirst().orElse(null);
    }

    public ArrayList<Pioni> getPionia(){
        return Pionia;
    }
    public void move(char xOrig, int yOrig, char xDest,int yDest){
        Pioni p = getPioniAt(xOrig,yOrig);
        Pioni pioniAtDestination = getPioniAt(xDest,yDest);
        if (pioniAtDestination != null && p.getIsWhite() != pioniAtDestination.getIsWhite()) capture(pioniAtDestination);
        placePioniAt(p,xDest,yDest);
        if (p.type.equals("Pyrgos")) ((Pyrgos) p).setMoved(true);
        else if (p.type.equals("Vasilias")) ((Vasilias) p).setMoved(true);
    }
    public Boolean getWhiteTurn(){
        return whiteTurn;
    }
    public void setWhiteTurn(boolean whiteTurn){ this.whiteTurn = whiteTurn; }
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
    @Override
    protected ChessBoard clone() {
        ChessBoard chessBoard = new ChessBoard();
        chessBoard.whiteTurn = whiteTurn;
        for (Pioni p : Pionia){
            Pioni clone = p.clone();
            clone.setChessBoard(chessBoard);
            chessBoard.Pionia.add(clone);
        }
        return chessBoard;
    }
    public void capture(Pioni p){
        p.setCaptured(true);
    }
}
