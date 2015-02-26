package com.davidicius.dnc.oz.traits;

import com.tinkerpop.blueprints.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class Trait03 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait03.class);

    public String getId() {
        return "03";
    }

    public String getName() {
        return "Size";
    }

    public boolean hasTrait(Vertex domain, String page) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;
        if (page == null) return false;

        return page.length() < 5*1000;
    }
}
