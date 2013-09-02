package pl.psnc.dl.wf4ever.accesscontrol;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import pl.psnc.dl.wf4ever.accesscontrol.model.Permission;
import pl.psnc.dl.wf4ever.accesscontrol.model.dao.PermissionDAO;
import pl.psnc.dl.wf4ever.auth.RequestAttribute;
import pl.psnc.dl.wf4ever.model.Builder;

import com.hp.hpl.jena.shared.NotFoundException;

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

    /** Permissions dao. */
    PermissionDAO dao = new PermissionDAO();


    @POST
    @Consumes("application/json")
    public Response addPermissions(Permission permission) {
        return null;
    }


    @Path("{permission_id}/")
    @Produces("application/json")
    @GET
    public Permission getPermission(@PathParam("permission_id") String permission_id) {
        Permission result = dao.findById(Integer.getInteger(permission_id));
        if (result != null) {
            result.setUri(uriInfo.getRequestUri().resolve(result.getId().toString()));
        }
        return result;
    }


    @Path("{permission_id}/")
    @Produces("application/json")
    @DELETE
    public Response deletePermission(@PathParam("permission_id") String permission_id) {
        Permission permission = dao.findById(Integer.getInteger(permission_id));
        if (permission == null) {
            throw new NotFoundException("The permission " + permission_id + " doesn't exists");
        }
        dao.delete(permission);
        return Response.noContent().build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Permission[] getPermissions(@QueryParam("ro") String ro) {
        List<Permission> result = dao.findByResearchObject(ro);
        if (result == null || result.size() == 0) {
            return new Permission[0];
        }
        Permission[] permissionArray = new Permission[result.size()];
        for (int i = 0; i < result.size(); i++) {
            permissionArray[i] = result.get(i);
            permissionArray[i].setUri(uriInfo.getRequestUri().resolve(permissionArray[i].getId().toString()));
        }
        return permissionArray;

    }
}
