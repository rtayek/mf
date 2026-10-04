package com.tayek.mf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class GameNode {
    private final Move move;
    private final GameNode parent;
    private final List<SgfProperty> properties;
    private final List<GameNode> children = new ArrayList<>();

    public GameNode(Move move, GameNode parent) {
        this(move, parent, List.of());
    }

    public GameNode(Move move, GameNode parent, List<SgfProperty> properties) {
        this.move = move;
        this.parent = parent;
        this.properties = List.copyOf(properties);
    }

    public Move move() { return move; }
    public GameNode parent() { return parent; }
    public List<SgfProperty> properties() { return properties; }

    /** All values of properties with this identifier, in document order. */
    public List<String> property(String id) {
        return properties.stream()
                .filter(property -> property.id().equals(id))
                .flatMap(property -> property.values().stream())
                .toList();
    }

    /** Compatibility view for code that wants one entry per property id. */
    public Map<String,List<String>> propertyMap() {
        return properties.stream().collect(Collectors.toMap(
                SgfProperty::id,
                SgfProperty::values,
                (left, right) -> {
                    List<String> values = new ArrayList<>(left);
                    values.addAll(right);
                    return List.copyOf(values);
                },
                java.util.LinkedHashMap::new));
    }

    public List<GameNode> children() { return Collections.unmodifiableList(children); }
    public void addChild(GameNode child) { children.add(child); }
}
