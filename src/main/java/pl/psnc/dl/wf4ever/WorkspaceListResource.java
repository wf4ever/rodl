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
import javax.ws.rs.core.UriInfo;

import pl.psnc.dl.wf4ever.dlibra.DLibraDataSource;
import pl.psnc.dlibra.service.DLibraException;

/**
 * @author Piotr Hołubowicz
 * 
 */
@Path(Constants.WORKSPACES_URL_PART)
public class WorkspaceListResource
{

	@Context
	HttpServletRequest request;

	@Context
	private UriInfo uriInfo;


	/**
	 * Creates new workspace with given WORKSPACE_ID. input: WORKSPACE_ID
	 * @param data text/plain with id in first line and password
	 * in second.
	 * @return 201 (Created) when the workspace was successfully created, 400
	 *         (Bad Request) if the content is malformed 409 (Conflict) if the
	 *         WORKSPACE_ID is already used
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	@POST
	@Consumes("text/plain")
	public Response createWorkspace(String workspaceId)
		throws RemoteException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		dLibraDataSource.getPublicationsHelper().createGroupPublication(
			workspaceId);

		return Response.created(uriInfo.getAbsolutePath().resolve(workspaceId))
				.build();
	}
}
