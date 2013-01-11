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

    /** aggregated annotations, grouped based on ao:annotatesResource. */
    private Multimap<URI, Annotation> annotationsByBodyUri;

    /** aggregated annotations. */
    private Map<URI, Annotation> annotations;

    /** folder resource maps. */
    private Map<URI, ResourceMap> resourceMaps;

    /** Manifest. */
    private Manifest manifest;


    //TODO add properties stored in evo_info.ttl

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
     *            the RO URI
     */
    public ResearchObject(UserMetadata user, Dataset dataset, boolean useTransactions, URI uri) {
        super(user, dataset, useTransactions, uri);
    }


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     * @param uri
     *            the RO URI
     */
    public ResearchObject(UserMetadata user, URI uri) {
        super(user, uri);
    }


    /**
     * Create new Research Object.
     * 
     * @param builder
     *            model instance builder
     * @param uri
     *            RO URI
     * @return an instance
     */
    public static ResearchObject create(Builder builder, URI uri) {
        if (get(builder, uri) != null) {
            throw new ConflictException("Research Object already exists: " + uri);
        }
        ResearchObject researchObject = builder.buildResearchObject(uri, builder.getUser().getUri(), DateTime.now());
        researchObject.manifest = Manifest.create(builder, researchObject.getUri().resolve(MANIFEST_PATH),
            researchObject);
        researchObject.save();
        return researchObject;
    }


    /**
     * Generate and save the evolution information.
     */
    public void generateEvoInfo() {
        Annotation ann = ROSRService.SMS.get().generateEvoInformation(this, null, EvoType.LIVE);
        this.getAnnotations().put(ann.getUri(), ann);
        this.getAnnotationsByTarget().put(ann.getAnnotated().iterator().next().getUri(), ann);
        this.getEvoInfoBody().serialize();
        this.getManifest().serialize();
    }


    /**
     * Get the resource with the evolution metadata.
     * 
     * @return an evolution resource
     */
    public AggregatedResource getEvoInfoBody() {
        //HACK this should be added automatically
        this.getAggregatedResources().put(getFixedEvolutionAnnotationBodyUri(),
            new AggregatedResource(user, this, getFixedEvolutionAnnotationBodyUri()));

        return aggregatedResources.get(getFixedEvolutionAnnotationBodyUri());
    }


    /**
     * Get the manifest, loaded lazily.
     * 
     * @return the manifest
     */
    public Manifest getManifest() {
        if (manifest == null) {
            this.manifest = builder.buildManifest(getManifestUri(), this);
        }
        return manifest;
    }


    /**
     * Get a Research Object if it exists.
     * 
     * @param builder
     *            model instance builder
     * @param uri
     *            uri
     * @return an existing Research Object or null
     */
    public static ResearchObject get(Builder builder, URI uri) {
        ResearchObject researchObject = builder.buildResearchObject(uri);
        if (researchObject.getManifest().isNamedGraph()) {
            return researchObject;
        } else {
            return null;
        }
    }


    /**
     * Delete the Research Object including its resources and annotations.
     */
    @Override
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
    }


    @Override
    public void save() {
        super.save();
        getManifest().save();

        //TODO check if to create an RO or only serialize the manifest
        ROSRService.DL.get().createResearchObject(uri, getManifest().getGraphAsInputStream(RDFFormat.RDFXML),
            ResearchObject.MANIFEST_PATH, RDFFormat.RDFXML.getDefaultMIMEType());
        generateEvoInfo();
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
     * @throws BadRequestException
     *             if it should be an annotation body according to an existing annotation and it's the wrong format
     */
    public Resource aggregate(String path, InputStream content, String contentType)
            throws BadRequestException {
        URI resourceUri = UriBuilder.fromUri(uri).path(path).build();
        Resource resource = Resource.create(builder, this, resourceUri, content, contentType);
        if (getAnnotationsByBodyUri().containsKey(resource.getUri())) {
            resource.saveGraph();
        }
        getManifest().serialize();
        this.getResources().put(resource.getUri(), resource);
        this.getAggregatedResources().put(resource.getUri(), resource);
        this.getProxies().put(resource.getProxy().getUri(), resource.getProxy());
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
        this.getManifest().serialize();
        this.getResources().put(resource.getUri(), resource);
        this.getAggregatedResources().put(resource.getUri(), resource);
        return resource;
    }


    /**
     * Add a new folder and update the maps.
     * 
     * @param folderUri
     *            folder URI
     * @param content
     *            folder description
     * @return a folder instance
     * @throws BadRequestException
     *             the folder description is not valid
     */
    public Folder aggregateFolder(URI folderUri, InputStream content)
            throws BadRequestException {
        Folder folder = Folder.create(builder, this, folderUri, content);
        getManifest().serialize();
        this.getFolders().put(folder.getUri(), folder);
        this.getAggregatedResources().put(folder.getUri(), folder);
        this.getProxies().put(folder.getProxy().getUri(), folder.getProxy());
        return folder;
    }


    /**
     * Add and aggregate a new annotation to the research object.
     * 
     * @param body
     *            annotation body URI
     * @param targets
     *            list of annotated resources URIs
     * @return new annotation
     * @throws BadRequestException
     *             if there is no data in storage or the file format is not RDF
     */
    public Annotation annotate(URI body, Set<Thing> targets)
            throws BadRequestException {
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
     * @throws BadRequestException
     *             if there is no data in storage or the file format is not RDF
     */
    public Annotation annotate(URI body, Set<Thing> targets, String annotationId)
            throws BadRequestException {
        URI annotationUri = getAnnotationUri(annotationId);
        Annotation annotation = Annotation.create(builder, this, annotationUri, body, targets);
        return postAnnotate(annotation);
    }


    /**
     * Add and aggregate a new annotation to the research object.
     * 
     * @param data
     *            annotation description
     * @return new annotation
     * @throws BadRequestException
     *             if there is no data in storage or the file format is not RDF
     */
    public Annotation annotate(InputStream data)
            throws BadRequestException {
        URI annotationUri = getAnnotationUri(null);
        Annotation annotation = Annotation.create(builder, this, annotationUri, data);
        return postAnnotate(annotation);
    }


    /**
     * Make all changes necessary after creating the annotation.
     * 
     * @param annotation
     *            new annotation
     * @return the annotation
     * @throws BadRequestException
     *             if there is no data in storage or the file format is not RDF
     */
    private Annotation postAnnotate(Annotation annotation)
            throws BadRequestException {
        AggregatedResource resource = getAggregatedResources().get(annotation.getBodyUri());
        if (resource != null && resource.isInternal()) {
            resource.saveGraph();
        }
        getManifest().serialize();
        this.getAnnotations().put(annotation.getUri(), annotation);
        for (Thing target : annotation.getAnnotated()) {
            this.getAnnotationsByTarget().put(target.getUri(), annotation);
        }
        this.getAnnotationsByBodyUri().put(annotation.getBodyUri(), annotation);
        this.getAggregatedResources().put(annotation.getUri(), annotation);
        return annotation;
    }


    /**
     * Get an annotation URI based on the id.
     * 
     * @param annotationId
     *            annotation id, random UUID will be used if null
     * @return the annotation URI
     */
    private URI getAnnotationUri(String annotationId) {
        if (annotationId == null) {
            annotationId = UUID.randomUUID().toString();
        }
        URI annotationUri = uri.resolve(".ro/annotations/" + annotationId);
        return annotationUri;
    }


    /**
     * Update the RO index.
     */
    public void updateIndexAttributes() {
        //FIXME this makes no sense without dLibra
        Multimap<URI, Object> roAttributes = ROSRService.SMS.get().getAllAttributes(uri);
        roAttributes.put(URI.create("Identifier"), this);
        try {
            ROSRService.DL.get().storeAttributes(uri, roAttributes);
        } catch (Exception e) {
            LOGGER.error("Caught an exception when updating RO attributes, will continue", e);
        }
    }


    /**
     * Create a new research object submitted in ZIP format.
     * 
     * @param builder
     *            model instance builder
     * @param researchObjectUri
     *            the new research object
     * @param zip
     *            the ZIP file
     * @return HTTP response (created in case of success, 404 in case of error)
     * @throws IOException
     *             error creating the temporary file
     * @throws BadRequestException
     *             the zip contains an invalid RO
     */
    public static ResearchObject create(Builder builder, URI researchObjectUri, MemoryZipFile zip)
            throws IOException, BadRequestException {
        ResearchObject researchObject = create(builder, researchObjectUri);
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
                if (researchObject.getAggregatedResources().containsKey(annotation.getBodyUri())) {
                    ROSRService.convertRoResourceToAnnotationBody(researchObject, researchObject
                            .getAggregatedResources().get(annotation.getBodyUri()));
                }
                researchObject.annotate(annotation.getBodyUri(), annotation.getAnnotated());
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


    /**
     * Get aggregated ro:Resources, excluding ro:Folders, loaded lazily.
     * 
     * @return aggregated resources mapped by their URI
     */
    public Map<URI, Resource> getResources() {
        if (resources == null) {
            this.resources = getManifest().extractResources();
        }
        return resources;
    }


    /**
     * Get aggregated ro:Folders, loaded lazily.
     * 
     * @return aggregated folders mapped by their URI
     */
    public Map<URI, Folder> getFolders() {
        if (folders == null) {
            this.folders = getManifest().extractFolders();
        }
        return folders;
    }


    /**
     * Get proxies for aggregated resources, loaded lazily.
     * 
     * @return proxies mapped by their URI
     */
    public Map<URI, Proxy> getProxies() {
        if (proxies == null) {
            this.proxies = new HashMap<>();
            for (AggregatedResource aggregatedResource : this.getAggregatedResources().values()) {
                Proxy proxy = aggregatedResource.getProxy();
                if (proxy != null) {
                    this.proxies.put(proxy.getUri(), proxy);
                }
            }
        }
        return proxies;
    }


    /**
     * Get aggregated annotations, loaded lazily.
     * 
     * @return aggregated annotations mapped by their URI
     */
    public Map<URI, Annotation> getAnnotations() {
        if (annotations == null) {
            this.annotations = getManifest().extractAnnotations();
        }
        return annotations;
    }


    /**
     * Get aggregated annotations, mapped by the annotated resources, loaded lazily.
     * 
     * @return aggregated annotations mapped by annotated resources URIs
     */
    public Multimap<URI, Annotation> getAnnotationsByTarget() {
        if (annotationsByTargetUri == null) {
            this.annotationsByTargetUri = HashMultimap.<URI, Annotation> create();
            for (Annotation ann : getAnnotations().values()) {
                for (Thing target : ann.getAnnotated()) {
                    this.annotationsByTargetUri.put(target.getUri(), ann);
                }
            }
        }
        return annotationsByTargetUri;
    }


    /**
     * Get aggregated annotations, mapped by the bodies, loaded lazily.
     * 
     * @return aggregated annotations mapped by body URIs
     */
    public Multimap<URI, Annotation> getAnnotationsByBodyUri() {
        if (annotationsByBodyUri == null) {
            this.annotationsByBodyUri = HashMultimap.<URI, Annotation> create();
            for (Annotation ann : getAnnotations().values()) {
                this.annotationsByBodyUri.put(ann.getBodyUri(), ann);
            }
        }
        return annotationsByBodyUri;
    }


    /**
     * Get the aggregated resource. Load the metadata first, if necessary.
     * 
     * @return a map of aggregated resource by their URI
     */
    public Map<URI, AggregatedResource> getAggregatedResources() {
        if (aggregatedResources == null) {
            this.aggregatedResources = getManifest().extractAggregatedResources(getResources(), getFolders(),
                getAnnotations());
        }
        return aggregatedResources;
    }


    @Override
    public ResourceMap getResourceMap() {
        return getManifest();
    }


    /**
     * Get manifest and folder resource maps, loading the lazily.
     * 
     * @return folder resource maps mapped by their URIs
     */
    public Map<URI, ResourceMap> getResourceMaps() {
        if (resourceMaps == null) {
            this.resourceMaps = new HashMap<>();
            this.resourceMaps.put(getManifest().getUri(), getManifest());
            for (Folder folder : getFolders().values()) {
                resourceMaps.put(folder.getResourceMap().getUri(), folder.getResourceMap());
            }
        }
        return resourceMaps;
    }


    @Override
    public DateTime getCreated() {
        if (created == null) {
            this.created = getManifest().extractCreated(this);
        }
        return super.getCreated();
    }


    @Override
    public URI getCreator() {
        if (creator == null) {
            this.creator = getManifest().extractCreator(this);
        }
        return super.getCreator();
    }


    /**
     * Is there already a resource in this RO with that URI.
     * 
     * @param uri
     *            the URI
     * @return true if there is an aggregated resource / proxy / folder resource map / manifest with that URI
     */
    public boolean isUriUsed(URI uri) {
        //FIXME folder entries are missing
        return getAggregatedResources().containsKey(uri) || getProxies().containsKey(uri)
                || getResourceMaps().containsKey(uri);
    }

}
