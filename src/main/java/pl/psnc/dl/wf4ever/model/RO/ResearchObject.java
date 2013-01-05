/**
 * 
 */
package pl.psnc.dl.wf4ever.model.RO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.common.Builder;
import pl.psnc.dl.wf4ever.common.EvoType;
import pl.psnc.dl.wf4ever.common.util.MemoryZipFile;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.exceptions.IncorrectModelException;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.ORE.Aggregation;
import pl.psnc.dl.wf4ever.model.ORE.Proxy;
import pl.psnc.dl.wf4ever.model.ORE.ResourceMap;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceTdb;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.query.Dataset;

/**
 * A research object, live by default.
 * 
 * @author piotrekhol
 * 
 */
public class ResearchObject extends Thing implements Aggregation {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(ResearchObject.class);

    /** Manifest path. */
    public static final String MANIFEST_PATH = ".ro/manifest.rdf";

    /** Fixed roevo annotation file path. */
    private static final String ROEVO_PATH = ".ro/evo_info.ttl";

    /** has the metadata been loaded from triplestore. */
    protected boolean loaded;

    /** aggregated resources, including annotations, resources and folders. */
    protected Map<URI, AggregatedResource> aggregatedResources;

    /** proxies declared in this RO. */
    private Map<URI, Proxy> proxies;

    /** aggregated ro:Resources, excluding ro:Folders. */
    private Map<URI, Resource> resources;

    /** aggregated ro:Folders. */
    private Map<URI, Folder> folders;

    /** aggregated annotations, grouped based on ao:annotatesResource. */
    private Multimap<URI, Annotation> annotationsByTargetUri;

    /** aggregated annotations. */
    private Map<URI, Annotation> annotations;

    /** folder resource maps. */
    private Map<URI, ResourceMap> folderResourceMaps;

    /** Manifest. */
    private Manifest manifest;


    //TODO add properties stored in evo_info.ttl

    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     * @param dataset
     *            RO URI
     */
    //FIXME should this be private?
    public ResearchObject(UserMetadata user, Dataset dataset, boolean useTransactions, URI uri) {
        super(user, dataset, useTransactions, uri);
    }


    public ResearchObject(UserMetadata user, URI uri) {
        super(user, uri);
    }


    /**
     * Create new Research Object.
     * 
     * @param user
     *            user creating the instance
     * @param uri
     *            RO URI
     * @return an instance
     * @throws AccessDeniedException
     * @throws NotFoundException
     * @throws DigitalLibraryException
     * @throws ConflictException
     */
    public static ResearchObject create(Builder builder, URI uri)
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        if (get(builder, uri) != null) {
            throw new ConflictException("Research Object already exists: " + uri);
        }
        ResearchObject researchObject = builder.buildResearchObject(uri, builder.getUser().getUri(), DateTime.now());
        researchObject.manifest = Manifest.create(builder, researchObject.getUri().resolve(MANIFEST_PATH),
            researchObject);
        researchObject.save();
        return researchObject;
    }


    public void generateEvoInfo()
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        ROSRService.SMS.get().generateEvoInformation(this, null, EvoType.LIVE);
        if (!loaded) {
            load();
        }
        this.getEvoInfoBody().serialize();
        this.getManifest().serialize();
    }


    /**
     * Get a resource with a given URI or null if doesn't exist.
     * 
     * @param resourceUri
     *            resource URI
     * @return resource instance or null
     */
    public Resource getResource(URI resourceUri) {
        return resources.get(resourceUri);
    }


    public AggregatedResource getEvoInfoBody() {
        if (!loaded) {
            load();
        }
        //HACK this should be added automatically
        this.aggregatedResources.put(getFixedEvolutionAnnotationBodyUri(), new AggregatedResource(user, this,
                getFixedEvolutionAnnotationBodyUri()));
        return aggregatedResources.get(getFixedEvolutionAnnotationBodyUri());
    }


    public Manifest getManifest() {
        if (!loaded) {
            load();
        }
        return manifest;
    }


    /**
     * Get a Research Object.
     * 
     * @param user
     *            user creating the instance
     * @param uri
     *            uri
     * @return an existing Research Object or null
     */
    public static ResearchObject get(Builder builder, URI uri) {
        if (ROSRService.SMS.get() == null
                || ROSRService.SMS.get().containsNamedGraph(uri.resolve(ResearchObject.MANIFEST_PATH))) {
            return builder.buildResearchObject(uri);
        } else {
            return null;
        }
    }


    /**
     * Delete the Research Object including its resources and annotations.
     */
    public void delete() {
        try {
            ROSRService.DL.get().deleteResearchObject(uri);
        } finally {
            try {
                ROSRService.SMS.get().removeResearchObject(this);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("URI not found in SMS: " + uri);
            }
        }
        loaded = false;
    }


    public void save() {
        super.save();
        getManifest().save();

        ROSRService.DL.get().createResearchObject(uri, getManifest().getGraphAsInputStream(RDFFormat.RDFXML),
            ResearchObject.MANIFEST_PATH, RDFFormat.RDFXML.getDefaultMIMEType());
        generateEvoInfo();
    }


    /**
     * Load the metadata from the triplestore.
     * 
     * @return this instance, loaded
     */
    //TODO should it be overridden?
    public ResearchObject load() {
        //FIXME here we could first load the manifest URI from the RO named graph
        this.manifest = Manifest.get(builder, getManifestUri(), this);
        this.creator = manifest.extractCreator(this);
        this.created = manifest.extractCreated(this);
        this.resources = manifest.extractResources();
        this.folders = manifest.extractFolders();
        this.folderResourceMaps = new HashMap<>();
        for (Folder folder : folders.values()) {
            folderResourceMaps.put(folder.getResourceMap().getUri(), folder.getResourceMap());
        }
        this.annotations = manifest.extractAnnotations();
        this.annotationsByTargetUri = HashMultimap.<URI, Annotation> create();
        for (Annotation ann : annotations.values()) {
            for (Thing target : ann.getAnnotated()) {
                this.annotationsByTargetUri.put(target.getUri(), ann);
            }
        }
        this.aggregatedResources = manifest.extractAggregatedResources(resources, folders, annotations);
        this.proxies = new HashMap<>();
        for (AggregatedResource aggregatedResource : this.aggregatedResources.values()) {
            Proxy proxy = aggregatedResource.getProxy();
            if (proxy != null) {
                this.proxies.put(proxy.getUri(), proxy);
            }
        }
        this.loaded = true;
        return this;
    }


    /**
     * Add an internal resource to the research object.
     * 
     * @param path
     *            resource path, relative to the RO URI, not encoded
     * @param content
     *            resource content
     * @param contentType
     *            resource Content Type
     * @return the resource instance
     */
    public Resource aggregate(String path, InputStream content, String contentType) {
        URI resourceUri = UriBuilder.fromUri(uri).path(path).build();
        if (!loaded) {
            load();
        }
        Resource resource = Resource.create(builder, this, resourceUri, content, contentType);
        getManifest().serialize();
        this.resources.put(resource.getUri(), resource);
        this.aggregatedResources.put(resource.getUri(), resource);
        this.proxies.put(resource.getProxy().getUri(), resource.getProxy());
        return resource;
    }


    /**
     * Add an external resource (a reference to a resource) to the research object.
     * 
     * @param uri
     *            resource URI
     * @return the resource instance
     */
    public Resource aggregate(URI uri) {
        Resource resource = ROSRService.SMS.get().addResource(this, uri, null);
        resource.setProxy(ROSRService.SMS.get().addProxy(this, resource));
        // update the manifest that describes the resource in dLibra
        if (!loaded) {
            load();
        }
        this.getManifest().serialize();
        this.resources.put(resource.getUri(), resource);
        this.aggregatedResources.put(resource.getUri(), resource);
        return resource;
    }


    public Annotation annotate(Thing body, Set<Thing> targets) {
        return annotate(body, targets, null);
    }


    /**
     * Add and aggregate a new annotation to the research object.
     * 
     * @param body
     *            annotation body URI
     * @param targets
     *            list of annotated resources URIs
     * @param annotationId
     *            the id of the annotation, may be null
     * @return new annotation
     */
    public Annotation annotate(Thing body, Set<Thing> targets, String annotationId) {
        Annotation annotation = ROSRService.SMS.get().addAnnotation(this, targets, body, annotationId);
        annotation.setProxy(ROSRService.SMS.get().addProxy(this, annotation));
        getManifest().serialize();
        if (!loaded) {
            load();
        }
        this.annotations.put(annotation.getUri(), annotation);
        for (Thing target : annotation.getAnnotated()) {
            this.annotationsByTargetUri.put(target.getUri(), annotation);
        }
        this.aggregatedResources.put(annotation.getUri(), annotation);
        return annotation;
    }


    /**
     * Create a new research object submitted in ZIP format.
     * 
     * @param researchObjectUri
     *            the new research object
     * @param zip
     *            the ZIP file
     * @return HTTP response (created in case of success, 404 in case of error)
     * @throws IOException
     *             error creating the temporary file
     * @throws BadRequestException
     */
    public static ResearchObject create(Builder builder, URI researchObjectUri, MemoryZipFile zip)
            throws IOException, BadRequestException {
        ResearchObject researchObject = create(builder, researchObjectUri);
        researchObject.load();
        SemanticMetadataService tmpSms = new SemanticMetadataServiceTdb(ROSRService.SMS.get().getUserProfile(),
                researchObject, zip.getManifestAsInputStream(), RDFFormat.RDFXML);

        List<AggregatedResource> aggregatedList;
        List<Annotation> annotationsList;

        try {
            aggregatedList = tmpSms.getAggregatedResources(researchObject);
            annotationsList = tmpSms.getAnnotations(researchObject);
            aggregatedList = tmpSms.removeSpecialFilesFromAggergated(aggregatedList);
            annotationsList = tmpSms.removeSpecialFilesFromAnnotatios(annotationsList);
        } catch (IncorrectModelException e) {
            throw new BadRequestException(e.getMessage(), e);
        }

        InputStream mimeTypesIs = ROSRService.class.getClassLoader().getResourceAsStream("mime.types");
        MimetypesFileTypeMap mfm = new MimetypesFileTypeMap(mimeTypesIs);
        mimeTypesIs.close();
        for (AggregatedResource aggregated : aggregatedList) {
            String originalResourceName = researchObject.getUri().relativize(aggregated.getUri()).getPath();
            URI resourceURI = UriBuilder.fromUri(researchObject.getUri()).path(originalResourceName).build();
            UUID uuid = UUID.randomUUID();
            File tmpFile = File.createTempFile("tmp_resource", uuid.toString());
            try {
                if (zip.containsEntry(originalResourceName)) {
                    try (InputStream is = zip.getEntryAsStream(originalResourceName)) {
                        FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
                        IOUtils.copy(is, fileOutputStream);
                        String mimeType = mfm.getContentType(resourceURI.getPath());
                        researchObject.aggregate(originalResourceName, new FileInputStream(tmpFile), mimeType);
                    }
                } else {
                    researchObject.aggregate(aggregated.getUri());
                }
            } catch (AccessDeniedException | DigitalLibraryException | NotFoundException | IncorrectModelException e) {
                LOGGER.error("Error when aggregating resources", e);
            } finally {
                tmpFile.delete();
            }
        }
        for (Annotation annotation : annotationsList) {
            try {
                if (researchObject.getAggregatedResources().containsKey(annotation.getBody())) {
                    ROSRService.convertRoResourceToAnnotationBody(researchObject, researchObject
                            .getAggregatedResources().get(annotation.getBody()));
                }
                researchObject.annotate(annotation.getBody(), annotation.getAnnotated());
            } catch (DigitalLibraryException | NotFoundException e) {
                LOGGER.error("Error when adding annotations", e);
            }
        }

        tmpSms.close();
        return researchObject;
    }


    /**
     * Get the manifest URI.
     * 
     * @return manifest URI
     */
    public URI getManifestUri() {
        return uri != null ? uri.resolve(MANIFEST_PATH) : null;
    }


    /**
     * Get the roevo annotation body URI.
     * 
     * @return roevo annotation body URI
     */
    public URI getFixedEvolutionAnnotationBodyUri() {
        return getUri().resolve(ROEVO_PATH);
    }


    public boolean isLoaded() {
        return loaded;
    }


    public Map<URI, Resource> getResources() {
        if (!loaded) {
            load();
        }
        return resources;
    }


    public Map<URI, Folder> getFolders() {
        if (!loaded) {
            load();
        }
        return folders;
    }


    public Map<URI, Proxy> getProxies() {
        return proxies;
    }


    public Multimap<URI, Annotation> getAnnotationsByTarget() {
        if (!loaded) {
            load();
        }
        return annotationsByTargetUri;
    }


    /**
     * Get the aggregated resource. Load the metadata first, if necessary.
     * 
     * @return a map of aggregated resource by their URI
     */
    public Map<URI, AggregatedResource> getAggregatedResources() {
        if (!loaded) {
            load();
        }
        return aggregatedResources;
    }


    @Override
    public ResourceMap getResourceMap() {
        return getManifest();
    }


    public Map<URI, ResourceMap> getFolderResourceMaps() {
        return folderResourceMaps;
    }

}
