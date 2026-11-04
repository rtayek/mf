package com.tayek.mf;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class GameControllerTest {
    @Test void newGoGameStartsEmptyAndCanBeSaved() throws Exception {
        for (int size : new int[] {9, 13, 19}) {
            GameController controller = new GameController();
            controller.newGoGame(size);
            assertEquals(GameType.GO, controller.game().type());
            assertEquals(size, controller.position().width());
            assertEquals(size, controller.position().height());
            assertEquals(0, controller.position().moveNumber());
            assertEquals(Stone.BLACK, controller.position().sideToMove());
            assertNull(controller.file());
            assertFalse(controller.modified());
            assertTrue(controller.move(0, 0));
            assertEquals(Stone.BLACK, controller.position().stoneAt(0, 0));
            var path = Files.createTempFile("mf-new-game-", ".sgf");
            try {
                controller.save(path);
                GameController loaded = new GameController();
                loaded.open(path);
                assertEquals(size, loaded.game().boardSize());
                assertTrue(loaded.right());
                assertEquals(Stone.BLACK, loaded.position().stoneAt(0, 0));
            } finally {
                Files.deleteIfExists(path);
            }
        }
        assertThrows(IllegalArgumentException.class, () -> new GameController().newGoGame(10));
    }

    @Test void variationsObserversAndRoundTrip() throws Exception {
        GameController controller = new GameController();
        var events = new ArrayList<GameController.Change>();
        controller.addObserver((c, change) -> events.add(change));
        ByteArrayOutputStream text = new ByteArrayOutputStream();
        MfCli cli = new MfCli(controller, new PrintStream(text));
        controller.open(SgfReader.read("(;GM[1]SZ[9];B[aa];W[bb];B[cc])"));
        assertTrue(controller.right());
        assertTrue(controller.right());
        assertTrue(controller.right());
        assertTrue(controller.left());
        assertTrue(controller.move(4, 4));
        assertEquals(2, controller.current().parent().children().size());
        assertTrue(controller.up()); // wraps to original variation
        assertEquals(2, controller.current().move().x());
        assertTrue(controller.down()); // wraps back to new variation
        assertEquals(4, controller.current().move().x());
        assertTrue(controller.undo());
        assertEquals(1, controller.current().children().size());
        assertTrue(controller.redo());
        assertEquals(4, controller.current().move().x());
        var path = Files.createTempFile("mf-controller-", ".sgf");
        try {
            controller.save(path);
            GameController reopened = new GameController();
            reopened.open(path);
            assertTrue(reopened.right());
            assertTrue(reopened.right());
            assertEquals(2, reopened.current().children().size());
        } finally {
            Files.deleteIfExists(path);
        }
        assertTrue(events.contains(GameController.Change.NAVIGATED));
        assertTrue(events.contains(GameController.Change.EDITED));
        assertTrue(text.toString().contains("Move "));
    }
}
