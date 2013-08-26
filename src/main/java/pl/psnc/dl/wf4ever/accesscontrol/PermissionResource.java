package pl.psnc.dl.wf4ever.accesscontrol;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.auth.RequestAttribute;
import pl.psnc.dl.wf4ever.model.Builder;

/**
 * API for granting permissions.
 * 
 * @author pejot
 * 
 */
@Path("accesscontrol/permissions/")
public class PermissionResource {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(PermissionResource.class);

    /** URI info. */
    @Context
    UriInfo uriInfo;

    /** Resource builder. */
    @RequestAttribute("Builder")
    private Builder builder;


    @POST
    @Consumes("application/json")
    public Response addPermissions() {
        return null;
    }


    @Path("{permission_id}/")
    @Produces("application/json")
    @GET
    public Response getPermission(@PathParam("permission_id") String mode_id) {
        return null;
    }


    @Path("{permission_id}/")
    @Produces("application/json")
    @DELETE
    public Response deletePermission(@PathParam("permission_id") String mode_id) {
        return null;
    }


    @Produces("application/json")
    @GET
    public Response getPermissions(@QueryParam("ro") String ro) {
        return null;
    }
}
