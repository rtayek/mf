package com.tayek.mf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class SgfWriterTest {
    @TempDir Path directory;

    @Test void roundTripRtgoFixtures() throws Exception {
        for (var entry : SgfFixtures.valid().entrySet()) {
            Path input = directory.resolve("input.sgf");
            Path output = directory.resolve("output.sgf");
            Files.writeString(input, entry.getValue());
            GameCollection original = SgfReader.read(input);
            SgfWriter.write(original, output);
            GameCollection restored = SgfReader.read(output);
            assertCollectionsEqual(original, restored, entry.getKey());
        }
    }

    @Test void writerDoesNotChangeTreeOnRepeatedRoundTrip() throws Exception {
        Path file = directory.resolve("test.sgf");
        Files.writeString(file, SgfFixtures.valid().get("variationOfAVariation"));
        GameCollection original = SgfReader.read(file);
        String first = SgfWriter.write(original);
        Files.writeString(file, first);
        String second = SgfWriter.write(SgfReader.read(file));
        assertEquals(first, second);
    }

    /**
     * Diagnostic only: compare original text with the serialized text.
     * Ignore whitespace only outside property values. Do not fail the build.
     * View output with: ./gradlew test --info
     */
    @Test void reportTextualRoundTrips() throws Exception {
        int exact = 0, whitespaceOnly = 0, different = 0;
        for (var entry : SgfFixtures.valid().entrySet()) {
            Path input = directory.resolve("diagnostic.sgf");
            String source = entry.getValue();
            Files.writeString(input, source);
            String written = SgfWriter.write(SgfReader.read(input));
            if (source.equals(written)) {
                exact++;
            } else if (outsideWhitespaceRemoved(source).equals(outsideWhitespaceRemoved(written))) {
                whitespaceOnly++;
            } else {
                different++;
                System.out.println("TEXT DIFFERENCE: " + entry.getKey());
            }
        }
        System.out.printf("SGF text round-trip: %d fixtures, %d exact, %d whitespace-only, %d different%n",
                exact + whitespaceOnly + different, exact, whitespaceOnly, different);
    }

    /** Removes whitespace between SGF tokens, preserving property value contents and escapes. */
    private static String outsideWhitespaceRemoved(String source) {
        StringBuilder normalized = new StringBuilder();
        boolean insideValue = false, escaped = false;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (insideValue) {
                normalized.append(c);
                if (escaped) escaped = false;
                else if (c == '\\') escaped = true;
                else if (c == ']') insideValue = false;
            } else if (c == '[') {
                insideValue = true;
                normalized.append(c);
            } else if (!Character.isWhitespace(c)) {
                normalized.append(c);
            }
        }
        return normalized.toString();
    }

    private static void assertCollectionsEqual(GameCollection expected, GameCollection actual, String name) {
        assertEquals(expected.games().size(), actual.games().size(), name + " game count");
        for (int i = 0; i < expected.games().size(); i++) {
            Game a = expected.games().get(i), b = actual.games().get(i);
            assertEquals(a.type(), b.type(), name + " type");
            assertEquals(a.boardSize(), b.boardSize(), name + " size");
            assertEquals(a.blackPlayer(), b.blackPlayer(), name + " black");
            assertEquals(a.whitePlayer(), b.whitePlayer(), name + " white");
            assertNodeEqual(a.tree().root(), b.tree().root(), name + " game " + i);
        }
    }

    private static void assertNodeEqual(GameNode expected, GameNode actual, String location) {
        assertEquals(expected.properties(), actual.properties(), location + " properties");
        assertEquals(expected.move(), actual.move(), location + " move");
        assertEquals(expected.children().size(), actual.children().size(), location + " child count");
        for (int i = 0; i < expected.children().size(); i++)
            assertNodeEqual(expected.children().get(i), actual.children().get(i), location + "/" + i);
    }
}
