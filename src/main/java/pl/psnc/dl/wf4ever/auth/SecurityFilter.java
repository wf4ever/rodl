package pl.psnc.dl.wf4ever.auth;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.Constants;
import pl.psnc.dl.wf4ever.common.UserProfile;
import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
import pl.psnc.dl.wf4ever.connection.SemanticMetadataServiceFactory;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;

import com.sun.jersey.api.container.MappableContainerException;
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
            ROSRService.DL.set(DigitalLibraryFactory.getDigitalLibrary(creds));
            UserProfile user = ROSRService.DL.get().getUserProfile(creds.getUserId());
            ROSRService.SMS.set(SemanticMetadataServiceFactory.getService(user));
            httpRequest.setAttribute(Constants.USER, user);

            //TODO in here should go access rights control, based on dLibra for example
            //            if (!request.getMethod().equals("GET") && user.getRole() == UserProfile.Role.PUBLIC) {
            //                throw new AuthenticationException("Only authenticated users can do that.", SecurityFilter.REALM);
            //            }
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
            return UserCredentials.getPublicUserCredentials();
        }
        if (authentication.equals(DigitalLibraryFactory.getAdminToken())) {
            return UserCredentials.getAdminUserCredentials();
        }
        try {
            if (authentication.startsWith("Bearer ")) {
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
     * @param tokenValue
     *            access token
     * @return user credentials
     */
    public UserCredentials getBearerCredentials(String tokenValue) {
        AccessToken accessToken = AccessToken.findByValue(tokenValue);
        if (accessToken != null) {
            accessToken.setLastUsed(new Date());
            accessToken.save();
            return accessToken.getUser();
        } else {
            throw new MappableContainerException(new AuthenticationException("Incorrect access token\r\n", REALM));
        }
    }


    public boolean isSecure() {
        return "https".equals(uriInfo.getRequestUri().getScheme());
    }

}
