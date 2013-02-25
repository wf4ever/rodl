package pl.psnc.dl.wf4ever.exceptions.mappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import pl.psnc.dl.wf4ever.exceptions.ForbiddenException;

/**
 * <p>
 * Maps <code>ForbiddenException</code> to a HTTP <code>403 (Forbidden)</code> response.
 * </p>
 */
@Provider
public class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {

    @Override
    public Response toResponse(ForbiddenException e) {
        return Response.status(Status.FORBIDDEN).type("text/plain").entity(e.getMessage()).build();
    }

}
