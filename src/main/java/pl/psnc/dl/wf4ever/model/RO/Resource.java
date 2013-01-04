package pl.psnc.dl.wf4ever.model.RO;

import java.io.InputStream;
import java.net.URI;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.common.Builder;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.ORE.Proxy;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;

import com.hp.hpl.jena.query.Dataset;

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
     */
    public Resource(UserMetadata user, Dataset dataset, boolean useTransactions, ResearchObject researchObject, URI uri) {
        super(user, dataset, useTransactions, researchObject, uri);
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
     */
    public Resource(UserMetadata user, ResearchObject researchObject, URI uri) {
        super(user, researchObject, uri);
    }


    public static Resource create(Builder builder, ResearchObject researchObject, URI resourceUri)
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        Resource resource = builder.buildResource(researchObject, resourceUri, builder.getUser().getUri(),
            DateTime.now());
        resource.setProxy(Proxy.create(builder, researchObject, resource));
        resource.save();
        return resource;
    }


    public static Resource create(Builder builder, ResearchObject researchObject, URI resourceUri, InputStream content,
            String contentType)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException, ConflictException {
        //TODO check for conflict
        Resource resource = builder.buildResource(researchObject, resourceUri, builder.getUser().getUri(),
            DateTime.now());
        resource.setProxy(Proxy.create(builder, researchObject, resource));
        resource.save(content, contentType);
        return resource;
    }


    public static Resource get(Builder builder, ResearchObject researchObject, URI uri, URI creator, DateTime created) {
        Resource resource = builder.buildResource(researchObject, uri, creator, created);
        return resource;
    }


    public void save(InputStream content, String contentType)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException, ConflictException {
        String path = researchObject.getUri().relativize(uri).getPath();
        setStats(ROSRService.DL.get().createOrUpdateFile(researchObject.getUri(), path, content,
            contentType != null ? contentType : "text/plain"));
        save();
    }


    @Override
    public void save()
            throws ConflictException, DigitalLibraryException, AccessDeniedException, NotFoundException {
        super.save();
        researchObject.getManifest().saveRoResourceClass(this);
        researchObject.getManifest().saveRoStats(this);
    }


    public ResourceMetadata getStats() {
        return stats;
    }


    public void setStats(ResourceMetadata stats) {
        this.stats = stats;
    }

}
