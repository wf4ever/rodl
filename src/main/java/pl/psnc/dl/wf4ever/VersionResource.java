package pl.psnc.dl.wf4ever;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
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

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.connection.DLibraDataSource;
import pl.psnc.dlibra.service.DLibraException;

import com.sun.jersey.core.header.ContentDisposition;

/**
 * 
 * @author nowakm
 *
 */
@Path(Constants.WORKSPACES_URL_PART + "/{W_ID}/"
		+ Constants.RESEARCH_OBJECTS_URL_PART + "/{RO_ID}/{RO_VERSION_ID}")
public class VersionResource
{

	private final static Logger logger = Logger
			.getLogger(VersionResource.class);

	@Context
	private HttpServletRequest request;

	@Context
	private UriInfo uriInfo;


	@GET
	@Produces("application/rdf+xml")
	public Response getVersionFileList(@PathParam("W_ID") String workspaceId,
			@PathParam("RO_ID") String researchObjectId,
			@PathParam("RO_VERSION_ID") String versionId)
		throws RemoteException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);
		List<String> list = dLibraDataSource.listFilesInPublication(
			researchObjectId, versionId);

		String absolutePath = uriInfo.getAbsolutePath().toString();
		if (absolutePath.endsWith("/")) {
			absolutePath = absolutePath.substring(0, absolutePath.length() - 1);
		}
		for (int i = 0; i < list.size(); i++) {
			list.set(i, absolutePath + list.get(i));
		}

		String responseBody = RdfBuilder.serializeResource(RdfBuilder
				.createCollection(absolutePath, list));

		return Response.ok().entity(responseBody).build();
	}


	@GET
	@Produces("application/zip")
	public Response getVersionArchive(@PathParam("W_ID") String workspaceId,
			@PathParam("RO_ID") String researchObjectId,
			@PathParam("RO_VERSION_ID") String versionId)
		throws RemoteException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		InputStream body = dLibraDataSource.getZippedPublication(
			researchObjectId, versionId);

		ContentDisposition cd = ContentDisposition.type("application/zip")
				.fileName(versionId + ".zip").build();
		return Response.ok(body)
				.header(Constants.CONTENT_DISPOSITION_HEADER_NAME, cd).build();
	}


	@POST
	@Consumes("text/plain")
	public Response createVersion(@PathParam("W_ID") String workspaceId,
			@PathParam("RO_ID") String researchObjectId,
			@PathParam("RO_VERSION_ID") String versionId, String baseVersionUri)
		throws DLibraException, IOException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		String baseVersionId = null;
		if (baseVersionUri != null && baseVersionUri.length() > 0) {
			String roPath = uriInfo
					.getPath()
					.toString()
					.substring(0,
						uriInfo.getPath().toString().lastIndexOf(versionId));

			// remove "/" from the end of uri
			if (baseVersionUri.lastIndexOf("/") == baseVersionUri.length() - 1) {
				baseVersionUri = baseVersionUri.substring(0,
					baseVersionUri.length() - 1);
			}

			// check if this is correct URI
			if (!baseVersionUri.contains(roPath)) {
				return Response
						.status(Status.BAD_REQUEST)
						.entity("Bad base version URI")
						.header(Constants.CONTENT_TYPE_HEADER_NAME,
							"text/plain").build();
			}

			baseVersionId = baseVersionUri.substring(baseVersionUri
					.indexOf(roPath) + roPath.length());
		}

		String manifestUri = uriInfo.getAbsolutePath().toString();
		dLibraDataSource.createPublication(researchObjectId, versionId,
			baseVersionId, manifestUri);

		return Response.created(uriInfo.getAbsolutePath()).build();
	}


	@DELETE
	public void deleteVersion(@PathParam("W_ID") String workspaceId,
			@PathParam("RO_ID") String researchObjectId,
			@PathParam("RO_VERSION_ID") String versionId)
		throws DLibraException, IOException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		dLibraDataSource.deletePublication(researchObjectId, versionId, uriInfo
				.getAbsolutePath().toString());
	}
}
