package com.tayek.mf;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class KataGoVariationTest {
    @Test void convertsCoordinatesAndAlternatesPlayers() {
        List<Move> moves = KataGoVariation.moves(List.of("D16", "Q4", "pass", "T1"), 19, 19, Stone.BLACK);
        assertEquals(new Move(3, 3, Stone.BLACK), moves.get(0));
        assertEquals(new Move(15, 15, Stone.WHITE), moves.get(1));
        assertEquals(new Move(-1, -1, Stone.BLACK), moves.get(2));
        assertEquals(new Move(18, 18, Stone.WHITE), moves.get(3));
    }

    @Test void rejectsInvalidAndOutOfBoundsMoves() {
        assertThrows(IllegalArgumentException.class,
                () -> KataGoVariation.moves(List.of("I9"), 19, 19, Stone.BLACK));
        assertThrows(IllegalArgumentException.class,
                () -> KataGoVariation.moves(List.of("T19"), 9, 9, Stone.BLACK));
        assertThrows(IllegalArgumentException.class,
                () -> KataGoVariation.moves(List.of("D0"), 19, 19, Stone.BLACK));
    }
}
