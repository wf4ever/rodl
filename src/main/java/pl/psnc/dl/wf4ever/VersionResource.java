package pl.psnc.dl.wf4ever;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
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

import pl.psnc.dl.wf4ever.dlibra.DLibraDataSource;
import pl.psnc.dl.wf4ever.dlibra.IncorrectManifestException;
import pl.psnc.dlibra.metadata.Edition;
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
	public Response getManifestFile(@PathParam("W_ID")
	String workspaceId, @PathParam("RO_ID")
	String researchObjectId, @PathParam("RO_VERSION_ID")
	String versionId, @QueryParam("content")
	String isContentRequested, @QueryParam("edition_id")
	@DefaultValue(Constants.EDITION_QUERY_PARAM_DEFAULT_STRING)
	long editionId, @QueryParam("edition_list")
	String isEditionListRequested)
		throws IOException, DLibraException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		if (isEditionListRequested != null) {
			return getEditionList(researchObjectId, versionId, dLibraDataSource);
		}
		else if (isContentRequested == null) {
			return getManifest(researchObjectId, versionId, dLibraDataSource,
				editionId);
		}
		else {
			return getZippedPublication(researchObjectId, versionId,
				dLibraDataSource, editionId);
		}
	}


	/**
	 * @param researchObjectId
	 * @param versionId
	 * @param dLibraDataSource
	 * @param editionId 
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	private Response getZippedPublication(String researchObjectId,
			String versionId, DLibraDataSource dLibraDataSource, long editionId)
		throws RemoteException, DLibraException
	{
		InputStream body = dLibraDataSource.getPublicationsHelper()
				.getZippedPublication(
					Utils.getEditionId(dLibraDataSource, researchObjectId,
						versionId, editionId));
		ContentDisposition cd = ContentDisposition.type("application/zip")
				.fileName(versionId + ".zip").build();
		return Response.ok(body)
				.header(Constants.CONTENT_DISPOSITION_HEADER_NAME, cd).build();
	}


	/**
	 * @param researchObjectId
	 * @param versionId
	 * @param dLibraDataSource
	 * @param editionId 
	 * @return
	 * @throws IOException
	 * @throws DLibraException
	 */
	private Response getManifest(String researchObjectId, String versionId,
			DLibraDataSource dLibraDataSource, Long editionId)
		throws IOException, DLibraException
	{
		InputStream manifest = dLibraDataSource.getManifestHelper()
				.getManifest(
					Utils.getEditionId(dLibraDataSource, researchObjectId,
						versionId, editionId));
		ContentDisposition cd = ContentDisposition.type("application/rdf+xml")
				.fileName(Constants.MANIFEST_FILENAME).build();
		return Response.ok(manifest)
				.header(Constants.CONTENT_DISPOSITION_HEADER_NAME, cd).build();
	}


	/**
	 * @param researchObjectId
	 * @param versionId
	 * @param dLibraDataSource
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	private Response getEditionList(String researchObjectId, String versionId,
			DLibraDataSource dLibraDataSource)
		throws RemoteException, DLibraException
	{
		logger.debug("Getting edition list");
		Set<Edition> editions = dLibraDataSource.getEditionHelper()
				.getEditionList(researchObjectId, versionId);
		StringBuilder sb = new StringBuilder();
		for (Edition edition : editions) {
			sb.append((edition.isPublished() ? "*" : "") + edition.getId()
					+ "=" + edition.getCreationDate() + "\n");
		}
		return Response.ok(sb.toString()).build();
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
	public Response updateManifestFile(@PathParam("W_ID")
	String workspaceId, @PathParam("RO_ID")
	String researchObjectId, @PathParam("RO_VERSION_ID")
	String versionId, @QueryParam("publish")
	String publish, String rdfAsString)
		throws DLibraException, IOException, TransformerException,
		JenaException, IncorrectManifestException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		URI versionUri = uriInfo.getAbsolutePath();

		if (publish != null) {
			if (!publish.equals("false")) {
				dLibraDataSource.getPublicationsHelper().publishPublication(
					researchObjectId, versionId);
			}
			else {
				dLibraDataSource.getPublicationsHelper().unpublishPublication(
					researchObjectId, versionId);
			}
		}
		else {
			dLibraDataSource.getManifestHelper().updateManifest(versionUri,
				researchObjectId, versionId,
				new ByteArrayInputStream(rdfAsString.getBytes()));
		}

		return Response.ok().build();
	}


	@POST
	public Response createEdition(@PathParam("W_ID")
	String workspaceId, @PathParam("RO_ID")
	String researchObjectId, @PathParam("RO_VERSION_ID")
	String versionId)
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
	public void deleteVersion(@PathParam("W_ID")
	String workspaceId, @PathParam("RO_ID")
	String researchObjectId, @PathParam("RO_VERSION_ID")
	String versionId)
		throws DLibraException, IOException, TransformerException
	{
		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
				.getAttribute(Constants.DLIBRA_DATA_SOURCE);

		dLibraDataSource.getPublicationsHelper().deletePublication(
			researchObjectId, versionId, uriInfo.getAbsolutePath());
	}
}
