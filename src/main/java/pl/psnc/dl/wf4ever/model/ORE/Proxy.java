package pl.psnc.dl.wf4ever.model.ORE;

import java.net.URI;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.RDF.Thing;

/**
 * Represents an ore:Proxy.
 * 
 * @author piotrekhol
 * 
 */
public class Proxy extends Thing {

    /** Aggregated resource. */
    protected AggregatedResource proxyFor;

    /** URI of aggregating resource. */
    protected URI proxyIn;


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     */
    public Proxy(UserMetadata user) {
        super(user);
    }


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     * @param uri
     *            proxy URI
     * @param proxyFor
     *            URI of aggregated resource
     * @param proxyIn
     *            URI of aggregating resource
     */
    public Proxy(UserMetadata user, URI uri, AggregatedResource proxyFor, URI proxyIn) {
        super(user, uri);
        this.proxyFor = proxyFor;
        this.proxyIn = proxyIn;
    }


    public URI getProxyIn() {
        return proxyIn;
    }


    public void setProxyIn(URI proxyIn) {
        this.proxyIn = proxyIn;
    }


    public AggregatedResource getProxyFor() {
        return proxyFor;
    }


    public void setProxyFor(AggregatedResource proxyFor) {
        this.proxyFor = proxyFor;
    }

}
