package com.alexk.chess;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.websocket.*;

import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WebSocket {

    private Session session;
    private WebSocketMessageListener listener;

    // Constructor with optional listener
    public WebSocket(WebSocketMessageListener listener) {
        this.listener = listener;
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(this, URI.create("ws://localhost:8025/chat"));
        } catch (Exception e) {
            System.err.println("WebSocket initialization error: " + e.getMessage());
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to server.");
    }

    @OnMessage
    public void onMessage(String message) throws JsonProcessingException {
        System.out.println("Received raw message: " + message);

        // Deserialize the message
        Message res = Message.mapper.readValue(message, Message.class);

        // Notify the listener, if available
        if (listener != null) {
            listener.onMessageReceived(res);
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
}
