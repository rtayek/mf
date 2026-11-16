package com.tayek.mf;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class KataGoHeatmapTest {
    @Test void mapsKataGoCoordinatesAndSkipsInvalidCandidates() {
        KataGoAnalysis analysis = new KataGoAnalysis("q", 0, "B", 100, .5, 0, List.of(
                candidate("A19", 30), candidate("J10", 20), candidate("T1", 10),
                candidate("pass", 10), candidate("I5", 10), candidate("A19", 5),
                candidate("T20", 5), candidate("B3", 0)));
        assertEquals(List.of(new KataGoHeatmap.Spot(0, 0, 30, 0),
                new KataGoHeatmap.Spot(8, 9, 20, 1),
                new KataGoHeatmap.Spot(18, 18, 10, 2)),
                KataGoHeatmap.candidates(analysis, 19, 19));
    }

    @Test void excludesCoordinatesOutsideSmallerBoard() {
        KataGoAnalysis analysis = new KataGoAnalysis("q", 0, "B", 100, .5, 0,
                List.of(candidate("J9", 15), candidate("K9", 10), candidate("A1", 5)));
        assertEquals(List.of(new KataGoHeatmap.Spot(8, 0, 15, 0),
                new KataGoHeatmap.Spot(0, 8, 5, 1)), KataGoHeatmap.candidates(analysis, 9, 9));
    }

    private static KataGoAnalysis.Candidate candidate(String move, int visits) {
        return new KataGoAnalysis.Candidate(move, visits, .5, 0, List.of());
    }
}
