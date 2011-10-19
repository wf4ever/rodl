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

import pl.psnc.dl.wf4ever.dlibra.DLibraDataSource;
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


	@GET
	public Response getWorkspace(@PathParam("W_ID")
	String workspaceId)
		throws RemoteException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		if (dLibraDataSource.getUsersHelper().userExists(workspaceId)) {
			return Response.ok(workspaceId).build();
		}
		else {
			return Response.status(Status.NOT_FOUND).type("text/plain")
					.entity("Workspace " + workspaceId + " does not exist")
					.build();
		}
	}


	/**
	 * Deletes the workspace.
	 * @param workspaceId identifier of a workspace in the RO SRS
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	@DELETE
	public void deleteWorkspace(@PathParam("W_ID")
	String workspaceId)
		throws RemoteException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		dLibraDataSource.getUsersHelper().deleteUser(workspaceId);

	}
}
