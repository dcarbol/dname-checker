package com.davidicius.dnc.oz;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

public class MyGraph extends OrientGraph {
    public MyGraph(ODatabaseDocumentTx iDatabase) {
        super(iDatabase);
    }

    public MyGraph(ODatabaseDocumentTx iDatabase, boolean iAutoStartTx) {
        super(iDatabase, iAutoStartTx);
    }

}
