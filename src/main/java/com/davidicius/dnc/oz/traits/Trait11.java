package com.davidicius.dnc.oz.traits;

import com.davidicius.dnc.oz.Keys;
import com.davidicius.dnc.oz.OZ;
import com.davidicius.dnc.oz.TraitsFactory;
import com.tinkerpop.blueprints.Vertex;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

public class Trait11 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait11.class);
    static List<Pattern> patterns = new ArrayList<Pattern>();

    static {
        patterns.add(Pattern.compile("include\\s+your\\s+ad"));
    }

    public String getId() {
        return "11";
    }

    public String getName() {
        return "SContent";
    }

    private static Set<String> ALWAYS_BAD = new HashSet<String>(Arrays.asList("casino", "sex", "porno", "escort", "automaty", "internethity", "www.tl1.cz"));
    private static Set<String> BADCONTAINS = new HashSet<String>(Arrays.asList(" sex", " porno", "casino"));

    private String snippet(String page, String word, int start,int offset) {
        int index = page.indexOf(word, start);
        if (index >= 0) {
            int s = Math.max(0, index - offset);
            int e = Math.min(page.length(), index + word.length() + offset);
            return page.substring(s, e);
        } else {
            return "";
        }
    }

    public boolean hasTrait(Vertex domain, String page, Document document, OZ oz) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;
        if (page == null) return false;
        if (oz == null) {
            log.warn(String.format("Domain %s, trait %s: Domain has no valid OZ'", domain.getProperty("name"), getName()));
            return false;
        }

        int count = 0;
        for (String s : BADCONTAINS) {
            if (page.contains(s)) {
                if (TraitsFactory.INSTANCE.isVERBOSE()) {
                    log.info(String.format("Domain %s, trait %s: Page contains bad word '%s' :%s", domain.getProperty("name"), getName(), s, snippet(page, s, 0, 30)));
                }

                count++;
            }

        }

        for (Pattern p : patterns) {
            if (p.matcher(page).find()) {
                if (TraitsFactory.INSTANCE.isVERBOSE()) {
                    log.info(String.format("Domain %s, trait %s: Page contains suspicious pattern '%s'", domain.getProperty("name"), getName(), p.toString()));
                }

                count += 2;
            }
        }


        int alwaysBad = countWords(page, ALWAYS_BAD, domain.getProperty("name").toString(), oz);
        int competitors = countWords(page, oz.getCompetitors(), domain.getProperty("name").toString(), oz);
        Set<String> otherAreaWords = Keys.wordsForAreaExcept(oz.getArea());
        int otherArea = countWords(page, otherAreaWords, domain.getProperty("name").toString(), oz);

        count += alwaysBad + competitors + otherArea;

        //@todo - spatna slova jsou az na strance O Nas :(    eskoda-shop.cz
        return count >= 2;
    }

    private static String startDelimiters = "\\.'=; ,\">/-";
    private static String endDelimiters = "'=; ,\"</.";

    private int countWords(String normalizedPage, Set<String> words, String domainName, OZ oz) {
        int count = 0;

        for (String key : words) {
            if (key.equals("escort") && oz.getName().equals("ford") ) continue;

            int start = 0;
            while (start < normalizedPage.length()) {
                int index = normalizedPage.indexOf(key, start);
                if (index >= 0) {
                    boolean before = index - 1 >= 0 && startDelimiters.indexOf(normalizedPage.charAt(index - 1)) >= 0;
                    boolean after = index + key.length() < normalizedPage.length() && endDelimiters.indexOf(normalizedPage.charAt(index + key.length())) >= 0;
                    if (before && after || key.length() >= 6) {
                        if (TraitsFactory.INSTANCE.isVERBOSE()) {
                            log.info(String.format("Domain %s, trait %s: Page contains bad word '%s': %s", domainName, getName(), key, snippet(normalizedPage, key, start, 30)));
                        }

                        count++;
                    }

                    start = index + 1;
                } else {
                    break;
                }
            }
        }

        return count;
    }

}
