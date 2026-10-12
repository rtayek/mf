package com.tayek.mf;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

final class GameEditsTest {
    @Test void undoRedoPreservesVariationOrderAndIdentity() {
        GameCollection games = SgfReader.read("(;GM[1]SZ[9];B[aa](;W[bb])(;W[cc]))");
        GameNode parent = games.games().get(0).tree().root().children().get(0);
        GameNode first = parent.children().get(0);
        GameNode second = parent.children().get(1);
        GameNode added = new GameNode(new Move(3, 3, Stone.WHITE), parent,
                List.of(new SgfProperty("W", List.of("dd"))));
        CommandHistory history = new CommandHistory();
        history.execute(new AddNodeCommand(parent, added, 1));
        assertEquals(List.of(first, added, second), parent.children());
        String saved = SgfWriter.write(games);
        assertEquals(saved, SgfWriter.write(SgfReader.read(saved)));

        assertTrue(history.undo());
        assertEquals(List.of(first, second), parent.children());
        assertTrue(history.redo());
        assertEquals(List.of(first, added, second), parent.children());
        assertSame(added, parent.children().get(1));
        assertEquals(saved, SgfWriter.write(games));
    }

    @Test void newEditClearsRedoAndHistoryIsPerDocument() {
        GameNode root = SgfReader.read("(;GM[1]SZ[9])").games().get(0).tree().root();
        CommandHistory history = new CommandHistory();
        GameNode a = new GameNode(new Move(0, 0, Stone.BLACK), root);
        history.execute(new AddNodeCommand(root, a));
        assertTrue(history.undo());
        assertTrue(history.canRedo());
        GameNode b = new GameNode(new Move(1, 1, Stone.BLACK), root);
        history.execute(new AddNodeCommand(root, b));
        assertFalse(history.canRedo());
        assertEquals(List.of(b), root.children());
        history.clear();
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test void undoRetainsDetachedSubtreeForRedo() {
        GameNode root = SgfReader.read("(;GM[1]SZ[9])").games().get(0).tree().root();
        GameNode child = new GameNode(new Move(0, 0, Stone.BLACK), root,
                List.of(new SgfProperty("B", List.of("aa"))));
        GameNode grandchild = new GameNode(new Move(1, 1, Stone.WHITE), child,
                List.of(new SgfProperty("W", List.of("bb"))));
        child.addChild(grandchild);
        CommandHistory history = new CommandHistory();
        history.execute(new AddNodeCommand(root, child));
        history.undo();
        assertTrue(root.children().isEmpty());
        history.redo();
        assertSame(grandchild, root.children().get(0).children().get(0));
    }
}
