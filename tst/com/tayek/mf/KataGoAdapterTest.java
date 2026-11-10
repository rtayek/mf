package com.tayek.mf;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class KataGoAdapterTest {
    @Test void serializesCurrentBoardWithoutInventingHistory() {
        GameController controller = new GameController();
        controller.newGoGame(19);
        assertTrue(controller.move(3, 3));
        String request = KataGoAdapter.query("a", controller.position(), 7.5, 100);
        assertTrue(request.contains("\"initialStones\":[[\"B\",\"D16\"]]"));
        assertTrue(request.contains("\"initialPlayer\":\"W\""));
        assertTrue(request.contains("\"moves\":[]"));
        assertEquals("a", ((java.util.Map<?, ?>) KataGoJson.parse(request)).get("id"));
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
