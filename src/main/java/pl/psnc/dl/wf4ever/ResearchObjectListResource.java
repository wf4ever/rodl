package pl.psnc.dl.wf4ever;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
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
		+ Constants.RESEARCH_OBJECTS_URL_PART)
public class ResearchObjectListResource
{

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
	 */
	@GET
	@Produces("application/rdf+xml")
	public Response getResearchObjectList(@PathParam("W_ID")
	String workspaceId)
		throws RemoteException, DLibraException, TransformerException,
		MalformedURLException, UnknownHostException, DigitalLibraryException
	{

		DigitalLibrary dLibraDataSource = ((DigitalLibraryFactory) request
				.getAttribute(Constants.DLFACTORY)).getDigitalLibrary();
		List<String> list;
		logger.debug(String.format("Received %d query params", uriInfo
				.getQueryParameters().size()));
		if (uriInfo.getQueryParameters().isEmpty()) {
			list = dLibraDataSource.getResearchObjectIds(workspaceId);
		}
		else {
			list = dLibraDataSource.getVersionIds(workspaceId,
				uriInfo.getQueryParameters());
		}

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
	 * @throws IdNotFoundException 
	 */
	@POST
	@Consumes("text/plain")
	public Response createResearchObject(@PathParam("W_ID")
	String workspaceId, String researchObjectId)
		throws RemoteException, MalformedURLException, UnknownHostException,
		DigitalLibraryException, IdNotFoundException
	{
		DigitalLibrary dLibraDataSource = ((DigitalLibraryFactory) request
				.getAttribute(Constants.DLFACTORY)).getDigitalLibrary();

		dLibraDataSource.createResearchObject(workspaceId, researchObjectId);

		URI resourceUri = uriInfo.getAbsolutePathBuilder().path("/").build()
				.resolve(researchObjectId);

		return Response.created(resourceUri).build();
	}

}
