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

public class Trait06 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait06.class);

    public String getId() {
        return "06";
    }

    public String getName() {
        return "GKeywords";
    }

    public boolean hasTrait(Vertex domain, String page, Document document, OZ oz) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;
        if (page == null || document == null) return false;
        if (oz == null) {
            log.warn(String.format("Domain %s, trait %s: Domain has no valid OZ'", domain.getProperty("name"), getName()));
            return false;
        }

        Elements c = document.select("meta[name=keywords]");
        for (Element e : c) {
            String keywords = e.attr("content");
            keywords = normalizePage(keywords);

            // @todo klicova slova svayana s OZ
            // treba skoda/auto ma: auto,akce,citigo,fabia,octavia,rapid,roomster,superb,yeti,autosalon,konfigurátor,prodej,servis,škoda,vozy,vůz
            if (keywords.contains(oz.getName())) {
                if (TraitsFactory.INSTANCE.isVERBOSE()) {
                    log.info(String.format("Domain %s, trait %s: Keywords '%s' contains OZ '%s''", domain.getProperty("name"), getName(), keywords, oz.getName()));
                }

                return true;
            }
        }

        return false;
    }
}
