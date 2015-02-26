package com.davidicius.dnc.oz.traits;

import com.tinkerpop.blueprints.Vertex;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Trait08 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait08.class);

    public String getId() {
        return "08";
    }

    public String getName() {
        return "Ads";
    }

    public boolean hasTrait(Vertex domain, String page) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;
        if (page == null) return false;

        String pp = normalizePage(page);
        if (pp.contains("googletag.pubads()")) {
            return true;
        }

        // Hodne silna podminka!!! @todo
        if (pp.contains("hit.gemius.pl")) {
            return true;
        }

        Document doc = Jsoup.parse(page);
        if (doc == null) {
            log.warn("Cannot parse page for domain: " + domain.getProperty("name"));
            return false;
        }

        //@todo clubskoda.cz.... maji tam vlastn9 reklamu :( hledej banner...
        Elements c = doc.select("script");
        for (Element e : c) {
            String src = e.attr("src").toLowerCase();

            if (src.contains("ad.czechia.com/js")) {
                return true;
            }

//            if (src.contains("pagead2.googlesyndication.com/pagead/show_ads.js")) {
//                return true;
//            }

            if (src.contains("pagead2.googlesyndication.com")) {
                return true;
            }

            if (src.contains("i.imedia.cz/js")) {
                return true;
            }

//            if (src.contains("www.googleadservices.com")) {
//                return true;
//            }

            if (src.contains("ad2.billboard.cz")) {
                return true;
            }

            if (src.contains("ibillboard.com")) {
                return true;
            }

            if (src.contains("go.cz.bbelements.com")) {
                return true;
            }
        }

        c = doc.select("a");
        for (Element e : c) {
            String src = e.attr("href").toLowerCase();

            if (src.contains("hit.gemius.pl/hitredir")) {
                return true;
            }

            if (src.contains("ad2.billboard.cz")) {
                return true;
            }

            if (src.contains("go.cz.bbelements.com")) {
                return true;
            }

            if (src.contains("adclick.g.doubleclick.net")) {
                return true;
            }

            // todo - prilis silna podminka kvuli skoda/nahradni/dily
//            if (src.contains("banner")) {
//                return true;
//            }

        }

        c = doc.select("img");
        for (Element e : c) {
            String src = e.attr("src").toLowerCase();

            if (src.contains("tracking.affiliateclub.cz")) {
                return true;
            }
        }

//        c = doc.select("input");
//        for (Element e : c) {
//            String src = e.attr("value").toLowerCase();
//
//            if (src.contains("search ads")) {
//                return true;
//            }
//        }

        return false;
    }
}
