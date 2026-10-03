package com.tayek.mf;

import java.util.Objects;

public record Game(GameType type, int boardSize, String blackPlayer,
        String whitePlayer, String name, GameTree tree) {
    public Game {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(blackPlayer, "blackPlayer");
        Objects.requireNonNull(whitePlayer, "whitePlayer");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(tree, "tree");
        if (boardSize < 2) {
            throw new IllegalArgumentException("board must be at least 2 x 2");
        }
    }
}
