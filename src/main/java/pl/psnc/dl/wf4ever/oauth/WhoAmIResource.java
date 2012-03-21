package pl.psnc.dl.wf4ever.oauth;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.auth.AuthenticationException;
import pl.psnc.dl.wf4ever.auth.SecurityFilter;
import pl.psnc.dl.wf4ever.connection.SemanticMetadataServiceFactory;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dl.wf4ever.dlibra.UserProfile.Role;
import pl.psnc.dl.wf4ever.sms.QueryResult;
import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;

/**
 * 
 * @author Piotr Hołubowicz
 * 
 */
@Path(("whoami/"))
public class WhoAmIResource
{

	@Context
	HttpServletRequest request;

	@Context
	UriInfo uriInfo;


	@GET
	public Response getUserRdfXml(@PathParam("U_ID")
	String urlSafeUserId)
		throws DigitalLibraryException, NotFoundException, ClassNotFoundException, IOException, NamingException,
		SQLException, URISyntaxException
	{
		return getUser(RDFFormat.RDFXML);
	}


	@GET
	@Produces({ "application/x-turtle", "text/turtle"})
	public Response getUserTurtle(@PathParam("U_ID")
	String urlSafeUserId)
		throws DigitalLibraryException, NotFoundException, ClassNotFoundException, IOException, NamingException,
		SQLException, URISyntaxException
	{
		return getUser(RDFFormat.TURTLE);
	}


	@GET
	@Produces("text/rdf+n3")
	public Response getUserN3(@PathParam("U_ID")
	String urlSafeUserId)
		throws DigitalLibraryException, NotFoundException, ClassNotFoundException, IOException, NamingException,
		SQLException, URISyntaxException
	{
		return getUser(RDFFormat.N3);
	}


	private Response getUser(RDFFormat rdfFormat)
		throws DigitalLibraryException, NotFoundException, ClassNotFoundException, IOException, NamingException,
		SQLException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		if (user.getRole() == Role.PUBLIC) {
			throw new AuthenticationException("Only authenticated users can use this resource", SecurityFilter.REALM);
		}
		SemanticMetadataService sms = SemanticMetadataServiceFactory.getService(user);

		QueryResult qs;
		try {
			qs = sms.getUser(UserProfile.generateAbsoluteURI(null, user.getLogin()), rdfFormat);
		}
		finally {
			sms.close();
		}

		return Response.ok(qs.getInputStream()).type(qs.getFormat().getDefaultMIMEType()).build();
	}
}
