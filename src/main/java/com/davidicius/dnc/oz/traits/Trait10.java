package com.davidicius.dnc.oz.traits;

import com.tinkerpop.blueprints.Vertex;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class  Trait10 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait10.class);

    public String getId() {
        return "10";
    }

    public String getName() {
        return "Frames";
    }

    public boolean hasTrait(Vertex domain, String page) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;
        if (page == null) return false;

        Document doc = Jsoup.parse(page);
        if (doc == null) {
            log.warn("Cannot parse page for domain: " + domain.getProperty("name"));
            return false;
        }

        Elements c = doc.select("frame");
        return c.size() >= 2;
    }
}
