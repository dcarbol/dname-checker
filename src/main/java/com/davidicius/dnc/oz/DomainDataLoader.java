package com.davidicius.dnc.oz;

import com.davidicius.dnc.Starter;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.ODatabaseRecordTx;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.object.db.OObjectDatabaseTxPooled;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DomainDataLoader {
    private ArrayBlockingQueue<Runnable> queue;
    private ThreadPoolExecutor threadPool;

    private static final Logger log = LoggerFactory.getLogger(DomainDataLoader.class);
    private static PoolingHttpClientConnectionManager POOL = new PoolingHttpClientConnectionManager();

    static {
        POOL.setMaxTotal(5);
    }

    public static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.151 Safari/535.19";
    //    private static CloseableHttpClient CLIENT = HttpClients.custom().setConnectionManager(POOL).build();
    private static final CloseableHttpClient CLIENT = HttpClients.custom()
            .setUserAgent(USER_AGENT)
            .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).build())
            .build();

    public static boolean doesDomainExist(String domainName) {
        try {
            log.info("Check existence of domain: " + domainName);
            InetAddress inetAddress = InetAddress.getByName(domainName.trim().toLowerCase());
            log.info("Domain " + domainName + " exists");
            return true;
        } catch (UnknownHostException e) {
            log.info("Domain " + domainName + " DOES NOT exists");
            return false;
        }
    }

    public static String entityToString(final HttpEntity entity, final Charset defaultCharset) throws IOException, ParseException {
         Args.notNull(entity, "Entity");

         final InputStream instream = entity.getContent();
         if (instream == null) {
             return null;
         }

         try {
             Args.check(entity.getContentLength() <= Integer.MAX_VALUE, "HTTP entity too large to be buffered in memory");
             int i = (int)entity.getContentLength();
             if (i < 0) {
                 i = 4096;
             }

             Charset charset = null;
             try {
                 final ContentType contentType = ContentType.get(entity);
                 if (contentType != null) {
                     charset = contentType.getCharset();
                 }
             } catch (final UnsupportedCharsetException ex) {
                 throw new UnsupportedEncodingException(ex.getMessage());
             }

//             if (charset == null) {
//                 charset = defaultCharset;
//             }
//             if (charset == null) {
//                 charset = HTTP.DEF_CONTENT_CHARSET;
//             }

             //final Reader reader = new InputStreamReader(instream, charset);
             final ByteArrayBuffer buffer = new ByteArrayBuffer(i);
             int size;
             byte[] byteBuffer = new byte[100*1000];
             while((size = instream.read(byteBuffer)) != -1) {
                 buffer.append(byteBuffer, 0, size);
             }

             if (charset == null) {
                 Document doc = Jsoup.parse(new String(buffer.buffer()));
                 if (doc != null) {
                     Elements elements = doc.select("meta[http-equiv]");
                     for (Element e : elements) {
                         String httpEquiv = e.attr("http-equiv");
                         if (!httpEquiv.equalsIgnoreCase("content-type")) {
                             continue;
                         }

                         String content = e.attr("content");
                         if (content != null) {
                            String[] parts = content.split(";");
                            for (String part : parts) {
                                part = part.trim().toLowerCase();
                                if(part.startsWith("charset")) {
                                    String[] kv = part.split("=");
                                    if (kv.length == 2) {
                                        String cs = kv[1].trim();
                                        charset = Charset.forName(cs);
                                    }
                                }
                            }
                         }
                     }
                 }
             }

             if (charset == null) {
                 charset = defaultCharset;
             }

             return new String(buffer.buffer(), charset);
         } finally {
             instream.close();
         }
     }

    public static boolean loadPage(String domainName, List<String> forwards) throws IOException {
        log.info("Start loading page: " + domainName);

        BasicCookieStore cookieStore = new BasicCookieStore();
//        CloseableHttpClient httpClient = HttpClients.custom()
//                .setDefaultCookieStore(cookieStore)
//                .build();

        byte[] start = new byte[1000*4];
        try {
            HttpClientContext context = HttpClientContext.create();
            HttpUriRequest request = RequestBuilder.get()
                    .setUri("http://" + domainName)
                    .build();

            CloseableHttpResponse response = CLIENT.execute(request, context);
            try {
                HttpEntity entity = response.getEntity();
                List<URI> locations = context.getRedirectLocations();
                if (locations != null) {
                    for (URI uri : locations) {
                        forwards.add(uri.toASCIIString());
                    }
                }

//                Header contentEncoding = entity.getContentEncoding();

                String body = entityToString(entity, Charset.forName("UTF-8"));
//                String body = entityToString(entity, Charset.forName("windows-1250"));
//                String body = EntityUtils.toString(entity, "WINDOWS-1250");

                File file = new File("pages");
                file = new File(file, domainName);

                FileUtils.writeStringToFile(file, body);
                log.info("Page loaded: " + domainName);

                return true;
            } finally {
                response.close();
            }
        } finally {
        }
    }

    public static class Consumer implements Runnable {
        private static final Logger log = LoggerFactory.getLogger(Consumer.class);
        final private OrientGraph graph;
        final private ODatabaseDocumentTx documentTx;

        private String domainName;

        public Consumer(String domainName, OrientGraph graph, ODatabaseDocumentTx documentTx) {
            this.domainName = domainName;
            this.graph = graph;
            this.documentTx = documentTx;
        }

        public void run() {
            log.info("Starting consumer for " + domainName);

            OIndex<?> domNameIndex;
            String exists = "?";

            boolean loadOwner = false;
            boolean czDomain = domainName.endsWith(".cz");
            boolean loadPage = false;

            Vertex v = null;
            synchronized (graph) {
                ODatabaseRecordThreadLocal.INSTANCE.set(documentTx);
                OrientVertexType vertexType = graph.getVertexType("DOM");
                domNameIndex = vertexType.getClassIndex("DOM.name");

                Object o = domNameIndex.get(domainName);
                if (o == null) throw new IllegalStateException();

                v = graph.getVertex(o);
                if (v == null) throw new IllegalStateException();

                if (v.getProperty("exists") != null) {
                    exists = v.getProperty("exists").toString();
                }

                String owner = v.getProperty("owner");
                if (owner == null || owner.equals("NotAv") || owner.equals("")) {
                    loadOwner = true;
                }

                String loaded = v.getProperty("loaded");
                if (loaded == null || loaded.equals("?")) {
                    loadPage = true;
                }

                graph.commit();
            }

            if (exists.equalsIgnoreCase("?")) {
                boolean e = doesDomainExist(domainName);
                synchronized (graph) {
                    exists = e ? "E" : "N";
                    v.setProperty("exists", exists);
                    graph.commit();
                }
            }

            if (exists.equalsIgnoreCase("E")) {
                if (loadOwner && czDomain) {
                    try {
                        Map<String, String> map = Starter.loadDomainInformation2(domainName, Thread.currentThread().hashCode() % 200);
                        if (map == null) {
                            map = Starter.loadDomainInformation2(domainName, Thread.currentThread().hashCode() % 200);
                        }

                        if (map != null && map.size() != 0) {
                            synchronized (graph) {
                                for (String key : map.keySet()) {
                                    String value = map.get(key);
                                    v.setProperty(key, value);
                                }

                                String domainOwner = map.get("dritel");
                                if (domainOwner != null && !domainOwner.equals("")) {
                                    v.setProperty("owner", domainOwner);
                                }

                                graph.commit();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (loadPage) {
                    try {
                        List<String> forwards = new ArrayList<String>();
                        boolean result = loadPage(domainName, forwards);

                        StringBuilder sb = new StringBuilder(32);
                        for (String f : forwards) {
                            f = f.trim().toLowerCase();
                            sb.append(f).append(" ");
                        }

                        if (result) {
                            synchronized (graph) {
                                v.setProperty("loaded", "L");
                                v.setProperty("forward", sb.toString().trim());

                                graph.commit();
                            }
                        }
                    } catch (IOException e) {
                        synchronized (graph) {
                            v.setProperty("loaded", "E");
                            graph.commit();
                        }
                    }
                }

            }

            // Updating traits.
            String body = null;
            try {
                File file = new File("pages");
                file = new File(file, domainName);
                body = FileUtils.readFileToString(file);
            } catch (IOException e) {

            }

            StringBuilder sb = new StringBuilder();
            for (Trait trait : TraitsFactory.INSTANCE.traits()) {
                boolean result = trait.hasTrait(v, body);
                if (result) {
                    sb.append(trait.getId()).append(" ");
                }
            }

            synchronized (graph) {
                v.setProperty("traits", sb.toString().trim());
                graph.commit();
            }

            log.info("Task finished for " + domainName);
        }


    }

    public DomainDataLoader() {
        log.info("Starting Domain Data Loader...");
        this.queue = new ArrayBlockingQueue<Runnable>(5000);
        this.threadPool = new ThreadPoolExecutor(5, 5, 5, TimeUnit.SECONDS, this.queue);
    }

    public void processDomain(String domainName, OrientGraph graph) {
        this.threadPool.execute(new Consumer(domainName, graph, graph.getRawGraph()));
    }

    public void stop() {
        log.info("Stopping Domain Data Loader...");
        this.threadPool.shutdown();
        log.info("Domain Data Loader stopped...");
    }

    public int countActiveTasks() {
        return this.threadPool.getActiveCount();
    }

}
