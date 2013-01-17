package pl.psnc.dl.wf4ever.model.ORE;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.vocabulary.ORE;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

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
     */
    public Proxy(UserMetadata user, URI uri) {
        super(user, uri);
    }


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     * @param dataset
     *            custom dataset
     * @param useTransactions
     *            should transactions be used. Note that not using transactions on a dataset which already uses
     *            transactions may make it unreadable.
     * @param uri
     *            proxy URI
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


    /**
     * Create and save a new proxy.
     * 
     * @param builder
     *            model instances builder
     * @param researchObject
     *            research object aggregating the proxy
     * @param resource
     *            resource for which the proxy is
     * @return a proxy instance
     */
    public static Proxy create(Builder builder, ResearchObject researchObject, AggregatedResource resource) {
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


    /**
     * Find the proxyFor resource URI in the proxy RDF description.
     * 
     * @param researchObject
     *            research object which will aggregate the proxy
     * @param content
     *            proxy description
     * @return URI of the proxied resource of null if not found
     * @throws BadRequestException
     *             if the description is not valid
     */
    public static URI assemble(ResearchObject researchObject, InputStream content)
            throws BadRequestException {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.read(content, researchObject.getUri().toString());
        ExtendedIterator<Individual> it = model.listIndividuals(ORE.Proxy);
        if (it.hasNext()) {
            NodeIterator it2 = it.next().listPropertyValues(ORE.proxyFor);
            if (it2.hasNext()) {
                RDFNode proxyForResource = it2.next();
                if (proxyForResource.isURIResource()) {
                    try {
                        return new URI(proxyForResource.asResource().getURI());
                    } catch (URISyntaxException e) {
                        throw new BadRequestException("Wrong target resource URI", e);
                    }
                } else {
                    throw new BadRequestException("The target is not an URI resource.");
                }
            } else {
                return null;
            }
        } else {
            throw new BadRequestException("The entity body does not define any ore:Proxy.");
        }
    }

}
