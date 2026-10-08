package com.tayek.mf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import javafx.scene.Node;

/**
 * Narrow boundary between the generic SGF/tree viewer and a game-specific
 * position/view pair. This deliberately does not try to define the game's
 * rules, board geometry, or universal state model.
 */
interface GameRuntime {
    Node view();
    void showPath(List<GameNode> path);
    default void setShowSingleContinuation(boolean show) { }
    int moveNumber();
    Stone sideToMove();

    static GameRuntime forGame(Game game, Runnable positionChanged, IntConsumer variationSelected) {
        if (game.type().equals(GameType.GO)) return new GoRuntime(game, positionChanged, variationSelected);
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
        private boolean showSingleContinuation = true;
        private BoardPosition position;
        private final BoardView view;

        GoRuntime(Game game, Runnable positionChanged, IntConsumer variationSelected) {
            size = game.boardSize();
            position = new BoardPosition(size, size);
            view = new BoardView(position);
            view.setOnPositionChanged(positionChanged);
            view.setOnVariationSelected(variationSelected);
        }

        @Override public Node view() { return view; }
        @Override public void setShowSingleContinuation(boolean show) { showSingleContinuation = show; }

        @Override public void showPath(List<GameNode> path) {
            BoardPosition loaded = new BoardPosition(size, size);
            loaded.loadRecord(setup(path), moves(path), playerToMove(path));
            position = loaded;
            view.setPosition(loaded);
            GameNode current = path.isEmpty() ? null : path.get(path.size() - 1);
            view.setLabels(labels(current));
            view.setMarks(marks(current));
        }

        private List<BoardPosition.SetupStone> setup(List<GameNode> path) {
            ArrayList<BoardPosition.SetupStone> result = new ArrayList<>();
            for (GameNode node : path) {
                for (String value : node.property("AB")) addSetup(result, value, Stone.BLACK);
                for (String value : node.property("AW")) addSetup(result, value, Stone.WHITE);
                for (String value : node.property("AE")) addSetup(result, value, Stone.EMPTY);
            }
            return result;
        }

        private void addSetup(List<BoardPosition.SetupStone> result, String value, Stone stone) {
            if (value.length() < 2) return;
            int x = value.charAt(0) - 'a', y = value.charAt(1) - 'a';
            if (x < 0 || x >= size || y < 0 || y >= size) return;
            result.removeIf(s -> s.x() == x && s.y() == y);
            result.add(new BoardPosition.SetupStone(x, y, stone));
        }

        private Stone playerToMove(List<GameNode> path) {
            Stone result = null;
            for (GameNode node : path)
                for (String value : node.property("PL")) {
                    if ("B".equalsIgnoreCase(value)) result = Stone.BLACK;
                    else if ("W".equalsIgnoreCase(value)) result = Stone.WHITE;
                }
            return result;
        }

        private List<BoardView.BoardMark> marks(GameNode node) {
            if(node==null)return List.of();
            ArrayList<BoardView.BoardMark> result=new ArrayList<>();
            for(String id:List.of("TR","SQ","CR","MA")){
                BoardView.BoardMark.Kind kind=switch(id){
                    case "TR" -> BoardView.BoardMark.Kind.TRIANGLE;
                    case "SQ" -> BoardView.BoardMark.Kind.SQUARE;
                    case "CR" -> BoardView.BoardMark.Kind.CIRCLE;
                    default -> BoardView.BoardMark.Kind.X;
                };
                for(String value:node.property(id)){
                    if(value.length()!=2)continue;
                    int x=value.charAt(0)-'a',y=value.charAt(1)-'a';
                    if(x>=0&&y>=0&&x<size&&y<size)
                        result.add(new BoardView.BoardMark(x,y,kind));
                }
            }
            return result;
        }

        private List<BoardLabel> labels(GameNode node) {
            if (node == null) return List.of();
            ArrayList<BoardLabel> authored = new ArrayList<>();
            for (String value : node.property("LB")) {
                int colon = value.indexOf(':');
                if (colon < 2) continue;
                int x = value.charAt(0) - 'a';
                int y = value.charAt(1) - 'a';
                String text = value.substring(colon + 1);
                if (x >= 0 && x < position.width() && y >= 0 && y < position.height() && !text.isEmpty())
                    authored.add(new BoardLabel(x, y, text));
            }

            // Variation controls are derived only from the current node. If the
            // SGF already labels a child's move point, reuse that authored text
            // as the control instead of drawing a second synthesized label.
            ArrayList<BoardLabel> result = new ArrayList<>();
            boolean[] used = new boolean[authored.size()];
            for (int childIndex = 0; childIndex < node.children().size(); childIndex++) {
                Move move = node.children().get(childIndex).move();
                if (move == null || move.x() < 0 || move.y() < 0
                        || move.x() >= position.width() || move.y() >= position.height()) continue;
                String text = variationLabel(childIndex);
                boolean hideSingle = !showSingleContinuation && node.children().size() == 1;
                for (int i = 0; i < authored.size(); i++) {
                    BoardLabel label = authored.get(i);
                    if (label.x() == move.x() && label.y() == move.y()) {
                        text = label.text();
                        used[i] = true;
                        break;
                    }
                }
                // A navigation label never belongs underneath an existing stone.
                if (position.stoneAt(move.x(), move.y()) == Stone.EMPTY)
                    result.add(BoardLabel.variation(move.x(), move.y(), hideSingle ? "" : text, childIndex));
            }
            for (int i = 0; i < authored.size(); i++)
                if (!used[i]) result.add(authored.get(i));
            return result;
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
