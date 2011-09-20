/**
 * 
 */
package pl.psnc.dl.wf4ever;

import java.rmi.RemoteException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import pl.psnc.dl.wf4ever.dlibra.DLibraDataSource;
import pl.psnc.dlibra.service.DLibraException;

/**
 * @author Piotr Hołubowicz
 * 
 */
@Path(Constants.USERS_URL_PART)
public class UserListResource
{

	@Context
	HttpServletRequest request;

	@Context
	private UriInfo uriInfo;


	/**
	 * Creates new user with given USER_ID. input: USER_ID (the password is generated internally).

	 * @param data text/plain with id in first line
	 * in second.
	 * @return 201 (Created) when the user was successfully created, 400
	 *         (Bad Request) if the content is malformed 409 (Conflict) if the
	 *         USER_ID is already used
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	@POST
	@Consumes("text/plain")
	public Response createUser(String userId)
		throws RemoteException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		if (userId == null || userId.isEmpty()) {
			return Response.status(Status.BAD_REQUEST)
					.entity("User id is null or empty")
					.header(Constants.CONTENT_TYPE_HEADER_NAME, "text/plain")
					.build();
		}
		String password = UUID.randomUUID().toString().substring(0, 20);
		dLibraDataSource.getUsersHelper().createUser(userId, password);
		dLibraDataSource.getOAuthManager().createUserCredentials(userId, password);

		return Response.created(uriInfo.getAbsolutePath().resolve(userId))
				.build();
	}
}
