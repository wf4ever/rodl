package pl.psnc.dl.wf4ever.model.ORE;

import java.io.InputStream;
import java.net.URI;

import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.IncorrectModelException;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.ResearchObjectComponent;
import pl.psnc.dl.wf4ever.vocabulary.ORE;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;

/**
 * ore:ResourceMap, including ro:Manifest.
 * 
 * @author piotrekhol
 * 
 */
public abstract class ResourceMap extends Thing implements ResearchObjectComponent {

    /** ore:Aggregation described by this resource map. */
    protected Aggregation aggregation;

    /** physical representation metadata. */
    private ResourceMetadata stats;


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     * @param uri
     *            resource map URI
     */
    public ResourceMap(UserMetadata user, URI uri) {
        super(user, uri);
    }


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     * @param aggregation
     *            aggregation described by the resource map
     * @param uri
     *            resource URI
     */
    public ResourceMap(UserMetadata user, Aggregation aggregation, URI uri) {
        super(user, uri);
        this.aggregation = aggregation;
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
     * @param aggregation
     *            aggregation described by the resource map
     * @param uri
     *            resource URI
     */
    public ResourceMap(UserMetadata user, Dataset dataset, boolean useTransactions, Aggregation aggregation, URI uri) {
        super(user, dataset, useTransactions, uri);
        this.aggregation = aggregation;
    }


    @Override
    public void save()
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        super.save();
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            Individual aggregationInd = model.createIndividual(aggregation.getUri().toString(), ORE.Aggregation);
            Individual resourceMapInd = model.createIndividual(uri.toString(), ORE.ResourceMap);
            model.add(aggregationInd, ORE.isDescribedBy, resourceMapInd);
            model.add(resourceMapInd, ORE.describes, aggregationInd);

            saveAuthor((Thing) aggregation);
            saveAuthor(this);
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }


    /**
     * Delete the resource map from the triple store and the storage.
     */
    @Override
    public void delete() {
        DigitalLibraryFactory.getDigitalLibrary().deleteFile(getResearchObject().getUri(), getPath());
        super.delete();
    }


    /**
     * Add a new aggregated resource and save it.
     * 
     * @param resource
     *            a new aggregated resource
     */
    public void saveAggregatedResource(AggregatedResource resource) {
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            Individual ro = model.getIndividual(aggregation.getUri().toString());
            if (ro == null) {
                throw new IncorrectModelException("Aggregation not found: " + aggregation.getUri());
            }
            Individual resourceR = model.createIndividual(resource.getUri().toString(), ORE.AggregatedResource);
            model.add(ro, ORE.aggregates, resourceR);
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }


    /**
     * Add a new proxy and save it.
     * 
     * @param proxy
     *            a new proxy
     */
    public void saveProxy(Proxy proxy) {
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            com.hp.hpl.jena.rdf.model.Resource aggregationR = model.createResource(aggregation.getUri().toString());
            com.hp.hpl.jena.rdf.model.Resource resourceR = model.createResource(proxy.getProxyFor().getUri()
                    .normalize().toString());
            Individual proxyR = model.createIndividual(proxy.getUri().toString(), ORE.Proxy);
            model.add(proxyR, ORE.proxyIn, aggregationR);
            model.add(proxyR, ORE.proxyFor, resourceR);
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
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
        serialize(getResearchObject().getUri());
    }


    public String getPath() {
        return getResearchObject().getUri().relativize(uri).getPath();
    }


    @Override
    public ResourceMetadata getStats() {
        if (stats == null) {
            stats = DigitalLibraryFactory.getDigitalLibrary().getFileInfo(getResearchObject().getUri(), getPath());
        }
        return stats;
    }


    public void setStats(ResourceMetadata stats) {
        this.stats = stats;
    }


    @Override
    public InputStream getSerialization() {
        return DigitalLibraryFactory.getDigitalLibrary().getFileContents(getResearchObject().getUri(), getPath());
    }


    @Override
    public boolean isInternal() {
        return true;
    }

}
