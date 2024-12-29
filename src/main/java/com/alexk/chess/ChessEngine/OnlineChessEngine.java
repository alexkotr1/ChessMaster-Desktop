package com.alexk.chess.ChessEngine;

import com.alexk.chess.ChessBoard.OnlineChessBoard;
import com.alexk.chess.Message;
import com.alexk.chess.Pionia.Pioni;
import com.alexk.chess.Utilities;
import com.alexk.chess.WebSocket;

import java.util.ArrayList;
import java.util.HashMap;

public class OnlineChessEngine extends ChessEngine {
    private WebSocket socket;
    public OnlineChessBoard chessBoard;
    public OnlineChessEngine(WebSocket socket) {
        this.socket = socket;
        chessBoard = new OnlineChessBoard(socket);
    }

    public void playChess() {

    }

    public ArrayList<Pioni> nextMove(char xOrig, int yOrig, char xDest, int yDest) {
        int[][] move = new int[2][2];
        move[0][0] = Utilities.char2Int(xOrig);
        move[0][1] = yOrig;
        move[1][0] = Utilities.char2Int(xDest);
        move[1][1] = yDest;
        Message msg = new Message();
        msg.setData(move);
        msg.send(socket);

        return null;
    }

    public Pioni upgradePioni(Pioni p, String type) {
        return null;
    }

    public HashMap<Pioni, ArrayList<int[]>> kingCheckMate(boolean white) {
        return null;
    }

    public boolean stalemateCheck(boolean white) {
        return false;
    }

    public boolean checkDumbMove(Pioni p, int[] dest) {
        return false;
    }

    public void setGameEnded(Boolean gameEnded, Winner winner) {

    }

    public Boolean getGameEnded() {
        return null;
    }

    public Winner getWinner() {
        return null;
    }


    public OnlineChessBoard getBoard() {
        return chessBoard;
    }
}
