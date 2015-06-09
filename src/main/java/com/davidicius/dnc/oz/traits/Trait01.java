package com.davidicius.dnc.oz.traits;

import com.davidicius.dnc.oz.OZ;
import com.davidicius.dnc.oz.TraitsFactory;
import com.tinkerpop.blueprints.Vertex;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class Trait01 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait01.class);

    public String getId() {
        return "01";
    }

    public String getName() {
        return "Park";
    }

    static List<Pattern> patterns = new ArrayList<Pattern>();

    static {
        patterns.add(Pattern.compile("domena\\s+je\\s+registrovana"));
        patterns.add(Pattern.compile("web\\s+neexistuje"));
        patterns.add(Pattern.compile("domena\\s+je\\s+jiz\\s+registrovana"));
        patterns.add(Pattern.compile("tato\\s+domena\\s+je\\s+zaregistrovana\\s+prostrednictvim\\s+domenoveho"));
        patterns.add(Pattern.compile("domena\\s+na\\s+prodej"));
        patterns.add(Pattern.compile("the\\s+domain\\s+name\\s+is\\s+registered"));
        patterns.add(Pattern.compile("stranky\\s+v\\s+rekonstrukci"));
        patterns.add(Pattern.compile("kontaktujte\\s+na\\s+domeny"));
        patterns.add(Pattern.compile("under\\s+construction"));
        patterns.add(Pattern.compile("website\\s+is\\s+up\\s+and\\s+running"));
        patterns.add(Pattern.compile("domena\\s+byla\\s+zaregistrovana"));
        patterns.add(Pattern.compile("have\\s+permission\\s+to\\s+access"));
        patterns.add(Pattern.compile("domain.{1,100}sale"));
        patterns.add(Pattern.compile("server.{1,20}not\\s+found"));
        patterns.add(Pattern.compile("this\\s+page\\s+is\\s+parked"));
        patterns.add(Pattern.compile("domena\\s+je\\s+blokovana\\s+poskytovatelem"));
        patterns.add(Pattern.compile("domain.*is\\s+listed\\s+for\\s+sale"));
        patterns.add(Pattern.compile("there\\s+is\\s+no\\s+web\\s+site\\s+at\\s+this\\s+address"));
        patterns.add(Pattern.compile("stranka\\s+nebyla\\s+nalezena"));
        patterns.add(Pattern.compile("docasne\\s+mimo\\s+provoz"));
        patterns.add(Pattern.compile("novy\\s+virtualni\\s+server"));
        patterns.add(Pattern.compile("nemaji\\s+na\\s+nasem\\s+serveru\\s+prezentaci"));
        patterns.add(Pattern.compile("domena.{1,100}je\\s+jiz\\s+registrovana\\s+na"));
        patterns.add(Pattern.compile("webhosting\\s+je\\s+vypnuty"));
        patterns.add(Pattern.compile("na\\s+teto\\s+domene\\s+zatim\\s+nebezi\\s+internetova\\s+prezentace"));
        patterns.add(Pattern.compile("pracujeme\\s+na\\s+doplnen.{1,100}obsahu\\s+expirovan"));
        patterns.add(Pattern.compile("tato\\s+domena\\s+je\\s+na\\s+prodej"));
        patterns.add(Pattern.compile("tuto\\s+domenu\\s+ma\\s+zaregistrovanou\\s+jeden\\s+z\\s+nasich\\s+klientu"));
        patterns.add(Pattern.compile("webhosting\\s+je\\s+vypnuty"));
        patterns.add(Pattern.compile("na\\s+teto\\s+domene\\s+zatim\\s+nebezi\\s+internetova\\s+prezentace"));
        patterns.add(Pattern.compile("novy\\s+www\\s+prostor\\s+na\\s+webhostingu\\s+web4u"));
        patterns.add(Pattern.compile("<title>webhosting</title>"));
        patterns.add(Pattern.compile("server\\s+error\\s+in\\s+application"));
        patterns.add(Pattern.compile("provoz\\s+serveru\\s+na\\s+domene"));
        patterns.add(Pattern.compile("lorem\\s+ipsum\\s+dolor\\s+sit\\s+amet"));
        patterns.add(Pattern.compile("this\\s+is\\s+the\\s+default\\s+web\\s+page\\s+for\\s+this\\s+server"));
        patterns.add(Pattern.compile("<title>\\s+mimo\\s+provoz\\s+</title>\\s+b"));
        patterns.add(Pattern.compile("domena\\s+je\\s+zaregistrovana"));
        patterns.add(Pattern.compile("this\\s+account\\s+has\\s+been\\s+suspended"));
        patterns.add(Pattern.compile("the\\s+site\\s+is\\s+currently\\s+not\\s+available\\s+due\\s+to\\s+technical\\s+problems"));
        patterns.add(Pattern.compile("apache\\s+2\\s+test\\s+page"));
        patterns.add(Pattern.compile("domena.{1,100}je\\s+zaparkovana"));
        patterns.add(Pattern.compile("internal\\s+server\\s+error"));
        patterns.add(Pattern.compile("u\\s+ziet\\s+deze\\s+pagina\\s+omdat"));
        patterns.add(Pattern.compile("tato\\s+domena\\s+je\\s+v\\s+soucasne\\s+dobe"));
    }

    public boolean hasTrait(Vertex domain, String page, Document document, OZ oz) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;

        String f = domain.getProperty("forward");
        if (f != null && f.trim().equals("")) {
            URL furl;
            try {
                furl = new URL(f.trim());
                String path = furl.getPath().toLowerCase();
                if (path.contains("suspendedpage")) {
                    if (TraitsFactory.INSTANCE.isVERBOSE()) {
                        log.info(String.format("Domain %s, trait %s: Forward  '%s' contains 'suspendedpage'", domain.getProperty("name"), getName(), f));
                    }

                    return true;
                }

                if (path.contains("non-existing-virtual")) {
                    if (TraitsFactory.INSTANCE.isVERBOSE()) {
                        log.info(String.format("Domain %s, trait %s: Forward  '%s' contains 'non-existing-virtual'", domain.getProperty("name"), getName(), f));
                    }

                    return true;
                }
            } catch (MalformedURLException e) {
            }
        }

        for (Pattern p : patterns) {
            if (p.matcher(page).find()) {
                if (TraitsFactory.INSTANCE.isVERBOSE()) {
                    log.info(String.format("Domain %s, trait %s: Page matches pattern '%s'", domain.getProperty("name"), getName(), p.toString()));
                }

                return true;
            }
        }

        if (page.contains("http://domainfwding.com")) {
            if (TraitsFactory.INSTANCE.isVERBOSE()) {
                log.info(String.format("Domain %s, trait %s: Page contains 'http://domainfwding.com'", domain.getProperty("name"), getName()));
            }

            return true;
        }

        if (page.length() < 100 && !(page.contains("refresh") && page.contains("http-equiv"))) {
            if (TraitsFactory.INSTANCE.isVERBOSE()) {
                log.info(String.format("Domain %s, trait %s: Page contains 'http-equiv' and has size under 100 characters", domain.getProperty("name"), getName()));
            }

            return true;
        }

        return false;
    }
}

