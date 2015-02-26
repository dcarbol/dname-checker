package com.davidicius.dnc.oz;

import com.tinkerpop.blueprints.Vertex;

public interface Trait {
    String getId();
    String getName();
    boolean hasTrait(Vertex domain, String page);
}
