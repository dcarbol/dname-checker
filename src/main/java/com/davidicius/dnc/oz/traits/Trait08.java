package com.davidicius.dnc.oz.traits;

import com.davidicius.dnc.oz.OZ;
import com.davidicius.dnc.oz.TraitsFactory;
import com.tinkerpop.blueprints.Vertex;
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

    private static String[] pageContains = {"googletag.pubads()", "hit.gemius.pl"};
    private static String[] scriptContains = {"ad.czechia.com/js", "pagead2.googlesyndication.com", "i.imedia.cz/js", "ad2.billboard.cz", "ibillboard.com",
            "go.cz.bbelements.com", "google.com/adsense/domains/caf.js",
    "http://c.imedia.cz/js", "out.sklik.cz/js/script.js", "s.adexpert.cz"};

    private static String[] hrefContains = {"hit.gemius.pl/hitredir", "ad2.billboard.cz", "go.cz.bbelements.com", "adclick.g.doubleclick.net", "c.imedia.cz/click", "tracking.espoluprace.cz", "api.nejprovize.cz", "dpbolvw.net/click"};
    private static String[] imgContains = {"tracking.affiliateclub.cz", "tracking.espoluprace.cz"};
    private static String[] objectContains = {"pap.onioprovize.cz"};

    public boolean hasTrait(Vertex domain, String page, Document document, OZ oz) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;
        if (page == null || document == null) return false;

        int ads = 0;
        for (String s : pageContains) {
            if (page.contains(s)) {
                if (TraitsFactory.INSTANCE.isVERBOSE()) {
                    log.info(String.format("Domain %s, trait %s: Page contains Ads '%s''", domain.getProperty("name"), getName(), s));
                }

                ads++;
            }
        }

        //@todo clubskoda.cz.... maji tam vlastn9 reklamu :( hledej banner...
        Elements c = document.select("script");
        for (Element e : c) {
            String src = e.attr("src").toLowerCase();

            for (String s : scriptContains) {
                if (src.contains(s)) {
                    if (TraitsFactory.INSTANCE.isVERBOSE()) {
                        log.info(String.format("Domain %s, trait %s: Page contains script Ads '%s''", domain.getProperty("name"), getName(), s));
                    }

                    ads++;
                }
            }
//            if (src.contains("pagead2.googlesyndication.com/pagead/show_ads.js")) {
//                return true;
//            }

//            if (src.contains("www.googleadservices.com")) {
//                return true;
//            }
        }

        c = document.select("a");
        for (Element e : c) {
            String src = e.attr("href").toLowerCase();

            for (String s : hrefContains) {
                if (src.contains(s)) {
                    if (TraitsFactory.INSTANCE.isVERBOSE()) {
                        log.info(String.format("Domain %s, trait %s: Page contains href Ads '%s''", domain.getProperty("name"), getName(), s));
                    }

                    ads++;
                }
            }


            // todo - prilis silna podminka kvuli skoda/nahradni/dily
//            if (src.contains("banner")) {
//                return true;
//            }

        }

        c = document.select("img");
        for (Element e : c) {
            String src = e.attr("src").toLowerCase();

            for (String s : imgContains) {
                if (src.contains(s)) {
                    if (TraitsFactory.INSTANCE.isVERBOSE()) {
                        log.info(String.format("Domain %s, trait %s: Page contains img Ads '%s'", domain.getProperty("name"), getName(), s));
                    }

                    ads++;
                }
            }
        }

        c = document.select("object");
        for (Element e : c) {
            String src = e.attr("data").toLowerCase();

            for (String s : objectContains) {
                if (src.contains(s)) {
                    if (TraitsFactory.INSTANCE.isVERBOSE()) {
                        log.info(String.format("Domain %s, trait %s: Page contains img Ads '%s'", domain.getProperty("name"), getName(), s));
                    }

                    ads++;
                }
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

        return ads > 0;
    }
}
