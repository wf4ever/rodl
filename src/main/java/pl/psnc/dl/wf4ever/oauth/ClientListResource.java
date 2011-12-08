/**
 * 
 */
package pl.psnc.dl.wf4ever.oauth;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.TransformerException;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.auth.ForbiddenException;
import pl.psnc.dl.wf4ever.auth.OAuthClient;
import pl.psnc.dl.wf4ever.auth.OAuthClientList;
import pl.psnc.dl.wf4ever.auth.OAuthManager;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;

/**
 * @author Piotr Hołubowicz
 * 
 */
@Path("clients")
public class ClientListResource {

	@Context
	HttpServletRequest request;

	@Context
	private UriInfo uriInfo;

	/**
	 * Returns list of OAuth clients as XML.
	 * 
	 * @return TBD
	 * @throws IdNotFoundException
	 * @throws RemoteException
	 * @throws UnknownHostException
	 * @throws MalformedURLException
	 * @throws DigitalLibraryException
	 * @throws DLibraException
	 * @throws TransformerException
	 */
	@GET
	@Produces("text/xml")
	public OAuthClientList getClientList() throws RemoteException, IdNotFoundException, MalformedURLException,
			UnknownHostException, DigitalLibraryException {
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		OAuthManager oauth = new OAuthManager();

		if (!user.isAdmin()) {
			throw new ForbiddenException("Only admin users can manage access tokens.");
		}
		List<OAuthClient> list = oauth.getClients();
		return new OAuthClientList(list);
	}

	/**
	 * Creates new OAuth 2.0 client. input: name and redirection URI.
	 * 
	 * @param data
	 *            text/plain with name in first line and URI in second.
	 * @return 201 (Created) when the client was successfully created, 409
	 *         (Conflict) if the client id already exists.
	 * @throws IdNotFoundException
	 * @throws RemoteException
	 * @throws UnknownHostException
	 * @throws MalformedURLException
	 * @throws DigitalLibraryException
	 * @throws DLibraException
	 */
	@POST
	@Consumes("text/plain")
	@Produces("text/plain")
	public Response createClient(String data) throws RemoteException, IdNotFoundException, MalformedURLException,
			UnknownHostException, DigitalLibraryException {
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		OAuthManager oauth = new OAuthManager();

		if (!user.isAdmin()) {
			throw new ForbiddenException("Only admin users can manage access tokens.");
		}
		String lines[] = data.split("[\\r\\n]+");
		if (lines.length < 2) {
			return Response.status(Status.BAD_REQUEST).entity("Content is shorter than 2 lines")
					.header("Content-type", "text/plain").build();
		}

		String clientId = oauth.createClient(lines[0], lines[1]);

		URI resourceUri = uriInfo.getAbsolutePathBuilder().path("/").build().resolve(clientId);

		return Response.created(resourceUri).build();
	}
}
