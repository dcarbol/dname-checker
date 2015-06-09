package com.davidicius.dnc.oz.traits;

import com.davidicius.dnc.oz.OZ;
import com.davidicius.dnc.oz.TraitsFactory;
import com.tinkerpop.blueprints.Vertex;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Trait12 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait12.class);

    public String getId() {
        return "12";
    }

    public String getName() {
        return "GOwner";
    }

    public static String[] NAMES = {"jiri", "jan", "miroslav", "vladimir"};

    public boolean hasTrait(Vertex domain, String page, Document document, OZ oz) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;
        if (page == null || document == null) return false;
        if (oz == null) {
            log.warn(String.format("Domain %s, trait %s: Domain has no valid OZ'", domain.getProperty("name"), getName()));
            return false;
        }

        String owner = domain.getProperty("owner");
        if (owner == null) return false;

        owner = normalizePage(owner);
        if (owner.contains(oz.getName())) {
            if (TraitsFactory.INSTANCE.isVERBOSE()) {
                log.info(String.format("Domain %s, trait %s: Domain owner '%s' contains OZ '%s'", domain.getProperty("name"), getName(), owner, oz.getName()));
            }

            return true;
        }

        //@todo Hledat jmena ve strance //manazerskeporadenstvi-jiriskoda.cz
        if (!oz.getName().equals("skoda")) return false;

        int index = 0;
        int delta = 40;
        String ozName = oz.getName();
        while (index < page.length()) {
            index = page.indexOf(ozName, index);
            if (index >= 0) {
                int from = Math.max(0, index - delta);
                int to = Math.min(index + delta, page.length());
                String part = page.substring(from, to);

                for (String key : NAMES) {
                    int keyIndex = part.indexOf(key);
                    if (keyIndex >= 0) {
                        if (keyIndex < 40 && keyIndex >= 40- (key.length() + 2)) {
                            if (TraitsFactory.INSTANCE.isVERBOSE()) {
                                log.info(String.format("Domain %s, trait %s: Page contains name '%s' close to  OZ name '%s'", domain.getProperty("name"), getName(), key, oz.getName()));
                            }

                            return true;
                        }

                        if (keyIndex >= 40 + ozName.length() && keyIndex <= 40 + ozName.length() + 2 ) {
                            if (TraitsFactory.INSTANCE.isVERBOSE()) {
                                log.info(String.format("Domain %s, trait %s: Page contains name '%s' close to  OZ name '%s'", domain.getProperty("name"), getName(), key, oz.getName()));
                            }

                            return true;
                        }
                    }
                }

                if (part.contains("roman") && !part.contains("new roman")) {
                    if (TraitsFactory.INSTANCE.isVERBOSE()) {
                        log.info(String.format("Domain %s, trait %s: Page contains name '%s' close to  OZ name '%s'", domain.getProperty("name"), getName(), "roman", oz.getName()));
                    }

                    return true;
                }

                index++;
            } else {
                break;
            }
        }

        return false;
    }
}
