package pl.psnc.dl.wf4ever;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import pl.psnc.dl.wf4ever.dlibra.IncorrectManifestException;

/**
 * Maps <code>IdNotFoundException</code> to <code>409 (Conflict)</code> HTTP response.
 * @author piotrekhol
 *
 */
@Provider
public class IncorrectManifestExceptionMapper
	implements ExceptionMapper<IncorrectManifestException>
{

	@Override
	public Response toResponse(IncorrectManifestException e)
	{
		return Response.status(Status.CONFLICT).type("text/plain")
				.entity(e.getMessage()).build();
	}

}
