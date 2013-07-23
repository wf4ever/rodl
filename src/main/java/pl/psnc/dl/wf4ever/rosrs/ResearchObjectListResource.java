package pl.psnc.dl.wf4ever.rosrs;

import java.io.InputStream;
import java.net.URI;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.auth.RequestAttribute;
import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
import pl.psnc.dl.wf4ever.model.Builder;
import pl.psnc.dl.wf4ever.model.RO.ResearchObject;

import com.sun.jersey.core.header.ContentDisposition;

/**
 * A list of research objects REST APIs.
 * 
 * @author piotrhol
 * 
 */
@Path("ROs/")
public class ResearchObjectListResource {

    /** logger. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(ResearchObjectListResource.class);

    /** URI info. */
    @Context
    UriInfo uriInfo;

    /** Resource builder. */
    @RequestAttribute("Builder")
    private Builder builder;


    /**
     * Returns list of relative links to research objects.
     * 
     * @return 200 OK
     */
    @GET
    @Produces("text/plain")
    public Response getResearchObjectList() {
        Set<ResearchObject> list = ResearchObject.getAll(builder, builder.getUser());
        StringBuilder sb = new StringBuilder();
        for (ResearchObject id : list) {
            sb.append(id.getUri().toString());
            sb.append("\r\n");
        }
        ContentDisposition cd = ContentDisposition.type("attachment").fileName("ROs.txt").build();
        return Response.ok().entity(sb.toString()).header("Content-disposition", cd).type("text/plain").build();
    }


    /**
     * Creates new RO with given RO_ID.
     * 
     * @param researchObjectId
     *            slug header
     * @param accept
     *            Accept header
     * @return 201 (Created) when the RO was successfully created, 409 (Conflict) if the RO_ID is already used in the
     *         WORKSPACE_ID workspace
     * @throws BadRequestException
     *             the RO id is incorrect
     */
    @POST
    public Response createResearchObject(@HeaderParam("Slug") String researchObjectId,
            @HeaderParam("Accept") String accept)
            throws BadRequestException {
        if (researchObjectId == null || researchObjectId.isEmpty()) {
            throw new BadRequestException("Research object ID is null or empty");
        }
        if (researchObjectId.contains("/")) {
            throw new BadRequestException("Research object ID cannot contain slashes, see WFE-703");
        }
        URI uri = uriInfo.getAbsolutePathBuilder().path(researchObjectId).path("/").build();
        ResearchObject researchObject = ResearchObject.create(builder, uri);
        RDFFormat format = accept != null ? RDFFormat.forMIMEType(accept, RDFFormat.RDFXML) : RDFFormat.RDFXML;
        InputStream manifest = researchObject.getManifest().getGraphAsInputStream(format);
        ContentDisposition cd = ContentDisposition.type("attachment").fileName(ResearchObject.MANIFEST_PATH).build();
        return Response.created(researchObject.getUri()).entity(manifest).header("Content-disposition", cd)
                .type(format.getDefaultMIMEType()).build();
    }
}
