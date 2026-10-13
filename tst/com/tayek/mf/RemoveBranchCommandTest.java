package com.tayek.mf;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

final class RemoveBranchCommandTest {
    @Test void deletionAndUndoRestoreOriginalSubtreeAndVariationOrder() {
        GameCollection collection = SgfReader.read("(;GM[1]SZ[9];B[aa](;W[bb];B[bc])(;W[cc])(;W[dd]))");
        GameNode parent = collection.games().get(0).tree().root().children().get(0);
        GameNode first = parent.children().get(0);
        GameNode descendant = first.children().get(0);
        GameNode second = parent.children().get(1);
        GameNode third = parent.children().get(2);
        String original = SgfWriter.write(collection);
        CommandHistory history = new CommandHistory();
        history.execute(new RemoveBranchCommand(first));
        assertEquals(List.of(second, third), parent.children());
        assertSame(descendant, first.children().get(0));
        assertFalse(SgfWriter.write(collection).equals(original));

        assertTrue(history.undo());
        assertEquals(List.of(first, second, third), parent.children());
        assertSame(descendant, parent.children().get(0).children().get(0));
        assertEquals(original, SgfWriter.write(collection));

        assertTrue(history.redo());
        assertEquals(List.of(second, third), parent.children());
        assertTrue(history.undo());
        assertEquals(original, SgfWriter.write(collection));
    }

    @Test void rootCannotBeDeleted() {
        GameNode root = SgfReader.read("(;GM[1]SZ[9])").games().get(0).tree().root();
        assertThrows(NullPointerException.class, () -> new RemoveBranchCommand(root));
    }

    @Test void newEditAfterUndoClearsRedo() {
        GameNode root = SgfReader.read("(;GM[1]SZ[9];B[aa];W[bb])").games().get(0).tree().root();
        GameNode first = root.children().get(0);
        CommandHistory history = new CommandHistory();
        history.execute(new RemoveBranchCommand(first));
        history.undo();
        history.execute(new RemoveBranchCommand(first.children().get(0)));
        assertFalse(history.canRedo());
    }
}
