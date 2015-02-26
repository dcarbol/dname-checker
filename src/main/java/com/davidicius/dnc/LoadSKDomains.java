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

public class LoadSKDomains {
    private static final Logger log = LoggerFactory.getLogger(LoadDomain.class);
    private static OrientGraph allGraph;
    private static ArrayList<String> domainNames = new ArrayList<String>(1000 * 100);
    private static ArrayList<String> domainOwners = new ArrayList<String>(1000 * 100);

    public static void loadDomainList() {
        String userDir = System.getProperty("user.dir");
        File file = new File(userDir, "SK-domeny.txt");
        if (file.exists() && file.isFile()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                while (true) {
                    String line = br.readLine();
                    if (line == null) break;

                    line = line.trim().toLowerCase();
                    if (line.equals("")) continue;

                    if (line.startsWith("--")) continue;

                    if (line.startsWith("domena")) continue;

                    String[] parts = line.split(";");
                    if (parts.length < 4 ) {
                        log.info("Ignore:" + line);
                        continue;
                    }

                    String domainName = parts[0].trim();
                    String domainOwner = parts[2].trim();
                    String domainFlag = parts[3].trim();
                    //String domainIco = parts[9].trim();

                    if (!domainFlag.equals("new"))  {
                        System.out.println("problem");
                      //  domainOwner = domainIco;
                    }

                    domainNames.add(domainName);
                    domainOwners.add(domainOwner);
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        } else {
            log.error("Cannot file list of domains...");
        }
    }

    public static void main(String[] args) throws IOException {
    //    allGraph = OzWorker.startDb("final-db");

        loadDomainList();
        int newCount = 0;
        int existed = 0;
        for (int i = 0; i < domainNames.size(); ++i) {
            String domainName = domainNames.get(i);
            String domainOwner = domainOwners.get(i);

            OrientVertexType vertexType = allGraph.getVertexType("DOM");
            OIndex<?> index = vertexType.getClassIndex("DOM.name");
            Object o = index.get(domainName);
            if (o == null) {
                Vertex newV = allGraph.addVertex("class:DOM");
                newV.setProperty("name", domainName);
                newV.setProperty("czDomeny", "FALSE");
                newV.setProperty("czTom", "FALSE");
                newV.setProperty("top1m", "FALSE");
                newV.setProperty("skDomeny", "TRUE");
                newV.setProperty("top1mRank", "NotAv");
                newV.setProperty("owner", domainOwner);

                allGraph.commit();
                newCount++;
            } else {
                OrientVertex vertex = allGraph.getVertex(o);
                vertex.setProperty("skDomeny", "TRUE");
                vertex.setProperty("owner", domainOwner);
                allGraph.commit();
                existed++;
            }
        }

        log.info("Added " + newCount + " domains");
        log.info("Existed " + existed + " domains");

        allGraph.shutdown();
    }
}
