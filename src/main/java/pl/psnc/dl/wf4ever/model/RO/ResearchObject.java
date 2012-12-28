/**
 * 
 */
package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.exceptions.DuplicateURIException;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.RDF.Thing;

import com.google.common.collect.Multimap;

/**
 * A DAO for a research object.
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
    public ResearchObject(URI uri) {
        super(uri);
    }


    /**
     * Create new Research Object.
     * 
     * @param uri
     *            RO URI
     * @return an instance
     * @throws DuplicateURIException
     *             in case the URI is already being used
     */
    public static ResearchObject create(URI uri)
            throws DuplicateURIException {
        return new ResearchObject(uri);
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
