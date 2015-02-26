package com.davidicius.dnc;

import com.davidicius.dnc.oz.DbService;
import com.davidicius.dnc.oz.OzWorker;
import com.orientechnologies.orient.core.index.OIndex;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MergeDbDomains {
    private static final Logger log = LoggerFactory.getLogger(MergeDbDomains.class);

    public static void main(String[] args) throws IOException {
        OrientGraph graph = Starter.startDb("plocal:db-domains-14");
        HashMap<String, String> domainToOwner = new LinkedHashMap<String, String>();

        Iterable<Vertex> vertices = graph.getVertices();
        for (Vertex v : vertices) {
            String domainName = v.getProperty("domainName");
//            String ok = v.getProperty("ok");
//            String urenregistrtor = v.getProperty("urenregistrtor");
//            String domainName2 = v.getProperty("domnovjmno");
//            String doasnkontakt = v.getProperty("doasnkontakt");
//            String administrativnkontakt = v.getProperty("administrativnkontakt");
//            String registraceod = v.getProperty("registraceod");
//            String zabezpeenopomocdnssec = v.getProperty("zabezpeenopomocdnssec");
//            String poslednaktualizace = v.getProperty("poslednaktualizace");
//            String datumexpirace = v.getProperty("datumexpirace");
            String domainOwner = v.getProperty("dritel");
//            String stav = v.getProperty("stav");

            domainToOwner.put(domainName, domainOwner);
        }
        graph.shutdown();

        OrientGraph allGraph = DbService.db.start();
        int existed = 0;
        int ignored = 0;
        for (Map.Entry<String, String> e : domainToOwner.entrySet()) {
            String domainName = e.getKey();
            String domainOwner = e.getValue();

            Object o = DbService.db.getDomNameIndex().get(domainName);
            if (o == null) {
                throw new IllegalStateException("Domain does not exists: " + domainName);
            }

            OrientVertex vertex = allGraph.getVertex(o);
            if (domainOwner != null && !domainOwner.equals("")) {
                vertex.setProperty("owner", domainOwner.trim() );
                allGraph.commit();
            } else {
                ignored++;
            }

            existed++;
        }

        log.info("Existed " + existed + " domains");
        log.info("Ignored " + ignored + " domains");

    }
}
