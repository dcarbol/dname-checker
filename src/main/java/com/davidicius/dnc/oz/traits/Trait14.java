package com.davidicius.dnc.oz.traits;

import com.tinkerpop.blueprints.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class Trait14 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait14.class);

    public String getId() {
        return "14";
    }

    public String getName() {
        return "SMS";
    }

    Pattern pattern01 = Pattern.compile("cena.{1,20}sms");

    public boolean hasTrait(Vertex domain, String page) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;

        page = normalizePage(page);
        if (pattern01.matcher(page).find()) {
            return true;
        }

        return false;
    }
}
