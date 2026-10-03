package com.tayek.mf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GameNode {
    private final Move move;
    private final GameNode parent;
    private final List<GameNode> children = new ArrayList<>();

    public GameNode(Move move, GameNode parent) {
        this.move = move;
        this.parent = parent;
    }

    public Move move() { return move; }
    public GameNode parent() { return parent; }
    public List<GameNode> children() { return Collections.unmodifiableList(children); }
    public void addChild(GameNode child) { children.add(child); }
}
