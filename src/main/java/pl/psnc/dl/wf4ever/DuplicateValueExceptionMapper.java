/**
 * 
 */
package pl.psnc.dl.wf4ever;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import pl.psnc.dlibra.service.DuplicatedValueException;

/**
 * Maps <code>DuplicatedValueException</code> to <code>409 (Conflict)</code> HTTP response.
 * @author nowakm
 *
 */
@Provider
public class DuplicateValueExceptionMapper
	implements ExceptionMapper<DuplicatedValueException>

{

	@Override
	public Response toResponse(DuplicatedValueException e)
	{
		{
			return Response.status(Status.CONFLICT).type("text/plain")
					.entity(e.getMessage()).build();
		}
	}

}
