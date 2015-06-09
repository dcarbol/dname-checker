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

public class Trait07 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait07.class);

    public String getId() {
        return "07";
    }

    public String getName() {
        return "SKeywords";
    }

    private static String[] BAD = {"webhosting", "domeny", "domena", "hosting", "serverhosting", "freehosting"};

    public boolean hasTrait(Vertex domain, String page, Document document, OZ oz) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;
        if (page == null || document == null) return false;

        Elements c = document.select("meta[name=keywords]");
        for (Element e : c) {
            String keywords = e.attr("content");
            keywords = normalizePage(keywords);

            for (String bad : BAD) {
                if (keywords.contains(bad)) {
                    if (TraitsFactory.INSTANCE.isVERBOSE()) {
                        log.info(String.format("Domain %s, trait %s: Keywords '%s' contains suspicious keyword '%s''", domain.getProperty("name"), getName(), keywords, bad));
                    }

                    return true;
                }
            }
        }

        return false;
    }
}
