/**
 * 
 */
package pl.psnc.dl.wf4ever.common;

import java.net.URI;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

/**
 * A DAO for a research object.
 * 
 * @author piotrekhol
 * 
 */
public class ResearchObject {

    /** logger. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(ResearchObject.class);

    /** Manifest path. */
    public static final String MANIFEST_PATH = ".ro/manifest.rdf";

    /** RO URI. */
    private URI uri;

    /** Fixed roevo annotation file path. */
    private String roevoPath = ".ro/evo_info.ttl";


    /**
     * Constructor.
     */
    private ResearchObject() {

    }


    /**
     * Constructor.
     * 
     * @param uri
     *            RO URI
     */
    public ResearchObject(URI uri) {
        this();
        setUri(uri.normalize());
    }


    /**
     * Load from database or create a new instance.
     * 
     * @param uri
     *            RO URI
     * @return an instance
     */
    public static ResearchObject create(URI uri) {
        return new ResearchObject(uri);
    }


    @Id
    @Column(name = "uri")
    public String getUriString() {
        return uri.toString();
    }


    /**
     * Set URI and id.
     * 
     * @param uriString
     *            RO URI as String
     */
    public void setUriString(String uriString) {
        setUri(URI.create(uriString));
    }


    @Transient
    public URI getUri() {
        return uri;
    }


    public void setUri(URI uri) {
        this.uri = uri;
    }


    @Transient
    public URI getManifestUri() {
        return uri != null ? uri.resolve(MANIFEST_PATH) : null;
    }


    /**
     * Get the manifest format.
     * 
     * @return manifest format
     */
    @Transient
    public String getManifestFormat() {
        return "RDF/XML";
    }


    /**
     * Get the manifest format.
     * 
     * @return manifest format
     */
    @Transient
    public URI getFixedEvolutionAnnotationBodyUri() {
        return getUri().resolve(roevoPath);
    }
}
