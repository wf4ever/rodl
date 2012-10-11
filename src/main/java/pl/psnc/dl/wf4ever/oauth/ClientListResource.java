/**
 * 
 */
package pl.psnc.dl.wf4ever.oauth;

import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.auth.ForbiddenException;
import pl.psnc.dl.wf4ever.auth.OAuthClient;
import pl.psnc.dl.wf4ever.auth.OAuthClientList;
import pl.psnc.dl.wf4ever.auth.OAuthManager;
import pl.psnc.dl.wf4ever.common.UserProfile;

/**
 * OAuth client list REST API resource.
 * 
 * @author Piotr Hołubowicz
 * 
 */
@Path("clients")
public class ClientListResource {

    /** HTTP request. */
    @Context
    HttpServletRequest request;

    /** URI info. */
    @Context
    private UriInfo uriInfo;


    /**
     * Returns list of OAuth clients as XML.
     * 
     * @return An XML serialization of a list of client application DAOs.
     */
    @GET
    @Produces("text/xml")
    public OAuthClientList getClientList() {
        UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
        OAuthManager oauth = new OAuthManager();

        if (user.getRole() != UserProfile.Role.ADMIN) {
            throw new ForbiddenException("Only admin users can manage access tokens.");
        }
        List<OAuthClient> list = oauth.getClients();
        return new OAuthClientList(list);
    }


    /**
     * Creates new OAuth 2.0 client. input: name and redirection URI.
     * 
     * @param data
     *            text/plain with name in first line and URI in second.
     * @return 201 (Created) when the client was successfully created, 409 (Conflict) if the client id already exists.
     */
    @POST
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response createClient(String data) {
        UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
        OAuthManager oauth = new OAuthManager();

        if (user.getRole() != UserProfile.Role.ADMIN) {
            throw new ForbiddenException("Only admin users can manage access tokens.");
        }
        String[] lines = data.split("[\\r\\n]+");
        if (lines.length < 2) {
            return Response.status(Status.BAD_REQUEST).entity("Content is shorter than 2 lines")
                    .header("Content-type", "text/plain").build();
        }

        String clientId = oauth.createClient(lines[0], lines[1]);

        URI resourceUri = uriInfo.getAbsolutePathBuilder().path("/").build().resolve(clientId);

        return Response.created(resourceUri).build();
    }
}
