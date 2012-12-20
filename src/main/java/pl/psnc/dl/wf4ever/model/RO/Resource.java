package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;

import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;

/**
 * ro:Resource.
 * 
 * @author piotrekhol
 * @author pejot
 */
public class Resource extends AggregatedResource {

    /** URI of a proxy of this resource. */
    protected URI proxyUri;

    /** physical representation metadata. */
    private ResourceMetadata stats;


    /**
     * Constructor.
     */
    public Resource() {
        super();
    }


    /**
     * Constructor.
     * 
     * @param uri
     *            URI
     */
    public Resource(URI uri) {
        super(uri);
    }


    /**
     * Constructor.
     * 
     * @param uri
     *            URI
     * @param stats
     *            physical representation metadata
     */
    public Resource(URI uri, ResourceMetadata stats) {
        this(uri);
        this.stats = stats;
    }


    public URI getProxyUri() {
        return proxyUri;
    }


    public void setProxyUri(URI proxyUri) {
        this.proxyUri = proxyUri;
    }


    public ResourceMetadata getStats() {
        return stats;
    }


    public void setStats(ResourceMetadata stats) {
        this.stats = stats;
    }
}
