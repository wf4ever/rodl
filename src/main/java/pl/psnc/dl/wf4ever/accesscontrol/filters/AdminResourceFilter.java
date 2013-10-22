package pl.psnc.dl.wf4ever.accesscontrol.filters;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.db.UserProfile;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.exceptions.ForbiddenException;
import pl.psnc.dl.wf4ever.model.Builder;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * Authentication and authorization filter.
 * 
 * @author pejot
 * 
 */
public class AdminResourceFilter implements ContainerRequestFilter {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(AdminResourceFilter.class);

    /** URI info. */
    @Context
    private UriInfo uriInfo;

    /** HTTP request. */
    @Context
    private HttpServletRequest httpRequest;


    @Override
    public ContainerRequest filter(ContainerRequest request) {
        Builder builder = (Builder) httpRequest.getAttribute("Builder");
        UserMetadata user = builder.getUser();
        if (!user.equals(UserProfile.ADMIN)) {
            throw new ForbiddenException("Only admin user can access admin resource");
        }
        return request;

    }
}
