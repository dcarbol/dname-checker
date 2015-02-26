package com.davidicius.dnc.oz.traits;

import com.tinkerpop.blueprints.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Trait00 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait00.class);

    public String getId() {
        return "00";
    }

    public String getName() {
        return "NoPage";
    }

    public boolean hasTrait(Vertex domain, String page) {
        if (!forceExists(domain)) return false;

        String loaded = domain.getProperty("loaded");
        if (loaded == null || loaded.equals("?")) {
            log.warn(String.format("Trait '%s', domain '%s': 'loaded' property must not be ?", domain.getProperty("name"), getName() ));
            return false;
        }

        if (page != null && page.trim().equals("")) {
            return true;
        }

        return loaded.equals("E");
    }
}
