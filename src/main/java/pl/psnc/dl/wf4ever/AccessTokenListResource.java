/**
 * 
 */
package pl.psnc.dl.wf4ever;

import java.rmi.RemoteException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.TransformerException;

import pl.psnc.dl.wf4ever.auth.AccessToken;
import pl.psnc.dl.wf4ever.auth.AccessTokenList;
import pl.psnc.dl.wf4ever.dlibra.DLibraDataSource;
import pl.psnc.dlibra.service.DLibraException;

/**
 * @author Piotr Hołubowicz
 * 
 */
@Path(Constants.ACCESSTOKEN_URL_PART)
public class AccessTokenListResource
{

	@Context
	HttpServletRequest request;

	@Context
	private UriInfo uriInfo;


	/**
	 * Returns list of access tokens as XML. The optional parameters are client_id and user.
	 * @param workspaceId identifier of a workspace in the RO SRS
	 * @return TBD
	 * @throws RemoteException
	 * @throws DLibraException
	 * @throws TransformerException 
	 */
	@GET
	@Produces("text/xml")
	public AccessTokenList getAccessTokenList(
			@QueryParam("client_id") String clientId,
			@QueryParam("user") String user)
		throws RemoteException, DLibraException, TransformerException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);
		List<AccessToken> list = dLibraDataSource.getAccessTokenManager()
				.getAccessTokens(clientId, user);
		return new AccessTokenList(list);
	}


	/**
	 * Creates new access token for a given client and user. input: client_id and user.
	 * @param data text/plain with id in first line and password
	 * in second.
	 * @return 201 (Created) when the access token was successfully created, 400
	 *         (Bad Request) if the user does not exist
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	@POST
	@Consumes("text/plain")
	@Produces("text/plain")
	public Response createAccessToken(String data)
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

		String accessToken = dLibraDataSource.getAccessTokenManager()
				.createAccessToken(lines[0], lines[1]).getToken();
		
		return Response.created(uriInfo.getAbsolutePath().resolve(Constants.ACCESSTOKEN_URL_PART).resolve(accessToken))
				.build();
	}
}
