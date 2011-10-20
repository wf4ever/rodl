package pl.psnc.dl.wf4ever;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import pl.psnc.dl.wf4ever.auth.ForbiddenException;
import pl.psnc.dl.wf4ever.auth.OAuthManager;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;

/**
 * 
 * @author Piotr Hołubowicz
 *
 */
@Path(Constants.ACCESSTOKEN_URL_PART + "/{T_ID}")
public class AccessTokenResource
{

	@Context
	HttpServletRequest request;

	@Context
	UriInfo uriInfo;


	/**
	 * Deletes the access token.
	 * @param workspaceId identifier of a workspace in the RO SRS
	 * @throws RemoteException
	 * @throws DigitalLibraryException 
	 * @throws UnknownHostException 
	 * @throws MalformedURLException 
	 * @throws IdNotFoundException 
	 * @throws DLibraException
	 */
	@DELETE
	public void deletAccessToken(@PathParam("T_ID")
	String token)
		throws RemoteException, DigitalLibraryException, MalformedURLException,
		UnknownHostException, IdNotFoundException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		OAuthManager oauth = new OAuthManager();

		if (!user.isAdmin()) {
			throw new ForbiddenException(
					"Only admin users can manage access tokens.");
		}

		oauth.deleteToken(token);
	}
}
