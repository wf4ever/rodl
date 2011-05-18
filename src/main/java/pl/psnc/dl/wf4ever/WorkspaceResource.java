package pl.psnc.dl.wf4ever;

import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import pl.psnc.dl.wf4ever.connection.DLibraDataSourceInterface;
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

	/**
	 * Deletes the workspace.
	 * @param workspaceId identifier of a workspace in the RO SRS
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	@DELETE
	public void deleteWorkspace(@PathParam("W_ID") String workspaceId)
		throws RemoteException, DLibraException
	{
		DLibraDataSourceInterface dLibraDataSource = (DLibraDataSourceInterface) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		dLibraDataSource.deleteUser(workspaceId);

	}
}
