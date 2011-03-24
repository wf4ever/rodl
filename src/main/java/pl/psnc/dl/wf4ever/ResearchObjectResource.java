package pl.psnc.dl.wf4ever;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import pl.psnc.dl.wf4ever.connection.DLibraDataSource;
import pl.psnc.dlibra.metadata.PublicationInfo;
import pl.psnc.dlibra.service.DLibraException;

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

	@Context
	HttpServletRequest request;

	@Context
	UriInfo uriInfo;


	@GET
	@Produces("application/rdf+xml")
	public Response getListOfVersions(@PathParam("W_ID") String workspaceId,
			@PathParam("RO_ID") String researchObjectId)
		throws RemoteException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);
		List<PublicationInfo> list = dLibraDataSource
				.listPublicationsInGroup(researchObjectId);

		List<String> links = new ArrayList<String>(list.size());

		for (PublicationInfo info : list) {
			links.add(uriInfo.getAbsolutePath() + "/" + info.getLabel());
		}

		String responseBody = RdfBuilder.serializeResource(RdfBuilder
				.createCollection(uriInfo.getAbsolutePath().toString(), links));

		ContentDisposition cd = ContentDisposition.type("application/rdf+xml")
				.fileName(researchObjectId + ".rdf").build();

		return Response.ok().entity(responseBody)
				.header(Constants.CONTENT_DISPOSITION_HEADER_NAME, cd).build();
	}


	@PUT
	public Response createResearchObject(@PathParam("W_ID") String workspaceId,
			@PathParam("RO_ID") String researchObjectId)
		throws RemoteException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		dLibraDataSource.createGroupPublication(researchObjectId);

		return Response.created(uriInfo.getAbsolutePath()).build();
	}


	@DELETE
	public void deleteResearchObject(@PathParam("W_ID") String workspaceId,
			@PathParam("RO_ID") String researchObjectId)
		throws RemoteException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		dLibraDataSource.deleteGroupPublication(researchObjectId);

	}
}
