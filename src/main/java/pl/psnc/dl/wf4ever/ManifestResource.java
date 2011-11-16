/**
 * 
 */
package pl.psnc.dl.wf4ever;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.connection.SemanticMetadataServiceFactory;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;

import com.sun.jersey.core.header.ContentDisposition;

/**
 * @author Piotr Hołubowicz
 * 
 */
@Path(URIs.MANIFEST)
public class ManifestResource
{

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ManifestResource.class);

	@Context
	HttpServletRequest request;

	@Context
	private UriInfo uriInfo;


	@GET
	@Produces({ "application/rdf+xml", "text/plain", "text/turtle",
			"application/x-turtle", "text/rdf+n3", "application/trix",
			"application/x-trig"})
	public Response getManifest()
		throws ClassNotFoundException, IOException, NamingException,
		SQLException
	{
		String contentType = request.getContentType() != null ? request
				.getContentType() : "application/rdf+xml";
		RDFFormat rdfFormat = RDFFormat.forMIMEType(contentType);

		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		SemanticMetadataService sms = SemanticMetadataServiceFactory
				.getService(user);
		InputStream manifest;
		try {
			manifest = sms.getManifest(uriInfo.getAbsolutePath(), rdfFormat);
		}
		finally {
			sms.close();
		}

		ContentDisposition cd = ContentDisposition.type(contentType)
				.fileName("manifest." + rdfFormat.getDefaultFileExtension())
				.build();
		return Response.ok(manifest).header("Content-disposition", cd).build();
	}


	@PUT
	@Consumes({ "application/rdf+xml", "text/plain", "text/turtle",
			"application/x-turtle", "text/rdf+n3", "application/trix",
			"application/x-trig"})
	public Response updateManifest(InputStream data)
		throws URISyntaxException, IOException, ClassNotFoundException,
		NamingException, SQLException
	{
		String contentType = request.getContentType() != null ? request
				.getContentType() : "application/rdf+xml";
		RDFFormat rdfFormat = RDFFormat.forMIMEType(contentType);

		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);

		SemanticMetadataService sms = SemanticMetadataServiceFactory
				.getService(user);
		try {

			sms.createManifest(uriInfo.getAbsolutePath(), data, rdfFormat, user);
		}
		finally {
			sms.close();
		}

		return Response.ok().build();
	}

}
