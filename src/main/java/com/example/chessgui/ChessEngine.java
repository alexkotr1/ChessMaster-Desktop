package com.example.chessgui;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class ChessEngine {
    protected ChessBoard chessBoard = new ChessBoard();
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
    public boolean nextMove(char xOrig, int yOrig, char xDest, int yDest){
        Pioni p = chessBoard.getPioniAt(xOrig,yOrig);
        if (p == null) {
            System.out.println("There is no pioni at " + xOrig + " at " + yOrig);
            return false;
        }
        if (p.getIsWhite() != chessBoard.getWhiteTurn()) {
            System.out.println("It's not " + (chessBoard.getWhiteTurn() ? "black" : "white" + "'s turn"));
            return false;
        }
        if (!p.isLegalMove(xDest,yDest)) {
            System.out.println("Illegal move!");
            return false;
        }

        chessBoard.move(xOrig,yOrig,xDest,yDest);
        //chessBoard.printBoard();
        return true;
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
    public boolean kingCheckMate(boolean white, HashMap<Pioni,ArrayList<int[]>> legalMovesWhenKingThreatened) {
        ArrayList<Pioni> duplicatePieces = chessBoard.getPionia().stream().filter(pioni -> pioni.getIsWhite() == white && !pioni.getCaptured()).collect(Collectors.toCollection(ArrayList::new));
        for (Pioni p : duplicatePieces) {
            for (int[] pos : allPositions) {
                ChessBoard testChessBoard = chessBoard.clone();
                Pioni duplicatePioni = testChessBoard.getPioniAt(p.getXPos(), p.getYPos());
                if (duplicatePioni.isLegalMove(Utilities.int2Char(pos[0]), pos[1])) {
                    testChessBoard.move(p.getXPos(), p.getYPos(), Utilities.int2Char(pos[0]), pos[1]);
                    if (legalMovesWhenKingThreatened == null && !ChessEngine.checkKingMat(testChessBoard, white)) return false;
                    else if (!ChessEngine.checkKingMat(testChessBoard, white)) {
                        Pioni origPioni = chessBoard.getPioniAt(p.getXPos(), p.getYPos());
                        ArrayList<int[]> existingRoutes = legalMovesWhenKingThreatened.get(origPioni);
                        if (existingRoutes == null) existingRoutes = new ArrayList<>();
                        existingRoutes.add(new int[]{ pos[0],pos[1] });
                        legalMovesWhenKingThreatened.put(origPioni, existingRoutes);
                    }
                }
            }
        }
        return true;
    }
}
