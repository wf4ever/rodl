package pl.psnc.dl.wf4ever.rosrs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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
import pl.psnc.dl.wf4ever.auth.AuthenticationException;
import pl.psnc.dl.wf4ever.auth.SecurityFilter;
import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
import pl.psnc.dl.wf4ever.connection.SemanticMetadataServiceFactory;
import pl.psnc.dl.wf4ever.dlibra.ConflictException;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibrary;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dl.wf4ever.dlibra.UserProfile.Role;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
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
            SemanticMetadataService sms = SemanticMetadataServiceFactory.getService(user);
            try {
                list = sms.findResearchObjects(uriInfo.getAbsolutePath());
            } finally {
                sms.close();
            }
        } else {
            DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(user.getLogin(), user.getPassword());
            list = new HashSet<URI>();
            for (String wId : dl.getWorkspaceIds()) {
                for (String rId : dl.getResearchObjectIds(wId)) {
                    for (String vId : dl.getVersionIds(wId, rId)) {
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
    @Consumes("text/plain")
    public Response createResearchObject()
            throws NotFoundException, DigitalLibraryException, ClassNotFoundException, IOException, NamingException,
            SQLException, ConflictException, BadRequestException {
        UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
        if (user.getRole() == UserProfile.Role.PUBLIC) {
            throw new AuthenticationException("Only authenticated users can do that.", SecurityFilter.REALM);
        }
        String researchObjectId = request.getHeader(Constants.SLUG_HEADER);
        if (researchObjectId == null || researchObjectId.isEmpty()) {
            throw new BadRequestException("Research object ID is null or empty");
        }
        URI researchObjectURI = uriInfo.getAbsolutePathBuilder().path(researchObjectId).path("/").build();

        InputStream manifest;
        SemanticMetadataService sms = SemanticMetadataServiceFactory.getService(user);
        try {
            try {
                sms.createResearchObject(researchObjectURI);
                manifest = sms.getManifest(researchObjectURI.resolve(".ro/manifest.rdf"), RDFFormat.RDFXML);
            } catch (IllegalArgumentException e) {
                // RO already existed in sms, maybe created by someone else
                throw new ConflictException("The RO with identifier " + researchObjectId + " already exists");
            }

            DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(user.getLogin(), user.getPassword());

            try {
                dl.createWorkspace(Constants.workspaceId);
            } catch (ConflictException e) {
                // nothing
            }
            try {
                dl.createResearchObject(Constants.workspaceId, researchObjectId);
            } catch (ConflictException e) {
                // nothing
            }
            dl.createVersion(Constants.workspaceId, researchObjectId, Constants.versionId, manifest,
                ".ro/manifest.rdf", RDFFormat.RDFXML.getDefaultMIMEType());
            dl.publishVersion(Constants.workspaceId, researchObjectId, Constants.versionId);
        } finally {
            sms.close();
        }

        return Response.created(researchObjectURI).build();
    }

}
