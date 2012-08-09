package pl.psnc.dl.wf4ever;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pl.psnc.dl.wf4ever.auth.AuthenticationException;
import pl.psnc.dl.wf4ever.auth.SecurityFilter;
import pl.psnc.dl.wf4ever.auth.UserCredentials;
import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
import pl.psnc.dl.wf4ever.connection.SemanticMetadataServiceFactory;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dl.wf4ever.rosrs.ROSRService;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class TestServletContainer extends ServletContainer {

    private static final long serialVersionUID = -5162327450787936293L;

    static {
        DigitalLibraryFactory.loadDigitalLibraryConfiguration("connection.properties");
    }


    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SecurityFilter filter = new SecurityFilter();
        String authentication = request.getHeader(ContainerRequest.AUTHORIZATION);
        UserCredentials creds = UserCredentials.PUBLIC_USER;
        if (authentication != null) {
            if (authentication.startsWith("Basic ")) {
                creds = filter.getBasicCredentials(authentication.substring("Basic ".length()));
            } else if (authentication.startsWith("Bearer ")) {
                creds = filter.getBearerCredentials(authentication.substring("Bearer ".length()));
            }
        }
        try {
            UserProfile user = DigitalLibraryFactory.getDigitalLibrary(creds).getUserProfile();
            //TODO in here should go access rights control, based on dLibra for example
            if (!request.getMethod().equals("GET") && user.getRole() == UserProfile.Role.PUBLIC) {
                throw new AuthenticationException("Only authenticated users can do that.", SecurityFilter.REALM);
            }
            request.setAttribute(Constants.USER, user);
            //HACK FIXME
            UserCredentials superUserCreds = new UserCredentials("wfadmin", "wfadmin!!!");
            ROSRService.DL.set(DigitalLibraryFactory.getDigitalLibrary(superUserCreds));
            ROSRService.SMS.set(SemanticMetadataServiceFactory.getService(user));

        } catch (DigitalLibraryException | NotFoundException | ClassNotFoundException | NamingException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        super.service(request, response);
    }
}
