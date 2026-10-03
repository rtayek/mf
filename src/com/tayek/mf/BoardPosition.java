package com.tayek.mf;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class BoardPosition {
    private static final int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private final int width;
    private final int height;
    private final Stone[][] stones;
    private final List<Move> moves = new ArrayList<>();
    private int moveNumber;
    private boolean lastMoveCreatedAtari;

    public BoardPosition(int width, int height) {
        if (width < 2 || height < 2) throw new IllegalArgumentException("board must be at least 2 x 2");
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
    public boolean lastMoveCreatedAtari() { return lastMoveCreatedAtari; }
    public Stone sideToMove() { return moveNumber % 2 == 0 ? Stone.BLACK : Stone.WHITE; }
    public Move lastMove() { return moveNumber == 0 ? null : moves.get(moveNumber - 1); }

    public boolean play(int x, int y) {
        lastMoveCreatedAtari = false;
        if (!inside(x, y) || stones[y][x] != Stone.EMPTY) return false;

        Stone color = sideToMove();
        Stone opponent = color.opposite();
        boolean[][] opponentAtariBefore = groupsInAtari(stones, opponent);
        Stone[][] candidate = copyBoard(stones);
        if (!applyMove(candidate, new Move(x, y, color))) return false;

        if (moveNumber > 0 && sameBoard(candidate, positionAfter(moveNumber - 1))) return false;

        boolean[][] opponentAtariAfter = groupsInAtari(candidate, opponent);
        lastMoveCreatedAtari = hasNewAtari(opponentAtariBefore, opponentAtariAfter);

        while (moves.size() > moveNumber) moves.remove(moves.size() - 1);
        moves.add(new Move(x, y, color));
        moveNumber++;
        copyInto(candidate, stones);
        return true;
    }

    public boolean previous() { if (moveNumber == 0) return false; moveNumber--; rebuild(); return true; }
    public boolean next() { if (moveNumber >= moves.size()) return false; moveNumber++; rebuild(); return true; }
    public boolean first() { if (moveNumber == 0) return false; moveNumber = 0; rebuild(); return true; }
    public boolean last() { if (moveNumber == moves.size()) return false; moveNumber = moves.size(); rebuild(); return true; }

    private void rebuild() {
        lastMoveCreatedAtari = false;
        clear(stones);
        for (int i = 0; i < moveNumber; i++) {
            if (!applyMove(stones, moves.get(i))) throw new IllegalStateException("recorded move is illegal: " + moves.get(i));
        }
    }

    private Stone[][] positionAfter(int count) {
        Stone[][] board = newBoard();
        for (int i = 0; i < count; i++) {
            if (!applyMove(board, moves.get(i))) throw new IllegalStateException("recorded move is illegal: " + moves.get(i));
        }
        return board;
    }

    private boolean applyMove(Stone[][] board, Move move) {
        int x = move.x(), y = move.y();
        if (!inside(x, y) || board[y][x] != Stone.EMPTY) return false;
        board[y][x] = move.stone();
        Stone opponent = move.stone().opposite();
        boolean[][] checked = new boolean[height][width];
        for (int[] d : directions) {
            int nx = x + d[0], ny = y + d[1];
            if (!inside(nx, ny) || checked[ny][nx] || board[ny][nx] != opponent) continue;
            Group group = groupAt(board, nx, ny);
            for (Point p : group.stones()) checked[p.y()][p.x()] = true;
            if (group.liberties() == 0) for (Point p : group.stones()) board[p.y()][p.x()] = Stone.EMPTY;
        }
        if (groupAt(board, x, y).liberties() == 0) {
            board[y][x] = Stone.EMPTY;
            return false;
        }
        return true;
    }

    private boolean[][] groupsInAtari(Stone[][] board, Stone color) {
        boolean[][] atari = new boolean[height][width];
        boolean[][] seen = new boolean[height][width];
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
            if (seen[y][x] || board[y][x] != color) continue;
            Group group = groupAt(board, x, y);
            for (Point p : group.stones()) seen[p.y()][p.x()] = true;
            if (group.liberties() == 1) for (Point p : group.stones()) atari[p.y()][p.x()] = true;
        }
        return atari;
    }

    private boolean hasNewAtari(boolean[][] before, boolean[][] after) {
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
            if (after[y][x] && !before[y][x]) return true;
        }
        return false;
    }

    private Group groupAt(Stone[][] board, int startX, int startY) {
        Stone color = board[startY][startX];
        boolean[][] seen = new boolean[height][width];
        boolean[][] libertySeen = new boolean[height][width];
        ArrayDeque<Point> queue = new ArrayDeque<>();
        List<Point> group = new ArrayList<>();
        int liberties = 0;
        queue.add(new Point(startX, startY));
        seen[startY][startX] = true;
        while (!queue.isEmpty()) {
            Point p = queue.removeFirst();
            group.add(p);
            for (int[] d : directions) {
                int nx = p.x() + d[0], ny = p.y() + d[1];
                if (!inside(nx, ny)) continue;
                if (board[ny][nx] == Stone.EMPTY) {
                    if (!libertySeen[ny][nx]) { libertySeen[ny][nx] = true; liberties++; }
                } else if (board[ny][nx] == color && !seen[ny][nx]) {
                    seen[ny][nx] = true;
                    queue.addLast(new Point(nx, ny));
                }
            }
        }
        return new Group(group, liberties);
    }

    private boolean inside(int x, int y) { return x >= 0 && x < width && y >= 0 && y < height; }
    private Stone[][] newBoard() { Stone[][] board = new Stone[height][width]; clear(board); return board; }
    private void clear(Stone[][] board) { for (Stone[] row : board) Arrays.fill(row, Stone.EMPTY); }
    private Stone[][] copyBoard(Stone[][] source) { Stone[][] copy = new Stone[height][width]; copyInto(source, copy); return copy; }
    private void copyInto(Stone[][] source, Stone[][] target) { for (int y = 0; y < height; y++) System.arraycopy(source[y], 0, target[y], 0, width); }
    private boolean sameBoard(Stone[][] a, Stone[][] b) { for (int y = 0; y < height; y++) if (!Arrays.equals(a[y], b[y])) return false; return true; }

    private record Point(int x, int y) { }
    private record Group(List<Point> stones, int liberties) { }
}
