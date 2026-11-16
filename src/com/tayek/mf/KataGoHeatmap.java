package com.tayek.mf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Candidate moves from one KataGo response, mapped onto board intersections. */
final class KataGoHeatmap {
    record Spot(int x, int y, int visits, int rank) { }

    static List<Spot> candidates(KataGoAnalysis analysis, int width, int height) {
        ArrayList<Spot> spots = new ArrayList<>();
        Set<Integer> used = new HashSet<>();
        int rank = 0;
        for (KataGoAnalysis.Candidate candidate : analysis.moves()) {
            if (spots.size() == 10) break;
            String move = candidate.move();
            if (move == null || !move.matches("(?i)[A-HJ-T](?:[1-9]|1[0-9])")) continue;
            char column = Character.toUpperCase(move.charAt(0));
            int x = column - 'A' - (column > 'I' ? 1 : 0);
            int row = Integer.parseInt(move.substring(1));
            int y = height - row;
            if (x < 0 || x >= width || y < 0 || y >= height || candidate.visits() <= 0
                    || !used.add(y * width + x)) continue;
            spots.add(new Spot(x, y, candidate.visits(), rank++));
        }
        return List.copyOf(spots);
    }

    private KataGoHeatmap() { }
}
