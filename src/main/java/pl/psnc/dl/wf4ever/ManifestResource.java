package pl.psnc.dl.wf4ever;

import java.io.IOException;

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

import pl.psnc.dl.wf4ever.connection.DLibraDataSource;
import pl.psnc.dlibra.service.DLibraException;

import com.sun.jersey.core.header.ContentDisposition;

/**
 * 
 * @author nowakm
 *
 */
@Path(Constants.WORKSPACES_URL_PART + "/{W_ID}/"
		+ Constants.RESEARCH_OBJECTS_URL_PART + "/{RO_ID}/{RO_VERSION_ID}/"
		+ Constants.MANIFEST_FILENAME)
public class ManifestResource
{

	//	private final static Logger logger = Logger
	//			.getLogger(ManifestResource.class);

	@Context
	private HttpServletRequest request;

	@Context
	private UriInfo uriInfo;


	@GET
	@Produces("application/rdf+xml")
	public Response getManifestFile(@PathParam("W_ID") String workspaceId,
			@PathParam("RO_ID") String researchObjectId,
			@PathParam("RO_VERSION_ID") String versionId)
		throws IOException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);
		String manifest = dLibraDataSource.getManifest(researchObjectId,
			versionId);

		ContentDisposition cd = ContentDisposition.type("application/rdf+xml")
				.fileName(Constants.MANIFEST_FILENAME).build();

		return Response.ok(manifest)
				.header(Constants.CONTENT_DISPOSITION_HEADER_NAME, cd).build();
	}


	@POST
	@Consumes("application/rdf+xml")
	public Response updateManifestFile(@PathParam("W_ID") String workspaceId,
			@PathParam("RO_ID") String researchObjectId,
			@PathParam("RO_VERSION_ID") String versionId, String rdfAsString)
		throws DLibraException, IOException, TransformerException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		String versionUri = uriInfo
				.getAbsolutePath()
				.toString()
				.substring(0,
					uriInfo.getAbsolutePath().toString().lastIndexOf("/"));

		dLibraDataSource.updateManifest(versionUri, researchObjectId,
			versionId, rdfAsString);

		return Response.ok().build();
	}
}
