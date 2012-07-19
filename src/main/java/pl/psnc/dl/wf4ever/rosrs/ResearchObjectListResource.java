package pl.psnc.dl.wf4ever.rosrs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.BadRequestException;
import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.dlibra.ConflictException;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dl.wf4ever.dlibra.UserProfile.Role;
import pl.psnc.dlibra.service.IdNotFoundException;

import com.sun.jersey.core.header.ContentDisposition;

/**
 * 
 * @author piotrhol
 * 
 */
@Path("ROs/")
public class ResearchObjectListResource {

    @SuppressWarnings("unused")
    private final static Logger logger = Logger.getLogger(ResearchObjectListResource.class);

    @Context
    HttpServletRequest request;

    @Context
    UriInfo uriInfo;


    /**
     * Returns list of relative links to research objects
     * 
     * @return TBD
     * @throws SQLException
     * @throws NamingException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NotFoundException
     * @throws DigitalLibraryException
     */
    @GET
    @Produces("text/plain")
    public Response getResearchObjectList()
            throws ClassNotFoundException, IOException, NamingException, SQLException, DigitalLibraryException,
            NotFoundException {
        UserProfile user = (UserProfile) request.getAttribute(Constants.USER);

        Set<URI> list;
        if (user.getRole() == Role.PUBLIC) {
            list = ROSRService.SMS.get().findResearchObjects(uriInfo.getAbsolutePath());
        } else {
            list = new HashSet<URI>();
            for (String wId : ROSRService.DL.get().getWorkspaceIds(user)) {
                for (String rId : ROSRService.DL.get().getResearchObjectIds(user, wId)) {
                    for (String vId : ROSRService.DL.get().getVersionIds(user, wId, rId)) {
                        if (wId.equals(Constants.workspaceId) && vId.equals(Constants.versionId)) {
                            list.add(uriInfo.getAbsolutePathBuilder().path(rId).path("/").build());
                        } else {
                            list.add(uriInfo.getAbsolutePathBuilder().path("..").path("workspaces").path(wId)
                                    .path("ROs").path(rId).path(vId).path("/").build());
                        }
                    }
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (URI id : list) {
            sb.append(id.toString());
            sb.append("\r\n");
        }

        ContentDisposition cd = ContentDisposition.type("text/plain").fileName("ROs.txt").build();

        return Response.ok().entity(sb.toString()).header("Content-disposition", cd).build();
    }


    /**
     * Creates new RO with given RO_ID.
     * 
     * @param researchObjectId
     *            RO_ID in plain text (text/plain)
     * @return 201 (Created) when the RO was successfully created, 409 (Conflict) if the RO_ID is already used in the
     *         WORKSPACE_ID workspace
     * @throws DigitalLibraryException
     * @throws NotFoundException
     * @throws SQLException
     * @throws NamingException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws ConflictException
     * @throws BadRequestException
     * @throws IdNotFoundException
     */
    @POST
    public Response createResearchObject()
            throws NotFoundException, DigitalLibraryException, ClassNotFoundException, IOException, NamingException,
            SQLException, ConflictException, BadRequestException {
        String researchObjectId = request.getHeader(Constants.SLUG_HEADER);
        if (researchObjectId == null || researchObjectId.isEmpty()) {
            throw new BadRequestException("Research object ID is null or empty");
        }
        URI researchObjectURI = ROSRService.createResearchObject(uriInfo.getAbsolutePathBuilder()
                .path(researchObjectId).path("/").build());

        RDFFormat format = RDFFormat.forMIMEType(request.getHeader(Constants.ACCEPT_HEADER), RDFFormat.RDFXML);
        InputStream manifest = ROSRService.SMS.get().getNamedGraph(researchObjectURI.resolve(Constants.MANIFEST_PATH),
            format);
        ContentDisposition cd = ContentDisposition.type(format.getDefaultMIMEType()).fileName(Constants.MANIFEST_PATH)
                .build();

        return Response.created(researchObjectURI).entity(manifest).header("Content-disposition", cd).build();
    }
}
