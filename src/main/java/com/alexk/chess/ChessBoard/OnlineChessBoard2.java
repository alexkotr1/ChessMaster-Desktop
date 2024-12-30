//package com.alexk.chess.ChessBoard;
//
//import com.alexk.chess.Message;
//import com.alexk.chess.Pionia.Pioni;
//import com.alexk.chess.RequestCodes;
//import com.alexk.chess.Utilities;
//import com.alexk.chess.WebSocket;
//
//import java.util.ArrayList;
//import java.util.concurrent.CompletableFuture;
//
//public class OnlineChessBoard extends ChessBoard {
//    private WebSocket socket;
//    public OnlineChessBoard(){};
//    public OnlineChessBoard(WebSocket socket) {
//        this.socket = socket;
//    }
//    public boolean isDangerousPosition(char xOrig, int yOrig, boolean white) {
//        CompletableFuture<Boolean> future = new CompletableFuture<>();
//        Message message = new Message();
//        message.setData(new int[]{ Utilities.char2Int(xOrig), yOrig, white ? 1 : 0 });
//        message.setCode(RequestCodes.CHECK_DANGEROUS_POSITION);
//        message.onReply(m -> future.complete(Boolean.parseBoolean(m.getData())));
//        message.send(socket);
//        try {
//            return future.get();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//
//    public Pioni getPioniAt(char xPos, int yPos) {
//        Message message = new Message();
//        message.setCode(RequestCodes.GET_PIONI_AT_POS);
//        message.setData(new int[]{ Utilities.char2Int(xPos), yPos });
//        message.send(socket);
//        return null;
//    }
//
//    public ArrayList<Pioni> getPionia() {
//        CompletableFuture<ArrayList<Pioni>> future = new CompletableFuture<>();
//        Message message = new Message();
//        message.setCode(RequestCodes.GET_PIONIA);
//        message.send(socket);
//        System.out.println("Waiting reply for ID: " + message.getMessageID());
//        message.onReply(m -> {
//            try {
//                ArrayList<Pioni> pionia = Message.mapper.readValue(m.getData(), Message.mapper.getTypeFactory().constructCollectionType(ArrayList.class, Pioni.class));
//                future.complete(pionia);
//            } catch (Exception e) {
//                e.printStackTrace();
//                future.completeExceptionally(e);
//            }
//        });
//
//        try {
//            return future.get();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//
//    public Boolean getWhiteTurn() {
//        CompletableFuture<Boolean> future = new CompletableFuture<>();
//        Message message = new Message();
//        message.setCode(RequestCodes.GET_WHITE_TURN);
//        message.onReply(m -> future.complete(Boolean.parseBoolean(m.getData())));
//        message.send(socket);
//        try {
//            return future.get();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public int getMovesRemaining() {
//        CompletableFuture<Integer> future = new CompletableFuture<>();
//        Message message = new Message();
//        message.setCode(RequestCodes.GET_MOVES_REMAINING);
//        message.onReply(m -> future.complete(Integer.parseInt(m.getData())));
//        message.send(socket);
//        try {
//            return future.get();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return 0;
//        }
//    }
//
//    public void placePioniAt(Pioni p, char xPos, int yPos) {}
//    public void move(char xOrig, int yOrig, char xDest, int yDest) {}
//    public void loadBoard() {}
//    public void capture(Pioni p) {}
//    public void setWhiteTurn(boolean whiteTurn) {}
//    public void setMovesRemaining(int movesRemaining) {}
//}
