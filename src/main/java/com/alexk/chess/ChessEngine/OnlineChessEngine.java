package com.alexk.chess.ChessEngine;

import com.alexk.chess.ChessBoard.OnlineChessBoard;
import com.alexk.chess.Message;
import com.alexk.chess.Pionia.Pioni;
import com.alexk.chess.RequestCodes;
import com.alexk.chess.Utilities;
import com.alexk.chess.WebSocket;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import org.glassfish.grizzly.http.server.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class OnlineChessEngine extends ChessEngine {
    private WebSocket socket;
    public OnlineChessBoard chessBoard;
    public OnlineChessEngine(WebSocket socket) {
        this.socket = socket;
        refreshBoard(()->{});
    }




    public ArrayList<Pioni> nextMove(char xOrig, int yOrig, char xDest, int yDest) {
        int[][] move = new int[2][2];
        move[0][0] = Utilities.char2Int(xOrig);
        move[0][1] = yOrig;
        move[1][0] = Utilities.char2Int(xDest);
        move[1][1] = yDest;

        Message msg = new Message();
        msg.setCode(RequestCodes.REQUEST_MOVE);
        msg.setData(move);
        CompletableFuture<ArrayList<Pioni>> moveFuture = new CompletableFuture<>();
        msg.onReply(m -> {
            try {
                JavaType type = Message.mapper.getTypeFactory().constructCollectionType(ArrayList.class, Pioni.class);
                ArrayList<Pioni> updatedPionia = Message.mapper.readValue(m.getData(), type);
                refreshBoard(() -> moveFuture.complete(updatedPionia));
            } catch (Exception e) {
                moveFuture.completeExceptionally(e);
            }
        });
        msg.send(socket);
        try {
            return moveFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void refreshBoard(Runnable callback) {
        Message message = new Message();
        message.setCode(RequestCodes.REQUEST_CHESSBOARD);
        message.onReply(m -> {
            try {
                OnlineChessBoard board = Message.mapper.readValue(m.getData(), OnlineChessBoard.class);
                if (chessBoard == null) chessBoard = new OnlineChessBoard();
                if (!board.getPionia().isEmpty()) mergePionia(chessBoard.getPionia(), board.getPionia());
                else this.chessBoard.setPionia(board.getPionia());
                this.chessBoard.setWhiteTurn(board.getWhiteTurn());
                this.chessBoard.setMovesRemaining(board.getMovesRemaining());
                this.chessBoard.setGameEnded(board.getGameEnded());
                this.chessBoard.setWinner(board.getWinner());
                chessBoard.printBoard();
                if (callback != null) callback.run();
            } catch (JsonProcessingException e) {
                System.err.println("Error deserializing chessboard: " + e.getMessage());
            }
        });

        message.send(socket);
    }
    private void mergePionia(ArrayList<Pioni> currentPionia, ArrayList<Pioni> refreshedPionia) {
        HashMap<String, Pioni> currentMap = currentPionia.stream()
                .collect(Collectors.toMap(Pioni::getID, p -> p, (p1, p2) -> p1, HashMap::new));
        for (Pioni refreshed : refreshedPionia) {
            Pioni existing = currentMap.get(refreshed.getID());
            if (existing != null) {
                existing.setPosition(refreshed.getXPos(), refreshed.getYPos());
                existing.setCaptured(refreshed.getCaptured());
            } else {
                currentPionia.add(refreshed);
            }
        }
        currentPionia.removeIf(p -> refreshedPionia.stream().noneMatch(r -> r.getID().equals(p.getID())));
    }
    public Pioni upgradePioni(Pioni p, String type) {
        CompletableFuture<Pioni> future = new CompletableFuture<>();
        Message message = new Message();
        message.setCode(RequestCodes.REQUEST_UPGRADE);
        message.setPioni(p);
        message.setData(type);
        message.onReply(m -> future.complete(m.getPioni()));
        message.send(socket);
        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public HashMap<Pioni, ArrayList<int[]>> kingCheckMate(boolean white) {
        CompletableFuture<HashMap<Pioni, ArrayList<int[]>>> future = new CompletableFuture<>();
        Message message = new Message();
        message.setCode(RequestCodes.CHECKMATE);
        message.setData(white);
        message.onReply(m -> {
            try {
                future.complete(Message.mapper.readValue(m.getData(), Message.mapper.getTypeFactory()
                        .constructMapType(HashMap.class, Message.mapper.getTypeFactory().constructType(Pioni.class), Message.mapper.getTypeFactory()
                                .constructCollectionType(ArrayList.class, int[].class))));
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        message.send(socket);
        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean stalemateCheck(boolean white) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Message message = new Message();
        message.setCode(RequestCodes.STALEMATE_CHECK);
        message.onReply(m -> future.complete(Boolean.parseBoolean(m.getData())));
        message.send(socket);
        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkDumbMove(Pioni p, int[] dest) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Message message = new Message();
        message.setCode(RequestCodes.DUMB_MOVE_CHECK);
        message.setPioni(p);
        message.setData(dest);
        message.onReply(m -> future.complete(Boolean.parseBoolean(m.getData())));
        message.send(socket);
        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Winner getWinner() {
        CompletableFuture<Winner> future = new CompletableFuture<>();
        Message message = new Message();
        message.setCode(RequestCodes.GET_GAME_WINNER);
        message.onReply(m -> future.complete(Winner.valueOf(m.getData())));
        message.send(socket);
        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return Winner.Draw;
        }
    }


    public Boolean getGameEnded() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Message message = new Message();
        message.setCode(RequestCodes.IS_GAME_ENDED);
        message.onReply(m -> future.complete(Boolean.parseBoolean(m.getData())));
        message.send(socket);
        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public OnlineChessBoard getBoard() {
        CompletableFuture<OnlineChessBoard> future = new CompletableFuture<>();
        if (chessBoard == null) {
            refreshBoard(()->{
                future.complete(chessBoard);
            });
        } else return chessBoard;
        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void playChess() {}
    public void setGameEnded(Boolean gameEnded, Winner winner) {}

}
