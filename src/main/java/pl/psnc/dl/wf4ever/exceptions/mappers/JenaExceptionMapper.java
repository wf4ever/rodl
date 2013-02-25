/**
 * 
 */
package pl.psnc.dl.wf4ever.exceptions.mappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.shared.JenaException;

/**
 * Maps <code>JenaException</code> to <code>400 (Bad Request)</code> HTTP response.
 * 
 * @author piotrekhol
 * 
 */
@Provider
public class JenaExceptionMapper implements ExceptionMapper<JenaException> {

    /** logger. */
    private static final Logger logger = Logger.getLogger(JenaExceptionMapper.class);


    @Override
    public Response toResponse(JenaException e) {
        logger.error("Caught Jena exception", e);
        return Response.status(Status.BAD_REQUEST).type("text/plain").entity(e.getMessage()).build();
    }

}
