package pl.psnc.dl.wf4ever.rosrs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.DigitalLibrary;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.Folder;
import pl.psnc.dl.wf4ever.model.RO.FolderEntry;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
import pl.psnc.dl.wf4ever.vocabulary.AO;

import com.google.common.collect.Multimap;

/**
 * Utility class for distributing the RO tasks to dLibra and SMS.
 * 
 * @author piotrekhol
 * 
 */
public final class ROSRService {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(ROSRService.class);

    /** Thread local DL instance. */
    public static final ThreadLocal<DigitalLibrary> DL = new ThreadLocal<>();

    /** Thread local SMS instance. */
    public static final ThreadLocal<SemanticMetadataService> SMS = new ThreadLocal<>();


    /**
     * Private constructor.
     */
    private ROSRService() {
        //nope
    }


    /**
<<<<<<< HEAD
     * Create a new research object.
     * 
     * @param researchObject
     *            the research object
     * @return research object URI
     * @throws ConflictException
     *             the research object id is already used
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             no permissions
     */
    public static URI createResearchObject(ResearchObject researchObject)
            throws ConflictException, DigitalLibraryException, NotFoundException, AccessDeniedException {
        return createResearchObject(researchObject, null, null);
    }


    /**
     * Create a new research object of a specific ROEVO type.
     * 
     * @param researchObject
     *            the research object URI
     * @param type
     *            ROEVO type, may be null
     * @param sourceRO
     *            live RO for snapshots and archives, any URI for live ROs, may be null if type is null
     * @return research object URI
     * @throws ConflictException
     *             the research object id is already used
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             no permissions
     */
    public static URI createResearchObject(ResearchObject researchObject, EvoType type, ResearchObject sourceRO)
            throws ConflictException, DigitalLibraryException, NotFoundException, AccessDeniedException {
        InputStream manifest;
        try {
            if (type == null) {
                type = EvoType.LIVE;
            }
            switch (type) {
                default:
                case LIVE:
                    ROSRService.SMS.get().createLiveResearchObject(researchObject, sourceRO);
                    break;
                case SNAPSHOT:
                    ROSRService.SMS.get().createResearchObjectCopy(researchObject, sourceRO);
                    break;
                case ARCHIVE:
                    ROSRService.SMS.get().createResearchObjectCopy(researchObject, sourceRO);
                    break;
            }

            manifest = ROSRService.SMS.get().getManifest(researchObject, RDFFormat.RDFXML);
        } catch (IllegalArgumentException e) {
            // RO already existed in sms, maybe created by someone else
            throw new ConflictException("The RO with URI " + researchObject.getUri() + " already exists");
        }

        LOGGER.debug(String.format("%s\t\tcreate RO start", new DateTime().toString()));
        ROSRService.DL.get().createResearchObject(researchObject.getUri(), manifest, ResearchObject.MANIFEST_PATH,
            RDFFormat.RDFXML.getDefaultMIMEType());
        if (type == EvoType.LIVE) {
            generateEvoInfo(researchObject, null, EvoType.LIVE);
        }
        LOGGER.debug(String.format("%s\t\tcreate RO end", new DateTime().toString()));
        return researchObject.getUri();
    }


    /**
     * Delete a research object.
     * 
     * @param researchObject
     *            research object URI
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws NotFoundException
     *             Research Object not found neither in dLibra nor in SMS
     */
    public static void deleteResearchObject(ResearchObject researchObject)
            throws DigitalLibraryException, NotFoundException {
        try {
            ROSRService.DL.get().deleteResearchObject(researchObject.getUri());
        } finally {
            try {
                ROSRService.SMS.get().removeResearchObject(researchObject);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("URI not found in SMS: " + researchObject.getUri());
            }
        }
    }


    /**
     * Aggregate a new internal resource in the research object, upload the resource and add a proxy.
     * 
     * @param researchObject
     *            the research object
     * @param resourceUri
     *            resource URI, must be internal
     * @param entity
     *            request entity
     * @param contentType
     *            resource content type
     * @param original
     *            original resource in case of using a format specific URI
     * @param syntax
     *            response RDF format, default is RDF/XML
     * @return 201 Created with proxy URI
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public static Resource aggregateInternalResource(ResearchObject researchObject, URI resourceUri,
            InputStream entity, String contentType, String original)
            throws AccessDeniedException, DigitalLibraryException, NotFoundException {
        if (original != null) {
            resourceUri = resourceUri.resolve(original);
        }
        String filePath = researchObject.getUri().relativize(resourceUri).getPath();
        ResourceMetadata resourceInfo = ROSRService.DL.get().createOrUpdateFile(researchObject.getUri(), filePath,
            entity, contentType != null ? contentType : "text/plain");
        Resource resource = ROSRService.SMS.get().addResource(researchObject, resourceUri, resourceInfo);
        // update the manifest that describes the resource in dLibra
        updateNamedGraphInDlibra(ResearchObject.MANIFEST_PATH, researchObject, researchObject.getManifestUri());
        URI proxy = ROSRService.SMS.get().addProxy(researchObject, resourceUri);
        resource.setProxyUri(proxy);
        return resource;
    }


    /**
     * Aggregate a resource in the research object without uploading its content and add a proxy.
     * 
     * @param researchObject
     *            research object
     * @param resource
     *            resource that will be aggregated
     * @param syntax
     *            format in which the response should be returned
     * @return 201 Created response pointing to the proxy
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public static ResponseBuilder aggregateExternalResource(ResearchObject researchObject, URI resource,
            RDFFormat syntax)
            throws AccessDeniedException, DigitalLibraryException, NotFoundException {
        ROSRService.SMS.get().addResource(researchObject, resource, null);
        ResponseBuilder builder = addProxy(researchObject, resource, syntax);
        // update the manifest that describes the resource in dLibra
        updateNamedGraphInDlibra(ResearchObject.MANIFEST_PATH, researchObject, researchObject.getManifestUri());
        return builder;
    }


    /**
=======
>>>>>>> refactor
     * De-aggregate an internal resource, delete its content and proxy.
     * 
     * @param researchObject
     *            research object aggregating the resource
     * @param resource
     *            resource to delete
     * @return 204 No Content response
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public static Response deaggregateInternalResource(ResearchObject researchObject, URI resource)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        String filePath = researchObject.getUri().relativize(resource).getPath();
        ROSRService.DL.get().deleteFile(researchObject.getUri(), filePath);
        if (ROSRService.SMS.get().isROMetadataNamedGraph(researchObject, resource)) {
            ROSRService.SMS.get().removeAnnotationBody(researchObject, resource);
        } else {
            ROSRService.SMS.get().removeResource(researchObject, resource);
        }
        // update the manifest that describes the resource in dLibra
        researchObject.getManifest().serialize();
        return Response.noContent().build();
    }


    /**
     * Remove the aggregation of a resource in a research object and its proxy.
     * 
     * @param researchObject
     *            research object that aggregates the resource
     * @param proxy
     *            external resource proxy
     * @return 204 No Content
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public static Response deaggregateExternalResource(ResearchObject researchObject, URI proxy)
            throws AccessDeniedException, DigitalLibraryException, NotFoundException {
        URI resource = ROSRService.SMS.get().getProxyFor(researchObject, proxy);
        ROSRService.SMS.get().removeResource(researchObject, resource);
        // update the manifest that describes the resource in dLibra
        researchObject.getManifest().serialize();
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
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     */
    public static boolean isInternalResource(ResearchObject researchObject, URI resource)
            throws NotFoundException, DigitalLibraryException {
        String filePath = researchObject.getUri().relativize(resource).getPath();
        return !filePath.isEmpty() && ROSRService.DL.get().fileExists(researchObject.getUri(), filePath);
    }


    /**
     * Get The evolution Information.
     * 
     * @param researchObject
     *            the RO URI
     * @return InputStream with evolution information written in TTL format
     **/
    public static InputStream getEvoInfo(ResearchObject researchObject) {
        ROSRService.SMS.get();
        return null;
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
    private static Response deleteProxy(ResearchObject researchObject, URI proxy) {
        ROSRService.SMS.get().deleteProxy(researchObject, proxy);
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
     * @return 200 OK response or 201 Created if there had been no content
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public static ResponseBuilder updateInternalResource(ResearchObject researchObject, URI resource, String entity,
            String contentType)
            throws AccessDeniedException, DigitalLibraryException, NotFoundException {
        String filePath = researchObject.getUri().relativize(resource).getPath();
        boolean contentExisted = ROSRService.DL.get().fileExists(researchObject.getUri(), filePath);
        if (ROSRService.SMS.get().isROMetadataNamedGraph(researchObject, resource)) {
            RDFFormat format = RDFFormat.forMIMEType(contentType);
            if (format == null) {
                format = RDFFormat.forFileName(resource.getPath(), RDFFormat.RDFXML);
            }
            ROSRService.SMS.get().addAnnotationBody(researchObject, resource,
                new ByteArrayInputStream(entity.getBytes()), format);
            // update the named graph copy in dLibra, the manifest is not changed
            researchObject.getResources().get(resource).serialize();
            updateROAttributesInDlibra(researchObject);
        } else {
            ResourceMetadata resourceInfo = ROSRService.DL.get().createOrUpdateFile(researchObject.getUri(), filePath,
                new ByteArrayInputStream(entity.getBytes()), contentType != null ? contentType : "text/plain");
            ROSRService.SMS.get().addResource(researchObject, resource, resourceInfo);
            // update the manifest that describes the resource in dLibra
            researchObject.getManifest().serialize();
        }
        if (contentExisted) {
            return Response.ok();
        } else {
            return Response.created(resource);
        }
    }


    /**
     * Get resource information.
     * 
     * @param researchObject
     *            RO URI
     * @param resource
     *            resource URI
     * @param original
     *            original resource name in case of annotation bodies
     * @return resource info
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public static ResourceMetadata getResourceInfo(ResearchObject researchObject, URI resource, String original)
            throws AccessDeniedException, NotFoundException, DigitalLibraryException {
        String filePath = researchObject.getUri().relativize(original != null ? resource.resolve(original) : resource)
                .getPath();
        return ROSRService.DL.get().getFileInfo(researchObject.getUri(), filePath);
    }


    /**
     * Take out the resource file name from a URI.
     * 
     * @param uri
     *            the URI
     * @return the last segment of the path
     */
    private static String getFilename(URI uri) {
        return uri.resolve(".").relativize(uri).toString();
    }


    /**
     * Create a URI pointing to a specific format of an RDF resource.
     * 
     * @param absolutePath
     *            resource path
     * @param extensionFormat
     *            original RDF format
     * @param newFormat
     *            the requested RDF format
     * @return URI pointing to the serialization of the resource in the requested RDF format
     */
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
            LOGGER.error("Can't create a format-specific URI", e);
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
     * @return
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public static AggregatedResource convertRoResourceToAnnotationBody(ResearchObject researchObject,
            AggregatedResource resource)
            throws DigitalLibraryException, AccessDeniedException {
        String filePath = researchObject.getUri().relativize(resource.getUri()).getPath();
        try {
            InputStream data = ROSRService.DL.get().getFileContents(researchObject.getUri(), filePath);
            if (data != null) {
                RDFFormat format = RDFFormat.forMIMEType(ROSRService.DL.get()
                        .getFileInfo(researchObject.getUri(), filePath).getMimeType());
                SMS.get().removeResource(researchObject, resource.getUri());
                AggregatedResource res = SMS.get().addAnnotationBody(researchObject, resource.getUri(), data, format);
                res.setProxy(SMS.get().addProxy(researchObject, resource));
                // update the named graph copy in dLibra, the manifest is not changed
                resource.serialize();
                updateROAttributesInDlibra(researchObject);
                return res;
            } else {
                return null;
            }
        } catch (NotFoundException e) {
            LOGGER.debug("Could not find an aggregated resource, must be external: " + e.getMessage());
            return null;
        }
    }


    /**
     * An aggregated resource is no longer an annotation body. Remove it from the triplestore. If the annotation body is
     * not inside the triplestore (i.e. it's an external resource), do nothing.
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
    private static void convertAnnotationBodyToAggregatedResource(ResearchObject researchObject, URI resource)
            throws NotFoundException, DigitalLibraryException, AccessDeniedException {
        if (ROSRService.SMS.get().containsNamedGraph(resource)
                && !SMS.get().isROMetadataNamedGraph(researchObject, resource)) {
            ResourceMetadata info = DL.get().getFileInfo(researchObject.getUri(),
                researchObject.getUri().relativize(resource).toString());
            ROSRService.SMS.get().removeAnnotationBody(researchObject, resource);
            ROSRService.SMS.get().addResource(researchObject, resource, info);
            updateROAttributesInDlibra(researchObject);
        }
    }


    /**
     * Update the annotation targets and the annotation body of an annotation. This method does not add/delete the
     * annotation body to the triplestore.
     * 
     * @param researchObject
     *            the research object
     * @param annotation
     *            annotation URI
     * @param annotationBody
     *            annotation body URI
     * @param annotationTargets
     *            list of annotated resources URIs
     * @return 200 OK response
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public static Response updateAnnotation(ResearchObject researchObject, Annotation annotation)
            throws NotFoundException, DigitalLibraryException, AccessDeniedException {
        URI oldAnnotationBody = ROSRService.getAnnotationBody(researchObject, annotation.getUri(), null);
        ROSRService.SMS.get().updateAnnotation(researchObject, annotation);

        if (oldAnnotationBody == null || !oldAnnotationBody.equals(annotation.getBody().getUri())) {
            ROSRService.convertAnnotationBodyToAggregatedResource(researchObject, oldAnnotationBody);
            if (researchObject.getAggregatedResources().containsKey(annotation.getBody().getUri())) {
                ROSRService.convertRoResourceToAnnotationBody(researchObject, researchObject.getAggregatedResources()
                        .get(annotation.getBody().getUri()));
            }
        }

        String annotationBodyHeader = String.format(Constants.LINK_HEADER_TEMPLATE, annotation.getBody().getUri()
                .toString(), AO.annotatesResource);
        ResponseBuilder response = Response.ok().header(Constants.LINK_HEADER, annotationBodyHeader);
        for (Thing target : annotation.getAnnotated()) {
            String targetHeader = String
                    .format(Constants.LINK_HEADER_TEMPLATE, target.toString(), AO.annotatesResource);
            response = response.header(Constants.LINK_HEADER, targetHeader);
        }
        return response.build();
    }


    /**
     * Get the URI of an annotation body. If acceptHeader is not null, will return a format-specific URI.
     * 
     * @param researchObject
     *            research object in which the annotation is defined
     * @param annotationUri
     *            the annotation
     * @param acceptHeader
     *            MIME type requested (content negotiation) or null
     * @return URI of the annotation body
     * @throws NotFoundException
     *             could not find the resource in DL when checking if it's internal
     * @throws DigitalLibraryException
     *             could not connect to the DL to check if it's internal
     */
    public static URI getAnnotationBody(ResearchObject researchObject, URI annotationUri, String acceptHeader)
            throws NotFoundException, DigitalLibraryException {
        Annotation annotation = ROSRService.SMS.get().getAnnotation(researchObject, annotationUri);
        RDFFormat acceptFormat = RDFFormat.forMIMEType(acceptHeader);
        if (acceptFormat != null && isInternalResource(researchObject, annotation.getBody().getUri())) {
            RDFFormat extensionFormat = RDFFormat.forFileName(annotation.getUri().getPath());
            return createFormatSpecificURI(annotation.getBody().getUri(), extensionFormat, acceptFormat);
        } else {
            return annotation.getBody().getUri();
        }
    }


    /**
     * Delete and deaggregate an annotation. Does not delete the annotation body.
     * 
     * @param researchObject
     *            research object in which the annotation is defined
     * @param annotationUri
     *            the annotation URI
     * @return 204 No Content
     * @throws NotFoundException
     *             could not find the resource in DL when checking if it's internal
     * @throws DigitalLibraryException
     *             could not connect to the DL to check if it's internal
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public static Response deleteAnnotation(ResearchObject researchObject, URI annotationUri)
            throws NotFoundException, DigitalLibraryException, AccessDeniedException {
        Annotation annotation = ROSRService.SMS.get().getAnnotation(researchObject, annotationUri);
        ROSRService.SMS.get().deleteAnnotation(researchObject, annotation);
        ROSRService.convertAnnotationBodyToAggregatedResource(researchObject, annotation.getBody().getUri());
        return Response.noContent().build();
    }


    /**
     * Find out all annotations of the RO and store them in dLibra as attributes.
     * 
     * @param researchObject
     *            RO URI
     */
    private static void updateROAttributesInDlibra(ResearchObject researchObject) {
        Multimap<URI, Object> roAttributes = ROSRService.SMS.get().getAllAttributes(researchObject.getUri());
        roAttributes.put(URI.create("Identifier"), researchObject);
        try {
            ROSRService.DL.get().storeAttributes(researchObject.getUri(), roAttributes);
        } catch (Exception e) {
            LOGGER.error("Caught an exception when updating RO attributes, will continue", e);
        }
    }


    /**
     * Update a folder.
     * 
     * @param folder
     *            the folder
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public static void updateFolder(Folder folder)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        ROSRService.SMS.get().updateFolder(folder);
        folder.getResourceMap().serialize();
    }


    /**
     * Delete the folder and update the manifest.
     * 
     * @param folder
     *            folder
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public static void deleteFolder(Folder folder)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        String filePath = folder.getResearchObject().getUri().relativize(folder.getResourceMap().getUri()).getPath();
        ROSRService.DL.get().deleteFile(folder.getResearchObject().getUri(), filePath);
        ROSRService.SMS.get().deleteFolder(folder);
        folder.getResearchObject().getManifest().serialize();
    }


    /**
     * Delete a folder entry.
     * 
     * @param entry
     *            folder entry
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    public static void deleteFolderEntry(FolderEntry entry)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        Folder folder = ROSRService.SMS.get().getFolder(entry.getProxyIn().getUri());
        folder.getFolderEntries().remove(entry);
        updateFolder(folder);
    }

}
