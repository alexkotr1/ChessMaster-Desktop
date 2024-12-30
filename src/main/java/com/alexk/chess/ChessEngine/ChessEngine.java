package com.alexk.chess.ChessEngine;

import com.alexk.chess.ChessBoard.ChessBoard;
import com.alexk.chess.Pionia.Pioni;

import javax.security.auth.callback.Callback;
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
}
