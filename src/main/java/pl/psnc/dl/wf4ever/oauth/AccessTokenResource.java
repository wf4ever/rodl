package pl.psnc.dl.wf4ever.oauth;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import pl.psnc.dl.wf4ever.auth.RequestAttribute;
import pl.psnc.dl.wf4ever.db.AccessToken;
import pl.psnc.dl.wf4ever.db.UserProfile;
import pl.psnc.dl.wf4ever.db.dao.AccessTokenDAO;
import pl.psnc.dl.wf4ever.exceptions.ForbiddenException;
import pl.psnc.dl.wf4ever.model.Builder;

import com.sun.jersey.api.NotFoundException;

/**
 * The access token REST API resource.
 * 
 * @author Piotr Hołubowicz
 * 
 */
@Path(("accesstokens" + "/{T_ID}"))
public class AccessTokenResource {

    /** Resource builder. */
    @RequestAttribute("Builder")
    private Builder builder;


    /**
     * Deletes the access token.
     * 
     * @param token
     *            access token
     */
    @DELETE
    public void deletAccessToken(@PathParam("T_ID") String token) {
        if (builder.getUser().getRole() != UserProfile.Role.ADMIN) {
            throw new ForbiddenException("Only admin users can manage access tokens.");
        }

        AccessTokenDAO accessTokenDAO = new AccessTokenDAO();
        AccessToken accessToken = accessTokenDAO.findByValue(token);
        if (accessToken == null) {
            throw new NotFoundException();
        }
        accessTokenDAO.delete(accessToken);
    }
}
