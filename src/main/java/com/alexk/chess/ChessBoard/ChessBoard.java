package com.alexk.chess.ChessBoard;

import com.alexk.chess.ChessEngine.ChessEngine;
import com.alexk.chess.Pionia.Pioni;

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
}
