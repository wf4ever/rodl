package pl.psnc.dl.wf4ever;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.dl.NotFoundException;

/**
 * Maps {@link NotFoundException} to <code>404 (Not Found)</code> HTTP response.
 * 
 * @author piotrekhol
 * 
 */
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(NotFoundExceptionMapper.class);


    @Override
    public Response toResponse(NotFoundException e) {
        LOGGER.warn("Caught not found exception: " + e.getMessage());
        return Response.status(Status.NOT_FOUND).type("text/plain").entity(e.getMessage()).build();
    }

}
