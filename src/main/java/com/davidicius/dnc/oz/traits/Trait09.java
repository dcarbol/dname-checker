package com.davidicius.dnc.oz.traits;

import com.davidicius.dnc.oz.OZ;
import com.davidicius.dnc.oz.TraitsFactory;
import com.tinkerpop.blueprints.Vertex;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Trait09 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait09.class);

    public String getId() {
        return "09";
    }

    public String getName() {
        return "SURL";
    }

    Pattern pattern01 = Pattern.compile("w.*\\d+\\..+\\..+");

    public boolean hasTrait(Vertex domain, String page, Document document, OZ oz) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;
        if (oz == null) {
            log.warn(String.format("Domain %s, trait %s: Domain has no valid OZ'", domain.getProperty("name"), getName()));
            return false;
        }

        String dh = domain.getProperty("name");
        if (dh.startsWith("www.")) dh = dh.substring("www.".length());

        if (dh.startsWith("www") || dh.startsWith("www-")) {
            return true;
        }

        if (pattern01.matcher(dh).matches()) {
            if (TraitsFactory.INSTANCE.isVERBOSE()) {
                log.info(String.format("Domain %s, trait %s: Domain name matches '%s''", dh, getName(), pattern01.toString()));
            }

            return true;
        }

        String f = domain.getProperty("forward");
        if (f != null && !f.trim().equals("")) {
            URL furl;
            try {
                furl = new URL(f.trim());
                String fh = furl.getHost();

                if (pattern01.matcher(fh).matches()) {
                    if (TraitsFactory.INSTANCE.isVERBOSE()) {
                        log.info(String.format("Domain %s, trait %s: Forward matches '%s'", domain.getProperty("name"), getName(), pattern01.toString()));
                    }
                    return true;
                }

                if (fh.startsWith("www.")) fh = fh.substring("www.".length());
                if (suspiciousUrl(domain.getProperty("name").toString(), fh, oz, "Forward")) {
                    return true;
                }

                Set<String> competitorsInDh = new HashSet<String>();
                aggregateCompetitors(dh, oz, competitorsInDh);

                Set<String> competitorsInFh = new HashSet<String>();
                aggregateCompetitors(fh, oz, competitorsInFh);

                competitorsInFh.removeAll(competitorsInDh);
                if (competitorsInFh.size() > 0) {
                    if (TraitsFactory.INSTANCE.isVERBOSE()) {
                        log.info(String.format("Domain %s, trait %s: Forward contains competitors: '%s''", domain.getProperty("name"), getName(), competitorsInFh.toString()));
                    }

                    return true;
                }

            } catch (MalformedURLException e) {
            }
        }


        // @Todo- OZ nazev slova  a rozdelit pro OZ s vice slovy...
        if (suspiciousUrl(domain.getProperty("name").toString(), dh, oz, "Domain")) {
            return true;
        }

        return false;
    }

    private boolean suspiciousUrl(String domainName, String host, OZ oz, String msg) {
        String[] keywords = {oz.getName()};
        String[] parts = host.split("-");

        double coef = 0;
        for (String part : parts) {
            int usedChars = 0;
            for (String key : keywords) {
                if (part.contains(key)) {
                    usedChars += key.length();
                }
            }

            double c = usedChars * 1.0 / part.length();
            coef += c;
        }

        coef = coef / parts.length;
        if (coef < 0.15 && coef != 0) {
            if (TraitsFactory.INSTANCE.isVERBOSE()) {
                log.info(String.format("Domain %s, trait %s: %s name '%s' coef %f", domainName, getName(), msg, host, coef));
            }

            return true;
        } else {
            return false;
        }
    }

    private void aggregateCompetitors(String hostname, OZ oz, Set<String> result) {
        String[] parts = hostname.split("-");
        for (String part : parts) {
            for (String competitor : oz.getCompetitors()) {
                if (part.contains(competitor)) {
                    result.add(competitor);
                }
            }
        }
    }
}
