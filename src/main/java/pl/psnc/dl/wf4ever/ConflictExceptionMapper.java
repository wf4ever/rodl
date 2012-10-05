/**
 * 
 */
package pl.psnc.dl.wf4ever;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import pl.psnc.dl.wf4ever.dlibra.ConflictException;

/**
 * Maps <code>ConflictException</code> to <code>409 (Conflict)</code> HTTP response.
 * 
 * @author nowakm
 * 
 */
@Provider
public class ConflictExceptionMapper implements ExceptionMapper<ConflictException> {

    @Override
    public Response toResponse(ConflictException e) {
        return Response.status(Status.CONFLICT).type("text/plain").entity(e.getMessage()).build();
    }

}
