package pl.psnc.dl.wf4ever.rosrs;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.http.HttpStatus;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.auth.ForbiddenException;
import pl.psnc.dl.wf4ever.auth.RequestAttribute;
import pl.psnc.dl.wf4ever.common.Builder;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.ORE.Proxy;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.Folder;
import pl.psnc.dl.wf4ever.model.RO.FolderEntry;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.model.RO.ResearchObjectComponent;
import pl.psnc.dl.wf4ever.vocabulary.AO;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.sun.jersey.core.header.ContentDisposition;

/**
 * An aggregated resource REST API resource.
 * 
 * @author Piotr Hołubowicz
 * 
 */
@Path("ROs/{ro_id}/{filePath: .+}")
public class Resource {

    /** HTTP request. */
    @Context
    private HttpServletRequest servletRequest;

    /** URI info. */
    @Context
    private UriInfo uriInfo;

    /** Resource builder. */
    @RequestAttribute("Builder")
    private Builder builder;


    /**
     * Update an exiting resource or upload a one for which a proxy exists.
     * 
     * @param researchObjectId
     *            RO id
     * @param filePath
     *            file path relative to the RO URI
     * @param original
     *            original format in case of annotation bodies
     * @param entity
     *            resource content
     * @return 201 Created or 307 Temporary Redirect
     */
    @PUT
    public Response putResource(@PathParam("ro_id") String researchObjectId, @PathParam("filePath") String filePath,
            @QueryParam("original") String original, String entity) {
        URI uri = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
        ResearchObject researchObject = ResearchObject.get(builder, uri);
        if (researchObject == null) {
            throw new NotFoundException("Research Object not found");
        }
        URI resource = uriInfo.getAbsolutePath();

        if (ROSRService.SMS.get().isProxy(researchObject, resource)) {
            return Response.status(Status.TEMPORARY_REDIRECT)
                    .location(ROSRService.SMS.get().getProxyFor(researchObject, resource)).build();
        }
        if (ROSRService.SMS.get().isAggregatedResource(researchObject, resource)) {
            if (ROSRService.SMS.get().isAnnotation(researchObject, resource)) {
                return Response
                        .status(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE)
                        .entity(
                            "This resource is an annotation, only \"application/vnd.wf4ever.annotation\" media type is accepted")
                        .build();
            }
            if (original != null) {
                resource = resource.resolve(original);
            }
            if (researchObject.getManifestUri().equals(resource)) {
                throw new ForbiddenException("Can't update the manifest");
            } else if (researchObject.getFixedEvolutionAnnotationBodyUri().equals(resource)) {
                throw new ForbiddenException("Can't update the evo info");
            }
            ResponseBuilder rb = ROSRService.updateInternalResource(researchObject, resource, entity,
                servletRequest.getContentType());
            ResourceMetadata resInfo = ROSRService.getResourceInfo(researchObject, resource, original);
            if (resInfo != null) {
                CacheControl cache = new CacheControl();
                cache.setMustRevalidate(true);
                rb = rb.cacheControl(cache).tag(resInfo.getChecksum()).lastModified(resInfo.getLastModified().toDate());
            }
            return rb.build();
        } else {
            throw new ForbiddenException(
                    "You cannot use PUT to create new resources unless they have been referenced in a proxy or an annotation. Use POST instead.");
        }
    }


    /**
     * Make a PUT to update an annotation.
     * 
     * @param researchObjectId
     *            research object ID
     * @param filePath
     *            the file path
     * @param original
     *            original resource in case of a format-specific URI
     * @param content
     *            RDF/XML representation of an annotation
     * @return 200 OK
     * @throws BadRequestException
     *             the RDF/XML content is incorrect
     */
    @PUT
    @Consumes(Constants.ANNOTATION_MIME_TYPE)
    public Response updateAnnotation(@PathParam("ro_id") String researchObjectId,
            @PathParam("filePath") String filePath, @QueryParam("original") String original, InputStream content)
            throws BadRequestException {
        URI uri = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
        ResearchObject researchObject = ResearchObject.get(builder, uri);
        if (researchObject == null) {
            throw new NotFoundException("Research Object not found");
        }
        URI resource = uriInfo.getAbsolutePath();
        URI body;
        Set<Thing> targets = new HashSet<>();
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.read(content, researchObject.getUri().toString());
        ExtendedIterator<Individual> it = model.listIndividuals(RO.AggregatedAnnotation);
        if (it.hasNext()) {
            Individual aggregatedAnnotation = it.next();
            NodeIterator it2 = aggregatedAnnotation.listPropertyValues(AO.body);
            if (it2.hasNext()) {
                RDFNode bodyResource = it2.next();
                if (bodyResource.isURIResource()) {
                    try {
                        body = new URI(bodyResource.asResource().getURI());
                    } catch (URISyntaxException e) {
                        throw new BadRequestException("Wrong body resource URI", e);
                    }
                } else {
                    throw new BadRequestException("The body is not an URI resource.");
                }
            } else {
                throw new BadRequestException("The ro:AggregatedAnnotation does not have a ao:body property.");
            }
            it2 = aggregatedAnnotation.listPropertyValues(AO.annotatesResource);
            while (it2.hasNext()) {
                RDFNode targetResource = it2.next();
                if (targetResource.isURIResource()) {
                    try {
                        targets.add(builder.buildThing(new URI(targetResource.asResource().getURI())));
                    } catch (URISyntaxException e) {
                        throw new BadRequestException("Wrong target resource URI", e);
                    }
                } else {
                    throw new BadRequestException("The target is not an URI resource.");
                }
            }
        } else {
            throw new BadRequestException("The entity body does not define any ro:AggregatedAnnotation.");
        }

        if (!ROSRService.SMS.get().isAnnotation(researchObject, resource)) {
            throw new ForbiddenException("You cannot create a new annotation using PUT, use POST instead.");
        }
        return ROSRService.updateAnnotation(researchObject,
            builder.buildAnnotation(researchObject, resource, body, targets));
    }


    /**
     * Get a resource.
     * 
     * @param researchObjectId
     *            RO id
     * @param filePath
     *            the file path
     * @param original
     *            original resource in case of a format-specific URI
     * @param accept
     *            Accept header
     * @param request
     *            HTTP request for cacheing
     * @return 200 OK or 303 See Other
     */
    @GET
    public Response getResource(@PathParam("ro_id") String researchObjectId, @PathParam("filePath") String filePath,
            @QueryParam("original") String original, @HeaderParam("Accept") String accept, @Context Request request) {
        URI uri = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
        ResearchObject researchObject = ResearchObject.get(builder, uri);
        if (researchObject == null) {
            throw new NotFoundException("Research Object not found");
        }
        URI resourceUri = uriInfo.getAbsolutePath();
        String specificName = null;
        if (original != null) {
            specificName = resourceUri.resolve(".").relativize(resourceUri).getPath();
            resourceUri = UriBuilder.fromUri(resourceUri.resolve(".")).path(original).build();
        }
        //FIXME this won't work for Accept headers with more than one RDF format
        RDFFormat format = RDFFormat.forMIMEType(accept);

        if (researchObject.getProxies().containsKey(resourceUri)) {
            return getProxy(researchObject.getProxies().get(resourceUri));
        }
        if (researchObject.getFolderEntries().containsKey(resourceUri)) {
            return getProxy(researchObject.getFolderEntries().get(resourceUri));
        }
        if (researchObject.getAnnotations().containsKey(resourceUri)) {
            return getAnnotation(researchObject.getAnnotations().get(resourceUri), format);
        }
        if (researchObject.getFolders().containsKey(resourceUri)) {
            return getFolder(researchObject.getFolders().get(resourceUri), format);
        }

        ResearchObjectComponent resource;
        if (researchObject.getAggregatedResources().containsKey(resourceUri)) {
            resource = researchObject.getAggregatedResources().get(resourceUri);
        } else if (researchObject.getResourceMaps().containsKey(resourceUri)) {
            resource = researchObject.getResourceMaps().get(resourceUri);
        } else {
            throw new NotFoundException("Resource not found");
        }
        ResponseBuilder rb = request.evaluatePreconditions(resource.getStats().getLastModified().toDate(),
            new EntityTag(resource.getStats().getChecksum()));
        if (rb != null) {
            return rb.build();
        }
        InputStream data;
        String mimeType;
        String filename = resource.getFilename();
        if (!resource.isInternal()) {
            throw new NotFoundException("Resource has no content");
        }
        if (resource.isNamedGraph()) {
            // check if request is for a specific format
            if (specificName != null) {
                URI specificResourceUri = UriBuilder.fromUri(resource.getUri().resolve(".")).path(specificName).build();
                if (format == null) {
                    format = specificResourceUri.getPath() != null ? RDFFormat.forFileName(
                        specificResourceUri.getPath(), RDFFormat.RDFXML) : RDFFormat.RDFXML;
                }
                data = resource.getGraphAsInputStream(format);
                mimeType = format.getDefaultMIMEType();
                filename = specificName;
            } else {
                RDFFormat extensionFormat = RDFFormat.forFileName(resource.getUri().getPath());
                if (extensionFormat != null && (format == null || extensionFormat == format)) {
                    // 1. GET manifest.rdf Accept: application/rdf+xml
                    // 2. GET manifest.rdf
                    data = resource.getGraphAsInputStream(extensionFormat);
                    mimeType = extensionFormat.getDefaultMIMEType();
                } else {
                    // 3. GET manifest.rdf Accept: text/turtle
                    // 4. GET manifest Accept: application/rdf+xml
                    // 5. GET manifest
                    if (format == null) {
                        format = RDFFormat.RDFXML;
                    }
                    return Response.temporaryRedirect(resource.getUri(format)).build();
                }
            }
        } else {
            data = resource.getSerialization();
            mimeType = resource.getStats().getMimeType();
        }
        if (data == null) {
            throw new NotFoundException("Resource has no content");
        }

        ContentDisposition cd = ContentDisposition.type(mimeType).fileName(filename).build();
        CacheControl cache = new CacheControl();
        cache.setMustRevalidate(true);
        return Response.ok(data).type(mimeType).header("Content-disposition", cd).cacheControl(cache)
                .tag(resource.getStats().getChecksum()).lastModified(resource.getStats().getLastModified().toDate())
                .build();
    }


    /**
     * Get a folder.
     * 
     * @param folder
     *            folder
     * @param format
     *            format requested
     * @return 303 See Other redirecting to the resource map
     */
    private Response getFolder(Folder folder, RDFFormat format) {
        return Response.status(Status.SEE_OTHER).location(folder.getResourceMap().getUri(format)).build();
    }


    /**
     * Get an annotation.
     * 
     * @param annotation
     *            annotation
     * @param format
     *            format requested
     * @return 303 See Other redirecting to the body
     */
    private Response getAnnotation(Annotation annotation, RDFFormat format) {
        URI bodyUri = annotation.getBodyUri();
        if (annotation.getResearchObject().getAggregatedResources().containsKey(bodyUri) && format != null) {
            AggregatedResource resource = annotation.getResearchObject().getAggregatedResources().get(bodyUri);
            if (resource.isInternal()) {
                bodyUri = resource.getUri(format);
            }
        }
        return Response.status(Status.SEE_OTHER).location(bodyUri).build();
    }


    /**
     * Get a proxy.
     * 
     * @param proxy
     *            proxy
     * @return 303 See Other redirecting to the proxied resource
     */
    private Response getProxy(Proxy proxy) {
        return Response.status(Status.SEE_OTHER).location(proxy.getProxyFor().getUri()).build();
    }


    /**
     * Delete a resource.
     * 
     * @param researchObjectId
     *            RO id
     * @param filePath
     *            the file path
     * @param original
     *            original resource in case of a format-specific URI
     * @return 204 No Content or 307 Temporary Redirect
     */
    @DELETE
    public Response deleteResource(@PathParam("ro_id") String researchObjectId, @PathParam("filePath") String filePath,
            @QueryParam("original") String original) {
        URI uri = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
        ResearchObject researchObject = ResearchObject.get(builder, uri);
        if (researchObject == null) {
            throw new NotFoundException("Research Object not found");
        }
        URI resourceUri = uriInfo.getAbsolutePath();

        if (researchObject.getProxies().containsKey(resourceUri)) {
            AggregatedResource resource = researchObject.getProxies().get(resourceUri).getProxyFor();
            if (resource.isInternal()) {
                return Response.status(Status.TEMPORARY_REDIRECT).location(resource.getUri()).build();
            } else {
                return ROSRService.deaggregateExternalResource(researchObject, resourceUri);
            }
        }
        Annotation annotation = researchObject.getAnnotations().get(resourceUri);
        if (annotation != null) {
            if (researchObject.getFixedEvolutionAnnotationBodyUri().equals(annotation.getBodyUri())) {
                throw new ForbiddenException("Can't delete the evo annotation");
            }
            return ROSRService.deleteAnnotation(researchObject, resourceUri);
        }
        if (researchObject.getFolders().containsKey(resourceUri)) {
            Folder folder = researchObject.getFolders().get(resourceUri);
            ROSRService.deleteFolder(folder);
            return Response.noContent().build();
        }
        FolderEntry entry = ROSRService.SMS.get().getFolderEntry(resourceUri);
        if (entry != null) {
            ROSRService.deleteFolderEntry(entry);
            return Response.noContent().build();
        }
        if (original != null) {
            resourceUri = resourceUri.resolve(original);
        }
        if (researchObject.getManifestUri().equals(resourceUri)) {
            throw new ForbiddenException("Can't delete the manifest");
        } else if (researchObject.getFixedEvolutionAnnotationBodyUri().equals(resourceUri)) {
            throw new ForbiddenException("Can't delete the evo info");
        }

        return ROSRService.deaggregateInternalResource(researchObject, resourceUri);
    }


    /**
     * Create a new folder entry.
     * 
     * @param content
     *            folder entry description
     * @return 201 Created response pointing to the folder entry
     * @throws BadRequestException
     *             wrong request body
     */
    @POST
    @Consumes(Constants.FOLDERENTRY_MIME_TYPE)
    public Response addFolderEntry(InputStream content)
            throws BadRequestException {
        URI uri = uriInfo.getAbsolutePath();
        Folder folder = Folder.get(builder, uri);
        if (folder == null) {
            throw new NotFoundException("Folder not found; " + uri);
        }

        FolderEntry entry = folder.createFolderEntry(content);

        return Response
                .created(entry.getUri())
                .header(
                    "Link",
                    String.format(Constants.LINK_HEADER_TEMPLATE, entry.getProxyFor(),
                        "http://www.openarchives.org/ore/terms/proxyFor")).build();
    }
}
