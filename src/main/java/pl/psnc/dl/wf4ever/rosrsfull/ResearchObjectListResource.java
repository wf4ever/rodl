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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibrary;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;

import com.sun.jersey.core.header.ContentDisposition;

/**
 * 
 * @author nowakm
 * 
 */
@Path(("workspaces" + "/{W_ID}" + "/ROs"))
public class ResearchObjectListResource
{

	@SuppressWarnings("unused")
	private final static Logger logger = Logger
			.getLogger(ResearchObjectListResource.class);

	@Context
	HttpServletRequest request;

	@Context
	UriInfo uriInfo;


	/**
	 * Returns list of links to research objects. Output format is TBD.
	 * 
	 * @param workspaceId
	 *            identifier of a workspace in the RO SRS
	 * @return TBD
	 * @throws RemoteException
	 * @throws TransformerException
	 * @throws UnknownHostException
	 * @throws MalformedURLException
	 * @throws DigitalLibraryException
	 * @throws NotFoundException
	 */
	@GET
	@Produces("text/plain")
	public Response getResearchObjectList(@PathParam("W_ID")
	String workspaceId)
		throws RemoteException, DLibraException, TransformerException,
		MalformedURLException, UnknownHostException, DigitalLibraryException,
		NotFoundException
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
	 * Creates new RO with given RO_ID.
	 * 
	 * @param workspaceId
	 *            identifier of a workspace in the RO SRS
	 * @param researchObjectId
	 *            RO_ID in plain text (text/plain)
	 * @return 201 (Created) when the RO was successfully created, 409
	 *         (Conflict) if the RO_ID is already used in the WORKSPACE_ID
	 *         workspace
	 * @throws RemoteException
	 * @throws UnknownHostException
	 * @throws MalformedURLException
	 * @throws DigitalLibraryException
	 * @throws NotFoundException
	 * @throws IdNotFoundException
	 */
	@POST
	@Consumes("text/plain")
	public Response createResearchObject(@PathParam("W_ID")
	String workspaceId, String researchObjectId)
		throws RemoteException, MalformedURLException, UnknownHostException,
		DigitalLibraryException, NotFoundException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());

		dl.createResearchObject(workspaceId, researchObjectId);

		URI resourceUri = uriInfo.getAbsolutePathBuilder().path("/").build()
				.resolve(researchObjectId);

		return Response.created(resourceUri).build();
	}

}
