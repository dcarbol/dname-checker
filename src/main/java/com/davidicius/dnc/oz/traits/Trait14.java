package com.davidicius.dnc.oz.traits;

import com.davidicius.dnc.oz.OZ;
import com.tinkerpop.blueprints.Vertex;
import org.jsoup.nodes.Document;
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

    public boolean hasTrait(Vertex domain, String page, Document document, OZ oz) {
        return forceExists(domain) && forceLoaded(domain, page) && pattern01.matcher(page).find();
    }
}
