package pl.psnc.dl.wf4ever.accesscontrol;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.accesscontrol.model.Mode;
import pl.psnc.dl.wf4ever.accesscontrol.model.dao.ModeDAO;
import pl.psnc.dl.wf4ever.auth.RequestAttribute;
import pl.psnc.dl.wf4ever.integration.AbstractIntegrationTest;
import pl.psnc.dl.wf4ever.model.Builder;

/**
 * API for setting Research Object access mode.
 * 
 * @author pejot
 * 
 */
@Path("accesscontrol/modes/")
public class ModeResource extends AbstractIntegrationTest {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(ModeResource.class);
    /** URI info. */
    @Context
    UriInfo uriInfo;

    /** Resource builder. */
    @RequestAttribute("Builder")
    private Builder builder;

    /** Access Mode dao. */
    private ModeDAO dao = new ModeDAO();


    @POST
    @Consumes("application/json")
    public Response setMode(Mode mode) {
        return Response.created(URI.create("uri")).build();
    }


    @GET
    @Path("{mode_id}/")
    public Mode getModeById(@PathParam("mode_id") String mode_id) {
        return dao.findById(Integer.valueOf(mode_id));
    }


    @GET
    public Mode getModeByRo(@QueryParam("ro") String ro) {
        return dao.findByResearchObject(ro);
    }

}
