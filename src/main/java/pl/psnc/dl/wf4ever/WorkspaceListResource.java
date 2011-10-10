/**
 * 
 */
package pl.psnc.dl.wf4ever;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.TransformerException;

import pl.psnc.dl.wf4ever.dlibra.DLibraDataSource;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.service.DLibraException;

import com.sun.jersey.core.header.ContentDisposition;

/**
 * @author Piotr Hołubowicz
 * 
 */
@Path(Constants.WORKSPACES_URL_PART)
public class WorkspaceListResource {

	@Context
	HttpServletRequest request;

	@Context
	private UriInfo uriInfo;

	/**
	 * Returns list of links to workspaces. Output format is RDF.
	 * 
	 * @param workspaceId
	 *            identifier of a workspace in the RO SRS
	 * @return TBD
	 * @throws RemoteException
	 * @throws DLibraException
	 * @throws TransformerException
	 */
	@GET
	@Produces("application/rdf+xml")
	public Response getWorkspaceList() throws RemoteException, DLibraException,
			TransformerException {
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);
		List<AbstractPublicationInfo> list = dLibraDataSource
				.getPublicationsHelper().listUserGroupPublications(
						Publication.PUB_GROUP_ROOT);

		List<URI> links = new ArrayList<URI>(list.size());

		for (AbstractPublicationInfo info : list) {
			links.add(uriInfo.getAbsolutePathBuilder().path("/").build()
					.resolve(info.getLabel()));
		}

		String responseBody = RdfBuilder.serializeResource(RdfBuilder
				.createCollection(uriInfo.getAbsolutePath(), links));

		ContentDisposition cd = ContentDisposition.type("application/rdf+xml")
				.fileName("workspaces.rdf").build();

		return Response.ok().entity(responseBody)
				.header(Constants.CONTENT_DISPOSITION_HEADER_NAME, cd).build();
	}

	/**
	 * Creates new workspace with given WORKSPACE_ID. input: WORKSPACE_ID
	 * 
	 * @param data
	 *            text/plain with id in first line and password in second.
	 * @return 201 (Created) when the workspace was successfully created, 400
	 *         (Bad Request) if the content is malformed 409 (Conflict) if the
	 *         WORKSPACE_ID is already used
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	@POST
	@Consumes("text/plain")
	public Response createWorkspace(String workspaceId) throws RemoteException,
			DLibraException {
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		dLibraDataSource.getPublicationsHelper().createGroupPublication(
				workspaceId);

		URI resourceUri = uriInfo.getAbsolutePathBuilder().path("/").build()
				.resolve(workspaceId);

		return Response.created(resourceUri).build();
	}
}
