package com.alexk.chess;

public class GameSession {
    public enum GameState {
        WAITING_FOR_HOST_CODE,
        WAITING_FOR_PLAYER_JOIN,
        WAITING_FOR_PLAYER,
        IN_GAME,
        GAME_OVER
    }

    private static GameState state;

    public static GameState getState() {
        return state;
    }

    public static void setState(GameState state_) {
        state = state_;
    }
}
