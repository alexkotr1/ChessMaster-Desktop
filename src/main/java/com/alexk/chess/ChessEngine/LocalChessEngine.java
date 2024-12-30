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
        System.out.println(chessBoard.getPionia().size());
    }
    public ArrayList<Pioni> nextMove(char xOrig, int yOrig, char xDest, int yDest){
        Pioni p = chessBoard.getPioniAt(xOrig,yOrig);
        if (p == null || p.getIsWhite() != chessBoard.getWhiteTurn() || !p.isLegalMove(xDest,yDest) || getBoard().getGameEnded()) return null;
        ArrayList<Pioni> moved = new ArrayList<>();
        moved.add(p);
        Pioni pioniAtDest = chessBoard.getPioniAt(xDest,yDest);
        HashMap<Pioni, ArrayList<int[]>> legalMovesWhenKingThreatened = kingCheckMate(p.getIsWhite());
        if (legalMovesWhenKingThreatened != null && !legalMovesWhenKingThreatened.isEmpty()) {
            ArrayList<int[]> desiredMoves = legalMovesWhenKingThreatened.get(p);
            if (desiredMoves != null && desiredMoves.stream().noneMatch(arr -> arr[0] == Utilities.char2Int(xDest) && arr[1] == yDest)) {
                return null;
            } else legalMovesWhenKingThreatened.clear();
        }
        if (checkDumbMove(p, new int[]{Utilities.char2Int(xDest), yDest})) return null;
        if (p.getType().equals("Vasilias") && pioniAtDest != null && pioniAtDest.getType().equals("Pyrgos") && p.getIsWhite() == pioniAtDest.getIsWhite()){
            moved.add(pioniAtDest);
            int[] dest = pioniAtDest.getPosition();
            int[] orig = p.getPosition();
            chessBoard.move(xOrig,yOrig,Utilities.int2Char(dest[0] > orig[0] ? orig[0] + 2 : orig[0] - 2),yOrig);
            chessBoard.move(Utilities.int2Char(dest[0]),dest[1],Utilities.int2Char(dest[0] > orig[0] ? orig[0] - 1 : orig[0] + 1),yOrig);
            chessBoard.setWhiteTurn(!chessBoard.getWhiteTurn());
            return moved;
        }
        chessBoard.move(xOrig,yOrig,xDest,yDest);
        if (pioniAtDest != null && p.getIsWhite() != pioniAtDest.getIsWhite()) {
            chessBoard.capture(pioniAtDest);
            chessBoard.setMovesRemaining(100);
            moved.add(pioniAtDest);
        }
        if (checkKingMat(chessBoard, !p.getIsWhite())) {
            HashMap<Pioni, ArrayList<int[]>> legalMovesWhenEnemyKingThreatened = kingCheckMate(!p.getIsWhite());
            if (legalMovesWhenEnemyKingThreatened == null || legalMovesWhenEnemyKingThreatened.isEmpty()) getBoard().setGameEndedWinner(true,p.getIsWhite() ? Winner.White : Winner.Black);
        }
        else if (stalemateCheck(!p.getIsWhite()) || chessBoard.getMovesRemaining() == 0) getBoard().setGameEndedWinner(true, Winner.Draw);
        if (!moved.isEmpty()) chessBoard.setWhiteTurn(!chessBoard.getWhiteTurn());
        return moved;
    }
    public Pioni upgradePioni(Pioni p,String type){
        if (p.getType().equals("Stratiotis") && ((p.getIsWhite() && p.getYPos() == 8) || (!p.getIsWhite() && p.getYPos() == 1))) {
            Pioni upgradedPioni;
            switch (type) {
                case "Alogo":
                    upgradedPioni = new Alogo(p.getIsWhite(), chessBoard, p.getXPos(), p.getYPos(),null, false);
                    break;
                case "Pyrgos":
                    upgradedPioni = new Pyrgos(p.getIsWhite(), chessBoard, p.getXPos(), p.getYPos(),null, false);
                    break;
                case "Stratigos":
                    upgradedPioni = new Stratigos(p.getIsWhite(), chessBoard, p.getXPos(), p.getYPos(),null, false);
                    break;
                case "Vasilissa":
                    upgradedPioni = new Vasilissa(p.getIsWhite(), chessBoard, p.getXPos(), p.getYPos(),null, false);
                    break;
                default:
                    System.err.println("Something went wrong!");
                    return null;
            }
            chessBoard.getPionia().remove(p);
            chessBoard.getPionia().add(upgradedPioni);
            return upgradedPioni;
        }
        return null;
    }

    public static boolean checkKingMat(ChessBoard chessBoard, boolean white){
        Pioni allyKing = chessBoard.getPionia()
                .stream()
                .filter(p -> p.getIsWhite() == white && p.getType().equals("Vasilias"))
                .findFirst()
                .orElse(null);
        assert allyKing != null;
        for (Pioni p : chessBoard.getPionia().stream().filter(pioni -> !pioni.getCaptured()).collect(Collectors.toCollection(ArrayList::new))) {
            if (p.getIsWhite() != white && p.isLegalMove(allyKing.getXPos(), allyKing.getYPos())) return true;
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
    public ChessBoard getBoard() { return this.chessBoard; }
    public void refreshBoard(Runnable callback) {}

}
