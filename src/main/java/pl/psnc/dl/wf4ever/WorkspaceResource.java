package pl.psnc.dl.wf4ever;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;
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
import pl.psnc.dl.wf4ever.connection.SemanticMetadataServiceFactory;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibrary;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
import pl.psnc.dlibra.service.DLibraException;

import com.sun.jersey.core.header.ContentDisposition;

/**
 * 
 * @author nowakm
 * 
 */
@Path(URIs.WORKSPACE_ID)
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
	 * @throws NotFoundException
	 */
	@GET
	@Produces("text/plain")
	public Response getWorkspace(@PathParam("W_ID")
	String workspaceId)
		throws RemoteException, DigitalLibraryException, MalformedURLException,
		UnknownHostException, TransformerException, NotFoundException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());
		List<String> list = dl.getResearchObjectIds(workspaceId);

		StringBuilder sb = new StringBuilder();
		for (String id : list) {
			sb.append(uriInfo.getAbsolutePathBuilder().path("/").build()
					.resolve(id).toString());
			sb.append("\r\n");
		}

		ContentDisposition cd = ContentDisposition.type("text/plain")
				.fileName(workspaceId + ".txt").build();

		return Response.ok().entity(sb.toString())
				.header("Content-disposition", cd).build();
	}


	/**
	 * Deletes the workspace.
	 * 
	 * @param workspaceId
	 *            identifier of a workspace in the RO SRS
	 * @throws DigitalLibraryException
	 * @throws NotFoundException
	 * @throws SQLException 
	 * @throws NamingException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	@DELETE
	public void deleteWorkspace(@PathParam("W_ID")
	String workspaceId)
		throws DigitalLibraryException, NotFoundException,
		ClassNotFoundException, IOException, NamingException, SQLException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());

		dl.deleteWorkspace(workspaceId);

		SemanticMetadataService sms = SemanticMetadataServiceFactory
				.getService(user);
		try {
			Set<URI> versions = sms.findManifests(uriInfo.getAbsolutePath());
			for (URI uri : versions) {
				sms.removeManifest(uri);
			}
		}
		finally {
			sms.close();
		}
	}
}
