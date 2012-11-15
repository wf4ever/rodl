package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;

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


    public URI getProxyUri() {
        return proxyUri;
    }


    public void setProxyUri(URI proxyUri) {
        this.proxyUri = proxyUri;
    }
}
