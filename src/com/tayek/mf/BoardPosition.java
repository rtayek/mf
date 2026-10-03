package com.tayek.mf;

import java.util.ArrayList;
import java.util.List;

public final class BoardPosition {
    private final int width;
    private final int height;
    private final Stone[][] stones;
    private final List<Move> moves = new ArrayList<>();
    private int moveNumber;

    public BoardPosition(int width, int height) {
        if (width < 2 || height < 2) {
            throw new IllegalArgumentException("board must be at least 2 x 2");
        }
        this.width = width;
        this.height = height;
        stones = new Stone[height][width];
        rebuild();
    }

    public int width() { return width; }
    public int height() { return height; }
    public Stone stoneAt(int x, int y) { return stones[y][x]; }
    public int moveNumber() { return moveNumber; }
    public int moveCount() { return moves.size(); }

    public Stone sideToMove() {
        return moveNumber % 2 == 0 ? Stone.BLACK : Stone.WHITE;
    }

    public Move lastMove() {
        return moveNumber == 0 ? null : moves.get(moveNumber - 1);
    }

    public boolean play(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height || stones[y][x] != Stone.EMPTY) {
            return false;
        }
        while (moves.size() > moveNumber) {
            moves.remove(moves.size() - 1);
        }
        moves.add(new Move(x, y, sideToMove()));
        moveNumber++;
        rebuild();
        return true;
    }

    public boolean previous() {
        if (moveNumber == 0) return false;
        moveNumber--;
        rebuild();
        return true;
    }

    public boolean next() {
        if (moveNumber >= moves.size()) return false;
        moveNumber++;
        rebuild();
        return true;
    }

    public boolean first() {
        if (moveNumber == 0) return false;
        moveNumber = 0;
        rebuild();
        return true;
    }

    public boolean last() {
        if (moveNumber == moves.size()) return false;
        moveNumber = moves.size();
        rebuild();
        return true;
    }

    private void rebuild() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                stones[y][x] = Stone.EMPTY;
            }
        }
        for (int i = 0; i < moveNumber; i++) {
            Move move = moves.get(i);
            stones[move.y()][move.x()] = move.stone();
        }
    }
}
