package pl.psnc.dl.wf4ever.auth;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
import pl.psnc.dl.wf4ever.connection.SemanticMetadataServiceFactory;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;

import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * Authentication and authorization filter.
 * 
 * @author piotrekhol
 * 
 */
public class SecurityFilter implements ContainerRequestFilter {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(SecurityFilter.class);

    /** authentication realm. */
    public static final String REALM = "ROSRS";

    /** URI info. */
    @Context
    private UriInfo uriInfo;

    /** HTTP request. */
    @Context
    private HttpServletRequest httpRequest;


    @Override
    public ContainerRequest filter(ContainerRequest request) {
        try {
            UserCredentials creds = authenticate(request);
            //HACK FIXME
            UserCredentials superUserCreds = new UserCredentials("wfadmin", "wfadmin!!!");
            ROSRService.DL.set(DigitalLibraryFactory.getDigitalLibrary(superUserCreds));
            UserProfile user = ROSRService.DL.get().getUserProfile(creds.getUserId());
            ROSRService.SMS.set(SemanticMetadataServiceFactory.getService(user));
            httpRequest.setAttribute(Constants.USER, user);

            //TODO in here should go access rights control, based on dLibra for example
            //            if (!request.getMethod().equals("GET") && user.getRole() == UserProfile.Role.PUBLIC) {
            //                throw new AuthenticationException("Only authenticated users can do that.", SecurityFilter.REALM);
            //            }
            if (creds == UserCredentials.PUBLIC_USER) {
                LOGGER.info("Public credentials for: " + request.getMethod() + " "
                        + request.getAbsolutePath().toString());
            }
        } catch (DigitalLibraryException e) {
            throw new MappableContainerException(new AuthenticationException("Incorrect login/password\r\n", REALM));
        } catch (NotFoundException | SQLException | NamingException | IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return request;
    }


    /**
     * Identify the user making the request.
     * 
     * @param request
     *            HTTP request
     * @return user credentials, UserCredentials.PUBLIC_USER if the request is not authenticated
     */
    private UserCredentials authenticate(ContainerRequest request) {
        //TODO allow only secure https connections
        //		logger.info("Connection secure? " + isSecure());
        LOGGER.info("Request to: " + uriInfo.getAbsolutePath() + " | method:  " + request.getMethod());

        // Extract authentication credentials
        String authentication = request.getHeaderValue(ContainerRequest.AUTHORIZATION);
        if (authentication == null) {
            return UserCredentials.PUBLIC_USER;
        }
        try {
            if (authentication.startsWith("Basic ")) {
                return getBasicCredentials(authentication.substring("Basic ".length()));
            } else if (authentication.startsWith("Bearer ")) {
                // this is the recommended OAuth 2.0 method
                return getBearerCredentials(authentication.substring("Bearer ".length()));
            } else {
                throw new MappableContainerException(new AuthenticationException(
                        "Only HTTP Basic and OAuth 2.0 Bearer authentications are supported\r\n", REALM));
            }
        } catch (IllegalArgumentException e) {
            throw new MappableContainerException(new AuthenticationException(e.getMessage(), REALM));
        }
    }


    /**
     * Find user credentials for a OAuth Bearer token.
     * 
     * @param accessToken
     *            access token
     * @return user credentials
     */
    public UserCredentials getBearerCredentials(String accessToken) {
        OAuthManager manager = new OAuthManager();
        AccessToken token = manager.getAccessToken(accessToken);
        if (token == null) {
            return getBasicCredentials(accessToken);
        }
        return token.getUser();
    }


    /**
     * Find user credentials for a Base64 encoded token. This is a deprecated, not very secure method.
     * 
     * @param authentication
     *            authentication token
     * @return user credentials
     */
    public UserCredentials getBasicCredentials(String authentication) {
        String[] values = new String(Base64.base64Decode(authentication)).split(":");
        if (values.length != 2) {
            throw new MappableContainerException(new AuthenticationException("Incorrect login/password\r\n", REALM));
        }
        return new UserCredentials(values[0], values[1]);
    }


    public boolean isSecure() {
        return "https".equals(uriInfo.getRequestUri().getScheme());
    }

}
