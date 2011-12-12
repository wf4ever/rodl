package pl.psnc.dl.wf4ever.rosrs;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.auth.ForbiddenException;
import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
import pl.psnc.dl.wf4ever.connection.SemanticMetadataServiceFactory;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibrary;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dl.wf4ever.dlibra.ResourceInfo;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;

import com.sun.jersey.core.header.ContentDisposition;

/**
 * 
 * @author Piotr Hołubowicz
 * 
 */
@Path("ROs/{ro_id}/{filePath: [^\\.].+}")
public class AggregatedResource
{

	@SuppressWarnings("unused")
	private final static Logger logger = Logger
			.getLogger(AggregatedResource.class);

	@Context
	private HttpServletRequest request;

	@Context
	private UriInfo uriInfo;

	private static final String workspaceId = "default";

	private static final String versionId = "v1";


	@GET
	public Response getResource(@PathParam("ro_id")
	String researchObjectId, @PathParam("filePath")
	String filePath, @QueryParam("content")
	String isContentRequested)
		throws IOException, TransformerException, DigitalLibraryException,
		NotFoundException, ClassNotFoundException, NamingException,
		SQLException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		SemanticMetadataService sms = SemanticMetadataServiceFactory
				.getService(user);

		URI researchObjectURI = uriInfo.getBaseUriBuilder().path("ROs")
				.path(researchObjectId).build();

		try {
			if (sms.isROMetadataNamedGraph(researchObjectURI,
				uriInfo.getAbsolutePath())) {
				return getNamedGraph(sms);
			}
			else {
				if (isContentRequested == null) {
					return getResourceMetadata(sms, researchObjectURI);
				}
				else {
					if (!sms.isRoFolder(researchObjectURI,
						uriInfo.getAbsolutePath())) {
						return getFileContent(workspaceId, researchObjectId,
							versionId, filePath, user);
					}
					else {
						return getFolderContent(workspaceId, researchObjectId,
							versionId, filePath, user);
					}
				}
			}
		}
		finally {
			sms.close();
		}
	}


	private Response getResourceMetadata(SemanticMetadataService sms,
			URI researchObjectURI)
		throws ClassNotFoundException, IOException, NamingException,
		SQLException
	{
		String contentType = request.getContentType() != null ? request
				.getContentType() : "application/rdf+xml";
		RDFFormat rdfFormat = RDFFormat.forMIMEType(contentType);

		InputStream body = sms.getResource(researchObjectURI,
			uriInfo.getAbsolutePath(), rdfFormat);
		String filename = uriInfo.getAbsolutePath().resolve(".")
				.relativize(uriInfo.getAbsolutePath()).toString();

		ContentDisposition cd = ContentDisposition.type(contentType)
				.fileName(filename + "." + rdfFormat.getDefaultFileExtension())
				.build();
		return Response.ok(body).header("Content-disposition", cd).build();
	}


	private Response getFolderContent(String workspaceId,
			String researchObjectId, String versionId, String filePath,
			UserProfile user)
		throws RemoteException, DigitalLibraryException, NotFoundException,
		MalformedURLException, UnknownHostException
	{
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());
		InputStream body = dl.getZippedFolder(workspaceId, researchObjectId,
			versionId, filePath);
		ContentDisposition cd = ContentDisposition.type("application/zip")
				.fileName(versionId + ".zip").build();
		return Response.ok(body).header("Content-disposition", cd).build();
	}


	private Response getFileContent(String workspaceId,
			String researchObjectId, String versionId, String filePath,
			UserProfile user)
		throws IOException, RemoteException, DigitalLibraryException,
		NotFoundException
	{
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());
		InputStream body = dl.getFileContents(workspaceId, researchObjectId,
			versionId, filePath);
		String mimeType = dl.getFileMimeType(workspaceId, researchObjectId,
			versionId, filePath);

		String fileName = uriInfo.getPath().substring(
			1 + uriInfo.getPath().lastIndexOf("/"));
		ContentDisposition cd = ContentDisposition.type(mimeType)
				.fileName(fileName).build();
		return Response.ok(body).header("Content-disposition", cd)
				.header("Content-type", mimeType).build();
	}


	private Response getNamedGraph(SemanticMetadataService sms)
		throws ClassNotFoundException, IOException, NamingException,
		SQLException
	{
		String contentType = request.getContentType() != null ? request
				.getContentType() : "application/rdf+xml";
		RDFFormat rdfFormat = RDFFormat.forMIMEType(contentType);

		InputStream manifest = sms.getNamedGraph(uriInfo.getAbsolutePath(),
			rdfFormat);

		ContentDisposition cd = ContentDisposition.type(contentType)
				.fileName("annotation." + rdfFormat.getDefaultFileExtension())
				.build();
		return Response.ok(manifest).header("Content-disposition", cd).build();
	}


	@PUT
	public Response createOrUpdateFile(@PathParam("ro_id")
	String researchObjectId, @PathParam("filePath")
	String filePath, InputStream inputStream)
		throws ClassNotFoundException, IOException, NamingException,
		SQLException, DigitalLibraryException, NotFoundException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		String contentType = request.getContentType() != null ? request
				.getContentType() : "application/rdf+xml";
		RDFFormat rdfFormat = RDFFormat.forMIMEType(contentType);
		URI researchObjectURI = uriInfo.getBaseUriBuilder().path("ROs")
				.path(researchObjectId).build();
		URI manifestURI = uriInfo.getBaseUriBuilder().path("ROs")
				.path(researchObjectId).path(".ro/manifest").build();

		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());

		ResourceInfo resourceInfo = dl.createOrUpdateFile(workspaceId,
			researchObjectId, versionId, filePath, inputStream,
			request.getContentType());

		SemanticMetadataService sms = SemanticMetadataServiceFactory
				.getService(user);
		try {
			if (sms.isROMetadataNamedGraph(researchObjectURI,
				uriInfo.getAbsolutePath())) {
				if (manifestURI.equals(uriInfo.getAbsolutePath())) {
					sms.updateManifest(manifestURI, inputStream, rdfFormat);
				}
				else {
					sms.addNamedGraph(uriInfo.getAbsolutePath(), inputStream,
						rdfFormat);
				}
			}
			else {
				sms.addResource(researchObjectURI, uriInfo.getAbsolutePath(),
					resourceInfo);
			}
		}
		finally {
			sms.close();
		}

		return Response.ok().build();
	}


	@DELETE
	public void deleteFile(@PathParam("ro_id")
	String researchObjectId, @PathParam("filePath")
	String filePath)
		throws DigitalLibraryException, NotFoundException,
		ClassNotFoundException, IOException, NamingException, SQLException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		DigitalLibrary dl = DigitalLibraryFactory.getDigitalLibrary(
			user.getLogin(), user.getPassword());

		URI researchObjectURI = uriInfo.getBaseUriBuilder().path("ROs")
				.path(researchObjectId).build();
		URI manifestURI = uriInfo.getBaseUriBuilder().path("ROs")
				.path(researchObjectId).path(".ro/manifest").build();

		if (manifestURI.equals(uriInfo.getAbsolutePath())) {
			throw new ForbiddenException("Can't delete the manifest");
		}

		dl.deleteFile(workspaceId, researchObjectId, versionId, filePath);
		SemanticMetadataService sms = SemanticMetadataServiceFactory
				.getService(user);
		try {
			if (sms.isROMetadataNamedGraph(researchObjectURI,
				uriInfo.getAbsolutePath())) {
				sms.removeNamedGraph(researchObjectURI,
					uriInfo.getAbsolutePath());
			}
			else {
				sms.removeResource(researchObjectURI, uriInfo.getAbsolutePath());
			}
		}
		finally {
			sms.close();
		}
	}

}
