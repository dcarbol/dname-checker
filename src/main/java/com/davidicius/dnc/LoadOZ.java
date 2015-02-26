package com.davidicius.dnc;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
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

public class LoadOZ {
    private static final Logger log = LoggerFactory.getLogger(LoadOZ.class);
    private static OrientGraph graph;

    public static void main(String[] args) throws IOException {
        graph = Starter.startDb("final-db");

        File currentDir = new File(System.getProperty("user.dir"));
        Collection<File> files = FileUtils.listFiles(currentDir, new String[] {"upv"}, false);
        for (File f : files) {
            log.info("Loading " + f.getAbsolutePath());
            Document doc = Jsoup.parse(f, "UTF-8");
            Elements elements = doc.select("table#listtemplatesadv tr");
            boolean first = true;
            int count = 0;
            for (Element e : elements) {
                if (first) {
                    first = false;
                    continue;
                }

                String cPrihlasky = e.select("td:eq(2)").text();
                String cZapisu = e.select("td:eq(3)").text();
                String priorita = e.select("td:eq(4)").text();
                String zneni = e.select("td:eq(5)").text();
                String stav = e.select("td:eq(6)").text();
                String majitel = e.select("td:eq(7)").text();
                String tridy = e.select("td:eq(8)").text();
                String d = e.select("td:eq(9)").text();
                String sn = e.select("td:eq(10)").text();

                Vertex v = graph.addVertex("class:OZ");
                v.setProperty("ozNumber", cPrihlasky);
                v.setProperty("ozId", cZapisu);
                v.setProperty("date", priorita);
                v.setProperty("name", zneni);
                v.setProperty("status", stav);
                v.setProperty("owner", majitel);
                v.setProperty("classes", tridy);
                v.setProperty("d", d);
                v.setProperty("sn", sn);
                v.setProperty("source", "UPV");

                graph.commit();
                count++;
            }

            log.info("Added " + count + " OZ from file " + f.getAbsolutePath());
        }
        graph.shutdown();
    }
}


/*<tr>
<td>
<span style="font-weight: bold;" title="(Detail spisu)">
    <a href="javascript:odesli_det('3001','on');">55639</a>
</span>
</td>
<td>167123</td>
<td>21.04.1988</td>
<td>MEVACOR</td>
<td>Platný dokument</td>
<td>Merck Sharp & Dohme Corp., One Merck Drive, Whitehouse Station, NJ US</td>
<td>5</td>
<td>S</td>
<td>N</td>
</tr>

<tr>
             <th style="width: 10px;">&nbsp;</th>
             <th style="width: 10px;">&nbsp;</th>
             <th data-placeholder="", style="width: 5%;">Č. přihlášky</th>
             <th data-placeholder="", style="width: 5%;">Č. zápisu</th>
             <th data-placeholder="", style="width: 7%;">Priorita</th>
             <th data-placeholder="", style="width: 25%;">Znění</th>
             <th data-placeholder="", style="width: 10%;">Stav</th>
             <th data-placeholder="", style="width: 28%;">Majitel</th>
             <th data-placeholder="", style="width: 20%;">Třídy</th>
             <th data-placeholder="">D</th>
             <th data-placeholder="">SN</th>
         </tr>

         */