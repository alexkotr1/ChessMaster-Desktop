package com.alexk.chess;

import com.alexk.chess.Pionia.*;

import java.util.ArrayList;

public class ChessBoard {
    private final ArrayList<Pioni> Pionia = new ArrayList<>();
    private boolean whiteTurn = true;
    private int movesRemaining = 100;
    private long whiteTimeRemaining = 0;
    private long blackTimeRemaining = 0;

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
    public static ChessBoard fromFEN(String fen) {
        ChessBoard board = new ChessBoard();
        board.Pionia.clear();

        String[] parts = fen.trim().split("\\s+");
        if (parts.length < 1) {
            throw new IllegalArgumentException("Invalid FEN: empty string");
        }

        String boardPart = parts[0];
        String[] ranks = boardPart.split("/");
        if (ranks.length != 8) {
            throw new IllegalArgumentException("Invalid FEN: must have 8 ranks");
        }

        for (int rankIndex = 0; rankIndex < 8; rankIndex++) {
            String rankStr = ranks[rankIndex];
            int y = 8 - rankIndex;
            int file = 1;

            for (int i = 0; i < rankStr.length(); i++) {
                char c = rankStr.charAt(i);

                if (Character.isDigit(c)) {
                    file += c - '0';
                } else {
                    if (file < 1 || file > 8) {
                        throw new IllegalArgumentException("Invalid FEN rank: " + rankStr);
                    }

                    boolean isWhite = Character.isUpperCase(c);
                    char pieceChar = Character.toLowerCase(c);
                    char xPos = Utilities.int2Char(file);

                    switch (pieceChar) {
                        case 'p' -> board.Pionia.add(new Stratiotis(isWhite, board, xPos, y));
                        case 'r' -> board.Pionia.add(new Pyrgos(isWhite, board, xPos, y));
                        case 'n' -> board.Pionia.add(new Alogo(isWhite, board, xPos, y));
                        case 'b' -> board.Pionia.add(new Stratigos(isWhite, board, xPos, y));
                        case 'q' -> board.Pionia.add(new Vasilissa(isWhite, board, xPos, y));
                        case 'k' -> board.Pionia.add(new Vasilias(isWhite, board, xPos, y));
                        default ->
                                throw new IllegalArgumentException("Invalid FEN piece char: " + c);
                    }

                    file++;
                }
            }

            if (file != 9) {
                throw new IllegalArgumentException("Invalid FEN rank (must cover 8 files): " + rankStr);
            }
        }

        if (parts.length > 1) {
            board.whiteTurn = parts[1].equalsIgnoreCase("w");
        } else {
            board.whiteTurn = true; // default if missing
        }

        board.movesRemaining = 100;
        if (parts.length > 4) {
            try {
                int halfmoveClock = Integer.parseInt(parts[4]);
                board.movesRemaining = Math.max(0, 100 - halfmoveClock);
            } catch (NumberFormatException ignored) {
            }
        }

        if (parts.length > 2) {
            String castling = parts[2];

            for (Pioni p : board.Pionia) {
                if (p instanceof Vasilias vasilias) {
                    vasilias.setMoved(true);
                } else if (p instanceof Pyrgos pyrgos) {
                    pyrgos.setMoved(true);
                }
            }

            if (!castling.equals("-")) {
                for (char c : castling.toCharArray()) {
                    switch (c) {
                        case 'K' -> {
                            Pioni k = board.getPioniAt('E', 1);
                            if (k instanceof Vasilias v) v.setMoved(false);
                            Pioni r = board.getPioniAt('H', 1);
                            if (r instanceof Pyrgos pr) pr.setMoved(false);
                        }
                        case 'Q' -> {
                            Pioni k = board.getPioniAt('E', 1);
                            if (k instanceof Vasilias v) v.setMoved(false);
                            Pioni r = board.getPioniAt('A', 1);
                            if (r instanceof Pyrgos pr) pr.setMoved(false);
                        }
                        case 'k' -> {
                            Pioni k = board.getPioniAt('E', 8);
                            if (k instanceof Vasilias v) v.setMoved(false);
                            Pioni r = board.getPioniAt('H', 8);
                            if (r instanceof Pyrgos pr) pr.setMoved(false);
                        }
                        case 'q' -> {
                            Pioni k = board.getPioniAt('E', 8);
                            if (k instanceof Vasilias v) v.setMoved(false);
                            Pioni r = board.getPioniAt('A', 8);
                            if (r instanceof Pyrgos pr) pr.setMoved(false);
                        }
                    }
                }
            }
        }

        return board;
    }

    public Pioni getPioniAt(char xPos, int yPos){
        return Pionia.stream().filter(pioni -> Utilities.int2Char(pioni.getPosition()[0]) == xPos && pioni.getPosition()[1] == yPos && !pioni.getCaptured()).findFirst().orElse(null);
    }

    public ArrayList<Pioni> getPionia(){
        return Pionia;
    }
    public void move(char xOrig, int yOrig, char xDest,int yDest){
        movesRemaining--;
        Pioni p = getPioniAt(xOrig,yOrig);
        Pioni pioniAtDestination = getPioniAt(xDest,yDest);
        if (pioniAtDestination != null && p.getIsWhite() != pioniAtDestination.getIsWhite()) {
            capture(pioniAtDestination);
            movesRemaining = 100;
        }
        if (p.type.equals("Stratiotis")) movesRemaining = 100;
        placePioniAt(p,xDest,yDest);
        if (p.type.equals("Pyrgos")) ((Pyrgos) p).setMoved(true);
        else if (p.type.equals("Vasilias")) ((Vasilias) p).setMoved(true);
    }
    public Boolean getWhiteTurn(){
        return whiteTurn;
    }
    public void setWhiteTurn(boolean whiteTurn){ this.whiteTurn = whiteTurn; }
    public void setMovesRemaining(int movesRemaining){ this.movesRemaining = movesRemaining; }
    public int getMovesRemaining(){ return movesRemaining; }
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
    public void setWhiteTimeRemaining(long whiteTimeRemaining){ this.whiteTimeRemaining = whiteTimeRemaining; }
    public long getWhiteTimeRemaining(){ return whiteTimeRemaining; }
    public void setBlackTimeRemaining(long blackTimeRemaining){
        this.blackTimeRemaining = blackTimeRemaining;
    }
    public long getBlackTimeRemaining(){ return blackTimeRemaining; }

    public boolean castlingRights(boolean white, boolean kingSide){
        Vasilias king = null;
        Pyrgos rook = null;
        for (Pioni p : getPionia()){
            if (p.getIsWhite() == white){
                if (p.getType().equals("Vasilias")) king = (Vasilias) p;
                else if (p.getType().equals("Pyrgos")){
                    if (((Pyrgos) p).getKingSide() == kingSide) rook = (Pyrgos) p;
                }
            }
        }
        return king != null && rook != null && !king.getMoved() && !rook.getMoved();
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
