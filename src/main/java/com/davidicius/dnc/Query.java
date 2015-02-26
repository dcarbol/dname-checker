package com.davidicius.dnc;


import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class Query {
    interface Rule {
        void increment();

        int count();

        String getRuleName();

        String rewrteOz(String oz);
    }

    public String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("");
    }

    abstract class AbstractRule implements Rule {
        int count;

        public void increment() {
            count++;
        }

        public int count() {
            return count;
        }
    }

    class Rule1 extends AbstractRule {
        public String getRuleName() {
            return "Simple over-write";
        }

        public String rewrteOz(String oz) {
            String r = oz.replaceAll("[^a-z0-9\\-]", "");
            if (!r.startsWith(".cz")) {
                r = r + ".cz";
            }

            return r;
        }

    }

    // Canonize, replace space with minus
    class Rule2 extends AbstractRule {
        public String getRuleName() {
            return "Space replace with minus";
        }

        public String rewrteOz(String oz) {
            if (!oz.contains(" ")) return "";

            String r = oz.replaceAll("[^a-z0-9\\- ]", "");
            r = r.replaceAll(" ", "-");

            if (!r.startsWith(".cz")) {
                r = r + ".cz";
            }

            return r;
        }
    }

    // Canonize, replace space with minus
    class Rule3 extends AbstractRule {
        public String getRuleName() {
            return "WWW added to Rule1";
        }

        public String rewrteOz(String oz) {
            if (!oz.contains(" ")) return "";

            String r = oz.replaceAll("[^a-z0-9\\- ]", "");
            r = r.replaceAll(" ", "-");

            if (!r.startsWith(".cz")) {
                r = r + ".cz";
            }

            return "www" + r;
        }

    }


    class Rule4 extends AbstractRule {
        public String getRuleName() {
            return "WWW- added to Rule1";
        }

        public String rewrteOz(String oz) {
            if (!oz.contains(" ")) return "";

            String r = oz.replaceAll("[^a-z0-9\\- ]", "");
            r = r.replaceAll(" ", "-");

            if (!r.startsWith(".cz")) {
                r = r + ".cz";
            }

            return "www-" + r;
        }

    }

    class Rule5 extends AbstractRule {
        public String getRuleName() {
            return "e added to Rule1";
        }

        public String rewrteOz(String oz) {
            if (!oz.contains(" ")) return "";

            String r = oz.replaceAll("[^a-z0-9\\- ]", "");
            r = r.replaceAll(" ", "-");

            if (!r.startsWith(".cz")) {
                r = r + ".cz";
            }

            return "e" + r;
        }

    }

    class Rule6 extends AbstractRule {
        public String getRuleName() {
            return "e- added to Rule1";
        }

        public String rewrteOz(String oz) {
            if (!oz.contains(" ")) return "";

            String r = oz.replaceAll("[^a-z0-9\\- ]", "");
            r = r.replaceAll(" ", "-");

            if (!r.startsWith(".cz")) {
                r = r + ".cz";
            }

            return "e-" + r;
        }
    }

    class Rule7 extends AbstractRule {
        public String getRuleName() {
            return "i added to Rule1";
        }

        public String rewrteOz(String oz) {
            if (!oz.contains(" ")) return "";

            String r = oz.replaceAll("[^a-z0-9\\- ]", "");
            r = r.replaceAll(" ", "-");

            if (!r.startsWith(".cz")) {
                r = r + ".cz";
            }

            return "i" + r;
        }

    }


    class Rule8 extends AbstractRule {
        public String getRuleName() {
            return "i- added to Rule1";
        }

        public String rewrteOz(String oz) {
            if (!oz.contains(" ")) return "";

            String r = oz.replaceAll("[^a-z0-9\\- ]", "");
            r = r.replaceAll(" ", "-");

            if (!r.startsWith(".cz")) {
                r = r + ".cz";
            }

            return "i-" + r;
        }

    }

    private Rule[] rules = {new Rule8()};
//    private Rule[] rules = {new Rule1(), new Rule2(), new Rule3()};

    public static void main(String[] args) throws IOException {
        Query query = new Query();
        query.start();
    }

    private void add(HashMap<Rule, Integer> h, Rule r) {
        Integer i = h.get(r);
        if (i == null) {
            h.put(r, 0);
        } else {
            h.put(r, i + 1);
        }
    }

    private void start() {
        OrientGraph graph = Starter.startDb("remote:/all-domains");
        Iterable<Vertex> query = graph.query().vertices();
        int count = 0;
        for (Vertex oz : query) {
            count++;
        }

        System.out.println(count);
        if (count > 10) return;

        List<String> domains = Starter.loadDomainList();
        Set<String> ddSet = new HashSet<String>(domains);
        HashMap<Rule, Integer> stats = new HashMap<Rule, Integer>();
        count = 0;
        query = graph.query().vertices();
        for (Vertex oz : query) {
            String ozName = oz.getProperty("zneni").toString().toLowerCase();
            String ozNameNoAccent = removeAccent(ozName);

            String ozDate = oz.getProperty("priorita");
            String ozOwner = oz.getProperty("majitel");

            count++;
            StringBuilder result = new StringBuilder(32);
            result.append(ozName).append(" ");

            int ri = 1;
            boolean found = false;
            for (Rule r : rules) {
                String rewrite = r.rewrteOz(ozNameNoAccent);
                if (ddSet.contains(rewrite)) {
                    result.append(" ").append(ri).append(":").append(rewrite);
                    found = true;
                    r.increment();
                } else {
                    result.append(" ").append(ri).append(": <NOP>");
                }

                ri++;
            }

            if (found) {
                System.out.println(result.toString());
            }
        }

        for (Rule r : rules) {
            System.out.println(String.format("Rule: '%s'  %d", r.getRuleName(), r.count()));
        }

    }
}
