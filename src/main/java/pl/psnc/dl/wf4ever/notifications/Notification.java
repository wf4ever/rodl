package pl.psnc.dl.wf4ever.notifications;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Resource for Atom feed.
 * 
 * @author pejot
 * 
 */
@Path("notifications/")
public class Notification {

    @GET
    @Produces(MediaType.APPLICATION_ATOM_XML)
    public Response getAtomFeed(@QueryParam("from") String from, @QueryParam("to") String to,
            @QueryParam("limit") Integer limit, @QueryParam("source") URI source) {
        System.out.println(from);
        System.out.println(to);
        System.out.println(source);
        System.out.println(limit);
        //create Atom feed and get list of entries
        //send it back 
        //ta dam :D
        return null;
    }

}
