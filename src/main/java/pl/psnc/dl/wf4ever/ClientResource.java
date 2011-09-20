package pl.psnc.dl.wf4ever;

import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import pl.psnc.dl.wf4ever.auth.ForbiddenException;
import pl.psnc.dl.wf4ever.dlibra.DLibraDataSource;
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
	 * @throws IdNotFoundException 
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	@DELETE
	public void deletAccessToken(@PathParam("C_ID") String clientId)
		throws RemoteException, IdNotFoundException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		if (!dLibraDataSource.isAdmin()) {
			throw new ForbiddenException(
					"Only admin users can manage access tokens.");
		}

		dLibraDataSource.getOAuthManager().deleteClient(clientId);
	}
}
