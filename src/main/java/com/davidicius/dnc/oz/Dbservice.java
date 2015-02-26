package com.davidicius.dnc.oz;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

public class DbService {
    private static final Logger log = LoggerFactory.getLogger(DbService.class);
    private OrientGraph graph;
    private ODatabaseDocumentTx database;
    private OIndex<?> domNameIndex;
    private OIndex<?> ozNumberIndex;

    public static DbService db = new DbService();

    public OrientGraph restart() throws IOException {
        if (graph != null) {
            graph.shutdown();
            graph = null;
        }

        return start();
    }

    public OrientGraph start() throws IOException {
        if (graph == null) {
            String path = "plocal:final-db";

            log.info("Starting " + path);
//             database = new ODatabaseDocumentTx(path);
//             if (database.exists()) {
//                 database.open("admin", "admin");
//             } else {
//                 database.create();
//             }

            graph = new OrientGraph(path, "admin", "admin");
            ODatabaseDocumentTx rawGraph = graph.getRawGraph();
            graph.setThreadMode(OrientBaseGraph.THREAD_MODE.MANUAL);
            ODatabaseRecordThreadLocal.INSTANCE.set(rawGraph);

            // Builds classes
            {
                OrientVertexType vertexType = graph.getVertexType("DOM");
                if (vertexType == null) {
                    vertexType = graph.createVertexType("DOM");
                    vertexType.createProperty("name", OType.STRING);
                    //                    vertexType.createIndex("DOM.name", OClass.INDEX_TYPE.UNIQUE, "name");
                }

                //                vertexType.createProperty("czDomeny", OType.STRING);
                //                vertexType.createProperty("czTom", OType.STRING);
                //                vertexType.createProperty("skDomeny", OType.STRING);
                //                vertexType.createProperty("owner", OType.STRING);

                //    vertexType.createIndex("DOM.name", OClass.INDEX_TYPE.UNIQUE, "name");
                //     graph.dropIndex("DOM.fulltext");
                //                ODocument metadata = new ODocument();
                //                metadata.field("indexRadix", true);
                //                metadata.field("stopWords", Arrays.asList(new String[] { "the", "in", "a", "at" }));
                //                metadata.field("separatorChars", " :;-[](.md)");
                //                metadata.field("ignoreChars", "$&");
                //                metadata.field("minWordLength", 3);
                //                vertexType.createIndex("DOM.fulltext", "FULLTEXT", null, metadata, null, new String[]{"name"});
                //  vertexType.createIndex("DOM.fulltext", "FULLTEXT", null, null, "LUCENE", new String[]{"name", "czDomeny", "czTom", "top1m", "skDomeny", "owner"});

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

//                vertexType.createProperty("ozNumber", OType.STRING);
//                vertexType.createIndex("OZ.ozNumber", OClass.INDEX_TYPE.NOTUNIQUE, "ozNumber");
//                     vertexType.createProperty("keys", OType.STRING);
                //     graph.dropIndex("OZ.name");
                //     vertexType.createIndex("OZ.name", "FULLTEXT", null, null, "LUCENE", new String[]{"name", "source", "owner", "keys"});
            }

            OrientVertexType vertexType = graph.getVertexType("DOM");
            domNameIndex = vertexType.getClassIndex("DOM.name");

            vertexType = graph.getVertexType("OZ");
            ozNumberIndex = vertexType.getClassIndex("OZ.ozNumber");

            log.info("DB started");
        }

        // directoryDomName = createDomNameLuceneIndex(graph);
        //    directoryDom = createDomLuceneIndex(graph);

        return graph;
    }

    public OrientGraph graph() {
        return this.graph;
    }

    public void shutdown() {
        this.graph.shutdown();
    }

    public OIndex<?> getDomNameIndex() {
        return domNameIndex;
    }

    public Vertex getDomain(String domainName) {
        Object o = DbService.db.getDomNameIndex().get(domainName);
        if (o != null) {
            return DbService.db.graph().getVertex(o);
        } else {
            return null;
        }
    }

    public Vertex getOz(String ozNumber) {
        Object o = ozNumberIndex.get(ozNumber);
        if (o instanceof Set) {
            Set s = (Set) o;
            if (s.isEmpty()) {
                return null;
            }

            if (s.size() > 1) {
                return null;
            }

            Object oo = s.iterator().next();
            return DbService.db.graph().getVertex(oo);
        } else {
            return null;
        }
    }

}
