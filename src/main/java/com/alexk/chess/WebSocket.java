package com.alexk.chess;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.websocket.*;

import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WebSocket {

    private static Session session;
    private WebSocketMessageListener listener;

    public WebSocket(WebSocketMessageListener listener) {
        this.listener = listener;
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(this, URI.create("ws://localhost:8025/chat"));
        } catch (Exception e) {
            System.err.println("WebSocket initialization error: " + e.getMessage());
        }
    }

    public void setListener(WebSocketMessageListener listener) { this.listener = listener; }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to server.");
    }

    @OnMessage
    public void onMessage(String message) throws JsonProcessingException {
        Message res = Message.mapper.readValue(message, Message.class);
        if (listener != null) {
            listener.onMessageReceived(res);
        }
        if (Message.pending.containsKey(res.getMessageID())) {
            Message reply = Message.pending.get(res.getMessageID());
            reply.triggerReplyCallback(res);
            Message.pending.remove(reply.getMessageID());
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("Disconnected from server.");
    }

    public void sendMessage(String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
    public static void closeConnection(){
        try {
            session.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
