/**
 * 
 */
package pl.psnc.dl.wf4ever.rosrsfull;

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
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.TransformerException;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
import pl.psnc.dl.wf4ever.dlibra.ConflictException;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibrary;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dlibra.service.DLibraException;

import com.sun.jersey.core.header.ContentDisposition;

/**
 * @author Piotr Hołubowicz
 * 
 */
@Path("workspaces")
public class WorkspaceListResource
{

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
	 * @throws DigitalLibraryException
	 * @throws UnknownHostException
	 * @throws MalformedURLException
	 * @throws NotFoundException
	 */
	@GET
	@Produces("text/plain")
	public Response getWorkspaceList()
		throws DigitalLibraryException, TransformerException, RemoteException,
		MalformedURLException, UnknownHostException, NotFoundException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());
		List<String> list = dl.getWorkspaceIds();

		StringBuilder sb = new StringBuilder();
		for (String id : list) {
			sb.append(uriInfo.getAbsolutePathBuilder().path("/").build()
					.resolve(id).toString());
			sb.append("\r\n");
		}

		ContentDisposition cd = ContentDisposition.type("application/rdf+xml")
				.fileName("workspaces.txt").build();

		return Response.ok().entity(sb.toString())
				.header("Content-disposition", cd).build();
	}


	/**
	 * Creates new workspace with given WORKSPACE_ID. input: WORKSPACE_ID
	 * 
	 * @param data
	 *            text/plain with id in first line and password in second.
	 * @return 201 (Created) when the workspace was successfully created, 400
	 *         (Bad Request) if the content is malformed 409 (Conflict) if the
	 *         WORKSPACE_ID is already used
	 * @throws DigitalLibraryException
	 * @throws UnknownHostException
	 * @throws MalformedURLException
	 * @throws RemoteException
	 * @throws NotFoundException
	 * @throws ConflictException 
	 */
	@POST
	@Consumes("text/plain")
	public Response createWorkspace(String workspaceId)
		throws DigitalLibraryException, RemoteException, MalformedURLException,
		UnknownHostException, NotFoundException, ConflictException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());

		dl.createWorkspace(workspaceId);

		URI resourceUri = uriInfo.getAbsolutePathBuilder().path("/").build()
				.resolve(workspaceId);

		return Response.created(resourceUri).build();
	}
}
