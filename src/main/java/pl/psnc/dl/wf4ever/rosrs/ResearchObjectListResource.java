package pl.psnc.dl.wf4ever.rosrs;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.Set;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
import pl.psnc.dl.wf4ever.connection.SemanticMetadataServiceFactory;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibrary;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
import pl.psnc.dlibra.service.IdNotFoundException;

import com.sun.jersey.core.header.ContentDisposition;

/**
 * 
 * @author piotrhol
 * 
 */
@Path("ROs/")
public class ResearchObjectListResource
{

	@SuppressWarnings("unused")
	private final static Logger logger = Logger
			.getLogger(ResearchObjectListResource.class);

	private static final String workspaceId = "default";

	private static final String versionId = "v1";

	@Context
	HttpServletRequest request;

	@Context
	UriInfo uriInfo;


	/**
	 * Returns list of relative links to research objects
	 * 
	 * @return TBD
	 * @throws SQLException 
	 * @throws NamingException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	@GET
	@Produces("text/plain")
	public Response getResearchObjectList()
		throws ClassNotFoundException, IOException, NamingException,
		SQLException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		SemanticMetadataService sms = SemanticMetadataServiceFactory
				.getService(user);
		StringBuilder sb = new StringBuilder();
		try {
			Set<URI> list = sms.findResearchObjects(uriInfo.getAbsolutePath());

			for (URI id : list) {
				sb.append(id.toString());
				sb.append("\r\n");
			}
		}
		finally {
			sms.close();
		}

		ContentDisposition cd = ContentDisposition.type("text/plain")
				.fileName("ROs.txt").build();

		return Response.ok().entity(sb.toString())
				.header("Content-disposition", cd).build();
	}


	/**
	 * Creates new RO with given RO_ID.
	 * 
	 * @param researchObjectId
	 *            RO_ID in plain text (text/plain)
	 * @return 201 (Created) when the RO was successfully created, 409
	 *         (Conflict) if the RO_ID is already used in the WORKSPACE_ID
	 *         workspace
	 * @throws DigitalLibraryException
	 * @throws NotFoundException
	 * @throws SQLException 
	 * @throws NamingException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws IdNotFoundException
	 */
	@POST
	@Consumes("text/plain")
	public Response createResearchObject(String researchObjectId)
		throws NotFoundException, DigitalLibraryException,
		ClassNotFoundException, IOException, NamingException, SQLException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());

		try {
			dl.createWorkspace(workspaceId);
		}
		catch (DigitalLibraryException e) {
			// nothing
		}
		try {
			dl.createResearchObject(workspaceId, researchObjectId);
		}
		catch (DigitalLibraryException e) {
			// nothing
		}
		dl.createVersion(workspaceId, researchObjectId, versionId);

		URI resourceURI = uriInfo.getAbsolutePathBuilder()
				.path(researchObjectId).build();
		URI researchObjectURI = uriInfo.getAbsolutePathBuilder().path(researchObjectId)
				.build();

		SemanticMetadataService sms = SemanticMetadataServiceFactory
				.getService(user);
		try {
			sms.createResearchObject(researchObjectURI);
		}
		finally {
			sms.close();
		}

		return Response.created(resourceURI).build();
	}

}
