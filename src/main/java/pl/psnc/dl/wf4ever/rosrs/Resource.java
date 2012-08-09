package pl.psnc.dl.wf4ever.rosrs;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import pl.psnc.dl.wf4ever.BadRequestException;
import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.auth.ForbiddenException;
import pl.psnc.dl.wf4ever.auth.SecurityFilter;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dlibra.service.AccessDeniedException;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * 
 * @author Piotr Hołubowicz
 * 
 */
@Path("ROs/{ro_id}/{filePath: .+}")
public class Resource {

    @Context
    private HttpServletRequest request;

    @Context
    private UriInfo uriInfo;


    /**
     * 
     * @param researchObjectId
     * @param filePath
     * @param original
     * @param entity
     * @return
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    @PUT
    public Response putResource(@PathParam("ro_id") String researchObjectId, @PathParam("filePath") String filePath,
            @QueryParam("original") String original, String entity)
            throws AccessDeniedException, DigitalLibraryException, NotFoundException {
        URI researchObject = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
        URI resource = uriInfo.getAbsolutePath();

        if (SecurityFilter.SMS.get().isProxy(researchObject, resource)) {
            return Response.status(Status.TEMPORARY_REDIRECT)
                    .location(SecurityFilter.SMS.get().getProxyFor(researchObject, resource)).build();
        }
        if (SecurityFilter.SMS.get().isAggregatedResource(researchObject, resource)) {
            if (original != null) {
                resource = resource.resolve(original);
            }
            if (researchObject.resolve(Constants.MANIFEST_PATH).equals(resource)) {
                throw new ForbiddenException("Can't update the manifest");
            }
            return ROSRService.updateInternalResource(researchObject, resource, entity, request.getContentType());
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
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     * @throws BadRequestException
     *             the RDF/XML content is incorrect
     */
    @PUT
    @Consumes(Constants.ANNOTATION_MIME_TYPE)
    public Response updateAnnotation(@PathParam("ro_id") String researchObjectId,
            @PathParam("filePath") String filePath, @QueryParam("original") String original, InputStream content)
            throws AccessDeniedException, DigitalLibraryException, NotFoundException, BadRequestException {
        URI researchObject = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
        URI resource = uriInfo.getAbsolutePath();
        URI body;
        List<URI> targets = new ArrayList<>();
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.read(content, researchObject.toString());
        ExtendedIterator<Individual> it = model.listIndividuals(Constants.RO_AGGREGATED_ANNOTATION_CLASS);
        if (it.hasNext()) {
            NodeIterator it2 = it.next().listPropertyValues(Constants.AO_BODY_PROPERTY);
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
            it2 = it.next().listPropertyValues(Constants.AO_ANNOTATES_RESOURCE_PROPERTY);
            while (it2.hasNext()) {
                RDFNode targetResource = it2.next();
                if (targetResource.isURIResource()) {
                    try {
                        targets.add(new URI(targetResource.asResource().getURI()));
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

        if (!SecurityFilter.SMS.get().isAnnotation(researchObject, resource)) {
            throw new ForbiddenException("You cannot create a new annotation using PUT, use POST instead.");
        }
        URI oldAnnotationBody = ROSRService.getAnnotationBody(researchObject, resource, null);
        if (oldAnnotationBody == null || !oldAnnotationBody.equals(body)) {
            ROSRService.convertAnnotationBodyToAggregatedResource(researchObject, oldAnnotationBody);
            if (SecurityFilter.SMS.get().isAggregatedResource(researchObject, body)) {
                ROSRService.convertAggregatedResourceToAnnotationBody(researchObject, body);
            }
        }
        return ROSRService.updateAnnotation(researchObject, resource, body, targets);
    }


    /**
     * 
     * @param researchObjectId
     * @param filePath
     * @param original
     * @return
     * @throws NotFoundException
     * @throws DigitalLibraryException
     */
    @GET
    public Response getResource(@PathParam("ro_id") String researchObjectId, @PathParam("filePath") String filePath,
            @QueryParam("original") String original)
            throws DigitalLibraryException, NotFoundException {
        URI researchObject = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
        URI resource = uriInfo.getAbsolutePath();

        if (SecurityFilter.SMS.get().isProxy(researchObject, resource)) {
            return Response.status(Status.SEE_OTHER)
                    .location(SecurityFilter.SMS.get().getProxyFor(researchObject, resource)).build();
        }
        if (SecurityFilter.SMS.get().isAnnotation(researchObject, resource)) {
            return Response
                    .status(Status.SEE_OTHER)
                    .location(
                        ROSRService.getAnnotationBody(researchObject, resource,
                            request.getHeader(Constants.ACCEPT_HEADER))).build();
        }
        return ROSRService.getInternalResource(researchObject, resource, request.getHeader("Accept"), original);
    }


    /**
     * 
     * @param researchObjectId
     * @param filePath
     * @param original
     * @return
     * @throws NotFoundException
     *             could not find the resource in DL
     * @throws DigitalLibraryException
     *             could not connect to the DL
     * @throws AccessDeniedException
     *             access denied when updating data in DL
     */
    @DELETE
    public Response deleteResource(@PathParam("ro_id") String researchObjectId, @PathParam("filePath") String filePath,
            @QueryParam("original") String original)
            throws AccessDeniedException, DigitalLibraryException, NotFoundException {
        URI researchObject = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
        URI resource = uriInfo.getAbsolutePath();

        if (SecurityFilter.SMS.get().isProxy(researchObject, resource)) {
            URI proxyFor = SecurityFilter.SMS.get().getProxyFor(researchObject, resource);
            if (ROSRService.isInternalResource(researchObject, proxyFor)) {
                return Response.status(Status.TEMPORARY_REDIRECT).location(proxyFor).build();
            } else {
                return ROSRService.deaggregateExternalResource(researchObject, resource);
            }
        }
        if (SecurityFilter.SMS.get().isAnnotation(researchObject, resource)) {
            URI annotationBody = ROSRService.getAnnotationBody(researchObject, resource, null);
            ROSRService.convertAnnotationBodyToAggregatedResource(researchObject, annotationBody);
            return ROSRService.deleteAnnotation(researchObject, resource);
        }
        if (original != null) {
            resource = resource.resolve(original);
        }
        if (researchObject.resolve(Constants.MANIFEST_PATH).equals(resource)) {
            throw new ForbiddenException("Can't delete the manifest");
        }
        return ROSRService.deaggregateInternalResource(researchObject, resource);
    }
}
