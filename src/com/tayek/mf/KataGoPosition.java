package com.tayek.mf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** The selected SGF path, converted to a KataGo analysis query without changing the game. */
final class KataGoPosition {
    static String query(String id, GameController controller, double komi, int maxVisits) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(controller);
        if (controller.game() == null || !controller.game().type().equals(GameType.GO))
            throw new IllegalArgumentException("Go games only");
        if (maxVisits < 1) throw new IllegalArgumentException("maxVisits must be positive");
        int size = controller.game().boardSize();
        Map<Integer, Stone> setup = new LinkedHashMap<>();
        List<Move> moves = new ArrayList<>();
        Stone player = null;
        boolean explicitPlayer = false;
        boolean handicap = false;
        for (GameNode node : controller.path()) {
            for (String idProperty : List.of("AB", "AW", "AE")) {
                for (String value : node.property(idProperty)) {
                    if (!moves.isEmpty())
                        throw new IllegalArgumentException("Setup stones after the first move are not supported for KataGo analysis");
                    if (value.length() != 2)
                        throw new IllegalArgumentException("SGF setup point ranges are not supported for KataGo analysis: " + value);
                    int x = value.charAt(0) - 'a', y = value.charAt(1) - 'a';
                    if (x < 0 || y < 0 || x >= size || y >= size)
                        throw new IllegalArgumentException("Invalid SGF setup point: " + value);
                    int point = y * size + x;
                    if (idProperty.equals("AE")) setup.remove(point);
                    else setup.put(point, idProperty.equals("AB") ? Stone.BLACK : Stone.WHITE);
                }
            }
            for (String value : node.property("HA")) {
                if (!moves.isEmpty()) throw new IllegalArgumentException("Midgame handicap is not supported");
                try { handicap = Integer.parseInt(value) > 1; }
                catch (NumberFormatException ex) { throw new IllegalArgumentException("Invalid SGF handicap: " + value, ex); }
            }
            for (String value : node.property("PL")) {
                if (!moves.isEmpty())
                    throw new IllegalArgumentException("Midgame player overrides are not supported for KataGo analysis");
                if (!value.equalsIgnoreCase("B") && !value.equalsIgnoreCase("W"))
                    throw new IllegalArgumentException("Invalid SGF player: " + value);
                player = value.equalsIgnoreCase("B") ? Stone.BLACK : Stone.WHITE;
                explicitPlayer = true;
            }
            if (node.move() != null) moves.add(node.move());
        }
        if (!moves.isEmpty()) {
            Stone first = moves.getFirst().stone();
            if (explicitPlayer && player != first)
                throw new IllegalArgumentException("SGF initial player conflicts with first recorded move");
            player = first;
        } else if (!explicitPlayer) {
            player = handicap ? Stone.WHITE : controller.position().sideToMove();
        }
        StringBuilder json = new StringBuilder("{\"id\":\"").append(id).append("\",\"initialStones\":[");
        boolean first = true;
        for (var entry : setup.entrySet()) {
            if (!first) json.append(',');
            first = false;
            int point = entry.getKey();
            pair(json, entry.getValue(), coordinate(point % size, point / size, size));
        }
        json.append("],\"moves\":[");
        for (int i = 0; i < moves.size(); i++) {
            if (i > 0) json.append(',');
            Move move = moves.get(i);
            pair(json, move.stone(), move.x() < 0 ? "pass" : coordinate(move.x(), move.y(), size));
        }
        return json.append("],\"initialPlayer\":\"").append(color(player))
                .append("\",\"rules\":\"chinese\",\"komi\":").append(komi)
                .append(",\"boardXSize\":").append(size)
                .append(",\"boardYSize\":").append(size)
                .append(",\"analyzeTurns\": [").append(moves.size()).append(']')
                .append(",\"maxVisits\":").append(maxVisits).append('}').toString();
    }

    private static void pair(StringBuilder json, Stone stone, String point) {
        json.append("[\"").append(color(stone)).append("\",\"").append(point).append("\"]");
    }

    private static String color(Stone stone) { return stone == Stone.BLACK ? "B" : "W"; }

    private static String coordinate(int x, int y, int height) {
        int column = x >= 8 ? x + 1 : x;
        return "" + (char) ('A' + column) + (height - y);
    }

    private KataGoPosition() { }
}
