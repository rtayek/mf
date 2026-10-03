package com.tayek.mf;

public record GameType(int sgfNumber) {
    public static final GameType GO = new GameType(1);

    public GameType {
        if (sgfNumber < 1) {
            throw new IllegalArgumentException("SGF game number must be positive");
        }
    }
}
