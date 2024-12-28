package com.alexk.chess;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
public class ChessEngine implements Serializable {
    public enum Winner { White, Black, Draw };
    protected final ChessBoard chessBoard = new ChessBoard();
    private Boolean gameEnded = false;
    private Winner winner = null;
    ArrayList<int[]> allPositions = new ArrayList<>();
    public ChessEngine() {
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
        if (p == null || p.getIsWhite() != chessBoard.getWhiteTurn() || !p.isLegalMove(xDest,yDest) || getGameEnded()) return null;
        ArrayList<Pioni> moved = new ArrayList<>();
        moved.add(p);
        Pioni pioniAtDest = chessBoard.getPioniAt(xDest,yDest);
        HashMap<Pioni, ArrayList<int[]>> legalMovesWhenKingThreatened = kingCheckMate(p.isWhite);
        if (legalMovesWhenKingThreatened != null && !legalMovesWhenKingThreatened.isEmpty()) {
            ArrayList<int[]> desiredMoves = legalMovesWhenKingThreatened.get(p);
            if (desiredMoves != null && desiredMoves.stream().noneMatch(arr -> arr[0] == Utilities.char2Int(xDest) && arr[1] == yDest)) {
                return null;
            } else legalMovesWhenKingThreatened.clear();
        }
        if (checkDumbMove(p, new int[]{Utilities.char2Int(xDest), yDest})) return null;
        if (p.type.equals("Vasilias") && pioniAtDest != null && pioniAtDest.type.equals("Pyrgos") && p.getIsWhite() == pioniAtDest.getIsWhite()){
            moved.add(pioniAtDest);
            int[] dest = pioniAtDest.getPosition();
            int[] orig = p.getPosition();
            chessBoard.move(xOrig,yOrig,Utilities.int2Char(dest[0] > orig[0] ? orig[0] + 2 : orig[0] - 2),yOrig);
            chessBoard.move(Utilities.int2Char(dest[0]),dest[1],Utilities.int2Char(dest[0] > orig[0] ? orig[0] - 1 : orig[0] + 1),yOrig);
            return null;
        }
        chessBoard.move(xOrig,yOrig,xDest,yDest);
        if (pioniAtDest != null && p.getIsWhite() != pioniAtDest.getIsWhite()) {
            chessBoard.capture(pioniAtDest);
            chessBoard.setMovesRemaining(100);
            moved.add(pioniAtDest);
        }
        if (ChessEngine.checkKingMat(chessBoard, !p.getIsWhite())) {
            HashMap<Pioni, ArrayList<int[]>> legalMovesWhenEnemyKingThreatened = kingCheckMate(!p.getIsWhite());
            if (legalMovesWhenEnemyKingThreatened == null || legalMovesWhenEnemyKingThreatened.isEmpty()) setGameEnded(true,p.getIsWhite() ? Winner.White : Winner.Black);
        } else if (stalemateCheck(!p.getIsWhite()) || chessBoard.getMovesRemaining() == 0) setGameEnded(true, Winner.Draw);
        if (!moved.isEmpty()) chessBoard.setWhiteTurn(!chessBoard.getWhiteTurn());
        return moved;
    }
    public Pioni upgradePioni(Pioni p,String type){
        if (p.type.equals("Stratiotis") && ((p.getIsWhite() && p.getYPos() == 8) || (!p.getIsWhite() && p.getYPos() == 1))) {
            Pioni upgradedPioni;
            switch (type) {
                case "Alogo":
                    upgradedPioni = new Alogo(p.isWhite, chessBoard, p.getXPos(), p.getYPos());
                    break;
                case "Pyrgos":
                    upgradedPioni = new Pyrgos(p.isWhite, chessBoard, p.getXPos(), p.getYPos());
                    break;
                case "Stratigos":
                    upgradedPioni = new Stratigos(p.isWhite, chessBoard, p.getXPos(), p.getYPos());
                    break;
                case "Vasilissa":
                    upgradedPioni = new Vasilissa(p.isWhite, chessBoard, p.getXPos(), p.getYPos());
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
                .filter(p -> p.getIsWhite() == white && p.type.equals("Vasilias"))
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
                if (!ChessEngine.checkKingMat(testChessBoard, white)) {
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
        return ChessEngine.checkKingMat(testChessBoard,p.isWhite);
    }
    public void setGameEnded(Boolean gameEnded, Winner winner) {
        this.gameEnded = gameEnded;
        this.winner = winner;
    }
    public Boolean getGameEnded() { return gameEnded;}
    public Winner getWinner() { return winner;}
}
