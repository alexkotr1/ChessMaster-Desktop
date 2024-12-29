package com.alexk.chess;


import com.alexk.chess.ChessEngine.ChessEngine;
import com.alexk.chess.ChessEngine.LocalChessEngine;
import com.alexk.chess.ChessEngine.OnlineChessEngine;
import com.alexk.chess.Pionia.Pioni;

import java.util.ArrayList;
import java.util.HashMap;

public class Proxy{
    ChessEngine chessEngine;
    private final boolean offlineMode;
    private final ArrayList<int[]> allPositions = new ArrayList<>();
    public Proxy(boolean offlineMode, WebSocket socket){
        this.offlineMode = offlineMode;
        if (offlineMode) chessEngine = new LocalChessEngine();
        else chessEngine = new OnlineChessEngine(socket);
        chessEngine.playChess();
        for (int x = 1; x <= 8; x++) {
            for (int y = 1; y <= 8; y++) {
                allPositions.add(new int[]{x, y});
            }
        }
    }
    public boolean isOfflineMode() {
        return offlineMode;
    }
    public void setOfflineMode(boolean offlineMode) {}

    public ArrayList<int[]> onPawnDrag(Pioni p){
        ArrayList<int[]> possibleMoveIndicators = new ArrayList<>();
        if (chessEngine.getBoard().getWhiteTurn() != p.getIsWhite() || chessEngine.getGameEnded()) return null;
        HashMap<Pioni, ArrayList<int[]>> legalMovesWhenKingThreatened = chessEngine.kingCheckMate(p.getIsWhite());
        if (legalMovesWhenKingThreatened != null && !legalMovesWhenKingThreatened.isEmpty()) {
            if (legalMovesWhenKingThreatened.get(p) == null) return null;
            for (int[] dest : legalMovesWhenKingThreatened.get(p)) {
                possibleMoveIndicators.add(new int[]{ dest[0],dest[1] });
            }
        } else {
            for (int[] dest : allPositions) {
                char destX = Utilities.int2Char(dest[0]);
                int destY = dest[1];
                boolean res = p.isLegalMove(destX, destY);
                if (res && !chessEngine.checkDumbMove(p, new int[]{Utilities.char2Int(destX), destY}))
                    possibleMoveIndicators.add(new int[]{ destX,destY });
            }
        }
        return possibleMoveIndicators;
    }
    public ArrayList<Pioni> requestMove(Pioni p, int[] position) {
        if (offlineMode) return chessEngine.nextMove(p.getXPos(), p.getYPos(), Utilities.int2Char(position[0]), position[1]);
        chessEngine.nextMove(p.getXPos(), p.getYPos(), Utilities.int2Char(position[0]), position[1]);

        return null;
    }

}
