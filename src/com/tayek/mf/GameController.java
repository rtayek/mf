package com.tayek.mf;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/** Shared, UI-independent session for SGF navigation and editing. */
public final class GameController {
    public enum Action { LEFT, RIGHT, UP, DOWN, MOVE, PASS, RESIGN, DELETE, UNDO, REDO }
    public enum Change { OPENED, NAVIGATED, EDITED, SAVED }
    @FunctionalInterface public interface Observer { void changed(GameController controller, Change change); }

    private final List<Observer> observers = new CopyOnWriteArrayList<>();
    private final CommandHistory history = new CommandHistory();
    private GameCollection collection;
    private Game game;
    private GameNode current;
    private Path file;

    public void addObserver(Observer observer) { observers.add(Objects.requireNonNull(observer)); }
    public void removeObserver(Observer observer) { observers.remove(observer); }
    private void publish(Change change) { for (Observer observer : observers) observer.changed(this, change); }

    public Game game() { return game; }
    public GameNode current() { return current; }
    public GameCollection collection() { return collection; }
    public Path file() { return file; }
    public boolean modified() { return history.isModified(); }
    public boolean canUndo() { return history.canUndo(); }
    public boolean canRedo() { return history.canRedo(); }

    public void open(Path path) throws IOException {
        GameCollection parsed = SgfReader.read(path);
        open(parsed);
        file = path;
    }
    public void open(GameCollection parsed) {
        Game selected = parsed.games().stream().findFirst().orElseThrow();
        collection = parsed;
        game = selected;
        current = game.tree().root();
        file = null;
        history.clear();
        publish(Change.OPENED);
    }
    public void save(Path path) throws IOException {
        requireGame();
        SgfSave.saveAs(collection, path);
        file = path;
        history.markSaved();
        publish(Change.SAVED);
    }
    public void save() throws IOException {
        if (file == null) throw new IllegalStateException("no file selected");
        save(file);
    }

    public boolean left() { return select(current == null ? null : current.parent()); }
    public boolean right() { return current != null && !current.children().isEmpty() && select(current.children().get(0)); }
    public boolean up() { return sibling(-1); }
    public boolean down() { return sibling(1); }
    public boolean select(GameNode node) {
        if (node == null || node == current || !attached(node)) return false;
        current = node;
        publish(Change.NAVIGATED);
        return true;
    }
    private boolean sibling(int delta) {
        if (current == null || current.parent() == null) return false;
        List<GameNode> siblings = current.parent().children();
        if (siblings.size() < 2) return false;
        return select(siblings.get(Math.floorMod(siblings.indexOf(current) + delta, siblings.size())));
    }
    private boolean attached(GameNode node) {
        if (game == null) return false;
        GameNode root = game.tree().root();
        while (node != root) {
            GameNode parent = node.parent();
            if (parent == null || !parent.children().contains(node)) return false;
            node = parent;
        }
        return true;
    }
    public List<GameNode> path() {
        if (current == null) return List.of();
        ArrayList<GameNode> result = new ArrayList<>();
        for (GameNode n = current; n != null; n = n.parent()) result.add(0, n);
        return List.copyOf(result);
    }
    public BoardPosition position() {
        requireGame();
        if (!game.type().equals(GameType.GO)) throw new UnsupportedOperationException("Go board only");
        BoardPosition board = new BoardPosition(game.boardSize(), game.boardSize());
        List<Move> moves = new ArrayList<>();
        List<BoardPosition.SetupStone> setup = new ArrayList<>();
        Stone player = null;
        for (GameNode node : path()) {
            if (node.move() != null) moves.add(node.move());
            for (String id : List.of("AB", "AW", "AE")) {
                Stone color = id.equals("AB") ? Stone.BLACK : id.equals("AW") ? Stone.WHITE : Stone.EMPTY;
                for (String value : node.property(id)) {
                    if (value.length() < 2) continue;
                    int x = value.charAt(0) - 'a', y = value.charAt(1) - 'a';
                    if (x < 0 || y < 0 || x >= game.boardSize() || y >= game.boardSize()) continue;
                    setup.removeIf(s -> s.x() == x && s.y() == y);
                    setup.add(new BoardPosition.SetupStone(x, y, color));
                }
            }
            for (String value : node.property("PL")) {
                if (value.equalsIgnoreCase("B")) player = Stone.BLACK;
                if (value.equalsIgnoreCase("W")) player = Stone.WHITE;
            }
        }
        board.loadRecord(setup, moves, player);
        return board;
    }
    public boolean move(int x, int y) {
        BoardPosition board = position();
        Stone side = board.sideToMove();
        for (GameNode child : current.children()) {
            Move move = child.move();
            if (move != null && move.x() == x && move.y() == y && move.stone() == side) return select(child);
        }
        if (!board.play(x, y)) return false;
        return addMove(new Move(x, y, side));
    }
    public boolean pass() {
        BoardPosition board = position();
        Stone side = board.sideToMove();
        for (GameNode child : current.children()) {
            Move move = child.move();
            if (move != null && move.x() < 0 && move.y() < 0 && move.stone() == side) return select(child);
        }
        return addMove(new Move(-1, -1, side));
    }
    private boolean addMove(Move move) {
        String id = move.stone() == Stone.BLACK ? "B" : "W";
        String point = move.x() < 0 ? "" : "" + (char) ('a' + move.x()) + (char) ('a' + move.y());
        GameNode child = new GameNode(move, current, List.of(new SgfProperty(id, List.of(point))));
        history.execute(new AddNodeCommand(current, child));
        current = child;
        publish(Change.EDITED);
        return true;
    }
    public boolean delete() {
        if (current == null || current.parent() == null) return false;
        GameNode parent = current.parent();
        history.execute(new RemoveBranchCommand(current));
        current = parent;
        publish(Change.EDITED);
        return true;
    }
    public boolean undo() {
        Command command = history.nextUndo();
        if (!history.undo()) return false;
        if (command instanceof RemoveBranchCommand removed) current = removed.branch();
        while (current != null && !attached(current)) current = current.parent();
        if (current == null) current = game.tree().root();
        publish(Change.EDITED);
        return true;
    }
    public boolean redo() {
        Command command = history.nextRedo();
        if (!history.redo()) return false;
        if (command instanceof AddNodeCommand added) current = added.child();
        else if (command instanceof RemoveBranchCommand removed) current = removed.parent();
        publish(Change.EDITED);
        return true;
    }
    /** Resignation requires SGF result editing; do not silently invent a move node. */
    public boolean resign() { throw new UnsupportedOperationException("resignation result editing is not implemented"); }
    private void requireGame() { if (game == null) throw new IllegalStateException("open an SGF game first"); }
}
