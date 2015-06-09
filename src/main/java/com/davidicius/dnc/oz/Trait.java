package com.davidicius.dnc.oz;

import com.tinkerpop.blueprints.Vertex;
import org.jsoup.nodes.Document;

public interface Trait {
    String getId();
    String getName();
    boolean hasTrait(Vertex domain, String page, Document document, OZ oz);
}
