package com.davidicius.dnc.oz.traits;

import com.davidicius.dnc.oz.OZ;
import com.tinkerpop.blueprints.Vertex;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

public class Trait13 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait13.class);

    public String getId() {
        return "13";
    }

    public String getName() {
        return "BForward";
    }

    public boolean hasTrait(Vertex domain, String page, Document document, OZ oz) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;

        String f = domain.getProperty("forward");
        if (f == null || f.trim().equals("") || page == null) return false;

        String domainName = domain.getProperty("name");
        try {
            URL furl = new URL(f.trim());

            String fh = furl.getHost();
            if (fh.startsWith("www.")) fh = fh.substring("www.".length());

            String dh = domainName;
            if (dh.startsWith("www.")) dh = dh.substring("www.".length());

            //@todo - ignorovat forward na legalni stranky...
            // @toto / ignorovat forward na stranky STEJNEHO ownera
            if (fh.equals("skoda-auto.sk")) {
                return false;
            }

            if (fh.equals("skoda-auto.cz")) {
                return false;
            }

            if (fh.equals("skoda.cz")) {
                return false;
            }

            // @todo
            // a ted oynacit za spatne ty, ktere smeruji na stranky konkurence nebio na domenu kterou vlastni konkurence nebo ktera obsahuje konkurencni OZ.
            if (fh.equals("blesk.cz")) {
                return true;
            }

            if (page.contains("dt001.net")) {
                return true;
            }
            return false;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
