package com.davidicius.dnc.oz.traits;

import com.davidicius.dnc.oz.OZ;
import com.davidicius.dnc.oz.TraitsFactory;
import com.tinkerpop.blueprints.Vertex;
import org.jsoup.nodes.Document;
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

    public boolean hasTrait(Vertex domain, String page, Document document, OZ oz) {

        if (forceExists(domain)) {
            if (forceLoaded(domain, page)) {
                if (page != null) {
                    if (page.length() < 5 * 1000) {
                        if (TraitsFactory.INSTANCE.isVERBOSE()) {
                            log.info(String.format("Domain %s, trait %s: Page has less then 5 000 characters.'", domain.getProperty("name"), getName()));
                        }

                        return true;
                    }
                }
            }
        }

        return false;
    }
}
