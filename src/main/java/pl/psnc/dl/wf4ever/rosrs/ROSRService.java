package pl.psnc.dl.wf4ever.rosrs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.auth.SecurityFilter;
import pl.psnc.dl.wf4ever.dlibra.ConflictException;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dl.wf4ever.dlibra.ResourceInfo;
import pl.psnc.dlibra.service.AccessDeniedException;

import com.google.common.collect.Multimap;
import com.sun.jersey.core.header.ContentDisposition;

public final class ROSRService {

    private final static Logger logger = Logger.getLogger(ROSRService.class);


    /**
     * Create a new research object.
     * 
     * @param researchObjectURI
     *            the research object URI
     * @param researchObjectId
     *            research object id
     * @return research object URI
     * @throws ConflictException
     *             the research object id is already used
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     */
    public static URI createResearchObject(URI researchObjectURI, String researchObjectId)
            throws ConflictException, DigitalLibraryException, NotFoundException {

        InputStream manifest;
        try {
            SecurityFilter.SMS.get().createResearchObject(researchObjectURI);
            manifest = SecurityFilter.SMS.get().getManifest(researchObjectURI.resolve(Constants.MANIFEST_PATH),
                RDFFormat.RDFXML);
        } catch (IllegalArgumentException e) {
            // RO already existed in sms, maybe created by someone else
            throw new ConflictException("The RO with identifier " + researchObjectId + " already exists");
        }

        try {
            SecurityFilter.DL.get().createWorkspace(Constants.workspaceId);
        } catch (ConflictException e) {
            // nothing
        }
        try {
            SecurityFilter.DL.get().createResearchObject(Constants.workspaceId, researchObjectId);
        } catch (ConflictException e) {
            // nothing
        }
        SecurityFilter.DL.get().createVersion(Constants.workspaceId, researchObjectId, Constants.versionId, manifest,
            Constants.MANIFEST_PATH, RDFFormat.RDFXML.getDefaultMIMEType());
        SecurityFilter.DL.get().publishVersion(Constants.workspaceId, researchObjectId, Constants.versionId);
        return researchObjectURI;
    }


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
    public static Response deaggregateInternalResource(URI researchObject, URI resource, String researchObjectId)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        String filePath = researchObject.relativize(resource).getPath();
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
     * @param researchObjectId
     *            research object ID
     * @return true if the resource content is deployed under the control of the service, false otherwise
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     */
    public static boolean isInternalResource(URI researchObject, URI resource, String researchObjectId)
            throws NotFoundException, DigitalLibraryException {
        String filePath = researchObject.relativize(resource).getPath();
        return SecurityFilter.DL.get().fileExists(Constants.workspaceId, researchObjectId, Constants.versionId,
            filePath);
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
        URI proxy = SecurityFilter.SMS.get().addProxy(researchObject, proxyFor);

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
     * @return 200 OK response
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public static Response updateInternalResource(URI researchObject, URI resource, String researchObjectId,
            String entity, String contentType)
            throws AccessDeniedException, DigitalLibraryException, NotFoundException {
        String filePath = researchObject.relativize(resource).getPath();
        if (SecurityFilter.SMS.get().isROMetadataNamedGraph(researchObject, resource)) {
            RDFFormat format = RDFFormat.forMIMEType(contentType);
            if (format == null) {
                format = RDFFormat.forFileName(resource.getPath(), RDFFormat.RDFXML);
            }
            SecurityFilter.SMS.get().addNamedGraph(resource, new ByteArrayInputStream(entity.getBytes()), format);
            // update the named graph copy in dLibra, the manifest is not changed
            updateNamedGraphInDlibra(researchObjectId, filePath, researchObject, resource);
            updateROAttributesInDlibra(researchObjectId, researchObject);
        } else {
            ResourceInfo resourceInfo = SecurityFilter.DL.get().createOrUpdateFile(Constants.workspaceId,
                researchObjectId, Constants.versionId, filePath, new ByteArrayInputStream(entity.getBytes()),
                contentType != null ? contentType : "text/plain");
            SecurityFilter.SMS.get().addResource(researchObject, resource, resourceInfo);
            // update the manifest that describes the resource in dLibra
            updateNamedGraphInDlibra(researchObjectId, Constants.MANIFEST_PATH, researchObject,
                researchObject.resolve(Constants.MANIFEST_PATH));
        }
        return Response.ok().build();
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
     * @return 200 OK with resource content
     * @throws NotFoundException
     * @throws DigitalLibraryException
     */
    public static Response getInternalResource(URI researchObject, URI resource, String researchObjectId,
            String accept, String original)
            throws DigitalLibraryException, NotFoundException {
        String filePath = researchObject.relativize(resource).getPath();

        // check if request is for a specific format
        if (original != null) {
            URI originalResource = resource.resolve(original);
            if (SecurityFilter.SMS.get().containsNamedGraph(originalResource)
                    && SecurityFilter.SMS.get().isROMetadataNamedGraph(researchObject, originalResource)) {
                RDFFormat format = RDFFormat.forMIMEType(accept);
                if (format == null) {
                    format = RDFFormat.forFileName(resource.getPath(), RDFFormat.RDFXML);
                }
                InputStream graph = SecurityFilter.SMS.get().getNamedGraph(originalResource, format);
                ContentDisposition cd = ContentDisposition.type(format.getDefaultMIMEType())
                        .fileName(getFilename(resource)).build();
                return Response.ok(graph).header("Content-disposition", cd).build();
            } else {
                return Response.status(Status.NOT_FOUND).type("text/plain").entity("Original resource not found")
                        .build();
            }
        }

        if (SecurityFilter.SMS.get().containsNamedGraph(resource)
                && SecurityFilter.SMS.get().isROMetadataNamedGraph(researchObject, resource)) {
            RDFFormat acceptFormat = RDFFormat.forMIMEType(accept);
            RDFFormat extensionFormat = RDFFormat.forFileName(resource.getPath());
            if (extensionFormat != null && (acceptFormat == null || extensionFormat == acceptFormat)) {
                // 1. GET manifest.rdf Accept: application/rdf+xml
                // 2. GET manifest.rdf
                InputStream graph = SecurityFilter.SMS.get().getNamedGraph(resource, extensionFormat);
                ContentDisposition cd = ContentDisposition.type(extensionFormat.getDefaultMIMEType())
                        .fileName(getFilename(resource)).build();
                return Response.ok(graph).header("Content-disposition", cd).build();
            }
            // 3. GET manifest.rdf Accept: text/turtle
            // 4. GET manifest Accept: application/rdf+xml
            // 5. GET manifest
            URI formatSpecificURI = createFormatSpecificURI(resource, extensionFormat,
                (acceptFormat != null ? acceptFormat : RDFFormat.RDFXML));
            return Response.temporaryRedirect(formatSpecificURI).build();
        }

        String mimeType = SecurityFilter.DL.get().getFileMimeType(Constants.workspaceId, researchObjectId,
            Constants.versionId, filePath);
        ContentDisposition cd = ContentDisposition.type(mimeType).fileName(getFilename(resource)).build();
        InputStream body = SecurityFilter.DL.get().getFileContents(Constants.workspaceId, researchObjectId,
            Constants.versionId, filePath);
        return Response.ok(body).header("Content-disposition", cd).header("Content-type", mimeType).build();
    }


    private static String getFilename(URI uri) {
        return uri.resolve(".").relativize(uri).toString();
    }


    private static URI createFormatSpecificURI(URI absolutePath, RDFFormat extensionFormat, RDFFormat newFormat) {
        String path = absolutePath.getPath().toString();
        if (extensionFormat != null) {
            for (String extension : extensionFormat.getFileExtensions()) {
                if (path.endsWith(extension)) {
                    path = path.substring(0, path.length() - extension.length() - 1);
                    break;
                }
            }
        }
        path = path.concat(".").concat(newFormat.getDefaultFileExtension());
        try {
            return new URI(absolutePath.getScheme(), absolutePath.getAuthority(), path,
                    "original=".concat(getFilename(absolutePath)), null);
        } catch (URISyntaxException e) {
            logger.error("Can't create a format-specific URI", e);
            return null;
        }
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
    public static void convertAnnotationBodyToAggregatedResource(URI researchObject, URI resource,
            String researchObjectId) {
        SecurityFilter.SMS.get().removeNamedGraph(researchObject, resource);
        updateROAttributesInDlibra(researchObjectId, researchObject);
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
    public static Response addAnnotation(URI researchObject, Annotation annotationData) {
        URI annotation = SecurityFilter.SMS.get().addAnnotation(researchObject, annotationData.getAnnotationTargets(),
            annotationData.getAnnotationBody());

        String annotationBodyHeader = String.format(Constants.LINK_HEADER_TEMPLATE, annotationData.getAnnotationBody()
                .toString(), Constants.AO_ANNOTATION_BODY_HEADER);
        ResponseBuilder response = Response.created(annotation).header(Constants.LINK_HEADER, annotationBodyHeader);
        for (URI target : annotationData.getAnnotationTargets()) {
            String targetHeader = String.format(Constants.LINK_HEADER_TEMPLATE, target.toString(),
                Constants.AO_ANNOTATES_RESOURCE_HEADER);
            response = response.header(Constants.LINK_HEADER, targetHeader);
        }
        return response.build();
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
    public static Response updateAnnotation(URI researchObject, URI annotation, Annotation annotationData) {
        SecurityFilter.SMS.get().updateAnnotation(researchObject, annotation, annotationData.getAnnotationTargets(),
            annotationData.getAnnotationBody());

        String annotationBodyHeader = String.format(Constants.LINK_HEADER_TEMPLATE, annotationData.getAnnotationBody()
                .toString(), Constants.AO_ANNOTATION_BODY_HEADER);
        ResponseBuilder response = Response.ok().header(Constants.LINK_HEADER, annotationBodyHeader);
        for (URI target : annotationData.getAnnotationTargets()) {
            String targetHeader = String.format(Constants.LINK_HEADER_TEMPLATE, target.toString(),
                Constants.AO_ANNOTATES_RESOURCE_HEADER);
            response = response.header(Constants.LINK_HEADER, targetHeader);
        }
        return response.build();
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
        URI body = SecurityFilter.SMS.get().getAnnotationBody(researchObject, annotation);
        RDFFormat acceptFormat = RDFFormat.forMIMEType(acceptHeader);
        if (acceptFormat != null) {
            RDFFormat extensionFormat = RDFFormat.forFileName(annotation.getPath());
            return createFormatSpecificURI(body, extensionFormat, acceptFormat);
        } else {
            return body;
        }
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
        SecurityFilter.SMS.get().deleteAnnotation(researchObject, annotation);
        return Response.noContent().build();
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
