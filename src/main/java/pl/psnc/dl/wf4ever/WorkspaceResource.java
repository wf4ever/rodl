package pl.psnc.dl.wf4ever;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.TransformerException;

import pl.psnc.dl.wf4ever.dlibra.DLibraDataSource;
import pl.psnc.dlibra.metadata.PublicationInfo;
import pl.psnc.dlibra.service.DLibraException;

import com.sun.jersey.core.header.ContentDisposition;

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
	UriInfo uriInfo;


	/**
	 * Returns list of research objects in this workspace.
	 * 
	 * @param workspaceId
	 *            identifier of a workspace in the RO SRS
	 * @return 200 (OK) response code with a rdf file in response body
	 *         containing OAI-ORE aggreagates tags.
	 * @throws RemoteException
	 * @throws DLibraException
	 * @throws TransformerException
	 */
	@GET
	@Produces("application/rdf+xml")
	public Response getListOfVersions(@PathParam("W_ID")
	String workspaceId)
		throws RemoteException, DLibraException, TransformerException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);
		List<PublicationInfo> list = dLibraDataSource.getPublicationsHelper()
				.listPublicationsInGroup(workspaceId);

		List<URI> links = new ArrayList<URI>(list.size());

		for (PublicationInfo info : list) {
			links.add(uriInfo.getAbsolutePathBuilder().path("/").build()
					.resolve(info.getLabel()));
		}

		String responseBody = RdfBuilder.serializeResource(RdfBuilder
				.createCollection(uriInfo.getAbsolutePath(), links));

		ContentDisposition cd = ContentDisposition.type("application/rdf+xml")
				.fileName(workspaceId + ".rdf").build();

		return Response.ok().entity(responseBody)
				.header(Constants.CONTENT_DISPOSITION_HEADER_NAME, cd).build();
	}


	/**
	 * Deletes the workspace.
	 * 
	 * @param workspaceId
	 *            identifier of a workspace in the RO SRS
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

		dLibraDataSource.getPublicationsHelper().deleteGroupPublication(
			workspaceId);

	}
}
