package pl.psnc.dl.wf4ever.model.ORE;

import java.net.URI;

import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

/**
 * Simple Aggregated Resource model.
 * 
 * @author pejot
 * 
 */
public class AggregatedResource extends Thing {

    /** RO it is aggregated in. */
    protected ResearchObject researchObject;

    /** URI of a proxy of this resource. */
    protected URI proxyUri;


    /**
     * Constructor.
     */
    public AggregatedResource() {

    }


    /**
     * Constructor.
     */
    public AggregatedResource(URI uri, ResearchObject researchObject) {
        super(uri);
        this.researchObject = researchObject;
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
        serialize(researchObject);
    }


    /**
     * Constructor.
     * 
     * @param uri
     *            URI
     */
    public AggregatedResource(URI uri) {
        super(uri);
    }


    public URI getProxyUri() {
        return proxyUri;
    }


    public void setProxyUri(URI proxyUri) {
        this.proxyUri = proxyUri;
    }


    public ResearchObject getResearchObject() {
        return researchObject;
    }


    public void setResearchObject(ResearchObject researchObject) {
        this.researchObject = researchObject;
    }
}
