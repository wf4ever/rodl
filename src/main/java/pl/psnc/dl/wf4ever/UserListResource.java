/**
 * 
 */
package pl.psnc.dl.wf4ever;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
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

import org.apache.commons.codec.binary.Base64;

import pl.psnc.dl.wf4ever.auth.OAuthManager;
import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibrary;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dlibra.service.DuplicatedValueException;
import pl.psnc.dlibra.service.IdNotFoundException;

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

	 * @param user id, base64 url-safe encoded
	 * @return 201 (Created) when the user was successfully created, 400
	 *         (Bad Request) if the content is malformed 409 (Conflict) if the
	 *         USER_ID is already used
	 * @throws RemoteException
	 * @throws DigitalLibraryException 
	 * @throws UnknownHostException 
	 * @throws MalformedURLException 
	 * @throws IdNotFoundException 
	 * @throws DuplicatedValueException 
	 */
	@POST
	@Consumes("text/plain")
	public Response createUser(String userId)
		throws RemoteException, DigitalLibraryException, MalformedURLException,
		UnknownHostException, IdNotFoundException, DuplicatedValueException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		OAuthManager oauth = new OAuthManager();
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());

		userId = new String(Base64.decodeBase64(userId));

		if (userId == null || userId.isEmpty()) {
			return Response.status(Status.BAD_REQUEST)
					.entity("User id is null or empty")
					.header(Constants.CONTENT_TYPE_HEADER_NAME, "text/plain")
					.build();
		}
		String password = UUID.randomUUID().toString().replaceAll("-", "")
				.substring(0, 20);
		dl.createUser(userId, password);
		oauth.createUserCredentials(userId, password);

		URI resourceUri = uriInfo.getAbsolutePathBuilder().path("/").build()
				.resolve(userId);
		return Response.created(resourceUri).build();
	}
}
