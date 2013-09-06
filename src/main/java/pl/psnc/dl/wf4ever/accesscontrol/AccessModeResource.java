package pl.psnc.dl.wf4ever.accesscontrol;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.accesscontrol.model.AccessMode;
import pl.psnc.dl.wf4ever.accesscontrol.model.dao.ModeDAO;
import pl.psnc.dl.wf4ever.auth.RequestAttribute;
import pl.psnc.dl.wf4ever.model.Builder;

/**
 * API for setting Research Object access mode.
 * 
 * @author pejot
 * 
 */
@Path("accesscontrol/modes/")
public class AccessModeResource {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(AccessModeResource.class);
    /** URI info. */
    @Context
    UriInfo uriInfo;

    /** Resource builder. */
    @RequestAttribute("Builder")
    private Builder builder;

    /** Access Mode dao. */
    private ModeDAO dao = new ModeDAO();


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setMode(AccessMode mode) {
        AccessMode storedMode = dao.findByResearchObject(mode.getRo());
        if (storedMode == null) {
            LOGGER.error("Mode for " + mode.getRo() + " Couldn't be found");
            storedMode = new AccessMode();
            storedMode.setRo(mode.getRo());
        }
        storedMode.setMode(mode.getMode());
        dao.save(storedMode);
        storedMode.setUri(uriInfo.getRequestUri().resolve(storedMode.getId().toString()));
        return Response.created(uriInfo.getRequestUri().resolve(storedMode.getId().toString())).entity(storedMode)
                .build();
    }


    @GET
    @Path("{mode_id}/")
    public AccessMode getModeById(@PathParam("mode_id") String mode_id) {
        AccessMode result = dao.findById(Integer.valueOf(mode_id));
        if (result != null) {
            result.setUri(uriInfo.getRequestUri().resolve(result.getId().toString()));
        }
        return result;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public AccessMode getModeByRo(@QueryParam("ro") String ro) {
        AccessMode result = dao.findByResearchObject(ro);
        if (result != null) {
            result.setUri(uriInfo.getRequestUri().resolve(result.getId().toString()));
        }
        return result;
    }
}
