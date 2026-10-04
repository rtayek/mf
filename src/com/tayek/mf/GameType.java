package com.tayek.mf;

public record GameType(int sgfNumber) {
    public static final GameType GO = new GameType(1);
    public static final GameType GOMOKU = new GameType(4);

    public GameType {
        if (sgfNumber < 1) {
            throw new IllegalArgumentException("SGF game number must be positive");
        }
    }
}
