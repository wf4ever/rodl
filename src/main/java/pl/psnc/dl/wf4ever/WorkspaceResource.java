package pl.psnc.dl.wf4ever;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
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

import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;

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
	 * @throws DigitalLibraryException 
	 * @throws UnknownHostException 
	 * @throws MalformedURLException 
	 * @throws IdNotFoundException 
	 */
	@GET
	@Produces("application/rdf+xml")
	public Response getWorkspace(@PathParam("W_ID")
	String workspaceId)
		throws RemoteException, DigitalLibraryException, MalformedURLException,
		UnknownHostException, TransformerException, IdNotFoundException
	{
		DigitalLibrary dLibraDataSource = ((DigitalLibraryFactory) request
				.getAttribute(Constants.DLFACTORY)).getDigitalLibrary();
		List<String> list = dLibraDataSource.getResearchObjectIds(workspaceId);

		List<URI> links = new ArrayList<URI>(list.size());

		for (String id : list) {
			links.add(uriInfo.getAbsolutePathBuilder().path("/").build()
					.resolve(id));
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
	 * @throws UnknownHostException 
	 * @throws MalformedURLException 
	 * @throws DigitalLibraryException 
	 * @throws IdNotFoundException 
	 */
	@DELETE
	public void deleteWorkspace(@PathParam("W_ID")
	String workspaceId)
		throws RemoteException, MalformedURLException, UnknownHostException,
		DigitalLibraryException, IdNotFoundException
	{
		DigitalLibrary dLibraDataSource = ((DigitalLibraryFactory) request
				.getAttribute(Constants.DLFACTORY)).getDigitalLibrary();

		dLibraDataSource.deleteWorkspace(workspaceId);

	}
}
