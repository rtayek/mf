package com.tayek.mf;

import java.util.Arrays;
import java.util.List;

/** Minimal Gomoku position used as the second SGF game implementation. */
public final class GomokuPosition {
    private final int width, height;
    private final Stone[][] stones;
    private int moveNumber;

    public GomokuPosition(int width, int height) {
        if (width < 2 || height < 2) throw new IllegalArgumentException("board must be at least 2 x 2");
        this.width = width;
        this.height = height;
        stones = new Stone[height][width];
        clear();
    }

    public int width() { return width; }
    public int height() { return height; }
    public Stone stoneAt(int x, int y) { return stones[y][x]; }
    public int moveNumber() { return moveNumber; }
    public Stone sideToMove() { return moveNumber % 2 == 0 ? Stone.BLACK : Stone.WHITE; }

    public void loadMoves(List<Move> moves) {
        clear();
        moveNumber = 0;
        for (Move move : moves) {
            if (move.x() < 0 || move.y() < 0) {
                moveNumber++;
                continue;
            }
            if (!inside(move.x(), move.y()) || stones[move.y()][move.x()] != Stone.EMPTY)
                throw new IllegalStateException("recorded Gomoku move is illegal: " + move);
            stones[move.y()][move.x()] = move.stone();
            moveNumber++;
        }
    }

    private boolean inside(int x, int y) { return x >= 0 && x < width && y >= 0 && y < height; }
    private void clear() { for (Stone[] row : stones) Arrays.fill(row, Stone.EMPTY); }
}
