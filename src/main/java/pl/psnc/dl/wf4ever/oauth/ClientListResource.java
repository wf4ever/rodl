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

import pl.psnc.dl.wf4ever.auth.ForbiddenException;
import pl.psnc.dl.wf4ever.auth.OAuthClient;
import pl.psnc.dl.wf4ever.auth.OAuthClientList;
import pl.psnc.dl.wf4ever.auth.RequestAttribute;
import pl.psnc.dl.wf4ever.common.UserProfile;
import pl.psnc.dl.wf4ever.dl.UserMetadata;

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

    /** Authenticated user. */
    @RequestAttribute("User")
    private UserMetadata user;


    /**
     * Returns list of OAuth clients as XML.
     * 
     * @return An XML serialization of a list of client application DAOs.
     */
    @GET
    @Produces("text/xml")
    public OAuthClientList getClientList() {
        if (user.getRole() != UserProfile.Role.ADMIN) {
            throw new ForbiddenException("Only admin users can manage access tokens.");
        }
        List<OAuthClient> list = OAuthClient.findAll();
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
        if (user.getRole() != UserProfile.Role.ADMIN) {
            throw new ForbiddenException("Only admin users can manage access tokens.");
        }
        String[] lines = data.split("[\\r\\n]+");
        if (lines.length < 2) {
            return Response.status(Status.BAD_REQUEST).entity("Content is shorter than 2 lines")
                    .header("Content-type", "text/plain").build();
        }

        OAuthClient client = new OAuthClient(lines[0], lines[1]);
        client.save();

        URI resourceUri = uriInfo.getAbsolutePathBuilder().path("/").build().resolve(client.getClientId());

        return Response.created(resourceUri).build();
    }
}
