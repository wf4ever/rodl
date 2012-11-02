package pl.psnc.dl.wf4ever;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sun.jersey.api.view.Viewable;

/**
 * The base URI of RODL.
 * 
 * @author piotrekhol
 * 
 */
@Path("/")
public class RootResource {

    /**
     * Return the main HTML page.
     * 
     * @return an HTML page
     */
    @GET
    @Produces("text/html")
    public Viewable index() {
        return new Viewable("/index");
    }
}
