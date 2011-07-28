package pl.psnc.dl.wf4ever.openid.wicket;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.openid4java.discovery.DiscoveryInformation;

import pl.psnc.dl.wf4ever.openid.model.RegistrationModel;
import pl.psnc.dl.wf4ever.openid.model.RegistrationService;

/**
 * This class represents the OpenIdRegistrationSavePage, which  
 * receives the authentication response from the OpenID Provider (OP)
 * and verifies the response with openid4java. It also provides a way to save 
 * the information retrieved from the OP somewhere (well, a hook for that has 
 * been provided).
 *  
 * @author J Steven Perry
 * @author http://makotoconsulting.com
 *
 */
public class OpenIdRegistrationSavePage
	extends WebPage
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Default Constructor
	 */
	public OpenIdRegistrationSavePage()
	{
		this(new PageParameters());
	}


	/**
	 * Constructor called by Wicket with an auth response (since the response
	 * has parameters associated with it... LOTS of them!). And, by the way,
	 * the auth response is the Request for this classl (not to be confusing).
	 * 
	 * @param pageParameters The request parameters (which are the response
	 *  parameters from the OP).
	 */
	public OpenIdRegistrationSavePage(PageParameters pageParameters)
	{
		RegistrationModel registrationModel = new RegistrationModel();
		if (!pageParameters.isEmpty()) {
			//
			// If this is a return trip (the OP will redirect here once authentication
			/// is compelete), then verify the response. If it looks good, send the
			/// user to the RegistrationSuccessPage. Otherwise, display a message.
			//
			String isReturn = pageParameters.get("is_return").toString();
			if ("true".equals(isReturn)) {
				//
				// Grab the session object so we can let openid4java do verification.
				//
				Session session = getSession();
				DiscoveryInformation discoveryInformation = (DiscoveryInformation) session
						.getAttribute(RegistrationService.DISCOVERY_INFORMATION);

				//
				// Delegate to the Service object to do verification. It will return
				/// the RegistrationModel to use to display the information that was
				/// retrieved from the OP about the User-Supplied identifier. The
				/// RegistrationModel reference will be null if there was a problem
				/// (check the logs for more information if this happens).
				//
				registrationModel = RegistrationService.processReturn(
					discoveryInformation, pageParameters,
					RegistrationService.getReturnToUrl());
				if (registrationModel == null) {
					//
					// Oops, something went wrong. Display a message on the screen.
					/// Check the logs for more information.
					//
					error("Open ID Confirmation Failed. No information was retrieved from the OpenID Provider. You will have to enter all information by hand into the text fields provided.");
				}
			}
		}
		add(new OpenIdRegistrationInformationDisplayForm("form",
				registrationModel));
	}

	/**
	 * The Form subclass for this page.
	 *  
	 * @author J Steven Perry
	 * @author http://makotoconsulting.com
	 */
	public static class OpenIdRegistrationInformationDisplayForm
		extends Form<RegistrationModel>
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = -1045594133856989168L;


		/**
		 * Constructor, takes the wicket:id value (probably "form") and the
		 * RegistrationModel object to be used as the model for the form.
		 * 
		 * @param id
		 * @param registrationModel
		 */
		@SuppressWarnings("serial")
		public OpenIdRegistrationInformationDisplayForm(String id,
				RegistrationModel registrationModel)
		{
			super(id, new CompoundPropertyModel<RegistrationModel>(
					registrationModel));
			//
			TextField<String> openId = new TextField<String>("openId");
			openId.setEnabled(false);
			add(openId);
			//
			TextField<String> fullName = new RequiredTextField<String>(
					"fullName");
			add(fullName);
			//
			TextField<String> emailAddress = new RequiredTextField<String>(
					"emailAddress");
			add(emailAddress);
			//
			TextField<String> country = new TextField<String>("country");
			add(country);
			//
			TextField<String> language = new TextField<String>("language");
			add(language);
			//
			Button saveButton = new Button("saveButton") {

				public void onSubmit()
				{
					// Store registration in the DB
					if (saveRegistrationInfo()) {
						info("Registration Info saved.");
					}
					else {
						error("Registration Info could not be saved!");
					}
				}
			};
			add(saveButton);
		}


		/**
		 * This is a hook where you would place code to save the registration
		 * information.
		 * 
		 * @return returns true if the information was successfully saved, false
		 *  otherwise.
		 */
		private boolean saveRegistrationInfo()
		{
			// TODO: Fill in implementation to save code to the DB
			return true;
		}
	}
}
