package pl.psnc.dl.wf4ever.model.ORE;

import java.net.URI;
import java.util.UUID;

import pl.psnc.dl.wf4ever.common.Builder;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

import com.hp.hpl.jena.query.Dataset;

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
     */
    public Proxy(UserMetadata user, URI uri) {
        super(user, uri);
    }


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     */
    public Proxy(UserMetadata user, Dataset dataset, boolean useTransactions, URI uri) {
        super(user, dataset, useTransactions, uri);
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


    public static Proxy create(Builder builder, ResearchObject researchObject, AggregatedResource resource)
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        URI proxyUri = researchObject.getUri().resolve(".ro/proxies/" + UUID.randomUUID());
        Proxy proxy = builder.buildProxy(proxyUri, resource, researchObject);
        proxy.save();
        return proxy;
    }


    @Override
    public void save()
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        super.save();
        proxyIn.getResourceMap().saveProxy(this);
    }


    public static Proxy get(Builder builder, URI pUri, AggregatedResource proxyFor, Aggregation proxyIn) {
        Proxy proxy = builder.buildProxy(pUri, proxyFor, proxyIn);
        return proxy;
    }

}
