package pl.psnc.dl.wf4ever.migrate5to6;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.BadRequestException;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;

/**
 * 
 * @author piotrhol
 * 
 */
@Path("userUpdate/")
public class UpdateUserUriResource {

    @SuppressWarnings("unused")
    private final static Logger logger = Logger.getLogger(UpdateUserUriResource.class);

    @Context
    HttpServletRequest request;

    @Context
    UriInfo uriInfo;


    @POST
    @Produces("text/plain")
    public Response updateUserURI(String content)
            throws NamingException, SQLException, BadRequestException, URISyntaxException {
        String[] ids = content.split("\r\n");
        if (ids.length < 2) {
            throw new BadRequestException("Must provide 2 URIs in separate lines");
        }
        int cnt = ROSRService.SMS.get().changeURI(new URI(ids[0]), new URI(ids[1]));

        return Response.ok().entity("" + cnt + " quads updated.").build();
    }
}
