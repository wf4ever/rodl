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
import java.util.UUID;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.common.EvoType;
import pl.psnc.dl.wf4ever.common.util.MemoryZipFile;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.exceptions.IncorrectModelException;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceTdb;
import pl.psnc.dl.wf4ever.vocabulary.AO;
import pl.psnc.dl.wf4ever.vocabulary.ORE;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.DCTerms;

/**
 * A research object, live by default.
 * 
 * @author piotrekhol
 * 
 */
public class ResearchObject extends Thing {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(ResearchObject.class);

    /** Manifest path. */
    public static final String MANIFEST_PATH = ".ro/manifest.rdf";

    /** Fixed roevo annotation file path. */
    private static final String ROEVO_PATH = ".ro/evo_info.ttl";

    /** has the metadata been loaded from triplestore. */
    private boolean loaded;

    /** aggregated ro:Resources, excluding ro:Folders. */
    private Map<URI, Resource> resources;

    /** aggregated ro:Folders. */
    private Map<URI, Folder> folders;

    /** aggregated annotations, grouped based on ao:annotatesResource. */
    private Multimap<URI, Annotation> annotations;


    //TODO add properties stored in evo_info.ttl

    /**
     * Constructor.
     * 
     * @param uri
     *            RO URI
     */
    //FIXME should this be private?
    public ResearchObject(URI uri) {
        super(uri);
    }


    /**
     * Create new Research Object.
     * 
     * @param uri
     *            RO URI
     * @return an instance
     * @throws AccessDeniedException
     * @throws NotFoundException
     * @throws DigitalLibraryException
     * @throws ConflictException
     */
    public static ResearchObject create(URI uri)
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        ResearchObject researchObject = new ResearchObject(uri);
        InputStream manifest;
        try {
            ROSRService.SMS.get().createLiveResearchObject(researchObject, null);
            manifest = ROSRService.SMS.get().getManifest(researchObject, RDFFormat.RDFXML);
        } catch (IllegalArgumentException e) {
            // RO already existed in sms, maybe created by someone else
            throw new ConflictException("The RO with URI " + researchObject.getUri() + " already exists");
        }

        ROSRService.DL.get().createResearchObject(researchObject.getUri(), manifest, ResearchObject.MANIFEST_PATH,
            RDFFormat.RDFXML.getDefaultMIMEType());
        researchObject.generateEvoInfo();
        return researchObject;
    }


    public void generateEvoInfo()
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        ROSRService.SMS.get().generateEvoInformation(this, null, EvoType.LIVE);
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
        return new AggregatedResource(getFixedEvolutionAnnotationBodyUri(), this);
    }


    public Manifest getManifest() {
        return new Manifest(getManifestUri(), this);
    }


    /**
     * Get a Research Object.
     * 
     * @param uri
     *            uri
     * @return an existing Research Object or null
     */
    public static ResearchObject get(URI uri) {
        if (ROSRService.SMS.get() == null
                || ROSRService.SMS.get().containsNamedGraph(uri.resolve(ResearchObject.MANIFEST_PATH))) {
            return new ResearchObject(uri);
        } else {
            return null;
        }
    }


    /**
     * Delete the Research Object including its resources and annotations.
     */
    public void delete() {
        throw new UnsupportedOperationException("Not implemented yet");
    }


    /**
     * Load the metadata from the triplestore.
     * 
     * @return this instance, loaded
     * @throws IncorrectModelException
     */
    public ResearchObject load()
            throws IncorrectModelException {
        //TODO should it be a private property?
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
        model.read(ROSRService.SMS.get().getManifest(this, RDFFormat.RDFXML), null);

        this.creator = extractCreator(model);
        this.created = extractCreated(model);
        this.resources = extractResources(model);
        this.folders = extractFolders(model);
        this.annotations = extractAnnotations(model);
        this.loaded = true;
        return this;
    }


    /**
     * Identify ro:Resources that are not ro:Folders, aggregated by the RO.
     * 
     * @param model
     *            manifest model
     * @return a set of resources (not loaded)
     */
    private Map<URI, Resource> extractResources(OntModel model) {
        Map<URI, Resource> resources2 = new HashMap<>();
        String queryString = String
                .format(
                    "PREFIX ore: <%s> PREFIX dcterms: <%s> PREFIX ro: <%s> SELECT ?resource ?proxy ?created ?creator WHERE { <%s> ore:aggregates ?resource . ?resource a ro:Resource . ?proxy ore:proxyFor ?resource . OPTIONAL { ?resource dcterms:creator ?creator . } OPTIONAL { ?resource dcterms:created ?created . } }",
                    ORE.NAMESPACE, DCTerms.NS, RO.NAMESPACE, uri.toString());

        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        try {
            ResultSet results = qe.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                RDFNode r = solution.get("resource");
                if (r.as(Individual.class).hasRDFType(RO.Folder)) {
                    continue;
                }
                URI rURI = URI.create(r.asResource().getURI());
                RDFNode p = solution.get("proxy");
                RDFNode creatorNode = solution.get("creator");
                URI resCreator = creatorNode != null && creatorNode.isURIResource() ? URI.create(creatorNode
                        .asResource().getURI()) : null;
                RDFNode createdNode = solution.get("created");
                DateTime resCreated = createdNode != null && createdNode.isLiteral() ? DateTime.parse(createdNode
                        .asLiteral().getString()) : null;
                resources2.put(rURI, new Resource(this, rURI, URI.create(p.asResource().getURI()), resCreator,
                        resCreated));
            }
        } finally {
            qe.close();
        }

        return resources2;
    }


    /**
     * Identify ro:Resources that are not ro:Folders, aggregated by the RO.
     * 
     * @param model
     *            manifest model
     * @return a set of folders (not loaded)
     */
    private Map<URI, Folder> extractFolders(OntModel model) {
        Map<URI, Folder> folders2 = new HashMap<>();
        String queryString = String
                .format(
                    "PREFIX ore: <%s> PREFIX dcterms: <%s> PREFIX ro: <%s> SELECT ?folder ?proxy ?resourcemap ?created ?creator WHERE { <%s> ore:aggregates ?folder . ?folder a ro:Folder ; ore:isDescribedBy ?resourcemap . ?proxy ore:proxyFor ?folder . OPTIONAL { ?folder dcterms:creator ?creator . } OPTIONAL { ?folder dcterms:created ?created . } }",
                    ORE.NAMESPACE, DCTerms.NS, RO.NAMESPACE, uri.toString());

        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        try {
            ResultSet results = qe.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                RDFNode f = solution.get("folder");
                URI fURI = URI.create(f.asResource().getURI());
                RDFNode p = solution.get("proxy");
                RDFNode rm = solution.get("resourcemap");
                RDFNode creatorNode = solution.get("creator");
                URI resCreator = creatorNode != null && creatorNode.isURIResource() ? URI.create(creatorNode
                        .asResource().getURI()) : null;
                RDFNode createdNode = solution.get("created");
                DateTime resCreated = createdNode != null && createdNode.isLiteral() ? DateTime.parse(createdNode
                        .asLiteral().getString()) : null;

                String queryString2 = String.format("PREFIX ro: <%s> ASK { <%s> ro:rootFolder <%s> }", RO.NAMESPACE,
                    uri.toString(), fURI.toString());
                Query query2 = QueryFactory.create(queryString2);
                QueryExecution qe2 = QueryExecutionFactory.create(query2, model);
                boolean isRootFolder = false;
                try {
                    isRootFolder = qe2.execAsk();
                } finally {
                    qe2.close();
                }

                folders2.put(fURI,
                    new Folder(this, fURI, URI.create(p.asResource().getURI()), URI.create(rm.asResource().getURI()),
                            resCreator, resCreated, isRootFolder));
            }
        } finally {
            qe.close();
        }

        return folders2;
    }


    /**
     * Identify ro:AggregatedAnnotations that aggregated by the RO.
     * 
     * @param model
     *            manifest model
     * @return a multivalued map of annotations, with bodies not loaded
     */
    private Multimap<URI, Annotation> extractAnnotations(OntModel model) {
        Multimap<URI, Annotation> annotations2 = HashMultimap.<URI, Annotation> create();
        Map<URI, Annotation> annotationsByUri = new HashMap<>();
        String queryString = String
                .format(
                    "PREFIX ore: <%s> PREFIX dcterms: <%s> PREFIX ao: <%s> PREFIX ro: <%s> SELECT ?annotation ?body ?target ?created ?creator WHERE { <%s> ore:aggregates ?annotation . ?annotation a ro:AggregatedAnnotation ; ao:body ?body ; ro:annotatesAggregatedResource ?target . OPTIONAL { ?annotation dcterms:creator ?creator . } OPTIONAL { ?annotation dcterms:created ?created . } }",
                    ORE.NAMESPACE, DCTerms.NS, AO.NAMESPACE, RO.NAMESPACE, uri.toString());

        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        try {
            ResultSet results = qe.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                RDFNode a = solution.get("annotation");
                URI aURI = URI.create(a.asResource().getURI());
                RDFNode t = solution.get("target");
                URI tURI = URI.create(t.asResource().getURI());
                Annotation annotation;
                if (annotationsByUri.containsKey(aURI)) {
                    annotation = annotationsByUri.get(aURI);
                    annotation.getAnnotated().add(tURI);
                } else {
                    RDFNode b = solution.get("body");
                    RDFNode creatorNode = solution.get("creator");
                    URI resCreator = creatorNode != null && creatorNode.isURIResource() ? URI.create(creatorNode
                            .asResource().getURI()) : null;
                    RDFNode createdNode = solution.get("created");
                    DateTime resCreated = createdNode != null && createdNode.isLiteral() ? DateTime.parse(createdNode
                            .asLiteral().getString()) : null;
                    annotation = new Annotation(this, aURI, URI.create(b.asResource().getURI()), tURI, resCreator,
                            resCreated);
                }
                annotations2.put(tURI, annotation);
            }
        } finally {
            qe.close();
        }

        return annotations2;
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
     * @throws NotFoundException
     * @throws DigitalLibraryException
     * @throws AccessDeniedException
     * @throws IncorrectModelException
     * @throws ROSRSException
     *             server returned an unexpected response
     * @throws ROException
     *             the manifest is incorrect
     */
    public Resource aggregate(String path, InputStream content, String contentType)
            throws AccessDeniedException, DigitalLibraryException, NotFoundException, IncorrectModelException {
        URI resourceUri = UriBuilder.fromUri(uri).path(path).build();
        ResourceMetadata resourceInfo = ROSRService.DL.get().createOrUpdateFile(uri, path, content,
            contentType != null ? contentType : "text/plain");
        Resource resource = ROSRService.SMS.get().addResource(this, resourceUri, resourceInfo);
        // update the manifest that describes the resource in dLibra
        getManifest().serialize();
        URI proxy = ROSRService.SMS.get().addProxy(this, resourceUri);
        resource.setProxyUri(proxy);
        if (!loaded) {
            load();
        }
        //this is unnecessary if the resource has been saved in the triplestore before loading
        this.resources.put(resource.getUri(), resource);
        return resource;
    }


    /**
     * Add an external resource (a reference to a resource) to the research object.
     * 
     * @param uri
     *            resource URI
     * @return the resource instance
     * @throws NotFoundException
     * @throws DigitalLibraryException
     * @throws AccessDeniedException
     * @throws IncorrectModelException
     * @throws ROSRSException
     *             server returned an unexpected response
     * @throws ROException
     *             the manifest is incorrect
     */
    public Resource aggregate(URI uri)
            throws AccessDeniedException, DigitalLibraryException, NotFoundException, IncorrectModelException {
        Resource resource = ROSRService.SMS.get().addResource(this, uri, null);
        resource.setProxyUri(ROSRService.SMS.get().addProxy(this, uri));
        // update the manifest that describes the resource in dLibra
        this.getManifest().serialize();
        if (!loaded) {
            load();
        }
        this.resources.put(resource.getUri(), resource);
        return resource;
    }


    /**
     * Create a new research object submitted in ZIP format.
     * 
     * @param researchObject
     *            the new research object
     * @param zip
     *            the ZIP file
     * @return HTTP response (created in case of success, 404 in case of error)
     * @throws BadRequestException .
     * @throws AccessDeniedException .
     * @throws IOException
     *             error creating the temporary file
     * @throws ConflictException
     *             Research Object already exists
     * @throws NotFoundException
     * @throws DigitalLibraryException
     */
    public static ResearchObject create(URI researchObjectUri, MemoryZipFile zip)
            throws BadRequestException, AccessDeniedException, IOException, ConflictException, DigitalLibraryException,
            NotFoundException {
        ResearchObject researchObject = create(researchObjectUri);
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
                if (ROSRService.SMS.get().isAggregatedResource(researchObject, annotation.getBody())) {
                    ROSRService.convertRoResourceToAnnotationBody(researchObject, annotation.getBody());
                }
                ROSRService.addAnnotation(researchObject, annotation.getBody(), annotation.getAnnotated());
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
        return resources;
    }


    public Map<URI, Folder> getFolders() {
        return folders;
    }


    public Multimap<URI, Annotation> getAnnotations() {
        return annotations;
    }

}
