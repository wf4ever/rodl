package pl.psnc.dl.wf4ever;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pl.psnc.dl.wf4ever.auth.SecurityFilter;
import pl.psnc.dl.wf4ever.auth.UserCredentials;
import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class TestServletContainer
	extends ServletContainer
{

	private static final long serialVersionUID = -5162327450787936293L;

	static {
		DigitalLibraryFactory.loadDigitalLibraryConfiguration("connection.properties");
	}


	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		SecurityFilter filter = new SecurityFilter();
		String authentication = request.getHeader(ContainerRequest.AUTHORIZATION);
		UserCredentials creds = UserCredentials.PUBLIC_USER;
		if (authentication != null) {
			if (authentication.startsWith("Basic ")) {
				creds = filter.getBasicCredentials(authentication.substring("Basic ".length()));
			}
			else if (authentication.startsWith("Bearer ")) {
				creds = filter.getBearerCredentials(authentication.substring("Bearer ".length()));
			}
		}
		try {
			UserProfile user = DigitalLibraryFactory.getDigitalLibrary(creds).getUserProfile();
			request.setAttribute(Constants.USER, user);
		}
		catch (DigitalLibraryException | NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		super.service(request, response);
	}
}
