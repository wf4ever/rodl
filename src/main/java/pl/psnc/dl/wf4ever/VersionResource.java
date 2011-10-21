package pl.psnc.dl.wf4ever;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Set;

import javax.naming.OperationNotSupportedException;
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

import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
import pl.psnc.dl.wf4ever.connection.SemanticMetadataServiceFactory;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibrary;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dl.wf4ever.dlibra.helpers.IncorrectManifestException;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService.Notation;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.service.IdNotFoundException;

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
	 * @throws DigitalLibraryException 
	 * @throws IdNotFoundException 
	 * @throws OperationNotSupportedException 
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
		throws IOException, DigitalLibraryException, IdNotFoundException,
		OperationNotSupportedException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());
		SemanticMetadataService sms = SemanticMetadataServiceFactory
				.getService(user);

		if (isEditionListRequested != null) {
			return getEditionList(workspaceId, researchObjectId, versionId, dl);
		}
		else if (isContentRequested == null) {
			if (editionId == Constants.EDITION_QUERY_PARAM_DEFAULT) {
				return getManifest(sms, uriInfo.getAbsolutePath(),
					request.getContentType());
			}
			else {
				throw new OperationNotSupportedException(
						"Editions are not supported when handling metadata");
			}
		}
		else {
			return getZippedPublication(workspaceId, researchObjectId,
				versionId, dl, editionId);
		}
	}


	/**
	 * @param researchObjectId
	 * @param versionId
	 * @param dLibraDataSource
	 * @param editionId 
	 * @return
	 * @throws RemoteException
	 * @throws DigitalLibraryException 
	 * @throws IdNotFoundException 
	 */
	private Response getZippedPublication(String workspaceId,
			String researchObjectId, String versionId,
			DigitalLibrary dLibraDataSource, long editionId)
		throws RemoteException, DigitalLibraryException, IdNotFoundException
	{
		InputStream body;
		if (editionId == Constants.EDITION_QUERY_PARAM_DEFAULT) {
			body = dLibraDataSource.getZippedVersion(workspaceId,
				researchObjectId, versionId);
		}
		else {
			body = dLibraDataSource.getZippedVersion(workspaceId,
				researchObjectId, versionId, editionId);
		}
		ContentDisposition cd = ContentDisposition.type("application/zip")
				.fileName(versionId + ".zip").build();
		return Response.ok(body)
				.header(Constants.CONTENT_DISPOSITION_HEADER_NAME, cd).build();
	}


	/**
	 * @param researchObjectId
	 * @param versionId
	 * @param versionId2 
	 * @param dLibraDataSource
	 * @param uri 
	 * @param string 
	 * @return
	 * @throws IOException
	 * @throws DigitalLibraryException 
	 * @throws IdNotFoundException 
	 */
	private Response getManifest(SemanticMetadataService sms, URI uri,
			String contentType)
	{
		SemanticMetadataService.Notation notation;
		if ("application/x+trig".equals(contentType)) {
			notation = Notation.TRIG;
		}
		else {
			contentType = "application/rdf+xml";
			notation = Notation.RDF_XML;
		}
		InputStream manifest = sms.getManifest(uri, notation);
		ContentDisposition cd = ContentDisposition.type(contentType)
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
	 * @throws DigitalLibraryException 
	 * @throws IdNotFoundException 
	 */
	private Response getEditionList(String workspaceId,
			String researchObjectId, String versionId,
			DigitalLibrary dLibraDataSource)
		throws RemoteException, DigitalLibraryException, IdNotFoundException
	{
		logger.debug("Getting edition list");
		Set<Edition> editions = dLibraDataSource.getEditionList(workspaceId,
			researchObjectId, versionId);
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
	 * @throws IOException
	 * @throws TransformerException
	 * @throws JenaException if the manifest is malformed
	 * @throws IncorrectManifestException if the manifest is missing a property
	 * @throws DigitalLibraryException 
	 * @throws IdNotFoundException 
	 */
	@PUT
	@Consumes("application/rdf+xml")
	public Response publish(@PathParam("W_ID")
	String workspaceId, @PathParam("RO_ID")
	String researchObjectId, @PathParam("RO_VERSION_ID")
	String versionId, @QueryParam("unpublish")
	String unpublish)
		throws IOException, TransformerException, JenaException,
		IncorrectManifestException, DigitalLibraryException,
		IdNotFoundException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());

		if (unpublish == null) {
			dl.publishVersion(workspaceId, researchObjectId, versionId);
		}
		else {
			dl.unpublishVersion(workspaceId, researchObjectId, versionId);
		}

		return Response.ok().build();
	}


	@POST
	public Response createEdition(@PathParam("W_ID")
	String workspaceId, @PathParam("RO_ID")
	String researchObjectId, @PathParam("RO_VERSION_ID")
	String versionId)
		throws RemoteException, DigitalLibraryException, MalformedURLException,
		UnknownHostException, IdNotFoundException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());

		long editionId = dl.createEdition(workspaceId, versionId,
			researchObjectId, versionId).getId();

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
	 * @throws DigitalLibraryException 
	 * @throws UnknownHostException 
	 * @throws MalformedURLException 
	 * @throws RemoteException 
	 * @throws IdNotFoundException 
	 */
	@DELETE
	public void deleteVersion(@PathParam("W_ID")
	String workspaceId, @PathParam("RO_ID")
	String researchObjectId, @PathParam("RO_VERSION_ID")
	String versionId)
		throws DigitalLibraryException, RemoteException, MalformedURLException,
		UnknownHostException, IdNotFoundException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());
		SemanticMetadataService sms = SemanticMetadataServiceFactory
				.getService(user);

		dl.deleteVersion(workspaceId, researchObjectId, versionId,
			uriInfo.getAbsolutePath());
		sms.removeResearchObject(uriInfo.getAbsolutePath());
	}
}
