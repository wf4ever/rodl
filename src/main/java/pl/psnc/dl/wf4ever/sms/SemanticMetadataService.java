package pl.psnc.dl.wf4ever.sms;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.common.EvoType;
import pl.psnc.dl.wf4ever.common.ResearchObject;
import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.ManifestTraversingException;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.RO.Folder;
import pl.psnc.dl.wf4ever.model.RO.FolderEntry;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.ontology.Individual;

/**
 * @author piotrhol
 * 
 */
public interface SemanticMetadataService {

    public static final RDFFormat SPARQL_XML = new RDFFormat("XML", "application/sparql-results+xml",
            Charset.forName("UTF-8"), "xml", false, false);

    public static final RDFFormat SPARQL_JSON = new RDFFormat("JSON", "application/sparql-results+json",
            Charset.forName("UTF-8"), "json", false, false);


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
     * Create a new ro:ResearchObject and ro:Manifest. The new research object will have a SnapshotRO evolution class.
     * 
     * @param researchObjectURI
     *            RO URI, absolute
     * @param liveRO
     *            URI of a live research object, may be null
     */
    void createSnapshotResearchObject(ResearchObject researchObject, ResearchObject liveRO);


    /**
     * Create a new ro:ResearchObject and ro:Manifest. The new research object will have a ArchivedRO evolution class.
     * 
     * @param researchObjectURI
     *            RO URI, absolute
     * @param liveRO
     *            URI of a live research object, may be null
     */
    void createArchivedResearchObject(ResearchObject researchObject, ResearchObject liveRO);


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
     * Removes a research object, its manifest, proxies, internal aggregated resources and internal named graphs. A
     * resource/named graph is considered internal if it contains the research object URI.
     * 
     * @param researchObjectURI
     *            RO URI, absolute
     */
    void removeResearchObject(ResearchObject researchObject);


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
    boolean addResource(ResearchObject researchObject, URI resourceURI, ResourceMetadata resourceInfo);


    /**
     * Removes a resource from ro:ResearchObject.
     * 
     * @param researchObjectURI
     *            RO URI, absolute
     * @param resourceURI
     *            resource URI, absolute or relative to RO URI
     */
    void removeResource(ResearchObject researchObject, URI resourceURI);


    /**
     * Returns resource metadata.
     * 
     * @param researchObjectURI
     *            RO URI, absolute
     * @param resourceURI
     *            resource URI, absolute or relative to RO URI
     * @param rdfFormat
     *            resource metadata format
     * @return resource description or null if no data found
     */
    InputStream getResource(ResearchObject researchObject, URI resourceURI, RDFFormat rdfFormat);


    /**
     * Return true if the resource exists and belongs to class ro:Folder
     * 
     * @param resourceURI
     *            resource URI
     * @return true if the resource exists and belongs to class ro:Folder, false otherwise
     */
    boolean isRoFolder(ResearchObject researchObject, URI resourceURI);


    /**
     * Add an annotation body.
     * 
     * @param researchObject
     *            RO that contains the annotation
     * @param graphURI
     *            named graph URI
     * @param inputStream
     *            named graph content
     * @param rdfFormat
     *            graph content format
     * @return true if a new named graph is added, false if it existed
     */
    boolean addAnnotationBody(ResearchObject researchObject, URI graphURI, InputStream inputStream, RDFFormat rdfFormat);


    /**
     * Remove an annotation body.
     * 
     * @param researchObject
     *            RO that contains the annotation
     * @param graphURI
     *            named graph URI
     */
    void removeAnnotationBody(ResearchObject researchObject, URI graphURI);


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
     * Checks if the resource is the manifest or there exists an annotation that has ao:body pointing to the resource.
     * Note that in case of the annotation body, it does not necessarily exist.
     * 
     * @param researchObject
     *            research object to search in
     * @param resource
     *            resource which should be pointed
     * @return true if it's a manifest or annotation body, false otherwise
     */
    boolean isROMetadataNamedGraph(ResearchObject researchObject, URI graphURI);


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
     * Get a portable named graph. The URIs will be relativized against the RO URI. All references to other named graphs
     * within the RO will have a file extension appended.
     * 
     * @param graphURI
     * @param rdfFormat
     * @param researchObjectURI
     * @param fileExtension
     * @return
     */
    InputStream getNamedGraphWithRelativeURIs(URI graphURI, ResearchObject researchObject, RDFFormat rdfFormat);


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
     * List ro:ResearchObject resources.
     * 
     * @return set of RO URIs
     */
    Set<URI> findResearchObjects();


    /**
     * Responses are a available in a range of different formats. The specific formats available depend on the type of
     * SPARQL query being executed. SPARQL defines four different types of query: CONSTRUCT, DESCRIBE, SELECT and ASK.
     * 
     * CONSTRUCT and DESCRIBE queries both return RDF graphs and so the usual range of RDF serializations are available,
     * including RDF/XML, RDF/JSON, Turtle, etc.
     * 
     * SELECT queries return a tabular result set, while ASK queries return a boolean value. Results from both of these
     * query types can be returned in either SPARQL XML Results Format or SPARQL JSON Results Format.
     * 
     * See also http://www.w3.org/TR/rdf-sparql-XMLres/
     * 
     * @param query
     * @param rdfFormat
     * @return
     */
    QueryResult executeSparql(String query, RDFFormat rdfFormat);


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
     * Create a new ro:Proxy for a resource. The resource does not have to be already aggregated.
     * 
     * @param researchObject
     *            research object to which to add the proxy
     * @param resource
     *            resource for which the proxy will be
     * @return proxy URI
     */
    URI addProxy(ResearchObject researchObject, URI resource);


    /**
     * Check if the research object defines a proxy with a given URI
     * 
     * @param researchObject
     *            research object to search
     * @param resource
     *            resource that may be a proxy
     * @return true if the resource is a proxy, false otherwise
     */
    boolean isProxy(ResearchObject researchObject, URI resource);


    /**
     * Checks if there exists a proxy that has ore:proxyFor pointing to the resource.
     * 
     * @param researchObject
     *            research object to search in
     * @param resource
     *            resource which should be pointed
     * @return true if a proxy was found, false otherwise
     */
    boolean existsProxyForResource(ResearchObject researchObject, URI resource);


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
     * Return the ore:proxyFor object of a proxy.
     * 
     * @param researchObject
     *            research object in which the proxy is
     * @param proxy
     *            the proxy URI
     * @return the proxyFor object URI or null if not defined
     */
    URI getProxyFor(ResearchObject researchObject, URI proxy);


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
     * @return URI of the annotation
     */
    URI addAnnotation(ResearchObject researchObject, List<URI> annotationTargets, URI annotationBody);


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
    URI addAnnotation(ResearchObject researchObject, List<URI> annotationTargets, URI annotationBody,
            String annotationUUID);


    /**
     * Update an existing annotation.
     * 
     * @param researchObject
     *            research object
     * @param annotation
     *            URI of the annotation
     * @param annotationTargets
     *            a list of annotated resources
     * @param annotationBody
     *            the annotation body
     */
    void updateAnnotation(ResearchObject researchObject, URI annotation, List<URI> annotationTargets, URI annotationBody);


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
     * Return the ao:body object of an annotation.
     * 
     * @param researchObject
     *            research object in which the annotation is
     * @param annotation
     *            the annotation URI
     * @return the ao:body object URI or null if not defined
     */
    URI getAnnotationBody(ResearchObject researchObject, URI annotation);


    /**
     * Delete an annotation.
     * 
     * @param researchObject
     *            research object in which the annotation is
     * @param annotation
     *            the annotation URI
     */
    void deleteAnnotation(ResearchObject researchObject, URI annotation);


    /**
     * Temporary method for converting ROSR5 triplestore to RODL6 triplestore.
     * 
     * @param oldDatasource
     *            ROSR5 datasource
     * 
     * @throws NamingException
     *             couldn't connect to the old datasource .
     * @throws SQLException
     *             couldn't connect to the old datasource .
     */
    int migrateRosr5To6(String oldDatasource)
            throws NamingException, SQLException;


    /**
     * Changes all references to the first URI into the second.
     * 
     * @param oldUri
     *            old URI
     * @param uri
     *            new URI
     * @return number of quads changed
     */
    int changeURI(URI oldUri, URI uri);


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
     * @param freshSnaphotOrArchive
     *            currently processed snapshot or archive
     * @param type
     *            EvoType snapshot/archive/null
     * @return the URI of the current snapshot or archive predecessor
     */
    URI getPreviousSnaphotOrArchive(ResearchObject liveRo, ResearchObject freshSnaphotOrArchive, EvoType type);


    /**
     * Get the predecessor of the currently processed snapshot or archive (default relevant path to the manifest file is
     * .ro/manifest.rdf).
     * 
     * @param liveRo
     *            live RO (source of the snapshot or archive) uri
     * @param freshSnaphotOrArchive
     *            currently processed snapshot or archive
     * @return the URI of the current snapshot or archive predecessor
     */
    URI getPreviousSnaphotOrArchive(ResearchObject liveRo, ResearchObject freshSnaphotOrArchive);


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
     * Change all occurrences of one URI into another in the context (manifest and annotation bodies) of one research
     * object.
     * 
     * @param researchObject
     *            RO URI
     * @param oldURI
     *            Old URI
     * @param newURI
     *            New URI
     * @param withBodies
     *            Change bodies in case of true, change only manifest otherwise
     * @return number of changed triples
     */
    int changeURIInManifestAndAnnotationBodies(ResearchObject researchObject, URI oldURI, URI newURI, Boolean withBodies);


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
     * Get the list of aggregated resources.
     * 
     * @param researchObject
     *            the research object
     * @return the list of aggregated resources.
     * @throws ManifestTraversingException .
     */
    List<AggregatedResource> getAggregatedResources(ResearchObject researchObject)
            throws ManifestTraversingException;


    /**
     * Get the list of RO annotations.
     * 
     * @param researchObject
     *            the research object
     * @return the list of annotations.
     * @throws ManifestTraversingException .
     */
    List<Annotation> getAnnotations(ResearchObject researchObject)
            throws ManifestTraversingException;


    /**
     * Add a named graph describing the folder and aggregate it by the RO. The folder must have its URI set. The folder
     * entries must have their proxyFor and RO name set. The folder entry URIs will be generated automatically if not
     * set.
     * 
     * If there is no root folder in the RO, the new folder will be the root folder of the RO.
     * 
     * @param researchObject
     *            RO
     * @param folder
     *            folder
     * @return an updated folder
     */
    Folder addFolder(ResearchObject researchObject, Folder folder);


    /**
     * Generate a RO evolution information .
     * 
     * @param type
     *            RO type
     * @param parent
     *            live Research Object
     * @param researchObject
     *            Snapshot or Archive
     */
    void generateEvoInformation(ResearchObject researchObject, ResearchObject parent, EvoType type);


    /**
     * Generate a RO evolution information. In case of snaphot/archive the creator information may change. The new
     * parameter is introduced in this method
     * 
     * @param type
     *            RO type
     * @param parent
     *            live Research Object
     * @param researchObject
     *            Snapshot or Archive
     * @param creator
     *            creator openID
     */
    void generateEvoInformation(ResearchObject researchObject, ResearchObject parent, EvoType type, String creator);


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
     * Find a folder description.
     * 
     * @param folderURI
     *            folder URI
     * @return a folder instance or null if not found
     */
    Folder getFolder(URI folderURI);


    /**
     * Update the named graph with the folder resource map. The folder must have its URI set. The folder entries must
     * have their proxyFor and RO name set. The folder entry URIs will be generated automatically if not set.
     * 
     * Note that the URIs are immutable. Subject to update are: list of folder entries, names of folder entries.
     * 
     * @param folder
     *            the new folder
     */
    void updateFolder(Folder folder);


    /**
     * Find the root folder of the RO.
     * 
     * @param researchObject
     *            research object
     * @return root folder or null if not defined
     */
    Folder getRootFolder(ResearchObject researchObject);


    /**
     * Delete the folder.
     * 
     * @param folder
     *            folder
     */
    void deleteFolder(Folder folder);


    /**
     * Create folder entry based on the URI or return null if doesn't exist.
     * 
     * @param resource
     *            entry URI
     * @return folder entry or null
     */
    FolderEntry getFolderEntry(URI resource);


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
