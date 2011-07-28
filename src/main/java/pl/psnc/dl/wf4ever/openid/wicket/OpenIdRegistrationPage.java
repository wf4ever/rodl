package pl.psnc.dl.wf4ever.openid.wicket;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.AuthRequest;

import pl.psnc.dl.wf4ever.openid.model.RegistrationModel;
import pl.psnc.dl.wf4ever.openid.model.RegistrationService;

/**
 * This class is used to gather information from the user to register them
 * with the Site. This page is used to get one piece of information: the
 * user's OpenID. The user enters their OpenID, and the RegistrationService
 * takes over to verify their ID - redirecting them to the OpenID Provider (OP)
 * to be signed in, if necessary - then redirects them back here. 
 * If the authentication succeeds, then the user is taken to the 
 * RegistrationSuccessPage, where they can then look at the information 
 * retrieved from the OP, make any changes to the information and then save
 * it to the DB (this part is left as an exercise to the reader, since it is
 * beyond the scope of the article).
 * 
 * @author J Steven Perry
 * @author http://makotoconsulting.com
 *
 */
public class OpenIdRegistrationPage
	extends WebPage
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8975579933617712699L;


	public OpenIdRegistrationPage()
	{
		this(new PageParameters());
	}

	private String returnToUrl;


	public OpenIdRegistrationPage(PageParameters pageParameters)
	{
		returnToUrl = RegistrationService.getReturnToUrl();
		//
		// If this is a new call, then use the OpenIdRegistrationForm, which
		/// only allows the user to enter their OpenID.
		//
		add(new OpenIdRegistrationForm("form", this, returnToUrl));
	}

	/**
	 * The Form used for this Page.
	 * 
	 * @author J Steven Perry
	 * @author http://makotoconsulting.com
	 */
	public static class OpenIdRegistrationForm
		extends Form<RegistrationModel>
	{

		private static final long serialVersionUID = 3828134783479387778L;


		public OpenIdRegistrationForm(String id,
				final OpenIdRegistrationPage owningPage, String returnToUrl)
		{
			this(id, owningPage, returnToUrl, new RegistrationModel());
		}


		public OpenIdRegistrationForm(String id,
				final OpenIdRegistrationPage owningPage,
				final String returnToUrl, final RegistrationModel formModel)
		{

			super(id);
			//
			setModel(new CompoundPropertyModel<RegistrationModel>(formModel));
			//
			TextField<String> openId = new RequiredTextField<String>("openId");
			openId.setLabel(new Model<String>("Your Open ID"));
			add(openId);
			// This is the "business end" of making the authentication request.
			/// The sequence of interaction with the OP is really hidden from us
			/// here by using RegistrationService.
			Button confirmOpenIdButton = new Button("confirmOpenIdButton") {

				private static final long serialVersionUID = -723600550506568627L;

				public void onSubmit()
				{
					// Delegate to Open ID code
					String userSuppliedIdentifier = formModel.getOpenId();
					DiscoveryInformation discoveryInformation = RegistrationService
							.performDiscoveryOnUserSuppliedIdentifier(userSuppliedIdentifier);
					// Store the disovery results in session.
					Session session = owningPage.getSession();
					session.setAttribute(
						RegistrationService.DISCOVERY_INFORMATION,
						discoveryInformation);
					// Create the AuthRequest
					AuthRequest authRequest = RegistrationService
							.createOpenIdAuthRequest(discoveryInformation,
								returnToUrl);
					// Now take the AuthRequest and forward it on to the OP
					IRequestHandler reqHandler = new RedirectRequestHandler(
							authRequest.getDestinationUrl(true));
					getRequestCycle().scheduleRequestHandlerAfterCurrent(
						reqHandler);
				}
			};
			add(confirmOpenIdButton);
		}
	}
}
