package pl.psnc.dl.wf4ever.auth;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * <p>Map an access denied exception to an HTTP 403 response.</p>
 */
@Provider
public class ForbiddenExceptionMapper
	implements ExceptionMapper<ForbiddenException>
{

	public Response toResponse(ForbiddenException e)
	{
		return Response.status(Status.FORBIDDEN).type("text/plain")
				.entity(e.getMessage()).build();
	}

}
