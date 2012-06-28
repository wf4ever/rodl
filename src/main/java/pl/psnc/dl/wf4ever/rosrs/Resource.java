package pl.psnc.dl.wf4ever.rosrs;

import java.net.URI;

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
import pl.psnc.dl.wf4ever.auth.AuthenticationException;
import pl.psnc.dl.wf4ever.auth.ForbiddenException;
import pl.psnc.dl.wf4ever.auth.SecurityFilter;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;

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


    @PUT
    public Response putResource(@PathParam("ro_id") String researchObjectId, @PathParam("filePath") String filePath,
            @QueryParam("original") String original, String entity)
            throws BadRequestException {
        UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
        if (user.getRole() == UserProfile.Role.PUBLIC) {
            //TODO check permissions in dLibra
            throw new AuthenticationException("Only authenticated users can do that.", SecurityFilter.REALM);
        }
        URI researchObject = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
        URI resource = uriInfo.getAbsolutePath();

        if (ROSRService.isProxy(researchObject, resource)) {
            return Response.status(Status.TEMPORARY_REDIRECT)
                    .location(ROSRService.getProxyFor(researchObject, resource)).build();
        }
        boolean isProxyTarget = ROSRService.existsProxyFor(researchObject, resource);
        boolean isAnnotationBody = ROSRService.isAnnotationBody(researchObject, resource);
        boolean isAggregatedResource = ROSRService.isAggregatedResource(researchObject, resource);
        if (isAggregatedResource) {
            return ROSRService.updateInternalResource(researchObject, resource, entity, request.getContentType(),
                original);
        } else {
            if (isProxyTarget || isAnnotationBody) {
                return ROSRService.aggregateInternalResource(researchObject, resource, entity,
                    request.getContentType(), original);
            } else {
                throw new ForbiddenException(
                        "You cannot use PUT to create new resources unless they have been referenced in a proxy or an annotation. Use POST instead.");
            }
        }
    }


    @PUT
    @Consumes(Constants.ANNOTATION_MIME_TYPE)
    public Response updateAnnotation(@PathParam("ro_id") String researchObjectId,
            @PathParam("filePath") String filePath, @QueryParam("original") String original, Annotation annotation) {
        UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
        if (user.getRole() == UserProfile.Role.PUBLIC) {
            //TODO check permissions in dLibra
            throw new AuthenticationException("Only authenticated users can do that.", SecurityFilter.REALM);
        }
        URI researchObject = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
        URI resource = uriInfo.getAbsolutePath();

        if (!ROSRService.isAnnotation(researchObject, resource)) {
            throw new ForbiddenException("You cannot create a new annotation using PUT, use POST instead.");
        }
        URI oldAnnotationBody = ROSRService.getAnnotationBody(researchObject, resource, null);
        if (oldAnnotationBody == null || !oldAnnotationBody.equals(annotation.getAnnotationBody())) {
            ROSRService.convertAnnotationBodyToAggregatedResource(researchObject, oldAnnotationBody);
            if (ROSRService.isAggregatedResource(researchObject, annotation.getAnnotationBody())) {
                ROSRService.convertAggregatedResourceToAnnotationBody(researchObject, annotation.getAnnotationBody());
            }
        }
        return ROSRService.updateAnnotation(researchObject, annotation);
    }


    @GET
    public Response getResource(@PathParam("ro_id") String researchObjectId, @PathParam("filePath") String filePath,
            @QueryParam("original") String original) {
        UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
        if (user.getRole() == UserProfile.Role.PUBLIC) {
            //TODO check permissions in dLibra
            throw new AuthenticationException("Only authenticated users can do that.", SecurityFilter.REALM);
        }
        URI researchObject = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
        URI resource = uriInfo.getAbsolutePath();

        if (ROSRService.isProxy(researchObject, resource)) {
            return Response.status(Status.SEE_OTHER).location(ROSRService.getProxyFor(researchObject, resource))
                    .build();
        }
        if (ROSRService.isAnnotation(researchObject, resource)) {
            return Response
                    .status(Status.SEE_OTHER)
                    .location(
                        ROSRService.getAnnotationBody(researchObject, resource,
                            request.getHeader(Constants.ACCEPT_HEADER))).build();
        }
        return ROSRService.getInternalResource(researchObject, resource, request.getHeader("Accept"), original);
    }


    @DELETE
    public Response deleteResource(@PathParam("ro_id") String researchObjectId, @PathParam("filePath") String filePath,
            @QueryParam("original") String original) {
        UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
        if (user.getRole() == UserProfile.Role.PUBLIC) {
            //TODO check permissions in dLibra
            throw new AuthenticationException("Only authenticated users can do that.", SecurityFilter.REALM);
        }
        URI researchObject = uriInfo.getBaseUriBuilder().path("ROs").path(researchObjectId).path("/").build();
        URI resource = uriInfo.getAbsolutePath();

        if (ROSRService.isProxy(researchObject, resource)) {
            return Response.status(Status.TEMPORARY_REDIRECT)
                    .location(ROSRService.getProxyFor(researchObject, resource)).build();
        }
        if (ROSRService.isAnnotation(researchObject, resource)) {
            URI annotationBody = ROSRService.getAnnotationBody(researchObject, resource, null);
            ROSRService.convertAnnotationBodyToAggregatedResource(researchObject, annotationBody);
            return ROSRService.deleteAnnotation(researchObject, resource);
        }
        return ROSRService.deleteInternalResource(researchObject, resource, original);
    }

}
