package pl.psnc.dl.wf4ever;

import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import pl.psnc.dl.wf4ever.auth.ForbiddenException;
import pl.psnc.dl.wf4ever.auth.OAuthManager;
import pl.psnc.dl.wf4ever.dlibra.DLibraDataSource;
import pl.psnc.dlibra.service.DLibraException;

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
	 * @throws DLibraException
	 */
	@DELETE
	public void deletAccessToken(@PathParam("T_ID")
	String token)
		throws RemoteException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);
		OAuthManager oauth = (OAuthManager) request
				.getAttribute(Constants.OAUTH_MANAGER);

		if (!dLibraDataSource.isAdmin()) {
			throw new ForbiddenException(
					"Only admin users can manage access tokens.");
		}

		oauth.deleteToken(token);
	}
}
