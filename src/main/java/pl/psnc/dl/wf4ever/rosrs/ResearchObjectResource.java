package pl.psnc.dl.wf4ever.rosrs;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.auth.AuthenticationException;
import pl.psnc.dl.wf4ever.auth.SecurityFilter;
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
 * @author Piotr Hołubowicz
 * 
 */
@Path("ROs/{ro_id}/")
public class ResearchObjectResource
{

	private final static Logger logger = Logger.getLogger(ResearchObjectResource.class);

	public static final URI portalRoPage = URI.create("http://sandbox.wf4ever-project.org/portal/ro");

	@Context
	HttpServletRequest request;

	@Context
	UriInfo uriInfo;

	private static final String workspaceId = "default";

	private static final String versionId = "v1";


	/**
	 * Returns zip archive with contents of RO version.
	 * 
	 * @param researchObjectId
	 *            RO identifier - defined by the user
	 * @return
	 * @throws UnknownHostException
	 * @throws MalformedURLException
	 * @throws RemoteException
	 * @throws IOException
	 * @throws DigitalLibraryException
	 * @throws IdNotFoundException
	 * @throws OperationNotSupportedException
	 * @throws NotFoundException
	 */
	@GET
	@Produces({ "application/zip", "multipart/related", "*/*"})
	public Response getZippedRO(@PathParam("ro_id")
	String researchObjectId)
		throws RemoteException, MalformedURLException, UnknownHostException, DigitalLibraryException, NotFoundException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(user.getLogin(), user.getPassword());

		InputStream body = dl.getZippedVersion(workspaceId, researchObjectId, versionId);
		//TODO add all named graphs from SMS that start with the base URI
		ContentDisposition cd = ContentDisposition.type("application/zip")
				.fileName(researchObjectId + "-" + versionId + ".zip").build();
		return Response.ok(body).header("Content-disposition", cd).build();
	}


	@GET
	@Produces({ "application/rdf+xml", "application/x-turtle", "text/turtle", "application/x-trig", "application/trix",
			"text/rdf+n3"})
	public Response getROMetadata()
	{
		return Response.seeOther(uriInfo.getAbsolutePath().resolve(".ro/manifest.rdf")).build();
	}


	@GET
	@Produces({ MediaType.TEXT_HTML})
	public Response getROHtml()
		throws URISyntaxException
	{
		URI portalURI = new URI(portalRoPage.getScheme(), portalRoPage.getAuthority(), portalRoPage.getPath(), "ro="
				+ uriInfo.getRequestUri().toString(), null);
		return Response.seeOther(portalURI).build();
	}


	@DELETE
	public void deleteVersion(@PathParam("ro_id")
	String researchObjectId)
		throws DigitalLibraryException, ClassNotFoundException, IOException, NamingException, SQLException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		if (user.getRole() == UserProfile.Role.PUBLIC) {
			throw new AuthenticationException("Only authenticated users can do that.", SecurityFilter.REALM);
		}
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(user.getLogin(), user.getPassword());

		try {
			dl.deleteVersion(workspaceId, researchObjectId, versionId);
			if (dl.getVersionIds(workspaceId, researchObjectId).isEmpty()) {
				dl.deleteResearchObject(workspaceId, researchObjectId);
				if (dl.getResearchObjectIds(workspaceId).isEmpty()) {
					dl.deleteWorkspace(workspaceId);
				}
			}
		}
		catch (NotFoundException e) {
			logger.warn("URI not found in dLibra: " + uriInfo.getAbsolutePath());
		}
		finally {

			SemanticMetadataService sms = SemanticMetadataServiceFactory.getService(user);
			try {
				sms.removeResearchObject(uriInfo.getAbsolutePath());
			}
			catch (IllegalArgumentException e) {
				logger.warn("URI not found in SMS: " + uriInfo.getAbsolutePath());
			}
			finally {
				sms.close();
			}
		}
	}
}
