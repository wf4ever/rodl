/**
 * 
 */
package pl.psnc.dl.wf4ever;

import java.rmi.RemoteException;

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
	 * Creates new user with given USER_ID. input: USER_ID and
	 * password. 

	 * @param data text/plain with id in first line and password
	 * in second.
	 * @return 201 (Created) when the user was successfully created, 400
	 *         (Bad Request) if the content is malformed 409 (Conflict) if the
	 *         USER_ID is already used
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	@POST
	@Consumes("text/plain")
	public Response createUser(String data)
		throws RemoteException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		String lines[] = data.split("[\\r\\n]+");
		if (lines.length < 2) {
			return Response.status(Status.BAD_REQUEST)
					.entity("Content is shorter than 2 lines")
					.header(Constants.CONTENT_TYPE_HEADER_NAME, "text/plain")
					.build();
		}
		String userId = lines[0];
		String password = lines[1];
		if (userId.isEmpty()) {
			return Response.status(Status.BAD_REQUEST)
					.entity("User id is empty")
					.header(Constants.CONTENT_TYPE_HEADER_NAME, "text/plain")
					.build();
		}
		// password can be empty
		dLibraDataSource.getUsersHelper().createUser(userId, password);
		dLibraDataSource.getAccessTokenManager().createUserCredentials(userId, password);

		return Response.created(uriInfo.getAbsolutePath().resolve(userId))
				.build();
	}
}
