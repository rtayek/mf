package com.tayek.mf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Immutable result from one completed KataGo analysis query. Rates are from Black's perspective. */
public record KataGoAnalysis(String id, int turnNumber, String player, int visits,
        double blackWinRate, double blackScoreLead, List<Candidate> moves) {
    public record Candidate(String move, int visits, double blackWinRate,
            double blackScoreLead, List<String> variation) {
        public Candidate { variation = List.copyOf(variation); }
    }

    public KataGoAnalysis { moves = List.copyOf(moves); }

    static KataGoAnalysis fromJson(String json) {
        Map<String, Object> root = map(KataGoJson.parse(json));
        if (root.containsKey("error")) throw new IllegalArgumentException("KataGo: " + root.get("error"));
        Map<String, Object> info = map(root.get("rootInfo"));
        List<Candidate> candidates = new ArrayList<>();
        for (Object item : list(root.get("moveInfos"))) {
            Map<String, Object> move = map(item);
            List<String> pv = new ArrayList<>();
            for (Object step : list(move.get("pv"))) pv.add((String) step);
            candidates.add(new Candidate((String) move.get("move"), integer(move, "visits"),
                    number(move, "winrate"), number(move, "scoreLead"), pv));
        }
        return new KataGoAnalysis((String) root.get("id"), integer(root, "turnNumber"),
                (String) info.get("currentPlayer"), integer(info, "visits"),
                number(info, "winrate"), number(info, "scoreLead"), candidates);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) { return (Map<String, Object>) value; }
    @SuppressWarnings("unchecked")
    private static List<Object> list(Object value) { return (List<Object>) value; }
    private static int integer(Map<String, Object> map, String key) { return ((Number) map.get(key)).intValue(); }
    private static double number(Map<String, Object> map, String key) { return ((Number) map.get(key)).doubleValue(); }
}
