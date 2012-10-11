package pl.psnc.dl.wf4ever.oauth;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.auth.ForbiddenException;
import pl.psnc.dl.wf4ever.auth.OAuthManager;
import pl.psnc.dl.wf4ever.common.UserProfile;

/**
 * The access token REST API resource.
 * 
 * @author Piotr Hołubowicz
 * 
 */
@Path(("accesstokens" + "/{T_ID}"))
public class AccessTokenResource {

    /** HTTP request. */
    @Context
    HttpServletRequest request;


    /**
     * Deletes the access token.
     * 
     * @param token
     *            access token
     */
    @DELETE
    public void deletAccessToken(@PathParam("T_ID") String token) {
        UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
        OAuthManager oauth = new OAuthManager();

        if (user.getRole() != UserProfile.Role.ADMIN) {
            throw new ForbiddenException("Only admin users can manage access tokens.");
        }

        oauth.deleteToken(token);
    }
}
