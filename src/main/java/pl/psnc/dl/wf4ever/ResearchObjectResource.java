package pl.psnc.dl.wf4ever;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;

import com.sun.jersey.core.header.ContentDisposition;

/**
 * 
 * @author nowakm
 * 
 */
@Path(Constants.WORKSPACES_URL_PART + "/{W_ID}/"
		+ Constants.RESEARCH_OBJECTS_URL_PART + "/{RO_ID}")
public class ResearchObjectResource
{

	@SuppressWarnings("unused")
	private final static Logger logger = Logger
			.getLogger(ResearchObjectResource.class);

	@Context
	HttpServletRequest request;

	@Context
	UriInfo uriInfo;


	/**
	 * Returns list of versions of this research object.
	 * 
	 * @param workspaceId
	 *            identifier of a workspace in the RO SRS
	 * @param researchObjectId
	 *            RO identifier - defined by the user
	 * @return 200 (OK) response code with a rdf file in response body
	 *         containing OAI-ORE aggreagates tags.
	 * @throws RemoteException
	 * @throws DLibraException
	 * @throws TransformerException
	 * @throws UnknownHostException 
	 * @throws MalformedURLException 
	 * @throws DigitalLibraryException 
	 */
	@GET
	@Produces("application/rdf+xml")
	public Response getListOfVersions(@PathParam("W_ID")
	String workspaceId, @PathParam("RO_ID")
	String researchObjectId)
		throws RemoteException, DLibraException, TransformerException,
		MalformedURLException, UnknownHostException, DigitalLibraryException
	{
		DigitalLibrary dLibraDataSource = ((DigitalLibraryFactory) request
				.getAttribute(Constants.DLFACTORY)).getDigitalLibrary();
		List<String> list = dLibraDataSource.getVersionIds(workspaceId,
			researchObjectId);

		List<URI> links = new ArrayList<URI>(list.size());

		for (String id : list) {
			links.add(uriInfo.getAbsolutePathBuilder().path("/").build()
					.resolve(id));
		}

		String responseBody = RdfBuilder.serializeResource(RdfBuilder
				.createCollection(uriInfo.getAbsolutePath(), links));

		ContentDisposition cd = ContentDisposition.type("application/rdf+xml")
				.fileName(researchObjectId + ".rdf").build();

		return Response.ok().entity(responseBody)
				.header(Constants.CONTENT_DISPOSITION_HEADER_NAME, cd).build();
	}


	/**
	 * Creates new version. Input is RO_VERSION_ID and optional URI of the base
	 * version that should be used to create a new version.
	 * 
	 * @param workspaceId
	 * @param researchObjectId
	 * @param data
	 *            Input format is text/plain with RO_VERSION_ID in first line
	 *            and base version URI in second (optional).
	 * @return 201 (Created) if the version was created, 409 (Conflict) if
	 *         version with given RO_VERSION_ID already exists
	 * @throws IOException
	 * @throws TransformerException
	 * @throws URISyntaxException
	 * @throws DigitalLibraryException 
	 * @throws IdNotFoundException 
	 * @throws SAXException
	 */
	@POST
	@Consumes("text/plain")
	public Response createVersion(@PathParam("W_ID")
	String workspaceId, @PathParam("RO_ID")
	String researchObjectId, String data)
		throws IOException, TransformerException, URISyntaxException,
		DigitalLibraryException, IdNotFoundException
	{
		DigitalLibrary dLibraDataSource = ((DigitalLibraryFactory) request
				.getAttribute(Constants.DLFACTORY)).getDigitalLibrary();

		String lines[] = data.split("[\\r\\n]+");
		if (lines.length < 1) {
			return Response.status(Status.BAD_REQUEST)
					.entity("Content is shorter than 2 lines")
					.header(Constants.CONTENT_TYPE_HEADER_NAME, "text/plain")
					.build();
		}
		String version = lines[0];
		String baseVersion = null;
		if (lines.length > 1) {
			URI baseVersionUri = new URI(lines[1]);
			URI roUri = baseVersionUri.resolve(".");
			baseVersion = roUri.relativize(baseVersionUri).toString();
		}

		URI resourceUri = uriInfo.getAbsolutePathBuilder().path("/").build()
				.resolve(version);

		if (baseVersion == null) {
			dLibraDataSource.createVersion(workspaceId, researchObjectId,
				version, resourceUri);
		}
		else {
			dLibraDataSource.createVersion(workspaceId, researchObjectId,
				version, baseVersion, resourceUri);
		}
		return Response.created(resourceUri).build();
	}


	/**
	 * Deletes the research object.
	 * 
	 * @param workspaceId
	 *            identifier of a workspace in the RO SRS
	 * @param researchObjectId
	 *            RO identifier - defined by the user
	 * @throws RemoteException
	 * @throws UnknownHostException 
	 * @throws MalformedURLException 
	 * @throws DigitalLibraryException 
	 * @throws IdNotFoundException 
	 */
	@DELETE
	public void deleteResearchObject(@PathParam("W_ID")
	String workspaceId, @PathParam("RO_ID")
	String researchObjectId)
		throws RemoteException, MalformedURLException, UnknownHostException,
		DigitalLibraryException, IdNotFoundException
	{
		DigitalLibrary dLibraDataSource = ((DigitalLibraryFactory) request
				.getAttribute(Constants.DLFACTORY)).getDigitalLibrary();

		dLibraDataSource.deleteResearchObject(workspaceId, researchObjectId);

	}
}
