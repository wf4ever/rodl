package pl.psnc.dl.wf4ever.exceptions.mappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import pl.psnc.dl.wf4ever.exceptions.AuthenticationException;

/**
 * <p>
 * Map an authentication exception to an HTTP 401 response, optionally including the realm for a credentials challenge
 * at the client.
 * </p>
 */
@Provider
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {

    @Override
    public Response toResponse(AuthenticationException e) {
        if (e.getRealm() != null) {
            return Response.status(Status.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Basic realm=\"" + e.getRealm() + "\"").type("text/plain")
                    .entity(e.getMessage()).build();
        } else {
            return Response.status(Status.UNAUTHORIZED).type("text/plain").entity(e.getMessage()).build();
        }
    }

}
