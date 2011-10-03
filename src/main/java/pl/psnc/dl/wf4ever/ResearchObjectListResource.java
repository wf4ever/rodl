package pl.psnc.dl.wf4ever;

import java.net.URI;
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

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.dlibra.DLibraDataSource;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.service.DLibraException;

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
	 * @param workspaceId identifier of a workspace in the RO SRS
	 * @return TBD
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	@GET
	@Produces("text/plain")
	public String getResearchObjectList(@PathParam("W_ID") String workspaceId)
		throws RemoteException, DLibraException
	{

		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);
		List<AbstractPublicationInfo> list;
		logger.debug(String.format("Received %d query params", uriInfo
				.getQueryParameters().size()));
		if (uriInfo.getQueryParameters().isEmpty()) {
			list = dLibraDataSource.getPublicationsHelper()
					.listUserGroupPublications();
		}
		else {
			list = dLibraDataSource.getPublicationsHelper()
					.listUserPublications(uriInfo.getQueryParameters());
		}

		StringBuilder sb = new StringBuilder("");

		for (AbstractPublicationInfo gp : list) {
			sb.append(uriInfo.getAbsolutePath());
			sb.append("/");
			sb.append(gp.getLabel());
			sb.append("\n");
		}

		return sb.toString();
	}


	/**
	 * Creates new RO with given RO_ID.

	 * @param workspaceId identifier of a workspace in the RO SRS
	 * @param researchObjectId RO_ID in plain text (text/plain)
	 * @return 201 (Created) when the RO was successfully created, 409 (Conflict) if the RO_ID is already used in the WORKSPACE_ID workspace
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	@POST
	@Consumes("text/plain")
	public Response createResearchObject(@PathParam("W_ID") String workspaceId,
			String researchObjectId)
		throws RemoteException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		dLibraDataSource.getPublicationsHelper().createGroupPublication(
			researchObjectId);

		URI resourceUri = uriInfo.getAbsolutePathBuilder().path("/").build()
				.resolve(researchObjectId);

		return Response.created(resourceUri).build();
	}

}
