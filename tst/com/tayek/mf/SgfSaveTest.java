package com.tayek.mf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class SgfSaveTest {
    @TempDir Path directory;

    @Test void saveAsWritesWholeCollectionAndReadsBack() throws Exception {
        String sgf = "(;GM[1]C[first])(;GM[4]C[second])";
        GameCollection original = SgfReader.read(sgf);
        Path destination = directory.resolve("saved.sgf");

        SgfSave.saveAs(original, destination);

        assertEquals(SgfWriter.write(original), Files.readString(destination, StandardCharsets.UTF_8));
        GameCollection restored = SgfReader.read(destination);
        assertEquals(2, restored.games().size());
        assertEquals("first", restored.games().get(0).tree().root().property("C").get(0));
        assertEquals("second", restored.games().get(1).tree().root().property("C").get(0));
    }

    @Test void saveAsReportsIoFailure() {
        Path missing = directory.resolve("missing").resolve("saved.sgf");
        assertThrows(java.io.IOException.class,
                () -> SgfSave.saveAs(SgfReader.read("(;GM[1])"), missing));
        assertTrue(Files.notExists(missing));
    }
}
