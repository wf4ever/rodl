package pl.psnc.dl.wf4ever.oauth;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.openrdf.rio.RDFFormat;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.auth.AuthenticationException;
import pl.psnc.dl.wf4ever.auth.SecurityFilter;
import pl.psnc.dl.wf4ever.common.UserProfile;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;
import pl.psnc.dl.wf4ever.sms.QueryResult;

/**
 * A REST API resource for identifying the access token owner.
 * 
 * @author Piotr Hołubowicz
 * 
 */
@Path(("whoami/"))
public class WhoAmIResource {

    /** HTTP request. */
    @Context
    HttpServletRequest request;

    /** URI info. */
    @Context
    UriInfo uriInfo;


    /**
     * Get the access token owner described with RDF/XML format.
     * 
     * @return 200 OK with user metadata serialized in RDF
     */
    @GET
    public Response getUserRdfXml() {
        return getUser(RDFFormat.RDFXML);
    }


    /**
     * Get the access token owner described with Turtle format.
     * 
     * @return 200 OK with user metadata serialized in RDF
     */
    @GET
    @Produces({ "application/x-turtle", "text/turtle" })
    public Response getUserTurtle() {
        return getUser(RDFFormat.TURTLE);
    }


    /**
     * Get the access token owner described with N3 format.
     * 
     * @return 200 OK with user metadata serialized in RDF
     */
    @GET
    @Produces("text/rdf+n3")
    public Response getUserN3() {
        return getUser(RDFFormat.N3);
    }


    /**
     * Get a user.
     * 
     * @param rdfFormat
     *            RDF format of the output
     * @return 200 OK with user metadata serialized in RDF
     */
    private Response getUser(RDFFormat rdfFormat) {
        UserMetadata user = (UserMetadata) request.getAttribute(Constants.USER);
        if (user.getRole() == Role.PUBLIC) {
            throw new AuthenticationException("Only authenticated users can use this resource", SecurityFilter.REALM);
        }

        QueryResult qs = ROSRService.SMS.get().getUser(UserProfile.generateAbsoluteURI(null, user.getLogin()),
            rdfFormat);

        return Response.ok(qs.getInputStream()).type(qs.getFormat().getDefaultMIMEType()).build();
    }
}
