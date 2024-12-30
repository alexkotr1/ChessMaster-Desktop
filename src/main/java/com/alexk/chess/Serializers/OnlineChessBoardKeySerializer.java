package com.alexk.chess.Serializers;

import com.alexk.chess.ChessBoard.OnlineChessBoard;
import com.alexk.chess.Pionia.Pioni;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class OnlineChessBoardKeySerializer extends JsonSerializer<OnlineChessBoard> {
    @Override
    public void serialize(OnlineChessBoard value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        StringBuilder keyBuilder = new StringBuilder();

        for (Pioni pioni : value.getPionia()) {
            String pioniKey = pioni.getType() + "-" + pioni.getIsWhite() + "-" + pioni.getXPos() + "-" + pioni.getYPos();
            keyBuilder.append(pioniKey).append(";");
        }

        keyBuilder.append("whiteTurn:").append(value.getWhiteTurn()).append(";");
        keyBuilder.append("movesRemaining:").append(value.getMovesRemaining());
        keyBuilder.append("gameEnded:").append(value.getGameEnded());
        keyBuilder.append("state:").append(value.getState());

        gen.writeFieldName(keyBuilder.toString());
    }
}
