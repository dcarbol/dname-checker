package com.davidicius.dnc;

import com.davidicius.dnc.oz.OzWorker;
import com.orientechnologies.orient.core.index.OIndex;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class LoadDomain {
    private static final Logger log = LoggerFactory.getLogger(LoadDomain.class);
    private static OrientGraph graph;
    private static OrientGraph allGraph;

    public static void main(String[] args) throws IOException {
        graph = Starter.startDb("all-domains");
     //   allGraph = OzWorker.startDb("final-db");
//        List<String> list = Starter.loadDomainList();
        int newCount = 0;
        int existed = 0;
        Iterable<Vertex> vertices = graph.getVertices();
        for (Vertex v : vertices) {
//        for (String d : list) {
            String domainName = v.getProperty("domainName").toString().toLowerCase().trim();
//            String domainName = d.toLowerCase().trim();

            OrientVertexType vertexType = allGraph.getVertexType("DOM");
            OIndex<?> index = vertexType.getClassIndex("DOM.name");
            Object o = index.get(domainName);
            if (o == null) {
//                Vertex newV = allGraph.addVertex("class:DOM");
//                newV.setProperty("name", domainName);
//                newV.setProperty("czTom", "YES");
//                allGraph.commit();
                newCount++;
            } else {
                OrientVertex vertex = allGraph.getVertex(o);
                vertex.setProperty("czDomeny", "TRUE");
                allGraph.commit();
                existed++;
            }
        }

        log.info("Added " + newCount + " domains");
        log.info("Existed " + existed + " domains");

        allGraph.shutdown();
//        graph.shutdown();
    }
}
