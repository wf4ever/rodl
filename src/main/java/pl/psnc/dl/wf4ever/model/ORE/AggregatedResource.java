package pl.psnc.dl.wf4ever.model.ORE;

import java.net.URI;

import pl.psnc.dl.wf4ever.model.RDF.Thing;

/**
 * Simple Aggregated Resource model.
 * 
 * @author pejot
 * 
 */
public class AggregatedResource extends Thing {

    /** ore:AggregationUri. */
    protected URI aggregationUri;


    /**
     * Constructor.
     */
    public AggregatedResource() {
        super();
    }


    /**
     * Constructor.
     * 
     * @param uri
     *            URI
     */
    public AggregatedResource(URI uri) {
        super(uri);
    }


    public URI getAggregationUri() {
        return aggregationUri;
    }


    public void setAggregationUri(URI aggregationUri) {
        this.aggregationUri = aggregationUri;
    }

}
