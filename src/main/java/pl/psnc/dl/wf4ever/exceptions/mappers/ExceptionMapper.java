/**
 * 
 */
package pl.psnc.dl.wf4ever.exceptions.mappers;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

/**
 * Used for catching exception that were not caught by other mappers and returning
 * <code>500 (Internal Server Error)</code> responses.
 * 
 * @author nowakm
 * 
 */
@Provider
public class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Exception> {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(ExceptionMapper.class);


    @Override
    public Response toResponse(Exception e) {
        if (e instanceof WebApplicationException) {
            WebApplicationException we = (WebApplicationException) e;
            return we.getResponse();
        }
        LOGGER.warn("Caught exception: " + e.getMessage(), e);
        StringBuilder sb = new StringBuilder("");
        sb.append(e.getMessage());
        sb.append("\n");

        // remove these lines before going live
        for (StackTraceElement ste : e.getStackTrace()) {
            sb.append("\t at ");
            sb.append(ste.toString());
            sb.append("\n");
        }

        return Response.status(Status.INTERNAL_SERVER_ERROR).type("text/plain").entity(sb.toString()).build();
    }
}
