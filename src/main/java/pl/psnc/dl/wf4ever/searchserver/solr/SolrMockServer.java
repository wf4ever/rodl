package pl.psnc.dl.wf4ever.searchserver.solr;

import java.io.IOException;
import java.net.URI;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.searchserver.SearchServer;

import com.google.common.collect.Multimap;

/**
 * Mock for installations without active solr installation (eg tests).
 * 
 * @author pejot
 * 
 */
public class SolrMockServer implements SearchServer {

    @Override
    public void saveROAttributes(URI ro, Multimap<URI, Object> attributes) {
        // TODO Auto-generated method stub

    }


    @Override
    public void deleteROAttributes(URI ro) {
        // TODO Auto-generated method stub

    }


    @Override
    public void saveROAttributes(URI ro, Multimap<URI, Object> attributes, URI source) {
        // TODO Auto-generated method stub

    }


    @Override
    public void deleteROAttributes(URI ro, URI source) {
        // TODO Auto-generated method stub

    }


    @Override
    public String getConnectionUrl() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public QueryResponse query(String query)
            throws SolrServerException {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void clearIndex()
            throws SolrServerException, IOException {
        // TODO Auto-generated method stub

    }


    @Override
    public QueryResponse query(SolrQuery query)
            throws SolrServerException {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void saveRO(ResearchObject ro, Multimap<URI, Object> roAttributes) {
        // TODO Auto-generated method stub

    }

}
