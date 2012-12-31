package pl.psnc.dl.wf4ever.model.ORE;

import java.net.URI;

import org.joda.time.DateTime;

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

    /** Proxy of this resource. */
    protected Proxy proxy;


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
     * @param researchObject
     *            The RO it is aggregated by
     * @param uri
     *            resource URI
     * @param proxyUri
     *            URI of the proxy
     * @param creator
     *            author of the resource
     * @param created
     *            creation date
     */
    public AggregatedResource(ResearchObject researchObject, URI uri, URI proxyUri, URI creator, DateTime created) {
        this.researchObject = researchObject;
        this.uri = uri;
        this.proxy = new Proxy(proxyUri, this, researchObject != null ? researchObject.getUri() : null);
        this.creator = creator;
        this.created = created;
    }


    public Proxy getProxy() {
        return proxy;
    }


    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }


    public ResearchObject getResearchObject() {
        return researchObject;
    }


    public void setResearchObject(ResearchObject researchObject) {
        this.researchObject = researchObject;
        this.proxy.setProxyIn(researchObject != null ? researchObject.getUri() : null);
    }
}
