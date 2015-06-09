package com.davidicius.dnc.oz;

import com.davidicius.dnc.structure.Object2StableIntHashSet;
import com.davidicius.dnc.structure.Object2StableIntSet;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;


public class Stats {
    private static final Logger log = LoggerFactory.getLogger(Stats.class);

    public static void exportDomainsToFile(String file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));

        String header = String.format("#%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                "name", "owner", "ozName", "traits", "exists", "loaded", "czDomeny", "czTom", "top1m", "skDomeny",
                "bad", "good", "neutral");
        bw.write(header);
        bw.newLine();
        DbService.db.start();
        Iterable<Vertex> domains = DbService.db.graph().getVerticesOfClass("DOM");
        int count = 0;
        for (Vertex v : domains) {
            String _exists = "?";
            String _loaded = "?";

            if (v.getProperty("exists") != null) {
                _exists = v.getProperty("exists").toString();
            }

            if (v.getProperty("loaded") != null) {
                _loaded = v.getProperty("loaded").toString();
            }

            if (_exists.equals("?")) {
                System.out.println(v);
            }

            if (!_loaded.equals("L")) {
//                System.out.println(v);
            }

            String _czDomeny = v.getProperty("czDomeny").toString();
            String _czTom = v.getProperty("czTom").toString();
            String _top1m = v.getProperty("top1m").toString();
            String _skDomeny = v.getProperty("skDomeny").toString();

            String name = v.getProperty("name").toString();
            String owner = v.getProperty("owner").toString();

            String traits = "?";
            if (v.getProperty("traits") != null) {
                traits = v.getProperty("traits").toString();
            }

            String ozName = "?";
            if (v.getProperty("ozName") != null) {
                ozName = v.getProperty("ozName").toString();
            }

            boolean exists = _exists.equals("E");
            boolean loaded = _loaded.equals("L");
            boolean czDomeny = _czDomeny.equals("TRUE");
            boolean czTom = _czTom.equals("TRUE");
            boolean top1m = _top1m.equals("TRUE");
            boolean skDomeny = _skDomeny.equals("TRUE");

            Iterable<Edge> edges = v.getEdges(Direction.OUT, "bad");
            boolean bad = edges.iterator().hasNext();

            edges = v.getEdges(Direction.OUT, "good");
            boolean good = edges.iterator().hasNext();

            edges = v.getEdges(Direction.OUT, "neutral");
            boolean neutral = edges.iterator().hasNext();

            count++;
            if (count % 10000 == 0) {
                System.out.println(String.format("Exported: %d", count));
            }

            DbService.db.graph().commit();

            String line = String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                    name, owner, ozName, traits, exists ? "T" : "F", loaded ? "T" : "F", czDomeny ? "T" : "F", czTom ? "T" : "F", top1m ? "T" : "F", skDomeny ? "T" : "F",
                    bad ? "T" : "F", good ? "T" : "F", neutral ? "T" : "F");
            bw.write(line);
            bw.newLine();
        }

        System.out.println(String.format("Exported: %d", count));

        DbService.db.shutdown();
        bw.close();
    }

    public static List<Domain> loadDomainsFromFile(String file, GlobalStringTable global) throws IOException {
        List<Domain> result = new ArrayList<Domain>(2 * 1000 * 1000);
        BufferedReader br = new BufferedReader(new FileReader(file));
        while (true) {
            String line = br.readLine();
            if (line == null) break;

            line = line.trim();
            if (line.startsWith("#")) continue;

            String[] parts = line.split("\\t");
            if (parts.length != 13) throw new IllegalStateException();
            for (int i = 0; i < parts.length; i++) {
                parts[i] = global.add(parts[i]);
            }

            Domain d = new Domain(parts[0], parts[1], parts[2], parts[3],
                    parts[4].equals("T"), parts[5].equals("T"), parts[6].equals("T"), parts[7].equals("T"), parts[8].equals("T"), parts[9].equals("T"),
                    parts[10].equals("T"), parts[11].equals("T"), parts[12].equals("T"));
            result.add(d);
        }

        br.close();
        return result;
    }

    public static List<StatOz> loadRawOz(String filename, GlobalStringTable global) throws IOException {
        log.info("Start loading OZ...");
        BufferedReader br = new BufferedReader(new FileReader(filename));
        List<StatOz> result = new ArrayList<StatOz>(100 * 1000);
        while (true) {
            String line = br.readLine();
            if (line == null) break;

            String name = line.trim();
            StatOz oz = new StatOz(global.add(name));
            result.add(oz);
        }

        br.close();
        log.info("Done.");

        return result;
    }

    public static void saveSet(String filename, Set<String> allCorpus) throws IOException {
        log.info("Size: " + allCorpus.size());
        BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
        for (String word : allCorpus) {
            bw.write(word);
            bw.newLine();
        }
        bw.close();
        log.info("Saved...");
    }

    public static void saveOzSet(String filename, Set<StatOz> allCorpus) throws IOException {
        log.info("Size: " + allCorpus.size());
        BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
        for (StatOz oz : allCorpus) {
            bw.write(oz.getName());
            bw.newLine();
        }
        bw.close();
        log.info("Saved...");
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Object2StableIntSet<String> global = new Object2StableIntHashSet<String>(1000);
        //      StringIndex a = StringIndex.loadIndex("habilitace\\CzDomains2AllCorpus.txt", global);
        //      OZAnalysis.createAndSaveReverseIndex(a, "habilitace\\allCorpus2CzDomains.txt");
        //      if (1 == 1) return;

        //exportDomainsToFile("habilitace\\aggregatedDomains.csv");
        log.info("Loading domains...");
        List<Domain> domains = loadDomainsFromFile("habilitace\\aggregatedDomains.csv", global);
        List<Domain> czDomains = getCZDomains(domains);

        log.info("Loading UPV and OHIM");
        List<StatOz> upvList = loadRawOz("exported-upv-oz.txt", global);
        List<StatOz> ohimList = loadRawOz("exported-ohim-oz.txt", global);
        Set<StatOz> allOZ = new HashSet<StatOz>(upvList);
        allOZ.addAll(ohimList);

        Set<String> czDictionary = OZAnalysis.loadCZDictionary("czech-slova.txt", global);
        Set<String> enCorpus = OZAnalysis.loadENCorpus("slovnik_data.txt", global);
        Set<String> czCorpus = OZAnalysis.loadCZCorpus("syn2010_word_abc.txt", global);

        Set<String> czCities = OZAnalysis.loadCZCities("seznam-obci-cr.txt", global);
        czCities = OZAnalysis.getCitiesKeys(czCities);
        Set<String> czNames = OZAnalysis.loadCZNames("prijmeniCZ.txt", global);

        Set<StatOz> filteredOz = OZAnalysis.filterOZ2(allOZ, czDictionary);
        Set<String> keysFromOz = new HashSet<String>();
        for (StatOz oz : allOZ) {
            keysFromOz.addAll(OZAnalysis.getRelevantKeys(oz.getName()));
        }

        log.info("creating corpus...");
        Set<String> allCorpus = new HashSet<String>();
        allCorpus.addAll(czDictionary);
        allCorpus.addAll(enCorpus);
        allCorpus.addAll(czCorpus);
        allCorpus.addAll(czCities);
        allCorpus.addAll(czNames);
        allCorpus.addAll(keysFromOz);

        allCorpus = adjustCorpus(allCorpus);
        Set<String> additionalWords = loadSet("habilitace\\dalsiCZSlova.txt", global);
        allCorpus.addAll(additionalWords);
        saveSet("habilitace\\allCorpus.txt", allCorpus);

        List<String> czDomainNames = new ArrayList<String>(czDomains.size());
        for (Domain d : czDomains) {
            czDomainNames.add(d.getName());
        }

        //trva 6 hodin ... OZAnalysis.buildAndSaveIndex(allCorpus, czDomainNames, "habilitace\\allCorpus2CzDomains.txt");
        log.info("Loading index...");
        StringIndex allCorpus2CzDomains = StringIndex.loadIndex("habilitace\\allCorpus2CzDomains.txt", global);

        log.info("Loading index...");
        //OZAnalysis.createAndSaveReverseIndex(allCorpus2CzDomains, "habilitace\\CzDomains2AllCorpus.txt");
        StringIndex czDomains2AllCorpus = StringIndex.loadIndex("habilitace\\CzDomains2AllCorpus.txt", global);

        log.info("Count OZ with hit...");
        //Set<String> ozWithHitInCzDomains = new HashSet<String>(10 * 1000);
        //populateHitSets(ozWithHitInCzDomains, filteredOz, allCorpus2CzDomains);
        //saveSet("habilitace\\FilteredOzWithHitInCzDomains.txt", ozWithHitInCzDomains);
        Set<String> ozWithHitInCzDomains = loadSet("habilitace\\FilteredOzWithHitInCzDomains.txt", global);

        //StringIndex czDomains2CorpusLine = buildAndSaveDomainsToCorpus(czDomains2AllCorpus, "habilitace\\CzDomains2CorpusLine.txt");
        StringIndex czDomains2CorpusLine = StringIndex.loadIndex("habilitace\\CzDomains2CorpusLine.txt", global);

        log.info("Build FINAL Corpus to CzDomain mapping");
//        Set<String> filteredCorpus = createFilteredCorpus(allCorpus);
//        OZAnalysis.buildAndSaveCorpus2DomainMapping(filteredCorpus, allCorpus2CzDomains, czDomains2CorpusLine, "habilitace\\FilteredCorpus2Domain.txt");

        StringIndex filteredCorpus2DomainSorted = StringIndex.loadIndex("habilitace\\FilteredCorpus2DomainSorted.txt", global);

        log.info("Build FINAL OZ to CzDomain mapping");
        //OZAnalysis.buildAndSaveOz2DomainMapping(ozWithHitInCzDomains, allCorpus2CzDomains, czDomains2CorpusLine, "habilitace\\Oz2Domain.txt");
        StringIndex oz2Domain = StringIndex.loadIndex("habilitace\\Oz2Domain.txt", global);

        StatsContext context = new StatsContext(domains, upvList, ohimList, allOZ, filteredOz, czDictionary, enCorpus,
                czCorpus, czCities, czNames, allCorpus, keysFromOz, czDomains, allCorpus2CzDomains,
                ozWithHitInCzDomains);
        global = null;

        log.info("READY*****");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        List<Stat> stats = new ArrayList<Stat>();
        while (true) {
            String in = br.readLine();
            if (in.equals("q")) break;

            if (in.equals("g")) {
                log.info("Generating stats...");

                BufferedWriter bw = new BufferedWriter(new FileWriter("habilitace\\stats.csv"));
                for (Stat s : stats) {
                    log.info(s.getClass().getSimpleName());
                    s.printStat(context, bw);
                    bw.newLine();

                }
                bw.close();
                log.info("Done...");
            }

            if (in.equals("r")) {
                File root = new File("target\\classes\\");
                URL url = root.toURI().toURL();
                URLClassLoader cl = new URLClassLoader(new URL[]{url});

                File file = new File("target\\classes\\com\\davidicius\\dnc\\oz\\stats\\");
                File[] list = file.listFiles();
                if (list != null) {
                    stats = new ArrayList<Stat>();
                    for (File c : list) {
                        String name = c.getName();
                        int i = name.lastIndexOf(".");
                        name = name.substring(0, i);

                        Class<?> aClass = cl.loadClass("com.davidicius.dnc.oz.stats." + name);
                        Stat ni = (Stat) aClass.newInstance();
                        stats.add(ni);
                    }
                }

                Collections.sort(stats, new Comparator<Stat>() {
                    public int compare(Stat o1, Stat o2) {
                        return o1.priority() - o2.priority();
                    }
                });

                for (Stat s : stats) {
                    System.out.println(s.getClass().getSimpleName());
                }
            }

            if (in.startsWith("a ")) {
                String word = in.substring(2, in.length()).trim().toLowerCase();

                // Adjust corpus
                allCorpus.add(word);
                additionalWords.add(word);
                saveSet("habilitace\\allCorpus.txt", allCorpus);
                saveSet("habilitace\\dalsiCZSlova.txt", additionalWords);

                // Adjust corpus to CZdomains index
                OZAnalysis.updateIndexWithNewKey(allCorpus2CzDomains, word, czDomainNames);
                allCorpus2CzDomains.saveIndex("habilitace\\allCorpus2CzDomains.txt");

                ArrayList<String> doms = allCorpus2CzDomains.get(word);
                for (String d : doms) {
                    ArrayList<String> strings = czDomains2AllCorpus.get(d);
                    if (strings != null) {
                        if (!strings.contains(word)) {
                            strings.add(word);
                        }
                    }
                }

                for (String domain : czDomains2AllCorpus.getKeys()) {
                    ArrayList<String> keys = czDomains2AllCorpus.get(domain);
                    Collections.sort(keys, new Comparator<String>() {
                        public int compare(String o1, String o2) {
                            return o2.length() - o1.length();
                        }
                    });
                }

                czDomains2AllCorpus.saveIndex("habilitace\\CzDomains2AllCorpus.txt");

                // Adjust OZwithHit
                for (StatOz oz : filteredOz) {
                    if (oz.getName().contains(word)) {
                        if (OZAnalysis.getDomainsForOz(oz.getName(), allCorpus2CzDomains).size() > 0) {
                            ozWithHitInCzDomains.add(oz.getName());
                            System.out.println("OZ added: " + oz.getName());
                        }
                    }
                }

                saveSet("habilitace\\FilteredOzWithHitInCzDomains.txt", ozWithHitInCzDomains);

                // Adjust czDomains2DorpusLIne
                OZAnalysis.updateDomainsToCorpusForKey(allCorpus2CzDomains, czDomains2CorpusLine, czDomains2AllCorpus, word);
                czDomains2CorpusLine.saveIndex("habilitace\\CzDomains2CorpusLine.txt");

                // Adjust filteredCorpus2DomainSorted

                System.out.println("Done.");
            }
        }

    }

    public static Set<String> createFilteredCorpus(Set<String> allCorpus) {
        Set<String> result = new HashSet<String>(1000);
        for (String s : allCorpus) {
            if (s.length() >= 3) {
                result.add(s);
            }
        }

        return result;
    }

    public static StringIndex buildAndSaveDomainsToCorpus(StringIndex czDomains2AllCorpus, String filename) throws IOException {
        log.info("Counting substring relevance...");
        StringIndex domainsSplit = new StringIndex();
        int count = 0;
        for (String domain : czDomains2AllCorpus.getKeys()) {
            ArrayList<String> list = czDomains2AllCorpus.get(domain);
//            if (!domain.equals("ventrocentrum.cz")) {
//                continue;
//            }
            OZAnalysis.countSubstringRelevance(domainsSplit, domain, list);

            count++;
            if (count % 1000 == 0) {
                log.info("Counted: " + count);
                // break;
            }
        }

        log.info("Saving substring relevance...");
        domainsSplit.saveIndex(filename);

        return domainsSplit;
    }

    public static Set<String> loadSet(String filename, GlobalStringTable global) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        Set<String> result = new HashSet<String>();
        while (true) {
            String line = br.readLine();
            if (line == null) break;

            line = line.trim();
            if (line.length() > 0) {
                result.add(global.add(line));
            }
        }

        br.close();

        return result;
    }

    private static void populateHitSets(Set<String> ozWithHitInCzDomains, Set<StatOz> filteredOz, StringIndex allCorpus2CzDomains) {
        for (StatOz oz : filteredOz) {
            if (OZAnalysis.getDomainsForOz(oz.getName(), allCorpus2CzDomains).size() > 0) {
                ozWithHitInCzDomains.add(oz.getName());
            }
        }
    }

    private static final String ADD_TO_CORPUS = "a o k i v u s do eu cz co ci cd pc po pi ma me mu my mi ne ke ho ja ve ul um ty tv to te ti tu ta se si sw pa bmw kia cau asi aby aut ave jed jel jen jez jev jas jdu jdi job " +
            "jon joe jil kit key kde kdo kdy koz kol kly kaz her gps ico hon hod hru hry hrb eur evu evy ema gay fuj fox fly dan dar dam clo cla coz cpu dve dva tvi dph dub sex set spy tvi tva tve in out e pre neo nuz " +
            "noe nic nil one two len lis leh maj muj mam mol mor zub zed zdi zlo ven vin vyr vys vis vez vaz vuz vse vrh vlk vor vos vak usb ufo tur tri dva pet osm ten ";

    private static Set<String> adjustCorpus(Set<String> allCorpus) {
        Set<String> add = new HashSet<String>();
        String[] parts = ADD_TO_CORPUS.split("\\s+");
        Collections.addAll(add, parts);

        Set<String> result = new HashSet<String>(allCorpus.size());
        for (String key : allCorpus) {
            if (key.length() <= 3) {
                if (add.contains(key)) {
                    //   System.out.println("Added to corpus: " + key);
                    result.add(key);
                } else {
                    //       System.out.println("IGNORED: " + key);
                }
            } else {
                result.add(key);
            }
        }

        return result;
    }

    private static List<Domain> getCZDomains(List<Domain> domains) {
        List<Domain> result = new ArrayList<Domain>();
        for (Domain d : domains) {
            String name = d.getName().toLowerCase();
            if (name.endsWith(".cz")) {
                result.add(d);
            }
        }

        return result;
    }
}


