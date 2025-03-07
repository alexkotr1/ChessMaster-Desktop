package com.alexk.chess.ChessEngine;

import com.alexk.chess.ChessBoard.ChessBoard;
import com.alexk.chess.ChessBoard.LocalChessBoard;
import com.alexk.chess.Pionia.*;
import com.alexk.chess.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
public class LocalChessEngine extends ChessEngine {
    protected final LocalChessBoard chessBoard = new LocalChessBoard();
    ArrayList<int[]> allPositions = new ArrayList<>();
    private int totalMoves = 1;
    public LocalChessEngine() {
        for (int x = 1;x<=8;x++) {
            for (int y = 1; y <= 8; y++) {
                allPositions.add(new int[]{x, y});
            }
        }
    }
    public void playChess(){
        chessBoard.loadBoard();
        chessBoard.printBoard();
    }

    public ArrayList<Pioni> nextMove(char xOrig, int yOrig, char xDest, int yDest){
        Pioni p = chessBoard.getPioniAt(xOrig,yOrig);
        if (p == null || p.getIsWhite() != chessBoard.getWhiteTurn() || !p.isLegalMove(xDest,yDest) || getBoard().getGameEnded()) return null;
        ArrayList<Pioni> moved = new ArrayList<>();
        moved.add(p);
        Pioni pioniAtDest = chessBoard.getPioniAt(xDest,yDest);
        //1
        HashMap<Pioni, ArrayList<int[]>> legalMovesWhenKingThreatened = kingCheckMate(p.getIsWhite());
        if (legalMovesWhenKingThreatened != null && !legalMovesWhenKingThreatened.isEmpty()) {
            ArrayList<int[]> desiredMoves = legalMovesWhenKingThreatened.get(p);
            if (desiredMoves != null && desiredMoves.stream().noneMatch(arr -> arr[0] == Utilities.char2Int(xDest) && arr[1] == yDest)) {
                return null;
            } else legalMovesWhenKingThreatened.clear();
        }
        //2
        if (checkDumbMove(p, new int[]{Utilities.char2Int(xDest), yDest})) return null;

        if (p.getType().equals("Vasilias") && pioniAtDest != null && pioniAtDest.getType().equals("Pyrgos") && p.getIsWhite() == pioniAtDest.getIsWhite()){
            moved.add(pioniAtDest);
            int[] dest = pioniAtDest.getPosition();
            int[] orig = p.getPosition();
            chessBoard.setWhiteTurn(!chessBoard.getWhiteTurn());
            chessBoard.move(xOrig,yOrig,Utilities.int2Char(dest[0] > orig[0] ? orig[0] + 2 : orig[0] - 2),yOrig);
            chessBoard.move(Utilities.int2Char(dest[0]),dest[1],Utilities.int2Char(dest[0] > orig[0] ? orig[0] - 1 : orig[0] + 1),yOrig);
            checkGameEnd(p.getIsWhite());
            return moved;
        }

        chessBoard.move(xOrig,yOrig,xDest,yDest);
        if (pioniAtDest != null && p.getIsWhite() != pioniAtDest.getIsWhite()) {
            chessBoard.capture(pioniAtDest);
            chessBoard.setMovesRemaining(100);
            moved.add(pioniAtDest);
        }
        if (!moved.isEmpty()) chessBoard.setWhiteTurn(!chessBoard.getWhiteTurn());
        checkGameEnd(p.getIsWhite());
        return moved;
    }

    public Pioni upgradePioni(Pioni p,String type){
        if (p.getType().equals("Stratiotis") && ((p.getIsWhite() && p.getYPos() == 8) || (!p.getIsWhite() && p.getYPos() == 1))) {
            Pioni upgradedPioni;
            switch (type) {
                case "Alogo":
                    upgradedPioni = new Alogo(p.getIsWhite(), chessBoard, p.getXPos(), p.getYPos(),null, false,null,null);
                    break;
                case "Pyrgos":
                    upgradedPioni = new Pyrgos(p.getIsWhite(), chessBoard, p.getXPos(), p.getYPos(),null, false,true,false);
                    break;
                case "Stratigos":
                    upgradedPioni = new Stratigos(p.getIsWhite(), chessBoard, p.getXPos(), p.getYPos(),null, false,null,null);
                    break;
                case "Vasilissa":
                    upgradedPioni = new Vasilissa(p.getIsWhite(), chessBoard, p.getXPos(), p.getYPos(),null, false,null,null);
                    break;
                default:
                    return null;
            }
            chessBoard.getPionia().remove(p);
            chessBoard.getPionia().add(upgradedPioni);
            return upgradedPioni;
        }
        return null;
    }

    public static boolean checkKingMat(ChessBoard chessBoard, boolean white) {
        Pioni allyKing = chessBoard.getPionia()
                .stream()
                .filter(p -> p.getIsWhite() == white && p.getType().equals("Vasilias"))
                .findFirst()
                .orElse(null);

        if (allyKing == null) {
            return false;
        }

        for (Pioni p : chessBoard.getPionia().stream().filter(pioni -> !pioni.getCaptured()).collect(Collectors.toCollection(ArrayList::new))) {
            if (p.getIsWhite() != white && p.isLegalMove(allyKing.getXPos(), allyKing.getYPos())) {
                return true;
            }
        }
        return false;
    }
    public HashMap<Pioni,ArrayList<int[]>> kingCheckMate(boolean white) {
        HashMap<Pioni,ArrayList<int[]>> legalMovesWhenKingThreatened = new HashMap<>();
        ArrayList<Pioni> duplicatePieces = chessBoard.getPionia().stream().filter(pioni -> pioni.getIsWhite() == white && !pioni.getCaptured()).collect(Collectors.toCollection(ArrayList::new));

        for (Pioni p : duplicatePieces) {
            for (int[] pos : allPositions) {
                ChessBoard testChessBoard = chessBoard.clone();
                Pioni duplicatePioni = testChessBoard.getPioniAt(p.getXPos(), p.getYPos());
                if (!duplicatePioni.isLegalMove(Utilities.int2Char(pos[0]), pos[1])) continue;
                testChessBoard.move(p.getXPos(), p.getYPos(), Utilities.int2Char(pos[0]), pos[1]);
                if (!checkKingMat(testChessBoard, white)) {
                    Pioni origPioni = chessBoard.getPioniAt(p.getXPos(), p.getYPos());
                    ArrayList<int[]> existingRoutes = legalMovesWhenKingThreatened.get(origPioni);
                    if (existingRoutes == null) existingRoutes = new ArrayList<>();
                    existingRoutes.add(new int[]{ pos[0],pos[1] });
                    legalMovesWhenKingThreatened.put(origPioni, existingRoutes);
                }
            }
        }
        return legalMovesWhenKingThreatened;
    }
    public void checkGameEnd(boolean white){
        if (checkKingMat(chessBoard, !white)) {
            HashMap<Pioni, ArrayList<int[]>> legalMovesWhenEnemyKingThreatened = kingCheckMate(!white);
            if (legalMovesWhenEnemyKingThreatened == null || legalMovesWhenEnemyKingThreatened.isEmpty()) getBoard().setGameEndedWinner(true,white ? Winner.White : Winner.Black);
        }
        else if (stalemateCheck(!white) || chessBoard.getMovesRemaining() == 0) {
            getBoard().setGameEndedWinner(true, Winner.Draw);
        }
    }
    public boolean stalemateCheck(boolean white) {
        ArrayList<Pioni> duplicatePieces = chessBoard.getPionia().stream().filter(pioni -> pioni.getIsWhite() == white && !pioni.getCaptured()).collect(Collectors.toCollection(ArrayList::new));
        for (Pioni p : duplicatePieces) {
            for (int[] pos : allPositions) {
                if (p.isLegalMove(Utilities.int2Char(pos[0]), pos[1]) && !checkDumbMove(p,pos)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkDumbMove(Pioni p, int[] dest){
        ChessBoard testChessBoard = chessBoard.clone();
        testChessBoard.move(p.getXPos(), p.getYPos(), Utilities.int2Char(dest[0]), dest[1]);
        return checkKingMat(testChessBoard,p.getIsWhite());
    }
    public ChessBoard getBoard() { return chessBoard; }
    public void refreshBoard(Runnable callback) {}
    @Override
    public int getTotalMoves() {
        return totalMoves;
    }

    @Override
    public void setTotalMoves(int totalMoves) {
        this.totalMoves = totalMoves;
    }

}
