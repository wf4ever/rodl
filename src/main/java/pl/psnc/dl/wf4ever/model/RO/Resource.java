package pl.psnc.dl.wf4ever.model.RO;

import java.io.InputStream;
import java.net.URI;

import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.EvoBuilder;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.ORE.Proxy;

import com.hp.hpl.jena.query.Dataset;

/**
 * ro:Resource.
 * 
 * @author piotrekhol
 * @author pejot
 */
public class Resource extends AggregatedResource {

    /**
     * Constructor.
     * 
     * @param user
     *            user creating the instance
     * @param dataset
     *            custom dataset
     * @param useTransactions
     *            should transactions be used. Note that not using transactions on a dataset which already uses
     *            transactions may make it unreadable.
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


    /**
     * Create and save a new ro:Resource.
     * 
     * @param builder
     *            model instance builder
     * @param researchObject
     *            research object that aggregates the resource
     * @param resourceUri
     *            the URI
     * @return the new resource
     */
    public static Resource create(Builder builder, ResearchObject researchObject, URI resourceUri) {
        if (researchObject.isUriUsed(resourceUri)) {
            throw new ConflictException("Resource already exists: " + resourceUri);
        }
        Resource resource = builder.buildResource(researchObject, resourceUri, builder.getUser(), DateTime.now());
        resource.setProxy(Proxy.create(builder, researchObject, resource));
        resource.save();
        return resource;
    }


    /**
     * Create and save a new ro:Resource.
     * 
     * @param builder
     *            model instance builder
     * @param researchObject
     *            research object that aggregates the resource
     * @param resourceUri
     *            the URI
     * @param content
     *            the resource content
     * @param contentType
     *            the content MIME type
     * @return the new resource
     * @throws BadRequestException
     *             if it is expected to be an RDF file and isn't
     */
    public static Resource create(Builder builder, ResearchObject researchObject, URI resourceUri, InputStream content,
            String contentType)
            throws BadRequestException {
        if (researchObject.isUriUsed(resourceUri)) {
            throw new ConflictException("Resource already exists: " + resourceUri);
        }
        Resource resource = builder.buildResource(researchObject, resourceUri, builder.getUser(), DateTime.now());
        resource.setProxy(Proxy.create(builder, researchObject, resource));
        resource.save(content, contentType);
        return resource;
    }


    /**
     * Create a new resource with all data except for the URI equal to another resource.
     * 
     * @param builder
     *            model instance builder
     * @param evoBuilder
     *            builder of evolution properties
     * @param researchObject
     *            research object that aggregates the resource
     * @return the new resource
     * @throws BadRequestException
     *             if it is expected to be an RDF file and isn't
     */
    public Resource copy(Builder builder, EvoBuilder evoBuilder, ResearchObject researchObject)
            throws BadRequestException {
        URI resourceUri = researchObject.getUri().resolve(getPath());
        if (researchObject.isUriUsed(resourceUri)) {
            throw new ConflictException("Resource already exists: " + resourceUri);
        }
        Resource resource2 = builder.buildResource(researchObject, resourceUri, getCreator(), getCreated());
        evoBuilder.setFrozenAt(resource2, DateTime.now());
        evoBuilder.setFrozenBy(resource2, builder.getUser());
        resource2.setProxy(Proxy.create(builder, researchObject, resource2));
        if (isInternal()) {
            resource2.save(getSerialization(), getStats().getMimeType());
        } else {
            resource2.save();
        }
        return resource2;
    }


    @Override
    public void save() {
        super.save();
        researchObject.getManifest().saveRoResourceClass(this);
        researchObject.getManifest().saveRoStats(this);
    }


    @Override
    public void delete() {
        getResearchObject().getResources().remove(uri);
        super.delete();
    }


    @Override
    public void saveGraphAndSerialize()
            throws BadRequestException {
        super.saveGraphAndSerialize();
        //FIXME the resource is still of class Resource, not AggregatedResource
        getResearchObject().getManifest().removeRoResourceClass(this);
        getResearchObject().getResources().remove(uri);
    }

}
