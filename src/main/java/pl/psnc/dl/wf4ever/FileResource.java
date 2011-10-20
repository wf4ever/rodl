package pl.psnc.dl.wf4ever;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.auth.ForbiddenException;
import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
import pl.psnc.dlibra.service.IdNotFoundException;

import com.sun.jersey.core.header.ContentDisposition;

/**
 * 
 * @author nowakm
 * 
 */
@Path(Constants.WORKSPACES_URL_PART
		+ "/{W_ID}/"
		+ Constants.RESEARCH_OBJECTS_URL_PART
		+ "/{RO_ID}/{RO_VERSION_ID}/{FILE_PATH : [\\w\\d:#%/;$()~_?\\-=\\\\.&]+}")
public class FileResource
{

	private final static Logger logger = Logger.getLogger(FileResource.class);

	@Context
	private HttpServletRequest request;

	@Context
	private UriInfo uriInfo;


	/**
	 * Returns requested file metadata. If requested URI leads to a folder,
	 * returns rdf file with list of files in this folder. Content - if this
	 * optional parameter is added to the query, instead of the metadata of the
	 * file, the file content will be returned. If requested URI leads to a
	 * folder, returns zip archive with contents of folder.
	 * 
	 * @param workspaceId
	 * @param researchObjectId
	 * @param versionId
	 * @param filePath
	 * @param isContentRequested
	 * @return
	 * @throws IOException
	 * @throws TransformerException
	 * @throws DigitalLibraryException 
	 * @throws IdNotFoundException 
	 */
	@GET
	public Response getFile(@PathParam("W_ID")
	String workspaceId, @PathParam("RO_ID")
	String researchObjectId, @PathParam("RO_VERSION_ID")
	String versionId, @PathParam("FILE_PATH")
	String filePath, @QueryParam("content")
	String isContentRequested, @QueryParam("edition_id")
	@DefaultValue(Constants.EDITION_QUERY_PARAM_DEFAULT_STRING)
	long editionId)
		throws IOException, TransformerException, DigitalLibraryException,
		IdNotFoundException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());

		if (isContentRequested != null) { // file or folder content
			try { // file
				return getFileContent(workspaceId, researchObjectId, versionId,
					filePath, dl, editionId);
			}
			catch (IdNotFoundException ex) { // folder
				return getFolderContent(workspaceId, researchObjectId,
					versionId, filePath, dl, editionId);
			}
		}
		else { // metadata
			try { // file
				return getFileMetadata(workspaceId, researchObjectId,
					versionId, filePath, dl, editionId);
			}
			catch (IdNotFoundException ex) { // folder
				return getFolderMetadata(workspaceId, researchObjectId,
					versionId, filePath, dl, editionId);
			}
		}

	}


	private Response getFolderMetadata(String workspaceId,
			String researchObjectId, String versionId, String filePath,
			DigitalLibrary dLibraDataSource, Long editionId)
		throws RemoteException, TransformerException, DigitalLibraryException,
		IdNotFoundException
	{
		if (!filePath.endsWith("/"))
			filePath = filePath.concat("/");
		List<String> files;
		if (editionId == Constants.EDITION_QUERY_PARAM_DEFAULT) {
			files = dLibraDataSource.getResourcePaths(workspaceId,
				researchObjectId, versionId, filePath);
		}
		else {
			files = dLibraDataSource.getResourcePaths(workspaceId,
				researchObjectId, versionId, filePath, editionId);
		}

		List<URI> links = new ArrayList<URI>(files.size());

		for (String path : files) {
			links.add(uriInfo.getAbsolutePathBuilder().path("/").path(path)
					.build());
		}

		String responseBody = RdfBuilder.serializeResource(RdfBuilder
				.createCollection(uriInfo.getAbsolutePath(), links));

		ContentDisposition cd = ContentDisposition.type("application/rdf+xml")
				.fileName(researchObjectId + ".rdf").build();

		return Response.ok().entity(responseBody)
				.header(Constants.CONTENT_DISPOSITION_HEADER_NAME, cd).build();
	}


	private Response getFileMetadata(String workspaceId,
			String researchObjectId, String versionId, String filePath,
			DigitalLibrary dLibraDataSource, Long editionId)
		throws RemoteException, TransformerException, DigitalLibraryException,
		IdNotFoundException
	{
		String metadata;
		if (editionId == Constants.EDITION_QUERY_PARAM_DEFAULT) {
			metadata = dLibraDataSource.getFileMetadata(workspaceId,
				researchObjectId, versionId, filePath,
				uriInfo.getAbsolutePath());
		}
		else {
			metadata = dLibraDataSource.getFileMetadata(workspaceId,
				researchObjectId, versionId, filePath, editionId,
				uriInfo.getAbsolutePath());
		}
		ContentDisposition cd = ContentDisposition.type(
			Constants.RDF_XML_MIME_TYPE).build();
		return Response
				.ok(metadata)
				.header(Constants.CONTENT_DISPOSITION_HEADER_NAME, cd)
				.header(Constants.CONTENT_TYPE_HEADER_NAME,
					Constants.RDF_XML_MIME_TYPE).build();
	}


	private Response getFolderContent(String workspaceId,
			String researchObjectId, String versionId, String filePath,
			DigitalLibrary dLibraDataSource, Long editionId)
		throws RemoteException, DigitalLibraryException, IdNotFoundException
	{
		logger.debug("Detected query for a folder: " + filePath);
		InputStream body;
		if (editionId == Constants.EDITION_QUERY_PARAM_DEFAULT) {
			body = dLibraDataSource.getZippedFolder(workspaceId,
				researchObjectId, versionId, filePath);
		}
		else {
			body = dLibraDataSource.getZippedFolder(workspaceId,
				researchObjectId, versionId, filePath, editionId);
		}
		ContentDisposition cd = ContentDisposition.type("application/zip")
				.fileName(versionId + ".zip").build();
		return Response.ok(body)
				.header(Constants.CONTENT_DISPOSITION_HEADER_NAME, cd).build();
	}


	private Response getFileContent(String workspaceId,
			String researchObjectId, String versionId, String filePath,
			DigitalLibrary dLibraDataSource, Long editionId)
		throws IOException, RemoteException, DigitalLibraryException,
		IdNotFoundException
	{
		InputStream body;
		String mimeType;
		if (editionId == Constants.EDITION_QUERY_PARAM_DEFAULT) {
			body = dLibraDataSource.getFileContents(workspaceId,
				researchObjectId, versionId, filePath);
			mimeType = dLibraDataSource.getFileMimeType(workspaceId,
				researchObjectId, versionId, filePath);
		}
		else {
			body = dLibraDataSource.getFileContents(workspaceId,
				researchObjectId, versionId, filePath, editionId);
			mimeType = dLibraDataSource.getFileMimeType(workspaceId,
				researchObjectId, versionId, filePath, editionId);
		}

		String fileName = uriInfo.getPath().substring(
			1 + uriInfo.getPath().lastIndexOf("/"));
		ContentDisposition cd = ContentDisposition.type(mimeType)
				.fileName(fileName).build();
		return Response.ok(body)
				.header(Constants.CONTENT_DISPOSITION_HEADER_NAME, cd)
				.header(Constants.CONTENT_TYPE_HEADER_NAME, mimeType).build();
	}


	@PUT
	public Response createOrUpdateFile(@PathParam("W_ID")
	String workspaceId, @PathParam("RO_ID")
	String researchObjectId, @PathParam("RO_VERSION_ID")
	String versionId, @PathParam("FILE_PATH")
	String filePath, @HeaderParam(Constants.CONTENT_TYPE_HEADER_NAME)
	String type, InputStream inputStream)
		throws IOException, TransformerException, DigitalLibraryException,
		IdNotFoundException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());

		URI versionUri = Utils.createVersionURI(uriInfo, workspaceId,
			researchObjectId, versionId);

		dl.createOrUpdateFile(versionUri, workspaceId, researchObjectId,
			versionId, filePath, inputStream, type);

		return Response.ok().build();
	}


	@DELETE
	public void deleteFile(@PathParam("W_ID")
	String workspaceId, @PathParam("RO_ID")
	String researchObjectId, @PathParam("RO_VERSION_ID")
	String versionId, @PathParam("FILE_PATH")
	String filePath)
		throws IOException, TransformerException, DigitalLibraryException,
		IdNotFoundException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());

		URI versionUri = Utils.createVersionURI(uriInfo, workspaceId,
			researchObjectId, versionId);

		if (filePath.equals("manifest.rdf"))
			throw new ForbiddenException(
					"Blocked attempt to delete manifest.rdf");

		dl.deleteFile(versionUri, workspaceId, researchObjectId, versionId,
			filePath);
	}
}
