package pl.psnc.dl.wf4ever.model.ORE;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.EvoBuilder;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.FolderEntry;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.model.RO.ResearchObjectComponent;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Simple Aggregated Resource model.
 * 
 * @author pejot
 * 
 */
public class AggregatedResource extends Thing implements ResearchObjectComponent {

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
     * @param dataset
     *            custom dataset
     * @param useTransactions
     *            should transactions be used. Note that not using transactions on a dataset which already uses
     *            transactions may make it unreadable.
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


    /**
     * Create a new resource with all data except for the URI equal to another resource.
     * 
     * @param builder
     *            model instance builder
     * @param evoBuilder
     *            builder of evolution properties
     * @param researchObject
     *            research object that aggregates the resource
     * @return the new resource
     * @throws BadRequestException
     *             if it is expected to be an RDF file and isn't
     */
    public AggregatedResource copy(Builder builder, EvoBuilder evoBuilder, ResearchObject researchObject)
            throws BadRequestException {
        URI resourceUri = researchObject.getUri().resolve(getPath());
        if (researchObject.isUriUsed(resourceUri)) {
            throw new ConflictException("Resource already exists: " + resourceUri);
        }
        AggregatedResource resource2 = builder.buildAggregatedResource(resourceUri, researchObject, getCreator(),
            getCreated());
        evoBuilder.setFrozenAt(resource2, DateTime.now());
        evoBuilder.setFrozenBy(resource2, builder.getUser());
        resource2.setProxy(Proxy.create(builder, researchObject, resource2));
        if (isInternal()) {
            resource2.save(getSerialization(), getStats().getMimeType());
        } else {
            resource2.save();
        }
        return resource2;
    }


    @Override
    public void save()
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        super.save();
        researchObject.getManifest().saveAggregatedResource(this);
        researchObject.getManifest().saveAuthor(this);
    }


    /**
     * Delete itself, the proxy, if exists, and folder entries.
     */
    @Override
    public void delete() {
        getResearchObject().getManifest().deleteResource(this);
        getResearchObject().getManifest().serialize();
        getResearchObject().getAggregatedResources().remove(uri);
        if (isInternal()) {
            // resource may no longer be internal if it is deleted by different classes independently, 
            // or if it's a folder that has just been emptied (i.e. the resource map was deleted), 
            // in which case it was also deleted automatically
            DigitalLibraryFactory.getDigitalLibrary().deleteFile(getResearchObject().getUri(), getPath());
        }
        if (getProxy() != null) {
            getProxy().delete();
        }
        //create another collection to avoid concurrent modification
        Set<FolderEntry> entriesToDelete = new HashSet<>(getResearchObject().getFolderEntriesByResourceUri().get(uri));
        for (FolderEntry entry : entriesToDelete) {
            entry.delete();
        }
        super.delete();
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
        serialize(researchObject.getUri(), RDFFormat.forMIMEType(getStats().getMimeType()));
        stats = null;
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
    @Override
    public boolean isInternal() {
        String path = getPath();
        return !path.isEmpty()
                && DigitalLibraryFactory.getDigitalLibrary().fileExists(getResearchObject().getUri(), path);
    }


    /**
     * An existing aggregated resource is being used as an annotation body now.
     * 
     * @throws BadRequestException
     *             if there is no data in storage or the file format is not RDF
     */
    public void saveGraphAndSerialize()
            throws BadRequestException {
        String filePath = getPath();
        RDFFormat format;
        if (getStats() != null && getStats().getMimeType() != null) {
            format = RDFFormat.forMIMEType(getStats().getMimeType());
        } else {
            // required for resource not stored in RODL, for example new zipped ROs
            // FIXME such resources should have the MIME type set anyway
            format = RDFFormat.forFileName(getPath());
        }
        if (format == null) {
            throw new BadRequestException("Unrecognized RDF format: " + filePath);
        }
        try (InputStream data = DigitalLibraryFactory.getDigitalLibrary().getFileContents(researchObject.getUri(),
            filePath)) {
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
        researchObject.updateIndexAttributes();
    }


    /**
     * Update the serialization using absolute URIs and delete the named graph with that resource.
     */
    public void deleteGraphAndSerialize() {
        String filePath = getPath();
        try (InputStream data = getGraphAsInputStream(RDFFormat.RDFXML)) {
            // can only be null if a resource that is an annotation body exists serialized but not in the triple store
            if (data != null) {
                DigitalLibraryFactory.getDigitalLibrary().createOrUpdateFile(researchObject.getUri(), filePath, data,
                    getStats().getMimeType());
            }
        } catch (IOException e) {
            LOGGER.warn("Could not close stream", e);
        }
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            dataset.removeNamedModel(uri.toString());
            commitTransaction(transactionStarted);
        } finally {
            endTransaction(transactionStarted);
        }
    }


    @Override
    public InputStream getSerialization() {
        return DigitalLibraryFactory.getDigitalLibrary().getFileContents(researchObject.getUri(), getPath());
    }


    /**
     * Save the resource and its content.
     * 
     * @param content
     *            the resource content
     * @param contentType
     *            the content MIME type
     * @throws BadRequestException
     *             if it is expected to be an RDF file and isn't
     */
    public void save(InputStream content, String contentType)
            throws BadRequestException {
        String path = researchObject.getUri().relativize(uri).getPath();
        setStats(DigitalLibraryFactory.getDigitalLibrary().createOrUpdateFile(researchObject.getUri(), path, content,
            contentType != null ? contentType : "text/plain"));
        if (isNamedGraph()) {
            saveGraphAndSerialize();
        }
        save();
    }


    /**
     * Update the file contents.
     * 
     * @param content
     *            the resource content
     * @param contentType
     *            the content MIME type
     * @throws BadRequestException
     *             if it is expected to be an RDF file and isn't
     */
    public void update(InputStream content, String contentType)
            throws BadRequestException {
        save(content, contentType);
    }


    /**
     * Find all references in the resource to objects related to the provided research object and update them to
     * reference matching objects of this research object. Matching is done by path relative to RO URI. Related objects
     * are all aggregated resources, the manifest and the RO itself.
     * 
     * Example: if the provided RO has URI "/ro1/", it aggregates "/ro1/x" and this RO "/ro2/" aggregates "/ro2/x", then
     * all references to "/ro1/x" will be replaced with "/ro2/x".
     * 
     * If this resource has no RDF representation in the triplestore, this method does nothing.
     * 
     * Warning: note that if that method is called before aggregating all resources in this RO, then references to the
     * yet unaggregated resources will remain unchanged.
     * 
     * @param researchObject2
     *            research object whose aggregated resources should not be referenced in this resource
     * @return number of triples updated
     */
    public int updateReferences(ResearchObject researchObject2) {
        boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
        try {
            if (model == null) {
                return 0;
            }
            int changes = 0;
            Map<URI, URI> uris = new HashMap<>();
            uris.put(researchObject2.getUri(), getResearchObject().getUri());
            uris.put(researchObject2.getManifest().getUri(), getResearchObject().getManifest().getUri());
            for (AggregatedResource r1 : researchObject2.getAggregatedResources().values()) {
                URI r2Uri = getResearchObject().getUri().resolve(r1.getPath());
                if (getResearchObject().getAggregatedResources().containsKey(r2Uri)) {
                    uris.put(r1.getUri(), r2Uri);
                }
            }
            for (Entry<URI, URI> e : uris.entrySet()) {
                Resource oldResource = model.createResource(e.getKey().toString());
                Resource newResource = model.createResource(e.getValue().toString());

                List<Statement> s1 = model.listStatements(oldResource, null, (RDFNode) null).toList();
                for (Statement s : s1) {
                    model.remove(s.getSubject(), s.getPredicate(), s.getObject());
                    model.add(newResource, s.getPredicate(), s.getObject());
                }
                List<Statement> s2 = model.listStatements(null, null, oldResource).toList();
                for (Statement s : s2) {
                    model.remove(s.getSubject(), s.getPredicate(), s.getObject());
                    model.add(s.getSubject(), s.getPredicate(), newResource);
                }
                changes += s1.size() + s2.size();
            }
            commitTransaction(transactionStarted);
            return changes;
        } finally {
            endTransaction(transactionStarted);
        }
    }
}
