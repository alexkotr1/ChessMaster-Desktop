package com.alexk.chess;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.OnMessage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message implements Serializable {
    public static ObjectMapper mapper = new ObjectMapper();
    public String messageID;
    private RequestCodes code;
    private String data;
    private Consumer<Message> replyCallback;
    public static final HashMap<String, Message> pending = new HashMap<>();
    public Message(){
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
            System.err.println(e.getMessage());
        }
    }

    public void setData(int[][] data) {
        try {
            this.data = mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            System.err.println(e.getMessage());
        }
    }

    public boolean send(WebSocket socket) {
        try {
            String json = mapper.writeValueAsString(this);
            socket.sendMessage(json);
        } catch (JsonProcessingException e) {
            return false;
        }
        return true;
    }

    public static Message parse(String res) {
        try {
            return mapper.readValue(res, Message.class);
        } catch (JsonProcessingException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public void onReply(Consumer<Message> callback) {
        this.replyCallback = callback;
    }

    public void triggerReplyCallback(Message response) {
        if (replyCallback != null) {
            replyCallback.accept(response);
        }
    }

}
