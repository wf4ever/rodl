package pl.psnc.dl.wf4ever.model.ORE;

import java.net.URI;

import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

/**
 * Simple Aggregated Resource model.
 * 
 * @author pejot
 * 
 */
public class AggregatedResource extends Thing {

    /** ore:AggregationUri. */
    protected URI aggregationUri;

    /** URI of a proxy of this resource. */
    protected URI proxyUri;


    /**
     * Constructor.
     */
    public AggregatedResource() {

    }


    /**
     * Constructor.
     */
    public AggregatedResource(URI uri, URI agggregationUri) {
        super(uri);
        this.aggregationUri = agggregationUri;
    }


    /**
     * Store to disk.
     * 
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public void serialize()
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        //FIXME is this good?
        serialize(new ResearchObject(aggregationUri));
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


    public URI getProxyUri() {
        return proxyUri;
    }


    public void setProxyUri(URI proxyUri) {
        this.proxyUri = proxyUri;
    }
}
