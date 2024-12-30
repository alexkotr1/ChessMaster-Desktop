package com.alexk.chess;

import com.alexk.chess.Serializers.OnlineChessBoardKeySerializer;
import com.alexk.chess.ChessBoard.OnlineChessBoard;
import com.alexk.chess.Serializers.OnlineChessBoardKeyDeserializer;
import com.alexk.chess.Pionia.Pioni;
import com.alexk.chess.Serializers.PioniKeyDeserializer;
import com.alexk.chess.Serializers.PioniKeySerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

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
        module.addKeyDeserializer(OnlineChessBoardKeyDeserializer.class, new OnlineChessBoardKeyDeserializer());
        mapper.registerModule(module);
    }

    public String messageID;
    private RequestCodes code;
    private String data;
    private String pioni; // Optional field, can be null
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

    public String getPioni() {
        return pioni;
    }

    public void setPioni(Pioni pioni) {
        if (pioni == null) {
            this.pioni = null; // Explicitly set null if no pioni is provided
            return;
        }
        try {
            this.pioni = mapper.writeValueAsString(pioni);
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing pioni: " + e.getMessage());
        }
    }

    public Pioni getPioniAsObject() {
        if (pioni == null || pioni.isEmpty()) {
            return null; // Return null if pioni is not set or empty
        }
        try {
            return mapper.readValue(pioni, Pioni.class);
        } catch (JsonProcessingException e) {
            System.err.println("Error deserializing pioni: " + e.getMessage());
        }
        return null;
    }

    public void send(WebSocket socket) {
        try {
            String json = mapper.writeValueAsString(this);
            socket.sendMessage(json);
        } catch (JsonProcessingException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    public static Message parse(String res) {
        try {
            return mapper.readValue(res, Message.class);
        } catch (JsonProcessingException e) {
            System.err.println("Error parsing message: " + e.getMessage());
        }
        return null;
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
