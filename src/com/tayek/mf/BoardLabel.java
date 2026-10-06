package com.tayek.mf;

public record BoardLabel(int x, int y, String text, Kind kind, int variationIndex) {
    public enum Kind { ANNOTATION, VARIATION }

    public BoardLabel(int x, int y, String text) {
        this(x, y, text, Kind.ANNOTATION, -1);
    }

    public static BoardLabel variation(int x, int y, String text, int variationIndex) {
        return new BoardLabel(x, y, text, Kind.VARIATION, variationIndex);
    }

    public boolean isVariation() {
        return kind == Kind.VARIATION;
    }
}
