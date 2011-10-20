package pl.psnc.dl.wf4ever;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import pl.psnc.dl.wf4ever.auth.ForbiddenException;
import pl.psnc.dl.wf4ever.auth.OAuthClient;
import pl.psnc.dl.wf4ever.auth.OAuthManager;
import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;

/**
 * 
 * @author Piotr Hołubowicz
 *
 */
@Path(Constants.CLIENTS_URL_PART + "/{C_ID}")
public class ClientResource
{

	@Context
	HttpServletRequest request;

	@Context
	UriInfo uriInfo;


	/**
	 * Deletes the OAuth 2.0 client.
	 * @param clientId client id
	 * @return 
	 * @throws IdNotFoundException 
	 * @throws RemoteException
	 * @throws UnknownHostException 
	 * @throws MalformedURLException 
	 * @throws DigitalLibraryException 
	 * @throws DLibraException
	 */
	@GET
	public OAuthClient getClient(@PathParam("C_ID")
	String clientId)
		throws RemoteException, IdNotFoundException, MalformedURLException,
		UnknownHostException, DigitalLibraryException
	{
		DigitalLibrary dLibraDataSource = ((DigitalLibraryFactory) request
				.getAttribute(Constants.DLFACTORY)).getDigitalLibrary();
		OAuthManager oauth = (OAuthManager) request
				.getAttribute(Constants.OAUTH_MANAGER);

		if (!dLibraDataSource.getUserProfile().isAdmin()) {
			throw new ForbiddenException("Only admin users can manage clients.");
		}

		return oauth.getClient(clientId);
	}


	/**
	 * Deletes the OAuth 2.0 client.
	 * @param clientId client id
	 * @throws IdNotFoundException 
	 * @throws RemoteException
	 * @throws UnknownHostException 
	 * @throws MalformedURLException 
	 * @throws DigitalLibraryException 
	 * @throws DLibraException
	 */
	@DELETE
	public void deleteClient(@PathParam("C_ID")
	String clientId)
		throws RemoteException, IdNotFoundException, MalformedURLException,
		UnknownHostException, DigitalLibraryException
	{
		DigitalLibrary dLibraDataSource = ((DigitalLibraryFactory) request
				.getAttribute(Constants.DLFACTORY)).getDigitalLibrary();
		OAuthManager oauth = (OAuthManager) request
				.getAttribute(Constants.OAUTH_MANAGER);

		if (!dLibraDataSource.getUserProfile().isAdmin()) {
			throw new ForbiddenException("Only admin users can manage clients.");
		}

		oauth.deleteClient(clientId);
	}
}
