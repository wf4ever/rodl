package pl.psnc.dl.wf4ever.accesscontrol.filters;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * RO Resource AccessControl Filter.
 * 
 * @author pejot
 * 
 */
public class ROsResourceFilter implements ContainerRequestFilter {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(ROsResourceFilter.class);

    /** URI info. */
    @Context
    private UriInfo uriInfo;

    /** HTTP request. */
    @Context
    private HttpServletRequest httpRequest;


    @Override
    public ContainerRequest filter(ContainerRequest request) {
        //we can take care only 

        //there are several rules which tells which user read or edit RO
        //first if user is an author they can do everything
        //if RO is public ...
        //... then everybody can read
        //... and users with special permissions can edit
        //if RO is private
        //... nobody apart from author can read
        //if RO is accessed via permission link then RO can be read by everybody with link and can be edited is someone has a special permission.
        return request;
    }

}
