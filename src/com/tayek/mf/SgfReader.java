package com.tayek.mf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SgfReader {
    private SgfReader() { }

    public static GameCollection read(Path path) throws IOException {
        Parser parser = new Parser(Files.readString(path));
        List<ParsedNode> roots = parser.parseCollection();
        List<Game> games = new ArrayList<>();
        for (ParsedNode root : roots) {
            int size = intProperty(root, "SZ", 19);
            GameNode gameRoot = toGameNode(root, null, size);
            games.add(new Game(
                    new GameType(intProperty(root, "GM", GameType.GO.sgfNumber())),
                    size,
                    stringProperty(root, "PB", "Black"),
                    stringProperty(root, "PW", "White"),
                    stringProperty(root, "GN", path.getFileName().toString()),
                    new GameTree(gameRoot)));
        }
        return new GameCollection(games);
    }

    private static GameNode toGameNode(ParsedNode parsed, GameNode parent, int size) {
        GameNode node = new GameNode(move(parsed, size), parent);
        for (ParsedNode child : parsed.children) {
            node.addChild(toGameNode(child, node, size));
        }
        return node;
    }

    private static Move move(ParsedNode node, int size) {
        for (String id : List.of("B", "W")) {
            List<String> values = node.properties.get(id);
            if (values == null || values.isEmpty()) {
                continue;
            }
            Stone stone = id.equals("B") ? Stone.BLACK : Stone.WHITE;
            String value = values.get(0);
            if (value.isEmpty()) {
                return new Move(-1, -1, stone);
            }
            if (value.length() >= 2) {
                int x = value.charAt(0) - 'a';
                int y = value.charAt(1) - 'a';
                if (x >= 0 && x < size && y >= 0 && y < size) {
                    return new Move(x, y, stone);
                }
            }
        }
        return null;
    }

    private static String stringProperty(ParsedNode node, String id, String fallback) {
        List<String> values = node.properties.get(id);
        return values == null || values.isEmpty() ? fallback : values.get(0);
    }

    private static int intProperty(ParsedNode node, String id, int fallback) {
        try {
            return Integer.parseInt(stringProperty(node, id, Integer.toString(fallback)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static final class Parser {
        private Parser(String source) {
            this.source = source;
        }

        private List<ParsedNode> parseCollection() {
            List<ParsedNode> roots = new ArrayList<>();
            skipWhitespace();
            while (index < source.length()) {
                if (source.charAt(index) != '(') {
                    throw error("expected '('");
                }
                roots.add(parseTree());
                skipWhitespace();
            }
            if (roots.isEmpty()) {
                throw error("SGF collection contains no games");
            }
            return roots;
        }

        private ParsedNode parseTree() {
            expect('(');
            skipWhitespace();
            if (index >= source.length() || source.charAt(index) != ';') {
                throw error("game tree contains no nodes");
            }
            ParsedNode root = parseNode();
            ParsedNode current = root;
            skipWhitespace();
            while (index < source.length() && source.charAt(index) == ';') {
                ParsedNode child = parseNode();
                current.children.add(child);
                current = child;
                skipWhitespace();
            }
            while (index < source.length() && source.charAt(index) == '(') {
                current.children.add(parseTree());
                skipWhitespace();
            }
            expect(')');
            return root;
        }

        private ParsedNode parseNode() {
            expect(';');
            ParsedNode node = new ParsedNode();
            skipWhitespace();
            while (index < source.length() && Character.isUpperCase(source.charAt(index))) {
                String id = propertyIdentifier();
                skipWhitespace();
                List<String> values = new ArrayList<>();
                while (index < source.length() && source.charAt(index) == '[') {
                    values.add(propertyValue());
                    skipWhitespace();
                }
                if (values.isEmpty()) {
                    throw error("property " + id + " has no value");
                }
                node.properties.put(id, List.copyOf(values));
            }
            return node;
        }

        private String propertyIdentifier() {
            int start = index;
            while (index < source.length() && Character.isUpperCase(source.charAt(index))) {
                index++;
            }
            return source.substring(start, index);
        }

        private String propertyValue() {
            expect('[');
            StringBuilder value = new StringBuilder();
            boolean escaped = false;
            while (index < source.length()) {
                char character = source.charAt(index++);
                if (escaped) {
                    value.append(character);
                    escaped = false;
                } else if (character == '\\') {
                    escaped = true;
                } else if (character == ']') {
                    return value.toString();
                } else {
                    value.append(character);
                }
            }
            throw error("unterminated property value");
        }

        private void expect(char expected) {
            if (index >= source.length() || source.charAt(index) != expected) {
                throw error("expected '" + expected + "'");
            }
            index++;
        }

        private void skipWhitespace() {
            while (index < source.length() && Character.isWhitespace(source.charAt(index))) {
                index++;
            }
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " at character " + index);
        }

        private final String source;
        private int index;
    }

    private static final class ParsedNode {
        private final Map<String, List<String>> properties = new LinkedHashMap<>();
        private final List<ParsedNode> children = new ArrayList<>();
    }
}
