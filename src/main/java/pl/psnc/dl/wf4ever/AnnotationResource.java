/**
 * 
 */
package pl.psnc.dl.wf4ever;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
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
@Path(URIs.ANNOTATION_ID)
public class AnnotationResource
{

	@SuppressWarnings("unused")
	private static final Logger log = Logger
			.getLogger(AnnotationResource.class);

	@Context
	HttpServletRequest request;

	@Context
	private UriInfo uriInfo;


	@GET
	@Produces({ "application/rdf+xml", "text/plain", "text/turtle",
			"application/x-turtle", "text/rdf+n3", "application/trix",
			"application/x-trig"})
	public Response getAnnotation()
	{
		String contentType = request.getContentType() != null ? request
				.getContentType() : "application/rdf+xml";
		RDFFormat rdfFormat = RDFFormat.forMIMEType(contentType);

		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		SemanticMetadataService sms = SemanticMetadataServiceFactory
				.getService(user);
		InputStream manifest = sms.getAnnotationBody(uriInfo.getAbsolutePath(),
			rdfFormat);

		ContentDisposition cd = ContentDisposition.type(contentType)
				.fileName("annotation." + rdfFormat.getDefaultFileExtension())
				.build();
		return Response.ok(manifest).header("Content-disposition", cd).build();
	}


	@PUT
	@Consumes({ "application/rdf+xml", "text/plain", "text/turtle",
			"application/x-turtle", "text/rdf+n3", "application/trix",
			"application/x-trig"})
	public Response createAnnotation(InputStream data)
		throws URISyntaxException, IOException
	{
		String contentType = request.getContentType() != null ? request
				.getContentType() : "application/rdf+xml";
		String characterEncoding = request.getCharacterEncoding() != null ? request
				.getCharacterEncoding() : "UTF-8";
		RDFFormat rdfFormat = RDFFormat.forMIMEType(contentType);

		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		SemanticMetadataService sms = SemanticMetadataServiceFactory
				.getService(user);

		URI annotationBodyURI = uriInfo.getAbsolutePath();
		URI baseURI = annotationBodyURI.resolve(".");
		String annotationId = baseURI.relativize(annotationBodyURI).toString();
		URI annotationURI = uriInfo.getAbsolutePathBuilder().path(".")
				.fragment(annotationId).build();

		if (contentType.equals("text/plain")) {
			// FIXME: this could also mean NTriples
			String lines[] = IOUtils.toString(data, characterEncoding).split(
				"[\\r\\n]+");
			Map<URI, Map<URI, String>> triples = new HashMap<URI, Map<URI, String>>();
			for (String line : lines) {
				String[] values = line.split("[\\s]+", 3);
				if (values.length >= 3) {
					URI annotatedResourceURI = new URI(values[0]);
					if (!triples.containsKey(annotatedResourceURI)) {
						triples.put(annotatedResourceURI,
							new HashMap<URI, String>());
					}
					triples.get(annotatedResourceURI).put(new URI(values[1]),
						values[2]);
				}
			}

			sms.addAnnotation(annotationURI, annotationBodyURI, triples, user);
		}
		else {
			sms.addAnnotation(annotationURI, annotationBodyURI, data,
				rdfFormat, user);
		}

		return Response.ok().build();
	}


	@DELETE
	public void deleteAnnotation()
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		SemanticMetadataService sms = SemanticMetadataServiceFactory
				.getService(user);

		sms.deleteAnnotationWithBody(uriInfo.getAbsolutePath());
	}

}
