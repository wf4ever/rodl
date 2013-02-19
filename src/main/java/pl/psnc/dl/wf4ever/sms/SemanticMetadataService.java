package pl.psnc.dl.wf4ever.sms;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.common.db.EvoType;
import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.model.RO.Resource;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.ontology.Individual;

/**
 * @author piotrhol
 * 
 */
public interface SemanticMetadataService {

    /**
     * Return the user profile with which the service has been created.
     * 
     * @return the user of the service
     */
    UserMetadata getUserProfile();


    /**
     * Create a new ro:ResearchObject and ro:Manifest. The new research object will have a LiveRO evolution class.
     * 
     * @param researchObjectURI
     *            RO URI, absolute
     */
    void createResearchObject(ResearchObject researchObject);


    /**
     * Create a new ro:ResearchObject and ro:Manifest. The new research object will have a LiveRO evolution class, and
     * will point to its source.
     * 
     * @param researchObjectURI
     *            RO URI, absolute
     * @param source
     *            URI of a source of the research object, may be null
     */
    void createLiveResearchObject(ResearchObject researchObject, ResearchObject source);


    /**
     * Update the manifest of a research object.
     * 
     * @param researchObject
     *            research object
     * @param inputStream
     *            the manifest
     * @param rdfFormat
     *            manifest RDF format
     */
    void updateManifest(ResearchObject researchObject, InputStream inputStream, RDFFormat rdfFormat);


    /**
     * Returns the manifest of an RO.
     * 
     * @param manifestURI
     *            manifest URI, absolute
     * @param rdfFormat
     *            returned manifest format
     * @return manifest with the research object URI as base URI
     */
    InputStream getManifest(ResearchObject researchObject, RDFFormat rdfFormat);


    /**
     * Adds a resource to ro:ResearchObject.
     * 
     * @param researchObjectURI
     *            RO URI, absolute
     * @param resourceURI
     *            resource URI, absolute or relative to RO URI
     * @param resourceInfo
     *            resource metadata
     * @return true if a new resource is added, false if it existed
     */
    Resource addResource(ResearchObject researchObject, URI resourceURI, ResourceMetadata resourceInfo);


    /**
     * Add a named graph to the quadstore. This is a generic method and will be made private in future releases. Use
     * specific methods to add annotations, manifest or folder resource maps.
     * 
     * @param graphURI
     *            named graph URI
     * @param inputStream
     *            named graph content
     * @param rdfFormat
     *            graph content format
     * @return true if a new named graph is added, false if it existed
     */
    @Deprecated
    boolean addNamedGraph(URI graphURI, InputStream inputStream, RDFFormat rdfFormat);


    /**
     * Check if a named graph exists
     * 
     * @param graphURI
     *            named graph URI
     * @return true if a named graph with this URI exists, false otherwie
     */
    boolean containsNamedGraph(URI graphURI);


    /**
     * Get a named graph. If the named graph references other named graphs and the RDF format is TriG or TriX,
     * referenced named graphs are returned as well.
     * 
     * @param graphURI
     *            graph URI
     * @param rdfFormat
     *            response format
     * @return named graph or null
     */
    InputStream getNamedGraph(URI graphURI, RDFFormat rdfFormat);


    /**
     * Delete a named graph from the quadstore if exists. This is a generic method and will be made private in future
     * releases. Use specific methods to remove annotations, manifest or folder resource maps.
     * 
     * @param graphURI
     *            graph URI
     */
    @Deprecated
    void removeNamedGraph(URI graphURI);


    /**
     * List ro:ResearchObject resources that start with the given URI.
     * 
     * @param partialURI
     *            URI with which the RO URI must start. If null, all ROs are returned.
     * 
     * @return set of RO URIs
     */
    Set<URI> findResearchObjectsByPrefix(URI partialURI);


    /**
     * List ro:ResearchObject resources that have the given author as dcterms:creator in their manifest.
     * 
     * @param user
     *            User URI.
     * 
     * @return set of RO URIs
     */
    Set<URI> findResearchObjectsByCreator(URI user);


    /**
     * Returns an RDF graph describing the given user.
     * 
     * @param userURI
     *            User URI
     * @param rdfFormat
     *            Requested RDF format, RDF/XML is the default one
     * @return A FOAF RDF graph in selected format
     */
    QueryResult getUser(URI userURI, RDFFormat rdfFormat);


    /**
     * Removes all data about a given user.
     * 
     * @param userURI
     *            User URI
     */
    void removeUser(URI userURI);


    /**
     * Returns a flat list of all attributes (facts and annotations) having a given resource as a subject. This searches
     * all named graphs, in all ROs.
     * 
     * If the property is dcterms:creator and the object is a foaf:Person, instead of the Person resource, its foaf:name
     * is put.
     * 
     * @param subjectURI
     *            URI of the resource
     * @return map of property URI with either a resource URI or a literal value (i.e. String or Calendar)
     */
    Multimap<URI, Object> getAllAttributes(URI subjectURI);


    /**
     * Closes the SemanticMetadataService and frees up resources held. Any subsequent calls to methods of the object
     * have undefined results.
     */
    void close();


    /**
     * Check if the research object aggregates a resource.
     * 
     * @param researchObject
     *            the research object
     * @param resource
     *            the resource URI
     * @return true if the research object aggregates the resource, false otherwise
     */
    boolean isAggregatedResource(ResearchObject researchObject, URI resource);


    /**
     * Find a proxy that has ore:proxyFor pointing to the resource.
     * 
     * @param researchObject
     *            research object to search
     * @param resource
     *            resource that the proxy must be for
     * @return proxy URI or null
     */
    URI getProxyForResource(ResearchObject researchObject, URI resource);


    /**
     * Delete a proxy.
     * 
     * @param researchObject
     *            research object in which the proxy is
     * @param proxy
     *            the proxy URI
     */
    void deleteProxy(ResearchObject researchObject, URI proxy);


    /**
     * Add an annotation to the research object.
     * 
     * @param researchObject
     *            research object
     * @param annotationTargets
     *            a list of annotated resources
     * @param annotationBody
     *            the annotation body
     * @param annotationUUID
     *            annotation prefix
     * @return URI of the annotation
     */
    Annotation addAnnotation(ResearchObject researchObject, Set<Thing> annotationTargets, URI annotationBody,
            String annotationUUID);


    /**
     * Update an existing annotation.
     * 
     * @param researchObject
     *            research object
     * @param annotation
     *            the annotation
     */
    void updateAnnotation(ResearchObject researchObject, Annotation annotation);


    /**
     * Check if a resource is an annotation defined in a research object.
     * 
     * @param researchObject
     *            research object to search
     * @param resource
     *            resource that may be an annotation
     * @return true if this resource is an annotation in the research object, false otherwise
     */
    boolean isAnnotation(ResearchObject researchObject, URI resource);


    /**
     * Return the annotation for the URI.
     * 
     * @param researchObject
     *            research object in which the annotation is
     * @param annotationUri
     *            the annotation URI
     * @return the annotation instance or null
     */
    Annotation getAnnotation(ResearchObject researchObject, URI annotationUri);


    /**
     * Delete an annotation.
     * 
     * @param researchObject
     *            research object in which the annotation is
     * @param annotation
     *            the annotation
     */
    void deleteAnnotation(ResearchObject researchObject, Annotation annotation);


    /**
     * Get a URL of live RO basing on snapshot or archive URI.
     * 
     * @param snapshotOrArchive
     *            snapshotOrArchive
     * @return the URI to the live RO
     * @throws URISyntaxException .
     */
    URI getLiveURIFromSnapshotOrArchive(ResearchObject snapshotOrArchive)
            throws URISyntaxException;


    /**
     * Check if the object is a snapshot of certain RO (default relevant path to the manifest file is .ro/manifest.rdf).
     * 
     * @param ro
     *            ResearchObject
     * @return true in case object is a snapshot, false if it is not
     */
    boolean isSnapshot(ResearchObject ro);


    /**
     * Check if the object is an archive of certain RO (default relevant path to the manifest file is .ro/manifest.rdf).
     * 
     * @param ro
     *            ResearchObject
     * @return true in case object is an archive, false if it is not
     */
    boolean isArchive(ResearchObject ro);


    /**
     * Get the predecessor of the currently processed snapshot or archive (default relevant path to the manifest file is
     * .ro/manifest.rdf).
     * 
     * @param liveRo
     *            live RO (source of the snapshot or archive) uri
     * @param freshSnapshotOrArchive
     *            currently processed snapshot or archive
     * @param type
     *            EvoType snapshot/archive/null
     * @return the URI of the current snapshot or archive predecessor
     */
    URI getPreviousSnapshotOrArchive(ResearchObject liveRo, ResearchObject freshSnapshotOrArchive, EvoType type);


    /**
     * Get the predecessor of the currently processed snapshot or archive (default relevant path to the manifest file is
     * .ro/manifest.rdf).
     * 
     * @param liveRo
     *            live RO (source of the snapshot or archive) uri
     * @param freshSnapshotOrArchive
     *            currently processed snapshot or archive
     * @return the URI of the current snapshot or archive predecessor
     */
    URI getPreviousSnapshotOrArchive(ResearchObject liveRo, ResearchObject freshSnapshotOrArchive);


    /**
     * Store information about differences between current and previous snapshot/archive.
     * 
     * @param freshRO
     *            The freshest snapshot/archive
     * @param oldRO
     *            Previous snapshot/archive
     * @return list of changes
     * @throws URISyntaxException .
     * @throws IOException .
     */
    String storeAggregatedDifferences(ResearchObject freshRO, ResearchObject oldRO)
            throws URISyntaxException, IOException;


    /**
     * Get individual of the resource object.
     * 
     * @param ro
     *            ResearchObject
     * @return the individual of the resource object joined from manifest and evolution information file
     */
    Individual getIndividual(ResearchObject ro);


    /**
     * Get resolved URI from base and relative path. Created because of the JAVA URI bug.
     * 
     * @param base
     *            base URI
     * @param second
     *            relative URI
     * @return the resolved URI
     */
    URI resolveURI(URI base, String second);


    /**
     * Get the evolution information of research object.
     * 
     * @param researchObject
     *            the URI of research object
     * @return Input Stream with the evolution information
     */
    InputStream getEvoInfo(ResearchObject researchObject);


    /**
     * Generate a RO evolution information .
     * 
     * @param type
     *            RO type
     * @param parent
     *            live Research Object
     * @param researchObject
     *            Snapshot or Archive
     * @return
     */
    Annotation generateEvoInformation(ResearchObject researchObject, ResearchObject parent, EvoType type);


    /**
     * Find annotation for given body URI.
     * 
     * @param researchObject
     *            Research Object
     * @param body
     *            Annotation body
     * @return Annotation, null if not found
     */
    Annotation findAnnotationForBody(ResearchObject researchObject, URI body);


    /**
     * Remove from aggregated list special files (manifest, evo_info).
     * 
     * @param aggregated
     *            list of aggregated
     * @return cleaned list of aggregated resources
     */
    List<AggregatedResource> removeSpecialFilesFromAggergated(List<AggregatedResource> aggregated);


    /**
     * Remove from annotation special annotations (manifest annotation, roevo annotation).
     * 
     * @param annotations
     *            list of annotations
     * @return cleaned list of annotations
     */
    List<Annotation> removeSpecialFilesFromAnnotatios(List<Annotation> annotations);

}
