package com.alexk.chess.Serializers;

import com.alexk.chess.Pionia.*;
import com.fasterxml.jackson.databind.KeyDeserializer;

import java.io.IOException;

public class PioniKeyDeserializer extends KeyDeserializer {
    @Override
    public Object deserializeKey(String key, com.fasterxml.jackson.databind.DeserializationContext ctxt) throws IOException {
        String[] parts = key.split("/-/");
        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid Pioni key format: " + key);
        }

        String type = parts[0];
        Boolean isWhite = Boolean.parseBoolean(parts[1]);
        char x = parts[2].charAt(0);
        int y = Integer.parseInt(parts[3]);
        String id = parts[4];
        Boolean captured = Boolean.parseBoolean(parts[5]);
        Boolean moved = null;
        Boolean kingSide = null;
        if (type.equals("Vasilias")){
            moved = Boolean.parseBoolean(parts[6]);
        }
        else if (type.equals("Pyrgos")){
            moved = Boolean.parseBoolean(parts[6]);
            kingSide = Boolean.parseBoolean(parts[7]);
        }

        return switch (type) {
            case "Stratiotis" -> new Stratiotis(isWhite, null, x, y, id, captured,null,null);
            case "Pyrgos" -> new Pyrgos(isWhite, null, x, y, id, captured,moved,kingSide);
            case "Alogo" -> new Alogo(isWhite, null, x, y, id, captured,null,null);
            case "Stratigos" -> new Stratigos(isWhite, null, x, y, id, captured,null,null);
            case "Vasilissa" -> new Vasilissa(isWhite, null, x, y, id, captured,null,null);
            case "Vasilias" -> new Vasilias(isWhite, null, x, y, id, captured,moved,null);
            default -> throw new IllegalArgumentException("Unknown Pioni type: " + type);
        };
    }
}