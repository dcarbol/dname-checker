package com.davidicius.dnc;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.index.OIndex;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
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

import java.io.IOException;
import java.util.Scanner;

public class GetAllDomains {
    private static final Logger log = LoggerFactory.getLogger(Starter.class);
    private static OrientGraph graph;

    private static boolean STOP = false;

    public static void main(String[] args) {
        log.debug("Get all domains 0.2 Started...");

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

        t.setDaemon(true);
        t.start();

        graph = Starter.startDb("all-domains");
//        String[] prefix = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        String[] prefix = {"N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",};
        int[] prefixLen = {13, 11, 33, 1, 18, 43, 20, 5, 16, 7, 1, 1, 10 };
        int _from = 12;
        int _to = 13;//prefix.length;

        BasicCookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();

        for (int jj = _from; jj < _to; ++jj) {
            for (int i = 1; i <= prefixLen[jj]; i++) {
                String url = "???";
                try {
                    url = "http://www.czdomeny.cz/seznam-cz-domen/" + prefix[jj] + (i == 1 ? "" : "/" + i + "/");
                    log.info("Loading: " + url);

                    String body = "";
                    CloseableHttpResponse response = null;
                    try {
                        HttpUriRequest request = RequestBuilder.get()
                                .setUri(url)
                                .build();

                        response = httpClient.execute(request);
                        HttpEntity entity = response.getEntity();

                        body = EntityUtils.toString(entity);
                  //      EntityUtils.consume(entity);
                        Document doc = Jsoup.parse(body);

                        response.close();
                        response = null;

                        //Document doc = Jsoup.connect(url).get();
                        Elements elements = doc.select("a[href^=/domena/]");

                        int size = elements.size();
                        log.info("Loaded " + size + " domains");
                        for (Element e : elements) {
                            String domainName = e.text().toLowerCase().trim();
                            OrientVertexType vertexType = graph.getVertexType("Domain");
                            OIndex<?> index = vertexType.getClassIndex("domainNameIndex");
                            Object o = index.get(domainName);
                            if (o == null) {
                                Vertex v = graph.addVertex("class:Domain");
                                v.setProperty("domainName", domainName);
                                v.setProperty("active", "true");
                                graph.commit();
                            }
                        }

                        if (size == 0 || STOP) {
                            break;
                        }
                    } finally {
                        if (response != null) response.close();
                    }

                    try {
                        Thread.sleep(60 * 1000);
                    } catch (InterruptedException e) {
                    }

                } catch (IOException e) {
                    log.error("Error loading: " + url);
                    try {
                        Thread.sleep(120 * 1000);
                    } catch (InterruptedException e1) {
                    }
                }
            }

            if (STOP) {
                break;
            }
        }

        try {
            httpClient.close();
        } catch (IOException e) {
        }

        graph.shutdown();
    }
}
