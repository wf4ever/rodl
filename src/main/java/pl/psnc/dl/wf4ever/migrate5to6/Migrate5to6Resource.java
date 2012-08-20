package pl.psnc.dl.wf4ever.migrate5to6;

import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.rosrs.ROSRService;

/**
 * 
 * @author piotrhol
 * 
 */
@Path("5to6/")
public class Migrate5to6Resource {

    @SuppressWarnings("unused")
    private final static Logger logger = Logger.getLogger(Migrate5to6Resource.class);

    @Context
    HttpServletRequest request;

    @Context
    UriInfo uriInfo;


    @GET
    @Produces("text/plain")
    public Response migrate5to6()
            throws NamingException, SQLException {
        int cnt = ROSRService.SMS.get().migrateRosr5To6("java:/comp/env/jdbc/rosrs");

        return Response.ok().entity("" + cnt + " new quads copied.").build();
    }

}
