package com.tayek.mf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Writes the original SGF node properties, including unknown properties and variations. */
public final class SgfWriter {
    private SgfWriter() { }

    public static String write(GameCollection collection) {
        Objects.requireNonNull(collection, "collection");
        StringBuilder out = new StringBuilder();
        for (Game game : collection.games()) writeTree(out, game.tree().root());
        return out.toString();
    }

    public static void write(GameCollection collection, Path path) throws IOException {
        Files.writeString(path, write(collection), StandardCharsets.UTF_8);
    }

    private static void writeTree(StringBuilder out, GameNode start) {
        out.append('(');
        GameNode node = start;
        while (true) {
            writeNode(out, node);
            List<GameNode> children = node.children();
            if (children.size() == 1) {
                node = children.get(0);
            } else {
                for (GameNode child : children) writeTree(out, child);
                break;
            }
        }
        out.append(')');
    }

    private static void writeNode(StringBuilder out, GameNode node) {
        out.append(';');
        for (SgfProperty property : node.properties()) {
            out.append(property.id());
            for (String value : property.values()) {
                out.append('[');
                for (int i = 0; i < value.length(); i++) {
                    char c = value.charAt(i);
                    if (c == '\\' || c == ']') out.append('\\');
                    out.append(c);
                }
                out.append(']');
            }
        }
    }
}
