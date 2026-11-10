package com.tayek.mf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Small JSON reader for the line-delimited KataGo protocol. */
final class KataGoJson {
    static Object parse(String text) {
        KataGoJson reader = new KataGoJson(text);
        Object value = reader.value();
        reader.space();
        if (reader.index != text.length()) throw reader.error("trailing input");
        return value;
    }

    private KataGoJson(String text) { this.text = text; }

    private Object value() {
        space();
        char c = peek();
        if (c == '{') return object();
        if (c == '[') return array();
        if (c == '"') return string();
        if (c == 't') { literal("true"); return Boolean.TRUE; }
        if (c == 'f') { literal("false"); return Boolean.FALSE; }
        if (c == 'n') { literal("null"); return null; }
        return number();
    }

    private Map<String, Object> object() {
        expect('{');
        Map<String, Object> map = new LinkedHashMap<>();
        space();
        if (take('}')) return map;
        do {
            space();
            if (peek() != '"') throw error("expected object key");
            String key = string();
            space();
            expect(':');
            map.put(key, value());
            space();
            if (take('}')) return map;
            expect(',');
        } while (true);
    }

    private List<Object> array() {
        expect('[');
        List<Object> values = new ArrayList<>();
        space();
        if (take(']')) return values;
        do {
            values.add(value());
            space();
            if (take(']')) return values;
            expect(',');
        } while (true);
    }

    private String string() {
        expect('"');
        StringBuilder out = new StringBuilder();
        while (index < text.length()) {
            char c = text.charAt(index++);
            if (c == '"') return out.toString();
            if (c != '\\') { if (c < 32) throw error("control character"); out.append(c); continue; }
            if (index == text.length()) throw error("incomplete escape");
            char escaped = text.charAt(index++);
            switch (escaped) {
                case '"', '\\', '/' -> out.append(escaped);
                case 'b' -> out.append('\b');
                case 'f' -> out.append('\f');
                case 'n' -> out.append('\n');
                case 'r' -> out.append('\r');
                case 't' -> out.append('\t');
                case 'u' -> {
                    if (index + 4 > text.length()) throw error("incomplete unicode escape");
                    try { out.append((char) Integer.parseInt(text.substring(index, index + 4), 16)); }
                    catch (NumberFormatException ex) { throw error("invalid unicode escape"); }
                    index += 4;
                }
                default -> throw error("invalid escape");
            }
        }
        throw error("unterminated string");
    }

    private Number number() {
        int begin = index;
        if (take('-')) { }
        digits();
        if (take('.')) digits();
        if (take('e') || take('E')) {
            if (!take('+')) take('-');
            digits();
        }
        if (begin == index) throw error("expected value");
        try { return Double.valueOf(text.substring(begin, index)); }
        catch (NumberFormatException ex) { throw error("invalid number"); }
    }

    private void digits() {
        int start = index;
        while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
        if (start == index) throw error("expected digit");
    }

    private void literal(String target) {
        if (!text.startsWith(target, index)) throw error("expected " + target);
        index += target.length();
    }

    private void expect(char c) { space(); if (!take(c)) throw error("expected '" + c + "'"); }
    private boolean take(char c) { if (index < text.length() && text.charAt(index) == c) { index++; return true; } return false; }
    private char peek() { return index < text.length() ? text.charAt(index) : '\0'; }
    private void space() { while (index < text.length() && Character.isWhitespace(text.charAt(index))) index++; }
    private IllegalArgumentException error(String message) { return new IllegalArgumentException(message + " at character " + index); }

    private final String text;
    private int index;
}
