package com.tayek.mf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GameNode {
    private final Move move;
    private final GameNode parent;
    private final Map<String,List<String>> properties;
    private final List<GameNode> children = new ArrayList<>();

    public GameNode(Move move, GameNode parent) {
        this(move,parent,Map.of());
    }

    public GameNode(Move move, GameNode parent, Map<String,List<String>> properties) {
        this.move=move;
        this.parent=parent;
        LinkedHashMap<String,List<String>> copy=new LinkedHashMap<>();
        properties.forEach((key,value)->copy.put(key,List.copyOf(value)));
        this.properties=Collections.unmodifiableMap(copy);
    }

    public Move move() { return move; }
    public GameNode parent() { return parent; }
    public Map<String,List<String>> properties() { return properties; }
    public List<String> property(String id) { return properties.getOrDefault(id,List.of()); }
    public List<GameNode> children() { return Collections.unmodifiableList(children); }
    public void addChild(GameNode child) { children.add(child); }
}
