package pl.psnc.dl.wf4ever.rosrs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.auth.ForbiddenException;
import pl.psnc.dl.wf4ever.auth.SecurityFilter;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dl.wf4ever.dlibra.ResourceInfo;
import pl.psnc.dlibra.service.AccessDeniedException;

import com.google.common.collect.Multimap;

public final class ROSRService {

    private final static Logger logger = Logger.getLogger(ROSRService.class);


    /**
     * Aggregate a new internal resource in the research object, upload the resource and add a proxy.
     * 
     * @param researchObject
     *            the research object
     * @param resource
     *            resource URI, must be internal
     * @param researchObjectId
     *            research object ID from the URI
     * @param entity
     *            request entity
     * @param contentType
     *            resource content type
     * @param original
     *            original resource in case of using a format specific URI
     * @return 201 Created with proxy URI
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public static Response aggregateInternalResource(URI researchObject, URI resource, String researchObjectId,
            String entity, String contentType, String original)
            throws AccessDeniedException, DigitalLibraryException, NotFoundException {
        if (original != null) {
            resource = resource.resolve(original);
        }
        String filePath = researchObject.relativize(resource).getPath();
        ResourceInfo resourceInfo = SecurityFilter.DL.get().createOrUpdateFile(Constants.workspaceId, researchObjectId,
            Constants.versionId, filePath, new ByteArrayInputStream(entity.getBytes()),
            contentType != null ? contentType : "text/plain");
        SecurityFilter.SMS.get().addResource(researchObject, resource, resourceInfo);
        // update the manifest that describes the resource in dLibra
        updateNamedGraphInDlibra(researchObjectId, Constants.MANIFEST_PATH, researchObject,
            researchObject.resolve(Constants.MANIFEST_PATH));
        return addProxy(researchObject, resource);
    }


    /**
     * Aggregate a resource in the research object without uploading its content and add a proxy.
     * 
     * @param researchObject
     *            research object
     * @param resource
     *            resource that will be aggregated
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public static Response aggregateExternalResource(URI researchObject, URI resource, String researchObjectId)
            throws AccessDeniedException, DigitalLibraryException, NotFoundException {
        SecurityFilter.SMS.get().addResource(researchObject, resource, null);
        Response response = addProxy(researchObject, resource);
        // update the manifest that describes the resource in dLibra
        updateNamedGraphInDlibra(researchObjectId, Constants.MANIFEST_PATH, researchObject,
            researchObject.resolve(Constants.MANIFEST_PATH));
        return response;
    }


    /**
     * De-aggregate an internal resource, delete its content and proxy.
     * 
     * @param researchObject
     *            research object aggregating the resource
     * @param resource
     *            resource to delete
     * @param researchObjectId
     *            research object ID
     * @param original
     *            original resource in case of using a format specific URI
     * @return 204 No Content response
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public static Response deaggregateInternalResource(URI researchObject, URI resource, String researchObjectId,
            String original)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        if (original != null) {
            resource = resource.resolve(original);
        }
        String filePath = researchObject.relativize(resource).getPath();

        if (researchObject.resolve(Constants.MANIFEST_PATH).equals(resource)) {
            throw new ForbiddenException("Can't delete the manifest");
        }

        SecurityFilter.DL.get().deleteFile(Constants.workspaceId, researchObjectId, Constants.versionId, filePath);
        if (SecurityFilter.SMS.get().isROMetadataNamedGraph(researchObject, resource)) {
            SecurityFilter.SMS.get().removeNamedGraph(researchObject, resource);
        } else {
            SecurityFilter.SMS.get().removeResource(researchObject, resource);
        }
        URI proxy = SecurityFilter.SMS.get().getProxyForResource(researchObject, resource);
        Response response = deleteProxy(researchObject, proxy);
        // update the manifest that describes the resource in dLibra
        updateNamedGraphInDlibra(researchObjectId, Constants.MANIFEST_PATH, researchObject,
            researchObject.resolve(Constants.MANIFEST_PATH));
        return response;
    }


    /**
     * Remove the aggregation of a resource in a research object and its proxy.
     * 
     * @param researchObject
     *            research object that aggregates the resource
     * @param proxy
     *            external resource proxy
     * @param researchObjectId
     *            research object ID
     * @return 204 No Content
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public static Response deaggregateExternalResource(URI researchObject, URI proxy, String researchObjectId)
            throws AccessDeniedException, DigitalLibraryException, NotFoundException {
        URI resource = SecurityFilter.SMS.get().getProxyFor(researchObject, proxy);
        SecurityFilter.SMS.get().removeResource(researchObject, resource);
        // update the manifest that describes the resource in dLibra
        updateNamedGraphInDlibra(researchObjectId, Constants.MANIFEST_PATH, researchObject,
            researchObject.resolve(Constants.MANIFEST_PATH));
        return deleteProxy(researchObject, proxy);
    }


    /**
     * Check if the resource is internal. Resource is internal only if its content has been deployed under the control
     * of the service. A resource that has "internal" URI but the content has not been uploaded is considered external.
     * 
     * @param researchObject
     *            research object that aggregates the resource
     * @param resource
     *            the resource that may be internal
     * @return true if the resource content is deployed under the control of the service, false otherwise
     */
    public static boolean isInternalResource(URI researchObject, URI resource) {
        String filePath = researchObject.relativize(resource).getPath();

        // TODO Auto-generated method stub
        return false;
    }


    /**
     * Add a proxy to the research object.
     * 
     * @param researchObject
     *            the research object
     * @param proxyFor
     *            resource for which the proxy is
     * @return 201 Created response pointing to the proxy
     */
    private static Response addProxy(URI researchObject, URI proxyFor) {
        URI proxy = SecurityFilter.SMS.get().createProxy(researchObject, proxyFor);

        String proxyForHeader = String.format(Constants.LINK_HEADER_TEMPLATE, proxyFor.toString(),
            Constants.ORE_PROXY_FOR_HEADER);
        return Response.created(proxy).header(Constants.LINK_HEADER, proxyForHeader).build();
    }


    /**
     * Delete the proxy. Does not delete the resource that it points to.
     * 
     * @param researchObject
     *            research object that aggregates the proxy
     * @param proxy
     *            proxy to delete
     * @return 204 No Content
     */
    private static Response deleteProxy(URI researchObject, URI proxy) {
        SecurityFilter.SMS.get().deleteProxy(researchObject, proxy);
        return Response.noContent().build();
    }


    /**
     * Update an aggregated resource, which may be a regular file or RO metadata. The resource must be aggregated by the
     * research object but may have no content.
     * 
     * @param researchObject
     *            research object which aggregates the resource
     * @param resource
     *            resource URI
     * @param entity
     *            resource content
     * @param contentType
     *            resource content type
     * @param original
     *            original resource in case of using a format specific URI
     * @return 200 OK response
     */
    public static Response updateInternalResource(URI researchObject, URI resource, String entity, String contentType,
            String original) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * Return the content of an internal resource.
     * 
     * @param researchObject
     *            research object aggregating the resource
     * @param resource
     *            resource URI
     * @param accept
     *            requested MIME type
     * @param original
     *            original resource in case of using a format specific URI
     * @return 200 OK with resource content
     */
    public static Response getInternalResource(URI researchObject, URI resource, String accept, String original) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * An existing aggregated resource is being used as an annotation body now. Add it to the triplestore. If the
     * aggregated resource is external, do nothing.
     * 
     * @param researchObject
     *            research object aggregating the resource
     * @param resource
     *            URI of the resource that is converted
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public static void convertAggregatedResourceToAnnotationBody(URI researchObject, URI resource,
            String researchObjectId)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        String filePath = researchObject.relativize(resource).getPath();
        InputStream data = SecurityFilter.DL.get().getFileContents(Constants.workspaceId, researchObjectId,
            Constants.versionId, filePath);
        RDFFormat format = RDFFormat.forMIMEType(SecurityFilter.DL.get().getFileMimeType(Constants.workspaceId,
            researchObjectId, Constants.versionId, filePath));
        SecurityFilter.SMS.get().addNamedGraph(resource, data, format);
        // update the named graph copy in dLibra, the manifest is not changed
        updateNamedGraphInDlibra(researchObjectId, filePath, researchObject, resource);
        updateROAttributesInDlibra(researchObjectId, researchObject);
    }


    /**
     * An aggregated resource is no longer an annotation body. Remove it from the triplestore. If the annotation body is
     * not inside the triplestore (i.e. it's an external resource), do nothing.
     * 
     * @param researchObject
     *            research object aggregating the resource
     * @param resource
     *            URI of the resource that is converted
     */
    public static void convertAnnotationBodyToAggregatedResource(URI researchObject, URI resource) {
        // TODO Auto-generated method stub

    }


    /**
     * Add and aggregate a new annotation to the research object.
     * 
     * @param researchObject
     *            the research object
     * @param annotation
     *            annotation object, defining the target and body URIs
     * @return 201 Created response
     */
    public static Response addAnnotation(URI researchObject, Annotation annotation) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * Update the annotation targets and the annotation body of an annotation. This method does not add/delete the
     * annotation body to the triplestore.
     * 
     * @param researchObject
     *            the research object
     * @param annotation
     *            annotation object, defining the target and body URIs
     * @return 200 OK response
     */
    public static Response updateAnnotation(URI researchObject, Annotation annotation) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * Get the URI of an annotation body. If acceptHeader is not null, will return a format-specific URI.
     * 
     * @param researchObject
     *            research object in which the annotation is defined
     * @param annotation
     *            the annotation
     * @param acceptHeader
     *            MIME type requested (content negotiation) or null
     * @return URI of the annotation body
     */
    public static URI getAnnotationBody(URI researchObject, URI annotation, String acceptHeader) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * Delete and deaggregate an annotation. Does not delete the annotation body.
     * 
     * @param researchObject
     *            research object in which the annotation is defined
     * @param annotation
     *            the annotation
     * @return 204 No Content
     */
    public static Response deleteAnnotation(URI researchObject, URI annotation) {
        // TODO Auto-generated method stub
        return null;
    }


    private static void updateROAttributesInDlibra(String researchObjectId, URI researchObjectURI) {
        Multimap<URI, Object> roAttributes = SecurityFilter.SMS.get().getAllAttributes(researchObjectURI);
        roAttributes.put(URI.create("Identifier"), researchObjectURI);
        try {
            SecurityFilter.DL.get().storeAttributes(Constants.workspaceId, researchObjectId, Constants.versionId,
                roAttributes);
        } catch (Exception e) {
            logger.error("Caught an exception when updating RO attributes, will continue", e);
        }
    }


    private static void updateNamedGraphInDlibra(String researchObjectId, String filePath, URI researchObjectURI,
            URI namedGraphURI)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        RDFFormat format = RDFFormat.forFileName(filePath, RDFFormat.RDFXML);
        InputStream dataStream = SecurityFilter.SMS.get().getNamedGraphWithRelativeURIs(namedGraphURI,
            researchObjectURI, format);
        SecurityFilter.DL.get().createOrUpdateFile(Constants.workspaceId, researchObjectId, Constants.versionId,
            filePath, dataStream, format.getDefaultMIMEType());
    }

}
