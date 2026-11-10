package com.tayek.mf;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class KataGoAdapterTest {
    @Test void serializesRecordedMovesRatherThanFinalStones() {
        GameController controller = new GameController();
        controller.newGoGame(19);
        assertTrue(controller.move(3, 3));
        assertTrue(controller.move(15, 15));
        assertTrue(controller.pass());
        String request = KataGoPosition.query("a", controller, 7.5, 100);
        var data = (java.util.Map<?, ?>) KataGoJson.parse(request);
        assertEquals("a", data.get("id"));
        assertEquals(java.util.List.of(), data.get("initialStones"));
        assertEquals(java.util.List.of(
                java.util.List.of("B", "D16"),
                java.util.List.of("W", "Q4"),
                java.util.List.of("B", "pass")), data.get("moves"));
        assertEquals("B", data.get("initialPlayer"));
        assertEquals(java.util.List.of(3.0), data.get("analyzeTurns"));
    }

    @Test void followsSelectedVariationAndSetup() {
        GameController controller = new GameController();
        controller.open(SgfReader.read("(;GM[1]SZ[9]AB[cc]AW[gg]PL[W];W[dd](;B[ee])(;B[ff]))"));
        assertTrue(controller.right());
        assertTrue(controller.right());
        assertTrue(controller.down());
        var data = (java.util.Map<?, ?>) KataGoJson.parse(KataGoPosition.query("b", controller, 7.5, 30));
        assertEquals(java.util.List.of(java.util.List.of("B", "C7"), java.util.List.of("W", "G3")), data.get("initialStones"));
        assertEquals(java.util.List.of(java.util.List.of("W", "D6"), java.util.List.of("B", "F4")), data.get("moves"));
        assertEquals("W", data.get("initialPlayer"));
        assertEquals(java.util.List.of(2.0), data.get("analyzeTurns"));
    }

    @Test void refusesMidgameSetupInsteadOfLosingKoHistory() {
        GameController controller = new GameController();
        controller.open(SgfReader.read("(;GM[1]SZ[9];B[dd];W[ee]AB[aa])"));
        assertTrue(controller.right());
        assertTrue(controller.right());
        assertThrows(IllegalArgumentException.class, () -> KataGoPosition.query("c", controller, 7.5, 30));
    }

    @Test void parsesRepresentativeRecordedResponse() {
        String response = """
            {"id":"test1","isDuringSearch":false,"moveInfos":[
              {"move":"R16","visits":23,"winrate":0.360601088,"scoreLead":-0.88848952,
               "pv":["R16","D16","D3"]}],
             "rootInfo":{"currentPlayer":"B","visits":100,"winrate":0.36,"scoreLead":-0.9},
             "turnNumber":0}
            """;
        KataGoAnalysis result = KataGoAnalysis.fromJson(response);
        assertEquals("test1", result.id());
        assertEquals("R16", result.moves().getFirst().move());
        assertEquals(0.360601088, result.moves().getFirst().blackWinRate());
        assertEquals(3, result.moves().getFirst().variation().size());
        assertThrows(UnsupportedOperationException.class, () -> result.moves().clear());
    }

    @Test void parsesErrorsAndEscapes() {
        assertEquals("a\nb", ((java.util.Map<?, ?>) KataGoJson.parse("{\"x\":null,\"s\":\"a\\nb\"}")).get("s"));
        assertThrows(IllegalArgumentException.class, () -> KataGoAnalysis.fromJson("{\"error\":\"bad request\"}"));
    }
}
