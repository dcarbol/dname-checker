package com.davidicius.dnc;


import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
        log.debug("Loading: " + domainName);
        Document doc = null;
        try {
            doc = Jsoup.connect("http://www.nic.cz/whois/?q=" + domainName).get();
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
        if (doc.toString().contains("Záznam nenalezen")) {
            log.info("No results for domain '" + domainName + "'");
            return Collections.emptyMap();
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
            key = adjustKey(key);

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

    private static String adjustKey(String key) {
        return key.toLowerCase().replaceAll("[^a-z0-9]", "");
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
                    vertexType.createProperty("domainName", OType.STRING);
                    vertexType.createIndex("domainNameIndex", OClass.INDEX_TYPE.UNIQUE, "domainName");

                    vertexType.createProperty("lastModified", OType.LONG);
                }
            }

            graph.commit();
        }

        return graph;
    }

    public static void shutdown() {
        log.debug("Shutting down GModel...");

        database.close();
        graph = null;
    }

    private static boolean STOP = false;

    public static void main(String[] args) throws IOException {
        log.debug("DNC 0.2 Started...");

        log.info("Setting up stopper...");
        Thread t = new Thread(new Runnable() {
            public void run() {
                log.info("Stopper started...");
                Scanner sc = new Scanner(System.in);
                while (sc.hasNextLine()) {
                    String command = sc.nextLine();
                    if (command.equalsIgnoreCase("STOP")) {
                        STOP = true;
                        break;
                    }
                }

                log.info("Stopper stopped...");
            }
        });

        t.setDaemon(false);
  //      t.start();

        log.info("Start loading list of domains...");

        List<String> domains = loadDomainList();
        log.info("End loading list of domains...");

        startDb("db-domains");
        // Find some non-loaded...
        for (String domain : domains) {
            OrientVertexType vertexType = graph.getVertexType("Domain");
            OIndex<?> index = vertexType.getClassIndex("domainNameIndex");
            Object o = index.get(domain);
            if (o == null) {
                Map<String, String> map = null;
                try {
                    map = loadDomainInformation2(domain);
                } catch (IOException e) {
                    e.printStackTrace();
                }

//                Map<String, String> map = Collections.EMPTY_MAP;
                if (map != null) {
                    Vertex v = graph.addVertex("class:Domain");
                    v.setProperty("domainName", domain);
                    for (String key : map.keySet()) {
                        String value = map.get(key);
                        v.setProperty(key, value);
                    }
                    graph.commit();
                }

                try {
                    Thread.sleep(1 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                if (STOP) {
                    break;
                }
            }
        }

        shutdown();
    }

    public static Map<String, String> loadDomainInformation2(String domainName) throws IOException {
        log.debug("Loading2: " + domainName);
        BasicCookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();

        String body = "";
        try {
            HttpUriRequest request = RequestBuilder.get()
                    .setUri("http://www.nic.cz/whois/")
                    .addParameter("q", domainName)
                    .build();

            CloseableHttpResponse response = httpClient.execute(request);
            try {
                HttpEntity entity = response.getEntity();

                body = EntityUtils.toString(entity);
                Document doc = Jsoup.parse(body);
                Elements c = doc.select("#captcha_img");
                if (c.size() > 0) {
                    log.debug("Upss.. Captcha");

                    Elements captchaKey = doc.select("#captchakey");
                    String cKey = captchaKey.get(0).attr("value");
                    String imgUrl = "http://www.nic.cz" + c.get(0).attr("src");
                    System.out.println(imgUrl);

                    Scanner sc = new Scanner(System.in);
                    String cValue = null;
                    while (sc.hasNextLine()) {
                        cValue = sc.nextLine();
                        break;
                    }

                    HttpUriRequest captchaRequest = RequestBuilder.get()
                            .setUri("http://www.nic.cz/whois/")
                            .addParameter("captchakey", cKey)
                            .addParameter("q", domainName)
                            .addParameter("captcha", cValue)
                            .build();

                    CloseableHttpResponse captchaResponse = httpClient.execute(captchaRequest);
                    try {
                        HttpEntity entity2 = captchaResponse.getEntity();
                        body = EntityUtils.toString(entity2);
                        doc = Jsoup.parse(body);
                    } finally {
                        captchaResponse.close();
                    }
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
                    key = adjustKey(key);

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


//                List<Cookie> cookies = cookieStore.getCookies();
//                if (cookies.isEmpty()) {
//                    System.out.println("None");
//                } else {
//                    for (int i = 0; i < cookies.size(); i++) {
//                        System.out.println("- " + cookies.get(i).toString());
//                    }
//                }
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
    }
}

