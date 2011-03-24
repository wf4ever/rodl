package pl.psnc.dl.wf4ever;

import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import pl.psnc.dl.wf4ever.connection.DLibraDataSource;
import pl.psnc.dlibra.service.DLibraException;

/**
 * 
 * @author nowakm
 *
 */
@Path(Constants.WORKSPACES_URL_PART + "/{W_ID}")
public class WorkspaceResource
{

	@Context
	HttpServletRequest request;

	@Context
	private UriInfo uriInfo;


	@PUT
	@Consumes("text/plain")
	public Response createWorkspace(@PathParam("W_ID") String workspaceId,
			String password)
		throws RemoteException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		dLibraDataSource.createUser(workspaceId, password);

		return Response.created(uriInfo.getAbsolutePath()).build();
	}


	@DELETE
	public void deleteWorkspace(@PathParam("W_ID") String workspaceId)
		throws RemoteException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		dLibraDataSource.deleteUser(workspaceId);

	}
}
