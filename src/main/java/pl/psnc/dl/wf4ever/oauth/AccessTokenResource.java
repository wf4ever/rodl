package pl.psnc.dl.wf4ever.oauth;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import pl.psnc.dl.wf4ever.auth.AccessToken;
import pl.psnc.dl.wf4ever.auth.ForbiddenException;
import pl.psnc.dl.wf4ever.auth.RequestAttribute;
import pl.psnc.dl.wf4ever.common.db.UserProfile;
import pl.psnc.dl.wf4ever.dl.UserMetadata;

import com.sun.jersey.api.NotFoundException;

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

    /** Authenticated user. */
    @RequestAttribute("User")
    private UserMetadata user;


    /**
     * Deletes the access token.
     * 
     * @param token
     *            access token
     */
    @DELETE
    public void deletAccessToken(@PathParam("T_ID") String token) {
        if (user.getRole() != UserProfile.Role.ADMIN) {
            throw new ForbiddenException("Only admin users can manage access tokens.");
        }

        AccessToken accessToken = AccessToken.findByValue(token);
        if (accessToken == null) {
            throw new NotFoundException();
        }
        accessToken.delete();
    }
}
