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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Base64;

import pl.psnc.dl.wf4ever.auth.OAuthManager;
import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
import pl.psnc.dlibra.service.IdNotFoundException;

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
		throws RemoteException, MalformedURLException, UnknownHostException,
		DigitalLibraryException, IdNotFoundException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());

		userId = new String(Base64.decodeBase64(userId));

		if (dl.userExists(userId)) {
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
	 * @throws UnknownHostException 
	 * @throws MalformedURLException 
	 * @throws DigitalLibraryException 
	 * @throws IdNotFoundException 
	 */
	@DELETE
	public void deleteUser(@PathParam("U_ID")
	String userId)
		throws RemoteException, MalformedURLException, UnknownHostException,
		DigitalLibraryException, IdNotFoundException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		OAuthManager oauth = new OAuthManager();
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());

		userId = new String(Base64.decodeBase64(userId));

		dl.deleteUser(userId);
		oauth.deleteUserCredentials(userId);
	}
}
