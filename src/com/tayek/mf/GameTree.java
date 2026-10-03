package com.tayek.mf;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record GameTree(GameNode root) {
    public GameTree {
        Objects.requireNonNull(root, "root");
        if (root.parent() != null) {
            throw new IllegalArgumentException("tree root must not have a parent");
        }
    }

    public List<Move> mainLineMoves() {
        List<Move> moves = new ArrayList<>();
        GameNode node = root;
        while (node != null) {
            if (node.move() != null) {
                moves.add(node.move());
            }
            node = node.children().isEmpty() ? null : node.children().get(0);
        }
        return List.copyOf(moves);
    }
}
