package com.davidicius.dnc.oz.traits;

import com.tinkerpop.blueprints.Vertex;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Trait07 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait07.class);

    public String getId() {
        return "07";
    }

    public String getName() {
        return "SKeywords";
    }

    private static String[] BAD = {"webhosting", "domeny", "domena", "hosting", "serverhosting", "freehosting", "zdarma"};

    public boolean hasTrait(Vertex domain, String page) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;
        if (page == null) return false;

        Document doc = Jsoup.parse(page);
        if (doc == null) {
            log.warn("Cannot parse page for domain: " + domain.getProperty("name"));
            return false;
        }

        Elements c = doc.select("meta[name=keywords");
        for (Element e : c) {
            String keywords = e.attr("content");
            keywords = normalizePage(keywords);

            for (String bad : BAD) {
                if (keywords.contains(bad)) {
                    return true;
                }
            }
        }

        return false;
    }
}
