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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.TransformerException;

import pl.psnc.dl.wf4ever.auth.ForbiddenException;
import pl.psnc.dl.wf4ever.auth.OAuthClient;
import pl.psnc.dl.wf4ever.auth.OAuthClientList;
import pl.psnc.dl.wf4ever.dlibra.DLibraDataSource;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;

/**
 * @author Piotr Hołubowicz
 * 
 */
@Path(Constants.CLIENTS_URL_PART)
public class ClientListResource
{

	@Context
	HttpServletRequest request;

	@Context
	private UriInfo uriInfo;


	/**
	 * Returns list of OAuth clients as XML.
	 * @return TBD
	 * @throws IdNotFoundException 
	 * @throws RemoteException
	 * @throws DLibraException
	 * @throws TransformerException 
	 */
	@GET
	@Produces("text/xml")
	public OAuthClientList getClientList() throws RemoteException, IdNotFoundException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);
		if (!dLibraDataSource.isAdmin()) {
			throw new ForbiddenException(
					"Only admin users can manage access tokens.");
		}
		List<OAuthClient> list = dLibraDataSource.getOAuthManager()
				.getClients();
		return new OAuthClientList(list);
	}


	/**
	 * Creates new OAuth 2.0 client. input: client_id, name and redirection URI.
	 * @param data text/plain with client_id in first line, name in second and URI in third.
	 * @return 201 (Created) when the client was successfully created, 409
	 *         (Conflict) if the client id already exists.
	 * @throws IdNotFoundException 
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	@POST
	@Consumes("text/plain")
	@Produces("text/plain")
	public Response createClient(String data) throws RemoteException, IdNotFoundException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		if (!dLibraDataSource.isAdmin()) {
			throw new ForbiddenException(
					"Only admin users can manage access tokens.");
		}
		String lines[] = data.split("[\\r\\n]+");
		if (lines.length < 3) {
			return Response.status(Status.BAD_REQUEST)
					.entity("Content is shorter than 3 lines")
					.header(Constants.CONTENT_TYPE_HEADER_NAME, "text/plain")
					.build();
		}

		OAuthClient client = new OAuthClient(lines[0], lines[1], lines[2]);
		dLibraDataSource.getOAuthManager().storeClient(client);

		return Response.created(
			uriInfo.getAbsolutePath().resolve(Constants.CLIENTS_URL_PART)
					.resolve(lines[0])).build();
	}
}
