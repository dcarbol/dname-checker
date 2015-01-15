package com.davidicius.dnc;


import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class Starter {
    private static final Logger log = LoggerFactory.getLogger(Starter.class);
    private static OrientGraph graph;
    private static ODatabaseDocumentTx database;

    public static Map<String, String> loadDomainInformation(String domainName) {
        Document doc = null;
        try {
            doc = Jsoup.connect("http://www.nic.cz/whois/?q=" + domainName).get();
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
        if (doc.toString().contains("Záznam nenalezen")) {
            log.info("No results for domain '" + domainName + "'");
            return null;
        }

        Elements select = doc.select("table.result>tbody>tr");
        if (select.size() == 0) {
            log.warn("Corrupted result page for domain '" + domainName + "'");
            return null;
        }

        Map<String, String> result = new HashMap<String, String>(8);
        for (Element element : select) {
            Elements list = element.getElementsByTag("th");
            if (list.size() != 1) {
                log.warn("Corrupted result page for domain '" + domainName + "'");
                continue;
            }

            Element e = list.get(0);
            String key = e.text().trim();

            list = element.getElementsByTag("td");
            if (list.size() != 1) {
                log.warn("Corrupted result page for domain '" + domainName + "'");
                continue;
            }

            e = list.get(0);
            String value = e.text().trim();

            result.put(key, value);
        }

        return result;
    }

    public static List<String> loadDomainList() {
        String userDir = System.getProperty("user.dir");
        File file = new File(userDir, "CZ-domeny.txt");
        if (file.exists() && file.isFile()) {
            ArrayList<String> result = new ArrayList<String>(1000 * 100);
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                while (true) {
                    String line = br.readLine();
                    if (line == null) break;

                    String name = line.trim().toLowerCase();
                    result.add(name);
                }

                return result;
            } catch (IOException e) {
                log.error(e.getMessage());
                return Collections.emptyList();
            }
        } else {
            log.error("Cannot file list of domains...");
            return Collections.emptyList();
        }
    }

    public static OrientGraph startDb(String path) {
        if (graph == null) {
            log.debug("Starting GModel...");
            database = new ODatabaseDocumentTx("plocal:" + path);
            if (database.exists()) {
                database.open("admin", "admin");
            } else {
                database.create();
            }

            graph = new OrientGraph(database, false);

            // Builds classes
            {
                OrientVertexType vertexType = graph.getVertexType("Domain");
                if (vertexType == null) {
                    vertexType = graph.createVertexType("Domain");
                    vertexType.createProperty("relativePath", OType.STRING);
                    vertexType.createProperty("lastModified", OType.LONG);
                    vertexType.createIndex("relativePathIndex", OClass.INDEX_TYPE.UNIQUE, "relativePath");
                }
            }
        }

        return graph;
    }

    public static void shutdown() {
          log.debug("Shutting down GModel...");

          database.close();
          graph = null;
      }

    public static void main(String[] args) throws IOException {
        log.debug("DNC 0.2 Started...");
        log.info("Start loading list of domains...");

        List<String> domains = loadDomainList();
        log.info("End loading list of domains...");

        startDb("db-domains");

        String domainName = "KOFOLA.cz";
        Map<String, String> map = loadDomainInformation(domainName);
        if (map != null) {
            log.info(map.toString());
        }

        shutdown();
    }
}

