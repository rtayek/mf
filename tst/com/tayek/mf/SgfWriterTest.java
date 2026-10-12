package com.tayek.mf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class SgfWriterTest {
    @Test void roundTripRtgoFixtures() throws Exception {
        for (var entry : SgfFixtures.valid().entrySet()) {
            GameCollection original = SgfReader.read(entry.getValue());
            String serialized = SgfWriter.write(original);
            GameCollection restored = SgfReader.read(serialized);
            assertCollectionsEqual(original, restored, entry.getKey());
        }
    }

    @Test void writerIsByteStableAcrossAllFixtures() {
        for (var entry : SgfFixtures.valid().entrySet()) {
            String first = SgfWriter.write(SgfReader.read(entry.getValue()));
            String second = SgfWriter.write(SgfReader.read(first));
            assertEquals(first, second, entry.getKey() + " second serialization");
            assertTrue(java.util.Arrays.equals(
                    first.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    second.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                    entry.getKey() + " UTF-8 bytes");
        }
    }

    /**
     * Diagnostic only: compare original text with the serialized text.
     * Ignore whitespace only outside property values. Do not fail the build.
     * View output with: ./gradlew clean test --info
     */
    @Test void reportTextualRoundTrips() throws Exception {
        int exact = 0, whitespaceOnly = 0, different = 0;
        for (var entry : SgfFixtures.valid().entrySet()) {
            String source = entry.getValue();
            String written = SgfWriter.write(SgfReader.read(source));
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

    @Test void longGameIsWrappedAndStable() {
        StringBuilder input = new StringBuilder("(;GM[1]");
        for (int i = 0; i < 30; i++) input.append(";B[aa]");
        input.append(')');
        String formatted = SgfWriter.write(SgfReader.read(input.toString()));
        assertTrue(formatted.contains("\n"));
        assertEquals(outsideWhitespaceRemoved(input.toString()), outsideWhitespaceRemoved(formatted));
        assertEquals(formatted, SgfWriter.write(SgfReader.read(formatted)));
    }

    @Test void collectionGamesAreSeparated() {
        assertEquals("(;GM[1])\n(;GM[4])",
                SgfWriter.write(SgfReader.read("(;GM[1])(;GM[4])")));
    }

    @Test void realWorldSgfFilesRoundTripAndStabilize() throws Exception {
        for (String name : java.util.List.of(
                "39016682-086-DonJuan-rtayek.sgf",
                "smart-go-1.sgf")) {
            String resource = "/sgf/" + name;
            String sgf;
            try (var stream = SgfWriterTest.class.getResourceAsStream(resource)) {
                assertTrue(stream != null, "missing test resource " + resource);
                sgf = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
            GameCollection original = SgfReader.read(sgf);
            String first = SgfWriter.write(original);
            GameCollection restored = SgfReader.read(first);
            assertCollectionsEqual(original, restored, name);
            String second = SgfWriter.write(restored);
            assertEquals(first, second, name + " second serialization");
            assertTrue(java.util.Arrays.equals(
                    first.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    second.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                    name + " UTF-8 bytes");
        }
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
