package pl.psnc.dl.wf4ever.model.RO;

import java.net.URI;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.model.Builder;

/**
 * A factory that builds resources.
 * 
 * @author piotrekhol
 * 
 */
public class ResourceFactory {

    /** Model builder. */
    private final Builder builder;


    /**
     * Constructor.
     * 
     * @param builder
     *            model builder
     */
    public ResourceFactory(Builder builder) {
        this.builder = builder;
    }


    /**
     * Build a new resource, choosing an appropriate class.
     * 
     * @param uri
     *            resource URI
     * @param researchObject
     *            research object aggregating the resource
     * @param creator
     *            author
     * @param created
     *            creation date
     * @return a new resource
     */
    public Resource buildResource(URI uri, ResearchObject researchObject, UserMetadata creator, DateTime created) {
        Resource resource = builder.buildResource(uri, researchObject, creator, created);
        if (resource.getStats() != null) {
            return buildResource(uri, researchObject, creator, created, resource.getStats().getMimeType());
        } else {
            return resource;
        }
    }


    /**
     * Build a new resource, choosing an appropriate class based on the MIME type.
     * 
     * @param uri
     *            resource URI
     * @param researchObject
     *            research object aggregating the resource
     * @param creator
     *            author
     * @param created
     *            creation date
     * @param contentType
     *            MIME type
     * @return a new resource
     */
    public Resource buildResource(URI uri, ResearchObject researchObject, UserMetadata creator, DateTime created,
            String contentType) {
        if (contentType != null && contentType.equals(RoBundle.MIME_TYPE)) {
            return builder.buildRoBundle(uri, researchObject, creator, created);
        } else {
            return builder.buildResource(uri, researchObject, creator, created);
        }
    }

}
