package com.alexk.chess.ChessBoard;

import com.alexk.chess.Message;
import com.alexk.chess.Pionia.Pioni;
import com.alexk.chess.RequestCodes;
import com.alexk.chess.Utilities;
import com.alexk.chess.WebSocket;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class OnlineChessBoard extends ChessBoard {
    private WebSocket socket;
    public OnlineChessBoard(WebSocket socket) {
        this.socket = socket;
    }
    public boolean isDangerousPosition(char xOrig, int yOrig, boolean white) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Message message = new Message();
        message.setData(new int[]{ Utilities.char2Int(xOrig), yOrig, white ? 1 : 0 });
        message.onReply(m -> {
            boolean isDangerous = Boolean.parseBoolean(m.getData());
            future.complete(isDangerous);
        });
        message.send(socket);
        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public Pioni getPioniAt(char xPos, int yPos) {
        Message message = new Message();
        message.setCode(RequestCodes.GET_PIONI_AT_POS);
        message.setData(new int[]{ Utilities.char2Int(xPos), yPos });
        message.send(socket);
        return null;
    }

    public ArrayList<Pioni> getPionia() {
        CompletableFuture<ArrayList<Pioni>> future = new CompletableFuture<>();
        Message message = new Message();
        message.setCode(RequestCodes.GET_PIONIA);

        message.send(socket);
        System.out.println(1);
        System.out.println("Waiting reply for ID: " + message.getMessageID());
        message.onReply(m -> {
            System.out.println(2);
            try {
                ArrayList<Pioni> pionia = Message.mapper.readValue(m.getData(), Message.mapper.getTypeFactory().constructCollectionType(ArrayList.class, Pioni.class));
                future.complete(pionia);
            } catch (Exception e) {
                e.printStackTrace();
                future.completeExceptionally(e);
            }
        });

        System.out.println(3);
        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public Boolean getWhiteTurn() {
        return null;
    }

    public int getMovesRemaining() {
        return 0;
    }

    public void placePioniAt(Pioni p, char xPos, int yPos) {}
    public void move(char xOrig, int yOrig, char xDest, int yDest) {}
    public void loadBoard() {}
    public void capture(Pioni p) {}
    public void setWhiteTurn(boolean whiteTurn) {}
    public void setMovesRemaining(int movesRemaining) {}
}
