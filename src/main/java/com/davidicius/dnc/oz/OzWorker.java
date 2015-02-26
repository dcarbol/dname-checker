package com.davidicius.dnc.oz;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexCursor;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

class LimitedQueue<E> extends LinkedList<E> {
    private int limit;

    public LimitedQueue(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean add(E o) {
        if (!contains(o)) {
            super.add(o);
            while (size() > limit) {
                super.remove();
            }

            return true;
        } else {
            return false;
        }
    }
}

public class OzWorker {

    private static final Logger log = LoggerFactory.getLogger(OzWorker.class);

    private static Directory createDomLuceneIndex(OrientGraph graph) throws IOException {
        log.info("Start creating lucene index for DOM.owner...");

        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
        File fileIndex = new File("dom-lucene.index");
        FileUtils.deleteDirectory(fileIndex);

        Directory directory = new SimpleFSDirectory(fileIndex);

        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_47, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, iwc);
        OrientVertexType vertexType = graph.getVertexType("DOM");
        OIndex<?> index = vertexType.getClassIndex("DOM.name");
        OIndexCursor cursor = index.cursor();
        cursor.setPrefetchSize(1000);
        int i = 0;
        while (true) {
            Map.Entry<Object, OIdentifiable> next = cursor.nextEntry();
            if (next == null) break;

            Vertex v = graph.getVertex(next.getValue());
            String domainName = v.getProperty("name");
            String owner = v.getProperty("owner");

            Document document = new Document();
            document.add(new StringField("name", domainName, Field.Store.YES));
            document.add(new StringField("owner", owner, Field.Store.YES));
            indexWriter.addDocument(document);

            i++;
            if (i % 1000 == 0) {
                System.out.println("Indexed..." + i);
                graph.commit();
            }
        }

        indexWriter.close();

        log.info("Lucene index created..." + i);
        return directory;
    }

    public static String toStringOZ(Vertex v) {
        String ozId = v.getProperty("ozId").toString();
        String ozNumber = v.getProperty("ozNumber").toString();
        String date = v.getProperty("date").toString();
        String name = v.getProperty("name").toString();
        String owner = v.getProperty("owner").toString();
        String classes = v.getProperty("classes").toString();
        String source = v.getProperty("source").toString();
        String keys = v.getProperty("keys").toString();
        String format = "%7s | %-4s | %-10s | %-50s | %-80s | %-50s | %s ";

        return String.format(format, ozNumber, source, date, name, owner, keys, classes);
    }

    public static Vertex newGeneratedDomain(String domainName, OrientGraph graph, boolean generate) {
        OrientVertex v = graph.addVertex("class:DOM");
        v.setProperty("name", domainName);
        v.setProperty("owner", "NotAv");
        v.setProperty("exists", "?");
        v.setProperty("loaded", "?");
        v.setProperty("czDomeny", "FALSE");
        v.setProperty("czTom", "FALSE");
        v.setProperty("top1m", "FALSE");
        v.setProperty("skDomeny", "FALSE");
        v.setProperty("top1mRank", "NotAv");
        v.setProperty("generated", Boolean.valueOf(generate));

        return v;
    }

    public static String traitsToLabel(String traits) {
        StringBuilder sb = new StringBuilder(10);

        String[] parts = traits.split("\\s+");
        for (String part : parts) {
            Trait trait = TraitsFactory.INSTANCE.getTrait(part);
            if (trait != null) {
                sb.append(trait.getName()).append(" ");
            }
        }

        return sb.toString();
    }

    public static String toStringDOM(Vertex v) {
        String name = v.getProperty("name").toString();
        String owner = v.getProperty("owner").toString();
        String exists = "?";
        String loaded = "?";

        if (v.getProperty("exists") != null) {
            exists = v.getProperty("exists").toString();
        }

        if (v.getProperty("loaded") != null) {
            loaded = v.getProperty("loaded").toString();
        }

        String traits = "?";
        if (v.getProperty("traits") != null) {
            traits = v.getProperty("traits").toString();
            traits = traitsToLabel(traits);
        }

        String czDomeny = v.getProperty("czDomeny").toString();
        String czTom = v.getProperty("czTom").toString();
        String top1m = v.getProperty("top1m").toString();
        String skDomeny = v.getProperty("skDomeny").toString();
        String top1mRank = v.getProperty("top1mRank").toString();

        Iterable<Edge> edges = v.getEdges(Direction.OUT, "bad");
        boolean bad = edges.iterator().hasNext();

        edges = v.getEdges(Direction.OUT, "good");
        boolean good = edges.iterator().hasNext();

        edges = v.getEdges(Direction.OUT, "neutral");
        boolean neutral = edges.iterator().hasNext();

//        String format = "%1s %1s %1s | %-50s | %-48s | %-5s | %-5s | %-5s | %-5s | %7s || %s";
//        return String.format(format, (bad ? "B" : (good ? "G" : (neutral ? "N" : "?"))), exists, loaded, name, owner, czTom.equals("TRUE") ? "czTom" : "", czDomeny.equals("TRUE") ? "czDom" : "", top1m.equals("TRUE") ? "top1m" : "", skDomeny.equals("TRUE") ? "skDom" : "", top1mRank, traits);
        String format = "%1s %1s %1s | %-50s | %-48s || %s";
        return String.format(format, (bad ? "B" : (good ? "G" : (neutral ? "N" : "?"))), exists, loaded, name, owner, traits);
    }

    public static void main(String[] args) throws IOException, ParseException {
//        List<String> f = new ArrayList<String>();
//        DomainDataLoader.loadPage("autopujcovna-skoda.cz", f);
//        DomainDataLoader.loadPage("akskodaplzen.cz", f);
//        DomainDataLoader.loadPage("auto-dily-skoda.cz", f);

//        if (1 == 1) return;


        LimitedQueue<String> history = new LimitedQueue<String>(20);
        DbService.db.start();
//        Vertex dd = DbService.db.getDomain("interskoda.cz");
//        if (dd != null) {
//            Iterable<Edge> neutral = dd.getEdges(Direction.OUT, "neutral");
//            for (Edge e : neutral) {
//                e.remove();
//            }
//
//            DbService.db.graph().commit();
//        }

//        if (1 == 1) return;
//        Iterable<Vertex> dom = DbService.db.graph().getVerticesOfClass("DOM");
//        for (Vertex d : dom) {
//            String loaded = d.getProperty("loaded");
//            if (loaded != null && loaded.equals("L")) {
//                d.setProperty("loaded", "?");
//            }
//            DbService.db.graph().commit();
//        }
//
//        if (1 == 1) return;

        //  Directory directoryDom = createDomLuceneIndex(DbService.db.graph());
        Directory directoryDom = SimpleFSDirectory.open(new File("dom-lucene.index"));
        IndexReader ir = DirectoryReader.open(directoryDom);
        IndexSearcher is = new IndexSearcher(ir);

        DomainDataLoader domainDataLoader = new DomainDataLoader();
        Set<String> lastDomains = new LinkedHashSet<String>();

        Scanner sc = new Scanner(System.in);
        while (true) {
            if (sc.hasNextLine()) {
                String query = sc.nextLine();
                if (query.equalsIgnoreCase("QUIT")) {
                    break;
                }

                if (query.startsWith("#")) {
                    query = query.substring(1);
                    int number = Integer.parseInt(query);
                    if (number >= 0 && number < history.size()) {
                        query = history.get(number);
                    }
                }

                if (query.equalsIgnoreCase("recount keys")) {
                    history.add(query);
                    recountKeys(DbService.db.graph());
                    continue;
                }

                if (query.equalsIgnoreCase("correct domains")) {
                    history.add(query);
                    correctDomains(DbService.db.graph());
                    continue;
                }

                if (query.startsWith("oz ")) {
                    history.add(query);
                    query = query.substring(3);
                    log.info("Starting query '" + query + "'");
                    CloseableIterable<Vertex> result = DbService.db.graph().command(new OSQLSynchQuery<ODocument>("select * from OZ where [name, source, owner, keys] LUCENE ?")).execute(query);

                    int i = 0;
                    lastDomains.clear();
                    for (Vertex v : result) {
                        System.out.println(String.format("%03d. %s", (i + 1), toStringOZ(v)));
                        Set<String> domains = generateDomains(v);

                        int j = 0;
                        for (String d : domains) {
                            lastDomains.add(d);
                            System.out.println(String.format("     %03d. %s", (j + 1), d));
                            j++;
                        }

                        i++;
                    }

                    log.info("Found " + i + " hits.");
                }

                if (query.startsWith("dom-name ") || query.startsWith("dom-owner ")) {
                    history.add(query);
                    String field = "";
                    if (query.startsWith("dom-name ")) {
                        query = query.substring("dom-name ".length(), query.length());
                        field = "name";
                    }

                    if (query.startsWith("dom-owner ")) {
                        query = query.substring("dom-owner ".length(), query.length());
                        field = "owner";
                    }

                    log.info("Looking for '" + query + "'");
                    WildcardQuery wq = new WildcardQuery(new Term(field, query));
                    TopDocs search = is.search(wq, 10000);

                    lastDomains.clear();
                    for (int i = 0; i < search.totalHits; ++i) {
                        ScoreDoc scoreDoc = search.scoreDocs[i];
                        Document d = is.doc(scoreDoc.doc);

                        String domainName = d.get("name");
                        Object o = DbService.db.getDomNameIndex().get(domainName);
                        if (o != null) {
                            Vertex v = DbService.db.graph().getVertex(o);
                            System.out.println(String.format("%03d. %s", (i + 1), toStringDOM(v)));
                            lastDomains.add(domainName);
                            //  domainDataLoader.processDomain(graph, v);
                        } else {
                            System.out.println("Error: " + domainName);
                        }
                    }

                    log.info("Found " + search.totalHits + " hits.");
                }

                if (query.equalsIgnoreCase("last domains")) {
                    history.add(query);
                    int i = 0;
                    for (String domainName : lastDomains) {
                        Object o = DbService.db.getDomNameIndex().get(domainName);
                        if (o != null) {
                            Vertex v = DbService.db.graph().getVertex(o);
                            System.out.println(String.format("%03d. %s", (i + 1), toStringDOM(v)));
                        } else {
                            System.out.println(String.format("%03d. %s %s", (i + 1), domainName, " - not in DB"));
                        }

                        i++;
                    }
                }

                if (query.equalsIgnoreCase("update domains")) {
                    history.add(query);
                    for (String domainName : lastDomains) {
                        Object o = DbService.db.getDomNameIndex().get(domainName);
                        Vertex v;
                        if (o == null) {
                            //    v = newGeneratedDomain(domainName, graph);
                            //    graph.commit();
                        }
                        //
                        domainDataLoader.processDomain(domainName, DbService.db.graph());
                    }
                }

                if (query.equalsIgnoreCase("active tasks")) {
                    System.out.println("Active tasks: " + domainDataLoader.countActiveTasks());
                    history.add(query);
                }

                if (query.equalsIgnoreCase("restart db")) {
                    DbService.db.restart();
                    history.add(query);
                }

                if (query.equalsIgnoreCase("refresh index")) {
                    ir.close();
                    directoryDom.close();

                    createDomLuceneIndex(DbService.db.graph());

                    directoryDom = SimpleFSDirectory.open(new File("dom-lucene.index"));
                    ir = DirectoryReader.open(directoryDom);
                    is = new IndexSearcher(ir);
                    history.add(query);
                }

                if (query.equalsIgnoreCase("h")) {
                    for (int i = 0; i < history.size(); ++i) {
                        System.out.println(String.format("%02d: %s", i, history.get(i)));
                    }
                }

                if (query.equalsIgnoreCase("test traits")) {
                    history.add(query);
                    testTraits();
                }

                if (query.startsWith("bad ")) {
                    history.add(query);
                    String[] parts = query.split("\\s+");
                    if (parts.length == 3) {
                        markAs(parts[1].trim(), parts[2].trim(), "bad");
                    }
                }

                if (query.startsWith("good ")) {
                    history.add(query);
                    String[] parts = query.split("\\s+");
                    if (parts.length == 3) {
                        markAs(parts[1].trim(), parts[2].trim(), "good");
                    }
                }

                if (query.startsWith("neutral ")) {
                    history.add(query);
                    String[] parts = query.split("\\s+");
                    if (parts.length == 3) {
                        markAs(parts[1].trim(), parts[2].trim(), "neutral");
                    }
                }

                if (query.startsWith("unload domain ")) {
                    history.add(query);
                    String[] parts = query.split("\\s+");
                    if (parts.length == 3) {
                        String dn = parts[2].trim().toLowerCase();
                        Vertex domain = DbService.db.getDomain(dn);
                        if (domain != null) {
                            domain.setProperty("loaded", "?");
                            domain.setProperty("exists", "?");
                        }

                        DbService.db.graph().commit();
                        log.info("Domain " + dn + " unloaded.");
                    }
                }


                if (query.startsWith("create domain ")) {
                    String[] parts = query.split("\\s+");
                    if (parts.length == 3) {
                        String domainName = parts[2].trim().toLowerCase();
                        Vertex domain = DbService.db.getDomain(domainName);
                        if (domain != null) {
                            log.info("Domain " + domainName + " already exists.");
                        } else {
                            Vertex vertex = newGeneratedDomain(domainName, DbService.db.graph(), false);
                            DbService.db.graph().commit();
                            log.info("Domain " + domainName + " created.");

                            // Add to index

                        }
                    }
                }

                if (query.startsWith("update trait ")) {
                    history.add(query);
                    String[] parts = query.split("\\s+");
                    if (parts.length == 3) {
                        String traitId = parts[2].trim().toLowerCase();
                        Trait t = TraitsFactory.INSTANCE.getTrait(traitId);
                        if (t != null) {
                            for (String domainName : lastDomains) {
                                Vertex domain = DbService.db.getDomain(domainName);
                                String body = null;
                                try {
                                    File file = new File("pages");
                                    file = new File(file, domainName);
                                    body = FileUtils.readFileToString(file);
                                } catch (IOException e) {

                                }

                                StringBuilder sb = new StringBuilder();
                                for (Trait trait : TraitsFactory.INSTANCE.traits()) {
                                    boolean result = trait.hasTrait(domain, body);
                                    if (result) {
                                        sb.append(trait.getId()).append(" ");
                                    }
                                }

                                domain.setProperty("traits", sb.toString().trim());
                                DbService.db.graph().commit();
                            }
                        }

                        log.info(String.format("Trait '%s' updated.", traitId));
                    }
                }

                if (query.startsWith("test domains")) {
                    history.add(query);
                    for (String domainName : lastDomains) {
                        Vertex domain = DbService.db.getDomain(domainName);
                        String body = null;
                        try {
                            File file = new File("pages");
                            file = new File(file, domainName);
                            body = FileUtils.readFileToString(file);
                        } catch (IOException e) {

                        }

                        StringBuilder sb = new StringBuilder();
                        for (Trait trait : TraitsFactory.INSTANCE.traits()) {
                            boolean result = trait.hasTrait(domain, body);
                            if (result) {
                                sb.append(trait.getId()).append(" ");
                            }
                        }

                        String newTrait = sb.toString().trim();
                        String currentTrait = domain.getProperty("traits");
                        if (currentTrait == null || !currentTrait.trim().equals(newTrait)) {
                            System.out.println(String.format("%-60s current: '%s' new '%s'", domainName, traitsToLabel(currentTrait), traitsToLabel(newTrait)));
                        }

                        DbService.db.graph().commit();
                    }

                    System.out.println("Testing domains finished.");
                }

                if (query.startsWith("domain stats")) {
                    history.add(query);
                    printDomainStats(lastDomains);
                }

                if (query.equalsIgnoreCase("help")) {
                    System.out.println("QUIT - to quit the program");
                    System.out.println("recount keys");
                    System.out.println("correct domains");
                    System.out.println("oz <lucene-query> - the following properties are indexed: name, source, owner, keys");
                    System.out.println("dom-name <wild-card-pattern>");
                    System.out.println("dom-owner <wild-card-pattern>");
                    System.out.println("last domains");
                    System.out.println("active tasks");
                    System.out.println("update domains");
                    System.out.println("restart db");
                    System.out.println("refresh index");
                    System.out.println("test traits");
                    System.out.println("test domains");
                    System.out.println("bad <domain-name> <oz-id>");
                    System.out.println("good <domain-name> <oz-id>");
                    System.out.println("neutral <domain-name> <oz-id>");
                    System.out.println("unload domain <domain-name>");
                    System.out.println("update trait <trait-id>");
                    System.out.println("create domain <domain-name>");
                    System.out.println("domain stats");
                    System.out.println("h - lists history");
                }
            }
        }

        domainDataLoader.stop();
    }

    private static void add(Trait t, Map<Trait, Integer> map) {
        Integer i = map.get(t);
        if (i == null) {
            map.put(t, 1);
        } else {
            map.put(t, i + 1);
        }
    }

    private static void add(Trait a, Trait b, Map<MyPair<Trait, Trait>, Integer> map) {
        MyPair<Trait, Trait> t = new MyPair<Trait, Trait>(a, b);
        Integer i = map.get(t);
        if (i == null) {
            map.put(t, 1);
        } else {
            map.put(t, i + 1);
        }
    }

    private static int get(Trait t, Map<Trait, Integer> map) {
        Integer i = map.get(t);
        if (i == null) {
            return 0;
        } else {
            return i;
        }
    }

    private static int get(Trait a, Trait b, Map<MyPair<Trait,Trait>, Integer> map) {
        MyPair<Trait, Trait> t = new MyPair<Trait, Trait>(a, b);
        Integer i = map.get(t);
        if (i == null) {
            return 0;
        } else {
            return i;
        }
    }

    private static void printDomainStats(Set<String> lastDomains) {
        log.info("Printing domain statistics");
        int allDomains = 0;
        int existed = 0;
        int bad = 0;
        int neutral = 0;
        int good = 0;

        Map<Trait, Integer> badMap = new HashMap<Trait, Integer>();
        Map<Trait, Integer> neutralMap = new HashMap<Trait, Integer>();
        Map<Trait, Integer> goodMap = new HashMap<Trait, Integer>();
        int[] histogram = new int[TraitsFactory.INSTANCE.traits().size()];
        int[] histogramB = new int[TraitsFactory.INSTANCE.traits().size()];
        int[] histogramN = new int[TraitsFactory.INSTANCE.traits().size()];
        int[] histogramG = new int[TraitsFactory.INSTANCE.traits().size()];

        Map<MyPair<Trait,Trait>, Integer> coAll = new HashMap<MyPair<Trait,Trait>, Integer>();
        for (String domainName : lastDomains) {
            Vertex domain = DbService.db.getDomain(domainName);
            if (domain == null) {
                log.warn("Non existing domain " + domainName + "???");
                continue;
            }

            allDomains++;

            String s = domain.getProperty("exists");
            boolean domainExists = s != null && s.equalsIgnoreCase("E");
            if (domainExists) {
                existed++;
            }

            Iterable<Edge> edges = domain.getEdges(Direction.OUT, "bad");
            boolean isBad = edges.iterator().hasNext();

            edges = domain.getEdges(Direction.OUT, "neutral");
            boolean isNeutral = edges.iterator().hasNext();

            edges = domain.getEdges(Direction.OUT, "good");
            boolean isGood = edges.iterator().hasNext();

            if (isBad) {
                bad++;
            }

            if (isNeutral) {
                neutral++;
            }

            if (isGood) {
                good++;
            }

            String traits = domain.getProperty("traits");
            if (traits == null) continue;

            int countTraits = 0;
            for (Trait t : TraitsFactory.INSTANCE.traits()) {
                if (traits.contains(t.getId())) {
                    if (!domainExists) throw new IllegalStateException();
                    countTraits++;

                    if (isBad) {
                        add(t, badMap);
                    } else if (isNeutral) {
                        add(t, neutralMap);
                    } else if (isGood) {
                        add(t, goodMap);
                    }

                    for (Trait r : TraitsFactory.INSTANCE.traits()) {
                        if (traits.contains(r.getId())) {
                            add(t, r, coAll);
                        }
                    }
                }
            }

            if (domainExists) {
                histogram[countTraits]++;
                if (isBad) {
                    histogramB[countTraits]++;
                } else if (isNeutral) {
                    histogramN[countTraits]++;
                } else if (isGood) {
                    histogramG[countTraits]++;
                }
            }
        }

        System.out.println(String.format("All = %4d Non-existed = %4d (%4.1f%%) Existed = %4d (%4.1f%%)| Bad = %4d (%4.1f%%) Neutral = %4d (%4.1f%%) Good = %4d (%4.1f%%) ",
                allDomains, allDomains - existed, (allDomains - existed) * 100.0 / allDomains, existed, existed * 100.0 / allDomains,
                bad, bad * 100.0 / existed, neutral, neutral * 100.0 / existed, good, good * 100.0 / existed));

        for (Trait t : TraitsFactory.INSTANCE.traits()) {
            int badc = get(t, badMap);
            int neutralc = get(t, neutralMap);
            int goodc = get(t, goodMap);

            double all = (badc * 1.0 / existed) * (bad * 1.0 / existed) + (neutralc * 1.0 / existed) * (neutral * 1.0 / existed) + (goodc * 1.0 / existed) * (good * 1.0 / existed);
            double bb = (badc * 1.0 / existed) * (bad * 1.0 / existed) / all;
            double nn = (neutralc * 1.0 / existed) * (neutral * 1.0 / existed) / all;
            double gg = (goodc * 1.0 / existed) * (good * 1.0 / existed) / all;

            System.out.println(String.format("                               %s %-10s | %4d (%4.1f%%) |       %4d (%4.1f%% - %5.1f%%)           %4d (%4.1f%% - %5.1f%%)        %4d (%4.1f%% - %5.1f%%)", t.getId(), t.getName(),
                    badc + neutralc + goodc, (badc + neutralc + goodc) * 100.0 / existed, badc, badc * 100.0 / bad, bb * 100, neutralc, neutralc * 100.0 / neutral, nn * 100, goodc, goodc * 100.0 / good, gg * 100));
        }


        System.out.println("\nHISTORGRAM:");
        for (int i = 0; i < TraitsFactory.INSTANCE.traits().size(); ++i) {
            System.out.println(String.format("Number of domains with %2d traits: %4d (%4.1f%%) | %4d (%4.1f%%)     %4d (%4.1f%%)     %4d (%4.1f%%)", i,
                    histogram[i], histogram[i] * 100.0 / existed, histogramB[i], histogramB[i] * 100.0 / bad, histogramN[i], histogramN[i] * 100.0 / neutral, histogramG[i], histogramG[i] * 100.0 / good));
        }

        System.out.println("\nKORELACE ALL:");
        System.out.print("             ");
        for (Trait a : TraitsFactory.INSTANCE.traits()) {
            System.out.print(String.format("%-13s ", a.getName()));
        }
        System.out.print("\n             ");
        for (Trait a : TraitsFactory.INSTANCE.traits()) {
            int badc = get(a, badMap);
            int neutralc = get(a, neutralMap);
            int goodc = get(a, goodMap);
            System.out.print(String.format("%4.1f%%         ", (badc + neutralc + goodc) * 100.0 / existed));
        }
        System.out.print("\n             ");
        for (Trait a : TraitsFactory.INSTANCE.traits()) {
            System.out.print("--------------");
        }
        System.out.println();
        for (Trait b : TraitsFactory.INSTANCE.traits()) {
            System.out.print(String.format("%-10s |", b.getName()));
            for (Trait c : TraitsFactory.INSTANCE.traits()) {
                if (b.equals(c)) {
                    System.out.print("              ");
                    continue;
                }
                int badc = get(c, badMap);
                int neutralc = get(c, neutralMap);
                int goodc = get(c, goodMap);

                double co = get(b, c, coAll) * 100.0 / existed;
                double dd = get(b, c, coAll) * 100.0 / (badc + neutralc + goodc);
                System.out.print(String.format("%5.1f%%-%5.1f%% ", co, dd));
            }
            System.out.println();
        }
    }

    private static void markAs(String domainName, String ozNumber, String what) {
        Vertex d = DbService.db.getDomain(domainName);
        if (d == null) {
            System.out.println("Domain does not exists: " + domainName);
            return;
        }

        Vertex o = DbService.db.getOz(ozNumber);
        if (o == null) {
            System.out.println("OZ does not exists: " + ozNumber);
            return;
        }

        Iterable<Edge> edges = d.getEdges(Direction.OUT, what);
        for (Edge e : edges) {
            Vertex aa = e.getVertex(Direction.IN);
            if (aa.equals(o)) {
                System.out.println(String.format("Combination '%s' -> '%s' ALREADY marked as %s", domainName, ozNumber, what));
                return;
            }
        }

        d.addEdge(what, o);
        DbService.db.graph().commit();
        System.out.println(String.format("Combination '%s' -> '%s' marked as %s", domainName, ozNumber, what));
    }


    static interface DomainGenerator {
        Set<String> generate(Set<String> inputs, Vertex v);
    }

    // "skoda auto" ->  "skodaauto", "skoda-auto"
    private static Set<String> COUNTRIES = new HashSet<String>();

    static {
        COUNTRIES.add("cz");
    }

    static class SimpleDomainGenerator implements DomainGenerator {
        public Set<String> generate(Set<String> inputs, Vertex v) {
            Set<String> results = new HashSet<String>(inputs.size());
            for (String s : inputs) {
                results.add(s.replaceAll(" ", ""));
                results.add(s.replaceAll(" ", "-"));
            }

            return results;
        }
    }

    static class CountryGenerator implements DomainGenerator {
        public Set<String> generate(Set<String> inputs, Vertex v) {
            Set<String> results = new HashSet<String>(inputs.size());
            for (String s : inputs) {
                for (String country : COUNTRIES) {
                    if (s.endsWith("-")) {
                        s = s.substring(0, s.length() - 1);
                    }

                    results.add(s + "." + country);

                    if (s.endsWith("-" + country)) {
                        String a = s.substring(0, s.length() - country.length() - 1);
                        if (a.length() > 0) {
                            results.add(a + "." + country);
                        }
                    } else if (s.endsWith(country)) {
                        String a = s.substring(0, s.length() - country.length());
                        if (a.length() > 0) {
                            results.add(a + "." + country);
                        }
                    }
                }
            }

            return results;
        }

    }

    public static DomainGenerator[] generators = {new SimpleDomainGenerator(), new CountryGenerator()};

    private static Set<String> generateDomains(Vertex v) {
        String keys = v.getProperty("keys").toString();
        Set<String> results = new HashSet<String>();
        results.add(keys);

        for (int i = 0; i < generators.length; ++i) {
            results = generators[i].generate(results, v);
        }

        return results;
    }

    private static void correctDomains(OrientGraph graph) {
        log.info("Start correcting domains...");
        Iterable<Vertex> result = graph.getVerticesOfClass("DOM");
        int count = 0;
        for (Vertex v : result) {
            String name = v.getProperty("czDomeny");
            if (name == null) {
                v.setProperty("czDomeny", "FALSE");
                graph.commit();
            }

            name = v.getProperty("czTom");
            if (name == null) {
                v.setProperty("czTom", "FALSE");
                graph.commit();
            }

            name = v.getProperty("top1m");
            if (name == null) {
                v.setProperty("top1m", "FALSE");
                graph.commit();
            }

            name = v.getProperty("top1mRank");
            if (name == null) {
                v.setProperty("top1mRank", "NotAv");
                graph.commit();
            }

            name = v.getProperty("skDomeny");
            if (name == null) {
                v.setProperty("skDomeny", "FALSE");
                graph.commit();
            }

            name = v.getProperty("owner");
            if (name == null) {
                v.setProperty("owner", "NotAv");
                graph.commit();
            }

            count++;

            if (count % 1000 == 0) {
                log.info("" + count + " DOM corrected...");
            }
        }

        log.info("Correcting domains finished..." + count);

    }

    private static void recountKeys(OrientGraph graph) {
        log.info("Start recounting keys...");
        Iterable<Vertex> result = graph.getVerticesOfClass("OZ");
        int count = 0;
        for (Vertex v : result) {
            String name = v.getProperty("name").toString().toLowerCase();

            String noAccent = Normalizer.normalize(name, Normalizer.Form.NFD);
            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            noAccent = pattern.matcher(noAccent).replaceAll("");

            //String temp = noAccent.replaceAll("[^a-z0-9\\- ]", "").trim();
            String[] keys = noAccent.split("[ \\-,;:\\.]");

            StringBuilder k = new StringBuilder(24);
            for (String key : keys) {
                String normalizedKey = key.replaceAll("[^a-z0-9]", "").trim();
                if (!normalizedKey.equals("")) {
                    k.append(normalizedKey).append(" ");
                }
            }

            v.setProperty("keys", k.toString());
            graph.commit();
            count++;

            if (count % 1000 == 0) {
                log.info("" + count + " OZ recounted...");
            }
        }

        log.info("Recounting keys finished...");

    }

    private static int allTests = 0;
    private static int errorTests = 0;

    private static void testTraits() {
        allTests = 0;
        errorTests = 0;

        testTrait("aaaskoda.cz", "00", true);
        testTrait("skoda-roomster.sk", "00", true);
        testTrait("aaaskoda.sk", "00", false);
        testTrait("autodiagnostika-skoda.cz", "00", true);

        testTrait("aaaskoda.sk", "01", true);    // park
        testTrait("apa-skoda.cz", "01", true);
        testTrait("skoda-forum.sk", "01", true);
        testTrait("octaviaskoda.cz", "01", true);
        testTrait("yetiskoda.cz", "01", true);
        testTrait("autaskoda.sk", "01", true);
        testTrait("auto-dily-skoda.cz", "01", true);
        testTrait("autobazarskoda.cz", "01", true);
        testTrait("eshopskoda.cz", "01", false);
        testTrait("autopujcovna-skoda.cz", "01", true);
        testTrait("dily-skoda.cz", "01", true);
        testTrait("dilyskoda.cz", "01", true);
        testTrait("jiriskoda.cz", "01", true);
        testTrait("skodaautocz.cz", "01", true);
        testTrait("skodafavorit.cz", "01", true);
//        testTrait("skodafoto.cz", "01", true); obsah se dynamicky meni.
        testTrait("skodakarlovyvary.cz", "01", true);
        testTrait("skodamedia.cz", "01", true);
        testTrait("skodanet.cz", "01", true);
        testTrait("skodatour.cz", "01", true);
        testTrait("skodauto.cz", "01", true);
        testTrait("veteran-skoda.cz", "01", true);

        testTrait("aftersales-skoda.sk", "02", false);
        testTrait("octaviaskoda.cz", "02", true);
        testTrait("skoda-forum.sk", "02", true);
        testTrait("yetiskoda.cz", "02", true);
        testTrait("auto-skoda.cz", "02", false); // forwarduje na ofiko stranky
        testTrait("eshopskoda.cz", "02", true); // forward pres http/equiv

        testTrait("aaaskoda.sk", "03", true);
        testTrait("auto-dily-skoda.cz", "03", true);

        testTrait("akskodaplzen.cz", "04", true);  // links
        testTrait("araverskoda.sk", "04", true);
        testTrait("autobazar-skoda.cz", "04", false);
        testTrait("nezavisle-odbory-skoda.cz", "04", true);

        testTrait("akskodaplzen.cz", "05", true);
        testTrait("aftersales-skoda.sk", "05", true);
        testTrait("akskoda.cz", "05", false);
        testTrait("apa-skoda.cz", "05", false);

        testTrait("akskodaplzen.cz", "06", true);
        testTrait("akskoda.cz", "06", false);

        testTrait("apa-skoda.cz", "07", true);

        testTrait("apa-skoda.cz", "08", true); // Ads
        testTrait("auto-skoda.sk", "08", true); // Ads
        testTrait("dovozskoda.cz", "08", true); // Ads
        testTrait("eskoda.cz", "08", true);
        testTrait("fanklubskoda.cz", "08", true);
        testTrait("fotoskoda.cz", "08", false);
        testTrait("info-skoda-auto.sk", "08", false);
        testTrait("nahradni-dily-skoda.cz", "08", true);
        testTrait("skoda-auto.pl", "08", true);
        testTrait("skoda-autobaterie.cz", "08", true);
        testTrait("skoda-forum.cz", "08", true);
//        testTrait("skodafoto.cz", "08", true); obsah se dznamickz meni :(
        //     testTrait("skoda-nahradni-dily.cz", "08", true);     ma tam porno, ale nevim, jak to osefovat :)
//        skoda-web.cz - ma vlastni banery :(

        testTrait("autobazar-skoda-prodej-dovoz-aut-autoimport-brno-ojeta-auta.cz", "09", true); // SURL
        testTrait("skodafoto.cz", "09", true); // SURL
        testTrait("skodaland.cz", "09", true); // SURL

        testTrait("autoskoda.sk", "10", true); // Frames

        testTrait("autocentrumskoda.cz", "11", true); // SContent
        testTrait("autobazar-skoda.cz", "11", true); // SContent
        testTrait("nahradnedielyskoda.sk", "11", true); // SContent
        testTrait("fotoskoda.cz", "11", false); // SContent
        testTrait("skoda-auto.com", "11", false); // SContent
        testTrait("autopotahy-skoda.cz", "11", true); // SContent
//        testTrait("skoda-kolin.cz", "11", false); // SContent   ma keyword na konkurenci a pak presmeruje na ofiko, ktere je koser...
        testTrait("skoda-plus.cz", "11", true); // SContent    je to ofiko web, ale maji odkayz na konkurenci :)
        testTrait("skoda100.cz", "11", true); // SContent
        testTrait("skodateile.cz", "11", true); // SContent
        testTrait("skodavyzkum.cz", "11", true); // SContent
        testTrait("skodaweb.cz", "11", true); // SContent

        testTrait("autodopravaskoda.cz", "12", true); // GOwner
        testTrait("manazerskeporadenstvi-jiriskoda.cz", "12", true); // GOwner
        testTrait("quercus-skoda.cz", "12", true); // GOwner
        testTrait("romanskoda.cz", "12", true); // GOwner
        testTrait("skoda-nd.cz", "12", false); // GOwner
        testTrait("skoda130lr.cz", "12", false); // GOwner
        testTrait("strechy-skoda.cz", "12", true); // GOwner
        testTrait("autoczskoda.cz", "12", false); // GOwner
        testTrait("skodaoctavia.cz", "12", false); // GOwner

        testTrait("eskoda.cz", "13", true); //BForward

        testTrait("skoda-soutez.cz", "14", true); //SMS


        testTrait("skoda-eshop.cz", "15", true); //BLink
        testTrait("skoda-virt.cz", "15", true); //BLink
        testTrait("skodamotor.cz", "15", true); //BLink
        testTrait("skodateam.cz", "15", true); //BLink

        log.info(String.format("RESULTS: ALL tests = %d ERRORS = %d", allTests, errorTests));

    }

    private static void testTrait(String domainName, String traitId, boolean expectedValue) {
        Vertex domain = DbService.db.getDomain(domainName.trim());
        boolean error = false;
        boolean actual = false;
        if (domain != null) {
            Trait trait = TraitsFactory.INSTANCE.getTrait(traitId);

            String body = null;
            try {
                File file = new File("pages");
                file = new File(file, domainName);
                body = FileUtils.readFileToString(file, "UTF-8");
            } catch (IOException e) {
            }

            actual = trait.hasTrait(domain, body);
            if (actual != expectedValue) {
                error = true;
            }
        } else {
            error = true;
        }

        log.info(String.format("Test %-7s: domain='%-50s' trait='%s' expected='%-5s' actual='%-5s'", error ? "ERROR" : "SUCCESS", domainName, traitId, Boolean.toString(expectedValue), Boolean.toString(actual)));

        allTests++;
        if (error) errorTests++;
    }

}
