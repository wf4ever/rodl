package pl.psnc.dl.wf4ever.auth;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.connection.DlibraConnectionRegistry;
import pl.psnc.dl.wf4ever.dlibra.DLibraDataSource;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;

import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

public class SecurityFilter
	implements ContainerRequestFilter
{

	private final static Logger logger = Logger.getLogger(SecurityFilter.class);

	private static final String REALM = "RO SRS";

	@Context
	private UriInfo uriInfo;

	@Context
	private HttpServletRequest httpRequest;


	public ContainerRequest filter(ContainerRequest request)
	{
		try {
			DLibraDataSource dLibraDataSource = authenticate(request);
			httpRequest.setAttribute(Constants.DLIBRA_DATA_SOURCE,
				dLibraDataSource);
		}
		catch (AccessDeniedException e) {
			throw new MappableContainerException(new AuthenticationException(
					"Incorrect login/password\r\n", REALM));
		}
		catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		catch (DLibraException e) {
			throw new RuntimeException(e);
		}

		return request;
	}


	private DLibraDataSource authenticate(ContainerRequest request)
		throws MalformedURLException, RemoteException, AccessDeniedException,
		UnknownHostException, DLibraException
	{
		//TODO allow only secure https connections
		//		logger.info("Connection secure? " + isSecure());
		logger.info("Request to: " + uriInfo.getAbsolutePath() + " | method:  "
				+ request.getMethod());

		// Extract authentication credentials
		String authentication = request
				.getHeaderValue(ContainerRequest.AUTHORIZATION);
		if (authentication == null) {
			throw new MappableContainerException(new AuthenticationException(
					"Authentication credentials are required\r\n", REALM));
		}
		String[] values;
		if (authentication.startsWith("Basic ")) {
			values = getBasicCredentials(authentication.substring("Basic "
					.length()));
		}
		// this is the recommended OAuth 2.0 method
		else if (authentication.startsWith("Bearer ")) {
			values = getBearerCredentials(authentication.substring("Bearer "
					.length()));
		}
		else {
			throw new MappableContainerException(
					new AuthenticationException(
							"Only HTTP Basic and OAuth 2.0 Bearer authentications are supported\r\n",
							REALM));
		}
		if (values.length < 2) {
			throw new MappableContainerException(new AuthenticationException(
					"Invalid syntax for username and password\r\n", REALM));
		}
		String username = values[0];
		String password = values[1];
		if ((username == null) || (password == null)) {
			throw new MappableContainerException(new AuthenticationException(
					"Missing username or password\r\n", REALM));
		}

		logger.debug("Request from user: " + username + " | password: "
				+ password);

		return DlibraConnectionRegistry.getConnection().getDLibraDataSource(
			username, password);

	}


	private String[] getBearerCredentials(String accessToken)
	{
		OAuthManager manager = new OAuthManager();
		AccessToken token = manager.getAccessToken(accessToken);
		if (token == null) {
			return getBasicCredentials(accessToken);
		}
		return new String[] { token.getUser().getUsername(),
				token.getUser().getPassword()};
	}


	/**
	 * @param authentication
	 * @return
	 */
	private String[] getBasicCredentials(String authentication)
	{
		String[] values = new String(Base64.base64Decode(authentication))
				.split(":");
		return values;
	}


	public boolean isSecure()
	{
		return "https".equals(uriInfo.getRequestUri().getScheme());
	}
}
