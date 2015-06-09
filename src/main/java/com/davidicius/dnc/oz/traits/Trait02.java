package com.davidicius.dnc.oz.traits;

import com.davidicius.dnc.oz.OZ;
import com.davidicius.dnc.oz.TraitsFactory;
import com.tinkerpop.blueprints.Vertex;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

public class Trait02 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait02.class);

    public String getId() {
        return "02";
    }

    public String getName() {
        return "Forward";
    }

    Pattern pattern01 = Pattern.compile("http-equiv\\s*=\\s*\"refresh\"");

    public boolean hasTrait(Vertex domain, String page, Document document, OZ oz) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;

        if (page != null) {
            if (pattern01.matcher(page.toLowerCase()).find()) {
                if (TraitsFactory.INSTANCE.isVERBOSE()) {
                    log.info(String.format("Domain %s, trait %s: Page matches '%s'", domain.getProperty("name"), getName(), pattern01.toString()));
                }

                return true;
            }
        }

        String f = domain.getProperty("forward");
        if (f == null || f.trim().equals("")) return false;

        String domainName = domain.getProperty("name");

        String fh = normalizeUrl(f);
        String dh = domainName;
        if (dh.startsWith("www.")) dh = dh.substring("www.".length());

     /*       //@todo - ignorovat forward na legalni stranky...
            // @toto / ignorovat forward na strankz STEJNEHO ownera
            if (fh.equals("skoda-auto.sk")) {
                return false;
            }

            if (fh.equals("skoda-auto.cz")) {
                return false;
            }

            if (fh.equals("skoda.cz")) {
                return false;
            }
       */

        if (!dh.equals(fh)) {
            if (TraitsFactory.INSTANCE.isVERBOSE()) {
                log.info(String.format("Domain %s, trait %s: Domain '%s' differs from forward '%s'", domain.getProperty("name"), getName(), dh, fh));
            }

            return true;
        } else {
            return false;
        }
    }
}
