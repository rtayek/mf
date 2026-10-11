package com.tayek.mf;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/** File operation behind Save As, independent of JavaFX dialogs. */
public final class SgfSave {
    private SgfSave() { }

    public static void saveAs(GameCollection collection, Path destination) throws IOException {
        Objects.requireNonNull(collection, "collection");
        Objects.requireNonNull(destination, "destination");
        SgfWriter.write(collection, destination);
    }
}
