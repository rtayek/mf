package com.tayek.mf;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Converts a KataGo principal variation to board moves without altering the SGF. */
final class KataGoVariation {
    static List<Move> moves(List<String> variation, int width, int height, Stone player) {
        Objects.requireNonNull(variation);
        Objects.requireNonNull(player);
        List<Move> result = new ArrayList<>();
        for (String coordinate : variation) {
            int x, y;
            if ("pass".equalsIgnoreCase(coordinate)) {
                x = y = -1;
            } else {
                if (coordinate == null || !coordinate.matches("(?i)[A-HJ-Z][1-9][0-9]?"))
                    throw new IllegalArgumentException("Invalid KataGo move: " + coordinate);
                char letter = Character.toUpperCase(coordinate.charAt(0));
                x = letter - 'A' - (letter > 'I' ? 1 : 0);
                y = height - Integer.parseInt(coordinate.substring(1));
                if (x < 0 || x >= width || y < 0 || y >= height)
                    throw new IllegalArgumentException("Move outside board: " + coordinate);
            }
            result.add(new Move(x, y, player));
            player = player.opposite();
        }
        return List.copyOf(result);
    }

    private KataGoVariation() { }
}
