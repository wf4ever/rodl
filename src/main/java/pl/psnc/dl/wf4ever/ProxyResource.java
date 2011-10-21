/**
 * 
 */
package pl.psnc.dl.wf4ever;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

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

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.connection.SemanticMetadataServiceFactory;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService.Notation;

import com.sun.jersey.core.header.ContentDisposition;

/**
 * @author Piotr Hołubowicz
 * 
 */
@Path(URIs.PROXY_ID)
public class ProxyResource
{

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ProxyResource.class);

	@Context
	HttpServletRequest request;

	@Context
	private UriInfo uriInfo;


	@GET
	@Produces({ "text/xml", "application/rdf+xml", "application/x+trig"})
	public Response getProxy()
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		SemanticMetadataService sms = SemanticMetadataServiceFactory
				.getService(user);

		String contentType = request.getContentType();
		SemanticMetadataService.Notation notation;
		if ("application/x+trig".equals(contentType)) {
			notation = Notation.TRIG;
		}
		else if ("application/rdf+xml".equals(contentType)) {
			notation = Notation.RDF_XML;
		}
		else {
			contentType = "text/plain";
			notation = Notation.TEXT_PLAIN;
		}
		InputStream manifest = sms
				.getProxy(uriInfo.getAbsolutePath(), notation);
		ContentDisposition cd = ContentDisposition.type(contentType)
				.fileName(Constants.MANIFEST_FILENAME).build();
		return Response.ok(manifest)
				.header(Constants.CONTENT_DISPOSITION_HEADER_NAME, cd).build();
	}


	@PUT
	@Consumes("text/plain")
	@Produces("text/plain")
	public Response createProxy(String data)
		throws URISyntaxException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		SemanticMetadataService sms = SemanticMetadataServiceFactory
				.getService(user);

		URI proxyURI = uriInfo.getAbsolutePath();
		URI proxyForURI = new URI(data);
		URI proxyInURI = uriInfo.getAbsolutePath().resolve("./..");

		sms.addProxy(proxyURI, proxyForURI, proxyInURI);

		return Response.ok().build();
	}


	@DELETE
	public void deleteProxy()
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		SemanticMetadataService sms = SemanticMetadataServiceFactory
				.getService(user);

		sms.deleteProxy(uriInfo.getAbsolutePath());
	}

}
