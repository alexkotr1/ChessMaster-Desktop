package com.example.chessgui;

public class ChessEngine {
    ChessBoard chessBoard = new ChessBoard();
    public void playChess(){
        chessBoard.loadBoard();
        chessBoard.printBoard();
    }
    public boolean nextMove(char xOrig, int yOrig, char xDest, int yDest){
        Pioni p = chessBoard.getPioniAt(xOrig,yOrig);
        if (p == null) {
            System.out.println("There is no pioni at " + xOrig + " at " + yOrig);
            return false;
        }
        Pioni pioniAtDestination = chessBoard.getPioniAt(xDest,yDest);
        if (p.getIsWhite() != chessBoard.getWhiteTurn()) {
            System.out.println("It's not " + (chessBoard.getWhiteTurn() ? "black" : "white" + "'s turn"));
            return false;
        }
        if (!p.isLegalMove(xDest,yDest) || (pioniAtDestination != null && pioniAtDestination.isWhite == p.isWhite)) {
            System.out.println("Illegal move!");
            return false;
        }
        if (pioniAtDestination != null) chessBoard.capture(pioniAtDestination);

        chessBoard.move(xOrig,yOrig,xDest,yDest);
        chessBoard.printBoard();
        return true;
    }
}
