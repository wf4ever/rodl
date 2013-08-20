package pl.psnc.dl.wf4ever.integration.search;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import pl.psnc.dl.wf4ever.integration.AbstractIntegrationTest;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.searchserver.SearchServer;
import pl.psnc.dl.wf4ever.searchserver.solr.SolrSearchServer;

@Ignore
public class SearchServerTest extends AbstractIntegrationTest {

    protected String urlConnectionString;
    protected static final String CONNECTION_PROPERTIES_FILENAME = "connection.properties";
    protected String getAllQuery = "*:*";
    protected String cleaningString = "update?commit=true -d  '<delete><query>*:*</query></delete>'";


    @Override
    public void setUp()
            throws Exception {
        super.setUp();
        SolrSearchServer.get().clearIndex();
        try (InputStream is = Thing.class.getClassLoader().getResourceAsStream(CONNECTION_PROPERTIES_FILENAME)) {
            Properties props = new Properties();
            props.load(is);
            urlConnectionString = props.getProperty("solrServer");

        } catch (Exception e) {
            //do nth, it's just a test
        }
    }


    @Override
    public void tearDown()
            throws Exception {
        SolrSearchServer.get().clearIndex();
        super.tearDown();
    }


    @Test
    public void testUpdateIndexAttributes()
            throws IOException, SolrServerException {
        URI ro1uri = createRO();
        URI ro2uri = createRO();
        SearchServer searchServer = SolrSearchServer.get();
        QueryResponse response = searchServer.query("*:*");
        Assert.assertEquals(2, response.getResults().size());

    }


    @Test
    public void testSearchByAnnotation() {
        URI searchRO = createRO();
        InputStream is = getClass().getClassLoader().getResourceAsStream("resources/searchROAnnotationBody.ttl");
        URI annotationUri = addAnnotation(searchRO, is).getLocation();
    }


    @Test
    public void testDeleteIndexAttributes()
            throws SolrServerException {
        URI ro1uri = createRO();
        SearchServer searchServer = SolrSearchServer.get();
        QueryResponse response = searchServer.query("*:*");
        Assert.assertEquals(1, response.getResults().size());
        deleteROs();
        response = searchServer.query("*:*");
        Assert.assertEquals(0, response.getResults().size());
    }


    protected void deleteROs() {
        String list = webResource.path("ROs/").header("Authorization", "Bearer " + accessToken).get(String.class);
        if (!list.isEmpty()) {
            String[] ros = list.trim().split("\r\n");
            for (String ro : ros) {
                webResource.uri(URI.create(ro)).header("Authorization", "Bearer " + accessToken).delete();
            }
        }
    }

}
