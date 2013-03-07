package pl.psnc.dl.wf4ever.searchserver.solr;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;

/**
 * Builder for solr/Lucene query.
 * 
 * @author pejot
 * 
 */
public class QueryBuilder {

    /** dynamically added rdf property field prefix. */
    private static final String RDF_PROPERTY_PREFIX = "property_";
    /** query paramters. */
    private Map<String, String> queryParams;
    /** offset. */
    private Integer offset;
    /** query limit. */
    private Integer limit;
    /** sort field. */
    private String sortField;
    /** order. */
    private ORDER order;


    /**
     * Constructor.
     */
    public QueryBuilder() {
        queryParams = new HashMap<>();
    }


    /**
     * Add a new pair param:value to solr query.
     * 
     * @param param
     *            param
     * @param value
     *            value
     */
    public void addProperty(String param, String value) {
        queryParams.put(param, value);
    }


    /**
     * Add a new pair param:value to solr query with respect to the prefix that is added to dynamically indexed rdf
     * property.
     * 
     * @param param
     *            param
     * @param value
     *            value
     */
    public void addRDFProperty(String param, String value) {
        queryParams.put(RDF_PROPERTY_PREFIX + param, value);
    }


    /**
     * Set query limit.
     * 
     * @param limit
     *            query limit
     */
    public void setLimit(Integer limit) {
        this.limit = limit;
    }


    /**
     * Set query offset.
     * 
     * @param offset
     *            query offset.
     */
    public void setOffset(Integer offset) {
        this.offset = offset;
    }


    /**
     * Set sort field.
     * 
     * @param field
     *            sort field
     * @param order
     *            order
     */
    public void setSortField(String field, ORDER order) {
        this.sortField = field;
        this.order = order;
    }


    /**
     * Build query.
     * 
     * @return solr query
     */
    public SolrQuery build() {
        return build(false);
    }


    /**
     * Build query.
     * 
     * @param raw
     *            format true/false
     * 
     * @return solr query
     */
    public SolrQuery build(Boolean raw) {
        SolrQuery solrQuery = new SolrQuery();
        for (String key : queryParams.keySet()) {
            if (raw) {
                solrQuery.add(key, (queryParams.get(key)));
            } else {
                solrQuery.add(key, escapeQueryString(queryParams.get(key)));
            }
        }
        if (offset != null) {
            solrQuery.setStart(offset);
        }
        if (limit != null) {
            solrQuery.setRows(limit);
        }
        if (sortField != null && order != null) {
            setSortField(sortField, order);
        }
        return solrQuery;
    }


    /**
     * Get the name of the dynamic rdf property field.
     * 
     * @param rdfString
     *            rdf property string
     * @return rdf property field name
     */
    public static String getRDFPropertyFieldName(String rdfString) {
        return RDF_PROPERTY_PREFIX + rdfString;
    }


    /**
     * Escape solr special character.
     * 
     * @param queryString
     *            queryString
     * @return escaped string
     */
    private String escapeQueryString(String queryString) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < queryString.length(); i++) {
            char c = queryString.charAt(i);
            if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '(' || c == ')' || c == ':' || c == '^'
                    || c == '[' || c == ']' || c == '\"' || c == '{' || c == '}' || c == '~' || c == '*' || c == '?'
                    || c == '|' || c == '&' || c == ';') {
                sb.append('\\');
            }
            if (Character.isWhitespace(c)) {
                sb.append(" \\ ");
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
