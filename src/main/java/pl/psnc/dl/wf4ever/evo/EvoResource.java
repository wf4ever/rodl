package pl.psnc.dl.wf4ever.evo;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.Constants;

/**
 * 
 * @author piotrhol
 * 
 */
@Path("evo/")
public class EvoResource {

    @SuppressWarnings("unused")
    private final static Logger logger = Logger.getLogger(EvoResource.class);

    @Context
    HttpServletRequest request;

    @Context
    UriInfo uriInfo;


    @POST
    @Consumes(Constants.RO_COPY_MIME_TYPE)
    public Response createCopy() {
        return null;
    }
}
