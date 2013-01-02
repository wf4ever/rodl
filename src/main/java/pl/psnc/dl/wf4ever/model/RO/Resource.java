package pl.psnc.dl.wf4ever.model.RO;

import java.io.InputStream;
import java.net.URI;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.ORE.Proxy;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;

/**
 * ro:Resource.
 * 
 * @author piotrekhol
 * @author pejot
 */
public class Resource extends AggregatedResource {

    /** physical representation metadata. */
    private ResourceMetadata stats;


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
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
    public Resource(UserMetadata user, ResearchObject researchObject, URI uri, URI proxyUri, URI creator,
            DateTime created) {
        super(user, researchObject, uri, proxyUri, creator, created);
    }


    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     * @param researchObject
     *            The RO it is aggregated by
     * @param uri
     *            resource URI
     * @param proxyURI
     *            URI of the proxy
     * @param creator
     *            author of the resource
     * @param created
     *            creation date
     * @param stats
     *            physical statistics (size, checksum, etc)
     */
    public Resource(UserMetadata user, ResearchObject researchObject, URI uri, URI proxyURI, URI creator,
            DateTime created, ResourceMetadata stats) {
        this(user, researchObject, uri, proxyURI, creator, created);
        this.stats = stats;
    }


    public static Resource create(UserMetadata user, ResearchObject researchObject, URI resourceUri) {
        Resource resource = new Resource(user, researchObject, resourceUri, null, user.getUri(), DateTime.now());
        resource.setCreator(user.getUri());
        resource.setCreated(DateTime.now());
        resource.setProxy(Proxy.create(user, researchObject, resource));
        return resource;
    }


    public void save(InputStream content, String contentType)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException, ConflictException {
        save();
        String path = researchObject.getUri().relativize(uri).getPath();
        setStats(ROSRService.DL.get().createOrUpdateFile(researchObject.getUri(), path, content,
            contentType != null ? contentType : "text/plain"));
        Resource resource = ROSRService.SMS.get().addResource(researchObject, uri, stats);
    }


    public ResourceMetadata getStats() {
        return stats;
    }


    public void setStats(ResourceMetadata stats) {
        this.stats = stats;
    }

}
