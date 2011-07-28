package pl.psnc.dl.wf4ever.openid.wicket;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * Quick and dirty little class that serves as the requisite Application
 * class for this Wicket application.
 * 
 * @author J Steven Perry
 * @author http://makotoconsulting.com
 */
public class AuthenticationApplication
	extends WebApplication
{

	public AuthenticationApplication()
	{
		super();
	}


	@Override
	public void init()
	{
		super.init();
		//
		// Mount the classes. It makes the URLs so much cleaner-looking
		//
		mountPage("/register", OpenIdRegistrationPage.class);
		mountPage("/save", OpenIdRegistrationSavePage.class);
	}


	/**
	 * Return the "Home" page used by the application. Wicket will redirect
	 * here if you don't explicitly supply a Page destination.
	 */
	public Class< ? extends WebPage> getHomePage()
	{
		return OpenIdRegistrationPage.class;
	}

}
