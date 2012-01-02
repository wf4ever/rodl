package pl.psnc.dl.wf4ever.oauth;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.auth.AuthenticationException;
import pl.psnc.dl.wf4ever.auth.SecurityFilter;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dl.wf4ever.dlibra.UserProfile.Role;

/**
 * 
 * @author Piotr Hołubowicz
 *
 */
@Path(("whoami/"))
public class WhoAmIResource
{

	@Context
	HttpServletRequest request;

	@Context
	UriInfo uriInfo;


	@GET
	public Response getUser()
		throws RemoteException, MalformedURLException, UnknownHostException,
		DigitalLibraryException, NotFoundException
	{
		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
		if (user.getRole() == Role.PUBLIC) {
			throw new AuthenticationException(
					"Only authenticated users can use this resource",
					SecurityFilter.REALM);
		}

		StringBuilder sb = new StringBuilder();
		sb.append(user.getLogin());
		if (user.getName() != null) {
			sb.append("\r\n");
			sb.append(user.getName());
		}

		return Response.ok(sb.toString()).build();
	}
}
