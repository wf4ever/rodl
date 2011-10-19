package pl.psnc.dl.wf4ever;

import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Base64;

import pl.psnc.dl.wf4ever.dlibra.DLibraDataSource;
import pl.psnc.dlibra.service.DLibraException;

/**
 * 
 * @author Piotr Hołubowicz
 *
 */
@Path(Constants.USERS_URL_PART + "/{U_ID}")
public class UserResource
{

	@Context
	HttpServletRequest request;


	@GET
	public Response getUser(@PathParam("U_ID")
	String userId)
		throws RemoteException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		userId = new String(Base64.decodeBase64(userId));

		if (dLibraDataSource.getUsersHelper().userExists(userId)) {
			return Response.ok(userId).build();
		}
		else {
			return Response.status(Status.NOT_FOUND).type("text/plain")
					.entity("User " + userId + " does not exist").build();
		}
	}


	/**
	 * Deletes the workspace.
	 * @param userId identifier of a workspace in the RO SRS
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	@DELETE
	public void deleteUser(@PathParam("U_ID")
	String userId)
		throws RemoteException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		userId = new String(Base64.decodeBase64(userId));

		dLibraDataSource.getUsersHelper().deleteUser(userId);
		dLibraDataSource.getOAuthManager().deleteUserCredentials(userId);
	}
}
