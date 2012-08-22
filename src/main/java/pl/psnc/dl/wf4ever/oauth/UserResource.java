package pl.psnc.dl.wf4ever.oauth;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.auth.OAuthManager;
import pl.psnc.dl.wf4ever.connection.SemanticMetadataServiceFactory;
import pl.psnc.dl.wf4ever.dlibra.ConflictException;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;
import pl.psnc.dl.wf4ever.sms.QueryResult;

/**
 * 
 * @author Piotr Hołubowicz
 * 
 */
@Path(("users/{U_ID}"))
public class UserResource {

    private final static Logger logger = Logger.getLogger(UserResource.class);

    @Context
    HttpServletRequest request;

    @Context
    UriInfo uriInfo;


    @GET
    public Response getUserRdfXml(@PathParam("U_ID") String urlSafeUserId)
            throws DigitalLibraryException, NotFoundException, ClassNotFoundException, IOException, NamingException,
            SQLException, URISyntaxException {
        return getUser(urlSafeUserId, RDFFormat.RDFXML);
    }


    @GET
    @Produces({ "application/x-turtle", "text/turtle" })
    public Response getUserTurtle(@PathParam("U_ID") String urlSafeUserId)
            throws DigitalLibraryException, NotFoundException, ClassNotFoundException, IOException, NamingException,
            SQLException, URISyntaxException {
        return getUser(urlSafeUserId, RDFFormat.TURTLE);
    }


    @GET
    @Produces("text/rdf+n3")
    public Response getUserN3(@PathParam("U_ID") String urlSafeUserId)
            throws DigitalLibraryException, NotFoundException, ClassNotFoundException, IOException, NamingException,
            SQLException, URISyntaxException {
        return getUser(urlSafeUserId, RDFFormat.N3);
    }


    private Response getUser(@PathParam("U_ID") String urlSafeUserId, RDFFormat rdfFormat)
            throws DigitalLibraryException, NotFoundException, ClassNotFoundException, IOException, NamingException,
            SQLException, URISyntaxException {
        String userId = new String(Base64.decodeBase64(urlSafeUserId));

        QueryResult qs = ROSRService.SMS.get().getUser(UserProfile.generateAbsoluteURI(null, userId), rdfFormat);

        if (ROSRService.DL.get().userExists(userId)) {
            return Response.ok(qs.getInputStream()).type(qs.getFormat().getDefaultMIMEType()).build();
        } else {
            return Response.status(Status.NOT_FOUND).type("text/plain").entity("User " + userId + " does not exist")
                    .build();
        }
    }


    /**
     * Creates new user with given USER_ID. input: USER_ID (the password is generated internally).
     * 
     * @param user
     *            id, base64 url-safe encoded
     * @return 201 (Created) when the user was successfully created, 200 OK if it was updated, 400 (Bad Request) if the
     *         content is malformed 409 (Conflict) if the USER_ID is already used
     * @throws DigitalLibraryException
     * @throws ConflictException
     * @throws NotFoundException
     * @throws SQLException
     * @throws NamingException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @PUT
    @Consumes("text/plain")
    public Response createUser(@PathParam("U_ID") String urlSafeUserId, String username)
            throws DigitalLibraryException, NotFoundException, ConflictException, ClassNotFoundException, IOException,
            NamingException, SQLException {
        OAuthManager oauth = new OAuthManager();

        String userId = new String(Base64.decodeBase64(urlSafeUserId));

        try {
            new URI(userId);
        } catch (URISyntaxException e) {
            logger.warn("URI " + userId + " is not valid", e);
        }

        String password = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 20);
        boolean created = ROSRService.DL.get().createUser(userId, password,
            username != null && !username.isEmpty() ? username : userId);
        oauth.createUserCredentials(userId, password);
        SemanticMetadataServiceFactory.getService(
            new UserProfile(userId, username != null && !username.isEmpty() ? username : userId, null)).close();

        if (created) {
            return Response.created(uriInfo.getAbsolutePath()).build();
        } else {
            return Response.ok().build();
        }
    }


    /**
     * Deletes the workspace.
     * 
     * @param userId
     *            identifier of a workspace in the RO SRS
     * @throws DigitalLibraryException
     * @throws NotFoundException
     * @throws SQLException
     * @throws NamingException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws URISyntaxException
     */
    @DELETE
    public void deleteUser(@PathParam("U_ID") String urlSafeUserId)
            throws DigitalLibraryException, NotFoundException, ClassNotFoundException, IOException, NamingException,
            SQLException, URISyntaxException {
        OAuthManager oauth = new OAuthManager();

        String userId = new String(Base64.decodeBase64(urlSafeUserId));

        List<String> list = ROSRService.DL.get().getWorkspaceIds();
        for (String workspaceId : list) {
            ROSRService.DL.get().deleteWorkspace(workspaceId);
            Set<URI> versions = ROSRService.SMS.get().findResearchObjectsByPrefix(
                uriInfo.getBaseUriBuilder().path("workspaces").path(workspaceId).build());
            for (URI uri : versions) {
                ROSRService.SMS.get().removeResearchObject(uri);
            }
        }
        ROSRService.SMS.get().removeUser(new URI(userId));

        ROSRService.DL.get().deleteUser(userId);
        oauth.deleteUserCredentials(userId);
    }
}
