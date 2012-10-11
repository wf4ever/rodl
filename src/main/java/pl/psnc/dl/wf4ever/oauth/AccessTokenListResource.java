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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.auth.AccessToken;
import pl.psnc.dl.wf4ever.auth.AccessTokenList;
import pl.psnc.dl.wf4ever.auth.ForbiddenException;
import pl.psnc.dl.wf4ever.auth.OAuthManager;
import pl.psnc.dl.wf4ever.common.UserProfile;

/**
 * REST API access tokens resource.
 * 
 * @author Piotr Hołubowicz
 * 
 */
@Path("accesstokens")
public class AccessTokenListResource {

    /** logger. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(AccessTokenListResource.class);

    /** HTTP request. */
    @Context
    HttpServletRequest request;

    /** URI info. */
    @Context
    private UriInfo uriInfo;


    /**
     * Returns list of access tokens as XML. The optional parameters are client_id and user_id.
     * 
     * @param clientId
     *            client application id
     * @param userId
     *            Base64, url-safe encoded.
     * @return an access token XML encoded
     */
    @GET
    @Produces("text/xml")
    public AccessTokenList getAccessTokenList(@QueryParam("client_id") String clientId,
            @QueryParam("user_id") String userId) {
        UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
        OAuthManager oauth = new OAuthManager();

        if (user.getRole() != UserProfile.Role.ADMIN) {
            throw new ForbiddenException("Only admin users can manage access tokens.");
        }
        if (userId != null) {
            userId = new String(Base64.decodeBase64(userId));
        }
        List<AccessToken> list = oauth.getAccessTokens(clientId, userId);
        return new AccessTokenList(list);
    }


    /**
     * Creates new access token for a given client and user. input: client_id and user.
     * 
     * @param data
     *            text/plain with id in first line and password in second.
     * @return 201 (Created) when the access token was successfully created, 400 (Bad Request) if the user does not
     *         exist
     */
    @POST
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response createAccessToken(String data) {
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

        try {
            String accessToken = oauth.createAccessToken(lines[0], lines[1]).getToken();
            URI resourceUri = uriInfo.getAbsolutePathBuilder().path("/").build().resolve(accessToken);

            return Response.created(resourceUri).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Status.NOT_FOUND).type("text/plain").entity(e.getMessage()).build();
        }
    }
}
