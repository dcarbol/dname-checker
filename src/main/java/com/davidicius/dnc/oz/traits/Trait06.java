package com.davidicius.dnc.oz.traits;

import com.tinkerpop.blueprints.Vertex;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Trait06 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait06.class);

    public String getId() {
        return "06";
    }

    public String getName() {
        return "GKeywords";
    }

    public boolean hasTrait(Vertex domain, String page) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;
        if (page == null) return false;

//        if (pattern01.matcher(page.toLowerCase()).find()) {
//            return true;
//        }

        Document doc = Jsoup.parse(page);
        if (doc == null) {
            log.warn("Cannot parse page for domain: " + domain.getProperty("name"));
            return false;
        }

        Elements c = doc.select("meta[name=keywords");
        for (Element e : c) {
            String keywords = e.attr("content");
            keywords = normalizePage(keywords);

            // @todo klicova slova svayana s OZ
            // treba skoda/auto ma: auto,akce,citigo,fabia,octavia,rapid,roomster,superb,yeti,autosalon,konfigurátor,prodej,servis,škoda,vozy,vůz
            if (keywords.contains("skoda")) {
                return true;
            }
        }

        return false;
    }
}
