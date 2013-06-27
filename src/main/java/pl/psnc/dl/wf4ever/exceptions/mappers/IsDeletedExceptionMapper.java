package pl.psnc.dl.wf4ever.exceptions.mappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import pl.psnc.dl.wf4ever.exceptions.IsDeletedException;

/**
 * Maps {@link IsDeletedException} to <code>410 Gone</code> HTTP response.
 * 
 * @author piotrhol
 * 
 */
@Provider
public class IsDeletedExceptionMapper implements ExceptionMapper<IsDeletedException> {

    @Override
    public Response toResponse(IsDeletedException e) {
        return Response.status(Status.GONE).type("text/plain").entity(e.getMessage()).build();
    }

}
