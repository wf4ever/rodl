package pl.psnc.dl.wf4ever.model.ORE;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;
import pl.psnc.dl.wf4ever.vocabulary.ORE;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Simple Aggregated Resource model.
 * 
 * @author pejot
 * 
 */
public class AggregatedResource extends Thing {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(AggregatedResource.class);

    /** RO it is aggregated in. */
    protected ResearchObject researchObject;

    /** Proxy of this resource. */
    protected Proxy proxy;

    /** physical representation metadata. */
    private ResourceMetadata stats;


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     * @param uri
     *            resource URI
     * @param researchObject
     *            The RO it is aggregated by
     */
    public AggregatedResource(UserMetadata user, Dataset dataset, boolean useTransactions,
            ResearchObject researchObject, URI uri) {
        super(user, dataset, useTransactions, uri);
        this.researchObject = researchObject;
    }


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     * @param uri
     *            resource URI
     * @param researchObject
     *            The RO it is aggregated by
     */
    public AggregatedResource(UserMetadata user, ResearchObject researchObject, URI uri) {
        super(user, uri);
        this.researchObject = researchObject;
    }


    @Override
    public void save()
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        super.save();
        researchObject.getManifest().saveAggregation(this);
        researchObject.getManifest().saveAuthor(this);

        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            Individual resource = model.createIndividual(uri.toString(), ORE.AggregatedResource);
            Resource aggregation = model.createResource(researchObject.getUri().toString());
            resource.addProperty(ORE.isAggregatedBy, aggregation);
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
        serialize(researchObject.getUri());
    }


    public Proxy getProxy() {
        return proxy;
    }


    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }


    public ResearchObject getResearchObject() {
        return researchObject;
    }


    public ResourceMetadata getStats() {
        if (stats == null) {
            stats = ROSRService.DL.get().getFileInfo(getResearchObject().getUri(), getPath());
        }
        return stats;
    }


    public void setStats(ResourceMetadata stats) {
        this.stats = stats;
    }


    /**
     * Set the aggregating RO.
     * 
     * @param researchObject
     *            research object
     */
    public void setResearchObject(ResearchObject researchObject) {
        this.researchObject = researchObject;
        if (this.proxy != null) {
            this.proxy.setProxyIn(researchObject);
        }
    }


    public String getPath() {
        return getResearchObject().getUri().relativize(uri).getPath();
    }


    /**
     * Check if the resource is internal. Resource is internal only if its content has been deployed under the control
     * of the service. A resource that has "internal" URI but the content has not been uploaded is considered external.
     * 
     * @return true if the resource content is deployed under the control of the service, false otherwise
     */
    public boolean isInternal() {
        String path = getPath();
        return !path.isEmpty() && ROSRService.DL.get().fileExists(researchObject.getUri(), path);
    }


    /**
     * An existing aggregated resource is being used as an annotation body now. Add it to the triplestore. If the
     * aggregated resource is external, do nothing.
     * 
     * @throws BadRequestException
     *             if there is no data in storage or the file format is not RDF
     */
    public void saveGraph()
            throws BadRequestException {
        String filePath = getPath();
        RDFFormat format = RDFFormat.forMIMEType(getStats().getMimeType());
        if (format == null) {
            throw new BadRequestException("Unrecognized RDF format: " + filePath);
        }
        try (InputStream data = ROSRService.DL.get().getFileContents(researchObject.getUri(), filePath)) {
            if (data == null) {
                throw new BadRequestException("No data for resource: " + uri);
            }
            boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
            try {
                model.removeAll();
                model.read(data, uri.resolve(".").toString(), format.getName().toUpperCase());
                commitTransaction(transactionStarted);
            } finally {
                endTransaction(transactionStarted);
            }
        } catch (IOException e) {
            LOGGER.warn("Could not close stream", e);
        }
        serialize();
        setNamedGraph(true);
    }


    public void deleteGraph() {
        String filePath = getPath();
        RDFFormat format = RDFFormat.forMIMEType(getStats().getMimeType());
        try (InputStream data = getGraphAsInputStream(RDFFormat.RDFXML)) {
            ROSRService.DL.get().createOrUpdateFile(researchObject.getUri(), filePath, data,
                format.getDefaultMIMEType());
        } catch (IOException e) {
            LOGGER.warn("Could not close stream", e);
        }
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            model.removeAll();
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
        serialize();
        setNamedGraph(false);
    }
}
