package pl.psnc.dl.wf4ever;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.connection.DLibraDataSource;
import pl.psnc.dl.wf4ever.connection.IncorrectManifestException;
import pl.psnc.dlibra.service.DLibraException;

import com.hp.hpl.jena.shared.JenaException;
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


	/**
	 * Returns metadata of RO version as manifest.rdf file. Content - if this
	 * optional parameter will be added to the query, instead of the metadata of
	 * the version, zip archive with contents of RO version will be returned.
	 * 
	 * @param workspaceId
	 *            identifier of a workspace in the RO SRS
	 * @param researchObjectId
	 *            RO identifier - defined by the user
	 * @param versionId
	 *            identifier of version of RO - defined by the user
	 * @return
	 * @throws IOException
	 * @throws DLibraException
	 */
	@GET
	@Produces({ "application/rdf+xml", "application/zip"})
	public Response getManifestFile(@PathParam("W_ID") String workspaceId,
			@PathParam("RO_ID") String researchObjectId,
			@PathParam("RO_VERSION_ID") String versionId,
			@QueryParam("content") String isContentRequested,
			@QueryParam("edition_id") String editionId,
			@QueryParam("edition_list") String isEditionListRequested)
		throws IOException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);
		
		if (isEditionListRequested != null) {
			logger.debug("Getting edition list");
			Map<Date, Long> editions = dLibraDataSource.getEditionHelper()
					.getEditionList(researchObjectId, versionId);
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<Date, Long> entry : editions.entrySet()) {
				sb.append("" + entry.getValue() + "=" + entry.getKey() + "\n");
			}
			return Response.ok(sb.toString()).build();
		}
		else if (isContentRequested == null) {
			logger.debug("Getting manifest");
			String manifest = dLibraDataSource.getManifestHelper().getManifest(
				researchObjectId, versionId);
			ContentDisposition cd = ContentDisposition
					.type("application/rdf+xml")
					.fileName(Constants.MANIFEST_FILENAME).build();
			return Response.ok(manifest)
					.header(Constants.CONTENT_DISPOSITION_HEADER_NAME, cd)
					.build();
		}
		else {
			logger.debug("Getting zipped pub");
			InputStream body = dLibraDataSource.getPublicationsHelper()
					.getZippedPublication(researchObjectId, versionId);
			ContentDisposition cd = ContentDisposition.type("application/zip")
					.fileName(versionId + ".zip").build();
			return Response.ok(body)
					.header(Constants.CONTENT_DISPOSITION_HEADER_NAME, cd)
					.build();
		}
	}


	/**
	 * Used for updating metadata of version of RO (manifest.rdf file).
	 * 
	 * @param workspaceId
	 *            identifier of a workspace in the RO SRS
	 * @param researchObjectId
	 *            RO identifier - defined by the user
	 * @param versionId
	 *            identifier of version of RO - defined by the user
	 * @param rdfAsString
	 *            manifest.rdf
	 * @return 200 (OK) response code if the descriptive metadata was
	 *         successfully updated, 400 (Bad Request) if manifest.rdf is not
	 *         well-formed, 409 (Conflict) if manifest.rdf contains incorrect
	 *         data (for example, one of required tags is missing).
	 * @throws DLibraException
	 * @throws IOException
	 * @throws TransformerException
	 * @throws JenaException if the manifest is malformed
	 * @throws IncorrectManifestException if the manifest is missing a property
	 */
	@PUT
	@Consumes("application/rdf+xml")
	public Response updateManifestFile(@PathParam("W_ID") String workspaceId,
			@PathParam("RO_ID") String researchObjectId,
			@PathParam("RO_VERSION_ID") String versionId, String rdfAsString)
		throws DLibraException, IOException, TransformerException,
		JenaException, IncorrectManifestException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		String versionUri = uriInfo.getAbsolutePath().toString();

		dLibraDataSource.getManifestHelper().updateManifest(versionUri,
			researchObjectId, versionId, rdfAsString);

		return Response.ok().build();
	}


	@POST
	public Response createEdition(@PathParam("W_ID") String workspaceId,
			@PathParam("RO_ID") String researchObjectId,
			@PathParam("RO_VERSION_ID") String versionId)
		throws RemoteException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		long editionId = dLibraDataSource.getEditionHelper()
				.createEdition(versionId, researchObjectId, versionId).getId();

		String uri = uriInfo.getAbsolutePath().toString() + "?edition_id="
				+ editionId;

		return Response.created(URI.create(uri)).build();
	}


	/**
	 * Deletes this version of research object.
	 * 
	 * @param workspaceId
	 *            identifier of a workspace in the RO SRS
	 * @param researchObjectId
	 *            RO identifier - defined by the user
	 * @param versionId
	 *            identifier of version of RO - defined by the user
	 * @throws DLibraException
	 * @throws IOException
	 * @throws TransformerException
	 */
	@DELETE
	public void deleteVersion(@PathParam("W_ID") String workspaceId,
			@PathParam("RO_ID") String researchObjectId,
			@PathParam("RO_VERSION_ID") String versionId)
		throws DLibraException, IOException, TransformerException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		dLibraDataSource.getPublicationsHelper().deletePublication(
			researchObjectId, versionId, uriInfo.getAbsolutePath().toString());
	}
}
