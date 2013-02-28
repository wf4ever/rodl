package pl.psnc.dl.wf4ever.solr;

import java.io.IOException;
import java.util.UUID;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

public class Test {

    @org.junit.Test
    public void tset()
            throws SolrServerException, IOException {
        String url = "http://localhost:8983/solr";
        SolrServer server = new HttpSolrServer(url);
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("uri", UUID.randomUUID().toString());
        server.add(doc);
        server.commit();

    }
}
