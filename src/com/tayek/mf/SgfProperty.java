package com.tayek.mf;

import java.util.List;

/** One property occurrence on an SGF node. Unknown properties are retained. */
public record SgfProperty(String id, List<String> values) {
    public SgfProperty {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("empty SGF property id");
        values = List.copyOf(values);
    }
}
