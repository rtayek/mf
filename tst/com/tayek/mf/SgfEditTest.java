package com.tayek.mf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

final class SgfEditTest {
    @Test void addingVariationSurvivesSaveAndRead() {
        GameCollection collection = SgfReader.read("(;GM[1]SZ[9];B[aa];W[bb])");
        GameNode root = collection.games().get(0).tree().root();
        GameNode first = root.children().get(0);
        GameNode existing = first.children().get(0);
        GameNode variation = first.addMove(new Move(2, 2, Stone.WHITE));

        assertSame(first, variation.parent());
        assertSame(existing, first.children().get(0));
        assertEquals(java.util.List.of("cc"), variation.property("W"));
        assertEquals(2, first.children().size());

        String saved = SgfWriter.write(collection);
        GameCollection restored = SgfReader.read(saved);
        GameNode restoredFirst = restored.games().get(0).tree().root().children().get(0);
        assertEquals(2, restoredFirst.children().size());
        assertEquals(java.util.List.of("bb"), restoredFirst.children().get(0).property("W"));
        assertEquals(java.util.List.of("cc"), restoredFirst.children().get(1).property("W"));
        assertEquals(saved, SgfWriter.write(restored));
    }
}
