package pl.psnc.dl.wf4ever.accesscontrol.filters;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * Access Control Resource Access Control Filter.
 * 
 * @author pejot
 * 
 */
public class AccessControlResourceFilter implements ContainerRequestFilter {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(AccessControlResourceFilter.class);

    /** URI info. */
    @Context
    private UriInfo uriInfo;

    /** HTTP request. */
    @Context
    private HttpServletRequest httpRequest;


    @Override
    public ContainerRequest filter(ContainerRequest request) {
        //there are several action handled by accesscontrol service
        //first discover which component is processed (mode | permission | permissionlink)
        //permissions
        //only RO author and create/delete permission
        //only author and someone involved can query about permission/
        if (request.getPath().contains("/permissions/")) {
            handlePermissionsRequest(request);
        }
        //romode
        //only RO author can change the mode
        //only RO author can query about mode
        else if (request.getPath().contains("/modes/")) {
            handleROModesRequest(request);
        }
        //permissionlinks
        //only RO author and create/delete permission links
        //only author and someone involved can query about permission links
        else if (request.getPath().contains("/permissionlinks/")) {
            handlePermissionLinksRequest(request);
        }

        return request;
    }


    /**
     * Handle RO modes request.
     * 
     * @param request
     *            request
     */
    private void handleROModesRequest(ContainerRequest request) {

    }


    /**
     * Handle Permissions request.
     * 
     * @param request
     *            request
     */
    private void handlePermissionsRequest(ContainerRequest request) {

    }


    /**
     * Handle Permission Links request.
     * 
     * @param request
     *            request
     */
    private void handlePermissionLinksRequest(ContainerRequest request) {

    }

}
