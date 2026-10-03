package com.tayek.mf;

import java.util.List;

public record SgfGame(int boardSize, String blackPlayer, String whitePlayer,
        String gameName, List<Move> moves, GameNode root) {
}
