package com.davidicius.dnc.oz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

public class OZAnalysis {
    private static final Logger log = LoggerFactory.getLogger(OZAnalysis.class);

    private static int MINIMUM_CHARS_OF_OZ = 4;
    private static int MAXIMUM_CHARS_OF_OZ = 160;
    private static int MINIMUM_PARTS_OF_OZ = 1;
    private static int MAXIMUM_PARTS_OF_OZ = 4;
    private static int MINIMUM_CHARS_OF_KEYS = 4;
    private static int MAXIMUM_CHARS_OF_KEYS = 200;

    private static boolean REBUILD_ALL_INDICES = false;


    public static void main(String[] args) throws IOException {
        List<String> domains = loadRawDomains("exported-cz-domains.txt");
        GlobalStringTable global = new GlobalStringTable();
        List<String> upvList = loadRawOz("exported-upv-oz.txt");
        List<String> ohimList = loadRawOz("exported-ohim-oz.txt");
        Set<String> alloZ = new HashSet<String>(upvList);
        alloZ.addAll(ohimList);

        // Print overall statistics about used data
        printStats(domains, ohimList);

        Set<String> czDictionary = loadCZDictionary("czech-slova.txt", global);
        Set<String> enCorpus = loadENCorpus("slovnik_data.txt", global);
        Set<String> czCorpus = loadCZCorpus("syn2010_word_abc.txt", global);
        Set<String> czCities = loadCZCities("seznam-obci-cr.txt", global);
        Set<String> czNames = loadCZNames("prijmeniCZ.txt", global);

        Set<String> citiesKeys = getCitiesKeys(czCities);
        czCorpus.addAll(citiesKeys);
        czCorpus.addAll(czNames);

        Set<String> filteredOz = filterOZ(alloZ, czDictionary);
        List<String> filteredDomains = filterDomains(domains);

        Set<String> keys = generateKeys(filteredOz);
        if (REBUILD_ALL_INDICES) {
            log.info("Build index from KEYS do DOMAINS...");
            buildAndSaveIndex(keys, filteredDomains, "keys2domains.txt");
        }

        // Build additional keys for FORCED OZ like BMW, KIA...
        StringIndex key2domains = StringIndex.loadIndex("keys2domains.txt");
        for (String forcedOz : OZ_FORCE_ADD.split("\\s+")) {
            buildIndexForKey(key2domains, forcedOz, filteredDomains);
        }
        for (String forcedCz : ADDITIONAL_CZ_WORDS.split("\\s+")) {
            buildIndexForKey(key2domains, forcedCz, filteredDomains);
        }

        key2domains.saveIndex("keys2domains.txt");

        Set<String> allCzWords = new HashSet<String>(czDictionary);
        allCzWords.addAll(czCorpus);
        createAndSaveAllCorpus("allCorpus2.txt", alloZ, allCzWords, enCorpus);
        Set<String> allCorpus = loadCorpus("allCorpus2.txt");
        log.info("ALL corpus size:  " + allCorpus.size());


//        log.info("Build index from ALL CORPUS to FILTERED DOMAINS...");
//        buildAndSaveIndex(allCorpus, filteredDomains, "allCorpus2FilteredDomains2.txt");
        log.info("Loading ALL CORPUS to FILTERED DOMAINS index...");
        StringIndex allCorpus2FilteredDomains = StringIndex.loadIndex("allCorpus2FilteredDomains2.txt");

        for (String key : OZ_FORCE_ADD.split("\\s+")) {
            if (key.trim().length() > 0) {
                updateIndexWithNewKey(allCorpus2FilteredDomains, key.trim(), filteredDomains);
            }
        }

        for (String key : ADDITIONAL_CZ_WORDS.split("\\s+")) {
            if (key.trim().length() > 0) {
                updateIndexWithNewKey(allCorpus2FilteredDomains, key.trim(), filteredDomains);
            }
        }

        log.info("Loaded...");

        allCorpus2FilteredDomains = adjustCorpusIndex(allCorpus2FilteredDomains);
        allCorpus2FilteredDomains.saveIndex("allCorpus2FilteredDomains2.txt");

        // Tot je narocne vypocetne...
//        buildAndSaveDomainsToCorpus(allCorpus2FilteredDomains, "domains2substrings2.txt");
//        if (1 == 1) return;


        log.info("Loading domains 2 substrings index...");
        StringIndex domains2substring = StringIndex.loadIndex("domains2substrings2.txt");
        StringIndex domain2corpus = StringIndex.loadIndex("domain2corpus.txt");

        createAndSaveReverseIndex(allCorpus2FilteredDomains, "domain2corpus.txt");

        for (String key : OZ_FORCE_ADD.split("\\s+")) {
            if (key.trim().length() > 0) {
                updateDomainsToCorpusForKey(allCorpus2FilteredDomains, domains2substring, domain2corpus, key);
            }
        }

        for (String key : ADDITIONAL_CZ_WORDS.split("\\s+")) {
            if (key.trim().length() > 0) {
                updateDomainsToCorpusForKey(allCorpus2FilteredDomains, domains2substring, domain2corpus, key);
            }
        }

        domains2substring.saveIndex("domains2substrings2.txt");

//        final StringIndex keys2domains = null;// = StringIndex.loadIndex("keys2domains.txt");
        log.info("Getting OZ with some hit in domains...");
        Set<String> ozWithHitInFilteredDomains = new HashSet<String>(10 * 1000);
        for (String oz : filteredOz) {
            if (getDomainsForOz(oz, allCorpus2FilteredDomains).size() > 0) {
                ozWithHitInFilteredDomains.add(oz);
            }
        }
        log.info(String.format("Amount of filtered OZ with hit in filtered domains: %d (%8.2f%% from %d)", ozWithHitInFilteredDomains.size(), ozWithHitInFilteredDomains.size() * 100.0 / filteredOz.size(), filteredOz.size()));

        buildAndSaveOz2DomainMapping(ozWithHitInFilteredDomains, allCorpus2FilteredDomains, domains2substring, "finalOz2DomainMappping2.txt");

        log.info("Load final OZ 2 domain index");
        StringIndex finalOz2DomainMappping2 = StringIndex.loadIndex("finalOz2DomainMappping2.txt");
        log.info("Loaded.");

        log.info("Filtering OZ - only force OZ used...");
        StringIndex filteredFinalOz2DomainMapping = filterOz2DomainMapping(finalOz2DomainMappping2);
        log.info("Done.");
        filteredFinalOz2DomainMapping.saveIndex("filteredFinalOz2DomainMapping.txt");


        log.info("Counting filtered OZ pattern histogram...");
        StringIndex filteredHistogramIndex = countOzInDomainPatternsHistogram(filteredFinalOz2DomainMapping);
        filteredHistogramIndex.saveIndex("filteredHistogramIndex.txt");
        log.info("Done.");

        log.info("Counting ALL OZ pattern histogram...");
        StringIndex allHhistogramIndex = countOzInDomainPatternsHistogram(finalOz2DomainMappping2);
        allHhistogramIndex.saveIndex("allHistogramIndex.txt");
        log.info("Done.");

        if (1 == 1) return;


//        List<String> sortedOz = new ArrayList<String>(ozWithHitInFilteredDomains);
//        Collections.sort(sortedOz, new Comparator<String>() {
//            public int compare(String o1, String o2) {
//                return index.get(o2).size() - index.get(o1).size();
//            }
//        });

//        System.out.println("TOP OZ from hits perspective");
//        for (int i = 0; i < 10; ++i) {
//            System.out.println(String.format("%s: %d hits", sortedOz.get(i), index.get(sortedOz.get(i)).size()));
//        }

 /*       if (REBUILD_ALL_INDICES) {
            createAndSaveAllCorpus("filteredCorpus.txt", ozWithHitInFilteredDomains, czCorpus, enCorpus);
        }

        Set<String> filteredCorpus = loadCorpus("filteredCorpus.txt");
        System.out.println("Corpus loaded, size: " + filteredCorpus.size());

        if (REBUILD_ALL_INDICES) {
            buildAndSaveKeysToCorpusIndex(keys, filteredCorpus, "keysToCorpusIndex.txt");
        }

        final StringIndex keysToCorpusIndex = StringIndex.loadIndex("keysToCorpusIndex.txt");

        final StringIndex ozToDomainFinal = new StringIndex();
        Set<String> usedDomains = new HashSet<String>();
        for (String o : ozWithHitInFilteredDomains) {
            String[] parts = getRelevantParts(o);
            ArrayList<String> h = findHits(keys2domains, keysToCorpusIndex, filteredCorpus, parts, o);
            ozToDomainFinal.setList(o, h);
            for (String d : h) {
                usedDomains.add(d);
            }
        }

        ozToDomainFinal.saveIndex("ozToDomainFinal.txt");

        System.out.println(String.format("Domains with some OZ: %d (%8.3f%%)", usedDomains.size(), usedDomains.size() * 100.0 / filteredDomains.size()));
        System.out.println(String.format("Average domain per OZ: %8.3f", usedDomains.size() * 1.0 / ozWithHitInFilteredDomains.size()));

        ArrayList<String> sortedOz = new ArrayList<String>(ozWithHitInFilteredDomains);
        Collections.sort(sortedOz, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return ozToDomainFinal.get(o2).size() - ozToDomainFinal.get(o1).size();
            }
        });

        System.out.println("TOP OZ from FINAL index perspective");
        for (int i = 0; i < sortedOz.size(); ++i) {
            int s = ozToDomainFinal.get(sortedOz.get(i)).size();
//            if (s >= 1 && s <= 1) {
            System.out.println(String.format("%s: %d hits", sortedOz.get(i), s));
//            }
        }

//        String oz = "bravo";
//        List<String> hits = ozToDomainFinal.get(oz);
//        for (String hit : hits) {
//            System.out.println(hit);
//        }
//
//        System.out.println(String.format("All hits for %s: %d", oz, hits.size()));
   */
    }

    private static StringIndex countOzInDomainPatternsHistogram(StringIndex finalOz2DomainMappping) throws IOException {
        Histogram histogram = new Histogram();
        StringIndex result = new StringIndex();
        int count = 0;
        for (String oz : finalOz2DomainMappping.getKeys()) {
            String ozUpper = oz.toUpperCase();
            for (String line : finalOz2DomainMappping.get(oz)) {
                String[] parts = line.split("->");
                String domain = parts[1].trim();

                String pattern = domain.replaceAll(ozUpper, "?");
                pattern = pattern.replaceAll("\\s+", "");
                String f = pattern.toLowerCase() + ".cz";
                histogram.add(f);

                result.put(f, oz);
            }

            count++;
            if (count % 1000 == 0) {
                System.out.println("Counted: " + count);
            }

        }

//        histogram.save("histogram.txt");
        return result;
    }


    private static String NAMES = " jan jiri josef petr lucie ";
    private static String[] ignoreDomains = {"viphone.cz", "siphone.cz", "gattesco-mondo.cz", "advokatklega.cz", "kabelky-elega.cz", "legatus.cz", "szuslidlmusic.cz",
            "haipadaipa.cz"};

    private static StringIndex filterOz2DomainMapping(StringIndex index) {
        StringIndex result = new StringIndex();
        String[] names = NAMES.toUpperCase().split("\\s+");

        int domainCount = 0;
        for (String key : index.getKeys()) {
            String oz = " " + key + " ";
            if (!OZ_FORCE_ADD.contains(oz)) {
                continue;
            }


            for (String domain : index.get(key)) {
                boolean add = true;

                for (String ignore : ignoreDomains) {
                    if (domain.contains(ignore)) {
                        add = false;
                        break;
                    }
                }

                for (String name : names) {
                    if (name.length() > 0) {
                        name = " " + name.trim() + " ";
                        if (domain.contains(name)) {
                            add = false;
                            break;
                        }
                    }
                }

                if (add) {
                    result.put(key, domain);
                    domainCount++;
                }
            }
        }


        System.out.println(result.getKeys().size());
        System.out.println(domainCount);
        return result;
    }

    public static void buildAndSaveCorpus2DomainMapping(Set<String> filteredCorpus, StringIndex keys2domainIndex, StringIndex domains2substring, String filename) throws IOException {
        StringIndexFile result = new StringIndexFile(filename);
        int count = 0;
        for (String word : filteredCorpus) {
            List<String> domains = keys2domainIndex.get(word);
            if (domains == null || domains.size() == 0) {
                continue;
                //throw new IllegalStateException();
            }

            String upperWord = word.toUpperCase();
            for (String domain : domains) {
                List<String> strings = domains2substring.get(domain);
                if (strings == null || strings.size() != 1) continue;

                String line = strings.get(0);
                int start = 0;
                while (true) {
                    int index = line.indexOf(upperWord, start);
                    if (index == -1) break;

                    if (index == 0 || line.charAt(index - 1) == ' ') {
                        if (index + upperWord.length() == line.length() || line.charAt(index + upperWord.length()) == ' ') {
                            result.put(word, domain + ".cz -> " + line);
                            break;
                        }
                    }

                    start = index + 1;
                }

            }

            count++;
            if (count % 1000 == 0) {
                System.out.println("Counted:" + count);
          //      break;
            }
        }

        result.close();
    }

    public static void buildAndSaveOz2DomainMapping(Set<String> filteredOz, StringIndex keys2domainIndex, StringIndex domains2substring, String filename) throws IOException {
        StringIndex result = new StringIndex();
        int count = 0;
        for (String oz : filteredOz) {
            List<String> domains = getDomainsForOz(oz, keys2domainIndex);
            if (domains.size() == 0) {
                continue;
                //throw new IllegalStateException();
            }

            String upperOz = oz.toUpperCase();
            for (String domain : domains) {
                List<String> strings = domains2substring.get(domain);
                if (strings == null || strings.size() != 1) continue;

                String line = strings.get(0);
                int start = 0;
                while (true) {
                    int index = line.indexOf(upperOz, start);
                    if (index == -1) break;

                    if (index == 0 || line.charAt(index - 1) == ' ') {
                        if (index + upperOz.length() == line.length() || line.charAt(index + upperOz.length()) == ' ') {
                            result.put(oz, domain + ".cz -> " + line);
                            break;
                        }
                    }

                    start = index + 1;
                }
            }

            count++;
            if (count % 1000 == 0) {
                System.out.println("Counted:" + count);
//                break;
            }
        }

        result.saveIndex(filename);
    }

    public static Set<String> getCitiesKeys(Set<String> czCities) {
        Set<String> result = new HashSet<String>(czCities.size());
        for (String city : czCities) {
            String[] parts = city.split("[-\\s+]");
            for (String part : parts) {
                part = part.trim();
                if (part.length() > 0) {
                    result.add(part.toLowerCase());
                }
            }
        }

        return result;
    }

    public static String IGNORE_OZ_IN_CORPUS = " e r t n l c p m d y b h g f j x w q t  an j al ic va li tr pr ch ek av ec ac lo ot ik mo om x ba w ak rt ova il ob ed ur vo lu sa sp ca ab ny " +
            " ie ns ea ev iv im pl ng nd sh str kr est oc ep rk nk un ga ag lin sl eni he rm ge rv ss por ali dl oz mp ska ser ist sv tn hl mar sm oo dn fe eri spo ln rc uk ote sti ra " +
            " dy ts sc gi ib ai pu eo tk cl ki vs eh eg nu ax ds rb kv cr mb yd uh ef yb rl ym vk nn wa cu nl tl vr pt lm ow vl sr hn yr fl rp nf gu tc zk pp zu lf dk ft yo tb pn " +
            " wo vb uv lz ya lz sb ay tp sd lb ml xi nv bs aa dc dm rr ff rf mm kp ix fu cy zp vu lv pk np vc oe cb dh jk rh nb cc dd cm bk mt vp nm gh cp hm bb kd tw jd cv " +
            " jl sf ye xe zm sz tz uf mf md lg jp hb gm vd vt mh bp fk sg pb mv dg pd xy xs ii jv vh mg gb kj dw wl jz ww nw vj cf mw hf bj sq eq vv bf gf kw wp hz cj wb " +
            " gw fn cw hg pw rx jg wu vw qs jw tx qi xv qt cx px qc lx wv lq ni st ar er ni en ce ka ko le es el et de ad op di pe em io ap bi uc su du gc fy ces la pola ece tylu icky phan" +
            " poc tes inshop ph ol pis hr da ut nastro vent telu ark setin pulford lob koln au kc klu on ink ex kora ex che koleni teza mika ecs ";

    private static StringIndex adjustCorpusIndex(StringIndex allCorpus2FilteredDomains) {
        log.info("Adjusting corpus2domain index...");

        StringIndex result = new StringIndex();
        for (String key : allCorpus2FilteredDomains.getKeys()) {
            String kk = " " + key + " ";
            if (IGNORE_OZ_IN_CORPUS.contains(kk)) continue;

            for (String value : allCorpus2FilteredDomains.get(key)) {
                if (value.substring(0, value.length() - 3).contains(key)) {
                    result.put(key, value);
                }
            }
        }

        log.info("Adjusted...");

        return result;
    }


    public static StringIndex createAndSaveReverseIndex(StringIndex corpus2domains, String filename) throws IOException {
        log.info("Creating reverse corpus2domains index...");
        StringIndex domains2corpus = new StringIndex();
        for (String key : corpus2domains.getKeys()) {
            ArrayList<String> strings = corpus2domains.get(key);
            for (String value : strings) {
                domains2corpus.put(value, key);
            }
        }

        for (String domain : domains2corpus.getKeys()) {
            ArrayList<String> keys = domains2corpus.get(domain);
            Collections.sort(keys, new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return o2.length() - o1.length();
                }
            });
        }

        log.info("Done...");
        domains2corpus.saveIndex(filename);

        return domains2corpus;
    }

    public static void updateDomainsToCorpusForKey(StringIndex corpus2filteredDomains, StringIndex domains2substring, StringIndex domain2corpus, String newWord) throws IOException {
        List<String> domainsWithKey = corpus2filteredDomains.get(newWord);
        if (domainsWithKey == null) {
            return;
        }

        for (String domain : domainsWithKey) {
            ArrayList<String> list = domain2corpus.get(domain);

            String dd = domain.substring(0, domain.length() - ".cz".length());
            domains2substring.deleteKey(dd);
            if (list != null) {
                countSubstringRelevance(domains2substring, domain, list);
            }
        }
    }

    public static void buildAndSaveDomainsToCorpus(StringIndex corpus2filteredDomains, String filename) throws IOException {
        StringIndex domains2corpus = createAndSaveReverseIndex(corpus2filteredDomains, "domain2corpus.txt");

        log.info("Counting substring relevance...");
        StringIndex domainsSplit = new StringIndex();
        int count = 0;
        for (String domain : domains2corpus.getKeys()) {
            ArrayList<String> list = domains2corpus.get(domain);
//            if (!domain.equals("ventrocentrum.cz")) {
//                continue;
//            }
            countSubstringRelevance(domainsSplit, domain, list);

            count++;
            if (count % 1000 == 0) {
                log.info("Counted: " + count);
                   break;
            }
        }

        log.info("Saving substring relevance...");
        domainsSplit.saveIndex(filename);
    }

    public static List<String> getDomainsForOz(String oz, StringIndex index) {
        String[] parts = oz.split("[ ]+");
        List<String> list = null;
        int minList = Integer.MAX_VALUE;

        for (String part : parts) {
            part = part.trim();
            if (part.length() > 0) {
                List<String> l = index.get(part);
                if (l != null && l.size() < minList) {
                    minList = l.size();
                    list = l;
                }
            }
        }

        if (list == null) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>(list.size());
        for (String domain : list) {
            if (ozContainedInDomain(oz, domain)) {
                result.add(domain);
            }
        }

        return result;
    }

    private static boolean ozContainedInDomain(String[] ozParts, String domain) {
        for (String part : ozParts) {
            part = part.trim();
            if (part.length() > 0) {
                if (!domain.contains(part)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean ozContainedInDomain(String oz, String domain) {
        String[] parts = oz.split("[ ]+");
        return ozContainedInDomain(parts, domain);
    }

    private static String[] getRelevantParts(String oz) {
        return oz.trim().split("[ ]+");
    }

    public static List<String> getRelevantKeys(String oz) {
        String[] parts = getRelevantParts(oz);
        List<String> keys = new ArrayList<String>(parts.length);

        for (String p : parts) {
            p = p.trim();
            if (p.length() >= MINIMUM_CHARS_OF_KEYS && p.length() <= MAXIMUM_CHARS_OF_KEYS) {
                keys.add(p);
            }
        }

        return keys;
    }

    private static Set<String> generateKeys(Set<String> filteredOz) {
        log.info("Generating keys for filtered OZ... Chars length: " + MINIMUM_CHARS_OF_KEYS + "-" + MAXIMUM_CHARS_OF_KEYS);
        Set<String> keys = new HashSet<String>(filteredOz.size());
        for (String oz : filteredOz) {
            List<String> relevantKeys = getRelevantKeys(oz);
            keys.addAll(relevantKeys);
        }

        log.info("Amount of distinct keys in filtered OZ: " + keys.size());
        return keys;
    }

    private static List<String> loadRawDomains(String filename) throws IOException {
        log.info("Start loading CZ existing domains...");
        BufferedReader br = new BufferedReader(new FileReader(filename));
        List<String> domains = new ArrayList<String>(100 * 1000);
        while (true) {
            String line = br.readLine();
            if (line == null) break;

            domains.add(line.trim());
        }

        br.close();
        log.info("Done.");
        return domains;
    }

    public static List<String> loadRawOz(String filename) throws IOException {
        log.info("Start loading OZ...");
        BufferedReader br = new BufferedReader(new FileReader(filename));
        List<String> upv = new ArrayList<String>(100 * 1000);
        while (true) {
            String line = br.readLine();
            if (line == null) break;

            upv.add(line.trim());
        }

        br.close();
        log.info("Done.");

        return upv;
    }

    // Include this workds to CZ slovnik
    private static String ADDITIONAL_CZ_WORDS = "elektro profi  arka  lekarna  mobil  fest  antik  drevostavby  fajn  alex  jelinek  zeman  cajovna  wiki  giga  fenix  berger  jirka  blazek  katka  farmar  zastavarna  " +
            "sedlacek  vitek  beranek  kraus  javor  beruska  sazava  obora  erotika  mozaika  alenka  denisa  vestirna  veverka  burian  barborka  hlavacek  motylek  matyas  nikola  " +
            "kopecek  jarka  sofie  elias  artemis  pastelka  ostravice  medvidek  kasparek  bylinka  vilem  zdravicko  amalka  honzik  cestovka  chladek  chalupar  moravka  berounka " +
            " praded  dasa  karolinka  svice  krab  holecek  kleopatra  krcmar  mazel  jagr  klubicko  tlapka  janacek  ikona  veselka  polstarky  matysek  papousci  vorisek  dealer  krater  " +
            "odra  medunka  matejovsky  kristina  baroko  bobes  balada  svestka  koktejl  kaskada  josefina  kentaur  komornik  kolovrat  prosperita  tucnak  bublinka  vydra  elisa  hejtman  " +
            "chobot  prcek  marias  madlenka  bambule  bobik  vanesa  jasan  antonie  patricie  livia  bumerang  otahal  mazlik  angelika  farao  mourek  jezdectvi  tahak  lahudka  delfinek  olivie  " +
            "bukovsky  ostravak  alchymie  nicole  jednorozec  hawai  mazak  activa  permonik  lahoda  rarasek  gavlas  gaviota  nomis  herbadent  promedica  slagr  ineco  rando  consulta  rolla  abakus  " +
            "brixton  redpoint  libos  onga  profitex  ufon  kocicka  jahudka  virtuos  bazalka  prokupek  valdstejn  trhak  podloubi  moravice  budulinek  felicita  kvadro  svacinka  sasanka  rokoko  relaxuj " +
            " yvetta  klidek  meduza  kurzovni  alpska  cipisek  tapka  legionar  kominicek  filatelie  stopka  krivak  vcelicka  pericko  septima  velryba  pusinka  stribrnak  polarka  akcelerator  bokovka  evelina  " +
            "bedrunka  dorotka  permonicek  polda  claudie  apatykar  tygrik  katapult  klaudie  restart  klaudia  skolacek  zlaticko  lednacek  kutnar  brusinka  cykloservis  snehulak  zlataky  kratasy  certovina " +
            " rebeca  krajanka  parohac  spartakus  kotleta  ryzak  bambulka  tomicek  magnat  bazilika  bemex  altanek  nadoraz  pastyrka  zverokruh  chutovka  oltar  kozarstvi  leharo  arcivevoda  knoflicek  " +
            "divozenka  masarka  strepiny  jarmark  sluzebnicek  odpadlici  benzinka  rostlinka  zakladka  aloe  koala  poprask  krystal  zizalky  chobotnice  taboracek  natalie  valentin  smajlik  smolik  masinka " +
            " christian  sebastian  litina  roland  vaclav  inventura  achat  karin  mimino  apokalypsa  blondyna  meluzina  probost  rastislav  dablik  rejsek  balet  kaktus  veronika  karina  karlik  slapaci " +
            " krizovka  dragoun  veranda  jewels  zdislava  andilci  satlava  konvikt  samuraj  translator  polednik  radiator  majordomus  pavucina  rytina digestor smenarna " +
            " oplechovani diplomky nepisto jachticka bejkarna biatlon seznamme cesko esoteriky husiti nausnic hrej morava vychodocesky kamnarstvi advokati potapecska kineziologie " +
            " vychodocesky kamnarstvi advokati potapecska kineziologie starozitny bazenove calounictvi roznos parafinove opatovsky heligonkari stramberska necesany palirna samotisky holding pension " +
            " jablunkov bojkovice tereziansky nozicka bydlete vrakoviste sladovna podlaharstvi exekutorska geodeti vlastovicka dudlikarna slonecek " +
            " skakaci mimi klanovicky modrany kosmeticky homeopatie dharma hynkovo cytogenetika cytogenetika telupilova polib zlatnictvi hrebecek blazejova " +
            " vitkova kobylka zamluv vladka tesarstvi sklenarstvi fyzioterapie truhlarna jiskrive plisek zasklivani naradicko vinnetou hrbacek kutnohorsko uklidy " +
            " rozvoz drevorezba futsal frazova zasklivani mandragora janik statika jelinku promotion novakovy lakyrnictvi triatlon mandlovani hrobarici malirstvi " +
            " trampoliny jungrova insolvence kovarstvi exotika prostejovsko mrazak detoxikacni krejcovstvi genealogie kadernicky farmarske kasparka dendrologicka " +
            " ctyrkolka milosrdni bosonohy krusovicka urologie endokrinologie kardiologie poledance dementi epilace neurologie ortopedie gynekologie diabetologie alergologie hematologie imunologie " +
            " poklice statikl progres balustrady bozkova polynesie polytechnik lipolyza liposukce polygrafie polyamid polycarbon polycarbonat polyuretan polyuretanov kartograf lecitel kartove skartovace " +
            " skartovacky skartovat sadrokartony veterina rytectvi rotopedy ergometry synergogika safirlt skutryl podnikej podnikejme podnikejte mednikem domoval domulte lopatel matikal hydraulika spindl eko" +
            " herlikovice klikov majales konvektomaty esencialni koralek krakora sikora kokora sykora zenovy kutnohorska safir schejbal simanovsky skorpil skuhrovec skutry slovjak smetak polotovar apolo topte " +
            " elektromontazes lakovna bohemika genomika farmako cajove konvicky konsolidace kontaktni rakovecke rakovec rakowski makowski makowitz makovicka makovec autobazary apartman " +
            " letenka pomnenka ztracenka dasenka drevenka klicenka irenka slamenka mshousenka hajenka vsepro vzdelavejte webdesign pedikura ctyrkolky autodiagnostika prognostika " +
            " homeopatika chromatika numismatika politika nmatika geodetika personalistika ortopedicka ortopedicke designerka dispecerka automoto sadrokarton palenice moravskoslezska " +
            " moravske moravistika klempir geodet geodezie plovarna lahvarna kovarna richard simon mares necky orava makler repro barrandov puzzle motorest vana laguna potisk dodomu " +
            " dula burda karavan florian radon diskont elektro senza kafka chlum elektromontaze silesia bazarek fischer stolarstvi stolar oliver jonas trojan tehotenska hajovna dalkia audio audit ";


    public static Set<String> loadCZDictionary(String filename, GlobalStringTable global) throws IOException {
        log.info("Loading CZ dictionary...");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), Charset.forName("windows-1250")));
        Set<String> result = new HashSet<String>(10 * 1000);
        while (true) {
            String line = br.readLine();
            if (line == null) break;

            line = line.trim();
            if (line.equals("")) continue;

            String normalizedKey = normalizeString(line);
            result.add(global.add(normalizedKey));
        }
        br.close();

        String[] parts = ADDITIONAL_CZ_WORDS.split("[ ]+");
        for (String p : parts) {
            p = p.trim();
            if (!p.equals("")) {
                result.add(p);
            }
        }

        log.info("Loaded: " + result.size());
        return result;
    }

    public static Set<String> loadCZCities(String filename, GlobalStringTable global) throws IOException {
        log.info("Loading CZ cities...");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), Charset.forName("utf8")));
        Set<String> result = new HashSet<String>(10 * 1000);
        while (true) {
            String line = br.readLine();
            if (line == null) break;

            line = line.trim();
            if (line.equals("")) continue;

            String normalizedKey = normalizeString(line);
            result.add(global.add(normalizedKey));
        }
        br.close();

        log.info("Loaded: " + result.size());
        return result;
    }

    public static Set<String> loadCZNames(String filename, GlobalStringTable global) throws IOException {
        log.info("Loading CZ names...");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), Charset.forName("windows-1250")));
        Set<String> result = new HashSet<String>(10 * 1000);
        while (true) {
            String line = br.readLine();
            if (line == null) break;

            line = line.trim();
            if (line.equals("")) continue;

            String[] parts = line.split("\\t+");
            String name = parts[0];
            if (name.equals("prijmeni")) continue;

            parts = name.split("\\s+");
            for (String part : parts) {
                if (part.length() >= 4) {
                    String normalizedKey = normalizeString(part);
                    result.add(global.add(normalizedKey));
                }
            }
        }

        br.close();

        log.info("Loaded: " + result.size());
        return result;
    }

    private static List<String> filterDomains(List<String> domains) {
        int maximum_domain_parts = 4;
        log.info("Filter out domains... Maximum domain parts: " + maximum_domain_parts);

        List<String> filteredDomains = new ArrayList<String>(domains.size());
        for (String domain : domains) {
            String[] parts = domain.split("-");
            int countParts = 0;
            for (String p : parts) {
                if (!p.trim().equals("")) countParts++;
            }

            if (countParts <= maximum_domain_parts) {
                filteredDomains.add(domain);
            }
        }

        log.info("Filtered domains: " + filteredDomains.size());
        return filteredDomains;
    }


    public static String OZ_FORCE_ADD = " skoda apple apples renault avon iphone hyundai opel citroen bosch toyota honda audi google mercedes peugeot suzuki samsung facebook nordic subaru " +
            " android nokia lego lega porsche tatra nissan nike enduro mazda tesco windows minecraft allianz jawa jeep zverimex ikea segway adidas mitsubishi braun xbox onyx ferratum gourmet " +
            " youtube dedra lidl panasonic vibro zetor patria barbie csob acer tescoma husqvarna dell bitcoin vodafone karcher husky nikon lenovo seat aukro tchibo skype " +
            " ipad playstation ferrari hummer aston flamenco alza thule provident chevrolet philips portas lomax jaguar penny volkswagen ptakoviny " +
            " idos okay billa disney landrover salomon hitachi harley zepter iprima saab joomla sapeli csas snapback narex lamborghini hannah chrysler microsoft electrolux invia knihcentrum gambrinus " +
            " fabia wordpress fiskars octavia budweiser envy solingen replay loreal cadillac subway neckermann lexus dunlop pronatal chopper ipod corel ericsson tomtom thalia michelin mondeo tipsport " +
            " blackberry ulozto sodexo calvin velox ogilvy ebay ducati kaufland goodwill jacobs nintendo vichy gardena prestashop uniqa vimax kofola intersport velorex elektromont merida merino felicia " +
            " royce besip logan dior pepsi rejoice motorola agrofert cisco bentley toshiba kodak daihatsu mattel dressler lindab adobe gradior mogul fermacell ubuntu whirlpool " +
            " avizo coleman budvar staropramen purina hornbach magnesia passat wrangler osram herbalife disneyland vacushape gillette sinclair gorenje madeta durisol agip ferrum " +
            " nordica corazon hitec slevomat fuji fatboy starbucks nivea marlenka reebok wedos viagra mammut civic mocca lidovky umbro nestle govinda xterra shell chevron daimler ford vodafone " +
            " amazon adobe oracle cisco yahoo bmw kia puma nilfisk ";

    public static String OZ_FORCE_IGNORE = " club real portal fitness bike fashion best point aqua metal free smart relax power energy instal easy cool inka moravia wood security racing anka gold hobby color " +
            " poker happy open tattoo flex tina express link anet project truck face royal creative master region jobs credit maxi well logo image play retro port aero west profit king therm toner europe " +
            " fast senior future craft people vodo revue board decor silver living direct international elite money disco holiday yacht homeo ultra snow cargo nature chip crystal bach bonus bios squash " +
            " turbo angel diamond spirit quick fort rally beach pink progress diesel ride comfort zoom wind adventure escort forte story wall thermo logic sunny storm panda dreams kiss simple optimal " +
            " dragon globe cash benefit bond guard universal contact girl cent trial develop hero coach summit bridge empire clip banner celebrity freestyle monitoring kick frame hello shock singer pretty " +
            " challenge block meteor interactive bear lifestyle excellent secret duel like silent economy filter victory gladiator guess freedom quartet developer excelent erekce break pluto grace underground " +
            " custom tracker jupiter slice popular faster strong keep jungle portfolio record here complete dial sphere exact ultimate webshop billboard enterprise platinum division boots webmaster excalibur " +
            " exotic parlament success explorer cashflow cardio champion finger sunrise investing this pussy danger buffalo medusa skeleton professionals symphony charisma pinguin dawn kamasutra " +
            " blackhawk pointer colorado michelangelo portrait commander surprise nevada survive challenger stavo dekor vera next poly mach urban flash kart bauer nika prof reno ergo ling classic " +
            " just praga luna cube spin nemo base opti ever lion ance orange hell click surf leon richter rande diana jack trek gear hall activ boom sara chilli motion deko york room " +
            " acko river galaxy duda koliba brain janka matrix klimes level klein endy nikol raut mata maka gurman afro rain adler semi geek twin andre mikes zamecka wagner benda barton machac " +
            " doctor deep borek olda dona bird heart nina grunt kaja flek only clear save kiwi verner mates target paja lupa choc rand korbel neumann kaiser uran e4you true sandra done charlie " +
            " embassy into betty guest enjoy silva domain else rubin jansky victor bulldog sklar dada fabian grey seifert complet transit austerlitz karasek zeus brother visio forever pitbull " +
            " attack sara architect impact arrow vorel self usvit cake kristian cherry elements upol scott kavka thomas gloria slunecna obchodak root jarosov marathon rambo " +
            " group info euro team cafe bohemia trade tour travel music trans taxi soft photo comp world cars rock blog life home agency porno star nice land dent acek " +
            " wellness live beauty tuning dance glass office server icek blue black green global solar outdoor camp joga partners apart time optik academy mobile solutions" +
            " magic steel enka news hotels dream 2000 love energo fresh digital factory hair mikro mark book sound audit light show clinic fish multi work casino promo " +
            " family kids last astro street 2014 tika weby alka hydro gallery alarm erka nano zumba sign guru 2013 lastminute prag esign speed judo fire country" +
            " elka youga card  farm offroad solution stone hockey central lucky envi medica guide rovi swiss berg wolf crazy techno villa pack virtual astra reko tube event trika " +
            " core elko infra wifi control 2012 centre realit revival form meteo heat bara clean dark pool works adro ball inline books stars pale term nails door beer klik fine " +
            " rich hill carp white elek industry bella bart muller basket edia elektros page pure flexi agri fair tantra pyro paradise fantasy micro bures united arte aura hard " +
            " ulka inet jeans ekon riders kala odesign forest trip orka alik concept resort nica stol shopping road autop cloud lang sams eska erky neuro porta orion professional " +
            " toma flor wear medic focus cyber ella arts boss safe iska harmony bene esto equi manager regio atex rena sweet burger elektra clever 2010 visage museum maja" +
            " tree 3000 blues nord chod ekos days paul kana 1000 mpro visual look logistic smile dukla polo therapy final iris salsa erie marina ista olymp isport alpy rium " +
            " kace alpha miko wave tronic simply cinema health facility flowers tara deco orest victoria movie capital head wild shoes stream natural boutique elis alma tika" +
            " boat nito garage skov slim prestige first zech alta alba angels pema moon mich kite weber puls bina bera beat esro vala inex epro tool boys kubik pixel apollo " +
            " imex records andy apex fila chool ticket lights dino nela massage touch oska ilka tennis caravan mara queen tone watch mind tron bona john sina stas camping profis " +
            " quad piano bijoux elit network grill mako mana animal mobil friends long rescue hand bags atech strip action dynamic karna zero marine bonsai effect otto extreme " +
            " gabi rnet made rainbow north bern roll spro scan code musica ikon olin robo airport ters yoga autos sushi forex glamour impex patchwork remax pegas elektro " +
            " domus arnika linea tolar ktrade helios electric fruit mdesign young anima monte idesign rapid berry atour enter property spectrum staro vista hiphop optima ilove " +
            " shops mplus technic spinning bohemica exchange report polis force komat connect kinder class prost dotop ishop journal maria balance rallye    ";

    public static Set<String> filterOZ(Set<String> ozKeys, Set<String> czDictionary) {
        log.info("Filter out OZ...");

        Set<String> result = new HashSet<String>(ozKeys.size());
        int ignored = 0;
        for (String keys : ozKeys) {
            String[] parts = keys.split("[ ]+");

            if (parts.length >= MINIMUM_PARTS_OF_OZ && parts.length <= MAXIMUM_PARTS_OF_OZ) {
                String key = parts[0].trim();
                if (OZ_FORCE_ADD.contains(key + " ")) {
//                    System.out.println("Force add to OZ: " + key);
                    result.add(keys);
                } else if (OZ_FORCE_IGNORE.contains(key + " ")) {
//                    System.out.println("Ignoring OZ from FORCE ignore list: " + key);
                    ignored++;
                } else if (czDictionary.contains(key)) {
//                    System.out.println("Ignoring OZ as a CZ word: " + key);
                    ignored++;
//                    } else if (enDictionary.contains(key)) {
//                        System.out.println("Ignoring OZ as a EN word: " + key);
//                        ignored++;
                } else if (key.endsWith("ova")) {
//                    System.out.println("Ignoring OZ as a OVA word: " + key);
                    ignored++;
                } else {
                    if (key.length() >= MINIMUM_CHARS_OF_OZ && key.length() <= MAXIMUM_CHARS_OF_OZ) {
                        result.add(keys);
                    }
                }
            }
        }

        log.info(String.format("Filtered OZ: %s Ignored OZ: %d", result.size(), ignored));
        return result;
    }

    public static Set<StatOz> filterOZ2(Set<StatOz> ozKeys, Set<String> czDictionary) {
        log.info("Filter out OZ...");

        Set<StatOz> result = new HashSet<StatOz>(ozKeys.size());
        int ignored = 0;
        for (StatOz oz : ozKeys) {
            String keys = oz.getName();
            String[] parts = keys.split("[ ]+");

            if (parts.length >= MINIMUM_PARTS_OF_OZ && parts.length <= MAXIMUM_PARTS_OF_OZ) {
                String key = parts[0].trim();
                if (OZ_FORCE_ADD.contains(" " + key + " ")) {
                    result.add(oz);
                } else if (OZ_FORCE_IGNORE.contains(" " + key + " ")) {
                    ignored++;
                } else if (czDictionary.contains(key)) {
                    ignored++;
                } else if (key.endsWith("ova")) {
                    ignored++;
                } else {
                    if (key.length() >= MINIMUM_CHARS_OF_OZ && key.length() <= MAXIMUM_CHARS_OF_OZ) {
                        result.add(oz);
                    }
                }
            }
        }

        log.info(String.format("Filtered OZ: %s Ignored OZ: %d", result.size(), ignored));
        return result;
    }

    private static void buildAndSaveKeysToCorpusIndex(Set<String> keys, Set<String> allCorpus, String filename) throws IOException {
        StringIndex index = new StringIndex();

        int count = 0;
        int records = 0;
        for (String key : keys) {
            for (String corpus : allCorpus) {
                if (!corpus.equals(key) && corpus.contains(key)) {
                    index.put(key, corpus);
                    records++;
                }
            }

            count++;
            if (count % 1000 == 0) {
                System.out.println("Indexed: " + count);
//                                break;
            }
        }

        log.info(String.format("%d keys (OZ) added to index. %d records created.", index.countKeys(), records));
        index.saveIndex(filename);
    }

    private static ArrayList<String> findHits(StringIndex keysToDomainsIndex, StringIndex keysToCorpusIndex, Set<String> allCorpus, String[] ozParts, String originalOz) {
        ArrayList<String> minList = null;
        for (String oz : ozParts) {
            ArrayList<String> list = keysToDomainsIndex.get(oz);
            if (list != null) {
                if (minList == null || list.size() < minList.size()) {
                    minList = list;
                }
            }
        }

        List<String> keys = getRelevantKeys(originalOz);
        ArrayList<String> result = new ArrayList<String>();
        for (String domainHit : minList) {
            if (domainHit.endsWith(".cz")) {
                domainHit = domainHit.substring(0, domainHit.length() - 3);
            }

            if (!ozContainedInDomain(ozParts, domainHit)) {
                continue;
            }

            boolean ozCorrect = false;
            for (String key : keys) {
                ArrayList<String> corpuses = keysToCorpusIndex.get(key);

                int start = 0;
                boolean domainCorrect = false;
                while (true) {
                    int ozStart = domainHit.indexOf(key, start);
                    if (ozStart < 0) break;
                    if (ozStart != 0 && ozStart + key.length() != domainHit.length() - ".cz".length()) {
                        break;
                    }

                    int ozEnd = ozStart + key.length() - 1;

                    // Pro kazdy nalez OZ v domene ...
                    boolean corpusFound = false;
                    if (corpuses != null) {
                        for (String corpus : corpuses) {
                            int start2 = 0;
                            while (true) {
                                int j = domainHit.indexOf(corpus, start2);
                                if (j < 0) break;

                                int corpusStart = j;
                                int corpusEnd = j + corpus.length() - 1; // inclusive

                                if (ozStart >= corpusStart && ozEnd <= corpusEnd) {
                                    corpusFound = true;
                                    break;
                                }

                                start2 = corpusStart + 1;
                            }

                            if (corpusFound) {
                                break;
                            }
                        }
                    }

                    if (!corpusFound) {
                        domainCorrect = true;
                        break;
                    }

                    start = ozStart + 1;
                }

                if (domainCorrect) {
                    ozCorrect = true;
                    break;
                }
            }

            if (ozCorrect) {
                result.add(domainHit);
            }
        }

        return result;
    }

    public static Set<String> loadCorpus(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        Set<String> corpus = new HashSet<String>(100 * 1000);
        while (true) {
            String line = br.readLine();
            if (line == null) break;

            corpus.add(line.trim());

        }
        br.close();

        return corpus;
    }

    public static void createAndSaveAllCorpus(String filename, Set<String> oz, Set<String> czCorpus, Set<String> enCorpus) throws IOException {
        Set<String> allCorpus = new HashSet<String>(czCorpus);
        allCorpus.addAll(enCorpus);

        for (String o : oz) {
            allCorpus.addAll(getRelevantKeys(o));
        }

        System.out.println("Ccorpus size: " + allCorpus.size());
        BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
        for (String word : allCorpus) {
            bw.write(word);
            bw.newLine();
        }
        bw.close();
    }

    private static String normalizeString(String s) {
        if (s == null) return null;

        String noAccent = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        noAccent = pattern.matcher(noAccent).replaceAll("");

        return noAccent.replaceAll("\\s+", " ").toLowerCase();
    }

    public static Set<String> loadCZCorpus(String filename, GlobalStringTable global) throws IOException {
        log.info("Loading CZ corpus...");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), Charset.forName("windows-1250")));
        Set<String> result = new HashSet<String>(10 * 1000);
        while (true) {
            String line = br.readLine();
            if (line == null) break;

            line = line.trim();
            if (line.equals("")) continue;

            String[] parts = line.split("\\t");
            String key = parts[1].trim();
            if (key.equals("")) throw new IllegalStateException();

            String normalizedKey = normalizeString(key);
            result.add(global.add(normalizedKey));
        }
        br.close();

        log.info("Loaded: " + result.size());
        return result;
    }

    private String ENwithNUmbers = "10th 11th 12th 13th 14th 15th 16th 17th 18th 19th 1st 20th 24th 25th 26th 27th 28th 29th 2nd 3d 3rd 4th 5th 6th 7th 8th 9th i18n number1 y2k y2kcompliant";

    private static Pattern p = Pattern.compile(".*[0-9].*");

    public static Set<String> loadENCorpus(String filename, GlobalStringTable global) throws IOException {
//        BufferedReader br = new BufferedReader(new FileReader(filename));
        log.info("Loading EN corpus...");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), Charset.defaultCharset()));
        Set<String> result = new HashSet<String>(10 * 1000);
        boolean ignore = true;
        while (true) {
            String line = br.readLine();
            if (line == null) break;

            line = line.trim();
            if (line.equals("")) continue;
            if (line.startsWith("#")) continue;

            if (line.contains("-fold")) ignore = false;
            if (ignore) continue;

            String[] parts = line.split("\\t", 2);
            String key = parts[0].trim();
            if (key.equals("")) throw new IllegalStateException();

            // Ignore words with numbers...
            if (p.matcher(key).matches()) {
                continue;
            }

            String normalizedKey = normalizeString(key);
            String onlyDigitsAndChars = normalizedKey.replaceAll("[^a-z ]", "");

            String[] words = onlyDigitsAndChars.split(" ");
            for (String word : words) {
                word = word.trim();
                if (word.equals("")) continue;

                result.add(global.add(word));
            }

        }
        br.close();

        log.info("Loaded: " + result.size());
        return result;
    }


    public static void updateIndexWithNewKey(StringIndex index, String newKey, List<String> values) {
        index.deleteKey(newKey);
        for (String domain : values) {
            if (domain.contains(newKey)) {
                index.put(newKey, domain);
            }
        }
    }


    public static int buildIndexForKey(StringIndex index, String key, List<String> domains) throws IOException {
        key = key.trim();
        if (key.length() == 0) return 0;

        index.deleteKey(key);
        int records = 0;
        for (String domain : domains) {

            if (domain.contains(key)) {
                index.put(key, domain);
                records++;
            }
        }

        return records;
    }

    public static StringIndex buildAndSaveIndex(Set<String> keys, List<String> domains, String filename) throws IOException {
        StringIndex index = new StringIndex();

        int count = 0;
        int records = 0;
        for (String key : keys) {
            records += buildIndexForKey(index, key, domains);
            count++;
            if (count % 1000 == 0) {
                System.out.println("Indexed: " + count);
                //break;
            }
        }

        log.info(String.format("%d keys (OZ) added to index. %d records created.", index.countKeys(), records));
        index.saveIndex(filename);

        return index;
    }

    private static void printStats(List<String> czDomains, List<String> oz) {
        long domainsCharacterSize = 0;
        long averageParts = 0;
        int[] partsCount = new int[20];
        for (String domain : czDomains) {
            if (!domain.endsWith(".cz")) throw new IllegalStateException();

            domainsCharacterSize += domain.length() - 3;
            String[] parts = domain.split("-");
            int countParts = 0;
            for (String p : parts) {
                if (!p.trim().equals("")) countParts++;
            }

            partsCount[countParts]++;
            averageParts += countParts;
        }

        System.out.println(String.format("All CZ existing domains: %d", czDomains.size()));
        System.out.println(String.format("Average domain size in characters: %f", domainsCharacterSize * 1.0 / czDomains.size()));
        System.out.println(String.format("Average domain parts: %f", averageParts * 1.0 / czDomains.size()));
        for (int i = 0; i < partsCount.length; ++i) {
            System.out.println(String.format("  Count for %d parts: %d (%5.1f%%)", i, partsCount[i], partsCount[i] * 100.0 / czDomains.size()));
        }

        long ozCharacterSize = 0;
        int maxOzCharacterSize = 0;
        long averageKeysParts = 0;
        int[] keysPartsCount = new int[100];
        int[] charsCount = new int[300];
        int[] charsCount4Parts = new int[100];

        Set<String> ozKeys = new HashSet<String>(oz);
        int countOzUpTo4Parts = 0;
        for (String keys : ozKeys) {
            ozCharacterSize += keys.length();
            if (keys.length() > maxOzCharacterSize) {
                maxOzCharacterSize = keys.length();
            }
            String[] parts = keys.split("[ ]+");
            averageKeysParts += parts.length;
            keysPartsCount[parts.length]++;
            charsCount[keys.length()]++;
            if (parts.length >= MINIMUM_PARTS_OF_OZ && parts.length <= MAXIMUM_PARTS_OF_OZ) {
                countOzUpTo4Parts++;
                charsCount4Parts[keys.length()]++;
            }
        }

        System.out.println(String.format("All OZ records: %d", oz.size()));
        System.out.println(String.format("All OZ no.duplicates: %d", ozKeys.size()));
        System.out.println(String.format("Average OZ size in characters: %f", ozCharacterSize * 1.0 / ozKeys.size()));
        System.out.println(String.format("Max OZ size in characters: %d", maxOzCharacterSize));
        System.out.println(String.format("Average OZ parts: %f", averageKeysParts * 1.0 / ozKeys.size()));
//        for (int i = 0; i < keysPartsCount.length; ++i) {
        for (int i = 0; i < 10; ++i) {
            System.out.println(String.format("  Count for %d parts: %d (%5.1f%%)", i, keysPartsCount[i], keysPartsCount[i] * 100.0 / ozKeys.size()));
        }

        double charactersCoverage = 0;
        for (int i = 0; i < charsCount.length; ++i) {
            double d = charsCount[i] * 100.0 / ozKeys.size();
//            System.out.println(String.format("  Count for %d characters: %d (%8.3f%%)", i, charsCount[i], d));
            if (i >= MINIMUM_CHARS_OF_OZ && i <= MAXIMUM_CHARS_OF_OZ) {
                charactersCoverage += d;
            }
        }

        System.out.println(String.format("  OZ covered with threshold: (%8.3f%%)", charactersCoverage));
        System.out.println();

        charactersCoverage = 0;
        for (int i = 0; i < charsCount4Parts.length; ++i) {
            double d = charsCount4Parts[i] * 100.0 / countOzUpTo4Parts;
//            System.out.println(String.format("  Count4parts for %d characters: %d (%8.3f%%)", i, charsCount4Parts[i], d));
            if (i >= MINIMUM_CHARS_OF_OZ && i <= MAXIMUM_CHARS_OF_OZ) {
                charactersCoverage += d;
            }
        }

        System.out.println(String.format("  OZ4parts covered with threshold: (%8.3f%%)", charactersCoverage));

    }

    private static boolean stringStartsWithAtPosition(String bigString, String part, int position) {
        return bigString.regionMatches(position, part, 0, part.length());
    }

    static class State {
        public String substring;
        public int position;
        public int usedChars;
        public int skippedChars;

        State(String substring, int position, int usedChars, int skippedChars) {
            this.substring = substring;
            this.position = position;
            this.usedChars = usedChars;
            this.skippedChars = skippedChars;
        }
    }

    private static void countSubstringRelevance(String originalString, int startingOffset, List<String> substrings, LinkedList<State> currentState, LinkedList<State> bestState) {
        State best = bestState.peekLast();
        State current = currentState.peekLast();

        if (best != null && current != null && current.skippedChars > best.skippedChars) {
            return;
        }

        boolean prolonged = false;
        if (startingOffset < originalString.length() - 2) {
            for (int i = startingOffset; i < originalString.length(); ++i) {
                for (int j = 0; j < substrings.size(); j++) {
                    String part = substrings.get(j);
//                if (part.length() <= 2) continue;

                    if (stringStartsWithAtPosition(originalString, part, i)) {
                        int newUsedChars = (current == null ? 0 : current.usedChars) + part.length();
                        int newSkippedChars = (current == null ? 0 : current.skippedChars) + i - startingOffset;

                        currentState.add(new State(part, i, newUsedChars, newSkippedChars));
                        countSubstringRelevance(originalString, i + part.length(), substrings, currentState, bestState);
                        currentState.removeLast();

                        prolonged = true;
                    }
                }
            }
        }

        if (!prolonged) {
            if (best == null || (current != null && equalsState(currentState, bestState) > 0)) {
                bestState.clear();
                bestState.addAll(currentState);

                if (current == null) {
                    current = new State(originalString, 0, 0, originalString.length());
                    bestState.add(current);
                } else {
                    current.skippedChars += originalString.length() - startingOffset;
                }
            }
        }
    }

    static int[] LENA = new int[30];
    static int[] LENB = new int[30];

    private static int equalsState(LinkedList<State> a, LinkedList<State> b) {
        State aTop = a.peekLast();
        State bTop = b.peekLast();

        if (aTop == null) {
            if (bTop == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (bTop == null) {
                return 1;
            }
        }

        if (aTop.usedChars != bTop.usedChars) {
            return aTop.usedChars - bTop.usedChars;
        }

        Arrays.fill(LENA, 0);
        Arrays.fill(LENB, 0);

        for (State as : a) {
            LENA[as.substring.length()]++;
        }

        for (State bs : b) {
            LENB[bs.substring.length()]++;
        }

        for (int i = LENA.length - 1; i >= 0; --i) {
            if (LENA[i] != LENB[i]) {
                return LENA[i] - LENB[i];
            }
        }

        return 0;
    }

    public static void countSubstringRelevance(StringIndex domainsSplit, String originalString, List<String> substrings) {

        LinkedList<State> states = new LinkedList<State>();
        LinkedList<State> bestState = new LinkedList<State>();
        String trimmedString = originalString.substring(0, originalString.length() - ".cz".length());
        //originalString = originalString.replaceAll("-", "");

        countSubstringRelevance(trimmedString, 0, substrings, states, bestState);

        int sizeOfLastPart = 0;
        StringBuilder sb = new StringBuilder(16);
        int previousIndex = 0;
        for (State s : bestState) {
            if (s.position - previousIndex > 0) {
                sb.append(trimmedString.substring(previousIndex, s.position)).append(" ");
            }

            sb.append(s.substring.toUpperCase()).append(" ");
            previousIndex = s.position + s.substring.length();

            sizeOfLastPart = s.substring.length();
        }

        if (trimmedString.length() - previousIndex > 0) {
            sb.append(trimmedString.substring(previousIndex, trimmedString.length()));
            sizeOfLastPart = trimmedString.length() - previousIndex;
        }

        boolean add = true;
        if (sizeOfLastPart <= 2) {
            if (sb.length() - sizeOfLastPart > 0) {
                add = false;
            }
        }

        if (add) {
            domainsSplit.put(originalString, sb.toString());
        }
    }
}
