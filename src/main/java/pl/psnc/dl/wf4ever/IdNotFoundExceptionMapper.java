package pl.psnc.dl.wf4ever;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import pl.psnc.dlibra.service.IdNotFoundException;


@Provider
public class IdNotFoundExceptionMapper
	implements ExceptionMapper<IdNotFoundException>
{

	@Override
	public Response toResponse(IdNotFoundException e)
	{
		return Response.status(Status.NOT_FOUND).type("text/plain")
				.entity(e.getMessage()).build();
	}

}
