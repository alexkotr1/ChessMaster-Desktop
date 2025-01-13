package com.alexk.chess;

import com.alexk.chess.ChessBoard.OnlineChessBoard;
import com.alexk.chess.Pionia.Pioni;
import com.alexk.chess.Serializers.OnlineChessBoardKeyDeserializer;
import com.alexk.chess.Serializers.OnlineChessBoardKeySerializer;
import com.alexk.chess.Serializers.PioniKeyDeserializer;
import com.alexk.chess.Serializers.PioniKeySerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message implements Serializable {
    public static ObjectMapper mapper;
    static {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        SimpleModule module = new SimpleModule();
        module.addKeySerializer(Pioni.class, new PioniKeySerializer());
        module.addKeyDeserializer(Pioni.class, new PioniKeyDeserializer());
        module.addKeySerializer(OnlineChessBoard.class, new OnlineChessBoardKeySerializer());
        module.addKeyDeserializer(OnlineChessBoard.class, new OnlineChessBoardKeyDeserializer());
        mapper.registerModule(module);
    }

    public String messageID;
    private RequestCodes code;
    private String data;
    private Pioni pioni;
    private Consumer<Message> replyCallback;
    public static final Map<String, Message> pending = new ConcurrentHashMap<>();

    public Message() {
        messageID = UUID.randomUUID().toString();
        pending.put(messageID, this);
    }

    public RequestCodes getCode() {
        return code;
    }

    public void setCode(RequestCodes code) {
        this.code = code;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setData(Object data) {
        try {
            this.data = mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing data: " + e.getMessage());
        }
    }

    public Pioni getPioni() {
        return pioni;
    }

    public void setPioni(Pioni pioni) {
        this.pioni = pioni;
    }

    public void send(WebSocket socket) {
        try {
            String json = mapper.writeValueAsString(this);
            socket.sendMessage(json);
            System.out.println("Sending message: " + json);
        } catch (JsonProcessingException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
    public void send(WebSocket socket, Message replyTo) {
        try {
            if (replyTo != null) {
                this.messageID = replyTo.messageID;
            }
            String json = mapper.writeValueAsString(this);
            socket.sendMessage(json);
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    public void onReply(Consumer<Message> callback) {
        if (this.replyCallback != null) {
            System.err.println("Callback already set");
            return;
        }
        this.replyCallback = callback;
    }

    public void triggerReplyCallback(Message response) {
        if (replyCallback != null) {
            replyCallback.accept(response);
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "code=" + code +
                ", data='" + data + '\'' +
                ", messageID='" + messageID + '\'' +
                ", pioni='" + pioni + '\'' +
                '}';
    }
}
