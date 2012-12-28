/**
 * 
 */
package pl.psnc.dl.wf4ever.model.RO;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.common.EvoType;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;

import com.google.common.collect.Multimap;

/**
 * A research object, live by default.
 * 
 * @author piotrekhol
 * 
 */
public class ResearchObject extends Thing {

    /** logger. */
    @SuppressWarnings("unused")
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

    /** creator URI. */
    private URI creator;

    /** creation date. */
    private DateTime created;


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


    public AggregatedResource getEvoInfoBody() {
        return new AggregatedResource(getFixedEvolutionAnnotationBodyUri(), uri);
    }


    public Manifest getManifest() {
        return new Manifest(creator, this);
    }


    /**
     * Get a Research Object.
     * 
     * @param uri
     *            uri
     * @return an existing Research Object or null
     */
    public static ResearchObject get(URI uri) {
        throw new UnsupportedOperationException("Not implemented yet");
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
     */
    public ResearchObject load() {
        throw new UnsupportedOperationException("Not implemented yet");
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


    public URI getCreator() {
        return creator;
    }


    public DateTime getCreated() {
        return created;
    }

}
