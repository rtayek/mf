package com.tayek.mf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SgfReaderTest {
    @Test
    void readsAnOrderedMixedGameCollection(@TempDir Path directory) throws IOException {
        GameCollection collection = read(directory,
                "(;GM[1]SZ[9]GN[First];B[aa])"
                + "(;GM[3]SZ[8]GN[Chess])"
                + "(;GM[1]SZ[13]GN[Second];B[bb])");

        assertEquals(List.of("First", "Chess", "Second"),
                collection.games().stream().map(Game::name).toList());
        assertEquals(List.of("First", "Second"),
                collection.gamesOfType(GameType.GO).stream().map(Game::name).toList());
        assertEquals(List.of(new GameType(1), new GameType(3)),
                List.copyOf(collection.groupedByType().keySet()));
        assertFalse(collection.isHomogeneous());
        assertTrue(collection.commonType().isEmpty());
    }

    @Test
    void usesTheFirstSgfNodeAsTheTreeRoot(@TempDir Path directory) throws IOException {
        Game game = read(directory, "(;GM[1]SZ[9]B[aa];W[bb])").games().get(0);
        GameNode root = game.tree().root();

        assertNull(root.parent());
        assertEquals(new Move(0, 0, Stone.BLACK), root.move());
        assertEquals(new Move(1, 1, Stone.WHITE), root.children().get(0).move());
        assertSame(root, root.children().get(0).parent());
        assertEquals(List.of(root.move(), root.children().get(0).move()),
                game.tree().mainLineMoves());
    }

    @Test
    void preservesOrderedVariationsOnTheMultiWayTree(@TempDir Path directory) throws IOException {
        Game game = read(directory,
                "(;GM[1]SZ[9];B[aa](;W[bb])(;W[cc])(;W[dd]))").games().get(0);
        GameNode blackMove = game.tree().root().children().get(0);

        assertEquals(List.of(
                new Move(1, 1, Stone.WHITE),
                new Move(2, 2, Stone.WHITE),
                new Move(3, 3, Stone.WHITE)),
                blackMove.children().stream().map(GameNode::move).toList());
    }

    @Test
    void recognizesAHomogeneousCollection(@TempDir Path directory) throws IOException {
        GameCollection collection = read(directory, "(;GM[1])(;GM[1])");

        assertTrue(collection.isHomogeneous());
        assertEquals(GameType.GO, collection.commonType().orElseThrow());
    }

    private GameCollection read(Path directory, String contents) throws IOException {
        Path file = directory.resolve("games.sgf");
        Files.writeString(file, contents);
        return SgfReader.read(file);
    }
}
