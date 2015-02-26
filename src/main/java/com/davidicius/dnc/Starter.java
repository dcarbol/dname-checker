package com.davidicius.dnc;


import com.DeathByCaptcha.*;
import com.DeathByCaptcha.Exception;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import com.tinkerpop.pipes.util.structures.Pair;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
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

import java.io.*;
import java.util.*;

public class Starter {
    private static final Logger log = LoggerFactory.getLogger(Starter.class);
    private static OrientGraph graph;
    private static ODatabaseDocumentTx database;

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
            log.info("Starting " + path);

            database = new ODatabaseDocumentTx(path);
         //   if (database.exists()) {
                database.open("admin", "admin");
          //  } else {
          //      database.create();
          //  }

            graph = new OrientGraph(database, true);

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

            // Builds classes
            {
                OrientVertexType vertexType = graph.getVertexType("OZ");
                if (vertexType == null) {
                    vertexType = graph.createVertexType("OZ");
//                    vertexType.createProperty("name", OType.STRING);
//                    vertexType.createIndex("OZ.name", "FULLTEXT", null, null, "LUCENE", new String[]{"name"});
//                    vertexType.createProperty("lastModified", OType.LONG);
                }

           //     vertexType.createProperty("owner", OType.STRING);
           //     vertexType.createProperty("keys", OType.STRING);
           //     graph.dropIndex("OZ.name");
           //     vertexType.createIndex("OZ.name", "FULLTEXT", null, null, "LUCENE", new String[]{"name", "source", "owner", "keys"});
            }

            log.info("DB started");

        }

        return graph;
    }

    public static void shutdown() {
        log.debug("Shutting down DB...");

        database.close();
        graph = null;
    }

    private static boolean STOP = false;

    public static void main(String[] args) throws IOException {
        log.debug("DNC 0.2 Started...");
        String dbName = args[0];
        int modulo = Integer.parseInt(args[1]);
        log.info("Using: " + dbName + " with modulo " + modulo);

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
        t.start();

        log.info("Start loading list of domains...");

        List<String> domains = loadDomainList();
        log.info("End loading list of domains...");
        startDb(dbName);

        long last = 0;
        int count = 0;
        int overall = 0;
        for (int i = 0; i < domains.size(); ++i) {
            String domain = domains.get(i);
            if (i % 20 != modulo) continue;

            OrientVertexType vertexType = graph.getVertexType("Domain");
            OIndex<?> index = vertexType.getClassIndex("domainNameIndex");
            Object o = index.get(domain);
            if (o == null) {
                Map<String, String> map = null;
                try {
                    map = loadDomainInformation2(domain, modulo);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Vertex v = graph.addVertex("class:Domain");
                v.setProperty("domainName", domain);
                v.setProperty("ok", map != null ? "true" : false);
                if (map != null) {
                    for (String key : map.keySet()) {
                        String value = map.get(key);
                        v.setProperty(key, value);
                    }
                }
                graph.commit();

                count++;
                overall++;
                long cur = System.currentTimeMillis();
                if (cur - last > 10 * 60 * 1000) {
                    log.info(String.format("All loaded: %d, band %f", overall, count/((cur - last)/1000.0)*60*60*24));
                    last = cur;
                    count = 0;
                }

                if (STOP) {
                    break;
                }
            }
        }

        shutdown();
    }

    public static Map<String, String> loadDomainInformation2(String domainName, int modulo) throws IOException {
        log.debug("Loading: " + domainName);
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
                Pair<Client, Captcha> clientCaptcha = null;
                if (c.size() > 0) {
//                    log.debug("Upss.. Captcha");

                    Elements captchaKey = doc.select("#captchakey");
                    String cKey = captchaKey.get(0).attr("value");
                    String imgUrl = "http://www.nic.cz" + c.get(0).attr("src");

//                    Scanner sc = new Scanner(System.in);
//                    String cValue = null;
//                    while (sc.hasNextLine()) {
//                        cValue = sc.nextLine();
//                        break;
//                    }

                    clientCaptcha = solveCaptcha(imgUrl, modulo);
                    if (clientCaptcha != null) {
                        HttpUriRequest captchaRequest = RequestBuilder.get()
                                .setUri("http://www.nic.cz/whois/")
                                .addParameter("captchakey", cKey)
                                .addParameter("q", domainName)
                                .addParameter("captcha", clientCaptcha.getB().text.toLowerCase())
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
                }

                if (doc.toString().contains("Záznam nenalezen")) {
                    log.info("No results for domain '" + domainName + "'");
                    return Collections.emptyMap();
                }

                Elements select = doc.select("table.result>tbody>tr");
                if (select.size() == 0) {
                    if (body.contains("Kontrolní kód nesouhlasí")) {
                        log.warn("Bad captcha guess");
                        reportBadCaptcha(clientCaptcha, modulo);
                    } else {
                        log.warn("Corrupted result page for domain '" + domainName + "'");
                    }
                    return null;
                }

                Map<String, String> result = new HashMap<String, String>(8);
                for (Element element : select) {
                    Elements list = element.getElementsByTag("th");
                    if (list.size() != 1) {
                        if (body.contains("Kontrolní kód nesouhlasí")) {
                            log.warn("Bad captcha guess");
                            reportBadCaptcha(clientCaptcha, modulo);
                        } else {
                            log.warn("Corrupted result page for domain '" + domainName + "'");
                        }
                        return null;
                    }

                    Element e = list.get(0);
                    String key = e.text().trim();
                    key = adjustKey(key);

                    list = element.getElementsByTag("td");
                    if (list.size() != 1) {
                        if (body.contains("Kontrolní kód nesouhlasí")) {
                            log.warn("Bad captcha guess");
                            reportBadCaptcha(clientCaptcha, modulo);
                        } else {
                            log.warn("Corrupted result page for domain '" + domainName + "'");
                        }
                        return null;
                    }

                    e = list.get(0);
                    String value = e.text().trim();

                    result.put(key, value);
                }

                if (clientCaptcha != null) {
                    clientCaptcha.getA().close();
                }
                return result;
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
    }

    private static void reportBadCaptcha(Pair<Client, Captcha> clientCaptcha, int modulo) {
        try {
            FileUtils.copyFile(new File("c:/devel/cap" + modulo + ".jpeg"), new File("c:/devel/corrupted-" + clientCaptcha.getB().text + ".jpeg"));
            clientCaptcha.getA().report(clientCaptcha.getB());
            clientCaptcha.getA().close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }


    public static Pair<Client, Captcha> solveCaptcha2(String url, int modulo) {
        HttpUriRequest request = RequestBuilder.get()
                .setUri(url)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .build();

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            InputStream inputStream = entity.getContent();
            FileOutputStream fw = new FileOutputStream(new File("c:/devel/cap" + modulo + ".jpeg"));
            byte[] buffer = new byte[1024 * 16];
            while (true) {
                int i = inputStream.read(buffer);
                if (i > 0) {
                    fw.write(buffer, 0, i);
                } else {
                    break;
                }
            }
            fw.close();
            response.close();
            httpClient.close();

            Client client = new SocketClient("karbol", "Verunka");
//            client.isVerbose = true;

            try {
//                System.out.println("Your balance is " + client.getBalance() + " US cents");
                Captcha captcha = client.decode("c:/devel/cap" + modulo + ".jpeg", 60);
                if (null != captcha) {
//                    log.debug("CAPTCHA " + captcha.id + " solved: " + captcha.text);
                    return new Pair<Client, Captcha>(client, captcha);
                } else {
                    log.error("Failed solving CAPTCHA");
                    return null;
                }
            } catch (IOException e) {
                log.error(e.getMessage());
                return null;
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                return null;
            } catch (Exception e) {
                log.error(e.getMessage());
                return null;
            }
        } catch (ClientProtocolException e) {
            log.error(e.getMessage());
            return null;
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public static Pair<Client, Captcha> solveCaptcha(String url, int modulo) {
        HttpUriRequest request = RequestBuilder.get()
                .setUri(url)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .build();

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            InputStream inputStream = entity.getContent();
            FileOutputStream fw = new FileOutputStream(new File("c:/devel/cap" + modulo + ".jpeg"));
            byte[] buffer = new byte[1024 * 16];
            while (true) {
                int i = inputStream.read(buffer);
                if (i > 0) {
                    fw.write(buffer, 0, i);
                } else {
                    break;
                }
            }
            fw.close();
            response.close();
            httpClient.close();

            Client client = new SocketClient("karbol", "Verunka");
//            client.isVerbose = true;

            try {
//                System.out.println("Your balance is " + client.getBalance() + " US cents");
                Captcha captcha = client.decode("c:/devel/cap" + modulo + ".jpeg", 60);
                if (null != captcha) {
//                    log.debug("CAPTCHA " + captcha.id + " solved: " + captcha.text);
                    return new Pair<Client, Captcha>(client, captcha);
                } else {
                    log.error("Failed solving CAPTCHA");
                    return null;
                }
            } catch (IOException e) {
                log.error(e.getMessage());
                return null;
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                return null;
            } catch (Exception e) {
                log.error(e.getMessage());
                return null;
            }
        } catch (ClientProtocolException e) {
            log.error(e.getMessage());
            return null;
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}

