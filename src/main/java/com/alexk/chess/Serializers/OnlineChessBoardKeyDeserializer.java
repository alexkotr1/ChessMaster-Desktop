package com.alexk.chess.Serializers;

import com.alexk.chess.ChessBoard.OnlineChessBoard;
import com.alexk.chess.Pionia.Pioni;
import com.alexk.chess.Pionia.PioniFactory;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import java.io.IOException;
import java.util.ArrayList;

public class OnlineChessBoardKeyDeserializer extends KeyDeserializer {
    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) {
        String[] parts = key.split(";");
        ArrayList<Pioni> Pionia = new ArrayList<>();
        boolean whiteTurn = true;
        boolean gameEnded = false;
        int movesRemaining = 100;
        String state = "";
        for (String part : parts) {
            if (part.startsWith("whiteTurn:")) {
                whiteTurn = Boolean.parseBoolean(part.split(":")[1]);
            } else if (part.startsWith("movesRemaining:")) {
                movesRemaining = Integer.parseInt(part.split(":")[1]);
            } else if (part.startsWith("gameEnded:")) {
                gameEnded = Boolean.parseBoolean(part.split(":")[1]);
            } else if (part.startsWith("state:")) {
                state = part.split(":")[1];
            }
            else {
                String[] pioniParts = part.split("-");
                if (pioniParts.length == 6) {
                    String type = pioniParts[0];
                    boolean isWhite = Boolean.parseBoolean(pioniParts[1]);
                    char xPos = pioniParts[2].charAt(0);
                    int yPos = Integer.parseInt(pioniParts[3]);
                    String id = pioniParts[4];
                    Boolean cap = Boolean.parseBoolean(pioniParts[5]);
                    Pioni pioni = PioniFactory.createPioni(type, isWhite, null, xPos, yPos, id, cap);
                    Pionia.add(pioni);
                }
            }
        }

        return new OnlineChessBoard(Pionia, whiteTurn, movesRemaining, gameEnded,state);
    }
}
