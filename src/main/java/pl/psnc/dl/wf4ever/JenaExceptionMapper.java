/**
 * 
 */
package pl.psnc.dl.wf4ever;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.hp.hpl.jena.shared.JenaException;

/**
 * Maps <code>JenaException</code> to <code>400 (Bad Request)</code> HTTP response.
 * @author piotrekhol
 *
 */
@Provider
public class JenaExceptionMapper
	implements ExceptionMapper<JenaException>

{

	@Override
	public Response toResponse(JenaException e)
	{
		{
			return Response.status(Status.BAD_REQUEST).type("text/plain")
					.entity(e.getMessage()).build();
		}
	}

}
