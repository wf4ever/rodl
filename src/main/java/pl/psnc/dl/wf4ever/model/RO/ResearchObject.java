/**
 * 
 */
package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.exceptions.DuplicateURIException;
import pl.psnc.dl.wf4ever.model.RDF.Thing;

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
    private String roevoPath = ".ro/evo_info.ttl";


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
        return getUri().resolve(roevoPath);
    }

}
