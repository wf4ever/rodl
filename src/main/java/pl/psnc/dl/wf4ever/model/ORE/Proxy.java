package pl.psnc.dl.wf4ever.model.ORE;

import java.net.URI;
import java.util.UUID;

import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

/**
 * Represents an ore:Proxy.
 * 
 * @author piotrekhol
 * 
 */
public class Proxy extends Thing {

    /** Aggregated resource. */
    protected AggregatedResource proxyFor;

    /** Aggregating resource. */
    protected Aggregation proxyIn;


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
    public Proxy(UserMetadata user, URI uri, AggregatedResource proxyFor, Aggregation proxyIn) {
        super(user, uri);
        this.proxyFor = proxyFor;
        this.proxyIn = proxyIn;
    }


    public Aggregation getProxyIn() {
        return proxyIn;
    }


    public void setProxyIn(Aggregation proxyIn) {
        this.proxyIn = proxyIn;
    }


    public AggregatedResource getProxyFor() {
        return proxyFor;
    }


    public void setProxyFor(AggregatedResource proxyFor) {
        this.proxyFor = proxyFor;
    }


    public static Proxy create(UserMetadata user, ResearchObject researchObject, AggregatedResource resource)
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        URI proxyUri = researchObject.getUri().resolve(".ro/proxies/" + UUID.randomUUID());
        Proxy proxy = new Proxy(user, proxyUri, resource, researchObject);
        proxy.save();
        return proxy;
    }


    @Override
    public void save()
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        super.save();
        proxyIn.getResourceMap().saveProxy(this);
    }

}
