package pl.psnc.dl.wf4ever.searchserver;

import java.io.IOException;
import java.net.URI;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.google.common.collect.Multimap;

/**
 * Search/index server interface.
 * 
 * @author pejot
 * 
 */
public interface SearchServer {

    /**
     * Saves a document with all RO attributes using the RO URI as id.
     * 
     * @param ro
     *            the RO that is described
     * @param attributes
     *            a set of attributes
     */
    void saveROAttributes(URI ro, Multimap<URI, Object> attributes);


    /**
     * Delete all attributes describing the RO.
     * 
     * @param ro
     *            the RO
     */
    void deleteROAttributes(URI ro);


    /**
     * Saves a document with all RO attributes using the source URI as id.
     * 
     * @param ro
     *            the RO that is described
     * @param attributes
     *            a set of attributes
     * @param source
     *            the resource that contains these attributes
     */
    void saveROAttributes(URI ro, Multimap<URI, Object> attributes, URI source);


    /**
     * Delete attributes describing the RO that were taken from a specific resource.
     * 
     * @param ro
     *            the RO
     * @param source
     *            the resource that contains the attributes that must be deleted
     */
    void deleteROAttributes(URI ro, URI source);


    /**
     * Get connection url.
     * 
     * @return the connection url
     */
    String getConnectionUrl();


    /**
     * The most general request query.
     * 
     * @param query
     *            query string
     * @return query response
     * @throws SolrServerException .
     */
    QueryResponse query(String query)
            throws SolrServerException;


    /**
     * Clear index.
     * 
     * @throws IOException .
     * @throws SolrServerException .
     */
    void clearIndex()
            throws SolrServerException, IOException;

}