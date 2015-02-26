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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class LoadTop1m {
    private static final Logger log = LoggerFactory.getLogger(LoadDomain.class);
    private static OrientGraph allGraph;

    public static List<String> loadDomainList() {
        String userDir = System.getProperty("user.dir");
        File file = new File(userDir, "top-1m.csv");
        if (file.exists() && file.isFile()) {
            ArrayList<String> result = new ArrayList<String>(1000 * 100);
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                while (true) {
                    String line = br.readLine();
                    if (line == null) break;

                    String name = line.trim().toLowerCase();
                    String[] parts = name.split(",");
                    result.add(parts[1].trim());
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

    public static void main(String[] args) throws IOException {
      //  allGraph = OzWorker.startDb("final-db");
        List<String> list = loadDomainList();
        int newCount = 0;
        int existed = 0;
        int rank = 1;
        for (String d : list) {
            String domainName = d.toLowerCase().trim();

            OrientVertexType vertexType = allGraph.getVertexType("DOM");
            OIndex<?> index = vertexType.getClassIndex("DOM.name");
            Object o = index.get(domainName);
            if (o == null) {
                Vertex newV = allGraph.addVertex("class:DOM");
                newV.setProperty("name", domainName);
                newV.setProperty("top1m", "TRUE");
                newV.setProperty("top1mRank", Integer.toString(rank));
                allGraph.commit();
                newCount++;
            } else {
                OrientVertex vertex = allGraph.getVertex(o);
           //     vertex.setProperty("top1m", "TRUE");
                vertex.setProperty("top1mRank", Integer.toString(rank));
                allGraph.commit();
                existed++;
            }

            rank++;
        }

        log.info("Added " + newCount + " domains");
        log.info("Existed " + existed + " domains");

        allGraph.shutdown();
    }
}
