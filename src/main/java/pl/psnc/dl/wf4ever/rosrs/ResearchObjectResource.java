package pl.psnc.dl.wf4ever.rosrs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.common.util.HeaderUtils;
import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.AO.Annotation;
import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
import pl.psnc.dl.wf4ever.model.RDF.Thing;
import pl.psnc.dl.wf4ever.model.RO.Folder;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
import pl.psnc.dl.wf4ever.vocabulary.AO;
import pl.psnc.dl.wf4ever.vocabulary.ORE;
import pl.psnc.dl.wf4ever.vocabulary.RO;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.sun.jersey.api.ConflictException;

/**
 * Research Object REST API resource.
 * 
 * @author Piotr Hołubowicz
 * 
 */
@Path("ROs/{ro_id}/")
public class ResearchObjectResource {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(ResearchObjectResource.class);

    /** An application that displays the HTML version of a workflow. */
    private static final URI RO_HTML_PORTAL = URI.create("http://sandbox.wf4ever-project.org/portal/ro");

    /** HTTP request. */
    @Context
    HttpServletRequest request;

    /** URI info. */
    @Context
    UriInfo uriInfo;


    /**
     * Returns zip archive with contents of RO version.
     * 
     * @param researchObjectId
     *            RO identifier - defined by the user
     * @return 200 OK
     */
    @GET
    @Produces({ "application/zip", "multipart/related", "*/*" })
    public Response getZippedRO(@PathParam("ro_id") String researchObjectId) {
        return Response.seeOther(getZippedROURI(uriInfo.getBaseUriBuilder(), researchObjectId)).build();
    }


    /**
     * Redirect to the manifest.
     * 
     * @param researchObjectId
     *            RO id
     * @return 303 See Other
     */
    @GET
    @Produces({ "application/rdf+xml", "application/x-turtle", "text/turtle", "application/x-trig", "application/trix",
            "text/rdf+n3" })
    public Response getROMetadata(@PathParam("ro_id") String researchObjectId) {
        return Response.seeOther(getROMetadataURI(uriInfo.getBaseUriBuilder(), researchObjectId + "/"))
                .header(Constants.LINK_HEADER, getROEvoLinkURI(uriInfo.getBaseUriBuilder(), researchObjectId)).build();
    }


    /**
     * Redirect to the HTML page of the RO.
     * 
     * @param researchObjectId
     *            RO id
     * @return 303 See Other
     * @throws URISyntaxException
     *             could not construct a valid redirection URI
     */
    @GET
    @Produces({ MediaType.TEXT_HTML })
    public Response getROHtml(@PathParam("ro_id") String researchObjectId)
            throws URISyntaxException {
        URI uri = getROHtmlURI(uriInfo.getBaseUriBuilder(), researchObjectId);
        return Response.seeOther(uri).build();
    }


    /**
     * Delete a research object.
     * 
     * @param researchObjectId
     *            research object id
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws NotFoundException
     *             Research Object not found neither in dLibra nor in SMS
     */
    @DELETE
    public void deleteResearchObject(@PathParam("ro_id") String researchObjectId)
            throws DigitalLibraryException, NotFoundException {
        URI uri = uriInfo.getAbsolutePath();
        ResearchObject researchObject = ResearchObject.get(uri);
        if (researchObject == null) {
            throw new NotFoundException("Research Object not found");
        }
        researchObject.delete();
    }


    /**
     * Add a resource with no specific MIME type.
     * 
     * @param researchObjectId
     *            RO id
     * @param path
     *            resource path
     * @param accept
     *            Accept header
     * @param links
     *            Link headers
     * @param content
     *            resource content
     * @return 201 Created with proxy URI
     * @throws BadRequestException
     *             annotation target is an incorrect URI
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    @POST
    public Response addResource(@PathParam("ro_id") String researchObjectId, @HeaderParam("Slug") String path,
            @HeaderParam("Accept") String accept, @HeaderParam("Link") Set<String> links, InputStream content)
            throws BadRequestException, AccessDeniedException, DigitalLibraryException, NotFoundException {
        URI uri = uriInfo.getAbsolutePath();
        RDFFormat responseSyntax = RDFFormat.forMIMEType(accept, RDFFormat.RDFXML);
        ResearchObject researchObject = ResearchObject.get(uri);
        if (researchObject == null) {
            throw new NotFoundException("Research Object not found");
        }
        researchObject.load();
        if (path == null) {
            path = UUID.randomUUID().toString();
        }
        URI resourceUri = uriInfo.getAbsolutePathBuilder().path(path).build();
        if (ROSRService.SMS.get().isAggregatedResource(researchObject, resourceUri)) {
            throw new ConflictException("This resource has already been aggregated. Use PUT to update it.");
        }
        Collection<URI> annotated = HeaderUtils.getLinkHeaders(links).get(AO.annotatesResource.getURI());
        Set<Thing> annotationTargets = new HashSet<>();
        for (URI ann : annotated) {
            annotationTargets.add(new Thing(ann));
        }
        if (!annotationTargets.isEmpty()) {
            for (Thing target : annotationTargets) {
                if (!ROSRService.SMS.get().isAggregatedResource(researchObject, target.getUri())
                        && !target.getUri().equals(researchObject.getUri())
                        && !ROSRService.SMS.get().isProxy(researchObject, target.getUri())) {
                    throw new BadRequestException(String.format(
                        "The annotation target %s is not RO, aggregated resource nor proxy.", target));
                }
            }
            pl.psnc.dl.wf4ever.model.RO.Resource roResource = researchObject.aggregate(path, content,
                request.getContentType());
            AggregatedResource aggregatedResource = ROSRService.convertRoResourceToAnnotationBody(researchObject,
                roResource);

            Annotation annotation = researchObject.annotate(new Thing(resourceUri), annotationTargets);
            String annotationBodyHeader = String.format(Constants.LINK_HEADER_TEMPLATE,
                annotation.getBody().toString(), AO.body);
            InputStream annotationDesc = ROSRService.SMS.get().getResource(researchObject, responseSyntax,
                aggregatedResource.getProxy().getUri(), aggregatedResource.getUri(), annotation.getUri());
            ResponseBuilder response = Response.created(annotation.getUri()).entity(annotationDesc)
                    .type(responseSyntax.getDefaultMIMEType()).header(Constants.LINK_HEADER, annotationBodyHeader);
            for (Thing target : annotation.getAnnotated()) {
                String targetHeader = String.format(Constants.LINK_HEADER_TEMPLATE, target.toString(),
                    AO.annotatesResource);
                response = response.header(Constants.LINK_HEADER, targetHeader);
            }
            return response.build();
        } else {
            pl.psnc.dl.wf4ever.model.RO.Resource resource = researchObject.aggregate(path, content,
                request.getContentType());
            if (ROSRService.SMS.get().isROMetadataNamedGraph(researchObject, resource.getUri())) {
                ROSRService.convertRoResourceToAnnotationBody(researchObject, resource);
            }
            String proxyForHeader = String.format(Constants.LINK_HEADER_TEMPLATE, resource.getUri().toString(),
                Constants.ORE_PROXY_FOR_HEADER);
            InputStream proxyAndResourceDesc = ROSRService.SMS.get().getResource(researchObject, responseSyntax,
                resource.getProxy().getUri(), resource.getUri());
            ResponseBuilder builder = Response.created(resource.getProxy().getUri()).entity(proxyAndResourceDesc)
                    .type(responseSyntax.getDefaultMIMEType()).header(Constants.LINK_HEADER, proxyForHeader);
            if (resource.getStats() != null) {
                CacheControl cache = new CacheControl();
                cache.setMustRevalidate(true);
                builder = builder.cacheControl(cache).tag(resource.getStats().getChecksum())
                        .lastModified(resource.getStats().getLastModified().toDate());
            }
            return builder.build();
        }
    }


    /**
     * Create a new proxy.
     * 
     * @param researchObjectId
     *            RO id
     * @param slug
     *            Slug header
     * @param accept
     *            Accept header
     * @param content
     *            proxy description
     * @return 201 Created response pointing to the proxy
     * @throws BadRequestException
     *             wrong request body
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    @POST
    @Consumes(Constants.PROXY_MIME_TYPE)
    public Response addProxy(@PathParam("ro_id") String researchObjectId, @HeaderParam("Slug") String slug,
            @HeaderParam("Accept") String accept, InputStream content)
            throws BadRequestException, AccessDeniedException, DigitalLibraryException, NotFoundException {
        URI uri = uriInfo.getAbsolutePath();
        ResearchObject researchObject = ResearchObject.get(uri);
        if (researchObject == null) {
            throw new NotFoundException("Research Object not found");
        }
        URI proxyFor;
        if (slug != null) {
            // internal resource
            proxyFor = uriInfo.getAbsolutePathBuilder().path(slug).build();
        } else {
            // external resource
            OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            model.read(content, researchObject.getUri().toString());
            ExtendedIterator<Individual> it = model.listIndividuals(ORE.Proxy);
            if (it.hasNext()) {
                NodeIterator it2 = it.next().listPropertyValues(ORE.proxyFor);
                if (it2.hasNext()) {
                    RDFNode proxyForResource = it2.next();
                    if (proxyForResource.isURIResource()) {
                        try {
                            proxyFor = new URI(proxyForResource.asResource().getURI());
                        } catch (URISyntaxException e) {
                            throw new BadRequestException("Wrong target resource URI", e);
                        }
                    } else {
                        throw new BadRequestException("The target is not an URI resource.");
                    }
                } else {
                    //The ore:Proxy does not have a ore:proxyFor property.
                    proxyFor = uriInfo.getAbsolutePathBuilder().path(UUID.randomUUID().toString()).build();
                }
            } else {
                throw new BadRequestException("The entity body does not define any ore:Proxy.");
            }
        }
        pl.psnc.dl.wf4ever.model.RO.Resource resource = researchObject.aggregate(proxyFor);

        RDFFormat syntax = RDFFormat.forMIMEType(accept, RDFFormat.RDFXML);
        String proxyForHeader = String.format(Constants.LINK_HEADER_TEMPLATE, proxyFor.toString(),
            Constants.ORE_PROXY_FOR_HEADER);
        InputStream proxyDesc = ROSRService.SMS.get().getResource(researchObject, syntax, resource.getProxy().getUri());
        return Response.created(resource.getProxy().getUri()).entity(proxyDesc).type(syntax.getDefaultMIMEType())
                .header(Constants.LINK_HEADER, proxyForHeader).build();

    }


    /**
     * Add an annotation stub.
     * 
     * @param researchObjectId
     *            RO id
     * @param accept
     *            Accept header
     * @param content
     *            annotation definition
     * @return 201 Created response pointing to the annotation stub
     * @throws BadRequestException
     *             wrong request body
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    @POST
    @Consumes(Constants.ANNOTATION_MIME_TYPE)
    public Response addAnnotation(@PathParam("ro_id") String researchObjectId, @HeaderParam("Accept") String accept,
            InputStream content)
            throws AccessDeniedException, DigitalLibraryException, NotFoundException, BadRequestException {
        URI uri = uriInfo.getAbsolutePath();
        ResearchObject researchObject = ResearchObject.get(uri);
        if (researchObject == null) {
            throw new NotFoundException("Research Object not found");
        }
        researchObject.load();
        URI body;
        Set<Thing> targets = new HashSet<>();
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.read(content, researchObject.getUri().toString());
        List<Individual> aggregatedAnnotations = model.listIndividuals(RO.AggregatedAnnotation).toList();
        if (!aggregatedAnnotations.isEmpty()) {
            Individual aggregatedAnnotation = aggregatedAnnotations.get(0);
            List<RDFNode> bodyResources = aggregatedAnnotation.listPropertyValues(AO.body).toList();
            if (!bodyResources.isEmpty()) {
                RDFNode bodyResource = bodyResources.get(0);
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
            List<RDFNode> targetResources = aggregatedAnnotation.listPropertyValues(AO.annotatesResource).toList();
            for (RDFNode targetResource : targetResources) {
                if (targetResource.isURIResource()) {
                    targets.add(new Thing(URI.create(targetResource.asResource().getURI())));
                } else {
                    throw new BadRequestException("The target is not an URI resource.");
                }
            }
        } else {
            throw new BadRequestException("The entity body does not define any ro:AggregatedAnnotation.");
        }
        for (Thing target : targets) {
            if (!ROSRService.SMS.get().isAggregatedResource(researchObject, target.getUri())
                    && !target.equals(researchObject.getUri())
                    && !ROSRService.SMS.get().isProxy(researchObject, target.getUri())) {
                throw new BadRequestException(String.format(
                    "The annotation target %s is not RO, aggregated resource nor proxy.", target));
            }
        }
        if (researchObject.getAggregatedResources().containsKey(body)) {
            ROSRService.convertRoResourceToAnnotationBody(researchObject,
                researchObject.getAggregatedResources().get(body));
        }

        Annotation annotation = researchObject.annotate(new Thing(body), targets);
        String annotationBodyHeader = String.format(Constants.LINK_HEADER_TEMPLATE, annotation.getBody().toString(),
            AO.body);
        RDFFormat syntax = RDFFormat.forFileName(accept, RDFFormat.RDFXML);
        InputStream annotationDesc = ROSRService.SMS.get().getResource(researchObject, syntax, annotation.getUri());
        ResponseBuilder response = Response.created(annotation.getUri()).entity(annotationDesc)
                .type(syntax.getDefaultMIMEType()).header(Constants.LINK_HEADER, annotationBodyHeader);
        for (Thing target : annotation.getAnnotated()) {
            String targetHeader = String
                    .format(Constants.LINK_HEADER_TEMPLATE, target.toString(), AO.annotatesResource);
            response = response.header(Constants.LINK_HEADER, targetHeader);
        }
        return response.build();
    }


    /**
     * Add an ro:Folder.
     * 
     * @param researchObjectId
     *            RO id
     * @param path
     *            folder path
     * @param accept
     *            Accept header
     * @param content
     *            folder definition
     * @return 201 Created response pointing to the folder
     * @throws BadRequestException
     *             wrong request body
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    @POST
    @Consumes(Constants.FOLDER_MIME_TYPE)
    public Response addFolder(@PathParam("ro_id") String researchObjectId, @HeaderParam("Accept") String accept,
            @HeaderParam("Slug") String path, InputStream content)
            throws AccessDeniedException, DigitalLibraryException, NotFoundException, BadRequestException {
        URI uri = uriInfo.getAbsolutePath();
        ResearchObject researchObject = ResearchObject.get(uri);
        if (researchObject == null) {
            throw new NotFoundException("Research Object not found");
        }
        if (path == null) {
            path = UUID.randomUUID().toString();
        }
        URI folderURI = uriInfo.getAbsolutePathBuilder().path(path).build();
        Folder folder = ROSRService.assembleFolder(researchObject, folderURI, content);
        folder = ROSRService.createFolder(researchObject, folder);

        RDFFormat syntax = RDFFormat.forFileName(accept, RDFFormat.RDFXML);
        Model folderDesc = ModelFactory.createDefaultModel();
        folderDesc.read(ROSRService.SMS.get().getNamedGraph(folder.getResourceMapUri(), syntax), null);
        folderDesc.read(
            ROSRService.SMS.get().getResource(researchObject, syntax, folder.getUri(), folder.getProxy().getUri()),
            null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        folderDesc.write(out);

        ResponseBuilder rb = Response.created(folder.getProxy().getUri()).type(Constants.FOLDER_MIME_TYPE);
        rb = rb.header(Constants.LINK_HEADER,
            String.format(Constants.LINK_HEADER_TEMPLATE, folder.getUri().toString(), ORE.proxyFor.getURI()));
        rb = rb.header(Constants.LINK_HEADER, String.format(Constants.LINK_HEADER_TEMPLATE, folder.getResourceMapUri()
                .toString().toString(), ORE.isDescribedBy.getURI()));
        rb = rb.entity(new ByteArrayInputStream(out.toByteArray())).type(syntax.getDefaultMIMEType());
        return rb.build();
    }


    /**
     * Create a URI pointing to the zipped RO.
     * 
     * @param baseUriBuilder
     *            base URI builder
     * @param researchObjectId
     *            RO id
     * @return the URI pointing to the zipped RO
     */
    private static URI getZippedROURI(UriBuilder baseUriBuilder, String researchObjectId) {
        return baseUriBuilder.path("zippedROs").path(researchObjectId).path("/").build();
    }


    /**
     * Create a URI pointing to the HTML page of the RO.
     * 
     * @param baseUriBuilder
     *            base URI builder
     * @param researchObjectId
     *            RO id
     * @return the URI pointing to the HTML page of the RO
     * @throws URISyntaxException
     *             could not construct a valid redirection URI
     */
    private static URI getROHtmlURI(UriBuilder baseUriBuilder, String researchObjectId)
            throws URISyntaxException {
        return new URI(RO_HTML_PORTAL.getScheme(), RO_HTML_PORTAL.getAuthority(), RO_HTML_PORTAL.getPath(), "ro="
                + baseUriBuilder.path("ROs").path(researchObjectId).path("/").build().toString(), null);
    }


    /**
     * Create a URI pointing to the manifest.
     * 
     * @param baseUriBuilder
     *            base URI builder
     * @param researchObjectId
     *            RO id
     * @return the URI pointing to the manifest of the RO
     */
    private static URI getROMetadataURI(UriBuilder baseUriBuilder, String researchObjectId) {
        return baseUriBuilder.path("ROs").path(researchObjectId).path("/.ro/manifest.rdf").build();
    }


    /**
     * Create a URI pointing to the roevo information.
     * 
     * @param baseUriBuilder
     *            base URI builder
     * @param researchObjectId
     *            RO id
     * @return the URI pointing to the manifest of the RO
     */
    private static URI getROEvoLinkURI(UriBuilder baseUriBuilder, String researchObjectId) {
        String roUri = baseUriBuilder.clone().path("/ROs/").path(researchObjectId + "/").build().toString();
        return baseUriBuilder.path("/evo/info").queryParam("ro", roUri).build();
    }


    /**
     * Add Link headers pointing to different RO formats.
     * 
     * @param responseBuilder
     *            response builder to which to add the links
     * @param linkUriInfo
     *            URI info
     * @param researchObjectId
     *            RO id
     * @return the original response builder with the Link headers
     */
    public static ResponseBuilder addLinkHeaders(ResponseBuilder responseBuilder, UriInfo linkUriInfo,
            String researchObjectId) {
        try {
            return responseBuilder
                    .header(
                        Constants.LINK_HEADER,
                        String.format(Constants.LINK_HEADER_TEMPLATE,
                            getROHtmlURI(linkUriInfo.getBaseUriBuilder(), researchObjectId), "bookmark"))
                    .header(
                        Constants.LINK_HEADER,
                        String.format(Constants.LINK_HEADER_TEMPLATE,
                            getZippedROURI(linkUriInfo.getBaseUriBuilder(), researchObjectId), "bookmark"))
                    .header(
                        Constants.LINK_HEADER,
                        String.format(Constants.LINK_HEADER_TEMPLATE,
                            getROMetadataURI(linkUriInfo.getBaseUriBuilder(), researchObjectId), "bookmark"));
        } catch (URISyntaxException e) {
            LOGGER.error("Could not create RO Portal URI", e);
            return responseBuilder.header(
                Constants.LINK_HEADER,
                String.format(Constants.LINK_HEADER_TEMPLATE,
                    getZippedROURI(linkUriInfo.getBaseUriBuilder(), researchObjectId), "bookmark")).header(
                Constants.LINK_HEADER,
                String.format(Constants.LINK_HEADER_TEMPLATE,
                    getROMetadataURI(linkUriInfo.getBaseUriBuilder(), researchObjectId), "bookmark"));
        }
    }
}
