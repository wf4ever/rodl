package pl.psnc.dl.wf4ever.accesscontrol.filters;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * Authentication and authorization filter.
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
        return request;
    }

}
