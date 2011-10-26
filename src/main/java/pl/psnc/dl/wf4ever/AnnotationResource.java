/**
 * 
 */
package pl.psnc.dl.wf4ever;

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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.connection.SemanticMetadataServiceFactory;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;

import com.sun.jersey.core.header.ContentDisposition;

/**
 * @author Piotr Hołubowicz
 * 
 */
@Path(URIs.ANNOTATION_ID)
public class AnnotationResource {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(AnnotationResource.class);

	@Context
	HttpServletRequest request;

	@Context
	private UriInfo uriInfo;

	@GET
	@Produces({ "text/xml", "application/rdf+xml", "application/x+trig" })
	public Response getAnnotation() {
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		SemanticMetadataService sms = SemanticMetadataServiceFactory.getService(user);

		SemanticMetadataService.Notation notation = URIs.recognizeNotation(request.getContentType());
		InputStream manifest = sms.getAnnotationBody(uriInfo.getAbsolutePath(), notation);
		ContentDisposition cd = URIs.generateContentDisposition(notation, "annotations");
		return Response.ok(manifest).header("Content-disposition", cd).build();
	}

	@PUT
	@Consumes("text/plain")
	@Produces("text/plain")
	public Response createAnnotation(String data) throws URISyntaxException {
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		SemanticMetadataService sms = SemanticMetadataServiceFactory.getService(user);

		String lines[] = data.split("[\\r\\n]+");
		if (lines.length < 2) {
			return Response.status(Status.BAD_REQUEST).entity("Content is shorter than 2 lines")
					.header("Content-type", "text/plain").build();
		}
		URI annotatedResourceURI = new URI(lines[0]);
		Map<String, String> attributes = new HashMap<String, String>();
		for (int i = 1; i < lines.length; i++) {
			String[] values = lines[i].split("[\\s]+", 2);
			if (values.length == 2) {
				attributes.put(values[0], values[1]);
			}
		}
		URI annotationBodyURI = uriInfo.getAbsolutePath();
		URI baseURI = annotationBodyURI.resolve(".");
		String annotationId = baseURI.relativize(annotationBodyURI).toString();
		URI annotationURI = uriInfo.getAbsolutePathBuilder().path(".").fragment(annotationId).build();

		sms.addAnnotation(annotationURI, annotationBodyURI, annotatedResourceURI, attributes);

		return Response.ok().build();
	}

	@DELETE
	public void deleteAnnotation() {
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		SemanticMetadataService sms = SemanticMetadataServiceFactory.getService(user);

		sms.deleteAnnotationsWithBodies(uriInfo.getAbsolutePath());
	}

}
