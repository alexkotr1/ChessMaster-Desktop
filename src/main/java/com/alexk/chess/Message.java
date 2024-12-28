package com.alexk.chess;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {
    public static ObjectMapper mapper = new ObjectMapper();
    public enum RequestCodes { HOST_GAME, JOIN_GAME, HOST_GAME_RESULT, JOIN_GAME_RESULT }
    private RequestCodes code;
    private String data;

    public RequestCodes getCode() {
        return code;
    }

    public void setCode(RequestCodes code) {
        this.code = code;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
    public void send(WebSocket socket) throws JsonProcessingException {
        String json = mapper.writeValueAsString(this);
        socket.sendMessage(json);
    }
}
