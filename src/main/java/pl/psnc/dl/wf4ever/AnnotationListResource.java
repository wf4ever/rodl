/**
 * 
 */
package pl.psnc.dl.wf4ever;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
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
@Path(URIs.ANNOTATIONS)
public class AnnotationListResource
{

	@SuppressWarnings("unused")
	private static final Logger log = Logger
			.getLogger(AnnotationListResource.class);

	@Context
	HttpServletRequest request;

	@Context
	private UriInfo uriInfo;


	@GET
	@Produces({ "application/rdf+xml", "text/plain", "text/turtle",
			"application/x-turtle", "text/rdf+n3", "application/trix",
			"application/x-trig"})
	public Response getAnnotations()
		throws ClassNotFoundException, IOException, NamingException,
		SQLException
	{
		String contentType = request.getContentType() != null ? request
				.getContentType() : "application/rdf+xml";
		RDFFormat rdfFormat = RDFFormat.forMIMEType(contentType);

		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		SemanticMetadataService sms = SemanticMetadataServiceFactory
				.getService(user);
		InputStream annotations;
		try {
			if (rdfFormat.supportsContexts()) {
				annotations = sms.getAllAnnotationsWithBodies(
					uriInfo.getAbsolutePath(), rdfFormat);
			}
			else {
				annotations = sms.getAllAnnotations(uriInfo.getAbsolutePath(),
					rdfFormat);
			}
		}
		finally {
			sms.close();
		}

		ContentDisposition cd = ContentDisposition.type(contentType)
				.fileName("annotations." + rdfFormat.getDefaultFileExtension())
				.build();
		return Response.ok(annotations).header("Content-disposition", cd)
				.build();
	}


	@POST
	@Consumes({ "application/rdf+xml", "text/plain", "text/turtle",
			"application/x-turtle", "text/rdf+n3", "application/trix",
			"application/x-trig"})
	public Response createAnnotation(InputStream data)
		throws URISyntaxException, IOException, ClassNotFoundException,
		NamingException, SQLException
	{
		String contentType = request.getContentType() != null ? request
				.getContentType() : "application/rdf+xml";
		String characterEncoding = request.getCharacterEncoding() != null ? request
				.getCharacterEncoding() : "UTF-8";
		RDFFormat rdfFormat = RDFFormat.forMIMEType(contentType);

		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);

		String annotationId = UUID.randomUUID().toString();
		URI annotationURI = uriInfo.getAbsolutePathBuilder()
				.fragment(annotationId).build();
		URI annotationBodyURI = uriInfo.getAbsolutePathBuilder()
				.path(annotationId).build();

		SemanticMetadataService sms = SemanticMetadataServiceFactory
				.getService(user);
		try {

			if (contentType.equals("text/plain")) {
				// FIXME: this could also mean NTriples
				String lines[] = IOUtils.toString(data, characterEncoding)
						.split("[\\r\\n]+");
				Map<URI, Map<URI, String>> triples = new HashMap<URI, Map<URI, String>>();
				for (String line : lines) {
					String[] values = line.split("[\\s]+", 3);
					if (values.length >= 3) {
						URI annotatedResourceURI = new URI(values[0]);
						if (!triples.containsKey(annotatedResourceURI)) {
							triples.put(annotatedResourceURI,
								new HashMap<URI, String>());
						}
						triples.get(annotatedResourceURI).put(
							new URI(values[1]), values[2]);
					}
				}

				sms.addAnnotation(annotationURI, annotationBodyURI, triples,
					user);
			}
			else {
				sms.addAnnotation(annotationURI, annotationBodyURI, data,
					rdfFormat, user);
			}
		}
		finally {
			sms.close();
		}

		return Response.created(annotationBodyURI).build();
	}


	@DELETE
	public void deleteAnnotations()
		throws ClassNotFoundException, IOException, NamingException,
		SQLException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		SemanticMetadataService sms = SemanticMetadataServiceFactory
				.getService(user);

		try {
			sms.deleteAllAnnotationsWithBodies(uriInfo.getAbsolutePath());
		}
		finally {
			sms.close();
		}
	}

}
