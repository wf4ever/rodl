package pl.psnc.dl.wf4ever;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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
     * @param uriInfo
     *            URI info
     * @return an HTML page
     */
    @GET
    @Produces("text/html")
    public Response index(@Context UriInfo uriInfo) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("version", ApplicationProperties.getVersion());
        map.put("rosrsuri", uriInfo.getAbsolutePathBuilder().path("ROs/").build());
        map.put("roevouri", uriInfo.getAbsolutePathBuilder().path("evo/").build());
        return Response.ok(new Viewable("/index", map)).build();
    }
}
