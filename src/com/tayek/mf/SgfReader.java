package com.tayek.mf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SgfReader {
    private static final Pattern property = Pattern.compile("([A-Z]+)\\[((?:\\\\.|[^]])*)]");

    private SgfReader() { }

    public static SgfGame read(Path path) throws IOException {
        String sgf = Files.readString(path);
        String mainLine = mainLine(sgf);
        int size = intProperty(mainLine, "SZ", 19);
        String black = stringProperty(mainLine, "PB", "Black");
        String white = stringProperty(mainLine, "PW", "White");
        String name = stringProperty(mainLine, "GN", path.getFileName().toString());
        List<Move> moves = new ArrayList<>();

        Matcher matcher = property.matcher(mainLine);
        while (matcher.find()) {
            String id = matcher.group(1);
            if (!id.equals("B") && !id.equals("W")) continue;
            String value = unescape(matcher.group(2));
            Stone stone = id.equals("B") ? Stone.BLACK : Stone.WHITE;
            if (value.isEmpty()) {
                moves.add(new Move(-1, -1, stone));
            } else if (value.length() >= 2) {
                int x = value.charAt(0) - 'a';
                int y = value.charAt(1) - 'a';
                if (x >= 0 && x < size && y >= 0 && y < size) moves.add(new Move(x, y, stone));
            }
        }
        return new SgfGame(size, black, white, name, List.copyOf(moves));
    }

    // Keep the trunk and discard side variations for this first viewer slice.
    private static String mainLine(String sgf) {
        StringBuilder out = new StringBuilder();
        int depth = 0;
        boolean escaped = false;
        boolean inValue = false;
        for (int i = 0; i < sgf.length(); i++) {
            char c = sgf.charAt(i);
            if (inValue) {
                if (depth <= 1) out.append(c);
                if (escaped) escaped = false;
                else if (c == '\\') escaped = true;
                else if (c == ']') inValue = false;
                continue;
            }
            if (c == '[') {
                inValue = true;
                if (depth <= 1) out.append(c);
            } else if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (depth <= 1) {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static String stringProperty(String sgf, String id, String fallback) {
        Matcher m = Pattern.compile("(?:^|[;\\s])" + id + "\\[((?:\\\\.|[^]])*)]").matcher(sgf);
        return m.find() ? unescape(m.group(1)) : fallback;
    }

    private static int intProperty(String sgf, String id, int fallback) {
        try { return Integer.parseInt(stringProperty(sgf, id, Integer.toString(fallback))); }
        catch (NumberFormatException e) { return fallback; }
    }

    private static String unescape(String s) {
        return s.replace("\\]", "]").replace("\\\\", "\\");
    }
}
