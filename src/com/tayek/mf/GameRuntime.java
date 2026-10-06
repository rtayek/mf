package com.tayek.mf;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;

/**
 * Narrow boundary between the generic SGF/tree viewer and a game-specific
 * position/view pair. This deliberately does not try to define the game's
 * rules, board geometry, or universal state model.
 */
interface GameRuntime {
    Node view();
    void showPath(List<GameNode> path);
    int moveNumber();
    Stone sideToMove();

    static GameRuntime forGame(Game game, Runnable positionChanged) {
        if (game.type().equals(GameType.GO)) return new GoRuntime(game, positionChanged);
        if (game.type().equals(GameType.GOMOKU)) return new GomokuRuntime(game);
        if (game.type().equals(GameType.LINES_OF_ACTION)) return new LoaRuntime();
        throw new IllegalArgumentException(
                "SGF game type GM[" + game.type().sgfNumber() + "] is not yet displayable");
    }

    private static List<Move> moves(List<GameNode> path) {
        ArrayList<Move> result = new ArrayList<>();
        for (GameNode node : path) if (node.move() != null) result.add(node.move());
        return result;
    }

    final class GoRuntime implements GameRuntime {
        private final int size;
        private BoardPosition position;
        private final BoardView view;

        GoRuntime(Game game, Runnable positionChanged) {
            size = game.boardSize();
            position = new BoardPosition(size, size);
            view = new BoardView(position);
            view.setOnPositionChanged(positionChanged);
        }

        @Override public Node view() { return view; }

        @Override public void showPath(List<GameNode> path) {
            BoardPosition loaded = new BoardPosition(size, size);
            loaded.loadMoves(moves(path));
            position = loaded;
            view.setPosition(loaded);
            view.setLabels(labels(path.isEmpty() ? null : path.get(path.size() - 1)));
        }

        private List<BoardLabel> labels(GameNode node) {
            if (node == null) return List.of();
            ArrayList<BoardLabel> result = new ArrayList<>();
            for (String value : node.property("LB")) {
                int colon = value.indexOf(':');
                if (colon < 2) continue;
                int x = value.charAt(0) - 'a';
                int y = value.charAt(1) - 'a';
                String text = value.substring(colon + 1);
                if (x >= 0 && x < position.width() && y >= 0 && y < position.height() && !text.isEmpty())
                    result.add(new BoardLabel(x, y, text));
            }
            addVariationLabels(node, result);
            return result;
        }

        private void addVariationLabels(GameNode node, List<BoardLabel> result) {
            if (node == null || node.children().size() < 2) return;
            int variation = 0;
            for (GameNode child : node.children()) {
                Move move = child.move();
                if (move == null || move.x() < 0 || move.y() < 0
                        || move.x() >= position.width() || move.y() >= position.height()) continue;
                String text = variationLabel(variation++);
                boolean occupied = false;
                for (BoardLabel label : result)
                    if (label.x() == move.x() && label.y() == move.y()) { occupied = true; break; }
                if (!occupied) result.add(new BoardLabel(move.x(), move.y(), text));
            }
        }

        private static String variationLabel(int n) {
            StringBuilder result = new StringBuilder();
            do {
                result.append((char) ('a' + n % 26));
                n = n / 26 - 1;
            } while (n >= 0);
            return result.reverse().toString();
        }

        @Override public int moveNumber() { return position.moveNumber(); }
        @Override public Stone sideToMove() { return position.sideToMove(); }
    }

    final class GomokuRuntime implements GameRuntime {
        private final int size;
        private GomokuPosition position;
        private final GomokuView view;

        GomokuRuntime(Game game) {
            size = game.boardSize();
            position = new GomokuPosition(size, size);
            view = new GomokuView(position);
        }

        @Override public Node view() { return view; }

        @Override public void showPath(List<GameNode> path) {
            GomokuPosition loaded = new GomokuPosition(size, size);
            loaded.loadMoves(moves(path));
            position = loaded;
            view.setPosition(loaded);
        }

        @Override public int moveNumber() { return position.moveNumber(); }
        @Override public Stone sideToMove() { return position.sideToMove(); }
    }

    final class LoaRuntime implements GameRuntime {
        private LoaPosition position = new LoaPosition();
        private final LoaView view = new LoaView(position);

        @Override public Node view() { return view; }

        @Override public void showPath(List<GameNode> path) {
            LoaPosition loaded = new LoaPosition();
            loaded.loadNodes(path);
            position = loaded;
            view.setPosition(loaded);
        }

        @Override public int moveNumber() { return position.moveNumber(); }
        @Override public Stone sideToMove() { return position.sideToMove(); }
    }
}
