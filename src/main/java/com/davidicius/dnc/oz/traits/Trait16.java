package com.davidicius.dnc.oz.traits;

import com.davidicius.dnc.oz.DbService;
import com.davidicius.dnc.oz.OZ;
import com.davidicius.dnc.oz.OzWorker;
import com.davidicius.dnc.oz.TraitsFactory;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class Trait16 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait16.class);

    public String getId() {
        return "16";
    }

    public String getName() {
        return "NoOz";
    }

    public boolean hasTrait(Vertex domain, String page, Document document, OZ oz) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;
        if (page == null || document == null) return false;

        if (oz == null) {
            log.warn(String.format("Domain %s, trait %s: Domain has no valid OZ'", domain.getProperty("name"), getName()));
            return false;
        }

        String ozName = oz.getName();
        if (!page.contains(ozName)) {
            if (TraitsFactory.INSTANCE.isVERBOSE()) {
                log.info(String.format("Domain %s, trait %s: Page does not contain OZ '%s'", domain.getProperty("name"), getName(), ozName));
            }

            return true;
        }

        return false;
    }
}
