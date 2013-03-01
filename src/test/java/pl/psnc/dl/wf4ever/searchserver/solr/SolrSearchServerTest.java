package pl.psnc.dl.wf4ever.searchserver.solr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;

import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.searchserver.SearchServer;

public class SolrSearchServerTest {

    private static final String CONNECTION_PROPERTIES = "connection.properties";


    @Test
    public void testGetSolrServerInstance()
            throws SolrServerException, IOException {
        SearchServer server = SolrSearchServer.get();
        Assert.assertNotNull(server);
    }


    @Test
    public void testConnectionProperties()
            throws IOException {
        SearchServer server = SolrSearchServer.get();
        InputStream is = Thing.class.getClassLoader().getResourceAsStream(CONNECTION_PROPERTIES);
        Properties props = new Properties();
        props.load(is);
        String connectionUrl = props.getProperty("solrServer");
        Assert.assertEquals(connectionUrl, server.getConnectionUrl());
    }
}
