package com.tayek.mf;

public final class BoardPosition {
    private final int width;
    private final int height;
    private final Stone[][] stones;
    private Stone sideToMove = Stone.BLACK;

    public BoardPosition(int width, int height) {
        if (width < 2 || height < 2) {
            throw new IllegalArgumentException("board must be at least 2 x 2");
        }
        this.width = width;
        this.height = height;
        stones = new Stone[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                stones[y][x] = Stone.EMPTY;
            }
        }
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public Stone stoneAt(int x, int y) {
        return stones[y][x];
    }

    public Stone sideToMove() {
        return sideToMove;
    }

    public boolean play(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height || stones[y][x] != Stone.EMPTY) {
            return false;
        }
        stones[y][x] = sideToMove;
        sideToMove = sideToMove.opposite();
        return true;
    }
}
