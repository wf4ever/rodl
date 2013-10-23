package pl.psnc.dl.wf4ever.accesscontrol.filters;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.accesscontrol.dicts.Role;
import pl.psnc.dl.wf4ever.accesscontrol.model.Permission;
import pl.psnc.dl.wf4ever.accesscontrol.model.dao.PermissionDAO;
import pl.psnc.dl.wf4ever.db.UserProfile;
import pl.psnc.dl.wf4ever.db.dao.UserProfileDAO;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.ForbiddenException;
import pl.psnc.dl.wf4ever.model.Builder;

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

    /** Injected user. */
    private UserMetadata user;
    /** Located userProfile. */
    private UserProfile userProfile;
    /** Permissions dao. */
    private PermissionDAO dao = new PermissionDAO();


    @Override
    public ContainerRequest filter(ContainerRequest request) {
        Builder builder = (Builder) httpRequest.getAttribute("Builder");
        UserMetadata user = builder.getUser();
        if (user.equals(UserProfile.ADMIN)) {
            return request;
        }
        UserProfileDAO profileDao = new UserProfileDAO();
        userProfile = profileDao.findByLogin(user.getLogin());
        if (userProfile == null) {
            throw new ForbiddenException("The user isn't registered.");
        }
        //there are several action handled by accesscontrol service
        //first discover which component is processed (mode | permission | permissionlink)
        //permissions
        //only RO author and create/delete permission
        //only author and someone involved can query about permission/
        if (request.getPath().contains("/permissions/")) {
            handlePermissionsRequest(request);
        }
        //romode
        //only RO author can change the mode - it's about content so it checked in ro mode resource
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
        //if this is a POST everybody can do it it's a matter of logic to store object or not
        if (request.getMethod().equals("POST")) {
            return;
        }
        //if there is a GET request to get particular mode, chech if the user is an owner
        if (request.getMethod().equals("GET")) {
            //if there is an ro paramter
            if (request.getQueryParameters().getFirst("ro") != null) {
                String roUri = request.getQueryParameters().getFirst("ro");
                List<Permission> permissions = dao.findByUserROAndPermission(userProfile, roUri, Role.OWNER);
                if (permissions.size() == 1) {
                    return;
                } else if (permissions.size() == 0) {
                    throw new ForbiddenException("This resource doesn't belong to user");
                } else {
                    LOGGER.error("Data problem - more than one owner for " + roUri);
                    throw new WebApplicationException(500);
                }
            } else if (request.getPath().split("modes/").length == 2
                    && isInteger(request.getPath().split("modes/")[1].replace("/", "").replace(" ", ""))) {
                String modeIdString = request.getPath().split("modes/")[1].replace("/", "").replace(" ", "");
                Permission permission = dao.findById(Integer.valueOf(modeIdString));
                if (permission.getRole() != Role.OWNER || permission.getUser() != userProfile) {
                    throw new ForbiddenException("This resource doesn't belong to user");
                }
            }
        }

    }


    /**
     * Handle Permissions request.
     * 
     * @param request
     *            request
     */
    private void handlePermissionsRequest(ContainerRequest request) {
        //if this is a POST everybody can do it it's a matter of logic to store object or not
        if (request.getMethod().equals("POST")) {
            return;
        } else if (request.getMethod().equals("GET")) {
            if (request.getQueryParameters().getFirst("ro") != null) {
                String roUri = request.getQueryParameters().getFirst("ro");
                List<Permission> permissions = dao.findByResearchObject(roUri);
                for (Permission permission : permissions) {
                    //if user is an owner or has this permission granted
                    if (permission.getUser().equals(userProfile)) {
                        return;
                    }
                }
                throw new ForbiddenException("User has no permission to read from this research object");
            } else if (request.getPath().split("modes/").length == 2 && isInteger(request.getPath().split("modes/")[1])) {
                String permissionIdString = request.getPath().split("modes/")[1].replace("/", "").replace(" ", "");
                Permission permission = dao.findById(Integer.valueOf(permissionIdString));
                if (permission == null) {
                    //it will give 404
                    return;
                }
                List<Permission> ownerPermissionList = dao.findByUserROAndPermission(userProfile, permission.getRo(),
                    Role.OWNER);
                Permission ownerPermission = null;
                if (ownerPermissionList.size() == 0) {
                    //it's enough to check user
                    ownerPermission = permission;
                } else if (ownerPermissionList.size() == 1) {
                    ownerPermission = ownerPermissionList.get(1);
                } else if (ownerPermissionList.size() > 1) {
                    LOGGER.error("Data problem - more than one owner for " + permission.getRo());
                    throw new WebApplicationException(500);
                }
                //check resource permissions
                if (!permission.getUser().equals(userProfile) && !ownerPermission.getUser().equals(userProfile)) {
                    throw new ForbiddenException("User has no permission to read from this research object");
                }
            }
        } else if (request.getMethod().equals("DELETE") && isInteger(request.getPath().split("modes/")[1])) {
            //user must be an owner of ro to delete permissions
            String permissionIdString = request.getPath().split("modes/")[1].replace("/", "").replace(" ", "");
            Permission permission = dao.findById(Integer.valueOf(permissionIdString));
            if (permission == null) {
                //it iwll give 404
                return;
            }
            List<Permission> permissions = dao.findByUserROAndPermission(userProfile, permission.getRo(), Role.OWNER);
            if (permissions.size() == 0) {
                throw new ForbiddenException("This resource doesn't belong to user");
            } else if (permissions.size() > 1) {
                LOGGER.error("Data problem - more than one owner for " + permission.getRo());
                throw new WebApplicationException(500);
            }

        }
    }


    /**
     * Handle Permission Links request.
     * 
     * @param request
     *            request
     */
    private void handlePermissionLinksRequest(ContainerRequest request) {

    }


    /**
     * If is an integer.
     * 
     * @param s
     *            given string
     * @return true if it is.
     */
    public boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}
