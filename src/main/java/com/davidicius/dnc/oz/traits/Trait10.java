package com.davidicius.dnc.oz.traits;

import com.davidicius.dnc.oz.OZ;
import com.davidicius.dnc.oz.TraitsFactory;
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

    public boolean hasTrait(Vertex domain, String page, Document document, OZ oz) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;
        if (page == null || document == null) return false;

        Elements c = document.select("frame");
        if (c.size() >= 2) {
            if (TraitsFactory.INSTANCE.isVERBOSE()) {
                log.info(String.format("Domain %s, trait %s: Page contains >= 2 frames.", domain.getProperty("name"), getName()));
            }

            return true;
        } else {
            return false;
        }
    }
}
