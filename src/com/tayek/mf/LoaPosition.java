package com.tayek.mf;

import java.util.Arrays;
import java.util.List;

/** Minimal Lines of Action position for replaying GM[9] SGF records. */
public final class LoaPosition {
    public static final int SIZE = 8;
    private final Stone[][] pieces = new Stone[SIZE][SIZE];
    private int moveNumber;

    public LoaPosition() { reset(); }

    public int width() { return SIZE; }
    public int height() { return SIZE; }
    public Stone stoneAt(int x, int y) { return pieces[y][x]; }
    public int moveNumber() { return moveNumber; }
    public Stone sideToMove() { return moveNumber % 2 == 0 ? Stone.BLACK : Stone.WHITE; }

    /** Rebuild the position from the SGF nodes on the selected path. */
    public void loadNodes(List<GameNode> nodes) {
        reset();
        for (GameNode node : nodes) {
            if (!node.property("B").isEmpty()) apply(node.property("B").get(0), Stone.BLACK);
            else if (!node.property("W").isEmpty()) apply(node.property("W").get(0), Stone.WHITE);
        }
    }

    private void reset() {
        for (Stone[] row : pieces) Arrays.fill(row, Stone.EMPTY);
        // Standard LOA: black across top/bottom; white down left/right; corners empty.
        for (int x = 1; x < SIZE - 1; x++) {
            pieces[0][x] = Stone.BLACK;
            pieces[SIZE - 1][x] = Stone.BLACK;
        }
        for (int y = 1; y < SIZE - 1; y++) {
            pieces[y][0] = Stone.WHITE;
            pieces[y][SIZE - 1] = Stone.WHITE;
        }
        moveNumber = 0;
    }

    private void apply(String text, Stone player) {
        if (text == null || text.isBlank()) return;
        String move = text.trim().toUpperCase();
        int dash = move.indexOf('-');
        if (dash < 0) throw new IllegalArgumentException("unsupported LOA move: " + text);
        Point from = point(move.substring(0, dash));
        Point to = point(move.substring(dash + 1));
        if (pieces[from.y][from.x] != player)
            throw new IllegalStateException("LOA move has no " + player + " piece at " + move.substring(0, dash));
        pieces[from.y][from.x] = Stone.EMPTY;
        pieces[to.y][to.x] = player; // replacing an opposing piece is a capture
        moveNumber++;
    }

    private static Point point(String value) {
        if (value.length() < 2) throw new IllegalArgumentException("bad LOA square: " + value);
        int x = value.charAt(0) - 'A';
        int rank;
        try { rank = Integer.parseInt(value.substring(1)); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("bad LOA square: " + value); }
        int y = SIZE - rank; // SGF LOA notation uses A1 at lower left.
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE)
            throw new IllegalArgumentException("LOA square outside board: " + value);
        return new Point(x, y);
    }

    private record Point(int x, int y) { }
}
